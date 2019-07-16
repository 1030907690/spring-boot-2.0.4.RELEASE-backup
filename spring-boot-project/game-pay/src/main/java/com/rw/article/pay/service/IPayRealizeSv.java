package com.rw.article.pay.service;

import com.rw.article.common.jackson.JsonObject;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;

import java.util.Map;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 支付具体处理的service
 * @date 2018/8/117:05
 */
public interface IPayRealizeSv {

    JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String, String> params);

    String callback(PayPlatform payPlatform, Map<String, String> params);

}
