package com.student.course.controller;

import com.student.common.result.Result;
import com.student.course.document.CourseAttachmentDocument;
import com.student.course.service.AttachmentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 附件搜索控制器
 */
@Slf4j
@Tag(name = "附件搜索", description = "课程附件全文搜索相关接口")
@RestController
@RequestMapping("/course/attachment/search")
public class AttachmentSearchController {

    @Autowired
    private AttachmentSearchService searchService;

    @Operation(summary = "全文搜索", description = "在附件名称和内容中搜索关键词")
    @GetMapping("/keyword")
    public Result<List<CourseAttachmentDocument>> searchByKeyword(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {

        List<CourseAttachmentDocument> results = searchService.searchByKeyword(keyword, page, size);
        return Result.success(results);
    }

    @Operation(summary = "按课程搜索", description = "搜索指定课程的所有附件")
    @GetMapping("/course/{courseId}")
    public Result<List<CourseAttachmentDocument>> searchByCourseId(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {

        List<CourseAttachmentDocument> results = searchService.searchByCourseId(courseId);
        return Result.success(results);
    }

    @Operation(summary = "高级搜索", description = "支持多个条件的高级搜索")
    @GetMapping("/advanced")
    public Result<List<CourseAttachmentDocument>> advancedSearch(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "文件类型") @RequestParam(required = false) String fileType,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {

        List<CourseAttachmentDocument> results = searchService.advancedSearch(keyword, courseId, fileType, page, size);
        return Result.success(results);
    }

    @Operation(summary = "重新索引所有附件", description = "为数据库中所有附件重新建立Elasticsearch索引")
    @PostMapping("/reindex")
    public Result<Integer> reindexAllAttachments() {
        log.info("收到重新索引请求");
        int count = searchService.reindexAllAttachments();
        return Result.success(count);
    }
}
