package com.student.course.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket通知处理器
 */
@Slf4j
@Component
public class NotificationWebSocketHandler {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 存储用户ID和WebSocket会话的映射关系
    private final Map<Long, String> userSessions = new ConcurrentHashMap<>();

    /**
     * 注册用户会话
     */
    public void registerUserSession(Long userId, String sessionId) {
        userSessions.put(userId, sessionId);
        log.info("用户会话注册成功: userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * 移除用户会话
     */
    public void removeUserSession(Long userId) {
        userSessions.remove(userId);
        log.info("用户会话移除成功: userId=", userId);
    }

    /**
     * 向指定用户发送通知
     */
    public void sendNotificationToUser(Long userId, Object notification) {
        try {
            // 发送到用户的私有队列
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notification",
                    notification);
            log.info("通知发送成功: userId={}, notification={}", userId, notification);
        } catch (Exception e) {
            log.error("通知发送失败: userId={}", userId, e);
        }
    }

    /**
     * 向所有用户广播通知
     */
    public void broadcastNotification(Object notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notification", notification);
            log.info("广播通知发送成功: notification={}", notification);
        } catch (Exception e) {
            log.error("广播通知发送失败", e);
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return userSessions.containsKey(userId);
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
}
