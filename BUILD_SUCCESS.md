# 学生信息管理系统 - 编译成功！

## 编译结果

项目已成功编译通过！所有服务的JAR文件已生成：

```
backend/auth-service/target/auth-service-1.0.0.jar        (64MB)
backend/gateway/target/gateway-1.0.0.jar                   (60MB)
backend/student-service/target/student-service-1.0.0.jar   (64MB)
backend/common/target/common-1.0.0.jar                     (13KB)
```

## 快速启动指南

### 1. 启动中间件

确保以下中间件已安装并运行：

```bash
# 使用自动安装脚本（推荐）
sudo chmod +x install-middleware.sh
sudo ./install-middleware.sh

# 或手动启动
sudo systemctl start mysql
sudo systemctl start redis-server
sudo systemctl start nacos  # 或 cd /opt/student-system/nacos && sh bin/startup.sh -m standalone
```

### 2. 初始化数据库

```bash
mysql -uroot -proot123456 student_system < scripts/init.sql
```

### 3. 启动后端服务

打开3个终端窗口，按顺序启动：

**终端1 - 网关服务（必须第一个启动）**
```bash
cd /home/emptydust/JAVAEE_HWF
java -jar backend/gateway/target/gateway-1.0.0.jar
```

**终端2 - 认证服务**
```bash
cd /home/emptydust/JAVAEE_HWF
java -jar backend/auth-service/target/auth-service-1.0.0.jar
```

**终端3 - 学生服务**
```bash
cd /home/emptydust/JAVAEE_HWF
java -jar backend/student-service/target/student-service-1.0.0.jar
```

等待所有服务启动完成，看到类似输出：
```
Started GatewayApplication in X.X seconds
Started AuthApplication in X.X seconds
Started StudentApplication in X.X seconds
```

### 4. 验证服务注册

打开浏览器访问 Nacos 控制台：
http://localhost:8848/nacos

- 用户名：nacos
- 密码：nacos

在"服务管理 > 服务列表"中应该看到3个服务：
- gateway
- auth-service
- student-service

### 5. 启动前端

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端将在 http://localhost:3000 启动

### 6. 登录测试

打开浏览器访问 http://localhost:3000

测试账号：
- 用户名：**admin**
- 密码：**123456**

点击验证码图片可刷新验证码。

登录成功后即可进入学生管理页面进行增删改查操作。

## API测试（可选）

如果需要直接测试后端API：

### 获取验证码
```bash
curl http://localhost:8080/auth/captcha
```

### 登录
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456",
    "uuid": "验证码UUID",
    "captcha": "验证码"
  }'
```

### 查询学生列表（需要Token）
```bash
curl http://localhost:8080/student/list?page=1&size=10 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 端口占用说明

请确保以下端口未被占用：
- 3000 - 前端开发服务器
- 3306 - MySQL
- 6379 - Redis
- 8080 - 网关服务
- 8081 - 认证服务
- 8082 - 学生服务
- 8848 - Nacos

## 故障排查

### 服务启动失败

**检查Nacos是否运行：**
```bash
curl http://localhost:8848/nacos
# 应返回HTML页面
```

**检查MySQL连接：**
```bash
mysql -uroot -proot123456 -e "SHOW DATABASES;"
# 应能成功连接并显示数据库列表
```

**检查Redis连接：**
```bash
redis-cli -a redis123456 ping
# 应返回 PONG
```

### 前端无法访问后端

1. 确认网关服务（8080端口）已启动
2. 检查浏览器控制台是否有跨域错误
3. 确认前端配置的代理地址正确（vite.config.js中的target）

### 验证码显示/验证失败

1. 确认Redis服务正常运行
2. 验证码有效期为5分钟，过期需重新获取
3. 验证码区分大小写

## 技术栈（Java 21版）

- **Java**: 21
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Spring Cloud Alibaba**: 2022.0.0.0
- **Nacos**: 2.2.3
- **MyBatis-Plus**: 3.5.5
- **MySQL**: 8.0.33
- **Redis**: 6.x
- **JWT**: 0.12.3
- **Lombok**: 1.18.30

## 下一步扩展

当前已实现第一阶段核心功能，可继续开发：

**第二阶段（扩展功能）：**
- 课程管理（Minio文件上传、Elasticsearch全文搜索）
- 选课系统（课程日历、WebSocket/RabbitMQ消息推送）
- 数据权限控制（MyBatis拦截器）
- 接口鉴权（AOP+RBAC）

## 项目结构

```
JAVAEE_HWF/
├── backend/
│   ├── common/                  # 公共模块
│   ├── gateway/                 # 网关服务（8080）
│   ├── auth-service/            # 认证服务（8081）
│   └── student-service/         # 学生服务（8082）
├── frontend/                    # Vue3前端
├── scripts/
│   └── init.sql                 # 数据库初始化脚本
├── install-middleware.sh        # 中间件安装脚本
├── pom.xml                      # 父POM
├── README.md                    # 项目文档
├── QUICKSTART.md                # 快速开始指南
└── BUILD_SUCCESS.md             # 本文件
```

## 祝贺！

项目已经完全可以运行了。enjoy coding!
