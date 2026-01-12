package com.student.student.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.student.common.datapermission.DataPermissionInterceptor;
import com.student.common.datapermission.OwnershipValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Autowired
    private DataPermissionInterceptor dataPermissionInterceptor;

    @Autowired
    private OwnershipValidationInterceptor ownershipValidationInterceptor;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加数据权限拦截器（必须在分页插件之前）
        interceptor.addInnerInterceptor(dataPermissionInterceptor);

        // 添加所有权验证拦截器
        interceptor.addInnerInterceptor(ownershipValidationInterceptor);

        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L);
        paginationInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
