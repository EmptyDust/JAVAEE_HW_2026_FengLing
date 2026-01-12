package com.student.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学生注册消息负载
 * 用于创建学生用户账号
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRegistrationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 密码（用户设置的密码）
     */
    private String password;
}
