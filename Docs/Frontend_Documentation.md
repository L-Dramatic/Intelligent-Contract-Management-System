# 电信智慧合同管理系统 - 前端文档

## 1. 项目概述

本前端项目是电信领域智慧合同管理系统的Web端实现，基于Vue 3 + TypeScript + Element Plus构建，提供完整的合同管理、工作流审批、AI智能生成等功能。

### 1.1 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 渐进式JavaScript框架 |
| TypeScript | 5.9.x | JavaScript的超集，提供类型检查 |
| Vite | 7.x | 新一代前端构建工具 |
| Vue Router | 4.5.x | Vue官方路由管理器 |
| Pinia | 3.x | Vue官方状态管理库 |
| Element Plus | 2.11.x | Vue 3 UI组件库 |
| Axios | 1.12.x | HTTP请求库 |

### 1.2 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口层
│   │   ├── user.ts        # 用户相关接口
│   │   ├── contract.ts    # 合同相关接口
│   │   ├── workflow.ts    # 工作流相关接口
│   │   ├── knowledge.ts   # 知识库相关接口
│   │   ├── department.ts  # 组织架构相关接口
│   │   └── dashboard.ts   # 仪表盘相关接口
│   ├── assets/            # 样式资源
│   │   └── main.css       # 主样式文件
│   ├── components/        # 公共组件
│   ├── layout/            # 布局组件
│   │   └── MainLayout.vue # 主布局
│   ├── router/            # 路由配置
│   │   └── index.ts       # 路由定义
│   ├── stores/            # 状态管理
│   │   └── user.ts        # 用户状态
│   ├── types/             # TypeScript类型定义
│   │   └── index.ts       # 类型定义
│   ├── utils/             # 工具函数
│   │   └── request.ts     # Axios封装
│   ├── views/             # 页面组件
│   │   ├── login/         # 登录注册
│   │   ├── dashboard/     # 工作台
│   │   ├── contract/      # 合同管理
│   │   ├── workflow/      # 审批中心
│   │   ├── system/        # 系统管理
│   │   └── error/         # 错误页面
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── package.json           # 项目配置
├── vite.config.ts         # Vite配置
└── tsconfig.json          # TypeScript配置
```

---

## 2. 功能模块

### 2.1 用户认证模块

#### 页面列表

| 页面 | 路径 | 文件 | 说明 |
|------|------|------|------|
| 登录页 | /login | views/login/LoginView.vue | 用户登录 |
| 注册页 | /register | views/login/RegisterView.vue | 用户注册 |

#### 功能特性
- JWT Token认证
- 登录状态持久化（localStorage）
- 路由守卫（未登录自动跳转登录页）
- 演示账号提示

### 2.2 工作台模块

#### 页面列表

| 页面 | 路径 | 文件 | 说明 |
|------|------|------|------|
| 工作台 | /dashboard | views/dashboard/DashboardView.vue | 首页仪表盘 |

#### 功能特性
- 合同统计卡片（总数、待审批、本月已审批、草稿）
- 合同类型分布图
- 待办任务列表
- 最近合同列表

### 2.3 合同管理模块

#### 页面列表

| 页面 | 路径 | 文件 | 说明 |
|------|------|------|------|
| 合同列表 | /contract/list | views/contract/ContractList.vue | 所有合同列表 |
| 创建合同 | /contract/create | views/contract/ContractCreate.vue | 选择合同类型和创建方式 |
| AI智能生成 | /contract/ai-generate | views/contract/AiGenerate.vue | AI对话式生成合同 |
| 合同详情 | /contract/detail/:id | views/contract/ContractDetail.vue | 查看合同详情 |
| 编辑合同 | /contract/edit/:id | views/contract/ContractEdit.vue | 编辑合同内容 |
| 我的合同 | /contract/my | views/contract/MyContracts.vue | 当前用户的合同 |

#### 功能特性
- 四种合同类型支持（基站租赁、网络建设、设备采购、运维服务）
- 三种创建方式（AI对话生成、使用模板、上传文件）
- AI智能生成（RAG增强）
- AI条款合规审查
- 合同状态流转（草稿→审批中→已生效/已驳回）

### 2.4 审批中心模块

#### 页面列表

| 页面 | 路径 | 文件 | 说明 |
|------|------|------|------|
| 待办任务 | /workflow/pending | views/workflow/PendingTasks.vue | 待处理的审批任务 |
| 已办任务 | /workflow/completed | views/workflow/CompletedTasks.vue | 已处理的审批任务 |
| 我发起的 | /workflow/initiated | views/workflow/InitiatedTasks.vue | 用户发起的流程 |
| 审批页面 | /workflow/approve/:id | views/workflow/ApproveTask.vue | 执行审批操作 |

#### 功能特性
- 待办任务卡片展示
- 审批通过/驳回操作
- 转签功能
- 会签任务标识
- 审批历史记录

### 2.5 流程管理模块（流程管理员）

#### 页面列表

| 页面 | 路径 | 文件 | 说明 |
|------|------|------|------|
| 流程模板 | /workflow-admin/definition | views/workflow/WorkflowDefinition.vue | 流程模板列表 |
| 编辑流程 | /workflow-admin/definition/edit/:id | views/workflow/WorkflowEdit.vue | 编辑流程模板 |

#### 功能特性
- 流程模板CRUD
- 节点配置（开始、审批、会签、条件、结束）
- 审批策略配置（树状上报、指定角色、全局角色）
- 流程启用/停用

### 2.6 系统管理模块（系统管理员）

#### 页面列表

| 页面 | 路径 | 文件 | 说明 |
|------|------|------|------|
| 用户管理 | /system/user | views/system/UserManage.vue | 用户CRUD |
| 组织架构 | /system/department | views/system/DepartmentManage.vue | 部门树管理 |
| 知识库管理 | /system/knowledge | views/system/KnowledgeManage.vue | 电信知识库管理 |

#### 功能特性
- 用户增删改查
- 角色分配
- 密码重置
- 组织架构树形管理
- 知识库条目管理（术语、模板、法规、条款）
- 向量索引重建

---

## 3. API接口文档

### 3.1 用户接口 (api/user.ts)

| 方法 | 接口 | 说明 | 请求参数 | 后端状态 |
|------|------|------|----------|----------|
| POST | /api/user/login | 用户登录 | { username, password } | ✅ 已实现 |
| POST | /api/user/register | 用户注册 | { username, password, realName, email, phone } | ✅ 已实现 |
| GET | /api/user/info | 获取当前用户信息 | - | ✅ 已实现 |
| GET | /api/user/list | 获取用户列表 | { pageNum, pageSize, username?, role? } | ⏳ 待实现 |
| PUT | /api/user/{id} | 更新用户信息 | { realName, email, phone, role } | ⏳ 待实现 |
| DELETE | /api/user/{id} | 删除用户 | - | ✅ 已实现 |
| PUT | /api/user/password | 修改密码 | { oldPassword, newPassword } | ⏳ 待实现 |
| POST | /api/user/{id}/reset-password | 重置密码 | - | ⏳ 待实现 |

### 3.2 合同接口 (api/contract.ts)

| 方法 | 接口 | 说明 | 请求参数 | 后端状态 |
|------|------|------|----------|----------|
| GET | /api/contract/list | 获取合同列表 | ContractQuery | ⏳ 待实现 |
| GET | /api/contract/{id} | 获取合同详情 | - | ⏳ 待实现 |
| POST | /api/contract | 创建合同 | Contract | ⏳ 待实现 |
| PUT | /api/contract/{id} | 更新合同 | Contract | ⏳ 待实现 |
| DELETE | /api/contract/{id} | 删除合同 | - | ⏳ 待实现 |
| POST | /api/contract/{id}/submit | 提交合同审批 | - | ⏳ 待实现 |
| POST | /api/contract/{id}/withdraw | 撤回合同 | - | ⏳ 待实现 |
| POST | /api/contract/ai/generate | AI生成合同 | { contractType, messages } | ⏳ 待实现 |
| POST | /api/contract/{id}/ai/review | AI条款审查 | - | ⏳ 待实现 |
| GET | /api/contract/templates | 获取合同模板列表 | { type? } | ⏳ 待实现 |
| POST | /api/contract/from-template/{templateId} | 根据模板创建 | Contract | ⏳ 待实现 |
| POST | /api/contract/upload | 上传合同文件 | FormData(file) | ⏳ 待实现 |
| GET | /api/contract/{id}/export/pdf | 导出PDF | - | ⏳ 待实现 |
| GET | /api/contract/my | 获取我的合同 | ContractQuery | ⏳ 待实现 |

### 3.3 工作流接口 (api/workflow.ts)

#### 流程定义管理

| 方法 | 接口 | 说明 | 请求参数 | 后端状态 |
|------|------|------|----------|----------|
| GET | /api/workflow/definition/list | 获取流程定义列表 | { enabled?, name? } | ⏳ 待实现 |
| GET | /api/workflow/definition/{id} | 获取流程定义详情 | - | ⏳ 待实现 |
| POST | /api/workflow/definition | 创建流程定义 | WorkflowDefinition | ⏳ 待实现 |
| PUT | /api/workflow/definition/{id} | 更新流程定义 | WorkflowDefinition | ⏳ 待实现 |
| DELETE | /api/workflow/definition/{id} | 删除流程定义 | - | ⏳ 待实现 |
| PUT | /api/workflow/definition/{id}/status | 启用/停用流程 | { enabled } | ⏳ 待实现 |
| POST | /api/workflow/definition/{id}/nodes | 保存流程节点 | WorkflowNode[] | ⏳ 待实现 |
| POST | /api/workflow/definition/{id}/transitions | 保存流程连线 | WorkflowTransition[] | ⏳ 待实现 |

#### 流程实例管理

| 方法 | 接口 | 说明 | 请求参数 | 后端状态 |
|------|------|------|----------|----------|
| GET | /api/workflow/instance/list | 获取流程实例列表 | { status?, contractId?, pageNum, pageSize } | ⏳ 待实现 |
| GET | /api/workflow/instance/{id} | 获取流程实例详情 | - | ⏳ 待实现 |
| GET | /api/workflow/instance/{id}/history | 获取审批历史 | - | ⏳ 待实现 |
| POST | /api/workflow/instance/{id}/terminate | 终止流程 | { reason } | ⏳ 待实现 |

#### 审批任务管理

| 方法 | 接口 | 说明 | 请求参数 | 后端状态 |
|------|------|------|----------|----------|
| GET | /api/workflow/task/pending | 获取待办任务 | { pageNum, pageSize } | ⏳ 待实现 |
| GET | /api/workflow/task/completed | 获取已办任务 | { pageNum, pageSize } | ⏳ 待实现 |
| GET | /api/workflow/task/{id} | 获取任务详情 | - | ⏳ 待实现 |
| POST | /api/workflow/task/{id}/approve | 审批通过 | { opinion } | ⏳ 待实现 |
| POST | /api/workflow/task/{id}/reject | 审批驳回 | { opinion } | ⏳ 待实现 |
| POST | /api/workflow/task/{id}/transfer | 转签 | { targetUserId, reason } | ⏳ 待实现 |
| POST | /api/workflow/task/{id}/add-sign | 加签 | { userIds, reason } | ⏳ 待实现 |
| GET | /api/workflow/task/transferable-users | 获取可转签用户 | - | ⏳ 待实现 |

### 3.4 知识库接口 (api/knowledge.ts)

| 方法 | 接口 | 说明 | 请求参数 | 后端状态 |
|------|------|------|----------|----------|
| GET | /api/knowledge/list | 获取知识列表 | { type?, keyword?, pageNum, pageSize } | ⏳ 待实现 |
| GET | /api/knowledge/{id} | 获取知识详情 | - | ⏳ 待实现 |
| POST | /api/knowledge | 添加知识条目 | KnowledgeItem | ⏳ 待实现 |
| PUT | /api/knowledge/{id} | 更新知识条目 | KnowledgeItem | ⏳ 待实现 |
| DELETE | /api/knowledge/{id} | 删除知识条目 | - | ⏳ 待实现 |
| POST | /api/knowledge/import | 批量导入 | FormData(file, type) | ⏳ 待实现 |
| POST | /api/knowledge/rebuild-index | 重建向量索引 | - | ⏳ 待实现 |
| GET | /api/knowledge/search | RAG检索 | { query, type?, limit? } | ⏳ 待实现 |
| GET | /api/knowledge/stats | 知识库统计 | - | ⏳ 待实现 |

### 3.5 组织架构接口 (api/department.ts)

| 方法 | 接口 | 说明 | 请求参数 | 后端状态 |
|------|------|------|----------|----------|
| GET | /api/department/tree | 获取组织架构树 | - | ⏳ 待实现 |
| GET | /api/department/{id} | 获取部门详情 | - | ⏳ 待实现 |
| POST | /api/department | 创建部门 | Department | ⏳ 待实现 |
| PUT | /api/department/{id} | 更新部门 | Department | ⏳ 待实现 |
| DELETE | /api/department/{id} | 删除部门 | - | ⏳ 待实现 |
| GET | /api/department/{id}/users | 获取部门用户 | - | ⏳ 待实现 |
| GET | /api/position/list | 获取职位列表 | { departmentId? } | ⏳ 待实现 |
| POST | /api/position | 创建职位 | Position | ⏳ 待实现 |
| PUT | /api/position/{id} | 更新职位 | Position | ⏳ 待实现 |
| DELETE | /api/position/{id} | 删除职位 | - | ⏳ 待实现 |

### 3.6 仪表盘接口 (api/dashboard.ts)

| 方法 | 接口 | 说明 | 请求参数 | 后端状态 |
|------|------|------|----------|----------|
| GET | /api/dashboard/stats | 获取统计数据 | - | ⏳ 待实现 |
| GET | /api/dashboard/recent-contracts | 获取最近合同 | { limit? } | ⏳ 待实现 |
| GET | /api/dashboard/my-tasks | 获取我的待办 | { limit? } | ⏳ 待实现 |
| GET | /api/dashboard/contract-trend | 获取合同趋势 | { days? } | ⏳ 待实现 |
| GET | /api/dashboard/approval-efficiency | 获取审批效率 | - | ⏳ 待实现 |

---

## 4. 数据类型定义

### 4.1 用户相关

```typescript
interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  role: string  // USER | APPROVER | LEGAL | WORKFLOW_ADMIN | ADMIN
  departmentId: number
  departmentName: string
  permissions: string[]
  createTime: string
}
```

### 4.2 合同相关

```typescript
interface Contract {
  id: number
  contractNo: string           // 合同编号
  contractName: string         // 合同名称
  contractType: ContractType   // 合同类型
  partyA: string              // 甲方
  partyB: string              // 乙方
  amount: number              // 金额
  content: string             // 合同正文
  status: ContractStatus      // 状态
  isAiGenerated: boolean      // 是否AI生成
  creatorId: number
  creatorName: string
  createTime: string
  updateTime: string
  // 扩展字段...
}

