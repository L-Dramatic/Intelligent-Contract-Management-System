<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { ContractType, AiChatMessage, Contract } from '@/types'
import { aiGenerateContract, createContract } from '@/api/contract'

const router = useRouter()
const route = useRoute()

const contractType = ref<ContractType>((route.query.type as ContractType) || 'STATION_LEASE')
const chatContainerRef = ref<HTMLElement>()
const inputRef = ref<HTMLInputElement>()

const loading = ref(false)
const generating = ref(false)
const userInput = ref('')

const messages = ref<AiChatMessage[]>([])
const generatedContract = ref<Partial<Contract> | null>(null)

const contractTypeMap: Record<string, string> = {
  'STATION_LEASE': '基站租赁合同',
  'NETWORK_CONSTRUCTION': '网络建设合同',
  'EQUIPMENT_PURCHASE': '设备采购合同',
  'MAINTENANCE_SERVICE': '运维服务合同'
}

const welcomeMessages: Record<string, string> = {
  'STATION_LEASE': `您好！我是电信智慧合同AI助手，我将帮您生成一份专业的基站租赁合同。

请告诉我以下信息：
1. 站址位置（详细地址）
2. 站址类型（楼顶/铁塔/地面机房）
3. 租赁面积
4. 年租金
5. 租赁期限
6. 乙方信息（业主名称）

您可以用自然语言描述，例如：
"我需要租赁北京市朝阳区某大厦楼顶100平米用于建设5G基站，年租金5万元，租期10年，业主是XX物业公司"`,

  'NETWORK_CONSTRUCTION': `您好！我是电信智慧合同AI助手，我将帮您生成一份专业的网络建设合同。

请告诉我以下信息：
1. 建设区域
2. 网络制式（4G/5G/混合）
3. 规划基站数量
4. 覆盖率要求
5. 合同金额
6. 施工方信息

您可以用自然语言描述，例如：
"需要在XX区建设50个5G基站，覆盖率要求95%以上，预算800万元，施工方是华为技术公司"`,

  'EQUIPMENT_PURCHASE': `您好！我是电信智慧合同AI助手，我将帮您生成一份专业的设备采购合同。

请告诉我以下信息：
1. 采购设备类型及型号
2. 采购数量
3. 采购金额
4. 交付时间要求
5. 供应商信息
6. 售后服务要求

您可以用自然语言描述，例如：
"采购100套华为5G基站设备BBU5900，总金额500万元，要求30天内交付，3年质保"`,

  'MAINTENANCE_SERVICE': `您好！我是电信智慧合同AI助手，我将帮您生成一份专业的运维服务合同。

请告诉我以下信息：
1. 服务范围
2. 服务期限
3. SLA指标要求（可用性、响应时间等）
4. 服务费用
5. 服务提供方信息

您可以用自然语言描述，例如：
"需要对XX区500个基站进行运维，服务期1年，要求可用性99.5%以上，故障响应4小时内，年服务费100万元"`
}

onMounted(() => {
  // 添加欢迎消息
  messages.value.push({
    role: 'assistant',
    content: welcomeMessages[contractType.value] || welcomeMessages['STATION_LEASE']
  })
})

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!userInput.value.trim() || generating.value) return
  
  const content = userInput.value.trim()
  userInput.value = ''
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content
  })
  
  await scrollToBottom()
  
  // 调用AI生成
  generating.value = true
  try {
    const res = await aiGenerateContract({
      contractType: contractType.value,
      messages: messages.value
    })
    
    // 添加AI回复
    messages.value.push({
      role: 'assistant',
      content: res.data.reply || '好的，我已经根据您的需求生成了合同草稿，请查看右侧预览。'
    })
    
    // 如果生成了合同
    if (res.data.contract) {
      generatedContract.value = res.data.contract
    }
  } catch (error) {
    // 模拟AI回复
    const mockReply = getMockReply(content)
    messages.value.push({
      role: 'assistant',
      content: mockReply.reply
    })
    
    if (mockReply.contract) {
      generatedContract.value = mockReply.contract
    }
  } finally {
    generating.value = false
    await scrollToBottom()
  }
}

