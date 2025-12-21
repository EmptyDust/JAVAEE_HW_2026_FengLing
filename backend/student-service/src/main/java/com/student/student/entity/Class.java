package com.student.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class")
public class Class extends BaseEntity {
    private String className;
    private String classNo;
    private Long gradeId;
    private Long teacherId;
}
