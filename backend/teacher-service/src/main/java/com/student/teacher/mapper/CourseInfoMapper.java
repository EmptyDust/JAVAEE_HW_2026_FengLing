package com.student.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.teacher.entity.CourseInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程信息Mapper接口（用于查询教师的课程列表）
 */
@Mapper
public interface CourseInfoMapper extends BaseMapper<CourseInfo> {
}
