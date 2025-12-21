# 学生信息管理系统

基于Spring Boot + Nacos微服务架构的学生信息管理系统，实现用户认证、学生管理、课程管理、选课系统、实时通知等完整功能。

## 📋 需求文档说明

本项目为JavaEE上机实验内容，实现了以下难度档位的功能：

### ✅ 已实现功能

**基础功能（简单档）：**
- ✅ 用户注册（用户名+密码）
- ✅ 用户登录（用户名+密码）
- ✅ 学生信息管理（CRUD）

**中等难度：**
- ✅ 登录增强（用户名+密码+验证码+密码强度校验+密码过期+错误次数控制）
- ✅ 学生信息管理（CRUD+数据字典+班级联动）
- ✅ 教师管理（完整CRUD+头像上传+课程关联+自动创建账号）
- ✅ 课程管理（完整CRUD+教师关联）
- ✅ 课程附件上传（MinIO对象存储）
- ✅ 课程附件搜索（Elasticsearch + IK中文分词）
- ✅ 课程预览（文档、音视频、图片在线预览）
- ✅ 附件管理界面（统一管理+统计分析+性能优化）
- ✅ 学生选课系统
- ✅ 课程日历表（支持周期性事件）
- ✅ 课程通知（WebSocket实时推送 + RabbitMQ消息队列）
- ✅ 通知模板管理（支持多种类型模板）
- ✅ 定时发送通知（预约发送时间）

**困难档（部分）：**
- ✅ 学生信息管理（CRUD+数据字典+班级联动+头像上传）
- ✅ 登录增强（强弱密码校验+密码过期+错误次数控制）
- ✅ 接口鉴权（基于角色的权限控制）
- ⏳ 数据权限控制（MyBatis拦截器）- 待实现

**技术层面：**
- ✅ 微服务环境搭建（Nacos + MySQL + Redis）
- ✅ 文件服务（MinIO对象存储）
- ✅ 全文搜索（Elasticsearch + IK分词器）
- ✅ 消息队列（RabbitMQ）
- ✅ 实时通信（WebSocket）
- ✅ 前端框架（Vue3 + Element Plus）

## 🎯 功能特性

### 用户认证模块
- 用户注册与登录
- 图形验证码（EasyCaptcha）
- JWT Token认证
- Redis缓存验证码和Token
- 密码修改功能
- **密码强度校验**（最少8位，包含字母和数字）
- **密码过期策略**（180天自动过期）
- **登录失败锁定**（5次失败锁定30分钟，自动解锁）
- **管理员豁免**（管理员不受密码策略限制）

### 学生管理模块
- 学生信息CRUD（增删改查）
- 班级管理与联动
- 数据字典支持
- 学生头像上传（MinIO）
- 分页查询与条件筛选

### 教师管理模块
- 教师信息CRUD（增删改查）
- 教师头像上传（MinIO）
- 自动创建登录账号（添加教师时自动创建）
- 部门和职称筛选
- 关键词搜索（工号、姓名）
- 查看教师课程列表
- 课程-教师关联管理
- 教师信息与课程信息同步更新

### 课程管理模块
- 课程信息CRUD
- 课程-教师关联（下拉选择教师）
- 课程附件上传（支持文档、视频、音频、图片）
- 附件在线预览（Word、PDF、PPT、Excel、视频、音频、图片）
- 附件全文搜索（Elasticsearch + IK中文分词）
- 附件统一管理界面（统计分析、性能优化）
- 课程日历管理（支持周期性事件）
- 选课学生管理
- 成绩录入功能

### 选课系统模块
- 学生选课功能
- 课程浏览与搜索
- 我的课程列表
- 课程详情查看
- 退课功能
- 课程日历视图
- 每周课表展示

### 通知系统模块
- 实时通知推送（WebSocket）
- 消息队列处理（RabbitMQ）
- 通知类型（公告、作业、考试、取消）
- 优先级设置（普通、重要、紧急）
- 通知模板管理（支持多种类型模板）
- 定时发送通知（预约发送时间）
- 未读数量显示
- 通知列表与筛选
- 标记已读功能
- 向课程学生发送通知
- 向全体学生发送通知

