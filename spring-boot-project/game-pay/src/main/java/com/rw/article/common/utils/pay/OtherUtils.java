package com.rw.article.common.utils.pay;

import com.rw.article.common.constant.Constants;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtherUtils {

    private static Pattern linePattern = Pattern.compile("_(\\w)");
    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    private static final Logger log = LoggerFactory.getLogger(OtherUtils.class);

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

        /*if(null != money){
            //判断是否有小数点
            int index = money.indexOf(".");
            if(-1 != index){
                //判断小数点后面有几位数
                //System.out.println(money.substring(index+1).length());
                int length = money.substring(index+1).length();
                if(length == 2){
                    return money;
                }else if(length > 2){
                    //System.out.println(money.substring(0,index) + "---" + money.substring(index,index+3));
                    return money.substring(0,index) + money.substring(index,index+3);
                }else if(length < 2){
                    if(length == 1){
                        //表示只有一位小数,加上后面1个0
                        return money+"0";
                    }else if(length == 0){
                        //表示只有点,加上后面2个0
                        return money+"00";
                    }
                }
            }else{
                //没有小数点之间加上.00
                return money+".00";
            }

        }*/
        return moneyRetainDecimal(Double.parseDouble(money));
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
     * 金币倍率判定
     */
    public static Double getGoldIsMultipl(Long gold) {
        //return gold == null ? 0 : gold * 1.00 / Constants.TEN_THOUSAND;
        if (null != gold) {
            return round(gold, Constants.TEN_THOUSAND);
        }
        return 0.00;
    }

    public static String toSign(String secret, Map<String, String> fields) {
        try {
            //按参数名asscic码排序
            List<String> list = new ArrayList<>();
            list.addAll(fields.keySet());
            Collections.sort(list);
            String strSign = "";
            for (String key : list) {
                strSign += key + "=" + fields.get(key) + "&";
            }
            strSign += "key=" + secret;
            String s = MD5Utils.MD5Encoding(strSign).toUpperCase();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) RandomUtils.nextInt(65, 91));
        }
        return sb.toString();
    }



    /**
     * 获取精确到秒的时间戳 10 位数
     *
     * @return
     */
    public static int getSecondTimestamp(Date date) {
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return Integer.valueOf(timestamp.substring(0, length - 3));
        } else {
            return 0;
        }
    }

    /***
     *zhouzhongqing
     * 2018年1月26日17:58:22
     * 验证钱进支付回调签名是否成功 成功返回true 否则返回false
     * */
    public static boolean verificationMoneyInSign(String order_id, String orderNo, int money, long mch, String pay_type, String sign, int time, String key) {
        String signStrTemp = order_id + orderNo + money + mch + pay_type + time + MD5Utils.MD5Encoding(key);
        String signTemp = MD5Utils.MD5Encoding(signStrTemp);
        return signTemp.equals(sign);
    }

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 驼峰转下划线,效率比上面高
     */
    public static String humpToLine(String str) {
        if (StringUtils.isNotBlank(str)) {
            Matcher matcher = humpPattern.matcher(str);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return null;
        }
    }

    public static String formatURL(Map<String, String> paraMap) {
        return formatURL(paraMap, false, false);
    }

    /**
     * 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序），并且生成url参数串<br>
     *
     * @param paraMap    要排序的Map对象
     * @param urlEncode  是否需要java.net.URLEncoder
     * @param keyToLower 是否需要将Key转换为全小写<br/>
     *                   true:key转化成小写，false:不转化
     * @return 排序后的url参数串
     */
    public static String formatURL(Map<String, String> paraMap, boolean urlEncode, boolean keyToLower) {
        String buff;
        try {
            List<Map.Entry<String, String>> infoIds = new ArrayList<>(paraMap.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
                @Override
                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                    return (o1.getKey()).compareTo(o2.getKey());
                }
            });
            // 构造URL 键值对的格式
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> item : infoIds) {
                if (StringUtils.isNotBlank(item.getKey())) {
                    String key = item.getKey();
                    String val = item.getValue();
                    if (urlEncode) {
                        val = URLEncoder.encode(val, "UTF-8");
                    }
                    if (keyToLower) {
                        buf.append(key.toLowerCase()).append("=").append(val);
                    } else {
                        buf.append(key).append("=").append(val);
                    }
                    buf.append("&");
                }

            }
            buff = buf.toString();
            if (!buff.isEmpty()) {
                buff = buff.substring(0, buff.length() - 1);
            }
        } catch (Exception e) {
            return null;
        }
        return buff;
    }


    /**
     * zhouzhongqing
     * 2018年1月30日09:52:24
     * Unicode转中文
     * */
    public static String decodeUnicode( String utfString) {
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;

        while((i=utfString.indexOf("\\u", pos)) != -1){
            sb.append(utfString.substring(pos, i));
            if(i+5 < utfString.length()){
                pos = i+6;
                sb.append((char)Integer.parseInt(utfString.substring(i+2, i+6), 16));
            }
        }
        return sb.toString();
    }


    /**
     * 通过HTTP GET 发送参数 拼接完整的GET请求的url
     *
     * @param httpUrl
     * @param parameter
     */
    public static String sendGetUrl(String httpUrl, Map<String, String> parameter) {
        if (parameter == null || httpUrl == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = parameter.entrySet().iterator();
        while (iterator.hasNext()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value;
            try {
                value = URLEncoder.encode(entry.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                value = "";
            }
            sb.append(key).append('=').append(value);
        }
        String urlStr = null;
        if (httpUrl.lastIndexOf('?') != -1) {
            urlStr = httpUrl + '&' + sb.toString();
        } else {
            urlStr = httpUrl + '?' + sb.toString();
        }
        log.info("request url [ {} ]",urlStr);
        return urlStr;
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
        BigDecimal two = new BigDecimal(Long.toString(v2));
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


    public static void main(String []args){

        System.out.println(decodeUnicode("\\u7b7e\\u540d\\u9519\\u8bef"));
    }

}
