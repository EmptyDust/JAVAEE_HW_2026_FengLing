-- 简化版迁移脚本：清空并创建周期性日历事件

USE student_system;

-- 1. 清空现有日历记录
TRUNCATE TABLE course_calendar;

-- 2. 为课程1创建周期性事件（每周一、三）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    1, 'class', 'JavaEE企业级开发', '企业级Java开发技术讲解',
    '2025-02-24', '08:00:00', '09:40:00', 'A101',
    1, 'FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2025-06-30;INTERVAL=1', '#409EFF', 1
);

-- 3. 为课程2创建周期性事件（每周二、四）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    2, 'class', 'Web前端开发', '前端开发技术与实践',
    '2025-02-25', '08:00:00', '09:40:00', 'B203',
    1, 'FREQ=WEEKLY;BYDAY=TU,TH;UNTIL=2025-06-30;INTERVAL=1', '#67C23A', 1
);

-- 4. 为课程3创建周期性事件（每周五）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    3, 'class', '数据库原理与应用', '数据库系统原理与SQL',
    '2025-02-28', '14:00:00', '15:40:00', 'C305',
    1, 'FREQ=WEEKLY;BYDAY=FR;UNTIL=2025-06-30;INTERVAL=1', '#E6A23C', 1
);

-- 5. 为课程4创建周期性事件（每周一、三）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    4, 'class', '微服务架构', '微服务架构设计与实现',
    '2025-02-24', '14:00:00', '15:40:00', 'A201',
    1, 'FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2025-06-30;INTERVAL=1', '#409EFF', 1
);

-- 6. 为课程5创建周期性事件（每周二）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES (
    5, 'class', '大数据技术', '大数据处理技术与应用',
    '2025-02-25', '14:00:00', '15:40:00', 'D401',
    1, 'FREQ=WEEKLY;BYDAY=TU;UNTIL=2025-06-30;INTERVAL=1', '#F56C6C', 1
);

-- 7. 添加一些考试（单次事件）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES
(1, 'exam', 'JavaEE期中考试', 'JavaEE企业级开发期中考试',
 '2025-04-14', '14:00:00', '16:00:00', 'A201', 0, NULL, '#F56C6C', 1),
(1, 'exam', 'JavaEE期末考试', 'JavaEE企业级开发期末考试',
 '2025-06-23', '14:00:00', '16:00:00', 'A201', 0, NULL, '#F56C6C', 1),
(2, 'exam', 'Web前端期末考试', 'Web前端开发期末考试',
 '2025-06-26', '09:00:00', '11:00:00', 'B301', 0, NULL, '#F56C6C', 1);

-- 8. 添加一些作业（单次事件）
INSERT INTO course_calendar (
    course_id, event_type, event_title, event_description,
    event_date, start_time, end_time, location,
    is_recurring, recurrence_rule, color, status
) VALUES
(1, 'homework', 'Spring Boot作业', 'Spring Boot框架基础作业提交截止',
 '2025-03-15', '23:59:00', '23:59:00', '在线提交', 0, NULL, '#E6A23C', 1),
(1, 'homework', '微服务项目作业', '微服务架构实践项目提交截止',
 '2025-05-10', '23:59:00', '23:59:00', '在线提交', 0, NULL, '#E6A23C', 1),
(2, 'homework', 'Vue项目作业', 'Vue.js项目实战作业',
 '2025-04-20', '23:59:00', '23:59:00', '在线提交', 0, NULL, '#E6A23C', 1);

-- 查询验证
SELECT '=== 迁移完成 ===' AS status;
SELECT COUNT(*) AS '总记录数',
       SUM(CASE WHEN is_recurring = 1 THEN 1 ELSE 0 END) AS '周期性事件数',
       SUM(CASE WHEN is_recurring = 0 THEN 1 ELSE 0 END) AS '单次事件数'
FROM course_calendar;

SELECT id, course_id, event_type, event_title, is_recurring,
       SUBSTRING(recurrence_rule, 1, 30) as rule_preview
FROM course_calendar
ORDER BY course_id, is_recurring DESC, event_type;
