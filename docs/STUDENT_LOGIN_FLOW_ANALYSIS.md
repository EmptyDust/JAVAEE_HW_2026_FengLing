# 学生登录和注册流程详解

## 📋 学生注册流程（正确的设计）

### 流程图

```
前端                Gateway              auth-service           student-service
  │                   │                      │                        │
  │  POST /auth/register                     │                        │
  ├──────────────────>│                      │                        │
  │  {username, password, email, phone}      │                        │
  │                   │                      │                        │
  │                   │  转发请求             │                        │
  │                   ├─────────────────────>│                        │
  │                   │                      │                        │
  │                   │                      │ 1. 检查用户名是否存在   │
  │                   │                      │ 2. 创建 User 记录      │
  │                   │                      │    - userType="student"│
  │                   │                      │    - student_id=NULL   │
  │                   │                      │ 3. BCrypt 加密密码     │
  │                   │                      │ 4. 插入数据库          │
  │                   │                      │                        │
  │                   │  返回成功             │                        │
  │                   │<─────────────────────┤                        │
  │  注册成功          │                      │                        │
  │<──────────────────┤                      │                        │
  │                   │                      │                        │
  │  注意：此时 User 表有记录，但 student_id 为 NULL                  │
  │  需要管理员或教师在 student-service 中创建 Student 记录            │
```

### 关键代码分析

**AuthController.java:22-31**
```java
@PostMapping("/register")
public Result<?> register(@RequestBody Map<String, String> params) {
    authService.register(
        params.get("username"),
        params.get("password"),
        params.get("email"),
        params.get("phone")
    );
    return Result.success("注册成功");
}
```

**AuthService.java:41-75**
```java
public void register(String username, String password, String email, String phone) {
    register(username, password, email, phone, "student", null);
    //                                          ^^^^^^^^  ^^^^
    //                                          默认学生   student_id为空
}

public void register(..., String userType, Long studentId) {
    // 1. 验证密码强度
    PasswordValidator.validatePasswordStrength(password);

    // 2. 检查用户名是否存在
    if (existUser != null) {
        throw new BusinessException("用户名已存在");
    }

    // 3. 创建用户
    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setUserType(userType);      // "student"
    user.setStudentId(studentId);    // NULL

    userMapper.insert(user);
}
```

### 重要特点

1. **职责清晰**：
   - auth-service 只负责创建登录账号
   - student-service 负责管理学生详细信息
   - 两者通过 student_id 关联

2. **数据分离**：
   - User 表在 auth-service 数据库
   - Student 表在 student-service 数据库
   - 符合微服务数据库隔离原则

3. **注册后的状态**：
   - User 记录已创建，可以登录
   - Student 记录未创建，需要管理员补充

---

## 📋 学生登录流程（正确的设计）

### 流程图

```
前端                Gateway              auth-service           student-service
  │                   │                      │                        │
  │  1. GET /auth/captcha                    │                        │
  ├──────────────────>│─────────────────────>│                        │
  │  获取验证码         │                      │ 生成验证码              │
  │<──────────────────┤<─────────────────────┤ 存入 Redis             │
  │  {uuid, image}    │                      │                        │
  │                   │                      │                        │
  │  2. POST /auth/login                     │                        │
  ├──────────────────>│                      │                        │
  │  {username, password, uuid, captcha}     │                        │
  │                   │  转发请求             │                        │
  │                   ├─────────────────────>│                        │
  │                   │                      │                        │
  │                   │                      │ 1. 验证验证码           │
  │                   │                      │ 2. 查询 User 表        │
  │                   │                      │ 3. 验证密码             │
  │                   │                      │ 4. 生成 JWT Token      │
  │                   │                      │    包含：userId,       │
  │                   │                      │    username, userType, │
  │                   │                      │    studentId           │
  │                   │                      │ 5. 存入 Redis          │
  │                   │                      │                        │
  │                   │  返回登录信息          │                        │
  │                   │<─────────────────────┤                        │
  │  登录成功          │                      │                        │
  │<──────────────────┤                      │                        │
  │  {token, userId, username, userType, studentId}                   │
  │                   │                      │                        │
  │  3. GET /student/me                      │                        │
  ├──────────────────>│                      │                        │
  │  Header: Authorization: Bearer {token}   │                        │
  │                   │                      │                        │
  │                   │  解析 Token           │                        │
  │                   │  注入请求头：          │                        │
  │                   │  - userId            │                        │
  │                   │  - username          │                        │
  │                   │  - userType          │                        │
  │                   │  - studentId         │                        │
  │                   │                      │                        │
  │                   │  转发到 student-service                        │
  │                   ├────────────────────────────────────────────>│
  │                   │                      │                        │
  │                   │                      │  根据 studentId 查询   │
  │                   │                      │  Student 表            │
  │                   │                      │                        │
  │                   │  返回学生详细信息                               │
  │                   │<────────────────────────────────────────────┤
  │  学生信息          │                      │                        │
  │<──────────────────┤                      │                        │
```

