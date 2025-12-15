<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Contract, AiReviewResult, ApprovalTask } from '@/types'
import { getContractDetail, submitContract, aiReviewContract } from '@/api/contract'
import { getInstanceHistory } from '@/api/workflow'

const router = useRouter()
const route = useRoute()

const contractId = computed(() => Number(route.params.id))
const loading = ref(false)
const submitting = ref(false)
const reviewing = ref(false)

const contract = ref<Contract | null>(null)
const reviewResult = ref<AiReviewResult | null>(null)
const approvalHistory = ref<ApprovalTask[]>([])

const activeTab = ref('content')

const contractTypeMap: Record<string, string> = {
  'STATION_LEASE': '基站租赁合同',
  'NETWORK_CONSTRUCTION': '网络建设合同',
  'EQUIPMENT_PURCHASE': '设备采购合同',
  'MAINTENANCE_SERVICE': '运维服务合同'
}

const statusMap: Record<string, { text: string; type: string }> = {
  'DRAFT': { text: '草稿', type: 'info' },
  'PENDING': { text: '审批中', type: 'warning' },
  'APPROVED': { text: '已生效', type: 'success' },
  'REJECTED': { text: '已驳回', type: 'danger' }
}

onMounted(() => {
  loadContract()
})

const loadContract = async () => {
  loading.value = true
  try {
    const res = await getContractDetail(contractId.value)
    contract.value = res.data
    
    // 如果是审批中或已完成，加载审批历史
    if (contract.value?.status !== 'DRAFT') {
      loadApprovalHistory()
    }
  } catch (error) {
    // 模拟数据
    contract.value = {
      id: contractId.value,
      contractNo: 'HT-ZL-20251201-001',
      contractName: '某大厦5G基站租赁合同',
      contractType: 'STATION_LEASE',
      partyA: '中国电信股份有限公司',
      partyB: 'XX物业管理公司',
      amount: 50000,
      status: 'DRAFT',
      isAiGenerated: true,
      creatorId: 1,
      creatorName: '张三',
      createTime: '2025-12-01 10:30:00',
      updateTime: '2025-12-01 10:30:00',
      siteLocation: '北京市朝阳区某大厦楼顶',
      siteType: '楼顶',
      siteArea: 100,
      annualRent: 50000,
      leaseStartDate: '2025-01-01',
      leaseEndDate: '2034-12-31',
      content: `基站租赁合同

甲方：中国电信股份有限公司
乙方：XX物业管理公司

第一条 租赁标的
乙方同意将位于北京市朝阳区某大厦楼顶约100平方米的场地出租给甲方，用于建设和运营移动通信基站。

第二条 租赁期限
租赁期限为10年，自2025年1月1日起至2034年12月31日止。

第三条 租金及支付方式
1. 年租金为人民币伍万元整（¥50,000）
2. 租金按年支付，甲方应于每年1月15日前支付当年租金

第四条 甲方权利与义务
1. 甲方有权在租赁场地内建设、维护通信基站及配套设施
2. 甲方应确保基站运行符合国家电磁辐射安全标准
3. 甲方应承担基站运行所需的水电费用

第五条 乙方权利与义务
1. 乙方应保证甲方人员可24小时进入场地进行设备维护
2. 乙方不得擅自切断基站供电
3. 乙方应协助甲方完成应急通信保障任务

第六条 违约责任
任何一方违反本合同约定，应向对方支付违约金人民币壹万元整。

第七条 争议解决
本合同发生争议，双方应协商解决；协商不成的，提交甲方所在地人民法院诉讼解决。`
    }
  } finally {
    loading.value = false
  }
}

const loadApprovalHistory = async () => {
  try {
    const res = await getInstanceHistory(contractId.value)
    approvalHistory.value = res.data || []
  } catch {
    // 模拟审批历史
    approvalHistory.value = [
      {
        id: 1,
        instanceId: 1,
        nodeId: 1,
        nodeName: '发起申请',
        approverId: 1,
        approverName: '张三',
        status: 'APPROVED',
        opinion: '提交审批',
        approvalTime: '2025-12-01 10:30:00',
        contractId: contractId.value,
        contractName: '',
        contractNo: '',
        contractType: 'STATION_LEASE',
        initiatorName: '张三',
        createTime: '2025-12-01 10:30:00'
      }
    ]
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
    reviewResult.value = res.data
    activeTab.value = 'review'
    ElMessage.success('AI审查完成')
  } catch {
    // 模拟审查结果
    reviewResult.value = {
      riskLevel: 'LOW',
      score: 85,
      highRiskItems: [],
      mediumRiskItems: [
        {
          issue: '未明确约定电磁辐射检测周期',
          suggestion: '建议增加条款：甲方应每年委托有资质的检测机构进行电磁辐射检测，并向乙方提供检测报告',
          clause: '第四条'
        }
      ],
      lowRiskItems: [
        {
          issue: '违约金条款较为简单',
          suggestion: '可考虑细化不同违约情形的违约金计算方式',
          clause: '第六条'
        }
      ],
      goodClauses: [
        '明确了24小时维护权限',
        '包含应急通信保障条款',
        '租赁期限合理（10年）'
      ]
    }
    activeTab.value = 'review'
    ElMessage.success('AI审查完成')
  } finally {
    reviewing.value = false
  }
}

