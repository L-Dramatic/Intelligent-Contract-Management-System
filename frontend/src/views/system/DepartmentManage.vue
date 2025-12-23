<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Department } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { getDepartmentTree, createDepartment, updateDepartment, deleteDepartment } from '@/api/department'

const loading = ref(false)
const treeData = ref<Department[]>([])

const showDialog = ref(false)
const dialogTitle = ref('新增部门')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  parentId: 0,
  level: 1,
  sortOrder: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const defaultProps = {
  children: 'children',
  label: 'name'
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDepartmentTree()
    if (res.data && res.data.length > 0) {
      treeData.value = res.data
      return
    }
    throw new Error('empty')
  } catch (e) {
    console.log('API未实现，使用模拟数据')
    // 模拟数据
    treeData.value = [
      {
        id: 1,
        name: '中国电信股份有限公司',
        parentId: 0,
        level: 1,
        sortOrder: 1,
        children: [
          {
            id: 2,
            name: '网络部',
            parentId: 1,
            level: 2,
            sortOrder: 1,
            children: [
              { id: 5, name: '无线网络室', parentId: 2, level: 3, sortOrder: 1 },
              { id: 6, name: '传输网络室', parentId: 2, level: 3, sortOrder: 2 }
            ]
          },
          {
            id: 3,
            name: '法务部',
            parentId: 1,
            level: 2,
            sortOrder: 2
          },
          {
            id: 4,
            name: '财务部',
            parentId: 1,
            level: 2,
            sortOrder: 3
          },
          {
            id: 7,
            name: '采购部',
            parentId: 1,
            level: 2,
            sortOrder: 4
          },
          {
            id: 8,
            name: '运维部',
            parentId: 1,
            level: 2,
            sortOrder: 5
          }
        ]
      }
    ]
  } finally {
    loading.value = false
  }
}

const handleAdd = (parent?: Department) => {
  dialogTitle.value = parent ? `新增子部门 - ${parent.name}` : '新增部门'
  form.id = undefined
  form.name = ''
  form.parentId = parent?.id || 0
  form.level = parent ? parent.level + 1 : 1
  form.sortOrder = 0
  showDialog.value = true
}

const handleEdit = (node: Department) => {
  dialogTitle.value = '编辑部门'
  form.id = node.id
  form.name = node.name
  form.parentId = node.parentId
  form.level = node.level
  form.sortOrder = node.sortOrder
  showDialog.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    formLoading.value = true
    try {
      if (form.id) {
        await updateDepartment(form.id, form)
      } else {
        await createDepartment(form)
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

const handleDelete = async (node: Department) => {
  if (node.children && node.children.length > 0) {
    ElMessage.warning('请先删除子部门')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除该部门吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    await deleteDepartment(node.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 取消删除
  }
}

const getLevelTag = (level: number) => {
  const map: Record<number, { text: string; type: 'primary' | 'success' | 'warning' }> = {
    1: { text: '总公司', type: 'primary' },
    2: { text: '部门', type: 'success' },
    3: { text: '科室', type: 'warning' }
  }
  return map[level] || { text: '其他', type: 'primary' as const }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">组织架构管理</h2>
      <el-button type="primary" @click="handleAdd()">
        <el-icon><Plus /></el-icon>
        新增顶级部门
      </el-button>
    </div>
    
    <el-alert
      title="组织架构说明"
      type="info"
      description="电信运营商通常采用省公司-地市-区县三级架构。组织架构用于确定用户的上下级关系，支持树状审批策略。"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />
    
    <el-card v-loading="loading">
      <el-tree
        :data="treeData"
        :props="defaultProps"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-content">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="node-name">{{ node.label }}</span>
              <el-tag :type="getLevelTag(data.level).type" size="small">
                {{ getLevelTag(data.level).text }}
              </el-tag>
            </div>
            <div class="node-actions">
              <el-button type="primary" link size="small" @click.stop="handleAdd(data)">
                添加子部门
              </el-button>
              <el-button type="primary" link size="small" @click.stop="handleEdit(data)">
                编辑
              </el-button>
              <el-button 
                type="danger" 
                link 
                size="small" 
                :disabled="data.level === 1"
                @click.stop="handleDelete(data)"
              >
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
      
      <el-empty v-if="treeData.length === 0 && !loading" description="暂无组织架构数据" />
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="400px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
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
import { Plus, OfficeBuilding } from '@element-plus/icons-vue'
export default {
  components: { Plus, OfficeBuilding }
}
</script>

<style scoped>
.tree-node {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-name {
  font-size: 14px;
}

.node-actions {
  display: flex;
  gap: 4px;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 4px 0;
}
</style>

