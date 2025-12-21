package com.student.course.controller;

import com.student.common.result.Result;
import com.student.course.entity.CourseCalendar;
import com.student.course.service.CourseCalendarService;
import com.student.course.service.SemesterService;
import com.student.course.vo.SemesterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 课程日历控制器
 */
@Slf4j
@Tag(name = "课程日历", description = "课程日历管理相关接口")
@RestController
@RequestMapping("/course/calendar")
public class CourseCalendarController {

    @Autowired
    private CourseCalendarService calendarService;

    @Autowired
    private SemesterService semesterService;

    @Operation(summary = "获取可用学期列表")
    @GetMapping("/semesters")
    public Result<List<SemesterVO>> getAvailableSemesters() {
        List<SemesterVO> semesters = semesterService.getAvailableSemesters();
        return Result.success(semesters);
    }

    @Operation(summary = "获取学生所有课程日历（按学期）")
    @PostMapping("/student/semester")
    public Result<List<CourseCalendar>> getStudentCalendarBySemester(
            @Parameter(description = "课程ID列表") @RequestBody List<Long> courseIds,
            @Parameter(description = "学期名称") @RequestParam(required = false) String semester) {

        // 如果没有指定学期，使用当前学期
        SemesterVO semesterInfo = semester != null ?
                semesterService.getSemesterInfo(semester) :
                semesterService.getCurrentSemester();

        if (semesterInfo == null) {
            return Result.error("学期不存在");
        }

        List<CourseCalendar> calendar = calendarService.getStudentCalendar(
                courseIds, semesterInfo.getStartDate(), semesterInfo.getEndDate());
        return Result.success(calendar);
    }

    @Operation(summary = "获取课程日历")
    @GetMapping("/{courseId}")
    public Result<List<CourseCalendar>> getCourseCalendar(@PathVariable Long courseId) {
        List<CourseCalendar> calendar = calendarService.getCourseCalendar(courseId);
        return Result.success(calendar);
    }

    @Operation(summary = "获取指定日期范围的课程日历")
    @GetMapping("/{courseId}/range")
    public Result<List<CourseCalendar>> getCalendarByDateRange(
            @PathVariable Long courseId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<CourseCalendar> calendar = calendarService.getCalendarByDateRange(courseId, startDate, endDate);
        return Result.success(calendar);
    }

    @Operation(summary = "获取学生所有课程日历")
    @PostMapping("/student")
    public Result<List<CourseCalendar>> getStudentCalendar(
            @Parameter(description = "课程ID列表") @RequestBody List<Long> courseIds,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<CourseCalendar> calendar = calendarService.getStudentCalendar(courseIds, startDate, endDate);
        return Result.success(calendar);
    }

    @Operation(summary = "创建日历事件")
    @PostMapping("/add")
    public Result<CourseCalendar> createCalendarEvent(@RequestBody CourseCalendar calendar) {
        CourseCalendar result = calendarService.createCalendarEvent(calendar);
        return Result.success(result);
    }

    @Operation(summary = "更新日历事件")
    @PutMapping("/update")
    public Result<CourseCalendar> updateCalendarEvent(@RequestBody CourseCalendar calendar) {
        CourseCalendar result = calendarService.updateCalendarEvent(calendar);
        return Result.success(result);
    }

    @Operation(summary = "删除日历事件")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCalendarEvent(@PathVariable Long id) {
        calendarService.deleteCalendarEvent(id);
        return Result.success();
    }

    @Operation(summary = "取消日历事件")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancelCalendarEvent(@PathVariable Long id) {
        calendarService.cancelCalendarEvent(id);
        return Result.success();
    }

    @Operation(summary = "批量生成课程日历")
    @PostMapping("/generate/{courseId}")
    public Result<Integer> generateCourseCalendar(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学期开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "学期结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        int count = calendarService.generateCourseCalendar(courseId, startDate, endDate);
        return Result.success(count);
    }

    @Operation(summary = "获取课程的原始日历记录（不展开）")
    @GetMapping("/raw/{courseId}")
    public Result<List<CourseCalendar>> getRawCourseCalendar(@PathVariable Long courseId) {
        List<CourseCalendar> calendar = calendarService.getRawCourseCalendar(courseId);
        return Result.success(calendar);
    }
}
