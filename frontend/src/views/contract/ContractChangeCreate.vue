<template>
  <div class="contract-change-create">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">发起合同变更</span>
        </template>
      </el-page-header>
    </div>

    <!-- 原合同信息卡片 -->
    <el-card class="original-contract-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>📄 原合同信息</span>
          <el-tag :type="getContractStatusType(contract?.status)">
            {{ getContractStatusName(contract?.status) }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="3" border v-if="contract">
        <el-descriptions-item label="合同编号">{{ contract.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contract.name }}</el-descriptions-item>
        <el-descriptions-item label="当前版本">{{ contract.version || 'v1.0' }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ getTypeName(contract.type) }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">
          <span class="amount">¥ {{ formatAmount(contract.amount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="乙方">{{ contract.partyB }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 变更申请表单 -->
    <el-card class="change-form-card">
      <template #header>
        <div class="card-header">
          <span>✏️ 变更申请信息</span>
          <el-tag v-if="isMajorChange" type="danger">重大变更</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="变更标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入变更标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更类型" prop="changeType">
              <el-select v-model="form.changeType" placeholder="请选择变更类型" style="width: 100%">
                <el-option
                  v-for="item in CHANGE_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="变更原因" prop="reasonType">
              <el-select v-model="form.reasonType" placeholder="请选择变更原因" style="width: 100%">
                <el-option
                  v-for="item in REASON_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新合同金额">
              <el-input-number
                v-model="form.newAmount"
                :min="0"
                :precision="2"
                :step="10000"
                style="width: 100%"
                @change="checkMajor"
              />
              <div class="amount-diff" v-if="amountDiff !== 0">
                <span :class="amountDiff > 0 ? 'increase' : 'decrease'">
                  {{ amountDiff > 0 ? '+' : '' }}{{ formatAmount(amountDiff) }}
                  ({{ changePercent.toFixed(1) }}%)
                </span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="变更说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述变更内容和必要性"
          />
        </el-form-item>

        <el-form-item label="乙方沟通记录" v-if="form.reasonType === 'PASSIVE'">
          <el-input
            v-model="form.partyBCommunication"
            type="textarea"
            :rows="3"
            placeholder="若应乙方要求，请记录沟通情况（邮件截图、会议纪要等）"
          />
        </el-form-item>

        <!-- 变更内容编辑 -->
        <el-divider content-position="left">变更内容编辑</el-divider>

        <el-form-item label="新合同名称">
          <el-input v-model="form.newName" placeholder="留空表示不变更" />
        </el-form-item>

        <el-form-item label="新乙方名称">
          <el-input v-model="form.newPartyB" placeholder="留空表示不变更" />
        </el-form-item>

        <el-form-item label="新合同正文">
          <el-input
            v-model="form.newContent"
            type="textarea"
            :rows="8"
            placeholder="留空表示不变更合同正文"
          />
        </el-form-item>
      </el-form>

      <!-- 变更对比预览 -->
      <el-divider content-position="left">变更对比预览</el-divider>
      <div class="diff-preview">
        <el-table :data="diffItems" border stripe>
          <el-table-column prop="fieldLabel" label="字段" width="120" />
          <el-table-column label="变更前" min-width="200">
            <template #default="{ row }">
              <span class="before-value">{{ formatValue(row.beforeValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变更后" min-width="200">
            <template #default="{ row }">
              <span class="after-value">{{ formatValue(row.afterValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变化" width="150">
            <template #default="{ row }">
              <el-tag :type="row.changeDesc.includes('+') ? 'danger' : 'success'" size="small">
                {{ row.changeDesc }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="diffItems.length === 0" description="暂无变更内容" />
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="saveDraft" :loading="saving">
          保存草稿
        </el-button>
        <el-button type="success" @click="submitForApproval" :loading="submitting">
          提交审批
        </el-button>
      </div>
    </el-card>

    <!-- 重大变更提示对话框 -->
    <el-dialog v-model="majorChangeDialogVisible" title="重大变更提示" width="500px">
      <div class="major-change-warning">
        <el-icon class="warning-icon" color="#E6A23C" :size="48"><Warning /></el-icon>
        <p>此变更将被判定为<strong>重大变更</strong>，原因：</p>
        <ul>
          <li v-if="changePercent > 20">金额变更超过原金额的20%（当前：{{ changePercent.toFixed(1) }}%）</li>
          <li v-if="form.changeType === 'TECH'">技术方案变更</li>
        </ul>
        <p>重大变更将需要<strong>增加法务会签</strong>环节，审批流程会相应延长。</p>
        <p>是否继续提交？</p>
      </div>
      <template #footer>
        <el-button @click="majorChangeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSubmit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { getContractDetail } from '@/api/contract'
import { 
  createChange, 
  submitChange, 
  checkMajorChange,
  CHANGE_TYPE_OPTIONS,
  REASON_TYPE_OPTIONS,
  type ContractChangeDTO
} from '@/api/contractChange'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const contract = ref<any>(null)
const isMajorChange = ref(false)
const majorChangeDialogVisible = ref(false)
const createdChangeId = ref<number | null>(null)

// 表单数据
const form = reactive<ContractChangeDTO>({
  contractId: 0,
  title: '',
  changeType: '',
  reasonType: '',
  description: '',
  partyBCommunication: '',
  newName: '',
  newAmount: undefined as any,
  newContent: '',
  newPartyB: '',
  newAttributes: {},
  attachmentPath: ''
})

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入变更标题', trigger: 'blur' }],
  changeType: [{ required: true, message: '请选择变更类型', trigger: 'change' }],
  reasonType: [{ required: true, message: '请选择变更原因', trigger: 'change' }],
  description: [{ required: true, message: '请输入变更说明', trigger: 'blur' }]
}

// 计算金额差额
const amountDiff = computed(() => {
  if (!contract.value || form.newAmount === undefined || form.newAmount === null) return 0
  return form.newAmount - (contract.value.amount || 0)
})

// 计算变更百分比
const changePercent = computed(() => {
  if (!contract.value || !contract.value.amount || contract.value.amount === 0) return 0
  return Math.abs(amountDiff.value) / contract.value.amount * 100
})

// 计算变更对比项
const diffItems = computed(() => {
  if (!contract.value) return []
  
  const items: any[] = []
  
  if (form.newName && form.newName !== contract.value.name) {
    items.push({
      fieldLabel: '合同名称',
      beforeValue: contract.value.name,
      afterValue: form.newName,
      changeDesc: '已变更'
    })
  }
  
  if (form.newAmount !== undefined && form.newAmount !== null && form.newAmount !== contract.value.amount) {
    const diff = form.newAmount - (contract.value.amount || 0)
    items.push({
      fieldLabel: '合同金额',
      beforeValue: `¥${formatAmount(contract.value.amount)}`,
      afterValue: `¥${formatAmount(form.newAmount)}`,
      changeDesc: `${diff >= 0 ? '+' : ''}${formatAmount(diff)} (${changePercent.value.toFixed(1)}%)`
    })
  }
  
  if (form.newPartyB && form.newPartyB !== contract.value.partyB) {
    items.push({
      fieldLabel: '乙方',
      beforeValue: contract.value.partyB,
      afterValue: form.newPartyB,
      changeDesc: '已变更'
    })
  }
  
  if (form.newContent && form.newContent !== contract.value.content) {
    items.push({
      fieldLabel: '合同正文',
      beforeValue: '（原正文）',
      afterValue: '（新正文）',
      changeDesc: '已变更'
    })
  }
  
  return items
})

// 加载合同详情
const loadContract = async () => {
  const contractId = Number(route.params.contractId)
  if (!contractId) {
    ElMessage.error('缺少合同ID')
    router.back()
    return
  }
  
  form.contractId = contractId
  loading.value = true
  
  try {
    const res = await getContractDetail(contractId)
    contract.value = res.data
    
    // 初始化表单默认值
    form.newAmount = contract.value.amount
    form.title = `${contract.value.name} - 变更申请`
  } catch (error: any) {
    ElMessage.error(error.message || '加载合同失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 检查是否为重大变更
const checkMajor = async () => {
  if (changePercent.value > 20 || form.changeType === 'TECH') {
    isMajorChange.value = true
  } else {
    isMajorChange.value = false
  }
}

// 保存草稿
const saveDraft = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  
  saving.value = true
  try {
    const res = await createChange(form)
    createdChangeId.value = res.data.id
    ElMessage.success('草稿保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 提交审批
const submitForApproval = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  
  // 检查是否为重大变更
  if (isMajorChange.value) {
    majorChangeDialogVisible.value = true
    return
  }
  
  await doSubmit()
}

// 确认提交（重大变更确认后）
const confirmSubmit = async () => {
  majorChangeDialogVisible.value = false
  await doSubmit()
}

// 执行提交
const doSubmit = async () => {
  submitting.value = true
  try {
    // 先保存，再提交
    let changeId = createdChangeId.value
    if (!changeId) {
      const res = await createChange(form)
      changeId = res.data.id
    }
    
    await submitChange(changeId)
    ElMessage.success('变更申请已提交审批')
    router.push('/contract/change/list')
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 辅助函数
const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    'TYPE_A': 'A类-工程施工',
    'TYPE_B': 'B类-代维服务',
    'TYPE_C': 'C类-IT服务'
  }
  return map[type] || type
}

const getContractStatusType = (status: number) => {
  const map: Record<number, string> = {
    0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info', 5: 'warning', 6: 'info'
  }
  return map[status] || 'info'
}

const getContractStatusName = (status: number) => {
  const map: Record<number, string> = {
    0: '草稿', 1: '审批中', 2: '已生效', 3: '已驳回', 4: '已终止', 5: '待签署', 6: '已作废'
  }
  return map[status] || '未知'
}

const formatAmount = (amount: number) => {
  if (amount === undefined || amount === null) return '0.00'
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatValue = (value: any) => {
  if (value === undefined || value === null) return '-'
  if (typeof value === 'string' && value.length > 50) {
    return value.substring(0, 50) + '...'
  }
  return value
}

onMounted(() => {
  loadContract()
})
</script>

<style scoped lang="scss">
.contract-change-create {
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    .page-title {
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  .original-contract-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .amount {
      color: #e6a23c;
      font-weight: 600;
    }
  }
  
  .change-form-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .amount-diff {
      margin-top: 5px;
      font-size: 12px;
      
      .increase {
        color: #f56c6c;
      }
      
      .decrease {
        color: #67c23a;
      }
    }
  }
  
  .diff-preview {
    margin-bottom: 20px;
    
    .before-value {
      color: #909399;
    }
    
    .after-value {
      color: #409eff;
      font-weight: 500;
    }
  }
  
  .form-actions {
    display: flex;
    justify-content: center;
    gap: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }
  
  .major-change-warning {
    text-align: center;
    
    .warning-icon {
      margin-bottom: 15px;
    }
    
    ul {
      text-align: left;
      margin: 15px 0;
      padding-left: 20px;
      
      li {
        color: #e6a23c;
        margin: 5px 0;
      }
    }
    
    strong {
      color: #f56c6c;
    }
  }
}
</style>

