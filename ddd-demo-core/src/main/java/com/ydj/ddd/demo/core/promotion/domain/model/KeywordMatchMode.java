package com.ydj.ddd.demo.core.promotion.domain.model;

public class KeywordMatchMode {

    /**智能匹配模式（0-非智能 1-开启智能）*/
    private int intelMode;

    /**智能匹配模式价格*/
    private double intelMatchPrice;

    public KeywordMatchMode(int intelMode, double intelMatchPrice) {
        if (intelMode == 1 && intelMatchPrice < 0){
            throw new IllegalArgumentException("intelMatchPrice is error...");
        }
        this.intelMode = intelMode;
        this.intelMatchPrice = intelMatchPrice;
    }

    public int getIntelMode() {
        return intelMode;
    }

    public double getIntelMatchPrice() {
        return intelMatchPrice;
    }
}
