import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 根据后端Result结构处理
    if (res.code === 200) {
      return res
    } else {
      // 非关键接口静默处理
      const silentUrls = ['/user/list', '/dept/tree', '/workflow/scenario/scenarios']
      const isSilent = silentUrls.some(url => response.config.url?.includes(url))
      if (!isSilent) {
        ElMessage.error(res.msg || '请求失败')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  (error) => {
    // 非关键接口静默处理
    const silentUrls = [
      '/user/list',
      '/dept/tree',
      '/workflow/scenario/scenarios',
      '/history' // 审批历史加载失败不弹窗
    ]
    const isSilent = silentUrls.some(url => error.config?.url?.includes(url))

    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (status === 403) {
        if (!isSilent) ElMessage.error('没有权限访问')
      } else if (status === 500) {
        if (!isSilent) ElMessage.error('服务器错误')
      } else {
        if (!isSilent) ElMessage.error(error.response.data?.msg || '请求失败')
      }
    } else {
      if (!isSilent) ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request

