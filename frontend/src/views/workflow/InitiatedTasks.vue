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
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已驳回', value: 3 },
  { label: '已终止', value: 5 }
]

// 状态映射：后端返回数字状态
const statusMap: Record<number, { text: string; type: string }> = {
  1: { text: '进行中', type: 'primary' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已驳回', type: 'danger' },
  4: { text: '已撤销', type: 'info' },
  5: { text: '已终止', type: 'info' }
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getInstanceList(queryParams)
    // 后端返回数组
    const instances = res.data || []
    tableData.value = instances
    total.value = instances.length
  } catch (error) {
    console.error('加载我发起的流程失败:', error)
    tableData.value = []
    total.value = 0
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
          <el-tag v-if="row.status === 1" type="warning">
            {{ row.currentNodeName || '审批中' }}
          </el-tag>
          <span v-else>{{ row.currentNodeName || '结束' }}</span>
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

