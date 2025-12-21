package com.student.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.common.exception.BusinessException;
import com.student.course.entity.CourseCalendar;
import com.student.course.entity.CourseInfo;
import com.student.course.mapper.CourseCalendarMapper;
import com.student.course.util.RecurrenceRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程日历服务
 */
@Slf4j
@Service
public class CourseCalendarService {

    @Autowired
    private CourseCalendarMapper calendarMapper;

    @Autowired
    private CourseInfoService courseInfoService;

    /**
     * 获取课程的日历事件列表
     */
    public List<CourseCalendar> getCourseCalendar(Long courseId) {
        LambdaQueryWrapper<CourseCalendar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCalendar::getCourseId, courseId)
               .ne(CourseCalendar::getStatus, 0)  // 非取消状态
               .orderByAsc(CourseCalendar::getEventDate)
               .orderByAsc(CourseCalendar::getStartTime);

        List<CourseCalendar> events = calendarMapper.selectList(wrapper);

        // 对于周期性事件，展开为具体日期
        return expandRecurringEvents(events, null, null);
    }

    /**
     * 获取指定日期范围的日历事件
     */
    public List<CourseCalendar> getCalendarByDateRange(Long courseId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CourseCalendar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCalendar::getCourseId, courseId)
               .ne(CourseCalendar::getStatus, 0);

        List<CourseCalendar> events = calendarMapper.selectList(wrapper);

        // 展开周期性事件并过滤日期范围
        return expandRecurringEvents(events, startDate, endDate);
    }

    /**
     * 获取学生所有已选课程的日历事件
     */
    public List<CourseCalendar> getStudentCalendar(List<Long> courseIds, LocalDate startDate, LocalDate endDate) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<CourseCalendar> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CourseCalendar::getCourseId, courseIds)
               .ne(CourseCalendar::getStatus, 0);

        List<CourseCalendar> events = calendarMapper.selectList(wrapper);

        // 展开周期性事件并过滤日期范围
        return expandRecurringEvents(events, startDate, endDate);
    }

    /**
     * 展开周期性事件为具体日期
     */
    private List<CourseCalendar> expandRecurringEvents(List<CourseCalendar> events, LocalDate rangeStart, LocalDate rangeEnd) {
        List<CourseCalendar> expandedEvents = new ArrayList<>();

        for (CourseCalendar event : events) {
            if (event.getIsRecurring() != null && event.getIsRecurring() == 1) {
                // 周期性事件：根据规则展开
                List<CourseCalendar> occurrences = expandSingleRecurringEvent(event, rangeStart, rangeEnd);
                expandedEvents.addAll(occurrences);
            } else {
                // 一次性事件：检查是否在日期范围内
                if (rangeStart == null || rangeEnd == null ||
                    (!event.getEventDate().isBefore(rangeStart) && !event.getEventDate().isAfter(rangeEnd))) {
                    expandedEvents.add(event);
                }
            }
        }

        // 按日期和时间排序
        return expandedEvents.stream()
                .sorted((a, b) -> {
                    int dateCompare = a.getEventDate().compareTo(b.getEventDate());
                    if (dateCompare != 0) return dateCompare;

                    if (a.getStartTime() != null && b.getStartTime() != null) {
                        return a.getStartTime().compareTo(b.getStartTime());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    /**
     * 展开单个周期性事件
     */
    private List<CourseCalendar> expandSingleRecurringEvent(CourseCalendar event, LocalDate rangeStart, LocalDate rangeEnd) {
        List<CourseCalendar> occurrences = new ArrayList<>();

        // 解析重复规则
        RecurrenceRule rule = RecurrenceRule.parse(event.getRecurrenceRule());
        if (rule == null) {
            log.warn("无法解析重复规则: eventId={}, rule={}", event.getId(), event.getRecurrenceRule());
            return occurrences;
        }

        // 确定查询范围
        LocalDate effectiveStart = rangeStart != null ? rangeStart : event.getEventDate();
        LocalDate effectiveEnd = rangeEnd != null ? rangeEnd :
            (rule.getUntilDate() != null ? rule.getUntilDate() : event.getEventDate().plusYears(1));

        log.info("展开周期性事件: eventId={}, title={}, range=[{} to {}]",
            event.getId(), event.getEventTitle(), effectiveStart, effectiveEnd);

        // 生成所有重复日期
        List<LocalDate> dates = rule.generateOccurrences(event.getEventDate(), effectiveStart, effectiveEnd);
        log.info("生成了 {} 个重复日期", dates.size());

        // 为每个日期创建事件副本
        for (LocalDate date : dates) {
            CourseCalendar occurrence = new CourseCalendar();
            BeanUtils.copyProperties(event, occurrence);
            occurrence.setId(null);  // 清除ID，这是展开后的虚拟事件
            occurrence.setEventDate(date);
            occurrences.add(occurrence);
        }

        return occurrences;
    }

    /**
     * 创建日历事件
     */
    @Transactional(rollbackFor = Exception.class)
    public CourseCalendar createCalendarEvent(CourseCalendar calendar) {
        if (calendar.getStatus() == null) {
            calendar.setStatus(1);  // 默认正常状态
        }
        if (calendar.getColor() == null || calendar.getColor().isEmpty()) {
            calendar.setColor("#409EFF");  // 默认颜色
        }
        if (calendar.getIsRecurring() == null) {
            calendar.setIsRecurring(0);  // 默认非周期性
        }

        calendarMapper.insert(calendar);
        log.info("创建日历事件成功: courseId={}, eventTitle={}, isRecurring={}",
            calendar.getCourseId(), calendar.getEventTitle(), calendar.getIsRecurring());

        return calendar;
    }

    /**
     * 更新日历事件
     */
    @Transactional(rollbackFor = Exception.class)
    public CourseCalendar updateCalendarEvent(CourseCalendar calendar) {
        CourseCalendar existing = calendarMapper.selectById(calendar.getId());
        if (existing == null) {
            throw new BusinessException("日历事件不存在");
        }

        calendarMapper.updateById(calendar);
        log.info("更新日历事件成功: id={}, eventTitle={}", calendar.getId(), calendar.getEventTitle());

        return calendar;
    }

    /**
     * 删除日历事件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCalendarEvent(Long id) {
        calendarMapper.deleteById(id);
        log.info("删除日历事件成功: id={}", id);
    }

    /**
     * 取消日历事件（标记为取消状态，不删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelCalendarEvent(Long id) {
        CourseCalendar calendar = calendarMapper.selectById(id);
        if (calendar == null) {
            throw new BusinessException("日历事件不存在");
        }

        calendar.setStatus(0);  // 取消状态
        calendarMapper.updateById(calendar);
        log.info("取消日历事件成功: id={}, eventTitle={}", id, calendar.getEventTitle());
    }

    /**
     * 获取课程的原始日历记录（不展开周期性事件）
     */
    public List<CourseCalendar> getRawCourseCalendar(Long courseId) {
        LambdaQueryWrapper<CourseCalendar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCalendar::getCourseId, courseId)
               .ne(CourseCalendar::getStatus, 0)  // 非取消状态
               .orderByAsc(CourseCalendar::getEventDate)
               .orderByAsc(CourseCalendar::getStartTime);

        return calendarMapper.selectList(wrapper);
    }

    /**
     * 批量生成课程日历（根据课程的schedule_info自动生成整学期的上课日历）
     */
    @Transactional(rollbackFor = Exception.class)
    public int generateCourseCalendar(Long courseId, LocalDate startDate, LocalDate endDate) {
        // 1. 获取课程信息
        CourseInfo course = courseInfoService.getCourseById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        String scheduleInfo = course.getScheduleInfo();
        if (scheduleInfo == null || scheduleInfo.isEmpty()) {
            throw new BusinessException("课程没有设置上课时间");
        }

        // 2. 检查是否已经存在日历记录
        LambdaQueryWrapper<CourseCalendar> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(CourseCalendar::getCourseId, courseId)
                   .eq(CourseCalendar::getEventType, "class")
                   .eq(CourseCalendar::getIsRecurring, 1);

        long existingCount = calendarMapper.selectCount(checkWrapper);
        if (existingCount > 0) {
            throw new BusinessException("该课程已存在周期性课程日历，请先删除后再生成");
        }

        // 3. 解析 scheduleInfo 并生成 RRULE
        // scheduleInfo 格式: "周一1-2节/A101，周三3-4节/A101"
        List<ScheduleSegment> segments = parseScheduleInfo(scheduleInfo);

        if (segments.isEmpty()) {
            throw new BusinessException("无法解析课程时间信息");
        }

        // 4. 为每个时间段创建一条周期性日历记录
        int createdCount = 0;
        for (ScheduleSegment segment : segments) {
            CourseCalendar calendar = new CourseCalendar();
            calendar.setCourseId(courseId);
            calendar.setEventType("class");
            calendar.setEventTitle(course.getCourseName());
            calendar.setEventDescription(course.getCourseDescription());
            calendar.setEventDate(findFirstDateForWeekDay(startDate, segment.weekDay));
            calendar.setStartTime(segment.startTime);
            calendar.setEndTime(segment.endTime);
            calendar.setLocation(segment.location);
            calendar.setIsRecurring(1);

            // 生成 RRULE：FREQ=WEEKLY;BYDAY=MO;UNTIL=2025-01-31;INTERVAL=1
            String rrule = String.format("FREQ=WEEKLY;BYDAY=%s;UNTIL=%s;INTERVAL=1",
                    getWeekDayAbbr(segment.weekDay),
                    endDate.toString());
            calendar.setRecurrenceRule(rrule);

            calendar.setColor(getColorByType(course.getCourseType()));
            calendar.setStatus(1);

            calendarMapper.insert(calendar);
            createdCount++;
        }

        log.info("批量生成课程日历成功: courseId={}, courseName={}, count={}",
                courseId, course.getCourseName(), createdCount);

        return createdCount;
    }

    /**
     * 解析课程时间信息
     */
    private List<ScheduleSegment> parseScheduleInfo(String scheduleInfo) {
        List<ScheduleSegment> segments = new ArrayList<>();

        // 分割多个时间段："周一1-2节/A101，周三3-4节/A101"
        String[] parts = scheduleInfo.split("，");

        for (String part : parts) {
            // 匹配格式：周一1-2节/A101
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("周([一二三四五六日])(\\d+)-(\\d+)节/(.+)");
            java.util.regex.Matcher matcher = pattern.matcher(part.trim());

            if (matcher.find()) {
                ScheduleSegment segment = new ScheduleSegment();

                // 星期几
                String dayChar = matcher.group(1);
                segment.weekDay = convertChineseWeekDay(dayChar);

                // 开始和结束节次
                int startPeriod = Integer.parseInt(matcher.group(2));
                int endPeriod = Integer.parseInt(matcher.group(3));

                // 转换为时间
                segment.startTime = periodToTime(startPeriod);
                segment.endTime = periodToEndTime(endPeriod);

                // 地点
                segment.location = matcher.group(4);

                segments.add(segment);
            }
        }

        return segments;
    }

    /**
     * 转换中文星期为数字
     */
    private int convertChineseWeekDay(String dayChar) {
        switch (dayChar) {
            case "一": return 1;
            case "二": return 2;
            case "三": return 3;
            case "四": return 4;
            case "五": return 5;
            case "六": return 6;
            case "日": return 7;
            default: return 1;
        }
    }

    /**
     * 节次转换为开始时间
     */
    private LocalTime periodToTime(int period) {
        switch (period) {
            case 1: return LocalTime.of(8, 0);
            case 2: return LocalTime.of(8, 50);
            case 3: return LocalTime.of(10, 0);
            case 4: return LocalTime.of(10, 50);
            case 5: return LocalTime.of(14, 0);
            case 6: return LocalTime.of(14, 50);
            case 7: return LocalTime.of(16, 0);
            case 8: return LocalTime.of(16, 50);
            case 9: return LocalTime.of(19, 0);
            case 10: return LocalTime.of(19, 50);
            default: return LocalTime.of(8, 0);
        }
    }

    /**
     * 节次转换为结束时间
     */
    private LocalTime periodToEndTime(int period) {
        switch (period) {
            case 1: return LocalTime.of(8, 50);
            case 2: return LocalTime.of(9, 40);
            case 3: return LocalTime.of(10, 50);
            case 4: return LocalTime.of(11, 40);
            case 5: return LocalTime.of(14, 50);
            case 6: return LocalTime.of(15, 40);
            case 7: return LocalTime.of(16, 50);
            case 8: return LocalTime.of(17, 40);
            case 9: return LocalTime.of(19, 50);
            case 10: return LocalTime.of(20, 40);
            default: return LocalTime.of(9, 40);
        }
    }

    /**
     * 根据星期几获取缩写（用于RRULE）
     */
    private String getWeekDayAbbr(int weekDay) {
        switch (weekDay) {
            case 1: return "MO";
            case 2: return "TU";
            case 3: return "WE";
            case 4: return "TH";
            case 5: return "FR";
            case 6: return "SA";
            case 7: return "SU";
            default: return "MO";
        }
    }

    /**
     * 根据课程类型获取颜色
     */
    private String getColorByType(String courseType) {
        if (courseType == null) return "#409EFF";
        switch (courseType) {
            case "必修": return "#409EFF";
            case "选修": return "#67C23A";
            case "限选": return "#E6A23C";
            case "公选": return "#909399";
            default: return "#409EFF";
        }
    }

    /**
     * 找到指定日期范围内第一个匹配星期几的日期
     */
    private LocalDate findFirstDateForWeekDay(LocalDate startDate, int targetWeekDay) {
        LocalDate current = startDate;
        // Java的DayOfWeek: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
        while (current.getDayOfWeek().getValue() != targetWeekDay) {
            current = current.plusDays(1);
        }
        return current;
    }

    /**
     * 时间段解析结果
     */
    private static class ScheduleSegment {
        int weekDay;
        LocalTime startTime;
        LocalTime endTime;
        String location;
    }
}
