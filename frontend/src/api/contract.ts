import request from '@/utils/request'
import type { Contract, ContractQuery, PageResult, AiGenerateRequest, AiReviewResult, AiChatMessage } from '@/types'

// 获取合同列表
export function getContractList(params: ContractQuery) {
  return request<PageResult<Contract>>({
    url: '/contract/list',
    method: 'get',
    params
  })
}

// 获取合同详情
export function getContractDetail(id: number) {
  return request<Contract>({
    url: `/contract/${id}`,
    method: 'get'
  })
}

// 创建合同
export function createContract(data: Partial<Contract> & { isDraft?: boolean }) {
  return request<{ id: number }>({
    url: '/contract/create',
    method: 'post',
    data
  })
}

// 更新合同
export function updateContract(id: number, data: Partial<Contract>) {
  return request<boolean>({
    url: '/contract/update',
    method: 'put',
    data: { id, ...data }
  })
}

// 删除合同
export function deleteContract(id: number) {
  return request({
    url: `/contract/${id}`,
    method: 'delete'
  })
}

// 提交合同审批
export function submitContract(id: number) {
  return request({
    url: `/contract/${id}/submit`,
    method: 'post'
  })
}

// 撤回合同
export function withdrawContract(id: number) {
  return request({
    url: `/contract/${id}/withdraw`,
    method: 'post'
  })
}

// AI生成合同 - 对话式
export function aiGenerateContract(data: AiGenerateRequest) {
  return request({
    url: '/contract/ai/generate',
    method: 'post',
    data
  })
}

// AI生成合同 - 流式对话
export function aiChatStream(contractType: string, messages: AiChatMessage[]) {
  return fetch('/api/contract/ai/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    },
    body: JSON.stringify({ contractType, messages })
  })
}

// AI条款审查
export function aiReviewContract(id: number) {
  return request<AiReviewResult>({
    url: `/contract/${id}/ai/review`,
    method: 'post'
  })
}

// 获取合同模板列表
export function getContractTemplates(type?: string) {
  return request({
    url: '/contract/templates',
    method: 'get',
    params: { type }
  })
}

// 根据模板创建合同
export function createFromTemplate(templateId: number, data: Partial<Contract>) {
  return request<Contract>({
    url: `/contract/from-template/${templateId}`,
    method: 'post',
    data
  })
}

// 上传合同文件
export function uploadContractFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/contract/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出合同PDF
export function exportContractPdf(id: number) {
  return request({
    url: `/contract/${id}/export/pdf`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取我的合同
export function getMyContracts(params: ContractQuery) {
  return request<PageResult<Contract>>({
    url: '/contract/my',
    method: 'get',
    params
  })
}

