package com.rw.article.pay.service;

import com.rw.article.pay.entity.AttentionLog;
import com.rw.article.pay.entity.MoneyRechargeLog;
import com.rw.article.pay.entity.SysConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description:  关注表的jpa
 * @date 2018/9/12 18:19
 */
public interface IAttentionLogRepository extends MongoRepository<AttentionLog,Long> {

    /***
     * zhouzhongqing
     * 2018年9月12日18:21:14
     *根据userId和type查询关注
     *@param userId
     * @param type
     * @return AttentionLog
     * */
    AttentionLog findFirstByUserIdAndTypeOrderByCreateTimeAsc(Long userId, Integer type);
}
