package com.rw.article.pay.service.pay;



import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.OtherUtils;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/***
 * @author zhouzhongqing
 * 2018年1月29日11:17:49
 * 钱进支付银联快捷支付
 * */
@Service
public class MoneyInUnionPayQuick implements IPayRealizeSv {

    private final String PAY_URL = "http://api.91518lm.com/order/order_add";

    private static final Logger log = LoggerFactory.getLogger(AbPayImpl.class);


 /*   @Reference
    private IPaySvApi paySv;
    @Reference
    private IOrderService orderService;
*/

    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String, String> params) {
        JsonObject jsonObject = new JsonObject();
        Map<String, String> paramsH5 = new LinkedHashMap<String, String>();
        paramsH5.put("mch", payPlatform.getMerchId());
        paramsH5.put("pay_type", "ylpay");
        paramsH5.put("money", order.getMoney().toString());
        paramsH5.put("time", String.valueOf(OtherUtils.getSecondTimestamp(new Date())));
        paramsH5.put("order_id", order.getOrderId());
        paramsH5.put("return_url", "http://");
        paramsH5.put("notify_url", payPlatform.getNotifyUrl());
        paramsH5.put("extra", order.getMoney().toString());
        StringBuffer signStrTemp = new StringBuffer();
        signStrTemp.append(paramsH5.getOrDefault("order_id", ""));
        signStrTemp.append(paramsH5.getOrDefault("money", ""));
        signStrTemp.append(paramsH5.getOrDefault("pay_type", ""));
        signStrTemp.append(paramsH5.getOrDefault("time", ""));
        signStrTemp.append(paramsH5.getOrDefault("mch", ""));
        signStrTemp.append(MD5Utils.MD5Encoding(payPlatform.getMerchKey()));
        paramsH5.put("sign", MD5Utils.MD5Encoding(signStrTemp.toString()));
        String requestUrl = OtherUtils.sendGetUrl(PAY_URL, paramsH5);
        jsonObject.put("redirect", requestUrl);
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
        jsonObject.put("code", Constants.SUCCESS_MARK);
        jsonObject.put("message", "success");
        return jsonObject;
    }


    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {
        log.info(" money in ylpay callback params  [ {} ]  ", params.toString());
        String order_id = params.getOrDefault("order_id", "");
        String orderNo = params.getOrDefault("orderNo", "");
      /*  int money = Integer.parseInt(params.getOrDefault("money", "0"));
        int mch = Integer.parseInt(params.getOrDefault("mch", ""));
        String pay_type = params.getOrDefault("pay_type", "");
        //商户下单时传的产品名称
        String commodity_name = params.getOrDefault("commodity_name", "");
        String extra = params.getOrDefault("extra", "");
        //银行交易单号
        String transactionId = params.getOrDefault("transactionId", "");
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
                            int count = orderService.updateByPrimaryKeySelective(payOrder);
                            if (count > 0) {
                                return paySv.notify(payOrder) ? "SUCCESS" : "FAIL";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] 单位分", price);
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", order_id);
                    }
                }
            } else {
                return "FAIL";
            }
        } else {
            //验签失败
            return "FAIL";
        }*/
        return "FAIL";
    }
}
