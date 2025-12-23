import request from '@/utils/request'

// 合同类型相关接口

export interface ContractTypeGroup {
  typeCode: string       // TYPE_A, TYPE_B, TYPE_C
  typeName: string       // 采购类, 工程类, 服务类
  subTypes: SubType[]
}

export interface SubType {
  subTypeCode: string    // A1, A2, B1, B2, C1...
  subTypeName: string
  description: string
}

export interface ContractTypeItem {
  id: number
  typeCode: string
  typeName: string
  subTypeCode: string
  subTypeName: string
  description: string
  sortOrder: number
  isActive: number
}

// 获取分组的合同类型（用于类型选择页面）
export function getContractTypesGrouped() {
  return request<ContractTypeGroup[]>({
    url: '/contract-type/grouped',
    method: 'get'
  })
}

// 获取所有合同类型（扁平列表）
export function getContractTypes() {
  return request<ContractTypeItem[]>({
    url: '/contract-type/list',
    method: 'get'
  })
}

// 获取子类型详情
export function getSubTypeDetail(subTypeCode: string) {
  return request<ContractTypeItem>({
    url: `/contract-type/detail/${subTypeCode}`,
    method: 'get'
  })
}
