package com.rw.article.pay.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 关注表
 * @date 2018/8/17 9:29
 */
@Document(collection = "attention_log")
public class AttentionLog extends BaseEntity{

    /***
     * 玩家id
     * */
    @Indexed
    private Long userId;


    /***
     * 1 登录    2 提现关注  3 充值订单完成后关注
     * */
    private Integer type;


    /***
     * 根据上面type判断存储对应的记录id
     * */
    private List<String> idList;


    /**记录创建时间**/
    private Date createTime;

    /**更新时间**/
    private Date updateTime;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<String> getIdList() {
        return idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
    }
}
