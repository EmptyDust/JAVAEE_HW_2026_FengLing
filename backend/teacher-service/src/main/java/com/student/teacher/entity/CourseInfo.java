package com.student.teacher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 课程信息实体（用于查询教师的课程列表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_info")
public class CourseInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程编号
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程类型: 必修/选修/公选
     */
    private String courseType;

    /**
     * 学分
     */
    private BigDecimal credit;

    /**
     * 学时
     */
    private Integer hours;

    /**
     * 授课教师ID
     */
    private Long teacherId;

    /**
     * 授课教师姓名
     */
    private String teacherName;

    /**
     * 开课学期
     */
    private String semester;

    /**
     * 最大选课人数
     */
    private Integer maxStudents;

    /**
     * 已选课人数
     */
    private Integer enrolledStudents;

    /**
     * 课程简介
     */
    private String courseDescription;

    /**
     * 课程大纲
     */
    private String courseOutline;

    /**
     * 上课时间地点
     */
    private String scheduleInfo;

    /**
     * 课程状态: 0=停用 1=启用 2=已满
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;
}
