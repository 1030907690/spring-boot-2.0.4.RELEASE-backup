package com.rw.article.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.type.RedirectPayPlatformPageType;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.entity.SysConfig;
import com.rw.article.pay.entity.vo.RedirectObject;
import com.rw.article.pay.entity.vo.RedirectObjectParams;
import com.rw.article.pay.service.IPayPlatformRedirectService;
import com.rw.article.pay.service.ISysConfigRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 中转地址
 * @date 2018/11/7 9:09
 */
@Service
public class PayPlatformRedirectServiceImpl implements IPayPlatformRedirectService {

    @Resource
    private ISysConfigRepository sysConfigRepository;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public String getRedirectUrl(RedirectPayPlatformPageType redirectPayPlatformPageType, RechargeOrder order, Map<String, String> map,PayPlatform payPlatform,String method) {
        //支付项目地址
        String payProjectUrlPrefix = Constants.DEFAULT_PAY_PROJECT_URL_PREFIX;
        SysConfig payProjectUrlPrefixConf = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("pay_project_url_prefix", 1);
        if (null != payProjectUrlPrefixConf && payProjectUrlPrefixConf.getItemVal() != null) {
            payProjectUrlPrefix = payProjectUrlPrefixConf.getItemVal();
        }


        String payProjectUrl = payProjectUrlPrefix + Constants.DEFAULT_PAY_PROJECT_URL_REDIRECT;

        //支付平台转发验证秘钥
        SysConfig verificationSys = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("pay_platform_redirect_verification", 1);
        String platformRedirectVerification = Constants.DEFAULT_PAY_PLATFORM_REDIRECT_VERIFICATION;
        if (null != verificationSys && verificationSys.getItemVal() != null) {
            platformRedirectVerification = verificationSys.getItemVal();
        }


        List<RedirectObjectParams> redirectObjectParamsList = new ArrayList();

        map.forEach((k, v) -> {
            redirectObjectParamsList.add(new RedirectObjectParams(k,v));
        });
        RedirectObject redirectObject = new RedirectObject(redirectObjectParamsList,method,payPlatform.getPayGetUrl());

        redisTemplate.opsForValue().set(Constants.REDIRECT_PAY_PLATFORM_REDIS_PREFIX+order.getOrderId(), JSON.toJSONString(redirectObject),Constants.DEFAULT_REDIS_CACHE_SECONDS, TimeUnit.SECONDS);
        return payProjectUrl + "?payPlatform="+redirectPayPlatformPageType.getCode()+"&orderId="+order.getOrderId()+"&sign="+ MD5Utils.MD5Encoding(order.getOrderId() + platformRedirectVerification);
    }
}
