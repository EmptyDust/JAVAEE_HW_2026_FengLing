<template>
  <div class="course-list">
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
        v-model="searchForm.status"
        placeholder="课程状态"
        style="width: 150px"
        clearable
        @keyup.enter="handleSearch"
      >
        <el-option label="启用" :value="1"></el-option>
        <el-option label="停用" :value="0"></el-option>
        <el-option label="已满" :value="2"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button type="success" @click="handleAdd">添加课程</el-button>
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
          {{ row.enrolledStudents || 0 }}/{{ row.maxStudents }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else-if="row.status === 0" type="danger">停用</el-tag>
          <el-tag v-else-if="row.status === 2" type="warning">已满</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="420" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" size="small" @click="handleManageCalendar(row)">日历</el-button>
          <el-button type="info" size="small" @click="handleViewAttachments(row)">附件</el-button>
          <el-button type="warning" size="small" @click="handleViewStudents(row)">学生</el-button>
          <el-button type="primary" size="small" @click="handleSendNotification(row)" plain>通知</el-button>
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

    <!-- 添加/编辑课程对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="form.courseCode" placeholder="请输入课程编号"></el-input>
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称"></el-input>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="form.courseType" placeholder="选择课程类型" style="width: 100%">
            <el-option label="必修" value="必修"></el-option>
            <el-option label="选修" value="选修"></el-option>
            <el-option label="公选" value="公选"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :max="10" :step="0.5" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="form.hours" :min="0" :max="200" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="选择授课教师" style="width: 100%" @change="handleTeacherChange">
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.id"
              :label="`${teacher.teacherNo} - ${teacher.teacherName}`"
              :value="teacher.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开课学期" prop="semester">
          <el-input v-model="form.semester" placeholder="例: 2024-2025-2"></el-input>
        </el-form-item>
        <el-form-item label="最大人数" prop="maxStudents">
          <el-input-number v-model="form.maxStudents" :min="1" :max="200" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="上课安排" prop="scheduleInfo">
          <el-input v-model="form.scheduleInfo" type="textarea" :rows="2" placeholder="例: 周一1-2节/A101，周三3-4节/A101"></el-input>
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input v-model="form.courseDescription" type="textarea" :rows="3" placeholder="请输入课程简介"></el-input>
        </el-form-item>
        <el-form-item label="课程大纲">
          <el-input v-model="form.courseOutline" type="textarea" :rows="3" placeholder="请输入课程大纲"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 课程附件管理对话框 -->
    <el-dialog v-model="attachmentDialogVisible" title="课程附件管理" width="900px">
      <div style="margin-bottom: 20px">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="{ courseId: currentCourse.id }"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :show-file-list="false"
        >
          <el-button type="primary">上传附件</el-button>
        </el-upload>
      </div>

      <!-- 附件搜索 -->
      <div style="margin-bottom: 15px; display: flex; gap: 10px; align-items: center">
        <el-input
          v-model="attachmentSearchKeyword"
          placeholder="搜索附件名称或内容"
          clearable
          style="width: 300px"
          @clear="handleClearSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearchAttachments">搜索</el-button>
        <el-button @click="handleClearSearch">显示全部</el-button>
      </div>

      <el-table :data="attachments" border>
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
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button
              v-if="row.attachmentType === 'video' || row.attachmentType === 'audio' || row.attachmentType === 'document' || row.attachmentType === 'image'"
              type="success"
              size="small"
              @click="handlePreview(row)">预览</el-button>
            <el-button type="primary" size="small" @click="handleDownload(row)">下载</el-button>
            <el-button type="danger" size="small" @click="handleDeleteAttachment(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 选课学生列表对话框 -->
    <el-dialog v-model="studentsDialogVisible" title="选课学生列表" width="900px">
      <el-table :data="enrolledStudents" border>
        <el-table-column prop="studentNumber" label="学号" width="150"></el-table-column>
        <el-table-column prop="studentName" label="姓名"></el-table-column>
        <el-table-column prop="className" label="班级"></el-table-column>
        <el-table-column prop="enrollmentTime" label="选课时间" width="180"></el-table-column>
        <el-table-column prop="score" label="成绩" width="100">
          <template #default="{ row }">
            <span v-if="row.score">{{ row.score }}</span>
            <el-tag v-else type="info">未录入</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleInputScore(row)">录入成绩</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="studentPage"
        v-model:page-size="studentSize"
        :total="studentTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchCourseStudents"
        @current-change="fetchCourseStudents"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-dialog>

    <!-- 课程日历管理对话框 -->
    <el-dialog v-model="calendarDialogVisible" title="课程日历管理" width="1000px">
      <div style="margin-bottom: 20px">
        <el-button type="primary" @click="handleGenerateCalendar">批量生成整学期日历</el-button>
        <el-button type="success" @click="handleAddCalendarEvent">添加单个事件</el-button>
      </div>

      <el-table :data="calendarEvents" border>
        <el-table-column prop="eventType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.eventType === 'class'" type="primary">上课</el-tag>
            <el-tag v-else-if="row.eventType === 'exam'" type="danger">考试</el-tag>
            <el-tag v-else-if="row.eventType === 'homework'" type="warning">作业</el-tag>
            <el-tag v-else-if="row.eventType === 'activity'" type="success">活动</el-tag>
            <el-tag v-else type="info">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventTitle" label="标题" width="150"></el-table-column>
        <el-table-column label="是否周期" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isRecurring === 1" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventDate" label="日期" width="110"></el-table-column>
        <el-table-column label="时间" width="130">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" show-overflow-tooltip></el-table-column>
        <el-table-column prop="recurrenceRule" label="重复规则" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditCalendarEvent(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteCalendarEvent(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加/编辑日历事件对话框 -->
    <el-dialog v-model="calendarEventDialogVisible" :title="calendarEventDialogTitle" width="600px">
      <el-form :model="calendarEventForm" :rules="calendarEventRules" ref="calendarEventFormRef" label-width="100px">
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="calendarEventForm.eventType" placeholder="选择事件类型" style="width: 100%">
            <el-option label="上课" value="class"></el-option>
            <el-option label="考试" value="exam"></el-option>
            <el-option label="作业" value="homework"></el-option>
            <el-option label="活动" value="activity"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="事件标题" prop="eventTitle">
          <el-input v-model="calendarEventForm.eventTitle" placeholder="请输入事件标题"></el-input>
        </el-form-item>
        <el-form-item label="事件描述">
          <el-input v-model="calendarEventForm.eventDescription" type="textarea" :rows="2" placeholder="请输入事件描述"></el-input>
        </el-form-item>
        <el-form-item label="是否周期" prop="isRecurring">
          <el-switch v-model="calendarEventForm.isRecurring" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
        <el-form-item label="事件日期" prop="eventDate">
          <el-date-picker v-model="calendarEventForm.eventDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD"></el-date-picker>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="calendarEventForm.startTime" placeholder="选择时间" style="width: 100%" value-format="HH:mm:ss"></el-time-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="calendarEventForm.endTime" placeholder="选择时间" style="width: 100%" value-format="HH:mm:ss"></el-time-picker>
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="calendarEventForm.location" placeholder="请输入地点"></el-input>
        </el-form-item>
        <el-form-item label="重复规则" v-if="calendarEventForm.isRecurring === 1">
          <el-input v-model="calendarEventForm.recurrenceRule" type="textarea" :rows="2" placeholder="例: FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=2025-06-30;INTERVAL=1"></el-input>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="calendarEventForm.color"></el-color-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="calendarEventDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCalendarEvent">确定</el-button>
      </template>
    </el-dialog>

    <!-- 成绩录入对话框 -->
    <el-dialog v-model="scoreDialogVisible" title="录入成绩" width="500px">
      <el-form :model="scoreForm" :rules="scoreRules" ref="scoreFormRef" label-width="100px">
        <el-form-item label="学号">
          <el-input v-model="scoreForm.studentNumber" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="scoreForm.studentName" disabled></el-input>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="scoreForm.className" disabled></el-input>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number v-model="scoreForm.score" :min="0" :max="100" :precision="1" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="等级" prop="grade">
          <el-select v-model="scoreForm.grade" placeholder="选择等级" style="width: 100%">
            <el-option label="优秀" value="优秀"></el-option>
            <el-option label="良好" value="良好"></el-option>
            <el-option label="中等" value="中等"></el-option>
            <el-option label="及格" value="及格"></el-option>
            <el-option label="不及格" value="不及格"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitScore">确定</el-button>
      </template>
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

    <!-- 发送通知对话框 -->
    <el-dialog v-model="notificationDialogVisible" :title="notificationDialogTitle" width="600px">
      <el-form :model="notificationForm" :rules="notificationRules" ref="notificationFormRef" label-width="100px">
        <el-form-item label="通知类型" prop="notificationType">
          <el-select v-model="notificationForm.notificationType" placeholder="选择通知类型" style="width: 100%" @change="handleNotificationTypeChange">
            <el-option label="公告" value="announcement"></el-option>
            <el-option label="作业" value="homework"></el-option>
            <el-option label="考试" value="exam"></el-option>
            <el-option label="取消" value="cancel"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择模板">
          <el-select v-model="notificationForm.templateId" placeholder="选择通知模板（可选）" style="width: 100%" clearable @change="handleTemplateChange">
            <el-option
              v-for="template in notificationTemplates"
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
          <el-radio-group v-model="notificationForm.priority">
            <el-radio :label="0">普通</el-radio>
            <el-radio :label="1">重要</el-radio>
            <el-radio :label="2">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="notificationForm.content" type="textarea" :rows="5" placeholder="请输入通知内容"></el-input>
        </el-form-item>
        <el-form-item label="定时发送">
          <el-switch v-model="notificationForm.isScheduled"></el-switch>
          <span style="margin-left: 10px; color: #909399; font-size: 12px">开启后可设置未来时间发送</span>
        </el-form-item>
        <el-form-item label="发送时间" v-if="notificationForm.isScheduled">
          <el-date-picker
            v-model="notificationForm.scheduledTime"
            type="datetime"
            placeholder="选择发送时间"
            style="width: 100%"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledDate"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="发送方式" prop="sendMethod">
          <el-checkbox-group v-model="notificationForm.sendMethods">
            <el-checkbox label="websocket">站内信</el-checkbox>
            <el-checkbox label="sms" disabled>短信（暂未开通）</el-checkbox>
            <el-checkbox label="email" disabled>邮件（暂未开通）</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="目标对象">
          <el-tag v-if="notificationForm.targetType === 'enrolled'" type="success">
            {{ notificationForm.courseName }} 的选课学生
          </el-tag>
          <el-tag v-else-if="notificationForm.targetType === 'all'" type="warning">
            全体学生
          </el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="notificationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitNotification" :loading="sendingNotification">
          {{ notificationForm.isScheduled ? '定时发送' : '立即发送' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import {
  getCourseList,
  createCourse,
  updateCourse,
  deleteCourse,
  getCourseAttachments,
  deleteAttachment,
  getCourseStudents,
  recordDownload,
  getRawCourseCalendar,
  generateCourseCalendar,
  createCalendarEvent,
  updateCalendarEvent,
  deleteCalendarEvent,
  updateScore,
  advancedSearchAttachments
} from '../api/course'
import { createNotification, getTemplatesByType, applyTemplate } from '../api/notification'
import { getAllTeachers } from '../api/teacher'

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  courseName: '',
  courseType: '',
  status: null
})

// 分页
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = reactive({
  id: null,
  courseCode: '',
  courseName: '',
  courseType: '',
  credit: 2.0,
  hours: 32,
  teacherId: null,
  teacherName: '',
  semester: '',
  maxStudents: 50,
  scheduleInfo: '',
  courseDescription: '',
  courseOutline: ''
})

// 表单验证规则
const rules = {
  courseCode: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  hours: [{ required: true, message: '请输入学时', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择授课教师', trigger: 'change' }],
  semester: [{ required: true, message: '请输入开课学期', trigger: 'blur' }],
  maxStudents: [{ required: true, message: '请输入最大选课人数', trigger: 'blur' }]
}

// 教师列表
const teacherList = ref([])

// 附件管理
const attachmentDialogVisible = ref(false)
const currentCourse = ref({})
const attachments = ref([])
const attachmentSearchKeyword = ref('')
const uploadUrl = '/api/course/attachment/upload'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

// 学生列表
const studentsDialogVisible = ref(false)
const enrolledStudents = ref([])
const studentPage = ref(1)
const studentSize = ref(10)
const studentTotal = ref(0)

// 课程日历管理
const calendarDialogVisible = ref(false)
const calendarEvents = ref([])
const calendarEventDialogVisible = ref(false)
const calendarEventDialogTitle = ref('')
const calendarEventFormRef = ref(null)
const calendarEventForm = reactive({
  id: null,
  courseId: null,
  eventType: 'class',
  eventTitle: '',
  eventDescription: '',
  isRecurring: 0,
  eventDate: '',
  startTime: '',
  endTime: '',
  location: '',
  recurrenceRule: '',
  color: '#409EFF'
})

// 日历事件表单验证规则
const calendarEventRules = {
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  eventTitle: [{ required: true, message: '请输入事件标题', trigger: 'blur' }],
  eventDate: [{ required: true, message: '请选择事件日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }]
}

// 成绩录入
const scoreDialogVisible = ref(false)
const scoreFormRef = ref(null)
const scoreForm = reactive({
  enrollmentId: null,
  studentNumber: '',
  studentName: '',
  className: '',
  score: null,
  grade: ''
})

// 成绩表单验证规则
const scoreRules = {
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择等级', trigger: 'change' }]
}

// 媒体预览
const previewDialogVisible = ref(false)
const previewType = ref('')
const previewUrl = ref('')
const previewTitle = ref('')
const isOfficeDocument = ref(false)
const officeViewerUrl = ref('')

// 通知发送
const notificationDialogVisible = ref(false)
const notificationDialogTitle = ref('')
const notificationFormRef = ref(null)
const sendingNotification = ref(false)
const notificationTemplates = ref([])
const notificationForm = reactive({
  courseId: null,
  courseName: '',
  notificationType: 'announcement',
  title: '',
  content: '',
  priority: 0,
  sendMethods: ['websocket'],
  targetType: 'enrolled',
  templateId: null,
  isScheduled: false,
  scheduledTime: null
})

// 通知表单验证规则
const notificationRules = {
  notificationType: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

// 获取课程列表
const fetchData = async () => {
  try {
    const params = {
      current: page.value,
      size: size.value,
      ...searchForm
    }
    const res = await getCourseList(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
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

// 添加课程
const handleAdd = () => {
  dialogTitle.value = '添加课程'
  resetForm()
  dialogVisible.value = true
}

// 编辑课程
const handleEdit = (row) => {
  dialogTitle.value = '编辑课程'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.courseCode = ''
  form.courseName = ''
  form.courseType = ''
  form.credit = 2.0
  form.hours = 32
  form.teacherName = ''
  form.semester = ''
  form.maxStudents = 50
  form.scheduleInfo = ''
  form.courseDescription = ''
  form.courseOutline = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateCourse(form)
          ElMessage.success('更新成功')
        } else {
          await createCourse(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error(form.id ? '更新失败' : '添加失败')
      }
    }
  })
}

// 删除课程
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该课程吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 查看课程附件
const handleViewAttachments = async (row) => {
  currentCourse.value = row
  attachmentDialogVisible.value = true
  await fetchAttachments()
}

// 获取课程附件
const fetchAttachments = async () => {
  try {
    const res = await getCourseAttachments(currentCourse.value.id)
    if (res.code === 200) {
      attachments.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取附件列表失败')
  }
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    fetchAttachments()
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
    // 通过file-service的下载接口下载文件
    const downloadUrl = `/api/file/download/${row.fileId}`
    window.open(downloadUrl, '_blank')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 删除附件
const handleDeleteAttachment = (id) => {
  ElMessageBox.confirm('确定要删除该附件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAttachment(id)
      ElMessage.success('删除成功')
      fetchAttachments()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
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
      currentCourse.value.id,
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
const handleClearSearch = () => {
  attachmentSearchKeyword.value = ''
  fetchAttachments()
}

// 查看选课学生
const handleViewStudents = async (row) => {
  currentCourse.value = row
  studentsDialogVisible.value = true
  studentPage.value = 1
  await fetchCourseStudents()
}

// 获取选课学生列表
const fetchCourseStudents = async () => {
  try {
    const res = await getCourseStudents(currentCourse.value.id, {
      current: studentPage.value,
      size: studentSize.value
    })
    if (res.code === 200) {
      enrolledStudents.value = res.data.records
      studentTotal.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取学生列表失败')
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

// 管理课程日历
const handleManageCalendar = async (row) => {
  currentCourse.value = row
  calendarDialogVisible.value = true
  await fetchCalendarEvents()
}

// 获取课程日历事件列表
const fetchCalendarEvents = async () => {
  try {
    const res = await getRawCourseCalendar(currentCourse.value.id)
    if (res.code === 200) {
      calendarEvents.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取日历事件失败')
  }
}

// 批量生成整学期日历
const handleGenerateCalendar = async () => {
  if (!currentCourse.value.scheduleInfo) {
    ElMessage.warning('该课程没有设置上课时间，无法自动生成日历')
    return
  }

  if (!currentCourse.value.semester) {
    ElMessage.warning('该课程没有设置学期信息')
    return
  }

  ElMessageBox.prompt('请输入学期开始和结束日期（格式：2025-02-24,2025-06-30）', '批量生成日历', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^\d{4}-\d{2}-\d{2},\d{4}-\d{2}-\d{2}$/,
    inputErrorMessage: '日期格式不正确'
  }).then(async ({ value }) => {
    const [startDate, endDate] = value.split(',')
    try {
      const res = await generateCourseCalendar(currentCourse.value.id, startDate, endDate)
      if (res.code === 200) {
        ElMessage.success(`成功生成 ${res.data} 条日历记录`)
        await fetchCalendarEvents()
      } else {
        ElMessage.error(res.message || '生成失败')
      }
    } catch (error) {
      ElMessage.error('生成日历失败')
    }
  })
}

// 添加单个日历事件
const handleAddCalendarEvent = () => {
  calendarEventDialogTitle.value = '添加日历事件'
  resetCalendarEventForm()
  calendarEventForm.courseId = currentCourse.value.id
  calendarEventDialogVisible.value = true
}

// 编辑日历事件
const handleEditCalendarEvent = (row) => {
  calendarEventDialogTitle.value = '编辑日历事件'
  Object.assign(calendarEventForm, row)
  calendarEventDialogVisible.value = true
}

// 重置日历事件表单
const resetCalendarEventForm = () => {
  calendarEventForm.id = null
  calendarEventForm.courseId = null
  calendarEventForm.eventType = 'class'
  calendarEventForm.eventTitle = ''
  calendarEventForm.eventDescription = ''
  calendarEventForm.isRecurring = 0
  calendarEventForm.eventDate = ''
  calendarEventForm.startTime = ''
  calendarEventForm.endTime = ''
  calendarEventForm.location = ''
  calendarEventForm.recurrenceRule = ''
  calendarEventForm.color = '#409EFF'
  if (calendarEventFormRef.value) {
    calendarEventFormRef.value.clearValidate()
  }
}

// 提交日历事件表单
const handleSubmitCalendarEvent = async () => {
  if (!calendarEventFormRef.value) return

  await calendarEventFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (calendarEventForm.id) {
          await updateCalendarEvent(calendarEventForm)
          ElMessage.success('更新成功')
        } else {
          await createCalendarEvent(calendarEventForm)
          ElMessage.success('添加成功')
        }
        calendarEventDialogVisible.value = false
        await fetchCalendarEvents()
      } catch (error) {
        ElMessage.error(calendarEventForm.id ? '更新失败' : '添加失败')
      }
    }
  })
}

// 删除日历事件
const handleDeleteCalendarEvent = (id) => {
  ElMessageBox.confirm('确定要删除该日历事件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCalendarEvent(id)
      ElMessage.success('删除成功')
      await fetchCalendarEvents()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return ''
  return time.substring(0, 5)
}

// 录入成绩
const handleInputScore = (row) => {
  scoreForm.enrollmentId = row.id
  scoreForm.studentNumber = row.studentNumber
  scoreForm.studentName = row.studentName
  scoreForm.className = row.className
  scoreForm.score = row.score || null
  scoreForm.grade = row.grade || ''
  scoreDialogVisible.value = true
}

// 提交成绩
const handleSubmitScore = async () => {
  if (!scoreFormRef.value) return

  await scoreFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await updateScore(scoreForm.enrollmentId, scoreForm.score, scoreForm.grade)
        ElMessage.success('成绩录入成功')
        scoreDialogVisible.value = false
        await fetchCourseStudents()
      } catch (error) {
        ElMessage.error('成绩录入失败')
      }
    }
  })
}

// 预览附件
const handlePreview = (attachment) => {
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
    // 构造完整的文件URL（需要公网可访问）
    const fullFileUrl = `${window.location.origin}/api/file/stream/${attachment.fileId}`
    // 构造Office Web Viewer URL
    officeViewerUrl.value = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(fullFileUrl)}`
  } else {
    // PDF和其他文档使用流式传输接口
    previewUrl.value = `/api/file/stream/${attachment.fileId}`
  }

  previewDialogVisible.value = true
}

// 发送通知给课程学生
const handleSendNotification = (row) => {
  notificationDialogTitle.value = '发送课程通知'
  resetNotificationForm()
  notificationForm.courseId = row.id
  notificationForm.courseName = row.courseName
  notificationForm.targetType = 'enrolled'
  notificationDialogVisible.value = true
}

// 重置通知表单
const resetNotificationForm = () => {
  notificationForm.courseId = null
  notificationForm.courseName = ''
  notificationForm.notificationType = 'announcement'
  notificationForm.title = ''
  notificationForm.content = ''
  notificationForm.priority = 0
  notificationForm.sendMethods = ['websocket']
  notificationForm.targetType = 'enrolled'
  notificationForm.templateId = null
  notificationForm.isScheduled = false
  notificationForm.scheduledTime = null
  notificationTemplates.value = []
  if (notificationFormRef.value) {
    notificationFormRef.value.clearValidate()
  }
}

// 通知类型改变时加载对应模板
const handleNotificationTypeChange = async (type) => {
  try {
    const res = await getTemplatesByType(type)
    if (res.code === 200) {
      notificationTemplates.value = res.data
    }
  } catch (error) {
    console.error('加载模板失败', error)
  }
}

// 选择模板后应用模板内容
const handleTemplateChange = async (templateId) => {
  if (!templateId) {
    return
  }

  try {
    // 准备模板变量
    const variables = {
      courseName: notificationForm.courseName || '课程',
      teacherName: userStore.username || '教师',
      date: new Date().toLocaleDateString('zh-CN'),
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }

    const res = await applyTemplate(templateId, variables)
    if (res.code === 200) {
      notificationForm.title = res.data.title
      notificationForm.content = res.data.content
      ElMessage.success('模板应用成功')
    }
  } catch (error) {
    ElMessage.error('应用模板失败')
    console.error('应用模板失败', error)
  }
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 提交发送通知
const handleSubmitNotification = async () => {
  if (!notificationFormRef.value) return

  await notificationFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证定时发送时间
      if (notificationForm.isScheduled && !notificationForm.scheduledTime) {
        ElMessage.warning('请选择发送时间')
        return
      }

      try {
        sendingNotification.value = true

        // 如果是发送全体通知，需要选择一个课程ID（可以使用第一个课程）
        let courseId = notificationForm.courseId
        if (notificationForm.targetType === 'all' && !courseId) {
          // 获取第一个课程ID
          if (tableData.value.length > 0) {
            courseId = tableData.value[0].id
          } else {
            ElMessage.warning('没有可用的课程，无法发送通知')
            sendingNotification.value = false
            return
          }
        }

        const data = {
          courseId: courseId,
          notificationType: notificationForm.notificationType,
          title: notificationForm.title,
          content: notificationForm.content,
          priority: notificationForm.priority,
          sendMethod: notificationForm.sendMethods.join(','),
          targetType: notificationForm.targetType,
          templateId: notificationForm.templateId,
          isScheduled: notificationForm.isScheduled ? 1 : 0,
          scheduledTime: notificationForm.scheduledTime
        }

        const res = await createNotification(data)
        if (res.code === 200) {
          if (notificationForm.isScheduled) {
            ElMessage.success(`通知已创建，将在 ${notificationForm.scheduledTime} 发送`)
          } else {
            ElMessage.success('通知发送成功')
          }
          notificationDialogVisible.value = false
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

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const res = await getAllTeachers()
    if (res.code === 200) {
      teacherList.value = res.data
    }
  } catch (error) {
    console.error('获取教师列表失败', error)
  }
}

// 处理教师选择变化
const handleTeacherChange = (teacherId) => {
  const teacher = teacherList.value.find(t => t.id === teacherId)
  if (teacher) {
    form.teacherName = teacher.teacherName
  }
}

onMounted(() => {
  fetchData()
  fetchTeachers()
})
</script>

<style scoped>
.course-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
}
</style>
