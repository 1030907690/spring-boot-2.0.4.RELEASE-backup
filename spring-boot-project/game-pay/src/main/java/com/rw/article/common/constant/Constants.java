package com.rw.article.common.constant;

import com.rw.article.common.jackson.JsonObject;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 常量及公共方法
 * @date 2018/8/117:03
 */
public class Constants {


    /** 默认redis缓存秒数 **/
    public final static long DEFAULT_REDIS_CACHE_SECONDS = 50;

    /** 请求方式 **/
    public final static String POST = "post";
    public final static String GET = "get";

    /** 默认支付项目redirect地址 **/
    public final static String DEFAULT_PAY_PROJECT_URL_REDIRECT = "redirectPayPlatformAction/redirect";


    /** 默认支付项目地址 **/
    public final static String DEFAULT_PAY_PROJECT_URL_PREFIX = "http://192.168.0.111:8081/";


    /** 支付平台数据 redis前缀 **/
    public final static String REDIRECT_PAY_PLATFORM_REDIS_PREFIX = "redirect_pay_platform_redis_prefix:";

    /** 默认支付平台转发秘钥 **/
    public final static String DEFAULT_PAY_PLATFORM_REDIRECT_VERIFICATION = "123456";

    /** 支付默认回调页面 **/
    public final static String DEFAULT_HREF_BACKURL_PAGE = "https://pro.hzqp777.com/newDown/index.html";

    /**支付无返回**/
    public final static String NO_RETURN_ERROR = "支付无返回!";

    /**系统异常,请稍后再试!**/
    public final static String SYSTEM_ERROR = "系统异常,请稍后再试!";

    /*充值订单备注*/
    public final static String RECHARGE_ORDER_REMAKE = "用户%s实际支付%s人民币";


    /*充值关注*/
    public final static String ATTENTION_RECHARGE = "用户%s有新的充值,请您尽快查看!";

    /*db config*/
    public final static int NOT_DB_CONFIG = 1;


    /**100**/
    public final static int ONE_HUNDRED = 100;

    /**
     * 系统庄家用户编号
     */
    public static final int SYS_BANKER_USER_ID = -100;

    /***
     * 金币固定倍率数据库一万
     * */
    public final static long TEN_THOUSAND = 10000;


    public static class ErrorCode {
        /**
         * 系统错误
         */
        public static final String SYSTEM_ERROR = "1000";
        /**
         * 账户异常
         */
        public static final String ACCOUNT_EXCEPTION = "1001";

        /**
         * 其他错误
         */
        public static final String OTHER_ERROR = "1100";
    }
    /**
     * 操作成功标志
     */
    public static final String SUCCESS_MARK = "1";



    public final static String RECHARGE_ORDER = "recharge_order";



    public static JsonObject getErrorMessage(String code, String cmd, String message) {
        return getErrorMessage(code, cmd, message, null);
    }

    public static JsonObject getErrorMessage(String code, String cmd, String message, Object data) {
        JsonObject error = new JsonObject();
        error.put("cmd", cmd);
        error.put("code", code);
        error.put("message", message);
        if (data != null) {
            error.put("data", data);
        }
        return error;
    }


    /**
     * 获取操作成功消息对象
     *
     * @param cmd     命令
     * @param message 消息内容
     * @return 消息对象
     */
    public static JsonObject getSuccessMessage(String cmd, String message) {
        return getSuccessMessage(cmd, message, null);
    }



    public static JsonObject getSuccessMessage(String cmd, String message, Object data) {
        JsonObject success = new JsonObject();
        success.put("cmd", cmd);
        success.put("code", SUCCESS_MARK);
        success.put("message", message);
        if (data != null) {
            success.put("data", data);
        }
        return success;
    }

}
