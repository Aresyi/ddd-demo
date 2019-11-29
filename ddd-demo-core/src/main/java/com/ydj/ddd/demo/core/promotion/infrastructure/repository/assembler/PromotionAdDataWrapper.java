package com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Objects;

public class PromotionAdDataWrapper {

    private Ad ad;
    private PromotionProduct promotionProduct;
    private BroadcastPromotionData broadcastPromotionData;
    private List<TrafficPromotionData> trafficPromotionDataList = Lists.newArrayList();
    private List<Long> need2DeletePromotions = Lists.newArrayList();

    public static class BroadcastPromotionData {
        private Plan plan;
        private PlanDate planDate;
        private PlanItem planItem;

        public BroadcastPromotionData(Plan plan, PlanDate planDate, PlanItem planItem) {
            this.plan = plan;
            this.planDate = planDate;
            this.planItem = planItem;
        }

        public Plan getPlan() {
            return plan;
        }

        public void setPlan(Plan plan) {
            this.plan = plan;
        }

        public PlanDate getPlanDate() {
            return planDate;
        }

        public void setPlanDate(PlanDate planDate) {
            this.planDate = planDate;
        }

        public PlanItem getPlanItem() {
            return planItem;
        }

        public void setPlanItem(PlanItem planItem) {
            this.planItem = planItem;
        }
    }

    public static class TrafficPromotionData {
        private Plan plan;
        private PlanDate planDate;
        private PlanBoothRelation planBoothRelation;

        private Material material;
        private MaterialElement materialElement;
        private PlanMaterialRelation planMaterialRelation;

        private List<BidKeyword> newBidKeywords;
        private List<TargettingKeywords> targetKeywords;
        private Pair<List<BidKeyword>,List<BidKeyword>> newAndOldBidKeywords;

        public Plan getPlan() {
            return plan;
        }

        public void setPlan(Plan plan) {
            this.plan = plan;
        }

        public PlanDate getPlanDate() {
            return planDate;
        }

        public void setPlanDate(PlanDate planDate) {
            this.planDate = planDate;
        }

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public MaterialElement getMaterialElement() {
            return materialElement;
        }

        public void setMaterialElement(MaterialElement materialElement) {
            this.materialElement = materialElement;
        }

        public PlanMaterialRelation getPlanMaterialRelation() {
            return planMaterialRelation;
        }

        public void setPlanMaterialRelation(PlanMaterialRelation planMaterialRelation) {
            this.planMaterialRelation = planMaterialRelation;
        }

        public PlanBoothRelation getPlanBoothRelation() {
            return planBoothRelation;
        }

        public void setPlanBoothRelation(PlanBoothRelation planBoothRelation) {
            this.planBoothRelation = planBoothRelation;
        }

        public List<BidKeyword> getNewBidKeywords() {
            return newBidKeywords;
        }

        public void setNewBidKeywords(List<BidKeyword> newBidKeywords) {
            this.newBidKeywords = newBidKeywords;
        }

        public List<TargettingKeywords> getTargetKeywords() {
            return targetKeywords;
        }

        public void setTargetKeywords(List<TargettingKeywords> targetKeywords) {
            this.targetKeywords = targetKeywords;
        }

        public boolean isHasMaterial(){
            return Objects.isNull(material) ? false : true;
        }

        public Pair<List<BidKeyword>, List<BidKeyword>> getNewAndOldBidKeywords() {
            return newAndOldBidKeywords;
        }

        public void setNewAndOldBidKeywords(Pair<List<BidKeyword>, List<BidKeyword>> newAndOldBidKeywords) {
            this.newAndOldBidKeywords = newAndOldBidKeywords;
        }
    }

    public Ad getAd() {
        return ad;
    }

    public void setAd(Ad ad) {
        this.ad = ad;
    }

    public PromotionProduct getPromotionProduct() {
        return promotionProduct;
    }

    public void setPromotionProduct(PromotionProduct promotionProduct) {
        this.promotionProduct = promotionProduct;
    }

    public BroadcastPromotionData getBroadcastPromotionData() {
        return broadcastPromotionData;
    }

    public void setBroadcastPromotionData(BroadcastPromotionData broadcastPromotionData) {
        this.broadcastPromotionData = broadcastPromotionData;
    }

    public void addTrafficPlan(TrafficPromotionData trafficPromotionData){
        trafficPromotionDataList.add(trafficPromotionData);
    }

    public void addNeed2DeletePromotion(long promotionPlanId){
        need2DeletePromotions.add(promotionPlanId);
    }

    public List<Long> getNeed2DeletePromotions() {
        return need2DeletePromotions;
    }

    public List<TrafficPromotionData> getTrafficPromotionDataList() {
        return trafficPromotionDataList;
    }

    public boolean isHasBroadcastPromotion(){
        return Objects.isNull(broadcastPromotionData) ? false : true;
    }

    public boolean isHasTrafficPromotion(){
        if (CollectionUtils.isEmpty(trafficPromotionDataList)) {
            return false;
        }
        return true;
    }

    public boolean isHasNeed2DeletePromotions(){
        if (CollectionUtils.isEmpty(need2DeletePromotions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
