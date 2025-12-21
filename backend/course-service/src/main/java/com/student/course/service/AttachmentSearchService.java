package com.student.course.service;

import com.student.course.document.CourseAttachmentDocument;
import com.student.course.entity.CourseAttachment;
import com.student.course.entity.CourseInfo;
import com.student.course.mapper.CourseAttachmentMapper;
import com.student.course.repository.CourseAttachmentDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 附件搜索服务
 */
@Slf4j
@Service
public class AttachmentSearchService {

    @Autowired
    private CourseAttachmentDocumentRepository documentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private CourseAttachmentMapper attachmentMapper;

    @Autowired
    private CourseInfoService courseInfoService;

    @Autowired
    private DocumentContentExtractor contentExtractor;

    /**
     * 全文搜索（搜索附件名称和内容）
     *
     * @param keyword 搜索关键词
     * @param page    页码（从0开始）
     * @param size    每页大小
     * @return 搜索结果列表
     */
    public List<CourseAttachmentDocument> searchByKeyword(String keyword, int page, int size) {
        try {
            // 构建查询条件：在附件名称或内容中搜索关键词，并且状态为正常
            Criteria criteria = new Criteria("attachmentName").matches(keyword)
                    .or(new Criteria("content").matches(keyword));

            // 添加状态过滤：只搜索status=1的附件
            Criteria statusCriteria = new Criteria("status").is(1);
            criteria = criteria.and(statusCriteria);

            Query query = new CriteriaQuery(criteria);
            query.setPageable(PageRequest.of(page, size));

            // 执行搜索
            SearchHits<CourseAttachmentDocument> searchHits = elasticsearchOperations.search(query,
                    CourseAttachmentDocument.class);

            // 提取结果
            List<CourseAttachmentDocument> results = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            log.info("搜索完成: keyword={}, 结果数={}", keyword, results.size());
            return results;

        } catch (Exception e) {
            log.error("搜索失败: keyword=", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 按课程ID搜索附件
     *
     * @param courseId 课程ID
     * @return 搜索结果列表
     */
    public List<CourseAttachmentDocument> searchByCourseId(Long courseId) {
        try {
            return documentRepository.findByCourseId(courseId);
        } catch (Exception e) {
            log.error("按课程ID搜索失败: courseId={}", courseId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 高级搜索（支持多个条件）
     *
     * @param keyword  搜索关键词
     * @param courseId 课程ID（可选）
     * @param fileType 文件类型（可选）
     * @param page     页码（从0开始）
     * @param size     每页大小
     * @return 搜索结果列表
     */
    public List<CourseAttachmentDocument> advancedSearch(String keyword, Long courseId, String fileType, int page,
            int size) {
        try {
            // 构建查询条件
            Criteria criteria = null;

            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                criteria = new Criteria("attachmentName").matches(keyword)
                        .or(new Criteria("content").matches(keyword));
            }

            // 课程ID过滤
            if (courseId != null) {
                Criteria courseCriteria = new Criteria("courseId").is(courseId);
                criteria = criteria != null ? criteria.and(courseCriteria) : courseCriteria;
            }

            // 文件类型过滤
            if (fileType != null && !fileType.isEmpty()) {
                Criteria typeCriteria = new Criteria("fileType").is(fileType);
                criteria = criteria != null ? criteria.and(typeCriteria) : typeCriteria;
            }

            // 状态过滤：只搜索status=1的附件
            Criteria statusCriteria = new Criteria("status").is(1);
            criteria = criteria != null ? criteria.and(statusCriteria) : statusCriteria;

            // 如果没有任何条件，返回空列表
            if (criteria == null) {
                return new ArrayList<>();
            }

            Query query = new CriteriaQuery(criteria);
            query.setPageable(PageRequest.of(page, size));

            // 执行搜索
            SearchHits<CourseAttachmentDocument> searchHits = elasticsearchOperations.search(query,
                    CourseAttachmentDocument.class);

            // 提取结果
            List<CourseAttachmentDocument> results = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            log.info("高级搜索完成: keyword={}, courseId={}, fileType={}, 结果数={}",
                    keyword, courseId, fileType, results.size());
            return results;

        } catch (Exception e) {
            log.error("高级搜索失败: keyword={}, courseId={}, fileType={}",
                    keyword, courseId, fileType, e);
            return new ArrayList<>();
        }
    }

    /**
     * 重新索引所有附件
     * 为数据库中所有未索引的附件建立ES索引
     *
     * @return 成功索引的附件数量
     */
    public int reindexAllAttachments() {
        log.info("开始重新索引所有附件...");
        int successCount = 0;
        int failCount = 0;

        try {
            // 查询所有状态为1且未索引的附件
            List<CourseAttachment> attachments = attachmentMapper.selectList(null);
            log.info("找到 {} 个附件需要索引", attachments.size());

            for (CourseAttachment attachment : attachments) {
                try {
                    // 只处理文档类型的附件
                    if (!contentExtractor.isSupportedFileType(attachment.getAttachmentName())) {
                        log.debug("跳过不支持的文件类型: {}", attachment.getAttachmentName());
                        continue;
                    }

                    // 获取课程信息
                    CourseInfo courseInfo = courseInfoService.getCourseById(attachment.getCourseId());
                    String courseName = courseInfo != null ? courseInfo.getCourseName() : "";

                    // 创建ES文档（不提取内容，只索引文件名）
                    CourseAttachmentDocument document = new CourseAttachmentDocument();
                    document.setId(attachment.getId());
                    document.setAttachmentName(attachment.getAttachmentName());
                    document.setAttachmentType(attachment.getAttachmentType());
                    document.setFileId(attachment.getFileId());
                    document.setFileType(attachment.getFileExtension());
                    document.setContent(""); // 暂时不提取内容
                    document.setCourseId(attachment.getCourseId());
                    document.setCourseName(courseName);
                    document.setUploadTime(java.time.LocalDate.now().toString());
                    document.setUploadUser(attachment.getUploadUserName());
                    document.setFileSize(attachment.getFileSize());
                    document.setDownloadCount(attachment.getDownloadCount() != null ? attachment.getDownloadCount() : 0);
                    document.setViewCount(attachment.getViewCount() != null ? attachment.getViewCount() : 0);
                    document.setStatus(attachment.getStatus());

                    // 保存到ES
                    documentRepository.save(document);

                    // 更新索引状态
                    attachment.setEsIndexed(1);
                    attachmentMapper.updateById(attachment);

                    successCount++;
                    log.info("成功索引附件: id={}, name={}", attachment.getId(), attachment.getAttachmentName());

                } catch (Exception e) {
                    failCount++;
                    log.error("索引附件失败: id={}, name={}", attachment.getId(), attachment.getAttachmentName(), e);
                }
            }

            log.info("重新索引完成: 成功={}, 失败={}", successCount, failCount);
            return successCount;

        } catch (Exception e) {
            log.error("重新索引所有附件失败", e);
            return successCount;
        }
    }
}
