package com.ydj.ddd.demo.core.promotion.infrastructure.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class PromotionAdManageRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionAdManageRepo.class);
    
    @Resource
    private PlanMapper planMapper;

    @Resource
    private PlanItemMapper planItemMapper;

    /**
     * 软删除某条推广计划，同时修改推广计划状态为终止或下架
     *
     * @param promotionPlanId
     * @return
     */
    public boolean deletePromotionPlan(long promotionPlanId){
        LOGGER.info("deletePromotionPlan()->promotionPlanId = {}",promotionPlanId);
        int res = planMapper.deletePlan(promotionPlanId);
        boolean isDelete =  res > 0 ? true : false;
        if (isDelete){//简单直接下架直播推广，不管promotionPlanId是直播推广还是流量推广
            planItemMapper.offline(promotionPlanId);
        }
        return isDelete;
    }

}
