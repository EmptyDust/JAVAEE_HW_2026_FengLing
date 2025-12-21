package com.student.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_notification")
public class CourseNotification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 通知类型: announcement/homework/exam/cancel
     */
    private String notificationType;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 优先级: 0=普通 1=重要 2=紧急
     */
    private Integer priority;

    /**
     * 推送方式: websocket/sms/email
     */
    private String sendMethod;

    /**
     * 目标类型: enrolled=已选课学生/all=所有学生
     */
    private String targetType;

    /**
     * 发送状态: 0=待发送 1=发送中 2=已发送 3=失败
     */
    private Integer sendStatus;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 发送数量
     */
    private Integer sendCount;

    /**
     * 已读数量
     */
    private Integer readCount;

    /**
     * 状态: 0=删除 1=正常
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 定时发送时间
     */
    private LocalDateTime scheduledTime;

    /**
     * 是否定时发送: 0=立即发送 1=定时发送
     */
    private Integer isScheduled;
}
