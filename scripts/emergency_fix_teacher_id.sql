-- =====================================================
-- 紧急修复：添加 teacher_id 字段到 User 表
-- 执行时间：约 5 分钟
-- =====================================================

USE student_system;

-- 1. 添加 teacher_id 字段
ALTER TABLE user
ADD COLUMN teacher_id BIGINT COMMENT '教师ID（当用户类型为teacher时关联）' AFTER student_id;

-- 2. 添加索引
ALTER TABLE user
ADD INDEX idx_teacher_id (teacher_id);

-- 3. 数据修复：同步现有教师的 teacher_id
UPDATE user u
INNER JOIN teacher_info t ON t.user_id = u.id
SET u.teacher_id = t.id
WHERE u.user_type = 'teacher';

-- 4. 验证修复结果
SELECT
    u.id,
    u.username,
    u.user_type,
    u.student_id,
    u.teacher_id,
    t.teacher_name
FROM user u
LEFT JOIN teacher_info t ON t.id = u.teacher_id
WHERE u.user_type = 'teacher';

-- 预期结果：所有教师用户的 teacher_id 都不为空

-- =====================================================
-- 说明：
-- 1. 此脚本会修改 user 表结构，建议先备份数据库
-- 2. 如果需要回滚：ALTER TABLE user DROP COLUMN teacher_id;
-- =====================================================
