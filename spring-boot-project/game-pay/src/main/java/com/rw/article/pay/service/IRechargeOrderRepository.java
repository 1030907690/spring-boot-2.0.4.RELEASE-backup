package com.rw.article.pay.service;

import com.rw.article.pay.entity.RechargeOrder;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 订单service
 * @date 2018/8/11 7:44
 */
public interface IRechargeOrderRepository extends MongoRepository<RechargeOrder, Long> {


    /***
     *根据orderId查询
     * */
    List<RechargeOrder> findByOrderId(String orderId);
}
