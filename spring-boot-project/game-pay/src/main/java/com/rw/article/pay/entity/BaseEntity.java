package com.rw.article.pay.entity;

import com.rw.article.common.annotation.GeneratedValue;
import org.springframework.data.annotation.Id;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description:  基础对象
 * @date 2018/8/119:45
 */
public class BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    /**配置文件为=1**/
    private  Integer dbConfig;

    public Integer getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(Integer dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
