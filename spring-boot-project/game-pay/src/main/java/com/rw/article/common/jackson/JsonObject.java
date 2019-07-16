package com.rw.article.common.jackson;

import org.apache.commons.beanutils.ConvertUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonObject extends HashMap<Object, Object> {

    public JsonObject() {

    }

    public JsonObject(String json) {
        this.putAll(JsonUtils.getInstance().toBean(json, Map.class));
    }

    public JsonObject(Map<?, ?> map) {
        this.putAll(map);
    }

    public Object get(String key) {
        return super.get(key);
    }

    public boolean getBoolean(String key) {
        return ConvertUtils.lookup(Boolean.TYPE).convert(Boolean.TYPE, get(key));
    }

    public double getDouble(String key) {
        return ConvertUtils.lookup(Double.TYPE).convert(Double.TYPE, get(key));
    }

    public int getInt(String key) {
        return ConvertUtils.lookup(Integer.TYPE).convert(Integer.TYPE, get(key));
    }

    public long getLong(String key) {
        return ConvertUtils.lookup(Long.TYPE).convert(Long.TYPE, get(key));
    }

    public String getString(String key) {
        Object value = get(key);
        if (value instanceof Map) {
            return getJSONObject(key).toString();
        } else if (value instanceof List) {
            return getJSONArray(key).toString();
        } else {
            return ConvertUtils.lookup(String.class).convert(String.class, get(key));
        }
    }

    public JsonArray getJSONArray(String key) {
        Object value = get(key);
        if (value instanceof List) {
            return new JsonArray((List<?>) value);
        }
        return null;
    }

    public JsonObject getJSONObject(String key) {
        Object value = get(key);
        if (value instanceof Map) {
            return new JsonObject((Map<?, ?>) value);
        }
        return null;
    }

    @Override
    public String toString() {
        return JsonUtils.getInstance().toJson(this);
    }
}
