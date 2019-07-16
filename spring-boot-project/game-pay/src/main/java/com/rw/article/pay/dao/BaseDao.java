package com.rw.article.pay.dao;

import com.rw.article.pay.entity.BaseEntity;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: dao
 * @date 2018/8/119:44
 */
public interface BaseDao {

    /***
     * 遇上数值会相加
     * */
    public int updateById(BaseEntity entity);


    /***
     * 修改属性值
     * */
    public int updateByEntity(BaseEntity entity);
}
