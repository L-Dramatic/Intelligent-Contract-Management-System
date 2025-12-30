<template>
  <div class="ai-qa-container">
    <div class="ai-qa-header">
      <div class="header-left">
        <h2>🤖 AI问答助手</h2>
        <p class="subtitle">咨询合同或工作相关问题</p>
      </div>
    </div>

    <el-card class="chat-card">
      <!-- 聊天区域 -->
      <div ref="chatContainerRef" class="chat-messages">
        <div 
          v-for="message in aiMessages" 
          :key="message.id"
          :class="['message-item', message.role === 'USER' ? 'user-message' : 'assistant-message']"
        >
          <div class="message-avatar">
            <el-avatar v-if="message.role === 'USER'" :size="32" style="background-color: #409eff">
              <el-icon><User /></el-icon>
            </el-avatar>
            <el-avatar v-else :size="32" style="background-color: #67c23a">
              <el-icon><Service /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            <div class="message-time">{{ formatTime(message.createdAt) }}</div>
          </div>
        </div>
        
        <div v-if="aiLoading" class="message-item assistant-message">
          <div class="message-avatar">
            <el-avatar :size="32" style="background-color: #67c23a">
              <el-icon><Robot /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-text">
              <el-icon class="is-loading"><Loading /></el-icon>
              AI正在思考中...
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <el-input
          v-model="aiInput"
          type="textarea"
          :rows="3"
          placeholder="请输入您的问题，例如：合同违约责任条款应该怎么写？"
          @keydown.ctrl.enter="sendAIMessage"
          @keydown.enter.exact.prevent="sendAIMessage"
        />
        <div class="input-actions">
          <div class="input-tip">按 Enter 发送，Ctrl + Enter 换行</div>
          <el-button 
            type="primary" 
            @click="sendAIMessage" 
            :loading="aiLoading"
            :disabled="!aiInput.trim()"
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Service, Loading, Promotion } from '@element-plus/icons-vue'
import { createAISession, askAI, type AISession, type AIMessage, type AIChatResponse } from '@/api/aiChat'

const aiSession = ref<AISession | null>(null)
const aiMessages = ref<AIMessage[]>([])
const aiInput = ref('')
const aiLoading = ref(false)
const chatContainerRef = ref<HTMLElement>()

// 格式化消息内容
const formatMessage = (text: string): string => {
  if (!text) return ''
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
    .replace(/• /g, '&bull; ')
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}小时前`
  
  return date.toLocaleString('zh-CN', { 
    month: 'short', 
    day: 'numeric', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

// 初始化AI会话
const initAISession = async () => {
  try {
    const res = await createAISession({
      subTypeCode: 'GENERAL', // 通用问答，不需要特定合同类型
      mode: 'ASK'
    })
    aiSession.value = res.data
    
    // 添加欢迎消息
    aiMessages.value = [{
      id: 0,
      sessionId: res.data?.sessionId || '',
      role: 'ASSISTANT',
      content: `👋 您好！我是AI问答助手。

我可以为您解答与合同或工作相关的各种问题，例如：
• 合同条款应该如何撰写？
• 违约责任条款有哪些要点？
• 付款方式应该如何约定？
• 合同审核应该注意哪些问题？
• 工作流程相关问题

有什么我可以帮您的吗？`,
      mode: 'ASK',
      tokenCount: 0,
      createdAt: new Date().toISOString()
    }]
    
    scrollToBottom()
  } catch (error) {
    console.error('初始化AI会话失败', error)
    ElMessage.error('AI服务初始化失败，请稍后重试')
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
    mode: 'ASK',
    tokenCount: 0,
    createdAt: new Date().toISOString()
  })
  
  scrollToBottom()
  aiLoading.value = true
  
  try {
    const res = await askAI({
      sessionId: aiSession.value.sessionId,
      message: userMessage,
      mode: 'ASK',
      subTypeCode: 'GENERAL'
    })
    const response: AIChatResponse = res.data
    
    // 添加AI回复
    aiMessages.value.push({
      id: Date.now() + 1,
      sessionId: aiSession.value.sessionId,
      role: 'ASSISTANT',
      content: response.content || 'AI服务暂时不可用',
      mode: 'ASK',
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
      mode: 'ASK',
      tokenCount: 0,
      createdAt: new Date().toISOString()
    })
  } finally {
    aiLoading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  initAISession()
})
</script>

<style scoped>
.ai-qa-container {
  padding: 20px;
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
}

.ai-qa-header {
  margin-bottom: 20px;
}

.ai-qa-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.ai-qa-header .subtitle {
  margin: 5px 0 0;
  color: #909399;
  font-size: 14px;
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
  margin-bottom: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 12px;
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.user-message .message-content {
  align-items: flex-end;
}

.assistant-message .message-content {
  align-items: flex-start;
}

.message-text {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  word-wrap: break-word;
}

.user-message .message-text {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}

.assistant-message .message-text {
  background: #f0f2f5;
  color: #303133;
  border-top-left-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  padding: 0 4px;
}

.chat-input-area {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-tip {
  font-size: 12px;
  color: #909399;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>

