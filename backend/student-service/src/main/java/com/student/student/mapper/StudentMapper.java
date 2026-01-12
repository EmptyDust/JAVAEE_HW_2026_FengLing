package com.student.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.student.entity.Student;
import com.student.student.vo.StudentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Select("<script>" +
            "SELECT s.id, s.name, s.student_no, s.class_id, s.gender, s.age, " +
            "s.phone, s.address, s.avatar, s.avatar_file_id as avatarFileId, " +
            "s.create_time, s.update_time, c.class_name as className " +
            "FROM student s " +
            "LEFT JOIN class c ON s.class_id = c.id " +
            "WHERE 1=1 " +
            "<if test='name != null and name != \"\"'>" +
            "  AND s.name LIKE CONCAT('%', #{name}, '%') " +
            "</if>" +
            "<if test='classId != null'>" +
            "  AND s.class_id = #{classId} " +
            "</if>" +
            "ORDER BY s.create_time DESC" +
            "</script>")
    IPage<StudentVO> selectStudentVOPage(Page<?> page, @Param("name") String name, @Param("classId") Long classId);

    @Select("SELECT s.id, s.name, s.student_no, s.class_id, s.gender, s.age, " +
            "s.phone, s.address, s.avatar, s.avatar_file_id as avatarFileId, " +
            "s.create_time, s.update_time, c.class_name as className " +
            "FROM student s " +
            "LEFT JOIN class c ON s.class_id = c.id " +
            "WHERE s.id = #{id}")
    StudentVO selectStudentVOById(@Param("id") Long id);

    /**
     * 获取所有学生的用户ID列表
     * 从user表查询所有学生类型用户的ID
     */
    @Select("SELECT id FROM user WHERE user_type = 'student'")
    java.util.List<Long> selectAllStudentUserIds();
}
