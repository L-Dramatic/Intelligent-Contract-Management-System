<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDefaultTemplate, type ContractTemplate } from '@/api/template'
import { getSubTypeDetail } from '@/api/contractType'
import { createAISession, askAI, executeAgent, switchAIMode, getSessionMessages, undoAgentAction, type AISession, type AIMessage, type AIChatResponse } from '@/api/aiChat'
import { createContract, updateContract, getContractDetail } from '@/api/contract'
import { createChange, submitChange as submitChangeAPI, getChangeDetail } from '@/api/contractChange'
import type { ContractChangeDTO } from '@/api/contractChange'

const router = useRouter()
const route = useRoute()

// 路由参数
const subTypeCode = computed(() => route.query.subType as string || 'A1')
const mainType = computed(() => route.query.mainType as string || 'TYPE_A')
const contractId = computed(() => route.query.id ? Number(route.query.id) : null)
const isChangeMode = computed(() => route.query.changeMode === 'true') // 是否为变更模式
const changeContractId = computed(() => route.query.changeContractId ? Number(route.query.changeContractId) : null) // 变更模式下的原合同ID
const changeId = computed(() => route.query.changeId ? Number(route.query.changeId) : null) // 编辑已有变更时的变更ID
const originalContract = ref<any>(null) // 原合同信息
const savedChangeId = ref<number | null>(null) // 已保存的变更ID

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
    
    // 只有新建合同时才用模板内容初始化（编辑已有合同时不覆盖）
    if (!contractId.value && res.data?.content) {
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
            // 处理换行符：将字面的\n转换为真实换行符
            let processedValue = action.newValue.replace(/\\n/g, '\n').replace(/\\r\\n/g, '\r\n')
            
            if (action.actionType === 'INSERT') {
              contractForm.content += '\n\n' + processedValue
            } else if (action.actionType === 'REPLACE') {
              // REPLACE操作：直接替换整个内容
              contractForm.content = processedValue
            } else if (action.actionType === 'MODIFY') {
              // MODIFY操作：尝试替换指定部分
              if (action.oldValue && contractForm.content.includes(action.oldValue)) {
                contractForm.content = contractForm.content.replace(action.oldValue, processedValue)
              } else {
                // 如果找不到oldValue，直接替换整个内容（降级处理）
                contractForm.content = processedValue
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

// 撤销Agent操作
const handleUndo = async (undoToken: string, messageId: number) => {
  if (!undoToken) {
    ElMessage.warning('撤销令牌不存在')
    return
  }
  
  try {
    const res = await undoAgentAction(undoToken, contractId.value || undefined)
    const restoredContent = res.data
    
    // 更新合同内容
    contractForm.content = restoredContent
    
    // 找到对应的消息，禁用撤销按钮
    const message = aiMessages.value.find(m => m.id === messageId)
    if (message && message.agentAction) {
      message.agentAction.canUndo = false
    }
    
    // 添加撤销成功的提示消息
    aiMessages.value.push({
      id: Date.now(),
      sessionId: aiSession.value?.sessionId || '',
      role: 'ASSISTANT',
      content: '✅ 已撤销操作，合同内容已恢复。',
      mode: 'AGENT',
      tokenCount: 0,
      createdAt: new Date().toISOString()
    })
    
    ElMessage.success('撤销成功')
    scrollToBottom()
  } catch (error: any) {
    console.error('撤销操作失败', error)
    ElMessage.error(error?.message || '撤销失败，请稍后重试')
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

// =============================================
// Preflight Check - 提交前完整性检查
// =============================================
interface PreflightError {
  field: string
  message: string
}

const preflightErrors = ref<PreflightError[]>([])
const preflightDialogVisible = ref(false)

// 运行 Preflight 检查
const runPreflightCheck = (): boolean => {
  const errors: PreflightError[] = []
  
  // 1. 检查必填字段
  if (!contractForm.name || contractForm.name.trim() === '') {
    errors.push({ field: '合同名称', message: '合同名称不能为空' })
  }
  
  if (!contractForm.partyB || contractForm.partyB.trim() === '') {
    errors.push({ field: '乙方名称', message: '乙方名称不能为空' })
  }
  
  if (!contractForm.amount || contractForm.amount <= 0) {
    errors.push({ field: '合同金额', message: '合同金额必须大于0' })
  }
  
  if (!contractForm.content || contractForm.content.trim() === '') {
    errors.push({ field: '合同内容', message: '合同内容不能为空' })
  }
  
  // 2. 检查未填写的模板变量 {{xxx}}
  const templateVarPattern = /\{\{([^}]+)\}\}/g
  const content = contractForm.content || ''
  const matches = content.match(templateVarPattern)
  if (matches && matches.length > 0) {
    const uniqueVars = [...new Set(matches)]
    uniqueVars.forEach(v => {
      errors.push({ field: '模板变量', message: `未填写: ${v}` })
    })
  }
  
  // 3. 检查关键条款是否存在（基于内容长度和关键词）
  const minContentLength = 200
  if (content.length < minContentLength) {
    errors.push({ field: '合同内容', message: `内容过短（至少${minContentLength}字），可能缺少关键条款` })
  }
  
  // 4. 检查关键条款关键词（可选警告）
  const requiredKeywords = ['甲方', '乙方', '金额', '期限']
  const missingKeywords = requiredKeywords.filter(kw => !content.includes(kw))
  if (missingKeywords.length > 0) {
    errors.push({ field: '关键条款', message: `可能缺少: ${missingKeywords.join('、')}` })
  }
  
  preflightErrors.value = errors
  
  if (errors.length > 0) {
    preflightDialogVisible.value = true
    return false
  }
  
  return true
}

// 强制提交（忽略警告）
const forceSubmit = async () => {
  preflightDialogVisible.value = false
  await doSaveContract(false) // 非草稿模式
}


// 保存合同（变更模式或普通模式）
const saveContract = async (isDraft = true) => {
  // 非草稿模式时执行 Preflight 检查
  if (!isDraft) {
    const passed = runPreflightCheck()
    if (!passed) {
      return // 检查失败，弹窗已显示
    }
  }
  
  await doSaveContract(isDraft)
}

// 实际保存逻辑
const doSaveContract = async (isDraft = true) => {
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
    // 变更模式：调用变更API
    if (isChangeMode.value && changeContractId.value) {
      const changeData: ContractChangeDTO = {
        contractId: changeContractId.value,
        title: `${contractForm.name} - 变更申请`,
        changeType: detectChangeType(),
        reasonType: 'OTHER',
        description: '通过AI编辑器修改合同',
        newName: contractForm.name !== originalContract.value?.name ? contractForm.name : undefined,
        newAmount: contractForm.amount !== originalContract.value?.amount ? contractForm.amount : undefined,
        newContent: contractForm.content !== originalContract.value?.content ? contractForm.content : undefined,
        newPartyB: contractForm.partyB !== originalContract.value?.partyB ? contractForm.partyB : undefined,
        newAttributes: contractForm.attributes
      }
      
      // 如果是编辑已有变更，需要更新（这里先创建新变更，实际应该调用更新API）
      const res = await createChange(changeData)
      savedChangeId.value = res.data.id
      
      if (!isDraft) {
        // 保存并提交
        await submitChangeAPI(res.data.id)
        ElMessage.success('变更申请已提交审批')
        router.push('/contract/change/list')
      } else {
        ElMessage.success('变更草稿保存成功')
      }
    } else {
      // 普通模式：调用合同API
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
    }
  } catch (error) {
    console.error('保存失败', error)
    const err = error as { message?: string }
    ElMessage.error(err.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 检测变更类型
const detectChangeType = (): string => {
  if (originalContract.value) {
    if (contractForm.amount !== originalContract.value.amount) {
      return 'AMOUNT'
    }
    if (contractForm.content !== originalContract.value.content) {
      return 'TECH'
    }
    if (contractForm.partyB !== originalContract.value.partyB) {
      return 'CONTACT'
    }
  }
  return 'OTHER'
}

// 返回
const goBack = () => {
  // 如果是变更模式，直接返回变更页面并传递内容
  if (isChangeMode.value) {
    const changeContext = sessionStorage.getItem('changeEditContext')
    if (changeContext) {
      try {
        const context = JSON.parse(changeContext)
        // 触发自定义事件，传递编辑后的内容
        const event = new CustomEvent('changeContentUpdated', {
          detail: { content: contractForm.content }
        })
        window.dispatchEvent(event)
        // 跳转回变更页面
        router.push(context.returnPath)
        sessionStorage.removeItem('changeEditContext')
        return
      } catch (e) {
        console.error('解析变更上下文失败', e)
      }
    }
  }
  
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

// 加载已有合同数据
const loadContract = async () => {
  const idToLoad = contractId.value || changeContractId.value
  if (!idToLoad) return
  
  try {
    const res = await getContractDetail(idToLoad)
    const contract = res.data
    originalContract.value = contract
    
    // 填充合同表单数据
    contractForm.name = contract.name || ''
    contractForm.partyA = contract.partyA || '中国移动通信集团XX省有限公司'
    contractForm.partyB = contract.partyB || ''
    contractForm.amount = contract.amount ? Number(contract.amount) : 0
    contractForm.content = contract.content || ''
    
    // 如果是编辑已有变更，加载变更数据
    if (changeId.value) {
      try {
        const changeRes = await getChangeDetail(changeId.value)
        const changeData = changeRes.data
        savedChangeId.value = changeData.id
        // 如果有新内容，使用新内容
        if (changeData.diffData?.newContent) {
          contractForm.content = changeData.diffData.newContent
        }
        if (changeData.diffData?.newAmount !== undefined) {
          contractForm.amount = changeData.diffData.newAmount
        }
        if (changeData.diffData?.newName) {
          contractForm.name = changeData.diffData.newName
        }
        if (changeData.diffData?.newPartyB) {
          contractForm.partyB = changeData.diffData.newPartyB
        }
      } catch (e) {
        console.error('加载变更数据失败', e)
      }
    }
    contractForm.type = contract.type || mainType.value
    
    // 填充扩展属性
    if (contract.attributes) {
      contractForm.attributes = { ...contract.attributes }
    }
  } catch (error) {
    console.error('加载合同数据失败', error)
    ElMessage.warning('加载合同数据失败，将使用空白模板')
  }
}

onMounted(async () => {
  await Promise.all([
    loadTemplate(),
    loadSubTypeInfo()
  ])
  
  // 如果是变更模式，先加载原合同
  if (isChangeMode.value && changeContractId.value) {
    await loadContract()
  } else if (contractId.value) {
    // 普通模式：加载已有合同数据
    await loadContract()
  }
  
  await initAISession()
})

// 监听页面卸载，如果是变更模式，保存内容到sessionStorage
onUnmounted(() => {
  if (isChangeMode.value) {
    // 保存当前编辑的内容，以便返回时使用
    const changeContext = sessionStorage.getItem('changeEditContext')
    if (changeContext) {
      try {
        const context = JSON.parse(changeContext)
        context.editedContent = contractForm.content
        sessionStorage.setItem('changeEditContext', JSON.stringify(context))
      } catch (e) {
        console.error('保存变更上下文失败', e)
      }
    }
  }
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
          <el-tag v-if="isChangeMode" type="warning" size="small">变更模式</el-tag>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="toggleSidebar">
          <el-icon><ChatDotRound /></el-icon>
          {{ aiSidebarVisible ? '收起AI助手' : '展开AI助手' }}
        </el-button>
        <el-button @click="saveContract(true)" :loading="saving">
          {{ isChangeMode ? '保存草稿' : '保存草稿' }}
        </el-button>
        <el-button type="primary" @click="saveContract(false)" :loading="saving">
          <el-icon><Check /></el-icon>
          {{ isChangeMode ? '保存并提交' : '保存' }}
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
                <!-- 撤销按钮 -->
                <el-button 
                  v-if="msg.agentAction.canUndo && msg.agentAction.undoToken"
                  type="text" 
                  size="small" 
                  class="undo-btn"
                  @click="handleUndo(msg.agentAction!.undoToken!, msg.id)"
                >
                  撤销此操作
                </el-button>
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
  
  <!-- Preflight Check 错误弹窗 -->
  <el-dialog
    v-model="preflightDialogVisible"
    title="📋 合同完整性检查"
    width="500px"
    :close-on-click-modal="false"
  >
    <div class="preflight-content">
      <el-alert
        title="提交前检查发现以下问题"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 16px;"
      />
      
      <el-table :data="preflightErrors" style="width: 100%">
        <el-table-column prop="field" label="字段" width="120">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.field }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="问题描述" />
      </el-table>
    </div>
    
    <template #footer>
      <el-button @click="preflightDialogVisible = false">返回修改</el-button>
      <el-button type="warning" @click="forceSubmit">忽略警告，强制提交</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts">
import { ArrowLeft, ChatDotRound, Check, ChatLineSquare, MagicStick, Position } from '@element-plus/icons-vue'

// 格式化消息（支持简单Markdown）
function formatMessage(text: string): string {
  if (!text) return ''
  return text
    // 先处理字面的\n字符串（转义后的），转换为真实换行符
    .replace(/\\n/g, '\n')
    .replace(/\\r\\n/g, '\r\n')
    .replace(/\\r/g, '\r')
    // 移除"[撤销此操作]"文本（因为现在使用按钮）
    .replace(/\[撤销此操作\]/g, '')
    // 处理Markdown格式
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    // 将真实换行符转换为HTML的<br>
    .replace(/\n/g, '<br>')
    .replace(/• /g, '&bull; ')
    // 清理多余的换行
    .replace(/<br><br><br>/g, '<br><br>')
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.undo-btn {
  margin-left: auto;
  color: #409eff;
  font-size: 12px;
}

.undo-btn:hover {
  color: #66b1ff;
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

