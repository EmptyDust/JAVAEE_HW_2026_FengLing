<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>学生信息管理系统</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-box">
            <el-input v-model="form.captcha" placeholder="请输入验证码" style="width: 150px"></el-input>
            <img :src="captchaImage" @click="refreshCaptcha" class="captcha-img" alt="验证码">
          </div>
        </el-form-item>
        <el-form-item label-width="0">
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
        <el-form-item label-width="0">
          <el-button type="text" @click="goToRegister" style="width: 100%">没有账号？立即注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, login } from '../api/auth'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const captchaImage = ref('')
const captchaUuid = ref('')

const form = ref({
  username: '',
  password: '',
  captcha: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captchaImage.value = res.data.image
    captchaUuid.value = res.data.uuid
  } catch (error) {
    ElMessage.error('获取验证码失败')
  }
}

const handleLogin = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const res = await login({
        username: form.value.username,
        password: form.value.password,
        uuid: captchaUuid.value,
        captcha: form.value.captcha
      })

      userStore.setToken(res.data.token)
      userStore.setUserInfo({
        userId: res.data.userId,
        username: res.data.username,
        userType: res.data.userType,
        studentId: res.data.studentId
      })

      // 检查密码是否过期
      if (res.data.passwordExpired) {
        ElMessage.warning({
          message: res.data.message || '您的密码已过期，请立即修改密码',
          duration: 5000
        })
        // 可以在这里跳转到密码修改页面
        // router.push('/change-password?forced=true')
        // 暂时先跳转到正常页面，用户可以在个人中心修改密码
      } else {
        ElMessage.success('登录成功')
      }

      // 根据用户类型跳转到不同页面
      if (res.data.userType === 'student') {
        router.push('/home/profile')
      } else {
        router.push('/home/students')
      }
    } catch (error) {
      // 显示详细的错误信息
      const errorMessage = error.response?.data?.message || error.message || '登录失败'

      // 根据错误类型显示不同的消息类型
      if (errorMessage.includes('锁定')) {
        ElMessage.error({
          message: errorMessage,
          duration: 5000
        })
      } else if (errorMessage.includes('还有')) {
        ElMessage.warning({
          message: errorMessage,
          duration: 3000
        })
      } else {
        ElMessage.error(errorMessage)
      }

      form.value.captcha = ''
      refreshCaptcha()
    }
  })
}

const goToRegister = () => {
  router.push('/register')
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 480px;
  max-width: 100%;
  padding: 35px 45px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

h2 {
  text-align: center;
  margin-bottom: 25px;
  margin-top: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.captcha-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-img {
  height: 40px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
}

.captcha-img:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

/* 优化表单项间距 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 最后一个表单项（注册按钮）减少底部间距 */
:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

/* 优化按钮样式 */
:deep(.el-button--primary) {
  height: 42px;
  font-size: 16px;
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

:deep(.el-button--text) {
  color: #909399;
  font-size: 14px;
}

:deep(.el-button--text:hover) {
  color: #409eff;
}

/* 优化输入框样式 */
:deep(.el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}
</style>
