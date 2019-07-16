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
 * 第三方 支付宝网页支付
 *
 * @author ZengXianxue
 * @date 2018-04-17 14:06
 */
@SuppressWarnings("All")
@Service
public class BirdPayAliPayWapImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(BirdPayAliPayWapImpl.class);


    private static final String PAYTYPE = "ALIPAY";

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
        String body = "" + CommonFunction.round(order.getRechargePrice(), Constants.TEN_THOUSAND); //商品名称
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
        String payType = "DIRECT_PAY";
        /**
         * 2 放入map
         */
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("mechno", mechNo);
        paramMap.put("amount", amount);
        paramMap.put("orderno", orderNo);
        paramMap.put("notifyurl", notifyUrl);
        //paramMap.put("returl", returl);
        paramMap.put("orderip", orderIp);
        paramMap.put("body", body);
        paramMap.put("timestamp", timestamp);
        paramMap.put("payway", payWay);
        paramMap.put("paytype", payType);
        /**
         * 3 进行签名,同样放入map
         */
        String sign = SignUtils.getSign(paramMap, mechSecret); //签名
        paramMap.put("sign", sign);
        System.out.println("sign=" + sign);

        /**
         * 4 请求支付服务,
         * （1）WAP支付方式：实际应用中客户端无需解析response，直接重定向到拼接的地址reqUrl即可
         * 		如：return new ModelAndView("redirect:"+reqUrl);
         */
        String reqParams = SignUtils.getParamStr(paramMap);
        System.out.println("params=" + reqParams);
        String reqUrl = payPlatform.getPayGetUrl() + "?" + reqParams;
        System.out.println("reqUrl=" + reqParams);


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
        log.info("   BirdPayAliPayWapImpl callback params  [ {} ]  ", params.toString());

        //0成功,非0失败
        int status = Integer.parseInt(params.getOrDefault("status", "-1"));
        //默认UTF-8
        String charset = params.getOrDefault("charset", "UTF-8");
        //平台交易号
        String transactionId = params.getOrDefault("transactionid", "");
        //银行交易号
        String outtransactionId = params.getOrDefault("", "");
        //平台订单号
        String outorderNo = params.getOrDefault("outorderno", "");
        //支付金额，以分为单位
        Integer totalfee = Integer.parseInt(params.getOrDefault("totalfee", "0"));
        //交易状态 取值说明：100：成功，0：初始化，1：进行中，3：退款 , 4：取消
        String tradeState = params.getOrDefault("tradestate", "0");
        //支付时间 UNIX时间戳
        String payTime = params.getOrDefault("paytime", "");
        //目前填的是商户订单号,对应请求参数orderno
        String extra = params.getOrDefault("extra", "");
        //除sign外的请求参数按字典排序后加上密钥进行签名，签名为大写字母，文档最后会附上java和php版的签名算法
        String sign = params.getOrDefault("sign", "");

        Map<String, String> map = new HashMap<>();
        for (String s : params.keySet()) {
            String value = params.get(s);
            if (StringUtils.isNotBlank(value) && !StringUtils.equals(s, "sign")) {
                map.put(s, value);
            }
        }

        String comSign = SignUtils.getSign(map, payPlatform.getMerchKey());

        if (StringUtils.equals(sign, comSign)) {
            if (status == 0) {
                if (null != extra && !"".equals(extra)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(extra);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(extra);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        if (price.equals(totalfee)) {
                            if (rechargeOrder.getPayStatus().equals(0)) {
                                Date nowTime = new Date();
                                rechargeOrder.setPayTime(nowTime);
                                rechargeOrder.setStatus(1);
                                rechargeOrder.setPayStatus(1);
                                int count = 0;
                                count = rechargeOrderService.payAfterHandle(count, rechargeOrder, beforeRechargeOrder);
                                if (count > 0) {
                                    count = baseDao.updateByEntity(rechargeOrder);
                                }
                                return count > 0 ? "SUCCESS" : "FAIL";
                            } else {
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId());
                                return "SUCCESS";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] , [ {} ]", price, totalfee);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", extra);
                        return "FAIL";
                    }
                } else {
                    log.info("透传参数异常 [ {} ]", extra);
                }
            } else {
                log.info("状态异常 [ {} ]", status);
                return "FAIL";
            }
        } else {
            log.info("签名错误");
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
