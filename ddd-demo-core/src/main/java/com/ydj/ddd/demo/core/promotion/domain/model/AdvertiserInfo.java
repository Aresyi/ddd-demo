package com.ydj.ddd.demo.core.promotion.domain.model;

public class AdvertiserInfo {

    /**广告主ID*/
    private  long   id;

    /**对应的用户ID*/
    private  long   userId;

    /**对应的商家ID*/
    private  long   merchantId;

    /**名称*/
    private  String name;

    /**直播推广账户余额*/
    private long balance;

    /**流量推广账户余额*/
    private long trafficBalance;

    public AdvertiserInfo(long id, long merchantId,long userId, String name, long balance,long trafficBalance) {
        this.id = id;
        this.merchantId = merchantId;
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.trafficBalance = trafficBalance;
    }

    /**
     * 是否直播推广余额不足
     * @return
     */
    public boolean isInsufficientBroadcastBalance(){
        return balance > 0 ? false : true;
    }

    /**
     * 是否流量推广余额不足
     * @return
     */
    public boolean isInsufficientTrafficBalance(){
        return trafficBalance > 0 ? false : true;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public long getMerchantId() {
        return merchantId;
    }

    public long getBalance() {
        return balance;
    }

    public long getTrafficBalance() {
        return trafficBalance;
    }
}
