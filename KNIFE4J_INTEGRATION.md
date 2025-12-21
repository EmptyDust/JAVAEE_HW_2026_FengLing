# Knife4j API 文档集成

## 概述

已成功为学生信息管理系统集成 Knife4j (Swagger) API 文档功能，提供可视化的 API 接口文档和在线测试工具。

## 集成版本

- **Knife4j**: 4.4.0 (支持 Spring Boot 3 + Jakarta EE)
- **OpenAPI**: 3.x
- **依赖**: `knife4j-openapi3-jakarta-spring-boot-starter`

## 访问地址

### 认证服务 API 文档
```
http://localhost:8081/doc.html
```

### 学生服务 API 文档
```
http://localhost:8082/doc.html
```

### 通过网关访问（推荐）
```
http://localhost:8080/auth/doc.html      # 认证服务
http://localhost:8080/student/doc.html   # 学生服务
```

## 已实现功能

### 1. 依赖配置

**父 POM** (`pom.xml`)
```xml
<properties>
    <knife4j.version>4.4.0</knife4j.version>
</properties>

<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>${knife4j.version}</version>
</dependency>
```

**服务模块**
- `backend/auth-service/pom.xml` - 已添加
- `backend/student-service/pom.xml` - 已添加

### 2. 配置类

**认证服务** (`backend/auth-service/src/main/java/com/student/auth/config/Knife4jConfig.java`)
```java
@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("认证服务 API")
                        .description("学生信息管理系统 - 认证服务接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@student.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
```

**学生服务** (`backend/student-service/src/main/java/com/student/student/config/Knife4jConfig.java`)
```java
@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("学生服务 API")
                        .description("学生信息管理系统 - 学生服务接口文档")
                        .version("v1.0.0")
                        // ...
                });
    }
}
```

### 3. Controller 注解

#### 认证服务接口

**AuthController** (`backend/auth-service/src/main/java/com/student/auth/controller/AuthController.java`)
```java
@Tag(name = "认证接口", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Operation(summary = "用户注册", description = "新用户注册接口")
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) { ... }

    @Operation(summary = "获取验证码", description = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<Map<String, Object>> getCaptcha() { ... }

    @Operation(summary = "用户登录", description = "用户名密码登录，需要验证码")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) { ... }

    @Operation(summary = "用户登出", description = "退出登录")
    @PostMapping("/logout")
    public Result<?> logout(@Parameter(description = "用户ID") @RequestHeader("userId") Long userId) { ... }
}
```

#### 学生服务接口

**StudentController** (`backend/student-service/src/main/java/com/student/student/controller/StudentController.java`)
```java
@Tag(name = "学生管理", description = "学生信息管理相关接口")
@RestController
@RequestMapping("/student")
public class StudentController {

    @Operation(summary = "查询学生列表", description = "分页查询学生信息，支持按姓名和班级筛选")
    @GetMapping("/list")
    public Result<Page<Student>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "学生姓名") @RequestParam(required = false) String name,
            @Parameter(description = "班级ID") @RequestParam(required = false) Long classId) { ... }

    @Operation(summary = "添加学生", description = "新增学生信息")
    @PostMapping("/add")
    public Result<?> add(@RequestBody Student student) { ... }

    @Operation(summary = "更新学生", description = "更新学生信息")
    @PutMapping("/update")
    public Result<?> update(@RequestBody Student student) { ... }

    @Operation(summary = "删除学生", description = "根据ID删除学生")
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@Parameter(description = "学生ID") @PathVariable Long id) { ... }

    @Operation(summary = "查询学生详情", description = "根据ID查询学生详细信息")
    @GetMapping("/{id}")
    public Result<Student> getById(@Parameter(description = "学生ID") @PathVariable Long id) { ... }
}
```

## 使用说明

### 1. 启动服务

确保两个服务都已启动：
```bash
# 认证服务
java -jar backend/auth-service/target/auth-service-1.0.0.jar

# 学生服务
java -jar backend/student-service/target/student-service-1.0.0.jar
```

### 2. 访问文档

打开浏览器访问：
- 认证服务文档：http://localhost:8081/doc.html
- 学生服务文档：http://localhost:8082/doc.html

### 3. 在线测试

Knife4j 提供了强大的在线测试功能：

1. **选择接口**：在左侧菜单选择要测试的接口
2. **填写参数**：在右侧表单填写请求参数
3. **发送请求**：点击"发送"按钮
4. **查看响应**：查看响应状态、响应头和响应体

### 4. 认证设置

对于需要认证的接口（如学生管理接口）：

1. 先调用认证服务的 `/auth/login` 接口获取 Token
2. 点击页面右上角的"Authorize"按钮
3. 输入格式：`Bearer YOUR_TOKEN_HERE`
4. 保存后，后续请求会自动携带 Authorization 头

## 常用 Swagger 注解

| 注解 | 作用 | 使用位置 |
|-----|------|---------|
| `@Tag` | 定义接口分组 | Controller 类 |
| `@Operation` | 描述接口信息 | 接口方法 |
| `@Parameter` | 描述参数信息 | 方法参数 |
| `@Schema` | 描述实体字段 | 实体类字段 |
| `@ApiResponse` | 描述响应信息 | 接口方法 |

## 高级功能

### 1. 全局参数配置

可以在 `application.yml` 中配置全局参数：

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

knife4j:
  enable: true
  setting:
    language: zh-CN
```

### 2. 分组管理

如果需要多个API分组（如公开接口和内部接口），可以配置多个 `GroupedOpenApi` Bean。

### 3. 请求示例

可以在 `@Operation` 注解中添加请求示例：

```java
@Operation(
    summary = "用户登录",
    description = "用户名密码登录，需要验证码",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(
            examples = @ExampleObject(
                value = "{\"username\":\"admin\",\"password\":\"123456\",\"uuid\":\"xxx\",\"captcha\":\"1234\"}"
            )
        )
    )
)
```

## 故障排查

### 问题1：访问 doc.html 返回 404

**原因**：服务未正确集成 Knife4j 依赖

**解决**：确认 pom.xml 已添加 `knife4j-openapi3-jakarta-spring-boot-starter` 依赖并重新编译

### 问题2：接口不显示

**原因**：Controller 未添加 Swagger 注解

**解决**：为 Controller 类添加 `@Tag` 注解，为方法添加 `@Operation` 注解

### 问题3：参数说明不显示

**原因**：参数未添加 `@Parameter` 注解

**解决**：为接口参数添加 `@Parameter(description = "参数说明")` 注解

## 后续扩展

如果需要为其他服务（如 Gateway）添加 API 文档聚合功能：

1. 在 Gateway 添加 Knife4j Gateway 依赖
2. 配置聚合路由
3. 实现统一文档入口

## 技术说明

### Spring Boot 3 兼容性

- 使用 `knife4j-openapi3-jakarta-spring-boot-starter`（支持 Jakarta EE）
- 不能使用旧版 `knife4j-spring-boot-starter`（基于 javax.*）
- OpenAPI 3.x 规范，更现代的 API 文档标准

### Knife4j vs Swagger UI

- **Knife4j**：国产增强版 Swagger UI，界面更美观，功能更强大
- **界面特色**：中文界面、调试增强、离线文档、全局参数
- **兼容性**：完全兼容 OpenAPI 3.x 规范

## 总结

Knife4j 已成功集成到认证服务和学生服务，提供了：

✓ 可视化 API 文档
✓ 在线接口测试
✓ 参数说明和示例
✓ 响应数据结构展示
✓ Spring Boot 3 完全兼容

现在可以通过浏览器方便地查看和测试所有 API 接口！