### 文件管理模块
- MinIO对象存储
- 文件上传与下载
- 文件流式传输
- 文件预览支持
- 下载量和浏览量统计

### 接口鉴权模块
- 基于角色的权限控制（RBAC）
- @RequireRole注解支持方法级和类级权限控制
- HandlerInterceptor拦截器统一鉴权
- 三种角色：admin（管理员）、teacher（教师）、student（学生）
- **课程管理**：管理员和教师可以增删改，所有人可以查看
- **教师管理**：仅管理员可以操作
- **文件管理**：上传者和管理员可以删除，所有人可以下载
- **通知管理**：教师和管理员可以发送，学生只能查看
- **选课管理**：学生可以选课退课，教师和管理员可以查看学生名单和录入成绩

## 🛠 技术栈

### 后端技术
- **框架**：Spring Boot 3.2.0
- **微服务**：Spring Cloud Alibaba 2022.0.0.0
- **服务注册与配置**：Nacos 2.2.3
- **ORM框架**：MyBatis-Plus 3.5.5
- **数据库**：MySQL 8.0
- **缓存**：Redis 6.x
- **对象存储**：MinIO
- **全文搜索**：Elasticsearch 7.17.10 + IK分词器
- **消息队列**：RabbitMQ 4.0.5
- **实时通信**：WebSocket (STOMP)
- **认证**：JWT
- **验证码**：EasyCaptcha
- **文档解析**：Apache Tika、Apache POI、PDFBox
- **API文档**：Knife4j (OpenAPI 3.0)

### 前端技术
- **框架**：Vue 3.3
- **构建工具**：Vite 4.4
- **UI组件库**：Element Plus 2.3
- **HTTP客户端**：Axios
- **路由**：Vue Router 4
- **状态管理**：Pinia 2
- **WebSocket客户端**：SockJS + STOMP.js

## 📁 项目结构

```
JAVAEE_HWF/
├── backend/                          # 后端服务
│   ├── common/                       # 公共模块
│   │   ├── entity/                   # 公共实体类
│   │   ├── result/                   # 统一返回结果
│   │   ├── exception/                # 异常处理
│   │   └── utils/                    # 工具类
│   ├── gateway/                      # 网关服务 (端口: 8080)
│   │   ├── filter/                   # 网关过滤器
│   │   └── config/                   # 网关配置
│   ├── auth-service/                 # 认证服务 (端口: 8081)
│   │   ├── controller/               # 认证控制器
│   │   ├── service/                  # 认证服务
│   │   └── entity/                   # 用户实体
│   ├── student-service/              # 学生服务 (端口: 8082)
│   │   ├── controller/               # 学生控制器
│   │   ├── service/                  # 学生服务
│   │   ├── mapper/                   # MyBatis映射器
│   │   └── entity/                   # 学生实体
│   ├── file-service/                 # 文件服务 (端口: 8083)
│   │   ├── controller/               # 文件控制器
│   │   ├── service/                  # 文件服务
│   │   ├── config/                   # MinIO配置
│   │   └── entity/                   # 文件实体
│   └── course-service/               # 课程服务 (端口: 8084)
│       ├── controller/               # 课程控制器
│       ├── service/                  # 课程服务
│       ├── mapper/                   # MyBatis映射器
│       ├── entity/                   # 课程实体
│       ├── document/                 # ES文档实体
│       ├── repository/               # ES仓库
│       ├── listener/                 # RabbitMQ监听器
│       ├── websocket/                # WebSocket处理器
│       └── config/                   # 配置类
├── frontend/                         # 前端项目
│   ├── src/
│   │   ├── api/                      # 接口封装
│   │   │   ├── auth.js               # 认证接口
│   │   │   ├── student.js            # 学生接口
│   │   │   ├── teacher.js            # 教师接口
│   │   │   ├── course.js             # 课程接口
│   │   │   └── notification.js       # 通知接口
│   │   ├── views/                    # 页面组件
│   │   │   ├── Login.vue             # 登录页
│   │   │   ├── Home.vue              # 主页
│   │   │   ├── StudentList.vue       # 学生列表
│   │   │   ├── TeacherList.vue       # 教师列表
│   │   │   ├── CourseList.vue        # 课程列表
│   │   │   ├── CourseSelection.vue   # 选课中心
│   │   │   ├── MyCourses.vue         # 我的课程
│   │   │   ├── CourseCalendar.vue    # 课程日历
│   │   │   ├── WeeklySchedule.vue    # 每周课表
│   │   │   └── NotificationList.vue  # 通知列表
│   │   ├── components/               # 通用组件
│   │   │   └── NotificationBell.vue  # 通知图标
│   │   ├── router/                   # 路由配置
│   │   ├── store/                    # 状态管理
│   │   └── utils/                    # 工具类
│   │       ├── request.js            # Axios封装
│   │       └── websocket.js          # WebSocket管理器
│   └── package.json
├── scripts/                          # 脚本文件
│   └── init.sql                      # 数据库初始化脚本
├── install-middleware.sh             # 中间件自动安装脚本
├── pom.xml                           # 父POM
└── README.md                         # 项目文档
```

