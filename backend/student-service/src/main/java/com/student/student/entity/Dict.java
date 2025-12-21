package com.student.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dict")
public class Dict extends BaseEntity {
    private String dictType;
    private String dictCode;
    private String dictValue;
    private Integer sort;
}
