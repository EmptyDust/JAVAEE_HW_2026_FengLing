-- 迁移现有课程日历到周期性事件模式
-- 删除旧的单次事件记录，创建新的周期性事件记录

USE course_system;

-- 1. 删除所有现有的课程日历记录（保留表结构）
DELETE FROM course_calendar;

-- 2. 为现有课程创建周期性日历规则
-- 假设学期时间: 2025-02-24 至 2025-06-30 (春季学期)

-- 课程1: JavaEE企业级开发 (每周一、三 08:00-09:40)
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    1, 'class', 'JavaEE企业级开发', '企业级Java开发技术讲解',
    '2025-02-24', '08:00:00', '09:40:00', 'A101',
    1, 'FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2025-06-30;INTERVAL=1', '#409EFF', 1
);

-- 课程1: 期中考试（单次事件）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    1, 'exam', 'JavaEE期中考试', 'JavaEE企业级开发期中考试',
    '2025-04-14', '14:00:00', '16:00:00', 'A201',
    0, NULL, '#F56C6C', 1
);

-- 课程1: 期末考试（单次事件）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    1, 'exam', 'JavaEE期末考试', 'JavaEE企业级开发期末考试',
    '2025-06-23', '14:00:00', '16:00:00', 'A201',
    0, NULL, '#F56C6C', 1
);

-- 课程2: 数据结构与算法 (每周二、四 10:00-11:40)
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    2, 'class', '数据结构与算法', '数据结构与算法分析',
    '2025-02-25', '10:00:00', '11:40:00', 'B203',
    1, 'FREQ=WEEKLY;BYDAY=TU,TH;UNTIL=2025-06-30;INTERVAL=1', '#67C23A', 1
);

-- 课程2: 期末考试
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    2, 'exam', '数据结构期末考试', '数据结构与算法期末考试',
    '2025-06-26', '09:00:00', '11:00:00', 'B301',
    0, NULL, '#F56C6C', 1
);

-- 课程3: 计算机网络 (每周五 14:00-16:40，连续2节)
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    3, 'class', '计算机网络', '计算机网络原理与应用',
    '2025-02-28', '14:00:00', '16:40:00', 'C105',
    1, 'FREQ=WEEKLY;BYDAY=FR;UNTIL=2025-06-30;INTERVAL=1', '#E6A23C', 1
);

-- 课程4: 操作系统 (每周一、三 14:00-15:40)
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    4, 'class', '操作系统', '操作系统原理与实践',
    '2025-02-24', '14:00:00', '15:40:00', 'D102',
    1, 'FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2025-06-30;INTERVAL=1', '#909399', 1
);

-- 课程5: 软件工程 (每周二 08:00-10:40，连续2节)
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    5, 'class', '软件工程', '软件工程理论与实践',
    '2025-02-25', '08:00:00', '10:40:00', 'E201',
    1, 'FREQ=WEEKLY;BYDAY=TU;UNTIL=2025-06-30;INTERVAL=1', '#409EFF', 1
);

-- 添加一些作业截止日期示例
-- JavaEE课程作业1
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    1, 'homework', 'Spring Boot作业', 'Spring Boot框架基础作业提交截止',
    '2025-03-15', '23:59:00', '23:59:00', '在线提交',
    0, NULL, '#E6A23C', 1
);

-- JavaEE课程作业2
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    1, 'homework', '微服务项目作业', '微服务架构实践项目提交截止',
    '2025-05-10', '23:59:00', '23:59:00', '在线提交',
    0, NULL, '#E6A23C', 1
);

-- 数据结构课程作业
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    2, 'homework', '树与图算法作业', '树和图相关算法实现作业',
    '2025-04-20', '23:59:00', '23:59:00', '在线提交',
    0, NULL, '#E6A23C', 1
);

-- 添加一个课程活动示例
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    1, 'activity', 'JavaEE技术讲座', '企业级开发实践经验分享',
    '2025-04-25', '19:00:00', '21:00:00', '学术报告厅',
    0, NULL, '#67C23A', 1
);

-- 查询验证
SELECT
    cc.id,
    ci.course_name,
    cc.event_type,
    cc.event_title,
    cc.event_date,
    cc.start_time,
    cc.is_recurring,
    cc.recurrence_rule
FROM course_calendar cc
LEFT JOIN course_info ci ON cc.course_id = ci.id
ORDER BY ci.id, cc.event_type, cc.event_date;

SELECT '========== 迁移完成 ==========' AS status;
SELECT COUNT(*) AS '总记录数',
       SUM(CASE WHEN is_recurring = 1 THEN 1 ELSE 0 END) AS '周期性事件数',
       SUM(CASE WHEN is_recurring = 0 THEN 1 ELSE 0 END) AS '单次事件数'
FROM course_calendar;
