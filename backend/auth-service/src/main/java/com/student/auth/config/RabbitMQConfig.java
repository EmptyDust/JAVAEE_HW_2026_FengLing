package com.student.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
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
 * 配置消息消费者相关组件
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.user-registration}")
    private String registrationQueue;

    @Value("${rabbitmq.exchange.system}")
    private String systemExchange;

    @Value("${rabbitmq.dlx.name}")
    private String dlxName;

    /**
     * 声明系统交换机（Topic类型）
     */
    @Bean
    public TopicExchange systemExchange() {
        return ExchangeBuilder.topicExchange(systemExchange)
                .durable(true)
                .build();
    }

    /**
     * 声明死信交换机（Direct类型）
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(dlxName)
                .durable(true)
                .build();
    }

    /**
     * 声明用户注册队列（带死信配置）
     */
    @Bean
    public Queue registrationQueue() {
        return QueueBuilder.durable(registrationQueue)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", "user.registration.failed")
                .withArgument("x-message-ttl", 300000) // 5 minutes
                .withArgument("x-max-priority", 10)
                .build();
    }

    /**
     * 声明死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("student.system.dlq")
                .build();
    }

    /**
     * 绑定注册队列到系统交换机
     */
    @Bean
    public Binding registrationBinding() {
        return BindingBuilder
                .bind(registrationQueue())
                .to(systemExchange())
                .with("user.registration.#");
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("user.registration.failed");
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
        return template;
    }

    /**
     * 配置监听器容器工厂（手动确认模式）
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(5);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // Manual ack for better control
        return factory;
    }
}
