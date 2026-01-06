<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UserInfo, Department } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { getUserList, deleteUser, resetPassword, createUser, updateUser } from '@/api/user'
import { getDepartmentTree } from '@/api/department'
import { getRoleList } from '@/api/role'
import type { Role } from '@/api/role'

const loading = ref(false)
const tableData = ref<UserInfo[]>([])
const total = ref(0)

const queryParams = reactive({
  username: '',
  role: '',  // 默认不筛选角色
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
  role: 'CITY',  // 默认市级员工
  departmentId: undefined as number | undefined,
  primaryRole: undefined as string | undefined
})

// 选中的部门信息
const selectedDept = ref<Department | null>(null)

// 根据部门自动计算人员级别
const autoRole = computed(() => {
  if (!selectedDept.value) return ''
  const dept = selectedDept.value
  const level = dept.level
  const type = dept.type
  const code = dept.code || ''
  
  // 根据部门级别确定 role（优先按 level 判断）
  // level 4 = 县级职能部门
  if (level === 4) return 'COUNTY'
  // level 3 且 type 是 COUNTY = 县级分公司本身
  if (level === 3 && type === 'COUNTY') return 'COUNTY'
  // level 3 且是市级职能部门
  if (level === 3 && type === 'DEPT') return 'CITY'
  // level 2 且 type 是 CITY = 市级分公司
  if (level === 2 && type === 'CITY') return 'CITY'
  // level 2 且是省级职能部门
  if (level === 2 && type === 'DEPT') return 'PROVINCE'
  // level 1 或 type 是 PROVINCE = 省公司
  if (level === 1 || type === 'PROVINCE') return 'PROVINCE'
  
  // 根据部门代码判断
  if (code.startsWith('COUNTY-')) return 'COUNTY'
  if (code.startsWith('CITY-')) return 'CITY'
  if (code.startsWith('PROV-')) return 'PROVINCE'
  
  return 'CITY'  // 默认市级
})

// 人员级别的显示名称
const autoRoleLabel = computed(() => {
  const roleMap: Record<string, string> = {
    'PROVINCE': '省级员工',
    'CITY': '市级员工',
    'COUNTY': '县级员工',
    'ADMIN': '系统管理员',
    'BOSS': '领导层'
  }
  return autoRole.value ? roleMap[autoRole.value] || autoRole.value : '请先选择部门'
})

const departmentTree = ref<Department[]>([])
const roleList = ref<Role[]>([])
const departmentProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}

const roleOptions = [
  { label: '系统管理员', value: 'ADMIN' },
  { label: '领导层', value: 'BOSS' },
  { label: '省级员工', value: 'PROVINCE' },
  { label: '市级员工', value: 'CITY' },
  { label: '县级员工', value: 'COUNTY' }
]

// 部门类型与职位的映射关系
const deptTypeToRoles: Record<string, string[]> = {
  // 省级部门
  'PROV-NET': ['DEPT_MANAGER', 'PROJECT_MANAGER', 'RF_ENGINEER', 'NETWORK_ENGINEER', 'NETWORK_PLANNING', 'BROADBAND_SPECIALIST', 'OPS_CENTER', 'DESIGN_REVIEWER', 'SITE_ACQUISITION'],
  'PROV-MKT': ['DEPT_MANAGER', 'CUSTOMER_SERVICE_LEAD'],
  'PROV-GOV': ['DEPT_MANAGER', 'SOLUTION_ARCHITECT', 'DICT_PM', 'TECHNICAL_LEAD', 'SECURITY_REVIEWER', 'IT_ARCHITECT'],
  'PROV-LEGAL': ['DEPT_MANAGER', 'LEGAL_REVIEWER'],
  'PROV-FIN': ['DEPT_MANAGER', 'COST_AUDITOR', 'FINANCE_RECEIVABLE'],
  'PROV-ADMIN': ['DEPT_MANAGER', 'FACILITY_COORDINATOR'],
  // 市级部门
  'NET': ['DEPT_MANAGER', 'PROJECT_MANAGER', 'RF_ENGINEER', 'NETWORK_ENGINEER', 'NETWORK_PLANNING', 'BROADBAND_SPECIALIST', 'OPS_CENTER', 'DESIGN_REVIEWER', 'SITE_ACQUISITION'],
  'MKT': ['DEPT_MANAGER', 'CUSTOMER_SERVICE_LEAD'],
  'GOV': ['DEPT_MANAGER', 'SOLUTION_ARCHITECT', 'DICT_PM', 'TECHNICAL_LEAD', 'SECURITY_REVIEWER', 'IT_ARCHITECT'],
  'LEGAL': ['DEPT_MANAGER', 'LEGAL_REVIEWER'],
  'FIN': ['DEPT_MANAGER', 'COST_AUDITOR', 'FINANCE_RECEIVABLE'],
  'ADMIN': ['DEPT_MANAGER', 'FACILITY_COORDINATOR'],
  'PROC': ['DEPT_MANAGER', 'PROCUREMENT_SPECIALIST', 'VENDOR_MANAGER'],
  // 市公司级别（统一使用部门经理）
  'CITY': ['DEPT_MANAGER'],
  // 县级部门职位（县级员工显示实际职位，但在审批流程中自动识别为发起人）
  'COUNTY': ['DEPT_MANAGER'],  // 县级分公司经理
  'COUNTY-NET': ['DEPT_MANAGER', 'NETWORK_ENGINEER', 'PROJECT_MANAGER'],  // 县级网络部
  'COUNTY-MKT': ['DEPT_MANAGER', 'CUSTOMER_SERVICE_LEAD'],  // 县级市场部
  'COUNTY-GOV': ['DEPT_MANAGER', 'DICT_PM'],  // 县级政企部
  'COUNTY-ADMIN': ['DEPT_MANAGER', 'FACILITY_COORDINATOR'],  // 县级综合部
  // 省公司（统一使用部门经理）
  'PROVINCE': ['SYSTEM_ADMIN', 'DEPT_MANAGER']
}

