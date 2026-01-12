package com.student.course.feign;

import com.student.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 学生服务Feign客户端
 */
@FeignClient(name = "student-service")
public interface StudentFeignClient {

    /**
     * 获取所有学生的用户ID列表
     */
    @GetMapping("/student/user-ids")
    Result<List<Long>> getAllStudentUserIds();
}
