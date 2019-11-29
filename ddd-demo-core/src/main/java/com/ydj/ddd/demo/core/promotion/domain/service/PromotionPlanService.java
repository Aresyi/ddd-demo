package com.ydj.ddd.demo.core.promotion.domain.service;


public interface PromotionPlanService {

    /**
     * 创建推广计划广告
     *
     * @param userId
     * @param merchantPromotionRequest
     * @return
     */
    Long createPromotionPlan(long userId, MerchantPromotionRequest merchantPromotionRequest);

    /**
     * 编辑推广计划广告
     *
     * @param userId
     * @param merchantPromotionRequest
     * @return
     */
    Long updatePromotionPlan(long userId, MerchantPromotionRequest merchantPromotionRequest);

    /**
     * 删除推广计划
     *
     * @param userId
     * @param promotionPlanId
     * @return
     */
    Boolean deletePromotionPlan(long userId, long promotionPlanId);

}
