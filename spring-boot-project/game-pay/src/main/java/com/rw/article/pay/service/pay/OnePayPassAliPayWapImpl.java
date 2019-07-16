package com.rw.article.pay.service.pay;

import com.alibaba.fastjson.JSON;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.http.HttpClient;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.OtherUtils;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.entity.SysConfig;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.IRechargeOrderRepository;
import com.rw.article.pay.service.IRechargeOrderService;
import com.rw.article.pay.service.ISysConfigRepository;
import org.apache.commons.lang3.StringUtils;
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
 * @Description: 一付通支付宝wap支付
 * @date 2018/9/29 17:08
 */
@Service
public class OnePayPassAliPayWapImpl implements IPayRealizeSv {
    private static final Logger log = LoggerFactory.getLogger(OnePayPassAliPayWapImpl.class);

    /**
     * 支付类型
     **/
    private final String PAY_TYPE = "alipay";

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

        log.info("调用一付通支付宝wap支付");
        JsonObject jsonObject = new JsonObject();
        TreeMap<String, String> map = new TreeMap<>();
        TreeMap<String, String> mapTemp = new TreeMap<>();
        String redirectUrl = null;
        try {


            mapTemp.put("TransCode", "130101");
            mapTemp.put("Appid", payPlatform.getMerchId());
            mapTemp.put("Version", "1.0.0.1");
            mapTemp.put("Out_Trade_No", order.getOrderId().replace("_", ""));
            //订单总金额，单位为元，精确到小数点后两位
            mapTemp.put("Total_Amount", OtherUtils.moneyRetainDecimal(OtherUtils.getGoldIsMultipl(order.getRechargePrice())));
            mapTemp.put("Subject", "shop_" + OtherUtils.moneyRetainDecimal(OtherUtils.getGoldIsMultipl(order.getRechargePrice())));
            mapTemp.put("Method", PAY_TYPE);
            mapTemp.put("Notify_Url", payPlatform.getNotifyUrl());
            mapTemp.put("Description", "Description");


            StringBuffer signKey = new StringBuffer();

            for (Map.Entry<String, String> entry : mapTemp.entrySet()) {
                signKey.append(entry.getKey() + "=" + entry.getValue() + "&");
            }

            signKey.deleteCharAt(signKey.length() - 1);
            String signTemp = MD5Utils.MD5Encoding(signKey.toString() + payPlatform.getMerchKey());

            map.put("Body", JSON.toJSONString(mapTemp));
            map.put("SignKey", signTemp);
            map.put("TransCode", "130101");
            String responseBody = HttpClient.sendHttpRequestPost(payPlatform.getPayGetUrl(), map, HttpClient.ENCODING);

            log.info("responseBody [ {} ]", responseBody);

            // {"ResultCode":0,"ErrCode":100112,"ErrMsg":"SUCCESS","Body":{"Sdorder_No":"1538221653187shop100","Pay_Url":"HTTPS:\/\/QR.ALIPAY.COM\/FKX04177YC9K6XWKGMYB33","Money":"100.00","Risk_Money":"100.00","Expire_Time":1538221955}}
            JsonObject respJsonObject = new JsonObject(responseBody);
            if ("SUCCESS".equals(respJsonObject.getString("ErrMsg"))) {
                JsonObject body = new JsonObject(respJsonObject.getString("Body"));
                if(StringUtils.isNotBlank(body.getString("Pay_Url"))){

                    redirectUrl = body.getString("Pay_Url");
                    //修改订单号
                    RechargeOrder temp = new RechargeOrder();
                    temp.setId(order.getId());
                    temp.setOrderId(mapTemp.getOrDefault("Out_Trade_No", ""));
                    baseDao.updateByEntity(temp);
                    jsonObject.put("code", Constants.SUCCESS_MARK);
                }else{
                    jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
                }
            }else{
                jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
                jsonObject.put("message",respJsonObject.getString("ErrMsg"));
            }

        } catch (Exception e) {
            log.error("获取参数错误 [{}] [ {} ] [ {} ]", e, e.getMessage(), e.getStackTrace());
            jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
            jsonObject.put("message",Constants.SYSTEM_ERROR);
        }
        jsonObject.put("redirect", redirectUrl);
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
        return jsonObject;
    }


    public static void main(String[] args) {

        //{Status=1, Pay_Type=alipay, Status_Msg=SUCCESS, body=, Out_Trade_No=1538223047056shop1,
        // Money=1.00, SignKey=a95397516b406b9a85437cf14ab281bd, Pay_Time=1538223187, Appid=25687709, Version=1.0.0.1,
        // Risk_Money=1.00, Real_Money=1.00, Sign_Type=MD5}
        Map<String,String> map = new HashMap<>();
        map.put("Status","1");
        map.put("Pay_Type","alipay");
        map.put("Status_Msg","SUCCESS");
        map.put("Out_Trade_No","1538223047056shop1");
        map.put("Money","1.00");
        map.put("SignKey","a95397516b406b9a85437cf14ab281bd");
        map.put("Pay_Time","1538223187");
        map.put("Appid","25687709");
        map.put("Version","1.0.0.1");
        map.put("Risk_Money","1.00");
        map.put("Real_Money","1.00");
        map.put("Sign_Type","MD5");


        PayPlatform payPlatform = new PayPlatform();
        payPlatform.setMerchKey("532f738db4d491e3b1166a1fcf5aff83");

        new OnePayPassAliPayWapImpl().callback(payPlatform,map);
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
        log.info("   OnePayPassAliPayWapImpl callback params  [ {} ]  ", params.toString());
        //回调参数


        //商户号
        String appId = params.getOrDefault("Appid", "");
        //接口版本号
        String Version = params.getOrDefault("Version", "");
        //原商户订单号
        String outTradeNo = params.getOrDefault("Out_Trade_No", "");
        //订单金额，单位为元，精确到小数点后两位
        String money = params.getOrDefault("Money", "");
        //实收金额
        String realMoney = params.getOrDefault("Real_Money", "");
        //风控金额
        String riskMoney = params.getOrDefault("Risk_Money", "");
        //支付状态
        String status = params.getOrDefault("Status", "");
        //状态描述
        String statusMsg = params.getOrDefault("Status_Msg", "");
        //支付通道
        String payType = params.getOrDefault("Pay_Type", "");
        //支付时间戳
        String payTime = params.getOrDefault("Pay_Time", "");
        //签名串
        String signKey = params.getOrDefault("SignKey", "");
        //签名方式
        String signType = params.getOrDefault("Sign_Type", "");


        Map<String, Object> map = new TreeMap<>();
        for (String s : params.keySet()) {
            String value = params.get(s);
            if (StringUtils.isNotBlank(value) && !StringUtils.equals(s, "SignKey") && !StringUtils.equals(s, "Sign_Type")) {
                map.put(s, value);
            }
        }

        StringBuffer signKeySb = new StringBuffer();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            signKeySb.append(entry.getKey() + "=" + entry.getValue() + "&");
        }
        if (null != signKeySb && signKeySb.length() > 0) {
            signKeySb.deleteCharAt(signKeySb.length() - 1);
        }

        String signStr = MD5Utils.MD5Encoding(signKeySb.toString() + payPlatform.getMerchKey());
        log.info("signStr:  [ {} ]", signStr);


        if (StringUtils.equals(signKey, signStr)) {
            if ("1".equals(status)) {
                if (null != outTradeNo && !"".equals(outTradeNo)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(outTradeNo);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(outTradeNo);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        //乘100来比较   --- 使用订单金额比较
                        Integer price_amount = Integer.parseInt(String.valueOf(OtherUtils.multiply(Double.parseDouble(money), 100)));
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
                                    rechargeOrder.setRemark(String.format(Constants.RECHARGE_ORDER_REMAKE,rechargeOrder.getUserId(),realMoney));
                                    count = baseDao.updateByEntity(rechargeOrder);
                                }

                                return count > 0 ? "SUCCESS" : "FAIL";
                            } else {
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId());
                                return "SUCCESS";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", price_amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", outTradeNo);
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