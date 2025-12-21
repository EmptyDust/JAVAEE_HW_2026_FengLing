package com.student.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.annotation.RequireRole;
import com.student.common.result.Result;
import com.student.course.entity.CourseInfo;
import com.student.course.service.CourseInfoService;
import com.student.course.vo.CourseListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理控制器
 */
@Slf4j
@Tag(name = "课程管理", description = "课程信息管理相关接口")
@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseInfoService courseInfoService;

    @Operation(summary = "分页查询课程列表")
    @GetMapping("/list")
    public Result<IPage<CourseListVO>> getCourseList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "课程名称/编号") @RequestParam(required = false) String courseName,
            @Parameter(description = "课程类型") @RequestParam(required = false) String courseType,
            @Parameter(description = "开课学期") @RequestParam(required = false) String semester,
            @Parameter(description = "课程状态") @RequestParam(required = false) Integer status,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long userId) {

        // 使用优化后的方法，一次查询返回课程列表和选课状态
        IPage<CourseListVO> page = courseInfoService.getCourseListWithEnrollStatus(
                current, size, courseName, courseType, semester, status, userId);
        return Result.success(page);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{id}")
    public Result<CourseInfo> getCourseById(@PathVariable Long id) {
        CourseInfo course = courseInfoService.getCourseById(id);
        return Result.success(course);
    }

    @Operation(summary = "创建课程")
    @RequireRole({"admin", "teacher"})
    @PostMapping("/add")
    public Result<CourseInfo> createCourse(
            @RequestBody CourseInfo courseInfo,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "username", required = false) String username) {

        // 设置创建人信息
        courseInfo.setCreateUserId(userId);
        courseInfo.setCreateUserName(username);

        CourseInfo result = courseInfoService.createCourse(courseInfo);
        return Result.success(result);
    }

    @Operation(summary = "更新课程")
    @RequireRole({"admin", "teacher"})
    @PutMapping("/update")
    public Result<CourseInfo> updateCourse(@RequestBody CourseInfo courseInfo) {
        CourseInfo result = courseInfoService.updateCourse(courseInfo);
        return Result.success(result);
    }

    @Operation(summary = "删除课程")
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseInfoService.deleteCourse(id);
        return Result.success();
    }
}
