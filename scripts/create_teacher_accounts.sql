-- 为教师创建登录账号并关联

-- 1. 为4位教师创建用户账号（密码都是 123456）
INSERT INTO user (username, password, user_type, create_time) VALUES
('T001', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'teacher', NOW()),
('T002', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'teacher', NOW()),
('T003', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'teacher', NOW()),
('T004', '$2a$10$/4ZJ/a/iWQlgQo54.WnHQOxsRW/qicge2URGrDJCzWxSVzSDjCZOm', 'teacher', NOW());

-- 2. 关联教师信息与用户账号
UPDATE teacher_info t
JOIN user u ON t.teacher_no = u.username
SET t.user_id = u.id
WHERE u.user_type = 'teacher' AND t.teacher_no IN ('T001', 'T002', 'T003', 'T004');
