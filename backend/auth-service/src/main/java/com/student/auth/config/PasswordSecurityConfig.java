package com.student.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 密码安全配置类
 * 可通过 application.yml 配置，便于后续调整策略
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "password.security")
public class PasswordSecurityConfig {

    /**
     * 密码过期天数 (默认180天)
     */
    private int expirationDays = 180;

    /**
     * 最大失败登录次数 (默认5次)
     */
    private int maxFailedAttempts = 5;

    /**
     * 账户锁定时长（分钟）(默认30分钟)
     */
    private int lockoutDurationMinutes = 30;

    /**
     * 是否对管理员应用密码策略 (默认false，管理员豁免)
     */
    private boolean applyToAdmin = false;
}
