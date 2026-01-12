package com.student.course.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.course-notification}")
    private String notificationQueue;

    @Value("${rabbitmq.exchange.course}")
    private String courseExchange;

    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.queue.teacher-name-sync:teacher.name.sync.queue}")
    private String teacherNameSyncQueue;

    /**
     * 声明通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue)
                .build();
    }

    /**
     * 声明教师姓名同步队列
     */
    @Bean
    public Queue teacherNameSyncQueue() {
        return QueueBuilder.durable(teacherNameSyncQueue)
                .build();
    }

    /**
     * 声明课程交换机（Direct类型）
     */
    @Bean
    public DirectExchange courseExchange() {
        return ExchangeBuilder.directExchange(courseExchange)
                .durable(true)
                .build();
    }

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
     * 绑定通知队列到交换机
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(courseExchange())
                .with(notificationRoutingKey);
    }

    /**
     * 绑定教师姓名同步队列到系统交换机
     */
    @Bean
    public Binding teacherNameSyncBinding() {
        return BindingBuilder
                .bind(teacherNameSyncQueue())
                .to(systemExchange())
                .with("teacher.update.name");
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
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    /**
     * 配置监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
