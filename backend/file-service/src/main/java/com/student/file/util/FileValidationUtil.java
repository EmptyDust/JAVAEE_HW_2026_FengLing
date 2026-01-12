package com.student.file.util;

import com.student.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 文件校验工具类
 *
 * 功能：
 * 1. 文件大小校验
 * 2. 文件类型白名单校验
 * 3. 文件名安全清理
 */
@Slf4j
public class FileValidationUtil {

    /**
     * 最大文件大小：100MB
     */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024L;

    /**
     * 允许的文件类型（MimeType白名单）
     *
     * 包括：
     * - 图片：JPEG, PNG, GIF, BMP, WebP
     * - 文档：PDF, Word, Excel, PowerPoint, TXT
     * - 压缩包：ZIP, RAR
     */
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
            // 图片类型
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp",
            "image/svg+xml",

            // PDF文档
            "application/pdf",

            // Microsoft Office 文档
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-powerpoint", // .ppt
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx

            // 文本文件
            "text/plain",
            "text/csv",

            // 压缩文件
            "application/zip",
            "application/x-zip-compressed",
            "application/x-rar-compressed",
            "application/x-7z-compressed"));

    /**
     * 允许的文件扩展名（作为MimeType的补充校验）
     */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            // 图片
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg",
            // 文档
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv",
            // 压缩包
            ".zip", ".rar", ".7z"));

    /**
     * 危险文件扩展名黑名单（即使MimeType通过也要拒绝）
     */
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".exe", ".bat", ".cmd", ".sh", ".ps1", ".vbs", ".js", ".jar", ".war",
            ".jsp", ".php", ".asp", ".aspx", ".py", ".rb", ".pl", ".cgi"));

    /**
     * 文件名非法字符正则（用于清理文件名）
     */
    private static final Pattern ILLEGAL_FILENAME_PATTERN = Pattern.compile("[\\\\/:*?\"<>|\\x00-\\x1F]");

    /**
     * 校验文件（综合校验）
     *
     * @param file 上传的文件
     * @throws BusinessException 校验失败时抛出
     */
    public static void validateFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file);
        validateFileType(file);
        validateFileName(file);
    }

    /**
     * 校验文件是否为空
     */
    public static void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
    }

    /**
     * 校验文件大小
     *
     * @param file 上传的文件
     * @throws BusinessException 文件过大时抛出
     */
    public static void validateFileSize(MultipartFile file) {
        long fileSize = file.getSize();

        if (fileSize <= 0) {
            throw new BusinessException("文件大小异常");
        }

        if (fileSize > MAX_FILE_SIZE) {
            String maxSizeMB = String.format("%.2f", MAX_FILE_SIZE / 1024.0 / 1024.0);
            String actualSizeMB = String.format("%.2f", fileSize / 1024.0 / 1024.0);
            throw new BusinessException(
                    String.format("文件大小超过限制，最大允许 %s MB，当前文件 %s MB", maxSizeMB, actualSizeMB));
        }

        log.debug("文件大小校验通过: {} bytes", fileSize);
    }

    /**
     * 校验文件类型（MimeType + 扩展名双重校验）
     *
     * @param file 上传的文件
     * @throws BusinessException 文件类型不允许时抛出
     */
    public static void validateFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        // 1. 校验文件名是否存在
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        // 2. 提取文件扩展名
        String extension = getFileExtension(originalFilename).toLowerCase();

        // 3. 黑名单校验（优先级最高）
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            log.warn("检测到危险文件类型: filename={}, extension=", originalFilename, extension);
            throw new BusinessException("不允许上传可执行文件或脚本文件");
        }

        // 4. 扩展名白名单校验
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("文件扩展名不在白名单中: filename={}, extension={}", originalFilename, extension);
            throw new BusinessException(
                    String.format("不支持的文件类型: %s，仅支持图片、PDF和常用文档格式", extension));
        }

        // 5. MimeType白名单校验
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("文件MimeType不在白名单中: filename={}, contentType={}", originalFilename, contentType);
            throw new BusinessException(
                    String.format("不支持的文件类型: %s，仅支持图片、PDF和常用文档格式", contentType));
        }

        log.debug("文件类型校验通过: filename={}, extension={}, contentType={}",
                originalFilename, extension, contentType);
    }

    /**
     * 校验文件名安全性
     *
     * @param file 上传的文件
     * @throws BusinessException 文件名不安全时抛出
     */
    public static void validateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            log.warn("检测到路径遍历攻击: filename={}", originalFilename);
            throw new BusinessException("文件名包含非法字符");
        }

        if (originalFilename.length() > 255) {
            throw new BusinessException("文件名过长，最多255个字符");
        }

        log.debug("文件名安全校验通过: {}", originalFilename);
    }

    /**
     * 清理文件名（移除非法字符，防止XSS和路径遍历）
     *
     * @param originalFilename 原始文件名
     * @return 清理后的安全文件名
     */
    public static String sanitizeFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "unnamed_file";
        }

        String sanitized = ILLEGAL_FILENAME_PATTERN.matcher(originalFilename).replaceAll("_");

        sanitized = sanitized.replace("..", "_");

        sanitized = sanitized.trim().replaceAll("^\\.+", "").replaceAll("\\.+$", "");

        if (sanitized.length() > 200) {
            String extension = getFileExtension(sanitized);
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
            sanitized = nameWithoutExt.substring(0, 200 - extension.length()) + extension;
        }

        if (sanitized.isEmpty()) {
            sanitized = "unnamed_file";
        }

        return sanitized;
    }

    /**
     * 获取文件扩展名（包含点号）
     *
     * @param filename 文件名
     * @return 扩展名（如 ".jpg"），如果没有扩展名则返回空字符串
     */
    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 格式化文件大小为可读字符串
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串（如 "1.5 MB"）
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / 1024.0 / 1024.0);
        } else {
            return String.format("%.2f GB", size / 1024.0 / 1024.0 / 1024.0);
        }
    }
}
