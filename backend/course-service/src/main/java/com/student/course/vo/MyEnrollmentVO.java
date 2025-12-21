package com.student.course.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的选课视图对象
 * 包含课程信息和选课信息
 */
@Data
public class MyEnrollmentVO {
    // 选课信息
    private Long enrollmentId;
    private LocalDateTime enrollmentTime;
    private BigDecimal score;
    private String grade;

    // 课程信息
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseType;
    private BigDecimal credit;
    private Integer hours;
    private String teacherName;
    private String semester;
    private String scheduleInfo;
    private String courseDescription;
    private String courseOutline;
}
