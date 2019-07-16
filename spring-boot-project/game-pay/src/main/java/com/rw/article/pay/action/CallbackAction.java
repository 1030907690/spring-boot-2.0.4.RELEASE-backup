package com.rw.article.pay.action;


import com.rw.article.pay.service.IPayProxySv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/***
 *@author zhouzhongqing
 * 2018年8月2日14:45:00
 * 支付回调
 * */
@RestController
public class CallbackAction {

    private static final Logger log = LoggerFactory.getLogger(CallbackAction.class);
    private IPayProxySv payProxySv;

    public CallbackAction(IPayProxySv payProxySv) {
        this.payProxySv = payProxySv;
    }

    @RequestMapping(value = "/notify/{pid}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String callback(@PathVariable String pid, HttpServletRequest request) {
        Enumeration enumeration = request.getParameterNames();
        Map<String, String> params = new HashMap<>();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            params.put(name, request.getParameter(name));
        }
        params.put("body", getBody(request));
        return payProxySv.callback(Integer.parseInt(pid), params);
    }

    private String getBody(HttpServletRequest request) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            CharArrayWriter data = new CharArrayWriter();
            char[] buf = new char[8192];
            int ret;
            while ((ret = in.read(buf, 0, 8192)) != -1) {
                data.write(buf, 0, ret);
            }
            return data.toString();
        } catch (Exception e) {
            log.error("接收BODY内容失败：", e);
        }
        return null;
    }
}
