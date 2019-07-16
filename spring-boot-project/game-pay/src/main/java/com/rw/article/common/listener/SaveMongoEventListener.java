package com.rw.article.common.listener;

import com.rw.article.common.annotation.GeneratedValue;
import com.rw.article.common.sequence.SequenceId;
import com.rw.article.pay.action.RechargeOrderAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: id自增的监听
 * @date 2018/8/118:40
 */
@Component
public class SaveMongoEventListener extends AbstractMongoEventListener<Object> {

    private  final Logger log = LoggerFactory.getLogger(this.getClass());

    public SaveMongoEventListener(){
        log.info("init SaveMongoEventListener ... ");
    }

    @Resource
    private MongoTemplate mongoTemplate;

    /** 所以id往上加**/
    private Long id = 1000000L;

    @Override
    public void onBeforeConvert( BeforeConvertEvent<Object>  env) {
         Object source = env.getSource();
        if (source != null) {
            ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field);
                    //log.info(field +  " field.isAnnotationPresent(GeneratedValue.class) " + field.isAnnotationPresent(GeneratedValue.class));
                    if (field.isAnnotationPresent(GeneratedValue.class)) {
                        //设置自增ID
                        field.set(source, getNextId(source.getClass().getSimpleName()) + id);
                       // log.info("集合的ID为======================="+ source);
                    }
                }
            });
        }
    }





    /**
     * 获取下一个自增ID
     *
     * @param collName 集合名
     * @return
     * @author yinjihuan
     */
    private Long getNextId(String collName) {
        Query query = new Query(Criteria.where("collName").is(collName));
        Update update = new Update();
        update.inc("seqId", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        SequenceId seqId = mongoTemplate.findAndModify(query, update, options, SequenceId.class);
        return seqId.getSeqId();
    }

}
