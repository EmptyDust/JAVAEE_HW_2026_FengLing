package com.student.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {
    private String name;
    private String studentNo;
    private Long classId;
    private String gender;
    private Integer age;
    private String phone;
    private String address;
    private String avatar;
    private Long avatarFileId;
}
