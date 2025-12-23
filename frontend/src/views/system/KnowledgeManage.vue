<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 统计数据
const stats = ref({
  document_count: 0,
  rag_enabled: false,
  files: {
    contracts: 0,
    laws: 0,
    total: 0
  },
  collection_name: '',
  embedding_model: ''
})

// 文件列表
interface KnowledgeFile {
  name: string
  path: string
  type: string
  category: string
  size: number
  modified: string
}
const files = ref<KnowledgeFile[]>([])
const filesLoading = ref(false)

// 搜索
const searchQuery = ref('')
const searchResults = ref<any[]>([])
const searchLoading = ref(false)

// 重建索引
const rebuildLoading = ref(false)

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await request.get('/knowledge/stats')
    if (res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

// 加载文件列表
const loadFiles = async () => {
  filesLoading.value = true
  try {
    const res = await request.get('/knowledge/files')
    files.value = res.data?.files || []
  } catch (error) {
    console.error('获取文件列表失败', error)
    files.value = []
  } finally {
    filesLoading.value = false
  }
}

// 搜索知识库
const handleSearch = async () => {
  if (!searchQuery.value || searchQuery.value.trim().length < 2) {
    ElMessage.warning('请输入至少2个字符')
    return
  }
  
  searchLoading.value = true
  try {
    const res = await request.get('/knowledge/search', {
      params: { query: searchQuery.value, limit: 5 }
    })
    searchResults.value = res.data?.results || []
    if (searchResults.value.length === 0) {
      ElMessage.info('未找到相关结果')
    }
  } catch (error) {
    ElMessage.error('搜索失败，请确认AI服务已启动')
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

// 重建索引
const handleRebuildIndex = async () => {
  rebuildLoading.value = true
  ElMessage.info('开始重建索引，请稍候...')
  
  try {
    const res = await request.post('/knowledge/rebuild-index')
    if (res.data?.success) {
      ElMessage.success('索引重建完成')
      // 刷新统计
      await loadStats()
    } else {
      ElMessage.error(res.data?.error || '重建失败')
    }
  } catch (error) {
    ElMessage.error('重建失败，请确认AI服务已启动')
  } finally {
    rebuildLoading.value = false
  }
}

// 格式化文件大小
const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

// 获取类型标签样式
const getTypeTag = (type: string) => {
  return type === 'CONTRACT' ? 'primary' : 'warning'
}

onMounted(() => {
  loadStats()
  loadFiles()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">知识库监控</h2>
      <el-button 
        type="primary" 
        :loading="rebuildLoading"
        @click="handleRebuildIndex"
      >
        <el-icon><Refresh /></el-icon>
        重建向量索引
      </el-button>
    </div>
    
    <!-- 状态提示 -->
    <el-alert
      v-if="!stats.rag_enabled"
      title="RAG服务未启用"
      description="请启动AI服务（ai-service）以使用知识库搜索功能"
      type="warning"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />
    
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon contracts">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.files.contracts }}</div>
            <div class="stat-label">合同模板</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon laws">
            <el-icon :size="32"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.files.laws }}</div>
            <div class="stat-label">法律法规</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon indexed">
            <el-icon :size="32"><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.document_count }}</div>
            <div class="stat-label">已索引文档块</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon status" :class="{ active: stats.rag_enabled }">
            <el-icon :size="32"><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.rag_enabled ? '在线' : '离线' }}</div>
            <div class="stat-label">RAG服务状态</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 搜索测试区 -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span>知识库搜索测试</span>
          <el-tag size="small" type="info">RAG检索</el-tag>
        </div>
      </template>
      
      <div class="search-area">
        <el-input
          v-model="searchQuery"
          placeholder="输入问题测试知识库检索，如：基站租赁合同需要哪些条款？"
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :loading="searchLoading" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      
      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div v-for="(result, index) in searchResults" :key="index" class="result-item">
          <div class="result-header">
            <el-tag size="small">{{ result.type }}</el-tag>
            <span class="result-source">{{ result.source }}</span>
            <el-tag size="small" type="success">相关度: {{ (result.relevance * 100).toFixed(0) }}%</el-tag>
          </div>
          <div class="result-content">{{ result.content }}</div>
        </div>
      </div>
    </el-card>
    
    <!-- 文件列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>知识库文件列表</span>
          <el-tag size="small">共 {{ files.length }} 个文件</el-tag>
        </div>
      </template>
      
      <el-table :data="files" v-loading="filesLoading" stripe max-height="400">
        <el-table-column prop="name" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="100" align="right">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="modified" label="修改时间" width="180">
          <template #default="{ row }">
            {{ row.modified?.split('T')[0] }}
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-card>
    
    <!-- 说明 -->
    <el-alert
      title="知识库管理说明"
      type="info"
      :closable="false"
      style="margin-top: 20px"
    >
      <template #default>
        <p>• 知识文件存储在 <code>ai-service/knowledge_base/</code> 目录</p>
        <p>• 添加新知识：将 .md 文件放入对应目录，然后点击「重建向量索引」</p>
        <p>• 搜索功能依赖 AI 服务，请确保 ai-service 已启动（端口 8765）</p>
      </template>
    </el-alert>
  </div>
</template>

<script lang="ts">
import { Document, Reading, Collection, Connection, Refresh, Search } from '@element-plus/icons-vue'
export default {
  components: { Document, Reading, Collection, Connection, Refresh, Search }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 8px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon.contracts {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.stat-icon.laws {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
  color: white;
}

.stat-icon.indexed {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: white;
}

.stat-icon.status {
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  color: white;
}

.stat-icon.status.active {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 16px;
}

.search-results {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.result-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.result-source {
  color: #606266;
  font-size: 13px;
  flex: 1;
}

.result-content {
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}
</style>
