-- Password Security Enhancement Migration
-- Add new fields to user table for password security features
-- 密码安全增强迁移脚本

USE student_system;

-- Add password security related fields
-- 添加密码安全相关字段
ALTER TABLE `user`
ADD COLUMN `password_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '密码最后更新时间',
ADD COLUMN `failed_login_attempts` INT DEFAULT 0 COMMENT '连续登录失败次数',
ADD COLUMN `account_locked_until` DATETIME NULL COMMENT '账户锁定截止时间',
ADD COLUMN `password_expired` TINYINT(1) DEFAULT 0 COMMENT '密码是否已过期 0-未过期 1-已过期';

-- Add indexes for performance
-- 添加索引以提升查询性能
ALTER TABLE `user`
ADD INDEX `idx_account_locked_until` (`account_locked_until`),
ADD INDEX `idx_password_update_time` (`password_update_time`);

-- Update existing users: set password_update_time to current time for backward compatibility
-- 为现有用户设置密码更新时间，确保向后兼容
UPDATE `user`
SET `password_update_time` = CURRENT_TIMESTAMP
WHERE `password_update_time` IS NULL;

-- For existing admin users, ensure they won't be affected by password policies
-- (This is handled in application logic, but we ensure data consistency)
-- 管理员用户的策略豁免在应用层处理，这里确保数据一致性

COMMIT;
