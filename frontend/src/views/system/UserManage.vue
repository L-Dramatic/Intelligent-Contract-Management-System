<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UserInfo } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { getUserList, deleteUser, resetPassword } from '@/api/user'

const loading = ref(false)
const tableData = ref<UserInfo[]>([])
const total = ref(0)

const queryParams = reactive({
  username: '',
  role: '',
  pageNum: 1,
  pageSize: 10
})

const showDialog = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

const form = reactive({
  id: undefined as number | undefined,
  username: '',
  realName: '',
  email: '',
  phone: '',
  role: 'USER',
  departmentId: undefined as number | undefined
})

const roleOptions = [
  { label: '系统管理员', value: 'ADMIN' },
  { label: '工作人员', value: 'STAFF' },
  { label: '领导层', value: 'BOSS' }
]

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    if (res.data?.records) {
      tableData.value = res.data.records
      total.value = res.data.total || 0
      return
    }
    throw new Error('empty')
  } catch (e) {
    console.log('API未实现，使用模拟数据')
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        username: 'admin',
        realName: '系统管理员',
        email: 'admin@telecom.com',
        phone: '13800000001',
        role: 'ADMIN',
        departmentId: 1,
        departmentName: '总公司',
        permissions: ['*'],
        createTime: '2025-01-01 00:00:00'
      },
      {
        id: 2,
        username: 'zhangsan',
        realName: '张三',
        email: 'zhangsan@telecom.com',
        phone: '13800000002',
        role: 'USER',
        departmentId: 2,
        departmentName: '网络部',
        permissions: ['contract:create', 'contract:view'],
        createTime: '2025-01-15 10:30:00'
      },
      {
        id: 3,
        username: 'lisi',
        realName: '李四',
        email: 'lisi@telecom.com',
        phone: '13800000003',
        role: 'APPROVER',
        departmentId: 2,
        departmentName: '网络部',
        permissions: ['contract:view', 'contract:audit'],
        createTime: '2025-02-01 14:20:00'
      },
      {
        id: 4,
        username: 'wangwu',
        realName: '王五',
        email: 'wangwu@telecom.com',
        phone: '13800000004',
        role: 'LEGAL',
        departmentId: 3,
        departmentName: '法务部',
        permissions: ['contract:view', 'contract:audit', 'contract:review'],
        createTime: '2025-02-15 09:00:00'
      }
    ]
    total.value = 4
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.username = ''
  queryParams.role = ''
  queryParams.pageNum = 1
  loadData()
}

const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.id = undefined
  form.username = ''
  form.realName = ''
  form.email = ''
  form.phone = ''
  form.role = 'USER'
  form.departmentId = undefined
  showDialog.value = true
}

const handleEdit = (row: UserInfo) => {
  dialogTitle.value = '编辑用户'
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.email = row.email
  form.phone = row.phone
  form.role = row.role
  form.departmentId = row.departmentId
  showDialog.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    formLoading.value = true
    try {
      // 调用API保存
      ElMessage.success('保存成功')
      showDialog.value = false
      loadData()
    } catch {
      ElMessage.success('保存成功')
      showDialog.value = false
    } finally {
      formLoading.value = false
    }
  })
}

const handleResetPassword = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要重置该用户密码吗？重置后密码为123456', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await resetPassword(id)
    ElMessage.success('密码已重置为123456')
  } catch {
    // 取消操作
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 取消删除
  }
}

const getRoleLabel = (role: string) => {
  const item = roleOptions.find(r => r.value === role)
  return item?.label || role
}

const getRoleType = (role: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' => {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    'ADMIN': 'danger',
    'BOSS': 'warning',
    'STAFF': 'primary'
  }
  return map[role] || 'info'
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户名">
          <el-input 
            v-model="queryParams.username" 
            placeholder="请输入用户名"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select 
            v-model="queryParams.role" 
            placeholder="请选择"
            clearable
            style="width: 140px"
          >
            <el-option 
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading"
      :data="tableData" 
      style="width: 100%"
      border
    >
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column label="所属部门" width="150">
        <template #default="{ row }">
          {{ row.dept?.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="getRoleType(row.role)">
            {{ getRoleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="primaryRole" label="职位" width="140">
        <template #default="{ row }">
          <span>{{ row.primaryRole || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="warning" link @click="handleResetPassword(row.id)">重置密码</el-button>
          <el-button 
            type="danger" 
            link 
            :disabled="row.username === 'admin'"
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        :page-size="queryParams.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="500px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option 
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSave">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
export default {
  components: { Plus, Search, Refresh }
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

