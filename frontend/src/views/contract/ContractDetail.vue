<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Contract, AiReviewResult, ApprovalTask } from '@/types'
import { getContractDetail, submitContract, aiReviewContract, getReviewHistory, type ContractReviewDTO } from '@/api/contract'
import { getInstanceHistory } from '@/api/workflow'
import { getChangeHistory, type ContractChangeVO } from '@/api/contractChange'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const route = useRoute()
const md = new MarkdownIt({
  breaks: true,
  html: false,
  linkify: true
})

const renderMarkdown = (text: string) => {
  return md.render(text || '')
}

const contractId = computed(() => Number(route.params.id))
const loading = ref(false)
const submitting = ref(false)
const reviewing = ref(false)

const contract = ref<Contract | null>(null)
const reviewResult = ref<AiReviewResult | null>(null)
const approvalHistory = ref<ApprovalTask[]>([])
const changeHistory = ref<ContractChangeVO[]>([])

const activeTab = ref('content')

const contractTypeMap: Record<string, string> = {
  'STATION_LEASE': '基站租赁合同',
  'NETWORK_CONSTRUCTION': '网络建设合同',
  'EQUIPMENT_PURCHASE': '设备采购合同',
  'MAINTENANCE_SERVICE': '运维服务合同'
}

// 数字状态映射（后端返回数字而非字符串）
const statusMap: Record<number | string, { text: string; type: string }> = {
  0: { text: '草稿', type: 'info' },
  1: { text: '审批中', type: 'warning' },
  2: { text: '已生效', type: 'success' },
  3: { text: '已驳回', type: 'danger' },
  // 兼容字符串格式
  'DRAFT': { text: '草稿', type: 'info' },
  'PENDING': { text: '审批中', type: 'warning' },
  'APPROVED': { text: '已生效', type: 'success' },
  'REJECTED': { text: '已驳回', type: 'danger' }
}

onMounted(() => {
  // 检查合同ID是否有效
  if (!contractId.value || isNaN(contractId.value)) {
    ElMessage.error('无效的合同ID，请返回重试')
    router.push('/contract/my')
    return
  }
  loadContract()
  loadReviewHistory()
})

const loadContract = async () => {
  if (!contractId.value || isNaN(contractId.value)) {
    ElMessage.error('无效的合同ID')
    return
  }
  loading.value = true
  try {
    const res = await getContractDetail(contractId.value)
    contract.value = res.data
    
    // 如果是审批中或已完成，加载审批历史
    if (contract.value?.status !== 0) {
      loadApprovalHistory()
    }
    
    // 加载变更历史
    loadChangeHistory()
  } catch (error) {
    console.error(error)
    ElMessage.error('加载合同详情失败')
  } finally {
    loading.value = false
  }
}

const loadApprovalHistory = async () => {
  try {
    const res = await getInstanceHistory(contractId.value)
    approvalHistory.value = res.data || []
  } catch (error) {
    console.error(error)
    // 审批历史加载失败不影响主详情显示
  }
}

const loadChangeHistory = async () => {
  try {
    const res = await getChangeHistory(contractId.value)
    changeHistory.value = res.data || []
  } catch {
    // 获取变更历史失败时不显示错误，保持空列表
    changeHistory.value = []
  }
}

