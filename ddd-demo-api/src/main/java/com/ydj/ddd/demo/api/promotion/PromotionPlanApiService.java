package com.ydj.ddd.demo.api.promotion;

import com.ydj.ddd.demo.api.promotion.dto.CreatePromotionDTO;

public interface PromotionPlanApiService {

    boolean createPromotionPlan(long userId, CreatePromotionDTO merchantPromotionRequest);

    boolean updatePromotionPlan(long userId, CreatePromotionDTO merchantPromotionRequest);

    boolean deletePromotionPlan(long userId, long promotionPlanId);
}
