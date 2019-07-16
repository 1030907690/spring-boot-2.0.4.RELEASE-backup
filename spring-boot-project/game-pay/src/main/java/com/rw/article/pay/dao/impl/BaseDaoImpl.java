package com.rw.article.pay.dao.impl;

import com.rw.article.pay.action.RechargeOrderAction;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.BaseEntity;
import com.rw.article.pay.service.IPayProxySv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: dao实现
 * @date 2018/8/119:47
 */
@Service
public class BaseDaoImpl implements BaseDao{


    @Resource
    private MongoTemplate mongoTemplate;

    private IPayProxySv payProxySv;
    private static final Logger log = LoggerFactory.getLogger(BaseDaoImpl.class);

    @Override
    public int updateById(BaseEntity entity) {
        int line = 0;
        try {
            Query query = new Query(Criteria.where("_id").is(entity.getId()));
            Update update = new Update();
            List<Field> fieldList = new ArrayList<Field>();
            Class tempClass = entity.getClass();
            while (tempClass != null) {
                //当父类为null的时候说明到达了最上层的父类(Object类).
                fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
                tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己}
            }

            if (fieldList != null && fieldList.size() > 0) {
                for (Field field : fieldList) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    if (field.getName().equals("id") || field.getName().equals("createTime")) {
                        continue;
                    }

                    PropertyDescriptor pd = null;
                    pd = new PropertyDescriptor(field.getName(), entity.getClass());
                    Method getMethod = pd.getReadMethod();// 获得get方法
                    Object o = getMethod.invoke(entity);
                    if (o != null) {
                        if (o instanceof Integer) {
                            update.inc(field.getName(), (Integer) o);
                        } else if (o instanceof Long) {
                            update.inc(field.getName(), (Long) o);
                        } else if (o instanceof Byte) {
                            update.inc(field.getName(), (Byte) o);
                        } else if (o instanceof Float) {
                            update.inc(field.getName(), (Float) o);
                        } else if (o instanceof Double) {
                            update.inc(field.getName(), (Double) o);
                        } else if (o instanceof Short) {
                            update.inc(field.getName(), (Double) o);
                        } else {
                            update.set(field.getName(), o);
                        }

                    }
                }
                mongoTemplate.updateFirst(query, update, entity.getClass());
                line = 1;
            }

        } catch (IllegalArgumentException e) {
            log.error("更新数据库对象出错", e);
        } catch (IllegalAccessException e) {
            log.error("更新数据库对象出错", e);
        } catch (IntrospectionException e) {
            log.error(entity.getClass().getName() + ":更新数据库对象出错", e);
        } catch (InvocationTargetException e) {
            log.error("更新数据库对象出错", e);
        }
        return line;
    }


    @Override
    public int updateByEntity(BaseEntity entity) {
        int line = 0;
        try {
            Query query = new Query(Criteria.where("_id").is(entity.getId()));
            Update update = new Update();
            List<Field> fieldList = new ArrayList<Field>();
            Class tempClass = entity.getClass();
            while (tempClass != null) {
                //当父类为null的时候说明到达了最上层的父类(Object类).
                fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
                tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己}
            }

            if (fieldList != null && fieldList.size() > 0) {
                for (Field field : fieldList) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    if (field.getName().equals("id") || field.getName().equals("createTime")) {
                        continue;
                    }

                    PropertyDescriptor pd = null;
                    pd = new PropertyDescriptor(field.getName(), entity.getClass());
                    Method getMethod = pd.getReadMethod();// 获得get方法
                    Object o = getMethod.invoke(entity);
                    if (o != null) {
                        if (o instanceof Integer) {
                            update.set(field.getName(), (Integer) o);
                        } else if (o instanceof Long) {
                            update.set(field.getName(), (Long) o);
                        } else if (o instanceof Byte) {
                            update.set(field.getName(), (Byte) o);
                        } else if (o instanceof Float) {
                            update.set(field.getName(), (Float) o);
                        } else if (o instanceof Double) {
                            update.set(field.getName(), (Double) o);
                        } else if (o instanceof Short) {
                            update.set(field.getName(), (Double) o);
                        } else {
                            update.set(field.getName(), o);
                        }

                    }
                }
                mongoTemplate.updateFirst(query, update, entity.getClass());
                line = 1;
            }

        } catch (IllegalArgumentException e) {
            log.error("更新数据库对象出错", e);
        } catch (IllegalAccessException e) {
            log.error("更新数据库对象出错", e);
        } catch (IntrospectionException e) {
            log.error(entity.getClass().getName() + ":更新数据库对象出错", e);
        } catch (InvocationTargetException e) {
            log.error("更新数据库对象出错", e);
        }
        return line;
    }
}
