package com.student.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.common.exception.BusinessException;
import com.student.file.entity.FileInfo;
import com.student.file.mapper.FileInfoMapper;
import com.student.file.strategy.FileStorageStrategy;
import com.student.file.util.FileValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Value("${file.storage-type}")
    private String storageType;

    @Resource(name = "localStorageStrategy")
    private FileStorageStrategy localStorageStrategy;

    @Resource(name = "minioStorageStrategy")
    private FileStorageStrategy minioStorageStrategy;

    /**
     * 获取当前存储策略
     */
    private FileStorageStrategy getStorageStrategy() {
        if ("minio".equalsIgnoreCase(storageType)) {
            return minioStorageStrategy;
        }
        return localStorageStrategy;
    }

    /**
     * 上传文件（已添加安全清理）
     */
    public FileInfo upload(MultipartFile file, String businessType, Long businessId,
            Long userId, String username) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        try {
            // 1. 清理原始文件名（防止XSS和路径遍历攻击）
            String originalName = file.getOriginalFilename();
            String sanitizedName = FileValidationUtil.sanitizeFileName(originalName);

            // 2. 生成存储文件名（使用UUID确保唯一性）
            String extension = "";
            if (sanitizedName != null && sanitizedName.contains(".")) {
                extension = sanitizedName.substring(sanitizedName.lastIndexOf("."));
            }
            String storageName = UUID.randomUUID().toString() + extension;

            // 3. 上传文件到存储系统
            FileStorageStrategy strategy = getStorageStrategy();
            String filePath = strategy.upload(file, storageName);
            String accessUrl = strategy.getAccessUrl(filePath);

            // 4. 保存文件信息到数据库（使用清理后的文件名）
            FileInfo fileInfo = new FileInfo();
            fileInfo.setOriginalName(sanitizedName);  // 使用清理后的文件名
            fileInfo.setStorageName(storageName);
            fileInfo.setFilePath(filePath);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(file.getContentType());
            fileInfo.setStorageType(storageType);
            fileInfo.setAccessUrl(accessUrl);
            fileInfo.setUploadUserId(userId);
            fileInfo.setUploadUserName(username);
            fileInfo.setBusinessType(businessType);
            fileInfo.setBusinessId(businessId);

            fileInfoMapper.insert(fileInfo);

            log.info("文件上传成功: {} -> {} (清理前: {})", sanitizedName, filePath, originalName);
            return fileInfo;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    public InputStream download(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }

        try {
            FileStorageStrategy strategy;
            if ("minio".equalsIgnoreCase(fileInfo.getStorageType())) {
                strategy = minioStorageStrategy;
            } else {
                strategy = localStorageStrategy;
            }
            return strategy.download(fileInfo.getFilePath());
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件指定范围（支持Range请求）
     */
    public InputStream downloadRange(Long fileId, long start, long end) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }

        try {
            FileStorageStrategy strategy;
            if ("minio".equalsIgnoreCase(fileInfo.getStorageType())) {
                strategy = minioStorageStrategy;
            } else {
                strategy = localStorageStrategy;
            }
            return strategy.downloadRange(fileInfo.getFilePath(), start, end);
        } catch (Exception e) {
            log.error("文件范围下载失败", e);
            throw new BusinessException("文件范围下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    public void delete(Long fileId, Long userId, String userType) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }

        // 管理员可以删除任何文件，其他人只能删除自己上传的文件
        boolean isAdmin = "admin".equals(userType);
        boolean isUploader = userId != null && userId.equals(fileInfo.getUploadUserId());

        if (!isAdmin && !isUploader) {
            throw new BusinessException("无权删除该文件");
        }

        try {
            // 删除物理文件
            FileStorageStrategy strategy;
            if ("minio".equalsIgnoreCase(fileInfo.getStorageType())) {
                strategy = minioStorageStrategy;
            } else {
                strategy = localStorageStrategy;
            }
            strategy.delete(fileInfo.getFilePath());

            // 删除数据库记录
            fileInfoMapper.deleteById(fileId);

            log.info("文件删除成功: {}", fileInfo.getOriginalName());
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询文件信息
     */
    public FileInfo getById(Long fileId) {
        return fileInfoMapper.selectById(fileId);
    }

    /**
     * 根据业务类型和业务ID查询文件列表
     */
    public List<FileInfo> listByBusiness(String businessType, Long businessId) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getBusinessType, businessType);
        wrapper.eq(FileInfo::getBusinessId, businessId);
        wrapper.orderByDesc(FileInfo::getCreateTime);
        return fileInfoMapper.selectList(wrapper);
    }

    /**
     * 根据用户ID查询文件列表
     */
    public List<FileInfo> listByUser(Long userId) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUploadUserId, userId);
        wrapper.orderByDesc(FileInfo::getCreateTime);
        return fileInfoMapper.selectList(wrapper);
    }
}
