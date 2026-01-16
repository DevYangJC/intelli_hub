import request from './request'

// API信息响应
export interface ApiInfoResponse {
  id: string
  tenantId: string
  groupId: string
  groupName: string
  name: string
  code: string
  version: string
  description: string
  method: string
  path: string
  protocol: string
  contentType: string
  status: string
  authType: string
  timeout: number
  retryCount: number
  cacheEnabled: boolean
  cacheTtl: number
  rateLimitEnabled: boolean
  rateLimitQps: number
  mockEnabled: boolean
  mockResponse: string
  todayCalls: number
  totalCalls: number
  createdBy: string
  creatorName: string
  publishedAt: string
  createdAt: string
  updatedAt: string
  requestParams?: ApiParamResponse[]
  backend?: ApiBackendResponse
  stats?: ApiStatsVO
}

// API统计数据VO
export interface ApiStatsVO {
  todayCalls: number
  totalCalls: number
  successCalls: number
  successRate: number
  avgResponseTime: number
}

// API参数响应
export interface ApiParamResponse {
  id: string
  apiId: string
  name: string
  type: string
  location: string
  required: boolean
  defaultValue: string
  example: string
  description: string
  sort: number
}

// API后端配置响应
export interface ApiBackendResponse {
  id: string
  apiId: string
  type: string
  protocol: string
  method: string
  host: string
  path: string
  timeout: number
  connectTimeout: number
}

// API分组响应
export interface ApiGroupResponse {
  id: string
  tenantId: string
  name: string
  code: string
  description: string
  sort: number
  status: string
  createdBy: string
  createdAt: string
  updatedAt: string
  apiCount: number
}

// API查询请求
export interface ApiQueryRequest {
  keyword?: string
  groupId?: string
  status?: string
  method?: string
  page?: number
  size?: number
}

// 创建API请求
export interface CreateApiRequest {
  groupId?: string
  name: string
  code: string
  version?: string
  description?: string
  method: string
  path: string
  protocol?: string
  contentType?: string
  authType?: string
  timeout?: number
  retryCount?: number
  cacheEnabled?: boolean
  cacheTtl?: number
  rateLimitEnabled?: boolean
  rateLimitQps?: number
  mockEnabled?: boolean
  mockResponse?: string
  requestParams?: ApiParamRequest[]
  backend?: ApiBackendRequest
}

// API参数请求
export interface ApiParamRequest {
  name: string
  type: string
  location: string
  required?: boolean
  defaultValue?: string
  example?: string
  description?: string
  sort?: number
}

// API后端配置请求
export interface ApiBackendRequest {
  type?: string
  protocol?: string
  method?: string
  host?: string
  path?: string
  timeout?: number
  connectTimeout?: number
}

// 更新API请求
export interface UpdateApiRequest {
  groupId?: string
  name?: string
  version?: string
  description?: string
  method?: string
  path?: string
  protocol?: string
  contentType?: string
  authType?: string
  timeout?: number
  retryCount?: number
  cacheEnabled?: boolean
  cacheTtl?: number
  rateLimitEnabled?: boolean
  rateLimitQps?: number
  mockEnabled?: boolean
  mockResponse?: string
  requestParams?: ApiParamRequest[]
  backend?: ApiBackendRequest
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
  success?: boolean
}

// API版本响应
export interface ApiVersionResponse {
  id: string
  apiId: string
  version: string
  snapshot: string
  changeLog: string
  createdBy: string
  createdAt: string
}

// 创建版本请求
export interface CreateApiVersionRequest {
  version: string
  changeLog?: string
}

// API管理接口
export const apiManageApi = {
  // 获取API列表
  list(params?: ApiQueryRequest): Promise<ApiResponse<PageData<ApiInfoResponse>>> {
    return request.get('/v1/apis/list', { params })
  },

  // 获取API详情
  getById(id: string): Promise<ApiResponse<ApiInfoResponse>> {
    return request.get(`/v1/apis/${id}/detail`)
  },

  // 创建API
  create(data: CreateApiRequest): Promise<ApiResponse<ApiInfoResponse>> {
    return request.post('/v1/apis/create', data)
  },

  // 更新API
  update(id: string, data: UpdateApiRequest): Promise<ApiResponse<ApiInfoResponse>> {
    return request.post(`/v1/apis/${id}/update`, data)
  },

  // 删除API
  delete(id: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${id}/delete`)
  },

  // 发布API
  publish(id: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${id}/publish`)
  },

  // 下线API
  offline(id: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${id}/offline`)
  },

  // 废弃API
  deprecate(id: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${id}/deprecate`)
  },

  // 复制API
  copy(id: string): Promise<ApiResponse<ApiInfoResponse>> {
    return request.post(`/v1/apis/${id}/copy`)
  },
}

