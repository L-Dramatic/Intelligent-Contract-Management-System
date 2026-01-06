<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, View, Switch, Plus, Delete, ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface ScenarioNode {
  id?: number
  scenarioId: string
  nodeOrder: number
  roleCode: string
  roleName?: string
  nodeLevel: string
  actionType: string
  isMandatory: number
  canSkip: number
}

interface Scenario {
  id?: number
  scenarioId?: string
  subTypeCode: string
  subTypeName: string
  amountMin: number | null
  amountMax: number | null
  isActive: number
  isFastTrack: number
  description: string
  nodes?: ScenarioNode[]
}

const loading = ref(false)
const scenarios = ref<Scenario[]>([])

// 节点查看/编辑弹窗
const nodesDialogVisible = ref(false)
const nodesLoading = ref(false)
const currentScenario = ref<Scenario | null>(null)
const currentNodes = ref<ScenarioNode[]>([])
const isEditingNodes = ref(false)
const nodesSaving = ref(false)

// 编辑场景弹窗
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = ref<Partial<Scenario>>({})

// 新建场景弹窗
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createForm = ref<Partial<Scenario>>({
  subTypeCode: '',
  subTypeName: '',
  amountMin: 0,
  amountMax: null,
  isFastTrack: 0,
  description: ''
})

// 添加节点弹窗
const addNodeDialogVisible = ref(false)
const newNode = ref<Partial<ScenarioNode>>({
  roleCode: '',
  nodeLevel: 'CITY',
  actionType: 'APPROVE',
  isMandatory: 1,
  canSkip: 0
})

// 合同类型选项
const contractTypeOptions = [
  { label: 'A1 - 土建工程', value: 'A1' },
  { label: 'A2 - 装修工程', value: 'A2' },
  { label: 'A3 - 零星维修', value: 'A3' },
  { label: 'B1 - 光缆代维', value: 'B1' },
  { label: 'B2 - 基站代维', value: 'B2' },
  { label: 'B3 - 家宽代维', value: 'B3' },
  { label: 'B4 - 应急保障', value: 'B4' },
  { label: 'C1 - 定制开发', value: 'C1' },
  { label: 'C2 - 商用软件采购', value: 'C2' },
  { label: 'C3 - DICT集成', value: 'C3' },
  { label: 'D1 - 物业租赁', value: 'D1' },
  { label: 'CUSTOM - 自定义类型', value: 'CUSTOM' }
]

