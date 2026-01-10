package com.student.common.datapermission;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 数据权限规则（内存对象）
 * 用于在MyBatis拦截器中进行SQL过滤
 */
@Data
@Builder
public class DataPermissionRule {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 实体类名
     */
    private String entityClass;

    /**
     * 字段规则映射：userType -> FieldRule
     */
    private Map<String, FieldRule> fieldRules;

    /**
     * 字段规则
     */
    @Data
    @Builder
    public static class FieldRule {

        /**
         * 过滤字段名（如：student_id）
         */
        private String fieldName;

        /**
         * 过滤操作符（如：=, IN, >, <）
         */
        private String operator;

        /**
         * UserContext中的字段名（如：studentId）
         */
        private String contextField;

        /**
         * 过滤类型：SIMPLE(简单条件), SUBQUERY(子查询)
         */
        private String filterType;

        /**
         * 子查询SQL（当filterType=SUBQUERY时使用）
         */
        private String subquerySql;
    }
}
