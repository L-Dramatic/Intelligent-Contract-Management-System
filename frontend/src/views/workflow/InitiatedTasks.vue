<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { WorkflowInstance } from '@/types'
import { getInstanceList } from '@/api/workflow'

const router = useRouter()

const loading = ref(false)
const tableData = ref<WorkflowInstance[]>([])
const total = ref(0)

const queryParams = reactive({
  status: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '进行中', value: 'RUNNING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已终止', value: 'TERMINATED' }
]

const statusMap: Record<string, { text: string; type: string }> = {
  'RUNNING': { text: '进行中', type: 'primary' },
  'COMPLETED': { text: '已完成', type: 'success' },
  'REJECTED': { text: '已驳回', type: 'danger' },
  'TERMINATED': { text: '已终止', type: 'info' }
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getInstanceList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        workflowDefinitionId: 1,
        contractId: 1,
        contractName: '某大厦5G基站租赁合同',
        currentNodeId: 2,
        currentNodeName: '部门经理审批',
        status: 'RUNNING',
        initiatorId: 1,
        initiatorName: '当前用户',
        startTime: '2025-12-01 10:30:00'
      },
      {
        id: 2,
        workflowDefinitionId: 2,
        contractId: 2,
        contractName: '某区5G网络建设合同',
        currentNodeId: 4,
        currentNodeName: '分管领导审批',
        status: 'RUNNING',
        initiatorId: 1,
        initiatorName: '当前用户',
        startTime: '2025-12-02 14:20:00'
      },
      {
        id: 10,
        workflowDefinitionId: 1,
        contractId: 10,
        contractName: '某商场基站租赁合同',
        currentNodeId: 5,
        currentNodeName: '结束',
        status: 'COMPLETED',
        initiatorId: 1,
        initiatorName: '当前用户',
        startTime: '2025-11-30 10:00:00',
        endTime: '2025-12-01 16:00:00'
      }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  loadData()
}

const goToContractDetail = (id: number) => {
  router.push(`/contract/detail/${id}`)
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">我发起的</h2>
    </div>
    
    <!-- 快速筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="queryParams.status" @change="handleSearch">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button 
          v-for="item in statusOptions" 
          :key="item.value" 
          :value="item.value"
        >
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading"
      :data="tableData" 
      style="width: 100%"
    >
      <el-table-column prop="contractName" label="合同名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="goToContractDetail(row.contractId)">
            {{ row.contractName }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="currentNodeName" label="当前环节" width="140">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'RUNNING'" type="warning">
            {{ row.currentNodeName }}
          </el-tag>
          <span v-else>{{ row.currentNodeName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type as any">
            {{ statusMap[row.status]?.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="发起时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180">
        <template #default="{ row }">
          {{ row.endTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="goToContractDetail(row.contractId)">
            查看详情
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
  </div>
</template>

<style scoped>
.filter-bar {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

