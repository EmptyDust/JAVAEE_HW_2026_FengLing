package com.student.course.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rabbitmq.client.Channel;
import com.student.common.mq.dto.BaseMessage;
import com.student.course.entity.CourseInfo;
import com.student.course.mapper.CourseInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 教师姓名同步监听器
 * 处理教师姓名变更消息，同步更新课程表
 */
@Component
@Slf4j
public class TeacherNameSyncListener {

    @Autowired
    private CourseInfoMapper courseInfoMapper;

    @RabbitListener(queues = "${rabbitmq.queue.teacher-name-sync}")
    public void handleTeacherNameChange(
            @Payload BaseMessage<?> message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) throws IOException {

        String messageId = message.getMessageId();
        log.info("Received teacher name change message: messageId={}", messageId);

        try {
            // Extract payload
            Map<String, Object> payloadMap = (Map<String, Object>) message.getPayload();
            Long teacherId = Long.valueOf(payloadMap.get("teacherId").toString());
            String newName = (String) payloadMap.get("newName");

            // Update all courses for this teacher
            LambdaUpdateWrapper<CourseInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CourseInfo::getTeacherId, teacherId)
                    .set(CourseInfo::getTeacherName, newName);

            int updatedCount = courseInfoMapper.update(null, updateWrapper);

            log.info("Teacher name synchronized: teacherId={}, newName={}, coursesUpdated={}",
                    teacherId, newName, updatedCount);

            // Acknowledge
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Failed to sync teacher name: messageId={}", messageId, e);

            // Check retry count
            Integer retryCount = message.getMetadata().getRetryCount();
            if (retryCount < 3) {
                log.info("Retrying message: messageId={}, retryCount={}", messageId, retryCount + 1);
                message.getMetadata().setRetryCount(retryCount + 1);
                channel.basicNack(deliveryTag, false, true); // Requeue
            } else {
                log.error("Max retries exceeded, rejecting: messageId={}", messageId);
                channel.basicReject(deliveryTag, false); // Send to DLQ
            }
        }
    }
}
