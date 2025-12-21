package com.student.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.exception.BusinessException;
import com.student.common.result.Result;
import com.student.student.client.FileServiceClient;
import com.student.student.entity.Student;
import com.student.student.mapper.StudentMapper;
import com.student.student.vo.StudentVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private FileServiceClient fileServiceClient;

    public IPage<StudentVO> list(int page, int size, String name, Long classId) {
        Page<StudentVO> pageParam = new Page<>(page, size);
        return studentMapper.selectStudentVOPage(pageParam, name, classId);
    }

    public void add(Student student) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, student.getStudentNo());
        Student exist = studentMapper.selectOne(wrapper);
        if (exist != null) {
            throw new BusinessException("学号已存在");
        }
        studentMapper.insert(student);
    }

    public void update(Student student) {
        studentMapper.updateById(student);
    }

    public void delete(Long id) {
        studentMapper.deleteById(id);
    }

    public Student getById(Long id) {
        return studentMapper.selectById(id);
    }

    public StudentVO getByIdWithClassName(Long id) {
        return studentMapper.selectStudentVOById(id);
    }

    /**
     * 上传学生头像
     */
    public Map<String, Object> uploadAvatar(MultipartFile file, Long studentId, Long userId, String username) {
        // 验证学生是否存在
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }

        // 验证文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("头像文件大小不能超过5MB");
        }

        try {
            // 调用文件服务上传文件
            Result<Map<String, Object>> result = fileServiceClient.upload(
                    file, "avatar", studentId, userId, username);

            if (result.getCode() != 200) {
                throw new BusinessException("文件上传失败：" + result.getMessage());
            }

            // 获取文件信息
            Map<String, Object> data = result.getData();
            Long fileId = Long.valueOf(data.get("id").toString());
            String fileUrl = (String) data.get("fileUrl");

            // 更新学生头像字段
            student.setAvatarFileId(fileId);
            student.setAvatar(fileUrl);
            studentMapper.updateById(student);

            // 返回文件信息
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("avatarFileId", fileId);
            response.put("avatarUrl", fileUrl);
            return response;
        } catch (Exception e) {
            throw new BusinessException("头像上传失败：" + e.getMessage());
        }
    }
}
