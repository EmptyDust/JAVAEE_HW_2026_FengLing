package com.student.student.interceptor;

import com.student.common.annotation.RequireRole;
import com.student.common.context.UserContext;
import com.student.common.context.UserContextHolder;
import com.student.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 权限拦截器
 * 验证用户是否有权限访问接口，并设置用户上下文
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 提取请求头中的用户信息
            String userIdStr = request.getHeader("userId");
            String userType = request.getHeader("userType");
            String studentIdStr = request.getHeader("studentId");
            String teacherIdStr = request.getHeader("teacherId");

            // 构建UserContext
            UserContext.UserContextBuilder builder = UserContext.builder();
            if (userIdStr != null && !userIdStr.isEmpty()) {
                builder.userId(Long.parseLong(userIdStr));
            }
            if (userType != null && !userType.isEmpty()) {
                builder.userType(userType);
            }
            if (studentIdStr != null && !studentIdStr.isEmpty()) {
                builder.studentId(Long.parseLong(studentIdStr));
            }
            if (teacherIdStr != null && !teacherIdStr.isEmpty()) {
                builder.teacherId(Long.parseLong(teacherIdStr));
            }

            // 设置到ThreadLocal
            UserContextHolder.setContext(builder.build());

            // 权限检查逻辑
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
        } catch (NumberFormatException e) {
            throw new BusinessException("无效的用户信息");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 关键：清理ThreadLocal防止内存泄漏
        UserContextHolder.clear();
    }
}
