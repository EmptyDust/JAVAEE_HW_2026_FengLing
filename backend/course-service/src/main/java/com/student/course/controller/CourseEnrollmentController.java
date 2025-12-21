package com.student.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.annotation.RequireRole;
import com.student.common.result.Result;
import com.student.course.entity.CourseEnrollment;
import com.student.course.service.CourseEnrollmentService;
import com.student.course.vo.MyEnrollmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 学生选课控制器
 */
@Slf4j
@Tag(name = "学生选课", description = "学生选课相关接口")
@RestController
@RequestMapping("/course/enrollment")
public class CourseEnrollmentController {

    @Autowired
    private CourseEnrollmentService enrollmentService;

    @Operation(summary = "学生选课")
    @PostMapping("/enroll")
    public Result<CourseEnrollment> enrollCourse(
            @Parameter(description = "课程ID") @RequestParam Long courseId,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long studentId,
            @Parameter(hidden = true) @RequestHeader(value = "username", required = false) String username,
            @Parameter(description = "学号") @RequestParam(required = false) String studentNumber,
            @Parameter(description = "班级ID") @RequestParam(required = false) Long classId,
            @Parameter(description = "班级名称") @RequestParam(required = false) String className) {

        // username 是学生姓名，从 header 传来
        CourseEnrollment enrollment = enrollmentService.enrollCourse(courseId, studentId, username,
                studentNumber, classId, className);
        return Result.success(enrollment);
    }

    @Operation(summary = "学生退课")
    @PostMapping("/drop/{enrollmentId}")
    public Result<Void> dropCourse(
            @PathVariable Long enrollmentId,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long studentId) {

        enrollmentService.dropCourse(enrollmentId, studentId);
        return Result.success();
    }

    @Operation(summary = "查询我的选课")
    @GetMapping("/my")
    public Result<IPage<MyEnrollmentVO>> getMyEnrollments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "课程名称/编号") @RequestParam(required = false) String courseName,
            @Parameter(description = "学期") @RequestParam(required = false) String semester,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long studentId) {

        IPage<MyEnrollmentVO> page = enrollmentService.getMyEnrollments(studentId, current, size, courseName, semester);
        return Result.success(page);
    }

    @Operation(summary = "查询课程的选课学生")
    @RequireRole({"admin", "teacher"})
    @GetMapping("/students/{courseId}")
    public Result<IPage<CourseEnrollment>> getCourseStudents(
            @PathVariable Long courseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size) {

        IPage<CourseEnrollment> page = enrollmentService.getCourseStudents(courseId, current, size);
        return Result.success(page);
    }

    @Operation(summary = "检查是否已选课")
    @GetMapping("/check")
    public Result<Boolean> checkEnrollment(
            @Parameter(description = "课程ID") @RequestParam Long courseId,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long studentId) {

        boolean enrolled = enrollmentService.isEnrolled(courseId, studentId);
        return Result.success(enrolled);
    }

    @Operation(summary = "录入成绩")
    @RequireRole({"admin", "teacher"})
    @PostMapping("/score")
    public Result<Void> updateScore(
            @Parameter(description = "选课记录ID") @RequestParam Long enrollmentId,
            @Parameter(description = "成绩") @RequestParam BigDecimal score,
            @Parameter(description = "等级") @RequestParam(required = false) String grade) {

        enrollmentService.updateScore(enrollmentId, score, grade);
        return Result.success();
    }
}
