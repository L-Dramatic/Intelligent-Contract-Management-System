<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { OfficeBuilding } from '@element-plus/icons-vue'

// 部门统计数据
const deptStats = ref([
  { name: 'A市网络部', total: 32, pending: 3, approved: 28, rejected: 1 },
  { name: 'A市政企部', total: 25, pending: 2, approved: 22, rejected: 1 },
  { name: 'A市法务部', total: 18, pending: 1, approved: 17, rejected: 0 },
  { name: 'A市财务部', total: 15, pending: 2, approved: 12, rejected: 1 },
  { name: 'C县分公司', total: 28, pending: 4, approved: 23, rejected: 1 },
  { name: 'D县分公司', total: 22, pending: 2, approved: 19, rejected: 1 }
])

const loading = ref(false)

onMounted(() => {
  // TODO: 从后端API获取数据
})
</script>

<template>
  <div class="dept-stats-page">
    <h2 class="page-title">部门统计</h2>
    
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><OfficeBuilding /></el-icon>
            各部门合同审批统计
          </span>
          <el-button type="primary" size="small" plain>导出报表</el-button>
        </div>
      </template>
      
      <el-table :data="deptStats" v-loading="loading" stripe>
        <el-table-column prop="name" label="部门名称" min-width="150">
          <template #default="{ row }">
            <span class="dept-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="total" label="合同总数" width="120" align="center">
          <template #default="{ row }">
            <span class="count total">{{ row.total }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="pending" label="待审批" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="warning" effect="light">{{ row.pending }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="approved" label="已通过" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success" effect="light">{{ row.approved }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="rejected" label="已驳回" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="danger" effect="light">{{ row.rejected }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="通过率" width="150" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="Math.round(row.approved / row.total * 100)" 
              :stroke-width="8"
              :color="row.approved / row.total > 0.9 ? '#67c23a' : '#e6a23c'"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" align="center">
          <template #default>
            <el-button type="primary" link size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.dept-stats-page {
  padding: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dept-name {
  font-weight: 500;
}

.count.total {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}
</style>

