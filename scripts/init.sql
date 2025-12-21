-- 学生信息管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `user_type` VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT '用户类型: admin-管理员, teacher-教师, student-学生',
    `student_id` BIGINT COMMENT '学生ID (当用户类型为student时关联)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 班级表
CREATE TABLE IF NOT EXISTS `class` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_name` VARCHAR(100) NOT NULL COMMENT '班级名称',
    `class_no` VARCHAR(50) NOT NULL COMMENT '班级编号',
    `grade_id` BIGINT COMMENT '年级ID',
    `teacher_id` BIGINT COMMENT '班主任ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_class_no` (`class_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 学生表
CREATE TABLE IF NOT EXISTS `student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '学生姓名',
    `student_no` VARCHAR(50) NOT NULL COMMENT '学号',
    `class_id` BIGINT COMMENT '班级ID',
    `gender` VARCHAR(10) COMMENT '性别',
    `age` INT COMMENT '年龄',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(255) COMMENT '家庭住址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 数据字典表
CREATE TABLE IF NOT EXISTS `dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- 插入初始数据

-- 插入测试用户 (密码: 123456)
-- BCrypt 哈希通过系统生成，确保与 BCryptPasswordEncoder 兼容
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `user_type`, `student_id`) VALUES
('admin', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'admin@example.com', '13800138000', 'admin', NULL),
('teacher1', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'teacher1@example.com', '13800138001', 'teacher', NULL);

-- 插入班级数据
INSERT INTO `class` (`class_name`, `class_no`, `grade_id`, `teacher_id`) VALUES
('软件工程1班', 'SE2021-01', 1, 2),
('软件工程2班', 'SE2021-02', 1, 2),
('计算机科学1班', 'CS2021-01', 1, 2),
('数据科学1班', 'DS2021-01', 1, 2);

-- 插入学生数据
INSERT INTO `student` (`name`, `student_no`, `class_id`, `gender`, `age`, `phone`, `address`) VALUES
('张三', '2021001', 1, '男', 20, '13900139001', '北京市海淀区'),
('李四', '2021002', 1, '女', 19, '13900139002', '北京市朝阳区'),
('王五', '2021003', 2, '男', 21, '13900139003', '上海市浦东新区'),
('赵六', '2021004', 2, '女', 20, '13900139004', '广州市天河区'),
('钱七', '2021005', 3, '男', 19, '13900139005', '深圳市南山区'),
('孙八', '2021006', 3, '女', 20, '13900139006', '杭州市西湖区'),
('周九', '2021007', 4, '男', 21, '13900139007', '成都市武侯区'),
('吴十', '2021008', 4, '女', 19, '13900139008', '武汉市洪山区');

-- 为学生创建对应的用户账号 (密码: 123456, 用户名为学号)
-- BCrypt 哈希通过系统生成，确保与 BCryptPasswordEncoder 兼容
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `user_type`, `student_id`) VALUES
('2021001', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'zhangsan@student.com', '13900139001', 'student', 1),
('2021002', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'lisi@student.com', '13900139002', 'student', 2),
('2021003', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'wangwu@student.com', '13900139003', 'student', 3),
('2021004', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'zhaoliu@student.com', '13900139004', 'student', 4),
('2021005', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'qianqi@student.com', '13900139005', 'student', 5),
('2021006', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'sunba@student.com', '13900139006', 'student', 6),
('2021007', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'zhoujiu@student.com', '13900139007', 'student', 7),
('2021008', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'wushi@student.com', '13900139008', 'student', 8);

-- 插入数据字典
INSERT INTO `dict` (`dict_type`, `dict_code`, `dict_value`, `sort`) VALUES
('gender', 'male', '男', 1),
('gender', 'female', '女', 2),
('grade', 'grade1', '大一', 1),
('grade', 'grade2', '大二', 2),
('grade', 'grade3', '大三', 3),
('grade', 'grade4', '大四', 4);

-- 创建索引
ALTER TABLE `student` ADD INDEX `idx_name` (`name`);
ALTER TABLE `class` ADD INDEX `idx_class_name` (`class_name`);

-- 数据权限规则表
CREATE TABLE IF NOT EXISTS `data_permission_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_type` VARCHAR(20) NOT NULL COMMENT '角色类型：admin/teacher/student',
    `table_name` VARCHAR(50) NOT NULL COMMENT '表名',
    `entity_class` VARCHAR(100) COMMENT '实体类名',
    `filter_field` VARCHAR(50) NOT NULL COMMENT '过滤字段名（如：student_id, teacher_id）',
    `filter_operator` VARCHAR(10) DEFAULT '=' COMMENT '过滤操作符：=, IN, >, <等',
    `context_field` VARCHAR(50) NOT NULL COMMENT 'UserContext中的字段名（如：studentId, teacherId, userId）',
    `filter_type` VARCHAR(20) DEFAULT 'SIMPLE' COMMENT '过滤类型：SIMPLE(简单条件), SUBQUERY(子查询)',
    `subquery_sql` TEXT COMMENT '子查询SQL（当filter_type=SUBQUERY时使用）',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `description` VARCHAR(200) COMMENT '规则描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_table_field` (`role_type`, `table_name`, `filter_field`),
    KEY `idx_role_type` (`role_type`),
    KEY `idx_table_name` (`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限规则表';

-- 插入数据权限规则初始化数据

-- 学生规则
INSERT INTO `data_permission_rule` (`role_type`, `table_name`, `entity_class`, `filter_field`, `filter_operator`, `context_field`, `filter_type`, `description`) VALUES
('student', 'course_enrollment', 'CourseEnrollment', 'student_id', '=', 'studentId', 'SIMPLE', '学生只能看到自己的选课记录'),
('student', 'notification_receive', 'NotificationReceive', 'user_id', '=', 'userId', 'SIMPLE', '学生只能看到发给自己的通知'),
('student', 'student', 'Student', 'id', '=', 'studentId', 'SIMPLE', '学生只能看到自己的学生信息');

-- 教师规则（默认配置，管理员可后续调整）
INSERT INTO `data_permission_rule` (`role_type`, `table_name`, `entity_class`, `filter_field`, `filter_operator`, `context_field`, `filter_type`, `description`) VALUES
('teacher', 'course_info', 'CourseInfo', 'teacher_id', '=', 'teacherId', 'SIMPLE', '教师只能看到自己教授的课程'),
('teacher', 'course_enrollment', 'CourseEnrollment', 'course_id', 'IN', 'teacherId', 'SUBQUERY', '教师只能看到自己课程的选课学生'),
('teacher', 'file_info', 'FileInfo', 'upload_user_id', '=', 'userId', 'SIMPLE', '教师只能看到自己上传的文件'),
('teacher', 'course_notification', 'CourseNotification', 'create_user_id', '=', 'userId', 'SIMPLE', '教师只能看到自己创建的通知'),
('teacher', 'course_attachment', 'CourseAttachment', 'upload_user_id', '=', 'userId', 'SIMPLE', '教师只能看到自己上传的附件');

-- 为CourseEnrollment的教师规则设置子查询
UPDATE `data_permission_rule`
SET `subquery_sql` = 'SELECT id FROM course_info WHERE teacher_id = ?'
WHERE `role_type` = 'teacher' AND `table_name` = 'course_enrollment';

COMMIT;
