<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <!-- 装饰背景层 (z-index: 0) -->
      <div class="bg-decoration">
        <div class="circle c1"></div>
        <div class="circle c2"></div>
        <div class="line l1"></div>
        <div class="line l2"></div>
      </div>

      <!-- 内容层 (z-index: 2) -->
      <div class="brand-content">
        <div class="brand-header">
          <!-- 纯代码实现的 Logo -->
          <div class="logo-box">
            <el-icon :size="24" color="#0052d9"><DataLine /></el-icon>
          </div>
          <span class="brand-name">电信公司</span>
        </div>
        
        <div class="hero-text">
          <h1>智慧合同<br/>管理系统</h1>
          <p class="slogan">Intelligent Contract Management System</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon><Monitor /></el-icon>
              <span>全流程数字化</span>
            </div>
            <div class="feature-item">
              <el-icon><Cpu /></el-icon>
              <span>AI 智能审核</span>
            </div>
            <div class="feature-item">
              <el-icon><Connection /></el-icon>
              <span>多级协同审批</span>
            </div>
          </div>
        </div>

        <div class="brand-footer">
          <p>© 2025 China Telecom. All Rights Reserved.</p>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="form-section">
      <div class="form-wrapper">
        <h2 class="welcome-title">用户登录</h2>
        <p class="welcome-subtitle">请使用您的企业账号登录</p>

        <el-form 
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          size="large"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户ID或用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住账号</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <el-button 
            type="primary" 
            class="login-btn" 
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>

          <div class="form-footer">
            <span class="no-account">还没有账号？</span>
            <el-link type="primary" @click="$router.push('/register')">
              申请注册
            </el-link>
          </div>
        </el-form>


      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { User, Lock, Monitor, Cpu, Connection, DataLine } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        userStore.setToken(res.data.token)
        userStore.setUserInfo({
          ...(res.data.userInfo || {}),
          permissions: res.data.permissions || []
        })
        ElMessage.success('登录成功')
        const redirect = route.query.redirect as string
        router.push(redirect || '/dashboard')
      } catch (error: any) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #fff;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
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
  overflow: hidden;
  box-sizing: border-box; 
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
  font-size: 48px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 16px;
}

.slogan {
  font-size: 18px;
  opacity: 0.8;
  margin-bottom: 48px;
  font-weight: 300;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  opacity: 0.9;
}

.brand-footer {
  font-size: 12px;
  opacity: 0.5;
  margin-top: auto;
}

/* 装饰线条背景 */
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

.c1 { width: 600px; height: 600px; top: -100px; right: -200px; }
.c2 { width: 400px; height: 400px; bottom: -50px; left: -100px; }

/* 右侧表单区 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.form-wrapper {
  width: 400px;
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

.login-form :deep(.el-input__wrapper) {
  padding: 10px 15px;
  border-radius: 4px;
}

.login-form :deep(.el-input__inner) {
  height: 24px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 4px;
  margin-bottom: 24px;
  background-color: #0052d9; /* 腾讯/电信类蓝 */
  border-color: #0052d9;
}

.login-btn:hover {
  background-color: #003cab;
  border-color: #003cab;
}

.form-footer {
  text-align: center;
  font-size: 14px;
}

.no-account {
  color: #909399;
  margin-right: 8px;
}

.demo-tip {
  margin-top: 32px;
}
</style>
