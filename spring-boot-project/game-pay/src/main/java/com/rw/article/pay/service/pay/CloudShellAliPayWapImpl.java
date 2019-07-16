package com.rw.article.pay.service.pay;

import com.alibaba.fastjson.JSON;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.http.HttpClient;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.OtherUtils;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.common.utils.pay.bird.MD5Util;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.entity.SysConfig;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.IRechargeOrderRepository;
import com.rw.article.pay.service.IRechargeOrderService;
import com.rw.article.pay.service.ISysConfigRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.bcel.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 云贝支付宝网页支付
 * @date 2018/9/26 11:23
 */
@Service
public class CloudShellAliPayWapImpl implements IPayRealizeSv {
    private static final Logger log = LoggerFactory.getLogger(CloudShellAliPayWapImpl.class);

    /**
     * 支付类型
     **/
    private final String PAY_TYPE = "2002";

    @Resource
    private IRechargeOrderService rechargeOrderService;

    @Resource
    private IRechargeOrderRepository rechargeOrderRepository;

    @Resource
    private BaseDao baseDao;


    @Resource
    private ISysConfigRepository sysConfigRepository;


    /**
     * 调用支付
     *
     * @param payPlatform 支付平台
     * @param order       订单
     * @param params      参数
     * @return 返回支付结果
     */
    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String, String> params) {

        log.info("调用云贝支付宝wap支付");
        JsonObject jsonObject = new JsonObject();
        Map<String, String> map = new TreeMap<>();
        String redirectUrl = null;
        try {
            SysConfig pageConfig = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("href_backurl_page", 1);
            if (null == pageConfig) {
                pageConfig = new SysConfig();
                pageConfig.setItemVal("https://pro.hzqp777.com/newDown/index.html");
            }
            //商户ID
            map.put("cpId", payPlatform.getMerchId().split(",")[0]);
            //服务id
            map.put("serviceId", payPlatform.getMerchId().split(",")[1]);
            map.put("payType", PAY_TYPE);
            //支付金额（单位分）
            map.put("fee", order.getMoney().toString());
            map.put("subject", "shop_" + order.getMoney().toString());
            map.put("description", "shop_" + order.getMoney().toString());
            //商户订单号
            map.put("orderIdCp", order.getOrderId());
            //异步回调地址
            map.put("notifyUrl", payPlatform.getNotifyUrl());
            //前端回调地址
            map.put("callbackUrl", pageConfig.getItemVal());
            //透传参数
            map.put("cpParam", order.getOrderId());
            //当前时间戳（13 位）
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));
            //用户ip
            map.put("ip", params.getOrDefault("spbillCreateIp", "127.0.0.1"));
            //版本号
            map.put("version", "1");

            String[] signStrArray = {"cpId", "serviceId", "payType", "fee", "subject", "description",
                    "orderIdCp", "notifyUrl", "callbackUrl", "timestamp", "ip", "version"};
            Map<String,String> signMap = new TreeMap<>();
            for (int i = 0; i < signStrArray.length; i++) {
                signMap.put(signStrArray[i],map.getOrDefault(signStrArray[i],""));
            }
            StringBuffer signStr = new StringBuffer();
            for (Map.Entry<String,String> entry : signMap.entrySet()) {
                signStr.append(entry.getKey() + "=" + entry.getValue()+ "&");
            }
            if (null != signStr && signStr.length() > 0) {
                signStr.deleteCharAt(signStr.length()-1);
            }

            //签名
            map.put("sign",MD5Utils.MD5Encoding(signStr.toString()+"&"+payPlatform.getMerchKey()).toUpperCase());


            log.info("请求参数 [ {} ]" , JSON.toJSONString(map));
            String responseBody = HttpClient.sendHttpRequestPost(payPlatform.getPayGetUrl(), JSON.toJSONString(map), HttpClient.ENCODING);
            log.info("responseBody [ {} ] " ,responseBody);
            if (null != responseBody && !"".equals(responseBody)) {
                JsonObject respJsonObj = new JsonObject(responseBody);
                if ("0".equals(respJsonObj.getString("status"))) {
                    redirectUrl = respJsonObj.getString("payUrl");
                    jsonObject.put("code", Constants.SUCCESS_MARK);
                } else {
                    jsonObject.put("message", respJsonObj.getString("message"));
                    jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
                }
            } else {
                jsonObject.put("message", Constants.NO_RETURN_ERROR);
                jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
            }

        } catch (Exception e) {
            log.error("获取参数错误 [{}] [ {} ] [ {} ]", e, e.getMessage(), e.getStackTrace());
            jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
        }
        jsonObject.put("redirect", redirectUrl);
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
        return jsonObject;
    }


    /**
     * 支付回调
     *
     * @param payPlatform 支付平台
     * @param params      参数
     * @return 回调信息
     */
    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {
        log.info("   CloudShellAliPayWapImpl callback params  [ {} ]  ", params.toString());
        //回调参数

        JsonObject jsonObject = new JsonObject(params.getOrDefault("body",""));
        //状态码（0 支付成功）
        String status = jsonObject.getString("status");
        //订单号
        String orderId = jsonObject.getString("orderId");
        //商户订单号
        String orderIdCp = jsonObject.getString("orderIdCp");
        //官方流水号
        String linkId = jsonObject.getString("linkId");
        //支付金额（单位分）
        String fee = jsonObject.getString("fee");
        //支付时间戳（13 位）
        String payTime = jsonObject.getString("payTime");
        //商户透传
        String cpParam = jsonObject.getString("cpParam");
        //版本号
        String version = jsonObject.getString("version");
        //签名
        String sign = jsonObject.getString("sign");


        String signTemp = "fee="+fee+"&orderIdCp="+orderIdCp+"&version="+version+"&"+payPlatform.getMerchKey();
        String signStr = MD5Utils.MD5Encoding(signTemp).toUpperCase();
        log.info("signStr:  [ {} ]", signStr);


        if (StringUtils.equals(sign, signStr)) {
            if ("0".equals(status)) {
                if (null != orderIdCp && !"".equals(orderIdCp)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(orderIdCp);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(orderIdCp);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        //使用订单金额比较
                        Integer price_amount = Integer.parseInt(fee);
                        if (price.equals(price_amount)) {
                            if (rechargeOrder.getPayStatus().equals(0)) {
                                Date nowTime = new Date();
                                rechargeOrder.setPayTime(nowTime);
                                rechargeOrder.setStatus(1);
                                rechargeOrder.setPayStatus(1);
                                int count = 0;

                                count = rechargeOrderService.payAfterHandle(count, rechargeOrder, beforeRechargeOrder);
                                log.info("payAfterHandle count [{}] rechargeOrder [{}]", count, JSON.toJSONString(rechargeOrder));
                                if (count > 0) {
                                    count = baseDao.updateByEntity(rechargeOrder);
                                }

                                return count > 0 ? "success" : "FAIL";
                            } else {
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId());
                                return "success";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", price_amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", orderIdCp);
                        return "FAIL";
                    }
                }
            } else {
                log.info("状态异常");
                return "FAIL";
            }
        } else {
            log.error("签名错误");
            return "FAIL";
        }
        log.info("支付失败-其他异常");
        return "FAIL";
    }
}
