package com.student.course.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.student.common.datapermission.DataPermissionInterceptor;
import com.student.common.datapermission.OwnershipValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 */
@Configuration
public class MybatisPlusConfig {

    @Autowired
    private DataPermissionInterceptor dataPermissionInterceptor;

    @Autowired
    private OwnershipValidationInterceptor ownershipValidationInterceptor;

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加数据权限拦截器（必须在分页插件之前）
        interceptor.addInnerInterceptor(dataPermissionInterceptor);

        // 添加所有权验证拦截器
        interceptor.addInnerInterceptor(ownershipValidationInterceptor);

        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }
}
