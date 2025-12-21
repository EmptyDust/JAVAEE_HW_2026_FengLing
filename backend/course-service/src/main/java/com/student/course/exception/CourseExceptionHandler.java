package com.student.course.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 课程服务专用异常处理器
 * 处理Spring MVC特有的异常
 */
@Slf4j
@RestControllerAdvice
public class CourseExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e) {
        // 静默处理favicon.ico等静态资源的404请求，不打印错误日志
        log.debug("静态资源未找到: {}", e.getMessage());
    }
}
