package com.ydj.ddd.demo.api.promotion;

import com.ydj.ddd.demo.api.promotion.dto.MerchantPromotionRequestDTO;

public interface PromotionPlanApiService {

    boolean createPromotionPlan(long userId, MerchantPromotionRequestDTO merchantPromotionRequest);

    boolean updatePromotionPlan(long userId, MerchantPromotionRequestDTO merchantPromotionRequest);

    boolean deletePromotionPlan(long userId, long promotionPlanId);
}
