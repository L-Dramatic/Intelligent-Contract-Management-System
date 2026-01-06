import request from '@/utils/request'

export interface Role {
  id: number
  roleCode: string
  roleName: string
  roleCategory: string
  deptTypeRequired?: string
  description?: string
  createdAt: string
}

// 获取所有角色
export function getRoleList() {
  return request<Role[]>({
    url: '/role/list',
    method: 'get'
  })
}

// 获取审批角色（排除系统管理员）
export function getApprovalRoles() {
  return request<Role[]>({
    url: '/role/approval-roles',
    method: 'get'
  })
}

// 根据类别获取角色
export function getRolesByCategory(category: string) {
  return request<Role[]>({
    url: `/role/category/${category}`,
    method: 'get'
  })
}



