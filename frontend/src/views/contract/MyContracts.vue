<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Contract, ContractQuery } from '@/types'
import { getMyContracts, deleteContract, submitContract } from '@/api/contract'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Contract[]>([])
const total = ref(0)

const queryParams = reactive<ContractQuery>({
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// 状态选项：使用 Integer 值
const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '审批中', value: 1 },
  { label: '已生效', value: 2 },
  { label: '已驳回', value: 3 }
]

// 合同类型映射
const contractTypeMap: Record<string, string> = {
  'TYPE_A': '工程施工合同',
  'TYPE_B': '代维服务合同',
  'TYPE_C': 'IT服务合同',
  'STATION_LEASE': '基站租赁', // 兼容旧数据
  'NETWORK_CONSTRUCTION': '网络建设',
  'EQUIPMENT_PURCHASE': '设备采购',
  'MAINTENANCE_SERVICE': '运维服务'
}

// 状态映射：使用 Integer 键
const statusMap: Record<number, { text: string; type: string }> = {
  0: { text: '草稿', type: 'info' },
  1: { text: '审批中', type: 'warning' },
  2: { text: '已生效', type: 'success' },
  3: { text: '已驳回', type: 'danger' },
  4: { text: '已终止', type: 'info' },
  5: { text: '待签署', type: 'warning' },
  6: { text: '已作废', type: 'danger' }
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    console.log('[MyContracts] 请求参数:', queryParams)
    const res = await getMyContracts(queryParams)
    console.log('[MyContracts] 返回数据:', res.data)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
    console.log('[MyContracts] 合同数量:', tableData.value.length, '总数:', total.value)
  } catch (err) {
    console.error('[MyContracts] 加载失败:', err)
    ElMessage.error('加载失败')
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
  } catch (err: any) {
    if (err !== 'cancel') {
      console.error(err)
      ElMessage.error(err.message || '提交失败')
    }
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
          <!-- 这里的 row.name 是后端返回的字段名，之前mock用contractName -->
          <el-link type="primary" @click="goToDetail(row.id)">
            {{ row.name || row.contractName }}
          </el-link>
          <el-tag v-if="row.isAiGenerated" size="small" type="success" style="margin-left: 8px">
            AI
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="contractType" label="类型" width="140">
        <template #default="{ row }">
          <!-- 后端返回 type，之前mock用contractType -->
          {{ contractTypeMap[row.type || row.contractType] || row.type }}
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="140" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ (row.amount || 0).toLocaleString() }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'">
            {{ statusMap[row.status]?.text || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180">
        <template #default="{ row }">
          <!-- 后端返回 updatedAt，之前mock用 updateTime -->
          {{ row.updatedAt ? new Date(row.updatedAt).toLocaleString() : (row.updateTime || '-') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="goToDetail(row.id)">查看</el-button>
          <!-- 只有草稿(0)或已驳回(3)可以编辑删除 -->
          <template v-if="row.status === 0 || row.status === 3">
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
