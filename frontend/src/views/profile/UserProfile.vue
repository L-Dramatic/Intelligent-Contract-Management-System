<template>
  <div class="profile-page">
    <!-- 顶部用户卡片 -->
    <div class="user-hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="avatar-section">
          <div class="avatar">
            <span class="avatar-text">{{ avatarText }}</span>
          </div>
          <div class="user-meta">
            <h1 class="user-name">{{ form.realName || form.username }}</h1>
            <div class="user-role">
              <el-tag :type="roleTagType" effect="dark" size="large">{{ roleName }}</el-tag>
              <span class="dept-name" v-if="form.departmentName">{{ form.departmentName }}</span>
            </div>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-value">{{ userStore.userInfo?.id || '-' }}</div>
            <div class="stat-label">用户ID</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ formatDate(userStore.userInfo?.createdAt) }}</div>
            <div class="stat-label">注册时间</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="profile-content">
      <el-row :gutter="24">
        <!-- 左侧：基本信息编辑 -->
        <el-col :xs="24" :sm="24" :md="16" :lg="16">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><User /></el-icon>
                  <span>基本信息</span>
                </div>
                <el-tag v-if="hasChanges" type="warning" size="small">有未保存的修改</el-tag>
              </div>
            </template>

            <el-form 
              ref="formRef" 
              :model="form" 
              :rules="rules" 
              label-position="top"
              v-loading="loading"
              class="profile-form"
            >
              <el-row :gutter="24">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="用户名">
                    <el-input 
                      v-model="form.username" 
                      disabled 
                      prefix-icon="User"
                    >
                      <template #suffix>
                        <el-tooltip content="用户名不可修改" placement="top">
                          <el-icon class="info-icon"><InfoFilled /></el-icon>
                        </el-tooltip>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="12">
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input 
                      v-model="form.realName" 
                      placeholder="请输入真实姓名"
                      prefix-icon="UserFilled"
                      clearable
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="邮箱地址" prop="email">
                    <el-input 
                      v-model="form.email" 
                      placeholder="请输入邮箱地址"
                      prefix-icon="Message"
                      clearable
                    />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="12">
                  <el-form-item label="手机号码" prop="mobile">
                    <el-input 
                      v-model="form.mobile" 
                      placeholder="请输入手机号码"
                      prefix-icon="Phone"
                      clearable
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 系统分配信息（只读） -->
              <el-divider content-position="left">
                <el-icon><Setting /></el-icon>
                <span style="margin-left: 6px;">系统分配信息（由管理员设定）</span>
              </el-divider>
              
              <el-row :gutter="24">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="所属部门">
                    <el-input :value="form.departmentName || '未分配'" disabled prefix-icon="OfficeBuilding">
                      <template #suffix>
                        <el-tooltip content="部门由管理员分配，如需修改请联系管理员" placement="top">
                          <el-icon class="info-icon"><InfoFilled /></el-icon>
                        </el-tooltip>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="12">
                  <el-form-item label="岗位职责">
                    <el-input :value="primaryRoleName" disabled prefix-icon="Briefcase">
                      <template #suffix>
                        <el-tooltip content="职位由管理员分配，如需修改请联系管理员" placement="top">
                          <el-icon class="info-icon"><InfoFilled /></el-icon>
                        </el-tooltip>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="24">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="人员级别">
                    <el-input :value="roleName" disabled prefix-icon="Avatar">
                      <template #suffix>
                        <el-tooltip content="级别由管理员分配" placement="top">
                          <el-icon class="info-icon"><InfoFilled /></el-icon>
                        </el-tooltip>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions">
                <el-button 
                  type="primary" 
                  @click="handleSave" 
                  :loading="saving"
                  :disabled="!hasChanges"
                  size="large"
                >
                  <el-icon><Check /></el-icon>
                  保存修改
                </el-button>
                <el-button 
                  @click="handleReset" 
                  :disabled="!hasChanges"
                  size="large"
                >
                  <el-icon><RefreshLeft /></el-icon>
                  重置
                </el-button>
              </div>
            </el-form>
          </el-card>
        </el-col>

        <!-- 右侧：安全设置和其他信息 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8">
          <!-- 安全设置 -->
          <el-card class="security-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Lock /></el-icon>
                  <span>安全设置</span>
                </div>
              </div>
            </template>

            <div class="security-item" @click="showPasswordDialog = true">
              <div class="security-info">
                <el-icon class="security-icon"><Key /></el-icon>
                <div class="security-text">
                  <div class="security-title">登录密码</div>
                  <div class="security-desc">定期修改密码可以提高账户安全性</div>
                </div>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </el-card>

          <!-- 账户信息 -->
          <el-card class="account-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Document /></el-icon>
                  <span>账户信息</span>
                </div>
              </div>
            </template>

            <div class="account-info">
              <div class="info-row">
                <span class="info-label">账户状态</span>
                <el-tag :type="userStore.userInfo?.isActive === 1 ? 'success' : 'danger'" effect="plain">
                  {{ userStore.userInfo?.isActive === 1 ? '正常' : '已禁用' }}
                </el-tag>
              </div>
              <div class="info-row">
                <span class="info-label">最后更新</span>
                <span class="info-value">{{ formatDate(userStore.userInfo?.updatedAt) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog 
      v-model="showPasswordDialog" 
      title="修改密码" 
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="passwordFormRef" 
        :model="passwordForm" 
        :rules="passwordRules"
        label-position="top"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  User, UserFilled, Message, Phone, Lock, Key, 
  Check, RefreshLeft, ArrowRight, InfoFilled, 
  Document, Avatar, OfficeBuilding, Setting, Briefcase 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateProfile, changePassword } from '@/api/user'
import type { UserInfo } from '@/types'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const showPasswordDialog = ref(false)
const changingPassword = ref(false)

const form = reactive({
  id: 0,
  username: '',
  realName: '',
  email: '',
  mobile: '',
  departmentName: ''
})

const originalForm = reactive({
  realName: '',
  email: '',
  mobile: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 角色名称映射
const roleNameMap: Record<string, string> = {
  'ADMIN': '系统管理员',
  'BOSS': '领导层',
  'PROVINCE': '省级员工',
  'CITY': '市级员工',
  'COUNTY': '县级员工'
}

const roleName = computed(() => {
  return roleNameMap[userStore.role] || userStore.role || '未知角色'
})

// 职位名称映射
const primaryRoleName = computed(() => {
  const roleMap: Record<string, string> = {
    'DEPT_MANAGER': '部门经理',
    'NETWORK_ENGINEER': '网络工程师',
    'LEGAL_REVIEWER': '法务审查员',
    'FINANCE_AUDITOR': '财务审核员',
    'DICT_PM': '政企项目经理',
    'CUSTOMER_SERVICE_LEAD': '客服主管',
    'FACILITY_COORDINATOR': '设施协调员',
    'INITIATOR': '合同发起人'
  }
  const primaryRole = userStore.userInfo?.primaryRole
  return primaryRole ? (roleMap[primaryRole] || primaryRole) : '未分配职位'
})

const roleTagType = computed(() => {
  const typeMap: Record<string, string> = {
    'ADMIN': 'danger',
    'BOSS': 'warning',
    'PROVINCE': 'success',
    'CITY': 'primary',
    'COUNTY': 'info'
  }
  return typeMap[userStore.role] || 'info'
})

// 头像文字
const avatarText = computed(() => {
  const name = form.realName || form.username || 'U'
  return name.substring(0, 2).toUpperCase()
})

// 是否有修改
const hasChanges = computed(() => {
  return form.realName !== originalForm.realName ||
         form.email !== originalForm.email ||
         form.mobile !== originalForm.mobile
})

// 格式化日期
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 表单验证规则
const rules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { 
      validator: (_rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
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
    form.mobile = userInfo.mobile || ''
    // 优先使用 departmentName，如果没有再尝试从 dept 对象获取
    form.departmentName = userInfo.departmentName || userInfo.dept?.name || userInfo.dept?.deptName || ''
    
    // 保存原始值
    originalForm.realName = form.realName
    originalForm.email = form.email
    originalForm.mobile = form.mobile
    
    // 同步更新 store（保留已有的 departmentName 和 primaryRole，避免被覆盖）
    userStore.setUserInfo({
      ...userStore.userInfo!,
      ...userInfo,
      // 确保 departmentName 和 primaryRole 不被覆盖为空
      departmentName: userInfo.departmentName || userStore.userInfo?.departmentName || form.departmentName,
      primaryRole: userInfo.primaryRole || userStore.userInfo?.primaryRole
    })
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
      await updateProfile({
        realName: form.realName,
        email: form.email,
        mobile: form.mobile
      })
      
      // 更新 store 中的用户信息
      if (userStore.userInfo) {
        userStore.setUserInfo({
          ...userStore.userInfo,
          realName: form.realName,
          email: form.email,
          mobile: form.mobile
        })
      }
      
      // 更新原始值
      originalForm.realName = form.realName
      originalForm.email = form.email
      originalForm.mobile = form.mobile
      
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
  form.mobile = originalForm.mobile
  formRef.value?.clearValidate()
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    changingPassword.value = true
    try {
      await changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      
      ElMessage.success('密码修改成功')
      showPasswordDialog.value = false
      
      // 重置表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordFormRef.value?.resetFields()
    } catch (error) {
      const err = error as { message?: string }
      ElMessage.error(err.message || '密码修改失败')
    } finally {
      changingPassword.value = false
    }
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

/* 顶部英雄区 */
.user-hero {
  position: relative;
  padding: 40px 32px 32px;
  margin-bottom: 24px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.95;
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.avatar-text {
  font-size: 36px;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-meta {
  color: white;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-role {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dept-name {
  font-size: 14px;
  opacity: 0.9;
}

.hero-stats {
  display: flex;
  gap: 48px;
}

.stat-item {
  text-align: center;
  color: white;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.8;
}

/* 主内容区 */
.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 24px;
}

/* 卡片样式 */
.info-card,
.security-card,
.account-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 20px;
}

.info-card :deep(.el-card__header),
.security-card :deep(.el-card__header),
.account-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.header-icon {
  font-size: 20px;
  color: #667eea;
}

/* 表单样式 */
.profile-form {
  padding: 8px 0;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.info-icon {
  color: #909399;
  cursor: help;
}

.form-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}

.form-actions .el-button {
  min-width: 120px;
}

/* 安全设置 */
.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.security-item:hover {
  background: #f5f7fa;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.security-icon {
  font-size: 24px;
  color: #667eea;
}

.security-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.security-desc {
  font-size: 12px;
  color: #909399;
}

.arrow-icon {
  color: #c0c4cc;
  font-size: 16px;
}

/* 账户信息 */
.account-info {
  padding: 8px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }
  
  .avatar-section {
    flex-direction: column;
  }
  
  .hero-stats {
    gap: 32px;
  }
  
  .user-hero {
    padding: 32px 20px;
  }
  
  .profile-content {
    padding: 0 16px 16px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
}
</style>
