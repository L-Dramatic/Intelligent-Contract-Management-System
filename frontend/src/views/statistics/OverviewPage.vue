<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { TrendCharts, DataLine, Document, Checked, Timer } from '@element-plus/icons-vue'

// 统计数据
const stats = ref({
  totalContracts: 0,
  pendingApproval: 0,
  approvedThisMonth: 0,
  avgApprovalTime: 0
})

// 加载统计数据
onMounted(() => {
  // TODO: 从后端API获取数据
  // 模拟数据
  stats.value = {
    totalContracts: 156,
    pendingApproval: 12,
    approvedThisMonth: 28,
    avgApprovalTime: 2.5
  }
})
</script>

<template>
  <div class="statistics-page">
    <h2 class="page-title">审批概览</h2>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalContracts }}</div>
            <div class="stat-label">合同总数</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
            <el-icon :size="28"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingApproval }}</div>
            <div class="stat-label">待审批</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
            <el-icon :size="28"><Checked /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.approvedThisMonth }}</div>
            <div class="stat-label">本月已审批</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7)">
            <el-icon :size="28"><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.avgApprovalTime }}天</div>
            <div class="stat-label">平均审批时长</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">合同审批趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表开发中...">
              <template #image>
                <el-icon :size="60" color="#909399"><TrendCharts /></el-icon>
              </template>
            </el-empty>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">合同类型分布</span>
          </template>
          <div class="chart-placeholder">
            <div class="type-list">
              <div class="type-item">
                <span class="type-dot" style="background: #667eea"></span>
                <span class="type-name">工程施工类</span>
                <span class="type-count">45份</span>
              </div>
              <div class="type-item">
                <span class="type-dot" style="background: #f5576c"></span>
                <span class="type-name">代维服务类</span>
                <span class="type-count">62份</span>
              </div>
              <div class="type-item">
                <span class="type-dot" style="background: #4facfe"></span>
                <span class="type-name">IT/DICT类</span>
                <span class="type-count">49份</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.statistics-page {
  padding: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-section {
  margin-bottom: 24px;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-list {
  width: 100%;
  padding: 20px 0;
}

.type-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.type-item:last-child {
  border-bottom: none;
}

.type-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 12px;
}

.type-name {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.type-count {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
</style>

