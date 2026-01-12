# 微服务架构诊断报告：重构建议与应急方案（第二部分）

## 🎯 问题4：重构建议（权衡时间压力）

### 4.1 理想方案：拆分 teacher-service（不推荐今晚执行）

**理想架构**：
```
┌─────────────────────────────────────────────────────────────┐
│                     理想架构（正确）                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  student-service    teacher-service    course-service        │
│  ┌──────────┐      ┌──────────┐      ┌──────────┐          │
│  │ Student  │      │ Teacher  │      │  Course  │          │
│  │ Service  │      │ Service  │      │  Service │          │
│  └──────────┘      └──────────┘      └──────────┘          │
│       │                 │                  │                 │
│       ▼                 ▼                  ▼                 │
│  ┌──────────┐      ┌──────────┐      ┌──────────┐          │
│  │ student  │      │ teacher  │      │  course  │          │
│  │   表     │      │   表     │      │   表     │          │
│  └──────────┘      └──────────┘      └──────────┘          │
│                                                               │
│              auth-service (统一认证)                          │
│              ┌─────────────────┐                             │
│              │  User 表        │                             │
│              │  - student_id   │                             │
│              │  - teacher_id   │                             │
│              └─────────────────┘                             │
└─────────────────────────────────────────────────────────────┘
```

**拆分步骤**（需要2-3天）：
1. 创建 teacher-service 模块
2. 迁移 TeacherInfo 相关代码
3. 修改 User 表，添加 teacher_id 字段
4. 修改 auth-service，支持教师登录
5. 修改 course-service，移除教师管理逻辑
6. 添加 Feign 客户端，实现服务间调用
7. 全面测试

**时间评估**：
- 代码迁移：4-6小时
- 数据库迁移：1-2小时
- 服务间调用改造：2-3小时
- 测试和调试：3-4小时
- **总计：10-15小时**

**结论**：❌ **今晚不推荐执行**，风险太高，时间不够

---

### 4.2 今晚应急方案：最小化改动的"打补丁"策略

#### 方案A：保持现状 + 添加权限校验逻辑（推荐）⭐

**核心思路**：
- 不改动服务拆分
- 在 course-service 中添加教师自助接口
- 使用数据权限拦截器实现细粒度权限控制

**具体步骤**：

##### 步骤1：添加 teacher_id 到 User 表（5分钟）

```sql
-- 紧急修复：添加 teacher_id 字段
ALTER TABLE user ADD COLUMN teacher_id BIGINT COMMENT '教师ID（当用户类型为teacher时关联）';
ALTER TABLE user ADD INDEX idx_teacher_id (teacher_id);

-- 数据修复：同步现有教师的 teacher_id
UPDATE user u
INNER JOIN teacher_info t ON t.user_id = u.id
SET u.teacher_id = t.id
WHERE u.user_type = 'teacher';
```

##### 步骤2：修改 AuthService 登录逻辑（10分钟）

```java
// AuthService.java:139
public Map<String, Object> login(...) {
    // 原有逻辑...

    // 新增：如果是教师，查询 teacher_id
    Long teacherId = null;
    if ("teacher".equals(user.getUserType())) {
        teacherId = user.getTeacherId();
    }

    // 生成 Token 时包含 teacherId
    String token = JwtUtil.generateToken(
        user.getId(),
        user.getUsername(),
        user.getUserType(),
        user.getStudentId(),
        teacherId  // 新增参数
    );

    result.put("teacherId", teacherId);
    return result;
}
```

##### 步骤3：修改 Gateway 注入 teacherId（10分钟）

```java
// Gateway 的 AuthFilter.java
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 解析 Token
    Claims claims = JwtUtil.parseToken(token);

    // 注入请求头
    request = request.mutate()
        .header("userId", userId)
        .header("username", username)
        .header("userType", userType)
        .header("studentId", studentId)
        .header("teacherId", teacherId)  // 新增
        .build();
}
```

##### 步骤4：添加教师自助接口（30分钟）

