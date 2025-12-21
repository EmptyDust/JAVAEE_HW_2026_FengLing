<template>
  <div class="notification-management">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 通知列表标签页 -->
      <el-tab-pane label="通知列表" name="notifications">
        <div class="toolbar">
          <el-select v-model="notificationFilter.courseId" placeholder="选择课程" clearable style="width: 200px">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="`${course.courseCode} - ${course.courseName}`"
              :value="course.id"
            ></el-option>
          </el-select>
          <el-button type="primary" @click="handleSearchNotifications">查询</el-button>
          <el-button type="success" @click="handleRefreshStatistics">刷新统计</el-button>
          <el-button type="warning" @click="handleSendNotification">发送通知</el-button>
        </div>

        <!-- 统计卡片 -->
        <el-row :gutter="20" style="margin: 20px 0">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-card">
                <div class="stat-icon" style="background: #409EFF">
                  <span style="font-size: 24px">📊</span>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.totalCount }}</div>
                  <div class="stat-label">总通知数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-card">
                <div class="stat-icon" style="background: #67C23A">
                  <span style="font-size: 24px">📤</span>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.totalSendCount }}</div>
                  <div class="stat-label">已发送数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-card">
                <div class="stat-icon" style="background: #E6A23C">
                  <span style="font-size: 24px">👁️</span>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.totalReadCount }}</div>
                  <div class="stat-label">已读数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-card">
                <div class="stat-icon" style="background: #F56C6C">
                  <span style="font-size: 24px">📈</span>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.readRate }}%</div>
                  <div class="stat-label">已读率</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 通知列表 -->
        <el-table :data="notificationList" border>
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getNotificationTypeTag(row.notificationType)" size="small">
                {{ getNotificationTypeText(row.notificationType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip></el-table-column>
          <el-table-column prop="content" label="内容" show-overflow-tooltip></el-table-column>
          <el-table-column label="优先级" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.priority === 2" type="danger" size="small">紧急</el-tag>
              <el-tag v-else-if="row.priority === 1" type="warning" size="small">重要</el-tag>
              <el-tag v-else type="info" size="small">普通</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发送状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.sendStatus === 0" type="info" size="small">待发送</el-tag>
              <el-tag v-else-if="row.sendStatus === 1" type="warning" size="small">发送中</el-tag>
              <el-tag v-else-if="row.sendStatus === 2" type="success" size="small">已发送</el-tag>
              <el-tag v-else-if="row.sendStatus === 3" type="danger" size="small">失败</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发送/已读" width="100">
            <template #default="{ row }">
              {{ row.sendCount || 0 }} / {{ row.readCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleDeleteNotification(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="notificationPage"
          v-model:page-size="notificationSize"
          :total="notificationTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchNotifications"
          @current-change="fetchNotifications"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </el-tab-pane>

      <!-- 模板管理标签页 -->
      <el-tab-pane label="模板管理" name="templates">
        <div class="toolbar">
          <el-button type="primary" @click="handleAddTemplate">添加模板</el-button>
          <el-button type="success" @click="handleInitDefaultTemplates">初始化默认模板</el-button>
        </div>

        <el-table :data="templateList" border style="margin-top: 20px">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="templateName" label="模板名称" width="150"></el-table-column>
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getNotificationTypeTag(row.notificationType)" size="small">
                {{ getNotificationTypeText(row.notificationType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="titleTemplate" label="标题模板" show-overflow-tooltip></el-table-column>
          <el-table-column prop="contentTemplate" label="内容模板" show-overflow-tooltip></el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isEnabled === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="info" size="small">禁用</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEditTemplate(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDeleteTemplate(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="templatePage"
          v-model:page-size="templateSize"
          :total="templateTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchTemplates"
          @current-change="fetchTemplates"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑模板对话框 -->
    <el-dialog v-model="templateDialogVisible" :title="templateDialogTitle" width="700px">
      <el-form :model="templateForm" :rules="templateRules" ref="templateFormRef" label-width="120px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称"></el-input>
        </el-form-item>
        <el-form-item label="通知类型" prop="notificationType">
          <el-select v-model="templateForm.notificationType" placeholder="选择通知类型" style="width: 100%">
            <el-option label="公告" value="announcement"></el-option>
            <el-option label="作业" value="homework"></el-option>
            <el-option label="考试" value="exam"></el-option>
            <el-option label="取消" value="cancel"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标题模板" prop="titleTemplate">
          <el-input v-model="templateForm.titleTemplate" placeholder="例: {courseName} - 作业通知"></el-input>
        </el-form-item>
        <el-form-item label="内容模板" prop="contentTemplate">
          <el-input
            v-model="templateForm.contentTemplate"
            type="textarea"
            :rows="5"
            placeholder="例: 各位同学，{courseName}课程有新的作业，请在{date}前完成。"
          ></el-input>
        </el-form-item>
        <el-form-item label="变量说明">
          <el-input
            v-model="templateForm.variables"
            type="textarea"
            :rows="2"
            placeholder="例: courseName,teacherName,date,time"
          ></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="templateForm.description" placeholder="请输入模板描述"></el-input>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="templateForm.isEnabled" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTemplate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 发送通知对话框 -->
    <el-dialog v-model="sendNotificationDialogVisible" title="发送通知" width="600px">
      <el-form :model="sendNotificationForm" :rules="sendNotificationRules" ref="sendNotificationFormRef" label-width="100px">
        <el-form-item label="目标对象" prop="targetType">
          <el-radio-group v-model="sendNotificationForm.targetType">
            <el-radio label="course">指定课程</el-radio>
            <el-radio label="all">全体学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择课程" v-if="sendNotificationForm.targetType === 'course'" prop="courseId" required>
          <el-select v-model="sendNotificationForm.courseId" placeholder="选择课程" style="width: 100%">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="`${course.courseCode} - ${course.courseName}`"
              :value="course.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通知类型" prop="notificationType">
          <el-select v-model="sendNotificationForm.notificationType" placeholder="选择通知类型" style="width: 100%" @change="handleNotificationTypeChange">
            <el-option label="公告" value="announcement"></el-option>
            <el-option label="作业" value="homework"></el-option>
            <el-option label="考试" value="exam"></el-option>
            <el-option label="取消" value="cancel"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择模板">
          <el-select v-model="sendNotificationForm.templateId" placeholder="选择通知模板（可选）" style="width: 100%" clearable @change="handleTemplateSelect">
            <el-option
              v-for="template in availableTemplates"
              :key="template.id"
              :label="template.templateName"
              :value="template.id"
            >
              <span>{{ template.templateName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ template.description }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="sendNotificationForm.priority">
            <el-radio :label="0">普通</el-radio>
            <el-radio :label="1">重要</el-radio>
            <el-radio :label="2">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="sendNotificationForm.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="sendNotificationForm.content" type="textarea" :rows="5" placeholder="请输入通知内容"></el-input>
        </el-form-item>
        <el-form-item label="定时发送">
          <el-switch v-model="sendNotificationForm.isScheduled"></el-switch>
          <span style="margin-left: 10px; color: #909399; font-size: 12px">开启后可设置未来时间发送</span>
        </el-form-item>
        <el-form-item label="发送时间" v-if="sendNotificationForm.isScheduled">
          <el-date-picker
            v-model="sendNotificationForm.scheduledTime"
            type="datetime"
            placeholder="选择发送时间"
            style="width: 100%"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledDate"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="发送方式" prop="sendMethod">
          <el-checkbox-group v-model="sendNotificationForm.sendMethods">
            <el-checkbox label="websocket">站内信</el-checkbox>
            <el-checkbox label="sms" disabled>短信（暂未开通）</el-checkbox>
            <el-checkbox label="email" disabled>邮件（暂未开通）</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendNotificationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitSendNotification" :loading="sendingNotification">
          {{ sendNotificationForm.isScheduled ? '定时发送' : '立即发送' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList } from '../api/course'
import {
  getNotificationList,
  deleteNotification,
  getNotificationStatistics,
  getTemplateList,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  initDefaultTemplates,
  createNotification,
  getTemplatesByType,
  applyTemplate
} from '../api/notification'

const activeTab = ref('notifications')

// 课程列表
const courses = ref([])

// 通知列表相关
const notificationFilter = reactive({
  courseId: null
})
const notificationPage = ref(1)
const notificationSize = ref(10)
const notificationTotal = ref(0)
const notificationList = ref([])

// 统计数据
const statistics = ref({
  totalCount: 0,
  totalSendCount: 0,
  totalReadCount: 0,
  readRate: 0,
  typeStats: {},
  statusStats: {}
})

// 模板列表相关
const templatePage = ref(1)
const templateSize = ref(10)
const templateTotal = ref(0)
const templateList = ref([])

// 模板对话框
const templateDialogVisible = ref(false)
const templateDialogTitle = ref('')
const templateFormRef = ref(null)
const templateForm = reactive({
  id: null,
  templateName: '',
  notificationType: 'announcement',
  titleTemplate: '',
  contentTemplate: '',
  variables: '',
  description: '',
  isEnabled: 1
})

// 模板表单验证规则
const templateRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  notificationType: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  titleTemplate: [{ required: true, message: '请输入标题模板', trigger: 'blur' }],
  contentTemplate: [{ required: true, message: '请输入内容模板', trigger: 'blur' }]
}

// 发送通知对话框
const sendNotificationDialogVisible = ref(false)
const sendNotificationFormRef = ref(null)
const sendingNotification = ref(false)
const availableTemplates = ref([])
const sendNotificationForm = reactive({
  targetType: 'course',
  courseId: null,
  notificationType: 'announcement',
  templateId: null,
  title: '',
  content: '',
  priority: 0,
  sendMethods: ['websocket'],
  isScheduled: false,
  scheduledTime: null
})

// 发送通知表单验证规则
const sendNotificationRules = {
  notificationType: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

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

// 获取通知列表
const fetchNotifications = async () => {
  try {
    const params = {
      current: notificationPage.value,
      size: notificationSize.value,
      courseId: notificationFilter.courseId
    }
    const res = await getNotificationList(params)
    if (res.code === 200) {
      notificationList.value = res.data.records
      notificationTotal.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取通知列表失败')
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const params = {
      courseId: notificationFilter.courseId
    }
    const res = await getNotificationStatistics(params)
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 搜索通知
const handleSearchNotifications = () => {
  notificationPage.value = 1
  fetchNotifications()
  fetchStatistics()
}

// 刷新统计
const handleRefreshStatistics = () => {
  fetchStatistics()
  ElMessage.success('统计数据已刷新')
}

// 删除通知
const handleDeleteNotification = (id) => {
  ElMessageBox.confirm('确定要删除该通知吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNotification(id)
      ElMessage.success('删除成功')
      fetchNotifications()
      fetchStatistics()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 获取模板列表
const fetchTemplates = async () => {
  try {
    const params = {
      current: templatePage.value,
      size: templateSize.value
    }
    const res = await getTemplateList(params)
    if (res.code === 200) {
      templateList.value = res.data.records
      templateTotal.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取模板列表失败')
  }
}

// 添加模板
const handleAddTemplate = () => {
  templateDialogTitle.value = '添加模板'
  resetTemplateForm()
  templateDialogVisible.value = true
}

// 编辑模板
const handleEditTemplate = (row) => {
  templateDialogTitle.value = '编辑模板'
  Object.assign(templateForm, row)
  templateDialogVisible.value = true
}

// 重置模板表单
const resetTemplateForm = () => {
  templateForm.id = null
  templateForm.templateName = ''
  templateForm.notificationType = 'announcement'
  templateForm.titleTemplate = ''
  templateForm.contentTemplate = ''
  templateForm.variables = ''
  templateForm.description = ''
  templateForm.isEnabled = 1
  if (templateFormRef.value) {
    templateFormRef.value.clearValidate()
  }
}

// 提交模板表单
const handleSubmitTemplate = async () => {
  if (!templateFormRef.value) return

  await templateFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (templateForm.id) {
          await updateTemplate(templateForm)
          ElMessage.success('更新成功')
        } else {
          await createTemplate(templateForm)
          ElMessage.success('添加成功')
        }
        templateDialogVisible.value = false
        fetchTemplates()
      } catch (error) {
        ElMessage.error(templateForm.id ? '更新失败' : '添加失败')
      }
    }
  })
}

// 删除模板
const handleDeleteTemplate = (id) => {
  ElMessageBox.confirm('确定要删除该模板吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTemplate(id)
      ElMessage.success('删除成功')
      fetchTemplates()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 初始化默认模板
const handleInitDefaultTemplates = () => {
  ElMessageBox.confirm('确定要初始化默认模板吗？这将创建系统预设的模板。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await initDefaultTemplates()
      ElMessage.success('默认模板初始化成功')
      fetchTemplates()
    } catch (error) {
      ElMessage.error('初始化失败')
    }
  })
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

// 打开发送通知对话框
const handleSendNotification = async () => {
  sendNotificationDialogVisible.value = true
  resetSendNotificationForm()
  // 加载默认通知类型的模板
  await handleNotificationTypeChange('announcement')
}

// 重置发送通知表单
const resetSendNotificationForm = () => {
  sendNotificationForm.targetType = 'course'
  sendNotificationForm.courseId = null
  sendNotificationForm.notificationType = 'announcement'
  sendNotificationForm.templateId = null
  sendNotificationForm.title = ''
  sendNotificationForm.content = ''
  sendNotificationForm.priority = 0
  sendNotificationForm.sendMethods = ['websocket']
  sendNotificationForm.isScheduled = false
  sendNotificationForm.scheduledTime = null
  availableTemplates.value = []
  if (sendNotificationFormRef.value) {
    sendNotificationFormRef.value.clearValidate()
  }
}

// 通知类型改变时加载对应模板
const handleNotificationTypeChange = async (type) => {
  try {
    const res = await getTemplatesByType(type)
    if (res.code === 200) {
      availableTemplates.value = res.data
    }
  } catch (error) {
    console.error('加载模板失败', error)
  }
}

// 选择模板后应用模板内容
const handleTemplateSelect = async (templateId) => {
  if (!templateId) {
    return
  }

  try {
    // 准备模板变量
    const courseName = sendNotificationForm.courseId
      ? courses.value.find(c => c.id === sendNotificationForm.courseId)?.courseName || '课程'
      : '全体学生'

    const variables = {
      courseName: courseName,
      teacherName: '教师',
      date: new Date().toLocaleDateString('zh-CN'),
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }

    const res = await applyTemplate(templateId, variables)
    if (res.code === 200) {
      sendNotificationForm.title = res.data.title
      sendNotificationForm.content = res.data.content
      ElMessage.success('模板应用成功')
    }
  } catch (error) {
    ElMessage.error('应用模板失败')
    console.error('应用模板失败', error)
  }
}

// 提交发送通知
const handleSubmitSendNotification = async () => {
  if (!sendNotificationFormRef.value) return

  await sendNotificationFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证目标对象和课程选择
      if (sendNotificationForm.targetType === 'course') {
        if (!sendNotificationForm.courseId) {
          ElMessage.warning('请选择课程')
          return
        }
      }

      // 验证定时发送时间
      if (sendNotificationForm.isScheduled && !sendNotificationForm.scheduledTime) {
        ElMessage.warning('请选择发送时间')
        return
      }

      try {
        sendingNotification.value = true

        // 确定courseId和课程信息
        let courseId = sendNotificationForm.courseId
        let courseName = ''
        let courseCode = ''

        if (sendNotificationForm.targetType === 'all') {
          // 发送全体通知，使用第一个课程ID
          if (courses.value.length > 0) {
            courseId = courses.value[0].id
          } else {
            ElMessage.warning('没有可用的课程，无法发送通知')
            sendingNotification.value = false
            return
          }
        } else {
          // 获取选中课程的信息
          const selectedCourse = courses.value.find(c => c.id === sendNotificationForm.courseId)
          if (selectedCourse) {
            courseName = selectedCourse.courseName
            courseCode = selectedCourse.courseCode
          }
        }

        // 构建通知数据，为指定课程的通知添加课程信息前缀
        let title = sendNotificationForm.title
        if (sendNotificationForm.targetType === 'course' && courseCode && courseName) {
          // 如果标题中还没有课程信息，则添加
          if (!title.includes(courseCode) && !title.includes(courseName)) {
            title = `[${courseCode} - ${courseName}] ${title}`
          }
        }

        const data = {
          courseId: courseId,
          notificationType: sendNotificationForm.notificationType,
          title: title,
          content: sendNotificationForm.content,
          priority: sendNotificationForm.priority,
          sendMethod: sendNotificationForm.sendMethods.join(','),
          targetType: sendNotificationForm.targetType === 'all' ? 'all' : 'enrolled',
          templateId: sendNotificationForm.templateId,
          isScheduled: sendNotificationForm.isScheduled ? 1 : 0,
          scheduledTime: sendNotificationForm.scheduledTime
        }

        const res = await createNotification(data)
        if (res.code === 200) {
          if (sendNotificationForm.isScheduled) {
            ElMessage.success(`通知已创建，将在 ${sendNotificationForm.scheduledTime} 发送`)
          } else {
            ElMessage.success('通知发送成功')
          }
          sendNotificationDialogVisible.value = false
          fetchNotifications()
          fetchStatistics()
        } else {
          ElMessage.error(res.message || '通知发送失败')
        }
      } catch (error) {
        ElMessage.error('通知发送失败')
        console.error('发送通知失败', error)
      } finally {
        sendingNotification.value = false
      }
    }
  })
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

onMounted(() => {
  fetchCourses()
  fetchNotifications()
  fetchStatistics()
  fetchTemplates()
})
</script>

<style scoped>
.notification-management {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
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
</style>
