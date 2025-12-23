<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

interface ScenarioNode {
  id: number
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
  id: number
  scenarioId: string        // 后端字段
  subTypeCode: string       // 后端字段
  subTypeName: string       // 后端字段
  amountMin: number | null
  amountMax: number | null
  isActive: number          // 后端是 Integer
  isFastTrack: number
  description: string
  nodes?: ScenarioNode[]
}

const loading = ref(false)
const scenarios = ref<Scenario[]>([])

// 节点查看弹窗
const nodesDialogVisible = ref(false)
const nodesLoading = ref(false)
const currentScenario = ref<Scenario | null>(null)
const currentNodes = ref<ScenarioNode[]>([])

// 编辑弹窗
const editDialogVisible = ref(false)
const editForm = ref<Partial<Scenario>>({})

// 角色名称映射
const roleNameMap: Record<string, string> = {
  'DEPT_MANAGER': '部门经理',
  'LEGAL_REVIEWER': '法务审核员',
  'COST_AUDITOR': '成本审核员',
  'TECH_REVIEWER': '技术审核员',
  'PROJECT_REVIEWER': '项目审核员',
  'VICE_PRESIDENT': '分管副总',
  'GENERAL_MANAGER': '总经理',
  'T1M': '三重一大会议'
}

// 级别名称映射
const levelNameMap: Record<string, string> = {
  'COUNTY': '县级',
  'CITY': '市级',
  'PROVINCE': '省级'
}

// 动作类型映射
const actionTypeMap: Record<string, string> = {
  'INITIATE': '发起',
  'REVIEW': '审查',
  'VERIFY': '核实',
  'APPROVE': '审批',
  'FINAL_APPROVE': '最终审批'
}

// 加载审批场景列表
const loadScenarios = async () => {
  loading.value = true
  try {
    const res = await request.get('/workflow/scenario/scenarios')
    scenarios.value = res.data || []
  } catch (error) {
    console.log('使用模拟数据')
    scenarios.value = []
  } finally {
    loading.value = false
  }
}

// 格式化金额
const formatAmount = (amount: number | null) => {
  if (amount === null) return '-'
  return `¥${(amount / 10000).toFixed(0)}万`
}

// 查看节点
const viewNodes = async (row: Scenario) => {
  currentScenario.value = row
  nodesDialogVisible.value = true
  nodesLoading.value = true
  
  try {
    const res = await request.get(`/workflow/scenario/scenarios/${row.scenarioId}`)
    currentNodes.value = res.data?.nodes || []
  } catch (error) {
    console.error('获取节点失败', error)
    // 模拟节点数据
    currentNodes.value = [
      { id: 1, scenarioId: row.scenarioId, nodeOrder: 1, roleCode: 'DEPT_MANAGER', nodeLevel: 'COUNTY', actionType: 'REVIEW', isMandatory: 1, canSkip: 0 },
      { id: 2, scenarioId: row.scenarioId, nodeOrder: 2, roleCode: 'LEGAL_REVIEWER', nodeLevel: 'CITY', actionType: 'REVIEW', isMandatory: 1, canSkip: 0 },
      { id: 3, scenarioId: row.scenarioId, nodeOrder: 3, roleCode: 'COST_AUDITOR', nodeLevel: 'CITY', actionType: 'VERIFY', isMandatory: 1, canSkip: 0 },
    ]
  } finally {
    nodesLoading.value = false
  }
}

// 编辑场景
const editScenario = (row: Scenario) => {
  editForm.value = { ...row }
  editDialogVisible.value = true
}

// 保存编辑（只读提示）
const saveEdit = () => {
  ElMessage.info('场景配置为只读，如需修改请联系系统管理员')
  editDialogVisible.value = false
}

// 获取角色显示名称
const getRoleName = (roleCode: string) => {
  return roleNameMap[roleCode] || roleCode
}

// 获取级别显示名称
const getLevelName = (level: string) => {
  return levelNameMap[level] || level
}

// 获取动作类型显示名称
const getActionName = (action: string) => {
  return actionTypeMap[action] || action
}

onMounted(() => {
  loadScenarios()
})
</script>

