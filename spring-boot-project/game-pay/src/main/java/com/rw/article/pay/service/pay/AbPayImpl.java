package com.rw.article.pay.service.pay;


import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.jackson.JsonUtils;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.IRechargeOrderRepository;
import com.rw.article.pay.service.IRechargeOrderService;
import com.rw.article.pay.service.pay.ab.SignHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@Service
public class AbPayImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(AbPayImpl.class);
    // 支付跳转网关
    private static final String API_GATEWAY = "https://web.iapppay.com/h5/gateway";
    // 创建订单API
    private static final String API_ORDER = "http://ipay.iapppay.com:9999/payapi/order";
    // 订单查询API
    // private static final String API_QUERY = "http://ipay.iapppay.com:9999/payapi/queryresult";

    @Resource
    private IRechargeOrderService rechargeOrderService;

    @Resource
    private IRechargeOrderRepository rechargeOrderRepository;

    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String, String> param) {
        JsonObject result = new JsonObject();
     //   try {
           /* String params = "transdata=%s&sign=%s&signtype=RSA";
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("appid", payPlatform.getMerchId());
            data.put("waresid", 1);
            data.put("waresname", order.getDetail());
            data.put("cporderid", order.getOrderNo());
            data.put("price", order.getPrice() / 100.0);
            data.put("currency", "RMB");
            data.put("appuserid", String.valueOf(System.currentTimeMillis()));
            data.put("cpprivateinfo", order.getAttach());
            data.put("notifyurl", order.getNotifyUrl());
            String json = JsonUtils.getInstance().toJson(data);
            String sign = SignHelper.sign(json, payPlatform.getMerchKey());

            String reqData = String.format(params, URLEncoder.encode(json, "UTF-8"), URLEncoder.encode(sign, "UTF-8"));
            String respData = HttpUtils.sentPost(API_ORDER, reqData, "UTF-8");

            Map<String, String> resultMap = SignUtils.getParameter(respData);
            if (StringUtils.isNotBlank(resultMap.get("signtype"))) {
                if (SignHelper.verify(resultMap.get("transdata"), resultMap.get("sign"), payPlatform.getMerchPublicKey())) {
                    Map map = JsonUtils.getInstance().toBean(resultMap.get("transdata"), Map.class);
                    String transId = (String) map.get("transid");
                    log.debug("爱贝：verify ok");

                    Map<String, Object> jsonObject = new HashMap<>();
                    jsonObject.put("tid", transId);
                    jsonObject.put("app", payPlatform.getMerchId());
                    jsonObject.put("url_r", order.getReturnUrl());
                    jsonObject.put("url_h", order.getReturnUrl());
                    String content = JsonUtils.getInstance().toJson(jsonObject);
                    sign = SignHelper.sign(content, payPlatform.getMerchKey());
                    String notifyUrl = API_GATEWAY + "?data=" + URLEncoder.encode(content, "UTF-8") + "&sign=" + URLEncoder.encode(sign, "UTF-8") + "&signtype=RSA";
                    result.put("redirect",notifyUrl);
                    result.put("message","success");
                    result.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
                    result.put("code",1);
                    return result;
                } else {
                    log.debug("爱贝：verify fail");
                }
            }
*/
       /* } catch (UnsupportedEncodingException e) {
            log.error("爱贝支付：创建订单失败", e);
        }*/
        result.put("message","error");
        result.put("code",0);
        return result;
    }

    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {
        String transData = params.get("transdata");
        String sign = params.get("sign");
        String signType = params.get("signtype");
       /* if (StringUtils.isNotBlank(signType) && SignHelper.verify(transData, sign, payPlatform.getMerchPublicKey())) {
            JsonObject data = JsonUtils.getInstance().toBean(transData, JsonObject.class);
            try {
                String orderNo = data.getString("cporderid");
                int price = (int) (data.getDouble("money") * 100);
                PayOrder order = orderService.selectByPrimaryKey(orderNo);
                if (order != null) {
                    order.setPlatformOrderNo(data.getString("transid"));
                    order.setPayType(data.getInt("paytype"));
                    order.setPrice(price);
                    order.setPayTime(DateUtils.parseDate(data.getString("transtime"), "yyyy-MM-dd HH:mm:ss"));
                    order.setNotifyTime(new Date());
                    order.setAttach(data.getString("cpprivate"));
                    order.setStatus(data.getInt("result") == 0 ? 1 : -1);
                    if (price == order.getPrice()) {
                        int count = orderService.updateByPrimaryKeySelective(order);
                        if (count > 0) {
                            return paySv.notify(order) ? "SUCCESS" : "FAIL";
                        }
                    } else {
                        log.error("订单数据异常：" + transData);
                    }
                } else {
                    return "FAIL";
                }
            } catch (ParseException e) {
                log.error("解析时间数据异常：", e);
                return "FAIL";
            }
        }*/
        return "SUCCESS";
    }
}
