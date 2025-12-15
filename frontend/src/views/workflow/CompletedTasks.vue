<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { ApprovalTask } from '@/types'
import { getMyCompletedTasks } from '@/api/workflow'

const router = useRouter()

const loading = ref(false)
const tableData = ref<ApprovalTask[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const contractTypeMap: Record<string, string> = {
  'STATION_LEASE': '基站租赁',
  'NETWORK_CONSTRUCTION': '网络建设',
  'EQUIPMENT_PURCHASE': '设备采购',
  'MAINTENANCE_SERVICE': '运维服务'
}

const statusMap: Record<string, { text: string; type: string }> = {
  'APPROVED': { text: '已通过', type: 'success' },
  'REJECTED': { text: '已驳回', type: 'danger' },
  'TRANSFERRED': { text: '已转签', type: 'info' }
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyCompletedTasks(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 模拟数据
    tableData.value = [
      {
        id: 10,
        instanceId: 10,
        nodeId: 2,
        nodeName: '部门经理审批',
        approverId: 1,
        approverName: '当前用户',
        status: 'APPROVED',
        opinion: '同意，合同条款符合规范',
        approvalTime: '2025-12-01 15:30:00',
        contractId: 10,
        contractName: '某商场基站租赁合同',
        contractNo: 'HT-ZL-20251130-010',
        contractType: 'STATION_LEASE',
        initiatorName: '张三',
        createTime: '2025-11-30 10:00:00'
      },
      {
        id: 11,
        instanceId: 11,
        nodeId: 3,
        nodeName: '法务审核',
        approverId: 1,
        approverName: '当前用户',
        status: 'REJECTED',
        opinion: 'SLA条款需要补充完善',
        approvalTime: '2025-11-28 16:20:00',
        contractId: 11,
        contractName: '某区网络运维服务合同',
        contractNo: 'HT-YW-20251128-011',
        contractType: 'MAINTENANCE_SERVICE',
        initiatorName: '李四',
        createTime: '2025-11-28 09:00:00'
      }
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
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
      <h2 class="page-title">已办任务</h2>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading"
      :data="tableData" 
      style="width: 100%"
    >
      <el-table-column prop="contractNo" label="合同编号" width="200" />
      <el-table-column prop="contractName" label="合同名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="goToContractDetail(row.contractId)">
            {{ row.contractName }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="contractType" label="合同类型" width="100">
        <template #default="{ row }">
          {{ contractTypeMap[row.contractType] }}
        </template>
      </el-table-column>
      <el-table-column prop="nodeName" label="审批环节" width="120" />
      <el-table-column prop="status" label="审批结果" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type as any">
            {{ statusMap[row.status]?.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="opinion" label="审批意见" min-width="200" show-overflow-tooltip />
      <el-table-column prop="approvalTime" label="审批时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="goToContractDetail(row.contractId)">
            查看
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
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

