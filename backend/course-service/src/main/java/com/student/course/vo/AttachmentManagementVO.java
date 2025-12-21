package com.student.course.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.course.entity.CourseAttachment;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 附件管理VO - 包含附件列表和统计信息
 */
@Data
public class AttachmentManagementVO {

    /**
     * 附件分页列表
     */
    private IPage<CourseAttachment> attachments;

    /**
     * 统计信息
     */
    private Statistics statistics;

    @Data
    public static class Statistics {
        /**
         * 附件总数
         */
        private Long totalCount = 0L;

        /**
         * 总大小（字节）
         */
        private Long totalSize = 0L;

        /**
         * 总下载次数
         */
        private Long totalDownloads = 0L;

        /**
         * 总浏览次数
         */
        private Long totalViews = 0L;

        /**
         * 按类型统计
         */
        private Map<String, Long> typeStats = new HashMap<>();
    }
}
