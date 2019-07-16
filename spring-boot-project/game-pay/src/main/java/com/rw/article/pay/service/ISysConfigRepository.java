package com.rw.article.pay.service;

import com.rw.article.pay.entity.SysConfig;
import com.rw.article.pay.entity.SysMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 系统配置
 * @date 2018/8/6 10:29
 */
public interface ISysConfigRepository extends MongoRepository<SysConfig, Long> {



    SysConfig findFirstByItemCodeAndStatusOrderByCreateTimeAsc(String itemCode,Integer status);

}
