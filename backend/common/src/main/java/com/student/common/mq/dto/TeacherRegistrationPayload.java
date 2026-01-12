package com.student.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 教师注册消息负载
 * 用于创建教师用户账号
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRegistrationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师工号
     */
    private String teacherNo;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 所属院系
     */
    private String department;
}
