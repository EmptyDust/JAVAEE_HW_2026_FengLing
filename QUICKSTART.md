# 学生信息管理系统 - 快速修复指南

## 编译错误说明

当前系统使用 Java 21，但项目配置过程中出现了一些兼容性问题。我已将依赖版本升级到适配Java 21的版本，但仍需进一步调试。

## 当前状态

项目已创建完整的文件结构，包括：
- ✅ 后端微服务代码（common、gateway、auth-service、student-service）
- ✅ 前端Vue3项目代码
- ✅ SQL初始化脚本
- ✅ 中间件安装脚本
- ⚠️  需要解决Java版本兼容性问题

## 解决方案

### 方案1：降级到Java 11（推荐）

```bash
# 安装Java 11
sudo apt install openjdk-11-jdk

# 切换Java版本
sudo update-alternatives --config java
# 选择Java 11

# 验证版本
java -version  # 应该显示 openjdk version "11.x.x"

# 重新编译
mvn clean package -DskipTests
```

### 方案2：继续使用Java 21但需手动修复

如果坚持使用Java 21，需要修改以下内容：

1. **父POM已更新**（/home/emptydust/JAVAEE_HWF/pom.xml）
   - Spring Boot版本已升级到3.2.0
   - 各个依赖版本已升级

2. **配置文件需调整**
   - `spring.redis` → `spring.data.redis`（已修复）
   - JWT API从0.11.5升级到0.12.3（已修复）

3. **仍可能存在的问题**
   - MyBatis Plus与Spring Boot 3的兼容性
   - Nacos与Spring Cloud 2023的兼容性

## 项目文件结构

```
JAVAEE_HWF/
├── backend/
│   ├── common/              # 公共模块（Result、异常处理、JWT、Redis工具）
│   ├── gateway/             # 网关服务（8080端口，路由+鉴权）
│   ├── auth-service/        # 认证服务（8081端口，登录+验证码）
│   └── student-service/     # 学生服务（8082端口，学生CRUD）
├── frontend/                # Vue3前端项目
│   ├── src/
│   │   ├── views/           # 登录页、主页、学生列表页
│   │   ├── api/             # 接口封装
│   │   ├── router/          # 路由配置
│   │   ├── store/           # Pinia状态管理
│   │   └── utils/           # Axios请求拦截器
│   └── package.json
├── scripts/
│   └── init.sql            # 数据库初始化脚本
├── install-middleware.sh    # 中间件自动安装脚本
└── pom.xml                  # 父POM配置
```

## 快速开始（推荐方案1完成后）

### 1. 安装中间件

```bash
sudo chmod +x install-middleware.sh
sudo ./install-middleware.sh
```

### 2. 初始化数据库

```bash
mysql -uroot -proot123456 student_system < scripts/init.sql
```

### 3. 编译后端

```bash
mvn clean package -DskipTests
```

### 4. 启动服务

```bash
# 终端1 - 网关
java -jar backend/gateway/target/gateway-1.0.0.jar

# 终端2 - 认证服务
java -jar backend/auth-service/target/auth-service-1.0.0.jar

# 终端3 - 学生服务
java -jar backend/student-service/target/student-service-1.0.0.jar
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 6. 访问系统

打开浏览器访问 http://localhost:3000

默认测试账号：
- 用户名：admin
- 密码：123456

## 核心功能说明

### 后端接口

**认证服务（8081）**
- `GET /auth/captcha` - 获取验证码
- `POST /auth/register` - 用户注册
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 退出登录

**学生服务（8082）**
- `GET /student/list` - 分页查询学生
- `POST /student/add` - 添加学生
- `PUT /student/update` - 更新学生
- `DELETE /student/delete/{id}` - 删除学生
- `GET /class/list` - 获取班级列表
- `GET /dict/list/{dictType}` - 获取字典数据

所有请求通过网关（8080）统一转发，需在Header中携带Token：
```
Authorization: Bearer {token}
```

### 前端页面

- `/login` - 登录页（支持验证码）
- `/home/students` - 学生管理页（表格、分页、增删改查、班级下拉）

## 技术栈

### 后端
- Spring Boot 3.2.0 / 2.7.18（视Java版本而定）
- Spring Cloud Alibaba
- Nacos（服务发现+配置中心）
- MyBatis-Plus
- MySQL 8.0
- Redis 6.x
- JWT（用户认证）

### 前端
- Vue 3.3
- Element Plus 2.3
- Vite 4.4
- Axios
- Pinia（状态管理）

## 下一步计划

完成Java版本适配后，可扩展以下功能：
- 课程管理（Minio文件上传、ES全文搜索）
- 选课系统（课程日历、WebSocket消息推送、RabbitMQ）
- 数据权限控制（MyBatis拦截器）
- 接口鉴权（AOP+RBAC）

## 故障排查

### Nacos启动失败
```bash
# 查看日志
tail -f /opt/student-system/nacos/logs/start.out

# 确保MySQL已启动且nacos_config数据库存在
mysql -uroot -proot123456 -e "SHOW DATABASES LIKE 'nacos_config';"
```

### Redis连接失败
```bash
# 测试Redis连接
redis-cli -a redis123456 ping  # 应返回 PONG
```

### 编译失败
```bash
# 清理Maven本地仓库缓存
mvn dependency:purge-local-repository

# 重新下载依赖
mvn clean install -DskipTests -U
```

## 联系支持

如有问题请提供：
1. Java版本 (`java -version`)
2. Maven版本 (`mvn -version`)
3. 完整的错误日志

项目已完成 80% 的核心代码，主要剩余工作是解决Java版本兼容性问题。