// API分组接口
export const apiGroupApi = {
  // 获取分组列表
  list(): Promise<ApiResponse<ApiGroupResponse[]>> {
    return request.get('/v1/api-groups/list')
  },

  // 获取分组详情
  getById(id: string): Promise<ApiResponse<ApiGroupResponse>> {
    return request.get(`/v1/api-groups/${id}/detail`)
  },

  // 创建分组
  create(data: { name: string; code: string; description?: string }): Promise<ApiResponse<ApiGroupResponse>> {
    return request.post('/v1/api-groups/create', data)
  },

  // 更新分组
  update(id: string, data: { name?: string; description?: string; sort?: number }): Promise<ApiResponse<ApiGroupResponse>> {
    return request.post(`/v1/api-groups/${id}/update`, data)
  },

  // 删除分组
  delete(id: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/api-groups/${id}/delete`)
  },
}

// API参数接口
export const apiParamApi = {
  // 获取请求参数列表
  listRequestParams(apiId: string): Promise<ApiResponse<ApiParamResponse[]>> {
    return request.get(`/v1/apis/${apiId}/params/request/list`)
  },

  // 获取响应参数列表
  listResponseParams(apiId: string): Promise<ApiResponse<ApiParamResponse[]>> {
    return request.get(`/v1/apis/${apiId}/params/response/list`)
  },

  // 批量保存请求参数
  saveRequestParams(apiId: string, params: ApiParamRequest[]): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${apiId}/params/request/batch-save`, params)
  },

  // 批量保存响应参数
  saveResponseParams(apiId: string, params: ApiParamRequest[]): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${apiId}/params/response/batch-save`, params)
  },

  // 添加请求参数
  addRequestParam(apiId: string, param: ApiParamRequest): Promise<ApiResponse<ApiParamResponse>> {
    return request.post(`/v1/apis/${apiId}/params/request/add`, param)
  },

  // 添加响应参数
  addResponseParam(apiId: string, param: ApiParamRequest): Promise<ApiResponse<ApiParamResponse>> {
    return request.post(`/v1/apis/${apiId}/params/response/add`, param)
  },

  // 更新请求参数
  updateRequestParam(apiId: string, paramId: string, param: ApiParamRequest): Promise<ApiResponse<ApiParamResponse>> {
    return request.post(`/v1/apis/${apiId}/params/request/${paramId}/update`, param)
  },

  // 更新响应参数
  updateResponseParam(apiId: string, paramId: string, param: ApiParamRequest): Promise<ApiResponse<ApiParamResponse>> {
    return request.post(`/v1/apis/${apiId}/params/response/${paramId}/update`, param)
  },

  // 删除请求参数
  deleteRequestParam(apiId: string, paramId: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${apiId}/params/request/${paramId}/delete`)
  },

  // 删除响应参数
  deleteResponseParam(apiId: string, paramId: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${apiId}/params/response/${paramId}/delete`)
  },
}

// API版本接口
export const apiVersionApi = {
  // 获取版本列表
  list(apiId: string): Promise<ApiResponse<ApiVersionResponse[]>> {
    return request.get(`/v1/apis/${apiId}/versions/list`)
  },

  // 获取版本详情
  getById(apiId: string, versionId: string): Promise<ApiResponse<ApiVersionResponse>> {
    return request.get(`/v1/apis/${apiId}/versions/${versionId}/detail`)
  },

  // 创建版本
  create(apiId: string, data: CreateApiVersionRequest): Promise<ApiResponse<ApiVersionResponse>> {
    return request.post(`/v1/apis/${apiId}/versions/create`, data)
  },

  // 回滚到指定版本
  rollback(apiId: string, versionId: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${apiId}/versions/${versionId}/rollback`)
  },

  // 删除版本
  delete(apiId: string, versionId: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${apiId}/versions/${versionId}/delete`)
  },

  // 比较版本差异
  compare(apiId: string, versionId1: string, versionId2: string): Promise<ApiResponse<string>> {
    return request.get(`/v1/apis/${apiId}/versions/compare`, { params: { versionId1, versionId2 } })
  },
}

// 公开API接口（API市场）
export const publicApiApi = {
  // 获取公开API列表（跨租户）
  list(params?: ApiQueryRequest): Promise<ApiResponse<PageData<ApiInfoResponse>>> {
    return request.get('/v1/public/apis/list', { params })
  },

  // 获取公开API详情
  getById(id: string): Promise<ApiResponse<ApiInfoResponse>> {
    return request.get(`/v1/public/apis/${id}/detail`)
  },
}

// API后端配置接口
export const apiBackendApi = {
  // 获取后端配置
  getByApiId(apiId: string): Promise<ApiResponse<ApiBackendResponse>> {
    return request.get(`/v1/apis/${apiId}/backend/detail`)
  },

  // 保存后端配置
  save(apiId: string, data: ApiBackendRequest): Promise<ApiResponse<ApiBackendResponse>> {
    return request.post(`/v1/apis/${apiId}/backend/save`, data)
  },

  // 删除后端配置
  delete(apiId: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/apis/${apiId}/backend/delete`)
  },

  // 测试连接
  testConnection(apiId: string, data: ApiBackendRequest): Promise<ApiResponse<boolean>> {
    return request.post(`/v1/apis/${apiId}/backend/test-connection`, data)
  },
}
