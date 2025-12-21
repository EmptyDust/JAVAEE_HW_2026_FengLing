package com.student.student.controller;

import com.student.common.result.Result;
import com.student.student.entity.Dict;
import com.student.student.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Tag(name = "数据字典", description = "数据字典管理相关接口")
@RestController
@RequestMapping("/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @Operation(summary = "查询字典数据", description = "根据字典类型查询字典数据列表，如性别、年级等")
    @GetMapping("/list/{dictType}")
    public Result<List<Dict>> getByType(
            @Parameter(description = "字典类型，如：gender（性别）、grade（年级）") @PathVariable String dictType) {
        return Result.success(dictService.getByType(dictType));
    }
}
