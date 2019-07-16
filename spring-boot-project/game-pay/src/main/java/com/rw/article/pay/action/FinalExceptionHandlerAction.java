package com.rw.article.pay.action;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 处理异常
 * @date 2018/8/214:30
 */
@RestController
public class FinalExceptionHandlerAction implements ErrorController {
    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping(value = "/error")
    public Object error(HttpServletResponse resp, HttpServletRequest req) {
        // 错误处理逻辑
        return "error";
    }

}
