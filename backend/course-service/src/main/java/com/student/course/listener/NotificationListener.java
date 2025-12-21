package com.student.course.listener;

import com.student.course.entity.CourseNotification;
import com.student.course.mapper.CourseNotificationMapper;
import com.student.course.service.NotificationService;
import com.student.course.websocket.NotificationWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知消息监听器
 */
@Slf4j
@Component
public class NotificationListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CourseNotificationMapper notificationMapper;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    /**
     * 监听通知队列
     */
    @RabbitListener(queues = "${rabbitmq.queue.course-notification}")
    public void handleNotification(Map<String, Object> message) {
        try {
            log.info("收到通知消息: {}", message);

            Long notificationId = Long.valueOf(message.get("notificationId").toString());
            Long courseId = Long.valueOf(message.get("courseId").toString());
            String targetType = message.get("targetType").toString();
            String sendMethod = message.get("sendMethod").toString();

            // 查询通知详情
            CourseNotification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                log.error("通知不存在: notificationId={}", notificationId);
                return;
            }

            // 获取目标用户列表
            List<Long> targetUserIds = getTargetUserIds(courseId, targetType);
            log.info("目标用户数量: {}", targetUserIds.size());

            // 发送通知给每个用户
            int successCount = 0;
            for (Long userId : targetUserIds) {
                try {
                    // 创建通知接收记录
                    notificationService.createNotificationReceive(
                            notificationId,
                            userId,
                            null, // 用户名可以从用户服务获取
                            sendMethod
                    );

                    // 如果用户在线，通过WebSocket推送
                    if (sendMethod.contains("websocket") && webSocketHandler.isUserOnline(userId)) {
                        Map<String, Object> wsMessage = new HashMap<>();
                        wsMessage.put("notificationId", notificationId);
                        wsMessage.put("title", notification.getTitle());
                        wsMessage.put("content", notification.getContent());
                        wsMessage.put("notificationType", notification.getNotificationType());
                        wsMessage.put("priority", notification.getPriority());
                        wsMessage.put("courseId", notification.getCourseId());

                        webSocketHandler.sendNotificationToUser(userId, wsMessage);
                    }

                    successCount++;
                } catch (Exception e) {
                    log.error("发送通知给用户失败: userId={}", userId, e);
                }
            }

            // 更新通知发送状态
            notificationService.updateNotificationSendStatus(notificationId, 2, successCount);

            log.info("通知发送完成: notificationId={}, successCount={}/{}",
                    notificationId, successCount, targetUserIds.size());

        } catch (Exception e) {
            log.error("处理通知消息失败", e);
        }
    }

    /**
     * 获取目标用户ID列表
     */
    private List<Long> getTargetUserIds(Long courseId, String targetType) {
        if ("enrolled".equals(targetType)) {
            // 获取已选课学生
            return notificationService.getCourseStudentIds(courseId);
        } else if ("all".equals(targetType)) {
            // 获取所有学生（需要调用学生服务）
            // 这里暂时返回已选课学生
            return notificationService.getCourseStudentIds(courseId);
        }
        return List.of();
    }
}
