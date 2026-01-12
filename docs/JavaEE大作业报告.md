# JavaEE课程报告

---

**题目：** 学生选课管理系统

**学号：** [请填写]

**姓名：** [请填写]

**同组姓名：** [请填写]

**院系：** [请填写]

**专业年级：** [请填写]

**日期：** 2026年1月

---

## 目录

- [第一章 引言](#第一章-引言)
  - [1.1 项目背景](#11-项目背景)
  - [1.2 项目意义](#12-项目意义)
- [第二章 需求分析](#第二章-需求分析)
  - [2.1 功能需求](#21-功能需求)
  - [2.2 技术要求](#22-技术要求)
- [第三章 系统设计](#第三章-系统设计)
  - [3.1 系统架构](#31-系统架构)
  - [3.2 模块划分](#32-模块划分)
  - [3.3 数据库设计](#33-数据库设计)
  - [3.4 接口设计](#34-接口设计)
  - [3.5 技术选型](#35-技术选型)
- [第四章 关键功能实现](#第四章-关键功能实现)
  - [4.1 用户认证与权限管理](#41-用户认证与权限管理)
  - [4.2 课程选课功能](#42-课程选课功能)
  - [4.3 课程日历功能](#43-课程日历功能)
  - [4.4 通知推送功能](#44-通知推送功能)
  - [4.5 数据权限控制](#45-数据权限控制)
- [第五章 系统总结](#第五章-系统总结)
  - [5.1 系统已实现的功能](#51-系统已实现的功能)
  - [5.2 心得体会](#52-心得体会)

---

## 第一章 引言

### 1.1 项目背景

随着高等教育规模的不断扩大和教学管理信息化的深入发展，传统的人工选课方式已经无法满足现代高校教学管理的需求。学生选课管理系统作为高校教务管理的核心组成部分，对于提高教学管理效率、优化教学资源配置、提升学生学习体验具有重要意义。

本项目旨在开发一个基于微服务架构的学生选课管理系统，该系统采用前后端分离的设计理念，利用Spring Cloud生态构建分布式服务体系，实现了用户管理、课程管理、选课管理、通知推送、文件管理等核心功能。系统支持管理员、教师、学生三种角色，通过细粒度的权限控制和数据权限管理，确保不同角色用户只能访问和操作其权限范围内的数据。

在技术选型上，系统后端采用Spring Boot 3.2.0、Spring Cloud、MyBatis-Plus等主流JavaEE技术栈，前端使用Vue.js 3框架，数据库采用MySQL，并集成了Redis缓存、RabbitMQ消息队列、Elasticsearch全文检索、WebSocket实时通信等技术，构建了一个功能完善、性能优良、易于扩展的现代化教学管理平台。

### 1.2 项目意义

本项目的开发和实施具有以下重要意义：

**1. 提升教学管理效率**

通过系统化、自动化的选课流程，替代传统的人工选课方式，大幅减少教务人员的工作量，提高选课处理速度和准确性。系统支持批量操作、并发选课、自动冲突检测等功能，能够在短时间内完成大规模学生的选课任务。

**2. 优化教学资源配置**

系统提供实时的课程容量监控、选课统计分析等功能，帮助教务管理部门及时了解课程的选课情况，合理调配教学资源。通过数据分析，可以为下学期的课程开设、教室安排、师资配备提供科学依据。

**3. 改善学生学习体验**

学生可以通过Web界面随时随地查看课程信息、进行选课操作、查看课程表、接收课程通知等。系统提供课程日历功能，帮助学生直观地了解课程安排；通知推送功能确保学生及时获取课程变更、作业布置等重要信息。

**4. 实践微服务架构**

本项目采用微服务架构设计，将系统拆分为多个独立的服务模块，每个服务专注于特定的业务领域。这种架构模式具有高内聚、低耦合、易扩展、易维护等优点，是当前企业级应用开发的主流方向。

**5. 掌握JavaEE核心技术**

项目综合运用了Spring Boot、Spring Cloud、MyBatis-Plus、Redis、RabbitMQ、Elasticsearch等JavaEE生态中的核心技术，涵盖了服务注册与发现、配置管理、负载均衡、消息队列、缓存、全文检索、实时通信等多个技术领域。

---

## 第二章 需求分析

### 2.1 功能需求

本系统面向管理员、教师、学生三类用户，提供以下核心功能：

**1. 用户管理功能**
- 用户注册与登录：支持学生、教师、管理员三种角色的注册和登录
- 身份认证：基于JWT的无状态身份认证机制
- 权限管理：基于角色的访问控制（RBAC），不同角色具有不同的操作权限
- 个人信息管理：用户可以查看和修改个人基本信息

**2. 课程管理功能**
- 课程信息管理：管理员和教师可以创建、编辑、删除课程信息
- 课程查询：支持按课程名称、课程编号、课程类型、学期等条件查询
- 课程状态管理：支持启用、停用、已满等状态管理
- 课程容量控制：设置课程最大选课人数，自动统计已选人数

**3. 选课管理功能**
- 在线选课：学生可以浏览课程列表并进行选课操作
- 退课功能：学生可以退选已选课程
- 选课冲突检测：自动检测时间冲突、容量限制等问题
- 选课状态查询：实时显示课程的选课状态（已选/未选）
- 我的课程：学生可以查看已选课程列表

**4. 课程日历功能**
- 日历生成：根据课程时间自动生成课程日历
- 日历展示：以日历视图展示学生的课程安排
- 学期切换：支持按学期查看课程日历
- 重复规则：支持周期性课程的重复规则设置（iCalendar RRULE格式）

**5. 通知推送功能**
- 通知创建：教师可以创建课程通知，支持多种通知类型（作业、考试、通知等）
- 通知发送：支持发送给已选课学生或全体学生
- 实时推送：通过WebSocket实现实时通知推送
- 消息队列：使用RabbitMQ实现异步通知发送，提高系统性能
- 通知管理：用户可以查看、标记已读、删除通知

**6. 文件管理功能**
- 文件上传：支持课程资料、作业等文件的上传
- 文件下载：学生可以下载课程相关文件
- 存储策略：支持本地存储和MinIO对象存储两种方式
- 文件检索：集成Elasticsearch实现文件内容全文检索

**7. 数据权限控制**
- 行级数据权限：教师只能查看和操作自己的课程数据
- 学生数据隔离：学生只能查看和操作自己的选课数据
- 动态SQL注入：通过MyBatis拦截器实现数据权限的自动过滤
- 权限规则配置：支持灵活的数据权限规则配置

### 2.2 技术要求

**1. 架构要求**
- 采用微服务架构，实现服务的解耦和独立部署
- 前后端分离设计，前端和后端通过RESTful API通信
- 支持水平扩展，能够应对高并发访问场景

**2. 性能要求**
- 系统响应时间：普通查询操作响应时间不超过1秒
- 并发处理能力：支持至少1000个并发用户同时在线
- 数据库查询优化：合理使用索引，避免慢查询
- 缓存机制：使用Redis缓存热点数据，减少数据库压力

**3. 安全要求**
- 身份认证：使用JWT实现无状态身份认证
- 数据加密：敏感数据（如密码）需要加密存储
- SQL注入防护：使用参数化查询防止SQL注入攻击
- XSS防护：对用户输入进行过滤和转义
- CSRF防护：实现跨站请求伪造防护机制

**4. 可靠性要求**
- 事务管理：关键业务操作使用事务保证数据一致性
- 异常处理：完善的异常捕获和处理机制
- 日志记录：记录关键操作日志，便于问题排查
- 消息可靠性：使用消息队列确认机制保证消息不丢失

---

## 第三章 系统设计

### 3.1 系统架构

本系统采用微服务架构设计，整体架构分为以下几层：

**1. 前端展示层**
- 技术栈：Vue.js 3 + Element Plus + Axios
- 职责：提供用户交互界面，处理用户输入，展示业务数据
- 特点：单页应用（SPA），前后端分离，响应式设计

**2. API网关层**
- 技术栈：Spring Cloud Gateway
- 职责：统一入口，路由转发，负载均衡，跨域处理
- 功能：请求路由、权限验证、限流熔断、日志记录

**3. 微服务层**
系统划分为以下微服务：
- **auth-service（认证服务）**：用户认证、JWT生成与验证
- **student-service（学生服务）**：学生信息管理、班级管理
- **teacher-service（教师服务）**：教师信息管理
- **course-service（课程服务）**：课程管理、选课管理、通知管理、日历管理
- **file-service（文件服务）**：文件上传下载、文件检索

**4. 基础设施层**
- **Nacos**：服务注册与发现、配置管理
- **MySQL**：关系型数据库，存储业务数据
- **Redis**：缓存、分布式锁、会话管理
- **RabbitMQ**：消息队列，异步通信
- **Elasticsearch**：全文检索引擎
- **MinIO**：对象存储服务

### 3.2 模块划分

系统按照业务领域划分为以下核心模块：

**1. 认证授权模块（auth-service）**
- 用户登录认证
- JWT令牌生成与验证
- 用户信息管理
- 密码加密与验证

**2. 学生管理模块（student-service）**
- 学生信息CRUD操作
- 班级信息管理
- 学生查询与统计
- 数据权限规则管理

**3. 教师管理模块（teacher-service）**
- 教师信息CRUD操作
- 教师查询与统计
- 教师权限管理

**4. 课程管理模块（course-service）**
- 课程信息管理：课程的创建、编辑、删除、查询
- 选课管理：学生选课、退课、选课状态查询
- 课程日历：日历生成、日历查询、重复规则管理
- 通知管理：通知创建、发送、查询、已读标记
- 学期管理：学期信息配置与查询

**5. 文件管理模块（file-service）**
- 文件上传与下载
- 文件存储策略（本地/MinIO）
- 文件元数据管理
- 文件内容检索（Elasticsearch）

**6. 公共模块（common）**
- 统一结果封装
- 统一异常处理
- 数据权限拦截器
- 用户上下文管理
- 工具类库

### 3.3 数据库设计

系统采用MySQL关系型数据库，主要数据表设计如下：

**1. 用户相关表**
- `user`：用户基本信息表，存储用户名、密码、角色等
- `student`：学生信息表，存储学号、姓名、班级等
- `teacher`：教师信息表，存储工号、姓名、部门等
- `class`：班级信息表，存储班级名称、专业、年级等

**2. 课程相关表**
- `course_info`：课程信息表，存储课程编号、名称、学分、容量等
- `course_enrollment`：选课记录表，存储学生选课关系
- `course_calendar`：课程日历表，存储课程时间安排和重复规则

**3. 通知相关表**
- `course_notification`：课程通知表，存储通知标题、内容、类型等
- `notification_receive`：通知接收表，存储用户接收通知的记录和已读状态

**4. 文件相关表**
- `file_info`：文件信息表，存储文件名、路径、大小、类型等

**5. 权限相关表**
- `data_permission_rule`：数据权限规则表，存储权限规则配置

### 3.4 接口设计

系统采用RESTful API设计风格，主要接口如下：

**1. 认证接口（auth-service）**
- `POST /auth/login`：用户登录
- `POST /auth/logout`：用户登出
- `GET /auth/info`：获取当前用户信息

**2. 学生管理接口（student-service）**
- `GET /student/list`：分页查询学生列表
- `GET /student/{id}`：获取学生详情
- `POST /student/add`：添加学生
- `PUT /student/update`：更新学生信息
- `DELETE /student/delete/{id}`：删除学生

**3. 课程管理接口（course-service）**
- `GET /course/list`：分页查询课程列表
- `GET /course/{id}`：获取课程详情
- `POST /course/add`：创建课程
- `PUT /course/update`：更新课程
- `DELETE /course/delete/{id}`：删除课程

**4. 选课管理接口（course-service）**
- `POST /course/enroll`：学生选课
- `POST /course/drop`：学生退课
- `GET /course/my-courses`：查询我的课程

**5. 通知管理接口（course-service）**
- `POST /course/notification/send`：发送通知
- `GET /course/notification/list`：查询通知列表
- `GET /course/notification/unread-count`：获取未读通知数量
- `PUT /course/notification/mark-read`：标记通知已读

### 3.5 技术选型

**1. 后端技术栈**
- **Spring Boot 3.2.0**：简化Spring应用开发，提供自动配置和快速启动能力
- **Spring Cloud**：微服务框架，提供服务注册、配置管理、负载均衡等功能
- **Spring Cloud Gateway**：API网关，统一入口和路由转发
- **Nacos**：服务注册与发现、配置中心
- **MyBatis-Plus**：增强版MyBatis，简化CRUD操作
- **MySQL 8.0**：关系型数据库
- **Redis**：缓存和分布式锁
- **RabbitMQ**：消息队列，实现异步通信
- **Elasticsearch 7.17**：全文检索引擎
- **WebSocket**：实时通信协议

**2. 前端技术栈**
- **Vue.js 3**：渐进式JavaScript框架
- **Element Plus**：基于Vue 3的UI组件库
- **Axios**：HTTP客户端
- **Pinia**：Vue 3状态管理库
- **Vue Router**：路由管理

**3. 开发工具**
- **IntelliJ IDEA**：Java开发IDE
- **VS Code**：前端开发编辑器
- **Maven**：项目构建和依赖管理
- **Git**：版本控制
- **Postman**：API测试工具

---

## 第四章 关键功能实现

### 4.1 用户认证与权限管理

**实现思路：**

系统采用JWT（JSON Web Token）实现无状态身份认证，主要流程如下：

1. **用户登录**：用户提交用户名和密码，系统验证通过后生成JWT令牌
2. **令牌存储**：前端将JWT令牌存储在localStorage中
3. **请求携带**：前端每次请求时在Header中携带JWT令牌
4. **令牌验证**：网关层验证JWT令牌的有效性，提取用户信息
5. **权限控制**：根据用户角色判断是否有权限访问特定接口

**核心代码实现：**

```java
// JWT工具类 - 生成令牌
public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("username", user.getUsername());
    claims.put("userType", user.getUserType());

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(user.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(SignatureAlgorithm.HS512, secret)
        .compact();
}

// 用户上下文管理 - 存储当前请求的用户信息
public class UserContextHolder {
    private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();

    public static void setContext(UserContext context) {
        contextHolder.set(context);
    }

    public static UserContext getContext() {
        return contextHolder.get();
    }
}
```

### 4.2 课程选课功能

**实现思路：**

选课功能是系统的核心业务，需要处理并发选课、容量控制、冲突检测等问题。

**关键技术点：**

1. **原子操作防止超卖**：使用数据库层面的原子更新操作，确保课程容量不会超限

```java
// 使用原子更新SQL，返回影响的行数
@Update("UPDATE course_info SET enrolled_students = enrolled_students + 1 " +
        "WHERE id = #{courseId} AND enrolled_students < max_students AND status = 1")
int incrementEnrollmentAtomic(@Param("courseId") Long courseId);
```

2. **事务管理**：选课操作涉及多个表的更新，使用@Transactional确保数据一致性

```java
@Transactional(rollbackFor = Exception.class)
public void enrollCourse(Long studentId, Long courseId) {
    // 1. 检查是否已选课
    // 2. 原子增加选课人数
    // 3. 创建选课记录
    // 4. 如果任何步骤失败，自动回滚
}
```

3. **数据权限过滤**：学生只能查看和操作自己的选课记录，通过MyBatis拦截器自动注入过滤条件

### 4.3 课程日历功能

**实现思路：**

课程日历功能采用iCalendar标准的RRULE格式实现周期性课程的重复规则。

**关键技术点：**

1. **RRULE格式**：使用标准的重复规则格式，例如：
   - `FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=20250630;INTERVAL=1`
   - 表示每周一、周三重复，直到2025年6月30日

2. **学期过滤**：后端在查询日历前先按学期过滤课程ID，避免跨学期数据混乱

```java
// 先按学期过滤课程ID
List<Long> filteredCourseIds = courseInfoService.filterCourseIdsBySemester(
    courseIds, semesterInfo.getSemester());

// 再查询日历
List<CourseCalendar> calendar = calendarService.getStudentCalendar(
    filteredCourseIds, semesterInfo.getStartDate(), semesterInfo.getEndDate());
```

3. **日历展开**：前端根据RRULE规则展开重复事件，生成具体的日期列表

### 4.4 通知推送功能

**实现思路：**

通知推送功能采用RabbitMQ消息队列实现异步发送，结合WebSocket实现实时推送。

**关键技术点：**

1. **异步发送**：教师创建通知后，系统将通知信息发送到RabbitMQ队列，立即返回响应

```java
// 发送通知到消息队列
Map<String, Object> message = new HashMap<>();
message.put("notificationId", notification.getId());
message.put("courseId", notification.getCourseId());
message.put("targetType", notification.getTargetType());
message.put("sendMethod", notification.getSendMethod());

rabbitTemplate.convertAndSend(exchange, routingKey, message);
```

2. **消息消费**：NotificationListener监听队列，获取目标用户列表，创建接收记录

3. **实时推送**：如果用户在线，通过WebSocket推送通知到前端

4. **跨服务调用**：使用OpenFeign调用student-service获取全体学生用户ID

```java
@FeignClient(name = "student-service")
public interface StudentFeignClient {
    @GetMapping("/student/user-ids")
    Result<List<Long>> getAllStudentUserIds();
}
```

### 4.5 数据权限控制

**实现思路：**

数据权限控制通过MyBatis拦截器实现，自动在SQL中注入权限过滤条件。

**关键技术点：**

1. **拦截器实现**：实现MyBatis-Plus的InnerInterceptor接口，拦截查询和更新操作

```java
@Override
public void beforeQuery(Executor executor, MappedStatement ms,
                       Object parameter, RowBounds rowBounds,
                       ResultHandler resultHandler, BoundSql boundSql) {
    // 获取当前用户信息
    UserContext context = UserContextHolder.getContext();
    if (context == null || context.isAdmin()) {
        return; // 管理员不过滤
    }

    // 根据用户角色注入过滤条件
    if (context.isTeacher()) {
        // 教师只能查看自己的课程
        // 自动添加 WHERE teacher_id = #{currentTeacherId}
    } else if (context.isStudent()) {
        // 学生只能查看自己的选课记录
        // 自动添加 WHERE student_id = #{currentStudentId}
    }
}
```

2. **注解跳过**：对于某些特殊查询，使用@IgnoreDataPermission注解跳过权限过滤

```java
@IgnoreDataPermission(reason = "该方法已通过参数正确过滤数据")
Long countByStudentAndCourse(@Param("studentId") Long studentId,
                             @Param("courseId") Long courseId);
```

---

## 第五章 系统总结

### 5.1 系统已实现的功能

本系统已成功实现以下核心功能模块：

**1. 用户管理功能**
- ✅ 用户注册与登录（支持管理员、教师、学生三种角色）
- ✅ JWT无状态身份认证
- ✅ 基于角色的权限控制（RBAC）
- ✅ 用户信息管理与查询

**2. 课程管理功能**
- ✅ 课程信息的增删改查课程多条件查询（课程名称、类型、学期等）课程状态管理（启用、停用、已满）课程容量控制与实时统计

**3. 选课管理功能**
- ✅ 学生在线选课学生退课并发选课控制（原子操作防止超卖）选课状态实时显示我的课程列表查询

**4. 课程日历功能**
- ✅ 基于iCalendar RRULE格式的日历生成；课程日历展示；学期切换与过滤；周期性课程重复规则管理；

**5. 通知推送功能**
- ✅ 课程通知创建与管理；支持多种通知类型（作业、考试、通知等）；基于RabbitMQ的异步通知发送；WebSocket实时通知推送；通知已读/未读状态管理；支持发送给已选课学生或全体学生；

**6. 文件管理功能**
- ✅ 文件上传与下载；支持本地存储和MinIO对象存储；基于Elasticsearch的文件内容全文检索；文件元数据管理；

**7. 数据权限控制**
- ✅ 基于MyBatis拦截器的行级数据权限;教师只能操作自己的课程数据;学生只能操作自己的选课数据;支持@IgnoreDataPermission注解跳过权限过;
**8. 微服务基础设施**
- ✅ 基于Nacos的服务注册与发现;基于Spring Cloud Gateway的API网关;基于OpenFeign的服务间通信;基于Redis的缓存管理;统一异常处理与结果封装

### 5.2 心得体会

通过本项目的开发实践，我深刻体会到了JavaEE企业级应用开发的复杂性和系统性，收获了宝贵的技术经验和项目经验。

**1. 微服务架构的理解与实践**

在项目初期，我对微服务架构只有理论上的认识。通过实际开发，我深刻理解了微服务架构的优势和挑战。服务拆分需要遵循单一职责原则，每个服务应该专注于特定的业务领域。服务间通信需要考虑网络延迟、服务降级、熔断等问题。Nacos作为服务注册中心和配置中心，极大地简化了微服务的管理和配置。

**2. 数据一致性问题的处理**

在分布式系统中，数据一致性是一个重要挑战。选课功能中的并发控制问题让我认识到，简单的查询-判断-更新模式在高并发场景下会导致超卖问题。通过使用数据库层面的原子更新操作（UPDATE ... WHERE条件），可以有效避免这类问题。此外，使用@Transactional注解确保事务的ACID特性，对于保证数据一致性至关重要。

**3. 异步处理与消息队列**

通知推送功能的实现让我深刻理解了异步处理的重要性。如果采用同步方式发送通知，当目标用户数量较多时，会导致接口响应时间过长，影响用户体验。通过引入RabbitMQ消息队列，将通知发送任务异步化，可以立即返回响应，提升系统性能。同时，消息队列还提供了削峰填谷、解耦服务等优势。

**4. 数据权限控制的实现**

数据权限控制是企业级应用的重要功能。通过MyBatis拦截器实现行级数据权限，可以在不修改业务代码的情况下，自动为SQL注入权限过滤条件。这种AOP思想的应用，既保证了数据安全，又提高了代码的可维护性。但在实践中也遇到了一些问题，比如某些特殊查询需要使用@IgnoreDataPermission注解跳过权限过滤，这需要仔细权衡安全性和灵活性。

**5. 前后端分离与接口设计**

前后端分离是现代Web应用的标准架构。通过RESTful API设计，前后端可以独立开发、独立部署。在接口设计过程中，我学会了如何设计清晰、一致的API，如何处理跨域问题，如何进行接口文档管理（使用Knife4j）。前端使用Vue.js 3和Element Plus，可以快速构建美观、易用的用户界面。

**6. 问题排查与调试能力**

在开发过程中遇到了许多问题，比如选课状态显示不正确、通知只发送给部分学生、课程日历生成0条记录等。通过查看日志、分析SQL、使用调试工具，逐步定位问题根源并解决。这个过程锻炼了我的问题排查能力和系统思维能力。特别是数据权限拦截器导致的SQL冲突问题，让我认识到在复杂系统中，各个组件之间的交互可能产生意想不到的问题，需要全面考虑。

**7. 技术选型与学习能力**

项目中使用了大量的技术栈，包括Spring Boot、Spring Cloud、MyBatis-Plus、Redis、RabbitMQ、Elasticsearch等。每个技术都有其特定的应用场景和最佳实践。通过阅读官方文档、查阅技术博客、实践验证，我逐步掌握了这些技术的使用方法。这个过程提升了我的自主学习能力和技术选型能力。

**8. 团队协作与项目管理**

虽然本项目主要是个人开发，但在实际企业开发中，团队协作至关重要。通过使用Git进行版本控制，可以有效管理代码变更。良好的代码规范、清晰的注释、完善的文档，都是团队协作的基础。

**总结：**

本项目是一次全面的JavaEE企业级应用开发实践，涵盖了微服务架构、分布式系统、消息队列、缓存、全文检索等多个技术领域。通过这次实践，我不仅掌握了具体的技术知识，更重要的是培养了系统设计能力、问题解决能力和工程实践能力。这些经验将对我未来的学习和工作产生深远影响。

---

**报告完成日期：** 2026年1月11日

---
