package com.student.teacher.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.annotation.RequireRole;
import com.student.common.result.Result;
import com.student.teacher.entity.CourseInfo;
import com.student.teacher.entity.Teacher;
import com.student.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 教师管理控制器
 */
@Tag(name = "教师管理", description = "教师信息管理相关接口")
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * 获取教师自己的信息
     * 关键：使用 @RequestHeader("teacherId") 获取当前教师ID，严禁使用Feign
     */
    @Operation(summary = "获取教师自己的信息", description = "教师登录后获取自己的详细信息")
    @RequireRole({"teacher"})
    @GetMapping("/me")
    public Result<Teacher> getMyInfo(@Parameter(hidden = true) @RequestHeader("teacherId") Long teacherId) {
        if (teacherId == null) {
            return Result.error("教师ID不能为空");
        }

        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null) {
            return Result.error("教师信息不存在");
        }

        return Result.success(teacher);
    }

    /**
     * 教师更新自己的信息（教师自助）
     * 只允许修改部分字段：电话、邮箱
     */
    @Operation(summary = "更新教师自己的信息", description = "教师只能修改自己的部分信息")
    @RequireRole({"teacher"})
    @PutMapping("/me")
    public Result<?> updateMyInfo(
            @RequestBody Teacher teacher,
            @Parameter(hidden = true) @RequestHeader("teacherId") Long teacherId) {

        if (teacherId == null || !teacherId.equals(teacher.getId())) {
            return Result.error("只能修改自己的信息");
        }

        // 教师只能修改部分字段（不能修改工号等关键信息）
        Teacher existing = teacherService.getById(teacherId);
        if (existing == null) {
            return Result.error("教师信息不存在");
        }

        // 只允许修改电话和邮箱
        existing.setPhone(teacher.getPhone());
        existing.setEmail(teacher.getEmail());
        teacherService.update(existing);

        return Result.success("更新成功");
    }

    /**
     * 查询教师列表（管理员）
     */
    @Operation(summary = "查询教师列表", description = "分页查询教师信息，支持按姓名和部门筛选")
    @RequireRole({"admin"})
    @GetMapping("/list")
    public Result<IPage<Teacher>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "教师姓名") @RequestParam(required = false) String teacherName,
            @Parameter(description = "部门") @RequestParam(required = false) String department) {
        return Result.success(teacherService.list(page, size, teacherName, department));
    }

    /**
     * 获取所有教师列表（用于下拉选择）
     */
    @Operation(summary = "获取所有教师列表", description = "获取所有启用状态的教师，用于下拉选择等场景")
    @RequireRole({"admin", "teacher"})
    @GetMapping("/all")
    public Result<java.util.List<Teacher>> getAllTeachers() {
        return Result.success(teacherService.getAllTeachers());
    }

    /**
     * 添加教师（前端编排第一步）
     */
    @Operation(summary = "添加教师", description = "新增教师信息，返回教师ID用于后续绑定用户")
    @RequireRole({"admin"})
    @PostMapping("/add")
    public Result<Teacher> add(@RequestBody Teacher teacher) {
        teacherService.add(teacher);
        return Result.success(teacher);
    }

    /**
     * 更新教师信息（管理员）
     */
    @Operation(summary = "更新教师", description = "更新教师信息，管理员可更新任意教师")
    @RequireRole({"admin"})
    @PutMapping("/update")
    public Result<?> update(@RequestBody Teacher teacher) {
        teacherService.update(teacher);
        return Result.success("更新成功");
    }

    /**
     * 删除教师（管理员）
     */
    @Operation(summary = "删除教师", description = "根据ID删除教师")
    @RequireRole({"admin"})
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@Parameter(description = "教师ID") @PathVariable Long id) {
        teacherService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 查询教师详情（管理员）
     */
    @Operation(summary = "查询教师详情", description = "根据ID查询教师详细信息")
    @RequireRole({"admin"})
    @GetMapping("/{id}")
    public Result<Teacher> getById(@Parameter(description = "教师ID") @PathVariable Long id) {
        return Result.success(teacherService.getById(id));
    }

    /**
     * 绑定用户ID（前端编排第三步）
     * 在创建教师和用户后，回写用户ID到教师记录
     */
    @Operation(summary = "绑定用户ID", description = "将用户ID绑定到教师记录，用于前端编排创建流程")
    @RequireRole({"admin"})
    @PutMapping("/{teacherId}/bind-user")
    public Result<?> bindUser(
            @Parameter(description = "教师ID") @PathVariable Long teacherId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {

        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null) {
            return Result.error("教师不存在");
        }

        teacher.setUserId(userId);
        teacherService.update(teacher);

        return Result.success("绑定成功");
    }

    /**
     * 更新教师头像
     */
    @Operation(summary = "更新教师头像", description = "更新教师头像信息。前端需先调用file-service上传文件获取fileId")
    @PostMapping("/update-avatar")
    public Result<Void> updateAvatar(
            @Parameter(description = "教师ID") @RequestParam("teacherId") Long teacherId,
            @Parameter(description = "文件ID") @RequestParam("fileId") Long fileId,
            @Parameter(hidden = true) @RequestHeader("userType") String userType,
            @Parameter(hidden = true) @RequestHeader(value = "teacherId", required = false) Long currentTeacherId) {

        // 教师只能更新自己的头像
        if ("teacher".equals(userType)) {
            if (currentTeacherId == null || !currentTeacherId.equals(teacherId)) {
                return Result.error("只能更新自己的头像");
            }
        }

        teacherService.updateAvatar(teacherId, fileId);
        return Result.success();
    }

    /**
     * 获取教师的课程列表
     */
    @Operation(summary = "获取教师的课程列表", description = "根据教师ID获取该教师授课的所有课程")
    @RequireRole({"admin", "teacher"})
    @GetMapping("/courses/{teacherId}")
    public Result<java.util.List<CourseInfo>> getTeacherCourses(
            @Parameter(description = "教师ID") @PathVariable Long teacherId) {
        return Result.success(teacherService.getTeacherCourses(teacherId));
    }
}
