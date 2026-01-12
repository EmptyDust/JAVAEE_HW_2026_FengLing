package com.student.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.exception.BusinessException;
import com.student.common.mq.dto.StudentRegistrationPayload;
import com.student.common.mq.producer.MessageProducer;
import com.student.student.entity.Student;
import com.student.student.mapper.StudentMapper;
import com.student.student.vo.StudentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private MessageProducer messageProducer;

    public IPage<StudentVO> list(int page, int size, String name, Long classId) {
        Page<StudentVO> pageParam = new Page<>(page, size);
        return studentMapper.selectStudentVOPage(pageParam, name, classId);
    }

    public void add(Student student, String password, String email) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, student.getStudentNo());
        Student exist = studentMapper.selectOne(wrapper);
        if (exist != null) {
            throw new BusinessException("学号已存在");
        }

        // 1. 插入学生记录
        studentMapper.insert(student);

        // 2. 发送异步消息到RabbitMQ创建用户账号
        StudentRegistrationPayload payload = StudentRegistrationPayload.builder()
                .studentId(student.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .phone(student.getPhone())
                .email(email) 
                .classId(student.getClassId())
                .password(password) 
                .build();

        messageProducer.sendMessage(
                "user.registration.student",
                "STUDENT_REGISTRATION",
                payload,
                5 // Normal priority
        );

        log.info("Student created and registration message sent: studentId={}, studentNo={}",
                student.getId(), student.getStudentNo());
    }

    @CacheEvict(value = {"student:detail", "student:vo"}, key = "#student.id")
    public void update(Student student) {
        studentMapper.updateById(student);
    }

    @CacheEvict(value = {"student:detail", "student:vo"}, key = "#id")
    public void delete(Long id) {
        studentMapper.deleteById(id);
    }

    @Cacheable(value = "student:detail", key = "#id", unless = "#result == null")
    public Student getById(Long id) {
        return studentMapper.selectById(id);
    }

    @Cacheable(value = "student:vo", key = "#id", unless = "#result == null")
    public StudentVO getByIdWithClassName(Long id) {
        return studentMapper.selectStudentVOById(id);
    }

    /**
     * 更新学生头像
     * 前端需先调用file-service上传文件获取fileId和fileUrl，再调用此方法更新
     */
    @CacheEvict(value = {"student:detail", "student:vo"}, key = "#studentId")
    public void updateAvatar(Long studentId, Long fileId, String fileUrl) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        student.setAvatarFileId(fileId);
        student.setAvatar(fileUrl);
        studentMapper.updateById(student);

        log.info("学生头像更新成功: studentId={}, fileId={}", studentId, fileId);
    }

    /**
     * 获取所有学生的用户ID列表（用于通知系统）
     */
    public java.util.List<Long> getAllStudentUserIds() {
        return studentMapper.selectAllStudentUserIds();
    }
}
