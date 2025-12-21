<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <el-button circle @click="handleClick">
        <el-icon :size="20">
          <Bell />
        </el-icon>
      </el-button>
    </el-badge>

    <!-- 通知弹出框 -->
    <el-drawer
      v-model="drawerVisible"
      title="我的通知"
      direction="rtl"
      size="400px"
    >
      <div class="notification-drawer">
        <div class="drawer-header">
          <el-button type="primary" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">
            全部已读
          </el-button>
          <el-button size="small" @click="handleViewAll">
            查看全部
          </el-button>
        </div>

        <el-divider />

        <div class="notification-list" v-if="notifications.length > 0">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: item.isRead === 0 }"
            @click="handleItemClick(item)"
          >
            <div class="item-header">
              <el-tag
                :type="getNotificationTypeTag(item.notificationType)"
                size="small"
              >
                {{ getNotificationTypeText(item.notificationType) }}
              </el-tag>
              <span class="item-time">{{ formatTime(item.receiveTime) }}</span>
            </div>
            <div class="item-title">{{ item.title }}</div>
            <div class="item-content">{{ item.content }}</div>
          </div>
        </div>

        <el-empty v-else description="暂无通知" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyNotifications, markAsRead, markAllAsRead, getUnreadCount } from '../api/notification'
import websocketManager from '../utils/websocket'

const router = useRouter()

const drawerVisible = ref(false)
const unreadCount = ref(0)
const notifications = ref([])

// 获取未读数量
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data
    }
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

// 获取通知列表
const fetchNotifications = async () => {
  try {
    const res = await getMyNotifications({ current: 1, size: 10 })
    if (res.code === 200) {
      notifications.value = res.data.records
    }
  } catch (error) {
    console.error('获取通知列表失败', error)
  }
}

// 点击通知图标
const handleClick = () => {
  drawerVisible.value = true
  fetchNotifications()
}

// 点击通知项
const handleItemClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    notifications.value.forEach(item => {
      item.isRead = 1
    })
    unreadCount.value = 0
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 查看全部
const handleViewAll = () => {
  drawerVisible.value = false
  router.push('/home/notifications')
}

// 获取通知类型标签
const getNotificationTypeTag = (type) => {
  const typeMap = {
    announcement: 'primary',
    homework: 'warning',
    exam: 'danger',
    cancel: 'info'
  }
  return typeMap[type] || 'info'
}

// 获取通知类型文本
const getNotificationTypeText = (type) => {
  const typeMap = {
    announcement: '公告',
    homework: '作业',
    exam: '考试',
    cancel: '取消'
  }
  return typeMap[type] || '通知'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  } else if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前'
  } else {
    return date.toLocaleDateString()
  }
}

// WebSocket消息处理器
const handleWebSocketMessage = (notification) => {
  console.log('收到WebSocket通知:', notification)
  // 刷新未读数量
  fetchUnreadCount()
  // 如果抽屉打开，刷新通知列表
  if (drawerVisible.value) {
    fetchNotifications()
  }
}

onMounted(() => {
  // 获取未读数量
  fetchUnreadCount()

  // 注册WebSocket消息处理器
  websocketManager.onMessage(handleWebSocketMessage)

  // 定期刷新未读数量（每30秒）
  const interval = setInterval(() => {
    fetchUnreadCount()
  }, 30000)

  // 保存定时器ID以便清理
  onUnmounted(() => {
    clearInterval(interval)
    websocketManager.offMessage(handleWebSocketMessage)
  })
})
</script>

<style scoped>
.notification-bell {
  display: inline-block;
}

.notification-drawer {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.drawer-header {
  display: flex;
  gap: 10px;
  justify-content: space-between;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
}

.notification-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-time {
  font-size: 12px;
  color: #909399;
}

.item-title {
  font-weight: bold;
  margin-bottom: 5px;
  color: #303133;
}

.item-content {
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
