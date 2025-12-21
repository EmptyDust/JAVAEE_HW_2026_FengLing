package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.course.entity.CourseNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程通知Mapper
 */
@Mapper
public interface CourseNotificationMapper extends BaseMapper<CourseNotification> {
}
