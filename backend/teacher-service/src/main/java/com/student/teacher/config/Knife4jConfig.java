package com.student.teacher.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("教师服务 API")
                        .description("教务管理系统 - 教师服务接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("风铃")
                                .email("fenglingyexing@gmail.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/license/mit")));
    }
}
