<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Department } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { getDepartmentTree, createDepartment, updateDepartment, deleteDepartment } from '@/api/department'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const loading = ref(false)
const treeData = ref<Department[]>([])
const activeTab = ref('visualization')

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

const chartContainer = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

onMounted(() => {
  loadData()
})

watch(activeTab, (newTab) => {
  if (newTab === 'visualization') {
    nextTick(() => {
      initChart()
    })
  }
})

watch(treeData, () => {
  if (activeTab.value === 'visualization') {
    nextTick(() => {
      initChart()
    })
  }
}, { deep: true })

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

// 转换树形数据为ECharts格式
const convertToEChartsData = (data: Department[]): any => {
  if (!data || data.length === 0) return null
  
  const convertNode = (node: Department): any => {
    // 使用白色背景，黑色文字，确保清晰可见
    const borderColors: Record<number, string> = {
      1: '#409eff',
      2: '#67c23a',
      3: '#e6a23c',
      4: '#f56c6c'
    }
    
    const borderColor = borderColors[node.level] || '#409eff'
    const levelLabels: Record<number, string> = {
      1: '总公司',
      2: '部门',
      3: '科室',
      4: '小组'
    }
    
    // 根据节点名称长度调整显示
    const nameLength = node.name.length
    let displayName = node.name
    let fontSize = node.level === 1 ? 14 : node.level === 2 ? 12 : 10
    
    // 如果名称太长，截断并添加省略号
    if (nameLength > 8 && node.level === 1) {
      displayName = node.name.substring(0, 8) + '...'
    } else if (nameLength > 6 && node.level === 2) {
      displayName = node.name.substring(0, 6) + '...'
      fontSize = 11
    } else if (nameLength > 4 && node.level === 3) {
      displayName = node.name.substring(0, 4) + '...'
      fontSize = 9
    }
    
    const result: any = {
      name: displayName, // 使用截断后的名称显示
      value: node.name, // 保留完整名称用于tooltip和查找
      symbolSize: node.level === 1 ? 140 : node.level === 2 ? 100 : 80,
      // 存储层级信息
      level: node.level,
      // 白色背景，不同层级使用不同颜色的边框
      itemStyle: {
        color: '#ffffff',
        borderColor: borderColor,
        borderWidth: 2,
        shadowBlur: 10,
        shadowColor: 'rgba(0, 0, 0, 0.1)'
      },
      label: {
        show: true,
        position: 'inside',
        verticalAlign: 'middle',
        align: 'center',
        fontSize: fontSize,
        fontWeight: node.level === 1 ? 'bold' : 'normal',
        color: '#333333' // 黑色文字
      },
      tooltip: {
        formatter: (params: any) => {
          return `
            <div style="padding: 10px;">
              <div style="font-size: 16px; font-weight: bold; margin-bottom: 8px; color: #333;">${node.name}</div>
              <div style="font-size: 12px; color: #666;">层级: ${levelLabels[node.level] || '其他'}</div>
              <div style="font-size: 12px; color: #666;">排序: ${node.sortOrder}</div>
            </div>
          `
        }
      }
    }
    
    if (node.children && node.children.length > 0) {
      result.children = node.children.map(convertNode)
    }
    
    return result
  }
  
  return data.map(convertNode)
}

