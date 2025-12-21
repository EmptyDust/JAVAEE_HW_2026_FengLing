<template>
  <div class="my-courses">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="已选课程" name="enrolled">
        <div class="toolbar">
          <el-input
            v-model="searchForm.courseName"
            placeholder="课程名称/编号"
            style="width: 200px"
            clearable
            @keyup.enter="handleSearch"
          ></el-input>
          <el-select
            v-model="searchForm.semester"
            placeholder="开课学期"
            style="width: 150px"
            clearable
            @keyup.enter="handleSearch"
          >
            <el-option
              v-for="sem in semesterOptions"
              :key="sem.semester"
              :label="sem.semester"
              :value="sem.semester">
            </el-option>
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
          <el-table-column prop="scheduleInfo" label="上课安排" show-overflow-tooltip></el-table-column>
          <el-table-column prop="enrollmentTime" label="选课时间" width="180"></el-table-column>
          <el-table-column prop="score" label="成绩" width="100">
            <template #default="{ row }">
              <span v-if="row.score">{{ row.score }}</span>
              <el-tag v-else type="info">未录入</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="info" size="small" @click="handleViewDetail(row)">详情</el-button>
              <el-button type="warning" size="small" @click="handleViewAttachments(row)">附件</el-button>
              <el-button type="danger" size="small" @click="handleDrop(row)" :disabled="!canDrop(row)">
                退课
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
      </el-tab-pane>
    </el-tabs>

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
        <el-descriptions-item label="成绩">
          <span v-if="currentCourse.score">{{ currentCourse.score }}</span>
          <el-tag v-else type="info">未录入</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="选课时间" :span="2">
          {{ currentCourse.enrollmentTime }}
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
    </el-dialog>

    <!-- 课程附件对话框 -->
    <el-dialog v-model="attachmentDialogVisible" title="课程附件" width="800px">
      <!-- 附件搜索 -->
      <div style="margin-bottom: 15px; display: flex; gap: 10px; align-items: center">
        <el-input
          v-model="attachmentSearchKeyword"
          placeholder="搜索附件名称或内容"
          clearable
          style="width: 300px"
          @clear="handleClearAttachmentSearch"
          @keyup.enter="handleSearchAttachments"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearchAttachments">搜索</el-button>
        <el-button @click="handleClearAttachmentSearch">显示全部</el-button>
      </div>

      <el-table :data="attachments" border v-if="attachments.length > 0">
        <el-table-column prop="attachmentName" label="文件名" show-overflow-tooltip></el-table-column>
        <el-table-column prop="attachmentType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.attachmentType === 'document'" type="primary">文档</el-tag>
            <el-tag v-else-if="row.attachmentType === 'video'" type="success">视频</el-tag>
            <el-tag v-else-if="row.attachmentType === 'audio'" type="warning">音频</el-tag>
            <el-tag v-else-if="row.attachmentType === 'image'" type="">图片</el-tag>
            <el-tag v-else type="info">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载" width="80"></el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80"></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.attachmentType === 'video' || row.attachmentType === 'audio' || row.attachmentType === 'document' || row.attachmentType === 'image'"
              type="success"
              size="small"
              @click="handlePreview(row)">预览</el-button>
            <el-button type="primary" size="small" @click="handleDownload(row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无附件"></el-empty>
    </el-dialog>

    <!-- 媒体预览对话框 -->
    <el-dialog v-model="previewDialogVisible" :title="previewTitle" width="900px">
      <div style="text-align: center">
        <!-- 视频播放器 -->
        <video
          v-if="previewType === 'video'"
          :src="previewUrl"
          controls
          style="width: 100%; max-height: 500px"
        ></video>

        <!-- 音频播放器 -->
        <audio
          v-if="previewType === 'audio'"
          :src="previewUrl"
          controls
          style="width: 100%"
        ></audio>

        <!-- 图片预览 -->
        <img
          v-if="previewType === 'image'"
          :src="previewUrl"
          style="max-width: 100%; max-height: 600px"
          alt="图片预览"
        />

        <!-- Office文档预览（Word/PPT/Excel） -->
        <iframe
          v-if="previewType === 'document' && isOfficeDocument"
          :src="officeViewerUrl"
          style="width: 100%; height: 600px; border: none"
        ></iframe>

        <!-- PDF预览 -->
        <iframe
          v-if="previewType === 'document' && !isOfficeDocument"
          :src="previewUrl"
          style="width: 100%; height: 600px; border: none"
        ></iframe>

        <!-- 提示信息 -->
        <div v-if="previewType === 'document' && isOfficeDocument" style="margin-top: 10px; color: #909399; font-size: 12px">
          使用 Microsoft Office Online 预览，首次加载可能需要几秒钟
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getMyEnrollments,
  dropCourse,
  getCourseAttachments,
  recordDownload,
  recordView,
  getAvailableSemesters,
  advancedSearchAttachments
} from '../api/course'

