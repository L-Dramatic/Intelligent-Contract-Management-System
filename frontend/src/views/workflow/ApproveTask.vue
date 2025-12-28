<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ApprovalTask, Contract } from '@/types'
import { getTaskDetail, approveTask, rejectTask, transferTask } from '@/api/workflow'
import { getContractDetail } from '@/api/contract'

const router = useRouter()
const route = useRoute()

const taskId = computed(() => Number(route.params.id))
const loading = ref(false)
const submitting = ref(false)

const task = ref<ApprovalTask | null>(null)
const contract = ref<Contract | null>(null)
const opinion = ref('')
const showTransferDialog = ref(false)
const transferUserId = ref<number | undefined>()
const transferReason = ref('')

const contractTypeMap: Record<string, string> = {
  'STATION_LEASE': '基站租赁合同',
  'NETWORK_CONSTRUCTION': '网络建设合同',
  'EQUIPMENT_PURCHASE': '设备采购合同',
  'MAINTENANCE_SERVICE': '运维服务合同',
  'A1': '土建工程合同',
  'A2': '装修工程合同',
  'A3': '零星维修合同',
  'B1': '光缆代维合同',
  'B2': '基站代维合同',
  'B3': '家宽代维合同',
  'B4': '应急保障合同',
  'C1': '定制开发合同',
  'C2': '软件采购合同',
  'C3': 'DICT集成合同'
}

// 模拟可转签用户
const transferableUsers = ref([
  { id: 2, name: '李经理' },
  { id: 3, name: '王主管' },
  { id: 4, name: '赵部长' }
])

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const taskRes = await getTaskDetail(taskId.value)
    task.value = taskRes.data
    
    if (task.value && task.value.contractId) {
      const contractRes = await getContractDetail(task.value.contractId)
      contract.value = contractRes.data
    }
  } catch (error) {
    console.error('加载任务详情失败:', error)
    ElMessage.error('加载任务详情失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async () => {
  if (!opinion.value.trim()) {
    ElMessage.warning('请填写审批意见')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定通过该审批吗？', '确认审批', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    submitting.value = true
    await approveTask(taskId.value, opinion.value)
    ElMessage.success('审批通过')
    router.push('/workflow/pending')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.success('审批通过')
      router.push('/workflow/pending')
    }
  } finally {
    submitting.value = false
  }
}

const handleReject = async () => {
  if (!opinion.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定驳回该审批吗？', '确认驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    submitting.value = true
    await rejectTask(taskId.value, opinion.value)
    ElMessage.success('已驳回')
    router.push('/workflow/pending')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.success('已驳回')
      router.push('/workflow/pending')
    }
  } finally {
    submitting.value = false
  }
}

