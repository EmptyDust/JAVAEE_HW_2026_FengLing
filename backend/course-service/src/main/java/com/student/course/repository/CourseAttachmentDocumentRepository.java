package com.student.course.repository;

import com.student.course.document.CourseAttachmentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课程附件Elasticsearch Repository
 */
@Repository
public interface CourseAttachmentDocumentRepository extends ElasticsearchRepository<CourseAttachmentDocument, Long> {

    /**
     * 根据课程ID查询附件文档
     */
    List<CourseAttachmentDocument> findByCourseId(Long courseId);

    /**
     * 根据附件名称搜索
     */
    List<CourseAttachmentDocument> findByAttachmentNameContaining(String keyword);

    /**
     * 根据内容搜索
     */
    List<CourseAttachmentDocument> findByContentContaining(String keyword);

    /**
     * 删除课程的所有附件文档
     */
    void deleteByCourseId(Long courseId);
}
