<template>
  <div class="dashboard-container">
    <!-- 欢迎区域（所有角色都显示） -->
    <el-card class="welcome-card" style="margin-bottom: 20px;">
      <div class="welcome-content">
        <div class="welcome-text">
          <h3 class="greeting">你好，{{ displayName }}</h3>
          <p class="welcome-subtitle">欢迎使用智慧合同管理系统</p>
          <div class="user-info-line" v-if="isBusinessUser">
            <el-tag type="info" effect="plain" size="small">
              <el-icon><OfficeBuilding /></el-icon>
              {{ userStore.userInfo?.departmentName || '未分配部门' }}
            </el-tag>
            <el-tag type="success" effect="plain" size="small">
              <el-icon><User /></el-icon>
              {{ primaryRoleName }}
            </el-tag>
          </div>
        </div>
        <el-button type="primary" @click="goToProfile">
          <el-icon><User /></el-icon>
          个人主页
        </el-button>
      </div>
    </el-card>

    <!-- 管理员工作台 -->
    <template v-if="isAdmin">
      <!-- 顶部数据卡片 -->
      <el-row :gutter="20">
        <el-col :span="6" v-for="(item, index) in adminStats" :key="index">
          <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }" style="background-color: #fff !important; color: #333 !important; border: 1px solid #e4e7ed;">
            <div class="stat-content">
              <div class="stat-icon" :style="{ backgroundColor: item.lightColor, color: item.color }">
                <el-icon :size="24"><component :is="item.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title" style="color: #333 !important;">{{ item.title }}</div>
                <div class="stat-value" style="color: #333 !important;">{{ item.value }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 快捷导航（管理员专用） -->
      <el-card shadow="never" class="box-card" style="background-color: #fff; margin-top: 20px; margin-bottom: 20px;">
        <template #header>
          <div class="card-header">
            <span>管理功能</span>
          </div>
        </template>
        <div class="quick-actions">
          <div class="action-btn" @click="$router.push('/system/user')">
            <div class="icon-box blue"><el-icon><User /></el-icon></div>
            <span>用户管理</span>
          </div>
          <div class="action-btn" @click="$router.push('/system/department')">
            <div class="icon-box green"><el-icon><OfficeBuilding /></el-icon></div>
            <span>组织架构</span>
          </div>
          <div class="action-btn" @click="$router.push('/system/knowledge')">
            <div class="icon-box orange"><el-icon><Reading /></el-icon></div>
            <span>知识库管理</span>
          </div>
          <div class="action-btn" @click="$router.push('/workflow-admin')">
            <div class="icon-box purple"><el-icon><Setting /></el-icon></div>
            <span>流程管理</span>
          </div>
        </div>
      </el-card>

      <!-- 中间区域：系统状态 + 最近活动 -->
      <el-row :gutter="20" class="mt-20">
        <!-- 左侧：系统状态 -->
        <el-col :span="12">
          <el-card shadow="never" class="box-card" style="background-color: #fff;" v-loading="loadingSystemStatus">
            <template #header>
              <div class="card-header">
                <span>系统运行状态</span>
              </div>
            </template>
            <div class="system-status">
              <div class="status-item" v-for="status in systemStatus" :key="status.name">
                <div class="status-label">
                  <el-icon :style="{ color: status.color }"><component :is="status.icon" /></el-icon>
                  <span>{{ status.name }}</span>
                </div>
                <div class="status-value" :style="{ color: status.color }">
                  <el-tag :type="status.tagType" size="small">{{ status.value }}</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：最近活动 -->
        <el-col :span="12">
          <el-card shadow="never" class="box-card" style="background-color: #fff;">
            <template #header>
              <div class="card-header">
                <span>最近活动</span>
              </div>
            </template>
            <div class="activity-list">
              <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
                <div class="activity-icon" :style="{ backgroundColor: activity.color }">
                  <el-icon><component :is="activity.icon" /></el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-text">{{ activity.text }}</div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 非业务人员提示（非管理员） -->
    <el-card v-else-if="!isBusinessUser" style="margin-bottom: 20px;">
      <el-result
        icon="info"
        title="工作台仅对起草和审批人员开放"
        sub-title="您当前的角色无法使用此功能，请联系管理员"
      />
    </el-card>

    <!-- 业务人员工作台 -->
    <template v-else>
    <!-- 顶部数据卡片 -->
    <el-row :gutter="20">
      <el-col :span="8" v-for="(item, index) in stats" :key="index">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }" style="background-color: #fff !important; color: #333 !important; border: 1px solid #e4e7ed;">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.lightColor, color: item.color }">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title" style="color: #333 !important;">{{ item.title }}</div>
              <div class="stat-value" style="color: #333 !important;">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷导航（业务人员专用） -->
    <el-card shadow="never" class="box-card" style="background-color: #fff; margin-top: 20px; margin-bottom: 20px;">
          <template #header>
            <div class="card-header">
              <span>快捷导航</span>
            </div>
          </template>
          <div class="quick-actions">
            <!-- 县级人员：发起合同 -->
            <div v-if="isCountyUser" class="action-btn" @click="$router.push('/contract/create')">
              <div class="icon-box blue"><el-icon><DocumentAdd /></el-icon></div>
              <span>发起合同</span>
            </div>
            <!-- 县级人员：我的合同 -->
            <div v-if="isCountyUser" class="action-btn" @click="$router.push('/contract/my')">
              <div class="icon-box green"><el-icon><Folder /></el-icon></div>
              <span>我的合同</span>
            </div>
            <!-- 市级及以上：待我审批 -->
            <div v-if="!isCountyUser" class="action-btn" @click="$router.push('/workflow/pending')">
              <div class="icon-box orange"><el-icon><Stamp /></el-icon></div>
              <span>待我审批</span>
            </div>
            <!-- 市级及以上：已办任务 -->
            <div v-if="!isCountyUser" class="action-btn" @click="$router.push('/workflow/completed')">
              <div class="icon-box green"><el-icon><Finished /></el-icon></div>
              <span>已办任务</span>
            </div>
            <!-- 所有人：AI问答 -->
            <div class="action-btn" @click="goToAIQA">
              <div class="icon-box purple"><el-icon><ChatDotRound /></el-icon></div>
              <span>AI问答</span>
            </div>
          </div>
        </el-card>

    <!-- 中间区域：任务列表 + 系统公告 -->
    <el-row :gutter="20" class="mt-20" v-if="isBusinessUser">
      <!-- 左侧：系统公告 -->
      <el-col :span="12">
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

      <!-- 右侧：待办任务 -->
      <el-col :span="12">
        <el-card shadow="never" class="box-card" style="background-color: #fff;" v-loading="loadingTasks">
          <template #header>
            <div class="card-header">
              <span>待办任务</span>
              <el-link type="primary" :underline="false" @click="$router.push('/workflow/pending')">更多</el-link>
            </div>
          </template>
          <div class="todo-list">
            <div v-if="todoTasks.length === 0" class="empty-todos">
              <el-empty description="暂无待办任务" :image-size="80" />
            </div>
            <div v-for="task in todoTasks" :key="task.id" class="todo-item">
              <div class="todo-tag">审批</div>
              <div class="todo-content">
                <div class="todo-title">{{ task.contractName || '合同审批' }}</div>
                <div class="todo-time">{{ formatTaskTime(task) }}</div>
              </div>
              <el-button type="primary" link size="small" @click="goToTask(task)">去处理</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  DocumentAdd, 
  Stamp, 
  Search, 
  ChatDotRound,
  Timer,
  Finished,
  User,
  OfficeBuilding,
  Reading,
  Setting,
  Monitor,
  Connection,
  DataLine,
  CircleCheck,
  Warning,
  Folder
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyPendingTasks, getMyCompletedTasks } from '@/api/workflow'
import { getMyContracts } from '@/api/contract'
import { getUserList } from '@/api/user'
import { getDepartmentTree } from '@/api/department'
import { getKnowledgeFiles } from '@/api/knowledge'
import { getContractList } from '@/api/contract'
import type { ApprovalTask } from '@/types'

