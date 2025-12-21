package com.student.course.util;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 重复规则解析器
 * 支持类似iCalendar RFC 5545的重复规则
 */
@Data
public class RecurrenceRule {

    public enum Frequency {
        DAILY, WEEKLY, MONTHLY
    }

    private Frequency frequency;
    private Integer interval = 1;  // 间隔
    private Set<DayOfWeek> byDay = new HashSet<>();  // 星期几
    private LocalDate untilDate;  // 结束日期

    /**
     * 解析重复规则字符串
     * 格式: FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2025-06-30;INTERVAL=1
     */
    public static RecurrenceRule parse(String ruleStr) {
        if (ruleStr == null || ruleStr.isEmpty()) {
            return null;
        }

        RecurrenceRule rule = new RecurrenceRule();
        String[] parts = ruleStr.split(";");

        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length != 2) continue;

            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case "FREQ":
                    rule.setFrequency(Frequency.valueOf(value));
                    break;

                case "INTERVAL":
                    rule.setInterval(Integer.parseInt(value));
                    break;

                case "BYDAY":
                    String[] days = value.split(",");
                    for (String day : days) {
                        rule.getByDay().add(parseDayOfWeek(day.trim()));
                    }
                    break;

                case "UNTIL":
                    rule.setUntilDate(LocalDate.parse(value));
                    break;
            }
        }

        return rule;
    }

    /**
     * 将简写的星期转换为DayOfWeek
     */
    private static DayOfWeek parseDayOfWeek(String day) {
        switch (day) {
            case "MO": return DayOfWeek.MONDAY;
            case "TU": return DayOfWeek.TUESDAY;
            case "WE": return DayOfWeek.WEDNESDAY;
            case "TH": return DayOfWeek.THURSDAY;
            case "FR": return DayOfWeek.FRIDAY;
            case "SA": return DayOfWeek.SATURDAY;
            case "SU": return DayOfWeek.SUNDAY;
            default: throw new IllegalArgumentException("Invalid day: " + day);
        }
    }

    /**
     * 生成指定日期范围内的所有重复日期
     */
    public List<LocalDate> generateOccurrences(LocalDate startDate, LocalDate rangeStart, LocalDate rangeEnd) {
        List<LocalDate> occurrences = new ArrayList<>();

        if (frequency == null) {
            return occurrences;
        }

        LocalDate current = startDate;
        LocalDate effectiveEnd = untilDate != null && untilDate.isBefore(rangeEnd) ? untilDate : rangeEnd;

        while (!current.isAfter(effectiveEnd)) {
            if (!current.isBefore(rangeStart)) {
                // 检查是否符合BYDAY规则
                if (byDay.isEmpty() || byDay.contains(current.getDayOfWeek())) {
                    occurrences.add(current);
                }
            }

            // 计算下一次日期
            current = getNextOccurrence(current);

            // 防止无限循环
            if (current == null) break;
        }

        return occurrences;
    }

    /**
     * 根据频率和间隔计算下一次日期
     */
    private LocalDate getNextOccurrence(LocalDate current) {
        switch (frequency) {
            case DAILY:
                return current.plusDays(interval);

            case WEEKLY:
                if (byDay.isEmpty()) {
                    // 没有指定星期几,按interval周递增
                    return current.plusWeeks(interval);
                } else {
                    // 有指定星期几,找下一个匹配的日期
                    LocalDate next = current.plusDays(1);
                    int daysChecked = 0;
                    int maxDays = 7 * interval;  // 最多检查interval周

                    while (daysChecked < maxDays) {
                        if (byDay.contains(next.getDayOfWeek())) {
                            return next;
                        }
                        next = next.plusDays(1);
                        daysChecked++;
                    }

                    // 如果在interval周内没找到匹配的日期,跳到下一个interval周期
                    return current.plusWeeks(interval);
                }

            case MONTHLY:
                return current.plusMonths(interval);

            default:
                return null;
        }
    }

    /**
     * 构建规则字符串
     */
    public String toRuleString() {
        StringBuilder sb = new StringBuilder();

        if (frequency != null) {
            sb.append("FREQ=").append(frequency.name());
        }

        if (interval != null && interval > 1) {
            sb.append(";INTERVAL=").append(interval);
        }

        if (!byDay.isEmpty()) {
            sb.append(";BYDAY=");
            List<String> dayStrs = new ArrayList<>();
            for (DayOfWeek day : byDay) {
                dayStrs.add(formatDayOfWeek(day));
            }
            sb.append(String.join(",", dayStrs));
        }

        if (untilDate != null) {
            sb.append(";UNTIL=").append(untilDate);
        }

        return sb.toString();
    }

    /**
     * 将DayOfWeek转换为简写
     */
    private String formatDayOfWeek(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "MO";
            case TUESDAY: return "TU";
            case WEDNESDAY: return "WE";
            case THURSDAY: return "TH";
            case FRIDAY: return "FR";
            case SATURDAY: return "SA";
            case SUNDAY: return "SU";
            default: return "";
        }
    }
}