### 关键代码分析

**AuthService.java:90-157（登录逻辑）**
```java
public Map<String, Object> login(String username, String password, String uuid, String captcha) {
    // 1. 验证验证码
    String cachedCaptcha = (String) redisUtil.get("captcha:" + uuid);
    if (cachedCaptcha == null || !cachedCaptcha.equalsIgnoreCase(captcha)) {
        throw new BusinessException("验证码错误或已过期");
    }

    // 2. 查询用户
    User user = userMapper.selectOne(wrapper);

    // 3. 验证密码
    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new BusinessException("用户名或密码错误");
    }

    // 4. 生成 Token（包含 studentId）
    String token = JwtUtil.generateToken(
        user.getId(),
        user.getUsername(),
        user.getUserType(),
        user.getStudentId()  // ✅ 关键：包含 studentId
    );

    // 5. 返回登录信息
    result.put("token", token);
    result.put("userId", user.getId());
    result.put("username", user.getUsername());
    result.put("userType", user.getUserType());
    result.put("studentId", user.getStudentId());  // ✅ 返回 studentId

    return result;
}
```

**Gateway 的 AuthFilter（解析 Token 并注入请求头）**
```java
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 解析 Token
    Claims claims = JwtUtil.parseToken(token);
    String userId = claims.get("userId").toString();
    String username = claims.get("username").toString();
    String userType = claims.get("userType").toString();
    String studentId = claims.get("studentId") != null ? claims.get("studentId").toString() : null;

    // 注入请求头
    ServerHttpRequest request = exchange.getRequest().mutate()
        .header("userId", userId)
        .header("username", username)
        .header("userType", userType)
        .header("studentId", studentId)  // ✅ 注入 studentId
        .build();

    return chain.filter(exchange.mutate().request(request).build());
}
```

**StudentController.java:37-46（获取学生信息）**
```java
@RequireRole({"student"})
@GetMapping("/me")
public Result<StudentVO> getMyInfo(
        @RequestHeader("studentId") Long studentId) {  // ✅ 从请求头获取 studentId

    StudentVO student = studentService.getByIdWithClassName(studentId);
    return Result.success(student);
}
```

### 重要特点

1. **Token 包含 studentId**：
   - JWT Token 中包含 studentId
   - Gateway 解析后注入到请求头
   - student-service 直接从请求头获取

2. **无需跨服务调用**：
   - 不需要 student-service 调用 auth-service
   - 不需要 auth-service 调用 student-service
   - 完全解耦

3. **权限控制清晰**：
   - @RequireRole({"student"}) 控制接口级权限
   - studentId 控制数据级权限

---

## 🔴 教师流程的问题对比

### 当前教师流程（错误的设计）

```
前端                Gateway              course-service         auth-service
  │                   │                      │                        │
  │  POST /teacher/add (管理员创建教师)       │                        │
  ├──────────────────>│─────────────────────>│                        │
  │                   │                      │                        │
  │                   │                      │ ❌ 直接操作 User 表     │
  │                   │                      │ userMapper.insert()    │
  │                   │                      │                        │
  │                   │                      │ teacher_info 表        │
  │                   │                      │ teacherInfoMapper.insert()
  │                   │                      │                        │
  │                   │  返回成功             │                        │
  │<──────────────────┤<─────────────────────┤                        │
  │                   │                      │                        │
  │  教师登录                                 │                        │
  ├──────────────────>│                      │                        │
  │  POST /auth/login │                      │                        │
  │                   │  转发到 auth-service  │                        │
  │                   ├────────────────────────────────────────────>│
  │                   │                      │                        │
  │                   │                      │  返回：{userId, userType="teacher"}
  │                   │                      │  ❌ 没有 teacherId！    │
  │                   │<────────────────────────────────────────────┤
  │<──────────────────┤                      │                        │
  │                   │                      │                        │
  │  ❓ 如何获取教师信息？                     │                        │
  │  ❌ 无法直接获取，因为没有 teacherId       │                        │
```

### 问题总结

| 维度 | 学生流程（正确） | 教师流程（错误） |
|------|----------------|----------------|
| **账号创建** | auth-service 负责 | ❌ course-service 直接操作 User 表 |
| **User 表字段** | ✅ 有 student_id | ❌ 没有 teacher_id |
| **登录返回** | ✅ 返回 studentId | ❌ 不返回 teacherId |
| **获取信息** | ✅ 直接用 studentId 查询 | ❌ 无法查询（没有 teacherId） |
| **数据库隔离** | ✅ 符合微服务原则 | ❌ 跨服务访问数据库 |
| **职责清晰** | ✅ 职责分明 | ❌ 职责混乱 |

---

**下一部分：修复方案和对比**
