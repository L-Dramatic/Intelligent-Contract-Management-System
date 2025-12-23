<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDefaultTemplate, type ContractTemplate } from '@/api/template'
import { getSubTypeDetail } from '@/api/contractType'
import { createAISession, askAI, executeAgent, switchAIMode, getSessionMessages, type AISession, type AIMessage, type AIChatResponse } from '@/api/aiChat'
import { createContract, updateContract } from '@/api/contract'

const router = useRouter()
const route = useRoute()

// 路由参数
const subTypeCode = computed(() => route.query.subType as string || 'A1')
const mainType = computed(() => route.query.mainType as string || 'TYPE_A')
const contractId = computed(() => route.query.id ? Number(route.query.id) : null)

// 状态
const loading = ref(false)
const saving = ref(false)
const template = ref<ContractTemplate | null>(null)
const subTypeInfo = ref<{ subTypeName: string; description: string } | null>(null)

// 合同表单数据
const contractForm = reactive({
  name: '',
  type: mainType.value,
  partyA: '中国移动通信集团XX省有限公司',
  partyB: '',
  amount: 0,
  content: '',
  attributes: {
    subTypeCode: subTypeCode.value
  } as Record<string, any>
})

// AI侧边栏状态
const aiSidebarVisible = ref(true)
const aiSession = ref<AISession | null>(null)
const aiMessages = ref<AIMessage[]>([])
const aiMode = ref<'ASK' | 'AGENT'>('ASK')
const aiInput = ref('')
const aiLoading = ref(false)
const chatContainerRef = ref<HTMLElement>()

// 加载模板
const loadTemplate = async () => {
  loading.value = true
  try {
    const res = await getDefaultTemplate(subTypeCode.value)
    template.value = res.data
    
    // 用模板内容初始化合同正文
    if (res.data?.content) {
      contractForm.content = res.data.content
      contractForm.name = `${res.data.name}-${new Date().toISOString().slice(0,10)}`
    }
  } catch (error) {
    console.error('加载模板失败', error)
    ElMessage.warning('模板加载失败，将使用空白模板')
  } finally {
    loading.value = false
  }
}

// 加载子类型信息
const loadSubTypeInfo = async () => {
  try {
    const res = await getSubTypeDetail(subTypeCode.value)
    subTypeInfo.value = {
      subTypeName: res.data?.subTypeName || subTypeCode.value,
      description: res.data?.description || ''
    }
  } catch (error) {
    subTypeInfo.value = { subTypeName: subTypeCode.value, description: '' }
  }
}

// 初始化AI会话
const initAISession = async () => {
  try {
    const res = await createAISession({
      subTypeCode: subTypeCode.value,
      contractId: contractId.value || undefined,
      mode: 'ASK'
    })
    aiSession.value = res.data
    
    // 添加欢迎消息
    aiMessages.value = [{
      id: 0,
      sessionId: res.data?.sessionId || '',
      role: 'ASSISTANT',
      content: `👋 您好！我是合同起草助手。

您正在起草 **${subTypeInfo.value?.subTypeName || subTypeCode.value}** 合同。

**Ask模式**：您可以问我任何问题，如：
• 这个条款应该怎么写？
• 安全费比例一般填多少？
• 帮我解释一下违约责任条款

**Agent模式**：我可以直接帮您修改合同，如：
• 把乙方名称改成XX公司
• 帮我生成付款条款
• 把金额改成50万

有什么我可以帮您的吗？`,
      mode: 'ASK',
      tokenCount: 0,
      createdAt: new Date().toISOString()
    }]
  } catch (error) {
    console.error('初始化AI会话失败', error)
  }
}

// 切换AI模式
const toggleAIMode = async (mode: 'ASK' | 'AGENT') => {
  if (!aiSession.value?.sessionId) return
  
  try {
    await switchAIMode(aiSession.value.sessionId, mode)
    aiMode.value = mode
    
    // 添加模式切换提示
    aiMessages.value.push({
      id: Date.now(),
      sessionId: aiSession.value.sessionId,
      role: 'ASSISTANT',
      content: mode === 'ASK' 
        ? '💬 已切换到 **Ask模式**，您可以向我提问。'
        : '⚡ 已切换到 **Agent模式**，我可以直接帮您修改合同内容。',
      mode: mode,
      tokenCount: 0,
      createdAt: new Date().toISOString()
    })
    
    scrollToBottom()
  } catch (error) {
    console.error('切换模式失败', error)
  }
}

