USE student_system;

-- 修复 course_enrollment 表中的错误数据
-- student_name 字段当前存储的是 student_id，应该存储学生姓名
-- student_number 字段为 NULL，应该存储学号
-- class_name 字段为 NULL，应该存储班级名称

-- 1. 先查看当前错误数据的情况
SELECT
    ce.id,
    ce.student_id,
    ce.student_name as current_student_name_wrong,
    ce.student_number as current_student_number_null,
    ce.class_name as current_class_name_null,
    s.name as correct_student_name,
    s.student_no as correct_student_number,
    c.class_name as correct_class_name
FROM course_enrollment ce
LEFT JOIN student s ON ce.student_id = s.id
LEFT JOIN class c ON s.class_id = c.id
WHERE ce.student_name IS NOT NULL
  AND (ce.student_number IS NULL OR ce.class_name IS NULL)
LIMIT 10;

-- 2. 修复数据：将 student_name 更新为真实姓名，student_number 更新为学号，class_name 更新为班级名称
UPDATE course_enrollment ce
INNER JOIN student s ON ce.student_id = s.id
LEFT JOIN class c ON s.class_id = c.id
SET
    ce.student_name = s.name,
    ce.student_number = s.student_no,
    ce.class_id = s.class_id,
    ce.class_name = c.class_name
WHERE ce.student_number IS NULL OR ce.class_name IS NULL;

-- 3. 验证修复结果
SELECT
    ce.id,
    ce.student_id,
    ce.student_name,
    ce.student_number,
    ce.class_name,
    ce.course_id
FROM course_enrollment ce
ORDER BY ce.id DESC
LIMIT 20;

-- 4. 统计修复情况
SELECT
    COUNT(*) as total_records,
    SUM(CASE WHEN student_name IS NOT NULL THEN 1 ELSE 0 END) as has_name,
    SUM(CASE WHEN student_number IS NOT NULL THEN 1 ELSE 0 END) as has_number,
    SUM(CASE WHEN class_name IS NOT NULL THEN 1 ELSE 0 END) as has_class_name
FROM course_enrollment;
