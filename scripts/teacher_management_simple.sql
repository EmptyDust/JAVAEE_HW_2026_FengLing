-- 教师管理模块数据库脚本（简化版）

-- 1. 创建教师信息表
CREATE TABLE IF NOT EXISTS teacher_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    teacher_no VARCHAR(50) NOT NULL UNIQUE COMMENT '教师工号',
    teacher_name VARCHAR(100) NOT NULL COMMENT '教师姓名',
    title VARCHAR(50) COMMENT '职称（助教、讲师、副教授、教授）',
    department VARCHAR(100) COMMENT '所属部门',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    avatar_file_id BIGINT COMMENT '头像文件ID',
    user_id BIGINT COMMENT '关联用户ID',
    status INT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

-- 2. 插入测试教师数据
INSERT INTO teacher_info (teacher_no, teacher_name, title, department, phone, email, user_id, status) VALUES
('T001', '张教授', '教授', '计算机科学与技术学院', '13800138001', 'zhang@example.com', NULL, 1),
('T002', '李副教授', '副教授', '计算机科学与技术学院', '13800138002', 'li@example.com', NULL, 1),
('T003', '王讲师', '讲师', '软件工程学院', '13800138003', 'wang@example.com', NULL, 1),
('T004', '赵助教', '助教', '软件工程学院', '13800138004', 'zhao@example.com', NULL, 1)
ON DUPLICATE KEY UPDATE teacher_name=VALUES(teacher_name);
