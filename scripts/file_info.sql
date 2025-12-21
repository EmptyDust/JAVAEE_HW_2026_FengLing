-- 创建文件信息表
CREATE TABLE IF NOT EXISTS `file_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `storage_name` VARCHAR(255) NOT NULL COMMENT '存储文件名(UUID)',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `file_type` VARCHAR(100) COMMENT '文件类型(MIME)',
  `storage_type` VARCHAR(20) NOT NULL COMMENT '存储类型: local, minio',
  `access_url` VARCHAR(500) COMMENT '访问URL',
  `upload_user_id` BIGINT COMMENT '上传用户ID',
  `upload_user_name` VARCHAR(50) COMMENT '上传用户名',
  `business_type` VARCHAR(50) COMMENT '业务类型: avatar, course, document等',
  `business_id` BIGINT COMMENT '关联业务ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_business` (`business_type`, `business_id`),
  INDEX `idx_user` (`upload_user_id`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';
