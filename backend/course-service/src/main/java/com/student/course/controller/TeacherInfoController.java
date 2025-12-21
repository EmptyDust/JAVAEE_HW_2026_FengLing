package com.student.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.annotation.RequireRole;
import com.student.common.result.Result;
import com.student.course.entity.CourseInfo;
import com.student.course.entity.TeacherInfo;
import com.student.course.service.TeacherInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 教师信息控制器
 */
@Slf4j
@Tag(name = "教师管理", description = "教师信息管理相关接口")
@RequireRole({"admin"})
@RestController
@RequestMapping("/teacher")
public class TeacherInfoController {

    @Autowired
    private TeacherInfoService teacherInfoService;

    @Operation(summary = "分页查询教师列表")
    @GetMapping("/list")
    public Result<IPage<TeacherInfo>> getTeacherList(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "关键词（工号或姓名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "部门") @RequestParam(required = false) String department,
            @Parameter(description = "职称") @RequestParam(required = false) String title) {

        IPage<TeacherInfo> page = teacherInfoService.getTeacherList(current, size, keyword, department, title);
        return Result.success(page);
    }

    @Operation(summary = "获取所有教师列表（用于下拉选择）")
    @GetMapping("/all")
    public Result<List<TeacherInfo>> getAllTeachers() {
        List<TeacherInfo> teachers = teacherInfoService.getAllTeachers();
        return Result.success(teachers);
    }

    @Operation(summary = "获取教师详情")
    @GetMapping("/{id}")
    public Result<TeacherInfo> getTeacherById(@PathVariable Long id) {
        TeacherInfo teacher = teacherInfoService.getTeacherById(id);
        return Result.success(teacher);
    }

    @Operation(summary = "添加教师")
    @PostMapping("/add")
    public Result<TeacherInfo> addTeacher(@RequestBody TeacherInfo teacher) {
        TeacherInfo result = teacherInfoService.addTeacher(teacher);
        return Result.success(result);
    }

    @Operation(summary = "更新教师")
    @PutMapping("/update")
    public Result<TeacherInfo> updateTeacher(@RequestBody TeacherInfo teacher) {
        TeacherInfo result = teacherInfoService.updateTeacher(teacher);
        return Result.success(result);
    }

    @Operation(summary = "删除教师")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteTeacher(@PathVariable Long id) {
        teacherInfoService.deleteTeacher(id);
        return Result.success();
    }

    @Operation(summary = "上传教师头像")
    @PostMapping("/avatar/upload")
    public Result<TeacherInfo> uploadAvatar(
            @Parameter(description = "教师ID") @RequestParam Long teacherId,
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {

        TeacherInfo teacher = teacherInfoService.uploadAvatar(teacherId, file);
        return Result.success(teacher);
    }

    @Operation(summary = "获取教师的课程列表")
    @GetMapping("/courses/{teacherId}")
    public Result<List<CourseInfo>> getTeacherCourses(@PathVariable Long teacherId) {
        List<CourseInfo> courses = teacherInfoService.getTeacherCourses(teacherId);
        return Result.success(courses);
    }
}
