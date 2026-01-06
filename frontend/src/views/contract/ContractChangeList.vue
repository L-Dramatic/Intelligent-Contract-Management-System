<template>
  <div class="contract-change-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2>📋 合同变更管理</h2>
        <p class="subtitle">管理合同变更申请和查看变更记录</p>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 发起变更 -->
      <el-tab-pane label="发起变更" name="create">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>选择要变更的合同</span>
              <span class="header-tip">只能变更您起草的已生效合同</span>
            </div>
          </template>
          
          <!-- 筛选 -->
          <el-form :inline="true" :model="contractFilter" class="filter-form">
            <el-form-item label="合同类型">
              <el-select v-model="contractFilter.type" placeholder="全部类型" clearable style="width: 150px" @change="loadChangeableContracts">
                <el-option
                  v-for="item in contractTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="合同名称">
              <el-input
                v-model="contractFilter.name"
                placeholder="请输入合同名称"
                clearable
                style="width: 200px"
                @keyup.enter="loadChangeableContracts"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadChangeableContracts">
                <el-icon><Search /></el-icon> 查询
              </el-button>
              <el-button @click="resetContractFilter">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 合同列表 -->
          <el-table 
            :data="changeableContracts" 
            v-loading="loadingContracts"
            border 
            stripe
            empty-text="暂无可变更的合同（只显示您起草的已生效合同）"
          >
            <el-table-column prop="contractNo" label="合同编号" width="180" />
            <el-table-column prop="name" label="合同名称" min-width="200" show-overflow-tooltip />
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getTypeName(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="150" align="right">
              <template #default="{ row }">
                ¥{{ formatAmount(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="partyB" label="乙方" min-width="150" show-overflow-tooltip />
            <el-table-column prop="version" label="版本" width="100" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="goToCreateChange(row)">
                  发起变更
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="contractPagination.total > 0">
            <el-pagination
              v-model:current-page="contractPagination.pageNum"
              v-model:page-size="contractPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="contractPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadChangeableContracts"
              @current-change="loadChangeableContracts"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 变更记录 -->
      <el-tab-pane label="变更记录" name="history">
        <el-card>
          <!-- 筛选 -->
          <el-form :inline="true" :model="changeFilter" class="filter-form">
        <el-form-item label="变更状态">
              <el-select v-model="changeFilter.status" placeholder="全部状态" clearable style="width: 150px" @change="loadChanges">
            <el-option
              v-for="item in CHANGE_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
              <el-button type="primary" @click="loadChanges">
            <el-icon><Search /></el-icon> 查询
          </el-button>
              <el-button @click="resetChangeFilter">重置</el-button>
        </el-form-item>
      </el-form>

          <!-- 变更列表 -->
          <el-table :data="changeTableData" v-loading="loadingChanges" border stripe>
        <el-table-column prop="changeNo" label="变更单号" width="160" />
        <el-table-column prop="title" label="变更标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contractNo" label="原合同编号" width="160" />
        <el-table-column prop="changeTypeName" label="变更类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.changeTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额变化" width="150">
          <template #default="{ row }">
            <span v-if="row.amountDiff" :class="row.amountDiff > 0 ? 'amount-increase' : 'amount-decrease'">
              {{ row.amountDiff > 0 ? '+' : '' }}{{ formatAmount(row.amountDiff) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isMajorChange" label="重大变更" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isMajorChange" type="danger" size="small">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发起时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewChangeDetail(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              size="small"
                  @click="editChange(row)"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="row.status === 0"
                  type="warning"
                  link
                  size="small"
                  @click="submitChange(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              link
              size="small"
                  @click="cancelChange(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
          <div class="pagination-wrapper" v-if="changePagination.total > 0">
        <el-pagination
              v-model:current-page="changePagination.pageNum"
              v-model:page-size="changePagination.pageSize"
          :page-sizes="[10, 20, 50]"
              :total="changePagination.total"
          layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadChanges"
              @current-change="loadChanges"
        />
      </div>
    </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 变更详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="变更详情" size="60%">
      <div v-if="currentChange" class="change-detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="变更单号">{{ currentChange.changeNo }}</el-descriptions-item>
          <el-descriptions-item label="变更版本">{{ currentChange.changeVersion }}</el-descriptions-item>
          <el-descriptions-item label="变更标题" :span="2">{{ currentChange.title }}</el-descriptions-item>
          <el-descriptions-item label="原合同编号">{{ currentChange.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="原合同名称">{{ currentChange.contractName }}</el-descriptions-item>
          <el-descriptions-item label="变更类型">
            <el-tag>{{ currentChange.changeTypeName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="变更原因">
            <el-tag type="info">{{ currentChange.reasonTypeName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="是否重大变更">
            <el-tag v-if="currentChange.isMajorChange" type="danger">是</el-tag>
            <span v-else>否</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentChange.status)">{{ currentChange.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发起人">{{ currentChange.initiatorName }}</el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ formatDate(currentChange.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 变更说明 -->
        <el-card class="detail-section" shadow="never">
          <template #header>变更说明</template>
          <p>{{ currentChange.description }}</p>
        </el-card>

        <!-- 乙方沟通记录 -->
        <el-card v-if="currentChange.partyBCommunication" class="detail-section" shadow="never">
          <template #header>乙方沟通记录</template>
          <p>{{ currentChange.partyBCommunication }}</p>
        </el-card>

        <!-- 变更对比 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <div class="diff-header">
              <span>变更对比</span>
              <span v-if="currentChange.amountDiff" class="amount-change">
                金额变化：
                <strong :class="currentChange.amountDiff > 0 ? 'increase' : 'decrease'">
                  {{ currentChange.amountDiff > 0 ? '+' : '' }}{{ formatAmount(currentChange.amountDiff) }}
                  ({{ currentChange.changePercent?.toFixed(1) || 0 }}%)
                </strong>
              </span>
            </div>
          </template>
          <el-table :data="currentChange.diffItems || []" border stripe>
            <el-table-column prop="fieldLabel" label="字段" width="120" />
            <el-table-column label="变更前">
              <template #default="{ row }">
                <span class="before-value">{{ formatValue(row.beforeValue) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="变更后">
              <template #default="{ row }">
                <span class="after-value">{{ formatValue(row.afterValue) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="changeDesc" label="变化" width="150">
              <template #default="{ row }">
                <el-tag size="small" :type="row.changeDesc.includes('+') ? 'danger' : 'success'">
                  {{ row.changeDesc }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 审批进度详情 -->
        <el-card class="detail-section" shadow="never" v-loading="loadingHistory">
          <template #header>审批进度</template>
          <el-timeline>
            <!-- 发起节点 -->
            <el-timeline-item :timestamp="formatDate(currentChange.createdAt)" placement="top" type="primary" icon="UserFilled">
              <div class="timeline-content">
                <div class="timeline-title">发起变更申请</div>
                <div class="timeline-person">发起人：{{ currentChange.initiatorName }}</div>
              </div>
            </el-timeline-item>
            
            <!-- 审批节点 -->
            <el-timeline-item 
              v-for="task in approvalHistory" 
              :key="task.id"
              :timestamp="formatDate(task.finishTime || task.createTime)" 
              placement="top"
              :type="getApprovalStatusType(task.status)"
              :hollow="task.status === 0"
            >
              <div class="timeline-content">
                <div class="timeline-title">
                  {{ task.nodeName || '审批节点' }}
                  <el-tag size="small" :type="getApprovalStatusType(task.status)" effect="plain" style="margin-left: 8px">
                    {{ getApprovalStatusText(task.status) }}
                  </el-tag>
                </div>
                <div class="timeline-person">审批人：{{ task.assigneeName || '未知' }}</div>
                <div v-if="task.comment" class="timeline-comment">意见：{{ task.comment }}</div>
              </div>
            </el-timeline-item>
            
            <!-- 结束节点 -->
             <el-timeline-item 
              v-if="currentChange.status === 2" 
              :timestamp="formatDate(currentChange.approvedAt)" 
              placement="top" 
              type="success"
              icon="Check"
            >
              <div class="timeline-content">
                <div class="timeline-title">流程结束</div>
                <div class="timeline-desc">变更已生效</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMyContracts, getContractDetail } from '@/api/contract'
import { getInstanceHistory } from '@/api/workflow'
import type { Contract, ContractQuery, ApprovalTask } from '@/types'
import {
  myChanges,
  getChangeDetail,
  submitChange as submitChangeAPI,
  cancelChange as cancelChangeAPI,
  CHANGE_STATUS_OPTIONS,
  type ContractChangeVO
} from '@/api/contractChange'

const router = useRouter()
const activeTab = ref('create')

// ============ 发起变更相关 ============
const loadingContracts = ref(false)
const changeableContracts = ref<Contract[]>([])

const contractFilter = reactive({
  type: undefined as string | undefined,
  name: undefined as string | undefined
})

const contractPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 合同类型选项
const contractTypeOptions = [
  { value: 'TYPE_A', label: 'A类-工程施工' },
  { value: 'TYPE_B', label: 'B类-代维服务' },
  { value: 'TYPE_C', label: 'C类-IT服务' }
]

// 加载可变更的合同（自己起草的已生效合同）
const loadChangeableContracts = async () => {
  loadingContracts.value = true
  try {
    const params: ContractQuery = {
      pageNum: contractPagination.pageNum,
      pageSize: contractPagination.pageSize,
      status: 2, // 只显示已生效的合同
      type: contractFilter.type,
      name: contractFilter.name
    }
    const res = await getMyContracts(params)
    // 兼容不同的返回格式
    let contracts: Contract[] = []
    if (res.data?.records) {
      contracts = res.data.records
      contractPagination.total = res.data.total || 0
    } else if (res.data?.list) {
      contracts = res.data.list
      contractPagination.total = res.data.total || 0
    } else if (Array.isArray(res.data)) {
      contracts = res.data
      contractPagination.total = res.data.length
    } else {
      contracts = []
      contractPagination.total = 0
    }
    
    // 加载变更记录，为每个合同标记是否有进行中的变更
    try {
      const changesRes = await myChanges({ pageNum: 1, pageSize: 999 })
      // 兼容多种返回格式
      let changes: ContractChangeVO[] = []
      if (changesRes.data?.records) {
        changes = changesRes.data.records
      } else if (changesRes.data?.list) {
        changes = changesRes.data.list
      } else if (Array.isArray(changesRes.data)) {
        changes = changesRes.data
      }
      
      // 获取有进行中变更的合同ID（草稿0和审批中1）
      const pendingContractIds = new Set<number>()
      for (const change of changes) {
        if (change.status === 0 || change.status === 1) {
          pendingContractIds.add(change.contractId)
        }
      }
      
      // 过滤掉有进行中变更的合同
      contracts = contracts.filter(contract => !pendingContractIds.has(contract.id))
    } catch (e) {
      console.error('加载变更状态失败:', e)
    }
    
    changeableContracts.value = contracts
  } catch (error) {
    console.error('加载合同列表失败:', error)
    changeableContracts.value = []
    contractPagination.total = 0
  } finally {
    loadingContracts.value = false
  }
}

const resetContractFilter = () => {
  contractFilter.type = undefined
  contractFilter.name = undefined
  contractPagination.pageNum = 1
  loadChangeableContracts()
}

// 跳转到创建变更页面（使用起草页面）
const goToCreateChange = async (contract: Contract) => {
  try {
    // 获取合同详情以获取类型信息
    const res = await getContractDetail(contract.id)
    const contractDetail = res.data
    
    router.push({
      path: '/contract/draft',
      query: {
        changeContractId: contract.id.toString(), // 标记为变更模式，传入原合同ID
        mainType: contractDetail.type || contract.type || 'TYPE_A',
        subType: (contractDetail.attributes as any)?.subTypeCode || 'A1',
        changeMode: 'true' // 标记为变更模式
      }
    })
  } catch (error) {
    const err = error as { message?: string }
    ElMessage.error(err.message || '获取合同信息失败')
  }
}

// ============ 变更记录相关 ============
const loadingChanges = ref(false)
const changeTableData = ref<ContractChangeVO[]>([])

const changeFilter = reactive({
  status: undefined as number | undefined
})

const changePagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载变更记录（只显示自己的变更）
const loadChanges = async () => {
  loadingChanges.value = true
  try {
    const params = {
      pageNum: changePagination.pageNum,
      pageSize: changePagination.pageSize,
      status: changeFilter.status
    }
    const res = await myChanges(params)
    
    // 处理分页数据
    if (res.data) {
      if (res.data.records) {
        changeTableData.value = res.data.records
        changePagination.total = res.data.total || 0
      } else if (Array.isArray(res.data)) {
        changeTableData.value = res.data
        changePagination.total = res.data.length
      } else {
        changeTableData.value = []
        changePagination.total = 0
      }
    } else {
      changeTableData.value = []
      changePagination.total = 0
    }
  } catch (error) {
    const err = error as { message?: string }
    ElMessage.error(err.message || '加载变更记录失败')
  } finally {
    loadingChanges.value = false
  }
}

const resetChangeFilter = () => {
  changeFilter.status = undefined
  changePagination.pageNum = 1
  loadChanges()
}

// 查看变更详情
const drawerVisible = ref(false)
const currentChange = ref<ContractChangeVO | null>(null)
const approvalHistory = ref<ApprovalTask[]>([])
const loadingHistory = ref(false)

const viewChangeDetail = async (row: ContractChangeVO) => {
  try {
    const res = await getChangeDetail(row.id)
    currentChange.value = res.data
    drawerVisible.value = true
    
    // 加载审批历史
    loadingHistory.value = true
    try {
      const historyRes = await getInstanceHistory(row.id)
      approvalHistory.value = (Array.isArray(historyRes.data) ? historyRes.data : []) as ApprovalTask[]
    } catch (e) {
      console.error('获取审批历史失败:', e)
      approvalHistory.value = []
    } finally {
      loadingHistory.value = false
    }
  } catch (error) {
    const err = error as { message?: string }
    ElMessage.error(err.message || '获取详情失败')
  }
}

// 编辑变更（草稿状态）
const editChange = async (row: ContractChangeVO) => {
  try {
    // 获取原合同信息
    const contractRes = await getContractDetail(row.contractId)
    const contract = contractRes.data
    
    router.push({
      path: '/contract/draft',
      query: {
        changeId: row.id.toString(), // 变更ID
        changeContractId: row.contractId.toString(), // 原合同ID
        mainType: contract.type || 'TYPE_A',
        subType: (contract.attributes as any)?.subTypeCode || 'A1',
        changeMode: 'true' // 标记为变更模式
      }
    })
  } catch (error) {
    const err = error as { message?: string }
    ElMessage.error(err.message || '获取合同信息失败')
  }
}

// 提交变更
const submitChange = async (row: ContractChangeVO) => {
  try {
    await ElMessageBox.confirm('确定要提交此变更申请进行审批吗？', '提示', {
      type: 'warning'
    })
    
    await submitChangeAPI(row.id)
    ElMessage.success('提交成功，已进入审批流程')
    loadChanges()
  } catch (error) {
    if (error !== 'cancel') {
      const err = error as { message?: string }
      ElMessage.error(err.message || '提交失败')
    }
  }
}

// 撤销变更
const cancelChange = async (row: ContractChangeVO) => {
  try {
    await ElMessageBox.confirm('确定要撤销此变更申请吗？此操作不可恢复？', '警告', {
      type: 'warning'
    })
    
    await cancelChangeAPI(row.id)
    ElMessage.success('撤销成功')
    loadChanges()
  } catch (error) {
    if (error !== 'cancel') {
      const err = error as { message?: string }
      ElMessage.error(err.message || '撤销失败')
    }
  }
}

// 标签页切换
const handleTabChange = (tabName: string) => {
  if (tabName === 'create') {
    loadChangeableContracts()
  } else if (tabName === 'history') {
    loadChanges()
  }
}

// 辅助函数
const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    'TYPE_A': 'A类-工程施工',
    'TYPE_B': 'B类-代维服务',
    'TYPE_C': 'C类-IT服务'
  }
  return map[type] || type
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  }
  return map[status] || 'info'
}

// 审批状态翻译
const getApprovalStatusType = (status: number | undefined) => {
  const map: Record<number, string> = {
    0: 'warning', // 待审批
    1: 'success', // 通过
    2: 'danger',  // 驳回
    3: 'info'     // 转发
  }
  return status !== undefined ? (map[status] || 'info') : 'info'
}

const getApprovalStatusText = (status: number | undefined) => {
  const map: Record<number, string> = {
    0: '待审批',
    1: '已通过',
    2: '已驳回',
    3: '已转发'
  }
  return status !== undefined ? (map[status] || '未知') : '未知'
}

const formatAmount = (amount: number) => {
  if (amount === undefined || amount === null) return '0.00'
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatValue = (value: unknown) => {
  if (value === undefined || value === null) return '-'
  if (typeof value === 'string' && value.length > 100) {
    return value.substring(0, 100) + '...'
  }
  return String(value)
}

onMounted(async () => {
  try {
    await loadChangeableContracts()
  } catch (error) {
    console.error('初始化合同变更列表失败:', error)
  }
})
</script>

<style scoped lang="scss">
.contract-change-list {
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    .header-left {
      h2 {
        margin: 0;
        font-size: 20px;
      }
      
      .subtitle {
        margin: 5px 0 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-tip {
      color: #909399;
      font-size: 12px;
    }
  }

  .filter-form {
    margin-bottom: 20px;
  }
  
    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
    
    .amount-increase {
      color: #f56c6c;
      font-weight: 500;
    }
    
    .amount-decrease {
      color: #67c23a;
      font-weight: 500;
  }
  
  .change-detail {
    .detail-section {
      margin-top: 20px;
      
      .diff-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .amount-change {
          font-size: 14px;
          
          .increase {
            color: #f56c6c;
          }
          
          .decrease {
            color: #67c23a;
          }
        }
      }
      
      .before-value {
        color: #909399;
        text-decoration: line-through;
      }
      
      .after-value {
        color: #409eff;
        font-weight: 500;
      }
    }
  }
}
</style>
