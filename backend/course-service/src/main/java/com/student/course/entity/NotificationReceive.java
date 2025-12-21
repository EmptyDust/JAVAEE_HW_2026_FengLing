package com.student.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知接收记录实体
 */
@Data
@TableName("notification_receive")
public class NotificationReceive {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知ID
     */
    private Long notificationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 接收方式: websocket/sms/email
     */
    private String receiveMethod;

    /**
     * 是否已读: 0=未读 1=已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 状态: 0=删除 1=正常
     */
    private Integer status;
}
