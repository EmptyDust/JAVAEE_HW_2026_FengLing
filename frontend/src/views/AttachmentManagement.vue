<template>
  <div class="attachment-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3>附件管理</h3>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon" style="background: #409EFF">
                <span style="font-size: 24px">📁</span>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.totalCount }}</div>
                <div class="stat-label">附件总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon" style="background: #67C23A">
                <span style="font-size: 24px">💾</span>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatFileSize(statistics.totalSize) }}</div>
                <div class="stat-label">总大小</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon" style="background: #E6A23C">
                <span style="font-size: 24px">📥</span>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.totalDownloads }}</div>
                <div class="stat-label">总下载次数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon" style="background: #F56C6C">
                <span style="font-size: 24px">👁️</span>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.totalViews }}</div>
                <div class="stat-label">总浏览次数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 250px">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="`${course.courseCode} - ${course.courseName}`"
              :value="course.id"
            ></el-option>
          </el-select>
          <el-select v-model="searchForm.attachmentType" placeholder="附件类型" clearable style="width: 150px">
            <el-option label="文档" value="document"></el-option>
            <el-option label="视频" value="video"></el-option>
            <el-option label="音频" value="audio"></el-option>
            <el-option label="图片" value="image"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索附件名称或内容"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
        <div class="toolbar-right">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="uploadData"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="false"
            :disabled="!uploadData.courseId"
          >
            <el-button type="success" :disabled="!uploadData.courseId">
              上传附件
            </el-button>
          </el-upload>
          <el-button type="info" @click="handleRefreshStatistics">刷新统计</el-button>
        </div>
      </div>

      <el-alert
        v-if="!uploadData.courseId"
        title="请先选择课程后再上传附件"
        type="warning"
        :closable="false"
        style="margin-top: 10px"
      />

      <!-- 附件列表 -->
      <el-table :data="attachmentList" border style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="attachmentName" label="文件名" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="课程" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getCourseNameById(row.courseId) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.attachmentType === 'document'" type="primary" size="small">文档</el-tag>
            <el-tag v-else-if="row.attachmentType === 'video'" type="success" size="small">视频</el-tag>
            <el-tag v-else-if="row.attachmentType === 'audio'" type="warning" size="small">音频</el-tag>
            <el-tag v-else-if="row.attachmentType === 'image'" type="" size="small">图片</el-tag>
            <el-tag v-else type="info" size="small">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载" width="80"></el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80"></el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180"></el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="['video', 'audio', 'document', 'image'].includes(row.attachmentType)"
              type="success"
              size="small"
              @click="handlePreview(row)"
            >
              预览
            </el-button>
            <el-button type="primary" size="small" @click="handleDownload(row)">下载</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { getCourseList, getCourseAttachments, deleteAttachment, recordDownload, recordView, advancedSearchAttachments, getAllAttachmentsWithStatistics } from '../api/course'

const userStore = useUserStore()

// 课程列表
const courses = ref([])

// 搜索表单
const searchForm = reactive({
  courseId: null,
  attachmentType: '',
  keyword: ''
})

// 分页
const page = ref(1)
const size = ref(10)
const total = ref(0)
const attachmentList = ref([])

// 统计数据
const statistics = ref({
  totalCount: 0,
  totalSize: 0,
  totalDownloads: 0,
  totalViews: 0
})

// 上传配置
const uploadUrl = '/api/course/attachment/upload'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))
const uploadData = reactive({
  courseId: null
})

// 预览对话框
const previewDialogVisible = ref(false)
const previewType = ref('')
const previewUrl = ref('')
const previewTitle = ref('')
const isOfficeDocument = ref(false)
const officeViewerUrl = ref('')

// 获取课程列表
const fetchCourses = async () => {
  try {
    const res = await getCourseList({ current: 1, size: 100 })
    if (res.code === 200) {
      courses.value = res.data.records
    }
  } catch (error) {
    console.error('获取课程列表失败', error)
  }
}

// 获取附件列表（使用新的统一API）
const fetchData = async () => {
  try {
    if (searchForm.keyword) {
      // 使用高级搜索（ES搜索）
      const res = await advancedSearchAttachments(
        searchForm.keyword,
        searchForm.courseId,
        searchForm.attachmentType,
        (page.value - 1) * size.value,
        size.value
      )
      if (res.code === 200) {
        attachmentList.value = res.data
        total.value = res.data.length
      }
      // 搜索时单独获取统计信息
      await fetchStatistics()
    } else {
      // 使用新的统一API，一次性获取附件列表和统计信息
      const params = {
        courseId: searchForm.courseId,
        attachmentType: searchForm.attachmentType,
        current: page.value,
        size: size.value
      }
      const res = await getAllAttachmentsWithStatistics(params)
      if (res.code === 200) {
        // 提取附件列表
        attachmentList.value = res.data.attachments.records
        total.value = res.data.attachments.total
        // 提取统计信息
        statistics.value = res.data.statistics
      }
    }
  } catch (error) {
    ElMessage.error('获取附件列表失败')
    console.error('获取附件列表失败', error)
  }
}

// 获取统计信息（仅在ES搜索时使用）
const fetchStatistics = async () => {
  try {
    const params = {
      current: 1,
      size: 1
    }
    const res = await getAllAttachmentsWithStatistics(params)
    if (res.code === 200) {
      statistics.value = res.data.statistics
    }
  } catch (error) {
    console.error('获取统计信息失败', error)
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.courseId = null
  searchForm.attachmentType = ''
  searchForm.keyword = ''
  uploadData.courseId = null
  page.value = 1
  fetchData()
}

// 刷新统计
const handleRefreshStatistics = async () => {
  await fetchData()
  ElMessage.success('数据已刷新')
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    fetchData()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 下载附件
const handleDownload = async (row) => {
  try {
    await recordDownload(row.id)
    const downloadUrl = `/api/file/download/${row.fileId}`
    window.open(downloadUrl, '_blank')
    // 更新下载次数
    row.downloadCount = (row.downloadCount || 0) + 1
    statistics.value.totalDownloads++
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 删除附件
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该附件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAttachment(id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 预览附件
const handlePreview = async (attachment) => {
  try {
    // 记录浏览次数
    await recordView(attachment.id)
    // 更新本地浏览次数
    attachment.viewCount = (attachment.viewCount || 0) + 1
    statistics.value.totalViews++
  } catch (error) {
    console.error('记录浏览次数失败', error)
  }

  previewType.value = attachment.attachmentType
  previewTitle.value = attachment.attachmentName

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

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 根据课程ID获取课程名称
const getCourseNameById = (courseId) => {
  const course = courses.value.find(c => c.id === courseId)
  return course ? course.courseName : '未知课程'
}

// 监听课程选择变化，自动同步到上传数据
watch(() => searchForm.courseId, (newValue) => {
  uploadData.courseId = newValue
})

onMounted(() => {
  fetchCourses().then(() => {
    fetchData()
  })
})
</script>

<style scoped>
.attachment-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.toolbar-left {
  display: flex;
  gap: 10px;
  flex: 1;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}
</style>
