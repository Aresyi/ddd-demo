package com.ydj.ddd.demo.core.promotion.enums;

public enum PlanStatusEnum {

    PAUSE,BEFORE_COMMIT,AFTER_COMMIT,IN_COMMIT,UN_COMMIT;

    String desc;
    int value;

    public int getValue() {
        return value;
    }
}
