package com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler;

import com.ydj.ddd.demo.core.promotion.domain.model.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PromotionAdConvert2PoHelper {

    /**广告ID*/
    private final long adId;

    /**广告名称*/
    private final String adName;

    /**广告主信息*/
    private final AdvertiserInfo advertiserInfo;

    /**所选择的需要推广的商品*/
    private final SelectedProduct selectedProduct;

    private final PromotionAdAggregate adAggregate;

    public PromotionAdConvert2PoHelper(PromotionAdAggregate adAggregate) {
        this.adAggregate = adAggregate;

        this.adId = adAggregate.getAdId();
        this.adName = adAggregate.getAdName();
        this.advertiserInfo = adAggregate.getAdvertiserInfo();
        this.selectedProduct = adAggregate.getSelectedProduct();
    }

    public PromotionAdDataWrapper convert2PoData() {

        PromotionAdDataWrapper promotionAdDataWrapper = new com.jd.b.promotion.infrastructure.repository.assembler.PromotionAdDataWrapper();
        promotionAdDataWrapper.setAd(this.convert2Ad());
        promotionAdDataWrapper.setPromotionProduct(this.convert2PromotionProduct());

        if (adAggregate.isHasBroadcastPromotion()){
            BroadcastPromotionPlan broadcastPromotion = (BroadcastPromotionPlan)adAggregate.getBroadcastPromotionPlan();

            long promotionPlanId = broadcastPromotion.getPromotionPlanId();
            Plan plan = this.convert2Plan(broadcastPromotion);
            PlanDate planDate = this.convert2PlanDate(promotionPlanId,broadcastPromotion.getPromotionCycle());
            PlanItem planItem = this.convert2PlanItem(broadcastPromotion);

           PromotionAdDataWrapper.BroadcastPromotionData broadcastPromotionData = new PromotionAdDataWrapper.BroadcastPromotionData(plan,planDate,planItem);
            promotionAdDataWrapper.setBroadcastPromotionData(broadcastPromotionData);
        }

        if (adAggregate.isHasTrafficPromotion()) {
            List<AbstractPromotionPlan> trafficPromotionPlanList = adAggregate.getTrafficPromotionPlanList();

            for (AbstractPromotionPlan one : trafficPromotionPlanList){
                TrafficPromotionPlan trafficPromotion = (TrafficPromotionPlan)one;

                long promotionPlanId = trafficPromotion.getPromotionPlanId();
                if (trafficPromotion.isNeed2Delete()){
                    promotionAdDataWrapper.addNeed2DeletePromotion(promotionPlanId);
                    continue;
                }

               PromotionAdDataWrapper.TrafficPromotionData trafficPromotionData = new PromotionAdDataWrapper.TrafficPromotionData();
                trafficPromotionData.setPlan(this.convert2Plan(trafficPromotion));
                trafficPromotionData.setPlanDate(this.convert2PlanDate(promotionPlanId,trafficPromotion.getPromotionCycle()));
                trafficPromotionData.setPlanBoothRelation(this.convert2PlanBoothRelation(trafficPromotion.getAdBoothInfo()));
                trafficPromotionData.setTargetKeywords(trafficPromotion.getTargetKeywords());
                trafficPromotionData.setNewBidKeywords(trafficPromotion.getNewBidKeywords(advertiserInfo.getId(), selectedProduct.getSpuId()));
                trafficPromotionData.setNewAndOldBidKeywords(trafficPromotion.getNewAndOldBidKeyword(advertiserInfo.getId(),selectedProduct.getSpuId()));

                if (trafficPromotion.isHasMaterial()){
                    AdMaterialInfo adMaterialInfo = trafficPromotion.getAdMaterialInfo();
                    trafficPromotionData.setMaterial(this.convert2Material(adMaterialInfo));
                    trafficPromotionData.setMaterialElement(this.convert2MaterialElement(adMaterialInfo));
                    trafficPromotionData.setPlanMaterialRelation(this.convert2PlanMaterialRelation());
                }

                promotionAdDataWrapper.addTrafficPlan(trafficPromotionData);
            }
        }
        return promotionAdDataWrapper;
    }

    private Ad convert2Ad() {
        Ad nowAd = new Ad();
        nowAd.setId(adId);
        nowAd.setName(adName);
        nowAd.setAdType(2);
        nowAd.setOwnerId(advertiserInfo.getId());
        nowAd.setStatus(1);
        nowAd.setStartTime(0);
        nowAd.setEndTime(0);
        nowAd.setDuration(0);
        nowAd.setAdIdWrapper("0");
        nowAd.setPutType(1);
        nowAd.setOwnerName(advertiserInfo.getName());
        nowAd.setOperator(advertiserInfo.getUserId()+"");
        nowAd.setPromotion(adAggregate.getPromotionType());
        nowAd.setSystem(1);
        return nowAd;
    }

    private PromotionProduct convert2PromotionProduct(){
        PromotionProduct promotionProduct = new PromotionProduct();
        promotionProduct.setAdId(adId);
        promotionProduct.setSpuid(selectedProduct.getSpuId());
        promotionProduct.setProductName(selectedProduct.getProductName());
        promotionProduct.setPrice((long) selectedProduct.getProductPrice()*100);
        promotionProduct.setCategoryId(selectedProduct.getProductCategoryId());
        promotionProduct.setCategoryName(selectedProduct.getProductCategory());
        promotionProduct.setSellerId(advertiserInfo.getMerchantId());
        promotionProduct.setSellerName(advertiserInfo.getName());
        promotionProduct.setAdName(adName);
        return promotionProduct;
    }

    private Plan convert2Plan(AbstractPromotionPlan promotionPlan) {
        Plan plan = new Plan();
        plan.setId(promotionPlan.getPromotionPlanId());
        plan.setAdId(adId);
        plan.setOwnerId(advertiserInfo.getId());
        plan.setState(adAggregate.getPlanStatus(promotionPlan).getValue());
        plan.setBudget(promotionPlan.getPromotionBudget());
        plan.setPlanIdWrapper(promotionPlan.getPlanIdWrapper());
        plan.setGmtModify(new Date());

        if (promotionPlan instanceof BroadcastPromotionPlan) {
            plan.setWeight(0);
            plan.setType(1);// 0-直投广告  1-直播推广  2-流量推广
            plan.setSellMode(0);//1-CPM 2-CPC 3-CPD 4-CPA 5-CPS
            plan.setSellPrice(0.0);
            plan.setAuditStatus(1);
        }else if (promotionPlan instanceof TrafficPromotionPlan){
            TrafficPromotionPlan trafficPromotionPlan = (TrafficPromotionPlan) promotionPlan;
            plan.setWeight(1);
            plan.setType(2);// 0-直投广告  1-直播推广  2-流量推广
            plan.setSellMode(2);//1-CPM 2-CPC 3-CPD 4-CPA 5-CPS
            plan.setSellPrice(trafficPromotionPlan.getClickPrice());
            plan.setAuditStatus(trafficPromotionPlan.getAuditStatus());
            plan.setCreateType(trafficPromotionPlan.getCreateType());
            plan.setTargettingFlag(trafficPromotionPlan.getTargettingFlag());
            plan.setIntelMode(trafficPromotionPlan.getIntelMode());
            plan.setIntelMatchPrice(trafficPromotionPlan.getIntelMatchPrice());
        }
        return  plan;
    }

    private PlanItem convert2PlanItem(BroadcastPromotionPlan broadcastPromotion) {
        PlanItem  planItem = new PlanItem();
        planItem.setPlanId(broadcastPromotion.getPromotionPlanId());
        planItem.setSpuid(selectedProduct.getSpuId() );
        planItem.setItemName(selectedProduct.getProductName() );
        planItem.setCategoryId(selectedProduct.getProductCategoryId() );
        planItem.setCategoryName(selectedProduct.getProductCategory());
        planItem.setPrice((long)selectedProduct.getProductPrice()*100 );
        planItem.setBrokerageRatio( (int)(broadcastPromotion.getBrokerageRatio() * 100) );
        planItem.setSellerId(advertiserInfo.getMerchantId());
        planItem.setSellerName(advertiserInfo.getName());
        planItem.setProfitId(broadcastPromotion.getProfitId());
        planItem.setPicture(selectedProduct.getPicture());
        planItem.setInventory(broadcastPromotion.getInventory(selectedProduct.getProductPrice()));
        planItem.setStatus(broadcastPromotion.getShelfStatus());//0-下架  1-上架
        planItem.setFlag(broadcastPromotion.getOfflineFlag());
        return  planItem;
    }

    private PlanDate convert2PlanDate(long promotionPlanId, PromotionCycle promotionCycle) {
        PlanDate  planDate = new PlanDate();
        planDate.setPlanId(promotionPlanId);
        planDate.setStartTime( promotionCycle.getStartDay() );
        planDate.setEndTime(promotionCycle.getEndDay());
        planDate.setWeekDeliveryMode(0);
        planDate.setStatus(1);
        return  planDate;
    }

    private PlanBoothRelation convert2PlanBoothRelation(AdBoothInfo adBoothInfo){
        PlanBoothRelation planBoothRelation = new PlanBoothRelation();
        planBoothRelation.setPlanId(-1);
        planBoothRelation.setBoothCode(adBoothInfo.getBoothCode());
        planBoothRelation.setCount(adBoothInfo.getCount());
        return planBoothRelation;
    }

    private PlanMaterialRelation convert2PlanMaterialRelation(){
        PlanMaterialRelation planMaterial = new PlanMaterialRelation();
        planMaterial.setPlanId(-1L);
        planMaterial.setMaterialId(-1L);
        return planMaterial;
    }

    private Material convert2Material(AdMaterialInfo adMaterialInfo) {
        Material material = new Material();
        material.setName(adMaterialInfo.getName());
        material.setContentKey(adMaterialInfo.getUrl());
        material.setContentSize(String.valueOf(adMaterialInfo.getWidth()) + "x" + String.valueOf(adMaterialInfo.getHeight()));
        material.setTitle(adMaterialInfo.getTitle());
        material.setSubTitle(adMaterialInfo.getSubTitle());
        material.setOpenMode(adMaterialInfo.getOpenMode());
        material.setShowSign(adMaterialInfo.isShowAdTag() ? 1 : 0);
        material.setTemplateId(21L);//URL链接模版ID
        material.setMaterialSource(adMaterialInfo.getMaterialSource());

        AdMaterialTemplateInfo adMaterialTemplate = adMaterialInfo.getAdMaterialTemplateInfo();
        boolean isHas = Objects.nonNull(adMaterialTemplate);
        if (isHas) {
            material.setMaterialTemplateId(adMaterialTemplate.getId());
            material.setShowTitle(adMaterialTemplate.getContentType());
            material.setStyle(adMaterialTemplate.getStyle());
            material.setShowStyle(adMaterialTemplate.getMaterialStyle());
        } else {
            material.setStyle(1);
            material.setShowStyle(0);
        }

        if (isHas && adMaterialTemplate.getIsClick() != null) {
            material.setIsClick(adMaterialTemplate.getIsClick());
        } else {
            material.setIsClick(1);
        }

        if (isHas && adMaterialTemplate.getContentType() > 1) {
            material.setContentType(adMaterialTemplate.getContentType());
        } else {
            material.setContentType(1);
        }

        String spuId = selectedProduct.getSpuId()+"";
        if (isHas && adMaterialTemplate.getAccessContent() == 1) {
            material.setBizContentId(spuId);
            material.setBizContentType(1);
        }
        material.setInput(spuId);

        return material;
    }

    private MaterialElement convert2MaterialElement(AdMaterialInfo adMaterialInfo) {
        if (!adMaterialInfo.isNeedMaterialElement()) {
            return null;
        }

        MaterialElement materialElement = new MaterialElement();
        materialElement.setMaterialId(adMaterialInfo.getId());
        materialElement.setType(1);
        materialElement.setUrl(adMaterialInfo.getUrl());
        materialElement.setWidth(adMaterialInfo.getWidth());
        materialElement.setHeight(adMaterialInfo.getHeight());
        materialElement.setAllowedOperation(adMaterialInfo.getOperations());
        materialElement.setDuration(0);
        return materialElement;
    }

}
