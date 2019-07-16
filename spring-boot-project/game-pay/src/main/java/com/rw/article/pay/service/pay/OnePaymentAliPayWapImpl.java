package com.rw.article.pay.service.pay;

import com.alibaba.fastjson.JSON;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.http.HttpClient;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.RedirectPayPlatformPageType;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.CommonFunction;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.entity.SysConfig;
import com.rw.article.pay.entity.vo.RedirectObject;
import com.rw.article.pay.entity.vo.RedirectObjectParams;
import com.rw.article.pay.service.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: OnePayment支付宝网页支付  - 必须使用post请求
 * @date 2018年11月2日15:54:26
 */
@Service
public class OnePaymentAliPayWapImpl implements IPayRealizeSv {
    private static final Logger log = LoggerFactory.getLogger(OnePaymentAliPayWapImpl.class);

    private static final String KEY_MAC_SHA1 = "HmacSHA1";

    private static final String CHARSET_UTF8 = "UTF-8";

    /**
     * 支付类型
     **/
    private final String PAY_TYPE = "T00302";

    @Resource
    private IRechargeOrderService rechargeOrderService;

    @Resource
    private IRechargeOrderRepository rechargeOrderRepository;

    @Resource
    private BaseDao baseDao;


    @Resource
    private ISysConfigRepository sysConfigRepository;


    @Resource
    private IPayPlatformRedirectService payPlatformRedirectService;

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

        log.info("调用OnePayment支付宝wap支付");
        JsonObject jsonObject = new JsonObject();
        Map<String, String> map = new TreeMap<>();
        String redirectUrl = null;
        try {
            SysConfig pageConfig = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("href_backurl_page", 1);
            if (null == pageConfig) {
                pageConfig = new SysConfig();
                pageConfig.setItemVal(Constants.DEFAULT_HREF_BACKURL_PAGE);
            }

            map.put("versionNo", "V01");
            map.put("txnCd", PAY_TYPE);
            map.put("merCd", payPlatform.getMerchId());
            map.put("merOrderNo", order.getOrderId());
            map.put("txnSubmitTime", DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));

            map.put("txnAmt",  String.valueOf(CommonFunction.round(order.getRechargePrice(), Constants.TEN_THOUSAND, 2)));
            map.put("txnGoodsName", String.valueOf(order.getMoney() / Constants.ONE_HUNDRED));
            map.put("pageBackUrl", pageConfig.getItemVal());
            map.put("notifyUrl", payPlatform.getNotifyUrl());
            map.put("bankCode","");


            StringBuffer signSb = new StringBuffer();
            map.forEach((k, v) -> {
                signSb.append(k + "=" + v + "&");
            });
            //不参与签名（HmacSHA1）
            map.put("signType", "HmacSHA1");
            if (null != signSb && signSb.length() > 0) {
                signSb.deleteCharAt(signSb.length() - 1);
            }
            log.info("signSb  [{}]", signSb);
            String sign = hmacSha1Encrypt(signSb.toString(), payPlatform.getMerchKey()).toLowerCase();