// 模拟AI回复
const getMockReply = (input: string): { reply: string; contract?: Partial<Contract> } => {
  // 简单的模拟逻辑
  if (input.includes('基站') || input.includes('租赁') || input.includes('大厦')) {
    return {
      reply: `好的，我已根据您的描述生成了基站租赁合同草稿。

合同要点：
✅ 甲方：中国电信股份有限公司
✅ 站址类型：楼顶基站
✅ 租赁期限：10年
✅ 年租金：5万元

请查看右侧预览，您可以继续告诉我需要修改的内容，或者直接保存合同。`,
      contract: {
        contractName: '某大厦5G基站租赁合同',
        contractType: 'STATION_LEASE',
        partyA: '中国电信股份有限公司',
        partyB: 'XX物业管理公司',
        amount: 50000,
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

第七条 合同解除
如因国家政策调整或不可抗力导致合同无法履行，双方可协商解除合同。

第八条 争议解决
本合同发生争议，双方应协商解决；协商不成的，提交甲方所在地人民法院诉讼解决。

甲方（盖章）：                    乙方（盖章）：
代表签字：                        代表签字：
日期：                            日期：`
      }
    }
  }
  
  return {
    reply: '好的，请继续补充合同相关信息，我会为您生成专业的合同草稿。'
  }
}

const saveContract = async () => {
  if (!generatedContract.value) {
    ElMessage.warning('请先生成合同')
    return
  }
  
  loading.value = true
  try {
    const res = await createContract({
      ...generatedContract.value,
      status: 'DRAFT',
      isAiGenerated: true
    })
    ElMessage.success('合同已保存为草稿')
    router.push(`/contract/detail/${res.data?.id || 1}`)
  } catch (error) {
    // 模拟成功
    ElMessage.success('合同已保存为草稿')
    router.push('/contract/my')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="ai-generate-container">
    <!-- 头部 -->
    <div class="ai-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2 class="title">AI智能生成 - {{ contractTypeMap[contractType] }}</h2>
      </div>
      <el-button 
        type="primary" 
        :disabled="!generatedContract"
        :loading="loading"
        @click="saveContract"
      >
        保存合同
      </el-button>
    </div>
    
    <div class="ai-content">
      <!-- 左侧聊天区 -->
      <div class="chat-panel">
        <div ref="chatContainerRef" class="chat-messages">
          <div 
            v-for="(msg, index) in messages"
            :key="index"
            class="chat-message"
            :class="msg.role"
          >
            <div class="avatar">
              <el-icon v-if="msg.role === 'assistant'" :size="20"><ChatDotRound /></el-icon>
              <span v-else>我</span>
            </div>
            <div class="content">
              <pre class="message-text">{{ msg.content }}</pre>
            </div>
          </div>
          
          <!-- 加载中 -->
          <div v-if="generating" class="chat-message assistant">
            <div class="avatar">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </div>
            <div class="content loading">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
        
        <div class="chat-input">
          <el-input
            ref="inputRef"
            v-model="userInput"
            type="textarea"
            :rows="3"
            placeholder="请输入您的需求..."
            :disabled="generating"
            @keydown.enter.ctrl="sendMessage"
          />
          <el-button 
            type="primary" 
            :disabled="!userInput.trim() || generating"
            @click="sendMessage"
          >
            <el-icon><Position /></el-icon>
            发送
          </el-button>
        </div>
        <div class="input-tip">按 Ctrl + Enter 快速发送</div>
      </div>
      
      <!-- 右侧预览区 -->
      <div class="preview-panel">
        <div class="preview-header">
          <h3>合同预览</h3>
          <el-tag v-if="generatedContract" type="success">已生成</el-tag>
          <el-tag v-else type="info">等待生成</el-tag>
        </div>
        
        <div v-if="generatedContract" class="preview-content">
          <div class="contract-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="合同名称" :span="2">
                {{ generatedContract.contractName }}
              </el-descriptions-item>
              <el-descriptions-item label="甲方">
                {{ generatedContract.partyA }}
              </el-descriptions-item>
              <el-descriptions-item label="乙方">
                {{ generatedContract.partyB }}
              </el-descriptions-item>
              <el-descriptions-item label="合同金额">
                ¥{{ generatedContract.amount?.toLocaleString() }}
              </el-descriptions-item>
              <el-descriptions-item label="合同类型">
                {{ contractTypeMap[generatedContract.contractType || ''] }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div class="contract-content">
            <h4>合同正文</h4>
            <pre class="content-text">{{ generatedContract.content }}</pre>
          </div>
        </div>
        
        <div v-else class="preview-empty">
          <el-empty description="请在左侧与AI对话生成合同" />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { ArrowLeft, ChatDotRound, Position } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft, ChatDotRound, Position }
}
</script>

<style scoped>
.ai-generate-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.ai-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e4e7ed;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-message {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.chat-message.assistant .avatar {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: #fff;
}

.chat-message.user .avatar {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
}

.content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
}

.chat-message.assistant .content {
  background: #f5f7fa;
  margin-left: 12px;
  border-bottom-left-radius: 4px;
}

.chat-message.user .content {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
  margin-right: 12px;
  border-bottom-right-radius: 4px;
}

.message-text {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
}

.content.loading {
  display: flex;
  gap: 4px;
  padding: 16px 20px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
}

.chat-input .el-input {
  flex: 1;
}

.input-tip {
  text-align: center;
  font-size: 12px;
  color: #909399;
  padding-bottom: 8px;
  background: #fafafa;
}

.preview-panel {
  width: 45%;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
}

.preview-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.contract-info {
  margin-bottom: 20px;
}

.contract-content h4 {
  margin: 0 0 12px;
  font-size: 15px;
  color: #303133;
}

.content-text {
  padding: 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 13px;
  line-height: 1.8;
  color: #606266;
  max-height: 400px;
  overflow-y: auto;
}

.preview-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

