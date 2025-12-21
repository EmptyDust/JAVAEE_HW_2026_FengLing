<template>
  <div class="course-selection">
    <div class="toolbar">
      <el-input
        v-model="searchForm.courseName"
        placeholder="课程名称/编号"
        style="width: 200px"
        clearable
        @keyup.enter="handleSearch"
      ></el-input>
      <el-select
        v-model="searchForm.courseType"
        placeholder="课程类型"
        style="width: 150px"
        clearable
        @keyup.enter="handleSearch"
      >
        <el-option label="必修" value="必修"></el-option>
        <el-option label="选修" value="选修"></el-option>
        <el-option label="公选" value="公选"></el-option>
      </el-select>
      <el-select
        v-model="searchForm.semester"
        placeholder="开课学期"
        style="width: 150px"
        clearable
        @keyup.enter="handleSearch"
      >
        <el-option label="2024-2025-1" value="2024-2025-1"></el-option>
        <el-option label="2024-2025-2" value="2024-2025-2"></el-option>
        <el-option label="2025-2026-1" value="2025-2026-1"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button type="success" @click="handleRefresh">刷新</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 20px">
      <el-table-column prop="courseCode" label="课程编号" width="120"></el-table-column>
      <el-table-column prop="courseName" label="课程名称" width="200"></el-table-column>
      <el-table-column prop="courseType" label="类型" width="80"></el-table-column>
      <el-table-column prop="credit" label="学分" width="70"></el-table-column>
      <el-table-column prop="hours" label="学时" width="70"></el-table-column>
      <el-table-column prop="teacherName" label="教师"></el-table-column>
      <el-table-column prop="semester" label="学期" width="120"></el-table-column>
      <el-table-column label="选课人数" width="100">
        <template #default="{ row }">
          <span :class="{ 'text-danger': row.enrolledStudents >= row.maxStudents }">
            {{ row.enrolledStudents || 0 }}/{{ row.maxStudents }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="scheduleInfo" label="上课安排" show-overflow-tooltip></el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">可选</el-tag>
          <el-tag v-else-if="row.status === 0" type="danger">停用</el-tag>
          <el-tag v-else-if="row.status === 2" type="warning">已满</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!row.enrolled"
            type="primary"
            size="small"
            @click="handleEnroll(row)"
            :disabled="row.status !== 1 || row.enrolledStudents >= row.maxStudents"
          >
            选课
          </el-button>
          <el-tag v-else type="success">已选</el-tag>
          <el-button type="info" size="small" @click="handleViewDetail(row)">详情</el-button>
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

    <!-- 课程详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="课程详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="课程编号">{{ currentCourse.courseCode }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ currentCourse.courseName }}</el-descriptions-item>
        <el-descriptions-item label="课程类型">{{ currentCourse.courseType }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ currentCourse.credit }}</el-descriptions-item>
        <el-descriptions-item label="学时">{{ currentCourse.hours }}</el-descriptions-item>
        <el-descriptions-item label="教师">{{ currentCourse.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ currentCourse.semester }}</el-descriptions-item>
        <el-descriptions-item label="选课人数">
          {{ currentCourse.enrolledStudents || 0 }}/{{ currentCourse.maxStudents }}
        </el-descriptions-item>
        <el-descriptions-item label="上课安排" :span="2">
          {{ currentCourse.scheduleInfo }}
        </el-descriptions-item>
        <el-descriptions-item label="课程简介" :span="2">
          {{ currentCourse.courseDescription || '暂无简介' }}
        </el-descriptions-item>
        <el-descriptions-item label="课程大纲" :span="2">
          <pre style="white-space: pre-wrap; font-family: inherit;">{{ currentCourse.courseOutline || '暂无大纲' }}</pre>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 课程附件列表 -->
      <div v-if="courseAttachments.length > 0" style="margin-top: 20px">
        <h4>课程附件</h4>
        <el-table :data="courseAttachments" border size="small">
          <el-table-column prop="attachmentName" label="文件名" show-overflow-tooltip></el-table-column>
          <el-table-column prop="attachmentType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.attachmentType === 'document'" type="primary">文档</el-tag>
              <el-tag v-else-if="row.attachmentType === 'video'" type="success">视频</el-tag>
              <el-tag v-else-if="row.attachmentType === 'audio'" type="warning">音频</el-tag>
              <el-tag v-else type="info">其他</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleDownload(row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="!currentCourse.enrolled"
          type="primary"
          @click="handleEnrollFromDetail"
          :disabled="currentCourse.status !== 1 || currentCourse.enrolledStudents >= currentCourse.maxStudents"
        >
          选课
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCourseList,
  enrollCourse,
  checkEnrollment,
  getCourseAttachments,
  recordDownload,
  getMyEnrollments
} from '../api/course'

// 搜索表单
const searchForm = reactive({
  courseName: '',
  courseType: '',
  semester: ''
})

// 分页
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

// 课程详情对话框
const detailDialogVisible = ref(false)
const currentCourse = ref({})
const courseAttachments = ref([])

