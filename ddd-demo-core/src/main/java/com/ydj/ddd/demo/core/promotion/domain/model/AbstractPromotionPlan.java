package com.ydj.ddd.demo.core.promotion.domain.model;


import com.ydj.ddd.demo.core.promotion.enums.PlanStatusEnum;
import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;

import java.util.Date;
import java.util.Objects;

public abstract class AbstractPromotionPlan {

    /**推广计划id*/
    protected long promotionPlanId;

    /**推广预算*/
    protected double promotionBudget;

    /**推广预算-已经消耗量*/
    protected double promotionBudgetConsumption;

    /**推广周期*/
    protected PromotionCycle promotionCycle;

    /**是否需要删除本推广计划*/
    protected boolean isNeed2Delete;

    public AbstractPromotionPlan(long promotionPlanId) {
        this.promotionPlanId = promotionPlanId;
        this.isNeed2Delete = true;
    }

    public AbstractPromotionPlan(long promotionPlanId, double promotionBudget, PromotionCycle promotionCycle) {
       this(promotionPlanId,promotionBudget,0,promotionCycle);
    }

    public AbstractPromotionPlan(long promotionPlanId, double promotionBudget, double promotionBudgetConsumption, PromotionCycle promotionCycle) {
        this.promotionPlanId = promotionPlanId;
        this.promotionBudget = promotionBudget;
        this.promotionBudgetConsumption = promotionBudgetConsumption;
        this.promotionCycle = promotionCycle;

        this.checkInit();
    }

    private void checkInit(){
        if (promotionBudget <= 0 || promotionBudget <= promotionBudgetConsumption) {
            throw  new BusinessException(ExceptionCodeEnum.BUDGET_LESS_CONSUMPTION);
        }

        if (Objects.isNull(promotionCycle)){
            throw  new BusinessException(ExceptionCodeEnum.WRONG_DATE_SETTING);
        }
    }

    /**
     * 新版本投放状态
     *
     * (1-终止, 2-已投放, 3-暂停, 4-可投未开始, 5-投放中,6-消耗完成,7-未投放)
     *
     * @return
     */
    public PlanStatusEnum getPlanStatus(){
        if (Objects.isNull(this.promotionCycle)){
            throw new IllegalStateException("promotionCycle is null...");
        }

        Date now = new Date();
        if (now.compareTo(this.promotionCycle.getStartDay()) < 0) {
            return  PlanStatusEnum.BEFORE_COMMIT;
        }
        if (now.compareTo(this.promotionCycle.getEndDay()) > 0) {
            return  PlanStatusEnum.AFTER_COMMIT;
        }
        return  PlanStatusEnum.IN_COMMIT;
    }

    /**
     * 推广计划编号
     * @return
     */
    public abstract String getPlanIdWrapper();

    public double getPromotionBudget() {
        return promotionBudget;
    }

    public PromotionCycle getPromotionCycle() {
        return promotionCycle;
    }

    public long getPromotionPlanId() {
        return promotionPlanId;
    }

    public boolean isNeed2Delete() {
        return isNeed2Delete;
    }
}
