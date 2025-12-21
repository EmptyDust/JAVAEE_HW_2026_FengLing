<template>
  <div class="teacher-list">
    <div class="toolbar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="工号或姓名"
        style="width: 200px"
        clearable
        @keyup.enter="handleSearch"
      ></el-input>
      <el-select
        v-model="searchForm.department"
        placeholder="选择部门"
        style="width: 200px"
        clearable
      >
        <el-option label="计算机科学与技术学院" value="计算机科学与技术学院"></el-option>
        <el-option label="软件工程学院" value="软件工程学院"></el-option>
        <el-option label="信息工程学院" value="信息工程学院"></el-option>
        <el-option label="数学与统计学院" value="数学与统计学院"></el-option>
      </el-select>
      <el-select
        v-model="searchForm.title"
        placeholder="选择职称"
        style="width: 150px"
        clearable
      >
        <el-option label="助教" value="助教"></el-option>
        <el-option label="讲师" value="讲师"></el-option>
        <el-option label="副教授" value="副教授"></el-option>
        <el-option label="教授" value="教授"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button type="success" @click="handleAdd">添加教师</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="头像" width="80">
        <template #default="{ row }">
          <el-avatar v-if="row.avatarFileId" :src="`/api/file/stream/${row.avatarFileId}`" :size="50"></el-avatar>
          <el-avatar v-else :size="50">{{ row.teacherName?.charAt(0) }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="teacherName" label="姓名" width="100"></el-table-column>
      <el-table-column prop="teacherNo" label="工号" width="120"></el-table-column>
      <el-table-column prop="title" label="职称" width="100"></el-table-column>
      <el-table-column prop="department" label="部门" show-overflow-tooltip></el-table-column>
      <el-table-column prop="phone" label="电话" width="130"></el-table-column>
      <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" size="small" @click="handleUploadAvatar(row)">头像</el-button>
          <el-button type="info" size="small" @click="handleViewCourses(row)">课程</el-button>
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

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="教师姓名" prop="teacherName">
          <el-input v-model="form.teacherName" placeholder="请输入教师姓名"></el-input>
        </el-form-item>
        <el-form-item label="教师工号" prop="teacherNo">
          <el-input v-model="form.teacherNo" placeholder="请输入教师工号" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="form.title" placeholder="选择职称" style="width: 100%">
            <el-option label="助教" value="助教"></el-option>
            <el-option label="讲师" value="讲师"></el-option>
            <el-option label="副教授" value="副教授"></el-option>
            <el-option label="教授" value="教授"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门" prop="department">
          <el-select v-model="form.department" placeholder="选择部门" style="width: 100%">
            <el-option label="计算机科学与技术学院" value="计算机科学与技术学院"></el-option>
            <el-option label="软件工程学院" value="软件工程学院"></el-option>
            <el-option label="信息工程学院" value="信息工程学院"></el-option>
            <el-option label="数学与统计学院" value="数学与统计学院"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 头像上传对话框 -->
    <el-dialog v-model="avatarDialogVisible" title="上传头像" width="400px">
      <div style="text-align: center">
        <el-avatar v-if="currentTeacher.avatarFileId" :src="`/api/file/stream/${currentTeacher.avatarFileId}`" :size="120" style="margin-bottom: 20px"></el-avatar>
        <el-avatar v-else :size="120" style="margin-bottom: 20px">{{ currentTeacher.teacherName?.charAt(0) }}</el-avatar>
        <div style="margin-bottom: 20px">
          <span style="color: #909399; font-size: 14px">{{ currentTeacher.teacherName }} ({{ currentTeacher.teacherNo }})</span>
        </div>
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :on-error="handleAvatarError"
          :before-upload="beforeAvatarUpload"
          accept="image/*"
        >
          <el-button type="primary">选择图片</el-button>
        </el-upload>
        <div style="margin-top: 10px; color: #909399; font-size: 12px">
          支持 JPG、PNG 格式，文件大小不超过 2MB
        </div>
      </div>
    </el-dialog>

    <!-- 教师课程列表对话框 -->
    <el-dialog v-model="coursesDialogVisible" title="教师课程列表" width="800px">
      <div style="margin-bottom: 10px">
        <span style="font-weight: bold">{{ currentTeacher.teacherName }}</span>
        <span style="color: #909399; margin-left: 10px">共 {{ teacherCourses.length }} 门课程</span>
      </div>
      <el-table :data="teacherCourses" border>
        <el-table-column prop="courseCode" label="课程编号" width="120"></el-table-column>
        <el-table-column prop="courseName" label="课程名称"></el-table-column>
        <el-table-column prop="courseType" label="课程类型" width="100"></el-table-column>
        <el-table-column prop="credit" label="学分" width="80"></el-table-column>
        <el-table-column prop="semester" label="学期" width="120"></el-table-column>
        <el-table-column label="选课情况" width="120">
          <template #default="{ row }">
            {{ row.enrolledStudents || 0 }} / {{ row.maxStudents || 0 }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import { getTeacherList, addTeacher, updateTeacher, deleteTeacher, uploadTeacherAvatar, getTeacherCourses } from '../api/teacher'

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  department: '',
  title: ''
})

// 分页
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const form = reactive({
  id: null,
  teacherName: '',
  teacherNo: '',
  title: '',
  department: '',
  phone: '',
  email: ''
})

// 表单验证规则
const rules = {
  teacherName: [{ required: true, message: '请输入教师姓名', trigger: 'blur' }],
  teacherNo: [{ required: true, message: '请输入教师工号', trigger: 'blur' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 头像上传
const avatarDialogVisible = ref(false)
const currentTeacher = ref({})
const uploadUrl = computed(() => `/api/teacher/avatar/upload?teacherId=${currentTeacher.value.id}`)
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

// 教师课程列表
const coursesDialogVisible = ref(false)
const teacherCourses = ref([])

// 获取教师列表
const fetchData = async () => {
  try {
    const params = {
      current: page.value,
      size: size.value,
      keyword: searchForm.keyword,
      department: searchForm.department,
      title: searchForm.title
    }
    const res = await getTeacherList(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取教师列表失败')
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchData()
}

// 添加教师
const handleAdd = () => {
  dialogTitle.value = '添加教师'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑教师
const handleEdit = (row) => {
  dialogTitle.value = '编辑教师'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    if (isEdit.value) {
      await updateTeacher(form)
      ElMessage.success('更新成功')
    } else {
      await addTeacher(form)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 删除教师
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该教师吗？如果教师有关联的课程将无法删除。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTeacher(id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}

// 上传头像
const handleUploadAvatar = (row) => {
  currentTeacher.value = row
  avatarDialogVisible.value = true
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('头像上传成功')
    avatarDialogVisible.value = false
    fetchData()
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 头像上传失败
const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

// 查看教师课程
const handleViewCourses = async (row) => {
  currentTeacher.value = row
  try {
    const res = await getTeacherCourses(row.id)
    if (res.code === 200) {
      teacherCourses.value = res.data
      coursesDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    teacherName: '',
    teacherNo: '',
    title: '',
    department: '',
    phone: '',
    email: ''
  })
  formRef.value?.clearValidate()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.teacher-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
}
</style>
