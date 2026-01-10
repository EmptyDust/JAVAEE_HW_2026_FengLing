package com.student.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户上下文对象
 * 存储当前请求的用户信息，用于数据权限控制
 */
@Data
@Builder
@AllArgsConstructor
public class UserContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 用户类型：admin-管理员, teacher-教师, student-学生
     */
    private final String userType;

    /**
     * 学生ID（仅当用户类型为student时有值）
     */
    private final Long studentId;

    /**
     * 教师ID（仅当用户类型为teacher时有值）
     */
    private final Long teacherId;

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(userType);
    }

    /**
     * 判断是否为教师
     */
    public boolean isTeacher() {
        return "teacher".equals(userType);
    }

    /**
     * 判断是否为学生
     */
    public boolean isStudent() {
        return "student".equals(userType);
    }
}
