package com.student.file.controller;

import com.student.common.result.Result;
import com.student.file.entity.FileInfo;
import com.student.file.service.FileService;
import com.student.file.util.FileValidationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@Slf4j
@Tag(name = "文件管理", description = "文件上传下载相关接口")
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Operation(summary = "上传文件", description = "上传文件到服务器,支持本地存储和Minio对象存储")
    @PostMapping("/upload")
    public Result<FileInfo> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(required = false) String businessType,
            @Parameter(description = "业务ID") @RequestParam(required = false) Long businessId,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "username", required = false) String username) {

        // 1. 文件安全校验（大小、类型、文件名）
        FileValidationUtil.validateFile(file);

        // 2. 记录上传日志
        log.info("文件上传请求: filename={}, size={}, contentType={}, userId={}, businessType={}",
                file.getOriginalFilename(),
                FileValidationUtil.formatFileSize(file.getSize()),
                file.getContentType(),
                userId,
                businessType);

        // 3. 调用服务层上传文件
        FileInfo fileInfo = fileService.upload(file, businessType, businessId, userId, username);

        log.info("文件上传成功: fileId={}, storageName={}", fileInfo.getId(), fileInfo.getStorageName());
        return Result.success(fileInfo);
    }

    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(
            @Parameter(description = "文件ID") @PathVariable Long id) {
        try {
            FileInfo fileInfo = fileService.getById(id);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            InputStream inputStream = fileService.download(id);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileInfo.getFileType()));
            headers.setContentLength(fileInfo.getFileSize());

            // 处理文件名编码
            String encodedFilename = URLEncoder.encode(fileInfo.getOriginalName(), "UTF-8")
                    .replaceAll("\\+", "%20");
            headers.setContentDispositionFormData("attachment", encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
        } catch (UnsupportedEncodingException e) {
            log.error("文件名编码失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "删除文件", description = "根据文件ID删除文件")
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(
            @Parameter(description = "文件ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "userType", required = false) String userType) {
        fileService.delete(id, userId, userType);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取文件信息", description = "根据文件ID获取文件详细信息")
    @GetMapping("/{id}")
    public Result<FileInfo> getById(@Parameter(description = "文件ID") @PathVariable Long id) {
        FileInfo fileInfo = fileService.getById(id);
        if (fileInfo == null) {
            return Result.error("文件不存在");
        }
        return Result.success(fileInfo);
    }

    @Operation(summary = "查询业务文件列表", description = "根据业务类型和业务ID查询关联的文件列表")
    @GetMapping("/list/business")
    public Result<List<FileInfo>> listByBusiness(
            @Parameter(description = "业务类型") @RequestParam String businessType,
            @Parameter(description = "业务ID") @RequestParam Long businessId) {
        List<FileInfo> list = fileService.listByBusiness(businessType, businessId);
        return Result.success(list);
    }

    @Operation(summary = "查询用户文件列表", description = "查询当前用户上传的所有文件")
    @GetMapping("/list/my")
    public Result<List<FileInfo>> listByUser(
            @Parameter(hidden = true) @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error("用户未登录");
        }
        List<FileInfo> list = fileService.listByUser(userId);
        return Result.success(list);
    }

    @Operation(summary = "流式传输文件", description = "支持Range请求的文件流式传输，用于音视频在线播放")
    @GetMapping("/stream/{id}")
    public ResponseEntity<Resource> stream(
            @Parameter(description = "文件ID") @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            FileInfo fileInfo = fileService.getById(id);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            long fileSize = fileInfo.getFileSize();

            // 解析Range请求头
            long start = 0;
            long end = fileSize - 1;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                try {
                    start = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        end = Long.parseLong(ranges[1]);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid range header: {}", rangeHeader);
                }
            }

            // 确保范围有效
            if (start > end || start < 0 || end >= fileSize) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            // 使用 downloadRange 方法直接获取指定范围的数据（使用 RandomAccessFile，高效）
            InputStream inputStream = fileService.downloadRange(id, start, end);
            long contentLength = end - start + 1;

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileInfo.getFileType()));
            headers.setContentLength(contentLength);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);

            // 如果是Range请求，返回206 Partial Content
            HttpStatus status = rangeHeader != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;

            // 创建限制长度的InputStreamResource
            InputStreamResource resource = new InputStreamResource(inputStream) {
                @Override
                public long contentLength() {
                    return contentLength;
                }
            };

            return ResponseEntity.status(status)
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("文件流式传输失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
