package com.student.auth.controller;

import com.student.auth.service.AuthService;
import com.student.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@Tag(name = "用户管理", description = "用户信息管理相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的个人信息")
    @GetMapping("/profile")
    public Result<?> getProfile(@Parameter(hidden = true) @RequestHeader("userId") Long userId) {
        return Result.success(authService.getUserProfile(userId));
    }

    @Operation(summary = "更新用户信息", description = "更新当前用户的邮箱和电话")
    @PutMapping("/profile")
    public Result<?> updateProfile(
            @Parameter(hidden = true) @RequestHeader("userId") Long userId,
            @RequestBody Map<String, String> params) {
        authService.updateUserProfile(userId, params.get("email"), params.get("phone"));
        return Result.success("更新成功");
    }

    @Operation(summary = "修改密码", description = "修改当前用户密码")
    @PutMapping("/password")
    public Result<?> changePassword(
            @Parameter(hidden = true) @RequestHeader("userId") Long userId,
            @RequestBody Map<String, String> params) {
        authService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success("密码修改成功");
    }
}