```java
// TeacherInfoController.java - 新增方法

/**
 * 教师查看自己的信息（不需要管理员权限）
 */
@Operation(summary = "获取当前教师信息")
@GetMapping("/my-info")
@RequireRole({"teacher"})  // 教师可以访问
public Result<TeacherInfo> getMyInfo(
        @RequestHeader(value = "teacherId", required = false) Long teacherId) {
    if (teacherId == null) {
        throw new BusinessException("教师ID不能为空");
    }
    TeacherInfo teacher = teacherInfoService.getTeacherById(teacherId);
    return Result.success(teacher);
}

/**
 * 教师修改自己的信息（不需要管理员权限）
 */
@Operation(summary = "修改当前教师信息")
@PutMapping("/my-info")
@RequireRole({"teacher"})
public Result<TeacherInfo> updateMyInfo(
        @RequestHeader(value = "teacherId", required = false) Long teacherId,
        @RequestBody TeacherInfo teacher) {
    if (teacherId == null) {
        throw new BusinessException("教师ID不能为空");
    }

    // 只能修改自己的信息
    if (!teacherId.equals(teacher.getId())) {
        throw new BusinessException("只能修改自己的信息");
    }

    // 限制可修改的字段（不能修改工号、部门等敏感信息）
    TeacherInfo existingTeacher = teacherInfoService.getTeacherById(teacherId);
    existingTeacher.setPhone(teacher.getPhone());
    existingTeacher.setEmail(teacher.getEmail());
    // 其他允许修改的字段...

    TeacherInfo result = teacherInfoService.updateTeacher(existingTeacher);
    return Result.success(result);
}
```

##### 步骤5：实现"教师只能修改自己课程的成绩"（20分钟）

```java
// CourseEnrollmentService.java:147
@Transactional(rollbackFor = Exception.class)
public void updateScore(Long enrollmentId, BigDecimal score, String grade, Long teacherId) {
    // 1. 查询选课记录
    CourseEnrollment enrollment = enrollmentMapper.selectById(enrollmentId);
    if (enrollment == null) {
        throw new BusinessException("选课记录不存在");
    }

    // 2. 查询课程信息
    CourseInfo course = courseInfoService.getCourseById(enrollment.getCourseId());

    // 3. 权限校验：只能修改自己课程的成绩
    if (teacherId != null && !teacherId.equals(course.getTeacherId())) {
        throw new BusinessException("您没有权限修改该课程的成绩");
    }

    // 4. 更新成绩
    enrollment.setScore(score);
    enrollment.setGrade(grade);
    enrollmentMapper.updateById(enrollment);
}
```

```java
// CourseEnrollmentController.java - 修改接口
@Operation(summary = "录入成绩")
@PutMapping("/score/update")
@RequireRole({"admin", "teacher"})  // 管理员和教师都可以
public Result<Void> updateScore(
        @RequestBody UpdateScoreRequest request,
        @RequestHeader(value = "teacherId", required = false) Long teacherId,
        @RequestHeader(value = "userType", required = false) String userType) {

    // 管理员可以修改任何成绩，教师只能修改自己课程的成绩
    Long teacherIdForCheck = "admin".equals(userType) ? null : teacherId;

    courseEnrollmentService.updateScore(
        request.getEnrollmentId(),
        request.getScore(),
        request.getGrade(),
        teacherIdForCheck
    );
    return Result.success();
}
```

**方案A的优点**：
- ✅ 改动最小（约1.5小时）
- ✅ 不需要拆分服务
- ✅ 可以立即实现权限控制
- ✅ 风险可控

**方案A的缺点**：
- ⚠️ 架构问题仍然存在
- ⚠️ 技术债务累积
- ⚠️ 未来需要重构

---

#### 方案B：使用数据权限拦截器（更优雅，但需要2小时）

**核心思路**：
- 利用已有的数据权限拦截器
- 配置教师的数据权限规则
- 自动拦截 SQL，添加权限过滤

**具体步骤**：

##### 步骤1：配置教师的数据权限规则

