package com.rw.article.pay.entity.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description:  参数 form
 * @date 2018/11/6 17:53
 */
public class RedirectObject implements Serializable {


    private List<RedirectObjectParams> redirectObjectParamsList;
    private String method;
    private String action;

    public RedirectObject( ) {

    }
    public RedirectObject(List<RedirectObjectParams> redirectObjectParamsList, String method, String action) {
        this.redirectObjectParamsList = redirectObjectParamsList;
        this.method = method;
        this.action = action;
    }

    public List<RedirectObjectParams> getRedirectObjectParamsList() {
        return redirectObjectParamsList;
    }

    public void setRedirectObjectParamsList(List<RedirectObjectParams> redirectObjectParamsList) {
        this.redirectObjectParamsList = redirectObjectParamsList;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
