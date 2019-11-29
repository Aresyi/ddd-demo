package com.ydj.ddd.demo.core.promotion.domain.factory;


import com.ydj.ddd.demo.core.promotion.domain.model.*;
import com.ydj.ddd.demo.core.promotion.enums.AdDeliveryStrategyEnum;
import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;
import org.apache.logging.log4j.core.net.Advertiser;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Component
public abstract class AbstractPromotionAdAggregateTemplateFactory implements PromotionAdAggregateFactory{

    @Resource
    MerchantManager merchantManager;

    @Resource
    AdvertiserMapper advertiserMapper;

    @Resource
    PromotionProductMapper promotionProductMapper;

    @Resource
    BoothMapper boothMapper;

    @Resource
    BoothTemplateRelationMapper boothTemplateRelationMapper;

    @Resource
    AdMaterialTemplateMapper adMaterialTemplateMapper;

    @Resource
    ProductItemFacade productItemFacade;

    @Resource
    AuditService auditService;

    @Resource
    AnchorProfitQueryService anchorProfitQueryService;


    @Override
    public final PromotionAdAggregate buildAdAggregate(long userId, MerchantPromotionRequest context){
        if (Objects.isNull(context)){
            throw new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        if (CollectionUtils.isEmpty(context.trafficPromotionDtos) && Objects.isNull(context.broadcastPromotionDto)) {
            throw new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        long adId = context.adId;
        PromotionAdAggregate promotionAdAggregate = new PromotionAdAggregate(adId,context.promotionName);

        AdvertiserInfo advertiserInfo = this.checkAndGetAdvertiser(userId, adId);
        SelectedProduct selectedProduct = this.checkAndGetSelectedProduct(context);
        AbstractPromotionPlan broadcastPromotion = this.buildBroadcastPromotion(context);
        List<AbstractPromotionPlan> trafficPromotionList = this.buildTrafficPromotion(context);

        promotionAdAggregate.setAdvertiserInfo(advertiserInfo);
        promotionAdAggregate.setSelectedProduct(selectedProduct);
        promotionAdAggregate.setBroadcastPromotionPlan(broadcastPromotion);
        promotionAdAggregate.setTrafficPromotionPlanList(trafficPromotionList);
        return promotionAdAggregate;
    }

    /**
     * 检查并获取商家——广告主信息
     *
     * @param userId
     * @param adId
     * @return
     */
    protected abstract AdvertiserInfo checkAndGetAdvertiser(long userId,long adId);

    /**
     * 检查并获取所选商品
     *
     * @param context
     * @return
     */
    protected abstract SelectedProduct checkAndGetSelectedProduct(final MerchantPromotionRequest context);

    /**
     *  构建直播推广计划
     *
     * @param context
     * @return
     */
    protected abstract AbstractPromotionPlan buildBroadcastPromotion(final MerchantPromotionRequest context);

    /**
     * 构建流量推广计划
     *
     * @param context
     * @return
     */
    protected abstract List<AbstractPromotionPlan> buildTrafficPromotion(final MerchantPromotionRequest context);


    /**
     * 获取广告主信息
     *
     * @param sellerId
     * @param userId
     * @return
     */
    protected final AdvertiserInfo getAdvertiserInfo(long sellerId,long userId){
        Advertiser advertiser = advertiserMapper.getAdvertiserByMerchantId(sellerId);
        if (Objects.isNull(advertiser) || advertiser.getStatus() == 0) {
            throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        AdvertiserInfo advertiserInfo = new AdvertiserInfo(advertiser.getId(),advertiser.getMerchantId(),userId,advertiser.getName(),advertiser.getBalance(),advertiser.getTrafficBalance());
        return advertiserInfo;
    }


    /**
     * 校验佣金比例
     *
     * @param productCategoryId
     * @param brokerageRatio
     */
    protected final void checkBrokerageRatio(long productCategoryId, double brokerageRatio){
        ItemRatioInoResponse itemRatio = this.merchantManager.getBrokerageRange(productCategoryId);
        if (Objects.nonNull(itemRatio) && brokerageRatio * 100 < itemRatio.minRatio){
            throw  new BusinessException(ExceptionCodeEnum.LESS_MIN_BROKERAGE);
        }
    }

    /**
     * 构建素材信息
     *
     * @param trafficPromotionDto
     * @param showAdTag
     * @return
     */
    protected final AdMaterialInfo buildMaterialInfo(final TrafficPromotionDto trafficPromotionDto, boolean showAdTag){
        AdMaterialTemplateInfo adMaterialTemplateInfo = this.getAdMaterialTemplate(trafficPromotionDto.boothCode);
        if (Objects.isNull(trafficPromotionDto.material)){
            return new AdMaterialInfo(adMaterialTemplateInfo, showAdTag);
        }
        MaterialDto materialDto = trafficPromotionDto.material;
        return new AdMaterialInfo(
            materialDto.id, materialDto.name, materialDto.url,
            materialDto.width, materialDto.height, materialDto.title,
            materialDto.subTitle, showAdTag, adMaterialTemplateInfo,
            trafficPromotionDto.materialSource
        );
    }

    /**
     * 构建关键词
     *
     * @param trafficPromotionDto
     * @param adBoothInfo
     * @param productCategoryId
     * @return
     */
    protected SelectedKeyword buildSelectedKeyword(final TrafficPromotionDto trafficPromotionDto, final AdBoothInfo adBoothInfo, long productCategoryId){
        if (adBoothInfo.getDeliveryStrategy() != AdDeliveryStrategyEnum.BID){
            return null;
        }

        KeywordMatchMode keywordMatchMode = new KeywordMatchMode(trafficPromotionDto.intelMode,trafficPromotionDto.intelMatchPrice);
        List<BidKeywordInfo> newBidKeywordInfoList = null;
        if ( !CollectionUtils.isEmpty(trafficPromotionDto.keywords) ) {
            newBidKeywordInfoList = trafficPromotionDto.keywords.stream()
                    .map(
                            one ->new BidKeywordInfo(one.createType,one.keyword,one.cpcPrice,one.type,one.state)
                    )
                    .collect(Collectors.toList());
        }

        SelectedKeyword selectedKeyword = new SelectedKeyword(keywordMatchMode,adBoothInfo.getKeywordSource(),newBidKeywordInfoList);

        if (adBoothInfo.getKeywordSource() == 2){//推广通广告位对应的关键字类型 (1-自定义  2-商品分类)
            CategoryDto categoryDto = promotionProductMapper.getCategoryByCategory(productCategoryId);
            if(Objects.isNull(categoryDto)){
                throw new BusinessException(NO_CATEGORY);
            }

            ProductCategoryWordInfo productCategoryWordInfo = new ProductCategoryWordInfo(categoryDto.getTopCategory(),categoryDto.getSecondaryCategory(),categoryDto.getCategory());
            selectedKeyword.setProductCategoryWordInfo(productCategoryWordInfo);
        }

        return selectedKeyword;
    }

    /**
     * 根据广告位获取广告位信息
     *
     * @param boothCode
     * @return
     */
    protected final AdBoothInfo getAdBoothInfo(String boothCode){
        Booth booth = boothMapper.findByCode(boothCode);
        if (Objects.isNull(booth)){
            throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }
        AdDeliveryStrategyEnum deliveryStrategy = AdDeliveryStrategyEnum.getDeliveryStrategy(booth.getDeliveryStrategy());
        return new AdBoothInfo(deliveryStrategy,boothCode,booth.getCount(),booth.getKeywordSource());
    }

    /**
     * 根据广告位获取对应的模板
     *
     * @param boothCode
     * @return
     */
    protected final AdMaterialTemplateInfo getAdMaterialTemplate(String boothCode){
        List<Long> tempIds = boothTemplateRelationMapper.getTemplateIdList(boothCode, 4);
        if (CollectionUtils.isEmpty(tempIds)) {
            throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }

        long templateId = tempIds.get(0);
        AdMaterialTemplate adMaterialTemplate = adMaterialTemplateMapper.getMaterialTemplate(templateId);
        if (Objects.isNull(adMaterialTemplate)){
            return null;
        }

        AdMaterialTemplateInfo adMaterialTemplateInfo = new AdMaterialTemplateInfo(adMaterialTemplate.id,
                adMaterialTemplate.contentType,adMaterialTemplate.isClick, adMaterialTemplate.getMaterialStyle(),
                adMaterialTemplate.getMaterialSource(),adMaterialTemplate.accessContent);

        return adMaterialTemplateInfo;
    }

    /**
     * 快速失败检查
     *
     * @param context
     */
    protected void check4FastFail(final MerchantPromotionRequest context){
        context.trafficPromotionDtos.stream().forEach(one ->{
            if (one.operation == 1){//表示需要删除
                return;
            }
            if(one.deliveryStrategy == AdDeliveryStrategyEnum.BID.getVal()){
                if(CollectionUtils.isEmpty(one.keywords) && one.intelMode != 1){//非智能匹配必有的有选择关键词
                    throw new BusinessException(NO_KEYWORDS);
                }
                if(CollectionUtils.isNotEmpty(one.keywords)
                        && one.keywords.stream().collect(groupingBy(it->it.keyword, counting())).
                        values().stream().anyMatch(it-> it > 1)){
                    throw new BusinessException(KEYWORDS_REPEATED);
                }
            }
            if (one.materialSource == 0) {
                if (Objects.isNull(one.material) || StringUtils.isEmpty(one.material.url)) {
                    throw new BusinessException(NO_MATERIAL);
                }
            }
            new PromotionCycle(one.unlimited,one.promotionStart,one.promotionEnd);
        });
    }

    /**
     * 构建新增的流量推广计划
     *
     * @param one
     * @param productCategoryId
     * @param isShowAdTag
     * @param auditTime
     * @return
     */
    protected final TrafficPromotionPlan buildNewTrafficPromotionPlan(final TrafficPromotionDto one, long productCategoryId, boolean isShowAdTag, long auditTime){
        PromotionCycle promotionCycle = new PromotionCycle(one.unlimited, one.promotionStart, one.promotionEnd);
        AdBoothInfo adBoothInfo = this.getAdBoothInfo(one.boothCode);
        AdMaterialInfo adMaterialInfo = this.buildMaterialInfo(one,isShowAdTag);
        SelectedKeyword selectedKeyword = this.buildSelectedKeyword(one,adBoothInfo,productCategoryId);
        TrafficPromotionPlan trafficPromotionPlan = new TrafficPromotionPlan(one.promotionBudget,promotionCycle,one.clickPrice,adBoothInfo, adMaterialInfo,selectedKeyword,one.createType,auditTime);
        return trafficPromotionPlan;
    }

    /**
     * 构建新增的直播推广计划
     *
     * @param one
     * @param productCategoryId
     * @return
     */
    protected final BroadcastPromotionPlan buildNewBroadcastPromotionPlan(final BroadcastPromotionDto one, long productCategoryId){
        PromotionCycle promotionCycle = new PromotionCycle(one.unlimited,one.promotionStart,one.promotionEnd);
        double brokerageRatio = one.brokerageRatio;
        this.checkBrokerageRatio(productCategoryId, brokerageRatio);
        long profitId = anchorProfitQueryService.getFixProfitId(brokerageRatio);
        BroadcastPromotionPlan broadcastPromotionPlan = new BroadcastPromotionPlan(one.promotionBudget,promotionCycle,brokerageRatio,profitId);
        return broadcastPromotionPlan;
    }

}