const router = useRouter()
const userStore = useUserStore()

// 显示名称（优先显示realName，否则显示username）
const displayName = computed(() => {
  return userStore.userInfo?.realName || userStore.username || '用户'
})

// 检查是否为管理员
const isAdmin = computed(() => {
  return userStore.role === 'ADMIN'
})

// 检查是否为业务人员（县级/市级/省级/领导层）
const isBusinessUser = computed(() => {
  const role = userStore.role
  return ['COUNTY', 'CITY', 'PROVINCE', 'BOSS'].includes(role)
})

const isCountyUser = computed(() => {
  return userStore.role === 'COUNTY'
})

// 职位名称映射
const primaryRoleName = computed(() => {
  const roleMap: Record<string, string> = {
    'DEPT_MANAGER': '部门经理',
    'NETWORK_ENGINEER': '网络工程师',
    'LEGAL_REVIEWER': '法务审查员',
    'FINANCE_AUDITOR': '财务审核员',
    'DICT_PM': '政企项目经理',
    'CUSTOMER_SERVICE_LEAD': '客服主管',
    'FACILITY_COORDINATOR': '设施协调员',
    'INITIATOR': '合同发起人'
  }
  const primaryRole = userStore.userInfo?.primaryRole
  return primaryRole ? (roleMap[primaryRole] || primaryRole) : '未分配职位'
})

