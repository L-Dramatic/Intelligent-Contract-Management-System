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
            <el-option label="工程施工合同" value="TYPE_A" />
            <el-option label="代维服务合同" value="TYPE_B" />
            <el-option label="IT服务合同" value="TYPE_C" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 180px">
            <el-option label="草稿" :value="0" />
            <el-option label="审批中" :value="1" />
            <el-option label="已生效" :value="2" />
            <el-option label="已驳回" :value="3" />
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
        <el-table-column prop="contractNo" label="合同编号" width="200" fixed />
        <el-table-column label="合同名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row.id)">
              {{ row.name || row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="140">
          <template #default="{ row }">
            <el-tag>{{ formatType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额 (元)" width="150" align="right">
          <template #default="{ row }">
            ¥{{ row.amount?.toLocaleString() || '0' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="plain">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorId" label="创建人ID" width="100" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createdAt || row.createTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.id)">详情</el-button>
            <el-button 
              v-if="row.status === 0 || row.status === 'DRAFT'" 
              link 
              type="primary" 
              @click="editContract(row.id)"
            >编辑</el-button>
            <el-button 
              v-if="row.status === 0 || row.status === 'DRAFT'" 
              link 
              type="danger" 
              @click="doDeleteContract(row.id)"
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContractList, deleteContract as deleteContractApi } from '@/api/contract'

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

const tableData = ref<any[]>([])

// 加载真实数据
const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getContractList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      name: searchForm.keyword || undefined,
      type: searchForm.type || undefined
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载合同列表失败:', error)
    ElMessage.error('加载合同列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.status = ''
  currentPage.value = 1
  handleSearch()
}

onMounted(() => {
  handleSearch()
})

const viewDetail = (id: number) => {
  router.push(`/contract/detail/${id}`)
}

const editContract = (id: number) => {
  router.push(`/contract/edit/${id}`)
}

const doDeleteContract = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该合同吗？', '警告', {
      type: 'warning'
    })
    await deleteContractApi(id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 辅助函数 - 匹配后端返回的数据格式
const formatType = (type: string) => {
  const map: Record<string, string> = {
    'TYPE_A': '工程施工合同',
    'TYPE_B': '代维服务合同',
    'TYPE_C': 'IT服务合同',
    'PURCHASE': '采购合同',
    'SALES': '销售合同',
    'LEASE': '租赁合同'
  }
  return map[type] || type
}

// 后端返回数字状态
const formatStatus = (status: number | string) => {
  const map: Record<number | string, string> = {
    0: '草稿',
    1: '审批中',
    2: '已生效',
    3: '已驳回',
    'DRAFT': '草稿',
    'PENDING': '审批中',
    'ACTIVE': '已生效',
    'REJECTED': '已驳回'
  }
  return map[status] || String(status)
}

const getStatusType = (status: number | string) => {
  const map: Record<number | string, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    'DRAFT': 'info',
    'PENDING': 'warning',
    'ACTIVE': 'success',
    'REJECTED': 'danger'
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
