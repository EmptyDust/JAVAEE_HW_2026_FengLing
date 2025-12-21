package com.student.student.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.annotation.RequireRole;
import com.student.common.result.Result;
import com.student.student.entity.Student;
import com.student.student.service.StudentService;
import com.student.student.vo.StudentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@Tag(name = "学生管理", description = "学生信息管理相关接口")
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Operation(summary = "查询学生列表", description = "分页查询学生信息，支持按姓名和班级筛选。管理员和教师可查看所有学生")
    @RequireRole({"admin", "teacher"})
    @GetMapping("/list")
    public Result<IPage<StudentVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "学生姓名") @RequestParam(required = false) String name,
            @Parameter(description = "班级ID") @RequestParam(required = false) Long classId) {
        return Result.success(studentService.list(page, size, name, classId));
    }

    @Operation(summary = "获取学生自己的信息", description = "学生登录后获取自己的详细信息")
    @RequireRole({"student"})
    @GetMapping("/me")
    public Result<StudentVO> getMyInfo(@Parameter(hidden = true) @RequestHeader("studentId") Long studentId) {
        StudentVO student = studentService.getByIdWithClassName(studentId);
        if (student == null) {
            return Result.error("学生信息不存在");
        }
        return Result.success(student);
    }

    @Operation(summary = "添加学生", description = "新增学生信息。仅管理员和教师可操作")
    @RequireRole({"admin", "teacher"})
    @PostMapping("/add")
    public Result<?> add(@RequestBody Student student) {
        studentService.add(student);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新学生", description = "更新学生信息。管理员和教师可更新任意学生，学生只能更新自己的部分信息")
    @PutMapping("/update")
    public Result<?> update(
            @RequestBody Student student,
            @Parameter(hidden = true) @RequestHeader("userType") String userType,
            @Parameter(hidden = true) @RequestHeader(value = "studentId", required = false) Long currentStudentId) {

        // 学生只能更新自己的信息
        if ("student".equals(userType)) {
            if (currentStudentId == null || !currentStudentId.equals(student.getId())) {
                return Result.error("只能修改自己的信息");
            }
            // 学生只能修改部分字段（不能修改学号等关键信息）
            Student existing = studentService.getById(student.getId());
            if (existing == null) {
                return Result.error("学生信息不存在");
            }
            // 只允许修改电话和地址
            existing.setPhone(student.getPhone());
            existing.setAddress(student.getAddress());
            studentService.update(existing);
        } else {
            // 管理员和教师可以修改所有信息
            studentService.update(student);
        }

        return Result.success("更新成功");
    }

    @Operation(summary = "删除学生", description = "根据ID删除学生。仅管理员可操作")
    @RequireRole({"admin"})
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@Parameter(description = "学生ID") @PathVariable Long id) {
        studentService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "查询学生详情", description = "根据ID查询学生详细信息。管理员和教师可查看任意学生，学生只能查看自己")
    @GetMapping("/{id}")
    public Result<Student> getById(
            @Parameter(description = "学生ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("userType") String userType,
            @Parameter(hidden = true) @RequestHeader(value = "studentId", required = false) Long currentStudentId) {

        // 学生只能查看自己的信息
        if ("student".equals(userType)) {
            if (currentStudentId == null || !currentStudentId.equals(id)) {
                return Result.error("只能查看自己的信息");
            }
        }

        return Result.success(studentService.getById(id));
    }

    @Operation(summary = "上传学生头像", description = "上传学生头像图片。管理员和教师可为任意学生上传，学生只能上传自己的头像")
    @PostMapping("/upload-avatar")
    public Result<Map<String, Object>> uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "学生ID") @RequestParam("studentId") Long studentId,
            @Parameter(hidden = true) @RequestHeader("userType") String userType,
            @Parameter(hidden = true) @RequestHeader(value = "studentId", required = false) Long currentStudentId,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "username", required = false) String username) {

        // 学生只能上传自己的头像
        if ("student".equals(userType)) {
            if (currentStudentId == null || !currentStudentId.equals(studentId)) {
                return Result.error("只能上传自己的头像");
            }
        }

        Map<String, Object> result = studentService.uploadAvatar(file, studentId, userId, username);
        return Result.success(result);
    }
}
