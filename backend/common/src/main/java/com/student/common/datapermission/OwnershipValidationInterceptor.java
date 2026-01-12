package com.student.common.datapermission;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.student.common.context.UserContext;
import com.student.common.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * 所有权验证拦截器
 * 确保教师只能修改自己的课程
 * 与DataPermissionInterceptor配合使用
 */
@Component
@Slf4j
public class OwnershipValidationInterceptor implements InnerInterceptor {

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        // 1. Check if should skip
        if (shouldSkip(ms)) {
            return;
        }

        // 2. Get user context
        UserContext context = UserContextHolder.getContext();
        if (context == null || context.isAdmin()) {
            return; // Admin bypasses ownership check
        }

        // 3. Only validate for teachers
        if (!context.isTeacher()) {
            return;
        }

        // 4. Check if this is a course_info operation
        BoundSql boundSql = ms.getBoundSql(parameter);
        String sql = boundSql.getSql().toLowerCase();

        if (sql.contains("course_info")) {
            // Log ownership validation for audit purposes
            log.debug("Ownership validation: teacher {} attempting to modify course_info",
                    context.getTeacherId());

            // The actual filtering is done by DataPermissionInterceptor
            // which adds WHERE teacher_id = ? to the SQL
            // This interceptor just ensures the validation is logged
        }
    }

    /**
     * Check if should skip ownership validation
     */
    private boolean shouldSkip(MappedStatement ms) {
        // Skip for SELECT operations (handled by DataPermissionInterceptor)
        SqlCommandType commandType = ms.getSqlCommandType();
        if (commandType == SqlCommandType.SELECT) {
            return true;
        }

        // Check if data permission is ignored
        if (UserContextHolder.isIgnoreDataPermission()) {
            return true;
        }

        return false;
    }
}
