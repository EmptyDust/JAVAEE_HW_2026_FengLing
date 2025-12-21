<template>
  <div class="notification-list">
    <div class="toolbar">
      <el-radio-group v-model="filterType" @change="handleFilterChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="unread">未读</el-radio-button>
        <el-radio-button label="read">已读</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="handleMarkAllRead" :disabled="unreadCount === 0">
        全部已读
      </el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 20px">
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isRead === 0" type="danger" size="small">未读</el-tag>
          <el-tag v-else type="info" size="small">已读</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getNotificationTypeTag(row.notificationType)" size="small">
            {{ getNotificationTypeText(row.notificationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="优先级" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.priority === 2" type="danger" size="small">紧急</el-tag>
          <el-tag v-else-if="row.priority === 1" type="warning" size="small">重要</el-tag>
          <el-tag v-else type="info" size="small">普通</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" width="200"></el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip></el-table-column>
      <el-table-column prop="receiveTime" label="接收时间" width="180"></el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
          <el-button
            v-if="row.isRead === 0"
            type="success"
            size="small"
            @click="handleMarkRead(row)"
          >
            已读
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchData"
      @current-change="fetchData"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 通知详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="通知详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="类型">
          <el-tag :type="getNotificationTypeTag(currentNotification.notificationType)" size="small">
            {{ getNotificationTypeText(currentNotification.notificationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag v-if="currentNotification.priority === 2" type="danger" size="small">紧急</el-tag>
          <el-tag v-else-if="currentNotification.priority === 1" type="warning" size="small">重要</el-tag>
          <el-tag v-else type="info" size="small">普通</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentNotification.title }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap;">{{ currentNotification.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="接收时间">{{ currentNotification.receiveTime }}</el-descriptions-item>
        <el-descriptions-item label="阅读时间">
          {{ currentNotification.readTime || '未读' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyNotifications, markAsRead, markAllAsRead, getUnreadCount } from '../api/notification'

const filterType = ref('all')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const unreadCount = ref(0)

const detailDialogVisible = ref(false)
const currentNotification = ref({})

// 获取通知列表
const fetchData = async () => {
  try {
    const params = {
      current: page.value,
      size: size.value
    }

    if (filterType.value === 'unread') {
      params.isRead = 0
    } else if (filterType.value === 'read') {
      params.isRead = 1
    }

    const res = await getMyNotifications(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取通知列表失败')
  }
}

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

// 筛选类型改变
const handleFilterChange = () => {
  page.value = 1
  fetchData()
}

// 查看详情
const handleView = async (row) => {
  currentNotification.value = { ...row }
  detailDialogVisible.value = true

  // 如果未读，标记为已读
  if (row.isRead === 0) {
    try {
      await markAsRead(row.id)
      row.isRead = 1
      row.readTime = new Date().toLocaleString()
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

// 标记为已读
const handleMarkRead = async (row) => {
  try {
    await markAsRead(row.id)
    ElMessage.success('已标记为已读')
    row.isRead = 1
    row.readTime = new Date().toLocaleString()
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    fetchData()
    fetchUnreadCount()
  } catch (error) {
    ElMessage.error('操作失败')
  }
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

onMounted(() => {
  fetchData()
  fetchUnreadCount()
})
</script>

<style scoped>
.notification-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
