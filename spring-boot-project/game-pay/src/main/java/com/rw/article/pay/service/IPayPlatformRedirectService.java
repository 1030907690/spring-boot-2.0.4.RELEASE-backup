package com.rw.article.pay.service;

import com.rw.article.common.type.RedirectPayPlatformPageType;
import com.rw.article.pay.entity.PayPlatform;
import com.rw.article.pay.entity.RechargeOrder;

import java.util.Map;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 支付平台转发
 * @date 2018/11/7 9:07
 */
public interface IPayPlatformRedirectService {

    /**
     * zhouzhongqing
     * 2018年11月7日09:08:44
     * 得到支付平台中转的地址
     * **/
    String getRedirectUrl(RedirectPayPlatformPageType redirectPayPlatformPageType, RechargeOrder order, Map<String,String> map,PayPlatform payPlatform,String method);

}
