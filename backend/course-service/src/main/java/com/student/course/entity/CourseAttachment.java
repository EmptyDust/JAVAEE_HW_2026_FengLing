package com.student.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程附件实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_attachment")
public class CourseAttachment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 文件ID(关联file_info表)
     */
    private Long fileId;

    /**
     * 附件类型: document/video/audio/other
     */
    private String attachmentType;

    /**
     * 附件名称
     */
    private String attachmentName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 是否已索引到ES: 0=否 1=是
     */
    private Integer esIndexed;

    /**
     * ES文档ID
     */
    private String esDocId;

    /**
     * 附件描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态: 0=删除 1=正常
     */
    private Integer status;

    /**
     * 上传人ID
     */
    private Long uploadUserId;

    /**
     * 上传人姓名
     */
    private String uploadUserName;
}
