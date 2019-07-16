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
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 开彩网网页支付宝支付
 * @date 2018/9/1215:12
 */
@Service
public class AnnounceLotteryAliPayWapImpl implements IPayRealizeSv {

    @Resource
    private ISysConfigRepository sysConfigRepository;

    //支付方式
    private final String PAY_TYPE = "ALIWAP";

    @Resource
    private IRechargeOrderService rechargeOrderService;

    @Resource
    private IRechargeOrderRepository rechargeOrderRepository;

    @Resource
    private BaseDao baseDao;

    private static final Logger log = LoggerFactory.getLogger(AnnounceLotteryAliPayWapImpl.class);
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
        try {

            SysConfig pageConfig = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("href_backurl_page",1);
            if(null == pageConfig){
                pageConfig = new SysConfig();
                pageConfig.setItemVal("https://pro.hzqp777.com/newDown/");
            }

            Map<String,String> map = new TreeMap<>();
            map.put("value", String.valueOf(OtherUtils.round(order.getRechargePrice(),Constants.TEN_THOUSAND,2)));
            map.put("orderid",order.getOrderId());
            map.put("parter",payPlatform.getMerchId());
            map.put("type",PAY_TYPE);
            map.put("callbackurl",payPlatform.getNotifyUrl());
            map.put("hrefbackurl",pageConfig.getItemVal());
            map.put("attach",order.getOrderId());
            //Md5(parter={}&type={}&orderid={}&callbackurl={}key)
            String signTemp = MD5Utils.MD5Encoding("value="+map.getOrDefault("value","")+"&parter="+payPlatform.getMerchId()+"&type="+PAY_TYPE+"&orderid="+order.getOrderId()+"&callbackurl="+payPlatform.getNotifyUrl()+payPlatform.getMerchKey()).toLowerCase();
            map.put("sign",signTemp);

            StringBuffer sb = new StringBuffer(payPlatform.getPayGetUrl() + "?");

            for (Map.Entry<String, String> entry : map.entrySet()) {
                sb.append(entry.getKey() + "=" + entry.getValue() + "&");
            }
            sb.deleteCharAt(sb.length() - 1);

            redirectUrl = sb.toString();
            jsonObject.put("code", Constants.SUCCESS_MARK);
        }catch (Exception e){
            log.error("支付异常 [ {} ] [{}] [ {}]",e,e.getMessage(),e.getStackTrace() );
            jsonObject.put("code", Constants.ErrorCode.SYSTEM_ERROR);

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
        log.info("   AnnounceLotteryAliPayWapImpl callback params  [ {} ]  ", params.toString());

        //回调参数


        //订单号
        String orderId = params.getOrDefault("orderid","");
        //订单金额 以元为单位。
        String ovalue = params.getOrDefault("ovalue","");
        //0 成功， 其他失败
        String reState = params.getOrDefault("restate","2");
        //附加信息域
        String attach = params.getOrDefault("attach","");

        //签名 Md5(orderid={}&restate={}&ovalue={}key)
        String sign = params.getOrDefault("sign","");

        String signStr = MD5Utils.MD5Encoding("orderid="+orderId+"&restate="+reState+"&ovalue="+ovalue+payPlatform.getMerchKey());



        log.info("signStr:  [ {} ]", signStr);


        if (StringUtils.equals(sign, signStr)) {
            if ("0".equals(reState)) {
                if (null != orderId && !"".equals(orderId)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(orderId);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(orderId);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        //乘100来比较
                        Integer price_amount = Integer.parseInt(String.valueOf(OtherUtils.multiply(Double.parseDouble(ovalue),100)));
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
                        log.info("没有这个商户订单号 [ {} ]", orderId);
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
