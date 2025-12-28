import request from '@/utils/request'
import type { 
  WorkflowDefinition, 
  WorkflowNode, 
  WorkflowTransition,
  WorkflowInstance, 
  ApprovalTask, 
  PageResult 
} from '@/types'

// ==================== 流程定义管理 ====================

// 获取流程定义列表
export function getWorkflowList(params?: { enabled?: boolean; name?: string }) {
  return request<WorkflowDefinition[]>({
    url: '/workflow/definition/list',
    method: 'get',
    params
  })
}

// 获取流程定义详情
export function getWorkflowDetail(id: number) {
  return request<WorkflowDefinition>({
    url: `/workflow/definition/${id}`,
    method: 'get'
  })
}

// 创建流程定义
export function createWorkflow(data: Partial<WorkflowDefinition>) {
  return request<WorkflowDefinition>({
    url: '/workflow/definition',
    method: 'post',
    data
  })
}

// 更新流程定义
export function updateWorkflow(id: number, data: Partial<WorkflowDefinition>) {
  return request<WorkflowDefinition>({
    url: `/workflow/definition/${id}`,
    method: 'put',
    data
  })
}

// 删除流程定义
export function deleteWorkflow(id: number) {
  return request({
    url: `/workflow/definition/${id}`,
    method: 'delete'
  })
}

// 启用/停用流程定义
export function toggleWorkflowStatus(id: number, enabled: boolean) {
  return request({
    url: `/workflow/definition/${id}/status`,
    method: 'put',
    data: { enabled }
  })
}

// 保存流程节点
export function saveWorkflowNodes(workflowId: number, nodes: Partial<WorkflowNode>[]) {
  return request({
    url: `/workflow/definition/${workflowId}/nodes`,
    method: 'post',
    data: nodes
  })
}

// 保存流程连线
export function saveWorkflowTransitions(workflowId: number, transitions: Partial<WorkflowTransition>[]) {
  return request({
    url: `/workflow/definition/${workflowId}/transitions`,
    method: 'post',
    data: transitions
  })
}

// ==================== 流程实例管理 ====================

// 获取我发起的流程实例列表
export function getInstanceList(params: { 
  status?: string
  contractId?: number
  pageNum: number
  pageSize: number 
}) {
  return request<WorkflowInstance[]>({
    url: '/workflow/scenario/instances/my',
    method: 'get',
    params
  })
}

// 获取流程实例详情
export function getInstanceDetail(id: number) {
  return request<WorkflowInstance>({
    url: `/workflow/instance/${id}`,
    method: 'get'
  })
}

// 获取流程实例的审批历史
export function getInstanceHistory(id: number) {
  return request<ApprovalTask[]>({
    url: `/workflow/scenario/contract/${id}/history`,
    method: 'get'
  })
}

// 终止流程实例
export function terminateInstance(id: number, reason: string) {
  return request({
    url: `/workflow/scenario/instance/${id}/cancel`,
    method: 'post',
    data: { reason }
  })
}

// ==================== 审批任务管理 ====================

// 获取我的待办任务
export function getMyPendingTasks(params: { pageNum: number; pageSize: number }) {
  return request<ApprovalTask[]>({
    url: '/workflow/scenario/tasks/pending',
    method: 'get',
    params
  })
}

// 获取我的已办任务
export function getMyCompletedTasks(params: { pageNum: number; pageSize: number }) {
  return request<ApprovalTask[]>({
    url: '/workflow/scenario/tasks/completed',
    method: 'get',
    params
  })
}

// 获取任务详情
export function getTaskDetail(id: number) {
  return request<ApprovalTask>({
    url: `/workflow/scenario/tasks/${id}/detail`,
    method: 'get'
  })
}

// 审批通过
export function approveTask(id: number, opinion: string) {
  return request({
    url: `/workflow/scenario/tasks/${id}/approve`,
    method: 'post',
    data: { comment: opinion }
  })
}

// 审批驳回
export function rejectTask(id: number, opinion: string) {
  return request({
    url: `/workflow/scenario/tasks/${id}/reject`,
    method: 'post',
    data: { comment: opinion }
  })
}

// 转签
export function transferTask(id: number, targetUserId: number, reason: string) {
  return request({
    url: `/workflow/task/${id}/transfer`,
    method: 'post',
    data: { targetUserId, reason }
  })
}

// 加签
export function addSignTask(id: number, userIds: number[], reason: string) {
  return request({
    url: `/workflow/task/${id}/add-sign`,
    method: 'post',
    data: { userIds, reason }
  })
}

// 获取可转签用户列表
export function getTransferableUsers() {
  return request({
    url: '/workflow/task/transferable-users',
    method: 'get'
  })
}

