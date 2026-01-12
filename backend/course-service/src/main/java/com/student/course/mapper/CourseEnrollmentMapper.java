package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.annotation.IgnoreDataPermission;
import com.student.course.entity.CourseEnrollment;
import com.student.course.vo.MyEnrollmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生选课Mapper
 */
@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {

        /**
         * 查询学生的选课列表(包含课程详细信息)
         */
        @IgnoreDataPermission(reason = "该方法已通过studentId参数正确过滤数据，无需数据权限拦截器额外过滤")
        IPage<MyEnrollmentVO> selectMyEnrollmentsWithCourse(
                        Page<?> page,
                        @Param("studentId") Long studentId,
                        @Param("courseName") String courseName,
                        @Param("semester") String semester);

        /**
         * 批量查询学生在指定课程中的选课记录
         */
        @IgnoreDataPermission(reason = "该方法已通过studentId参数正确过滤数据，无需数据权限拦截器额外过滤")
        List<CourseEnrollment> selectByStudentAndCourses(
                        @Param("studentId") Long studentId,
                        @Param("courseIds") List<Long> courseIds);

        /**
         * 检查学生是否已选某课程
         */
        @IgnoreDataPermission(reason = "该方法已通过studentId参数正确过滤数据，无需数据权限拦截器额外过滤")
        Long countByStudentAndCourse(
                        @Param("studentId") Long studentId,
                        @Param("courseId") Long courseId);

        IPage<CourseEnrollment> selectCourseStudentsWithDetails(Page<CourseEnrollment> page,
                        @Param("courseId") Long courseId);
}
