package com.ydj.ddd.demo.core.promotion.domain.model;

import com.ydj.ddd.demo.core.promotion.exception.BusinessException;
import com.ydj.ddd.demo.core.promotion.exception.ExceptionCodeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class AdMaterialInfo {

    /**素材id*/
    private long id;

    /**素材名称*/
    private String name;

    /**素材url*/
    private String url;

    /**素材宽*/
    private int width;

    /**素材高*/
    private int height;

    /**素材title*/
    private String title;

    /**素材子title*/
    private String subTitle;

    /**是否展示广告标 0=隐藏 1=显示*/
    private boolean showAdTag = true;

    /**广告素材模版*/
    private AdMaterialTemplateInfo adMaterialTemplateInfo;

    /**素材来源 0:广告系统，1:外部系统*/
    private int materialSource;

    private int operations =1;

    public AdMaterialInfo(AdMaterialTemplateInfo adMaterialTemplateInfo, boolean showAdTag) {
        this.adMaterialTemplateInfo = adMaterialTemplateInfo;
        this.showAdTag = showAdTag;
        this.materialSource = 1;
    }

    public AdMaterialInfo(long id, String name, String url, int width, int height, String title, String subTitle,
                          boolean showAdTag, AdMaterialTemplateInfo adMaterialTemplateInfo, int materialSource) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.width = width;
        this.height = height;
        this.title = title;
        this.subTitle = subTitle;
        this.showAdTag = showAdTag;
        this.adMaterialTemplateInfo = adMaterialTemplateInfo;
        this.materialSource = materialSource;

        this.checkInit();
    }

    private void checkInit(){
        if (materialSource == 0 && StringUtils.isEmpty(url)) {
            throw new BusinessException(ExceptionCodeEnum.NO_MATERIAL);
        }
    }

    public boolean isNeedMaterialElement(){
        if (Objects.isNull(adMaterialTemplateInfo) || materialSource == 1) {//素材来源 0:广告系统，1:外部系统
            return false;
        }
        return true;
    }

    public int getOpenMode(){//1:app-unify-webview,2:app-embed-browser
        if (operations == 1 || operations == 2) {
            return operations;
        }
        return 2;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return Objects.isNull(name) ? "" : name;
    }

    public String getUrl() {
        return Objects.isNull(url) ? "" : url;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public int getMaterialSource() {
        return materialSource;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isShowAdTag() {
        return showAdTag;
    }

    public AdMaterialTemplateInfo getAdMaterialTemplateInfo() {
        return adMaterialTemplateInfo;
    }

    public int getOperations() {
        return operations;
    }
}