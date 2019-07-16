package com.rw.article.common.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;


/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: spring util
 * @date 2018/8/1 17:08
 */
@Component
public class BeansUtils  implements ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public  <T> T getBean(Class<T> bean) {
        return context.getBean(bean);
    }
    public  <T> T getBean(String var1, @Nullable Class<T> var2){
        return context.getBean(var1, var2);
    }

    public  ApplicationContext getContext() {
        return context;
    }

}
