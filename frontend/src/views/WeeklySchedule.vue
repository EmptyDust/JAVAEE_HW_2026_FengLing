<template>
  <div class="weekly-schedule">
    <el-card>
      <template #header>
        <div class="header">
          <h3>每周课表</h3>
          <div class="actions">
            <el-select v-model="selectedSemester" @change="handleSemesterChange" placeholder="选择学期" style="width: 150px; margin-right: 10px;">
              <el-option
                v-for="sem in semesters"
                :key="sem.semester"
                :label="sem.semester + (sem.isCurrent ? ' (当前)' : '')"
                :value="sem.semester"
              />
            </el-select>
            <el-button type="primary" icon="Refresh" @click="fetchSchedule">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 课表网格 -->
      <div class="schedule-grid">
        <!-- 表头：星期 -->
        <div class="schedule-header">
          <div class="time-column">时间</div>
          <div class="day-column" v-for="day in weekDays" :key="day.value">
            {{ day.label }}
          </div>
        </div>

        <!-- 时间段行 -->
        <div class="schedule-body">
          <div class="time-row" v-for="timeSlot in timeSlots" :key="timeSlot.period">
            <!-- 时间列 -->
            <div class="time-cell">
              <div class="period">第{{ timeSlot.period }}节</div>
              <div class="time-range">{{ timeSlot.start }} - {{ timeSlot.end }}</div>
            </div>

            <!-- 每天的课程单元格 -->
            <div
              class="course-cell"
              v-for="day in weekDays"
              :key="`${day.value}-${timeSlot.period}`"
            >
              <div
                v-for="course in getCourseForSlot(day.value, timeSlot.start, timeSlot.end)"
                :key="course.id"
                class="course-item"
                :style="{ backgroundColor: course.color }"
                @click="handleViewCourse(course)"
              >
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-location">{{ course.location }}</div>
                <div class="course-teacher">{{ course.teacherName }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 课程冲突提示 -->
      <el-alert
        v-if="conflicts.length > 0"
        type="warning"
        :closable="false"
        style="margin-top: 20px"
      >
        <template #title>
          <strong>检测到课程时间冲突：</strong>
        </template>
        <ul>
          <li v-for="(conflict, index) in conflicts" :key="index">
            {{ conflict }}
          </li>
        </ul>
      </el-alert>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog v-model="courseDialogVisible" title="课程详情" width="600px">
      <el-descriptions :column="1" border v-if="currentCourse">
        <el-descriptions-item label="课程名称">{{ currentCourse.courseName }}</el-descriptions-item>
        <el-descriptions-item label="课程代码">{{ currentCourse.courseCode }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ currentCourse.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">
          {{ formatCourseTime(currentCourse) }}
        </el-descriptions-item>
        <el-descriptions-item label="上课地点">{{ currentCourse.location }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ currentCourse.credit }}</el-descriptions-item>
        <el-descriptions-item label="学时">{{ currentCourse.hours }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyEnrollments, getAvailableSemesters } from '../api/course'

// 星期定义
const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

// 时间段定义（一天12节课，从早8点到晚8点）
const timeSlots = [
  { period: 1, start: '08:00', end: '08:50' },
  { period: 2, start: '08:50', end: '09:40' },
  { period: 3, start: '10:00', end: '10:50' },
  { period: 4, start: '10:50', end: '11:40' },
  { period: 5, start: '14:00', end: '14:50' },
  { period: 6, start: '14:50', end: '15:40' },
  { period: 7, start: '16:00', end: '16:50' },
  { period: 8, start: '16:50', end: '17:40' },
  { period: 9, start: '19:00', end: '19:50' },
  { period: 10, start: '19:50', end: '20:40' }
]

// 学期列表和当前选中学期
const semesters = ref([])
const selectedSemester = ref('')

// 课程数据
const courses = ref([])
const conflicts = ref([])

// 当前查看的课程
const currentCourse = ref(null)
const courseDialogVisible = ref(false)

// 获取课表数据
const fetchSchedule = async () => {
  try {
    // 构建查询参数,包含学期过滤
    const params = { current: 1, size: 1000 }
    if (selectedSemester.value) {
      params.semester = selectedSemester.value
    }

    const res = await getMyEnrollments(params)
    if (res.code === 200 && res.data.records) {
      courses.value = parseCoursesToSchedule(res.data.records)
      checkConflicts()
    }
  } catch (error) {
    ElMessage.error('获取课表数据失败')
  }
}

// 解析课程信息为课表格式
const parseCoursesToSchedule = (enrollments) => {
  const scheduleList = []

  enrollments.forEach(enrollment => {
    const { courseCode, courseName, teacherName, credit, hours, scheduleInfo } = enrollment

    // 解析 scheduleInfo: "周一1-2节/A101，周三3-4节/A101"
    if (!scheduleInfo) return

    const scheduleParts = scheduleInfo.split('，')
    scheduleParts.forEach(part => {
      const match = part.match(/周([一二三四五六日])(\d+)-(\d+)节\/(.+)/)
      if (match) {
        const dayMap = { '一': 1, '二': 2, '三': 3, '四': 4, '五': 5, '六': 6, '日': 7 }
        const weekDay = dayMap[match[1]]
        const startPeriod = parseInt(match[2])
        const endPeriod = parseInt(match[3])
        const location = match[4]

        // 为每个时间段创建一个课程项
        scheduleList.push({
          id: `${enrollment.enrollmentId}-${weekDay}-${startPeriod}`,
          enrollmentId: enrollment.enrollmentId,
          courseCode,
          courseName,
          teacherName,
          credit,
          hours,
          weekDay,
          startPeriod,
          endPeriod,
          location,
          color: getColorByType(enrollment.courseType),
          // 计算开始和结束时间
          startTime: timeSlots[startPeriod - 1]?.start || '08:00',
          endTime: timeSlots[endPeriod - 1]?.end || '09:40'
        })
      }
    })
  })

  return scheduleList
}

// 根据课程类型获取颜色
const getColorByType = (courseType) => {
  const colorMap = {
    '必修': '#409EFF',
    '选修': '#67C23A',
    '限选': '#E6A23C',
    '公选': '#909399'
  }
  return colorMap[courseType] || '#909399'
}

// 获取指定时间段的课程
const getCourseForSlot = (weekDay, startTime, endTime) => {
  return courses.value.filter(course => {
    if (course.weekDay !== weekDay) return false

    // 检查时间是否重叠
    return (
      (course.startTime <= startTime && course.endTime > startTime) ||
      (course.startTime < endTime && course.endTime >= endTime) ||
      (course.startTime >= startTime && course.endTime <= endTime)
    )
  })
}

// 检查课程冲突
const checkConflicts = () => {
  const conflictList = []
  const courseMap = {}

  courses.value.forEach(course => {
    const key = `${course.weekDay}-${course.startPeriod}-${course.endPeriod}`
    if (!courseMap[key]) {
      courseMap[key] = []
    }
    courseMap[key].push(course)
  })

  // 检查每个时间段是否有多门课程
  Object.keys(courseMap).forEach(key => {
    const coursesInSlot = courseMap[key]
    if (coursesInSlot.length > 1) {
      const [weekDay, start, end] = key.split('-')
      const dayLabel = weekDays.find(d => d.value === parseInt(weekDay))?.label
      const courseNames = coursesInSlot.map(c => c.courseName).join('、')
      conflictList.push(
        `${dayLabel} 第${start}-${end}节：${courseNames}`
      )
    }
  })

  conflicts.value = conflictList
}

// 查看课程详情
const handleViewCourse = (course) => {
  currentCourse.value = course
  courseDialogVisible.value = true
}

// 格式化课程时间
const formatCourseTime = (course) => {
  if (!course) return ''
  const day = weekDays.find(d => d.value === course.weekDay)?.label
  return `${day} 第${course.startPeriod}-${course.endPeriod}节 (${course.startTime}-${course.endTime})`
}

// 学期选择变化
const handleSemesterChange = () => {
  fetchSchedule()
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
      // 加载课表数据
      fetchSchedule()
    }
  } catch (error) {
    ElMessage.error('获取学期列表失败')
  }
})
</script>

<style scoped>
.weekly-schedule {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h3 {
  margin: 0;
}

.schedule-grid {
  width: 100%;
  overflow-x: auto;
}

.schedule-header {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr);
  border-bottom: 2px solid #409EFF;
  background-color: #f5f7fa;
}

.time-column,
.day-column {
  padding: 15px;
  text-align: center;
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}

.schedule-body {
  min-width: 1000px;
}

.time-row {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr);
  border-bottom: 1px solid #EBEEF5;
}

.time-cell {
  padding: 10px;
  text-align: center;
  background-color: #fafafa;
  border-right: 1px solid #EBEEF5;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 70px;
}

.period {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
}

.time-range {
  font-size: 12px;
  color: #909399;
}

.course-cell {
  padding: 5px;
  border-right: 1px solid #EBEEF5;
  min-height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.course-item {
  width: 100%;
  padding: 8px;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.course-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.course-name {
  font-weight: bold;
  font-size: 13px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-location {
  font-size: 11px;
  opacity: 0.9;
  margin-bottom: 2px;
}

.course-teacher {
  font-size: 11px;
  opacity: 0.85;
}

/* 课程冲突高亮 */
.course-cell:has(.course-item:nth-child(2)) {
  background-color: #fef0f0;
}
</style>
