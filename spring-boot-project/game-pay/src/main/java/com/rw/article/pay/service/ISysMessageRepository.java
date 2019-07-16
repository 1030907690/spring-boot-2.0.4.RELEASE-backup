package com.rw.article.pay.service;

import com.rw.article.pay.entity.RechargeOrder;
import com.rw.article.pay.entity.SysMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 系统消息
 * @date 2018/8/211:34
 */
public interface ISysMessageRepository extends MongoRepository<SysMessage, Long> {
}
