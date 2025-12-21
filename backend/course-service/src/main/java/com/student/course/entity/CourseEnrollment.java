package com.student.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生选课实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_enrollment")
public class CourseEnrollment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNumber;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 选课时间
     */
    private LocalDateTime enrollmentTime;

    /**
     * 状态: 0=已退课 1=已选课 2=待审核
     */
    private Integer status;

    /**
     * 课程成绩
     */
    private BigDecimal score;

    /**
     * 等级: A/B/C/D/F
     */
    private String grade;

    /**
     * 出勤率
     */
    private BigDecimal attendanceRate;

    /**
     * 备注
     */
    private String remark;
}
