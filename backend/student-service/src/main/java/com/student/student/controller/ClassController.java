package com.student.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.result.Result;
import com.student.student.entity.Class;
import com.student.student.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@Tag(name = "班级管理", description = "班级信息管理相关接口")
@RestController
@RequestMapping("/class")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Operation(summary = "查询班级列表", description = "分页查询班级信息")
    @GetMapping("/list")
    public Result<Page<Class>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(classService.list(page, size));
    }

    @Operation(summary = "添加班级", description = "新增班级信息")
    @PostMapping("/add")
    public Result<?> add(
            @Parameter(description = "班级信息") @RequestBody Class classEntity) {
        classService.add(classEntity);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新班级", description = "更新班级信息")
    @PutMapping("/update")
    public Result<?> update(
            @Parameter(description = "班级信息") @RequestBody Class classEntity) {
        classService.update(classEntity);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除班级", description = "根据ID删除班级")
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(
            @Parameter(description = "班级ID") @PathVariable Long id) {
        classService.delete(id);
        return Result.success("删除成功");
    }
}
