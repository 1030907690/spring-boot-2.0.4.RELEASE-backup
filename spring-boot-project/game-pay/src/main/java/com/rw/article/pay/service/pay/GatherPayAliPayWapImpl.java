package com.rw.article.pay.service.pay;


import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.CommonFunction;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.IRechargeOrderRepository;
import com.rw.article.pay.service.IRechargeOrderService;
import com.rw.article.pay.service.ISysMessageService;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.*;

/**
 * 聚合支付 支付宝网页支付
 *
 * @author ZengXianxue
 * @date 2018-04-17 14:06
 */
@SuppressWarnings("All")
@Service
public class GatherPayAliPayWapImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(GatherPayAliPayWapImpl.class);


    private static final String payType = "AL_H5";


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
        String url = payPlatform.getPayGetUrl();
        TreeMap<String, Object> treeMap = new TreeMap<>();
        String redirectUrl = null;
        treeMap.put("key", payPlatform.getMerchKey());
        try {
            treeMap.put("mchId", payPlatform.getMerchId());
            treeMap.put("payType", payType);
            //判断充值金额是否等于最小充值金额, 如果等于加1分,如果不是随机减1-2分钱
            Long rechargeMoney = order.getRechargePrice();
            if(order.getRechargePrice().equals(payPlatform.getPayMin())){
                rechargeMoney = (rechargeMoney / 100) +1;
            }else{
                rechargeMoney = (rechargeMoney / 100) -  RandomUtils.nextInt(1, 3);
            }
            treeMap.put("amount", rechargeMoney);
            treeMap.put("mchOrderNo", params.get("orderId"));
            treeMap.put("notifyUrl", payPlatform.getNotifyUrl());
            // 签名
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
            String sign = toSign(payPlatform.getMerchKey(), treeMap);// md5 签名
            treeMap.put("sign", sign);
            // 拼接url
            redirectUrl = url + "?" + sb + "&sign=" + sign;

            //修改实际要支付的钱
            RechargeOrder rechargeOrderNew = new RechargeOrder();
            rechargeOrderNew.setId(order.getId());
            rechargeOrderNew.setMoney(Integer.parseInt(rechargeMoney.toString()));
            rechargeOrderNew.setRemark("用户实际要支付"+ CommonFunction.round(rechargeMoney,100)+ "人民币");
            baseDao.updateByEntity(rechargeOrderNew);
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
