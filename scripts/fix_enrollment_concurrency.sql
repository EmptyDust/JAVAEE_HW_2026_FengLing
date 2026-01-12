-- =====================================================
-- 修复选课系统并发问题 - 数据库索引优化
-- =====================================================

USE student_system;

-- 1. 为 course_enrollment 表添加唯一索引，防止重复选课
-- 注意：如果已存在该索引，需要先删除旧索引
-- DROP INDEX uk_course_student_status ON course_enrollment;

ALTER TABLE course_enrollment
ADD UNIQUE INDEX uk_course_student_status (course_id, student_id, status)
COMMENT '防止同一学生重复选课（按状态区分）';

-- 2. 为 course_info 表的 enrolled_students 字段添加索引（可选，提升查询性能）
ALTER TABLE course_info
ADD INDEX idx_enrolled_students (enrolled_students)
COMMENT '选课人数索引，提升查询性能';

-- 3. 验证索引是否创建成功
SHOW INDEX FROM course_enrollment WHERE Key_name = 'uk_course_student_status';
SHOW INDEX FROM course_info WHERE Key_name = 'idx_enrolled_students';

-- =====================================================
-- 说明：
-- 1. uk_course_student_status 唯一索引确保同一学生不能重复选同一门课（相同状态）
-- 2. 当并发插入时，数据库会抛出 DuplicateKeyException
-- 3. 应用层捕获该异常并转换为友好的业务提示
-- =====================================================
