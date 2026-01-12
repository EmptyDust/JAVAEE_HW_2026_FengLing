package com.student.teacher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.student.teacher", "com.student.common"})
@EnableDiscoveryClient
public class TeacherServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeacherServiceApplication.class, args);
    }
}
