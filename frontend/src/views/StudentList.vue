<template>
  <div class="student-list">
    <div class="toolbar">
      <el-input
        v-model="searchForm.name"
        placeholder="学生姓名"
        style="width: 200px"
        clearable
        @keyup.enter="handleSearch"
      ></el-input>
      <el-select
        v-model="searchForm.classId"
        placeholder="选择班级"
        style="width: 200px"
        clearable
        @keyup.enter="handleSearch"
      >
        <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button type="success" @click="handleAdd">添加学生</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="头像" width="80">
        <template #default="{ row }">
          <el-avatar v-if="row.avatarFileId" :src="`/api/file/stream/${row.avatarFileId}`" :size="50"></el-avatar>
          <el-avatar v-else :size="50">{{ row.name?.charAt(0) }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="姓名"></el-table-column>
      <el-table-column prop="studentNo" label="学号"></el-table-column>
      <el-table-column prop="className" label="班级"></el-table-column>
      <el-table-column prop="gender" label="性别"></el-table-column>
      <el-table-column prop="age" label="年龄"></el-table-column>
      <el-table-column prop="phone" label="电话"></el-table-column>
      <el-table-column prop="address" label="地址" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" size="small" @click="handleUploadAvatar(row)">头像</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号"></el-input>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" placeholder="选择班级" style="width: 100%">
            <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio v-for="item in genderList" :key="item.dictCode" :label="item.dictValue"></el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="1" :max="120"></el-input-number>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入地址"></el-input>
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
        <el-avatar v-if="currentStudent.avatarFileId" :src="`/api/file/stream/${currentStudent.avatarFileId}`" :size="120" style="margin-bottom: 20px"></el-avatar>
        <el-avatar v-else :size="120" style="margin-bottom: 20px">{{ currentStudent.name?.charAt(0) }}</el-avatar>
        <div style="margin-bottom: 20px">
          <span style="color: #909399; font-size: 14px">{{ currentStudent.name }} ({{ currentStudent.studentNo }})</span>
        </div>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          :show-file-list="false"
          accept="image/*"
        >
          <el-button type="primary">选择图片</el-button>
        </el-upload>
        <div v-if="selectedFile" style="margin-top: 10px; color: #67c23a">
          已选择: {{ selectedFile.name }}
        </div>
        <div style="margin-top: 10px; color: #909399; font-size: 12px">
          支持 JPG、PNG 格式，文件大小不超过 5MB
        </div>
      </div>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAvatarUpload" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, addStudent, updateStudent, deleteStudent, getClassList, getDictByType, uploadAvatar } from '../api/student'

const searchForm = ref({ name: '', classId: '' })
const tableData = ref([])
const classList = ref([])
const genderList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = ref({})
const isEdit = ref(false)

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

// 头像上传相关
const avatarDialogVisible = ref(false)
const currentStudent = ref({})
const selectedFile = ref(null)
const uploading = ref(false)
const uploadRef = ref(null)

const fetchData = async () => {
  try {
    const res = await getStudentList({
      page: page.value,
      size: size.value,
      name: searchForm.value.name,
      classId: searchForm.value.classId
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取学生列表失败')
  }
}

const fetchClassList = async () => {
  try {
    const res = await getClassList({ page: 1, size: 100 })
    classList.value = res.data.records
  } catch (error) {
    ElMessage.error('获取班级列表失败')
  }
}

const fetchGenderList = async () => {
  try {
    const res = await getDictByType('gender')
    genderList.value = res.data
  } catch (error) {
    ElMessage.error('获取字典失败')
  }
}

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加学生'
  form.value = { name: '', studentNo: '', classId: '', gender: '', age: 18, phone: '', address: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑学生'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await updateStudent(form.value)
        ElMessage.success('更新成功')
      } else {
        await addStudent(form.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该学生吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteStudent(id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// 打开头像上传对话框
const handleUploadAvatar = (row) => {
  currentStudent.value = { ...row }
  selectedFile.value = null
  avatarDialogVisible.value = true
}

// 处理文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

// 执行头像上传
const handleAvatarUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  // 验证文件类型
  if (!selectedFile.value.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }

  // 验证文件大小（5MB）
  if (selectedFile.value.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  uploading.value = true
  try {
    const res = await uploadAvatar(selectedFile.value, currentStudent.value.id)
    if (res.code === 200) {
      ElMessage.success('头像上传成功')
      avatarDialogVisible.value = false
      fetchData() // 刷新列表
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchClassList()
  fetchGenderList()
})
</script>

<style scoped>
.student-list {
  width: 100%;
}

.toolbar {
  display: flex;
  gap: 10px;
}
</style>
