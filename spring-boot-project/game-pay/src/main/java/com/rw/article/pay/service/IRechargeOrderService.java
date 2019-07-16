package com.rw.article.pay.service;

import com.rw.article.common.jackson.JsonObject;
import com.rw.article.pay.entity.RechargeOrder;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 订单的service
 * @date 2018/8/118:03
 */
public interface IRechargeOrderService {

    /***
     *zhouzhongqing
     * 2018年1月24日20:42:57
     * 用户下单
     * @param price
     * @param userId
     */
    JsonObject createRechargeOrder(String userId, String price, String payType, String type, String device, String remoteAddress);


    /*
    * zhouzhongqing
    * 2018年8月1日19:20:44
    * 根据orderId查询订单
    * */
   RechargeOrder getRechargeOrderByOrderNo(String orderId);


    /***
     * zhouzhongqing
     * 2018年5月21日15:52:27
     * 处理支付后的订单
     */
    int payAfterHandle(int line , RechargeOrder rechargeOrder, RechargeOrder beforeRechargeOrder);


}