## 🚀 快速开始

### 1. 环境要求

- **Java**：JDK 21+
- **Node.js**：16+
- **MySQL**：8.0+
- **Redis**：6.x+
- **Nacos**：2.2.3
- **MinIO**：最新版
- **Elasticsearch**：7.17.10
- **RabbitMQ**：4.0+
- **Maven**：3.6+

### 2. 安装中间件

#### MySQL 8.0

```bash
# 安装
sudo apt install mysql-server

# 配置
mysql -uroot
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root123456';
CREATE DATABASE student_system DEFAULT CHARACTER SET utf8mb4;

# 导入初始化脚本
mysql -uroot -proot123456 student_system < scripts/init.sql
```

**初始测试账号：**
- 管理员：admin / 123456
- 教师账号：
  - T001 / 123456（张伟 - 计算机学院 - 教授）
  - T002 / 123456（李娜 - 计算机学院 - 副教授）
  - T003 / 123456（王强 - 软件学院 - 讲师）
  - T004 / 123456（刘芳 - 软件学院 - 副教授）
- 学生：student / 123456

#### Redis 6.x

```bash
# 安装
sudo apt install redis-server

# 配置密码
sudo vim /etc/redis/redis.conf
# 找到并修改: requirepass redis123456

# 重启
sudo systemctl restart redis-server

# 测试
redis-cli -a redis123456
ping
```

#### Nacos 2.2.3

```bash
# 下载
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz
tar -zxvf nacos-server-2.2.3.tar.gz

# 配置MySQL存储（编辑 nacos/conf/application.properties）
spring.datasource.platform=mysql
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos_config
db.user.0=root
db.password.0=root123456

# 导入数据库
mysql -uroot -proot123456
CREATE DATABASE nacos_config DEFAULT CHARACTER SET utf8mb4;
USE nacos_config;
source nacos/conf/mysql-schema.sql;

# 启动（单机模式）
sh nacos/bin/startup.sh -m standalone

# 访问控制台
http://localhost:8848/nacos
用户名/密码：nacos/nacos
```

#### MinIO

```bash
# 下载
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio

# 启动
mkdir -p ~/minio/data
./minio server ~/minio/data --console-address ":9001"

# 访问控制台
http://localhost:9001
默认用户名/密码：minioadmin/minioadmin

# 创建bucket
在控制台创建名为 "student-system" 的bucket
设置访问策略为 "public"
```

#### Elasticsearch 7.17.10 + IK分词器

```bash
# 下载Elasticsearch
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.10-linux-x86_64.tar.gz
tar -zxvf elasticsearch-7.17.10-linux-x86_64.tar.gz
cd elasticsearch-7.17.10

# 下载IK分词器
wget https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.10/elasticsearch-analysis-ik-7.17.10.zip
unzip elasticsearch-analysis-ik-7.17.10.zip -d plugins/ik

# 启动
./bin/elasticsearch

# 测试
curl http://localhost:9200
```

