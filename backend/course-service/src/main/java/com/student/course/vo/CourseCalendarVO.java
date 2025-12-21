package com.student.course.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 课程日历视图对象
 * 包含周期性课程和特殊事件
 */
@Data
public class CourseCalendarVO {
    // 事件基本信息
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseName;

    // 事件类型: class(上课)/exam(考试)/homework(作业)/activity(活动)/cancel(停课)
    private String eventType;
    private String eventTitle;
    private String eventDescription;

    // 时间信息
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;

    // 周期性信息
    private Boolean isRecurring;  // 是否为周期性事件
    private String recurrenceRule;  // 重复规则

    // 其他信息
    private String color;
    private Integer status;  // 0=取消 1=正常 2=调整
}
