package com.rw.article.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 发送关注的消息
 * @date 2018/8/2316:41
 */
public class SendAdminOfflineMessageLog {

    private volatile static SendAdminOfflineMessageLog sendAdminOfflineMessageLog;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private SendAdminOfflineMessageLog(){

    }

    /***
     *得到实例
     * */
    public static SendAdminOfflineMessageLog getInstance() {
        if (sendAdminOfflineMessageLog == null) {
            synchronized (SendAdminOfflineMessageLog.class) {
                if (sendAdminOfflineMessageLog == null ) {
                    sendAdminOfflineMessageLog = new SendAdminOfflineMessageLog();
                }
            }
        }
        return sendAdminOfflineMessageLog;
    }


    /***
     * zhouzhongqing
     * 2018年8月23日16:50:47
     * 发送关注消息
     * @param url
     * @param type
     * @param context
     * */
    public void sendAdminOfflineMessageLog(String url,Integer type,String context){
        Map<String,String> params = new HashMap();
        params.put("id","0");
        params.put("type",type.toString());
        params.put("context",context);
        logger.info(" params url [ {} ] , [{}] ",url,params);
        HttpClient.sendHttpRequestPost(url == null ? "http://192.168.0.134:8080/gameSocket/send" : url,params,HttpClient.ENCODING);

    }



}
