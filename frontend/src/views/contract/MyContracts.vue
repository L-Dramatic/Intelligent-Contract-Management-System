<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Contract, ContractQuery, ContractStatus } from '@/types'
import { getMyContracts, deleteContract, submitContract } from '@/api/contract'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Contract[]>([])
const total = ref(0)

const queryParams = reactive<ContractQuery>({
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const statusOptions: { label: string; value: ContractStatus }[] = [
  { label: '草稿', value: 'DRAFT' },
  { label: '审批中', value: 'PENDING' },
  { label: '已生效', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const contractTypeMap: Record<string, string> = {
  'STATION_LEASE': '基站租赁',
  'NETWORK_CONSTRUCTION': '网络建设',
  'EQUIPMENT_PURCHASE': '设备采购',
  'MAINTENANCE_SERVICE': '运维服务'
}

const statusMap: Record<string, { text: string; type: string }> = {
  'DRAFT': { text: '草稿', type: 'info' },
  'PENDING': { text: '审批中', type: 'warning' },
  'APPROVED': { text: '已生效', type: 'success' },
  'REJECTED': { text: '已驳回', type: 'danger' }
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyContracts(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        contractNo: 'HT-ZL-20251201-001',
        contractName: '某大厦5G基站租赁合同',
        contractType: 'STATION_LEASE',
        partyA: '中国电信股份有限公司',
        partyB: 'XX物业管理公司',
        amount: 50000,
        content: '',
        status: 'DRAFT',
        isAiGenerated: true,
        creatorId: 1,
        creatorName: '我',
        createTime: '2025-12-01 10:30:00',
        updateTime: '2025-12-01 10:30:00'
      },
      {
        id: 2,
        contractNo: 'HT-JS-20251202-002',
        contractName: '某区5G网络建设合同',
        contractType: 'NETWORK_CONSTRUCTION',
        partyA: '中国电信股份有限公司',
        partyB: '华为技术有限公司',
        amount: 8000000,
        content: '',
        status: 'PENDING',
        isAiGenerated: false,
        creatorId: 1,
        creatorName: '我',
        createTime: '2025-12-02 14:20:00',
        updateTime: '2025-12-03 09:15:00'
      }
    ]
    total.value = 2
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

const goToDetail = (id: number) => {
  router.push(`/contract/detail/${id}`)
}

const goToEdit = (id: number) => {
  router.push(`/contract/edit/${id}`)
}

const goToCreate = () => {
  router.push('/contract/create')
}

const handleSubmit = async (row: Contract) => {
  try {
    await ElMessageBox.confirm('确定要提交该合同进行审批吗？', '提交审批', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await submitContract(row.id)
    ElMessage.success('已提交审批')
    loadData()
  } catch {
    // 取消或失败
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该合同吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteContract(id)
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
      <h2 class="page-title">我的合同</h2>
      <el-button type="primary" @click="goToCreate">
        <el-icon><Plus /></el-icon>
        创建合同
      </el-button>
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
      <el-table-column prop="contractNo" label="合同编号" width="200" />
      <el-table-column prop="contractName" label="合同名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="goToDetail(row.id)">
            {{ row.contractName }}
          </el-link>
          <el-tag v-if="row.isAiGenerated" size="small" type="success" style="margin-left: 8px">
            AI
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="contractType" label="类型" width="100">
        <template #default="{ row }">
          {{ contractTypeMap[row.contractType] }}
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="140" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ row.amount.toLocaleString() }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type as any">
            {{ statusMap[row.status]?.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="goToDetail(row.id)">查看</el-button>
          <template v-if="row.status === 'DRAFT'">
            <el-button type="primary" link @click="goToEdit(row.id)">编辑</el-button>
            <el-button type="success" link @click="handleSubmit(row)">提交</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { Plus } from '@element-plus/icons-vue'
export default {
  components: { Plus }
}
</script>

<style scoped>
.filter-bar {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.amount {
  font-weight: 500;
  color: #f56c6c;
}
</style>

