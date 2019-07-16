package com.rw.article.pay.service.pay;


import com.rw.article.common.constant.Constants;
import com.rw.article.common.http.HttpClient;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.IRechargeOrderRepository;
import com.rw.article.pay.service.IRechargeOrderService;
import com.rw.article.pay.service.ISysMessageService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.*;

/**
 * Created by doer on 2018/6/22.
 */
@Service
public class YiyunPayAliPayWapImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(YiyunPayAliPayWapImpl.class);


    //private static final String payType = "AL_H5";
    private static final int payType = 109;

    @Resource
    private IRechargeOrderService rechargeOrderService;

    @Resource
    private IRechargeOrderRepository rechargeOrderRepository;

    @Resource
    private BaseDao baseDao;


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
        JsonObject jsonObject = new JsonObject();
        String pay_info = "";
        String url = payPlatform.getPayGetUrl();
        TreeMap<String, Object> treeMap = new TreeMap<>();
        String redirectUrl = null;
        treeMap.put("key", payPlatform.getMerchKey());
        try {
            treeMap.put("partnerId", payPlatform.getMerchId());
            treeMap.put("channelOrderId", params.get("orderId"));
            String timeStamp = System.currentTimeMillis()/1000L+"";
            treeMap.put("timeStamp", timeStamp);
            treeMap.put("body", "测试");
            Integer rechargeMoney = order.getMoney();
            treeMap.put("totalFee", rechargeMoney);
            // 签名
            String sign = "partnerId="+payPlatform.getMerchId()+"&timeStamp="+timeStamp+"&totalFee="+rechargeMoney+"&key="+payPlatform.getMerchKey();
            sign = MD5(sign);
            treeMap.put("sign", sign);
            treeMap.put("payType", payType);
            treeMap.put("notifyUrl", payPlatform.getNotifyUrl());
            treeMap.put("returnUrl", "http://channel.kunbaow.com");

            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treeMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                // 参数不为空
                if (StringUtils.isNotBlank(treeMap.get(key).toString())) {
                    sb.append(key + "=" + treeMap.get(key));
                }
                if (iterator.hasNext()) {
                    sb.append("&");
                }
            }
            // 拼接url
            redirectUrl = url + "?" + sb;

            //修改实际要支付的钱,修改过支付金额之后才需要执行以下修改，参考聚合支付
//            RechargeOrder rechargeOrderNew = new RechargeOrder();
//            rechargeOrderNew.setId(order.getId());
//            rechargeOrderNew.setMoney(Integer.parseInt(rechargeMoney.toString()));
//            rechargeOrderNew.setRemark("用户实际要支付"+ CommonFunction.round(rechargeMoney,100)+ "人民币");
//            rechargeOrderService.updateByPrimaryKeySelective(rechargeOrderNew);
            String respBody = HttpClient.sendHttpRequestPost(redirectUrl,new HashMap<>(),HttpClient.ENCODING);
            log.info("respBody: [ {} ]  ", respBody );
            JsonObject resJsonObj = new JsonObject(respBody);

            if(null != resJsonObj  && resJsonObj.containsKey("return_code") && "0000".equals(resJsonObj.getString("return_code"))){
                JsonObject payParamObj =  new JsonObject(resJsonObj.getString("payParam"));
                if(payParamObj.containsKey("pay_info") && null !=  payParamObj.get("pay_info") && !"".equals(payParamObj.get("pay_info").toString())){
                    pay_info = payParamObj.get("pay_info").toString();
                    jsonObject.put("code", Constants.SUCCESS_MARK);
                }else {
                    jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
                }

            }else {
                jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
            }

        } catch (Exception e) {
            log.error("获取参数错误 {}", e);
            e.printStackTrace();
            jsonObject.put("code",Constants.ErrorCode.OTHER_ERROR);
        }

        jsonObject.put("redirect", pay_info);
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
        return jsonObject;
    }


    public static void main(String[] args) {
        Map<String,String> params = new HashMap<>();
        //channelOrderId=1529669191550_shop_10, timeStamp=1529669191, orderId=1806223866379611445, totalFee=1000, sign=2fc52b8407a9d961db8587b9ba876c1e, attach=null, body=, return_code=0000, transactionId=20180622200040011100430002284197} ]
        params.put("channelOrderId","1529669191550_shop_10");
        params.put("timeStamp","1529669191");
        params.put("orderId","1806223866379611445");
        params.put("totalFee","1000");
        params.put("sign","2fc52b8407a9d961db8587b9ba876c1e");

        params.put("return_code","0000");
        params.put("attach",null);
        params.put("body","");
        params.put("transactionId","20180622200040011100430002284197");


        String channelOrderId = params.get("channelOrderId");
        String orderId = params.get("orderId");
        String timeStamp = params.get("timeStamp");
        int totalFee = Integer.parseInt(params.get("totalFee"));
        String sign = params.get("sign");
        String return_code = params.get("return_code");

        String signStr = "channelOrderId="+channelOrderId+"&key="+"1a37788e11c647f8ab914aaee3ef52eb"+"&orderId="+orderId+"&timeStamp="+timeStamp+"&totalFee="+totalFee;

        System.out.println(MD5(signStr));
        PayPlatform p = new PayPlatform();
        p.setMerchKey("1a37788e11c647f8ab914aaee3ef52eb");

        new YiyunPayAliPayWapImpl().callback(p,params);

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
        log.info("   YiyunPayAliPayWapImpl callback params  [ {} ]  ", params.toString());
        String channelOrderId = params.get("channelOrderId");
        String orderId = params.get("orderId");
        String timeStamp = params.get("timeStamp");
        int totalFee = Integer.parseInt(params.get("totalFee"));
        String sign = params.get("sign");
        String return_code = params.get("return_code");

        // 签名
        String signStr = "channelOrderId="+channelOrderId+"&key="+payPlatform.getMerchKey()+"&orderId="+orderId+"&timeStamp="+timeStamp+"&totalFee="+totalFee;
        log.info("signStr:  [ {} ]", signStr );
        signStr = MD5(signStr);

        if (StringUtils.equalsIgnoreCase(sign, signStr)) {
            if (StringUtils.equals(return_code, "0000")) {
                if (null != channelOrderId && !"".equals(channelOrderId)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(channelOrderId);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(channelOrderId);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        if (price.equals(totalFee)) {
                            if(rechargeOrder.getPayStatus().equals(0)) {
                                Date nowTime = new Date();
                                rechargeOrder.setPayTime(nowTime);
                                rechargeOrder.setStatus(1);
                                rechargeOrder.setPayStatus(1);
                                int count = baseDao.updateByEntity(rechargeOrder);

                                rechargeOrderService.payAfterHandle(count, rechargeOrder, beforeRechargeOrder);

                                return count > 0 ? "SUCCESS" : "FAIL";
                            }else{
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId() );
                                return "SUCCESS";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", totalFee);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", channelOrderId);
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

    public static String MD5(String s) {

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            s = new String(s.getBytes("UTF-8"));
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
