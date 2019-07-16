package com.rw.article.common.exception;

import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 全局异常处理
 * @date 2018/8/214:10
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 在controller里面内容执行之前，校验一些参数不匹配啊，Get post方法不对啊之类的
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.info("--------------------------------------");
        return new ResponseEntity<Object>("error", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonObject jsonHandler(HttpServletRequest request, Exception e) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("code", Constants.ErrorCode.OTHER_ERROR);
        jsonObject.put("mesage", e.getMessage());
        log(e, request);
        return jsonObject;
    }

    private void log(Exception ex, HttpServletRequest request) {
        logger.error("************************异常开始*******************************");

        ex.printStackTrace();
       // logger.error(" " + ex);
        logger.error("请求地址：" + request.getRequestURL());
        Enumeration enumeration = request.getParameterNames();
        logger.error("请求参数");
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement().toString();
            logger.error(name + " --- " + request.getParameter(name));
        }

        StackTraceElement[] error = ex.getStackTrace();
        for (StackTraceElement stackTraceElement : error) {
            logger.error(stackTraceElement.toString());
        }
        logger.error("************************异常结束*******************************");
    }
}
