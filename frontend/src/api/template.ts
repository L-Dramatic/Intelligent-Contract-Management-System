import request from '@/utils/request'

// 合同模板相关接口

export interface ContractTemplate {
  id: number
  type: string           // TYPE_A, TYPE_B, TYPE_C
  subTypeCode: string    // A1, A2, B1...
  name: string
  description: string
  content: string        // 模板内容（Markdown格式，含{{变量}}占位符）
  fieldsSchema?: Record<string, any>   // 可编辑字段定义
  lockedSections?: Record<string, any> // 锁定区域
  isDefault: number
  isActive: number
  version: string
  createdAt?: string
  updatedAt?: string
}

// 获取默认模板（选完类型后调用）
export function getDefaultTemplate(subTypeCode: string) {
  return request<ContractTemplate>({
    url: `/template/default/${subTypeCode}`,
    method: 'get'
  })
}

// 获取模板详情
export function getTemplateById(id: number) {
  return request<ContractTemplate>({
    url: `/template/${id}`,
    method: 'get'
  })
}

// 获取所有模板
export function getAllTemplates() {
  return request<ContractTemplate[]>({
    url: '/template/list',
    method: 'get'
  })
}

// 按主类型获取模板
export function getTemplatesByType(type: string) {
  return request<ContractTemplate[]>({
    url: `/template/list/type/${type}`,
    method: 'get'
  })
}

// 按子类型获取模板
export function getTemplatesBySubType(subTypeCode: string) {
  return request<ContractTemplate[]>({
    url: `/template/list/subtype/${subTypeCode}`,
    method: 'get'
  })
}
