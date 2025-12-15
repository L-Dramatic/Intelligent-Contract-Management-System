// 用户相关类型
export interface UserInfo {
  id: number
  username: string
  realName?: string
  email?: string
  phone?: string
  role: string
  // 后端字段为 deptId；前端原来用 departmentId，这里都兼容
  deptId?: number
  departmentId?: number
  departmentName?: string
  permissions?: string[]
  createTime?: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  realName: string
  email: string
  phone: string
  departmentId: number
}

// 合同相关类型
export interface Contract {
  id: number
  contractNo: string
  contractName: string
  contractType: ContractType
  partyA: string
  partyB: string
  amount: number
  content: string
  status: ContractStatus
  isAiGenerated: boolean
  creatorId: number
  creatorName: string
  createTime: string
  updateTime: string
  // 基站租赁合同扩展字段
  siteLocation?: string
  siteType?: string
  siteArea?: number
  annualRent?: number
  leaseStartDate?: string
  leaseEndDate?: string
  // 网络建设合同扩展字段
  constructionArea?: string
  networkType?: string
  plannedStations?: number
  coverageRequirement?: number
}

export type ContractType = 'STATION_LEASE' | 'NETWORK_CONSTRUCTION' | 'EQUIPMENT_PURCHASE' | 'MAINTENANCE_SERVICE'

export type ContractStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED'

export interface ContractQuery {
  contractNo?: string
  contractName?: string
  contractType?: ContractType
  status?: ContractStatus
  partyA?: string
  partyB?: string
  minAmount?: number
  maxAmount?: number
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

// 工作流相关类型
export interface WorkflowDefinition {
  id: number
  name: string
  description: string
  applicableContractTypes: ContractType[]
  conditionExpression: string
  version: number
  enabled: boolean
  createTime: string
  updateTime: string
  nodes?: WorkflowNode[]
  transitions?: WorkflowTransition[]
}

export interface WorkflowNode {
  id: number
  workflowId: number
  nodeCode: string
  nodeType: NodeType
  nodeName: string
  nodeConfig: NodeConfig
  sortOrder: number
  x?: number
  y?: number
}

export type NodeType = 'START' | 'APPROVE' | 'COUNTERSIGN' | 'CONDITION' | 'END'

export interface NodeConfig {
  approverStrategy?: ApproverStrategy
  approverRole?: string
  countersignRule?: 'ALL' | 'MAJORITY'
  rejectTo?: 'INITIATOR' | 'PREVIOUS'
  conditionExpression?: string
}

export type ApproverStrategy = 'TREE_REPORT' | 'SPECIFIC_ROLE' | 'GLOBAL_ROLE'

export interface WorkflowTransition {
  id: number
  workflowId: number
  sourceNodeId: number
  targetNodeId: number
  conditionExpression?: string
  sortOrder: number
}

export interface WorkflowInstance {
  id: number
  workflowDefinitionId: number
  contractId: number
  contractName: string
  currentNodeId: number
  currentNodeName: string
  status: InstanceStatus
  initiatorId: number
  initiatorName: string
  startTime: string
  endTime?: string
  endReason?: string
}

export type InstanceStatus = 'RUNNING' | 'COMPLETED' | 'REJECTED' | 'TERMINATED'

export interface ApprovalTask {
  id: number
  instanceId: number
  nodeId: number
  nodeName: string
  approverId: number
  approverName: string
  status: TaskStatus
  parallelGroupId?: string
  opinion?: string
  approvalTime?: string
  contractId: number
  contractName: string
  contractNo: string
  contractType: ContractType
  initiatorName: string
  createTime: string
}

export type TaskStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'TRANSFERRED'

// 知识库相关类型
export interface KnowledgeItem {
  id: number
  type: KnowledgeType
  title: string
  content: string
  metadata?: Record<string, unknown>
  createTime: string
  updateTime: string
}

export type KnowledgeType = 'TERM' | 'TEMPLATE' | 'REGULATION' | 'CLAUSE'

// 组织架构相关类型
export interface Department {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  children?: Department[]
}

export interface Position {
  id: number
  name: string
  departmentId: number
  level: number
}

// AI相关类型
export interface AiChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export interface AiGenerateRequest {
  contractType: ContractType
  messages: AiChatMessage[]
}

export interface AiReviewResult {
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  score: number
  highRiskItems: RiskItem[]
  mediumRiskItems: RiskItem[]
  lowRiskItems: RiskItem[]
  goodClauses: string[]
}

export interface RiskItem {
  issue: string
  suggestion: string
  clause?: string
}

// 分页相关
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

// 统计相关
export interface DashboardStats {
  totalContracts: number
  pendingApprovals: number
  approvedThisMonth: number
  draftContracts: number
  contractsByType: Record<ContractType, number>
  contractsByStatus: Record<ContractStatus, number>
  recentContracts: Contract[]
  myPendingTasks: ApprovalTask[]
}