```sql
-- 插入教师的数据权限规则
INSERT INTO data_permission_rule (role_type, table_name, entity_class, filter_field, filter_operator, context_field, filter_type, description)
VALUES
-- 教师只能看到自己教授的课程
('teacher', 'course_info', 'CourseInfo', 'teacher_id', '=', 'teacherId', 'SIMPLE', '教师只能看到自己教授的课程'),

-- 教师只能看到自己课程的选课学生
('teacher', 'course_enrollment', 'CourseEnrollment', 'course_id', 'IN', 'teacherId', 'SUBQUERY', '教师只能看到自己课程的选课学生');

-- 为 course_enrollment 设置子查询
UPDATE data_permission_rule
SET subquery_sql = 'SELECT id FROM course_info WHERE teacher_id = ?'
WHERE role_type = 'teacher' AND table_name = 'course_enrollment';
```

##### 步骤2：修改 PermissionInterceptor 注入 teacherId

```java
// PermissionInterceptor.java
@Override
public boolean preHandle(HttpServletRequest request, ...) {
    String teacherId = request.getHeader("teacherId");

    // 设置到 UserContext
    UserContext.setTeacherId(teacherId != null ? Long.parseLong(teacherId) : null);

    return true;
}
```

##### 步骤3：自动生效

**无需修改业务代码**，数据权限拦截器会自动：
- 拦截所有 SELECT 查询
- 自动添加 `WHERE teacher_id = ?` 条件
- 教师只能查询到自己的课程和学生

**方案B的优点**：
- ✅ 更优雅，符合 AOP 思想
- ✅ 业务代码无需修改
- ✅ 统一的权限控制逻辑
- ✅ 易于维护和扩展

**方案B的缺点**：
- ⚠️ 需要理解数据权限拦截器的原理
- ⚠️ 调试相对复杂
- ⚠️ 需要2小时实现和测试

---

### 4.3 今晚推荐方案：方案A + 方案B 混合

**时间分配**（总计2小时）：
1. **立即执行方案A的步骤1-3**（25分钟）
   - 添加 teacher_id 字段
   - 修改登录逻辑
   - 修改 Gateway 注入

2. **执行方案A的步骤4**（30分钟）
   - 添加教师自助接口
   - 测试基本功能

3. **执行方案B**（1小时）
   - 配置数据权限规则
   - 修改拦截器
   - 全面测试

4. **预留缓冲时间**（5分钟）
   - 处理意外问题

**优先级**：
- P0：步骤1-3（必须完成，否则教师无法登录）
- P1：步骤4（教师自助功能）
- P2：方案B（数据权限自动拦截）

---

## 🎯 问题5：最稳妥的权限拦截实现方案

### 5.1 三层权限控制架构

```
┌─────────────────────────────────────────────────────────────┐
│                   三层权限控制架构                            │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  第一层：接口级权限（@RequireRole）                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ @RequireRole({"admin", "teacher"})                    │  │
│  │ 控制哪些角色可以访问该接口                              │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ↓                                    │
│  第二层：业务级权限（Service层校验）                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ if (!teacherId.equals(course.getTeacherId())) {      │  │
│  │     throw new BusinessException("无权操作");          │  │
│  │ }                                                      │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ↓                                    │
│  第三层：数据级权限（MyBatis拦截器）                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ SELECT * FROM course_info                             │  │
│  │ WHERE teacher_id = ?  ← 自动添加                      │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 具体实现方案

#### 第一层：接口级权限

```java
// 管理员专用接口
@RequireRole({"admin"})
@PostMapping("/teacher/add")
public Result<TeacherInfo> addTeacher(...) { }

// 教师和管理员都可以访问
@RequireRole({"admin", "teacher"})
@PutMapping("/score/update")
public Result<Void> updateScore(...) { }

// 教师专用接口
@RequireRole({"teacher"})
@GetMapping("/teacher/my-info")
public Result<TeacherInfo> getMyInfo(...) { }
```

#### 第二层：业务级权限

```java
// Service 层添加权限校验
public void updateScore(Long enrollmentId, BigDecimal score, String grade, Long teacherId) {
    // 查询课程信息
    CourseInfo course = courseInfoService.getCourseById(enrollment.getCourseId());

    // 权限校验：教师只能修改自己课程的成绩
    if (teacherId != null && !teacherId.equals(course.getTeacherId())) {
        throw new BusinessException("您没有权限修改该课程的成绩");
    }

    // 业务逻辑...
}
```

#### 第三层：数据级权限

```java
// 配置数据权限规则（自动生效）
INSERT INTO data_permission_rule (...)
VALUES ('teacher', 'course_info', 'CourseInfo', 'teacher_id', '=', 'teacherId', 'SIMPLE', ...);

