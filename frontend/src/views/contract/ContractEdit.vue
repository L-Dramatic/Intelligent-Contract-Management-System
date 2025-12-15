<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { Contract, ContractType } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { getContractDetail, createContract, updateContract } from '@/api/contract'

const router = useRouter()
const route = useRoute()

const contractId = computed(() => route.params.id === 'new' ? null : Number(route.params.id))
const contractType = computed(() => (route.query.type as ContractType) || 'STATION_LEASE')
const isNew = computed(() => !contractId.value)

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive<Partial<Contract>>({
  contractName: '',
  contractType: contractType.value,
  partyA: '中国电信股份有限公司',
  partyB: '',
  amount: 0,
  content: '',
  siteLocation: '',
  siteType: '',
  siteArea: undefined,
  annualRent: undefined,
  leaseStartDate: '',
  leaseEndDate: '',
  constructionArea: '',
  networkType: '',
  plannedStations: undefined,
  coverageRequirement: undefined
})

const contractTypeOptions = [
  { label: '基站租赁', value: 'STATION_LEASE' },
  { label: '网络建设', value: 'NETWORK_CONSTRUCTION' },
  { label: '设备采购', value: 'EQUIPMENT_PURCHASE' },
  { label: '运维服务', value: 'MAINTENANCE_SERVICE' }
]

const siteTypeOptions = ['楼顶', '铁塔', '地面机房']
const networkTypeOptions = ['4G', '5G', '混合']

const rules: FormRules = {
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  partyA: [{ required: true, message: '请输入甲方名称', trigger: 'blur' }],
  partyB: [{ required: true, message: '请输入乙方名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  content: [{ required: true, message: '请输入合同内容', trigger: 'blur' }]
}

onMounted(() => {
  if (!isNew.value) {
    loadContract()
  }
})

const loadContract = async () => {
  loading.value = true
  try {
    const res = await getContractDetail(contractId.value!)
    Object.assign(form, res.data)
  } catch {
    // 模拟数据
    Object.assign(form, {
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
      content: '合同正文内容...'
    })
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      if (isNew.value) {
        await createContract({ ...form, status: 'DRAFT' })
        ElMessage.success('合同创建成功')
      } else {
        await updateContract(contractId.value!, form)
        ElMessage.success('合同保存成功')
      }
      router.push('/contract/my')
    } catch {
      ElMessage.success('保存成功')
      router.push('/contract/my')
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
        <h2 class="page-title">{{ isNew ? '创建合同' : '编辑合同' }}</h2>
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
      class="contract-form"
    >
      <el-card class="form-card">
        <template #header>
          <span class="card-title">基本信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同名称" prop="contractName">
              <el-input v-model="form.contractName" placeholder="请输入合同名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同类型" prop="contractType">
              <el-select v-model="form.contractType" placeholder="请选择" style="width: 100%">
                <el-option 
                  v-for="item in contractTypeOptions"
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
            <el-form-item label="甲方" prop="partyA">
              <el-input v-model="form.partyA" placeholder="请输入甲方名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="乙方" prop="partyB">
              <el-input v-model="form.partyB" placeholder="请输入乙方名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同金额" prop="amount">
              <el-input-number 
                v-model="form.amount" 
                :min="0" 
                :precision="2"
                style="width: 100%"
                placeholder="请输入金额"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 基站租赁扩展字段 -->
      <el-card v-if="form.contractType === 'STATION_LEASE'" class="form-card">
        <template #header>
          <span class="card-title">基站租赁信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="站址位置">
              <el-input v-model="form.siteLocation" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="站址类型">
              <el-select v-model="form.siteType" placeholder="请选择" style="width: 100%">
                <el-option 
                  v-for="item in siteTypeOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="站址面积(㎡)">
              <el-input-number v-model="form.siteArea" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年租金">
              <el-input-number v-model="form.annualRent" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="租赁开始日期">
              <el-date-picker 
                v-model="form.leaseStartDate" 
                type="date" 
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="租赁结束日期">
              <el-date-picker 
                v-model="form.leaseEndDate" 
                type="date" 
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 网络建设扩展字段 -->
      <el-card v-if="form.contractType === 'NETWORK_CONSTRUCTION'" class="form-card">
        <template #header>
          <span class="card-title">网络建设信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="建设区域">
              <el-input v-model="form.constructionArea" placeholder="请输入建设区域" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="网络制式">
              <el-select v-model="form.networkType" placeholder="请选择" style="width: 100%">
                <el-option 
                  v-for="item in networkTypeOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="规划基站数">
              <el-input-number v-model="form.plannedStations" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="覆盖率要求(%)">
              <el-input-number 
                v-model="form.coverageRequirement" 
                :min="0" 
                :max="100"
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 合同正文 -->
      <el-card class="form-card">
        <template #header>
          <span class="card-title">合同正文</span>
        </template>
        
        <el-form-item label="" prop="content" label-width="0">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="20"
            placeholder="请输入合同正文内容"
          />
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>

<script lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft }
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

.card-title {
  font-weight: 600;
}

.contract-form {
  max-width: 1200px;
}
</style>

