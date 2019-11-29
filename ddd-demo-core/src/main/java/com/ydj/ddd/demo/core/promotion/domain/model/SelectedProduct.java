package com.ydj.ddd.demo.core.promotion.domain.model;

import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;
import org.apache.commons.lang3.StringUtils;

public class SelectedProduct {

    /**商品spuid*/
    private long spuId;

    /**商品名称*/
    private String productName;

    /**商品价格*/
    private double productPrice;

    /**商品分类Id*/
    private long productCategoryId;

    /**商品分类名称*/
    private String productCategory;

    /**商品图片*/
    private String picture;

    public SelectedProduct(long spuId, String productName, double productPrice, long productCategoryId, String productCategory) {
        this(spuId,productName,productPrice,productCategoryId,productCategory,"");
    }

    public SelectedProduct(long spuId, String productName, double productPrice, long productCategoryId, String productCategory,String picture) {
        this.spuId = spuId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategoryId = productCategoryId;
        this.productCategory = productCategory;
        this.picture = picture;

        this.checkInit();
    }

    private void checkInit(){
        if (this.spuId <= 0) {
            throw  new BusinessException(ExceptionCodeEnum.PARAMETER_ERROR);
        }
        if (StringUtils.isEmpty(picture)){
            this.picture = "";
        }
    }

    public long getSpuId() {
        return spuId;
    }

    public String getProductName() {
        return productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public long getProductCategoryId() {
        return productCategoryId;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getPicture() {
        return picture;
    }
}
