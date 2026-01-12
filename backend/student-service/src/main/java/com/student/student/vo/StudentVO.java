package com.student.student.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class StudentVO implements Serializable {
    private static final long serialVersionUID = 1L;

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
