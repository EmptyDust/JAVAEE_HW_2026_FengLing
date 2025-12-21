package com.student.course.dto;

import lombok.Data;

/**
 * 附件统计数据 DTO
 */
@Data
public class AttachmentStatisticsDTO {

    /**
     * 附件总数
     */
    private Long totalCount;

    /**
     * 总大小（字节）
     */
    private Long totalSize;

    /**
     * 总下载次数
     */
    private Long totalDownloads;

    /**
     * 总浏览次数
     */
    private Long totalViews;
}
