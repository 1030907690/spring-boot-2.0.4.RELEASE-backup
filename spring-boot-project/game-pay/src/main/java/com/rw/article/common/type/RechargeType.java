package com.rw.article.common.type;

import java.util.Arrays;
import java.util.Optional;

/***
 * @author zhouzhongqing
 * 2018年1月25日10:51:37
 * 充值类型枚举
 * */
public enum RechargeType {


    OFFICIAL_RECHARGE(1, "官方"),
    ALIPAY_RECHARGE(2, "支付宝"),
    WECHAT_RECHARGE(3, "微信"),
    QQ_RECHARGE(4, "QQ钱包充值"),
    UNIONPAY_RECHARGE(5, "银联"),
    SAFE_RECHARGE(6, "前端接口保险箱的存取"),
    GOLD_ACCESS(7, "金币<->保险箱的存取"),
    DEDUCT_MONEY(8, "后台扣金币"),
    SAFE_DEDUCT_MONEY(9, "后台操作(增、减)保险箱的金币"),
    REBATE_MONEY_RECHARGE(10, "返佣到保险箱金币变化"),
    GAME_IN_MONEY(11, "游戏中的金币增、减"),
    THIRD_PARTY_RECHARGE_GAME_IN_MONEY(12, "第三方支付充值保险箱变化");

    private Integer code;
    private String name;

    RechargeType(Integer code, String name) {
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

    public static RechargeType convert(Integer code) {
        Optional<RechargeType> type = Arrays.stream(values()).filter(v -> v.getCode().equals(code)).findFirst();
        return type.orElse(null);
    }
}
