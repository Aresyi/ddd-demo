package com.ydj.ddd.demo.core.promotion.domain.model;


import com.ydj.ddd.demo.core.promotion.enums.AdDeliveryStrategyEnum;

public class AdBoothInfo {

    /**投放策略（0-排序随机  1-排序轮播 2-竞价排序）*/
    private AdDeliveryStrategyEnum deliveryStrategy;

    /**广告展位号*/
    private String boothCode;

    /**当前广告位广告投放数量*/
    private int count;

    /**推广通关键字类型（1-自定义  2-商品分类）*/
    private int keywordSource;

    public AdBoothInfo(AdDeliveryStrategyEnum deliveryStrategy,String boothCode, int count,int keywordSource) {
        this.deliveryStrategy = deliveryStrategy;
        this.boothCode = boothCode;
        this.count = count;
        this.keywordSource = keywordSource;
    }

    public String getBoothCode() {
        return boothCode;
    }

    public int getCount() {
        return count;
    }

    public AdDeliveryStrategyEnum getDeliveryStrategy() {
        return deliveryStrategy;
    }

    public int getKeywordSource() {
        return keywordSource;
    }
}