// 从部门代码中提取类型关键字
const extractDeptType = (dept: Department | null): string | null => {
  if (!dept) return null
  const code = dept.code || ''
  
  // 如果是省/市/县公司本身
  if (dept.type === 'PROVINCE') return 'PROVINCE'
  if (dept.type === 'CITY') return 'CITY'
  if (dept.type === 'COUNTY') return 'COUNTY'
  
  // 省级部门（如 PROV-NET, PROV-LEGAL）
  if (code.startsWith('PROV-')) return code
  
  // 县级部门（如 COUNTY-C-NET 提取 COUNTY-NET）
  if (code.startsWith('COUNTY-')) {
    const parts = code.split('-')
    if (parts.length >= 3) {
      return `COUNTY-${parts[2]}`  // COUNTY-C-NET -> COUNTY-NET
    }
    return 'COUNTY'
  }
  
  // 市级部门，提取关键字（如 CITY-A-NET 提取 NET）
  const parts = code.split('-')
  if (parts.length >= 2) {
    return parts[parts.length - 1]
  }
  
  return null
}

// 根据选中部门过滤职位列表（只显示该部门实际存在的职位）
const filteredRoleList = computed(() => {
  const deptType = extractDeptType(selectedDept.value)
  
  if (!deptType || !roleList.value.length) {
    return {
      recommended: [] as Role[],
      others: [] as Role[]  // 不显示其他职位
    }
  }
  
  const allowedCodes = deptTypeToRoles[deptType] || []
  
  // 只返回该部门允许的职位，不显示其他职位
  const recommended = roleList.value.filter(r => allowedCodes.includes(r.roleCode))
  
  return { 
    recommended, 
    others: [] as Role[]  // 不显示其他职位，只显示该部门实际存在的职位
  }
})

// 从部门树中查找部门
const findDeptById = (tree: Department[], id: number): Department | null => {
  for (const dept of tree) {
    if (dept.id === id) return dept
    if (dept.children?.length) {
      const found = findDeptById(dept.children, id)
      if (found) return found
    }
  }
  return null
}

// 监听部门选择变化
watch(() => form.departmentId, (newId) => {
  if (newId && departmentTree.value.length) {
    selectedDept.value = findDeptById(departmentTree.value, newId)
    // 自动根据部门设置 role
    form.role = autoRole.value
    // 如果当前选择的职位不在推荐列表中，清空职位选择
    const recommended = filteredRoleList.value.recommended.map(r => r.roleCode)
    if (form.primaryRole && !recommended.includes(form.primaryRole)) {
      form.primaryRole = undefined  // 清空不匹配的职位
    }
  } else {
    selectedDept.value = null
    form.role = ''
  }
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  primaryRole: [{ required: true, message: '请选择职位', trigger: 'change' }]
}

onMounted(() => {
  loadData()
  loadDepartments()
  loadRoles()
})

const loadDepartments = async () => {
  try {
    const res = await getDepartmentTree()
    if (res.data && res.data.length > 0) {
      departmentTree.value = res.data
      return
    }
    throw new Error('empty')
  } catch (e) {
    console.log('部门API未实现，使用空数据')
    departmentTree.value = []
  }
}

const loadRoles = async () => {
  try {
    const res = await getRoleList()
    if (res.data && res.data.length > 0) {
      roleList.value = res.data
      return
    }
    throw new Error('empty')
  } catch (e) {
    console.log('角色API未实现，使用空数据')
    roleList.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    console.log('查询参数:', queryParams)
    const res = await getUserList(queryParams)
    if (res.data?.records) {
      tableData.value = res.data.records
      total.value = res.data.total || 0
      return
    }
    throw new Error('empty')
  } catch (e) {
    console.log('API未实现，使用模拟数据')
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
      }
    ]
    total.value = 1
  } finally {
    loading.value = false
  }
}

