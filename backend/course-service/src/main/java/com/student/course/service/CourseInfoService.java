package com.student.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.exception.BusinessException;
import com.student.course.entity.CourseEnrollment;
import com.student.course.entity.CourseInfo;
import com.student.course.mapper.CourseEnrollmentMapper;
import com.student.course.mapper.CourseInfoMapper;
import com.student.course.vo.CourseListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程信息服务
 */
@Slf4j
@Service
public class CourseInfoService {

    @Autowired
    private CourseInfoMapper courseInfoMapper;

    @Autowired
    private CourseEnrollmentMapper enrollmentMapper;

    /**
     * 分页查询课程列表（用于选课中心，包含选课状态）
     */
    public IPage<CourseListVO> getCourseListWithEnrollStatus(Integer current, Integer size, String courseName,
                                                              String courseType, String semester, Integer status,
                                                              Long studentId) {
        Page<CourseInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();

        // 条件查询
        if (StringUtils.hasText(courseName)) {
            wrapper.and(w -> w.like(CourseInfo::getCourseName, courseName)
                    .or()
                    .like(CourseInfo::getCourseCode, courseName));
        }
        if (StringUtils.hasText(courseType)) {
            wrapper.eq(CourseInfo::getCourseType, courseType);
        }
        if (StringUtils.hasText(semester)) {
            wrapper.eq(CourseInfo::getSemester, semester);
        }
        if (status != null) {
            wrapper.eq(CourseInfo::getStatus, status);
        }

        wrapper.orderByDesc(CourseInfo::getCreateTime);

        IPage<CourseInfo> coursePage = courseInfoMapper.selectPage(page, wrapper);

        // 批量查询学生的选课状态
        Set<Long> enrolledCourseIds = null;
        if (studentId != null && !coursePage.getRecords().isEmpty()) {
            List<Long> courseIds = coursePage.getRecords().stream()
                    .map(CourseInfo::getId)
                    .collect(Collectors.toList());

            LambdaQueryWrapper<CourseEnrollment> enrollWrapper = new LambdaQueryWrapper<>();
            enrollWrapper.eq(CourseEnrollment::getStudentId, studentId)
                    .in(CourseEnrollment::getCourseId, courseIds)
                    .eq(CourseEnrollment::getStatus, 1);

            List<CourseEnrollment> enrollments = enrollmentMapper.selectList(enrollWrapper);
            enrolledCourseIds = enrollments.stream()
                    .map(CourseEnrollment::getCourseId)
                    .collect(Collectors.toSet());
        }

        // 转换为VO
        List<CourseListVO> voList = new ArrayList<>();
        Set<Long> finalEnrolledCourseIds = enrolledCourseIds;
        for (CourseInfo course : coursePage.getRecords()) {
            CourseListVO vo = new CourseListVO();
            BeanUtils.copyProperties(course, vo);
            vo.setEnrolled(finalEnrolledCourseIds != null && finalEnrolledCourseIds.contains(course.getId()));
            voList.add(vo);
        }

        // 构建返回的分页对象 - 保留原分页信息
        Page<CourseListVO> voPage = new Page<>();
        voPage.setCurrent(coursePage.getCurrent());
        voPage.setSize(coursePage.getSize());
        voPage.setTotal(coursePage.getTotal());
        voPage.setPages(coursePage.getPages());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 分页查询课程列表
     */
    public IPage<CourseInfo> getCourseList(Integer current, Integer size, String courseName,
                                            String courseType, String semester, Integer status) {
        Page<CourseInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();

        // 条件查询
        if (StringUtils.hasText(courseName)) {
            wrapper.like(CourseInfo::getCourseName, courseName)
                   .or()
                   .like(CourseInfo::getCourseCode, courseName);
        }
        if (StringUtils.hasText(courseType)) {
            wrapper.eq(CourseInfo::getCourseType, courseType);
        }
        if (StringUtils.hasText(semester)) {
            wrapper.eq(CourseInfo::getSemester, semester);
        }
        if (status != null) {
            wrapper.eq(CourseInfo::getStatus, status);
        }

        wrapper.orderByDesc(CourseInfo::getCreateTime);

        return courseInfoMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID获取课程详情
     */
    public CourseInfo getCourseById(Long id) {
        CourseInfo course = courseInfoMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return course;
    }

    /**
     * 创建课程
     */
    @Transactional(rollbackFor = Exception.class)
    public CourseInfo createCourse(CourseInfo courseInfo) {
        // 检查课程编号是否重复
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseInfo::getCourseCode, courseInfo.getCourseCode());
        if (courseInfoMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("课程编号已存在");
        }

        // 设置初始值
        if (courseInfo.getEnrolledStudents() == null) {
            courseInfo.setEnrolledStudents(0);
        }
        if (courseInfo.getStatus() == null) {
            courseInfo.setStatus(1); // 默认启用
        }

        courseInfoMapper.insert(courseInfo);
        log.info("创建课程成功: {} - {}", courseInfo.getCourseCode(), courseInfo.getCourseName());
        return courseInfo;
    }

    /**
     * 更新课程
     */
    @Transactional(rollbackFor = Exception.class)
    public CourseInfo updateCourse(CourseInfo courseInfo) {
        CourseInfo existCourse = courseInfoMapper.selectById(courseInfo.getId());
        if (existCourse == null) {
            throw new BusinessException("课程不存在");
        }

        // 如果修改了课程编号，检查是否重复
        if (!existCourse.getCourseCode().equals(courseInfo.getCourseCode())) {
            LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CourseInfo::getCourseCode, courseInfo.getCourseCode())
                   .ne(CourseInfo::getId, courseInfo.getId());
            if (courseInfoMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("课程编号已存在");
            }
        }

        courseInfoMapper.updateById(courseInfo);
        log.info("更新课程成功: {} - {}", courseInfo.getCourseCode(), courseInfo.getCourseName());
        return courseInfo;
    }

    /**
     * 删除课程
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        CourseInfo course = courseInfoMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 检查是否有学生已选课
        if (course.getEnrolledStudents() != null && course.getEnrolledStudents() > 0) {
            throw new BusinessException("该课程已有学生选课，不能删除");
        }

        courseInfoMapper.deleteById(id);
        log.info("删除课程成功: {} - {}", course.getCourseCode(), course.getCourseName());
    }

    /**
     * 增加选课人数
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementEnrollment(Long courseId) {
        CourseInfo course = getCourseById(courseId);
        int enrolled = course.getEnrolledStudents() == null ? 0 : course.getEnrolledStudents();
        int max = course.getMaxStudents() == null ? Integer.MAX_VALUE : course.getMaxStudents();

        if (enrolled >= max) {
            throw new BusinessException("课程选课人数已满");
        }

        course.setEnrolledStudents(enrolled + 1);

        // 如果达到最大人数，更新状态为已满
        if (course.getEnrolledStudents() >= course.getMaxStudents()) {
            course.setStatus(2); // 已满
        }

        courseInfoMapper.updateById(course);
    }

    /**
     * 减少选课人数
     */
    @Transactional(rollbackFor = Exception.class)
    public void decrementEnrollment(Long courseId) {
        CourseInfo course = getCourseById(courseId);
        int enrolled = course.getEnrolledStudents() == null ? 0 : course.getEnrolledStudents();

        if (enrolled > 0) {
            course.setEnrolledStudents(enrolled - 1);

            // 如果原来是已满状态，恢复为启用状态
            if (course.getStatus() == 2) {
                course.setStatus(1);
            }

            courseInfoMapper.updateById(course);
        }
    }
}