// 跳转到个人主页
const goToProfile = () => {
  router.push('/profile')
}

// 跳转到AI问答
const goToAIQA = () => {
  try {
    router.push({ name: 'AIQAChat' })
  } catch (error) {
    console.error('跳转到AI问答失败', error)
    ElMessage.error('无法打开AI问答页面')
  }
}

// 业务人员统计数据
const stats = reactive([
  { title: '待办任务', value: '0', icon: 'Timer', color: '#ff9c6e', lightColor: '#fff7e6' },
  { title: '已办任务', value: '0', icon: 'Finished', color: '#5cdbd3', lightColor: '#e6fffb' },
  { title: '本月发起', value: '0', icon: 'DocumentAdd', color: '#597ef7', lightColor: '#f0f5ff' }
])

// 管理员统计数据
const adminStats = reactive([
  { title: '用户总数', value: '0', icon: 'User', color: '#409eff', lightColor: '#e6f4ff' },
  { title: '部门总数', value: '0', icon: 'OfficeBuilding', color: '#67c23a', lightColor: '#f0f9ff' },
  { title: '知识库文件', value: '0', icon: 'Reading', color: '#e6a23c', lightColor: '#fef7e6' },
  { title: '合同总数', value: '0', icon: 'DocumentAdd', color: '#f56c6c', lightColor: '#fef0f0' }
])

// 系统运行状态（模拟数据）
const loadingSystemStatus = ref(false)
const systemStatus = reactive([
  { name: '数据库连接', value: '正常', color: '#67c23a', tagType: 'success', icon: 'Connection' },
  { name: 'AI服务', value: '运行中', color: '#67c23a', tagType: 'success', icon: 'Monitor' },
  { name: '系统负载', value: '正常', color: '#67c23a', tagType: 'success', icon: 'DataLine' },
  { name: '存储空间', value: '充足', color: '#67c23a', tagType: 'success', icon: 'CircleCheck' }
])

// 最近活动（模拟数据）
const recentActivities = reactive([
  { text: '用户 张三 创建了新合同', time: '2分钟前', icon: 'DocumentAdd', color: '#409eff' },
  { text: '李四 提交了合同审批', time: '15分钟前', icon: 'Stamp', color: '#67c23a' },
  { text: '王五 更新了知识库文件', time: '1小时前', icon: 'Reading', color: '#e6a23c' },
  { text: '系统自动备份完成', time: '2小时前', icon: 'CircleCheck', color: '#909399' },
  { text: '管理员 添加了新部门', time: '3小时前', icon: 'OfficeBuilding', color: '#f56c6c' }
])

// 待办任务列表
const loadingTasks = ref(false)
const todoTasks = ref<ApprovalTask[]>([])

// 加载统计数据
const loadStats = async () => {
  try {
    // 加载待办任务数量（后端直接返回数组）
    const pendingRes = await getMyPendingTasks({ pageNum: 1, pageSize: 1000 })
    const pendingTasks = Array.isArray(pendingRes.data) 
      ? pendingRes.data
      : (pendingRes.data as any)?.records || []
    stats[0].value = pendingTasks.length.toString()

    // 加载已办任务数量（后端直接返回数组）
    const completedRes = await getMyCompletedTasks({ pageNum: 1, pageSize: 1000 })
    const completedTasks = Array.isArray(completedRes.data)
      ? completedRes.data
      : (completedRes.data as any)?.records || []
    stats[1].value = completedTasks.length.toString()

    // 加载本月发起的合同数量
    const now = new Date()
    const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
    const contractsRes = await getMyContracts({ pageNum: 1, pageSize: 1000 })
    const allContracts = Array.isArray(contractsRes.data)
      ? contractsRes.data
      : (contractsRes.data as any)?.records || []
    
    const thisMonthContracts = allContracts.filter((contract: any) => {
      const createdAt = new Date(contract.createdAt || contract.createTime || '')
      return createdAt >= firstDayOfMonth
    })
    stats[2].value = thisMonthContracts.length.toString()
  } catch (error) {
    console.error('加载统计数据失败', error)
    // 保持默认值 0
  }
}

