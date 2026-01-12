package com.student.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.context.UserContext;
import com.student.common.context.UserContextHolder;
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

            // 使用自定义方法查询选课记录，避免数据权限拦截器冲突
            List<CourseEnrollment> enrollments = enrollmentMapper.selectByStudentAndCourses(studentId, courseIds);
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

        // 教师不允许修改授课教师字段
        UserContext context = UserContextHolder.getContext();
        if (context != null && context.isTeacher()) {
            // 检查是否修改了 teacher_id
            if (courseInfo.getTeacherId() != null &&
                !courseInfo.getTeacherId().equals(existCourse.getTeacherId())) {
                throw new BusinessException("教师不允许修改授课教师");
            }
            // 检查是否修改了 teacher_name
            if (courseInfo.getTeacherName() != null &&
                !courseInfo.getTeacherName().equals(existCourse.getTeacherName())) {
                throw new BusinessException("教师不允许修改授课教师姓名");
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
     * 增加选课人数（原子操作，防止超卖）
     *
     * 使用数据库层面的原子更新，避免并发问题：
     * 1. 使用 enrolled_students = enrolled_students + 1 确保原子性
     * 2. 使用 WHERE enrolled_students < max_students 防止超卖
     * 3. 使用 WHERE status = 1 确保课程处于可选状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementEnrollment(Long courseId) {
        // 使用原子更新SQL，返回影响的行数
        int affectedRows = courseInfoMapper.incrementEnrollmentAtomic(courseId);

        if (affectedRows == 0) {
            // 更新失败，可能是课程已满或课程状态不可选
            CourseInfo course = getCourseById(courseId);

            if (course.getStatus() == 0) {
                throw new BusinessException("该课程已停用，不能选课");
            } else if (course.getStatus() == 2) {
                throw new BusinessException("该课程选课人数已满");
            } else {
                // 其他情况：可能是并发导致的已满
                int enrolled = course.getEnrolledStudents() == null ? 0 : course.getEnrolledStudents();
                int max = course.getMaxStudents() == null ? Integer.MAX_VALUE : course.getMaxStudents();

                if (enrolled >= max) {
                    throw new BusinessException("该课程选课人数已满");
                } else {
                    throw new BusinessException("选课失败，请重试");
                }
            }
        }

        log.debug("课程选课人数+1成功: courseId={}", courseId);
    }

    /**
     * 减少选课人数（原子操作）
     *
     * 使用数据库层面的原子更新，避免并发问题
     */
    @Transactional(rollbackFor = Exception.class)
    public void decrementEnrollment(Long courseId) {
        // 使用原子更新SQL
        int affectedRows = courseInfoMapper.decrementEnrollmentAtomic(courseId);

        if (affectedRows > 0) {
            log.debug("课程选课人数-1成功: courseId={}", courseId);
        } else {
            log.warn("课程选课人数-1失败，课程可能不存在: courseId={}", courseId);
        }
    }

    /**
     * 按学期过滤课程ID列表
     *
     * @param courseIds 原始课程ID列表
     * @param semester 学期名称（如 "2024-2025-2"）
     * @return 过滤后的课程ID列表（只包含该学期的课程）
     */
    public List<Long> filterCourseIdsBySemester(List<Long> courseIds, String semester) {
        if (courseIds == null || courseIds.isEmpty()) {
            return new ArrayList<>();
        }

        if (semester == null || semester.isEmpty()) {
            return courseIds;
        }

        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CourseInfo::getId, courseIds)
               .eq(CourseInfo::getSemester, semester);

        List<CourseInfo> courses = courseInfoMapper.selectList(wrapper);
        return courses.stream()
                .map(CourseInfo::getId)
                .collect(Collectors.toList());
    }
}