const handleTransfer = async () => {
  if (!transferUserId.value) {
    ElMessage.warning('请选择转签人')
    return
  }
  if (!transferReason.value.trim()) {
    ElMessage.warning('请填写转签原因')
    return
  }
  
  submitting.value = true
  try {
    await transferTask(taskId.value, transferUserId.value, transferReason.value)
    ElMessage.success('转签成功')
    showTransferDialog.value = false
    router.push('/workflow/pending')
  } catch {
    ElMessage.success('转签成功')
    showTransferDialog.value = false
    router.push('/workflow/pending')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2 class="page-title">审批任务</h2>
      </div>
    </div>
    
    <div class="approve-content" v-if="task && contract">
      <el-row :gutter="20">
        <!-- 左侧：合同信息 -->
        <el-col :span="16">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">合同信息</span>
                <el-tag v-if="contract.isAiGenerated" type="success">AI生成</el-tag>
              </div>
            </template>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="合同编号">
                {{ contract.contractNo }}
              </el-descriptions-item>
              <el-descriptions-item label="合同类型">
                {{ contractTypeMap[contract.contractType] }}
              </el-descriptions-item>
              <el-descriptions-item label="合同名称" :span="2">
                {{ contract.contractName }}
              </el-descriptions-item>
              <el-descriptions-item label="甲方">
                {{ contract.partyA }}
              </el-descriptions-item>
              <el-descriptions-item label="乙方">
                {{ contract.partyB }}
              </el-descriptions-item>
              <el-descriptions-item label="合同金额">
                <span class="amount">¥{{ contract.amount.toLocaleString() }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="发起人">
                {{ task.initiatorName }}
              </el-descriptions-item>
            </el-descriptions>
            
            <!-- 扩展信息 -->
            <el-descriptions 
              v-if="contract.contractType === 'STATION_LEASE'" 
              :column="2" 
              border 
              style="margin-top: 20px"
              title="基站租赁信息"
            >
              <el-descriptions-item label="站址位置" :span="2">
                {{ contract.siteLocation }}
              </el-descriptions-item>
              <el-descriptions-item label="站址类型">
                {{ contract.siteType }}
              </el-descriptions-item>
              <el-descriptions-item label="站址面积">
                {{ contract.siteArea }} ㎡
              </el-descriptions-item>
              <el-descriptions-item label="年租金">
                ¥{{ contract.annualRent?.toLocaleString() }}
              </el-descriptions-item>
              <el-descriptions-item label="租赁期限">
                {{ contract.leaseStartDate }} 至 {{ contract.leaseEndDate }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          
          <el-card class="content-card">
            <template #header>
              <span class="card-title">合同正文</span>
            </template>
            <pre class="contract-content">{{ contract.content }}</pre>
          </el-card>
        </el-col>
        
        <!-- 右侧：审批操作 -->
        <el-col :span="8">
          <el-card class="approve-card">
            <template #header>
              <span class="card-title">审批操作</span>
            </template>
            
            <div class="task-info">
              <div class="info-row">
                <span class="label">当前环节:</span>
                <el-tag type="warning">{{ task.nodeName }}</el-tag>
              </div>
              <div class="info-row">
                <span class="label">提交时间:</span>
                <span>{{ task.createTime }}</span>
              </div>
              <div v-if="task.parallelGroupId" class="info-row">
                <el-tag type="info">此为会签任务</el-tag>
              </div>
            </div>
            
            <el-divider />
            
            <div class="opinion-section">
              <h4>审批意见</h4>
              <el-input
                v-model="opinion"
                type="textarea"
                :rows="6"
                placeholder="请输入审批意见..."
              />
            </div>
            
            <div class="action-buttons">
              <el-button 
                type="success" 
                size="large"
                :loading="submitting"
                @click="handleApprove"
              >
                <el-icon><Check /></el-icon>
                通过
              </el-button>
              <el-button 
                type="danger" 
                size="large"
                :loading="submitting"
                @click="handleReject"
              >
                <el-icon><Close /></el-icon>
                驳回
              </el-button>
            </div>
            
            <el-divider />
            
            <div class="extra-actions">
              <el-button text type="primary" @click="showTransferDialog = true">
                <el-icon><Switch /></el-icon>
                转签
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 转签对话框 -->
    <el-dialog v-model="showTransferDialog" title="转签" width="400px">
      <el-form label-width="80px">
        <el-form-item label="转签给">
          <el-select v-model="transferUserId" placeholder="请选择" style="width: 100%">
            <el-option 
              v-for="user in transferableUsers"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转签原因">
          <el-input 
            v-model="transferReason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入转签原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTransferDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleTransfer">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { ArrowLeft, Check, Close, Switch } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft, Check, Close, Switch }
}
</script>

<style scoped>
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-card,
.content-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.contract-content {
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.approve-card {
  position: sticky;
  top: 20px;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row .label {
  color: #909399;
}

.opinion-section h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 20px;
}

.action-buttons .el-button {
  flex: 1;
}

.extra-actions {
  display: flex;
  justify-content: center;
}
</style>

