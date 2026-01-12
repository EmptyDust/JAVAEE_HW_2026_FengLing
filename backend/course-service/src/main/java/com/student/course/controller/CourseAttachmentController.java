package com.student.course.controller;

import com.student.common.result.Result;
import com.student.course.entity.CourseAttachment;
import com.student.course.service.CourseAttachmentService;
import com.student.course.vo.AttachmentManagementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程附件控制器
 */
@Slf4j
@Tag(name = "课程附件", description = "课程附件管理相关接口")
@RestController
@RequestMapping("/course/attachment")
public class CourseAttachmentController {

    @Autowired
    private CourseAttachmentService attachmentService;

    @Operation(summary = "获取课程附件列表")
    @GetMapping("/list/{courseId}")
    public Result<List<CourseAttachment>> getCourseAttachments(@PathVariable Long courseId) {
        List<CourseAttachment> attachments = attachmentService.getCourseAttachments(courseId);
        return Result.success(attachments);
    }

    @Operation(summary = "创建课程附件记录", description = "前端需先调用file-service上传文件获取文件信息")
    @PostMapping("/create")
    public Result<CourseAttachment> createAttachment(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId,
            @Parameter(description = "文件ID") @RequestParam("fileId") Long fileId,
            @Parameter(description = "文件名") @RequestParam("fileName") String fileName,
            @Parameter(description = "文件路径") @RequestParam("filePath") String filePath,
            @Parameter(description = "文件大小") @RequestParam("fileSize") Long fileSize,
            @Parameter(description = "MIME类型") @RequestParam("mimeType") String mimeType,
            @Parameter(description = "附件描述") @RequestParam(required = false) String description,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "username", required = false) String username) {

        CourseAttachment attachment = attachmentService.createAttachment(courseId, fileId, fileName,
                filePath, fileSize, mimeType, description, userId, username);
        return Result.success(attachment);
    }

    @Operation(summary = "删除课程附件")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return Result.success();
    }

    @Operation(summary = "记录附件下载")
    @PostMapping("/download/{id}")
    public Result<Void> recordDownload(@PathVariable Long id) {
        attachmentService.incrementDownloadCount(id);
        return Result.success();
    }

    @Operation(summary = "记录附件浏览")
    @PostMapping("/view/{id}")
    public Result<Void> recordView(@PathVariable Long id) {
        attachmentService.incrementViewCount(id);
        return Result.success();
    }

    @Operation(summary = "获取所有附件和统计信息（附件管理界面专用）")
    @GetMapping("/all")
    public Result<AttachmentManagementVO> getAllAttachmentsWithStatistics(
            @Parameter(description = "课程ID（可选）") @RequestParam(required = false) Long courseId,
            @Parameter(description = "附件类型（可选）") @RequestParam(required = false) String attachmentType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size) {

        AttachmentManagementVO result = attachmentService.getAllAttachmentsWithStatistics(
                courseId, attachmentType, current, size);
        return Result.success(result);
    }
}
