package com.rw.article.pay.service.pay;



import com.rw.article.common.jackson.JsonObject;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/***
 * @author zhouzhongqing
 * 2018年1月22日08:22:06
 * 支付宝h5网页支付
 * */
@Service
public class AliPayH5PayImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(AliPayH5PayImpl.class);

    private String APP_ID = "2018011601908279";
    private String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDRBs03KDbhc6NCfp1QKuhwNWGJnJEjkBFq3A0RqUaXBMucPwkoCiomSOJhotid5XVTOS9tksMbIHh0Bbsl3LE6I2tdZPnjMT53C3InTZtV4ohKvcspbIuT7dGaTmsxcwTbyHm/8XAEotnB6U0D+41SiCCERDEBvnjvyL5fXyw5TPYW4v2gai2w6Ru097V3T0Ifhzmj6zuUzcWWCXfLvqUCoyRIXt12uE8yXKBvXNBOkHAbVzxc6E8nRLnXI2wmJiqNYASzlXwcl0og1PTI13OszGS/8w82fPTcosV5iHXS6tqw1QIClbRHCuuSoJ3hCSztCVjKxJ/MXSGXQCjHXw/TAgMBAAECggEBAMJxxlMqCKt/a/7+U47w61r+fWaLDAT+DwXOACZOxsjTgCkwBm+FzSHiKR1UZJe1jYfGqZUPsom4kfS0JB0biI4hxs2RklfDRm1ta1qeegLSAaEtnyFAxTLuR0545oSQj5N4kbF5go/9gf497hPnXNbKpR0FZmVN76h3oIiNu1Ds1SVtIZhml/ARJwRjlk8/WV1k8+9d3H6Bf/rgp7N4t8z2K5jUAzuXP3n3s5qWiEfNaxfqelqCtd81rSqRWC6U0xt64YTDiiizGK2ywB257JzsZ/ddnDDFWr3Ie2/jkn/h4LmtL8x06Jjbv5bjnrd3yrLH4B5KrR+ShEIUMLj6CVkCgYEA8kGclUl58Y+YlL+hV1BY8T2eGDmbrXpgflDJt+7oGXumXcT1uI4oViM23UPvWgUxx51Nql//QQowiUHmAnms382Xw5Wo3LmEhz0jToDgAeP2H+Rs8Wg+/bvzoWoxgIENXjKBoiflGEnjtNjA1NwXhZqyqSLVs4R2XhBYZ8gaB5UCgYEA3OKUtn7r95bSmV6IDZv/YRm/1s7Xzx77oqIz1635WKXUhBqk4GWoG/UNqUKBZEVbTbKEBjDGCGP4cc2G77kO/ged5/iI9fSNFJNzccPJQtFgwKhBGODiR4T6vKRlbDMQHv5ILodQK//BVBEhl2okED9Ccl+IYfXgipe4jnbPv8cCgYBU3DDqhoOF36Q2Lu5odIfDca8okz16Rkz40nnmyXRaQNslK7JbhMiSa+FzHrIAWaN3Zh3nmZsDgHtCCWh144NwHkeURp+ROhc3fG4auMA/SUhVIaIb6kVugmE0YMuYVRyGq7CHSexRVsjB98+MgNBoh6kf4Ej1imggSR9+szSTBQKBgADPE5xXeqW9d6f4IKdsGtNQNNOkjkXD50gBMA9qI6+fa9fQASqRNLxleVdVVwP2/Q6byL/9DbyaIR7JkeTu2I81l+5xio7cBmg84f9YqDQGcEREWXit2iZI5mIqd/Sde0GhzMHe3PxMQ3j7VxQm31rv4F5q2yUAsYHQbGyZIsmhAoGBAK24V2tLI3yxBQAdxV3os1VhLA62wefhXbY5kHmbCReb/lHDPN2AY0oYuuR8WmUGHK4XIOc7ByWhXId1VJmEzrY3qkdip5q5KgR9lqsSyP33nx2Nn6X7xPfntGZVEhCyYjg3WVnbs68LSdgG6zqGHufWUD4DD4dXP/j6UNWNrBL3";
    private String CHARSET = "UTF-8";
    private String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlb35jKmxgDiZqTbhys4cClqsisz+K3v44BZwRAL/Xk5IJnvmh2SUrl0GvfvJ6ZOcSqN5uY3+Kvp1JJoNZa7SY7mB/o+6jPWpcoexJxbTvNLkUZdHYBIhVB4rUeO8zjomyLspG1H4PRgGNToxDAFK1hivnHTxMEj0RBl5JeNNqTCOEvhPbHsozgLURDi+xLsPyOI1sxQETpEzQxAzF3/R2wP4YdT+e3Qzf7sSKdOyqgmAcyDLQLf5HR9a67RAEuT38qhIOOMr6DqQ06C64nsSkvy8sI6NmzSx2V+0rK6xr+447TmaYh4RgDuQ8ikwueRGcklDys+XHK7LnHHk0F7XRQIDAQAB";

    private String notifyUrl = "http://pay.ghy9.com/notify/2";

 /*   @Reference
    private IPaySvApi paySv;
    @Reference
    private IOrderService orderService;*/


    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String, String> params) {
        JsonObject jsonObject = new JsonObject();
        /*Integer price = Integer.parseInt(params.getOrDefault("price", "0")) / 100;
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl(notifyUrl);//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"" + order.getMerchantOrderNo() + "\"," +
                //" \"total_amount\":\"0.01\"," +
                //  价格
                " \"total_amount\":\"" + price + "\"," +
                " \"subject\":\""+params.getOrDefault("goodsName","棋牌")+"\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
            jsonObject.put("code", 1);
            jsonObject.put("message", "success");
            jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
            jsonObject.put("data",form);
        } catch (AlipayApiException e) {
            //e.printStackTrace();
            jsonObject.put("code", 0);
            jsonObject.put("message", "form error");
        }
        log.info(" Alipay h5 gateway [ {} ] ",form);*/
        return jsonObject;
    }

    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {
        log.info(" AliPay h5 callback params [ {} ] ",params.toString());
      /*  try {
            boolean signVerified = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, CHARSET, "RSA2"); //调用SDK验证签名
            if (signVerified) {
                //  验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                JsonObject data = new JsonObject(params);
                String out_trade_no = data.getString("out_trade_no");
                String totalAmount = data.getString("total_amount");
                String timestamp = data.getString("timestamp");
                String tradeStatus = data.getString("trade_status");
                if(null == tradeStatus || !"TRADE_SUCCESS".equals(tradeStatus) ){
                    log.error("tradeStatus error [ {} ]",tradeStatus);
                    return "failure";
                }
                PayOrder order = orderService.selectByMerchantOrderNo(out_trade_no);
                int price = Integer.parseInt(totalAmount) * 100;
                if (order != null) {
                    order.setPlatformOrderNo(out_trade_no);
                    order.setPayType(9);
                    order.setTransactionId(out_trade_no);
                    order.setPayTime(DateUtils.parseDate(timestamp,"yyyy-MM-dd HH:mm:ss"));
                    order.setNotifyTime(new Date());
                    order.setAttach(params.getOrDefault("attach",""));
                    order.setStatus(1);
                    // TODO 价格判断已注释
                    //if (price == order.getPrice()) {
                        int count = orderService.updateByPrimaryKeySelective(order);
                        if (count > 0) {
                            return paySv.notify(order) ? "success" : "failure";
                        }
                   // } else {
                  //      log.error("订单数据异常： [ {} ]",data );
                  //  }
                }
            } else {
                //  验签失败则记录异常日志，并在response中返回failure.
                return "failure";
            }
        } catch (Exception e) {
            log.error("支付失败params  [ {} ] ", params.toString());
            return "failure";
        }*/
        return "failure";
    }
}
