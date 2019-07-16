package com.rw.article.pay.service.pay;

import com.alibaba.fastjson.JSON;
import com.rw.article.common.constant.Constants;

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
import java.security.MessageDigest;
import java.util.*;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 通汇支付宝网页支付
 * @date 2018/9/211 5:10
 */
@Service
public class ThroughRemittanceAliPayWapImpl implements IPayRealizeSv {
    private static final Logger log = LoggerFactory.getLogger(ThroughRemittanceAliPayWapImpl.class);


    private static final String PAY_TYPE = "alipaywap";

    @Resource
    private IRechargeOrderService rechargeOrderService;


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
        log.info("拉起通汇支付宝wap支付");
        JsonObject jsonObject = new JsonObject();
        TreeMap<String, String> map = new TreeMap<>();
        String redirectUrl = null;
        try {
            SysConfig pageConfig = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("href_backurl_page", 1);
            if (null == pageConfig) {
                pageConfig = new SysConfig();
                pageConfig.setItemVal("https://pro.hzqp777.com/newDown/index.html");
            }

            map.put("version", "1.0");
            map.put("customerid", payPlatform.getMerchId());
            map.put("sdorderno", order.getOrderId());
            //精确到小数点后两位，例如10.24
            map.put("total_fee", OtherUtils.moneyRetainDecimal(OtherUtils.getGoldIsMultipl(order.getRechargePrice())));
            map.put("paytype", PAY_TYPE);
            map.put("notifyurl", payPlatform.getNotifyUrl());
            //不能带有任何参数
            map.put("returnurl", pageConfig.getItemVal());

            //version={value}&customerid={value}&total_fee={value}&sdorderno={value}&notifyurl={value}&returnurl={value}&{apikey}
            StringBuffer signTemp = new StringBuffer();
            signTemp.append("version=" + map.getOrDefault("version", ""));
            signTemp.append("&customerid=" + map.getOrDefault("customerid", ""));
            signTemp.append("&total_fee=" + map.getOrDefault("total_fee", ""));
            signTemp.append("&sdorderno=" + map.getOrDefault("sdorderno", ""));
            signTemp.append("&notifyurl=" + map.getOrDefault("notifyurl", ""));
            signTemp.append("&returnurl=" + map.getOrDefault("returnurl", ""));
            signTemp.append("&" + payPlatform.getMerchKey());

            map.put("sign", MD5Utils.MD5Encoding(signTemp.toString()));

            StringBuffer sb = new StringBuffer(payPlatform.getPayGetUrl()+"?");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                sb.append(entry.getKey()+"="+entry.getValue()+"&");
            }
            sb.deleteCharAt(sb.length() - 1);
            redirectUrl = sb.toString();
            jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
        } catch (Exception e) {
            log.error("获取参数错误 [{}] [ {} ] [ {} ]", e, e.getMessage(), e.getStackTrace());
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
        log.info("   ThroughRemittanceAliPayWapImpl callback params  [ {} ]  ", params.toString());
        //回调参数

        //订单状态 1:成功，其他失败
        String status = params.getOrDefault("status","");
        //商户编号
        String customerId = params.getOrDefault("customerid","");
        //平台订单号
        String sdpayNo = params.getOrDefault("sdpayno","");
        //商户订单号
        String sdorderNo = params.getOrDefault("sdorderno","");
        //交易金额 最多两位小数
        String totalFee = params.getOrDefault("total_fee","");
        //支付类型
        String payType = params.getOrDefault("paytype","");
        //订单备注说明
        String remark = params.getOrDefault("remark","");
        //md5验证签名串
        String sign = params.getOrDefault("sign","");
        //customerid={value}&status={value}&sdpayno={value}&sdorderno={value}&total_fee={value}&paytype={value}&{apikey}
        // md5 签名
        String signStr = MD5Utils.MD5Encoding("customerid="+payPlatform.getMerchId()+ "&status="+status+"&sdpayno="+sdpayNo+"&sdorderno="+sdorderNo+"&total_fee="+totalFee+"&paytype="+payType+"&"+payPlatform.getMerchKey());
        log.info("signStr:  [ {} ]", signStr);


        if (StringUtils.equals(sign, signStr)) {
            if ("1".equals(status)) {
                if (null != sdorderNo && !"".equals(sdorderNo)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(sdorderNo);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(sdorderNo);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        //第三方返回的   乘以100去比较
                        Integer price_amount = Integer.parseInt(String.valueOf(OtherUtils.multiply(Double.parseDouble(totalFee),Constants.ONE_HUNDRED)));
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

                                return count > 0 ? "success" : "FAIL";
                            } else {
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId());
                                return "success";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", price_amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", sdorderNo);
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


}
