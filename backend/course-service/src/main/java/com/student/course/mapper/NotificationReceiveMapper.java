package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.common.annotation.IgnoreDataPermission;
import com.student.course.entity.NotificationReceive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知接收记录Mapper
 */
@Mapper
public interface NotificationReceiveMapper extends BaseMapper<NotificationReceive> {

    /**
     * 获取用户未读通知数量
     */
    @IgnoreDataPermission(reason = "notification_receive表使用userId字段，不需要student_id/teacher_id过滤")
    Long countUnreadByUserId(@Param("userId") Long userId);
}
