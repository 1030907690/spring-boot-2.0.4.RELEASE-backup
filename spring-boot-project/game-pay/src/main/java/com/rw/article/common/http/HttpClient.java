package com.rw.article.common.http;


import com.alibaba.fastjson.JSONObject;
import com.rw.article.common.jackson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//用于进行Https请求的HttpClient

/**
 *
 * */
public class HttpClient {


    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);


    /**
     * 字符编码 UTF-8
     * */
    public final static String ENCODING = "UTF-8";


    /***
     * Post发送json格式数据
     * @param url
     * @param json
     * @param encoding
     * */
    public static String sendHttpRequestPost(String url, String json, String encoding)   {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        try {
            StringEntity s = new StringEntity(json.toString());
            s.setContentEncoding(encoding);
            s.setContentType("application/json");//发送json数据需要设置contentType
            post.setEntity(s);
            HttpResponse res = httpclient.execute(post);
            String result = null;
            if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                result = EntityUtils.toString(res.getEntity());// 返回json格式：
            }
            return result;
        } catch (Exception e) {
            log.info("sendHttpRequestPost [ {} ]",e);
        }
        return null;
    }

    /**
     * 模拟Post请求
     * @param url       资源地址
     * @param map   参数列表
     * @param encoding  编码
     * @return
     * @throws Exception
     */
    public static String sendHttpRequestPost(String url, Map<String,String> map, String encoding)   {
        String body = "";
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);

            //装填参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            //设置参数到请求对象中
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));

            log.info("请求地址：" + url);
            log.info("请求参数：" + nvps.toString());

            //设置header信息
            //指定报文头【Content-type】、【User-Agent】
            //  httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            // httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            //执行请求操作，并拿到结果（同步阻塞）
            response = client.execute(httpPost);
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, encoding);
            }
            EntityUtils.consume(entity);

        }catch (Exception e){
            log.error("sendHttpRequestPost --"+e);
        }finally {
            if(null != response){
                try {
                    //释放链接
                    response.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }


        return body;
    }



    public static void main(String [] args)  {
        System.out.println("start");
    //    String body = sendHttpRequestPost("http://blog.csdn.net/xiaoxian8023/article/details/49863967",new HashMap<String,String>(),"UTF-8");
        System.out.println("\n\n\n");
    //    System.out.println(body);

       String body = "{ \"access_token\":\"ACCESS_TOKEN\",\"expires_in\":7200,\"refresh_token\":\"REFRESH_TOKEN\",\"openid\":\"OPENID\",\"scope\":\"SCOPE\" }";
        JsonObject json = new JsonObject(body);
        System.out.println(json.get("access_token"));
    }


}