package com.student.auth.mq.listener;

import com.rabbitmq.client.Channel;
import com.student.auth.entity.User;
import com.student.auth.mapper.UserMapper;
import com.student.common.mq.dto.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户注册消息监听器
 * 处理教师和学生的注册消息，创建用户账号
 */
@Component
@Slf4j
public class UserRegistrationListener {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String DEFAULT_PASSWORD = "$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm";
    private static final String IDEMPOTENCY_KEY_PREFIX = "mq:processed:";

    @RabbitListener(queues = "${rabbitmq.queue.user-registration}")
    public void handleRegistration(
            @Payload BaseMessage<?> message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) throws IOException {

        String messageId = message.getMessageId();
        String messageType = message.getMessageType();

        log.info("Received registration message: messageId={}, type={}", messageId, messageType);

        try {
            // 1. Idempotency check
            if (isMessageProcessed(messageId)) {
                log.info("Message already processed, skipping: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. Process based on message type
            if ("TEACHER_REGISTRATION".equals(messageType)) {
                processTeacherRegistration(message);
            } else if ("STUDENT_REGISTRATION".equals(messageType)) {
                processStudentRegistration(message);
            } else {
                log.warn("Unknown message type: {}", messageType);
                channel.basicAck(deliveryTag, false); // Ack to remove from queue
                return;
            }

            // 3. Mark as processed
            markMessageProcessed(messageId);

            // 4. Acknowledge message
            channel.basicAck(deliveryTag, false);
            log.info("Message processed successfully: messageId={}", messageId);

        } catch (DuplicateKeyException e) {
            // Username already exists - this is OK, mark as processed
            log.warn("User already exists, marking as processed: messageId={}", messageId);
            markMessageProcessed(messageId);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Failed to process message: messageId={}", messageId, e);

            // Check retry count
            Integer retryCount = message.getMetadata().getRetryCount();
            if (retryCount < 3) {
                // Retry with exponential backoff
                log.info("Retrying message: messageId={}, retryCount={}", messageId, retryCount + 1);
                message.getMetadata().setRetryCount(retryCount + 1);
                channel.basicNack(deliveryTag, false, true); // Requeue
            } else {
                // Max retries exceeded, send to DLQ
                log.error("Max retries exceeded, rejecting message: messageId={}", messageId);
                channel.basicReject(deliveryTag, false); // Don't requeue, goes to DLQ
            }
        }
    }

    private void processTeacherRegistration(BaseMessage<?> message) {
        Map<String, Object> payloadMap = (Map<String, Object>) message.getPayload();

        User user = new User();
        user.setUsername((String) payloadMap.get("teacherNo"));
        user.setPassword(DEFAULT_PASSWORD);
        user.setUserType("teacher");
        user.setTeacherId(Long.valueOf(payloadMap.get("teacherId").toString()));
        user.setPhone((String) payloadMap.get("phone"));
        user.setEmail((String) payloadMap.get("email"));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setPasswordExpired(false);

        userMapper.insert(user);
        log.info("Teacher user account created: userId={}, teacherId={}",
                user.getId(), user.getTeacherId());
    }

    private void processStudentRegistration(BaseMessage<?> message) {
        Map<String, Object> payloadMap = (Map<String, Object>) message.getPayload();

        // Extract password from payload, use default if not provided
        String password = (String) payloadMap.get("password");
        String encodedPassword;
        if (password != null && !password.isEmpty()) {
            // User-provided password, encrypt it
            encodedPassword = passwordEncoder.encode(password);
            log.info("Using user-provided password for student registration");
        } else {
            // No password provided, use default
            encodedPassword = DEFAULT_PASSWORD;
            log.info("Using default password for student registration");
        }

        User user = new User();
        user.setUsername((String) payloadMap.get("studentNo"));
        user.setPassword(encodedPassword);
        user.setUserType("student");
        user.setStudentId(Long.valueOf(payloadMap.get("studentId").toString()));
        user.setPhone((String) payloadMap.get("phone"));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setPasswordExpired(false);

        userMapper.insert(user);
        log.info("Student user account created: userId={}, studentId={}",
                user.getId(), user.getStudentId());
    }

    private boolean isMessageProcessed(String messageId) {
        String key = IDEMPOTENCY_KEY_PREFIX + messageId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void markMessageProcessed(String messageId) {
        String key = IDEMPOTENCY_KEY_PREFIX + messageId;
        redisTemplate.opsForValue().set(key, "1", 24, TimeUnit.HOURS);
    }
}
