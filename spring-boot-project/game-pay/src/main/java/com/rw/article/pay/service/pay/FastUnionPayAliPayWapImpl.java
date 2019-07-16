package com.rw.article.pay.service.pay;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.http.HttpClient;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.OtherUtils;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.entity.SysConfig;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.IRechargeOrderRepository;
import com.rw.article.pay.service.IRechargeOrderService;
import com.rw.article.pay.service.ISysConfigRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author zhouzhongqing
 * 2018年9月18日18:22:50
 * 讯联支付宝wap支付
 */
@Service
public class FastUnionPayAliPayWapImpl implements IPayRealizeSv {
    private static final Logger log = LoggerFactory.getLogger(FastUnionPayAliPayWapImpl.class);


    private static final String PAY_TYPE = "2";
    @Resource
    private IRechargeOrderService rechargeOrderService;

    @Resource
    private IRechargeOrderRepository rechargeOrderRepository;

    @Resource
    private BaseDao baseDao;


    @Resource
    private ISysConfigRepository sysConfigRepository;


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
        log.info("拉起讯联支付宝wap支付" );
        JsonObject jsonObject = new JsonObject();
        TreeMap<String, String> map = new TreeMap<>();
        String redirectUrl = null;
        try {
            SysConfig pageConfig = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("href_backurl_page",1);
            if(null == pageConfig){
                pageConfig = new SysConfig();
                pageConfig.setItemVal("https://pro.hzqp777.com/newDown/");
            }
            map.put("partnerId",payPlatform.getMerchId().split(",")[0]);
            map.put("appId",payPlatform.getMerchId().split(",")[1]);
            map.put("wapType",PAY_TYPE);
            //正整数，以分为单位
            map.put("money",order.getMoney().toString());
            map.put("outTradeNo",order.getOrderId());
            map.put("subject","shopping_money");
            map.put("returnUrl",pageConfig.getItemVal());


            String sign = OtherUtils.toSign(payPlatform.getMerchKey(),map);
            map.put("sign",sign);


            String responseBody = HttpClient.sendHttpRequestPost(payPlatform.getPayGetUrl(),JSON.toJSONString(map),HttpClient.ENCODING);
            log.info("responseBody  [ {} ] ",responseBody );
            JsonObject resp = new JsonObject(responseBody);
            if("10000".equals(resp.getString("resultCode"))){
                JsonObject data = resp.getJSONObject("data");
                //1是url 2 是html
                if("1".equals(data.getString("responseType"))){
                     redirectUrl = data.getString("value");
                }else{
                    log.info("返回了html类型 暂未处理 ");
                }
                jsonObject.put("code", Constants.SUCCESS_MARK);
            }else{
                jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
            }
        } catch (Exception e) {
            log.error("获取参数错误 [{}] [ {} ] [ {} ]", e,e.getMessage(),e.getStackTrace());
            jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
        }
        jsonObject.put("redirect", redirectUrl);
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
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
        log.info("   FastUnionPayAliPayWapImpl callback params  [ {} ]  ", params.toString());
        //回调参数


        JsonObject jsonObject = new JsonObject(params.getOrDefault("body",""));


        //0 交易成功 1 交易失败(保留值，交易失败暂不发回调通知)
        String code = jsonObject.getString("code");
        //可在商户后台创建和查询
        String appId = jsonObject.getString("appId");
        //1 微信H5 2 支付宝H5  3 银联H5 4 微信扫码  5 微信公众号   6 QQ 钱包H5  7 QQ 钱包扫码 8 银联快捷  9 支付宝扫码  10 京东钱包 11 银联扫码
        String wapType = jsonObject.getString("wapType");
        //支付请求同名参数透传
        String outTradeNo = jsonObject.getString("outTradeNo");
        //平台自动生成，全局唯一
        String invoiceNo = jsonObject.getString("invoiceNo");
        //以分为单位
        String money = jsonObject.getString("money");

        String sign = jsonObject.getString("sign");

        Map<String, String> map = new HashMap<>();
        for(Map.Entry<Object,Object> entry : jsonObject.entrySet()){
            if (StringUtils.isNotBlank(entry.getKey().toString()) && !StringUtils.equals(entry.getKey().toString(), "sign")) {
                map.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }

        String signStr = OtherUtils.toSign(payPlatform.getMerchKey(), map);// md5 签名

        log.info("signStr:  [ {} ]", signStr);

        if (StringUtils.equals(sign, signStr)) {
            if ("0".equals(code)) {
                if (null != outTradeNo && !"".equals(outTradeNo)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(outTradeNo);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(outTradeNo);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        //第三方返回的
                        Integer price_amount = Integer.parseInt(money);
                        if (price.equals(price_amount)) {
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

                                return count > 0 ? "0" : "FAIL";
                            } else {
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId());
                                return "0";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", price_amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", outTradeNo);
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

    /*
     * 随机生成国内IP地址
     */
    public static String getRandomIp() {

        // ip范围
        int[][] range = { { 607649792, 608174079 },// 36.56.0.0-36.63.255.255
                { 1038614528, 1039007743 },// 61.232.0.0-61.237.255.255
                { 1783627776, 1784676351 },// 106.80.0.0-106.95.255.255
                { 2035023872, 2035154943 },// 121.76.0.0-121.77.255.255
                { 2078801920, 2079064063 },// 123.232.0.0-123.235.255.255
                { -1950089216, -1948778497 },// 139.196.0.0-139.215.255.255
                { -1425539072, -1425014785 },// 171.8.0.0-171.15.255.255
                { -1236271104, -1235419137 },// 182.80.0.0-182.92.255.255
                { -770113536, -768606209 },// 210.25.0.0-210.47.255.255
                { -569376768, -564133889 }, // 222.16.0.0-222.95.255.255
        };

        Random rdint = new Random();
        int index = rdint.nextInt(10);
        String ip = num2ip(range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));
        return ip;
    }

    /*
     * 将十进制转换成ip地址
     */
    public static String num2ip(int ip) {
        int[] b = new int[4];
        String x = "";

        b[0] = (int) ((ip >> 24) & 0xff);
        b[1] = (int) ((ip >> 16) & 0xff);
        b[2] = (int) ((ip >> 8) & 0xff);
        b[3] = (int) (ip & 0xff);
        x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);

        return x;
    }


}
