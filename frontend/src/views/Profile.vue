<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="title">{{ userStore.userType === 'student' ? '我的信息' : '个人信息' }}</span>
          <el-button v-if="!isEditing" type="primary" @click="startEdit">编辑信息</el-button>
          <div v-else>
            <el-button type="success" @click="saveEdit">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </div>
        </div>
      </template>

      <el-form :model="userInfo" label-width="100px" v-loading="loading">
        <!-- 头像上传（仅学生） -->
        <el-form-item v-if="userStore.userType === 'student'" label="头像">
          <div class="avatar-container">
            <el-avatar :size="100" :src="avatarUrl" class="avatar">
              <el-icon :size="50"><User /></el-icon>
            </el-avatar>
            <el-upload
              :http-request="handleCustomUpload"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <el-button type="primary" size="small" style="margin-left: 20px">更换头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 学生特有字段 -->
        <template v-if="userStore.userType === 'student'">
          <el-form-item label="姓名">
            <el-input v-model="userInfo.name" disabled></el-input>
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model="userInfo.studentNo" disabled></el-input>
          </el-form-item>
          <el-form-item label="性别">
            <el-input v-model="userInfo.gender" disabled></el-input>
          </el-form-item>
          <el-form-item label="年龄">
            <el-input v-model="userInfo.age" disabled></el-input>
          </el-form-item>
          <el-form-item label="班级">
            <el-input v-model="userInfo.className" disabled></el-input>
          </el-form-item>
        </template>

        <!-- 教师特有字段 -->
        <template v-if="userStore.userType === 'teacher'">
          <el-form-item label="姓名">
            <el-input v-model="userInfo.teacherName" disabled></el-input>
          </el-form-item>
          <el-form-item label="教师工号">
            <el-input v-model="userInfo.teacherNo" disabled></el-input>
          </el-form-item>
          <el-form-item label="部门">
            <el-input v-model="userInfo.department" disabled></el-input>
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="userInfo.title" disabled></el-input>
          </el-form-item>
        </template>

        <!-- 所有用户共有字段 -->
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userInfo.email" :disabled="!isEditing" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="userInfo.phone" :disabled="!isEditing" placeholder="请输入联系电话"></el-input>
        </el-form-item>

        <!-- 学生特有的地址字段 -->
        <el-form-item v-if="userStore.userType === 'student'" label="家庭住址">
          <el-input
            v-model="userInfo.address"
            :disabled="!isEditing"
            type="textarea"
            :rows="3"
            placeholder="请输入家庭住址"
          ></el-input>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getMyProfile as getStudentProfile, updateMyProfile as updateStudent, uploadAvatar } from '../api/student'
import { getMyProfile as getTeacherProfile, updateMyProfile as updateTeacher } from '../api/teacher'
import { getUserProfile, updateUserProfile } from '../api/auth'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const loading = ref(false)
const isEditing = ref(false)
const userInfo = ref({
  id: '',
  username: '',
  email: '',
  phone: '',
  // 学生特有字段
  name: '',
  studentNo: '',
  gender: '',
  age: '',
  classId: '',
  className: '',
  address: '',
  avatarFileId: null,
  // 教师特有字段
  teacherNo: '',
  teacherName: '',
  department: '',
  title: ''
})

const originalInfo = ref({})

// 头像相关
const avatarUrl = computed(() => {
  if (userInfo.value.avatarFileId) {
    return `/api/file/stream/${userInfo.value.avatarFileId}`
  }
  return ''
})

// 自定义头像上传处理
const handleCustomUpload = async (options) => {
  try {
    const response = await uploadAvatar(options.file, userInfo.value.id)
    if (response.code === 200) {
      ElMessage.success('头像上传成功')
      // 重新加载个人信息以获取最新的头像
      await loadProfile()
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
}

const loadProfile = async () => {
  loading.value = true
  try {
    if (userStore.userType === 'student') {
      // 学生加载自己的学生信息
      const res = await getStudentProfile()
      userInfo.value = {
        ...res.data,
        username: userStore.username
      }
    } else if (userStore.userType === 'teacher') {
      // 教师加载自己的教师信息
      const res = await getTeacherProfile()
      userInfo.value = {
        ...res.data,
        username: userStore.username
      }
    } else {
      // 管理员加载用户信息
      const res = await getUserProfile()
      userInfo.value = res.data
    }
    originalInfo.value = { ...userInfo.value }
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  userInfo.value = { ...originalInfo.value }
}

const saveEdit = async () => {
  loading.value = true
  try {
    if (userStore.userType === 'student') {
      // 学生更新信息
      await updateStudent({
        id: userInfo.value.id,
        phone: userInfo.value.phone,
        address: userInfo.value.address
      })
    } else if (userStore.userType === 'teacher') {
      // 教师更新信息
      await updateTeacher({
        id: userInfo.value.id,
        phone: userInfo.value.phone,
        email: userInfo.value.email
      })
    } else {
      // 管理员更新信息
      await updateUserProfile({
        email: userInfo.value.email,
        phone: userInfo.value.phone
      })
    }
    ElMessage.success('信息更新成功')
    isEditing.value = false
    originalInfo.value = { ...userInfo.value }
  } catch (error) {
    ElMessage.error('更新信息失败')
  } finally {
    loading.value = false
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.profile-card {
  width: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.avatar-container {
  display: flex;
  align-items: center;
}

.avatar {
  border: 2px solid #dcdfe6;
}
</style>
