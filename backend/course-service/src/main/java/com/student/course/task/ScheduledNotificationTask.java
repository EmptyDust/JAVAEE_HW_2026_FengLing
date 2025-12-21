package com.student.course.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.course.entity.CourseNotification;
import com.student.course.mapper.CourseNotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时通知任务
 */
@Slf4j
@Component
public class ScheduledNotificationTask {

    @Autowired
    private CourseNotificationMapper notificationMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.course}")
    private String courseExchange;

    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    /**
     * 每分钟扫描一次待发送的定时通知
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanScheduledNotifications() {
        try {
            // 查询待发送的定时通知
            LambdaQueryWrapper<CourseNotification> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CourseNotification::getIsScheduled, 1)
                   .eq(CourseNotification::getSendStatus, 0)
                   .le(CourseNotification::getScheduledTime, LocalDateTime.now())
                   .eq(CourseNotification::getStatus, 1);

            List<CourseNotification> notifications = notificationMapper.selectList(wrapper);

            if (notifications.isEmpty()) {
                return;
            }

            log.info("扫描到 {} 条待发送的定时通知", notifications.size());

            // 发送通知
            for (CourseNotification notification : notifications) {
                try {
                    sendScheduledNotification(notification);
                } catch (Exception e) {
                    log.error("发送定时通知失败: notificationId={}", notification.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("扫描定时通知任务失败", e);
        }
    }

    /**
     * 发送定时通知
     */
    private void sendScheduledNotification(CourseNotification notification) {
        // 发送消息到RabbitMQ
        Map<String, Object> message = new HashMap<>();
        message.put("notificationId", notification.getId());
        message.put("courseId", notification.getCourseId());
        message.put("targetType", notification.getTargetType());
        message.put("sendMethod", notification.getSendMethod());

        rabbitTemplate.convertAndSend(courseExchange, notificationRoutingKey, message);

        // 更新发送状态为发送中
        notification.setSendStatus(1);
        notificationMapper.updateById(notification);

        log.info("定时通知已发送到队列: notificationId={}, scheduledTime={}",
                notification.getId(), notification.getScheduledTime());
    }
}
