<template>
  <div class="dashboard-container">
    <!-- 顶部数据卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in stats" :key="index">
        <!-- 强制内联样式 background: #fff 确保卡片是白色的 -->
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }" style="background-color: #fff !important; color: #333 !important; border: 1px solid #e4e7ed;">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.lightColor, color: item.color }">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <!-- 强制文字颜色深灰 -->
              <div class="stat-title" style="color: #333 !important;">{{ item.title }}</div>
              <div class="stat-value" style="color: #333 !important;">{{ item.value }}</div>
            </div>
          </div>
          <div class="stat-footer" style="color: #333 !important; border-top: 1px solid #f0f0f0;">
            <span>较上周</span>
            <span :class="item.trend > 0 ? 'up' : 'down'">
              {{ Math.abs(item.trend) }}% 
              <el-icon><component :is="item.trend > 0 ? 'CaretTop' : 'CaretBottom'" /></el-icon>
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中间区域：快捷入口 + 任务列表 -->
    <el-row :gutter="20" class="mt-20">
      <!-- 左侧：快捷操作 -->
      <el-col :span="16">
        <el-card shadow="never" class="box-card" style="background-color: #fff;">
          <template #header>
            <div class="card-header">
              <span>快捷导航</span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-btn" @click="$router.push('/contract/create')">
              <div class="icon-box blue"><el-icon><DocumentAdd /></el-icon></div>
              <span>发起合同</span>
            </div>
            <div class="action-btn" @click="$router.push('/workflow/pending')">
              <div class="icon-box orange"><el-icon><Stamp /></el-icon></div>
              <span>待我审批</span>
            </div>
            <div class="action-btn" @click="$router.push('/contract/list')">
              <div class="icon-box green"><el-icon><Search /></el-icon></div>
              <span>合同查询</span>
            </div>
            <div class="action-btn" @click="$router.push('/contract/ai-generate')">
              <div class="icon-box purple"><el-icon><MagicStick /></el-icon></div>
              <span>AI 起草</span>
            </div>
          </div>
        </el-card>

        <!-- 模拟图表区 -->
        <el-card shadow="never" class="box-card mt-20" style="background-color: #fff;">
          <template #header>
            <div class="card-header">
              <span>合同状态分布</span>
            </div>
          </template>
          <div class="chart-mock">
            <div class="chart-bar" v-for="i in 4" :key="i">
              <div class="label-row">
                <span class="label">类别 {{ i }}</span>
                <span class="value">{{ Math.floor(Math.random() * 80) + 10 }}%</span>
              </div>
              <el-progress 
                :percentage="Math.floor(Math.random() * 80) + 10" 
                :color="colors[i-1]"
                :stroke-width="12"
                :show-text="false"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：待办事项 -->
      <el-col :span="8">
        <el-card shadow="never" class="box-card" style="background-color: #fff;">
          <template #header>
            <div class="card-header">
              <span>待办任务</span>
              <el-link type="primary" :underline="false" @click="$router.push('/workflow/pending')">更多</el-link>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="i in 5" :key="i" class="todo-item">
              <div class="todo-tag">审批</div>
              <div class="todo-content">
                <div class="todo-title">关于采购华为服务器的合同审批</div>
                <div class="todo-time">2025-12-15 10:30</div>
              </div>
              <el-button type="primary" link size="small">去处理</el-button>
            </div>
          </div>
        </el-card>

        <!-- 系统公告 -->
        <el-card shadow="never" class="box-card mt-20" style="background-color: #fff;">
          <template #header>
            <div class="card-header">
              <span>系统公告</span>
            </div>
          </template>
          <div class="notice-list">
             <div class="notice-item">
               <span class="dot"></span>
               <span class="text">系统将于本周五晚进行维护升级</span>
             </div>
             <div class="notice-item">
               <span class="dot"></span>
               <span class="text">关于启用新版合同模板的通知</span>
             </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { 
  DocumentAdd, 
  Stamp, 
  Search, 
  MagicStick,
  CaretTop,
  CaretBottom,
  User,
  Tickets,
  Timer,
  Finished
} from '@element-plus/icons-vue'

const stats = [
  { title: '待办任务', value: '12', icon: 'Timer', color: '#ff9c6e', lightColor: '#fff7e6', trend: 15 },
  { title: '已办任务', value: '48', icon: 'Finished', color: '#5cdbd3', lightColor: '#e6fffb', trend: -5 },
  { title: '本月发起', value: '8', icon: 'DocumentAdd', color: '#597ef7', lightColor: '#f0f5ff', trend: 10 },
  { title: '用户总数', value: '1,203', icon: 'User', color: '#b37feb', lightColor: '#f9f0ff', trend: 2 }
]

const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c']
</script>

<style scoped>
.mt-20 {
  margin-top: 20px;
}

/* 基础卡片样式 */
.stat-card {
  border-radius: 8px;
  /* 移除这里的 background，直接在 template 内联样式控制，优先级更高 */
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.stat-content {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.stat-info .stat-title {
  font-size: 14px;
  margin-bottom: 8px;
  font-weight: 500;
  /* 颜色已在 template 内联控制 */
}

.stat-info .stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  /* 颜色已在 template 内联控制 */
}

.stat-footer {
  font-size: 13px;
  display: flex;
  justify-content: space-between;
  padding-top: 16px;
  /* 颜色和边框已在 template 内联控制 */
}

.up { color: #ff0000; display: flex; align-items: center; gap: 4px; font-weight: 600; }
.down { color: #67c23a; display: flex; align-items: center; gap: 4px; font-weight: 600; }

/* 快捷入口 */
.quick-actions {
  display: flex;
  gap: 40px;
  padding: 10px 0;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s;
  color: #606266;
}

.action-btn:hover {
  transform: translateY(-5px);
  color: #1890ff;
}

.icon-box {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.blue { background: linear-gradient(135deg, #409eff, #096dd9); }
.orange { background: linear-gradient(135deg, #ffbf00, #fa8c16); }
.green { background: linear-gradient(135deg, #95de64, #52c41a); }
.purple { background: linear-gradient(135deg, #b37feb, #722ed1); }

/* 图表模拟 */
.chart-mock {
  padding: 10px 0;
}
.chart-bar {
  margin-bottom: 20px;
}
.label-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}

/* 待办列表 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-tag {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 12px;
}

.todo-content {
  flex: 1;
  overflow: hidden;
  margin-right: 10px;
}

.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-time {
  font-size: 12px;
  color: #909399;
}

/* 公告 */
.notice-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px dashed #f0f0f0;
}

.notice-item:last-child {
  border-bottom: none;
}

.dot {
  width: 6px;
  height: 6px;
  background: #d9d9d9;
  border-radius: 50%;
  margin-right: 10px;
}
</style>
