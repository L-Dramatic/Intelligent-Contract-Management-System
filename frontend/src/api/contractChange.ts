import request from '@/utils/request'

// 变更类型选项
export const CHANGE_TYPE_OPTIONS = [
  { value: 'AMOUNT', label: '金额变更' },
  { value: 'TIME', label: '时间变更' },
  { value: 'TECH', label: '技术方案变更' },
  { value: 'ATTACHMENT', label: '附件补充' },
  { value: 'CONTACT', label: '联系人变更' },
  { value: 'OTHER', label: '其他' }
]

// 变更原因选项
export const REASON_TYPE_OPTIONS = [
  { value: 'ACTIVE', label: '本方主动' },
  { value: 'PASSIVE', label: '应乙方要求' },
  { value: 'FORCE_MAJEURE', label: '不可抗力' },
  { value: 'POLICY', label: '政策调整' },
  { value: 'OTHER', label: '其他' }
]

// 变更状态选项
export const CHANGE_STATUS_OPTIONS = [
  { value: 0, label: '草稿', type: 'info' },
  { value: 1, label: '审批中', type: 'warning' },
  { value: 2, label: '已通过', type: 'success' },
  { value: 3, label: '已驳回', type: 'danger' },
  { value: 4, label: '已撤销', type: 'info' }
]

export interface ContractChangeDTO {
  contractId: number
  title: string
  changeType: string
  reasonType: string
  description: string
  partyBCommunication?: string
  newName?: string
  newAmount?: number
  newContent?: string
  newPartyB?: string
  newAttributes?: Record<string, any>
  attachmentPath?: string
}

export interface ContractChangeVO {
  id: number
  contractId: number
  contractNo: string
  contractName: string
  changeNo: string
  title: string
  changeVersion: string
  changeType: string
  changeTypeName: string
  reasonType: string
  reasonTypeName: string
  description: string
  partyBCommunication?: string
  diffData?: Record<string, any>
  diffItems?: DiffItem[]
  isMajorChange: boolean
  amountDiff?: number
  changePercent?: number
  status: number
  statusName: string
  instanceId?: number
  initiatorId: number
  initiatorName: string
  createdAt: string
  approvedAt?: string
  effectiveAt?: string
  attachmentPath?: string
}

export interface DiffItem {
  fieldName: string
  fieldLabel: string
  beforeValue: any
  afterValue: any
  changeDesc: string
}

/**
 * 创建变更申请（草稿）
 */
export function createChange(data: ContractChangeDTO) {
  return request({
    url: '/contract-change/create',
    method: 'post',
    data
  })
}

/**
 * 提交变更审批
 */
export function submitChange(changeId: number) {
  return request({
    url: `/contract-change/${changeId}/submit`,
    method: 'post'
  })
}

/**
 * 撤销变更申请
 */
export function cancelChange(changeId: number) {
  return request({
    url: `/contract-change/${changeId}/cancel`,
    method: 'post'
  })
}

/**
 * 获取变更详情
 */
export function getChangeDetail(changeId: number) {
  return request<ContractChangeVO>({
    url: `/contract-change/${changeId}`,
    method: 'get'
  })
}

/**
 * 获取合同的变更历史
 */
export function getChangeHistory(contractId: number) {
  return request<ContractChangeVO[]>({
    url: `/contract-change/history/${contractId}`,
    method: 'get'
  })
}

/**
 * 分页查询变更申请
 */
export function listChanges(params: {
  pageNum?: number
  pageSize?: number
  contractId?: number
  status?: number
  initiatorId?: number
}) {
  return request({
    url: '/contract-change/list',
    method: 'get',
    params
  })
}

/**
 * 我的变更申请
 */
export function myChanges(params: {
  pageNum?: number
  pageSize?: number
  status?: number
}) {
  return request({
    url: '/contract-change/my',
    method: 'get',
    params
  })
}

/**
 * 检查合同是否可以发起变更
 */
export function canCreateChange(contractId: number) {
  return request<{ canChange: boolean; message: string }>({
    url: `/contract-change/can-change/${contractId}`,
    method: 'get'
  })
}

/**
 * 重大变更预检
 */
export function checkMajorChange(data: ContractChangeDTO) {
  return request<{ isMajorChange: boolean; message: string }>({
    url: '/contract-change/check-major',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户可变更的合同列表
 * 返回用户有权限变更的已生效合同（创建者或审批参与者）
 */
export function getChangeableContracts() {
  return request<any[]>({
    url: '/contract-change/changeable-contracts',
    method: 'get'
  })
}

