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
        if (shouldSkip()) {
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

    /**
     * 检查是否应该跳过数据权限过滤
     */
    private boolean shouldSkip() {
        // 检查ThreadLocal忽略标志
        if (UserContextHolder.isIgnoreDataPermission()) {
            return true;
        }

        // TODO: 检查@IgnoreDataPermission注解
        // 需要从MappedStatement中获取Mapper方法，检查是否有@IgnoreDataPermission注解
        // 这部分可以在后续优化时实现

        return false;
    }
}
