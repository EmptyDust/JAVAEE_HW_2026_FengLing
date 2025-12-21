<template>
  <div class="course-calendar">
    <el-card>
      <template #header>
        <div class="calendar-header">
          <h3>课程日历</h3>
          <div class="actions">
            <el-select v-model="selectedSemester" @change="handleSemesterChange" placeholder="选择学期" style="width: 150px; margin-right: 10px;">
              <el-option
                v-for="sem in semesters"
                :key="sem.semester"
                :label="sem.semester + (sem.isCurrent ? ' (当前)' : '')"
                :value="sem.semester"
              />
            </el-select>
            <el-button type="primary" @click="handleToday">今天</el-button>
          </div>
        </div>
      </template>

      <el-calendar v-model="currentDate">
        <template #date-cell="{ data }">
          <div class="calendar-day">
            <div class="day-number">{{ data.day.split('-')[2] }}</div>
            <div class="events" v-if="getEventsForDate(data.day).length > 0">
              <div
                v-for="event in getEventsForDate(data.day).slice(0, 3)"
                :key="event.id"
                class="event-item"
                :class="getEventClass(event)"
                @click="handleViewEvent(event)"
              >
                <el-tooltip :content="event.eventTitle" placement="top">
                  <div class="event-content">
                    <span class="event-time">{{ formatTime(event.startTime) }}</span>
                    <span class="event-title">{{ event.eventTitle }}</span>
                  </div>
                </el-tooltip>
              </div>
              <div v-if="getEventsForDate(data.day).length > 3" class="more-events">
                +{{ getEventsForDate(data.day).length - 3 }} 更多
              </div>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <!-- 事件详情对话框 -->
    <el-dialog v-model="eventDialogVisible" title="日历事件详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="课程名称">{{ currentEvent.courseName }}</el-descriptions-item>
        <el-descriptions-item label="事件标题">{{ currentEvent.eventTitle }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">
          <el-tag :type="getEventTypeTagType(currentEvent.eventType)">
            {{ getEventTypeText(currentEvent.eventType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentEvent.eventDate, currentEvent.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentEvent.eventDate, currentEvent.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ currentEvent.location || '无' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentEvent.status === 1" type="success">进行中</el-tag>
          <el-tag v-else-if="currentEvent.status === 0" type="info">未开始</el-tag>
          <el-tag v-else-if="currentEvent.status === 2" type="danger">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="事件描述">
          {{ currentEvent.eventDescription || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 事件列表对话框（点击"更多"时显示） -->
    <el-dialog v-model="eventsListDialogVisible" title="当天课程事件" width="800px">
      <el-table :data="selectedDateEvents" border>
        <el-table-column prop="courseName" label="课程" width="150"></el-table-column>
        <el-table-column prop="eventTitle" label="事件" width="150"></el-table-column>
        <el-table-column prop="eventType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getEventTypeTagType(row.eventType)">
              {{ getEventTypeText(row.eventType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="200">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点"></el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewEvent(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyEnrollments, getStudentCalendar, getAvailableSemesters, getStudentCalendarBySemester } from '../api/course'

// 当前选中的日期
const currentDate = ref(new Date())

// 学期列表和当前选中学期
const semesters = ref([])
const selectedSemester = ref('')

// 日历事件数据
const calendarEvents = ref([])

// 当前查看的事件
const currentEvent = ref({})
const eventDialogVisible = ref(false)

// 选中日期的事件列表
const selectedDateEvents = ref([])
const eventsListDialogVisible = ref(false)

// 获取学生已选课程列表
const fetchMyCourses = async () => {
  try {
    const res = await getMyEnrollments({ current: 1, size: 1000 })
    if (res.code === 200 && res.data.records.length > 0) {
      const courseIds = res.data.records.map(item => item.courseId)
      await fetchCalendarEvents(courseIds)
    }
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  }
}

// 获取日历事件
const fetchCalendarEvents = async (courseIds) => {
  try {
    if (!selectedSemester.value) return

    const res = await getStudentCalendarBySemester(courseIds, selectedSemester.value)
    if (res.code === 200) {
      calendarEvents.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取日历数据失败')
  }
}

// 获取指定日期的事件
const getEventsForDate = (dateStr) => {
  const date = dateStr.split('T')[0]
  return calendarEvents.value.filter(event => {
    const eventDate = event.eventDate  // 使用eventDate字段
    return eventDate === date
  }).sort((a, b) => {
    return (a.startTime || '').localeCompare(b.startTime || '')
  })
}

// 判断事件是否已过期
const isEventPast = (eventDate, endTime) => {
  const now = new Date()
  const eventDateTime = new Date(eventDate)

  if (endTime) {
    const [hours, minutes] = endTime.split(':')
    eventDateTime.setHours(parseInt(hours), parseInt(minutes))
  } else {
    eventDateTime.setHours(23, 59)
  }

  return eventDateTime < now
}

// 获取事件样式类
const getEventClass = (event) => {
  const isPast = isEventPast(event.eventDate, event.endTime)

  const typeClassMap = {
    'class': isPast ? 'event-class-past' : 'event-class',
    'exam': isPast ? 'event-exam-past' : 'event-exam',
    'homework': isPast ? 'event-homework-past' : 'event-homework',
    'activity': isPast ? 'event-activity-past' : 'event-activity',
    'other': isPast ? 'event-other-past' : 'event-other'
  }
  return typeClassMap[event.eventType] || 'event-other'
}

// 获取事件类型标签类型
const getEventTypeTagType = (eventType) => {
  const typeMap = {
    'class': 'primary',
    'exam': 'danger',
    'homework': 'warning',
    'activity': 'success',
    'other': 'info'
  }
  return typeMap[eventType] || 'info'
}

// 获取事件类型文本
const getEventTypeText = (eventType) => {
  const textMap = {
    'class': '上课',
    'exam': '考试',
    'homework': '作业',
    'activity': '活动',
    'other': '其他'
  }
  return textMap[eventType] || '其他'
}

// 查看事件详情
const handleViewEvent = (event) => {
  currentEvent.value = { ...event }
  eventDialogVisible.value = true
  eventsListDialogVisible.value = false
}

// 跳转到今天
const handleToday = () => {
  currentDate.value = new Date()
}

// 学期选择变化
const handleSemesterChange = () => {
  fetchMyCourses()
}

// 上个月（已废弃，但保留以免影响现有逻辑）
const handlePrevMonth = () => {
  const date = new Date(currentDate.value)
  date.setMonth(date.getMonth() - 1)
  currentDate.value = date
}

// 下个月（已废弃，但保留以免影响现有逻辑）
const handleNextMonth = () => {
  const date = new Date(currentDate.value)
  date.setMonth(date.getMonth() + 1)
  currentDate.value = date
}

// 获取月份开始日期
const getMonthStartDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}-01`
}

// 获取月份结束日期
const getMonthEndDate = (date) => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const lastDay = new Date(year, month, 0).getDate()
  const monthStr = String(month).padStart(2, '0')
  return `${year}-${monthStr}-${lastDay}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string' && time.includes(':')) {
    return time.substring(0, 5)  // 取HH:mm部分
  }
  return time
}

// 格式化日期时间
const formatDateTime = (date, time) => {
  if (!date) return '无'
  if (!time) return date
  return `${date} ${formatTime(time)}`
}

onMounted(async () => {
  try {
    // 获取学期列表
    const res = await getAvailableSemesters()
    if (res.code === 200) {
      semesters.value = res.data || []
      // 默认选择当前学期
      const currentSem = semesters.value.find(s => s.isCurrent)
      selectedSemester.value = currentSem ? currentSem.semester : (semesters.value[0]?.semester || '')

      // 设置日历显示日期为学期开始日期
      if (currentSem && currentSem.startDate) {
        currentDate.value = new Date(currentSem.startDate)
      }

      // 加载课程数据
      fetchMyCourses()
    }
  } catch (error) {
    ElMessage.error('获取学期列表失败')
  }
})
</script>

<style scoped>
.course-calendar {
  padding: 20px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-header h3 {
  margin: 0;
}

.actions {
  display: flex;
  gap: 10px;
}

.calendar-day {
  height: 100%;
  padding: 5px;
}

.day-number {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
}

.events {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.event-item {
  padding: 2px 5px;
  border-radius: 3px;
  font-size: 12px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.2s;
}

.event-item:hover {
  opacity: 0.8;
  transform: translateX(2px);
}

.event-content {
  display: flex;
  gap: 5px;
  align-items: center;
}

.event-time {
  font-weight: bold;
  flex-shrink: 0;
}

.event-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-class {
  background-color: #409eff;
  color: white;
}

.event-class-past {
  background-color: #a0cfff;
  color: #606266;
}

.event-exam {
  background-color: #f56c6c;
  color: white;
}

.event-exam-past {
  background-color: #f5b3b3;
  color: #606266;
}

.event-homework {
  background-color: #e6a23c;
  color: white;
}

.event-homework-past {
  background-color: #f3d19e;
  color: #606266;
}

.event-activity {
  background-color: #67c23a;
  color: white;
}

.event-activity-past {
  background-color: #b3e19d;
  color: #606266;
}

.event-other {
  background-color: #909399;
  color: white;
}

.event-other-past {
  background-color: #c8c9cc;
  color: #606266;
}

.more-events {
  font-size: 12px;
  color: #909399;
  text-align: center;
  padding: 2px;
  cursor: pointer;
}

.more-events:hover {
  color: #409eff;
}

:deep(.el-calendar-table .el-calendar-day) {
  height: 120px;
  padding: 0;
}

:deep(.el-calendar__body) {
  padding: 12px 20px;
}
</style>
