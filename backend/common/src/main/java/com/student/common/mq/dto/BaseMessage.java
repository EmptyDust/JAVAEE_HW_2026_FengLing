package com.student.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础消息类
 * 所有RabbitMQ消息的通用结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一ID (UUID)
     */
    private String messageId;

    /**
     * 消息类型 (如: TEACHER_REGISTRATION, STUDENT_REGISTRATION)
     */
    private String messageType;

    /**
     * 消息创建时间
     */
    private LocalDateTime timestamp;

    /**
     * 消息版本号
     */
    private String version;

    /**
     * 消息负载 (具体业务数据)
     */
    private T payload;

    /**
     * 消息元数据
     */
    private MessageMetadata metadata;
}
