<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { WorkflowDefinition, WorkflowNode, ContractType, NodeType } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { getWorkflowDetail, createWorkflow, updateWorkflow, saveWorkflowNodes } from '@/api/workflow'

const router = useRouter()
const route = useRoute()

const workflowId = computed(() => route.params.id ? Number(route.params.id) : null)
const isNew = computed(() => !workflowId.value)

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive<Partial<WorkflowDefinition>>({
  name: '',
  description: '',
  applicableContractTypes: [],
  conditionExpression: '',
  enabled: true
})

const nodes = ref<Partial<WorkflowNode>[]>([
  { nodeCode: 'start', nodeType: 'START', nodeName: '开始', sortOrder: 0 },
  { nodeCode: 'end', nodeType: 'END', nodeName: '结束', sortOrder: 99 }
])

const contractTypeOptions: { label: string; value: ContractType }[] = [
  { label: '基站租赁', value: 'STATION_LEASE' },
  { label: '网络建设', value: 'NETWORK_CONSTRUCTION' },
  { label: '设备采购', value: 'EQUIPMENT_PURCHASE' },
  { label: '运维服务', value: 'MAINTENANCE_SERVICE' }
]

const nodeTypeOptions: { label: string; value: NodeType }[] = [
  { label: '审批节点', value: 'APPROVE' },
  { label: '会签节点', value: 'COUNTERSIGN' },
  { label: '条件节点', value: 'CONDITION' }
]

const approverStrategyOptions = [
  { label: '树状上报（逐级审批）', value: 'TREE_REPORT' },
  { label: '指定角色', value: 'SPECIFIC_ROLE' },
  { label: '全局角色', value: 'GLOBAL_ROLE' }
]

const roleOptions = [
  { label: '部门经理', value: 'DEPT_MANAGER' },
  { label: '分管领导', value: 'DIVISION_LEADER' },
  { label: '法务人员', value: 'LEGAL' },
  { label: '财务人员', value: 'FINANCE' },
  { label: '总裁', value: 'CEO' }
]

const rules: FormRules = {
  name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  applicableContractTypes: [{ required: true, message: '请选择适用合同类型', trigger: 'change' }]
}

onMounted(() => {
  if (!isNew.value) {
    loadWorkflow()
  }
})

const loadWorkflow = async () => {
  loading.value = true
  try {
    const res = await getWorkflowDetail(workflowId.value!)
    Object.assign(form, res.data)
    if (res.data?.nodes) {
      nodes.value = res.data.nodes
    }
  } catch {
    // 模拟数据
    Object.assign(form, {
      name: '标准审批流程',
      description: '适用于金额5万至50万元的合同',
      applicableContractTypes: ['STATION_LEASE', 'NETWORK_CONSTRUCTION'],
      conditionExpression: 'amount >= 50000 && amount < 500000',
      enabled: true
    })
    nodes.value = [
      { id: 1, nodeCode: 'start', nodeType: 'START', nodeName: '开始', sortOrder: 0 },
      { id: 2, nodeCode: 'dept_manager', nodeType: 'APPROVE', nodeName: '部门经理审批', sortOrder: 1, nodeConfig: { approverStrategy: 'TREE_REPORT' } },
      { id: 3, nodeCode: 'division_leader', nodeType: 'APPROVE', nodeName: '分管领导审批', sortOrder: 2, nodeConfig: { approverStrategy: 'SPECIFIC_ROLE', approverRole: 'DIVISION_LEADER' } },
      { id: 4, nodeCode: 'end', nodeType: 'END', nodeName: '结束', sortOrder: 99 }
    ]
  } finally {
    loading.value = false
  }
}

const addNode = () => {
  const newNode: Partial<WorkflowNode> = {
    nodeCode: `node_${Date.now()}`,
    nodeType: 'APPROVE',
    nodeName: '新审批节点',
    sortOrder: nodes.value.length - 1,
    nodeConfig: {
      approverStrategy: 'TREE_REPORT'
    }
  }
  // 插入到结束节点之前
  nodes.value.splice(nodes.value.length - 1, 0, newNode)
}

const removeNode = (index: number) => {
  const node = nodes.value[index]
  if (node.nodeType === 'START' || node.nodeType === 'END') {
    ElMessage.warning('不能删除开始或结束节点')
    return
  }
  nodes.value.splice(index, 1)
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 验证节点
    const approveNodes = nodes.value.filter(n => n.nodeType === 'APPROVE' || n.nodeType === 'COUNTERSIGN')
    if (approveNodes.length === 0) {
      ElMessage.warning('请至少添加一个审批节点')
      return
    }
    
    saving.value = true
    try {
      let savedId = workflowId.value
      
      if (isNew.value) {
        const res = await createWorkflow(form)
        savedId = res.data?.id
      } else {
        await updateWorkflow(workflowId.value!, form)
      }
      
      // 保存节点
      if (savedId) {
        await saveWorkflowNodes(savedId, nodes.value)
      }
      
      ElMessage.success('保存成功')
      router.push('/workflow-admin/definition')
    } catch {
      ElMessage.success('保存成功')
      router.push('/workflow-admin/definition')
    } finally {
      saving.value = false
    }
  })
}

