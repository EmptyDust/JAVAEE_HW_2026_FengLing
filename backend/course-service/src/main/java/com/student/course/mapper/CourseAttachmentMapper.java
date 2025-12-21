package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.course.dto.AttachmentStatisticsDTO;
import com.student.course.dto.AttachmentTypeStatisticsDTO;
import com.student.course.entity.CourseAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程附件Mapper
 */
@Mapper
public interface CourseAttachmentMapper extends BaseMapper<CourseAttachment> {

    /**
     * 获取附件统计信息（使用SQL聚合函数）
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "COALESCE(SUM(file_size), 0) as totalSize, " +
            "COALESCE(SUM(download_count), 0) as totalDownloads, " +
            "COALESCE(SUM(view_count), 0) as totalViews " +
            "FROM course_attachment " +
            "WHERE status = 1")
    AttachmentStatisticsDTO getStatistics();

    /**
     * 获取按类型统计的附件信息
     */
    @Select("SELECT " +
            "attachment_type as attachmentType, " +
            "COUNT(*) as count " +
            "FROM course_attachment " +
            "WHERE status = 1 " +
            "GROUP BY attachment_type")
    List<AttachmentTypeStatisticsDTO> getTypeStatistics();
}
