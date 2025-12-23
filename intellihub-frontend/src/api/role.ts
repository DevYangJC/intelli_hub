import { request } from './request'

// 创建角色请求
export interface CreateRoleRequest {
  code: string
  name: string
  description?: string
  sort?: number
}

// 更新角色请求
export interface UpdateRoleRequest {
  name?: string
  description?: string
  sort?: number
}

// 更新角色权限请求
export interface UpdateRolePermissionsRequest {
  permissionIds: string[]
}

// 角色响应
export interface RoleResponse {
  id: string
  code: string
  name: string
  description?: string
  isSystem: boolean
  sort: number
  status: string
  tenantId?: string
  createdAt?: string
  updatedAt?: string
}

// 权限响应
export interface PermissionResponse {
  id: string
  code: string
  name: string
  groupName?: string
  description?: string
  sort: number
}

// 菜单响应
export interface MenuResponse {
  id: string
  parentId?: string
  name: string
  path?: string
  component?: string
  icon?: string
  permission?: string
  type: number
  visible: boolean
  sort: number
  children?: MenuResponse[]
}

// API响应包装
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 角色权限管理API
export const roleApi = {
  // 获取角色列表
  list(): Promise<ApiResponse<RoleResponse[]>> {
    return request.get('/iam/v1/roles')
  },

  // 创建角色
  create(data: CreateRoleRequest): Promise<ApiResponse<RoleResponse>> {
    return request.post('/iam/v1/roles', data)
  },

  // 更新角色
  update(id: string, data: UpdateRoleRequest): Promise<ApiResponse<RoleResponse>> {
    return request.put(`/iam/v1/roles/${id}`, data)
  },

  // 删除角色
  delete(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/iam/v1/roles/${id}`)
  },

  // 获取角色权限
  getPermissions(id: string): Promise<ApiResponse<string[]>> {
    return request.get(`/iam/v1/roles/${id}/permissions`)
  },

  // 更新角色权限
  updatePermissions(id: string, data: UpdateRolePermissionsRequest): Promise<ApiResponse<void>> {
    return request.put(`/iam/v1/roles/${id}/permissions`, data)
  },

  // 获取所有权限列表
  listPermissions(): Promise<ApiResponse<PermissionResponse[]>> {
    return request.get('/iam/v1/permissions')
  },

  // 获取菜单树
  getMenuTree(): Promise<ApiResponse<MenuResponse[]>> {
    return request.get('/iam/v1/menus')
  },
}

export default roleApi
