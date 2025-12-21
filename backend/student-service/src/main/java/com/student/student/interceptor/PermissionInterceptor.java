package com.student.student.interceptor;

import com.student.common.annotation.RequireRole;
import com.student.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 权限拦截器
 * 验证用户是否有权限访问接口
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查方法上的注解
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        // 检查类上的注解
        RequireRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);

        RequireRole requireRole = methodAnnotation != null ? methodAnnotation : classAnnotation;

        // 如果没有权限注解，允许访问
        if (requireRole == null) {
            return true;
        }

        // 从请求头获取用户类型
        String userType = request.getHeader("userType");
        if (userType == null || userType.isEmpty()) {
            throw new BusinessException("未授权访问");
        }

        // 检查用户类型是否在允许的角色列表中
        String[] allowedRoles = requireRole.value();
        boolean hasPermission = Arrays.asList(allowedRoles).contains(userType);

        if (!hasPermission) {
            throw new BusinessException("权限不足");
        }

        return true;
    }
}
