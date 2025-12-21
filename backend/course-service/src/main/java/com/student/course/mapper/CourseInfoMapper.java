package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.course.entity.CourseInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程信息Mapper
 */
@Mapper
public interface CourseInfoMapper extends BaseMapper<CourseInfo> {
}