// 发送AI消息
const sendAIMessage = async () => {
  if (!aiInput.value.trim() || aiLoading.value) return
  if (!aiSession.value?.sessionId) {
    ElMessage.warning('AI会话未初始化')
    return
  }
  
  const userMessage = aiInput.value.trim()
  aiInput.value = ''
  
  // 添加用户消息
  aiMessages.value.push({
    id: Date.now(),
    sessionId: aiSession.value.sessionId,
    role: 'USER',
    content: userMessage,
    mode: aiMode.value,
    tokenCount: 0,
    createdAt: new Date().toISOString()
  })
  
  scrollToBottom()
  aiLoading.value = true
  
  try {
    let response: AIChatResponse
    
    if (aiMode.value === 'ASK') {
      const res = await askAI({
        sessionId: aiSession.value.sessionId,
        message: userMessage,
        mode: 'ASK',
        subTypeCode: subTypeCode.value,
        currentContent: contractForm.content
      })
      response = res.data
    } else {
      const res = await executeAgent({
        sessionId: aiSession.value.sessionId,
        message: userMessage,
        mode: 'AGENT',
        subTypeCode: subTypeCode.value,
        currentContent: contractForm.content,
        contractId: contractId.value || undefined
      })
      response = res.data
      
      // Agent模式：如果有修改操作，更新合同内容
      if (response.actions && response.actions.length > 0) {
        for (const action of response.actions) {
          if (action.newValue) {
            // 简化处理：将新内容追加或替换
            if (action.actionType === 'INSERT') {
              contractForm.content += '\n\n' + action.newValue
            } else if (action.actionType === 'REPLACE' || action.actionType === 'MODIFY') {
              // 实际应该根据位置替换，这里简化处理
              if (action.oldValue) {
                contractForm.content = contractForm.content.replace(action.oldValue, action.newValue)
              }
            }
          }
        }
      }
    }
    
    // 添加AI回复
    aiMessages.value.push({
      id: Date.now() + 1,
      sessionId: aiSession.value.sessionId,
      role: 'ASSISTANT',
      content: response.content || 'AI服务暂时不可用',
      mode: aiMode.value,
      agentAction: response.actions?.[0],
      tokenCount: 0,
      createdAt: new Date().toISOString()
    })
  } catch (error) {
    console.error('AI请求失败', error)
    aiMessages.value.push({
      id: Date.now() + 1,
      sessionId: aiSession.value?.sessionId || '',
      role: 'ASSISTANT',
      content: '抱歉，AI服务暂时不可用，请稍后重试。',
      mode: aiMode.value,
      tokenCount: 0,
      createdAt: new Date().toISOString()
    })
  } finally {
    aiLoading.value = false
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

// 保存合同
const saveContract = async (isDraft = true) => {
  if (!contractForm.name) {
    ElMessage.warning('请输入合同名称')
    return
  }
  if (!contractForm.partyB) {
    ElMessage.warning('请输入乙方名称')
    return
  }
  
  saving.value = true
  try {
    const data = {
      name: contractForm.name,
      type: mainType.value,
      partyA: contractForm.partyA,
      partyB: contractForm.partyB,
      amount: contractForm.amount,
      content: contractForm.content,
      attributes: {
        ...contractForm.attributes,
        subTypeCode: subTypeCode.value
      },
      isDraft: isDraft
    }
    
    if (contractId.value) {
      await updateContract(contractId.value, data)
      ElMessage.success('合同保存成功')
    } else {
      const res = await createContract(data)
      ElMessage.success(isDraft ? '合同已保存为草稿' : '合同保存成功')
      // 跳转到合同详情或列表
      router.push('/contract/my')
    }
  } catch (error) {
    console.error('保存失败', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 返回
const goBack = () => {
  ElMessageBox.confirm('确定要离开吗？未保存的内容将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.back()
  }).catch(() => {})
}

// 切换侧边栏
const toggleSidebar = () => {
  aiSidebarVisible.value = !aiSidebarVisible.value
}

onMounted(async () => {
  await Promise.all([
    loadTemplate(),
    loadSubTypeInfo()
  ])
  await initAISession()
})
</script>

<template>
  <div class="draft-container" v-loading="loading">
    <!-- 顶部工具栏 -->
    <div class="draft-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="contract-title">
          <el-input 
            v-model="contractForm.name" 
            placeholder="请输入合同名称"
            class="title-input"
          />
          <el-tag type="info" size="small">{{ subTypeInfo?.subTypeName || subTypeCode }}</el-tag>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="toggleSidebar">
          <el-icon><ChatDotRound /></el-icon>
          {{ aiSidebarVisible ? '收起AI助手' : '展开AI助手' }}
        </el-button>
        <el-button @click="saveContract(true)" :loading="saving">
          保存草稿
        </el-button>
        <el-button type="primary" @click="saveContract(false)" :loading="saving">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </div>
    </div>
    
    <!-- 主体内容 -->
    <div class="draft-body">
      <!-- 左侧：合同编辑区 -->
      <div class="editor-panel" :class="{ 'full-width': !aiSidebarVisible }">
        <!-- 基本信息 -->
        <el-card class="info-card">
          <template #header>
            <span class="card-title">基本信息</span>
          </template>
          <el-form label-width="80px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="甲方">
                  <el-input v-model="contractForm.partyA" placeholder="甲方名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="乙方">
                  <el-input v-model="contractForm.partyB" placeholder="乙方名称" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="合同金额">
                  <el-input-number 
                    v-model="contractForm.amount" 
                    :min="0" 
                    :precision="2"
                    style="width: 100%"
                    placeholder="元"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="合同类型">
                  <el-tag>{{ subTypeInfo?.subTypeName }}</el-tag>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
        
        <!-- 合同正文编辑器 -->
        <el-card class="content-card">
          <template #header>
            <div class="content-header">
              <span class="card-title">合同正文</span>
              <span class="template-hint" v-if="template">
                基于模板: {{ template.name }}
              </span>
            </div>
          </template>
          <el-input
            v-model="contractForm.content"
            type="textarea"
            :rows="25"
            placeholder="在此编辑合同内容..."
            class="content-editor"
          />
        </el-card>
      </div>
      
      <!-- 右侧：AI助手侧边栏 -->
      <div class="ai-sidebar" v-show="aiSidebarVisible">
        <div class="ai-header">
          <div class="ai-title">
            <el-icon><ChatDotRound /></el-icon>
            AI助手
          </div>
          <div class="mode-switch">
            <el-radio-group v-model="aiMode" size="small" @change="toggleAIMode">
              <el-radio-button value="ASK">
                <el-icon><ChatLineSquare /></el-icon>
                Ask
              </el-radio-button>
              <el-radio-button value="AGENT">
                <el-icon><MagicStick /></el-icon>
                Agent
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
        
        <!-- 聊天区域 -->
        <div ref="chatContainerRef" class="ai-messages">
          <div 
            v-for="msg in aiMessages" 
            :key="msg.id"
            class="message-item"
            :class="msg.role.toLowerCase()"
          >
            <div class="message-avatar">
              <el-icon v-if="msg.role === 'ASSISTANT'" :size="18"><ChatDotRound /></el-icon>
              <span v-else>我</span>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <!-- Agent操作结果 -->
              <div v-if="msg.agentAction" class="agent-action">
                <el-tag type="success" size="small">
                  {{ msg.agentAction.actionType }}
                </el-tag>
                <span v-if="msg.agentAction.locationDesc">
                  {{ msg.agentAction.locationDesc }}
                </span>
              </div>
            </div>
          </div>
          
          <!-- 加载中 -->
          <div v-if="aiLoading" class="message-item assistant">
            <div class="message-avatar">
              <el-icon :size="18"><ChatDotRound /></el-icon>
            </div>
            <div class="message-content loading">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="ai-input">
          <el-input
            v-model="aiInput"
            type="textarea"
            :rows="2"
            :placeholder="aiMode === 'ASK' ? '输入问题...' : '输入修改指令...'"
            @keydown.enter.ctrl="sendAIMessage"
          />
          <el-button 
            type="primary" 
            :disabled="!aiInput.trim() || aiLoading"
            @click="sendAIMessage"
          >
            <el-icon><Position /></el-icon>
          </el-button>
        </div>
        <div class="input-hint">Ctrl + Enter 发送</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { ArrowLeft, ChatDotRound, Check, ChatLineSquare, MagicStick, Position } from '@element-plus/icons-vue'

// 格式化消息（支持简单Markdown）
function formatMessage(text: string): string {
  if (!text) return ''
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
    .replace(/• /g, '&bull; ')
}

export default {
  components: { ArrowLeft, ChatDotRound, Check, ChatLineSquare, MagicStick, Position },
  methods: { formatMessage }
}
</script>

<style scoped>
.draft-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 80px);
  background: #f5f7fa;
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.contract-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-input {
  width: 300px;
}

.title-input :deep(.el-input__inner) {
  font-size: 16px;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 12px;
}

.draft-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  padding: 16px;
  gap: 16px;
}

.editor-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
  transition: all 0.3s;
}

.editor-panel.full-width {
  max-width: 100%;
}

.info-card, .content-card {
  background: #fff;
  border-radius: 8px;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-hint {
  font-size: 12px;
  color: #909399;
}

.content-editor :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.8;
}

/* AI侧边栏样式 */
.ai-sidebar {
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  overflow: hidden;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.mode-switch :deep(.el-radio-button__inner) {
  padding: 6px 12px;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.message-item.user .message-avatar {
  background: #1890ff;
  color: #fff;
}

.message-content {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
}

.message-item.assistant .message-content {
  background: #f5f7fa;
  border-bottom-left-radius: 4px;
}

.message-item.user .message-content {
  background: #1890ff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-text {
  word-break: break-word;
}

.agent-action {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #e4e7ed;
  font-size: 12px;
  color: #909399;
}

.message-content.loading {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
  background: #f5f7fa;
}

.dot {
  width: 6px;
  height: 6px;
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

.ai-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
}

.ai-input .el-textarea {
  flex: 1;
}

.input-hint {
  text-align: center;
  font-size: 11px;
  color: #c0c4cc;
  padding-bottom: 8px;
  background: #fafafa;
}
</style>

