package com.rw.article.common.type;

import java.util.Arrays;
import java.util.Optional;

/***
 * @author zhouzhongqing
 * 2018年1月25日10:51:37
 * 关注表类型
 * */
public enum AttentionLogType {


    LOGIN(1, "登录"),
    WITHDRAW_ORDER(2, "提现订单"),
    RECHARGE_ORDER(3, "充值完成后订单");


    private Integer code;
    private String name;

    AttentionLogType(Integer code, String name) {
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

    public static AttentionLogType convert(Integer code) {
        Optional<AttentionLogType> type = Arrays.stream(values()).filter(v -> v.getCode().equals(code)).findFirst();
        return type.orElse(null);
    }
}
