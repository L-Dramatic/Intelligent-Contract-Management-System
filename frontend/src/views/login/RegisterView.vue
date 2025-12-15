<template>
  <div class="register-container">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <!-- 装饰背景 -->
      <div class="bg-decoration">
        <div class="circle c1"></div>
        <div class="circle c2"></div>
      </div>

      <!-- 内容层 -->
      <div class="brand-content">
        <div class="brand-header">
          <!-- 纯代码 Logo -->
          <div class="logo-box">
            <el-icon :size="24" color="#0052d9"><DataLine /></el-icon>
          </div>
          <span class="brand-name">中国电信</span>
        </div>
        
        <div class="hero-text">
          <h1>加入我们</h1>
          <p class="slogan">Join Intelligent Contract System</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon><Postcard /></el-icon>
              <span>实名认证体系</span>
            </div>
            <div class="feature-item">
              <el-icon><Connection /></el-icon>
              <span>部门协同办公</span>
            </div>
          </div>
        </div>

        <div class="brand-footer">
          <p>© 2025 China Telecom. All Rights Reserved.</p>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单区 -->
    <div class="form-section">
      <div class="form-wrapper">
        <h2 class="welcome-title">账号注册</h2>
        <p class="welcome-subtitle">创建您的企业内部账号</p>

        <el-form 
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          class="register-form"
          size="large"
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请设置用户名 (3-20位)" 
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请设置登录密码" 
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码" 
              show-password
              :prefix-icon="Check"
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="realName">
                <el-input 
                  v-model="registerForm.realName" 
                  placeholder="请输入姓名" 
                  :prefix-icon="Postcard"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="申请角色" prop="role">
                <el-select v-model="registerForm.role" placeholder="选择角色">
                  <el-option label="普通用户" value="USER" />
                  <el-option label="部门经理" value="MANAGER" />
                  <el-option label="法务人员" value="LEGAL" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-button 
            type="primary" 
            class="register-btn" 
            :loading="loading"
            @click="handleRegister"
          >
            立即注册
          </el-button>

          <div class="form-footer">
            <span class="has-account">已有账号？</span>
            <el-link type="primary" @click="$router.push('/login')">
              返回登录
            </el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'
import { User, Lock, Check, Postcard, Connection, DataLine } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  role: 'USER'
})

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
})

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: registerForm.username,
          password: registerForm.password,
          realName: registerForm.realName,
          role: registerForm.role
        })
        ElMessage.success('注册成功，请等待管理员审核或直接登录')
        router.push('/login')
      } catch (error: any) {
        // error handled by interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #fff;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 左侧品牌区 */
.brand-section {
  flex: 0 0 40%;
  background: linear-gradient(135deg, #0052d9 0%, #003cab 100%);
  color: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 60px 80px;
  box-sizing: border-box;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 纯代码 Logo 样式 */
.logo-box {
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.brand-name {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 1px;
}

.hero-text {
  margin-top: auto;
  margin-bottom: auto;
}

.hero-text h1 {
  font-size: 40px;
  font-weight: 700;
  margin-bottom: 16px;
}

.slogan {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 40px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  opacity: 0.9;
}

.brand-footer {
  font-size: 12px;
  opacity: 0.5;
  margin-top: auto;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  opacity: 0.1;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid #fff;
}
.c1 { width: 500px; height: 500px; top: -150px; right: -150px; }
.c2 { width: 300px; height: 300px; bottom: 50px; left: -100px; }

/* 右侧表单区 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.form-wrapper {
  width: 460px;
  padding: 40px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.welcome-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 600;
}

.welcome-subtitle {
  color: #909399;
  font-size: 14px;
  margin-bottom: 32px;
}

.register-form :deep(.el-input__wrapper) {
  padding: 8px 15px;
  border-radius: 4px;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 4px;
  margin-top: 16px;
  margin-bottom: 24px;
  background-color: #0052d9;
  border-color: #0052d9;
}

.register-btn:hover {
  background-color: #003cab;
  border-color: #003cab;
}

.form-footer {
  text-align: center;
  font-size: 14px;
}

.has-account {
  color: #909399;
  margin-right: 8px;
}
</style>
