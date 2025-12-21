package com.student.course.service;

import com.student.course.vo.SemesterVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 学期管理服务
 */
@Service
public class SemesterService {

    /**
     * 获取可用学期列表
     */
    public List<SemesterVO> getAvailableSemesters() {
        List<SemesterVO> semesters = new ArrayList<>();

        // 2024-2025学年第一学期（秋季）
        SemesterVO semester1 = new SemesterVO();
        semester1.setSemester("2024-2025-1");
        semester1.setStartDate(LocalDate.of(2024, 9, 1));
        semester1.setEndDate(LocalDate.of(2025, 1, 31));
        semester1.setIsCurrent(false);
        semesters.add(semester1);

        // 2024-2025学年第二学期（春季）
        SemesterVO semester2 = new SemesterVO();
        semester2.setSemester("2024-2025-2");
        semester2.setStartDate(LocalDate.of(2025, 2, 24));
        semester2.setEndDate(LocalDate.of(2025, 6, 30));
        semester2.setIsCurrent(true);  // 当前学期
        semesters.add(semester2);

        // 2025-2026学年第一学期（秋季）
        SemesterVO semester3 = new SemesterVO();
        semester3.setSemester("2025-2026-1");
        semester3.setStartDate(LocalDate.of(2025, 9, 1));
        semester3.setEndDate(LocalDate.of(2026, 1, 31));
        semester3.setIsCurrent(false);
        semesters.add(semester3);

        return semesters;
    }

    /**
     * 根据学期名称获取学期信息
     */
    public SemesterVO getSemesterInfo(String semester) {
        return getAvailableSemesters().stream()
                .filter(s -> s.getSemester().equals(semester))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取当前学期
     */
    public SemesterVO getCurrentSemester() {
        return getAvailableSemesters().stream()
                .filter(SemesterVO::getIsCurrent)
                .findFirst()
                .orElse(null);
    }
}
