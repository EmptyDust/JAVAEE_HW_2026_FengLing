package com.student.course.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 学期配置视图对象
 */
@Data
public class SemesterVO {
    /**
     * 学期名称：如 2024-2025-1, 2024-2025-2
     */
    private String semester;

    /**
     * 学期开始日期
     */
    private LocalDate startDate;

    /**
     * 学期结束日期
     */
    private LocalDate endDate;

    /**
     * 是否为当前学期
     */
    private Boolean isCurrent;
}
