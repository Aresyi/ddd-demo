package com.ydj.ddd.demo.core.promotion.interfaces.rpcapi;


import com.ydj.ddd.demo.api.promotion.PromotionPlanApiService;
import com.ydj.ddd.demo.api.promotion.dto.MerchantPromotionRequestDTO;
import com.ydj.ddd.demo.core.promotion.domain.service.PromotionPlanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@ApiAuthCheck(RoleEnum.MERCHANT_WATIER)
public class PromotionPlanApiImpl implements PromotionPlanApiService,ApiResultHandler {

    @Resource
    private PromotionPlanService promotionPlanService;


    @Override
    public boolean createPromotionPlan(long userId, MerchantPromotionRequestDTO merchantPromotionRequest) {
        if (userId <= 0 || merchantPromotionRequest == null) {
            DubboExtProperty.setErrorCode(AdReturnCode.INVALID_PARAMETER);
            return  false;
        }
        Boolean res = gwResultWrapper(
                String.format("createPromotionPlan()->userId = %s, merchantPromotionRequest = %s", userId, merchantPromotionRequest),
                () -> {
                    Long adId = promotionPlanService.createPromotionPlan(userId, merchantPromotionRequest);
                    return ( Objects.nonNull(adId) && adId > 0 ? true : false);
                });
        return Objects.isNull(res) ? false : res;
    }

    @Override
    public boolean updatePromotionPlan(long userId, MerchantPromotionRequestDTO merchantPromotionRequest) {
        if (userId <= 0 || merchantPromotionRequest == null) {
            DubboExtProperty.setErrorCode(AdReturnCode.INVALID_PARAMETER);
            return  false;
        }
        Boolean res =  gwResultWrapper(
                String.format("updatePromotionPlan()->userId = %s, merchantPromotionRequest = %s", userId, merchantPromotionRequest),
                () -> {
                    Long adId = promotionPlanService.updatePromotionPlan(userId,merchantPromotionRequest);
                    return ( Objects.nonNull(adId) && adId > 0 ? true : false);
                });
        return Objects.isNull(res) ? false : res;
    }

    @Override
    public boolean deletePromotionPlan(long userId, long promotionPlanId) {
        if (userId <= 0 || promotionPlanId < 1) {
            DubboExtProperty.setErrorCode(AdReturnCode.INVALID_PARAMETER);
            return  false;
        }
        Boolean res =  gwResultWrapper(
                String.format("deletePromotionPlan()->userId = %s, promotionPlanId = %s", userId, promotionPlanId),
                () -> promotionPlanService.deletePromotionPlan(userId,promotionPlanId)
        );
        return Objects.isNull(res) ? false : res;
    }

}
