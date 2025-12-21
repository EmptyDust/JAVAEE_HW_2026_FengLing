package com.student.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String email;
    private String phone;
    /**
     * 用户类型: admin/teacher/student
     */
    private String userType;
    /**
     * 学生ID (当用户类型为 student 时有值)
     */
    private Long studentId;

    // ========== Password Security Fields ==========
    /**
     * 密码最后更新时间
     */
    private LocalDateTime passwordUpdateTime;
    /**
     * 连续登录失败次数
     */
    private Integer failedLoginAttempts;
    /**
     * 账户锁定截止时间 (NULL表示未锁定)
     */
    private LocalDateTime accountLockedUntil;
    /**
     * 密码是否已过期 (0-未过期, 1-已过期)
     */
    private Boolean passwordExpired;
}