// 加载AI审查历史
const loadReviewHistory = async () => {
  try {
    const res = await getReviewHistory(contractId.value)
    const reviews = res.data || []
    
    // 取最新的已完成审查结果
    const completedReview = reviews.find(r => r.status === 'COMPLETED')
    if (completedReview) {
      reviewResult.value = {
        riskLevel: completedReview.riskLevel,
        score: completedReview.score,
        highRiskItems: completedReview.reviewContent?.highRisks || [],
        mediumRiskItems: completedReview.reviewContent?.mediumRisks || [],
        lowRiskItems: completedReview.reviewContent?.lowRisks || [],
        goodClauses: [] // 后端未返回此字段
      }
    }
  } catch {
    // 审查历史加载失败不影响主页面
  }
}

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm('确定要提交该合同进行审批吗？', '提交审批', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    submitting.value = true
    await submitContract(contractId.value)
    ElMessage.success('合同已提交审批')
    loadContract()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleAiReview = async () => {
  reviewing.value = true
  try {
    const res = await aiReviewContract(contractId.value)
    if (res.data) {
      reviewResult.value = {
        riskLevel: res.data.riskLevel || 'LOW',
        score: res.data.score || 80,
        highRiskItems: res.data.highRiskItems || [],
        mediumRiskItems: res.data.mediumRiskItems || [],
        lowRiskItems: res.data.lowRiskItems || [],
        goodClauses: res.data.goodClauses || [],
        rawAnalysis: res.data.rawAnalysis || '' // DeepSeek 原始分析
      }
      activeTab.value = 'review'
      ElMessage.success('AI审查完成')
    } else {
      ElMessage.error('AI审查返回数据为空')
    }
  } catch (error: any) {
    console.error('AI审查失败', error)
    ElMessage.error(error?.response?.data?.message || error?.message || 'AI服务调用失败，请稍后重试')
  } finally {
    reviewing.value = false
  }
}

const goEdit = () => {
  if (!contract.value) {
    ElMessage.warning('合同信息未加载')
    return
  }
  
  // 跳转到起草页面（带AI助手）
  router.push({
    path: '/contract/draft',
    query: {
      id: contractId.value,
      mainType: contract.value.type || 'TYPE_A',
      subType: (contract.value.attributes as any)?.subTypeCode || 'A1'
    }
  })
}

const goBack = () => {
  router.back()
}

const goToChange = () => {
  if (!contract.value) return
  
  router.push({
    path: '/contract/draft',
    query: {
      changeContractId: contractId.value.toString(), // 标记为变更模式，传入原合同ID
      mainType: contract.value.type || 'TYPE_A', // Note: detailed contract uses contractType field
      subType: (contract.value.attributes as any)?.subTypeCode || 'A1',
      changeMode: 'true' // 标记为变更模式
    }
  })
}

const getScoreDesc = (level: string) => {
  const map: Record<string, string> = {
    'LOW': '该合同整体风险较低，建议关注优化建议。',
    'MEDIUM': '该合同存在一定风险，建议重点关注中高风险项。',
    'HIGH': '该合同风险较高，建议暂缓签署并进行全面修改。'
  }
  return map[level] || map['LOW']
}

const getRiskLevelInfo = (level: string) => {
  const map: Record<string, { text: string; type: string; color: string }> = {
    'LOW': { text: '低风险', type: 'success', color: '#52c41a' },
    'MEDIUM': { text: '中风险', type: 'warning', color: '#faad14' },
    'HIGH': { text: '高风险', type: 'danger', color: '#ff4d4f' }
  }
  return map[level] || map['LOW']
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2 class="page-title">{{ contract?.name }}</h2>
        <el-tag v-if="contract" :type="statusMap[contract.status]?.type as any" size="large">
          {{ statusMap[contract.status]?.text }}
        </el-tag>
        <el-tag v-if="contract?.isAiGenerated" type="success" size="large">
          AI生成
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button 
          v-if="[0, 1].includes(contract?.status ?? -1)" 
          type="warning"
          :loading="reviewing"
          @click="handleAiReview"
        >
          <el-icon><MagicStick /></el-icon>
          AI条款审查
        </el-button>
        <el-button 
          v-if="contract?.status === 0" 
          @click="goEdit"
        >
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button 
          v-if="contract?.status === 0" 
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          <el-icon><Promotion /></el-icon>
          提交审批
        </el-button>
        <el-button 
          v-if="contract?.status === 2" 
          type="warning"
          @click="goToChange"
        >
          <el-icon><EditPen /></el-icon>
          发起变更
        </el-button>
      </div>
    </div>
    
    <el-tabs v-model="activeTab">
      <!-- 合同内容 -->
      <el-tab-pane label="合同内容" name="content">
        <div class="contract-detail" v-if="contract">
          <!-- 基本信息 -->
          <el-card class="info-card">
            <template #header>
              <span class="card-title">基本信息</span>
            </template>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="合同编号">
                {{ contract.contractNo }}
              </el-descriptions-item>
              <el-descriptions-item label="合同类型">
                {{ contractTypeMap[contract.type] || contract.type }}
              </el-descriptions-item>
              <el-descriptions-item label="合同金额">
                <span class="amount">¥{{ contract.amount.toLocaleString() }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="甲方">
                {{ contract.partyA }}
              </el-descriptions-item>
              <el-descriptions-item label="乙方">
                {{ contract.partyB }}
              </el-descriptions-item>
              <el-descriptions-item label="创建人">
                {{ contract.creatorName }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ contract.createdAt }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间">
                {{ contract.updatedAt }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          
          <!-- 扩展字段 -->
          <el-card v-if="contract.type === 'STATION_LEASE'" class="info-card">
            <template #header>
              <span class="card-title">基站租赁信息</span>
            </template>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="站址位置" :span="2">
                {{ contract.attributes?.siteLocation }}
              </el-descriptions-item>
              <el-descriptions-item label="站址类型">
                {{ contract.attributes?.siteType }}
              </el-descriptions-item>
              <el-descriptions-item label="站址面积">
                {{ contract.attributes?.siteArea }} 平方米
              </el-descriptions-item>
              <el-descriptions-item label="年租金">
                ¥{{ contract.attributes?.annualRent?.toLocaleString() }}
              </el-descriptions-item>
              <el-descriptions-item label="租赁期限">
                {{ contract.attributes?.leaseStartDate }} 至 {{ contract.attributes?.leaseEndDate }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          
          <!-- 合同正文 -->
          <el-card class="content-card">
            <template #header>
              <span class="card-title">合同正文</span>
            </template>
            <div class="contract-content markdown-body" v-html="renderMarkdown(contract.content)"></div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- AI审查结果 -->
      <el-tab-pane label="AI审查结果" name="review" :disabled="!reviewResult">
        <div v-if="reviewResult" class="review-result">
          <!-- 总体评估 -->
          <el-card class="score-card">
            <div class="score-content">
              <div class="score-main">
                <div 
                  class="score-circle"
                  :style="{ borderColor: getRiskLevelInfo(reviewResult.riskLevel).color }"
                >
                  <span class="score-value">{{ reviewResult.score }}</span>
                  <span class="score-label">综合评分</span>
                </div>
              </div>
              <div class="score-info">
                <el-tag 
                  :type="getRiskLevelInfo(reviewResult.riskLevel).type as any"
                  size="large"
                >
                  {{ getRiskLevelInfo(reviewResult.riskLevel).text }}
                </el-tag>
                <p class="score-desc">
                  {{ getScoreDesc(reviewResult.riskLevel) }}
                </p>
              </div>
            </div>
          </el-card>
          
          <!-- 风险项列表 -->
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="risk-card high">
                <template #header>
                  <div class="risk-header">
                    <span>高风险项</span>
                    <el-badge :value="reviewResult.highRiskItems.length" type="danger" />
                  </div>
                </template>
                <div v-if="reviewResult.highRiskItems.length === 0" class="empty-risk">
                  <el-icon :size="40"><CircleCheck /></el-icon>
                  <span>无高风险项</span>
                </div>
                <div v-else class="risk-list">
                  <div v-for="(item, index) in reviewResult.highRiskItems" :key="index" class="risk-item">
                    <div class="risk-issue">{{ item.issue }}</div>
                    <div class="risk-suggestion">{{ item.suggestion }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <el-card class="risk-card medium">
                <template #header>
                  <div class="risk-header">
                    <span>中风险项</span>
                    <el-badge :value="reviewResult.mediumRiskItems.length" type="warning" />
                  </div>
                </template>
                <div v-if="reviewResult.mediumRiskItems.length === 0" class="empty-risk">
                  <el-icon :size="40"><CircleCheck /></el-icon>
                  <span>无中风险项</span>
                </div>
                <div v-else class="risk-list">
                  <div v-for="(item, index) in reviewResult.mediumRiskItems" :key="index" class="risk-item">
                    <div class="risk-issue">{{ item.issue }}</div>
                    <div class="risk-suggestion">{{ item.suggestion }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <el-card class="risk-card good">
                <template #header>
                  <div class="risk-header">
                    <span>优质条款</span>
                    <el-badge :value="reviewResult.goodClauses.length" type="success" />
                  </div>
                </template>
                <div class="risk-list">
                  <div v-for="(item, index) in reviewResult.goodClauses" :key="index" class="risk-item">
                    <div class="risk-issue">{{ item }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 低风险项（新增） -->
          <el-row style="margin-top: 20px" v-if="reviewResult.lowRiskItems && reviewResult.lowRiskItems.length > 0">
            <el-col :span="24">
              <el-card class="risk-card low" style="height: auto">
                <template #header>
                  <div class="risk-header">
                    <span>低风险项 / 建议优化</span>
                    <el-badge :value="reviewResult.lowRiskItems.length" type="info" />
                  </div>
                </template>
                <div class="risk-list" style="max-height: none">
                  <div v-for="(item, index) in reviewResult.lowRiskItems" :key="index" class="risk-item">
                    <div class="risk-issue">{{ item.issue }}</div>
                    <div class="risk-suggestion">{{ item.suggestion }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 完整审查报告（大框展示） -->
          <el-card class="full-report-card" style="margin-top: 20px" v-if="reviewResult.rawAnalysis">
            <template #header>
              <div class="risk-header">
                <span class="card-title">🔍 AI深度审查报告全文</span>
                <el-tag type="info">DeepSeek V3 分析</el-tag>
              </div>
            </template>
            <div class="full-report-content markdown-body" v-html="renderMarkdown(reviewResult.rawAnalysis)">
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 审批流程 -->
      <el-tab-pane label="审批流程" name="workflow" :disabled="contract?.status === 0">
        <div class="workflow-timeline">
          <el-timeline>
            <el-timeline-item
              v-for="item in approvalHistory"
              :key="item.id"
              :timestamp="item.createTime"
              :type="item.status === 1 ? 'success' : item.status === 2 ? 'danger' : 'primary'"
              placement="top"
            >
              <el-card>
                <div class="timeline-content">
                  <div class="timeline-header">
                    <span class="node-name">{{ item.nodeName || '审批节点' }}</span>
                    <el-tag :type="item.status === 1 ? 'success' : item.status === 2 ? 'danger' : 'warning'" size="small">
                      {{ item.status === 1 ? '已通过' : item.status === 2 ? '已驳回' : '待处理' }}
                    </el-tag>
                  </div>
                  <div class="timeline-info">
                    <span>处理人：{{ item.assigneeName || '待分配' }}</span>
                  </div>
                  <div v-if="item.comment" class="timeline-opinion">
                    审批意见：{{ item.comment }}
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-tab-pane>
      
      <!-- 变更历史 -->
      <el-tab-pane label="变更历史" name="changes" :disabled="contract?.status === 0">
        <div class="change-history">
          <el-empty v-if="changeHistory.length === 0" description="暂无变更记录" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in changeHistory"
              :key="item.id"
              :timestamp="item.createdAt"
              :type="item.status === 2 ? 'success' : item.status === 3 ? 'danger' : 'warning'"
              placement="top"
            >
              <el-card>
                <div class="change-item">
                  <div class="change-header">
                    <span class="change-title">{{ item.title }}</span>
                    <el-tag v-if="item.isMajorChange" type="danger" size="small">重大变更</el-tag>
                    <el-tag :type="item.status === 2 ? 'success' : item.status === 3 ? 'danger' : 'warning'" size="small">
                      {{ item.statusName }}
                    </el-tag>
                  </div>
                  <div class="change-info">
                    <span>变更单号：{{ item.changeNo }}</span>
                    <span>版本：{{ item.changeVersion }}</span>
                    <span>类型：{{ item.changeTypeName }}</span>
                  </div>
                  <div v-if="item.amountDiff" class="change-amount">
                    金额变化：
                    <span :class="item.amountDiff > 0 ? 'increase' : 'decrease'">
                      {{ item.amountDiff > 0 ? '+' : '' }}{{ item.amountDiff.toLocaleString() }}元
                      ({{ item.changePercent?.toFixed(1) || 0 }}%)
                    </span>
                  </div>
                  <div class="change-desc">{{ item.description }}</div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts">
import { ArrowLeft, MagicStick, Edit, EditPen, Promotion, CircleCheck, Check } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft, MagicStick, Edit, EditPen, Promotion, CircleCheck, Check }
}
</script>

<style scoped>
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.info-card,
.content-card {
  margin-bottom: 20px;
}

.card-title {
  font-weight: 600;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.contract-content {
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.score-card {
  margin-bottom: 20px;
}

.score-content {
  display: flex;
  align-items: center;
  gap: 40px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 6px solid;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-value {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
}

.score-label {
  font-size: 12px;
  color: #909399;
}

.score-info {
  flex: 1;
}

.score-desc {
  margin-top: 12px;
  color: #606266;
}

.risk-card {
  height: 300px;
}

.risk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-risk {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 180px;
  color: #52c41a;
  gap: 12px;
}

.risk-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.risk-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.risk-issue {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.risk-suggestion {
  font-size: 13px;
  color: #909399;
}

.full-report-content {
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
  min-height: 300px;
  max-height: 600px;
  overflow-y: auto;
}

.good-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.good-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.workflow-timeline {
  padding: 20px;
}

.timeline-content {
  padding: 0;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.node-name {
  font-weight: 600;
}

.timeline-info {
  font-size: 13px;
  color: #909399;
}

.timeline-opinion {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}

/* 变更历史样式 */
.change-history {
  padding: 20px;
}

.change-item {
  .change-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
    
    .change-title {
      font-weight: 600;
      font-size: 15px;
    }
  }
  
  .change-info {
    display: flex;
    gap: 20px;
    font-size: 13px;
    color: #909399;
    margin-bottom: 8px;
  }
  
  .change-amount {
    font-size: 14px;
    margin-bottom: 8px;
    
    .increase {
      color: #f56c6c;
      font-weight: 600;
    }
    
    .decrease {
      color: #67c23a;
      font-weight: 600;
    }
  }
  
  .change-desc {
    padding: 10px;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
  }
}
/* Markdown Styles */
.contract-content, .full-report-content {
  white-space: normal !important;
}

.markdown-body h1, .markdown-body h2, .markdown-body h3 {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body h1 { font-size: 2em; }
.markdown-body h2 { font-size: 1.5em; border-bottom: 1px solid #eaecef; padding-bottom: .3em; }
.markdown-body h3 { font-size: 1.25em; }

.markdown-body p { margin-top: 0; margin-bottom: 16px; }
.markdown-body ul, .markdown-body ol { padding-left: 2em; margin-bottom: 16px; }
.markdown-body code {
  padding: .2em .4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(27,31,35,.05);
  border-radius: 3px;
}
.markdown-body pre {
  padding: 16px;
  overflow: auto;
  line-height: 1.45;
  background-color: #f6f8fa;
  border-radius: 3px;
}
.markdown-body pre code {
  background-color: transparent;
  padding: 0;
}
.markdown-body blockquote {
  padding: 0 1em;
  color: #6a737d;
  border-left: .25em solid #dfe2e5;
  margin: 0 0 16px 0;
}
</style>

