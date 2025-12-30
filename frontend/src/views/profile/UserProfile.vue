<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人主页</h2>
      <p class="subtitle">管理您的个人信息</p>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
        v-loading="loading"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
              <div class="form-tip">用户名不可修改</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色">
              <el-input :value="roleName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input :value="form.departmentName || '-'" disabled />
              <div class="form-tip">部门信息由管理员管理</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">
            保存修改
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUser } from '@/api/user'
import type { UserInfo } from '@/types'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  id: 0,
  username: '',
  realName: '',
  email: '',
  phone: '',
  departmentName: ''
})

const originalForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

// 角色名称映射
const roleNameMap: Record<string, string> = {
  'ADMIN': '系统管理员',
  'STAFF': '工作人员',
  'BOSS': '领导层'
}

const roleName = computed(() => {
  return roleNameMap[userStore.role] || userStore.role
})

// 表单验证规则
const rules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  loading.value = true
  try {
    const res = await getUserInfo()
    const userInfo = res.data as UserInfo
    
    form.id = userInfo.id
    form.username = userInfo.username || ''
    form.realName = userInfo.realName || ''
    form.email = userInfo.email || ''
    form.phone = userInfo.phone || ''
    form.departmentName = userInfo.departmentName || ''
    
    // 保存原始值
    originalForm.realName = form.realName
    originalForm.email = form.email
    originalForm.phone = form.phone
  } catch (error) {
    console.error('加载用户信息失败', error)
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

// 保存修改
const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      await updateUser(form.id, {
        realName: form.realName,
        email: form.email,
        phone: form.phone
      })
      
      // 更新 store 中的用户信息
      if (userStore.userInfo) {
        userStore.setUserInfo({
          ...userStore.userInfo,
          realName: form.realName,
          email: form.email,
          phone: form.phone
        })
      }
      
      // 更新原始值
      originalForm.realName = form.realName
      originalForm.email = form.email
      originalForm.phone = form.phone
      
      ElMessage.success('保存成功')
    } catch (error) {
      const err = error as { message?: string }
      ElMessage.error(err.message || '保存失败')
    } finally {
      saving.value = false
    }
  })
}

// 重置表单
const handleReset = () => {
  form.realName = originalForm.realName
  form.email = originalForm.email
  form.phone = originalForm.phone
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-header {
  margin-bottom: 20px;
}

.profile-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.profile-header .subtitle {
  margin: 5px 0 0;
  color: #909399;
  font-size: 14px;
}

.card-header {
  font-weight: 500;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

