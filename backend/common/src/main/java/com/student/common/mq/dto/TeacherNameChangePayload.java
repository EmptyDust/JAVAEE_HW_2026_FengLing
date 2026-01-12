package com.student.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教师姓名变更消息负载
 * 用于同步更新课程表中的教师姓名
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherNameChangePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 旧姓名
     */
    private String oldName;

    /**
     * 新姓名
     */
    private String newName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
