package com.ydj.ddd.demo.core.promotion.domain.model;

import com.ydj.ddd.demo.core.promotion.enums.PlanStatusEnum;
import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public class PromotionAdAggregate {

    /**广告ID*/
    private PromotionAdID adId;

    /**广告名称*/
    private String adName;

    /**广告主信息*/
    private AdvertiserInfo advertiserInfo;

    /**所选择的需要推广的商品*/
    private SelectedProduct selectedProduct;

    /**直播推广*/
    private AbstractPromotionPlan broadcastPromotionPlan;

    /**流量推广*/
    private List<AbstractPromotionPlan> trafficPromotionPlanList;

    public PromotionAdAggregate(long adId,String adName) {
        this.adId = PromotionAdID.create(adId);
        this.setAdName(adName);
    }

    /**
     * 判断是否有直播推广
     * @return
     */
    public boolean isHasBroadcastPromotion(){
        return Objects.isNull(broadcastPromotionPlan) ? false : true;
    }

    /**
     * 判断是否有流量推广
     * @return
     */
    public boolean isHasTrafficPromotion(){
       return CollectionUtils.isEmpty(trafficPromotionPlanList) ? false : true;
    }

    /**
     * 获取推广类型<br>
     *(0- 广告系统使用  1-需要直播推广  2-需要流量推广 3-两者都需要)
     * @return
     */
    public int getPromotionType(){
        boolean hasTrafficPromotion = isHasTrafficPromotion();
        boolean hasBroadcastPromotion = isHasBroadcastPromotion();

        if (hasTrafficPromotion && hasBroadcastPromotion) {
            return 3;
        }

        if (hasTrafficPromotion) {
            return 2;
        }

        if (hasBroadcastPromotion) {
            return 1;
        }

        return 0;
    }

    /**
     * 获取推广计划状态
     *
     * @param promotionPlan
     * @return
     */
    public PlanStatusEnum getPlanStatus(AbstractPromotionPlan promotionPlan){
        Objects.requireNonNull(promotionPlan,"error->promotionPlan is null");
        boolean isInsufficientBalance = false;
        if (promotionPlan instanceof BroadcastPromotionPlan) {
            isInsufficientBalance = advertiserInfo.isInsufficientBroadcastBalance();
        }else if (promotionPlan instanceof TrafficPromotionPlan) {
            isInsufficientBalance = advertiserInfo.isInsufficientTrafficBalance();
        }
        return (isInsufficientBalance ? PlanStatusEnum.PAUSE : promotionPlan.getPlanStatus());
    }

    public void setBroadcastPromotionPlan(AbstractPromotionPlan broadcastPromotionPlan) {
        this.broadcastPromotionPlan = broadcastPromotionPlan;
    }


    public void setAdvertiserInfo(AdvertiserInfo advertiserInfo) {
        this.advertiserInfo = advertiserInfo;
    }


    public void setSelectedProduct(SelectedProduct selectedProduct) {
        this.selectedProduct = selectedProduct;
    }


    public void setTrafficPromotionPlanList(List<AbstractPromotionPlan> trafficPromotionPlanList) {
        this.trafficPromotionPlanList = trafficPromotionPlanList;
    }

    public long getAdId() {
        return adId.getAdId();
    }

    public String getAdName() {
        return adName;
    }

    public AdvertiserInfo getAdvertiserInfo() {
        return advertiserInfo;
    }

    public SelectedProduct getSelectedProduct() {
        return selectedProduct;
    }

    public AbstractPromotionPlan getBroadcastPromotionPlan() {
        return broadcastPromotionPlan;
    }

    public List<AbstractPromotionPlan> getTrafficPromotionPlanList() {
        return trafficPromotionPlanList;
    }

    private void setAdName(String adName) {
        if (StringUtils.isEmpty(adName)){
            throw new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }
        this.adName = adName;
    }

}
