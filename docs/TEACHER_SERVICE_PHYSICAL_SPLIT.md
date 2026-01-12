# Teacher Service 物理拆分重构方案

## 📋 重构目标

完全模仿 student-service，创建独立的 teacher-service，严禁使用 Feign。

---

## ✅ 已完成的准备工作

### 1. Auth 服务（数据源层）

**SQL 脚本**：`scripts/emergency_fix_teacher_id.sql`
```sql
ALTER TABLE user ADD COLUMN teacher_id BIGINT;
ALTER TABLE user ADD INDEX idx_teacher_id (teacher_id);
```

**User 实体**：已添加 `teacherId` 字段 ✅

---

### 2. Auth 服务（登录层）

**AuthService.login()**：已返回 `teacherId` ✅
```java
result.put("teacherId", user.getTeacherId());
```

**JwtUtil**：已支持 `teacherId` ✅
```java
public static String generateToken(..., Long teacherId)
public static Long getTeacherId(String token)
```

---

### 3. Gateway 过滤器

**AuthFilter**：已注入 `teacherId` ✅
```java
Long teacherId = JwtUtil.getTeacherId(token);
if (teacherId != null) {
    builder.header("teacherId", String.valueOf(teacherId));
}
```

---

## 🔧 需要执行的重构任务

### 任务1：修改包名和类名

**当前问题**：teacher-service 中的代码都是 student 相关的

**需要修改**：
- 包名：`com.student.student` → `com.student.teacher`
- 主类：`StudentApplication` → `TeacherServiceApplication`
- 配置类保持不变（通用配置）

---

### 任务2：创建 Teacher 核心代码

**需要创建的文件**：
1. `entity/Teacher.java` - 教师实体
2. `mapper/TeacherMapper.java` - 教师Mapper
3. `service/TeacherService.java` - 教师Service
4. `controller/TeacherController.java` - 教师Controller（包含 /me 接口）

**关键要求**：
- `@GetMapping("/me")` 必须使用 `@RequestHeader("teacherId")`
- 严禁使用 Feign 调用其他服务

---

### 任务3：配置 application.yml

**端口分配**：
- auth-service: 8081
- student-service: 8082
- file-service: 8083
- course-service: 8084
- **teacher-service: 8085** ✅

**数据库配置**：
- 数据库：`student_system`
- 表：`teacher_info`（已存在于 course-service 的数据库中）

---

### 任务4：删除不需要的代码

**需要删除**：
- `StudentController.java`
- `StudentService.java`
- `StudentMapper.java`
- `Student.java`
- `ClassController.java`（班级管理保留在 student-service）
- `DictController.java`（字典管理保留在 student-service）

**需要保留**：
- 配置类（MybatisPlusConfig, Knife4jConfig, WebMvcConfig）
- 权限拦截器（PermissionInterceptor）
- FileServiceClient（用于头像上传）

---

## 🎯 关于"创建教师"的逻辑建议

### 方案对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **方案A：前端两步请求** | 简单，无需Feign | 可能产生僵尸数据 | ⭐⭐⭐ |
| **方案B：数据库共享** | 数据一致性好 | 违反微服务原则 | ⭐ |
| **方案C：消息队列** | 解耦，最终一致性 | 复杂度高 | ⭐⭐⭐⭐⭐ |

### 推荐方案：前端两步请求 + 补偿机制

**流程**：
```
1. 前端调用 teacher-service: POST /teacher/add
   → 创建 teacher_info 记录
   → 返回 teacherId

2. 前端调用 auth-service: POST /user/create-for-teacher
   → 创建 user 记录
   → user.teacher_id = teacherId
   → 返回 userId

3. 前端调用 teacher-service: PUT /teacher/{teacherId}/bind-user
   → 回写 teacher_info.user_id = userId
```

**补偿机制**：
- 如果步骤2失败，前端调用 `DELETE /teacher/{teacherId}` 删除教师记录
- 如果步骤3失败，前端提示用户重试

**优点**：
- 无需 Feign，完全解耦
- 逻辑清晰，易于理解
- 适合算法竞赛选手的思维方式

---

## 📝 执行步骤

### 步骤1：修改主类和包名（5分钟）
### 步骤2：创建 Teacher 实体和 Mapper（10分钟）
### 步骤3：创建 TeacherService（15分钟）
### 步骤4：创建 TeacherController（10分钟）
### 步骤5：配置 application.yml（5分钟）
### 步骤6：删除不需要的代码（5分钟）
### 步骤7：测试验证（10分钟）

**总计：1小时**

---

**下一步：开始执行重构**
