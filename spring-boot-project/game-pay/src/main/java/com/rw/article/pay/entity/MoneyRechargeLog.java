package com.rw.article.pay.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "money_recharge_log")
public class MoneyRechargeLog extends BaseEntity implements Serializable{

    /**记录创建时间**/
    private Date createTime;

    /**更新时间**/
    private Date updateTime;


    /**玩家id**/
    @Indexed
    private Long userId;

    /**现金（RMB）**/
    private Long balance;

    /**获得金币数**/
    private Long getMoney;

    /**RMB 与 金币比例**/
    private Integer rate;

    /**（代理、渠道商）userId**/
    private Integer channelId;

    /**充值状态：1成功、2进行中**/
    private Integer status;

    /**openId**/
    private Integer openId;

    /**充值类型： 1官方充值、2 支付宝  3  微信   4  QQ钱包   5 银联  6 保险箱  7 金币存取 8 后台扣背包钱  9 后台操作保险箱  10 返佣金币变化**/
    private Integer type;

    /**用户类型：1玩家、2代理商**/
    private Integer userType;

    /**备注**/
    private String remark;

    /**充值前金币**/
    private Long rechbefore;

    /**充值之后金币**/
    private Long rechlater;
    /****虚拟删除 1-正常 0删除****/
    private Integer flag = 1;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getGetMoney() {
        return getMoney;
    }

    public void setGetMoney(Long getMoney) {
        this.getMoney = getMoney;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOpenId() {
        return openId;
    }

    public void setOpenId(Integer openId) {
        this.openId = openId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getRechbefore() {
        return rechbefore;
    }

    public void setRechbefore(Long rechbefore) {
        this.rechbefore = rechbefore;
    }

    public Long getRechlater() {
        return rechlater;
    }

    public void setRechlater(Long rechlater) {
        this.rechlater = rechlater;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }
}