// 查询时自动添加过滤条件
// 原始SQL: SELECT * FROM course_info
// 拦截后: SELECT * FROM course_info WHERE teacher_id = ?
```

### 5.3 权限校验的最佳实践

#### 原则1：最小权限原则

```java
// ❌ 错误：给予过大权限
@RequireRole({"admin"})  // 只有管理员可以查看教师列表

// ✅ 正确：按需分配权限
@RequireRole({"admin", "teacher"})  // 教师也可以查看教师列表（用于选择授课教师）
```

#### 原则2：纵深防御

```java
// 不要只依赖一层权限控制
@RequireRole({"admin", "teacher"})  // 第一层：接口级
public Result<Void> updateScore(...) {
    // 第二层：业务级
    if (teacherId != null && !teacherId.equals(course.getTeacherId())) {
        throw new BusinessException("无权操作");
    }

    // 第三层：数据级（自动）
    // MyBatis 拦截器会自动过滤数据
}
```

#### 原则3：明确的错误提示

```java
// ❌ 错误：模糊的错误提示
throw new BusinessException("操作失败");

// ✅ 正确：明确的错误提示
throw new BusinessException("您没有权限修改该课程的成绩，只能修改自己教授的课程");
```

---

## 📋 今晚执行清单

### 阶段1：紧急修复（25分钟）⭐⭐⭐

- [ ] 1. 执行 SQL 添加 teacher_id 字段（5分钟）
- [ ] 2. 修改 User 实体类，添加 teacherId 字段（5分钟）
- [ ] 3. 修改 JwtUtil，支持 teacherId 参数（5分钟）
- [ ] 4. 修改 AuthService.login()，返回 teacherId（5分钟）
- [ ] 5. 修改 Gateway 注入 teacherId 请求头（5分钟）

### 阶段2：教师自助功能（30分钟）⭐⭐

- [ ] 6. 添加 TeacherInfoController.getMyInfo()（10分钟）
- [ ] 7. 添加 TeacherInfoController.updateMyInfo()（10分钟）
- [ ] 8. 测试教师登录和查看信息（10分钟）

### 阶段3：成绩权限控制（20分钟）⭐⭐

- [ ] 9. 修改 CourseEnrollmentService.updateScore()（10分钟）
- [ ] 10. 修改 CourseEnrollmentController.updateScore()（5分钟）
- [ ] 11. 测试教师录入成绩权限（5分钟）

### 阶段4：数据权限拦截（1小时）⭐

- [ ] 12. 配置教师的数据权限规则（10分钟）
- [ ] 13. 修改 PermissionInterceptor 注入 teacherId（10分钟）
- [ ] 14. 修改 UserContext 添加 teacherId 字段（10分钟）
- [ ] 15. 全面测试数据权限拦截（30分钟）

**总计时间：2小时15分钟**

---

## ⚠️ 风险提示

### 高风险操作

1. **修改 User 表结构**
   - 风险：可能影响现有功能
   - 缓解：先备份数据库
   - 回滚：`ALTER TABLE user DROP COLUMN teacher_id;`

2. **修改 JWT Token 结构**
   - 风险：旧 Token 可能失效
   - 缓解：保持向后兼容，teacherId 为可选参数
   - 回滚：用户重新登录即可

3. **修改数据权限拦截器**
   - 风险：可能影响现有权限控制
   - 缓解：先在测试环境验证
   - 回滚：删除教师的数据权限规则

### 测试检查点

- [ ] 管理员可以创建教师
- [ ] 教师可以登录
- [ ] 教师可以查看自己的信息
- [ ] 教师可以修改自己的信息
- [ ] 教师只能看到自己的课程
- [ ] 教师只能修改自己课程的成绩
- [ ] 教师不能修改其他教师课程的成绩
- [ ] 管理员可以修改任何成绩

---

**下一部分：数据一致性解决方案和总结**
