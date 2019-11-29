package com.ydj.ddd.demo.core.promotion.domain.factory;

import com.ydj.ddd.demo.core.promotion.domain.model.AbstractPromotionPlan;
import com.ydj.ddd.demo.core.promotion.domain.model.BroadcastPromotionPlan;
import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;
import com.ydj.ddd.demo.core.promotion.domain.model.AdvertiserInfo;
import com.ydj.ddd.demo.core.promotion.domain.model.SelectedProduct;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PromotionAdAggregateFactory4Create extends AbstractPromotionAdAggregateTemplateFactory implements PromotionAdAggregateFactory{

    @Resource
    private CommonUtil commonUtil;

    @Resource
    private BidensServiceRepo bidensServiceRepo;

    @Override
    protected AdvertiserInfo checkAndGetAdvertiser(long userId, long adId){
        UserInfoDTO userInfoDTO = this.commonUtil.getUserInfo(userId);
        if (Objects.isNull(userInfoDTO) || userInfoDTO.sellerId <= 0L) {
            throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        AdvertiserInfo advertiserInfo = this.getAdvertiserInfo(userInfoDTO.sellerId, userId);
        return advertiserInfo;
    }

    @Override
    protected SelectedProduct checkAndGetSelectedProduct(final MerchantPromotionRequest context){
        long spuId = context.spuId;
        if (spuId <= 0) {
            throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        Long oldAdId = promotionProductMapper.queryAdId(spuId);
        if (Objects.nonNull(oldAdId)) {
            throw  new BusinessException(ExceptionCodeEnum.REPEAT_PROMOTION_PRODUCT);
        }
        
        //校验限时类型活动
        this.bidensServiceRepo.validateItem(spuId);

        String picture = "";
        Spu mySpu = commonUtil.getSpu(spuId);
        if (Objects.nonNull(mySpu)){
            picture = mySpu.getPicture();
        }

        SelectedProduct selectedProduct = new SelectedProduct(spuId,context.productName,context.productPrice,context.productCategoryId,context.productCategory,picture);
        return selectedProduct;
    }

    @Override
    protected AbstractPromotionPlan buildBroadcastPromotion(final MerchantPromotionRequest context){
        BroadcastPromotionDto broadcastPromotionDto = context.broadcastPromotionDto;
        if (Objects.isNull(broadcastPromotionDto) || context.broadcastPromotionFlag == 0){
            return null;
        }

        BroadcastPromotionPlan broadcastPromotionPlan = this.buildNewBroadcastPromotionPlan(broadcastPromotionDto, context.productCategoryId);
        return broadcastPromotionPlan;
    }

    @Override
    protected List<AbstractPromotionPlan> buildTrafficPromotion(final MerchantPromotionRequest context){
        if (CollectionUtils.isEmpty(context.trafficPromotionDtos)) {
            return null;
        }

        this.check4FastFail(context);
        boolean isShowAdTag = this.productItemFacade.isShowAdTag(context.spuId);
        Long auditTime = auditService.getAuditLimitTime();

        List<AbstractPromotionPlan> trafficPromotionPlanList = context.trafficPromotionDtos.stream()
                .map(
                        one -> this.buildNewTrafficPromotionPlan(one,context.productCategoryId,isShowAdTag,auditTime)
                    )
                .collect(Collectors.toList());

        return trafficPromotionPlanList;
    }

}
