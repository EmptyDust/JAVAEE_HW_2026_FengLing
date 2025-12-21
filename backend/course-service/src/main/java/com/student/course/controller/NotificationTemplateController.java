package com.student.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.result.Result;
import com.student.course.entity.NotificationTemplate;
import com.student.course.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知模板控制器
 */
@Slf4j
@RestController
@RequestMapping("/notification/template")
@Tag(name = "通知模板管理", description = "通知模板相关接口")
public class NotificationTemplateController {

    @Autowired
    private NotificationTemplateService templateService;

    /**
     * 获取模板列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取模板列表")
    public Result<IPage<NotificationTemplate>> getTemplateList(
            @RequestParam(required = false) String templateType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            IPage<NotificationTemplate> page = templateService.getTemplateList(templateType, current, size);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有启用的模板
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有启用的模板")
    public Result<List<NotificationTemplate>> getAllTemplates() {
        try {
            List<NotificationTemplate> templates = templateService.getAllEnabledTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据类型获取模板
     */
    @GetMapping("/by-type/{type}")
    @Operation(summary = "根据类型获取模板")
    public Result<List<NotificationTemplate>> getTemplatesByType(@PathVariable String type) {
        try {
            List<NotificationTemplate> templates = templateService.getTemplatesByType(type);
            return Result.success(templates);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情")
    public Result<NotificationTemplate> getTemplate(@PathVariable Long id) {
        try {
            NotificationTemplate template = templateService.getTemplateById(id);
            return Result.success(template);
        } catch (Exception e) {
            log.error("获取模板详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建模板
     */
    @PostMapping("/create")
    @Operation(summary = "创建模板")
    public Result<NotificationTemplate> createTemplate(
            @RequestBody NotificationTemplate template,
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestHeader(value = "username", required = false) String username) {
        try {
            template.setCreateUserId(userId);
            template.setCreateUserName(username);
            NotificationTemplate result = templateService.createTemplate(template);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新模板
     */
    @PutMapping("/update")
    @Operation(summary = "更新模板")
    public Result<NotificationTemplate> updateTemplate(@RequestBody NotificationTemplate template) {
        try {
            NotificationTemplate result = templateService.updateTemplate(template);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 应用模板
     */
    @PostMapping("/apply/{id}")
    @Operation(summary = "应用模板")
    public Result<Map<String, String>> applyTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, String> variables) {
        try {
            Map<String, String> result = templateService.applyTemplate(id, variables);
            return Result.success(result);
        } catch (Exception e) {
            log.error("应用模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 初始化默认模板
     */
    @PostMapping("/init-default")
    @Operation(summary = "初始化默认模板")
    public Result<Void> initDefaultTemplates() {
        try {
            templateService.insertDefaultTemplates();
            return Result.success();
        } catch (Exception e) {
            log.error("初始化默认模板失败", e);
            return Result.error(e.getMessage());
        }
    }
}
