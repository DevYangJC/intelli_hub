import { request } from './request'

// 租户查询请求
export interface TenantQueryRequest {
  page?: number
  size?: number
  keyword?: string
  status?: string
}

// 创建租户请求
export interface CreateTenantRequest {
  name: string
  code: string
  adminUsername: string
  adminPassword: string
  adminEmail?: string
  adminPhone?: string
  description?: string
  quota?: TenantQuotaRequest
}

// 更新租户请求
export interface UpdateTenantRequest {
  name?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  description?: string
}

// 租户配额请求
export interface TenantQuotaRequest {
  maxUsers?: number
  maxApps?: number
  maxApis?: number
  maxQps?: number
}

// 租户响应
export interface TenantResponse {
  id: string
  name: string
  code: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  description?: string
  status: string
  quota?: TenantQuotaInfo
  createdAt?: string
  updatedAt?: string
}

// 租户配额信息
export interface TenantQuotaInfo {
  maxUsers: number
  maxApps: number
  maxApis: number
  maxQps: number
  usedUsers: number
  usedApps: number
  usedApis: number
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

// 租户管理API
export const tenantApi = {
  // 获取租户列表
  list(params?: TenantQueryRequest): Promise<ApiResponse<PageData<TenantResponse>>> {
    return request.get('/iam/v1/tenants', { params })
  },

  // 创建租户
  create(data: CreateTenantRequest): Promise<ApiResponse<TenantResponse>> {
    return request.post('/iam/v1/tenants', data)
  },

  // 获取租户详情
  getById(id: string): Promise<ApiResponse<TenantResponse>> {
    return request.get(`/iam/v1/tenants/${id}`)
  },

  // 更新租户
  update(id: string, data: UpdateTenantRequest): Promise<ApiResponse<TenantResponse>> {
    return request.put(`/iam/v1/tenants/${id}`, data)
  },

  // 删除租户
  delete(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/iam/v1/tenants/${id}`)
  },

  // 启用租户
  enable(id: string): Promise<ApiResponse<void>> {
    return request.post(`/iam/v1/tenants/${id}/enable`)
  },

  // 禁用租户
  disable(id: string): Promise<ApiResponse<void>> {
    return request.post(`/iam/v1/tenants/${id}/disable`)
  },

  // 获取租户配额
  getQuota(id: string): Promise<ApiResponse<TenantQuotaInfo>> {
    return request.get(`/iam/v1/tenants/${id}/quota`)
  },

  // 更新租户配额
  updateQuota(id: string, data: TenantQuotaRequest): Promise<ApiResponse<void>> {
    return request.put(`/iam/v1/tenants/${id}/quota`, data)
  },

  // 获取当前租户
  getCurrent(): Promise<ApiResponse<TenantResponse>> {
    return request.get('/iam/v1/tenants/current')
  },
}

export default tenantApi
