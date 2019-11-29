package com.ydj.ddd.demo.core.promotion.domain.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class SelectedKeyword {

    /**匹配模式*/
    private KeywordMatchMode keywordMatchMode ;

    /**关键字来源类型（依赖于所选择的广告位配置）*/
    private int keywordSource;

    /**商品类目词*/
    private ProductCategoryWordInfo productCategoryWordInfo;

    /**新关键字*/
    private List<BidKeywordInfo> newBidKeywordInfoList;

    /**老关键字*/
    private List<BidKeywordInfo> oldBidKeywordInfoList;

    public SelectedKeyword(KeywordMatchMode keywordMatchMode, int keywordSource, List<BidKeywordInfo> newBidKeywordInfoList) {
        this.keywordMatchMode = keywordMatchMode;
        this.keywordSource = keywordSource;
        this.newBidKeywordInfoList = newBidKeywordInfoList;

        this.checkInit();
    }

    private void checkInit(){
        if(Objects.isNull(keywordMatchMode)
                || (keywordMatchMode.getIntelMode() == 0 && CollectionUtils.isEmpty(newBidKeywordInfoList))){//非智能匹配必有的有选择关键词
            throw new BusinessException(ExceptionCodeEnum.NO_KEYWORDS);
        }

        if ( CollectionUtils.isNotEmpty(newBidKeywordInfoList) ) {
            boolean isRepeated = newBidKeywordInfoList.stream().collect(groupingBy(it -> it.getKeyword(), counting())).values().stream().anyMatch(it -> it > 1);
            if(isRepeated){
                throw new BusinessException(ExceptionCodeEnum.KEYWORDS_REPEATED);
            }
        }

    }

    public List<BidKeyword> getNewBidKeyword(long planId, long advertiserId, long spuId){
        if (CollectionUtils.isEmpty(newBidKeywordInfoList)) {
            return Collections.EMPTY_LIST;
        }

        List<BidKeyword> keywordList = newBidKeywordInfoList.stream()
                .map(src -> this.convert2BidKeyword(planId,advertiserId,spuId,src))
                .collect(Collectors.toList());

        return keywordList;
    }

    public Pair<List<BidKeyword>,List<BidKeyword>> getNewAndOldBidKeyword(long planId, long advertiserId, long spuId){

        List<BidKeyword> need2AddList = Lists.newArrayList();
        List<BidKeyword> need2UpdateList = Lists.newArrayList();

        if (CollectionUtils.isEmpty(newBidKeywordInfoList)) {
            return Pair.of(need2AddList,need2UpdateList);
        }

        Map<String, BidKeywordInfo> newBidKeywordMap = newBidKeywordInfoList.stream()
                .collect(
                        Collectors.toMap(
                                s -> s.getKeyword(),
                                Function.identity()
                        )
                );

        Map<String, BidKeywordInfo> oldBidKeywordMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(oldBidKeywordInfoList)) {
            oldBidKeywordMap = oldBidKeywordInfoList.stream()
                    .collect(
                            Collectors.toMap(BidKeywordInfo::getKeyword,
                                    Function.identity(),
                                    (a, b) -> a
                            )
                    );
        }

        for (Map.Entry<String,BidKeywordInfo> one : newBidKeywordMap.entrySet()){
            String keyword = one.getKey();
            BidKeywordInfo newBidKeywordInfo = one.getValue();

            BidKeywordInfo oldBidKeywordInfo = oldBidKeywordMap.get(keyword);
            if (Objects.isNull(oldBidKeywordInfo)) {
                BidKeyword bidKeyword = this.convert2BidKeyword(planId, advertiserId, spuId, newBidKeywordInfo);
                need2AddList.add(bidKeyword);
                continue;
            }

            long nPrice = BigDecimal.valueOf(newBidKeywordInfo.getCpcPrice()).multiply(BigDecimal.valueOf(100)).longValue();
            int nState = newBidKeywordInfo.getState();
            if (nState != oldBidKeywordInfo.getState() || nPrice != oldBidKeywordInfo.getCpcPrice()) { //判断价格，状态，有没有改变
                BidKeyword bidKeyword = new BidKeyword();
                bidKeyword.setKeyword(keyword);
                bidKeyword.setState(nState);
                bidKeyword.setPrice(nPrice);
                bidKeyword.setType(newBidKeywordInfo.getType());
                bidKeyword.setPlanId(oldBidKeywordInfo.getPlanId());

                need2UpdateList.add(bidKeyword);
            }
        }

        return Pair.of(need2AddList,need2UpdateList);
    }

    public List<TargettingKeywords> getTargettingKeywords(long planId){
        if (keywordSource != 2 || Objects.isNull(productCategoryWordInfo)){
            return Collections.EMPTY_LIST;
        }
        List<TargettingKeywords> list = Arrays.asList(productCategoryWordInfo.getTopCategory(),
                productCategoryWordInfo.getSecondaryCategory(),
                productCategoryWordInfo.getCategory()).stream().map(
                it -> {
                    TargettingKeywords one = new TargettingKeywords();
                    one.setValue(it);
                    one.setPlanId(planId);
                    one.setType("1");
                    return one;
                }).collect(toList());
        return  list;
    }

    private BidKeyword convert2BidKeyword(long planId, long advertiserId, long spuId,BidKeywordInfo src){
        BidKeyword result = new BidKeyword();
        result.setSpuid(spuId);
        result.setPlanId(planId);
        result.setKeyword(src.getKeyword().toLowerCase());
        result.setPrice(BigDecimal.valueOf(src.getCpcPrice()).multiply(BigDecimal.valueOf(100)).longValue());
        result.setSellerId(advertiserId);
        result.setType(src.getType());
        result.setState(src.getState());
        result.setCreateType(src.getCreateType());
        return result;
    }

    public KeywordMatchMode getKeywordMatchMode() {
        return keywordMatchMode;
    }

    public int getKeywordSource() {
        return keywordSource;
    }


    public void setOldBidKeywordInfoList(List<BidKeywordInfo> oldBidKeywordInfoList) {
        this.oldBidKeywordInfoList = oldBidKeywordInfoList;
    }

    public void setProductCategoryWordInfo(ProductCategoryWordInfo productCategoryWordInfo) {
        this.productCategoryWordInfo = productCategoryWordInfo;
    }
}
