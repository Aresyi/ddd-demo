package com.ydj.ddd.demo.core.promotion.domain.model;


import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PromotionCycle {

    /**是否是不限推广周期（true不限——开始时间即为推广计划的创建时间，结束时间定为2030-01-01）*/
    private boolean unlimited;

    /**开始时间*/
    private Date startDay;

    /**结束时间*/
    private Date endDay;

    public PromotionCycle(boolean unlimited, String startDay, String endDay) {
        this.init(unlimited,startDay,endDay);
    }

    private void  init(boolean unlimited, String startDay, String endDay){
        this.unlimited = unlimited;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            if (unlimited) {
                this.startDay = new Date();
                this.endDay = simpleDateFormat.parse("2030-01-01 00:00:00");
                return;
            }

            this.startDay = simpleDateFormat.parse(startDay + " 00:00:00");
            this.endDay = simpleDateFormat.parse(endDay + " 23:59:59");
        } catch (Exception e) {
            throw new BusinessException(ExceptionCodeEnum.WRONG_DATE_SETTING);
        }

        if ( this.startDay.after(this.endDay) || this.endDay.before(new Date())){
            throw new BusinessException(ExceptionCodeEnum.WRONG_DATE_SETTING);
        }
    }

    public static boolean isUnlimited(String endTime){
        if (Objects.nonNull(endTime)
                && endTime.contains("2030")){//FIXME
            return true;
        }
        return false;
    }

    public Date getStartDay() {
        return startDay;
    }

    public Date getEndDay() {
        return endDay;
    }
}
