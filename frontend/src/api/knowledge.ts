import request from '@/utils/request'

/**
 * 获取知识库统计信息
 */
export function getKnowledgeStats() {
  return request({
    url: '/knowledge/stats',
    method: 'get'
  })
}

/**
 * 获取知识库文件列表
 */
export function getKnowledgeFiles() {
  return request({
    url: '/knowledge/files',
    method: 'get'
  })
}

/**
 * 搜索知识库（RAG检索）
 */
export function searchKnowledge(query: string, limit: number = 5) {
  return request({
    url: '/knowledge/search',
    method: 'get',
    params: { query, limit }
  })
}

/**
 * 重建知识库向量索引
 */
export function rebuildIndex() {
  return request({
    url: '/knowledge/rebuild-index',
    method: 'post'
  })
}
