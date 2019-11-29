package com.ydj.ddd.demo.core.promotion.domain.model;

public class AdMaterialTemplateInfo {

    private Long id;

    private int contentType;

    private Integer isClick;

    private Integer materialStyle;

    private Integer materialSource;

    private Integer accessContent;

    public AdMaterialTemplateInfo(Long id, int contentType, Integer isClick, Integer materialStyle, Integer materialSource, Integer accessContent) {
        this.id = id;
        this.contentType = contentType;
        this.isClick = isClick;
        this.materialStyle = materialStyle;
        this.materialSource = materialSource;
        this.accessContent = accessContent;
    }

    public Long getId() {
        return id;
    }

    public int getContentType() {
        return contentType;
    }

    public Integer getIsClick() {
        return isClick;
    }


    public Integer getMaterialStyle() {
       return materialStyle;
    }

    public Integer getStyle() {
        if (materialStyle == null) {
            return 1;
        }
        switch (materialStyle) {
            case 0:
                return 0;
            case 1:
                return 3;
            case 2:
                return 1;
            case 16:
                return 4;
            case 32:
                return 5;
            default:
                return 1;
        }
    }

    public Integer getMaterialSource() {
        return materialSource;
    }

    public Integer getAccessContent() {
        return accessContent;
    }
}
