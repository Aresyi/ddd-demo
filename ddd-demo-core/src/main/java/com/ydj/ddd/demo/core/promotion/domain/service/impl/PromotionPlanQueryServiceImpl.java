package com.ydj.ddd.demo.core.promotion.domain.service.impl;


import com.google.common.collect.Lists;
import com.ydj.ddd.demo.core.promotion.domain.model.PromotionCycle;
import com.ydj.ddd.demo.core.promotion.domain.service.PromotionPlanQueryService;
import org.apache.logging.log4j.core.net.Advertiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PromotionPlanQueryServiceImpl implements PromotionPlanQueryService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionPlanQueryServiceImpl.class);

    @Resource
    private CommonUtil commonUtil;

    @Resource
    private PromotionProductMapper promotionProductMapper;

    @Resource
    private AdvertiserMapper advertiserMapper;

    @Resource
    private SellerTrafficPlanReportMapper sellerTrafficPlanReportMapper;

    @Override
    public MerchantPromotionListResponse queryBroadcastPromotionPlan(long userId, PromotionQueryConditionDto queryConditionDto) {
        PromotionPlanTypeEnum promotionPlanType = PromotionPlanTypeEnum.BROADCAST;
        QueryCondition queryCondition = this.convert2QueryCondition(userId, queryConditionDto);
        queryCondition.planTypes = Lists.newArrayList(promotionPlanType.getValue());//直播推广

        List<Long> adIdList = promotionProductMapper.getPromotionIdsByStatus(queryCondition);
        if (CollectionUtils.isEmpty(adIdList)) {
            return  MerchantPromotionListResponse.emptyRes();
        }

        int totalCount = adIdList.size();
        queryCondition.adIds = this.getPageAdIds(queryCondition.startRow,queryCondition.pageSize,adIdList);
        if ( CollectionUtils.isEmpty( queryCondition.adIds) ) {
           return MerchantPromotionListResponse.emptyRes(totalCount);
        }

        List<PromotionQueryResultDto> promotions;
        if (queryCondition.broadcastStatus == 0) {
            promotions = promotionProductMapper.getPromotionsByNoStatus(queryCondition);
        }else {
            promotions = promotionProductMapper.getBroadcastPromotionsByAdid(queryCondition.adIds);
        }

        MerchantPromotionListResponse  response = new MerchantPromotionListResponse();
        response.totalCount = totalCount;
        response.merchantPromotionResponses = this.convert4BroadcastPromotionResponse(promotions);
        response.insufficientBalance = this.isInsufficientBalance(queryCondition.merchantId, promotionPlanType);
        return response;
    }

    @Override
    public MerchantPromotionListResponse queryTrafficPromotionPlan(long userId, PromotionQueryConditionDto queryConditionDto) {
        PromotionPlanTypeEnum promotionPlanType = PromotionPlanTypeEnum.TRAFFIC;
        QueryCondition queryCondition = this.convert2QueryCondition(userId, queryConditionDto);
        queryCondition.planTypes = Lists.newArrayList(promotionPlanType.getValue());//流量推广

        List<Long> adIdList = promotionProductMapper.getPromotionIdsByStatus(queryCondition);
        if (CollectionUtils.isEmpty(adIdList)) {
            return  MerchantPromotionListResponse.emptyRes();
        }

        int totalCount = adIdList.size();
        queryCondition.adIds = this.getPageAdIds(queryCondition.startRow,queryCondition.pageSize,adIdList);
        if ( CollectionUtils.isEmpty( queryCondition.adIds) ) {
            return MerchantPromotionListResponse.emptyRes(totalCount);
        }

        List<PromotionQueryResultDto> promotions;
        if (queryCondition.trafficStatus == 0) {
            promotions = promotionProductMapper.getPromotionsByNoStatus(queryCondition);
        }else {
            promotions = promotionProductMapper.getPromotionsByTrafficStatus(queryCondition);
        }

        MerchantPromotionListResponse  response = new MerchantPromotionListResponse();
        response.totalCount = totalCount;
        response.merchantPromotionResponses = this.convert4TrafficPromotionResponse(promotions);
        response.insufficientBalance = this.isInsufficientBalance(queryCondition.merchantId,promotionPlanType);
        return response;
    }

    @Override
    public List<BroadcastPromotionDO> queryOnlinePlanItemBySellerId(long sellerId, int pageNo, int pageSize) {
        List<BroadcastPromotion> broadcastPromotionList = planItemRepo.queryOnlinePlanItemBySellerId(sellerId,pageNo,pageSize);
        return broadcastPromotionList.stream().map(s-> BroadcastPromotionPlanConvert.convert(s)).collect(Collectors.toList());
    }


    private List<Long> getPageAdIds(int startRow,int pageSize,List<Long> adIdList){
        int endIndex = startRow + pageSize;
        endIndex = endIndex < 0 ? 0 : endIndex;
        endIndex = endIndex >= adIdList.size() ? adIdList.size() : endIndex;

        return adIdList.subList(startRow, endIndex);
    }


    private List<MerchantPromotionResponse> convert4BroadcastPromotionResponse(List<PromotionQueryResultDto> broadcastPromots){
        if (CollectionUtils.isEmpty(broadcastPromots)) {
            return  Collections.EMPTY_LIST;
        }

        List<MerchantPromotionResponse> responseList = broadcastPromots.stream()
                .map(
                        pqr -> {
                            MerchantPromotionResponse promotion = this.newMerchantPromotion(pqr);
                            promotion.broadcastPromotionDto = this.newBroadcastPromotion(pqr);
                            return promotion;
                        }
                )
                .collect(Collectors.toList());

        return responseList;
    }


    private List<MerchantPromotionResponse> convert4TrafficPromotionResponse(List<PromotionQueryResultDto> broadcastPromots){
        if (CollectionUtils.isEmpty(broadcastPromots)) {
            return  Collections.EMPTY_LIST;
        }

        List<MerchantPromotionResponse> responseList = broadcastPromots.stream()
                .map(
                        pqr -> {
                            TrafficPromotionResponse traffic = this.newTrafficPromotion(pqr);
                            traffic.trafficPromotionBusinessData = this.getReportData(pqr.getPlanId());
                            MerchantPromotionResponse promotion = this.newMerchantPromotion(pqr);
                            promotion.trafficPromotionDtos.add(traffic);
                            return promotion;
                        }
                    )
                .collect(Collectors.toList());

        return responseList;
    }

    private TrafficPromotionBusinessData getReportData(long planId){
        SellerTrafficPlanReport planReport = null;
        try {
            planReport = this.sellerTrafficPlanReportMapper.getFixPlanReport(planId);
        } catch (Exception e) {
            LOGGER.error("getReportData()->planId = {}",planId, e);
        }
        if (Objects.nonNull(planReport)){
            return new TrafficPromotionBusinessData(planReport.getImp(), planReport.getClick(), planReport.getConsumption(), planReport.getOrderAmount(), planReport.getGmv());
        }
        return null;
    }

    private BroadcastPromotionResponse newBroadcastPromotion(PromotionQueryResultDto pqr){
        BroadcastPromotionResponse broadcast = new BroadcastPromotionResponse();
        broadcast.planId = pqr.getPlanId();
        broadcast.promotionStart = pqr.getStartTime();
        broadcast.promotionEnd = pqr.getEndTime();
        broadcast.promotionBudget = pqr.getBudget();
        broadcast.promotionExpend = pqr.getConsumption() / 100.0;
        broadcast.planStatus = pqr.getPlanStatus();
        broadcast.unlimited = PromotionCycle.isUnlimited(pqr.getEndTime());

        if (Objects.nonNull(pqr.getStatus())) {
            broadcast.status = pqr.getStatus();
        }
        if (Objects.nonNull(pqr.getAnchorNum())) {
            broadcast.selectedAnchorNum = pqr.getAnchorNum();
        }

        return broadcast;
    }

    private TrafficPromotionResponse newTrafficPromotion(PromotionQueryResultDto pqr){
        TrafficPromotionResponse traffic = new TrafficPromotionResponse();
        traffic.planId = pqr.getPlanId();
        traffic.promotionStart = pqr.getStartTime();
        traffic.promotionEnd = pqr.getEndTime();
        traffic.promotionBudget = pqr.getBudget();
        traffic.promotionExpend = pqr.getConsumption()  / 100.0;
        traffic.planStatus = pqr.getPlanStatus();
        traffic.auditStatus = pqr.getAuditStatu();
        traffic.boothName = pqr.getBoothName();
        traffic.unlimited = PromotionCycle.isUnlimited(pqr.getEndTime());
        return traffic;
    }

    private MerchantPromotionResponse newMerchantPromotion(PromotionQueryResultDto pqr){
        MerchantPromotionResponse promotion = new MerchantPromotionResponse();
        promotion.promotionId = pqr.getAdId();
        promotion.spuId = pqr.getSpuid();
        promotion.productName = pqr.getProductName();
        promotion.productPrice = pqr.getPrice();
        promotion.productCategoryId = pqr.getCategoryId();
        promotion.productCategory = pqr.getCategoryName();
        promotion.merchantName = pqr.getMerchantName();
        promotion.trafficPromotionDtos = new ArrayList<>();
        return promotion;
    }

    private boolean isInsufficientBalance(long merchantId, PromotionPlanTypeEnum promotionPlanType){
        if (merchantId < 1){
            return false;
        }
        Advertiser advertiser = advertiserMapper.getAdvertiserByMerchantId(merchantId);
        if (promotionPlanType == PromotionPlanTypeEnum.BROADCAST){
            return advertiser.getBalance() > 0 ? false : true;
        }
        if (promotionPlanType == PromotionPlanTypeEnum.TRAFFIC){
            return advertiser.getTrafficBalance() > 0 ? false : true;
        }
        return false;
    }


    private QueryCondition convert2QueryCondition(long userId, PromotionQueryConditionDto queryConditionDto){
        //userId > 0: 商家
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.promotionName = queryConditionDto.promotionPlan;
        queryCondition.spuId = queryConditionDto.spuId;
        queryCondition.productName = queryConditionDto.productName;
        queryCondition.productCategoryId = queryConditionDto.productCategoryId;
        queryCondition.merchantName = queryConditionDto.merchantName;

        if (queryConditionDto.pageSize <= 0) {
            queryCondition.pageSize = 10;
        } else {
            queryCondition.pageSize = queryConditionDto.pageSize;
        }
        queryCondition.startRow = (queryConditionDto.pageNo - 1) * queryConditionDto.pageSize;

        if (queryConditionDto.promotionStatus < 10) {
            queryCondition.broadcastStatus = queryConditionDto.promotionStatus;
        } else {
            queryCondition.trafficStatus = queryConditionDto.promotionStatus - 10;
        }

        if ( userId > 0 ) {
            UserInfoDTO userInfoDTO = commonUtil.getUserInfo(userId);
            if (userInfoDTO != null) {
                queryCondition.merchantId = userInfoDTO.sellerId;
            }
        }

        return queryCondition;
    }
}
