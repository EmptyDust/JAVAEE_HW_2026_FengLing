USE student_system;

-- 为 2024-2025-1 学期的课程添加课程日历
-- 学期时间：2024-09-01 至 2025-01-31

-- CS501 操作系统原理：周二3-4节/C201，周五5-6节/C201
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(39, 'class', '操作系统原理', '操作系统课程', '2024-09-03', '10:00:00', '11:40:00', 'C201', 1, 'FREQ=WEEKLY;BYDAY=TU,FR;UNTIL=2025-01-31;INTERVAL=1', '#409EFF', 1);

-- CS502 计算机网络：周一7-8节/D301，周三9-10节/D301
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(40, 'class', '计算机网络', '计算机网络课程', '2024-09-02', '16:00:00', '17:40:00', 'D301', 1, 'FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2025-01-31;INTERVAL=1', '#67C23A', 1);

-- CS503 Android开发：周四5-6节/E101
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(41, 'class', 'Android开发', 'Android应用开发', '2024-09-05', '14:00:00', '15:40:00', 'E101', 1, 'FREQ=WEEKLY;BYDAY=TH;UNTIL=2025-01-31;INTERVAL=1', '#E6A23C', 1);

-- CS504 iOS开发：周五3-4节/E102
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(42, 'class', 'iOS开发', 'iOS应用开发', '2024-09-06', '10:00:00', '11:40:00', 'E102', 1, 'FREQ=WEEKLY;BYDAY=FR;UNTIL=2025-01-31;INTERVAL=1', '#F56C6C', 1);

-- MATH501 离散数学：周一3-4节/B305，周三5-6节/B305
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(43, 'class', '离散数学', '离散数学课程', '2024-09-02', '10:00:00', '11:40:00', 'B305', 1, 'FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2025-01-31;INTERVAL=1', '#409EFF', 1);

-- MATH502 概率统计：周二7-8节/B201
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(44, 'class', '概率统计', '概率论与数理统计', '2024-09-03', '16:00:00', '17:40:00', 'B201', 1, 'FREQ=WEEKLY;BYDAY=TU;UNTIL=2025-01-31;INTERVAL=1', '#67C23A', 1);

-- ENG501 学术英语写作：周五7-8节/语言楼
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(45, 'class', '学术英语写作', '学术英语写作课程', '2024-09-06', '16:00:00', '17:40:00', '语言楼', 1, 'FREQ=WEEKLY;BYDAY=FR;UNTIL=2025-01-31;INTERVAL=1', '#E6A23C', 1);

-- ENG502 商务英语：周四3-4节/语言楼
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(46, 'class', '商务英语', '商务英语课程', '2024-09-05', '10:00:00', '11:40:00', '语言楼', 1, 'FREQ=WEEKLY;BYDAY=TH;UNTIL=2025-01-31;INTERVAL=1', '#F56C6C', 1);

-- 为 2025-2026-1 学期的课程添加课程日历
-- 学期时间：2025-09-01 至 2026-01-31

-- CS601 编译原理：周一1-2节/C301，周三3-4节/C301
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(47, 'class', '编译原理', '编译器设计原理', '2025-09-01', '08:00:00', '09:40:00', 'C301', 1, 'FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2026-01-31;INTERVAL=1', '#409EFF', 1);

-- CS602 软件工程：周二5-6节/D201
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(48, 'class', '软件工程', '软件工程课程', '2025-09-02', '14:00:00', '15:40:00', 'D201', 1, 'FREQ=WEEKLY;BYDAY=TU;UNTIL=2026-01-31;INTERVAL=1', '#67C23A', 1);

-- CS603 云计算技术：周四7-8节/F201
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(49, 'class', '云计算技术', '云计算架构与应用', '2025-09-04', '16:00:00', '17:40:00', 'F201', 1, 'FREQ=WEEKLY;BYDAY=TH;UNTIL=2026-01-31;INTERVAL=1', '#E6A23C', 1);

-- CS604 区块链技术：周五5-6节/F202
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(50, 'class', '区块链技术', '区块链原理与应用', '2025-09-05', '14:00:00', '15:40:00', 'F202', 1, 'FREQ=WEEKLY;BYDAY=FR;UNTIL=2026-01-31;INTERVAL=1', '#F56C6C', 1);

-- MATH601 数值分析：周五1-2节/B305
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(51, 'class', '数值分析', '数值计算方法', '2025-09-05', '08:00:00', '09:40:00', 'B305', 1, 'FREQ=WEEKLY;BYDAY=FR;UNTIL=2026-01-31;INTERVAL=1', '#409EFF', 1);

-- MATH602 运筹学：周二3-4节/B306
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(52, 'class', '运筹学', '优化理论与方法', '2025-09-02', '10:00:00', '11:40:00', 'B306', 1, 'FREQ=WEEKLY;BYDAY=TU;UNTIL=2026-01-31;INTERVAL=1', '#67C23A', 1);

-- PHY601 大学物理III：周一5-6节/理科楼，周三7-8节/理科楼
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(53, 'class', '大学物理III', '电磁学、光学、近代物理', '2025-09-01', '14:00:00', '15:40:00', '理科楼', 1, 'FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2026-01-31;INTERVAL=1', '#E6A23C', 1);

-- PE601 羽毛球进阶：周四9-10节/体育馆
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(54, 'class', '羽毛球进阶', '羽毛球技术与战术', '2025-09-04', '19:00:00', '20:40:00', '体育馆', 1, 'FREQ=WEEKLY;BYDAY=TH;UNTIL=2026-01-31;INTERVAL=1', '#F56C6C', 1);

-- PE602 网球：周五9-10节/网球场
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(55, 'class', '网球', '网球基础与实战', '2025-09-05', '19:00:00', '20:40:00', '网球场', 1, 'FREQ=WEEKLY;BYDAY=FR;UNTIL=2026-01-31;INTERVAL=1', '#409EFF', 1);

-- 添加一些考试和作业事件示例（2024-2025-1学期）
INSERT INTO course_calendar (course_id, event_type, event_title, event_description, event_date, start_time, end_time, location, is_recurring, recurrence_rule, color, status)
VALUES
(39, 'exam', '操作系统期中考试', '操作系统原理期中考试', '2024-11-15', '14:00:00', '16:00:00', 'C201', 0, NULL, '#F56C6C', 1),
(39, 'exam', '操作系统期末考试', '操作系统原理期末考试', '2025-01-10', '14:00:00', '16:00:00', 'C201', 0, NULL, '#F56C6C', 1),
(43, 'homework', '离散数学作业', '集合论与图论习题', '2024-10-15', '23:59:00', '23:59:00', '在线提交', 0, NULL, '#E6A23C', 1);

-- 查看插入结果
SELECT c.course_code, c.course_name, c.semester, cc.event_title, cc.event_date, cc.recurrence_rule
FROM course_calendar cc
JOIN course_info c ON cc.course_id = c.id
WHERE c.semester IN ('2024-2025-1', '2025-2026-1')
ORDER BY c.semester, c.course_code;
