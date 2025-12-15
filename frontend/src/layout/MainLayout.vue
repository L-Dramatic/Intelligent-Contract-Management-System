<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  Document,
  Checked,
  Setting,
  Tools,
  Fold,
  Expand,
  ArrowDown,
  User,
  SwitchButton,
  Bell,
  FullScreen,
  Monitor,
  DataLine
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)

const menuList = computed(() => {
  const menus = [
    {
      path: '/dashboard',
      title: '工作台',
      icon: 'HomeFilled'
    },
    {
      path: '/contract',
      title: '合同管理',
      icon: 'Document',
      children: [
        { path: '/contract/list', title: '合同列表' },
        { path: '/contract/create', title: '创建合同' },
        { path: '/contract/my', title: '我的合同' }
      ]
    },
    {
      path: '/workflow',
      title: '审批中心',
      icon: 'Checked',
      children: [
        { path: '/workflow/pending', title: '待办任务' },
        { path: '/workflow/completed', title: '已办任务' },
        { path: '/workflow/initiated', title: '我发起的' }
      ]
    }
  ]

  // 流程管理员可见
  if (userStore.hasRole('WORKFLOW_ADMIN') || userStore.hasRole('ADMIN')) {
    menus.push({
      path: '/workflow-admin',
      title: '流程管理',
      icon: 'Setting',
      children: [
        { path: '/workflow-admin/definition', title: '流程模板' }
      ]
    })
  }

  // 系统管理员可见
  if (userStore.hasRole('ADMIN')) {
    menus.push({
      path: '/system',
      title: '系统管理',
      icon: 'Tools',
      children: [
        { path: '/system/user', title: '用户管理' },
        { path: '/system/department', title: '组织架构' },
        { path: '/system/knowledge', title: '知识库管理' }
      ]
    })
  }

  return menus
})

const activeMenu = computed(() => route.path)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  } catch {
    // 取消退出
  }
}

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<template>
  <el-container class="layout-container">
    <!-- 侧边栏 (深色商务风) -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="20" color="#fff"><DataLine /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">中国电信智慧合同</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.85)"
        active-text-color="#ffffff"
        unique-opened
        router
        class="custom-menu"
      >
        <template v-for="menu in menuList" :key="menu.path">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="menu.children" :index="menu.path">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item 
              v-for="child in menu.children" 
              :key="child.path"
              :index="child.path"
            >
              {{ child.title }}
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 无子菜单 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <template #title>
              <span>{{ menu.title }}</span>
            </template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <div class="collapse-btn" @click="toggleSidebar">
             <el-icon :size="20">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in route.matched.slice(1)" :key="item.path">
              {{ item.meta?.title || item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 顶部操作图标 -->
          <div class="action-item">
            <el-tooltip content="全屏" placement="bottom">
              <el-icon :size="18"><FullScreen /></el-icon>
            </el-tooltip>
          </div>
          <div class="action-item">
             <el-tooltip content="消息通知" placement="bottom">
              <el-badge is-dot class="badge">
                <el-icon :size="18"><Bell /></el-icon>
              </el-badge>
             </el-tooltip>
          </div>

          <!-- 用户头像下拉 -->
          <el-dropdown trigger="click" @command="handleLogout">
            <div class="user-info">
              <el-avatar :size="28" class="avatar" style="background-color: #1890ff">
                {{ userStore.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.username }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item disabled>
                  <el-icon><User /></el-icon>
                  角色：{{ userStore.role }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main-content">
        <!-- 页面切换动画 -->
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}

/* 侧边栏 */
.sidebar {
  background-color: #001529;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  transition: width 0.3s;
  overflow-x: hidden;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 64px;
  line-height: 64px;
  background: #002140;
  display: flex;
  align-items: center;
  padding-left: 20px;
  overflow: hidden;
  transition: all 0.3s;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: #1890ff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  font-family: 'PingFang SC', sans-serif;
  letter-spacing: 1px;
}

/* 菜单样式重写 */
.custom-menu {
  border-right: none;
  flex: 1;
}

/* 默认文字颜色调亮 */
:deep(.el-menu-item), :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.85) !important;
}

:deep(.el-menu-item i), :deep(.el-sub-menu__title i) {
  color: rgba(255, 255, 255, 0.85) !important;
}

/* 选中项高亮背景 + 左侧亮条 */
:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #fff !important;
}

:deep(.el-sub-menu__title:hover),
:deep(.el-menu-item:hover) {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.08) !important;
}

/* 顶栏 */
.header {
  height: 64px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  display: flex;
  align-items: center;
}

.collapse-btn:hover {
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-item {
  height: 100%;
  padding: 0 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  display: flex;
  align-items: center;
}

.action-item:hover {
  background: #f6f6f6;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 12px;
  height: 64px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f6f6f6;
}

.username {
  font-size: 14px;
  color: #333;
}

/* 主内容区 */
.main-content {
  background-color: #f0f2f5; /* 经典的灰色底 */
  padding: 24px;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
