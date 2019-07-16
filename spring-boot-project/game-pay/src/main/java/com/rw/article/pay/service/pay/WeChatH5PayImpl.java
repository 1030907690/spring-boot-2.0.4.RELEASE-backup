package com.rw.article.pay.service.pay;

import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.pay.ab.HttpUtils;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/***
 *@author zhouzhongqing
 * 2018年1月20日09:58:01
 * 微信h5支付
 * */
@Service
public class WeChatH5PayImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(WeChatH5PayImpl.class);

    private static final String APPID = "wx16d89668fc14a3cc";

    private final String APPSECRET = "baba431d7ac8fe24bf3d9701b264cc1d";

    private static final String MCH_ID = "1496527962";

    private static final String MCH_ID_KEY = "S6C7YT3McfRXr2wfdbX4PV5rjtFwFSI8";

    private final String PAYURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";


    private String notifyUrl = "http://pay.ghy9.com/notify/1";



    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String, String> params) {
        JsonObject result = new JsonObject();
        try {
            //字典序列排序
            //第一次签名
            Map<String, String> paraMap = new HashMap<>();
            paraMap.put("total_fee", order.getMoney().toString());
            paraMap.put("appid", payPlatform.getMerchPublicKey());
            paraMap.put("out_trade_no", order.getOrderId());
            paraMap.put("attach", order.getOrderId());
            paraMap.put("body", params.getOrDefault("goodsName","棋牌"));  //如果不转码,参数带中文会签名失败
            paraMap.put("mch_id", payPlatform.getMerchId());
            paraMap.put("nonce_str", WeChatPublicNumberPayImpl.getNonceStr());
            paraMap.put("notify_url", notifyUrl);
            //paraMap.put("openid", params.getOrDefault("operId", ""));//"oPKW80lcsqmHLWvPLElQoN2p6Eow");
            String spbill_create_ip = params.getOrDefault("spbillCreateIp", "127.0.0.1");
            if (-1 != spbill_create_ip.indexOf(",")) {
                spbill_create_ip = spbill_create_ip.split(",")[0];
            }
            paraMap.put("spbill_create_ip", spbill_create_ip);
            paraMap.put("trade_type", "MWEB");
            //paraMap.put("scene_info","{\"h5_info\": {\"type\":\"Wap\",\"wap_url\": \"http://dwc.ccac7.com\",\"wap_name\": \""+setCharsets("老板棋牌")+"\"}} ");
            paraMap.put("scene_info", "{\"h5_info\": {\"type\":\"Wap\",\"wap_url\": \"http://dwc.ccac7.com\",\"wap_name\": \"棋牌\"}} ");
            String url = WeChatPublicNumberPayImpl.formatUrlMap(paraMap, false, true);
            url = url + "&key=" + payPlatform.getMerchKey();
            String sign = MD5Utils.MD5Encoding(url).toUpperCase();
            StringBuffer xml = new StringBuffer();
            xml.append("<xml>");
            for (Map.Entry<String, String> entry : paraMap.entrySet()) {
                xml.append("<" + entry.getKey() + ">");
                xml.append(entry.getValue());
                xml.append("</" + entry.getKey() + ">" + "\n");
            }
            xml.append("<sign>");
            xml.append(sign);
            xml.append("</sign>");
            xml.append("</xml>");

            log.info("xml \n {} ", xml.toString());
            String responseBosy = HttpUtils.sentPost(PAYURL, xml.toString(), "UTF-8");
            log.info("responseBosy \n {}  ", responseBosy);
            Map<String, String> respBodyMap = WeChatPublicNumberPayImpl.readStringXmlOut(responseBosy);
            String return_code = respBodyMap.getOrDefault("return_code", "");
            if (null != return_code && "SUCCESS".equals(return_code)) {
                //成功
                result.put("code", Constants.SUCCESS_MARK);
                result.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
                result.put("redirect", respBodyMap.getOrDefault("mweb_url", "") + "&redirect_url=" + params.getOrDefault("wechatH5Redirect","http://h5.ccac7.com/api/login"));
            } else {
                //失败
                result.put("code", Constants.ErrorCode.OTHER_ERROR);
                result.put("message", "sign error");
            }
        } catch (Exception e) {
            result.put("code",Constants.ErrorCode.OTHER_ERROR);
            result.put("message", "创建订单失败");
        }
        return result;
    }


    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {
        log.info("wechat pay  callback [ {} ]", params.toString());
        params = WeChatPublicNumberPayImpl.readStringXmlOut(params.getOrDefault("body", ""));
        //商户订单号
        String out_trade_no = params.getOrDefault("out_trade_no", "");
        String result_code = params.getOrDefault("result_code", "");
        //支付金额 (分)
        String total_fee = params.getOrDefault("total_fee", "");
        //支付完成时间
        String time_end = params.getOrDefault("time_end", "");
      /*  if (null != result_code && "SUCCESS".equals(result_code)) {
            //成功
            if (null != out_trade_no && !"".equals(out_trade_no)) {
                PayOrder payOrder = orderService.selectByMerchantOrderNo(out_trade_no);
                int price = Integer.parseInt(total_fee);
                if (null != payOrder) {
                    payOrder.setPlatformOrderNo(out_trade_no);
                    payOrder.setTransactionId(out_trade_no);
                    payOrder.setPayType(PayType.pay_type_wx_h5.getCode());
                    Date nowTime = new Date();
                    payOrder.setPayTime(nowTime);
                    payOrder.setNotifyTime(nowTime);
                    payOrder.setAttach(params.getOrDefault("attach", ""));
                    payOrder.setStatus(1);
                    // 价格判断被注释
                    if (payOrder.getPrice().equals(price)) {
                        int count = orderService.updateByPrimaryKeySelective(payOrder);
                        if (count > 0) {
                            return paySv.notify(payOrder) ? "SUCCESS" : "FAIL";
                        }
                    } else {
                        log.error("订单数据异常：" + out_trade_no);
                        return "FAIL";
                    }
                } else {
                    log.info("没有这个商户订单号 [ {} ]", out_trade_no);
                    return "FAIL";
                }
            }
        } else {
            //失败
            return "FAIL";
        }*/
        return "FAIL";
    }


    public static void main(String[] args) {
      /*  System.out.println(Charsets.ISO_8859_1.name());
        System.out.println(setCharsets("撒地方 "));
        System.out.println(PayType.convert(1).name());*/
    }

    /***
     *zhouzhongqing
     * 2018年1月20日11:42:33
     * 设置字符编码 默认ISO-8859-1
     * @param str
     * */
    public static String setCharsets(String str) {
        return setCharsets(str, null);
    }

    /***
     *zhouzhongqing
     * 2018年1月20日11:42:33
     * 设置字符编码
     * @param str
     * @param encoding
     * */
    public static String setCharsets(String str, String encoding) {
        try {
            return URLEncoder.encode(str, null == encoding ? "ISO8859-1" : encoding).replace("+", "%20");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
