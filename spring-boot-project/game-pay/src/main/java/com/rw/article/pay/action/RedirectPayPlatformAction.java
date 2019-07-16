package com.rw.article.pay.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.type.RedirectPayPlatformPageType;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.entity.SysConfig;
import com.rw.article.pay.entity.vo.RedirectObject;
import com.rw.article.pay.entity.vo.RedirectObjectParams;
import com.rw.article.pay.service.ISysConfigRepository;
import com.rw.article.pay.service.impl.RechargeOrderServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 重定向到支付平台
 * @date 2018/11/6 15:01
 */
@Controller
@RequestMapping("/redirectPayPlatformAction")
public class RedirectPayPlatformAction {
    private final   Logger log = LoggerFactory.getLogger(RedirectPayPlatformAction.class);

    @Resource
    private ISysConfigRepository sysConfigRepository;


    @Resource
    private RedisTemplate redisTemplate;




    //@RequestMapping(value = "/test", method = RequestMethod.GET)
    public ModelAndView test(){

        //TODO 测试代码

        List<RedirectObjectParams> list = new ArrayList();
        list.add(new RedirectObjectParams("name","lisi"));
        list.add(new RedirectObjectParams("wd","撒地方"));
        RedirectObject redirectObject = new RedirectObject(list,"post","https://www.baidu.com");

        redisTemplate.opsForValue().set(Constants.REDIRECT_PAY_PLATFORM_REDIS_PREFIX+"1", JSON.toJSONString(redirectObject),6, TimeUnit.SECONDS);

        String redirectObjectJson = (String) redisTemplate.opsForValue().get(Constants.REDIRECT_PAY_PLATFORM_REDIS_PREFIX+"1");
        RedirectObject redirectObjectTemp = JSONObject.parseObject(redirectObjectJson,RedirectObject.class);
        ModelAndView view = new ModelAndView();
        view.setViewName("index");
        view.addObject("redirectObject",redirectObjectTemp);

        return view;
    }
    /***
     *zhouzhongqing
     * 2018年11月6日15:26:09
     *转发页面
     * */
    @RequestMapping("/redirect")
    public ModelAndView redirect(@Param("payPlatform")Integer payPlatform, @Param("orderId") String orderId,@Param("sign")String sign){
        log.info(" payPlatform [ {} ],orderId [ {} ],sign [ {} ] ",payPlatform,orderId,sign);
        //支付平台转发验证秘钥
        SysConfig verificationSys = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("pay_platform_redirect_verification", 1);
        String platformRedirectVerification = Constants.DEFAULT_PAY_PLATFORM_REDIRECT_VERIFICATION;
        if (null != verificationSys && verificationSys.getItemVal() != null) {
            platformRedirectVerification = verificationSys.getItemVal();
        }

        if(StringUtils.isNotBlank(orderId)) {
            String validSign = MD5Utils.MD5Encoding(orderId + platformRedirectVerification);
            if (null != sign && sign.equals(validSign)) {
                RedirectPayPlatformPageType redirectPayPlatformPageType = RedirectPayPlatformPageType.convert(payPlatform);
                if (null != redirectPayPlatformPageType) {
                    String redirectObjectJson = (String) redisTemplate.opsForValue().get(Constants.REDIRECT_PAY_PLATFORM_REDIS_PREFIX + orderId);
                    RedirectObject redirectObjectTemp = JSONObject.parseObject(redirectObjectJson, RedirectObject.class);
                    if(null != redirectObjectTemp){
                        ModelAndView view = new ModelAndView();
                        view.setViewName(redirectPayPlatformPageType.getUrl());
                        view.addObject("redirectObject",redirectObjectTemp);
                        return view;
                    }else{
                        log.info("redirectObjectTemp is null");
                    }
                } else {
                    log.info(" 无处理页面 [ {} ]", payPlatform);
                }
            } else {
                log.info("sign不匹配 [ {} ]", validSign);
            }
        }else{
            log.info("orderId is null [ {} ]", orderId);
        }
        return new ModelAndView("404");
    }

}