// 角色选项（按部门类型分组）
// deptType: 对应的部门类型代码，null表示通用角色
const roleOptions = [
  // 通用角色（适用于所有部门）
  { label: '合同发起人', value: 'INITIATOR', deptType: null, category: '通用' },
  { label: '部门经理', value: 'DEPT_MANAGER', deptType: null, category: '通用' },
  { label: '副总经理', value: 'VICE_PRESIDENT', deptType: null, category: '管理层' },
  { label: '总经理', value: 'GENERAL_MANAGER', deptType: null, category: '管理层' },
  { label: '三重一大会议', value: 'T1M', deptType: null, category: '管理层' },
  
  // 网络部角色
  { label: '项目经理', value: 'PROJECT_MANAGER', deptType: 'NET', category: '网络部' },
  { label: '射频工程师', value: 'RF_ENGINEER', deptType: 'NET', category: '网络部' },
  { label: '网络工程师', value: 'NETWORK_ENGINEER', deptType: 'NET', category: '网络部' },
  { label: '网络规划', value: 'NETWORK_PLANNING', deptType: 'NET', category: '网络部' },
  { label: '家宽专家', value: 'BROADBAND_SPECIALIST', deptType: 'NET', category: '网络部' },
  { label: '运营中心', value: 'OPS_CENTER', deptType: 'NET', category: '网络部' },
  { label: '设计审查', value: 'DESIGN_REVIEWER', deptType: 'NET', category: '网络部' },
  { label: '站点获取', value: 'SITE_ACQUISITION', deptType: 'NET', category: '网络部' },
  
  // 市场部角色
  { label: '客服主管', value: 'CUSTOMER_SERVICE_LEAD', deptType: 'MKT', category: '市场部' },
  
  // 政企部角色
  { label: 'IT技术负责人', value: 'TECHNICAL_LEAD', deptType: 'GOV', category: '政企部' },
  { label: 'IT安全审查', value: 'SECURITY_REVIEWER', deptType: 'GOV', category: '政企部' },
  { label: 'IT架构师', value: 'IT_ARCHITECT', deptType: 'GOV', category: '政企部' },
  { label: '解决方案架构师', value: 'SOLUTION_ARCHITECT', deptType: 'GOV', category: '政企部' },
  { label: 'DICT项目经理', value: 'DICT_PM', deptType: 'GOV', category: '政企部' },
  
  // 法务部角色
  { label: '法务审查员', value: 'LEGAL_REVIEWER', deptType: 'LEGAL', category: '法务部' },
  
  // 财务部角色
  { label: '成本审计员', value: 'COST_AUDITOR', deptType: 'FIN', category: '财务部' },
  { label: '财务应收检查', value: 'FINANCE_RECEIVABLE', deptType: 'FIN', category: '财务部' },
  
  // 综合部角色
  { label: '设施协调员', value: 'FACILITY_COORDINATOR', deptType: 'ADMIN', category: '综合部' },
  
  // 采购部角色
  { label: '采购专员', value: 'PROCUREMENT_SPECIALIST', deptType: 'PROC', category: '采购部' },
  { label: '供应商管理', value: 'VENDOR_MANAGER', deptType: 'PROC', category: '采购部' }
]

// 各级部门配置
// 县级只负责发起合同，不参与审批，所以不需要部门分类
const deptTypesByLevel = {
  PROVINCE: [
    { code: 'NET', name: '省公司网络部' },
    { code: 'MKT', name: '省公司市场部' },
    { code: 'GOV', name: '省公司政企部' },
    { code: 'LEGAL', name: '省公司法务部' },
    { code: 'FIN', name: '省公司财务部' },
    { code: 'ADMIN', name: '省公司综合部' }
  ],
  CITY: [
    { code: 'NET', name: '网络部' },
    { code: 'MKT', name: '市场经营部' },
    { code: 'GOV', name: '政企客户部' },
    { code: 'LEGAL', name: '法务部' },
    { code: 'FIN', name: '财务部' },
    { code: 'ADMIN', name: '综合部' },
    { code: 'PROC', name: '采购部' }
  ],
  COUNTY: [] // 县级只有发起人，无需部门分类
}

// 获取指定级别可用的所有角色代码
const getValidRolesForLevel = (level: string): string[] => {
  // 县级只能是发起人
  if (level === 'COUNTY') {
    return ['INITIATOR']
  }
  
  const deptTypes = deptTypesByLevel[level as keyof typeof deptTypesByLevel] || deptTypesByLevel.CITY
  const validDeptCodes = deptTypes.map(d => d.code)
  
  const validRoles: string[] = []
  
  // 通用角色（市级/省级可用）
  roleOptions.filter(r => r.category === '通用').forEach(r => validRoles.push(r.value))
  
  // 管理层角色
  roleOptions.filter(r => r.category === '管理层').forEach(r => validRoles.push(r.value))
  
  // 该级别部门对应的角色
  roleOptions.filter(r => r.deptType && validDeptCodes.includes(r.deptType)).forEach(r => validRoles.push(r.value))
  
  return validRoles
}

// 检查角色在指定级别是否可用
const isRoleValidForLevel = (roleCode: string, level: string): boolean => {
  return getValidRolesForLevel(level).includes(roleCode)
}

