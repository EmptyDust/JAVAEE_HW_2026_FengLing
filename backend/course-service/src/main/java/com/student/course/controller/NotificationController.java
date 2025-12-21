package com.student.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.annotation.RequireRole;
import com.student.common.result.Result;
import com.student.course.entity.CourseNotification;
import com.student.course.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/notification")
@Tag(name = "通知管理", description = "课程通知相关接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 创建并发送通知
     */
    @RequireRole({"admin", "teacher"})
    @PostMapping("/create")
    @Operation(summary = "创建并发送通知")
    public Result<CourseNotification> createNotification(@RequestBody CourseNotification notification,
                                                          @RequestHeader(value = "userId", required = false) Long userId,
                                                          @RequestHeader(value = "username", required = false) String username) {
        try {
            notification.setCreateUserId(userId);
            notification.setCreateUserName(username);
            CourseNotification result = notificationService.createAndSendNotification(notification);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建通知失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取通知列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取通知列表")
    public Result<IPage<CourseNotification>> getNotificationList(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            IPage<CourseNotification> page = notificationService.getNotificationList(courseId, current, size);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的通知列表（分页）
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的通知列表")
    public Result<IPage<Map<String, Object>>> getMyNotifications(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            if (userId == null) {
                return Result.error("用户未登录");
            }
            IPage<Map<String, Object>> page = notificationService.getUserNotifications(userId, isRead, current, size);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取我的通知列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/read/{id}")
    @Operation(summary = "标记通知为已读")
    public Result<Void> markAsRead(@PathVariable Long id,
                                    @RequestHeader(value = "userId", required = false) Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户未登录");
            }
            notificationService.markAsRead(id, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("标记已读失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量标记为已读
     */
    @PutMapping("/read/all")
    @Operation(summary = "批量标记为已读")
    public Result<Void> markAllAsRead(@RequestHeader(value = "userId", required = false) Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户未登录");
            }
            notificationService.markAllAsRead(userId);
            return Result.success();
        } catch (Exception e) {
            log.error("批量标记已读失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知数量")
    public Result<Long> getUnreadCount(@RequestHeader(value = "userId", required = false) Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户未登录");
            }
            Long count = notificationService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未读数量失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除通知
     */
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取通知统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取通知统计信息")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> statistics = notificationService.getNotificationStatistics(courseId, startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取通知统计失败", e);
            return Result.error(e.getMessage());
        }
    }
}
