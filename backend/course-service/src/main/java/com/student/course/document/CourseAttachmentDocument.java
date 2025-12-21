package com.student.course.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 课程附件Elasticsearch文档
 */
@Data
@Document(indexName = "course_attachments")
public class CourseAttachmentDocument {

    /**
     * 文档ID（对应附件ID）
     */
    @Id
    private Long id;

    /**
     * 附件名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String attachmentName;

    /**
     * 附件类型（document/video/audio/other）
     */
    @Field(type = FieldType.Keyword)
    private String attachmentType;

    /**
     * 文件ID（用于下载）
     */
    @Field(type = FieldType.Long)
    private Long fileId;

    /**
     * 文件扩展名
     */
    @Field(type = FieldType.Keyword)
    private String fileType;

    /**
     * 文档内容（提取的文本）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 课程ID
     */
    @Field(type = FieldType.Long)
    private Long courseId;

    /**
     * 课程名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String courseName;

    /**
     * 上传时间
     */
    @Field(type = FieldType.Keyword)
    private String uploadTime;

    /**
     * 上传用户
     */
    @Field(type = FieldType.Keyword)
    private String uploadUser;

    /**
     * 文件大小
     */
    @Field(type = FieldType.Long)
    private Long fileSize;

    /**
     * 下载次数
     */
    @Field(type = FieldType.Integer)
    private Integer downloadCount;

    /**
     * 浏览次数
     */
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /**
     * 状态: 0=删除 1=正常
     */
    @Field(type = FieldType.Integer)
    private Integer status;
}
