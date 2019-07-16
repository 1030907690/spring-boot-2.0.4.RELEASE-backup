package com.rw.article.pay.service.pay;

import com.alibaba.fastjson.JSON;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.OtherUtils;
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
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 汇宝支付宝网页支付
 * @date 2018/9/6 10:15
 */
@Service
public class HuiBaoAliPayWapHandlerImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(HuiBaoAliPayWapHandlerImpl.class);


    @Resource
    private IRechargeOrderService rechargeOrderService;

    @Resource
    private IRechargeOrderRepository rechargeOrderRepository;

    @Resource
    private BaseDao baseDao;

    private final String PAY_TYPE = "ALIPAYWAP";

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
        String redirectUrl = null;
        Map<String, String> map = new TreeMap<>();
        try {
            map.put("partner", payPlatform.getMerchId());
            //通道类型
            map.put("banktype", PAY_TYPE);
            //支付金额 转为 元
            map.put("paymoney", String.valueOf(OtherUtils.round(order.getRechargePrice(), Constants.TEN_THOUSAND, 2)));
            map.put("ordernumber", order.getOrderId());
            map.put("callbackurl", payPlatform.getNotifyUrl());
            map.put("attach", order.getOrderId());

            // partner={}&banktype={}&paymoney={}&ordernumber={}&callbackurl={}key

            String sign = MD5("partner=" + map.getOrDefault("partner", "") + "&banktype=" + map.getOrDefault("banktype", "") + "&paymoney=" + map.getOrDefault("paymoney", "") +
                    "&ordernumber=" + map.getOrDefault("ordernumber", "") + "&callbackurl=" + map.getOrDefault("callbackurl", "") + payPlatform.getMerchKey());
            map.put("sign", sign.toLowerCase());

            StringBuffer sb = new StringBuffer(payPlatform.getPayGetUrl() + "?");

            for (Map.Entry<String, String> entry : map.entrySet()) {
                sb.append(entry.getKey() + "=" + entry.getValue() + "&");
            }
            sb.deleteCharAt(sb.length() - 1);
            redirectUrl = sb.toString();
            jsonObject.put("code", Constants.SUCCESS_MARK);
        } catch (Exception e) {
            log.error("获取参数错误 {}", e);
            e.printStackTrace();
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
        log.info("   HuiBaoAliPayWapHandlerImpl callback params  [ {} ]  ", params.toString());

        //回调参数
        //商户id
        String partner = params.getOrDefault("partner", "");
        //上行过程中商户系统传入的
        String orderNumber = params.getOrDefault("ordernumber", "");
        //1:支付成功，非1为支付失败
        String orderStatus = params.getOrDefault("orderstatus", "0");
        //单位元（人民币）
        String payMoney = params.getOrDefault("paymoney", "0");
        //此次交易中汇宝接口系统内的订单ID
        String sysNumber = params.getOrDefault("sysnumber", "");
        // 备注信息，上行中attach原样返回
        String attach = params.getOrDefault("attach", "");
        //32位小写MD5签名值，GB2312编码
        String sign = params.getOrDefault("sign","");

        //partner={}&ordernumber={}&orderstatus={}&paymoney={}key


        String signStr = MD5("partner="+partner+"&ordernumber="+orderNumber+"&orderstatus="+orderStatus+"&paymoney="+payMoney+payPlatform.getMerchKey()).toLowerCase();



        log.info("signStr:  [ {} ]", signStr);


        if (StringUtils.equals(sign, signStr)) {
            if ("1".equals(orderStatus)) {
                if (null != orderNumber && !"".equals(orderNumber)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(orderNumber);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(orderNumber);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        //乘100来比较
                       Integer price_amount = Integer.parseInt(String.valueOf(OtherUtils.multiply(Double.parseDouble(payMoney),100)));
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

                                return count > 0 ? "ok" : "FAIL";
                            } else {
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId());
                                return "ok";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", price_amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", orderNumber);
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
