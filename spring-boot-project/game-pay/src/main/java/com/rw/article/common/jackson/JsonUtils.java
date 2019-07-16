package com.rw.article.common.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final JsonUtils instance = new JsonUtils();
    private ObjectMapper mapper = new ObjectMapper();

    private JsonUtils() {
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static JsonUtils getInstance() {
        return instance;
    }

    public <T> String toJson(T bean) {
        try {
            return mapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            log.error("Json转换异常：", e);
        }
        return null;
    }


    public <T> T toBean(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Json转换异常：{}", json, e);
        }
        return null;
    }

    public <T> T toBean(String json, JavaType valueType) {
        try {
            return mapper.readValue(json, valueType);
        } catch (IOException e) {
            log.error("Json转换异常：", e);
        }
        return null;
    }

    public <T> T toBean(String json, TypeReference valueTypeRef) {
        try {
            return mapper.readValue(json, valueTypeRef);
        } catch (IOException e) {
            log.error("Json转换异常：", e);
        }
        return null;
    }

    public JavaType getJavaType(Class<?> parametrized, Class<?>... parameterClasses) {
        return mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    public ObjectMapper getMapper() {
        return mapper;
    }


    private static String regex_date = "^[1-9]\\d{3}\\-(0?[1-9]|1[0-2])\\-(0?[1-9]|[12]\\d|3[01])$";
    private static String regex_date_time = "^[1-9]\\d{3}\\-(0?[1-9]|1[0-2])\\-(0?[1-9]|[12]\\d|3[01])\\s*(0?[1-9]|1\\d|2[0-3])(\\:(0?[1-9]|[1-5]\\d)){2}$";


    public static void main(String[] args) throws Exception {
//        JsonObject object = JsonUtils.getInstance().toBean("{\"code\":0,\"message\":\"12\", \"data\":{\"code\":0,\"message\":\"13\"},\"list\":[1,2,3,\"4\",\"5\"]}", JsonObject.class);
//        System.out.println(object.getJSONObject("data"));
//        System.out.println(object);
//
//        JsonArray array = new JsonArray("[1,2,3,4,\"5\",{\"code\":0,\"message\":\"创建订单失败\"},[1,2,3,4]]");
//        System.out.println(array.get(1));
//        System.out.println(array.getString(5));
//        System.out.println(array.getJSONArray(6));

        ConvertUtils.register(new Converter() {
            @Override
            public <T> T convert(Class<T> type, Object value) {
                try {
                    if (value == null || type == value.getClass()) {
                        return (T) value;
                    }else if (value.toString().length() == 10) {
                        value = value + " 00:00:00";
                    }
                    return (T) DateUtils.parseDate(value.toString(), "yyyy-MM-dd HH:mm:ss");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }, Date.class);

        System.out.println(ConvertUtils.convert("2017-01-01 00:01:00", Date.class));
    }
}
