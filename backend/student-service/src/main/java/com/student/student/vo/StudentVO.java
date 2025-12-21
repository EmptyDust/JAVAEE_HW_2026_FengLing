package com.student.student.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentVO {
    private Long id;
    private String name;
    private String studentNo;
    private Long classId;
    private String className;
    private String gender;
    private Integer age;
    private String phone;
    private String address;
    private String avatar;
    private Long avatarFileId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
