package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.course.entity.CourseCalendar;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程日历Mapper
 */
@Mapper
public interface CourseCalendarMapper extends BaseMapper<CourseCalendar> {
}