            map.put("signData", sign);
            jsonObject.put("code", Constants.SUCCESS_MARK);
            redirectUrl = payPlatformRedirectService.getRedirectUrl(RedirectPayPlatformPageType.DEFAULT_PAGE, order, map, payPlatform, Constants.POST);

        } catch (Exception e) {
            log.error("获取参数错误 [{}] [ {} ] [ {} ]", e, e.getMessage(), e.getStackTrace());
            jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
        }
        jsonObject.put("redirect", redirectUrl);
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
        return jsonObject;
    }


    public static void main(String[] args) {
    /*    Integer price = 999;
        //使用订单金额比较
        Integer price_amount = 1000;
        System.out.println(Math.abs(price_amount - price));
        //判断是否在一角钱区间内
        if(Math.abs(price_amount - price) <= 10){
            System.out.println(1);
        }*/



        //{callbackData={"txnMsg":"%E4%BA%A4%E6%98%93%E6%88%90%E5%8A%9F","txnSta":"0000","signType":"HMAC-SHA1","remarks":"","merCd":"630059990000065","txnOrderNo":"509751893082243072","txnDate":"20181107","txnAmt":"10.00","txnCurry":"156","insCd":"","txnCd":"T00302","txnOrderDesc":"","txnSubmitTime":"20181107153157","versionNo":"V01","merOrderNo":"1541575917984_shop_10","signData":"72c89b3739def43a82ad85c3db025bff764045a5","txnTime":"153240"}, body=}

        Map<String, String> params = new HashMap<>();
        params.put("callbackData","{\"txnMsg\":\"%E4%BA%A4%E6%98%93%E6%88%90%E5%8A%9F\",\"txnSta\":\"0000\",\"signType\":\"HMAC-SHA1\",\"remarks\":\"\",\"merCd\":\"630059990000065\",\"txnOrderNo\":\"509751893082243072\",\"txnDate\":\"20181107\",\"txnAmt\":\"10.00\",\"txnCurry\":\"156\",\"insCd\":\"\",\"txnCd\":\"T00302\",\"txnOrderDesc\":\"\",\"txnSubmitTime\":\"20181107153157\",\"versionNo\":\"V01\",\"merOrderNo\":\"1541575917984_shop_10\",\"signData\":\"72c89b3739def43a82ad85c3db025bff764045a5\",\"txnTime\":\"153240\"}");
        PayPlatform payPlatform = new PayPlatform();
        payPlatform.setMerchKey("f5cdce1cd2728baea13163fcfd1760f8de3865b7");


        new OnePaymentAliPayWapImpl().callback(payPlatform, params);

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
        log.info("   OnePaymentAliPayWapImpl callback params  [ {} ]  ", params.toString());
        //回调参数

        JsonObject jsonObject = new JsonObject(params.getOrDefault("callbackData", ""));

        //版本号
        String versionNo = jsonObject.getString("versionNo");
        //交易类型
        String txnCd = jsonObject.getString("txnCd");
        //签名数据
        String sign = jsonObject.getString("signData");
        //  交易返回码 0000交易成功 0001待支付 0002交易处理中  0003交易失败
        String txnSta = jsonObject.getString("txnSta");
        //商户订单号
        String merOrderNo = jsonObject.getString("merOrderNo");

        //金额 单位 :元
        String txnAmt = jsonObject.getString("txnAmt");



        Map<String, String> signMap = new TreeMap<>();
        jsonObject.forEach((k, v) -> {
            if (!"signData".equals(k.toString()) && !"signType".equals(k.toString())) {
                signMap.put(k.toString(),null == v ? "" : v.toString());
            }
        });
        StringBuffer signSb = new StringBuffer();
        signMap.forEach((k, v) -> {
            signSb.append(k + "=" + v + "&");
        });
        if (null != signSb && signSb.length() > 0) {
            signSb.deleteCharAt(signSb.length() - 1);
        }



        String signStr = hmacSha1Encrypt(signSb.toString(),payPlatform.getMerchKey()).toLowerCase();

        log.info("signStr:  [ {} ]", signStr);


        if (StringUtils.equals(sign, signStr)) {
            if ("0000".equals(txnSta)) {
                if (null != merOrderNo && !"".equals(merOrderNo)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(merOrderNo);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(merOrderNo);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if (rechargeOrder.getStatus().equals(2)) {
                            log.info("该订单已被锁定 [ {} ] ", rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        //使用订单金额比较
                        Integer price_amount = Integer.parseInt(String.valueOf(CommonFunction.multiply(Double.parseDouble(txnAmt), Constants.ONE_HUNDRED)));
                        //判断是否在一角钱区间内
                        if(price_amount > 0 && price > 0 && Math.abs(price_amount - price) <= 10){
                            if (rechargeOrder.getPayStatus().equals(0)) {
                                Date nowTime = new Date();
                                rechargeOrder.setPayTime(nowTime);
                                rechargeOrder.setStatus(1);
                                rechargeOrder.setPayStatus(1);
                                int count = 0;

                                count = rechargeOrderService.payAfterHandle(count, rechargeOrder, beforeRechargeOrder);
                                log.info("payAfterHandle count [{}] rechargeOrder [{}]", count, JSON.toJSONString(rechargeOrder));
                                if (count > 0) {
                                    count = baseDao.updateByEntity(rechargeOrder);
                                }

                                return count > 0 ? "OK" : "FAIL";
                            } else {
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId());
                                return "OK";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", price_amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", merOrderNo);
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




    /*
        使用 HmacSha1 加密
     */
    public static String hmacSha1Encrypt(String encryptText, String encryptKey)  {
        try {
            byte[] text = encryptText.getBytes(CHARSET_UTF8);
            byte[] keyData = encryptKey.getBytes(CHARSET_UTF8);

            SecretKeySpec secretKey = new SecretKeySpec(keyData, KEY_MAC_SHA1);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return byte2hex(mac.doFinal(text));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //二行制转字符串
    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }


}
