-- ===================================================================
-- 课程管理系统数据库表
-- ===================================================================

USE student_system;

-- -------------------------------------------------------------------
-- 1. 课程信息表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `course_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_code` VARCHAR(50) NOT NULL COMMENT '课程编号',
  `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `course_type` VARCHAR(20) COMMENT '课程类型: 必修/选修/公选',
  `credit` DECIMAL(3,1) COMMENT '学分',
  `hours` INT COMMENT '学时',
  `teacher_id` BIGINT COMMENT '授课教师ID',
  `teacher_name` VARCHAR(50) COMMENT '授课教师姓名',
  `semester` VARCHAR(20) COMMENT '开课学期: 2024-2025-1',
  `max_students` INT DEFAULT 50 COMMENT '最大选课人数',
  `enrolled_students` INT DEFAULT 0 COMMENT '已选课人数',
  `course_description` TEXT COMMENT '课程简介',
  `course_outline` TEXT COMMENT '课程大纲',
  `schedule_info` VARCHAR(200) COMMENT '上课时间地点: 周一1-2节/A101',
  `status` TINYINT DEFAULT 1 COMMENT '课程状态: 0=停用 1=启用 2=已满',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `create_user_name` VARCHAR(50) COMMENT '创建人姓名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_code` (`course_code`),
  INDEX `idx_teacher` (`teacher_id`),
  INDEX `idx_semester` (`semester`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程信息表';

-- -------------------------------------------------------------------
-- 2. 课程附件表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `course_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `file_id` BIGINT NOT NULL COMMENT '文件ID(关联file_info表)',
  `attachment_type` VARCHAR(20) NOT NULL COMMENT '附件类型: document/video/audio/other',
  `attachment_name` VARCHAR(255) NOT NULL COMMENT '附件名称',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_size` BIGINT COMMENT '文件大小(字节)',
  `file_extension` VARCHAR(20) COMMENT '文件扩展名',
  `mime_type` VARCHAR(100) COMMENT 'MIME类型',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `es_indexed` TINYINT DEFAULT 0 COMMENT '是否已索引到ES: 0=否 1=是',
  `es_doc_id` VARCHAR(100) COMMENT 'ES文档ID',
  `description` VARCHAR(500) COMMENT '附件描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0=删除 1=正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `upload_user_id` BIGINT COMMENT '上传人ID',
  `upload_user_name` VARCHAR(50) COMMENT '上传人姓名',
  PRIMARY KEY (`id`),
  INDEX `idx_course_id` (`course_id`),
  INDEX `idx_file_id` (`file_id`),
  INDEX `idx_type` (`attachment_type`),
  INDEX `idx_es_indexed` (`es_indexed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程附件表';

-- -------------------------------------------------------------------
-- 3. 学生选课表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `course_enrollment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '选课记录ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `student_name` VARCHAR(50) COMMENT '学生姓名',
  `student_number` VARCHAR(50) COMMENT '学号',
  `class_id` BIGINT COMMENT '班级ID',
  `class_name` VARCHAR(100) COMMENT '班级名称',
  `enrollment_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0=已退课 1=已选课 2=待审核',
  `score` DECIMAL(5,2) COMMENT '课程成绩',
  `grade` VARCHAR(10) COMMENT '等级: A/B/C/D/F',
  `attendance_rate` DECIMAL(5,2) COMMENT '出勤率',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_student` (`course_id`, `student_id`, `status`),
  INDEX `idx_student` (`student_id`),
  INDEX `idx_course` (`course_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课表';

-- -------------------------------------------------------------------
-- 4. 课程通知表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `course_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `notification_type` VARCHAR(20) NOT NULL COMMENT '通知类型: announcement/homework/exam/cancel',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `priority` TINYINT DEFAULT 0 COMMENT '优先级: 0=普通 1=重要 2=紧急',
  `send_method` VARCHAR(50) COMMENT '推送方式: websocket/sms/email',
  `target_type` VARCHAR(20) DEFAULT 'enrolled' COMMENT '目标类型: enrolled=已选课学生/all=所有学生',
  `send_status` TINYINT DEFAULT 0 COMMENT '发送状态: 0=待发送 1=发送中 2=已发送 3=失败',
  `send_time` DATETIME COMMENT '发送时间',
  `send_count` INT DEFAULT 0 COMMENT '发送数量',
  `read_count` INT DEFAULT 0 COMMENT '已读数量',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0=删除 1=正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `create_user_name` VARCHAR(50) COMMENT '创建人姓名',
  PRIMARY KEY (`id`),
  INDEX `idx_course_id` (`course_id`),
  INDEX `idx_type` (`notification_type`),
  INDEX `idx_send_status` (`send_status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程通知表';

-- -------------------------------------------------------------------
-- 5. 通知接收记录表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `notification_receive` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `notification_id` BIGINT NOT NULL COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `user_name` VARCHAR(50) COMMENT '接收用户姓名',
  `receive_method` VARCHAR(20) COMMENT '接收方式: websocket/sms/email',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0=未读 1=已读',
  `read_time` DATETIME COMMENT '阅读时间',
  `receive_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0=删除 1=正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_user` (`notification_id`, `user_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_read` (`is_read`),
  INDEX `idx_receive_time` (`receive_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知接收记录表';

-- -------------------------------------------------------------------
-- 6. 课程日历表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `course_calendar` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日历ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `event_type` VARCHAR(20) NOT NULL COMMENT '事件类型: class/exam/homework/activity',
  `event_title` VARCHAR(200) NOT NULL COMMENT '事件标题',
  `event_description` TEXT COMMENT '事件描述',
  `event_date` DATE NOT NULL COMMENT '事件日期',
  `start_time` TIME COMMENT '开始时间',
  `end_time` TIME COMMENT '结束时间',
  `location` VARCHAR(200) COMMENT '地点',
  `week_day` TINYINT COMMENT '星期几: 1-7',
  `week_number` INT COMMENT '第几周',
  `is_recurring` TINYINT DEFAULT 0 COMMENT '是否重复: 0=否 1=是',
  `recurrence_rule` VARCHAR(200) COMMENT '重复规则',
  `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '日历颜色',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0=取消 1=正常 2=调整',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_course_id` (`course_id`),
  INDEX `idx_event_date` (`event_date`),
  INDEX `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程日历表';

-- -------------------------------------------------------------------
-- 初始化测试数据
-- -------------------------------------------------------------------

-- 插入测试课程
INSERT INTO `course_info` (`course_code`, `course_name`, `course_type`, `credit`, `hours`, `teacher_id`, `teacher_name`, `semester`, `max_students`, `course_description`, `schedule_info`, `status`, `create_user_id`, `create_user_name`)
VALUES
('CS101', 'JavaEE企业级开发', '必修', 4.0, 64, 1001, '张教授', '2024-2025-2', 50, 'JavaEE企业级应用开发技术，包括Spring Boot、微服务、分布式系统等', '周一1-2节/A101，周三3-4节/A101', 1, 1, 'admin'),
('CS102', 'Web前端开发', '必修', 3.0, 48, 1002, '李老师', '2024-2025-2', 45, 'Vue3、React等现代前端框架开发', '周二1-2节/B203，周四3-4节/B203', 1, 1, 'admin'),
('CS103', '数据库原理与应用', '必修', 3.5, 56, 1003, '王教授', '2024-2025-2', 50, 'MySQL、Redis等数据库技术', '周一3-4节/C305', 1, 1, 'admin'),
('CS201', '微服务架构', '选修', 2.0, 32, 1001, '张教授', '2024-2025-2', 30, 'Spring Cloud、Nacos、分布式系统设计', '周五1-2节/A201', 1, 1, 'admin'),
('CS202', '大数据技术', '选修', 2.5, 40, 1004, '赵老师', '2024-2025-2', 35, 'Hadoop、Spark、Elasticsearch等大数据技术栈', '周三5-6节/D401', 1, 1, 'admin');

-- 插入课程日历示例
INSERT INTO `course_calendar` (`course_id`, `event_type`, `event_title`, `event_description`, `event_date`, `start_time`, `end_time`, `location`, `week_day`, `week_number`, `color`)
VALUES
(1, 'class', 'JavaEE第一课', '课程介绍与环境搭建', '2025-02-24', '08:00:00', '09:40:00', 'A101', 1, 1, '#409EFF'),
(1, 'class', 'JavaEE第二课', 'Spring Boot基础', '2025-02-26', '10:00:00', '11:40:00', 'A101', 3, 1, '#409EFF'),
(1, 'homework', '作业1：搭建Spring Boot项目', '完成Spring Boot项目搭建', '2025-03-02', NULL, NULL, '线上提交', 7, 1, '#E6A23C'),
(2, 'class', 'Web前端第一课', 'Vue3入门', '2025-02-25', '08:00:00', '09:40:00', 'B203', 2, 1, '#67C23A'),
(3, 'exam', '数据库期中考试', 'MySQL理论与实践考试', '2025-04-15', '14:00:00', '16:00:00', 'C305', 2, 8, '#F56C6C');

COMMIT;
