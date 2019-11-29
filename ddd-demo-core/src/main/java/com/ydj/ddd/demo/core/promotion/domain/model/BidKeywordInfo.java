package com.ydj.ddd.demo.core.promotion.domain.model;

public class BidKeywordInfo {

    /**推广计划ID*/
    private long planId;

   /**创建类型 代码取值:0-人工自选创建 1-运营推荐导入后自选*/
    private int createType;

   /**关键字*/
    private  String keyword;

   /**价格*/
    private  double cpcPrice;

   /**类型*/
    private int type;

   /**状态*/
    private int state;

    public BidKeywordInfo(int createType, String keyword, double cpcPrice, int type, int state) {
       this(0,createType,keyword,cpcPrice,type,state);
    }

    public BidKeywordInfo(long planId, int createType, String keyword, double cpcPrice, int type, int state) {
        this.planId = planId;
        this.createType = createType;
        this.keyword = keyword;
        this.cpcPrice = cpcPrice;
        this.type = type;
        this.state = state;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getCreateType() {
        return createType;
    }

    public double getCpcPrice() {
        return cpcPrice;
    }

    public int getType() {
        return type;
    }

    public int getState() {
        return state;
    }

    public long getPlanId() {
        return planId;
    }
}