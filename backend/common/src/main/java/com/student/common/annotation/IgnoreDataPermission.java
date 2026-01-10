package com.student.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略数据权限注解
 * 用于Mapper方法上，跳过数据权限过滤
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreDataPermission {

    /**
     * 忽略原因（用于文档说明）
     */
    String reason() default "";
}
