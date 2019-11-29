package com.ydj.ddd.demo.core.promotion.infrastructure.repository;

import com.jd.b.promotion.infrastructure.repository.assembler.PromotionAdDataProcessResult;
import com.jd.b.promotion.infrastructure.repository.assembler.PromotionAdDataWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

;

@Repository
public class PromotionAdSaveOrUpdateRepo {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionAdSaveOrUpdateRepo.class);

    @Resource
    private AdMapper adMapper;

    @Resource
    private PromotionProductMapper promotionProductMapper;

    @Resource
    private PlanMapper planMapper;

    @Resource
    private PlanItemMapper planItemMapper;

    @Resource
    private PlanDateMapper planDateMapper;

    @Resource
    private MaterialMapper  materialMapper;

    @Resource
    private MaterialElementMapper  materialElementMapper;

    @Resource
    private PlanBoothRelationMapper  planBoothRelationMapper;

    @Resource
    private PlanMaterialRelationMapper  planMaterialRelationMapper;

    @Resource
    private BoothMapper  boothMapper;

    @Resource
    private BidKeywordMapper bidKeywordMapper;

    @Resource
    private TargettingKeywordsMapper targettingKeywordsMapper;

    @Transactional
    public PromotionAdDataProcessResult savePromotionAd(PromotionAdDataWrapper promotionAdDataWrapper){
        LOGGER.info("savePromotionAd()->promotionAdDataWrapper = {}", promotionAdDataWrapper);

        Ad ad = promotionAdDataWrapper.getAd();
        this.adMapper.insert(ad);

        PromotionProduct promotionProduct = promotionAdDataWrapper.getPromotionProduct();
        promotionProduct.setAdId(ad.getId());
        this.promotionProductMapper.addPromotionProduct(promotionProduct);

        PromotionAdDataProcessResult promotionAdDataProcessResult = new PromotionAdDataProcessResult(ad.getId());
        if (promotionAdDataWrapper.isHasBroadcastPromotion()){
           this.addNewBroadcastPromotion(promotionAdDataWrapper.getBroadcastPromotionData(),ad,promotionAdDataProcessResult);
        }

        if (promotionAdDataWrapper.isHasTrafficPromotion()) {
            promotionAdDataWrapper.getTrafficPromotionDataList().forEach(
                    one -> this.addNewTrafficPromotion(one,ad,promotionProduct, promotionAdDataProcessResult)
            );
        }

        return promotionAdDataProcessResult;
    }

    @Transactional
    public PromotionAdDataProcessResult updatePromotionAd(PromotionAdDataWrapper promotionAdDataWrapper){
        LOGGER.info("updatePromotionAd()->promotionAdDataWrapper = {}", promotionAdDataWrapper);

        Ad ad = promotionAdDataWrapper.getAd();
        PromotionProduct promotionProduct = promotionAdDataWrapper.getPromotionProduct();

        PromotionAdDataProcessResult promotionAdDataProcessResult = new PromotionAdDataProcessResult(ad.getId());

        this.adMapper.updateAdName(ad.getId(), ad.getName());
        this.promotionProductMapper.updateName(ad.getId(), ad.getName(),promotionProduct.getProductName());

        if (promotionAdDataWrapper.isHasBroadcastPromotion()){
            PromotionAdDataWrapper.BroadcastPromotionData broadcastPromotionData = promotionAdDataWrapper.getBroadcastPromotionData();
            Plan plan = broadcastPromotionData.getPlan();
            if (plan.getId() < 1){//新增
                this.addNewBroadcastPromotion(broadcastPromotionData,ad,promotionAdDataProcessResult);
            }else {//修改
                this.updateOldBroadcastPromotion(broadcastPromotionData);
            }
        }

        if (promotionAdDataWrapper.isHasNeed2DeletePromotions()){
            promotionAdDataWrapper.getNeed2DeletePromotions().stream().forEach(
                    planId -> {
                        this.planMapper.updateStatus(planId, 1, new Date());
                        this.bidKeywordMapper.deleteByPlanId(planId);
                    }
            );
        }

        if (promotionAdDataWrapper.isHasTrafficPromotion()) {
            promotionAdDataWrapper.getTrafficPromotionDataList().forEach(one ->{
                Plan plan = one.getPlan();
                if (plan.getId() < 1 ){//新增
                    this.addNewTrafficPromotion(one,ad,promotionProduct, promotionAdDataProcessResult);
                }else {//修改
                    this.updateOldTrafficPromotion(one,promotionProduct, promotionAdDataProcessResult);
                }
            });
        }

        return promotionAdDataProcessResult;
    }

    private void addNewTrafficPromotion(PromotionAdDataWrapper.TrafficPromotionData one, Ad ad,
                                        PromotionProduct promotionProduct, PromotionAdDataProcessResult promotionAdDataProcessResult){
        Plan plan = one.getPlan();
        plan.setAdId(ad.getId());
        this.planMapper.addPlan(plan);

        Long planId = plan.getId();

        PlanDate planDate = one.getPlanDate();
        planDate.setPlanId(planId);
        this.planDateMapper.addPlanDate(planDate);

        if (one.isHasMaterial()){
            Material material = one.getMaterial();
            this.materialMapper.insert(material);

            MaterialElement materialElement = one.getMaterialElement();
            if (Objects.nonNull(materialElement)){
                materialElement.setMaterialId( material.getId());
                this.materialElementMapper.insert(materialElement);
            }

            PlanMaterialRelation planMaterialRelation = one.getPlanMaterialRelation();
            planMaterialRelation.setPlanId(planId);
            planMaterialRelation.setMaterialId(material.getId());
            this.planMaterialRelationMapper.insert(planMaterialRelation);

            if (material.getMaterialSource() != 1){
                promotionAdDataProcessResult.addNeed2AuditPlanId(planId);
            }
        }

        List<BidKeyword> bidKeywordList = one.getNewBidKeywords();
        if (CollectionUtils.isNotEmpty(bidKeywordList) ) {
            bidKeywordList.forEach(src->src.setPlanId(planId));
            this.bidKeywordMapper.batchInsertBidKeyword(bidKeywordList);
        }

        List<TargettingKeywords> targetKeywords = one.getTargetKeywords();
        if (CollectionUtils.isNotEmpty(targetKeywords) ) {
            targetKeywords.forEach(src->src.setPlanId(planId));
            this.targettingKeywordsMapper.batchInsertTargettingKeywords(targetKeywords);
        }

        PlanBoothRelation planBoothRelation = one.getPlanBoothRelation();
        planBoothRelation.setPlanId(planId);
        this.planBoothRelationMapper.insert(planBoothRelation);
        this.updateBoothCount(planBoothRelation.getBoothCode(), planBoothRelation.getCount());

        promotionAdDataProcessResult.addTrafficPromotion(plan);
    }

    private void updateOldTrafficPromotion(PromotionAdDataWrapper.TrafficPromotionData one,
                                           PromotionProduct promotionProduct, PromotionAdDataProcessResult promotionAdDataProcessResult) {
        Plan plan = one.getPlan();
        PlanDate planDate = one.getPlanDate();
        this.planDateMapper.updatePlanDate(planDate);

        if (one.isHasMaterial()){
            Material material = one.getMaterial();
            material.setLink(this.generateLink(promotionProduct.getSpuid(), plan));
            materialMapper.updateById(material);

            MaterialElement materialElement = one.getMaterialElement();
            if (Objects.nonNull(materialElement)){
                materialElement.setMaterialId(material.getId());
                materialElementMapper.deleteByMaterialId(material.getId());
                materialElementMapper.insert(materialElement);
            }

            if (material.getMaterialSource() != 1){
                promotionAdDataProcessResult.addNeed2AuditPlanId(plan.getId());
            }
        }

        List<BidKeyword> bidKeywordList = one.getNewAndOldBidKeywords().getLeft();
        if (CollectionUtils.isNotEmpty(bidKeywordList) ) {
            bidKeywordList.forEach(bidKeyword -> bidKeyword.setPlanId(plan.getId()));
            this.bidKeywordMapper.batchInsertBidKeyword(bidKeywordList);
        }

        bidKeywordList = one.getNewAndOldBidKeywords().getRight();
        if (CollectionUtils.isNotEmpty(bidKeywordList) ) {
            bidKeywordList.forEach(bidKeyword -> {
                bidKeyword.setPlanId(plan.getId());
                this.bidKeywordMapper.updateByKeywordAndPlanId(bidKeyword);
            });
        }

        List<TargettingKeywords> targetKeywords = one.getTargetKeywords();
        if (CollectionUtils.isNotEmpty(targetKeywords) ) {
            targetKeywords.forEach(src->src.setPlanId(plan.getId()));
            this.targettingKeywordsMapper.batchInsertTargettingKeywords(targetKeywords);
        }

        planMapper.updateByPlanId(plan);
        planMapper.updateStatus(plan.getId(), plan.getState(), new Date());
        promotionAdDataProcessResult.addTrafficPromotion(plan);
    }


    private void addNewBroadcastPromotion(PromotionAdDataWrapper.BroadcastPromotionData one, Ad ad,PromotionAdDataProcessResult promotionAdDataProcessResult){
        Plan plan = one.getPlan();
        PlanDate planDate = one.getPlanDate();
        PlanItem planItem = one.getPlanItem();

        plan.setAdId(ad.getId());
        this.planMapper.addPlan(plan);

        Long planId = plan.getId();
        planDate.setPlanId(planId);
        planItem.setPlanId(planId);
        this.planDateMapper.addPlanDate(planDate);
        this.planItemMapper.AddPlanItem(planItem);
        this.planItemMapper.updateInventoryByPlanId(planId);
        promotionAdDataProcessResult.setBroadcastPromotionPlanId(planId);
    }

    private void updateOldBroadcastPromotion(PromotionAdDataWrapper.BroadcastPromotionData one){
        Plan plan = one.getPlan();
        PlanDate planDate = one.getPlanDate();
        PlanItem planItem = one.getPlanItem();
        this.planMapper.updatePlanByEdition(plan.getId(), plan.getBudget(), 0, plan.getState());
        this.planDateMapper.updatePlanDate(planDate);
        this.planItemMapper.updatePlanItem(planItem);
    }

    private void updateBoothCount(String boothCode, int count) {
        Booth booth = new Booth();
        booth.setBoothCode(boothCode);
        booth.setCount(count);
        int affectRow = boothMapper.updateBoothCount(booth);
        if (affectRow == 0) {
            throw new BusinessException(ExceptionCodeEnum.SYSTEM_ERROR);
        }
    }
}
