package com.student.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.exception.BusinessException;
import com.student.course.entity.CourseEnrollment;
import com.student.course.entity.CourseInfo;
import com.student.course.mapper.CourseEnrollmentMapper;
import com.student.course.vo.MyEnrollmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 学生选课服务
 */
@Slf4j
@Service
public class CourseEnrollmentService {

    @Autowired
    private CourseEnrollmentMapper enrollmentMapper;

    @Autowired
    private CourseInfoService courseInfoService;

    /**
     * 学生选课
     */
    @Transactional(rollbackFor = Exception.class)
    public CourseEnrollment enrollCourse(Long courseId, Long studentId, String studentName,
            String studentNumber, Long classId, String className) {
        // 检查课程是否存在
        CourseInfo course = courseInfoService.getCourseById(courseId);

        // 检查课程状态
        if (course.getStatus() == 0) {
            throw new BusinessException("该课程已停用，不能选课");
        }
        if (course.getStatus() == 2) {
            throw new BusinessException("该课程选课人数已满");
        }

        // 检查是否已选过该课程
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getStatus, 1); // 已选课状态

        if (enrollmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("您已选过该课程");
        }

        // 创建选课记录
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStudentName(studentName);
        enrollment.setStudentNumber(studentNumber);
        enrollment.setClassId(classId);
        enrollment.setClassName(className);
        enrollment.setEnrollmentTime(LocalDateTime.now());
        enrollment.setStatus(1); // 已选课

        enrollmentMapper.insert(enrollment);

        // 增加课程选课人数
        courseInfoService.incrementEnrollment(courseId);

        log.info("学生选课成功: studentId={}, courseId={}, courseName={}",
                studentId, courseId, course.getCourseName());

        return enrollment;
    }

    /**
     * 学生退课
     */
    @Transactional(rollbackFor = Exception.class)
    public void dropCourse(Long enrollmentId, Long studentId) {
        CourseEnrollment enrollment = enrollmentMapper.selectById(enrollmentId);

        if (enrollment == null) {
            throw new BusinessException("选课记录不存在");
        }

        if (!enrollment.getStudentId().equals(studentId)) {
            throw new BusinessException("您没有权限退课");
        }

        if (enrollment.getStatus() == 0) {
            throw new BusinessException("该课程已退课");
        }

        // 更新选课状态为已退课
        enrollment.setStatus(0);
        enrollmentMapper.updateById(enrollment);

        // 减少课程选课人数
        courseInfoService.decrementEnrollment(enrollment.getCourseId());

        log.info("学生退课成功: studentId={}, courseId={}", studentId, enrollment.getCourseId());
    }

    /**
     * 查询学生已选课程列表
     */
    public IPage<MyEnrollmentVO> getMyEnrollments(Long studentId, Integer current, Integer size, String courseName,
            String semester) {
        Page<MyEnrollmentVO> page = new Page<>(current, size);
        return enrollmentMapper.selectMyEnrollmentsWithCourse(page, studentId, courseName, semester);
    }

    /**
     * 查询课程的选课学生列表
     */
    public IPage<CourseEnrollment> getCourseStudents(Long courseId, Integer current, Integer size) {
        Page<CourseEnrollment> page = new Page<>(current, size);
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStatus, 1)
                .orderByAsc(CourseEnrollment::getStudentNumber);

        return enrollmentMapper.selectPage(page, wrapper);
    }

    /**
     * 检查学生是否已选某课程
     */
    public boolean isEnrolled(Long courseId, Long studentId) {
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getStatus, 1);

        return enrollmentMapper.selectCount(wrapper) > 0;
    }

    /**
     * 录入成绩
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateScore(Long enrollmentId, java.math.BigDecimal score, String grade) {
        CourseEnrollment enrollment = enrollmentMapper.selectById(enrollmentId);

        if (enrollment == null) {
            throw new BusinessException("选课记录不存在");
        }

        enrollment.setScore(score);
        enrollment.setGrade(grade);
        enrollmentMapper.updateById(enrollment);

        log.info("录入成绩成功: enrollmentId={}, score={}, grade={}", enrollmentId, score, grade);
    }
}