#### RabbitMQ 4.0+

```bash
# 安装
sudo apt install rabbitmq-server

# 启用管理插件
sudo rabbitmq-plugins enable rabbitmq_management

# 启动
sudo systemctl start rabbitmq-server

# 访问管理控制台
http://localhost:15672
默认用户名/密码：guest/guest
```

### 3. 启动后端服务

按以下顺序启动各服务：

```bash
cd JAVAEE_HWF

# 1. 编译打包
mvn clean package -DskipTests

# 2. 启动网关（必须第一个启动）
java -jar ./backend/gateway/target/gateway-1.0.0.jar &

# 3. 启动认证服务
java -jar ./backend/auth-service/target/auth-service-1.0.0.jar &

# 4. 启动学生服务
java -jar ./backend/student-service/target/student-service-1.0.0.jar &

# 5. 启动文件服务
java -jar ./backend/file-service/target/file-service-1.0.0.jar &

# 6. 启动课程服务
java -jar ./backend/course-service/target/course-service-1.0.0.jar &
```

**验证服务注册：**
访问 http://localhost:8848/nacos，查看服务列表，确保所有服务都已注册。

### 4. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 http://localhost:3000

### 5. 测试系统

1. **登录系统**
   - 打开浏览器访问 http://localhost:3000
   - 使用测试账号登录（admin/123456）

2. **测试学生管理**
   - 添加、编辑、删除学生
   - 上传学生头像

3. **测试教师管理**
   - 添加、编辑、删除教师
   - 上传教师头像
   - 查看教师课程列表
   - 验证自动创建的教师账号

4. **测试课程管理**
   - 创建课程（从下拉列表选择教师）
   - 上传课程附件（文档、视频、音频、图片）
   - 搜索附件内容
   - 预览附件
   - 管理课程日历

5. **测试选课系统**
   - 使用学生账号登录
   - 浏览课程列表
   - 选课和退课
   - 查看课程日历和每周课表

6. **测试通知系统**
   - 使用教师账号登录（T001/123456）
   - 发送课程通知或全体通知
   - 学生端查看实时通知
   - 标记已读

## 📡 API接口文档

### 认证服务（auth-service - 8081）

| 接口 | 方法 | 说明 |
|------|------|------|
| /auth/register | POST | 用户注册 |
| /auth/login | POST | 用户登录 |
| /auth/captcha | GET | 获取验证码 |
| /auth/logout | POST | 退出登录 |
| /user/info | GET | 获取用户信息 |
| /user/password | PUT | 修改密码 |

### 学生服务（student-service - 8082）

| 接口 | 方法 | 说明 |
|------|------|------|
| /student/list | GET | 分页查询学生列表 |
| /student/add | POST | 添加学生 |
| /student/update | PUT | 更新学生 |
| /student/delete/{id} | DELETE | 删除学生 |
| /student/{id} | GET | 获取学生详情 |
| /student/avatar/upload | POST | 上传学生头像 |
| /class/list | GET | 获取班级列表 |
| /dict/list/{dictType} | GET | 获取字典数据 |

### 教师服务（course-service - 8084）

| 接口 | 方法 | 说明 |
|------|------|------|
| /teacher/list | GET | 分页查询教师列表 |
| /teacher/all | GET | 获取所有教师（下拉选择） |
| /teacher/add | POST | 添加教师（自动创建账号） |
| /teacher/update | PUT | 更新教师 |
| /teacher/delete/{id} | DELETE | 删除教师 |
| /teacher/{id} | GET | 获取教师详情 |
| /teacher/avatar/upload | POST | 上传教师头像 |
| /teacher/courses/{teacherId} | GET | 获取教师课程列表 |

### 文件服务（file-service - 8083）

