package com.rw.article.pay.action;

import com.rw.article.common.utils.CommonFunction;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayProxySv;
import com.rw.article.pay.service.IRechargeOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 创建订单的controller
 * @date 2018/8/115:49
 */
@Controller
@RequestMapping("/rechargeOrder")
public class RechargeOrderAction {

    @Resource
    private IPayProxySv payProxySv;
    private static final Logger log = LoggerFactory.getLogger(RechargeOrderAction.class);

    @Resource
    private IRechargeOrderService rechargeOrderService;

    /***
     * zhouzhongqing
     * 2018年1月15日20:22:34
     * 创建充值订单
     * @param request
     * @param response
     * */
    @RequestMapping("/createRechargeOrder")
    @ResponseBody
    public Object createRechargeOrder(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "Set-Cookie");
        String price = request.getParameter("price");
        String payType = request.getParameter("payType");
        String device = request.getParameter("device");
        String userId = request.getParameter("userId");
        //类型 0 是公众号支付 1 是app支付 2是网页支付
        String type = request.getParameter("type");
        String remoteAddress = CommonFunction.getRemoteAddress(request);
        log.info(" createRechargeOrder ip [ {} ]", remoteAddress);
        JsonObject info = rechargeOrderService.createRechargeOrder(userId,price,payType,type,device,remoteAddress);
        //成功
        if(Constants.SUCCESS_MARK.equals(info.getString("code").toString())) {
            String orderNo = info.getString("message");
            RechargeOrder rechargeOrder = rechargeOrderService.getRechargeOrderByOrderNo(orderNo);
            if(null != rechargeOrder){
                Map<String ,String> params = new HashMap<>();
                params.put("orderId", rechargeOrder.getOrderId());// 订单编号

                params.put("price", rechargeOrder.getMoney().toString()); // 价格
                params.put("device", device);
                params.put("payType", payType); // 支付类型
                params.put("attach", rechargeOrder.getOrderId());
                //没有微信支付 operId暂时不要
                // params.put("operId", userInfo.getPayOpenId());

                if (-1 != remoteAddress.indexOf(",")) {
                    remoteAddress = remoteAddress.split(",")[0];
                }
                params.put("spbillCreateIp", remoteAddress);
                params.put("goodsName", "购买" + price + "元金币");
                // 没有微信支付 支付成功后重定向地址暂时不要
                //getRedirect(params, Integer.parseInt(payType));
                JsonObject jsonObject = payProxySv.gateway(rechargeOrder,params);
                jsonObject.put("cmd", Constants.RECHARGE_ORDER);
                return jsonObject;
            }
        }
        return info;
    }


}
