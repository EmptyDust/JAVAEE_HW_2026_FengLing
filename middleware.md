### 📋 教务管理系统 - 中间件安装报告

**环境概览：**
*   **操作系统**: Debian Trixie (Debian 13 / Testing)
*   **运行环境**: OpenJDK 21
*   **安装路径**: `/opt/student-system`

---

#### 1. 数据库 (MariaDB/MySQL)
*   **版本**: MariaDB 11.x
*   **访问地址**: `127.0.0.1:3306`
*   **管理用户**: `root`
*   **登录密码**: `root123456`
*   **已创建库**: 
    *   `student_system` (业务数据库)
    *   `nacos_config` (Nacos 配置数据库)

#### 2. 缓存服务 (Redis)
*   **版本**: Redis 7.x/8.x
*   **访问地址**: `127.0.0.1:6379`
*   **访问密码**: `redis123456`
*   **连接建议**: 生产环境已开启 `requirepass` 验证。

#### 3. 配置与注册中心 (Nacos)
*   **版本**: 2.2.3 (Standalone 模式)
*   **控制台地址**: [http://localhost:8848/nacos](http://localhost:8848/nacos)
*   **管理用户**: `nacos`
*   **登录密码**: `nacos`
*   **关键端口**: 
    *   `8848`: HTTP 访问端口
    *   `9848/9849`: gRPC 通信端口（Nacos 2.x 客户端连接必需）

---

#### 🛠 常用维护命令
*   **重启所有服务**:
    ```bash
    sudo systemctl restart mariadb redis-server nacos
    ```
*   **查看 Nacos 运行日志**:
    ```bash
    tail -f /opt/student-system/nacos/logs/start.out
    ```
*   **查看服务监听状态**:
    ```bash
    sudo ss -tunlp | grep -E '3306|6379|8848'
    ```

针对国内网络环境，直接从 `dl.min.io` 下载速度极慢甚至无法连接。推荐使用 **MinIO 中国社区镜像站** 或 **南京大学镜像站**。

以下是适配你之前环境（Debian Trixie, `/home/emptydust` 路径）的安装指令，并附带了 **Systemd 服务配置**，让它能像之前的 Nacos 一样自动后台运行。

### 1. 使用国内镜像下载 MinIO

```bash
# 进入安装目录
mkdir -p /home/emptydust/minio
cd /home/emptydust/minio

# 使用中国社区加速镜像下载
wget https://dl.minio.org.cn/server/minio/release/linux-amd64/minio

# 授权
chmod +x minio

# 创建数据存储目录
mkdir -p /home/emptydust/minio/data
```

---

### 2. 配置 Systemd 后台服务 (推荐)

为了不让启动命令断开连接就停止，建议创建一个服务文件：

```bash
sudo vim /etc/systemd/system/minio.service
```

将以下内容粘贴进去（注意：路径已适配你的用户名 `emptydust`）：

```ini
[Unit]
Description=MinIO
Documentation=https://docs.min.io
Wants=network-online.target
After=network-online.target

[Service]
User=root
Group=root
WorkingDirectory=/home/emptydust/minio

# 设置账号密码
Environment="MINIO_ROOT_USER=admin"
Environment="MINIO_ROOT_PASSWORD=admin123456"

# 启动命令：API端口9000，控制台端口9001
ExecStart=/home/emptydust/minio/minio server /home/emptydust/minio/data --address ":9000" --console-address ":9001"

Restart=always
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```

---

### 3. 启动并设置开机自启

```bash
# 重新加载配置
sudo systemctl daemon-reload

# 启动并设置自启
sudo systemctl start minio
sudo systemctl enable minio

# 查看状态
sudo systemctl status minio
```

---

### 4. 报告：MinIO 安装汇总

| 项目 | 内容 |
| :--- | :--- |
| **安装方式** | Linux 二进制 (中国加速镜像) |
| **API 访问地址** | `http://服务器IP:9000` (代码连接用) |
| **控制台访问地址** | `http://服务器IP:9001` (浏览器访问用) |
| **管理员账号** | `admin` |
| **管理员密码** | `admin123456` |
| **数据存储路径** | `/home/emptydust/minio/data` |

---

### 💡 开发提醒：
1. **防火墙放行**：如果你开启了 UFW，请务必执行：
   ```bash
   sudo ufw allow 9000/tcp
   sudo ufw allow 9001/tcp
   ```
2. **Spring Boot 集成**：在你的 `student-system` 项目中，如果需要上传学生头像等，请确保连接的是 **9000** 端口，而不是控制台的 9001 端口。
3. **Java SDK 依赖**：Java 21 环境下，建议使用最新版的 MinIO SDK：
   ```xml
   <dependency>
       <groupId>io.minio</groupId>
       <artifactId>minio</artifactId>
       <version>8.5.7</version>
   </dependency>
   ```

这是为您整理的中间件访问清单：

### 🛠️ 开发环境中间件访问指南

| 服务名称 | 访问地址 (URL) | 账号 | 密码 | 备注 |
| :--- | :--- | :--- | :--- | :--- |
| **Elasticsearch** | `http://localhost:9200` | 无 | 无 | 全文搜索服务 (7.17.10) |
| **Kibana** | `http://localhost:5601` | 无 | 无 | 可视化管理后台 (中文) |
| **RabbitMQ** | `http://localhost:15672` | `guest` | `guest` | 消息队列管理面板 (4.0.5) |

---

**💡 提示：**
*   **RabbitMQ 端口**: 管理后台使用 `15672`，Java 代码连接（AMQP）请使用 `5672`。
*   **远程访问**: 如果在宿主机（Windows）浏览器无法访问，请将 `localhost` 替换为 Debian 的实际 IP 地址（通过 `hostname -I` 查看）。