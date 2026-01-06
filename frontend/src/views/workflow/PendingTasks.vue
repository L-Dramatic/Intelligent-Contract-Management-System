<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { ApprovalTask } from '@/types'
import { getMyPendingTasks } from '@/api/workflow'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 权限检查：县级用户无审批权限
const hasPermission = ref(true)

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
  'MAINTENANCE_SERVICE': '运维服务',
  // 场景审批的合同类型
  'A1': '土建工程',
  'A2': '装修工程',
  'A3': '零星维修',
  'B1': '光缆代维',
  'B2': '基站代维',
  'B3': '家宽代维',
  'B4': '应急保障',
  'C1': '定制开发',
  'C2': '软件采购',
  'C3': 'DICT集成'
}

onMounted(() => {
  // 县级用户无审批权限
  if (userStore.isCountyUser) {
    hasPermission.value = false
    ElMessage.warning('县级员工暂无审批权限，合同需提交至上级市公司审批')
    return
  }
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyPendingTasks(queryParams)
    // 后端直接返回数组，不是分页对象
    const tasks = res.data || []
    tableData.value = tasks
    total.value = tasks.length
  } catch (error) {
    console.error('加载待办任务失败:', error)
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
      <el-tag v-if="hasPermission" type="warning" size="large">{{ total }} 条待处理</el-tag>
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
    
    <!-- 任务卡片列表 -->
    <div v-if="hasPermission" v-loading="loading" class="task-list">
      <div 
        v-for="task in tableData"
        :key="task.id"
        class="task-card"
      >
        <div class="task-header">
          <div class="task-title">
            <el-tag size="small">{{ contractTypeMap[task.contractType] || task.contractType || '合同' }}</el-tag>
            <span class="contract-name">{{ task.contractName || '未知合同' }}</span>
          </div>
          <el-tag type="warning">{{ task.nodeName || '审批节点' }}</el-tag>
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

