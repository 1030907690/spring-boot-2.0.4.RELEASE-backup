package com.rw.article.pay.service;

import com.rw.article.pay.entity.GamePlayer;
import com.rw.article.pay.entity.PayPlatform;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 用户
 * @date 2018/8/1 18:13
 */
public interface IGamePlayerRepository extends MongoRepository<GamePlayer, Long> {

}