// 处理节点级别变更（编辑模式）
const handleNodeLevelChange = (node: ScenarioNode) => {
  // 县级自动设置为发起人 + 发起动作
  if (node.nodeLevel === 'COUNTY') {
    node.roleCode = 'INITIATOR'
    node.actionType = 'INITIATE'
    ElMessage.info('县级节点已自动设置为"合同发起人"')
    return
  }
  
  // 其他级别检查角色是否有效
  if (node.roleCode && !isRoleValidForLevel(node.roleCode, node.nodeLevel)) {
    node.roleCode = ''
    ElMessage.warning('该角色在此级别不可用，请重新选择')
  }
}

// 处理新节点级别变更
const handleNewNodeLevelChange = () => {
  const level = newNode.value.nodeLevel || 'CITY'
  
  // 县级自动设置为发起人 + 发起动作
  if (level === 'COUNTY') {
    newNode.value.roleCode = 'INITIATOR'
    newNode.value.actionType = 'INITIATE'
    ElMessage.info('县级节点已自动设置为"合同发起人"')
    return
  }
  
  // 其他级别检查角色是否有效
  if (newNode.value.roleCode && !isRoleValidForLevel(newNode.value.roleCode, level)) {
    newNode.value.roleCode = ''
    ElMessage.warning('该角色在此级别不可用，请重新选择')
  }
}

// 获取指定级别的角色分组
const getRoleGroupsByLevel = (level: string) => {
  // 县级只能是发起人，不显示其他角色
  if (level === 'COUNTY') {
    const initiatorRole = roleOptions.filter(r => r.value === 'INITIATOR')
    return [{ name: '📝 县级发起', roles: initiatorRole }]
  }
  
  const deptTypes = deptTypesByLevel[level as keyof typeof deptTypesByLevel] || deptTypesByLevel.CITY
  const groups: { name: string; roles: typeof roleOptions }[] = []
  
  // 添加管理层角色
  const managementRoles = roleOptions.filter(r => r.category === '管理层')
  if (managementRoles.length > 0) {
    groups.push({ name: '🏢 管理层', roles: managementRoles })
  }
  
  // 添加通用角色（不包括INITIATOR，因为发起通常是县级）
  const commonRoles = roleOptions.filter(r => r.category === '通用' && r.value !== 'INITIATOR')
  if (commonRoles.length > 0) {
    groups.push({ name: '📋 通用角色', roles: commonRoles })
  }
  
  // 按部门类型添加角色
  deptTypes.forEach(dept => {
    const deptRoles = roleOptions.filter(r => r.deptType === dept.code)
    if (deptRoles.length > 0) {
      groups.push({ name: `🏛️ ${dept.name}`, roles: deptRoles })
    }
  })
  
  return groups
}

// 级别选项
const levelOptions = [
  { label: '县级（仅发起）', value: 'COUNTY', hint: '县级只能作为合同发起节点' },
  { label: '市级', value: 'CITY', hint: '市级部门审批' },
  { label: '省级', value: 'PROVINCE', hint: '省级部门审批' }
]

// 动作类型选项（简化版：只有发起和审批）
const actionTypeOptions = [
  { label: '发起', value: 'INITIATE' },
  { label: '审批', value: 'APPROVE' }
]

// 角色名称映射
const roleNameMap = computed(() => {
  const map: Record<string, string> = {}
  roleOptions.forEach(opt => {
    map[opt.value] = opt.label
  })
  return map
})

// 级别名称映射
const levelNameMap: Record<string, string> = {
  'COUNTY': '县级',
  'CITY': '市级',
  'PROVINCE': '省级'
}

// 动作类型映射（兼容旧数据，统一显示为审批）
const actionTypeMap: Record<string, string> = {
  'INITIATE': '发起',
  'REVIEW': '审批',      // 旧数据兼容
  'VERIFY': '审批',      // 旧数据兼容
  'APPROVE': '审批',
  'FINAL_APPROVE': '审批' // 旧数据兼容
}

