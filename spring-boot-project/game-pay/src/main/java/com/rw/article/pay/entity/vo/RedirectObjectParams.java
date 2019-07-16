package com.rw.article.pay.entity.vo;

import java.io.Serializable;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 参数 input
 * @date 2018/11/6 18:09
 */
public class RedirectObjectParams  implements Serializable {

    private String key;

    private String value;
    public RedirectObjectParams( ) {

    }
    public RedirectObjectParams(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