type ContractType = 'STATION_LEASE' | 'NETWORK_CONSTRUCTION' | 'EQUIPMENT_PURCHASE' | 'MAINTENANCE_SERVICE'

type ContractStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED'
```

### 4.3 工作流相关

```typescript
interface WorkflowDefinition {
  id: number
  name: string
  description: string
  applicableContractTypes: ContractType[]
  conditionExpression: string
  version: number
  enabled: boolean
  nodes?: WorkflowNode[]
  transitions?: WorkflowTransition[]
}

interface WorkflowNode {
  id: number
  workflowId: number
  nodeCode: string
  nodeType: NodeType  // START | APPROVE | COUNTERSIGN | CONDITION | END
  nodeName: string
  nodeConfig: NodeConfig
  sortOrder: number
}

interface ApprovalTask {
  id: number
  instanceId: number
  nodeId: number
  nodeName: string
  approverId: number
  approverName: string
  status: TaskStatus  // PENDING | APPROVED | REJECTED | TRANSFERRED
  parallelGroupId?: string  // 会签任务组标识
  opinion?: string
  approvalTime?: string
  contractId: number
  contractName: string
  contractNo: string
  contractType: ContractType
  initiatorName: string
  createTime: string
}
```

---

## 5. 启动与部署

### 5.1 开发环境

```bash
# 安装依赖
cd frontend
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

### 5.2 生产构建

```bash
# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

### 5.3 环境配置

开发环境下，Vite会自动将 `/api` 开头的请求代理到后端服务器：

```typescript
// vite.config.ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

---

## 6. 待办事项

### 6.1 后端待实现接口

根据接口文档，以下后端接口需要实现：

1. **合同管理接口** - 合同CRUD、AI生成、AI审查
2. **工作流引擎接口** - 流程定义、流程实例、审批任务
3. **知识库接口** - RAG检索、知识CRUD
4. **组织架构接口** - 部门树、职位管理
5. **仪表盘接口** - 统计数据、趋势分析

### 6.2 前端优化项

- [ ] 添加更多的表单验证
- [ ] 优化移动端响应式
- [ ] 添加更多的loading状态
- [ ] 完善错误处理机制
- [ ] 添加国际化支持

---

## 7. 更新日志

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0.0 | 2025-12-14 | 初始版本，完成所有页面开发 |

---

**文档编写日期：** 2025年12月14日

**前端版本：** 1.0.0

