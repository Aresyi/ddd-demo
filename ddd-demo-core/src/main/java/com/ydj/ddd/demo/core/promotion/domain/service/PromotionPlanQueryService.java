package com.ydj.ddd.demo.core.promotion.domain.service;

public interface PromotionPlanQueryService {

    /**
     * 查询直播推广计划
     *
     * @param userId
     * @param queryConditionDto
     * @return
     */
    MerchantPromotionListResponse queryBroadcastPromotionPlan(long userId, PromotionQueryConditionDto queryConditionDto);

    /**
     * 查询流量推广计划
     *
     * @param userId
     * @param queryConditionDto
     * @return
     */
    MerchantPromotionListResponse queryTrafficPromotionPlan(long userId, PromotionQueryConditionDto queryConditionDto);


    /****
     * 查询商家推广商品
     * @param sellerId
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<BroadcastPromotionDO> queryOnlinePlanItemBySellerId(long sellerId, int pageNo, int pageSize);


}
