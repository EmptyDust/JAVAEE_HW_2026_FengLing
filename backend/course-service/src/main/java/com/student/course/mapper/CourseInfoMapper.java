package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.course.entity.CourseInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 课程信息Mapper
 */
@Mapper
public interface CourseInfoMapper extends BaseMapper<CourseInfo> {

    /**
     * 原子性增加选课人数（防止超卖）
     * 使用 WHERE 条件确保不会超过最大人数
     *
     * @param courseId 课程ID
     * @return 影响的行数（1表示成功，0表示失败-已满员）
     */
    @Update("UPDATE course_info " +
            "SET enrolled_students = enrolled_students + 1, " +
            "    status = CASE WHEN enrolled_students + 1 >= max_students THEN 2 ELSE status END, " +
            "    update_time = NOW() " +
            "WHERE id = #{courseId} " +
            "AND enrolled_students < max_students " +
            "AND status = 1")
    int incrementEnrollmentAtomic(@Param("courseId") Long courseId);

    /**
     * 原子性减少选课人数
     *
     * @param courseId 课程ID
     * @return 影响的行数
     */
    @Update("UPDATE course_info " +
            "SET enrolled_students = CASE WHEN enrolled_students > 0 THEN enrolled_students - 1 ELSE 0 END, " +
            "    status = CASE WHEN status = 2 THEN 1 ELSE status END, " +
            "    update_time = NOW() " +
            "WHERE id = #{courseId}")
    int decrementEnrollmentAtomic(@Param("courseId") Long courseId);
}
