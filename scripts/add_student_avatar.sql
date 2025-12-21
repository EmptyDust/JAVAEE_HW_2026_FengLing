USE student_system;

-- 为student表添加avatar字段
ALTER TABLE student ADD COLUMN avatar VARCHAR(500) COMMENT '头像URL' AFTER address;

-- 查看表结构
DESCRIBE student;

-- 查看修改结果
SELECT id, name, student_no, avatar FROM student LIMIT 5;