// 加载审批场景列表
const loadScenarios = async () => {
  loading.value = true
  try {
    const res = await request.get('/workflow/scenario/scenarios')
    scenarios.value = res.data || []
  } catch (error) {
    console.error('加载场景失败', error)
    ElMessage.error('加载场景配置失败')
  } finally {
    loading.value = false
  }
}

// 格式化金额
const formatAmount = (amount: number | null) => {
  if (amount === null || amount === undefined) return '无限制'
  if (amount >= 10000) {
    return `¥${(amount / 10000).toFixed(0)}万`
  }
  return `¥${amount}`
}

// 查看/编辑节点
const viewNodes = async (row: Scenario) => {
  currentScenario.value = row
  nodesDialogVisible.value = true
  nodesLoading.value = true
  isEditingNodes.value = false
  
  try {
    const res = await request.get(`/workflow/scenario/scenarios/${row.scenarioId}`)
    currentNodes.value = res.data?.nodes || []
  } catch (error) {
    console.error('获取节点失败', error)
    ElMessage.error('获取审批节点失败')
    currentNodes.value = []
  } finally {
    nodesLoading.value = false
  }
}

// 编辑场景
const editScenario = (row: Scenario) => {
  editForm.value = { ...row }
  editDialogVisible.value = true
}

// 保存编辑
const saveEdit = async () => {
  if (!editForm.value.id) return
  
  editLoading.value = true
  try {
    await request.put(`/workflow/scenario/scenarios/${editForm.value.id}`, {
      subTypeName: editForm.value.subTypeName,
      description: editForm.value.description,
      isFastTrack: editForm.value.isFastTrack,
      amountMin: editForm.value.amountMin,
      amountMax: editForm.value.amountMax
    })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadScenarios()
  } catch (error) {
    console.error('保存失败', error)
    ElMessage.error('保存失败，请检查权限')
  } finally {
    editLoading.value = false
  }
}

// 切换启用状态
const toggleActive = async (row: Scenario) => {
  const action = row.isActive === 1 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}场景「${row.subTypeName}」吗？`,
      '确认操作',
      { type: 'warning' }
    )
    
    await request.put(`/workflow/scenario/scenarios/${row.id}/toggle-active`)
    ElMessage.success(`${action}成功`)
    loadScenarios()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('操作失败', error)
      ElMessage.error('操作失败，请检查权限')
    }
  }
}

// 打开新建场景弹窗
const openCreateDialog = () => {
  createForm.value = {
    subTypeCode: '',
    subTypeName: '',
    amountMin: 0,
    amountMax: null,
    isFastTrack: 0,
    description: ''
  }
  createDialogVisible.value = true
}

// 创建新场景
const createScenario = async () => {
  if (!createForm.value.subTypeCode || !createForm.value.subTypeName) {
    ElMessage.warning('请填写合同类型和场景名称')
    return
  }
  
  createLoading.value = true
  try {
    await request.post('/workflow/scenario/scenarios', createForm.value)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadScenarios()
  } catch (error) {
    console.error('创建失败', error)
    ElMessage.error('创建失败，请检查权限')
  } finally {
    createLoading.value = false
  }
}

// 删除场景
const deleteScenario = async (row: Scenario) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除场景「${row.subTypeName}」吗？此操作将同时删除所有关联的审批节点，且不可恢复！`,
      '危险操作',
      { type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    
    await request.delete(`/workflow/scenario/scenarios/${row.id}`)
    ElMessage.success('删除成功')
    loadScenarios()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败，请检查权限')
    }
  }
}

// 开始编辑节点
const startEditNodes = () => {
  isEditingNodes.value = true
}

// 取消编辑节点
const cancelEditNodes = async () => {
  isEditingNodes.value = false
  // 重新加载节点
  if (currentScenario.value) {
    await viewNodes(currentScenario.value)
  }
}

// 保存节点配置
const saveNodes = async () => {
  if (!currentScenario.value?.scenarioId) return
  
  nodesSaving.value = true
  try {
    await request.put(`/workflow/scenario/scenarios/${currentScenario.value.scenarioId}/nodes`, currentNodes.value)
    ElMessage.success('节点配置保存成功')
    isEditingNodes.value = false
  } catch (error) {
    console.error('保存节点失败', error)
    ElMessage.error('保存失败，请检查权限')
  } finally {
    nodesSaving.value = false
  }
}

