package com.student.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_info")
public class FileInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名(UUID)
     */
    private String storageName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件类型(MIME)
     */
    private String fileType;

    /**
     * 存储类型: local, minio
     */
    private String storageType;

    /**
     * 访问URL
     */
    private String accessUrl;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 上传用户名
     */
    private String uploadUserName;

    /**
     * 业务类型: avatar(头像), course(课程附件), document(文档)等
     */
    private String businessType;

    /**
     * 关联业务ID
     */
    private Long businessId;
}
