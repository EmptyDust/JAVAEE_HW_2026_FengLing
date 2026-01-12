package com.student.common.mq.producer;

import com.student.common.exception.BusinessException;
import com.student.common.mq.dto.BaseMessage;
import com.student.common.mq.dto.MessageMetadata;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 消息生产者服务
 * 统一处理消息发送逻辑
 * 只在RabbitMQ可用时创建此Bean
 */
@Component
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    private static final String EXCHANGE_NAME = "student.system.exchange";

    /**
     * 发送消息到RabbitMQ
     *
     * @param routingKey  路由键
     * @param messageType 消息类型
     * @param payload     消息负载
     * @param priority    消息优先级 (1-10)
     * @param <T>         负载类型
     */
    public <T> void sendMessage(String routingKey, String messageType, T payload, Integer priority) {
        final String messageId = UUID.randomUUID().toString();
        String traceId = MDC.get("traceId");
        final String correlationId = (traceId != null) ? traceId : UUID.randomUUID().toString();

        BaseMessage<T> message = BaseMessage.<T>builder()
                .messageId(messageId)
                .messageType(messageType)
                .timestamp(LocalDateTime.now())
                .version("1.0")
                .payload(payload)
                .metadata(MessageMetadata.builder()
                        .sourceService(serviceName)
                        .correlationId(correlationId)
                        .retryCount(0)
                        .priority(priority)
                        .build())
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    EXCHANGE_NAME,
                    routingKey,
                    message,
                    msg -> {
                        msg.getMessageProperties().setPriority(priority);
                        msg.getMessageProperties().setMessageId(messageId);
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    }
            );
            log.info("Message sent successfully: messageId={}, type={}, routingKey={}",
                    messageId, messageType, routingKey);
        } catch (Exception e) {
            log.error("Failed to send message: messageId={}, type={}", messageId, messageType, e);
            throw new BusinessException("消息发送失败: " + e.getMessage());
        }
    }
}
