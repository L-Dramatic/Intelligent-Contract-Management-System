<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { ContractType } from '@/types'

const router = useRouter()

const contractTypes = [
  {
    value: 'STATION_LEASE' as ContractType,
    label: '基站租赁合同',
    icon: 'OfficeBuilding',
    description: '适用于基站站址租赁场景，包括楼顶、铁塔、地面机房等',
    color: '#1890ff'
  },
  {
    value: 'NETWORK_CONSTRUCTION' as ContractType,
    label: '网络建设合同',
    icon: 'Connection',
    description: '适用于4G/5G基站建设、光缆铺设、机房建设等工程',
    color: '#52c41a'
  },
  {
    value: 'EQUIPMENT_PURCHASE' as ContractType,
    label: '设备采购合同',
    icon: 'Box',
    description: '适用于通信主设备、传输设备、动力设备等采购',
    color: '#faad14'
  },
  {
    value: 'MAINTENANCE_SERVICE' as ContractType,
    label: '运维服务合同',
    icon: 'SetUp',
    description: '适用于网络维护、故障处理、优化服务等场景',
    color: '#722ed1'
  }
]

const createMethods = [
  {
    value: 'ai',
    label: 'AI对话式生成',
    icon: 'ChatDotRound',
    description: '与AI助手对话，快速生成专业合同（推荐）',
    tag: '推荐'
  },
  {
    value: 'template',
    label: '使用标准模板',
    icon: 'Document',
    description: '选择预置模板，手动填写合同内容'
  },
  {
    value: 'upload',
    label: '上传已有合同',
    icon: 'Upload',
    description: '上传Word/PDF文件，系统自动解析'
  }
]

const selectedType = ref<ContractType | null>(null)
const selectedMethod = ref<string | null>(null)
const step = ref(1)

const selectType = (type: ContractType) => {
  selectedType.value = type
}

const selectMethod = (method: string) => {
  selectedMethod.value = method
}

const nextStep = () => {
  if (step.value === 1 && selectedType.value) {
    step.value = 2
  } else if (step.value === 2 && selectedMethod.value) {
    // 根据选择的方式跳转
    if (selectedMethod.value === 'ai') {
      router.push({
        path: '/contract/ai-generate',
        query: { type: selectedType.value }
      })
    } else if (selectedMethod.value === 'template') {
      router.push({
        path: '/contract/edit/new',
        query: { type: selectedType.value, mode: 'template' }
      })
    } else {
      router.push({
        path: '/contract/edit/new',
        query: { type: selectedType.value, mode: 'upload' }
      })
    }
  }
}

const prevStep = () => {
  if (step.value === 2) {
    step.value = 1
    selectedMethod.value = null
  }
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">创建合同</h2>
      <el-button @click="goBack">返回</el-button>
    </div>
    
    <!-- 步骤条 -->
    <el-steps :active="step" align-center class="steps">
      <el-step title="选择合同类型" />
      <el-step title="选择创建方式" />
      <el-step title="填写合同内容" />
    </el-steps>
    
    <!-- 步骤1：选择合同类型 -->
    <div v-if="step === 1" class="step-content">
      <h3 class="step-title">请选择合同类型</h3>
      <div class="type-grid">
        <div 
          v-for="type in contractTypes"
          :key="type.value"
          class="type-card"
          :class="{ active: selectedType === type.value }"
          @click="selectType(type.value)"
        >
          <div class="type-icon" :style="{ backgroundColor: type.color }">
            <el-icon :size="32"><component :is="type.icon" /></el-icon>
          </div>
          <div class="type-info">
            <h4 class="type-label">{{ type.label }}</h4>
            <p class="type-desc">{{ type.description }}</p>
          </div>
          <div v-if="selectedType === type.value" class="check-icon">
            <el-icon :size="24" color="#52c41a"><CircleCheck /></el-icon>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 步骤2：选择创建方式 -->
    <div v-if="step === 2" class="step-content">
      <h3 class="step-title">请选择创建方式</h3>
      <div class="method-grid">
        <div 
          v-for="method in createMethods"
          :key="method.value"
          class="method-card"
          :class="{ active: selectedMethod === method.value }"
          @click="selectMethod(method.value)"
        >
          <div class="method-icon">
            <el-icon :size="40"><component :is="method.icon" /></el-icon>
          </div>
          <div class="method-info">
            <h4 class="method-label">
              {{ method.label }}
              <el-tag v-if="method.tag" size="small" type="success">{{ method.tag }}</el-tag>
            </h4>
            <p class="method-desc">{{ method.description }}</p>
          </div>
          <div v-if="selectedMethod === method.value" class="check-icon">
            <el-icon :size="24" color="#52c41a"><CircleCheck /></el-icon>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部操作 -->
    <div class="step-actions">
      <el-button v-if="step > 1" @click="prevStep">上一步</el-button>
      <el-button 
        type="primary" 
        :disabled="(step === 1 && !selectedType) || (step === 2 && !selectedMethod)"
        @click="nextStep"
      >
        {{ step === 2 ? '开始创建' : '下一步' }}
      </el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { CircleCheck, OfficeBuilding, Connection, Box, SetUp, ChatDotRound, Document, Upload } from '@element-plus/icons-vue'
export default {
  components: { CircleCheck, OfficeBuilding, Connection, Box, SetUp, ChatDotRound, Document, Upload }
}
</script>

<style scoped>
.steps {
  margin: 30px 0;
}

.step-content {
  padding: 30px 0;
}

.step-title {
  text-align: center;
  font-size: 18px;
  color: #303133;
  margin-bottom: 30px;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.type-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.type-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.type-card.active {
  border-color: #52c41a;
  background: #f6ffed;
}

.type-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: #fff;
  flex-shrink: 0;
}

.type-info {
  flex: 1;
}

.type-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

.type-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
  line-height: 1.5;
}

.check-icon {
  position: absolute;
  top: 16px;
  right: 16px;
}

.method-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 600px;
  margin: 0 auto;
}

.method-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: #fff;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.method-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.method-card.active {
  border-color: #52c41a;
  background: #f6ffed;
}

.method-icon {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  border-radius: 12px;
  color: #fff;
  flex-shrink: 0;
}

.method-info {
  flex: 1;
}

.method-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

.method-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 30px;
  border-top: 1px solid #e4e7ed;
}
</style>

