import request from '@/utils/request'

// AI对话相关接口

export interface AISession {
  id: number
  sessionId: string
  userId: number
  contractId?: number
  mode: 'ASK' | 'AGENT'
  contextData?: Record<string, any>
  contractSnapshot?: string
  subTypeCode?: string
  messageCount: number
  lastActiveAt: string
  expiredAt: string
  createdAt: string
}

export interface AIMessage {
  id: number
  sessionId: string
  role: 'USER' | 'ASSISTANT' | 'SYSTEM'
  content: string
  mode: 'ASK' | 'AGENT'
  agentAction?: AgentAction
  tokenCount: number
  createdAt: string
}

export interface AgentAction {
  actionType: 'MODIFY' | 'INSERT' | 'DELETE' | 'REPLACE' | 'GENERATE'
  fieldPath?: string
  locationDesc?: string
  oldValue?: string
  newValue?: string
  undoToken?: string
  canUndo?: boolean
}

export interface AIChatRequest {
  sessionId?: string
  message: string
  mode?: 'ASK' | 'AGENT'
  contractId?: number
  subTypeCode?: string
  currentContent?: string  // 当前合同内容（用于Agent模式）
  stream?: boolean
}

export interface AIChatResponse {
  sessionId: string
  mode: string
  content: string
  success: boolean
  actions?: AgentAction[]
  suggestions?: string[]
  error?: string
}

// 创建AI会话（进入编辑器时调用）
export function createAISession(params: {
  contractId?: number
  subTypeCode: string
  mode?: 'ASK' | 'AGENT'
}) {
  return request<AISession>({
    url: '/ai/chat/session/create',
    method: 'post',
    params
  })
}

// 获取会话信息
export function getAISession(sessionId: string) {
  return request<AISession>({
    url: `/ai/chat/session/${sessionId}`,
    method: 'get'
  })
}

// 切换模式
export function switchAIMode(sessionId: string, mode: 'ASK' | 'AGENT') {
  return request<AISession>({
    url: `/ai/chat/session/${sessionId}/mode`,
    method: 'post',
    params: { mode }
  })
}

// 获取会话历史消息
export function getSessionMessages(sessionId: string) {
  return request<AIMessage[]>({
    url: `/ai/chat/session/${sessionId}/messages`,
    method: 'get'
  })
}

// 更新合同快照
export function updateContractSnapshot(sessionId: string, content: string) {
  return request<boolean>({
    url: `/ai/chat/session/${sessionId}/snapshot`,
    method: 'post',
    data: content,
    headers: { 'Content-Type': 'text/plain' }
  })
}

// Ask模式 - 同步对话
export function askAI(data: AIChatRequest) {
  return request<AIChatResponse>({
    url: '/ai/chat/ask',
    method: 'post',
    data
  })
}

// Agent模式 - 执行命令
export function executeAgent(data: AIChatRequest) {
  return request<AIChatResponse>({
    url: '/ai/chat/agent',
    method: 'post',
    data
  })
}

// 撤销Agent操作
export function undoAgentAction(undoToken: string, contractId?: number) {
  return request<string>({
    url: '/ai/chat/agent/undo',
    method: 'post',
    data: { undoToken, contractId }
  })
}

// Ask模式 - 流式对话 (SSE)
export function askAIStream(data: AIChatRequest): EventSource | null {
  const token = localStorage.getItem('token')
  if (!token) return null
  
  // 使用fetch + ReadableStream实现SSE
  // 注意：实际实现需要后端支持SSE
  return null // 简化版本先返回null，实际使用同步API
}
