package com.student.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.teacher.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教师Mapper接口
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}
