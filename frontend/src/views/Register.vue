<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>学生注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="1" :max="150" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%" filterable>
            <el-option
              v-for="classItem in classList"
              :key="classItem.id"
              :label="`${classItem.className} (${classItem.classNo})`"
              :value="classItem.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%">注册</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="text" @click="goToLogin" style="width: 100%">已有账号？立即登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addStudent } from '../api/student'
import { getAllClasses } from '../api/class'

const router = useRouter()
const formRef = ref()
const classList = ref([])

const form = ref({
  name: '',
  studentNo: '',
  gender: '',
  age: null,
  classId: null,
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{8,12}$/, message: '学号应为8-12位数字', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  age: [
    { required: false },
    { type: 'number', min: 1, max: 150, message: '年龄必须在1-150之间', trigger: 'blur' }
  ],
  classId: [
    { required: true, message: '请输入班级ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '班级ID必须大于0', trigger: 'blur' }
  ],
  email: [
    { required: false },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: false },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await addStudent({
        name: form.value.name,
        studentNo: form.value.studentNo,
        gender: form.value.gender,
        age: form.value.age,
        classId: form.value.classId,
        email: form.value.email,
        phone: form.value.phone,
        password: form.value.password
      })

      ElMessage.success('注册成功！系统已自动创建登录账号，用户名为学号，请使用您设置的密码登录')
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } catch (error) {
      // 错误已在拦截器中处理
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

// 加载班级列表
const loadClasses = async () => {
  try {
    const res = await getAllClasses()
    if (res.code === 200) {
      classList.value = res.data
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
    ElMessage.error('加载班级列表失败')
  }
}

// 组件挂载时加载班级列表
onMounted(() => {
  loadClasses()
})
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.register-card {
  width: 450px;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
</style>