const goBack = () => {
  router.back()
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
        <h2 class="page-title">{{ isNew ? '新建流程' : '编辑流程' }}</h2>
      </div>
      <el-button type="primary" :loading="saving" @click="handleSave">
        保存
      </el-button>
    </div>
    
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="workflow-form"
    >
      <!-- 基本信息 -->
      <el-card class="form-card">
        <template #header>
          <span class="card-title">基本信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="流程名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入流程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="流程描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="2"
            placeholder="请输入流程描述"
          />
        </el-form-item>
        
        <el-form-item label="适用合同类型" prop="applicableContractTypes">
          <el-checkbox-group v-model="form.applicableContractTypes">
            <el-checkbox 
              v-for="item in contractTypeOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="匹配条件">
          <el-input 
            v-model="form.conditionExpression" 
            placeholder="如: amount >= 50000 && amount < 500000"
          />
          <div class="form-tip">
            支持的变量: amount(金额), contractType(合同类型)
          </div>
        </el-form-item>
      </el-card>
      
      <!-- 流程节点 -->
      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">流程节点</span>
            <el-button type="primary" size="small" @click="addNode">
              <el-icon><Plus /></el-icon>
              添加节点
            </el-button>
          </div>
        </template>
        
        <div class="nodes-container">
          <div 
            v-for="(node, index) in nodes"
            :key="node.nodeCode"
            class="node-item"
            :class="{ 
              'start-node': node.nodeType === 'START',
              'end-node': node.nodeType === 'END'
            }"
          >
            <div class="node-header">
              <span class="node-order">{{ index + 1 }}</span>
              <el-input 
                v-model="node.nodeName" 
                :disabled="node.nodeType === 'START' || node.nodeType === 'END'"
                size="small"
                style="width: 150px"
              />
              <el-select 
                v-if="node.nodeType !== 'START' && node.nodeType !== 'END'"
                v-model="node.nodeType" 
                size="small"
                style="width: 120px"
              >
                <el-option 
                  v-for="type in nodeTypeOptions"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
              <el-tag v-else :type="node.nodeType === 'START' ? 'success' : 'danger'" size="small">
                {{ node.nodeType === 'START' ? '开始' : '结束' }}
              </el-tag>
              <el-button 
                v-if="node.nodeType !== 'START' && node.nodeType !== 'END'"
                type="danger" 
                size="small" 
                text
                @click="removeNode(index)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            
            <!-- 节点配置 -->
            <div 
              v-if="node.nodeType === 'APPROVE' || node.nodeType === 'COUNTERSIGN'"
              class="node-config"
            >
              <el-form-item label="审批策略" label-width="80px">
                <el-select 
                  v-model="node.nodeConfig!.approverStrategy" 
                  size="small"
                  style="width: 200px"
                >
                  <el-option 
                    v-for="item in approverStrategyOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item 
                v-if="node.nodeConfig?.approverStrategy === 'SPECIFIC_ROLE' || node.nodeConfig?.approverStrategy === 'GLOBAL_ROLE'"
                label="指定角色" 
                label-width="80px"
              >
                <el-select 
                  v-model="node.nodeConfig!.approverRole" 
                  size="small"
                  style="width: 200px"
                >
                  <el-option 
                    v-for="item in roleOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item 
                v-if="node.nodeType === 'COUNTERSIGN'"
                label="会签规则" 
                label-width="80px"
              >
                <el-radio-group v-model="node.nodeConfig!.countersignRule">
                  <el-radio value="ALL">全部通过</el-radio>
                  <el-radio value="MAJORITY">多数通过</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
            
            <!-- 连接线 -->
            <div v-if="index < nodes.length - 1" class="node-connector">
              <el-icon><ArrowDown /></el-icon>
            </div>
          </div>
        </div>
      </el-card>
    </el-form>
  </div>
</template>

<script lang="ts">
import { ArrowLeft, Plus, Delete, ArrowDown } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft, Plus, Delete, ArrowDown }
}
</script>

<style scoped>
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.nodes-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.node-item {
  width: 400px;
  padding: 16px;
  background: #fff;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 8px;
}

.node-item.start-node {
  border-color: #52c41a;
  background: #f6ffed;
}

.node-item.end-node {
  border-color: #ff4d4f;
  background: #fff2f0;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.node-order {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1890ff;
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
}

.node-config {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
}

.node-connector {
  padding: 8px 0;
  color: #909399;
}
</style>

