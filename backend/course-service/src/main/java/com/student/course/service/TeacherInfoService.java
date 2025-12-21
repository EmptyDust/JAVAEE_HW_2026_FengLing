package com.student.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.exception.BusinessException;
import com.student.common.result.Result;
import com.student.course.client.FileServiceClient;
import com.student.course.entity.CourseInfo;
import com.student.course.entity.TeacherInfo;
import com.student.course.entity.User;
import com.student.course.mapper.CourseInfoMapper;
import com.student.course.mapper.TeacherInfoMapper;
import com.student.course.mapper.UserMapper;
import com.student.course.utils.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 教师信息服务
 */
@Slf4j
@Service
public class TeacherInfoService {

    @Autowired
    private TeacherInfoMapper teacherInfoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseInfoMapper courseInfoMapper;

    @Autowired
    private FileServiceClient fileServiceClient;

    /**
     * 分页查询教师列表
     */
    public IPage<TeacherInfo> getTeacherList(Integer current, Integer size, String keyword, String department, String title) {
        Page<TeacherInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<TeacherInfo> wrapper = new LambdaQueryWrapper<>();

        // 只查询启用状态的教师
        wrapper.eq(TeacherInfo::getStatus, 1);

        // 关键词搜索（工号或姓名）
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TeacherInfo::getTeacherNo, keyword)
                             .or()
                             .like(TeacherInfo::getTeacherName, keyword));
        }

        // 部门筛选
        if (department != null && !department.isEmpty()) {
            wrapper.eq(TeacherInfo::getDepartment, department);
        }

        // 职称筛选
        if (title != null && !title.isEmpty()) {
            wrapper.eq(TeacherInfo::getTitle, title);
        }

        wrapper.orderByDesc(TeacherInfo::getCreateTime);

        return teacherInfoMapper.selectPage(page, wrapper);
    }

    /**
     * 获取所有教师列表（用于下拉选择）
     */
    public List<TeacherInfo> getAllTeachers() {
        LambdaQueryWrapper<TeacherInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherInfo::getStatus, 1)
               .orderBy(true, true, TeacherInfo::getTeacherNo);
        return teacherInfoMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取教师详情
     */
    public TeacherInfo getTeacherById(Long id) {
        TeacherInfo teacher = teacherInfoMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }
        return teacher;
    }

    /**
     * 添加教师
     */
    @Transactional(rollbackFor = Exception.class)
    public TeacherInfo addTeacher(TeacherInfo teacher) {
        // 检查工号是否已存在
        LambdaQueryWrapper<TeacherInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherInfo::getTeacherNo, teacher.getTeacherNo());
        Long count = teacherInfoMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("教师工号已存在");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getUsername, teacher.getTeacherNo());
        Long userCount = userMapper.selectCount(userWrapper);
        if (userCount > 0) {
            throw new BusinessException("用户名已存在，无法创建账号");
        }

        // 1. 创建用户账号
        User user = new User();
        user.setUsername(teacher.getTeacherNo());
        user.setPassword(PasswordUtils.encode("123456")); // 默认密码：123456
        user.setUserType("teacher");
        user.setEmail(teacher.getEmail());
        user.setPhone(teacher.getPhone());
        userMapper.insert(user);

        // 2. 设置教师信息的user_id
        teacher.setUserId(user.getId());

        // 设置默认状态
        if (teacher.getStatus() == null) {
            teacher.setStatus(1);
        }

        // 3. 插入教师信息
        teacherInfoMapper.insert(teacher);

        log.info("添加教师成功并创建账号: teacherNo={}, teacherName={}, userId={}",
                teacher.getTeacherNo(), teacher.getTeacherName(), user.getId());
        return teacher;
    }

    /**
     * 更新教师
     */
    @Transactional(rollbackFor = Exception.class)
    public TeacherInfo updateTeacher(TeacherInfo teacher) {
        TeacherInfo existingTeacher = teacherInfoMapper.selectById(teacher.getId());
        if (existingTeacher == null) {
            throw new BusinessException("教师不存在");
        }

        // 如果修改了工号，检查新工号是否已存在
        if (!existingTeacher.getTeacherNo().equals(teacher.getTeacherNo())) {
            LambdaQueryWrapper<TeacherInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TeacherInfo::getTeacherNo, teacher.getTeacherNo())
                   .ne(TeacherInfo::getId, teacher.getId());
            Long count = teacherInfoMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("教师工号已存在");
            }
        }

        teacherInfoMapper.updateById(teacher);

        // 如果教师姓名发生变化，同步更新课程表中的教师姓名
        if (!existingTeacher.getTeacherName().equals(teacher.getTeacherName())) {
            updateCourseTeacherName(teacher.getId(), teacher.getTeacherName());
        }

        log.info("更新教师成功: id=, teacherNo={}", teacher.getId(), teacher.getTeacherNo());
        return teacher;
    }

    /**
     * 删除教师（逻辑删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeacher(Long id) {
        TeacherInfo teacher = teacherInfoMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }

        // 检查是否有关联的课程
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseInfo::getTeacherId, id);
        Long courseCount = courseInfoMapper.selectCount(wrapper);
        if (courseCount > 0) {
            throw new BusinessException("该教师有关联的课程，无法删除");
        }

        // 逻辑删除
        teacher.setStatus(0);
        teacherInfoMapper.updateById(teacher);

        log.info("删除教师成功: id={}, teacherNo={}", id, teacher.getTeacherNo());
    }

    /**
     * 上传教师头像
     */
    @Transactional(rollbackFor = Exception.class)
    public TeacherInfo uploadAvatar(Long teacherId, MultipartFile file) {
        TeacherInfo teacher = teacherInfoMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }

        try {
            // 调用文件服务上传头像
            Result<Map<String, Object>> uploadResult = fileServiceClient.upload(
                    file,
                    "teacher-avatar",
                    teacherId,
                    null,
                    teacher.getTeacherName()
            );

            if (uploadResult == null || uploadResult.getCode() != 200) {
                throw new BusinessException("头像上传失败");
            }

            Map<String, Object> fileInfo = uploadResult.getData();
            Long fileId = Long.valueOf(fileInfo.get("id").toString());

            // 删除旧头像
            if (teacher.getAvatarFileId() != null) {
                try {
                    fileServiceClient.deleteFile(teacher.getAvatarFileId());
                } catch (Exception e) {
                    log.error("删除旧头像失败: fileId={}", teacher.getAvatarFileId(), e);
                }
            }

            // 更新头像文件ID
            teacher.setAvatarFileId(fileId);
            teacherInfoMapper.updateById(teacher);

            log.info("上传教师头像成功: teacherId={}, fileId={}", teacherId, fileId);
            return teacher;

        } catch (Exception e) {
            log.error("上传教师头像失败", e);
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师的课程列表
     */
    public List<CourseInfo> getTeacherCourses(Long teacherId) {
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseInfo::getTeacherId, teacherId)
               .orderByDesc(CourseInfo::getCreateTime);
        return courseInfoMapper.selectList(wrapper);
    }

    /**
     * 同步更新课程表中的教师姓名
     */
    private void updateCourseTeacherName(Long teacherId, String teacherName) {
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseInfo::getTeacherId, teacherId);
        List<CourseInfo> courses = courseInfoMapper.selectList(wrapper);

        for (CourseInfo course : courses) {
            course.setTeacherName(teacherName);
            courseInfoMapper.updateById(course);
        }

        log.info("同步更新课程教师姓名: teacherId={}, teacherName={}, courseCount={}",
                teacherId, teacherName, courses.size());
    }
}
