package com.rw.article.pay.service.pay;


import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.CommonFunction;
import com.rw.article.common.utils.pay.bird.SignUtils;

import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.IRechargeOrderRepository;
import com.rw.article.pay.service.IRechargeOrderService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.*;

/**
 * 第三方 QQ网页支付
 *
 * @author zhouzhongqing
 * @date 2018年6月1日16:03:20
 */
@SuppressWarnings("All")
@Service
public class BirdPayQQPayWapImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(BirdPayQQPayWapImpl.class);


    private static final String PAYTYPE = "QQPAY";
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
        /**
         * 1 初始化参数
         */
        String mechNo = payPlatform.getMerchId();//商户号，平台分配,请跟我方商务索取!!!!
        String mechSecret = payPlatform.getMerchKey();//商户密钥，平台分配,请跟我方商务索取!!!!
        String amount = order.getMoney().toString(); //金额,单位分
        String orderIp = params.getOrDefault("spbillCreateIp", "127.0.0.1"); //客户端IP
        String body = ""+ CommonFunction.round(order.getRechargePrice(), Constants.TEN_THOUSAND); //商品名称
        String orderNo = order.getOrderId(); //商户自定义订单号
        String notifyUrl = payPlatform.getNotifyUrl();//支付成功通知回调地址
        //String returl = "http://www.baidu.com"; //支付成功后跳转页面
        String timestamp = System.currentTimeMillis() + ""; //时间戳
        /**
         * 微信：扫码支付填SCANPAY,公众号支付填OPENPAY,转H5填H5PAY;
         * 支付宝：WAP方式填DIRECT_PAY,服务窗方式填SRVWIN_PAY;
         * QQ支付: 扫码支付填QQ_SCAN, WAP支付填QQ_WAP;
         */
        String payWay = PAYTYPE;
        String payType ="QQ_WAP";
        /**
         * 2 放入map
         */
        Map<String, String> paramMap =  new HashMap<String, String>();
        paramMap.put("mechno", mechNo);
        paramMap.put("amount", amount);
        paramMap.put("orderno", orderNo);
        paramMap.put("notifyurl",notifyUrl);
        //paramMap.put("returl", returl);
        paramMap.put("orderip",orderIp);
        paramMap.put("body", body);
        paramMap.put("timestamp", timestamp);
        paramMap.put("payway", payWay);
        paramMap.put("paytype", payType);
        /**
         * 3 进行签名,同样放入map
         */
        String sign = SignUtils.getSign(paramMap , mechSecret); //签名
        paramMap.put("sign", sign);
        System.out.println("sign="+sign);

        /**
         * 4 请求支付服务,
         * （1）WAP支付方式：实际应用中客户端无需解析response，直接重定向到拼接的地址reqUrl即可
         * 		如：return new ModelAndView("redirect:"+reqUrl);
         */
        String reqParams = SignUtils.getParamStr(paramMap);
        System.out.println("params="+reqParams);
        String reqUrl = payPlatform.getPayGetUrl()+"?"+reqParams;
        System.out.println("reqUrl="+reqParams);



        JsonObject jsonObject = new JsonObject();
        jsonObject.put("redirect", reqUrl);
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
        jsonObject.put("code", Constants.SUCCESS_MARK);
        return jsonObject;
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
        log.info("   gatherPay callback params  [ {} ]  ", params.toString());
        String mchId = params.get("mchId");
        String tradeNo = params.get("tradeNo");
        String mchOrderNo = params.get("mchOrderNo"); //商户订单号
        String payType = params.get("payType");
        String amount = params.get("amount");
        String status = params.get("status");
        String extra = params.get("extra");
        String payTime = params.get("payTime");
        String sign = params.get("sign");

        Map<String, Object> map = new HashMap<>();
        for (String s : params.keySet()) {
            String value = params.get(s);
            if (StringUtils.isNotBlank(value) && !StringUtils.equals(s, "sign")) {
                map.put(s, value);
            }
        }
        String toSign = toSign(payPlatform.getMerchPublicKey(), map);// md5 签名
        if (StringUtils.equals(sign, toSign)) {
            if (StringUtils.equals(status, "2")) {
                if (null != mchOrderNo && !"".equals(mchOrderNo)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(mchOrderNo);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(mchOrderNo);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        if (price.equals(Integer.parseInt(amount))) {
                            if (rechargeOrder.getPayStatus().equals(0)) {
                                Date nowTime = new Date();
                                rechargeOrder.setPayTime(nowTime);
                                rechargeOrder.setStatus(1);
                                rechargeOrder.setPayStatus(1);
                                int count = baseDao.updateByEntity(rechargeOrder);

                                rechargeOrderService.payAfterHandle(count, rechargeOrder, beforeRechargeOrder);

                                return count > 0 ? "SUCCESS" : "FAIL";
                            } else {
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId());
                                return "SUCCESS";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", mchOrderNo);
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

    public static String toSign(String secret, Map<String, Object> fields) {
        try {
            //按参数名asscic码排序
            List<String> list = new ArrayList<>();
            list.addAll(fields.keySet());
            Collections.sort(list);
            String strSign = "";
            for (String key : list) {
                strSign += key + "=" + fields.get(key) + "&";
            }
            strSign += "key=" + secret;
            String s = MD5(strSign).toUpperCase();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
