import request from '@/utils/request'
import type { KnowledgeItem, KnowledgeType, PageResult } from '@/types'

// 获取知识库列表
export function getKnowledgeList(params: {
  type?: KnowledgeType
  keyword?: string
  pageNum: number
  pageSize: number
}) {
  return request<PageResult<KnowledgeItem>>({
    url: '/knowledge/list',
    method: 'get',
    params
  })
}

// 获取知识详情
export function getKnowledgeDetail(id: number) {
  return request<KnowledgeItem>({
    url: `/knowledge/${id}`,
    method: 'get'
  })
}

// 添加知识条目
export function createKnowledge(data: Partial<KnowledgeItem>) {
  return request<KnowledgeItem>({
    url: '/knowledge',
    method: 'post',
    data
  })
}

// 更新知识条目
export function updateKnowledge(id: number, data: Partial<KnowledgeItem>) {
  return request<KnowledgeItem>({
    url: `/knowledge/${id}`,
    method: 'put',
    data
  })
}

// 删除知识条目
export function deleteKnowledge(id: number) {
  return request({
    url: `/knowledge/${id}`,
    method: 'delete'
  })
}

// 批量导入知识条目
export function importKnowledge(file: File, type: KnowledgeType) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  return request({
    url: '/knowledge/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 重建向量索引
export function rebuildIndex() {
  return request({
    url: '/knowledge/rebuild-index',
    method: 'post'
  })
}

// 搜索知识库（RAG检索）
export function searchKnowledge(query: string, type?: KnowledgeType, limit?: number) {
  return request<KnowledgeItem[]>({
    url: '/knowledge/search',
    method: 'get',
    params: { query, type, limit }
  })
}

// 获取知识库统计
export function getKnowledgeStats() {
  return request<{
    totalTerms: number
    totalTemplates: number
    totalRegulations: number
    totalClauses: number
    lastUpdateTime: string
  }>({
    url: '/knowledge/stats',
    method: 'get'
  })
}

