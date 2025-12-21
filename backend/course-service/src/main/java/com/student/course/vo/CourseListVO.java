package com.student.course.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程列表视图对象（用于选课中心）
 * 包含课程信息和选课状态
 */
@Data
public class CourseListVO {
    // 课程基本信息
    private Long id;
    private String courseCode;
    private String courseName;
    private String courseType;
    private BigDecimal credit;
    private Integer hours;
    private String teacherName;
    private String semester;
    private Integer maxStudents;
    private Integer enrolledStudents;
    private String scheduleInfo;
    private String courseDescription;
    private String courseOutline;
    private Integer status;
    private LocalDateTime createTime;

    // 学生选课状态
    private Boolean enrolled;  // 当前学生是否已选该课程
}
