package com.rw.article.pay.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Document(collection="pay_platform_config")
public class PayPlatform implements Serializable {
    private Integer id;

    private String name;

    private String merchId;

    private String merchKey;

    private String merchPublicKey;

    private String notifyUrl;

    private String handler;

    private String remark;

    private Integer status;

    private Date updateTime;

    private Date createTime;

    private Integer payModeType;

    private Integer payType;

    private Long payMax;

    private  Long payMin;

    private String clientUrl;

    private String payGetUrl;

    private Integer sort;


    /**配置文件为=1**/
    private  Integer dbConfig;

    /**充值金额数组**/
    private List<Long> rechargeMoneyArray;

    /** 充值金额是否可以累加 0不能累加 1 能累加 **/
    private Integer isAccumulation;


    public List<Long> getRechargeMoneyArray() {
        return rechargeMoneyArray;
    }

    public void setRechargeMoneyArray(List<Long> rechargeMoneyArray) {
        this.rechargeMoneyArray = rechargeMoneyArray;
    }

    public Integer getIsAccumulation() {
        return isAccumulation;
    }

    public void setIsAccumulation(Integer isAccumulation) {
        this.isAccumulation = isAccumulation;
    }

    public Integer getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(Integer dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getPayModeType() {
        return payModeType;
    }

    public void setPayModeType(Integer payModeType) {
        this.payModeType = payModeType;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Long getPayMax() {
        return payMax;
    }

    public void setPayMax(Long payMax) {
        this.payMax = payMax;
    }

    public Long getPayMin() {
        return payMin;
    }

    public void setPayMin(Long payMin) {
        this.payMin = payMin;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }

    public String getPayGetUrl() {
        return payGetUrl;
    }

    public void setPayGetUrl(String payGetUrl) {
        this.payGetUrl = payGetUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getMerchId() {
        return merchId;
    }

    public void setMerchId(String merchId) {
        this.merchId = merchId == null ? null : merchId.trim();
    }

    public String getMerchKey() {
        return merchKey;
    }

    public void setMerchKey(String merchKey) {
        this.merchKey = merchKey == null ? null : merchKey.trim();
    }

    public String getMerchPublicKey() {
        return merchPublicKey;
    }

    public void setMerchPublicKey(String merchPublicKey) {
        this.merchPublicKey = merchPublicKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl == null ? null : notifyUrl.trim();
    }


    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}