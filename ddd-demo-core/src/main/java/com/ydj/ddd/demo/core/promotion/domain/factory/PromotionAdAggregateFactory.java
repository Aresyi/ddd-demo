package com.ydj.ddd.demo.core.promotion.domain.factory;

import com.ydj.ddd.demo.core.promotion.domain.model.PromotionAdAggregate;
import com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler.PromotionAdConvert2PoHelper;
import com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler.PromotionAdDataWrapper;

public interface PromotionAdAggregateFactory {

    /**
     * 创建推广计划
     *
     * @param userId
     * @param context
     * @return
     */
    PromotionAdAggregate buildAdAggregate(long userId, MerchantPromotionRequest context);

    /**
     * 数据转换
     *
     * @param promotionAdAggregate
     * @return
     */
    default PromotionAdDataWrapper convert2PoData(PromotionAdAggregate promotionAdAggregate){
        PromotionAdConvert2PoHelper promotionAdConvert2PoHelper = new PromotionAdConvert2PoHelper(promotionAdAggregate);
        return promotionAdConvert2PoHelper.convert2PoData();
    }

}