// 打开添加节点弹窗
const openAddNodeDialog = () => {
  newNode.value = {
    roleCode: '',
    nodeLevel: 'CITY',
    actionType: 'APPROVE',
    isMandatory: 1,
    canSkip: 0
  }
  addNodeDialogVisible.value = true
}

// 添加节点
const addNode = () => {
  if (!newNode.value.roleCode) {
    ElMessage.warning('请选择审批角色')
    return
  }
  
  const node: ScenarioNode = {
    scenarioId: currentScenario.value?.scenarioId || '',
    nodeOrder: currentNodes.value.length + 1,
    roleCode: newNode.value.roleCode!,
    nodeLevel: newNode.value.nodeLevel!,
    actionType: newNode.value.actionType!,
    isMandatory: newNode.value.isMandatory!,
    canSkip: newNode.value.canSkip!
  }
  
  currentNodes.value.push(node)
  addNodeDialogVisible.value = false
  ElMessage.success('节点已添加，记得保存配置')
}

// 删除节点
const removeNode = (index: number) => {
  currentNodes.value.splice(index, 1)
  // 重新排序
  currentNodes.value.forEach((node, idx) => {
    node.nodeOrder = idx + 1
  })
  ElMessage.success('节点已移除，记得保存配置')
}

// 上移节点
const moveNodeUp = (index: number) => {
  if (index <= 0) return
  const temp = currentNodes.value[index]
  currentNodes.value[index] = currentNodes.value[index - 1]
  currentNodes.value[index - 1] = temp
  // 重新排序
  currentNodes.value.forEach((node, idx) => {
    node.nodeOrder = idx + 1
  })
}

// 下移节点
const moveNodeDown = (index: number) => {
  if (index >= currentNodes.value.length - 1) return
  const temp = currentNodes.value[index]
  currentNodes.value[index] = currentNodes.value[index + 1]
  currentNodes.value[index + 1] = temp
  // 重新排序
  currentNodes.value.forEach((node, idx) => {
    node.nodeOrder = idx + 1
  })
}

// 获取角色显示名称
const getRoleName = (roleCode: string) => {
  return roleNameMap.value[roleCode] || roleCode
}

// 获取角色所属部门类型名称
const getRoleDeptName = (roleCode: string) => {
  const role = roleOptions.find(r => r.value === roleCode)
  if (!role) return ''
  if (role.deptType === null) return role.category === '管理层' ? '管理层' : ''
  const deptNames: Record<string, string> = {
    'NET': '网络部',
    'MKT': '市场部',
    'GOV': '政企部',
    'LEGAL': '法务部',
    'FIN': '财务部',
    'ADMIN': '综合部',
    'PROC': '采购部'
  }
  return deptNames[role.deptType] || ''
}

// 获取级别显示名称
const getLevelName = (level: string) => {
  return levelNameMap[level] || level
}

// 获取动作类型显示名称
const getActionName = (action: string) => {
  return actionTypeMap[action] || action
}

// 获取级别标签类型
const getLevelType = (level: string) => {
  const map: Record<string, string> = {
    'COUNTY': 'info',
    'CITY': 'warning',
    'PROVINCE': 'danger'
  }
  return map[level] || 'info'
}

onMounted(() => {
  loadScenarios()
})
</script>

