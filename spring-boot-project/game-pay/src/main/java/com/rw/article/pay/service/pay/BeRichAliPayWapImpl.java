package com.rw.article.pay.service.pay;

import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.type.ReturnType;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayRealizeSv;
import com.rw.article.pay.service.IRechargeOrderRepository;
import com.rw.article.pay.service.IRechargeOrderService;
import com.rw.article.pay.service.ISysMessageService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * zhouzhongqing
 * 2018年6月23日14:20:54
 * 乾富吧wap 支付宝支付
 */
@Service
public class BeRichAliPayWapImpl implements IPayRealizeSv {

    private static final Logger log = LoggerFactory.getLogger(BeRichAliPayWapImpl.class);


    private static final String PAY_TYPE = "aliwap";

   /* @Reference
    private IPaySvApi paySv;

    @Reference
    private IOrderService orderService;

    @Reference
    private IRechargeOrderService rechargeOrderService;

    @Reference
    private ISysMessageService messageService;

    @Reference
    private IUserService userService;

    @Reference
    private ISysConfigService sysConfigService;

    @Resource
    private com.rw.article.service.pay.IRechargeOrderService iRechargeOrderService;

*/



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

        try {
            Map<String,Object> getMap = getParamsMap(payPlatform,order,params);
            StringBuilder strSign = new StringBuilder();
            for (Map.Entry<String, Object> entry : getMap.entrySet()) {
                strSign.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            log.info("发送参数: [ {} ] ",strSign.toString());
            if(StringUtils.isNotBlank(strSign.toString())){
                strSign.deleteCharAt(strSign.length()-1);
                jsonObject.put("code", Constants.SUCCESS_MARK);
                jsonObject.put("redirect", payPlatform.getPayGetUrl()+"?"+strSign.toString());
            }else{
                jsonObject.put("code", Constants.ErrorCode.SYSTEM_ERROR);
            }
        }catch (Exception e){
            log.info("调用支付异常 [ {} ] ",e);
            jsonObject.put("code", Constants.ErrorCode.SYSTEM_ERROR);
        }
        jsonObject.put("type", ReturnType.JUMP_PAGE_TYPE.getCode());
        return jsonObject;
    }

    /***
     *得到参数map
     * */
    public Map<String,Object> getParamsMap(PayPlatform payPlatform,RechargeOrder order,Map<String, String> params) {
       Map<String,Object> map = new HashMap<>();
        String mch_number = payPlatform.getMerchId();
        map.put("mch_number",mch_number);
        map.put("pay_type",PAY_TYPE);
        map.put("totle_amount",order.getMoney());
        String this_date = System.currentTimeMillis()/1000L+"";
        map.put("this_date",this_date);
        map.put("order_sn",order.getOrderId());
        String jump_url = "http://channel.kunbaow.com";
        map.put("jump_url",jump_url);
        String asyn_url = payPlatform.getNotifyUrl();
        map.put("asyn_url",asyn_url);
        map.put("bank_code","");
        map.put("extra_note","");
        map.put("ip_add",params.getOrDefault("spbillCreateIp",""));
        String sign_info = MD5Utils.MD5Encoding(payPlatform.getMerchKey()).toLowerCase();
        sign_info = order.getOrderId() + order.getMoney() + PAY_TYPE + this_date + mch_number + sign_info;
        sign_info = MD5Utils.MD5Encoding(sign_info).toLowerCase();
        map.put("sign_info",sign_info);
        return map;
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
        log.info("   BeRichAliPayWapImpl callback params  [ {} ]  ", params.toString());


         String order_sn = params.getOrDefault("order_sn","");        //订单号码
         String platform_sn = params.getOrDefault("platform_sn","");     //系统平台返回的订单
         int totle_amount = Integer.parseInt(params.getOrDefault("totle_amount","0"));       //支付金额 单位为分
         String mch_number = params.getOrDefault("mch_number","");        //商户注册获得的商务号
         String pay_type = params.getOrDefault("pay_type","");        //支付类型
         String extra_note = params.getOrDefault("extra_note","");      //商户下单时传的透传参数
         String transaction_id = params.getOrDefault("transaction_id","");  //银行交易单号
         int status = Integer.parseInt(params.getOrDefault("status",""));          //支付状态, 1：成功；0：失败
         String sign_info = params.getOrDefault("sign_info","");       //签名
         String this_date = params.getOrDefault("this_date","");         //时间戳

        String signStr = MD5Utils.MD5Encoding(payPlatform.getMerchKey()).toLowerCase();

        signStr = order_sn + platform_sn + totle_amount + mch_number + pay_type + this_date + signStr;

        String sign =  MD5Utils.MD5Encoding(signStr).toLowerCase();

        log.info("回调后自己的签名 [ {} ] ", sign);

        if (StringUtils.equalsIgnoreCase(sign, sign_info)) {
            if (1 == status) {
                if (null != order_sn && !"".equals(order_sn)) {
                    RechargeOrder beforeRechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(order_sn);
                    RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(order_sn);
                    if (null != rechargeOrder) {
                        //判断是否已被锁定状态
                        if(rechargeOrder.getStatus().equals(2)){
                            log.info("该订单已被锁定 [ {} ] ",rechargeOrder.getStatus());
                            return "FAIL";
                        }
                        Integer price = rechargeOrder.getMoney();
                        if (price.equals(totle_amount)) {
                            if(rechargeOrder.getPayStatus().equals(0)) {
                                Date nowTime = new Date();
                                rechargeOrder.setPayTime(nowTime);
                                rechargeOrder.setStatus(1);
                                rechargeOrder.setPayStatus(1);
                                int count = 0;

                                count = rechargeOrderService.payAfterHandle(count, rechargeOrder, beforeRechargeOrder);
                                if(count > 0){
                                    count = baseDao.updateByEntity(rechargeOrder);
                                }

                                return count > 0 ? "success" : "FAIL";
                            }else{
                                log.info("订单已支付  [ {} ] ", rechargeOrder.getOrderId() );
                                return "SUCCESS";
                            }
                        } else {
                            log.info("订单金额异常 [ {} ] ", totle_amount);
                            return "FAIL";
                        }
                    } else {
                        log.info("没有这个商户订单号 [ {} ]", order_sn);
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

}