<template>
  <div class="scenarios-page">
    <div class="page-header">
      <h2 class="page-title">审批场景配置</h2>
      <el-tag type="info" size="large">共 {{ scenarios.length }} 个场景</el-tag>
    </div>

    <el-card shadow="hover">
      <el-table :data="scenarios" v-loading="loading" stripe>
        <el-table-column prop="scenarioId" label="场景编码" width="180" />
        <el-table-column prop="subTypeName" label="场景名称" min-width="150" />
        <el-table-column prop="subTypeCode" label="合同类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.subTypeCode === 'FALLBACK'" type="info">兜底</el-tag>
            <el-tag v-else type="primary">{{ row.subTypeCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额范围" width="180" align="center">
          <template #default="{ row }">
            {{ formatAmount(row.amountMin) }} ~ {{ formatAmount(row.amountMax) }}
          </template>
        </el-table-column>
        <el-table-column prop="isFastTrack" label="快速通道" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isFastTrack === 1" type="warning">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive === 1 ? 'success' : 'info'">
              {{ row.isActive === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editScenario(row)">详情</el-button>
            <el-button type="success" link size="small" @click="viewNodes(row)">节点</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-alert 
      title="提示：场景配置说明" 
      type="info" 
      :closable="false"
      style="margin-top: 16px"
    >
      <template #default>
        <p>• 系统已配置34个标准审批场景 + 3个兜底场景</p>
        <p>• 场景匹配优先级：精确匹配 > 兜底场景</p>
        <p>• 每个场景的审批节点决定了具体的审批路径</p>
      </template>
    </el-alert>

    <!-- 节点查看弹窗 -->
    <el-dialog 
      v-model="nodesDialogVisible" 
      :title="`审批节点 - ${currentScenario?.subTypeName || ''}`"
      width="700px"
    >
      <div v-loading="nodesLoading">
        <el-alert 
          v-if="currentScenario" 
          :title="`场景编码: ${currentScenario.scenarioId}`" 
          type="info" 
          :closable="false" 
          style="margin-bottom: 16px"
        />
        
        <div class="nodes-flow">
          <div 
            v-for="(node, index) in currentNodes" 
            :key="node.id" 
            class="node-item"
          >
            <div class="node-order">{{ node.nodeOrder }}</div>
            <div class="node-content">
              <div class="node-role">{{ getRoleName(node.roleCode) }}</div>
              <div class="node-meta">
                <el-tag size="small" type="warning">{{ getLevelName(node.nodeLevel) }}</el-tag>
                <el-tag size="small">{{ getActionName(node.actionType) }}</el-tag>
                <el-tag v-if="node.isMandatory === 1" size="small" type="danger">必须</el-tag>
                <el-tag v-if="node.canSkip === 1" size="small" type="info">可跳过</el-tag>
              </div>
            </div>
            <div v-if="index < currentNodes.length - 1" class="node-arrow">→</div>
          </div>
        </div>
        
        <el-empty v-if="currentNodes.length === 0" description="暂无节点配置" />
      </div>
      
      <template #footer>
        <el-button @click="nodesDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 详情查看弹窗 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="场景详情"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="场景编码">{{ editForm.scenarioId }}</el-descriptions-item>
        <el-descriptions-item label="场景名称">{{ editForm.subTypeName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ editForm.subTypeCode }}</el-descriptions-item>
        <el-descriptions-item label="金额范围">
          {{ formatAmount(editForm.amountMin ?? null) }} ~ {{ formatAmount(editForm.amountMax ?? null) }}
        </el-descriptions-item>
        <el-descriptions-item label="快速通道">
          <el-tag v-if="editForm.isFastTrack === 1" type="warning">是</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="editForm.isActive === 1 ? 'success' : 'info'">
            {{ editForm.isActive === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ editForm.description || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">关闭</el-button>
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

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

/* 节点流程样式 */
.nodes-flow {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.node-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-order {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.node-content {
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-width: 120px;
}

.node-role {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.node-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.node-arrow {
  font-size: 20px;
  color: #909399;
  margin: 0 4px;
}
</style>