// 获取课程列表
const fetchData = async () => {
  try {
    const params = {
      current: page.value,
      size: size.value,
      status: 1, // 只显示启用的课程
      ...searchForm
    }
    const res = await getCourseList(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
      // 选课状态已经由后端返回，无需再次查询
    }
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchData()
}

// 刷新
const handleRefresh = () => {
  searchForm.courseName = ''
  searchForm.courseType = ''
  searchForm.semester = ''
  page.value = 1
  fetchData()
}

// 选课
const handleEnroll = async (row) => {
  // 先检查时间冲突
  const hasConflict = await checkTimeConflict(row)
  if (hasConflict) {
    return // 检测到冲突，已提示用户，不继续选课
  }

  ElMessageBox.confirm(
    `确定要选修《${row.courseName}》吗？`,
    '确认选课',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      const res = await enrollCourse(row.id)
      if (res.code === 200) {
        ElMessage.success('选课成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '选课失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '选课失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 检查时间冲突
const checkTimeConflict = async (newCourse) => {
  try {
    // 获取已选课程（只获取相同学期的课程）
    const params = {
      current: 1,
      size: 1000,
      semester: newCourse.semester  // 只检查同学期的课程
    }
    const res = await getMyEnrollments(params)
    if (res.code !== 200 || !res.data.records) {
      return false
    }

    const enrolledCourses = res.data.records
    const conflicts = []

    // 解析新课程的时间安排
    const newSchedules = parseScheduleInfo(newCourse.scheduleInfo)

    // 检查每门已选课程
    for (const enrolled of enrolledCourses) {
      const enrolledSchedules = parseScheduleInfo(enrolled.scheduleInfo)

      // 检查时间冲突
      for (const newSch of newSchedules) {
        for (const enrolledSch of enrolledSchedules) {
          if (isTimeConflict(newSch, enrolledSch)) {
            conflicts.push({
              courseName: enrolled.courseName,
              time: `${enrolledSch.dayLabel} 第${enrolledSch.startPeriod}-${enrolledSch.endPeriod}节`
            })
          }
        }
      }
    }

    // 如果有冲突，提示用户
    if (conflicts.length > 0) {
      const conflictMsg = conflicts.map(c => `《${c.courseName}》 ${c.time}`).join('\n')
      ElMessageBox.alert(
        `该课程与以下已选课程时间冲突:\n\n${conflictMsg}\n\n请先退掉冲突的课程再选择`,
        '时间冲突',
        {
          type: 'warning',
          confirmButtonText: '知道了'
        }
      )
      return true
    }

    return false
  } catch (error) {
    console.error('检查时间冲突失败:', error)
    return false
  }
}

// 解析课程时间安排
// 格式: "周一1-2节/A101，周三3-4节/A101"
const parseScheduleInfo = (scheduleInfo) => {
  if (!scheduleInfo) return []

  const dayMap = { '一': 1, '二': 2, '三': 3, '四': 4, '五': 5, '六': 6, '日': 7 }
  const dayLabelMap = { '一': '周一', '二': '周二', '三': '周三', '四': '周四', '五': '周五', '六': '周六', '日': '周日' }
  const schedules = []

  const parts = scheduleInfo.split('，')
  for (const part of parts) {
    const match = part.match(/周([一二三四五六日])(\d+)-(\d+)节/)
    if (match) {
      schedules.push({
        weekDay: dayMap[match[1]],
        dayLabel: dayLabelMap[match[1]],
        startPeriod: parseInt(match[2]),
        endPeriod: parseInt(match[3])
      })
    }
  }

  return schedules
}

// 判断两个时间段是否冲突
const isTimeConflict = (schedule1, schedule2) => {
  // 不同的星期不冲突
  if (schedule1.weekDay !== schedule2.weekDay) {
    return false
  }

  // 检查节次是否重叠
  // 两个时间段重叠的条件:
  // (start1 <= end2) && (end1 >= start2)
  return (
    schedule1.startPeriod <= schedule2.endPeriod &&
    schedule1.endPeriod >= schedule2.startPeriod
  )
}

// 查看课程详情
const handleViewDetail = async (row) => {
  currentCourse.value = { ...row }
  detailDialogVisible.value = true

  // 获取课程附件
  try {
    const res = await getCourseAttachments(row.id)
    if (res.code === 200) {
      courseAttachments.value = res.data
    }
  } catch (error) {
    courseAttachments.value = []
  }
}

// 从详情页选课
const handleEnrollFromDetail = async () => {
  await handleEnroll(currentCourse.value)
  detailDialogVisible.value = false
}

// 下载附件
const handleDownload = async (row) => {
  try {
    await recordDownload(row.id)
    const downloadUrl = `/api/file/download/${row.fileId}`
    window.open(downloadUrl, '_blank')
    ElMessage.success('开始下载')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.course-selection {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

pre {
  margin: 0;
  font-size: 14px;
}
</style>
