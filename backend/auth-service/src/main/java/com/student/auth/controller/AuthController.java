package com.student.auth.controller;

import com.student.auth.service.AuthService;
import com.student.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@Tag(name = "认证接口", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "用户注册", description = "新用户注册接口，默认注册为学生用户")
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) {
        authService.register(
                params.get("username"),
                params.get("password"),
                params.get("email"),
                params.get("phone")
        );
        return Result.success("注册成功");
    }

    @Operation(summary = "获取验证码", description = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<Map<String, Object>> getCaptcha() {
        return Result.success(authService.getCaptcha());
    }

    @Operation(summary = "用户登录", description = "用户名密码登录，需要验证码。登录成功后返回token、用户信息和用户类型")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = authService.login(
                params.get("username"),
                params.get("password"),
                params.get("uuid"),
                params.get("captcha")
        );
        return Result.success(result);
    }

    @Operation(summary = "用户登出", description = "退出登录")
    @PostMapping("/logout")
    public Result<?> logout(@Parameter(description = "用户ID") @RequestHeader("userId") Long userId) {
        authService.logout(userId);
        return Result.success("退出成功");
    }
}
