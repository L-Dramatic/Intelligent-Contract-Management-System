<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWorkflowList } from '@/api/workflow'

interface WorkflowDef {
  id: number
  name: string
  description: string
  applicableContractTypes: string[]
  conditionExpression: string
  version: number
  enabled: boolean
  scenarioCount?: number
  createTime: string
  updateTime: string
}

const router = useRouter()

const loading = ref(false)
const tableData = ref<WorkflowDef[]>([])

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWorkflowList()
    if (res.data && res.data.length > 0) {
      tableData.value = res.data
      return
    }
    throw new Error('empty')
  } catch {
    console.log('API未实现，使用模拟数据')
    tableData.value = [
      {
        id: 1,
        name: '工程施工合同审批流程',
        description: '适用于A1土建工程、A2装修工程、A3零星维修类合同',
        applicableContractTypes: ['A1', 'A2', 'A3'],
        conditionExpression: "subType == 'A1' || subType == 'A2' || subType == 'A3'",
        version: 1,
        enabled: true,
        scenarioCount: 12,
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-01-01 00:00:00'
      },
      {
        id: 2,
        name: '代维服务合同审批流程',
        description: '适用于B1光缆代维、B2基站代维、B3家宽代维、B4应急保障类合同',
        applicableContractTypes: ['B1', 'B2', 'B3', 'B4'],
        conditionExpression: "subType == 'B1' || subType == 'B2' || subType == 'B3' || subType == 'B4'",
        version: 1,
        enabled: true,
        scenarioCount: 13,
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-06-15 10:30:00'
      },
      {
        id: 3,
        name: 'IT/DICT合同审批流程',
        description: '适用于C1定制开发、C2商用软件采购、C3 DICT集成类合同',
        applicableContractTypes: ['C1', 'C2', 'C3'],
        conditionExpression: "subType == 'C1' || subType == 'C2' || subType == 'C3'",
        version: 1,
        enabled: true,
        scenarioCount: 9,
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-03-01 00:00:00'
      },
      {
        id: 4,
        name: '兜底审批流程',
        description: '当没有匹配的专用流程时，使用此兜底流程',
        applicableContractTypes: ['*'],
        conditionExpression: 'default',
        version: 1,
        enabled: true,
        scenarioCount: 3,
        createTime: '2025-03-01 00:00:00',
        updateTime: '2025-03-01 00:00:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

const contractTypeMap: Record<string, string> = {
  'A1': '土建工程',
  'A2': '装修工程',
  'A3': '零星维修',
  'B1': '光缆代维',
  'B2': '基站代维',
  'B3': '家宽代维',
  'B4': '应急保障',
  'C1': '定制开发',
  'C2': '商用软件采购',
  'C3': 'DICT集成',
  '*': '全部类型'
}

// 跳转到审批场景页面
const goToScenarios = () => {
  router.push('/workflow-admin/scenarios')
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">流程模板总览</h2>
      <el-button type="primary" @click="goToScenarios">
        <el-icon><Setting /></el-icon>
        管理审批场景
      </el-button>
    </div>
    
    <el-alert
      title="流程模板说明"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    >
      <template #default>
        <p>• 流程模板是审批场景的聚合视图，按合同大类展示</p>
        <p>• 实际审批配置请在「审批场景」页面管理</p>
        <p>• 每个场景对应具体的合同子类型+金额组合</p>
      </template>
    </el-alert>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading"
      :data="tableData" 
      style="width: 100%"
      border
    >
      <el-table-column prop="name" label="流程名称" width="200">
        <template #default="{ row }">
          <span class="flow-name">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
      <el-table-column prop="applicableContractTypes" label="适用合同类型" width="220">
        <template #default="{ row }">
          <div class="type-tags">
            <el-tag 
              v-for="type in row.applicableContractTypes" 
              :key="type"
              size="small"
              style="margin: 2px"
            >
              {{ contractTypeMap[type] || type }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="scenarioCount" label="场景数量" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="info">{{ row.scenarioCount || '-' }} 个</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">
            {{ row.enabled ? '已启用' : '已停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180" />
    </el-table>
  </div>
</template>

<script lang="ts">
import { Setting } from '@element-plus/icons-vue'
export default {
  components: { Setting }
}
</script>

<style scoped>
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

.type-tags {
  display: flex;
  flex-wrap: wrap;
}

.flow-name {
  font-weight: 500;
  color: #303133;
}
</style>

