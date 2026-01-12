#!/bin/bash

# =====================================================
# 教师登录测试脚本（完整流程）
# =====================================================

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "教师登录测试（验证 teacherId 是否返回）"
echo "=========================================="
echo ""

# 步骤1：获取验证码
echo "步骤1：获取验证码..."
CAPTCHA_RESPONSE=$(curl -s -X GET "${BASE_URL}/auth/captcha")
echo "验证码响应: $CAPTCHA_RESPONSE"
echo ""

# 解析 uuid（使用 jq 或 grep）
UUID=$(echo $CAPTCHA_RESPONSE | grep -o '"uuid":"[^"]*"' | cut -d'"' -f4)
echo "UUID: $UUID"
echo ""

# 注意：验证码图片是 base64 编码的，无法在命令行中显示
# 在实际测试中，你需要：
# 1. 访问前端页面获取验证码图片
# 2. 或者临时禁用验证码校验
echo "⚠️  注意：由于无法在命令行中显示验证码图片，请选择以下方式之一："
echo "   方式1：访问前端页面 http://localhost:3000 获取验证码"
echo "   方式2：临时禁用验证码校验（见下方说明）"
echo ""

# 步骤2：手动输入验证码
read -p "请输入验证码（4位字符）: " CAPTCHA
echo ""

# 步骤3：教师登录
echo "步骤2：教师登录（用户名: T001）..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"T001\",
    \"password\": \"123456\",
    \"uuid\": \"${UUID}\",
    \"captcha\": \"${CAPTCHA}\"
  }")

echo "登录响应:"
echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"
echo ""

# 步骤4：验证 teacherId 是否存在
echo "步骤3：验证 teacherId 是否返回..."
TEACHER_ID=$(echo $LOGIN_RESPONSE | grep -o '"teacherId":[0-9]*' | cut -d':' -f2)

if [ -n "$TEACHER_ID" ]; then
    echo "✅ 成功：teacherId = $TEACHER_ID"
    echo ""

    # 提取 token
    TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

    # 步骤5：测试获取教师信息
    echo "步骤4：测试获取教师信息（/teacher/me）..."
    TEACHER_INFO=$(curl -s -X GET "${BASE_URL}/teacher/me" \
      -H "Authorization: Bearer ${TOKEN}")

    echo "教师信息响应:"
    echo "$TEACHER_INFO" | jq '.' 2>/dev/null || echo "$TEACHER_INFO"
    echo ""
else
    echo "❌ 失败：响应中没有 teacherId 字段"
    echo ""
    echo "可能的原因："
    echo "1. 验证码错误"
    echo "2. 用户名或密码错误"
    echo "3. User 表中没有 teacher_id 字段"
    echo "4. 数据库中该教师的 teacher_id 为 NULL"
    echo ""
fi

echo "=========================================="
echo "测试完成"
echo "=========================================="
