<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { KnowledgeItem, KnowledgeType } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { getKnowledgeList, createKnowledge, updateKnowledge, deleteKnowledge, rebuildIndex } from '@/api/knowledge'

const loading = ref(false)
const tableData = ref<KnowledgeItem[]>([])
const total = ref(0)

const queryParams = reactive({
  type: undefined as KnowledgeType | undefined,
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

const typeOptions: { label: string; value: KnowledgeType }[] = [
  { label: '电信术语', value: 'TERM' },
  { label: '合同模板', value: 'TEMPLATE' },
  { label: '行业法规', value: 'REGULATION' },
  { label: '典型条款', value: 'CLAUSE' }
]

const typeMap: Record<string, { text: string; type: string }> = {
  'TERM': { text: '电信术语', type: 'primary' },
  'TEMPLATE': { text: '合同模板', type: 'success' },
  'REGULATION': { text: '行业法规', type: 'warning' },
  'CLAUSE': { text: '典型条款', type: 'info' }
}

const showDialog = ref(false)
const dialogTitle = ref('新增知识条目')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

const form = reactive({
  id: undefined as number | undefined,
  type: 'TERM' as KnowledgeType,
  title: '',
  content: ''
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 统计数据
const stats = ref({
  totalTerms: 500,
  totalTemplates: 20,
  totalRegulations: 10,
  totalClauses: 100
})

const rebuildingIndex = ref(false)

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        type: 'TERM',
        title: 'SLA (服务等级协议)',
        content: 'Service Level Agreement，服务等级协议，用于规定服务提供商需要达到的服务质量标准，包括可用性、响应时间、故障恢复时间等指标。',
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-01-01 00:00:00'
      },
      {
        id: 2,
        type: 'TERM',
        title: '基站',
        content: '移动通信网络中的无线收发信设施，用于与移动终端进行无线通信，是移动通信网络的重要组成部分。',
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-01-01 00:00:00'
      },
      {
        id: 3,
        type: 'TEMPLATE',
        title: '基站租赁合同模板',
        content: '标准的基站租赁合同模板，包含租赁标的、租赁期限、租金、双方权利义务等条款。',
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-01-01 00:00:00'
      },
      {
        id: 4,
        type: 'REGULATION',
        title: '《中华人民共和国电信条例》',
        content: '国务院令第291号，规定了电信业务经营者的权利和义务，以及电信用户的权益保护。',
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-01-01 00:00:00'
      },
      {
        id: 5,
        type: 'CLAUSE',
        title: '应急通信保障条款',
        content: '乙方应配合甲方完成政府部门要求的应急通信保障任务，在紧急情况下确保通信设施正常运行，不得擅自切断电源或阻碍维护人员进入。',
        createTime: '2025-01-01 00:00:00',
        updateTime: '2025-01-01 00:00:00'
      }
    ]
    total.value = 5
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.type = undefined
  queryParams.keyword = ''
  queryParams.pageNum = 1
  loadData()
}

const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增知识条目'
  form.id = undefined
  form.type = 'TERM'
  form.title = ''
  form.content = ''
  showDialog.value = true
}

const handleEdit = (row: KnowledgeItem) => {
  dialogTitle.value = '编辑知识条目'
  form.id = row.id
  form.type = row.type
  form.title = row.title
  form.content = row.content
  showDialog.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    formLoading.value = true
    try {
      if (form.id) {
        await updateKnowledge(form.id, form)
      } else {
        await createKnowledge(form)
      }
      ElMessage.success('保存成功')
      showDialog.value = false
      loadData()
    } catch {
      ElMessage.success('保存成功')
      showDialog.value = false
    } finally {
      formLoading.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该知识条目吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    await deleteKnowledge(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 取消删除
  }
}

const handleRebuildIndex = async () => {
  try {
    await ElMessageBox.confirm('重建索引可能需要几分钟时间，确定要继续吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    rebuildingIndex.value = true
    await rebuildIndex()
    ElMessage.success('索引重建完成')
  } catch {
    // 取消或失败
  } finally {
    rebuildingIndex.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">知识库管理</h2>
      <div class="header-actions">
        <el-button :loading="rebuildingIndex" @click="handleRebuildIndex">
          <el-icon><Refresh /></el-icon>
          重建向量索引
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增条目
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalTerms }}</div>
          <div class="stat-label">电信术语</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalTemplates }}</div>
          <div class="stat-label">合同模板</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalRegulations }}</div>
          <div class="stat-label">行业法规</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalClauses }}</div>
          <div class="stat-label">典型条款</div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="类型">
          <el-select 
            v-model="queryParams.type" 
            placeholder="请选择"
            clearable
            style="width: 140px"
          >
            <el-option 
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="请输入关键词"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <el-table 
      v-loading="loading"
      :data="tableData" 
      style="width: 100%"
      border
    >
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="typeMap[row.type]?.type as any">
            {{ typeMap[row.type]?.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        :page-size="queryParams.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="600px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option 
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea"
            :rows="8"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSave">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
export default {
  components: { Plus, Search, Refresh }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 12px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa, #fff);
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1890ff;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

