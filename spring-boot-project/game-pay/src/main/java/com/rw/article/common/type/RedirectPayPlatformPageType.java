package com.rw.article.common.type;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description:  转发的页面
 * @date 2018/11/6 15:15
 */
public enum RedirectPayPlatformPageType {

    DEFAULT_PAGE(0,"redirect/default_platform","默认的支付转发页面");


    private Integer code;
    private String url;
    private String desc;


    RedirectPayPlatformPageType(Integer code, String url,String desc) {
        this.code = code;
        this.url = url;
        this.desc = desc;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static RedirectPayPlatformPageType convert(Integer code) {
        Optional<RedirectPayPlatformPageType> type = Arrays.stream(values()).filter(v -> v.getCode().equals(code)).findFirst();
        return type.orElse(null);
    }
}
