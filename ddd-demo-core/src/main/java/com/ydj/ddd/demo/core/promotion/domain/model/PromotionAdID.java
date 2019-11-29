package com.ydj.ddd.demo.core.promotion.domain.model;

import lombok.Getter;

@Getter
public class PromotionAdID {

    /**广告ID*/
    private long adId;

    private PromotionAdID(long adId){
        this.adId = adId;
    }

    public static PromotionAdID create(long adId){
        return new PromotionAdID(adId);
    }

}