// Tab
const activeTab = ref('enrolled')

// 搜索表单
const searchForm = reactive({
  courseName: '',
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

// 附件对话框
const attachmentDialogVisible = ref(false)
const attachments = ref([])
const attachmentSearchKeyword = ref('')

// 预览对话框
const previewDialogVisible = ref(false)
const previewType = ref('')
const previewUrl = ref('')
const previewTitle = ref('')
const isOfficeDocument = ref(false)
const officeViewerUrl = ref('')

// 学期选项列表
const semesterOptions = ref([])

// 获取已选课程列表
const fetchData = async () => {
  try {
    const params = {
      current: page.value,
      size: size.value,
      ...searchForm
    }
    const res = await getMyEnrollments(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  }
}

// Tab切换
const handleTabChange = () => {
  page.value = 1
  fetchData()
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchData()
}

// 刷新
const handleRefresh = () => {
  searchForm.courseName = ''
  searchForm.semester = ''
  page.value = 1
  fetchData()
}

// 查看课程详情
const handleViewDetail = (row) => {
  currentCourse.value = { ...row }
  detailDialogVisible.value = true
}

// 查看附件
const handleViewAttachments = async (row) => {
  currentCourse.value = row
  attachmentDialogVisible.value = true

  try {
    const res = await getCourseAttachments(row.courseId)
    if (res.code === 200) {
      attachments.value = res.data
    }
  } catch (error) {
    attachments.value = []
    ElMessage.error('获取附件列表失败')
  }
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

// 搜索附件
const handleSearchAttachments = async () => {
  if (!attachmentSearchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    // 使用高级搜索，直接在后端按课程ID过滤
    const res = await advancedSearchAttachments(
      attachmentSearchKeyword.value,
      currentCourse.value.courseId,
      null,
      0,
      100
    )
    if (res.code === 200) {
      attachments.value = res.data
      if (attachments.value.length === 0) {
        ElMessage.info('未找到相关附件')
      } else {
        ElMessage.success(`找到 ${attachments.value.length} 个相关附件`)
      }
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

// 清除搜索，显示全部附件
const handleClearAttachmentSearch = async () => {
  attachmentSearchKeyword.value = ''
  try {
    const res = await getCourseAttachments(currentCourse.value.courseId)
    if (res.code === 200) {
      attachments.value = res.data
    }
  } catch (error) {
    attachments.value = []
    ElMessage.error('获取附件列表失败')
  }
}

// 预览附件
const handlePreview = async (attachment) => {
  previewType.value = attachment.attachmentType
  previewTitle.value = attachment.attachmentName

  // 记录浏览次数
  try {
    await recordView(attachment.id)
  } catch (error) {
    console.error('记录浏览次数失败', error)
  }

  // 判断是否是Office文档
  const fileName = attachment.attachmentName.toLowerCase()
  const isOffice = fileName.endsWith('.docx') || fileName.endsWith('.pptx') ||
                   fileName.endsWith('.xlsx') || fileName.endsWith('.doc') ||
                   fileName.endsWith('.ppt') || fileName.endsWith('.xls')

  isOfficeDocument.value = isOffice

  if (isOffice) {
    // Office文档使用Office Web Viewer
    const fullFileUrl = `${window.location.origin}/api/file/stream/${attachment.fileId}`
    officeViewerUrl.value = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(fullFileUrl)}`
  } else {
    // PDF和其他文档使用流式传输接口
    previewUrl.value = `/api/file/stream/${attachment.fileId}`
  }

  previewDialogVisible.value = true
}

// 判断是否可以退课
const canDrop = (row) => {
  // 如果已经有成绩，不允许退课
  if (row.score) {
    return false
  }
  // 可以添加其他退课规则，比如选课时间超过一定期限不能退课
  return true
}

// 退课
const handleDrop = (row) => {
  ElMessageBox.confirm(
    `确定要退选《${row.courseName}》吗？退课后可以重新选择其他课程。`,
    '确认退课',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await dropCourse(row.enrollmentId)
      if (res.code === 200) {
        ElMessage.success('退课成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '退课失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '退课失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

onMounted(async () => {
  // 加载学期选项
  try {
    const res = await getAvailableSemesters()
    if (res.code === 200) {
      semesterOptions.value = res.data || []
    }
  } catch (error) {
    console.error('获取学期列表失败', error)
  }

  // 加载课程数据
  fetchData()
})
</script>

<style scoped>
.my-courses {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

pre {
  margin: 0;
  font-size: 14px;
}
</style>
