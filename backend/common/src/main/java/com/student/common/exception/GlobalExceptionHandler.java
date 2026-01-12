package com.student.common.exception;

import com.student.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 生成唯一的请求ID用于追踪
        String requestId = UUID.randomUUID().toString();

        // 记录详细的异常信息
        log.error("系统异常 [RequestId: {}]", requestId, e);

        // 开发/测试环境返回详细错误信息
        if (isDevelopmentMode()) {
            return Result.error("系统异常: " + e.getMessage() + " [RequestId: " + requestId + "]");
        }

        // 生产环境返回友好提示 + RequestId
        return Result.error("系统异常，请联系管理员并提供错误码: " + requestId);
    }

    /**
     * 判断是否为开发模式
     */
    private boolean isDevelopmentMode() {
        return "dev".equals(activeProfile) || "test".equals(activeProfile);
    }
}
