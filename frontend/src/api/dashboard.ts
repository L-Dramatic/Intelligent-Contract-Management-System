import request from '@/utils/request'
import type { DashboardStats, Contract, ApprovalTask } from '@/types'

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request<DashboardStats>({
    url: '/dashboard/stats',
    method: 'get'
  })
}

// 获取最近合同
export function getRecentContracts(limit?: number) {
  return request<Contract[]>({
    url: '/dashboard/recent-contracts',
    method: 'get',
    params: { limit }
  })
}

// 获取我的待办任务
export function getMyTasks(limit?: number) {
  return request<ApprovalTask[]>({
    url: '/dashboard/my-tasks',
    method: 'get',
    params: { limit }
  })
}

// 获取合同趋势数据
export function getContractTrend(days?: number) {
  return request<{ date: string; count: number }[]>({
    url: '/dashboard/contract-trend',
    method: 'get',
    params: { days }
  })
}

// 获取审批效率统计
export function getApprovalEfficiency() {
  return request<{
    avgApprovalTime: number
    completedToday: number
    pendingCount: number
  }>({
    url: '/dashboard/approval-efficiency',
    method: 'get'
  })
}

