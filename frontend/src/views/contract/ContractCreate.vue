<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContractTypesGrouped, type ContractTypeGroup, type SubType } from '@/api/contractType'

const router = useRouter()

// 合同类型数据（从后端获取）
const typeGroups = ref<ContractTypeGroup[]>([])
const loading = ref(false)

// 选中的类型
const selectedMainType = ref<string | null>(null)
const selectedSubType = ref<SubType | null>(null)

// 主类型图标和颜色配置
const mainTypeConfig: Record<string, { icon: string; color: string; bgColor: string }> = {
  'TYPE_A': { icon: 'Box', color: '#1890ff', bgColor: '#e6f7ff' },
  'TYPE_B': { icon: 'SetUp', color: '#52c41a', bgColor: '#f6ffed' },
  'TYPE_C': { icon: 'Monitor', color: '#722ed1', bgColor: '#f9f0ff' }
}

// 加载合同类型
const loadContractTypes = async () => {
  loading.value = true
  try {
    const res = await getContractTypesGrouped()
    typeGroups.value = res.data || []
  } catch (error) {
    console.error('加载合同类型失败', error)
    // 使用备用数据
    typeGroups.value = [
      {
        typeCode: 'TYPE_A',
        typeName: '采购类',
        subTypes: [
          { subTypeCode: 'A1', subTypeName: '土建工程', description: '基站建设、传输管线、机房土建等' },
          { subTypeCode: 'A2', subTypeName: '装修工程', description: '营业厅装修、办公场所装修' },
          { subTypeCode: 'A3', subTypeName: '零星维修', description: '小额维修、日常维护' }
        ]
      },
      {
        typeCode: 'TYPE_B',
        typeName: '工程类',
        subTypes: [
          { subTypeCode: 'B1', subTypeName: '光缆代维', description: '光缆线路日常维护、故障抢修' },
          { subTypeCode: 'B2', subTypeName: '基站代维', description: '基站设备日常维护、巡检' },
          { subTypeCode: 'B3', subTypeName: '家宽代维', description: '家庭宽带安装、维护' },
          { subTypeCode: 'B4', subTypeName: '应急保障', description: '重大活动通信保障' }
        ]
      },
      {
        typeCode: 'TYPE_C',
        typeName: '服务类',
        subTypes: [
          { subTypeCode: 'C1', subTypeName: '定制开发', description: '软件系统定制开发' },
          { subTypeCode: 'C2', subTypeName: '商用软件采购', description: '软件许可、SaaS服务' },
          { subTypeCode: 'C3', subTypeName: 'DICT集成', description: '数字化转型、ICT集成' }
        ]
      }
    ]
  } finally {
    loading.value = false
  }
}

// 选择主类型
const selectMainType = (typeCode: string) => {
  selectedMainType.value = typeCode
  selectedSubType.value = null
}

// 选择子类型
const selectSubType = (subType: SubType) => {
  selectedSubType.value = subType
}

// 获取当前主类型的子类型列表
const currentSubTypes = () => {
  if (!selectedMainType.value) return []
  const group = typeGroups.value.find(g => g.typeCode === selectedMainType.value)
  return group?.subTypes || []
}

