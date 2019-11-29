package com.ydj.ddd.demo.core.promotion.domain.repo;

import com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler.PromotionAdDataProcessResult;
import com.ydj.ddd.demo.core.promotion.infrastructure.repository.assembler.PromotionAdDataWrapper;

public interface PromotionAdSaveOrUpdateRepo {

    PromotionAdDataProcessResult savePromotionAd(PromotionAdDataWrapper promotionAdDataWrapper);

    PromotionAdDataProcessResult updatePromotionAd(PromotionAdDataWrapper promotionAdDataWrapper);

    boolean deletePromotionPlan(long promotionPlanId);
}