const handleRoleChange = (value: string) => {
  queryParams.pageNum = 1
  loadData()
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
  form.role = ''  // 根据部门自动确定
  form.departmentId = undefined
  form.primaryRole = undefined
  selectedDept.value = null
  showDialog.value = true
}

const handleEdit = (row: UserInfo) => {
  dialogTitle.value = '编辑用户'
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.email = row.email
  form.phone = row.phone || row.mobile
  form.role = row.role
  form.departmentId = row.departmentId || row.deptId
  form.primaryRole = (row as any).primaryRole
  // 查找选中的部门
  if (form.departmentId && departmentTree.value.length) {
    selectedDept.value = findDeptById(departmentTree.value, form.departmentId)
  }
  showDialog.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    formLoading.value = true
    try {
      const userData: any = {
        username: form.username,
        realName: form.realName,
        email: form.email,
        phone: form.phone,
        mobile: form.phone,
        role: form.role,
        deptId: form.departmentId,
        departmentId: form.departmentId,
        primaryRole: form.primaryRole,
        isActive: 1
      }
      
      if (form.id) {
        await updateUser(form.id, userData)
        ElMessage.success('更新成功')
      } else {
        userData.password = '123456'
        await createUser(userData)
        ElMessage.success('创建成功，默认密码为123456')
      }
      
      showDialog.value = false
      loadData()
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败')
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
    'PROVINCE': 'success',
    'CITY': 'primary',
    'COUNTY': 'info'
  }
  return map[role] || 'info'
}

// 获取职位显示名称
const getPrimaryRoleLabel = (roleCode: string) => {
  const role = roleList.value.find(r => r.roleCode === roleCode)
  return role?.roleName || roleCode
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
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="150" show-overflow-tooltip />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column label="所属部门" width="150">
        <template #default="{ row }">
          {{ row.dept?.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="getRoleType(row.role)" size="small">
            {{ getRoleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="primaryRole" label="职位" width="140">
        <template #default="{ row }">
          <span>{{ getPrimaryRoleLabel(row.primaryRole) || '-' }}</span>
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
    <el-dialog v-model="showDialog" :title="dialogTitle" width="550px">
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
        <el-form-item label="人员级别">
          <el-input :value="autoRoleLabel" disabled placeholder="根据部门自动确定">
            <template #suffix>
              <el-tooltip content="人员级别根据所选部门自动确定" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
          <div class="form-tip">自动根据部门级别确定：省级部门→省级员工，市级部门→市级员工，县级部门→县级员工</div>
        </el-form-item>
        <el-form-item label="所属部门" prop="departmentId">
          <el-tree-select
            v-model="form.departmentId"
            :data="departmentTree"
            :props="departmentProps"
            placeholder="请先选择部门（职位会自动过滤）"
            style="width: 100%"
            check-strictly
            :render-after-expand="false"
          />
          <div v-if="selectedDept" class="form-tip">
            已选择：{{ selectedDept.name }} ({{ selectedDept.type || selectedDept.code }})
          </div>
        </el-form-item>
        <el-form-item label="职位" prop="primaryRole">
          <el-select 
            v-model="form.primaryRole" 
            placeholder="请先选择部门" 
            style="width: 100%"
            filterable
            :disabled="!form.departmentId"
          >
            <!-- 只显示该部门实际存在的职位 -->
            <el-option 
              v-for="role in filteredRoleList.recommended"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            >
              <div class="role-option">
                <span class="role-name">{{ role.roleName }}</span>
                <span class="role-code">{{ role.roleCode }}</span>
              </div>
            </el-option>
          </el-select>
          <div v-if="!form.departmentId" class="form-tip warning">
            ⚠️ 请先选择部门，职位列表会根据部门自动过滤
          </div>
          <div v-else-if="filteredRoleList.recommended.length === 0" class="form-tip warning">
            ⚠️ 该部门暂无可用职位，请检查部门配置
          </div>
          <div v-else class="form-tip success">
            ✅ 已根据「{{ selectedDept?.name }}」显示可用职位（{{ filteredRoleList.recommended.length }}个）
          </div>
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
import { Plus, Search, Refresh, InfoFilled } from '@element-plus/icons-vue'
export default {
  components: { Plus, Search, Refresh, InfoFilled }
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.form-tip.warning {
  color: #e6a23c;
}

.form-tip.success {
  color: #67c23a;
}

.role-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.role-option .role-name {
  font-weight: 500;
}

.role-option .role-code {
  font-size: 12px;
  color: #909399;
}

.role-option.other .role-name {
  color: #909399;
}
</style>
