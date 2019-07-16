package com.rw.article.pay.service.pay;


import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.OtherUtils;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/***
 * @author zhouzhongqing
 * 2018年1月26日14:57:33
 * 钱进支付h5网页支付
 * */
@Service
public class MoneyInWechatH5Impl implements IPayRealizeSv {
    private static final Logger log = LoggerFactory.getLogger(MoneyInWechatH5Impl.class);


    private final String PAY_URL = "http://api.91518lm.com/order/order_add";


    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String, String> params) {
        JsonObject jsonObject = new JsonObject();
        Map<String, String> paramsH5 = new LinkedHashMap<String, String>();
        paramsH5.put("mch", payPlatform.getMerchId());
        paramsH5.put("pay_type", "wxhtml");
        paramsH5.put("money",String.valueOf(order.getMoney() - RandomUtils.nextInt(1,9))); //money必须为小数
        paramsH5.put("time", String.valueOf(OtherUtils.getSecondTimestamp(new Date())));
        paramsH5.put("order_id", order.getOrderId());
        paramsH5.put("return_url", "http://");
        paramsH5.put("notify_url", payPlatform.getNotifyUrl());
        paramsH5.put("extra", order.getMoney().toString());
        paramsH5.put("create_ip",params.getOrDefault("spbillCreateIp","127.0.0.1"));
        StringBuffer signStrTemp = new StringBuffer();
        signStrTemp.append(paramsH5.getOrDefault("order_id", ""));
        signStrTemp.append(paramsH5.getOrDefault("money", ""));
        signStrTemp.append(paramsH5.getOrDefault("pay_type", ""));
        signStrTemp.append(paramsH5.getOrDefault("time", ""));
        signStrTemp.append(paramsH5.getOrDefault("mch", ""));
        signStrTemp.append(MD5Utils.MD5Encoding(payPlatform.getMerchKey()));
        paramsH5.put("sign", MD5Utils.MD5Encoding(signStrTemp.toString()));
        String requestUrl = OtherUtils.sendGetUrl(PAY_URL,paramsH5);
        jsonObject.put("code", Constants.SUCCESS_MARK);
        jsonObject.put("redirect",requestUrl);
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
        jsonObject.put("message", "success");
        return jsonObject;
    }


    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {

        log.info(" money in h5 callback params  [ {} ]  ",params.toString());
        String order_id = params.getOrDefault("order_id", "");
        String orderNo = params.getOrDefault("orderNo", "");
        int money = Integer.parseInt(params.getOrDefault("money", "0"));
        long mch = Long.parseLong(params.getOrDefault("mch", ""));
        String pay_type = params.getOrDefault("pay_type", "");
        //商户下单时传的产品名称
        String commodity_name = params.getOrDefault("commodity_name", "");
        //透传参数
        String extra = params.getOrDefault("extra", "");
        //银行交易单号
       /* String transactionId = params.getOrDefault("transactionId", "");
        int status = Integer.parseInt(params.getOrDefault("status", "0"));
        String sign = params.getOrDefault("sign", "");
        int time = Integer.parseInt(params.getOrDefault("time", "0"));
        if (OtherUtils.verificationMoneyInSign(order_id, orderNo, money, mch, pay_type, sign, time, payPlatform.getMerchKey())) {
            //验签成功
            if (1 == status) {
                if (null != order_id && !"".equals(order_id)) {
                    PayOrder payOrder = orderService.selectByMerchantOrderNo(order_id);
                    if (null != payOrder) {
                        Integer price = payOrder.getPrice();
                        if (price.equals(Integer.parseInt(extra))) {
                            payOrder.setPlatformOrderNo(order_id);
                            payOrder.setTransactionId(orderNo);
                            payOrder.setPayType(PayType.pay_type_moneyin_wxhtml.getCode());
                            Date nowTime = new Date();
                            payOrder.setPayTime(nowTime);
                            payOrder.setNotifyTime(nowTime);
                            payOrder.setAttach(String.valueOf(money));
                            payOrder.setStatus(1);
                            payOrder.setDetail("商品价格(分): "+extra + " 用户真实支付(分): "+money);
                            int count = orderService.updateByPrimaryKeySelective(payOrder);
                            if (count > 0) {
                                return paySv.notify(payOrder) ? "SUCCESS" : "FAIL";
                            }
                        }else{
                            log.info("订单金额异常 [ {} ] 单位分",price);
                        }
                    }else{
                        log.info("没有这个商户订单号 [ {} ]",order_id);
                    }
                }
            } else {
                log.info("订单状态异常 [ {} ]",status);
                return "FAIL";
            }
        } else {
            log.info("验签失败");
            //验签失败
            return "FAIL";
        }*/
        return "FAIL";
    }



}
