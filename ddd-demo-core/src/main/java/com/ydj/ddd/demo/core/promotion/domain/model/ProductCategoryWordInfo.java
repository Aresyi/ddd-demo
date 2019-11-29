package com.ydj.ddd.demo.core.promotion.domain.model;

public class ProductCategoryWordInfo {

    private String topCategory;

    private String secondaryCategory;

    private String category;

    public ProductCategoryWordInfo(String topCategory, String secondaryCategory, String category) {
        this.topCategory = topCategory;
        this.secondaryCategory = secondaryCategory;
        this.category = category;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public String getSecondaryCategory() {
        return secondaryCategory;
    }

    public String getCategory() {
        return category;
    }
}
