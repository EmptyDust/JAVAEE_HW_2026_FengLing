package com.student.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息元数据
 * 包含消息的追踪和控制信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 源服务名称
     */
    private String sourceService;

    /**
     * 关联ID (用于追踪请求链路)
     */
    private String correlationId;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 消息优先级 (1-10, 10最高)
     */
    private Integer priority;
}
