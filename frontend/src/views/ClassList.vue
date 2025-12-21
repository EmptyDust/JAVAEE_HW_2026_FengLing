<template>
  <div class="class-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">班级管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加班级
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="班级名称">
          <el-input
            v-model="searchForm.className"
            placeholder="请输入班级名称"
            clearable
            @keyup.enter="handleSearch"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="classNo" label="班级编号" width="120"></el-table-column>
        <el-table-column prop="className" label="班级名称"></el-table-column>
        <el-table-column prop="gradeId" label="年级ID" width="100"></el-table-column>
        <el-table-column prop="teacherId" label="班主任ID" width="120"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="班级编号" prop="classNo">
          <el-input v-model="form.classNo" placeholder="请输入班级编号"></el-input>
        </el-form-item>
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="form.className" placeholder="请输入班级名称"></el-input>
        </el-form-item>
        <el-form-item label="年级ID" prop="gradeId">
          <el-input-number v-model="form.gradeId" :min="1" placeholder="请输入年级ID"></el-input-number>
        </el-form-item>
        <el-form-item label="班主任ID" prop="teacherId">
          <el-input-number v-model="form.teacherId" :min="1" placeholder="请输入班主任ID"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getClassList, addClass, updateClass, deleteClass } from '../api/class'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加班级')
const formRef = ref()
const isEdit = ref(false)

const searchForm = reactive({
  className: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const form = reactive({
  id: null,
  classNo: '',
  className: '',
  gradeId: null,
  teacherId: null
})

const rules = {
  classNo: [
    { required: true, message: '请输入班级编号', trigger: 'blur' }
  ],
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' }
  ]
}

// 获取班级列表
const fetchClassList = async () => {
  loading.value = true
  try {
    const res = await getClassList({
      page: pagination.page,
      size: pagination.size,
      className: searchForm.className
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchClassList()
}

// 重置
const handleReset = () => {
  searchForm.className = ''
  pagination.page = 1
  fetchClassList()
}

// 添加
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加班级'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑班级'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该班级吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteClass(row.id)
      ElMessage.success('删除成功')
      fetchClassList()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// 提交
const handleSubmit = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateClass(form)
        ElMessage.success('更新成功')
      } else {
        await addClass(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchClassList()
    } catch (error) {
      // 错误已在拦截器中处理
    } finally {
      submitLoading.value = false
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  form.id = null
  form.classNo = ''
  form.className = ''
  form.gradeId = null
  form.teacherId = null
}

// 分页
const handleSizeChange = (val) => {
  pagination.size = val
  fetchClassList()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  fetchClassList()
}

onMounted(() => {
  fetchClassList()
})
</script>

<style scoped>
.class-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.search-form {
  margin-bottom: 20px;
}
</style>
