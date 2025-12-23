import request from '@/utils/request'
import type { Department, Position, UserInfo } from '@/types'

// 获取组织架构树
export function getDepartmentTree() {
  return request<Department[]>({
    url: '/dept/tree',  // 后端接口是 /dept/tree
    method: 'get'
  })
}

// 获取部门详情
export function getDepartmentDetail(id: number) {
  return request<Department>({
    url: `/department/${id}`,
    method: 'get'
  })
}

// 创建部门
export function createDepartment(data: Partial<Department>) {
  return request<Department>({
    url: '/department',
    method: 'post',
    data
  })
}

// 更新部门
export function updateDepartment(id: number, data: Partial<Department>) {
  return request<Department>({
    url: `/department/${id}`,
    method: 'put',
    data
  })
}

// 删除部门
export function deleteDepartment(id: number) {
  return request({
    url: `/department/${id}`,
    method: 'delete'
  })
}

// 获取部门下的用户
export function getDepartmentUsers(departmentId: number) {
  return request<UserInfo[]>({
    url: `/department/${departmentId}/users`,
    method: 'get'
  })
}

// 获取职位列表
export function getPositionList(departmentId?: number) {
  return request<Position[]>({
    url: '/position/list',
    method: 'get',
    params: { departmentId }
  })
}

// 创建职位
export function createPosition(data: Partial<Position>) {
  return request<Position>({
    url: '/position',
    method: 'post',
    data
  })
}

// 更新职位
export function updatePosition(id: number, data: Partial<Position>) {
  return request<Position>({
    url: `/position/${id}`,
    method: 'put',
    data
  })
}

// 删除职位
export function deletePosition(id: number) {
  return request({
    url: `/position/${id}`,
    method: 'delete'
  })
}

