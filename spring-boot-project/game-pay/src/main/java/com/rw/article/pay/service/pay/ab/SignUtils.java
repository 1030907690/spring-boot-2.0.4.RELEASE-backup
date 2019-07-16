package com.rw.article.pay.service.pay.ab;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
public class SignUtils {
    private static final String transdata = "transdata";
    private static final String sign = "sign";
    private static final String signtype = "signtype";

    /**
     * 组装请求参数
     *
     * @param respData 从爱贝服务端获取的签名数据
     * @return 包含各个签名数据的一个map，有transdata，sign，signtype三个
     */
    public static Map<String, String> getParameter(String respData) {
        //开始分割参数
        Map<String, String> resultMap = new HashMap<String, String>();
        try {
            String[] dataArray = respData.split("&");
            for (String s : dataArray) {
                if (s.startsWith(transdata)) {
                    resultMap.put(transdata, URLDecoder.decode(s.substring(s.indexOf("=") + 1, s.length()), "UTF-8"));
                } else if (s.startsWith(signtype)) {
                    resultMap.put(signtype, s.substring(s.indexOf("=") + 1, s.length()));
                } else if (s.startsWith(sign)) {
                    resultMap.put(sign, URLDecoder.decode(s.substring(s.indexOf("=") + 1, s.length()), "UTF-8"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}