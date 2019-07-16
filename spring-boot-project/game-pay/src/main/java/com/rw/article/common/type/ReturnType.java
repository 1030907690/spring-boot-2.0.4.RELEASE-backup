package com.rw.article.common.type;

import java.util.Arrays;
import java.util.Optional;

/***
 * @author zhouzhongqing
 * 2018年1月20日12:04:31
 *返回类型枚举
 * */
public enum ReturnType {


    JUMP_PAGE_TYPE(0,"跳转页面"),
    RETURN_APP_TYPE(1,"app支付类型");


    private Integer code;
    private String name;


    ReturnType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static ReturnType convert(Integer code) {
        Optional<ReturnType> type = Arrays.stream(values()).filter(v -> v.getCode().equals(code)).findFirst();
        return type.orElse(null);
    }
}
