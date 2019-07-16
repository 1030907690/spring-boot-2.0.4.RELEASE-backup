package com.rw.article.common.utils;

import com.rw.article.common.constant.Constants;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: ${todo}
 * @date 2018/8/117:21
 */
public class CommonFunction {
    /***
     * zhouzhongqing
     * 2018年6月19日19:15:24
     * 金币保留2位小数
     * */
    public static String moneyRetainDecimal(Double money){
        DecimalFormat formater = new DecimalFormat("0.00");
        return  formater.format(money);
    }
    /***
     * zhouzhongqing
     * 2018年6月19日19:15:24
     * 金币保留2位小数
     * */
    public static String moneyRetainDecimal(String money){
        if( money == null ){
            return "0.00";
        }
        return moneyRetainDecimal(Double.parseDouble(money));
    }

    /**
     * 人民币转金币
     */
    public static Long exchangeGoldCoin(String convertmoney, Long money) {
        try {
            long num = (long) (money * Constants.TEN_THOUSAND);
            if (StringUtils.isNotBlank(convertmoney) && StringUtils.isNumeric(convertmoney)) {
                num = num * Integer.parseInt(convertmoney);//  需要乘以倍率
            }
            return num;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 人民币转金币
     */
    public static Long exchangeGoldCoin(String convertmoney, Double money) {
        try {
            long num = (long) (money * Constants.TEN_THOUSAND);
            if (StringUtils.isNotBlank(convertmoney) && StringUtils.isNumeric(convertmoney)) {
                num = num * Integer.parseInt(convertmoney);//  需要乘以倍率
            }
            return num;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 金币倍率判定
     */
    public static Double getGoldIsMultipl(Long gold) {
        //return gold == null ? 0 : gold * 1.00 / Constants.TEN_THOUSAND;
        if (null != gold) {
            return round(gold, Constants.TEN_THOUSAND);
        }
        return 0.00;
    }

    /**
     * 金币倍率判定
     */
    public static Double getGoldIsMultipl(Double gold) {
        //return gold == null ? 0 : gold * 1.00 / Constants.TEN_THOUSAND;
        if (null != gold) {
            return round(gold, Constants.TEN_THOUSAND);
        }
        return 0.00;
    }

    /**
     * long和long相加
     *
     * @param v1
     * @param v2
     * @return double
     */
    public static long add(long v1, long v2) {
        BigDecimal b1 = new BigDecimal(Long.toString(v1));
        BigDecimal b2 = new BigDecimal(Long.toString(v2));
        return b1.add(b2).longValue();
    }


    /**
     * double和double相加
     *
     * @param v1
     * @param v2
     * @return double
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * double和double相减
     *
     * @param v1
     * @param v2
     * @return double
     **/
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }




    /**
     * double和long相乘
     *
     * @param v1
     * @param v2
     * @return long
     */
    public static Integer multiply(int v1, int v2) {
        BigDecimal b1 = new BigDecimal(Integer.toString(v1));
        BigDecimal b2 = new BigDecimal(Integer.toString(v2));
        return b1.multiply(b2).intValue();
    }

    /**
     * double和long相乘
     *
     * @param v1
     * @param v2
     * @return long
     */
    public static long multiply(float v1, long v2) {
        BigDecimal b1 = new BigDecimal(Float.toString(v1));
        BigDecimal b2 = new BigDecimal(Long.toString(v2));
        return b1.multiply(b2).longValue();
    }

    /**
     * double和long相乘
     *
     * @param v1
     * @param v2
     * @return long
     */
    public static long multiply(double v1, long v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Long.toString(v2));
        return b1.multiply(b2).longValue();
    }

    /**
     * double和long相乘
     *
     * @param v1
     * @param v2
     * @return long
     */
    public static long multiply(long v1, long v2) {
        BigDecimal b1 = new BigDecimal(Long.toString(v1));
        BigDecimal b2 = new BigDecimal(Long.toString(v2));
        return b1.multiply(b2).longValue();
    }

    /**
     * double和int相乘
     *
     * @param v1
     * @param v2
     * @return long
     */
    public static long multiply(double v1, int v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Integer.toString(v2));
        return b1.multiply(b2).longValue();
    }


    /**
     * float和double相乘
     *
     * @param v1
     * @param v2
     * @return long
     */
    public static double multiply(float v1, double v2) {
        BigDecimal b1 = new BigDecimal(Float.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }


    /**
     * double 相乘
     *
     * @param v1
     * @param v2
     * @return double
     */
    public static double multiply(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }


    /**
     * 相除,做除法的时候相对的麻烦一点,涉及的多一些
     * 提供精确的小数位四舍五入处理。
     *
     * @param v1    需要四舍五入的数字
     * @param v2    需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static long round(long v1, long v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("此参数错误");
        }
        BigDecimal one = new BigDecimal(Long.toString(v1));
        BigDecimal two = new BigDecimal(Long.toString(v2));
        return one.divide(two, scale, BigDecimal.ROUND_HALF_UP).longValue();
    }


    /**
     * 相除,做除法的时候相对的麻烦一点,涉及的多一些
     * 提供精确的小数位四舍五入处理。
     *
     * @param v1    需要四舍五入的数字
     * @param v2    需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static Double roundDouble(long v1, long v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("此参数错误");
        }
        BigDecimal one = new BigDecimal(Long.toString(v1));
        BigDecimal two = new BigDecimal(Long.toString(v2));
        return one.divide(two, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 相除,做除法的时候相对的麻烦一点,涉及的多一些
     * 提供精确的小数位四舍五入处理。
     *
     * @param v1    需要四舍五入的数字
     * @param v2    需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("此参数错误");
        }
        BigDecimal one = new BigDecimal(Double.toString(v1));
        BigDecimal two = new BigDecimal(Double.toString(v2));
        return one.divide(two, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /**
     * 相除,做除法的时候相对的麻烦一点,涉及的多一些
     * 提供精确的小数位四舍五入处理。
     *
     * @param v1    需要四舍五入的数字
     * @param v2    需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v1, long v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("此参数错误");
        }
        BigDecimal one = new BigDecimal(Double.toString(v1));
        BigDecimal two = new BigDecimal(Double.toString(v2));
        return one.divide(two, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * double和double相除 保留2位小数
     *
     * @param v1
     * @param v2
     * @return double
     */
    public static double round(double v1, double v2) {
        return round(v1, v2, 2);
    }

    /**
     * double和double相除 保留2位小数
     *
     * @param v1
     * @param v2
     * @return double
     */
    public static double round(double v1, long v2) {
        return round(v1, v2, 2);
    }

    /**
     * 获得用户远程地址
     */
    public static String getRemoteAddress(HttpServletRequest request) {
        String remoteAddress = request.getHeader("X-Real-IP");


        if (StringUtils.isNotBlank(remoteAddress)) {
            remoteAddress = request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(remoteAddress)) {
            remoteAddress = request.getHeader("Proxy-Client-IP");
        } else if (StringUtils.isNotBlank(remoteAddress)) {
            remoteAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if(remoteAddress != null && remoteAddress.trim().contains(",")){//为什么会有这一步，因为经过多层代理后会有多个代理，取第一个ip地址就可以了
            String [] ips=remoteAddress.split(",");
            remoteAddress=ips[0];
        }
        return remoteAddress != null ? remoteAddress : request.getRemoteAddr();
    }

}
