<template>
  <div class="contract-change-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2>📋 合同变更管理</h2>
        <p class="subtitle">查看和管理所有合同变更申请</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showChangeableContracts">
          <el-icon><Plus /></el-icon> 发起变更
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="变更状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option
              v-for="item in CHANGE_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="仅看我的">
          <el-switch v-model="filterForm.onlyMine" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
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
        <el-table-column prop="initiatorName" label="发起人" width="100" />
        <el-table-column prop="createdAt" label="发起时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              size="small"
              @click="doSubmit(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              link
              size="small"
              @click="doCancel(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 可变更合同选择对话框 -->
    <el-dialog v-model="contractSelectVisible" title="选择要变更的合同" width="800px">
      <p class="dialog-tip">以下是您有权限变更的已生效合同，请选择一个进行变更：</p>
      <el-table 
        :data="changeableContracts" 
        v-loading="loadingContracts"
        border 
        stripe
        empty-text="暂无可变更的合同"
      >
        <el-table-column prop="contractNo" label="合同编号" width="180" />
        <el-table-column prop="name" label="合同名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ contractTypeMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.amount?.toLocaleString() || '0' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="goToCreateChange(row.id)">
              发起变更
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

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

        <!-- 审批时间线 -->
        <el-card v-if="currentChange.approvedAt || currentChange.effectiveAt" class="detail-section" shadow="never">
          <template #header>时间线</template>
          <el-timeline>
            <el-timeline-item :timestamp="formatDate(currentChange.createdAt)" placement="top">
              发起变更申请
            </el-timeline-item>
            <el-timeline-item v-if="currentChange.approvedAt" :timestamp="formatDate(currentChange.approvedAt)" placement="top" type="success">
              审批通过
            </el-timeline-item>
            <el-timeline-item v-if="currentChange.effectiveAt" :timestamp="formatDate(currentChange.effectiveAt)" placement="top" type="primary">
              变更生效
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
import { Search, Plus } from '@element-plus/icons-vue'
import {
  listChanges,
  myChanges,
  getChangeDetail,
  submitChange,
  cancelChange,
  getChangeableContracts,
  CHANGE_STATUS_OPTIONS,
  type ContractChangeVO
} from '@/api/contractChange'

const router = useRouter()
const loading = ref(false)

// 可变更合同选择
const contractSelectVisible = ref(false)
const changeableContracts = ref<any[]>([])
const loadingContracts = ref(false)

const contractTypeMap: Record<string, string> = {
  'TYPE_A': '工程施工合同',
  'TYPE_B': '代维服务合同',
  'TYPE_C': 'IT服务合同'
}

// 显示可变更合同列表
const showChangeableContracts = async () => {
  contractSelectVisible.value = true
  loadingContracts.value = true
  try {
    const res = await getChangeableContracts()
    changeableContracts.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '获取可变更合同失败')
    changeableContracts.value = []
  } finally {
    loadingContracts.value = false
  }
}

// 跳转到创建变更页面
const goToCreateChange = (contractId: number) => {
  contractSelectVisible.value = false
  router.push(`/contract/change/create/${contractId}`)
}
const tableData = ref<ContractChangeVO[]>([])
const drawerVisible = ref(false)
const currentChange = ref<ContractChangeVO | null>(null)

// 筛选条件
const filterForm = reactive({
  status: undefined as number | undefined,
  onlyMine: false
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: filterForm.status
    }
    
    const res = filterForm.onlyMine
      ? await myChanges(params)
      : await listChanges(params)
    
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.status = undefined
  filterForm.onlyMine = false
  pagination.pageNum = 1
  loadData()
}

// 查看详情
const viewDetail = async (row: ContractChangeVO) => {
  try {
    const res = await getChangeDetail(row.id)
    currentChange.value = res.data
    drawerVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取详情失败')
  }
}

// 提交审批
const doSubmit = async (row: ContractChangeVO) => {
  try {
    await ElMessageBox.confirm('确定要提交此变更申请吗？', '提示', {
      type: 'warning'
    })
    
    await submitChange(row.id)
    ElMessage.success('提交成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交失败')
    }
  }
}

// 撤销
const doCancel = async (row: ContractChangeVO) => {
  try {
    await ElMessageBox.confirm('确定要撤销此变更申请吗？此操作不可恢复。', '警告', {
      type: 'warning'
    })
    
    await cancelChange(row.id)
    ElMessage.success('撤销成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤销失败')
    }
  }
}

// 辅助函数
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

const formatAmount = (amount: number) => {
  if (amount === undefined || amount === null) return '0.00'
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatValue = (value: any) => {
  if (value === undefined || value === null) return '-'
  if (typeof value === 'string' && value.length > 100) {
    return value.substring(0, 100) + '...'
  }
  return value
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.contract-change-list {
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
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
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .table-card {
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
  }
  
  .dialog-tip {
    color: #606266;
    font-size: 14px;
    margin-bottom: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
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

