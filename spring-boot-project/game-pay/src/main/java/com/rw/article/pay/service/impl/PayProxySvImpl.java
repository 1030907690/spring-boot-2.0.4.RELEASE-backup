package com.rw.article.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.spring.BeansUtils;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.service.IPayPlatformRepository;
import com.rw.article.pay.service.IPayProxySv;
import com.rw.article.pay.service.IPayRealizeSv;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: ${todo}
 * @date 2018/8/116:15
 */
@Service
public class PayProxySvImpl implements IPayProxySv {

    private static final Logger log = LoggerFactory.getLogger(PayProxySvImpl.class);

    @Resource
    BeansUtils beansUtils;

    @Resource
    private IPayPlatformRepository payPlatformRepository;

    @Override
    public JsonObject gateway(RechargeOrder rechargeOrder, Map<String, String> params) {
       // log.info("PayProxySvImpl  gateway ");
        PayPlatform payPlatform = getPayPlatform(Integer.parseInt(params.getOrDefault("payType", "0")));
       // log.info("payPlatform " + JSON.toJSONString(payPlatform));
        if (null != payPlatform && payPlatform.getStatus() == 1 && StringUtils.isNotBlank(payPlatform.getHandler())) {
            IPayRealizeSv payRealizeSv = beansUtils.getBean(payPlatform.getHandler(), IPayRealizeSv.class);
            return payRealizeSv != null ? payRealizeSv.gateway(payPlatform, rechargeOrder, params) : null;
        }

        return null;
    }

    /***
     * zhouzhongqing
     * 2018年5月16日14:44:02
     * 获取平台配置
     * */
    private PayPlatform getPayPlatform(int payType) {
        List<PayPlatform> payPlatformList = payPlatformRepository.findByPayTypeAndStatus(payType, 1);
        if (null != payPlatformList && payPlatformList.size() == 1) {
            return payPlatformList.get(0);
        }
        return null;
    }


    @Override
    public String callback(int platform, Map<String, String> params) {
        PayPlatform payPlatform = getPayPlatform(platform);
        if (null != payPlatform && payPlatform.getStatus() == 1 && StringUtils.isNotBlank(payPlatform.getHandler())) {
            IPayRealizeSv payRealizeSv = beansUtils.getBean(payPlatform.getHandler(), IPayRealizeSv.class);
            return payRealizeSv != null ? payRealizeSv.callback(payPlatform, params) : null;
        }
        return null;
    }
}
