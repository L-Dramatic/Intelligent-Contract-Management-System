<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { ApprovalTask } from '@/types'
import { getMyCompletedTasks } from '@/api/workflow'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 权限检查
const hasPermission = ref(true)

const loading = ref(false)
const tableData = ref<ApprovalTask[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const contractTypeMap: Record<string, string> = {
  'TYPE_A': '工程施工合同',
  'TYPE_B': '代维服务合同',
  'TYPE_C': 'IT服务合同',
  'STATION_LEASE': '基站租赁',
  'NETWORK_CONSTRUCTION': '网络建设',
  'EQUIPMENT_PURCHASE': '设备采购',
  'MAINTENANCE_SERVICE': '运维服务'
}

// 状态映射：后端返回数字状态
const statusMap: Record<number, { text: string; type: string }> = {
  1: { text: '已通过', type: 'success' },
  2: { text: '已驳回', type: 'danger' },
  3: { text: '已转签', type: 'info' }
}

onMounted(() => {
  if (userStore.isCountyUser) {
    hasPermission.value = false
    ElMessage.warning('县级员工暂无审批权限')
    return
  }
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    console.log('[CompletedTasks] 开始加载已办任务...')
    const res = await getMyCompletedTasks(queryParams)
    console.log('[CompletedTasks] 返回数据:', res.data)
    // 后端直接返回数组
    const tasks = res.data || []
    tableData.value = tasks
    total.value = tasks.length
    console.log('[CompletedTasks] 已办任务数量:', tasks.length)
  } catch (error) {
    console.error('[CompletedTasks] 加载失败:', error)
    tableData.value = []
    total.value = 0
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
    
    <!-- 无权限提示 -->
    <el-result 
      v-if="!hasPermission" 
      icon="warning" 
      title="暂无审批权限"
      sub-title="县级员工仅负责起草合同，审批工作由上级市公司完成"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/contract/my')">返回我的合同</el-button>
      </template>
    </el-result>
    
    <!-- 数据表格 -->
    <el-table 
      v-if="hasPermission"
      v-loading="loading"
      :data="tableData" 
      style="width: 100%"
      empty-text="暂无已办任务"
    >
      <el-table-column prop="contractNo" label="合同编号" width="200" />
      <el-table-column prop="contractName" label="合同名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="goToContractDetail(row.contractId)">
            {{ row.contractName }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="contractType" label="合同类型" width="120">
        <template #default="{ row }">
          {{ contractTypeMap[row.contractType] || row.contractType }}
        </template>
      </el-table-column>
      <el-table-column prop="nodeName" label="审批环节" width="120">
        <template #default="{ row }">
          {{ row.nodeName || '审批' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="审批结果" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type as any">
            {{ statusMap[row.status]?.text || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="审批意见" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.comment || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="finishTime" label="审批时间" width="180">
        <template #default="{ row }">
          {{ row.finishTime || '-' }}
        </template>
      </el-table-column>
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

