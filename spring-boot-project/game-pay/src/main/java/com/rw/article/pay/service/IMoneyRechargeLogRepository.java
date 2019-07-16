package com.rw.article.pay.service;

import com.rw.article.pay.entity.MoneyRechargeLog;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 金币变化日志
 * @date 2018/8/211:53
 */
public interface IMoneyRechargeLogRepository extends MongoRepository<MoneyRechargeLog,Long> {
}
