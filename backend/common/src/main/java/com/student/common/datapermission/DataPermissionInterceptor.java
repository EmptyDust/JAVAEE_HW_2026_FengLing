package com.student.common.datapermission;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.student.common.context.UserContext;
import com.student.common.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * 数据权限拦截器
 * 拦截MyBatis查询操作，自动添加数据权限过滤条件
 */
@Component
@Slf4j
public class DataPermissionInterceptor implements InnerInterceptor {

    @Autowired
    private DataPermissionRuleRegistry ruleRegistry;

    @Autowired
    private DataPermissionSqlParser sqlParser;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 1. 检查是否应该跳过
        if (shouldSkip(ms)) {
            return;
        }

        // 2. 获取用户上下文
        UserContext context = UserContextHolder.getContext();
        if (context == null || context.isAdmin()) {
            // 管理员绕过数据权限过滤
            return;
        }

        // 3. 获取原始SQL
        String originalSql = boundSql.getSql();

        // 4. 解析并修改SQL
        String modifiedSql = sqlParser.addDataPermission(originalSql, context, ruleRegistry);

        // 如果SQL没有变化，不做处理
        if (originalSql.equals(modifiedSql)) {
            return;
        }

        log.debug("Original SQL: {}", originalSql);
        log.debug("Modified SQL: {}", modifiedSql);

        // 5. 使用反射修改BoundSql中的sql字段
        MetaObject metaObject = MetaObject.forObject(
                boundSql,
                SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());
        metaObject.setValue("sql", modifiedSql);
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        // 1. 检查是否应该跳过
        if (shouldSkip(ms)) {
            return;
        }

        // 2. 获取用户上下文
        UserContext context = UserContextHolder.getContext();
        if (context == null || context.isAdmin()) {
            // 管理员绕过数据权限过滤
            return;
        }

        // 3. 只对教师进行课程表的更新权限控制
        if (!context.isTeacher()) {
            return;
        }

        // 4. 获取SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();

        // 5. 检查是否是course_info表的更新
        if (!originalSql.toLowerCase().contains("course_info")) {
            return;
        }

        // 6. 解析并修改SQL，添加teacher_id限制
        String modifiedSql = sqlParser.addDataPermission(originalSql, context, ruleRegistry);

        // 如果SQL没有变化，不做处理
        if (originalSql.equals(modifiedSql)) {
            return;
        }

        log.debug("Original UPDATE SQL: {}", originalSql);
        log.debug("Modified UPDATE SQL: {}", modifiedSql);

        // 7. 使用反射修改BoundSql中的sql字段
        MetaObject metaObject = MetaObject.forObject(
                boundSql,
                SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());
        metaObject.setValue("sql", modifiedSql);
    }

    /**
     * 检查是否应该跳过数据权限过滤
     */
    private boolean shouldSkip(MappedStatement ms) {
        // 检查ThreadLocal忽略标志
        if (UserContextHolder.isIgnoreDataPermission()) {
            return true;
        }

        // 检查@IgnoreDataPermission注解
        try {
            String mapperId = ms.getId();
            // mapperId格式: com.student.course.mapper.CourseEnrollmentMapper.selectByStudentAndCourses
            int lastDotIndex = mapperId.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String className = mapperId.substring(0, lastDotIndex);
                String methodName = mapperId.substring(lastDotIndex + 1);

                // 加载Mapper接口类
                Class<?> mapperClass = Class.forName(className);

                // 查找方法（简化处理，只匹配方法名）
                for (java.lang.reflect.Method method : mapperClass.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        // 检查是否有@IgnoreDataPermission注解
                        if (method.isAnnotationPresent(com.student.common.annotation.IgnoreDataPermission.class)) {
                            log.debug("跳过数据权限过滤: {}", mapperId);
                            return true;
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.warn("无法加载Mapper类进行注解检查: {}", ms.getId());
        }

        return false;
    }
}
