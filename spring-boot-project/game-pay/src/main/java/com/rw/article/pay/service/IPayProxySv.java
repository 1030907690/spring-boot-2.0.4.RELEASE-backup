package com.rw.article.pay.service;

import com.rw.article.common.jackson.JsonObject;
import com.rw.article.pay.entity.RechargeOrder;

import java.util.Map;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 根据handel字段跳转到不同的service
 * @date 2018/8/116:10
 */
public interface IPayProxySv {


    JsonObject gateway(RechargeOrder rechargeOrder, Map<String, String> params);

    String callback(int platform, Map<String, String> params);

}
