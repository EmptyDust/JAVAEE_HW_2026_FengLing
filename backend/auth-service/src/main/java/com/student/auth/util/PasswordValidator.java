package com.student.auth.util;

import com.student.common.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * 密码验证工具类
 * 提供密码强度验证功能
 */
public class PasswordValidator {

    // 密码最小长度
    private static final int MIN_PASSWORD_LENGTH = 8;

    // 密码必须包含字母和数字的正则表达式
    private static final Pattern LETTER_PATTERN = Pattern.compile(".*[a-zA-Z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");

    /**
     * 验证密码强度
     * 规则：最少8个字符，必须包含字母和数字
     *
     * @param password 待验证的密码
     * @throws BusinessException 如果密码不符合要求
     */
    public static void validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        // 检查长度
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("密码长度至少为" + MIN_PASSWORD_LENGTH + "个字符");
        }

        // 检查是否包含字母
        if (!LETTER_PATTERN.matcher(password).matches()) {
            throw new BusinessException("密码必须包含字母");
        }

        // 检查是否包含数字
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new BusinessException("密码必须包含数字");
        }
    }

    /**
     * 获取密码要求说明（用于前端显示）
     *
     * @return 密码要求说明
     */
    public static String getPasswordRequirements() {
        return "密码要求：至少8个字符，必须包含字母和数字";
    }
}
