package com.rw.article.common.utils.pay;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

public class HttpUtils {

    private static HttpUtils instance;
    private static Map<String, HttpUtils> instances;
    private static List<Header> headers;
    private final static int connectTimeout = 10000;
    private final static int connectRequestTimeout = 10000;
    private final static int MAX_CONNECTIONS_PER_HOST = 50; // 连接池最大并发连接数
    private final static int MAX_TOTAL_CONNECTIONS = 256; // 单路由最大并发数

    private RequestConfig requestConfig;
    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpClient;

    private HttpUtils() {
        cookieStore = new BasicCookieStore();
        SSLConnectionSocketFactory sslSocketFactory = this.getSslSocketFactory();
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectRequestTimeout)
                .build();

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(MAX_CONNECTIONS_PER_HOST);
        manager.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);

        httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(sslSocketFactory)
                .setConnectionManager(manager)
                .build();
        this.defaultHeader();
    }

    public static HttpUtils getInstance() {
        synchronized (HttpUtils.class) {
            if (headers == null) {
                headers = new ArrayList<Header>();
            }
            if (instance == null) {
                instance = new HttpUtils();
            }
        }
        return instance;
    }

    public static HttpUtils getInstance(String client) {
        synchronized (HttpUtils.class) {
            if (headers == null) {
                headers = new ArrayList<Header>();
            }
            if (instances == null) {
                instances = new HashMap<String, HttpUtils>();
            }
            if (!instances.containsKey(client)) {
                instances.put(client, new HttpUtils());
            }
        }
        return instances.get(client);
    }

    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    /**
     * 添加请求Header
     *
     * @param name  名称
     * @param value 值
     */
    public void addHeader(String name, String value) {
        headers.add(new BasicHeader(name, value));
    }

    /**
     * 设置默认的请求Header
     */
    private void defaultHeader() {
        this.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        this.addHeader("Accept-Encoding", "gzip,deflate,sdch");
        this.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
        this.addHeader("Connection", "keep-alive");
        this.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
    }

    /**
     * HTTPS访问（允许信任自签证书，即信任所有证书）
     *
     * @return SSL
     * @author 吴尚云
     * @date 2014-5-9 下午5:46:58
     */
    private SSLConnectionSocketFactory getSslSocketFactory() {
        SSLConnectionSocketFactory sslSocketFactory = null;
        try {
            // 信任所有SSL
            TrustStrategy trustStore = new TrustSelfSignedStrategy();
            SSLContext sslcontext = new SSLContextBuilder().loadTrustMaterial(null, trustStore).build();
            sslSocketFactory = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslSocketFactory;
    }

    /**
     * HTTP GET请求
     *
     * @param url    URL
     * @param params 参数
     * @return HTML内容
     * @throws Exception
     * @auther wsyte_000
     * @date 2014-4-12 上午2:14:35
     */
    public String httpGet(String url, Map<String, String> params) throws Exception {
        return httpGet(url, headers, params, null, null);
    }

    public String httpGet(String url, Map<String, String> params, HttpHost proxy) throws Exception {
        return httpGet(url, headers, params, null, proxy);
    }

    public String httpGet(String url, Map<String, String> params, String defaultCharset) throws Exception {
        return httpGet(url, headers, params, defaultCharset, null);
    }

    public String httpGet(String url, List<Header> headers, Map<String, String> params) throws Exception {
        return httpGet(url, headers, params, null, null);
    }

    /**
     * HTTP POST请求
     *
     * @param url    URL
     * @param params 参数
     * @return HTML内容
     * @throws Exception
     * @auther wsyte_000
     * @date 2014-4-12 上午2:15:47
     */
    public String httpPost(String url, Map<String, String> params) throws Exception {
        return httpPost(url, headers, params);
    }

    /**
     * HTTP GET请求
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求参数
     * @return 请求结果
     * @throws Exception
     * @auther wsyte_000
     * @date 2014-5-9 下午11:46:32
     */
    public String httpGet(String url, List<Header> headers, Map<String, String> params, String defaultCharset, HttpHost proxy) throws Exception {
        String html = null;
        CloseableHttpResponse response = get(url, headers, params, proxy);
        if (response != null) {
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    html = StringUtils.isNotBlank(defaultCharset) ? EntityUtils.toString(entity, defaultCharset) : EntityUtils.toString(entity);
                }
            } finally {
                response.close();
            }
        }
        return html;
    }

    /**
     * HTTP POST请求
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求参数
     * @return 请求结果
     * @throws Exception
     * @auther wsyte_000
     * @date 2014-5-9 下午11:46:51
     */
    public String httpPost(String url, List<Header> headers, Map<String, String> params) throws Exception {
        String html = null;
        CloseableHttpResponse response = post(url, headers, params);
        if (response != null) {
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    html = EntityUtils.toString(entity);
                }
            } finally {
                response.close();
            }
        }
        return html;
    }

    /**
     * GET 请求
     *
     * @param url     请求地址
     * @param headers 请求header
     * @param params  请求参数
     * @return 请求结果
     * @throws Exception
     * @author 吴尚云
     * @date 2014-6-9 下午1:55:52
     */
    private CloseableHttpResponse get(String url, List<Header> headers, Map<String, String> params, HttpHost proxy) throws Exception {
        // 写入请求参数
        String paramStr = null;
        if (params != null && params.size() > 0) {
            paramStr = "";
            for (String key : params.keySet()) {
                paramStr += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(params.get(key), "UTF-8") + "&";
            }
            paramStr = paramStr.substring(0, paramStr.length() - 1);
        }

        HttpGet request = new HttpGet(url + (paramStr == null ? "" : "?" + paramStr));
        if (proxy != null) {
            request.setConfig(RequestConfig.copy(requestConfig).setProxy(proxy).build());
        }
        // 设置请求Header
        for (Header header : headers) {
            request.addHeader(header);
        }
        CloseableHttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            request.abort();
            return null;
        }
        return response;
    }

    /**
     * POST请求
     *
     * @param url     请求地址
     * @param headers 请求Header
     * @param params  请求参数
     * @return 请求结果
     * @throws Exception
     * @author 吴尚云
     * @date 2014-6-9 下午1:57:28
     */
    private CloseableHttpResponse post(String url, List<Header> headers, Map<String, String> params) throws Exception {
        HttpPost request = new HttpPost(url);
        // 写入请求参数
        List<NameValuePair> nameValuePairs;
        if (params != null && params.size() > 0) {
            nameValuePairs = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
            }
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));
        }
        // 设置请求Header
        for (Header header : headers) {
            request.addHeader(header);
        }

        CloseableHttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            request.abort();
            return null;
        }
        return response;
    }

    /**
     * 下载文件
     *
     * @param url  URL
     * @param file 保存文件
     * @return 文件
     * @throws Exception
     */
    public File downloadFile(String url, File file) throws Exception {
        // 下载文件
        CloseableHttpResponse response = get(url, headers, null, null);
        if (response != null && response.getStatusLine().getStatusCode() == 200) {
            try {
                // 通过输入流获取图片数据
                InputStream inStream = response.getEntity().getContent();
                // 得到二进制数据
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                inStream.close();
                if (file == null) {
                    String fileName = null;
                    Header[] headers = response.getHeaders("Content-Disposition");
                    if (headers != null) {
                        for (Header header : headers) {
                            String value = header.getValue().replace("\"", "");
                            if (value.contains("filename")) {
                                value = value.substring(value.indexOf("filename"));
                                fileName = value.substring(value.indexOf("=") + 1);
                                break;
                            }
                        }
                    }
                    if (StringUtils.isBlank(fileName)) {
                        fileName = UUID.randomUUID().toString();
                    }
                    file = new File(fileName);
                }
                // 写入到磁盘
                FileOutputStream fops = new FileOutputStream(file);
                fops.write(outStream.toByteArray());
                fops.flush();
                fops.close();
            } finally {
                response.close();
            }
        }
        return file;
    }

    /**
     * 文件上传
     *
     * @param url    请求地址
     * @param name   field
     * @param file   文件
     * @param params 参数
     * @return 返回值
     * @throws IOException
     */
    public String uploadFile(String url, String name, File file, Map<String, String> params) throws IOException {
        String result = null;
        HttpPost request = new HttpPost(url);
        FileBody fileBody = new FileBody(file);
        // 写入请求参数
        List<NameValuePair> nameValuePairs;
        if (params != null && params.size() > 0) {
            nameValuePairs = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
            }
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));
        }
        request.setEntity(MultipartEntityBuilder.create().addPart(name, fileBody).build());
        CloseableHttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            result = EntityUtils.toString(entity);
        }
        response.close();
        return result;
    }

    /**
     * 读取网络图片
     *
     * @param url 地址
     * @return Image
     * @author 吴尚云
     * @date 2014年8月5日 下午4:17:06
     */
    public BufferedImage readImage(String url) {
        BufferedImage image = null;
        CloseableHttpResponse response;
        try {
            response = get(url, headers, null, null);
            if (response != null) {
                try {
                    InputStream inputStream = response.getEntity().getContent();
                    image = ImageIO.read(inputStream);
                    inputStream.close();
                } finally {
                    response.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public void close() {
        try {
            httpClient.close();
            instance = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试代理IP
     *
     * @param proxyHost 代理IP
     * @param proxyPort 代理端口
     * @param scheme    请求类型
     */
    public String testProxy(String url, String proxyHost, int proxyPort, String scheme) {
        String result = null;
        HttpHost proxy;
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            proxy = new HttpHost(proxyHost, proxyPort, scheme);
        } else {
            proxy = new HttpHost(proxyHost, proxyPort);
        }
        HttpGet request = new HttpGet(url);
        request.setConfig(RequestConfig.copy(requestConfig).setProxy(proxy).build());
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                request.abort();
            } else {
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            // 测试失败
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    // 关闭失败
                }
            }
        }
        return result;
    }
}
