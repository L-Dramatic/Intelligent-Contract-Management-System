import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/RegisterView.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' }
      },
      // 合同管理
      {
        path: 'contract',
        name: 'Contract',
        redirect: '/contract/list',
        meta: { title: '合同管理' },
        children: [
          {
            path: 'list',
            name: 'ContractList',
            component: () => import('@/views/contract/ContractList.vue'),
            meta: { title: '合同列表' }
          },
          {
            path: 'create',
            name: 'ContractCreate',
            component: () => import('@/views/contract/ContractCreate.vue'),
            meta: { title: '创建合同' }
          },
          {
            path: 'my',
            name: 'MyContracts',
            component: () => import('@/views/contract/MyContracts.vue'),
            meta: { title: '我的合同' }
          },
          {
            path: 'detail/:id',
            name: 'ContractDetail',
            component: () => import('@/views/contract/ContractDetail.vue'),
            meta: { title: '合同详情' }
          },
          {
            path: 'edit/:id',
            name: 'ContractEdit',
            component: () => import('@/views/contract/ContractEdit.vue'),
            meta: { title: '编辑合同' }
          },
          {
            path: 'ai-generate',
            name: 'AiGenerate',
            component: () => import('@/views/contract/AiGenerate.vue'),
            meta: { title: 'AI智能生成' }
          }
        ]
      },
      // 审批中心
      {
        path: 'workflow',
        name: 'Workflow',
        redirect: '/workflow/pending',
        meta: { title: '审批中心' },
        children: [
          {
            path: 'pending',
            name: 'PendingTasks',
            component: () => import('@/views/workflow/PendingTasks.vue'),
            meta: { title: '待办任务' }
          },
          {
            path: 'completed',
            name: 'CompletedTasks',
            component: () => import('@/views/workflow/CompletedTasks.vue'),
            meta: { title: '已办任务' }
          },
          {
            path: 'initiated',
            name: 'InitiatedTasks',
            component: () => import('@/views/workflow/InitiatedTasks.vue'),
            meta: { title: '我发起的' }
          },
          {
            path: 'approve/:id',
            name: 'ApproveTask',
            component: () => import('@/views/workflow/ApproveTask.vue'),
            meta: { title: '审批' }
          }
        ]
      },
      // 流程管理（流程管理员）
      {
        path: 'workflow-admin',
        name: 'WorkflowAdmin',
        redirect: '/workflow-admin/definition',
        meta: { title: '流程管理', roles: ['ADMIN', 'WORKFLOW_ADMIN'] },
        children: [
          {
            path: 'definition',
            name: 'WorkflowDefinition',
            component: () => import('@/views/workflow/WorkflowDefinition.vue'),
            meta: { title: '流程模板' }
          },
          {
            path: 'definition/edit/:id?',
            name: 'WorkflowEdit',
            component: () => import('@/views/workflow/WorkflowEdit.vue'),
            meta: { title: '编辑流程' }
          }
        ]
      },
      // 系统管理
      {
        path: 'system',
        name: 'System',
        redirect: '/system/user',
        meta: { title: '系统管理', roles: ['ADMIN'] },
        children: [
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/system/UserManage.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'department',
            name: 'DepartmentManage',
            component: () => import('@/views/system/DepartmentManage.vue'),
            meta: { title: '组织架构' }
          },
          {
            path: 'knowledge',
            name: 'KnowledgeManage',
            component: () => import('@/views/system/KnowledgeManage.vue'),
            meta: { title: '知识库管理' }
          }
        ]
      }
    ]
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '智慧合同系统'} - 电信智慧合同管理系统`
  
  const userStore = useUserStore()
  
  // 公开页面直接放行
  if (to.meta.public) {
    // 已登录用户访问登录页，跳转到首页
    if (userStore.isLoggedIn && (to.name === 'Login' || to.name === 'Register')) {
      next('/dashboard')
      return
    }
    next()
    return
  }
  
  // 未登录跳转登录页
  if (!userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  
  // 角色权限检查
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRole = requiredRoles.some(role => userStore.hasRole(role))
    if (!hasRole) {
      next('/dashboard')
      return
    }
  }
  
  next()
})

export default router
