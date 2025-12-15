import request from '@/utils/request'
import type { LoginForm, RegisterForm, UserInfo, PageResult } from '@/types'

// 用户登录
export function login(data: LoginForm) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data: RegisterForm) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 获取用户列表（管理员）
export function getUserList(params: { pageNum: number; pageSize: number; username?: string; role?: string }) {
  return request<PageResult<UserInfo>>({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 更新用户信息
export function updateUser(id: number, data: Partial<UserInfo>) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id: number) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

// 修改密码
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

// 重置密码（管理员）
export function resetPassword(userId: number) {
  return request({
    url: `/user/${userId}/reset-password`,
    method: 'post'
  })
}

