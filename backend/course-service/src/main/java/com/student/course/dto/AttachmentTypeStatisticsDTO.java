package com.student.course.dto;

import lombok.Data;

/**
 * 附件类型统计 DTO
 */
@Data
public class AttachmentTypeStatisticsDTO {

    /**
     * 附件类型
     */
    private String attachmentType;

    /**
     * 该类型的数量
     */
    private Long count;
}
