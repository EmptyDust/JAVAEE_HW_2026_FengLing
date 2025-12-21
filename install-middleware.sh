#!/bin/bash

set -e

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}>>> 正在修复 RabbitMQ 安装 (Debian 13 专用)...${NC}"

# 1. 彻底清理可能冲突的旧版 Erlang (Debian 13 自带的 v27 不兼容 3.12)
echo -e "${GREEN}清理可能冲突的 Erlang 版本...${NC}"
apt-get purge -y erlang* || true
apt-get autoremove -y

# 2. 安装必要依赖
apt-get update
apt-get install -y socat logrotate curl ca-certificates

# 3. 定义版本和新的加速代理 (使用 ghfast.top)
# RabbitMQ 3.12.13 + Erlang 26.2.5.2 (Bookworm版，兼容Trixie)
PROXY="https://ghfast.top/"
ERLANG_URL="${PROXY}https://github.com/rabbitmq/erlang-debian-package/releases/download/v26.2.5.2/erlang-nox_26.2.5.2-1_debian_bookworm_amd64.deb"
RABBIT_URL="${PROXY}https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.12.13/rabbitmq-server_3.12.13-1_all.deb"

# 4. 下载 Erlang
echo -e "${GREEN}正在下载 Erlang 26 (适配版)...${NC}"
curl -L -o /tmp/erlang-nox.deb "${ERLANG_URL}"

# 5. 下载 RabbitMQ
echo -e "${GREEN}正在下载 RabbitMQ 3.12.13...${NC}"
curl -L -o /tmp/rabbitmq-server.deb "${RABBIT_URL}"

# 6. 安装 Erlang 并锁定版本
echo -e "${GREEN}正在安装 Erlang...${NC}"
dpkg -i /tmp/erlang-nox.deb || apt-get install -f -y
apt-mark hold erlang-nox

# 7. 安装 RabbitMQ
echo -e "${GREEN}正在安装 RabbitMQ Server...${NC}"
dpkg -i /tmp/rabbitmq-server.deb || apt-get install -f -y

# 8. 启用插件与配置
echo -e "${GREEN}正在配置 RabbitMQ...${NC}"
rabbitmq-plugins enable rabbitmq_management

# 允许 guest 远程访问管理界面
cat > /etc/rabbitmq/rabbitmq.conf <<EOF
loopback_users.guest = false
listeners.tcp.default = 5672
management.listener.port = 15672
EOF

# 9. 启动服务
systemctl daemon-reload
systemctl enable rabbitmq-server
systemctl restart rabbitmq-server

# 清理临时文件
rm -f /tmp/erlang-nox.deb /tmp/rabbitmq-server.deb