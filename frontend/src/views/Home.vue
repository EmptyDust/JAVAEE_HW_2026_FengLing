<template>
  <el-container class="home-container">
    <el-header>
      <div class="header-content">
        <h2>学生信息管理系统</h2>
        <div class="user-info">
          <el-tag v-if="userStore.userType === 'admin'" type="danger">管理员</el-tag>
          <el-tag v-else-if="userStore.userType === 'teacher'" type="warning">教师</el-tag>
          <el-tag v-else-if="userStore.userType === 'student'" type="success">学生</el-tag>
          <span>欢迎，{{ userStore.username }}</span>
          <NotificationBell />
          <el-button @click="handleLogout" type="danger" size="small">退出登录</el-button>
        </div>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu :default-active="$route.path" router>
          <!-- 管理员和教师可见 -->
          <el-menu-item v-if="['admin', 'teacher'].includes(userStore.userType)" index="/home/students">
            <el-icon><User /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          <el-menu-item v-if="['admin', 'teacher'].includes(userStore.userType)" index="/home/classes">
            <el-icon><School /></el-icon>
            <span>班级管理</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.userType === 'admin'" index="/home/teachers">
            <el-icon><User /></el-icon>
            <span>教师管理</span>
          </el-menu-item>
          <el-menu-item v-if="['admin', 'teacher'].includes(userStore.userType)" index="/home/courses">
            <el-icon><Reading /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
          <el-menu-item v-if="['admin', 'teacher'].includes(userStore.userType)" index="/home/notification-management">
            <el-icon><Document /></el-icon>
            <span>通知管理</span>
          </el-menu-item>
          <el-menu-item v-if="['admin', 'teacher'].includes(userStore.userType)" index="/home/attachment-management">
            <el-icon><Folder /></el-icon>
            <span>附件管理</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.userType === 'admin'" index="/home/data-permission">
            <el-icon><Setting /></el-icon>
            <span>数据权限管理</span>
          </el-menu-item>

          <!-- 学生可见 -->
          <el-menu-item v-if="userStore.userType === 'student'" index="/home/course-selection">
            <el-icon><Reading /></el-icon>
            <span>选课中心</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.userType === 'student'" index="/home/my-courses">
            <el-icon><Star /></el-icon>
            <span>我的课程</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.userType === 'student'" index="/home/course-calendar">
            <el-icon><Calendar /></el-icon>
            <span>课程日历</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.userType === 'student'" index="/home/weekly-schedule">
            <el-icon><Grid /></el-icon>
            <span>每周课表</span>
          </el-menu-item>

          <!-- 所有用户可见 -->
          <el-menu-item index="/home/notifications">
            <el-icon><Bell /></el-icon>
            <span>我的通知</span>
          </el-menu-item>
          <el-menu-item index="/home/profile">
            <el-icon><Avatar /></el-icon>
            <span>{{ userStore.userType === 'student' ? '我的信息' : '个人信息' }}</span>
          </el-menu-item>
          <el-menu-item index="/home/change-password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Avatar, Lock, School, Reading, Star, Calendar, Grid, Bell, Document, Folder, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { logout } from '../api/auth'
import NotificationBell from '../components/NotificationBell.vue'
import websocketManager from '../utils/websocket'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 断开WebSocket连接
      websocketManager.disconnect()

      await logout()
      userStore.logout()
      router.push('/login')
      ElMessage.success('退出成功')
    } catch (error) {
      userStore.logout()
      router.push('/login')
    }
  }).catch(() => {})
}

// 初始化WebSocket连接
onMounted(() => {
  if (userStore.userId) {
    websocketManager.connect(userStore.userId)
  }
})

// 清理WebSocket连接
onUnmounted(() => {
  websocketManager.disconnect()
})
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.el-aside {
  background-color: #f5f5f5;
}

.el-main {
  background-color: #fff;
  padding: 20px;
}

h2 {
  margin: 0;
}
</style>
