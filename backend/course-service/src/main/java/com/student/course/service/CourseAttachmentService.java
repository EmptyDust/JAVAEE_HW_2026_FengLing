package com.student.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.common.exception.BusinessException;
import com.student.course.document.CourseAttachmentDocument;
import com.student.course.entity.CourseAttachment;
import com.student.course.entity.CourseInfo;
import com.student.course.mapper.CourseAttachmentMapper;
import com.student.course.repository.CourseAttachmentDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.course.vo.AttachmentManagementVO;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程附件服务
 */
@Slf4j
@Service
public class CourseAttachmentService {

    @Autowired
    private CourseAttachmentMapper attachmentMapper;

    @Autowired
    private DocumentContentExtractor contentExtractor;

    @Autowired
    private CourseAttachmentDocumentRepository documentRepository;

    @Autowired
    private CourseInfoService courseInfoService;

    /**
     * 获取课程的所有附件
     */
    public List<CourseAttachment> getCourseAttachments(Long courseId) {
        LambdaQueryWrapper<CourseAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseAttachment::getCourseId, courseId)
               .eq(CourseAttachment::getStatus, 1)
               .orderByAsc(CourseAttachment::getSortOrder)
               .orderByDesc(CourseAttachment::getCreateTime);
        return attachmentMapper.selectList(wrapper);
    }

    /**
     * 创建课程附件记录
     * 前端需先调用file-service上传文件获取文件信息，再调用此方法创建附件记录
     */
    @Transactional(rollbackFor = Exception.class)
    public CourseAttachment createAttachment(Long courseId, Long fileId, String fileName,
                                             String filePath, Long fileSize, String mimeType,
                                             String description, Long userId, String username) {
        // 解析文件类型
        String extension = "";
        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }

        String attachmentType = getAttachmentType(extension);

        // 创建附件记录
        CourseAttachment attachment = new CourseAttachment();
        attachment.setCourseId(courseId);
        attachment.setFileId(fileId);
        attachment.setAttachmentType(attachmentType);
        attachment.setAttachmentName(fileName);
        attachment.setFilePath(filePath);
        attachment.setFileSize(fileSize);
        attachment.setFileExtension(extension);
        attachment.setMimeType(mimeType);
        attachment.setDescription(description);
        attachment.setDownloadCount(0);
        attachment.setViewCount(0);
        attachment.setEsIndexed(0);
        attachment.setSortOrder(0);
        attachment.setStatus(1);
        attachment.setUploadUserId(userId);
        attachment.setUploadUserName(username);

        attachmentMapper.insert(attachment);

        log.info("课程附件记录创建成功: courseId={}, fileName={}", courseId, fileName);
        return attachment;
    }

    /**
     * 根据文件扩展名判断附件类型
     */
    private String getAttachmentType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "other";
        }

        // 文档类型
        if (extension.matches("doc|docx|pdf|ppt|pptx|xls|xlsx|txt")) {
            return "document";
        }

        // 视频类型
        if (extension.matches("mp4|avi|mov|wmv|flv|mkv|webm")) {
            return "video";
        }

        // 音频类型
        if (extension.matches("mp3|wav|flac|aac|ogg|wma")) {
            return "audio";
        }

        // 图片类型
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp|svg")) {
            return "image";
        }

        return "other";
    }

    /**
     * 删除附件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long id) {
        CourseAttachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }

        // 标记为删除状态
        attachment.setStatus(0);
        attachmentMapper.updateById(attachment);

        // 删除Elasticsearch索引
        try {
            documentRepository.deleteById(id);
            log.info("删除Elasticsearch索引成功: attachmentId={}", id);
        } catch (Exception e) {
            log.error("删除Elasticsearch索引失败: attachmentId={}", id, e);
        }

        log.info("删除课程附件成功: id={}, fileName={}", id, attachment.getAttachmentName());
    }

    /**
     * 增加下载次数
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementDownloadCount(Long id) {
        CourseAttachment attachment = attachmentMapper.selectById(id);
        if (attachment != null) {
            attachment.setDownloadCount(
                    (attachment.getDownloadCount() == null ? 0 : attachment.getDownloadCount()) + 1
            );
            attachmentMapper.updateById(attachment);
        }
    }

    /**
     * 增加浏览次数
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        CourseAttachment attachment = attachmentMapper.selectById(id);
        if (attachment != null) {
            attachment.setViewCount(
                    (attachment.getViewCount() == null ? 0 : attachment.getViewCount()) + 1
            );
            attachmentMapper.updateById(attachment);
        }
    }

    /**
     * 异步建立Elasticsearch索引
     */
    @Async
    public void indexAttachmentAsync(CourseAttachment attachment, MultipartFile file) {
        try {
            String content = "";

            // 判断是否支持内容提取
            if (contentExtractor.isSupportedFileType(attachment.getAttachmentName())) {
                // 支持内容提取的文件，提取文档内容
                try (InputStream inputStream = file.getInputStream()) {
                    content = contentExtractor.extractContent(inputStream, attachment.getAttachmentName());
                }
            } else {
                // 不支持内容提取的文件（如图片、视频等），content为空，但仍然索引文件名
                log.info("文件类型不支持内容提取，仅索引文件名: {}", attachment.getAttachmentName());
            }

            // 获取课程信息
            CourseInfo courseInfo = courseInfoService.getCourseById(attachment.getCourseId());
            String courseName = courseInfo != null ? courseInfo.getCourseName() : "";

            // 创建Elasticsearch文档
            CourseAttachmentDocument document = new CourseAttachmentDocument();
            document.setId(attachment.getId());
            document.setAttachmentName(attachment.getAttachmentName());
            document.setAttachmentType(attachment.getAttachmentType());
            document.setFileId(attachment.getFileId());
            document.setFileType(attachment.getFileExtension());
            document.setContent(content);
            document.setCourseId(attachment.getCourseId());
            document.setCourseName(courseName);
            document.setUploadTime(java.time.LocalDate.now().toString());
            document.setUploadUser(attachment.getUploadUserName());
            document.setFileSize(attachment.getFileSize());
            document.setDownloadCount(attachment.getDownloadCount() != null ? attachment.getDownloadCount() : 0);
            document.setViewCount(attachment.getViewCount() != null ? attachment.getViewCount() : 0);
            document.setStatus(attachment.getStatus());

            // 保存到Elasticsearch
            documentRepository.save(document);

            // 更新索引状态
            attachment.setEsIndexed(1);
            attachmentMapper.updateById(attachment);

            log.info("Elasticsearch索引建立成功: attachmentId={}, fileName={}",
                    attachment.getId(), attachment.getAttachmentName());

        } catch (Exception e) {
            log.error("建立Elasticsearch索引失败: attachmentId={}, fileName={}",
                    attachment.getId(), attachment.getAttachmentName(), e);
        }
    }

    /**
     * 获取所有附件和统计信息（用于附件管理界面）
     * 一次性返回分页附件列表和统计信息，避免前端多次调用API
     */
    public AttachmentManagementVO getAllAttachmentsWithStatistics(
            Long courseId, String attachmentType, Integer current, Integer size) {

        AttachmentManagementVO result = new AttachmentManagementVO();

        // 1. 构建查询条件
        LambdaQueryWrapper<CourseAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseAttachment::getStatus, 1);

        if (courseId != null) {
            wrapper.eq(CourseAttachment::getCourseId, courseId);
        }

        if (attachmentType != null && !attachmentType.isEmpty()) {
            wrapper.eq(CourseAttachment::getAttachmentType, attachmentType);
        }

        wrapper.orderByDesc(CourseAttachment::getCreateTime);

        // 2. 分页查询附件列表
        Page<CourseAttachment> page = new Page<>(current, size);
        IPage<CourseAttachment> attachmentPage = attachmentMapper.selectPage(page, wrapper);
        result.setAttachments(attachmentPage);

        // 3. 使用SQL聚合函数计算统计信息（高性能）
        AttachmentManagementVO.Statistics statistics = new AttachmentManagementVO.Statistics();

        // 获取总体统计信息
        var statsDTO = attachmentMapper.getStatistics();
        if (statsDTO != null) {
            statistics.setTotalCount(statsDTO.getTotalCount());
            statistics.setTotalSize(statsDTO.getTotalSize());
            statistics.setTotalDownloads(statsDTO.getTotalDownloads());
            statistics.setTotalViews(statsDTO.getTotalViews());
        }

        // 获取按类型统计信息
        Map<String, Long> typeStats = new HashMap<>();
        var typeStatsList = attachmentMapper.getTypeStatistics();
        for (var typeStat : typeStatsList) {
            typeStats.put(typeStat.getAttachmentType(), typeStat.getCount());
        }
        statistics.setTypeStats(typeStats);

        result.setStatistics(statistics);

        log.info("获取附件管理数据成功: 总数={}, 当前页={}, 每页={}",
                statistics.getTotalCount(), current, size);

        return result;
    }
}
