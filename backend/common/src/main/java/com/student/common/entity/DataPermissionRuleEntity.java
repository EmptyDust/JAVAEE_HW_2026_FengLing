package com.student.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据权限规则实体类
 * 对应数据库表：data_permission_rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_permission_rule")
public class DataPermissionRuleEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 角色类型：admin/teacher/student
     */
    private String roleType;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 实体类名
     */
    private String entityClass;

    /**
     * 过滤字段名（如：student_id, teacher_id）
     */
    private String filterField;

    /**
     * 过滤操作符：=, IN, >, <等
     */
    private String filterOperator;

    /**
     * UserContext中的字段名（如：studentId, teacherId, userId）
     */
    private String contextField;

    /**
     * 过滤类型：SIMPLE(简单条件), SUBQUERY(子查询)
     */
    private String filterType;

    /**
     * 子查询SQL（当filter_type=SUBQUERY时使用）
     */
    private String subquerySql;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer enabled;

    /**
     * 规则描述
     */
    private String description;
}
