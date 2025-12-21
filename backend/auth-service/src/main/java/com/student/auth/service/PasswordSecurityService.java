package com.student.auth.service;

import com.student.auth.config.PasswordSecurityConfig;
import com.student.auth.entity.User;
import com.student.auth.mapper.UserMapper;
import com.student.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 密码安全服务
 * 处理账户锁定、密码过期等安全策略
 */
@Slf4j
@Service
public class PasswordSecurityService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordSecurityConfig securityConfig;

    /**
     * 检查用户是否需要应用密码安全策略
     * 管理员默认豁免
     */
    private boolean shouldApplySecurityPolicy(User user) {
        if ("admin".equals(user.getUserType())) {
            return securityConfig.isApplyToAdmin();
        }
        return true; // 学生和教师都应用策略
    }

    /**
     * 检查账户是否被锁定
     * 如果锁定时间已过，自动解锁
     *
     * @param user 用户对象
     * @throws BusinessException 如果账户仍在锁定期内
     */
    public void checkAccountLockout(User user) {
        if (!shouldApplySecurityPolicy(user)) {
            return;
        }

        if (user.getAccountLockedUntil() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(user.getAccountLockedUntil())) {
                // 账户仍在锁定期内
                long minutesRemaining = java.time.Duration.between(now, user.getAccountLockedUntil()).toMinutes();
                throw new BusinessException("账户已被锁定，请在 " + minutesRemaining + " 分钟后重试");
            } else {
                // 锁定时间已过，自动解锁
                unlockAccount(user);
            }
        }
    }

    /**
     * 检查密码是否过期
     *
     * @param user 用户对象
     * @return true-密码已过期, false-密码未过期
     */
    public boolean isPasswordExpired(User user) {
        if (!shouldApplySecurityPolicy(user)) {
            return false;
        }

        // 检查强制过期标志
        if (Boolean.TRUE.equals(user.getPasswordExpired())) {
            return true;
        }

        // 检查时间过期
        if (user.getPasswordUpdateTime() == null) {
            return false; // 如果没有更新时间，认为未过期（向后兼容）
        }

        LocalDateTime expirationDate = user.getPasswordUpdateTime().plusDays(securityConfig.getExpirationDays());

        return LocalDateTime.now().isAfter(expirationDate);
    }

    /**
     * 记录登录失败
     * 如果失败次数达到阈值，锁定账户
     *
     * @param user 用户对象
     */
    public void recordLoginFailure(User user) {
        if (!shouldApplySecurityPolicy(user)) {
            return;
        }

        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= securityConfig.getMaxFailedAttempts()) {
            // 锁定账户
            LocalDateTime lockUntil = LocalDateTime.now()
                    .plusMinutes(securityConfig.getLockoutDurationMinutes());
            user.setAccountLockedUntil(lockUntil);

            log.warn("用户 {} 因连续 {} 次登录失败被锁定至 {}",
                    user.getUsername(), attempts, lockUntil);
        }

        userMapper.updateById(user);
    }

    /**
     * 重置登录失败计数（登录成功时调用）
     *
     * @param user 用户对象
     */
    public void resetLoginFailures(User user) {
        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userMapper.updateById(user);
        }
    }

    /**
     * 解锁账户
     *
     * @param user 用户对象
     */
    private void unlockAccount(User user) {
        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userMapper.updateById(user);
        log.info("用户 {} 账户已自动解锁", user.getUsername());
    }

    /**
     * 更新密码更新时间
     *
     * @param user 用户对象
     */
    public void updatePasswordTimestamp(User user) {
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setPasswordExpired(false);
        userMapper.updateById(user);
    }
}
