package com.student.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_template")
public class NotificationTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型: announcement/homework/exam/cancel
     */
    private String templateType;

    /**
     * 标题模板（支持变量：{courseName}, {teacherName}, {date}, {time}等）
     */
    private String titleTemplate;

    /**
     * 内容模板（支持变量）
     */
    private String contentTemplate;

    /**
     * 变量说明（JSON格式）
     */
    private String variables;

    /**
     * 状态: 0=停用 1=启用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;
}
