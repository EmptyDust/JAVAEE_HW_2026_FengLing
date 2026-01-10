package com.student.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解
 * 用于实体类上，显式配置数据权限规则
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    /**
     * 过滤字段名（默认自动检测）
     */
    String field() default "";

    /**
     * 适用的用户类型（默认所有非admin用户）
     */
    String[] userTypes() default {"teacher", "student"};

    /**
     * 自定义过滤条件（用于复杂场景）
     */
    String condition() default "";
}
