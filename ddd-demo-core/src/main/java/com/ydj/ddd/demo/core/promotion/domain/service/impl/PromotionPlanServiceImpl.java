package com.ydj.ddd.demo.core.promotion.domain.service.impl;

import com.ydj.ddd.demo.core.promotion.domain.factory.PromotionAdAggregateFactory;
import com.ydj.ddd.demo.core.promotion.domain.model.BroadcastPromotionPlan;
import com.ydj.ddd.demo.core.promotion.domain.model.PromotionAdAggregate;
import com.ydj.ddd.demo.core.promotion.domain.model.TrafficPromotionPlan;
import com.ydj.ddd.demo.core.promotion.domain.repo.PromotionAdSaveOrUpdateRepo;
import com.ydj.ddd.demo.core.promotion.domain.service.PromotionPlanService;
import com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler.PromotionAdDataProcessResult;
import com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler.PromotionAdDataWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PromotionPlanServiceImpl implements PromotionPlanService {

    @Resource
    private PromotionAdAggregateFactory promotionAdAggregateFactory4Create;

    @Resource
    private PromotionAdAggregateFactory promotionAdAggregateFactory4Update;

    @Resource
    private PromotionAdSaveOrUpdateRepo promotionAdSaveOrUpdateRepo;

    @Resource
    private ProfitShareUpdateService profitShareUpdateService;

    @Resource
    private SyncDataToRedisService syncDataToRedisService;

    @Resource
    private AuditDelayJobService auditDelayJob;

    @Resource
    private WebcastSyncService webcastSyncService;

    @Resource
    private AnchorSelectedItemQueryService anchorSelectedItemQueryService;

    @Override
    public Long createPromotionPlan(long userId, MerchantPromotionRequest merchantPromotionRequest) {
        PromotionAdAggregate adAggregate = this.promotionAdAggregateFactory4Create.buildAdAggregate(userId, merchantPromotionRequest);
        PromotionAdDataWrapper promotionAdDataWrapper = this.promotionAdAggregateFactory4Create.convert2PoData(adAggregate);

        PromotionAdDataProcessResult promotionAdDataProcessResult = this.promotionAdSaveOrUpdateRepo.savePromotionAd(promotionAdDataWrapper);

        long broadcastPromotionPlanId = promotionAdDataProcessResult.getBroadcastPromotionPlanId();
        if (broadcastPromotionPlanId > 0){
            this.profitShareUpdateService.updateProfitShare(broadcastPromotionPlanId);
        }

        List<Plan> trafficPromotionList = promotionAdDataProcessResult.getTrafficPromotionList();
        if (CollectionUtils.isNotEmpty(trafficPromotionList) ) {
            trafficPromotionList.forEach(plan ->
                    this.syncDataToRedisService.syncToRedis(plan)
            );
        }

        List<Long> need2AuditPlanIds = promotionAdDataProcessResult.getNeed2AuditPlanIds();
        if ( CollectionUtils.isNotEmpty(need2AuditPlanIds) ) {
            need2AuditPlanIds.forEach(planId ->
                    this.auditDelayJob.addDelayJob(planId)
            );
        }

        return promotionAdDataProcessResult.getAdId();
    }


    @Override
    public Long updatePromotionPlan(long userId, MerchantPromotionRequest merchantPromotionRequest) {
        PromotionAdAggregate adAggregate = this.promotionAdAggregateFactory4Update.buildAdAggregate(userId, merchantPromotionRequest);
        PromotionAdDataWrapper promotionAdDataWrapper = this.promotionAdAggregateFactory4Update.convert2PoData(adAggregate);

        PromotionAdDataProcessResult promotionAdDataProcessResult = this.promotionAdSaveOrUpdateRepo.updatePromotionAd(promotionAdDataWrapper);

        if(adAggregate.isHasBroadcastPromotion()){
            BroadcastPromotionPlan broadcastPromotionPlan = (BroadcastPromotionPlan)adAggregate.getBroadcastPromotionPlan();
            List<Long> anchors = anchorSelectedItemQueryService.getAnchors4SelectedThisPlan(broadcastPromotionPlan.getPromotionPlanId());
            this.webcastSyncService.syncDataToWebcastBatch(anchors);
        }

        List<Plan> trafficPromotionList = promotionAdDataProcessResult.getTrafficPromotionList();
        if (CollectionUtils.isNotEmpty(trafficPromotionList) ) {
            trafficPromotionList.forEach(
                    plan -> this.syncDataToRedisService.syncToRedis(plan)
            );
        }

        List<Long> need2AuditPlanIds = promotionAdDataProcessResult.getNeed2AuditPlanIds();
        if ( CollectionUtils.isNotEmpty(need2AuditPlanIds) ) {
            TrafficPromotionPlan one = (TrafficPromotionPlan)adAggregate.getTrafficPromotionPlanList().get(0);
            int delayTime = (int) (one.getAuditTime() * 60);
            need2AuditPlanIds.forEach(
                    planId -> this.auditDelayJob.rebuildDelayJob(planId,delayTime)
            );
        }

        return promotionAdDataProcessResult.getAdId();
    }


    @Override
    public Boolean deletePromotionPlan(long userId,long promotionPlanId) {
        boolean isDelete = this.promotionAdSaveOrUpdateRepo.deletePromotionPlan(promotionPlanId);
        if (isDelete){
            List<Long> anchors = anchorSelectedItemQueryService.getAnchors4SelectedThisPlan(promotionPlanId);
            this.webcastSyncService.syncDataToWebcastBatch(anchors);
        }
        return isDelete;
    }
}
