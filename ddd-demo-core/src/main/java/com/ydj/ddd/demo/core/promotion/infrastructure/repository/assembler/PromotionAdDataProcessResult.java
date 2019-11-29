package com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler;

import com.google.common.collect.Lists;

import java.util.List;

public class PromotionAdDataProcessResult {

    private long adId;

    private long broadcastPromotionPlanId;

    private List<Long> need2AuditPlanIds;

    private List<Plan> trafficPromotionList;


    public PromotionAdDataProcessResult(long adId) {
        this.adId = adId;
        this.need2AuditPlanIds = Lists.newArrayList();
        this.trafficPromotionList = Lists.newArrayList();
    }

    public long getAdId() {
        return adId;
    }

    public void setAdId(long adId) {
        this.adId = adId;
    }

    public long getBroadcastPromotionPlanId() {
        return broadcastPromotionPlanId;
    }

    public void setBroadcastPromotionPlanId(long broadcastPromotionPlanId) {
        this.broadcastPromotionPlanId = broadcastPromotionPlanId;
    }

    public List<Plan> getTrafficPromotionList() {
        return trafficPromotionList;
    }


    public void addTrafficPromotion(Plan plan){
        trafficPromotionList.add(plan);
    }

    public void addNeed2AuditPlanId(long planId){
        need2AuditPlanIds.add(planId);
    }

    public List<Long> getNeed2AuditPlanIds() {
        return need2AuditPlanIds;
    }

}