| 接口 | 方法 | 说明 |
|------|------|------|
| /file/upload | POST | 上传文件 |
| /file/download/{id} | GET | 下载文件 |
| /file/stream/{id} | GET | 流式传输文件 |
| /file/delete/{id} | DELETE | 删除文件 |
| /file/{id} | GET | 获取文件信息 |

### 课程服务（course-service - 8084）

**课程管理：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /course/list | GET | 分页查询课程列表 |
| /course/add | POST | 添加课程 |
| /course/update | PUT | 更新课程 |
| /course/delete/{id} | DELETE | 删除课程 |
| /course/{id} | GET | 获取课程详情 |

**课程附件：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /course/attachment/upload | POST | 上传课程附件 |
| /course/attachment/list/{courseId} | GET | 获取课程附件列表 |
| /course/attachment/delete/{id} | DELETE | 删除附件 |
| /course/attachment/search | GET | 搜索附件（ES全文搜索） |
| /course/attachment/download/{id} | POST | 记录下载 |
| /course/attachment/view/{id} | POST | 记录浏览 |

**选课管理：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /course/enroll | POST | 学生选课 |
| /course/drop/{enrollmentId} | DELETE | 学生退课 |
| /course/my-enrollments | GET | 我的选课列表 |
| /course/students/{courseId} | GET | 获取选课学生列表 |
| /course/score/update | PUT | 录入成绩 |

**课程日历：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /course/calendar/{courseId} | GET | 获取课程日历 |
| /course/calendar/generate | POST | 批量生成日历 |
| /course/calendar/event/create | POST | 创建日历事件 |
| /course/calendar/event/update | PUT | 更新日历事件 |
| /course/calendar/event/delete/{id} | DELETE | 删除日历事件 |

**通知管理：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /notification/create | POST | 创建并发送通知 |
| /notification/list | GET | 获取通知列表 |
| /notification/my | GET | 获取我的通知 |
| /notification/read/{id} | PUT | 标记已读 |
| /notification/read/all | PUT | 全部标记已读 |
| /notification/unread/count | GET | 获取未读数量 |
| /notification/delete/{id} | DELETE | 删除通知 |

**所有接口统一通过网关访问（端口：8080），需在请求头中携带Token：**
```
Authorization: Bearer {token}
```

## 🔧 核心技术说明

### 验证码生成
- 使用EasyCaptcha生成图形验证码
- Base64编码返回前端
- Redis存储验证码，key格式：`captcha:{uuid}`，有效期5分钟

### JWT认证
- Token有效期：24小时
- Token存储于Redis，支持续期和主动失效
- 网关统一拦截并验证Token

### 文件上传（MinIO）
- 对象存储，支持大文件上传
- 自动生成唯一文件名
- 支持文件流式传输
- 记录下载量和浏览量

### 全文搜索（Elasticsearch）
- IK中文分词器（ik_max_word索引，ik_smart搜索）
- 支持文档内容提取（Word、PDF、PPT、Excel、TXT）
- 异步建立索引
- 支持高级搜索（按课程、类型筛选）

### 消息推送（RabbitMQ + WebSocket）
- RabbitMQ处理消息队列
- WebSocket实时推送给在线用户
- 支持站内信、短信、邮件（短信和邮件待接入）
- 消息持久化存储
- 未读数量统计

### 网关路由
- `/auth/**`, `/user/**` → auth-service
- `/student/**`, `/class/**`, `/dict/**` → student-service
- `/file/**` → file-service
- `/course/**`, `/notification/**`, `/teacher/**` → course-service

### 接口鉴权（基于角色的权限控制）
- **@RequireRole注解**：自定义注解，支持方法级和类级权限控制
- **PermissionInterceptor拦截器**：实现HandlerInterceptor接口，统一拦截所有请求
- **权限验证流程**：
  1. 拦截器读取方法和类上的@RequireRole注解
  2. 从请求头获取userType（由Gateway注入）
  3. 验证userType是否在允许的角色列表中
  4. 方法级注解优先于类级注解
- **权限策略**：
  - 无@RequireRole注解的接口：所有已认证用户可访问
  - @RequireRole({"admin"})：仅管理员可访问
  - @RequireRole({"admin", "teacher"})：管理员和教师可访问
