package com.ydj.ddd.demo.core.promotion.domain.factory;

import com.ydj.ddd.demo.core.promotion.domain.model.*;
import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PromotionAdAggregateFactory4Update extends AbstractPromotionAdAggregateTemplateFactory implements PromotionAdAggregateFactory {

    @Resource
    private PlanMapper planMapper;

    @Resource
    private PlanItemMapper planItemMapper;

    @Resource
    private MaterialElementMapper materialElementMapper;

    @Resource
    private BidKeywordMapper bidKeywordMapper;

    @Override
    protected AdvertiserInfo checkAndGetAdvertiser(long userId, long adId){
        long sellerId ;
        if (userId > 0) {//商家操作时
            String mwsid = MDC.get("mwsid");
            if (mwsid == null) {
                throw new BusinessException(ExceptionCodeEnum.INVALID_MERCHANT);
            }
            sellerId = Long.valueOf(mwsid);
        } else {//后台运营操作时
            PromotionProduct promotionProduct = promotionProductMapper.queryProductByAdId(adId);
            if (Objects.isNull(promotionProduct)) {
                throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
            }
            sellerId = promotionProduct.getSellerId();
        }

        AdvertiserInfo advertiserInfo = this.getAdvertiserInfo(sellerId, userId);
        return advertiserInfo;
    }

    @Override
    protected SelectedProduct checkAndGetSelectedProduct(final MerchantPromotionRequest context){
        long spuId = context.spuId;
        if (spuId <= 0) {
            throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        SelectedProduct selectedProduct = new SelectedProduct(spuId,context.productName,context.productPrice,context.productCategoryId,context.productCategory);
        return selectedProduct;
    }

    @Override
    protected AbstractPromotionPlan buildBroadcastPromotion(final MerchantPromotionRequest context){
        BroadcastPromotionDto broadcastPromotionDto = context.broadcastPromotionDto;
        if (Objects.isNull(broadcastPromotionDto) || context.broadcastPromotionFlag == 0){
            return null;
        }

        BroadcastPromotionPlan broadcastPromotionPlan;
        if (broadcastPromotionDto.planId < 1){//新增
            broadcastPromotionPlan = this.buildNewBroadcastPromotionPlan(broadcastPromotionDto, context.productCategoryId);
            return broadcastPromotionPlan;
        }

        broadcastPromotionPlan = this.buildOldBroadcastPromotionPlan(broadcastPromotionDto,context.productCategoryId);
        return broadcastPromotionPlan;
    }

    @Override
    protected List<AbstractPromotionPlan> buildTrafficPromotion(MerchantPromotionRequest context) {
        if (CollectionUtils.isEmpty(context.trafficPromotionDtos)) {
            return null;
        }

        this.check4FastFail(context);
        boolean isShowAdTag = this.productItemFacade.isShowAdTag(context.spuId);
        Long auditTime = auditService.getAuditLimitTime();

        List<AbstractPromotionPlan> trafficPromotionPlanList = context.trafficPromotionDtos.stream()
                .map(one -> {
                        long planId = one.planId;
                        long productCategoryId = context.productCategoryId;
                        if (planId > 0 && one.operation == 1){//需要删除
                                return new TrafficPromotionPlan(planId);
                            }else if (planId == 0){//新增加
                               return this.buildNewTrafficPromotionPlan(one, productCategoryId,isShowAdTag,auditTime);
                            }else{//修改老的
                                return this.buildOldTrafficPromotionPlan(one, productCategoryId,isShowAdTag,auditTime);
                            }
                        }
                )
                .collect(Collectors.toList());

        return trafficPromotionPlanList;
    }


    private TrafficPromotionPlan buildOldTrafficPromotionPlan(final TrafficPromotionDto one, long productCategoryId, boolean isShowAdTag, long auditTime){
        Plan oldPlan = planMapper.queryPlan(one.planId);
        if (Objects.isNull(oldPlan)) {
            throw new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        int auditStatus = oldPlan.getAuditStatus();
        Integer oldPlanState = oldPlan.getState();
        double promotionBudgetConsumption = oldPlan.getConsumption() / 100.0;
        if (one.promotionBudget <= promotionBudgetConsumption) {
            throw new BusinessException(ExceptionCodeEnum.BUDGET_LESS_CONSUMPTION);
        }

        PromotionCycle promotionCycle = new PromotionCycle(one.unlimited, one.promotionStart, one.promotionEnd);
        AdBoothInfo adBoothInfo = this.getAdBoothInfo(one.boothCode);

        AdMaterialInfo adMaterialInfo = null;
        MaterialElement oldME = this.materialElementMapper.getMaterialElementByPlanId(one.planId);
        if (Objects.nonNull(one.material ) && Objects.nonNull(oldME) && !Objects.equals(oldME.getUrl(),one.material.url)) {
            adMaterialInfo = this.buildMaterialInfo(one,isShowAdTag);
        }

        SelectedKeyword selectedKeyword = this.buildSelectedKeyword(one,adBoothInfo,productCategoryId);
        TrafficPromotionPlan trafficPromotionPlan = new TrafficPromotionPlan(one.planId,one.promotionBudget,promotionBudgetConsumption,
                promotionCycle, one.clickPrice,adBoothInfo, adMaterialInfo,selectedKeyword,one.createType,auditTime,auditStatus,oldPlanState);
        return trafficPromotionPlan;
    }

    @Override
    protected SelectedKeyword buildSelectedKeyword(TrafficPromotionDto trafficPromotionDto, AdBoothInfo adBoothInfo, long productCategoryId) {
        SelectedKeyword selectedKeyword = super.buildSelectedKeyword(trafficPromotionDto, adBoothInfo, productCategoryId);
        if (Objects.isNull(selectedKeyword)){
            return selectedKeyword;
        }

        long planId = trafficPromotionDto.planId;
        List<BidKeyword> oldBidKeywordList = this.bidKeywordMapper.queryKeywordListByPlanId(planId);
        if ( CollectionUtils.isNotEmpty(oldBidKeywordList) ) {
            List<BidKeywordInfo> oldBidKeywordInfoList = oldBidKeywordList.stream()
                    .map(
                            one -> new BidKeywordInfo(planId,one.getCreateType(), one.getKeyword(), one.getPrice(), one.getType(), one.getState())
                    )
                    .collect(Collectors.toList());
            selectedKeyword.setOldBidKeywordInfoList(oldBidKeywordInfoList);
        }
        return selectedKeyword;
    }


    private BroadcastPromotionPlan buildOldBroadcastPromotionPlan(final BroadcastPromotionDto one,long productCategoryId){
        PlanItem oldPlanItem = planItemMapper.queryOneByPlanId(one.planId);
        if (Objects.isNull(oldPlanItem)) {
            throw new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        PromotionCycle promotionCycle = new PromotionCycle(one.unlimited,one.promotionStart,one.promotionEnd);

        double brokerageRatio = one.brokerageRatio;
        double promotionBudget = one.promotionBudget;
        double promotionBudgetConsumption = this.planMapper.getPlanConsumption(one.planId) / 100.0;

        if (promotionBudget <= promotionBudgetConsumption) {
            throw new BusinessException(ExceptionCodeEnum.BUDGET_LESS_CONSUMPTION);
        }

        this.checkBrokerageRatio(productCategoryId, brokerageRatio);
        long profitId = anchorProfitQueryService.getFixProfitId(brokerageRatio);
        BroadcastPromotionPlan broadcastPromotionPlan = new BroadcastPromotionPlan(one.planId, promotionBudget,promotionBudgetConsumption,promotionCycle, brokerageRatio,profitId,oldPlanItem.getFlag(),oldPlanItem.getStatus());
        return broadcastPromotionPlan;
    }
}