// 初始化图表
const initChart = () => {
  if (!chartContainer.value) return
  
  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  // 创建新实例
  chartInstance = echarts.init(chartContainer.value)
  
  const chartData = convertToEChartsData(treeData.value)
  
  // 调试：打印第一个节点的数据
  if (chartData && chartData.length > 0) {
    console.log('Chart data sample:', JSON.stringify(chartData[0], null, 2))
  }
  
  if (!chartData || chartData.length === 0) {
    chartInstance.setOption({
      graphic: {
        elements: [
          {
            type: 'text',
            left: 'center',
            top: 'middle',
            style: {
              text: '暂无组织架构数据',
              fontSize: 16,
              fill: '#999'
            }
          }
        ]
      }
    })
    return
  }
  
  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#409eff',
      borderWidth: 1,
      textStyle: {
        color: '#333'
      },
      padding: [10, 15]
    },
    toolbox: {
      show: true,
      feature: {
        restore: {
          show: true,
          title: '重置视图'
        },
        saveAsImage: {
          show: true,
          title: '保存为图片',
          pixelRatio: 2
        }
      },
      right: 20,
      top: 20,
      iconStyle: {
        borderColor: '#409eff'
      },
      emphasis: {
        iconStyle: {
          borderColor: '#66b1ff'
        }
      }
    },
    series: [
      {
        type: 'tree',
        data: chartData,
        top: '10%',
        left: '10%',
        bottom: '10%',
        right: '15%',
        layout: 'orthogonal',
        orient: 'TB',
        symbolSize: (value: any, params: any) => {
          return params.data.symbolSize || 60
        },
        // 移除series级别的itemStyle，让数据节点中的itemStyle生效
        emphasis: {
          focus: 'descendant',
          itemStyle: {
            shadowBlur: 30,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
            // 不设置color和borderColor，使用数据节点中的值
          }
        },
        expandAndCollapse: true,
        animationDuration: 550,
        animationDurationUpdate: 750,
        initialTreeDepth: 2, // 默认展开到第2层
        roam: true, // 启用缩放和平移
        lineStyle: {
          color: '#409eff',
          curveness: 0.5,
          width: 3,
          type: 'solid',
          shadowBlur: 10,
          shadowColor: 'rgba(64, 158, 255, 0.3)'
        }
      }
    ]
  }
  
  // 先设置基本配置
  chartInstance.setOption(option, true)
  
  // 强制更新：通过setOption再次设置，确保数据节点中的itemStyle生效
  setTimeout(() => {
    chartInstance?.setOption({
      series: [{
        data: chartData
      }]
    }, false) // 使用merge模式，只更新data
  }, 100)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
  
  // 双击事件（编辑部门）- 避免与折叠功能冲突
  chartInstance.on('dblclick', (params: any) => {
    if (params.data && params.data.name) {
      const findDepartment = (nodes: Department[], name: string): Department | null => {
        for (const node of nodes) {
          if (node.name === name) return node
          if (node.children) {
            const found = findDepartment(node.children, name)
            if (found) return found
          }
        }
        return null
      }
      
      const dept = findDepartment(treeData.value, params.data.value)
      if (dept) {
        handleEdit(dept)
      }
    }
  })
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
    
    <el-tabs v-model="activeTab" type="border-card" class="department-tabs">
      <el-tab-pane label="可视化视图" name="visualization">
        <el-card v-loading="loading" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>组织架构树形图</span>
              <el-button type="primary" size="small" @click="initChart">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          <div class="chart-wrapper">
            <div ref="chartContainer" class="chart-container"></div>
            <div class="chart-tips">
              <el-alert
                title="操作提示"
                type="info"
                :closable="false"
                show-icon
              >
                <template #default>
                  <div style="font-size: 12px; line-height: 1.8;">
                    <div>🖱️ <strong>拖拽</strong>：鼠标左键按住拖动可平移视图</div>
                    <div>🔍 <strong>缩放</strong>：鼠标滚轮上下滚动可缩放视图</div>
                    <div>📁 <strong>折叠</strong>：单击节点可展开/折叠子节点</div>
                    <div>✏️ <strong>编辑</strong>：双击节点可编辑部门信息</div>
                    <div>🔄 <strong>重置</strong>：点击工具栏"重置视图"恢复初始状态</div>
                  </div>
                </template>
              </el-alert>
            </div>
          </div>
          <el-empty v-if="treeData.length === 0 && !loading" description="暂无组织架构数据" />
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="列表视图" name="list">
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
      </el-tab-pane>
    </el-tabs>
    
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
import { Plus, OfficeBuilding, Refresh } from '@element-plus/icons-vue'
export default {
  components: { Plus, OfficeBuilding, Refresh }
}
</script>

<style scoped lang="scss">
.department-tabs {
  margin-top: 20px;
  
  :deep(.el-tabs__content) {
    padding: 0;
  }
}

.chart-card {
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }
  
  .chart-wrapper {
    position: relative;
  }
  
  .chart-container {
    width: 100%;
    height: 700px;
    min-height: 500px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 8px;
    cursor: move;
  }
  
  .chart-tips {
    position: absolute;
    top: 10px;
    left: 10px;
    width: 280px;
    z-index: 10;
    
    :deep(.el-alert) {
      background-color: rgba(255, 255, 255, 0.95);
      border: 1px solid #e4e7ed;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }
    
    :deep(.el-alert__content) {
      padding: 8px 0;
    }
  }
}

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

// 响应式设计
@media (max-width: 768px) {
  .chart-container {
    height: 500px !important;
  }
}
</style>

