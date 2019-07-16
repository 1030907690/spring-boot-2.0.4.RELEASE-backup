package com.rw.article.common.utils.pay;

import com.rw.article.common.jackson.JsonObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class XmlUtils {

    private static final Logger log = LoggerFactory.getLogger(XmlUtils.class);

    public static String toJson(String xml) {
        return toJson(xml, false);
    }

    public static String toJson(String xml, boolean needRootKey) {
        return new JsonObject(toMap(xml, needRootKey)).toString();
    }

    public static Map<String, Object> toMap(String xml) {
        return toMap(xml, false);
    }

    public static Map<String, Object> toMap(String xml, boolean needRootKey) {
        Map<String, Object> map = null;
        try {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            map = xml2map(root); // xml2map(root);
            if (root.elements().size() == 0 && root.attributes().size() == 0) {
                return map;
            }
            if (needRootKey) { //在返回的map里加根节点键（如果需要）
                Map<String, Object> rootMap = new HashMap<>();
                rootMap.put(root.getName(), map);
                return rootMap;
            }
        } catch (Exception e) {
            log.error("解析XML异常：", e);
        }
        return map;
    }

    /**
     * xml转map 不带属性
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> xml2map(Element root) {
        Iterator<Element> rootIterator = root.elementIterator();
        Map<String, Object> rMap = new LinkedHashMap<>();
        while (rootIterator.hasNext()) {
            Element temp = rootIterator.next();
            String name = temp.getName();
            Object value = temp.isTextOnly() ? temp.getTextTrim() : xml2map(temp);
            if (rMap.containsKey(name)) {
                Object item = rMap.get(name);
                if (item instanceof List) {
                    List<Object> list = (List<Object>) item;
                    list.add(value);
                } else {
                    rMap.put(name, new ArrayList<>(Arrays.asList(item, value)));
                }
            } else {
                rMap.put(name, value);
            }
        }
        return rMap;
    }

    /**
     * map转xml map中没有根节点的键
     */
    public static Document map2xml(Map<String, Object> map, String rootName) throws DocumentException, IOException {
        Document doc = DocumentHelper.createDocument();
        Element root = DocumentHelper.createElement(rootName);
        doc.add(root);
        map2xml(map, root);
        return doc;
    }

    /**
     * map转xml map中含有根节点的键
     */
    public static Document map2xml(Map<String, Object> map) throws DocumentException, IOException {
        Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
        if (entries.hasNext()) { //获取第一个键创建根节点
            Map.Entry<String, Object> entry = entries.next();
            Document doc = DocumentHelper.createDocument();
            Element root = DocumentHelper.createElement(entry.getKey());
            doc.add(root);
            map2xml((Map<String, Object>) entry.getValue(), root);
            return doc;
        }
        return null;
    }

    /**
     * map转xml
     *
     * @param map  map 对象
     * @param body xml元素
     */
    private static Element map2xml(Map<String, Object> map, Element body) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith("@")) { // 属性
                body.addAttribute(key.substring(1, key.length()), value.toString());
            } else if (key.equals("#text")) { //有属性时的文本
                body.setText(value.toString());
            } else {
                if (value instanceof List) {
                    List list = (List) value;
                    Object obj;
                    for (Object aList : list) {
                        obj = aList;
                        //list里是map或String，不会存在list里直接是list的，
                        if (obj instanceof Map) {
                            Element subElement = body.addElement(key);
                            map2xml((Map) aList, subElement);
                        } else {
                            body.addElement(key).setText((String) aList);
                        }
                    }
                } else if (value instanceof Map) {
                    Element subElement = body.addElement(key);
                    map2xml((Map) value, subElement);
                } else {
                    body.addElement(key).setText(value.toString());
                }
            }
        }
        return body;
    }


    public static void main(String[] args) throws Exception {
        String xml = "<PayResult xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.datacontract.org/2004/07/PaymentPlatform.API.Models\"><MerchantOrderNo>2017102310583992557605</MerchantOrderNo><PlatformOrderNo i:nil=\"true\" /><ReqDate>20171023105830</ReqDate><RespCode>12</RespCode><RespDate>20171023105831</RespDate><RespMessage>商户不存在或已禁用</RespMessage><RespType>-1</RespType><Sign i:nil=\"true\" /><BuyerAccount i:nil=\"true\" /><BuyerId i:nil=\"true\" /><ExtMsg>111111</ExtMsg><QRCodeUrl i:nil=\"true\" /><Status i:nil=\"true\" /><ToPayData i:nil=\"true\" /><test><a>1</a><b>2</b></test><test><a>1</a><b>3</b></test><test><a>1</a><b>4</b></test></PayResult>";
        Map<String, Object> map = toMap(xml, false);
        System.out.println(map);
        System.out.println(toJson(xml));

        Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();
        System.out.println(xml2map(root));
    }
}
