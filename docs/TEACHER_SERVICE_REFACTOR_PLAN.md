# 教师模块架构重构方案（完全对称学生模块）

## ✅ 已完成的改动

### 1. Auth 服务升级

#### 1.1 User 实体类
**文件**: `backend/auth-service/src/main/java/com/student/auth/entity/User.java`

**改动**：
```java
// 新增字段
private Long teacherId;  // 教师ID (当用户类型为 teacher 时有值)
```

#### 1.2 JwtUtil 工具类
**文件**: `backend/common/src/main/java/com/student/common/util/JwtUtil.java`

**改动**：
```java
// 新增方法重载
public static String generateToken(Long userId, String username, String userType, Long studentId, Long teacherId)

// 新增解析方法
public static Long getTeacherId(String token)
```

#### 1.3 AuthService 登录逻辑
**文件**: `backend/auth-service/src/main/java/com/student/auth/service/AuthService.java`

**改动**：
```java
// 生成Token时包含teacherId
String token = JwtUtil.generateToken(
    user.getId(),
    user.getUsername(),
    user.getUserType(),
    user.getStudentId(),
    user.getTeacherId()  // ✅ 新增
);

// 返回结果中包含teacherId
result.put("teacherId", user.getTeacherId());  // ✅ 新增
```

### 2. Gateway 适配

**文件**: `backend/gateway/src/main/java/com/student/gateway/filter/AuthFilter.java`

**改动**：
```java
// 解析Token获取teacherId
Long teacherId = JwtUtil.getTeacherId(token);  // ✅ 新增

// 注入请求头
if (teacherId != null) {
    builder.header("teacherId", String.valueOf(teacherId));  // ✅ 新增
}
```

---

## 📋 第三步：独立 Teacher 服务设计

### 3.1 创建 teacher-service 模块结构

```
backend/teacher-service/
├── pom.xml
└── src/main/java/com/student/teacher/
    ├── TeacherServiceApplication.java
    ├── controller/
    │   └── TeacherController.java
    ├── service/
    │   └── TeacherService.java
    ├── mapper/
    │   └── TeacherMapper.java
    ├── entity/
    │   └── Teacher.java
    └── client/
        └── FileServiceClient.java
```

### 3.2 核心代码实现

#### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.student</groupId>
        <artifactId>student-system</artifactId>
        <version>1.0.0</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>teacher-service</artifactId>
    <name>Teacher Service</name>

    <dependencies>
        <!-- Common Module -->
        <dependency>
            <groupId>com.student</groupId>
            <artifactId>common</artifactId>
            <version>1.0.0</version>
        </dependency>

        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Nacos Discovery -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>

        <!-- OpenFeign -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
    </dependencies>
</project>
```

#### Teacher 实体类
```java
package com.student.teacher.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.student.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teacher")
public class Teacher extends BaseEntity {
    /**
     * 教师工号
     */
    private String teacherNo;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 部门
     */
    private String department;

    /**
     * 职称
     */
    private String title;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像文件ID
     */
    private Long avatarFileId;

    /**
     * 关联的用户ID
     */
    private Long userId;

    /**
     * 状态 (0-禁用, 1-启用)
     */
    private Integer status;
}
```

#### TeacherController（完全对称学生模块）
```java
package com.student.teacher.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.common.annotation.RequireRole;
import com.student.common.result.Result;
import com.student.teacher.entity.Teacher;
import com.student.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Tag(name = "教师管理", description = "教师信息管理相关接口")
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * 教师获取自己的信息（完全对称学生的 /student/me）
     */
    @Operation(summary = "获取教师自己的信息", description = "教师登录后获取自己的详细信息")
    @RequireRole({"teacher"})
    @GetMapping("/me")
    public Result<Teacher> getMyInfo(
            @Parameter(hidden = true) @RequestHeader("teacherId") Long teacherId) {

        if (teacherId == null) {
            return Result.error("教师ID不能为空");
        }

        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null) {
            return Result.error("教师信息不存在");
        }
        return Result.success(teacher);
    }

    /**
     * 教师更新自己的信息（完全对称学生的 /student/update）
     */
    @Operation(summary = "更新教师自己的信息", description = "教师只能修改自己的部分信息")
    @RequireRole({"teacher"})
    @PutMapping("/me")
    public Result<?> updateMyInfo(
            @RequestBody Teacher teacher,
            @Parameter(hidden = true) @RequestHeader("teacherId") Long teacherId) {

        if (teacherId == null || !teacherId.equals(teacher.getId())) {
            return Result.error("只能修改自己的信息");
        }

        // 教师只能修改部分字段（不能修改工号等关键信息）
        Teacher existing = teacherService.getById(teacherId);
        if (existing == null) {
            return Result.error("教师信息不存在");
        }

        // 只允许修改电话和邮箱
        existing.setPhone(teacher.getPhone());
        existing.setEmail(teacher.getEmail());
        teacherService.update(existing);

        return Result.success("更新成功");
    }

    /**
     * 查询教师列表（管理员和教师都可以查看）
     */
    @Operation(summary = "查询教师列表", description = "分页查询教师信息")
    @RequireRole({"admin", "teacher"})
    @GetMapping("/list")
    public Result<IPage<Teacher>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "部门") @RequestParam(required = false) String department) {

        return Result.success(teacherService.list(page, size, keyword, department));
    }

    /**
     * 获取所有教师列表（用于下拉选择）
     */
    @Operation(summary = "获取所有教师列表", description = "用于下拉选择")
    @GetMapping("/all")
    public Result<List<Teacher>> getAllTeachers() {
        return Result.success(teacherService.getAllTeachers());
    }

    /**
     * 添加教师（仅管理员）
     */
    @Operation(summary = "添加教师", description = "新增教师信息。仅管理员可操作")
    @RequireRole({"admin"})
    @PostMapping("/add")
    public Result<?> add(@RequestBody Teacher teacher) {
        teacherService.add(teacher);
        return Result.success("添加成功");
    }

    /**
     * 更新教师（仅管理员）
     */
    @Operation(summary = "更新教师", description = "更新教师信息。仅管理员可操作")
    @RequireRole({"admin"})
    @PutMapping("/update")
    public Result<?> update(@RequestBody Teacher teacher) {
        teacherService.update(teacher);
        return Result.success("更新成功");
    }

    /**
     * 删除教师（仅管理员）
     */
    @Operation(summary = "删除教师", description = "根据ID删除教师。仅管理员可操作")
    @RequireRole({"admin"})
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 上传教师头像
     */
    @Operation(summary = "上传教师头像")
    @PostMapping("/avatar/upload")
    public Result<?> uploadAvatar(
            @RequestParam Long teacherId,
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @RequestHeader("userId") Long userId,
            @Parameter(hidden = true) @RequestHeader("username") String username) {

        teacherService.uploadAvatar(teacherId, file, userId, username);
        return Result.success("上传成功");
    }
}
```

---

## 📋 第四步：解耦逻辑建议

### 4.1 管理员创建教师的流程设计

**推荐方案：先创建Teacher，再同步创建User**

#### 流程图
```
管理员                teacher-service         auth-service
  │                        │                      │
  │  POST /teacher/add     │                      │
  ├───────────────────────>│                      │
  │  {teacherNo, name...}  │                      │
  │                        │                      │
  │                        │ 1. 插入 teacher 表   │
  │                        │    获得 teacherId    │
  │                        │                      │
  │                        │ 2. 调用 auth-service │
  │                        │    创建 User 账号    │
  │                        ├─────────────────────>│
  │                        │  Feign: createUser() │
  │                        │  {username, password,│
  │                        │   userType="teacher",│
  │                        │   teacherId}         │
  │                        │                      │
  │                        │                      │ 3. 插入 user 表
  │                        │                      │    user.teacherId = teacherId
  │                        │                      │
  │                        │  返回 userId         │
  │                        │<─────────────────────┤
  │                        │                      │
  │                        │ 4. 回写 userId       │
  │                        │    teacher.userId = userId
  │                        │                      │
  │  返回成功               │                      │
  │<───────────────────────┤                      │
