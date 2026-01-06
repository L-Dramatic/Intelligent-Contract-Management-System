import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const role = computed(() => userInfo.value?.role || '')
  const permissions = computed(() => userInfo.value?.permissions || [])
  
  // 判断是否为县级用户（直接根据 role 判断）
  const isCountyUser = computed(() => role.value === 'COUNTY')
  
  // 判断是否为市级用户
  const isCityUser = computed(() => role.value === 'CITY')
  
  // 判断是否为省级用户
  const isProvinceUser = computed(() => role.value === 'PROVINCE')
  
  // 判断是否有审批权限（市级及以上才有审批权限）
  const canApprove = computed(() => ['CITY', 'PROVINCE', 'BOSS'].includes(role.value))
  
  // 判断是否可以起草合同（只有县级可以起草）
  const canDraft = computed(() => role.value === 'COUNTY')

  // 方法
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function loadUserInfo() {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
      } catch {
        userInfo.value = null
      }
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function hasPermission(permission: string): boolean {
    return permissions.value.includes(permission)
  }

  function hasRole(roleName: string): boolean {
    return role.value === roleName
  }

  // 初始化时加载用户信息
  loadUserInfo()

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    role,
    permissions,
    isCountyUser,
    isCityUser,
    isProvinceUser,
    canApprove,
    canDraft,
    setToken,
    setUserInfo,
    logout,
    hasPermission,
    hasRole,
    loadUserInfo
  }
})

