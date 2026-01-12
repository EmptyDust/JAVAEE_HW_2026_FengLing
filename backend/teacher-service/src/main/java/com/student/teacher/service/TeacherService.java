package com.student.teacher.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.exception.BusinessException;
import com.student.common.mq.dto.TeacherNameChangePayload;
import com.student.common.mq.dto.TeacherRegistrationPayload;
import com.student.common.mq.producer.MessageProducer;
import com.student.teacher.entity.CourseInfo;
import com.student.teacher.entity.Teacher;
import com.student.teacher.mapper.TeacherMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 教师服务类
 */
@Slf4j
@Service
public class TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private com.student.teacher.mapper.CourseInfoMapper courseInfoMapper;

    @Autowired
    private MessageProducer messageProducer;

    /**
     * 分页查询教师列表
     */
    public IPage<Teacher> list(int page, int size, String teacherName, String department) {
        Page<Teacher> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();

        if (teacherName != null && !teacherName.isEmpty()) {
            wrapper.like(Teacher::getTeacherName, teacherName);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.eq(Teacher::getDepartment, department);
        }

        return teacherMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 添加教师（通过RabbitMQ异步创建登录账号）
     */
    public void add(Teacher teacher) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getTeacherNo, teacher.getTeacherNo());
        Teacher exist = teacherMapper.selectOne(wrapper);
        if (exist != null) {
            throw new BusinessException("教师工号已存在");
        }

        // 1. 插入教师记录
        teacherMapper.insert(teacher);

        // 2. 发送异步消息到RabbitMQ创建用户账号
        TeacherRegistrationPayload payload = TeacherRegistrationPayload.builder()
                .teacherId(teacher.getId())
                .teacherNo(teacher.getTeacherNo())
                .teacherName(teacher.getTeacherName())
                .phone(teacher.getPhone())
                .email(teacher.getEmail())
                .department(teacher.getDepartment())
                .build();

        messageProducer.sendMessage(
                "user.registration.teacher",
                "TEACHER_REGISTRATION",
                payload,
                5 // Normal priority
        );

        log.info("Teacher created and registration message sent: teacherId={}, teacherNo={}",
                teacher.getId(), teacher.getTeacherNo());
    }

    /**
     * 更新教师信息（检测姓名变更并同步到课程表）
     */
    public void update(Teacher teacher) {
        Teacher existing = teacherMapper.selectById(teacher.getId());
        if (existing == null) {
            throw new BusinessException("教师不存在");
        }

        // 检测姓名是否变更
        boolean nameChanged = !existing.getTeacherName().equals(teacher.getTeacherName());

        // 更新教师记录
        teacherMapper.updateById(teacher);

        // 如果姓名变更，发送同步消息
        if (nameChanged) {
            TeacherNameChangePayload payload = TeacherNameChangePayload.builder()
                    .teacherId(teacher.getId())
                    .oldName(existing.getTeacherName())
                    .newName(teacher.getTeacherName())
                    .updateTime(LocalDateTime.now())
                    .build();

            messageProducer.sendMessage(
                    "teacher.update.name",
                    "TEACHER_NAME_CHANGED",
                    payload,
                    8 // High priority for data consistency
            );

            log.info("Teacher name changed, sync message sent: teacherId={}, oldName={}, newName={}",
                    teacher.getId(), existing.getTeacherName(), teacher.getTeacherName());
        }
    }

    /**
     * 删除教师
     */
    public void delete(Long id) {
        teacherMapper.deleteById(id);
    }

    /**
     * 根据ID查询教师
     */
    public Teacher getById(Long id) {
        return teacherMapper.selectById(id);
    }

    /**
     * 根据用户ID查询教师
     */
    public Teacher getByUserId(Long userId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getUserId, userId);
        return teacherMapper.selectOne(wrapper);
    }

    /**
     * 获取所有教师列表（用于下拉选择）
     */
    public java.util.List<Teacher> getAllTeachers() {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getStatus, 1); // 只查询启用状态的教师
        wrapper.orderByAsc(Teacher::getTeacherNo);
        return teacherMapper.selectList(wrapper);
    }

    /**
     * 更新教师头像
     * 前端需先调用file-service上传文件获取fileId，再调用此方法更新
     */
    public void updateAvatar(Long teacherId, Long fileId) {
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }

        teacher.setAvatarFileId(fileId);
        teacherMapper.updateById(teacher);

        log.info("教师头像更新成功: teacherId={}, fileId={}", teacherId, fileId);
    }

    /**
     * 获取教师的课程列表
     */
    public java.util.List<CourseInfo> getTeacherCourses(Long teacherId) {
        LambdaQueryWrapper<CourseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseInfo::getTeacherId, teacherId);
        wrapper.orderByDesc(CourseInfo::getId);
        return courseInfoMapper.selectList(wrapper);
    }
}
