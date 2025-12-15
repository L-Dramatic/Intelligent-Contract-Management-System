<template>
  <div class="page-container">
    <!-- 1. 搜索筛选区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="合同名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable />
        </el-form-item>
        <el-form-item label="合同类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 180px">
            <el-option label="采购合同" value="PURCHASE" />
            <el-option label="销售合同" value="SALES" />
            <el-option label="租赁合同" value="LEASE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 180px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="审批中" value="PENDING" />
            <el-option label="已生效" value="ACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 2. 表格内容区 -->
    <el-card shadow="never" class="table-card mt-20">
      <!-- 工具栏 -->
      <div class="table-toolbar">
        <div class="left">
          <h3 class="title">合同列表</h3>
        </div>
        <div class="right">
          <el-button type="primary" @click="$router.push('/contract/create')">
            <el-icon><Plus /></el-icon> 新建合同
          </el-button>
          <el-button plain>
            <el-icon><Download /></el-icon> 导出
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table 
        v-loading="loading"
        :data="tableData" 
        style="width: 100%" 
        border
        :header-cell-style="{ background: '#fafafa', color: '#606266' }"
      >
        <el-table-column prop="contractNo" label="合同编号" width="180" fixed />
        <el-table-column prop="title" label="合同名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ formatType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额 (元)" width="150" align="right">
          <template #default="{ row }">
            {{ row.amount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="plain">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.id)">详情</el-button>
            <el-button 
              v-if="row.status === 'DRAFT'" 
              link 
              type="primary" 
              @click="editContract(row.id)"
            >编辑</el-button>
            <el-button 
              v-if="row.status === 'DRAFT'" 
              link 
              type="danger" 
              @click="deleteContract(row.id)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  type: '',
  status: ''
})

// 模拟数据 (后面换成 API 调用)
const tableData = ref([
  {
    id: 1,
    contractNo: 'HT-20251215-001',
    title: '基站租赁合同-朝阳区01号站',
    type: 'LEASE',
    amount: 50000,
    status: 'PENDING',
    createTime: '2025-12-15 10:00:00'
  },
  {
    id: 2,
    contractNo: 'HT-20251215-002',
    title: '华为服务器采购合同',
    type: 'PURCHASE',
    amount: 1200000,
    status: 'DRAFT',
    createTime: '2025-12-15 11:30:00'
  }
])

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    total.value = 2 // Mock total
  }, 500)
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.status = ''
  handleSearch()
}

const viewDetail = (id: number) => {
  router.push(`/contract/detail/${id}`)
}

const editContract = (id: number) => {
  router.push(`/contract/edit/${id}`)
}

const deleteContract = (id: number) => {
  ElMessageBox.confirm('确认删除该合同吗？', '警告', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

// 辅助函数
const formatType = (type: string) => {
  const map: Record<string, string> = {
    PURCHASE: '采购合同',
    SALES: '销售合同',
    LEASE: '租赁合同'
  }
  return map[type] || type
}

const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '审批中',
    ACTIVE: '已生效',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning',
    ACTIVE: 'success',
    REJECTED: 'danger'
  }
  return map[status] || ''
}
</script>

<style scoped>
.search-card {
  border: none;
  border-radius: 4px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.table-card {
  border: none;
  border-radius: 4px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  padding-left: 10px;
  border-left: 4px solid #1890ff; /* 标题左侧蓝条 */
  line-height: 1;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
