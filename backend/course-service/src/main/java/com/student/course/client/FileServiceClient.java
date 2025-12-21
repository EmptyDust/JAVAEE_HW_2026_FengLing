package com.student.course.client;

import com.student.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件服务Feign客户端
 */
@FeignClient(name = "file-service", path = "/file")
public interface FileServiceClient {

    /**
     * 上传文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<Map<String, Object>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "businessId", required = false) Long businessId,
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestHeader(value = "username", required = false) String username);

    /**
     * 获取文件信息
     */
    @GetMapping("/{id}")
    Result<Map<String, Object>> getFileInfo(@PathVariable("id") Long id);

    /**
     * 删除文件
     */
    @DeleteMapping("/delete/{id}")
    Result<Void> deleteFile(@PathVariable("id") Long id);
}
