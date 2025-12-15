<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { WorkflowDefinition } from '@/types'
import { getWorkflowList, deleteWorkflow, toggleWorkflowStatus } from '@/api/workflow'

const router = useRouter()

const loading = ref(false)
const tableData = ref<WorkflowDefinition[]>([])

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWorkflowList()
    tableData.value = res.data || []
  } catch {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        name: '简易审批流程',
        description: '适用于金额小于5万元的合同',
        applicableContractTypes: ['STATION_LEASE', 'EQUIPMENT_PURCHASE'],
        conditionExpression: 'amount < 50000',
        version: 1,
        enabled: true,
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-01-01 00:00:00'
      },
      {
        id: 2,
        name: '标准审批流程',
        description: '适用于金额5万至50万元的合同',
        applicableContractTypes: ['STATION_LEASE', 'NETWORK_CONSTRUCTION', 'EQUIPMENT_PURCHASE', 'MAINTENANCE_SERVICE'],
        conditionExpression: 'amount >= 50000 && amount < 500000',
        version: 1,
        enabled: true,
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-01-01 00:00:00'
      },
      {
        id: 3,
        name: '重大审批流程',
        description: '适用于金额大于等于50万元的合同，包含法务和财务会签',
        applicableContractTypes: ['NETWORK_CONSTRUCTION', 'EQUIPMENT_PURCHASE'],
        conditionExpression: 'amount >= 500000',
        version: 2,
        enabled: true,
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-06-15 10:30:00'
      },
      {
        id: 4,
        name: '基站租赁专用流程',
        description: '专门用于基站租赁合同的审批',
        applicableContractTypes: ['STATION_LEASE'],
        conditionExpression: "contractType == 'STATION_LEASE'",
        version: 1,
        enabled: false,
        createTime: '2025-03-01 00:00:00',
        updateTime: '2025-03-01 00:00:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

const contractTypeMap: Record<string, string> = {
  'STATION_LEASE': '基站租赁',
  'NETWORK_CONSTRUCTION': '网络建设',
  'EQUIPMENT_PURCHASE': '设备采购',
  'MAINTENANCE_SERVICE': '运维服务'
}

const goToCreate = () => {
  router.push('/workflow-admin/definition/edit')
}

const goToEdit = (id: number) => {
  router.push(`/workflow-admin/definition/edit/${id}`)
}

const handleToggleStatus = async (row: WorkflowDefinition) => {
  const action = row.enabled ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该流程吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await toggleWorkflowStatus(row.id, !row.enabled)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch {
    // 取消操作
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该流程吗？删除后无法恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    await deleteWorkflow(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 取消删除
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">流程模板管理</h2>
      <el-button type="primary" @click="goToCreate">
        <el-icon><Plus /></el-icon>
        新建流程
      </el-button>
    </div>
    
    <el-alert
      title="流程模板说明"
      type="info"
      description="流程模板定义了合同审批的路径和规则。系统会根据合同属性自动匹配适用的流程模板。"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading"
      :data="tableData" 
      style="width: 100%"
      border
    >
      <el-table-column prop="name" label="流程名称" width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="goToEdit(row.id)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="applicableContractTypes" label="适用合同类型" width="200">
        <template #default="{ row }">
          <div class="type-tags">
            <el-tag 
              v-for="type in row.applicableContractTypes" 
              :key="type"
              size="small"
              style="margin: 2px"
            >
              {{ contractTypeMap[type] }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="conditionExpression" label="匹配条件" width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <code class="condition-code">{{ row.conditionExpression }}</code>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80" align="center">
        <template #default="{ row }">
          v{{ row.version }}
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
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="goToEdit(row.id)">编辑</el-button>
          <el-button 
            :type="row.enabled ? 'warning' : 'success'" 
            link 
            @click="handleToggleStatus(row)"
          >
            {{ row.enabled ? '停用' : '启用' }}
          </el-button>
          <el-button 
            type="danger" 
            link 
            :disabled="row.enabled"
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts">
import { Plus } from '@element-plus/icons-vue'
export default {
  components: { Plus }
}
</script>

<style scoped>
.type-tags {
  display: flex;
  flex-wrap: wrap;
}

.condition-code {
  font-size: 12px;
  padding: 2px 6px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
}
</style>

