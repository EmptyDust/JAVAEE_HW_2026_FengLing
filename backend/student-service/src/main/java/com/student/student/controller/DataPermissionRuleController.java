package com.student.student.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.annotation.RequireRole;
import com.student.common.entity.DataPermissionRuleEntity;
import com.student.common.result.Result;
import com.student.student.service.DataPermissionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据权限规则管理控制器
 */
@Slf4j
@Tag(name = "数据权限管理", description = "数据权限规则配置相关接口")
@RestController
@RequestMapping("/student/data-permission")
@RequireRole({"admin"})
public class DataPermissionRuleController {

    @Autowired
    private DataPermissionRuleService ruleService;

    @Operation(summary = "分页查询权限规则列表")
    @GetMapping("/list")
    public Result<IPage<DataPermissionRuleEntity>> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "角色类型") @RequestParam(required = false) String roleType,
            @Parameter(description = "表名") @RequestParam(required = false) String tableName) {

        IPage<DataPermissionRuleEntity> page = ruleService.list(current, size, roleType, tableName);
        return Result.success(page);
    }

    @Operation(summary = "获取权限规则详情")
    @GetMapping("/{id}")
    public Result<DataPermissionRuleEntity> getById(
            @Parameter(description = "规则ID") @PathVariable Long id) {

        DataPermissionRuleEntity entity = ruleService.getById(id);
        return Result.success(entity);
    }

    @Operation(summary = "添加权限规则")
    @PostMapping("/add")
    public Result<DataPermissionRuleEntity> add(@RequestBody DataPermissionRuleEntity entity) {
        DataPermissionRuleEntity result = ruleService.add(entity);
        return Result.success(result);
    }

    @Operation(summary = "更新权限规则")
    @PutMapping("/update")
    public Result<DataPermissionRuleEntity> update(@RequestBody DataPermissionRuleEntity entity) {
        DataPermissionRuleEntity result = ruleService.update(entity);
        return Result.success(result);
    }

    @Operation(summary = "删除权限规则")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@Parameter(description = "规则ID") @PathVariable Long id) {
        ruleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用权限规则")
    @PutMapping("/toggle/{id}")
    public Result<Void> toggle(@Parameter(description = "规则ID") @PathVariable Long id) {
        ruleService.toggle(id);
        return Result.success();
    }

    @Operation(summary = "刷新权限规则缓存")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        ruleService.refreshCache();
        return Result.success();
    }

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/roles")
    public Result<List<String>> getRoles() {
        List<String> roles = ruleService.getRoles();
        return Result.success(roles);
    }

    @Operation(summary = "获取所有表名列表")
    @GetMapping("/tables")
    public Result<List<String>> getTables() {
        List<String> tables = ruleService.getTables();
        return Result.success(tables);
    }
}
