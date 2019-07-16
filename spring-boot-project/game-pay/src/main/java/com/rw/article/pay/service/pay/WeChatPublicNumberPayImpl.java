package com.rw.article.pay.service.pay;


import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.OtherUtils;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.pay.ab.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;

/***
 *zhouzhongqing
 * 2018年1月17日19:45:36
 * 微信公众号内部网页支付
 * */
@Service
public class WeChatPublicNumberPayImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(WeChatPublicNumberPayImpl.class);

    private static final String APPID = "wx16d89668fc14a3cc";

    private final String APPSECRET = "baba431d7ac8fe24bf3d9701b264cc1d";

    private static final String MCH_ID = "1496527962";

    private static final String MCH_ID_KEY = "S6C7YT3McfRXr2wfdbX4PV5rjtFwFSI8";

    private final String PAYURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";


    private String notifyUrl = "http://pay.ghy9.com/notify/6";


    @Override
    public JsonObject gateway(PayPlatform payPlatform, RechargeOrder order, Map<String, String> params) {
        JsonObject result = new JsonObject();
        try {
            //字典序列排序
            //第一次签名
            Map<String, String> paraMap = new HashMap<>();
            // paraMap.put("total_fee", order.getPrice().toString());
            paraMap.put("total_fee", order.getMoney().toString());
            paraMap.put("appid", payPlatform.getMerchPublicKey());
            paraMap.put("out_trade_no", order.getOrderId());
            paraMap.put("attach", order.getOrderId());
            paraMap.put("body", params.getOrDefault("goodsName","棋牌"));
            paraMap.put("mch_id", payPlatform.getMerchId());
            paraMap.put("detail", "购买商品");
            paraMap.put("nonce_str", getNonceStr());
            paraMap.put("notify_url", notifyUrl);
            paraMap.put("openid", params.getOrDefault("operId", ""));
            String spbill_create_ip = params.getOrDefault("spbillCreateIp", "127.0.0.1");
            if (-1 != spbill_create_ip.indexOf(",")) {
                spbill_create_ip = spbill_create_ip.split(",")[0];
            }
            paraMap.put("spbill_create_ip", spbill_create_ip);
            paraMap.put("trade_type", "JSAPI");
            String url = formatUrlMap(paraMap, false, true);
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

            log.info("xml {} ", xml.toString());
            String responseBosy = HttpUtils.sentPost(PAYURL, xml.toString(), "UTF-8");
            log.info("responseBosy -- " + responseBosy + "===" + url + "\n" + paraMap + "\n" + sign);
            String prepay_id = readStringXmlOut(responseBosy).getOrDefault("prepay_id", "");
            log.info("result prepay_id [ {} ] ", prepay_id);
            if (null != prepay_id && !"".equals(prepay_id.trim())) {
                String timeStamp = String.valueOf(OtherUtils.getSecondTimestamp(new Date()));
                String h5NonceStr = getNonceStr();
                result.put("code", 1);
                result.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
                result.put("redirect", params.getOrDefault("wechatPublicRedirect","http://dwc.ccac7.com/goods/wechatPublicPayPage"));
                //进行第二次签名
                Map<String, String> paramsH5 = new HashMap<>();
                paramsH5.put("appId", payPlatform.getMerchPublicKey());
                paramsH5.put("timeStamp", timeStamp);
                paramsH5.put("nonceStr", h5NonceStr);
                paramsH5.put("package", "prepay_id=" + prepay_id);
                paramsH5.put("signType", "MD5");
                //String h5Url = formatUrlMap(paramsH5, false, true);
                // h5Url = h5Url + "&key=" + MCH_ID_KEY;
                //String paySign = MD5Utils.MD5(h5Url).toUpperCase();
                String paySignStr =
                        "appId=" + payPlatform.getMerchPublicKey() +
                                "&nonceStr=" + h5NonceStr +
                                "&package=prepay_id=" + prepay_id +
                                "&signType=" + "MD5" +
                                "&timeStamp=" + timeStamp +
                                "&key=" + payPlatform.getMerchKey();//注意这里的参数要根据ASCII码 排序
                String paySign = MD5Utils.MD5(paySignStr).toUpperCase();
                paramsH5.put("paySign", paySign);
                paramsH5.put("goodsId", params.getOrDefault("goodsId", ""));
                paramsH5.put("orderId",order.getOrderId());
                result.put("data", paramsH5);
            } else {
                result.put("code", -100);
                result.put("message", "request wechat error");

            }
        } catch (Exception e) {
            result.put("code", 0);
        }
        return result;
    }

    @Override
    public String callback(PayPlatform payPlatform, Map<String, String> params) {
        log.info("wechat pay  callback [ {} ]", params.toString());
        params = readStringXmlOut(params.getOrDefault("body", ""));
        //商户订单号
        String out_trade_no = params.getOrDefault("out_trade_no", "");
        String result_code = params.getOrDefault("result_code", "");
        //支付金额 (分)
        String total_fee = params.getOrDefault("total_fee", "");
        //支付完成时间
        String time_end = params.getOrDefault("time_end", "");
       /* if (null != result_code && "SUCCESS".equals(result_code)) {
            //成功
            if (null != out_trade_no && !"".equals(out_trade_no)) {
                PayOrder payOrder = orderService.selectByMerchantOrderNo(out_trade_no);
                int price = Integer.parseInt(total_fee);
                if (null != payOrder) {
                    payOrder.setPlatformOrderNo(out_trade_no);
                    payOrder.setTransactionId(out_trade_no);
                    payOrder.setPayType(PayType.pay_type_wx_public.getCode());
                    Date nowTime = new Date();
                    payOrder.setPayTime(nowTime);
                    payOrder.setNotifyTime(nowTime);
                    payOrder.setAttach(params.getOrDefault("attach", ""));
                    payOrder.setStatus(1);
                    if ( payOrder.getPrice().equals(price)) {
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
        /*String xml = "<xml>\n" +
                "   <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "   <return_msg><![CDATA[OK]]></return_msg>\n" +
                "   <appid><![CDATA[wx2421b1c4370ec43b]]></appid>\n" +
                "   <mch_id><![CDATA[10000100]]></mch_id>\n" +
                "   <nonce_str><![CDATA[IITRi8Iabbblz1Jc]]></nonce_str>\n" +
                "   <openid><![CDATA[oUpF8uMuAJO_M2pxb1Q9zNjWeS6o]]></openid>\n" +
                "   <sign><![CDATA[7921E432F65EB8ED0CE9755F0E86D72F]]></sign>\n" +
                "   <result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "   <prepay_id><![CDATA[wx201411101639507cbf6ffd8b0779950874]]></prepay_id>\n" +
                "   <trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                "</xml>";

        log.info(readStringXmlOut(xml).toString() + "\n" + getSecondTimestamp(new Date()));*/

        //进行第二次签名
        SortedMap<String, String> paramsH5 = new TreeMap<>();
        paramsH5.put("appId", APPID);
        paramsH5.put("timeStamp", "1516349337");
        paramsH5.put("nonceStr", "2533ba56f6764a448f3d67117e44d50e");
        paramsH5.put("package", "prepay_id=wx2018011916085772ffb69ce20165288425");
        paramsH5.put("signType", "MD5");
        String h5Url = formatUrlMap(paramsH5, false, true);
        h5Url = h5Url + "&key=" + MCH_ID_KEY;


        String paySign =
                "appId=" + APPID +
                        "&nonceStr=" + "26b8f3d6b4e747089b8e3536bd880b4b" +
                        "&package=prepay_id=" + "wx20180119162818fa49ec02f20091592289" +
                        "&signType=" + "MD5" +
                        "&timeStamp=" + "1516350498" +
                        "&key=" + MCH_ID_KEY;//注意这里的参数要根据ASCII码 排序
        String paySign2 = MD5Utils.MD5(h5Url).toUpperCase();
        System.out.println("---" + h5Url);
        System.out.println("---" + paySign);
        String paySign3 = MD5Utils.MD5(paySign).toUpperCase();
        System.out.println(paySign3 + "paySign " + paySign2);

    }




    /**
     * @param xml
     * @return Map
     * @description 将xml字符串转换成map
     */
    public static Map<String, String> readStringXmlOut(String xml) {
        Map<String, String> map = new HashMap<String, String>();
       /* Document doc = null;
        try {
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            @SuppressWarnings("unchecked")
            List<Element> list = rootElt.elements();// 获取根节点下所有节点
            for (Element element : list) { // 遍历节点
                map.put(element.getName(), element.getText()); // 节点的name为map的key，text为map的value
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return map;
    }


    /*
    * 生成32位随机字符串
    * */
    public static String getNonceStr() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    /**
     * 方法用途: 对所有传入参数按照字段名的Unicode码从小到大排序（字典序），并且生成url参数串<br>
     * 实现步骤: <br>
     *
     * @param paraMap    要排序的Map对象
     * @param urlEncode  是否需要URLENCODE
     * @param keyToLower 是否需要将Key转换为全小写
     *                   true:key转化成小写，false:不转化
     * @return
     */
    public static String formatUrlMap(Map<String, String> paraMap, boolean urlEncode, boolean keyToLower) {
        String buff = "";
        Map<String, String> tmpMap = paraMap;
        try {
            List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(tmpMap.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
                @Override
                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                    return (o1.getKey()).toString().compareTo(o2.getKey());
                }
            });
            // 构造URL 键值对的格式
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> item : infoIds) {
                if (StringUtils.isNotBlank(item.getKey())) {
                    String key = item.getKey();
                    String val = item.getValue();
                    if (urlEncode) {
                        val = URLEncoder.encode(val, "utf-8");
                    }
                    if (keyToLower) {
                        buf.append(key.toLowerCase() + "=" + val);
                    } else {
                        buf.append(key + "=" + val);
                    }
                    buf.append("&");
                }

            }
            buff = buf.toString();
            if (buff.isEmpty() == false) {
                buff = buff.substring(0, buff.length() - 1);
            }
        } catch (Exception e) {
            return null;
        }
        return buff;
    }
}
