package com.student.student.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 配置消息生产者相关组件
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    /**
     * 声明系统交换机（Topic类型）
     */
    @Bean
    public TopicExchange systemExchange() {
        return ExchangeBuilder.topicExchange("student.system.exchange")
                .durable(true)
                .build();
    }

    /**
     * 消息转换器（使用JSON格式）
     */
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true); // Enable return callback

        // Confirm callback - 消息是否到达交换机
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message send failed to exchange: {}", cause);
            }
        });

        // Return callback - 消息是否从交换机路由到队列
        template.setReturnsCallback(returned -> {
            log.error("Message returned from exchange: message={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
        });

        return template;
    }
}
