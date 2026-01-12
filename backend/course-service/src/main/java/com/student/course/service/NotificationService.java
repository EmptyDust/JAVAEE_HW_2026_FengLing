package com.student.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.exception.BusinessException;
import com.student.course.entity.CourseEnrollment;
import com.student.course.entity.CourseNotification;
import com.student.course.entity.NotificationReceive;
import com.student.course.mapper.CourseEnrollmentMapper;
import com.student.course.mapper.CourseNotificationMapper;
import com.student.course.mapper.NotificationReceiveMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired
    private CourseNotificationMapper notificationMapper;

    @Autowired
    private NotificationReceiveMapper receiveMapper;

    @Autowired
    private CourseEnrollmentMapper enrollmentMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.course}")
    private String courseExchange;

    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    /**
     * 创建并发送通知
     */
    @Transactional(rollbackFor = Exception.class)
    public CourseNotification createAndSendNotification(CourseNotification notification) {
        // 1. 保存通知到数据库
        notification.setSendStatus(0); // 待发送
        notification.setSendCount(0);
        notification.setReadCount(0);
        notification.setStatus(1);

        // 如果没有设置isScheduled，默认为立即发送
        if (notification.getIsScheduled() == null) {
            notification.setIsScheduled(0);
        }

        notificationMapper.insert(notification);

        // 2. 如果是立即发送，发送消息到RabbitMQ；如果是定时发送，由定时任务处理
        if (notification.getIsScheduled() == 0) {
            try {
                Map<String, Object> message = new HashMap<>();
                message.put("notificationId", notification.getId());
                message.put("courseId", notification.getCourseId());
                message.put("targetType", notification.getTargetType());
                message.put("sendMethod", notification.getSendMethod());

                rabbitTemplate.convertAndSend(courseExchange, notificationRoutingKey, message);

                // 更新发送状态为发送中
                notification.setSendStatus(1);
                notificationMapper.updateById(notification);

                log.info("通知消息已发送到队列: notificationId={}", notification.getId());
            } catch (Exception e) {
                log.error("发送通知消息失败: notificationId={}", notification.getId(), e);
                notification.setSendStatus(3); // 失败
                notificationMapper.updateById(notification);
                throw new BusinessException("发送通知失败");
            }
        } else {
            log.info("定时通知已创建: notificationId={}, scheduledTime={}",
                    notification.getId(), notification.getScheduledTime());
        }

        return notification;
    }

    /**
     * 获取通知列表（分页）
     */
    public IPage<CourseNotification> getNotificationList(Long courseId, Integer page, Integer size) {
        Page<CourseNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CourseNotification> wrapper = new LambdaQueryWrapper<>();

        if (courseId != null) {
            wrapper.eq(CourseNotification::getCourseId, courseId);
        }

        wrapper.eq(CourseNotification::getStatus, 1)
               .orderByDesc(CourseNotification::getCreateTime);

        return notificationMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 获取用户的通知列表（分页）
     */
    public IPage<Map<String, Object>> getUserNotifications(Long userId, Integer isRead, Integer page, Integer size) {
        Page<NotificationReceive> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<NotificationReceive> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(NotificationReceive::getUserId, userId)
               .eq(NotificationReceive::getStatus, 1);

        if (isRead != null) {
            wrapper.eq(NotificationReceive::getIsRead, isRead);
        }

        wrapper.orderByDesc(NotificationReceive::getReceiveTime);

        IPage<NotificationReceive> receivePage = receiveMapper.selectPage(pageParam, wrapper);

        // 组装通知详情
        Page<Map<String, Object>> resultPage = new Page<>(page, size);
        resultPage.setTotal(receivePage.getTotal());

        List<Map<String, Object>> records = receivePage.getRecords().stream().map(receive -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", receive.getId());
            map.put("notificationId", receive.getNotificationId());
            map.put("isRead", receive.getIsRead());
            map.put("readTime", receive.getReadTime());
            map.put("receiveTime", receive.getReceiveTime());

            // 查询通知详情
            CourseNotification notification = notificationMapper.selectById(receive.getNotificationId());
            if (notification != null) {
                map.put("title", notification.getTitle());
                map.put("content", notification.getContent());
                map.put("notificationType", notification.getNotificationType());
                map.put("priority", notification.getPriority());
                map.put("courseId", notification.getCourseId());
            }

            return map;
        }).toList();

        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 标记通知为已读（优化版本）
     *
     * 性能优化：
     * 1. 使用原子更新 SQL 更新 read_count
     * 2. 减少数据库查询次数
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long receiveId, Long userId) {
        NotificationReceive receive = receiveMapper.selectById(receiveId);

        if (receive == null) {
            throw new BusinessException("通知记录不存在");
        }

        if (!receive.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此通知");
        }

        if (receive.getIsRead() == 0) {
            // 1. 更新接收记录为已读
            receive.setIsRead(1);
            receive.setReadTime(LocalDateTime.now());
            receiveMapper.updateById(receive);

            // 2. 原子性增加通知的已读数量（避免"先查后改"）
            notificationMapper.incrementReadCount(receive.getNotificationId());

            log.info("通知已标记为已读: receiveId={}, userId={}, notificationId={}",
                    receiveId, userId, receive.getNotificationId());
        }
    }

    /**
     * 批量标记为已读（优化版本，避免 N+1 查询）
     *
     * 性能优化：
     * 1. 使用 UpdateWrapper 批量更新 notification_receive 表（1次SQL）
     * 2. 使用子查询批量更新 course_notification 的 read_count（1次SQL）
     * 3. 避免循环中的数据库操作
     *
     * 优化前：100条未读通知需要 201次数据库操作
     * 优化后：100条未读通知只需要 3次数据库操作
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        // 1. 先统计未读数量（用于日志）
        LambdaQueryWrapper<NotificationReceive> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(NotificationReceive::getUserId, userId)
                    .eq(NotificationReceive::getIsRead, 0)
                    .eq(NotificationReceive::getStatus, 1);
        Long unreadCount = receiveMapper.selectCount(countWrapper);

        if (unreadCount == 0) {
            log.info("用户没有未读通知: userId={}", userId);
            return;
        }

        // 2. 批量更新 notification_receive 表（单条SQL，避免循环更新）
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<NotificationReceive> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                     .eq("is_read", 0)
                     .eq("status", 1)
                     .set("is_read", 1)
                     .set("read_time", LocalDateTime.now());

        int updatedRows = receiveMapper.update(null, updateWrapper);

        // 3. 批量更新 course_notification 的 read_count（单条SQL，使用子查询统计）
        int updatedNotifications = notificationMapper.batchIncrementReadCountByUserId(userId);

        log.info("批量标记已读成功: userId={}, unreadCount={}, updatedRows={}, updatedNotifications={}",
                userId, unreadCount, updatedRows, updatedNotifications);
    }

    /**
     * 获取未读通知数量
     */
    public Long getUnreadCount(Long userId) {
        // 使用自定义方法查询，避免数据权限拦截器冲突
        return receiveMapper.countUnreadByUserId(userId);
    }

    /**
     * 删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long id) {
        CourseNotification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }

        // 软删除
        notification.setStatus(0);
        notificationMapper.updateById(notification);

        log.info("通知已删除: id={}", id);
    }

    /**
     * 获取课程的选课学生ID列表
     */
    public List<Long> getCourseStudentIds(Long courseId) {
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getCourseId, courseId)
               .eq(CourseEnrollment::getStatus, 1);

        List<CourseEnrollment> enrollments = enrollmentMapper.selectList(wrapper);
        return enrollments.stream()
                .map(CourseEnrollment::getStudentId)
                .toList();
    }

    /**
     * 创建通知接收记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void createNotificationReceive(Long notificationId, Long userId, String userName, String receiveMethod) {
        NotificationReceive receive = new NotificationReceive();
        receive.setNotificationId(notificationId);
        receive.setUserId(userId);
        receive.setUserName(userName);
        receive.setReceiveMethod(receiveMethod);
        receive.setIsRead(0);
        receive.setReceiveTime(LocalDateTime.now());
        receive.setStatus(1);

        receiveMapper.insert(receive);
    }

    /**
     * 更新通知发送状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateNotificationSendStatus(Long notificationId, Integer sendStatus, Integer sendCount) {
        CourseNotification notification = notificationMapper.selectById(notificationId);
        if (notification != null) {
            notification.setSendStatus(sendStatus);
            notification.setSendCount(sendCount);
            notification.setSendTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }

    /**
     * 获取通知统计信息
     */
    public Map<String, Object> getNotificationStatistics(Long courseId, String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();

        // 构建查询条件
        LambdaQueryWrapper<CourseNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseNotification::getStatus, 1);

        if (courseId != null) {
            wrapper.eq(CourseNotification::getCourseId, courseId);
        }

        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(CourseNotification::getCreateTime, startDate);
        }

        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(CourseNotification::getCreateTime, endDate);
        }

        // 总通知数量
        Long totalCount = notificationMapper.selectCount(wrapper);
        statistics.put("totalCount", totalCount);

        // 按类型统计
        Map<String, Long> typeStats = new HashMap<>();
        for (String type : List.of("announcement", "homework", "exam", "cancel")) {
            LambdaQueryWrapper<CourseNotification> typeWrapper = new LambdaQueryWrapper<>();
            typeWrapper.eq(CourseNotification::getStatus, 1)
                       .eq(CourseNotification::getNotificationType, type);
            if (courseId != null) {
                typeWrapper.eq(CourseNotification::getCourseId, courseId);
            }
            if (startDate != null && !startDate.isEmpty()) {
                typeWrapper.ge(CourseNotification::getCreateTime, startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                typeWrapper.le(CourseNotification::getCreateTime, endDate);
            }
            Long count = notificationMapper.selectCount(typeWrapper);
            typeStats.put(type, count);
        }
        statistics.put("typeStats", typeStats);

        // 按发送状态统计
        Map<String, Long> statusStats = new HashMap<>();
        for (Integer status : List.of(0, 1, 2, 3)) {
            LambdaQueryWrapper<CourseNotification> statusWrapper = new LambdaQueryWrapper<>();
            statusWrapper.eq(CourseNotification::getStatus, 1)
                         .eq(CourseNotification::getSendStatus, status);
            if (courseId != null) {
                statusWrapper.eq(CourseNotification::getCourseId, courseId);
            }
            if (startDate != null && !startDate.isEmpty()) {
                statusWrapper.ge(CourseNotification::getCreateTime, startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                statusWrapper.le(CourseNotification::getCreateTime, endDate);
            }
            Long count = notificationMapper.selectCount(statusWrapper);
            String statusKey = switch (status) {
                case 0 -> "pending";
                case 1 -> "sending";
                case 2 -> "sent";
                case 3 -> "failed";
                default -> "unknown";
            };
            statusStats.put(statusKey, count);
        }
        statistics.put("statusStats", statusStats);

        // 计算已读率
        List<CourseNotification> notifications = notificationMapper.selectList(wrapper);
        long totalSendCount = notifications.stream()
                .mapToLong(n -> n.getSendCount() != null ? n.getSendCount() : 0)
                .sum();
        long totalReadCount = notifications.stream()
                .mapToLong(n -> n.getReadCount() != null ? n.getReadCount() : 0)
                .sum();

        double readRate = totalSendCount > 0 ? (double) totalReadCount / totalSendCount * 100 : 0;
        statistics.put("totalSendCount", totalSendCount);
        statistics.put("totalReadCount", totalReadCount);
        statistics.put("readRate", Math.round(readRate * 100.0) / 100.0);

        return statistics;
    }
}
