<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { ApprovalTask } from '@/types'
import { getMyPendingTasks } from '@/api/workflow'

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

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyPendingTasks(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        instanceId: 1,
        nodeId: 2,
        nodeName: '部门经理审批',
        approverId: 1,
        approverName: '当前用户',
        status: 'PENDING',
        contractId: 1,
        contractName: '某大厦5G基站租赁合同',
        contractNo: 'HT-ZL-20251201-001',
        contractType: 'STATION_LEASE',
        initiatorName: '张三',
        createTime: '2025-12-01 10:30:00'
      },
      {
        id: 2,
        instanceId: 2,
        nodeId: 3,
        nodeName: '法务审核',
        approverId: 1,
        approverName: '当前用户',
        status: 'PENDING',
        parallelGroupId: 'pg-001',
        contractId: 2,
        contractName: '某区5G网络建设合同',
        contractNo: 'HT-JS-20251202-002',
        contractType: 'NETWORK_CONSTRUCTION',
        initiatorName: '李四',
        createTime: '2025-12-02 14:20:00'
      },
      {
        id: 3,
        instanceId: 3,
        nodeId: 2,
        nodeName: '采购部门审核',
        approverId: 1,
        approverName: '当前用户',
        status: 'PENDING',
        contractId: 3,
        contractName: '通信设备采购合同',
        contractNo: 'HT-SB-20251203-003',
        contractType: 'EQUIPMENT_PURCHASE',
        initiatorName: '王五',
        createTime: '2025-12-03 09:15:00'
      }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  loadData()
}

const goToApprove = (id: number) => {
  router.push(`/workflow/approve/${id}`)
}

const goToContractDetail = (id: number) => {
  router.push(`/contract/detail/${id}`)
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">待办任务</h2>
      <el-tag type="warning" size="large">{{ total }} 条待处理</el-tag>
    </div>
    
    <!-- 任务卡片列表 -->
    <div v-loading="loading" class="task-list">
      <div 
        v-for="task in tableData"
        :key="task.id"
        class="task-card"
      >
        <div class="task-header">
          <div class="task-title">
            <el-tag size="small">{{ contractTypeMap[task.contractType] }}</el-tag>
            <span class="contract-name">{{ task.contractName }}</span>
          </div>
          <el-tag type="warning">{{ task.nodeName }}</el-tag>
        </div>
        
        <div class="task-info">
          <div class="info-item">
            <span class="label">合同编号:</span>
            <span class="value">{{ task.contractNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">发起人:</span>
            <span class="value">{{ task.initiatorName }}</span>
          </div>
          <div class="info-item">
            <span class="label">提交时间:</span>
            <span class="value">{{ task.createTime }}</span>
          </div>
          <div v-if="task.parallelGroupId" class="info-item">
            <el-tag type="info" size="small">会签任务</el-tag>
          </div>
        </div>
        
        <div class="task-actions">
          <el-button type="primary" link @click="goToContractDetail(task.contractId)">
            <el-icon><Document /></el-icon>
            查看合同
          </el-button>
          <el-button type="primary" @click="goToApprove(task.id)">
            <el-icon><Checked /></el-icon>
            去审批
          </el-button>
        </div>
      </div>
      
      <el-empty v-if="tableData.length === 0 && !loading" description="暂无待办任务" />
    </div>
    
    <!-- 分页 -->
    <div v-if="total > queryParams.pageSize" class="pagination-wrapper">
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

<script lang="ts">
import { Document, Checked } from '@element-plus/icons-vue'
export default {
  components: { Document, Checked }
}
</script>

<style scoped>
.task-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-card {
  padding: 20px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
}

.task-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.task-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.contract-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.task-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 6px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  color: #909399;
  font-size: 13px;
}

.info-item .value {
  color: #606266;
  font-size: 13px;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

