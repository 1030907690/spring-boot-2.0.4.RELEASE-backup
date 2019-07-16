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
 *zhouzhongqing
 * 2018年1月16日18:25:26
 * 支付宝app支付
 * */
@Service
public class AlipayAppImpl implements IPayRealizeSv {

    private String APP_ID = "2018011601908279";
    private String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDRBs03KDbhc6NCfp1QKuhwNWGJnJEjkBFq3A0RqUaXBMucPwkoCiomSOJhotid5XVTOS9tksMbIHh0Bbsl3LE6I2tdZPnjMT53C3InTZtV4ohKvcspbIuT7dGaTmsxcwTbyHm/8XAEotnB6U0D+41SiCCERDEBvnjvyL5fXyw5TPYW4v2gai2w6Ru097V3T0Ifhzmj6zuUzcWWCXfLvqUCoyRIXt12uE8yXKBvXNBOkHAbVzxc6E8nRLnXI2wmJiqNYASzlXwcl0og1PTI13OszGS/8w82fPTcosV5iHXS6tqw1QIClbRHCuuSoJ3hCSztCVjKxJ/MXSGXQCjHXw/TAgMBAAECggEBAMJxxlMqCKt/a/7+U47w61r+fWaLDAT+DwXOACZOxsjTgCkwBm+FzSHiKR1UZJe1jYfGqZUPsom4kfS0JB0biI4hxs2RklfDRm1ta1qeegLSAaEtnyFAxTLuR0545oSQj5N4kbF5go/9gf497hPnXNbKpR0FZmVN76h3oIiNu1Ds1SVtIZhml/ARJwRjlk8/WV1k8+9d3H6Bf/rgp7N4t8z2K5jUAzuXP3n3s5qWiEfNaxfqelqCtd81rSqRWC6U0xt64YTDiiizGK2ywB257JzsZ/ddnDDFWr3Ie2/jkn/h4LmtL8x06Jjbv5bjnrd3yrLH4B5KrR+ShEIUMLj6CVkCgYEA8kGclUl58Y+YlL+hV1BY8T2eGDmbrXpgflDJt+7oGXumXcT1uI4oViM23UPvWgUxx51Nql//QQowiUHmAnms382Xw5Wo3LmEhz0jToDgAeP2H+Rs8Wg+/bvzoWoxgIENXjKBoiflGEnjtNjA1NwXhZqyqSLVs4R2XhBYZ8gaB5UCgYEA3OKUtn7r95bSmV6IDZv/YRm/1s7Xzx77oqIz1635WKXUhBqk4GWoG/UNqUKBZEVbTbKEBjDGCGP4cc2G77kO/ged5/iI9fSNFJNzccPJQtFgwKhBGODiR4T6vKRlbDMQHv5ILodQK//BVBEhl2okED9Ccl+IYfXgipe4jnbPv8cCgYBU3DDqhoOF36Q2Lu5odIfDca8okz16Rkz40nnmyXRaQNslK7JbhMiSa+FzHrIAWaN3Zh3nmZsDgHtCCWh144NwHkeURp+ROhc3fG4auMA/SUhVIaIb6kVugmE0YMuYVRyGq7CHSexRVsjB98+MgNBoh6kf4Ej1imggSR9+szSTBQKBgADPE5xXeqW9d6f4IKdsGtNQNNOkjkXD50gBMA9qI6+fa9fQASqRNLxleVdVVwP2/Q6byL/9DbyaIR7JkeTu2I81l+5xio7cBmg84f9YqDQGcEREWXit2iZI5mIqd/Sde0GhzMHe3PxMQ3j7VxQm31rv4F5q2yUAsYHQbGyZIsmhAoGBAK24V2tLI3yxBQAdxV3os1VhLA62wefhXbY5kHmbCReb/lHDPN2AY0oYuuR8WmUGHK4XIOc7ByWhXId1VJmEzrY3qkdip5q5KgR9lqsSyP33nx2Nn6X7xPfntGZVEhCyYjg3WVnbs68LSdgG6zqGHufWUD4DD4dXP/j6UNWNrBL3";
    private String CHARSET = "UTF-8";
    private String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlb35jKmxgDiZqTbhys4cClqsisz+K3v44BZwRAL/Xk5IJnvmh2SUrl0GvfvJ6ZOcSqN5uY3+Kvp1JJoNZa7SY7mB/o+6jPWpcoexJxbTvNLkUZdHYBIhVB4rUeO8zjomyLspG1H4PRgGNToxDAFK1hivnHTxMEj0RBl5JeNNqTCOEvhPbHsozgLURDi+xLsPyOI1sxQETpEzQxAzF3/R2wP4YdT+e3Qzf7sSKdOyqgmAcyDLQLf5HR9a67RAEuT38qhIOOMr6DqQ06C64nsSkvy8sI6NmzSx2V+0rK6xr+447TmaYh4RgDuQ8ikwueRGcklDys+XHK7LnHHk0F7XRQIDAQAB";


    private String charset = "UTF-8";

    private static final Logger log = LoggerFactory.getLogger(AlipayAppImpl.class);

 /*   @Reference
    private IPaySvApi paySv;
    @Reference
    private IOrderService orderService;
*/
    private String notifyUrl = "http://pay.ghy9.com/notify/9";

    private String serverUrl = "https://openapi.alipay.com/gateway.do";

    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String,String> params) {
        JsonObject jsonObject = new JsonObject();
       /* //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2");
        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody("购买商品");
        model.setSubject("老板棋牌商品");
        String outTradeNo = order.getMerchantOrderNo();
        model.setOutTradeNo(outTradeNo);
        model.setTimeoutExpress("30m");
        model.setTotalAmount(String.valueOf(order.getPrice() / 100));
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
           log.info("支付宝response.getBody() [ {} ]",response.getBody());//就是orderString 可以直接给客户端请求，无需再做处理。
            jsonObject.put("code",1);
            jsonObject.put("type",1);//app类型的
            jsonObject.put("orderInfo",response.getBody());
        } catch (AlipayApiException e) {
            log.error("支付宝app支付失败 [ {} ]  [ {} ] ",e,e.getStackTrace());
            jsonObject.put("code",-1);
            jsonObject.put("message","支付失败");
        }*/
        return jsonObject;
    }

    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {
        //切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)

        /*try {
            boolean flag = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, charset, "RSA2");
            if (flag) {
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
                    if (price == order.getPrice()) {
                        int count = orderService.updateByPrimaryKeySelective(order);
                        if (count > 0) {
                            return paySv.notify(order) ? "success" : "failure";
                        }
                    } else {
                        log.error("订单数据异常： [ {} ]",data );
                    }
                }
            }else{
                //  验签失败则记录异常日志，并在response中返回failure.
                return "failure";
            }
        } catch (Exception e) {
            log.error("回调异常 [ {} ] [ {} ] ",e,e.getStackTrace());
            return "failure";
        }*/
        return "failure";
    }
}
