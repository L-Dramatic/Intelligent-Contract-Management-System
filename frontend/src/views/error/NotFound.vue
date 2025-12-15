<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="not-found-container">
    <div class="content">
      <div class="error-code">404</div>
      <div class="error-title">页面不存在</div>
      <div class="error-desc">抱歉，您访问的页面不存在或已被移除</div>
      <div class="actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
    
    <div class="decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script lang="ts">
import { HomeFilled, ArrowLeft } from '@element-plus/icons-vue'
export default {
  components: { HomeFilled, ArrowLeft }
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.content {
  text-align: center;
  z-index: 1;
}

.error-code {
  font-size: 160px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.error-title {
  font-size: 32px;
  color: #fff;
  margin: 20px 0 10px;
}

.error-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 40px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation: float 20s infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  animation: float 15s infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 40%;
  left: 10%;
  animation: float 10s infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  25% { transform: translate(10px, -20px); }
  50% { transform: translate(-10px, 20px); }
  75% { transform: translate(20px, 10px); }
}
</style>

