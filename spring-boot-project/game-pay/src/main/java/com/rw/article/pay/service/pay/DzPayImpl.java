package com.rw.article.pay.service.pay;



import com.rw.article.common.jackson.JsonObject;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class DzPayImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(DzPayImpl.class);
    private static final String API_HOST = "http://api.0592pay.com";
    private static final String WX_PAY = API_HOST + "/Order/ToPay";
/*
    @Reference
    private IPaySvApi paySv;
    @Reference
    private IOrderService orderService;
*/

    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String,String> param) {
        JsonObject resultJson = new JsonObject();
      /*  try {
            Map<String, String> params = new HashMap<>();
            params.put("AppId", payPlatform.getMerchId());
            params.put("MerchantOrderNo", order.getOrderNo());
            params.put("ProductName", order.getBody());
            params.put("ProductDescription", order.getDetail());
            params.put("Amount", String.valueOf(order.getPrice() / 100.0));
            params.put("NotifyUrl", order.getNotifyUrl());
            params.put("PayChannel", "1201");
            params.put("SceneInfo", "{\"h5_info\":{\"type\":\"Wap\",\"wap_url\":\"" + order.getReturnUrl() + "\",\"wap_name\":\"" + order.getBody() + "\"}}");
            params.put("Client_Ip", order.getClientIp());
            params.put("ReqDate", DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
            params.put("ExtMsg", order.getAttach());
            params.put("Sign", MD5Utils.MD5(OtherUtils.formatURL(params) + "&key=" + payPlatform.getMerchKey()));
            String result = HttpUtils.getInstance().httpPost(WX_PAY, params);
            JsonObject jsonObject = new JsonObject(XmlUtils.toMap(result));
            if (jsonObject.getInt("RespType") == 0) {
               String ToPayData = jsonObject.getString("ToPayData");
                resultJson.put("code",1);
                resultJson.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
                resultJson.put("toPayData",ToPayData);
                return resultJson;

            } else{
                log.debug("创建订单失败：" + jsonObject.toString());
            }
        } catch (Exception e) {
            log.error("处理异常：", e);
        }*/
        resultJson.put("code",0);
        resultJson.put("message","error");
        return resultJson;
    }

    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {
       /* JsonObject data = new JsonObject(params);
        String sign = data.getString("Sign");
        int status = data.getInt("Status");
        if (StringUtils.isNotBlank(sign) && status == 1 && verify(sign)) {
            String orderNo = data.getString("MerchantOrderNo");
            int price = (int) (data.getDouble("Amount") * 100);
            PayOrder order = orderService.selectByPrimaryKey(orderNo);
            if (order != null) {
                order.setPlatformOrderNo(data.getString("PlatformOrderNo"));
                order.setPrice(price);
                order.setPayTime(new Date());
                order.setNotifyTime(new Date());
                order.setAttach(data.getString("ExtMsg"));
                order.setStatus(1);
                if (price == order.getPrice()) {
                    int count = orderService.updateByPrimaryKeySelective(order);
                    if (count > 0) {
                        return paySv.notify(order) ? "ok" : "";
                    }
                } else {
                    log.error("订单数据异常：" + data.toString());
                }
            }
        }*/
        return "";
    }

    private boolean verify(String sign) {
        return !sign.isEmpty();
    }
}
