package com.ydj.ddd.demo.core.promotion.interfaces.restapi;


import com.ydj.ddd.demo.core.promotion.domain.service.PromotionPlanQueryService;
import com.ydj.ddd.demo.core.promotion.domain.service.PromotionPlanService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
@RequestMapping("/promotion/manage")
public class PromotionManageController implements ApiResultHandler {

    @Resource
    private PromotionPlanService promotionPlanService;

    @Resource
    private PromotionPlanQueryService promotionPlanQueryService;

    @RequestMapping(value = "/queryBroadcastPromotionPlan", method = {RequestMethod.GET})
    public AdResult<MerchantPromotionListResponse> queryBroadcastPromotionPlan(PromotionQueryConditionDto promotionQueryConditionDto) {
        return resultWrapper(
                String.format("queryBroadcastPromotionPlan()->promotionQueryConditionDto = %s", promotionQueryConditionDto),
                () -> {
                    MerchantPromotionListResponse response = this.promotionPlanQueryService.queryBroadcastPromotionPlan(0, promotionQueryConditionDto);
                    return response;
                });
    }

    @RequestMapping(value = "/queryTrafficPromotionPlan", method = {RequestMethod.GET})
    public AdResult<MerchantPromotionListResponse> queryTrafficPromotionPlan(PromotionQueryConditionDto promotionQueryConditionDto) {
        return resultWrapper(
                String.format("queryTrafficPromotionPlan()->promotionQueryConditionDto = %s", promotionQueryConditionDto),
                () -> {
                    MerchantPromotionListResponse response = this.promotionPlanQueryService.queryTrafficPromotionPlan(0, promotionQueryConditionDto);
                    return response;
                });
    }

    @AdminLog(logType = LogTypeEnum.ADMIN_ADD_UPDATE_PLAN_ITEM)
    @RequestMapping(value = "/updatePromotionPlan", method = {RequestMethod.POST,RequestMethod.GET})
    public AdResult<Boolean> updatePromotionPlan(@RequestBody MerchantPromotionRequest merchantPromotionRequest) {
        return resultWrapper(
                String.format("updatePromotionPlan()->merchantPromotionRequest = %s", merchantPromotionRequest),
                () -> {
                    Long res = this.promotionPlanService.updatePromotionPlan(0, merchantPromotionRequest);
                    return (Objects.nonNull(res) && res > 0 ? true : false);
                });
    }

    @RequestMapping(value = "/deletePromotionPlan", method = {RequestMethod.POST,RequestMethod.GET})
    public AdResult<Boolean> deletePromotionPlan(@RequestParam long promotionPlanId) {
        return resultWrapper(
                String.format("deletePromotionPlan()->promotionPlanId = %s", promotionPlanId),
                () -> this.promotionPlanService.deletePromotionPlan(0,promotionPlanId)
        );
    }
}
