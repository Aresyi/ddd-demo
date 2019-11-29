package com.ydj.ddd.demo.core.promotion.domain.model;


import com.ydj.ddd.demo.core.promotion.enums.AdDeliveryStrategyEnum;
import com.ydj.ddd.demo.core.promotion.enums.PlanStatusEnum;
import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;

import java.math.BigDecimal;
import java.util.Objects;

import static com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum.NO_KEYWORDS;


public class TrafficPromotionPlan extends AbstractPromotionPlan {

   /**点击价格*/
    private double clickPrice;

    /**所选择的广告位信息*/
    private AdBoothInfo adBoothInfo;

    /**素材信息*/
    private AdMaterialInfo adMaterialInfo;

    /**所选关键字*/
    private SelectedKeyword selectedKeyword;

    /**创建类型（0-人工自选创建 1-运营推荐导入后自选）*/
    private int createType;

    /**审核时间*/
    private long auditTime;

    /**审核状态 (1-待审核 2-审核通过 3-审核不通过 4-审核超时)*/
    private int auditStatus;

    /**投放状态(1-终止, 2-已投放, 3-暂停, 4-可投未开始, 5-投放中,6-消耗完成,7-未投放)*/
    private int planStatus;

    public TrafficPromotionPlan(long promotionPlanId) {
        super(promotionPlanId);
    }

    public TrafficPromotionPlan(double promotionBudget, PromotionCycle promotionCycle, double clickPrice,
                                AdBoothInfo adBoothInfo, AdMaterialInfo adMaterialInfo, SelectedKeyword selectedKeyword, int createType, long auditTime) {
       this(-1,promotionBudget,0,promotionCycle,clickPrice,adBoothInfo,adMaterialInfo,selectedKeyword,createType,auditTime,0,0);
    }

    public TrafficPromotionPlan(long promotionPlanId, double promotionBudget, double promotionBudgetConsumption, PromotionCycle promotionCycle,
                                double clickPrice, AdBoothInfo adBoothInfo, AdMaterialInfo adMaterialInfo, SelectedKeyword selectedKeyword, int createType, long auditTime, int auditStatus, int planStatus) {
        super(promotionPlanId, promotionBudget, promotionBudgetConsumption, promotionCycle);
        this.clickPrice = clickPrice;
        this.adBoothInfo = adBoothInfo;
        this.adMaterialInfo = adMaterialInfo;
        this.selectedKeyword = selectedKeyword;
        this.createType = createType;
        this.auditTime = auditTime;
        this.auditStatus = auditStatus;
        this.planStatus = planStatus;

        this.checkInit();
    }

    private void checkInit(){
        if (Objects.isNull(adBoothInfo)){
            throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        if(this.adBoothInfo.getDeliveryStrategy() == AdDeliveryStrategyEnum.BID && Objects.isNull(selectedKeyword)){
            throw new BusinessException(NO_KEYWORDS);
        }
    }

    @Override
    public PlanStatusEnum getPlanStatus() {

        if (planStatus == PlanStatusEnum.PAUSE.getValue()){//已暂停
            return PlanStatusEnum.PAUSE;
        }

        if (Objects.nonNull(this.adMaterialInfo)
                && this.adMaterialInfo.getMaterialSource() != 1 && this.auditTime == 0) {
           return PlanStatusEnum.UN_COMMIT;
        }

        if (auditStatus == 3 || auditStatus == 4){
            return PlanStatusEnum.UN_COMMIT;
        }

        return super.getPlanStatus();
    }

    @Override
    public String getPlanIdWrapper() {
        Integer number = 1000001 + this.adBoothInfo.getCount();
        return this.adBoothInfo.getBoothCode() + String.valueOf(number).substring(1);
    }

    public boolean isHasMaterial(){
        return Objects.isNull(adMaterialInfo) ? false : true;
    }

    public boolean isHasSelectedKeyword(){
        if(this.adBoothInfo.getDeliveryStrategy() == AdDeliveryStrategyEnum.BID && Objects.nonNull(selectedKeyword)){
            return true;
        }
        return false;
    }

    public int getAuditStatus(){
        if (this.isHasMaterial() && adMaterialInfo.getMaterialSource() == 1) {
            return 2;
        }
        return 1;
    }

    public int getTargettingFlag(){
        if(this.isHasSelectedKeyword() && selectedKeyword.getKeywordSource() == 2){
            return 1;
        }
        return 0;
    }

    public int getIntelMode(){
        if (isHasSelectedKeyword()){
            return selectedKeyword.getKeywordMatchMode().getIntelMode();
        }
        return 0;
    }

    public long getIntelMatchPrice(){
        if (isHasSelectedKeyword()){
            double intelMatchPrice = selectedKeyword.getKeywordMatchMode().getIntelMatchPrice();
            return new BigDecimal(intelMatchPrice*100).longValue();
        }
        return 0;
    }

//    public List<BidKeyword> getNewBidKeywords(long  advertiserId,long spuId){
//        if (isHasSelectedKeyword()) {
//            return selectedKeyword.getNewBidKeyword(promotionPlanId, advertiserId, spuId);
//        }
//        return Collections.EMPTY_LIST;
//    }
//
//    public List<TargettingKeywords> getTargetKeywords(){
//        if (isHasSelectedKeyword() && selectedKeyword.getKeywordSource() == 2) {
//            return selectedKeyword.getTargettingKeywords(promotionPlanId);
//        }
//        return Collections.EMPTY_LIST;
//    }
//
//    public Pair<List<BidKeyword>,List<BidKeyword>> getNewAndOldBidKeyword(long advertiserId, long spuId){
//        if (isHasSelectedKeyword()) {
//            return selectedKeyword.getNewAndOldBidKeyword(promotionPlanId, advertiserId, spuId);
//        }
//        return Pair.of(Collections.EMPTY_LIST,Collections.EMPTY_LIST);
//    }

    public AdMaterialInfo getAdMaterialInfo() {
        return adMaterialInfo;
    }

    public AdBoothInfo getAdBoothInfo() {
        return adBoothInfo;
    }

    public double getClickPrice() {
        return clickPrice;
    }

    public int getCreateType() {
        return createType;
    }

    public long getAuditTime() {
        return auditTime;
    }
}