const goEdit = () => {
  router.push(`/contract/edit/${contractId.value}`)
}

const goBack = () => {
  router.back()
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
        <h2 class="page-title">{{ contract?.contractName }}</h2>
        <el-tag v-if="contract" :type="statusMap[contract.status]?.type as any" size="large">
          {{ statusMap[contract.status]?.text }}
        </el-tag>
        <el-tag v-if="contract?.isAiGenerated" type="success" size="large">
          AI生成
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button 
          v-if="contract?.status === 'DRAFT'" 
          type="warning"
          :loading="reviewing"
          @click="handleAiReview"
        >
          <el-icon><MagicStick /></el-icon>
          AI条款审查
        </el-button>
        <el-button 
          v-if="contract?.status === 'DRAFT'" 
          @click="goEdit"
        >
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button 
          v-if="contract?.status === 'DRAFT'" 
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          <el-icon><Promotion /></el-icon>
          提交审批
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
                {{ contractTypeMap[contract.contractType] }}
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
                {{ contract.createTime }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间">
                {{ contract.updateTime }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          
          <!-- 扩展字段 -->
          <el-card v-if="contract.contractType === 'STATION_LEASE'" class="info-card">
            <template #header>
              <span class="card-title">基站租赁信息</span>
            </template>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="站址位置" :span="2">
                {{ contract.siteLocation }}
              </el-descriptions-item>
              <el-descriptions-item label="站址类型">
                {{ contract.siteType }}
              </el-descriptions-item>
              <el-descriptions-item label="站址面积">
                {{ contract.siteArea }} 平方米
              </el-descriptions-item>
              <el-descriptions-item label="年租金">
                ¥{{ contract.annualRent?.toLocaleString() }}
              </el-descriptions-item>
              <el-descriptions-item label="租赁期限">
                {{ contract.leaseStartDate }} 至 {{ contract.leaseEndDate }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          
          <!-- 合同正文 -->
          <el-card class="content-card">
            <template #header>
              <span class="card-title">合同正文</span>
            </template>
            <pre class="contract-content">{{ contract.content }}</pre>
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
                  该合同整体风险较低，建议关注中风险项的修改建议。
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
                <div class="good-list">
                  <div v-for="(item, index) in reviewResult.goodClauses" :key="index" class="good-item">
                    <el-icon color="#52c41a"><Check /></el-icon>
                    <span>{{ item }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <!-- 审批流程 -->
      <el-tab-pane label="审批流程" name="workflow" :disabled="contract?.status === 'DRAFT'">
        <div class="workflow-timeline">
          <el-timeline>
            <el-timeline-item
              v-for="item in approvalHistory"
              :key="item.id"
              :timestamp="item.approvalTime || item.createTime"
              :type="item.status === 'APPROVED' ? 'success' : item.status === 'REJECTED' ? 'danger' : 'primary'"
              placement="top"
            >
              <el-card>
                <div class="timeline-content">
                  <div class="timeline-header">
                    <span class="node-name">{{ item.nodeName }}</span>
                    <el-tag :type="item.status === 'APPROVED' ? 'success' : 'warning'" size="small">
                      {{ item.status === 'APPROVED' ? '已通过' : item.status === 'REJECTED' ? '已驳回' : '待处理' }}
                    </el-tag>
                  </div>
                  <div class="timeline-info">
                    <span>处理人：{{ item.approverName }}</span>
                  </div>
                  <div v-if="item.opinion" class="timeline-opinion">
                    审批意见：{{ item.opinion }}
                  </div>
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
import { ArrowLeft, MagicStick, Edit, Promotion, CircleCheck, Check } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft, MagicStick, Edit, Promotion, CircleCheck, Check }
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
</style>