- **特殊逻辑**：文件删除采用服务层权限控制（上传者OR管理员）

### 跨域处理
- 网关统一配置CORS，允许所有来源

## 🐛 故障排查

### 中间件连接失败

```bash
# 检查MySQL
mysql -uroot -proot123456

# 检查Redis
redis-cli -a redis123456
ping

# 检查Nacos
curl http://localhost:8848/nacos

# 检查MinIO
curl http://localhost:9000/minio/health/live

# 检查Elasticsearch
curl http://localhost:9200

# 检查RabbitMQ
curl http://localhost:15672
```

### 服务注册失败
- 确认Nacos已启动
- 检查application.yml中的nacos地址配置
- 查看服务日志

### 前端API请求失败
- 确认网关服务已启动（8080端口）
- 检查浏览器控制台网络请求
- 确认Token是否过期

### 文件上传失败
- 确认MinIO已启动
- 检查bucket是否创建
- 检查访问策略是否为public

### 搜索功能不工作
- 确认Elasticsearch已启动
- 检查IK分词器是否安装
- 查看索引是否创建成功

### 通知推送不工作
- 确认RabbitMQ已启动
- 检查WebSocket连接状态（浏览器控制台）
- 查看队列是否创建

## 💻 开发说明

### 添加新服务
1. 在backend下创建新模块
2. pom.xml中添加module声明
3. 继承父POM，引入common模块
4. 配置Nacos服务发现
5. 在gateway中配置路由规则

### 数据库字段自动填充
BaseEntity提供了自动填充支持：
- createTime：插入时自动填充
- updateTime：插入和更新时自动填充

### 统一异常处理
GlobalExceptionHandler提供全局异常拦截：
- BusinessException：业务异常
- Exception：系统异常

### WebSocket连接
- 端点：`/ws/notification`
- 协议：STOMP over SockJS
- 订阅：`/user/{userId}/queue/notification`（私有队列）
- 订阅：`/topic/notification`（广播主题）

## 🚀 生产部署建议

### 数据库
- 修改默认密码
- 配置主从复制
- 定期备份数据
- 优化索引

### Redis
- 开启持久化（AOF/RDB）
- 配置密码认证
- 调整maxmemory策略
- 配置主从复制

### Nacos
- 使用集群模式部署
- 配置持久化到MySQL
- 开启鉴权
- 配置健康检查

### MinIO
- 配置集群模式
- 开启版本控制
- 配置生命周期策略
- 定期备份

### Elasticsearch
- 配置集群模式
- 调整JVM堆内存
- 配置索引分片和副本
- 定期清理旧索引

### RabbitMQ
- 配置集群模式
- 开启消息持久化
- 配置死信队列
- 监控队列积压

### 应用服务
- 配置JVM参数优化内存
- 使用systemd管理服务
- 配置日志轮转
- 配置健康检查
- 使用Nginx反向代理

## 📈 后续扩展计划

### 功能扩展
- [x] 登录增强（强弱密码校验+密码过期+错误次数控制）
- [x] 接口鉴权（基于角色的权限控制）
- [ ] 数据权限控制（MyBatis拦截器）
- [x] 教师管理模块（完整CRUD+头像上传+自动创建账号）
- [x] 通知模板功能
- [x] 定时发送通知
- [x] 附件管理界面（统一管理+统计分析）
- [ ] 短信和邮件推送
- [ ] 在线考试系统
- [ ] 作业提交系统
- [ ] 成绩分析报表

### 技术优化
- [ ] 服务监控（Prometheus + Grafana）
- [ ] 链路追踪（SkyWalking）
- [ ] 日志收集（ELK）
- [ ] 接口限流（Sentinel）
- [ ] 分布式事务（Seata）
- [ ] 单元测试覆盖

## 📄 许可证

MIT License

## 👥 联系方式

如有问题，请提Issue或联系开发者。

---

**项目完成度：中等难度 100% | 困难档 50%**

**最后更新：2025-12-21**
