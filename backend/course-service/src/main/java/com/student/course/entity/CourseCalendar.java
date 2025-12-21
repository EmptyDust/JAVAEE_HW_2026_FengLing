package com.student.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 课程日历实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_calendar")
public class CourseCalendar extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 事件类型: class/exam/homework/activity
     */
    private String eventType;

    /**
     * 事件标题
     */
    private String eventTitle;

    /**
     * 事件描述
     */
    private String eventDescription;

    /**
     * 事件日期
     */
    private LocalDate eventDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 地点
     */
    private String location;

    /**
     * 星期几: 1-7
     */
    private Integer weekDay;

    /**
     * 第几周
     */
    private Integer weekNumber;

    /**
     * 是否重复: 0=否 1=是
     */
    private Integer isRecurring;

    /**
     * 重复规则
     */
    private String recurrenceRule;

    /**
     * 日历颜色
     */
    private String color;

    /**
     * 状态: 0=取消 1=正常 2=调整
     */
    private Integer status;
}
