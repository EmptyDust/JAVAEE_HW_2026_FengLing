package com.student.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.auth.config.PasswordSecurityConfig;
import com.student.auth.entity.User;
import com.student.auth.mapper.UserMapper;
import com.student.auth.util.PasswordValidator;
import com.student.common.exception.BusinessException;
import com.student.common.util.JwtUtil;
import com.student.common.util.RedisUtil;
import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PasswordSecurityService passwordSecurityService;

    @Autowired
    private PasswordSecurityConfig securityConfig;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void register(String username, String password, String email, String phone) {
        register(username, password, email, phone, "student", null);
    }

    public void register(String username, String password, String email, String phone, String userType,
            Long studentId) {
        // 1. 验证密码强度
        PasswordValidator.validatePasswordStrength(password);

        // 2. 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setUserType(userType);
        user.setStudentId(studentId);

        // 4. 设置密码安全字段初始值
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setPasswordExpired(false);

        userMapper.insert(user);
        log.info("用户 {} 注册成功，类型：{}", username, userType);
    }

    public Map<String, Object> getCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text();
        String uuid = UUID.randomUUID().toString();

        redisUtil.set("captcha:" + uuid, code, 5, TimeUnit.MINUTES);

        Map<String, Object> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("image", captcha.toBase64());
        return result;
    }

    public Map<String, Object> login(String username, String password, String uuid, String captcha) {
        // 1. 验证码检查
        String cachedCaptcha = (String) redisUtil.get("captcha:" + uuid);
        if (cachedCaptcha == null || !cachedCaptcha.equalsIgnoreCase(captcha)) {
            throw new BusinessException("验证码错误或已过期");
        }

        redisUtil.delete("captcha:" + uuid);

        // 2. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查账户锁定状态（会自动解锁过期的锁定）
        try {
            passwordSecurityService.checkAccountLockout(user);
        } catch (BusinessException e) {
            // 账户被锁定，直接抛出异常，不增加失败次数
            throw e;
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 密码错误，记录失败次数
            passwordSecurityService.recordLoginFailure(user);

            int remainingAttempts = securityConfig.getMaxFailedAttempts() -
                    (user.getFailedLoginAttempts() == null ? 1 : user.getFailedLoginAttempts() + 1);

            if (remainingAttempts > 0) {
                throw new BusinessException("用户名或密码错误，还有 " + remainingAttempts + " 次尝试机会");
            } else {
                throw new BusinessException("用户名或密码错误，账户已被锁定 " +
                        securityConfig.getLockoutDurationMinutes() + " 分钟");
            }
        }

        // 5. 密码正确，重置失败次数
        passwordSecurityService.resetLoginFailures(user);

        // 6. 检查密码是否过期
        boolean passwordExpired = passwordSecurityService.isPasswordExpired(user);

        // 7. 生成token（包含 studentId 和 teacherId）
        String token = JwtUtil.generateToken(
            user.getId(),
            user.getUsername(),
            user.getUserType(),
            user.getStudentId(),
            user.getTeacherId()  // 新增：包含 teacherId
        );
        redisUtil.set("token:" + user.getId(), token, 24, TimeUnit.HOURS);

        // 8. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("userType", user.getUserType());
        result.put("studentId", user.getStudentId());
        result.put("teacherId", user.getTeacherId());  // 新增：返回 teacherId
        result.put("passwordExpired", passwordExpired); // 前端根据此字段决定是否强制修改密码

        if (passwordExpired) {
            result.put("message", "您的密码已过期，请立即修改密码");
        }

        log.info("用户 {} 登录成功，密码过期状态：{}", username, passwordExpired);
        return result;
    }

    public void logout(Long userId) {
        redisUtil.delete("token:" + userId);
    }

    public User getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return user;
    }

    public void updateUserProfile(Long userId, String email, String phone) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setEmail(email);
        user.setPhone(phone);
        userMapper.updateById(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 1. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 2. 验证新密码强度
        PasswordValidator.validatePasswordStrength(newPassword);

        // 3. 检查新密码是否与旧密码相同
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 4. 更新新密码
        user.setPassword(passwordEncoder.encode(newPassword));

        // 5. 更新密码更新时间，清除过期标志
        passwordSecurityService.updatePasswordTimestamp(user);

        // 6. 清除token，要求重新登录
        redisUtil.delete("token:" + userId);

        log.info("用户 {} 修改密码成功", user.getUsername());
    }
}