```

#### 代码实现

**TeacherService.java**
```java
@Autowired
private AuthServiceClient authServiceClient;  // Feign客户端

@Transactional(rollbackFor = Exception.class)
public void add(Teacher teacher) {
    // 1. 检查工号是否已存在
    if (teacherMapper.selectCount(
        new LambdaQueryWrapper<Teacher>()
            .eq(Teacher::getTeacherNo, teacher.getTeacherNo())) > 0) {
        throw new BusinessException("教师工号已存在");
    }

    // 2. 插入教师信息，获得teacherId
    teacher.setStatus(1);
    teacherMapper.insert(teacher);
    Long teacherId = teacher.getId();

    try {
        // 3. 调用auth-service创建User账号
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(teacher.getTeacherNo());
        request.setPassword("123456");  // 默认密码
        request.setUserType("teacher");
        request.setTeacherId(teacherId);  // ✅ 关键：传递teacherId
        request.setEmail(teacher.getEmail());
        request.setPhone(teacher.getPhone());

        Result<Long> result = authServiceClient.createUser(request);
        if (result.getCode() != 200) {
            throw new BusinessException("创建用户账号失败: " + result.getMessage());
        }

        // 4. 回写userId到teacher表
        Long userId = result.getData();
        teacher.setUserId(userId);
        teacherMapper.updateById(teacher);

        log.info("创建教师成功: teacherId={}, userId={}", teacherId, userId);

    } catch (Exception e) {
        // 补偿事务：删除已创建的teacher记录
        teacherMapper.deleteById(teacherId);
        log.error("创建教师失败，已回滚: teacherId={}", teacherId, e);
        throw new BusinessException("创建教师失败: " + e.getMessage());
    }
}
```

**AuthServiceClient.java（Feign客户端）**
```java
package com.student.teacher.client;

import com.student.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", path = "/user")
public interface AuthServiceClient {

    @PostMapping("/create")
    Result<Long> createUser(@RequestBody CreateUserRequest request);
}
```

**AuthService.java（新增方法）**
```java
/**
 * 创建用户账号（供其他服务调用）
 */
@Transactional(rollbackFor = Exception.class)
public Long createUser(String username, String password, String userType,
                       Long studentId, Long teacherId, String email, String phone) {
    // 检查用户名是否存在
    if (userMapper.selectCount(
        new LambdaQueryWrapper<User>()
            .eq(User::getUsername, username)) > 0) {
        throw new BusinessException("用户名已存在");
    }

    // 创建用户
    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setUserType(userType);
    user.setStudentId(studentId);
    user.setTeacherId(teacherId);  // ✅ 设置teacherId
    user.setEmail(email);
    user.setPhone(phone);

    // 设置密码安全字段
    user.setPasswordUpdateTime(LocalDateTime.now());
    user.setFailedLoginAttempts(0);
    user.setPasswordExpired(false);

    userMapper.insert(user);

    log.info("创建用户成功: userId={}, username={}, userType={}",
             user.getId(), username, userType);

    return user.getId();
}
```

### 4.2 优点分析

| 优点 | 说明 |
|------|------|
| **职责清晰** | teacher-service负责教师信息，auth-service负责账号 |
| **数据一致性** | 通过补偿事务保证一致性 |
| **完全解耦** | 不再跨服务访问数据库 |
| **架构对称** | 与学生模块完全一致 |

---

**下一部分：数据库迁移和测试方案**
