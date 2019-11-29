package com.ydj.ddd.demo.core.promotion.domain.model;


public class BroadcastPromotionPlan extends AbstractPromotionPlan {

   /**佣金比例*/
    private double brokerageRatio;

    /**利润ID（主播分成配置）*/
    private long profitId;

    /**下架标识 0-无 1-手动下架  2-自动下架 */
    private int offlineFlag;

    /** 是否下架 0-下架 1-上架*/
    private int shelfStatus;

    public BroadcastPromotionPlan(double promotionBudget, PromotionCycle promotionCycle, double brokerageRatio, long profitId) {
        super(-1,promotionBudget,promotionCycle);
        this.brokerageRatio = brokerageRatio;
        this.profitId = profitId;
        this.offlineFlag = 0;
        this.shelfStatus = 1;
    }

    public BroadcastPromotionPlan(long promotionPlanId, double promotionBudget, double promotionBudgetConsumption,
                                  PromotionCycle promotionCycle, double brokerageRatio, long profitId,int offlineFlag,int shelfStatus) {
        super(promotionPlanId, promotionBudget, promotionBudgetConsumption, promotionCycle);
        this.brokerageRatio = brokerageRatio;
        this.profitId = profitId;
        this.offlineFlag = offlineFlag;
        this.shelfStatus = shelfStatus;
    }

    @Override
    public String getPlanIdWrapper() {
        return "";
    }


    /**
     * 获取推广库存量
     *
     * @param productPrice
     * @return
     */
    public int getInventory(double productPrice){
        double onePrice = productPrice * brokerageRatio / 100.0;
        double balance = promotionBudget - promotionBudgetConsumption;

        int inventory = (int)( balance / onePrice);
        return inventory;
    }

    public double getBrokerageRatio() {
        return brokerageRatio;
    }

    public long getProfitId() {
        return profitId;
    }

    /**
     * 1-手动下架  2-自动下架
     *
     * @return
     */
    public int getOfflineFlag() {
        if (shelfStatus == 1){//上架
            return 0;
        }
        if (offlineFlag == 2){//自动下架的需要在编辑时自动上架
            return 0;
        }
        return offlineFlag;
    }

    /**
     * 获取上下架状态
     *（0-下架 1-上架）
     *
     * @return
     */
    public int getShelfStatus() {
        if (offlineFlag == 2 && shelfStatus == 0){//自动下架的需要在编辑时自动上架
            return 1;
        }
        return shelfStatus;
    }
}