// 开始起草
const startDraft = () => {
  if (!selectedSubType.value) {
    ElMessage.warning('请先选择合同子类型')
    return
  }
  
  // 跳转到起草页面，传递子类型代码
  router.push({
    path: '/contract/draft',
    query: { 
      subType: selectedSubType.value.subTypeCode,
      mainType: selectedMainType.value
    }
  })
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadContractTypes()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <h2 class="page-title">创建合同</h2>
      <el-button @click="goBack">返回</el-button>
    </div>
    
    <!-- 步骤提示 -->
    <div class="step-hint">
      <el-steps :active="selectedSubType ? 2 : (selectedMainType ? 1 : 0)" align-center>
        <el-step title="选择主类型" />
        <el-step title="选择子类型" />
        <el-step title="开始起草" />
      </el-steps>
    </div>
    
    <div class="type-selection">
      <!-- 左侧：主类型 -->
      <div class="main-types">
        <h3 class="section-title">合同主类型</h3>
        <div class="main-type-list">
          <div 
            v-for="group in typeGroups"
            :key="group.typeCode"
            class="main-type-card"
            :class="{ active: selectedMainType === group.typeCode }"
            :style="{ 
              borderColor: selectedMainType === group.typeCode ? mainTypeConfig[group.typeCode]?.color : '',
              backgroundColor: selectedMainType === group.typeCode ? mainTypeConfig[group.typeCode]?.bgColor : ''
            }"
            @click="selectMainType(group.typeCode)"
          >
            <div 
              class="type-icon"
              :style="{ backgroundColor: mainTypeConfig[group.typeCode]?.color || '#1890ff' }"
            >
              <el-icon :size="28">
                <component :is="mainTypeConfig[group.typeCode]?.icon || 'Document'" />
              </el-icon>
            </div>
            <div class="type-info">
              <div class="type-name">{{ group.typeName }}</div>
              <div class="type-count">{{ group.subTypes?.length || 0 }} 个子类型</div>
            </div>
            <el-icon v-if="selectedMainType === group.typeCode" class="check-icon" color="#52c41a">
              <CircleCheck />
            </el-icon>
          </div>
        </div>
      </div>
      
      <!-- 右侧：子类型 -->
      <div class="sub-types" v-if="selectedMainType">
        <h3 class="section-title">
          选择具体类型
          <span class="selected-main">
            （{{ typeGroups.find(g => g.typeCode === selectedMainType)?.typeName }}）
          </span>
        </h3>
        <div class="sub-type-list">
          <div 
            v-for="subType in currentSubTypes()"
            :key="subType.subTypeCode"
            class="sub-type-card"
            :class="{ active: selectedSubType?.subTypeCode === subType.subTypeCode }"
            @click="selectSubType(subType)"
          >
            <div class="sub-type-header">
              <span class="sub-type-code">{{ subType.subTypeCode }}</span>
              <span class="sub-type-name">{{ subType.subTypeName }}</span>
            </div>
            <div class="sub-type-desc">{{ subType.description }}</div>
            <el-icon v-if="selectedSubType?.subTypeCode === subType.subTypeCode" class="check-icon" color="#52c41a">
              <CircleCheck />
            </el-icon>
          </div>
        </div>
      </div>
      
      <!-- 占位：未选择主类型时 -->
      <div class="sub-types empty" v-else>
        <el-empty description="请先选择合同主类型" />
      </div>
    </div>
    
    <!-- 底部操作 -->
    <div class="action-bar">
      <div class="selected-info" v-if="selectedSubType">
        <el-tag type="success" size="large">
          已选择：{{ selectedSubType.subTypeName }} ({{ selectedSubType.subTypeCode }})
        </el-tag>
      </div>
      <el-button 
        type="primary" 
        size="large"
        :disabled="!selectedSubType"
        @click="startDraft"
      >
        <el-icon><EditPen /></el-icon>
        开始起草
      </el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { CircleCheck, Box, SetUp, Monitor, Document, EditPen } from '@element-plus/icons-vue'
export default {
  components: { CircleCheck, Box, SetUp, Monitor, Document, EditPen }
}
</script>

<style scoped>
.step-hint {
  margin: 20px 0 30px;
  padding: 0 50px;
}

.type-selection {
  display: flex;
  gap: 30px;
  min-height: 400px;
}

.main-types {
  width: 320px;
  flex-shrink: 0;
}

.sub-types {
  flex: 1;
  min-width: 0;
}

.sub-types.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-radius: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.selected-main {
  font-weight: 400;
  color: #909399;
  font-size: 14px;
}

.main-type-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.main-type-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.main-type-card:hover {
  border-color: #c0c4cc;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.main-type-card.active {
  border-width: 2px;
}

.type-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: #fff;
}

.type-info {
  flex: 1;
}

.type-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.type-count {
  font-size: 13px;
  color: #909399;
}

.check-icon {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 20px;
}

.sub-type-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.sub-type-card {
  position: relative;
  padding: 20px;
  background: #fff;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.sub-type-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.sub-type-card.active {
  border-color: #52c41a;
  background: #f6ffed;
}

.sub-type-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.sub-type-code {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 8px;
}

.sub-type-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.sub-type-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.selected-info {
  margin-right: auto;
}
</style>
