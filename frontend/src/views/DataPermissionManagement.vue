<template>
  <div class="data-permission-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据权限规则管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">添加规则</el-button>
            <el-button type="success" @click="handleRefreshCache">刷新缓存</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="角色类型">
          <el-select v-model="queryParams.roleType" placeholder="请选择角色" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="教师" value="teacher"></el-option>
            <el-option label="学生" value="student"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="表名">
          <el-input v-model="queryParams.tableName" placeholder="请输入表名" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="ruleList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="roleType" label="角色类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.roleType === 'admin'" type="danger">管理员</el-tag>
            <el-tag v-else-if="row.roleType === 'teacher'" type="warning">教师</el-tag>
            <el-tag v-else-if="row.roleType === 'student'" type="success">学生</el-tag>
            <span v-else>{{ row.roleType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tableName" label="表名" width="150"></el-table-column>
        <el-table-column prop="entityClass" label="实体类" width="150"></el-table-column>
        <el-table-column prop="filterField" label="过滤字段" width="120"></el-table-column>
        <el-table-column prop="filterOperator" label="操作符" width="80"></el-table-column>
        <el-table-column prop="contextField" label="上下文字段" width="120"></el-table-column>
        <el-table-column prop="filterType" label="过滤类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.filterType === 'SIMPLE'" type="info">简单条件</el-tag>
            <el-tag v-else-if="row.filterType === 'SUBQUERY'" type="warning">子查询</el-tag>
            <span v-else>{{ row.filterType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggle(row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="pagination"
      ></el-pagination>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="角色类型" prop="roleType">
          <el-select v-model="form.roleType" placeholder="请选择角色">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="教师" value="teacher"></el-option>
            <el-option label="学生" value="student"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="表名" prop="tableName">
          <el-input v-model="form.tableName" placeholder="请输入表名"></el-input>
        </el-form-item>
        <el-form-item label="实体类名" prop="entityClass">
          <el-input v-model="form.entityClass" placeholder="请输入实体类名"></el-input>
        </el-form-item>
        <el-form-item label="过滤字段" prop="filterField">
          <el-input v-model="form.filterField" placeholder="例如: student_id"></el-input>
        </el-form-item>
        <el-form-item label="过滤操作符" prop="filterOperator">
          <el-select v-model="form.filterOperator" placeholder="请选择操作符">
            <el-option label="=" value="="></el-option>
            <el-option label="IN" value="IN"></el-option>
            <el-option label=">" value=">"></el-option>
            <el-option label="<" value="<"></el-option>
            <el-option label=">=" value=">="></el-option>
            <el-option label="<=" value="<="></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="上下文字段" prop="contextField">
          <el-select v-model="form.contextField" placeholder="请选择上下文字段">
            <el-option label="userId" value="userId"></el-option>
            <el-option label="studentId" value="studentId"></el-option>
            <el-option label="teacherId" value="teacherId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="过滤类型" prop="filterType">
          <el-radio-group v-model="form.filterType">
            <el-radio label="SIMPLE">简单条件</el-radio>
            <el-radio label="SUBQUERY">子查询</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="子查询SQL" prop="subquerySql" v-if="form.filterType === 'SUBQUERY'">
          <el-input
            v-model="form.subquerySql"
            type="textarea"
            :rows="3"
            placeholder="例如: SELECT id FROM course_info WHERE teacher_id = ?"
          ></el-input>
        </el-form-item>
        <el-form-item label="规则描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入规则描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="是否启用" prop="enabled">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listDataPermissionRules,
  addDataPermissionRule,
  updateDataPermissionRule,
  deleteDataPermissionRule,
  toggleDataPermissionRule,
  refreshCache
} from '../api/dataPermission'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleType: '',
  tableName: ''
})

// 数据列表
const ruleList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

// 表单数据
const form = reactive({
  id: null,
  roleType: '',
  tableName: '',
  entityClass: '',
  filterField: '',
  filterOperator: '=',
  contextField: '',
  filterType: 'SIMPLE',
  subquerySql: '',
  description: '',
  enabled: 1
})

// 表单验证规则
const rules = {
  roleType: [{ required: true, message: '请选择角色类型', trigger: 'change' }],
  tableName: [{ required: true, message: '请输入表名', trigger: 'blur' }],
  filterField: [{ required: true, message: '请输入过滤字段', trigger: 'blur' }],
  filterOperator: [{ required: true, message: '请选择过滤操作符', trigger: 'change' }],
  contextField: [{ required: true, message: '请选择上下文字段', trigger: 'change' }],
  filterType: [{ required: true, message: '请选择过滤类型', trigger: 'change' }]
}

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listDataPermissionRules(queryParams)
    if (res.code === 200) {
      ruleList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置按钮
const handleReset = () => {
  queryParams.roleType = ''
  queryParams.tableName = ''
  queryParams.pageNum = 1
  getList()
}

// 添加按钮
const handleAdd = () => {
  dialogTitle.value = '添加权限规则'
  resetForm()
  dialogVisible.value = true
}

// 编辑按钮
const handleEdit = (row) => {
  dialogTitle.value = '编辑权限规则'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除按钮
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该权限规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteDataPermissionRule(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 启用/禁用切换
const handleToggle = async (row) => {
  try {
    const res = await toggleDataPermissionRule(row.id)
    if (res.code === 200) {
      ElMessage.success(row.enabled === 1 ? '已启用' : '已禁用')
      getList()
    } else {
      ElMessage.error(res.message || '操作失败')
      row.enabled = row.enabled === 1 ? 0 : 1
    }
  } catch (error) {
    ElMessage.error('操作失败')
    row.enabled = row.enabled === 1 ? 0 : 1
  }
}

// 刷新缓存
const handleRefreshCache = async () => {
  try {
    const res = await refreshCache()
    if (res.code === 200) {
      ElMessage.success('缓存刷新成功')
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const api = form.id ? updateDataPermissionRule : addDataPermissionRule
        const res = await api(form)
        if (res.code === 200) {
          ElMessage.success(form.id ? '修改成功' : '添加成功')
          dialogVisible.value = false
          getList()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.roleType = ''
  form.tableName = ''
  form.entityClass = ''
  form.filterField = ''
  form.filterOperator = '='
  form.contextField = ''
  form.filterType = 'SIMPLE'
  form.subquerySql = ''
  form.description = ''
  form.enabled = 1
}

// 对话框关闭
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  resetForm()
}

// 页面加载
onMounted(() => {
  getList()
})
</script>

<style scoped>
.data-permission-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
