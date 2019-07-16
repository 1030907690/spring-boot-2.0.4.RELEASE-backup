package com.rw.article.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 对于要返回view的按下面的处理
 * @date 2018/8/214:27
 */
@Component
public class ExceptionHandler implements HandlerExceptionResolver {

  private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public ModelAndView resolveException(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        // 异常处理逻辑 goes here
        log.info("got exception: {}", ex.getClass());
        return new ModelAndView("redirect/error");
    }
}
