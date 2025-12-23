import { request } from './request'

// 用户查询请求
export interface UserQueryRequest {
  page?: number
  size?: number
  keyword?: string
  status?: string
  roleId?: string
}

// 创建用户请求
export interface CreateUserRequest {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
  roleIds?: string[]
}

// 更新用户请求
export interface UpdateUserRequest {
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
}

// 分配角色请求
export interface AssignRolesRequest {
  roleIds: string[]
}

// 用户响应
export interface UserResponse {
  id: string
  username: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  status: string
  role?: string
  roleNames?: string[]
  tenantId?: string
  tenantName?: string
  lastLoginAt?: string
  lastLoginIp?: string
  createdAt?: string
  updatedAt?: string
}

// 分页数据
export interface PageData<T> {
  records: T[]
  total: number
  page: number
  capacity: number
  pageCount: number
}

// API响应包装
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 用户管理API
export const userApi = {
  // 获取用户列表
  list(params?: UserQueryRequest): Promise<ApiResponse<PageData<UserResponse>>> {
    return request.get('/iam/v1/users', { params })
  },

  // 创建用户
  create(data: CreateUserRequest): Promise<ApiResponse<UserResponse>> {
    return request.post('/iam/v1/users', data)
  },

  // 获取用户详情
  getById(id: string): Promise<ApiResponse<UserResponse>> {
    return request.get(`/iam/v1/users/${id}`)
  },

  // 更新用户
  update(id: string, data: UpdateUserRequest): Promise<ApiResponse<UserResponse>> {
    return request.put(`/iam/v1/users/${id}`, data)
  },

  // 删除用户
  delete(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/iam/v1/users/${id}`)
  },

  // 启用用户
  enable(id: string): Promise<ApiResponse<void>> {
    return request.post(`/iam/v1/users/${id}/enable`)
  },

  // 禁用用户
  disable(id: string): Promise<ApiResponse<void>> {
    return request.post(`/iam/v1/users/${id}/disable`)
  },

  // 重置密码
  resetPassword(id: string): Promise<ApiResponse<{ newPassword: string }>> {
    return request.post(`/iam/v1/users/${id}/reset-password`)
  },

  // 分配角色
  assignRoles(id: string, data: AssignRolesRequest): Promise<ApiResponse<void>> {
    return request.post(`/iam/v1/users/${id}/roles`, data)
  },
}

export default userApi
