package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.course.entity.TeacherInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教师信息Mapper
 */
@Mapper
public interface TeacherInfoMapper extends BaseMapper<TeacherInfo> {
}
