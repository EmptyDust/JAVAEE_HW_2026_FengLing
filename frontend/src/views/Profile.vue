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
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ studentId: userInfo.id }"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
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
import { getMyProfile, updateStudent, uploadAvatar } from '../api/student'
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
  avatarFileId: null
})

const originalInfo = ref({})

// 头像相关
const uploadUrl = '/api/student/upload-avatar'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

const avatarUrl = computed(() => {
  if (userInfo.value.avatarFileId) {
    return `/api/file/stream/${userInfo.value.avatarFileId}`
  }
  return ''
})

const loadProfile = async () => {
  loading.value = true
  try {
    if (userStore.userType === 'student') {
      // 学生加载自己的学生信息
      const res = await getMyProfile()
      userInfo.value = {
        ...res.data,
        username: userStore.username
      }
    } else {
      // 管理员和教师加载用户信息
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
    } else {
      // 管理员和教师更新信息
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

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('头像上传成功')
    // 更新头像文件ID
    userInfo.value.avatarFileId = response.data.avatarFileId
    originalInfo.value.avatarFileId = response.data.avatarFileId
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 头像上传失败
const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
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