<template>
  <div class="scenarios-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">审批场景配置</h2>
        <el-tag type="info" size="large">共 {{ scenarios.length }} 个场景</el-tag>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建场景
        </el-button>
        <el-button @click="loadScenarios" :loading="loading">
          刷新列表
        </el-button>
      </div>
    </div>

    <el-card shadow="hover">
      <el-table :data="scenarios" v-loading="loading" stripe border>
        <el-table-column prop="scenarioId" label="场景编码" width="140" fixed />
        <el-table-column prop="subTypeName" label="场景名称" min-width="150">
          <template #default="{ row }">
            <span class="scenario-name">{{ row.subTypeName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subTypeCode" label="合同类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.subTypeCode === 'FALLBACK'" type="info">兜底</el-tag>
            <el-tag v-else type="primary">{{ row.subTypeCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额范围" width="180" align="center">
          <template #default="{ row }">
            <span class="amount-range">
              {{ formatAmount(row.amountMin) }} ~ {{ formatAmount(row.amountMax) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="isFastTrack" label="快速通道" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isFastTrack === 1" type="warning" effect="dark" size="small">是</el-tag>
            <span v-else class="text-muted">否</span>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ row.isActive === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editScenario(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="success" link size="small" @click="viewNodes(row)">
              <el-icon><View /></el-icon> 节点
            </el-button>
            <el-button 
              :type="row.isActive === 1 ? 'warning' : 'success'" 
              link 
              size="small" 
              @click="toggleActive(row)"
            >
              {{ row.isActive === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="deleteScenario(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-alert 
      title="系统说明：县级起草 → 市级审批 → [省级审批]" 
      type="info" 
      :closable="false"
      style="margin-top: 16px"
    >
      <template #default>
        <p>• <strong>固定起点</strong>：所有场景以"县级发起人"为起点，统一身份起草合同</p>
        <p>• <strong>审批层级</strong>：市级部门审批 → 市级管理层审批 → [省级审批（大额）]</p>
        <p>• <strong>场景匹配</strong>：根据合同类型+金额自动匹配审批路径</p>
        <p>• <strong>可扩展</strong>：管理员可添加省级审批节点以支持更高级别审批</p>
      </template>
    </el-alert>

    <!-- 节点查看/编辑弹窗 -->
    <el-dialog 
      v-model="nodesDialogVisible" 
      :title="`审批节点 - ${currentScenario?.subTypeName || ''}`"
      width="850px"
      :close-on-click-modal="!isEditingNodes"
    >
      <div v-loading="nodesLoading">
        <div class="nodes-header">
          <el-alert 
            v-if="currentScenario" 
            :title="`场景编码: ${currentScenario.scenarioId}`" 
            type="info" 
            :closable="false"
          />
          <div v-if="!isEditingNodes" class="nodes-actions">
            <el-button type="primary" size="small" @click="startEditNodes">
              <el-icon><Edit /></el-icon> 编辑节点
            </el-button>
          </div>
          <div v-else class="nodes-actions">
            <el-button type="success" size="small" @click="openAddNodeDialog">
              <el-icon><Plus /></el-icon> 添加节点
            </el-button>
            <el-button type="primary" size="small" @click="saveNodes" :loading="nodesSaving">
              保存配置
            </el-button>
            <el-button size="small" @click="cancelEditNodes">取消</el-button>
          </div>
        </div>
        
        <!-- 只读模式：流程图展示 -->
        <div class="nodes-flow" v-if="currentNodes.length > 0 && !isEditingNodes">
          <div 
            v-for="(node, index) in currentNodes" 
            :key="node.id || index" 
            class="node-item"
          >
            <div class="node-order">{{ node.nodeOrder }}</div>
            <div class="node-content">
              <div class="node-role">{{ getRoleName(node.roleCode) }}</div>
              <div class="node-dept" v-if="getRoleDeptName(node.roleCode)">
                {{ getLevelName(node.nodeLevel) }} · {{ getRoleDeptName(node.roleCode) }}
              </div>
              <div class="node-meta">
                <el-tag size="small" :type="getLevelType(node.nodeLevel)">{{ getLevelName(node.nodeLevel) }}</el-tag>
                <el-tag size="small">{{ getActionName(node.actionType) }}</el-tag>
                <el-tag v-if="node.isMandatory === 1" size="small" type="danger">必须</el-tag>
                <el-tag v-if="node.canSkip === 1" size="small" type="info">可跳过</el-tag>
              </div>
            </div>
            <div v-if="index < currentNodes.length - 1" class="node-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
        
        <!-- 编辑模式：表格展示 -->
        <el-table v-if="isEditingNodes" :data="currentNodes" border stripe>
          <el-table-column prop="nodeOrder" label="顺序" width="70" align="center" />
          <el-table-column prop="nodeLevel" label="审批级别" width="140">
            <template #default="{ row }">
              <el-select v-model="row.nodeLevel" style="width: 100%" @change="handleNodeLevelChange(row)">
                <el-option 
                  v-for="opt in levelOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="roleCode" label="审批角色" min-width="180">
            <template #default="{ row }">
              <el-select 
                v-model="row.roleCode" 
                placeholder="选择角色" 
                style="width: 100%" 
                filterable
                :disabled="row.nodeLevel === 'COUNTY'"
              >
                <el-option-group 
                  v-for="group in getRoleGroupsByLevel(row.nodeLevel)" 
                  :key="group.name" 
                  :label="group.name"
                >
                  <el-option 
                    v-for="opt in group.roles"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  >
                    <span>{{ opt.label }}</span>
                    <span style="float: right; color: #8492a6; font-size: 12px;">{{ opt.value }}</span>
                  </el-option>
                </el-option-group>
              </el-select>
              <div v-if="row.nodeLevel === 'COUNTY'" class="county-hint">县级固定为发起人</div>
            </template>
          </el-table-column>
          <el-table-column prop="actionType" label="动作类型" width="130">
            <template #default="{ row }">
              <el-select 
                v-model="row.actionType" 
                style="width: 100%"
                :disabled="row.nodeLevel === 'COUNTY'"
              >
                <el-option 
                  v-for="opt in actionTypeOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template #default="{ $index }">
              <el-button type="primary" link size="small" @click="moveNodeUp($index)" :disabled="$index === 0">↑</el-button>
              <el-button type="primary" link size="small" @click="moveNodeDown($index)" :disabled="$index === currentNodes.length - 1">↓</el-button>
              <el-button type="danger" link size="small" @click="removeNode($index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="currentNodes.length === 0 && !nodesLoading" description="暂无节点配置">
          <el-button v-if="!isEditingNodes" type="primary" @click="startEditNodes">开始配置</el-button>
        </el-empty>
      </div>
      
      <template #footer>
        <el-button @click="nodesDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加节点弹窗 -->
    <el-dialog v-model="addNodeDialogVisible" title="添加审批节点" width="500px">
      <el-form :model="newNode" label-width="100px">
        <el-form-item label="审批级别">
          <el-radio-group v-model="newNode.nodeLevel" @change="handleNewNodeLevelChange">
            <el-radio-button v-for="opt in levelOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
          <div class="form-tip" v-if="newNode.nodeLevel === 'COUNTY'">
            <el-tag type="info" size="small">县级节点固定为"合同发起人 + 发起"</el-tag>
          </div>
          <div class="form-tip" v-else>先选择级别，角色选项会根据级别自动调整</div>
        </el-form-item>
        <el-form-item label="审批角色" required>
          <el-select 
            v-model="newNode.roleCode" 
            placeholder="选择审批角色" 
            style="width: 100%" 
            filterable
            :disabled="newNode.nodeLevel === 'COUNTY'"
          >
            <el-option-group 
              v-for="group in getRoleGroupsByLevel(newNode.nodeLevel || 'CITY')" 
              :key="group.name" 
              :label="group.name"
            >
              <el-option 
                v-for="opt in group.roles"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              >
                <span>{{ opt.label }}</span>
                <span style="float: right; color: #8492a6; font-size: 12px;">{{ opt.value }}</span>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="动作类型">
          <el-select 
            v-model="newNode.actionType" 
            style="width: 100%"
            :disabled="newNode.nodeLevel === 'COUNTY'"
          >
            <el-option 
              v-for="opt in actionTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="节点属性">
          <el-checkbox v-model="newNode.isMandatory" :true-value="1" :false-value="0">必须节点</el-checkbox>
          <el-checkbox v-model="newNode.canSkip" :true-value="1" :false-value="0">可跳过</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addNodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addNode">添加</el-button>
      </template>
    </el-dialog>

    <!-- 编辑场景弹窗 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑审批场景"
      width="550px"
    >
      <el-form :model="editForm" label-width="100px" label-position="right">
        <el-form-item label="场景编码">
          <el-input v-model="editForm.scenarioId" disabled />
        </el-form-item>
        <el-form-item label="合同类型">
          <el-input v-model="editForm.subTypeCode" disabled />
        </el-form-item>
        <el-form-item label="场景名称">
          <el-input v-model="editForm.subTypeName" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="金额下限">
          <el-input-number 
            v-model="editForm.amountMin" 
            :min="0" 
            :step="10000"
            placeholder="单位：元"
            style="width: 100%"
          />
          <div class="form-tip">单位：元，0表示无下限</div>
        </el-form-item>
        <el-form-item label="金额上限">
          <el-input-number 
            v-model="editForm.amountMax" 
            :min="0" 
            :step="10000"
            placeholder="单位：元"
            style="width: 100%"
          />
          <div class="form-tip">单位：元，留空表示无上限</div>
        </el-form-item>
        <el-form-item label="快速通道">
          <el-switch 
            v-model="editForm.isFastTrack" 
            :active-value="1" 
            :inactive-value="0"
            active-text="是"
            inactive-text="否"
          />
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入场景描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新建场景弹窗 -->
    <el-dialog 
      v-model="createDialogVisible" 
      title="新建审批场景"
      width="550px"
    >
      <el-form :model="createForm" label-width="100px" label-position="right">
        <el-form-item label="合同类型" required>
          <el-select v-model="createForm.subTypeCode" placeholder="选择合同类型" style="width: 100%">
            <el-option 
              v-for="opt in contractTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <div class="form-tip">选择此场景适用的合同类型</div>
        </el-form-item>
        <el-form-item label="场景名称" required>
          <el-input v-model="createForm.subTypeName" placeholder="如：土建工程（大额）省级审批" />
        </el-form-item>
        <el-form-item label="金额下限">
          <el-input-number 
            v-model="createForm.amountMin" 
            :min="0" 
            :step="10000"
            placeholder="单位：元"
            style="width: 100%"
          />
          <div class="form-tip">单位：元，0表示无下限</div>
        </el-form-item>
        <el-form-item label="金额上限">
          <el-input-number 
            v-model="createForm.amountMax" 
            :min="0" 
            :step="10000"
            placeholder="单位：元"
            style="width: 100%"
          />
          <div class="form-tip">单位：元，留空表示无上限</div>
        </el-form-item>
        <el-form-item label="快速通道">
          <el-switch 
            v-model="createForm.isFastTrack" 
            :active-value="1" 
            :inactive-value="0"
            active-text="是"
            inactive-text="否"
          />
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input 
            v-model="createForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入场景描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createScenario" :loading="createLoading">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.scenarios-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.scenario-name {
  font-weight: 500;
}

.amount-range {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #606266;
}

.text-muted {
  color: #909399;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.county-hint {
  font-size: 11px;
  color: #67c23a;
  margin-top: 4px;
}

/* 节点头部 */
.nodes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
}

.nodes-header .el-alert {
  flex: 1;
}

.nodes-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 节点流程样式 */
.nodes-flow {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 12px;
}

.node-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-order {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.node-content {
  background: white;
  padding: 12px 16px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  min-width: 120px;
  border: 1px solid #ebeef5;
}

.node-role {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  font-size: 14px;
}

.node-dept {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.node-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.node-arrow {
  font-size: 20px;
  color: #667eea;
  margin: 0 4px;
}
</style>
