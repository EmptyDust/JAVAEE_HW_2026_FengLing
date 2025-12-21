package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.course.entity.CourseEnrollment;
import com.student.course.vo.MyEnrollmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学生选课Mapper
 */
@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {

    /**
     * 查询学生的选课列表(包含课程详细信息)
     */
    IPage<MyEnrollmentVO> selectMyEnrollmentsWithCourse(
            Page<?> page,
            @Param("studentId") Long studentId,
            @Param("courseName") String courseName,
            @Param("semester") String semester);
}