// 加载待办任务列表
const loadTasks = async () => {
  loadingTasks.value = true
  try {
    const res = await getMyPendingTasks({ pageNum: 1, pageSize: 1000 })
    const allTasks = Array.isArray(res.data) 
      ? res.data
      : (res.data as any)?.records || []
    todoTasks.value = allTasks.slice(0, 5)
  } catch (error) {
    console.error('加载待办任务失败', error)
    ElMessage.error('加载待办任务失败')
    todoTasks.value = []
  } finally {
    loadingTasks.value = false
  }
}

// 格式化任务时间
const formatTaskTime = (task: ApprovalTask) => {
  // 如果有创建时间，使用创建时间；否则显示"待处理"
  if (task.approvalTime) {
    return new Date(task.approvalTime).toLocaleString('zh-CN')
  }
  return '待处理'
}

// 跳转到任务处理页面
const goToTask = (task: ApprovalTask) => {
  router.push(`/workflow/approve/${task.id}`)
}

// 加载管理员统计数据
const loadAdminStats = async () => {
  try {
    // 加载用户总数
    const userRes = await getUserList({ pageNum: 1, pageSize: 1 })
    adminStats[0].value = (userRes.data as any)?.total?.toString() || '0'

    // 加载部门总数
    const deptRes = await getDepartmentTree()
    const countDepartments = (depts: any[]): number => {
      let count = depts.length
      depts.forEach(dept => {
        if (dept.children && dept.children.length > 0) {
          count += countDepartments(dept.children)
        }
      })
      return count
    }
    adminStats[1].value = countDepartments(Array.isArray(deptRes.data) ? deptRes.data : []).toString()

    // 加载知识库文件数
    try {
      const knowledgeRes = await getKnowledgeFiles()
      // API返回格式: { files: [...], total: 10 }
      const total = knowledgeRes.data?.total
      const files = knowledgeRes.data?.files || []
      adminStats[2].value = (total !== undefined ? total : files.length).toString()
    } catch (error) {
      console.error('加载知识库文件失败', error)
      adminStats[2].value = '0'
    }

    // 加载合同总数
    const contractRes = await getContractList({ pageNum: 1, pageSize: 1 })
    adminStats[3].value = (contractRes.data as any)?.total?.toString() || '0'
  } catch (error) {
    console.error('加载管理员统计数据失败', error)
  }
}

// 加载系统状态（模拟数据，可以后续接入真实API）
const loadSystemStatus = async () => {
  loadingSystemStatus.value = true
  // 模拟加载延迟
  await new Promise(resolve => setTimeout(resolve, 500))
  // 这里可以接入真实的系统状态API
  loadingSystemStatus.value = false
}

onMounted(() => {
  // 管理员加载管理员数据
  if (isAdmin.value) {
    loadAdminStats()
    loadSystemStatus()
  }
  // 业务人员加载业务数据
  else if (isBusinessUser.value) {
    loadStats()
    loadTasks()
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

/* 欢迎区域 */
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.welcome-card :deep(.el-card__body) {
  padding: 24px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text {
  color: #fff;
}

.greeting {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.user-info-line {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  
  .el-tag {
    display: flex;
    align-items: center;
    gap: 4px;
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: #fff;
  }
}

.welcome-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.mt-20 {
  margin-top: 20px;
}

/* 基础卡片样式 */
.stat-card {
  border-radius: 8px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.stat-content {
  display: flex;
  align-items: center;
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
}

.stat-info .stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

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

/* 待办列表 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-list {
  min-height: 200px;
}

.empty-todos {
  padding: 40px 0;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.todo-item:hover {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding-left: 8px;
  padding-right: 8px;
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
  white-space: nowrap;
}

.todo-content {
  flex: 1;
  overflow: hidden;
  margin-right: 10px;
  min-width: 0;
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
.notice-list {
  padding: 10px 0;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
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
  flex-shrink: 0;
}

.text {
  flex: 1;
}

/* 管理员工作台样式 */
.system-status {
  padding: 10px 0;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.status-value {
  font-weight: 500;
}

.activity-list {
  padding: 10px 0;
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 12px;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.5;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}
</style>
