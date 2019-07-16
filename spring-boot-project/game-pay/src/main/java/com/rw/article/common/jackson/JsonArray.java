package com.rw.article.common.jackson;

import org.apache.commons.beanutils.ConvertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonArray extends ArrayList<Object> {

    public JsonArray() {

    }

    public JsonArray(String json) {
        this(JsonUtils.getInstance().toBean(json, List.class));
    }

    public JsonArray(List<?> value) {
        this.addAll(value);
    }

    @Override
    public Object get(int index) {
        return super.get(index);
    }

    public boolean getBoolean(int index) {
        return ConvertUtils.lookup(Boolean.TYPE).convert(Boolean.TYPE, get(index));
    }

    public double getDouble(int index) {
        return ConvertUtils.lookup(Double.TYPE).convert(Double.TYPE, get(index));
    }

    public int getInt(int index) {
        return ConvertUtils.lookup(Integer.TYPE).convert(Integer.TYPE, get(index));
    }

    public long getLong(int index) {
        return ConvertUtils.lookup(Long.TYPE).convert(Long.TYPE, get(index));
    }

    public String getString(int index) {
        Object value = get(index);
        if (value instanceof List) {
            return getJSONArray(index).toString();
        } else if (value instanceof Map) {
            return getJSONObject(index).toString();
        } else {
            return ConvertUtils.lookup(String.class).convert(String.class, value);
        }
    }

    public JsonArray getJSONArray(int index) {
        Object value = get(index);
        if (value instanceof List) {
            return new JsonArray((List<?>) value);
        }
        return null;
    }

    public JsonObject getJSONObject(int index) {
        Object value = get(index);
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
