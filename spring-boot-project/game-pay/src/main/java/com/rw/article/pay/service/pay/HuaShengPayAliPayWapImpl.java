package com.rw.article.pay.service.pay;


import com.alibaba.fastjson.JSON;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.entity.SysConfig;
import com.rw.article.pay.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.*;

/**
 * Created by doer on 2018/7/4.
 */
@Service
public class HuaShengPayAliPayWapImpl  implements IPayRealizeSv {
    private static final Logger log = LoggerFactory.getLogger(HuaShengPayAliPayWapImpl.class);


    private static final String payType = "aliwap";
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
        log.info("拉起花生支付支付宝" );
        JsonObject jsonObject = new JsonObject();
        String pay_info = "";
        String url = payPlatform.getPayGetUrl();
        TreeMap<String, Object> treeMap = new TreeMap<>();
        String redirectUrl = null;
        //treeMap.put("key", payPlatform.getMerchKey());
        try {
            SysConfig pageConfig = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("href_backurl_page", 1);
            if (null == pageConfig) {
                pageConfig = new SysConfig();
                pageConfig.setItemVal("https://pro.hzqp777.com/newDown/index.html");
            }
            treeMap.put("mch_code", payPlatform.getMerchId());
            treeMap.put("pay_type", payType);
            Integer rechargeMoney = order.getMoney();
            treeMap.put("price_amount", rechargeMoney);
            String timeStamp = System.currentTimeMillis()/1000L+"";
            treeMap.put("unix_date", timeStamp);
            treeMap.put("order_code", order.getOrderId());
            treeMap.put("syn_url", pageConfig.getItemVal());
            treeMap.put("asyn_url", payPlatform.getNotifyUrl());
            treeMap.put("ip", getRandomIp());

            // 签名
            String signStr = MD5(payPlatform.getMerchKey()).toLowerCase();
            signStr = order.getOrderId() + rechargeMoney + payType + timeStamp + payPlatform.getMerchId() + signStr;
            log.info("signStr  [ {} ] ", signStr );
            signStr = MD5(signStr).toLowerCase();
            treeMap.put("sign", signStr);

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
            log.info("redirectUrl：  [ {} ] ", redirectUrl );
            //修改实际要支付的钱,修改过支付金额之后才需要执行以下修改，参考聚合支付
//            RechargeOrder rechargeOrderNew = new RechargeOrder();
//            rechargeOrderNew.setId(order.getId());
//            rechargeOrderNew.setMoney(Integer.parseInt(rechargeMoney.toString()));
//            rechargeOrderNew.setRemark("用户实际要支付"+ CommonFunction.round(rechargeMoney,100)+ "人民币");
//            rechargeOrderService.updateByPrimaryKeySelective(rechargeOrderNew);
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
        log.info("   HuaShengPayAliPayWapImpl callback params  [ {} ]  ", params.toString());
        String order_code = params.get("order_code");
        String platform_code = params.get("platform_code");
        int price_amount = Integer.parseInt(params.get("price_amount"));
        String pay_type = params.get("pay_type");
        String mch_code = params.get("mch_code");
        String unix_date = params.get("unix_date");
        int status = Integer.parseInt(params.get("status"));
        String sign = params.get("sign");

        // 签名
        String signStr = MD5(payPlatform.getMerchKey()).toLowerCase();
        signStr = order_code + platform_code + price_amount + mch_code
                + pay_type + unix_date + signStr;
        log.info("signStr:  [ {} ]", signStr );
        signStr = MD5(signStr).toLowerCase();

        if (StringUtils.equalsIgnoreCase(sign, signStr)) {
            if (status == 1) {
                if (null != order_code && !"".equals(order_code)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(order_code);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(order_code);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        if (price.equals(price_amount)) {
                            if(rechargeOrder.getPayStatus().equals(0)) {
                                Date nowTime = new Date();
                                rechargeOrder.setPayTime(nowTime);
                                rechargeOrder.setStatus(1);
                                rechargeOrder.setPayStatus(1);
                                int count = 0;

                                count = rechargeOrderService.payAfterHandle(count, rechargeOrder, beforeRechargeOrder);
                                log.info("payAfterHandle count [{}] rechargeOrder [{}]",count, JSON.toJSONString(rechargeOrder));
                                if(count > 0){
                                    count = baseDao.updateByEntity(rechargeOrder);
                                }

                                return count > 0 ? "success" : "FAIL";
                            }else{
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId() );
                                return "success";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", price_amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", order_code);
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
