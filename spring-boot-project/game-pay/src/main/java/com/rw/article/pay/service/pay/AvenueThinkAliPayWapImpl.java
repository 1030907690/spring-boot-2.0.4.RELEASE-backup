package com.rw.article.pay.service.pay;

import com.alibaba.fastjson.JSON;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.http.HttpClient;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.CommonFunction;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description:  道思支付宝wap支付
 * @date 2018/10/19 11:24
 */
@Service
public class AvenueThinkAliPayWapImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(AvenueThinkAliPayWapImpl.class);

    /**
     * 支付类型
     **/
    private final String PAY_TYPE = "alipaywap";

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

        log.info("调用道思支付宝wap支付");
        JsonObject jsonObject = new JsonObject();
        Map<String, String> map = new TreeMap<>();
        String redirectUrl = null;
        try {
            SysConfig pageConfig = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("href_backurl_page", 1);
            if (null == pageConfig) {
                pageConfig = new SysConfig();
                pageConfig.setItemVal("https://pro.hzqp777.com/newDown/index.html");
            }

            map.put("version","1.0");
            map.put("customerid",payPlatform.getMerchId());
            map.put("sdorderno",order.getOrderId());
            map.put("total_fee",CommonFunction.moneyRetainDecimal(CommonFunction.roundDouble(order.getRechargePrice(),Constants.TEN_THOUSAND,2)));
            map.put("paytype",PAY_TYPE);
            map.put("notifyurl",payPlatform.getNotifyUrl());
            map.put("returnurl",pageConfig.getItemVal());
            map.put("remark",order.getOrderId().toString());


            //version={value}&customerid={value}&total_fee={value}&sdorderno={value}&notifyurl={value}&returnurl={value}&apikey
            String signTemp = "version="+map.getOrDefault("version","")+"&customerid="+map.getOrDefault("customerid","")
                    +"&total_fee="+map.getOrDefault("total_fee","")+"&sdorderno="+map.getOrDefault("sdorderno","")
                    +"&notifyurl="+map.getOrDefault("notifyurl","")+"&returnurl="+map.getOrDefault("returnurl","")+"&"+payPlatform.getMerchKey();
            map.put("sign",MD5Utils.MD5Encoding(signTemp));

            StringBuffer sb = new StringBuffer(payPlatform.getPayGetUrl()+"?");

            log.info("请求参数 [ {} ]",JSON.toJSONString(map));

            map.forEach((k,v)->{
                sb.append(k+"="+v+"&");
            });

            if (null != sb && sb.length() > 0) {
                sb.deleteCharAt(sb.length()-1);
            }

            redirectUrl = sb.toString();

            jsonObject.put("code",Constants.SUCCESS_MARK);

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
        log.info("   AvenueThinkAliPayWapImpl callback params  [ {} ]  ", params.toString());
        //回调参数
        //订单状态 1成功 0失败
        String status = params.getOrDefault("status","");
        //customerid
        String customerId = params.getOrDefault("customerid","");
        //平台订单号
        String sdpayNo = params.getOrDefault("sdpayno","");
        //商户订单号
        String sdorderNo = params.getOrDefault("sdorderno","");
        //交易金额 最多两位小数
        String total_fee = params.getOrDefault("total_fee","");
        //支付类型
        String payType = params.getOrDefault("paytype","");
        //订单备注说明	原样返回
        String remark = params.getOrDefault("remark","");
        //md5验证签名
        String sign = params.getOrDefault("sign","");

        //customerid={value}&status={value}&sdpayno={value}&sdorderno={value}&total_fee={value}&paytype={value}&{apikey}
        StringBuffer signTemp = new StringBuffer();
        signTemp.append("customerid="+payPlatform.getMerchId());
        signTemp.append("&status="+status);
        signTemp.append("&sdpayno="+sdpayNo);
        signTemp.append("&sdorderno="+sdorderNo);
        signTemp.append("&total_fee="+total_fee);
        signTemp.append("&paytype="+PAY_TYPE+"&"+payPlatform.getMerchKey());
        String signStr = MD5Utils.MD5Encoding(signTemp.toString());
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

                        //使用订单金额比较  * 100 转为分
                        Integer price_amount = Integer.parseInt(String.valueOf(CommonFunction.multiply(Double.parseDouble(total_fee),Constants.ONE_HUNDRED)));
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
