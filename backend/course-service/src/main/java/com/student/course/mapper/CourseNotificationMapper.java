package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.course.entity.CourseNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 课程通知Mapper
 */
@Mapper
public interface CourseNotificationMapper extends BaseMapper<CourseNotification> {

    /**
     * 批量增加通知的已读数量（基于子查询统计）
     *
     * 使用场景：批量标记已读时，一次性更新所有相关通知的 read_count
     *
     * @param userId 用户ID
     * @return 影响的行数
     */
    @Update("UPDATE course_notification cn " +
            "SET cn.read_count = cn.read_count + ( " +
            "    SELECT COUNT(*) " +
            "    FROM notification_receive nr " +
            "    WHERE nr.notification_id = cn.id " +
            "    AND nr.user_id = #{userId} " +
            "    AND nr.is_read = 0 " +
            "    AND nr.status = 1 " +
            "), " +
            "cn.update_time = NOW() " +
            "WHERE cn.id IN ( " +
            "    SELECT DISTINCT nr2.notification_id " +
            "    FROM notification_receive nr2 " +
            "    WHERE nr2.user_id = #{userId} " +
            "    AND nr2.is_read = 0 " +
            "    AND nr2.status = 1 " +
            ")")
    int batchIncrementReadCountByUserId(@Param("userId") Long userId);

    /**
     * 增加单个通知的已读数量（原子操作）
     *
     * @param notificationId 通知ID
     * @return 影响的行数
     */
    @Update("UPDATE course_notification " +
            "SET read_count = read_count + 1, " +
            "    update_time = NOW() " +
            "WHERE id = #{notificationId}")
    int incrementReadCount(@Param("notificationId") Long notificationId);
}
