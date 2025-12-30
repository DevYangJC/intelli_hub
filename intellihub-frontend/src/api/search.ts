import request from './request'

// 搜索类型
export type SearchType = 'api' | 'app' | 'user' | 'audit' | 'alert'

// 聚合搜索请求
export interface AggregateSearchRequest {
  keyword: string
  types?: SearchType[]
  filters?: Record<string, any>
  page?: number
  size?: number
  highlight?: boolean
}

// 搜索项
export interface SearchItem {
  type: SearchType
  id: string
  name: string
  description: string
  score: number
  highlights?: Record<string, string[]>
  data: any
}

// 聚合搜索响应
export interface AggregateSearchResponse {
  total: number
  items: SearchItem[]
  facets?: Record<string, Record<string, number>>
  page: number
  size: number
  totalPages: number
  took: number
}

// API 搜索结果
export interface ApiSearchResult {
  id: string
  name: string
  code: string
  path: string
  method: string
  protocol: string
  description: string
  groupId: string
  groupName: string
  status: string
  statusName: string
  version: string
  tenantId: string
  createdBy: string
  creatorName: string
  createdAt: string
  updatedAt: string
}

// 应用搜索结果
export interface AppSearchResult {
  id: string
  name: string
  code: string
  description: string
  appType: string
  appTypeName: string
  appKey: string
  status: string
  statusName: string
  contactName: string
  contactEmail: string
  tenantId: string
  createdBy: string
  createdByName: string
  createdAt: string
  updatedAt: string
}

// 用户搜索结果
export interface UserSearchResult {
  id: string
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: string
  statusName: string
  tenantId: string
  lastLoginAt: string
  createdAt: string
  updatedAt: string
}

// 搜索响应包装
export interface SearchResponse<T> {
  total: number
  hits: Array<{
    id: string
    index: string
    score: number
    source: T
    highlights?: Record<string, string[]>
  }>
  page: number
  size: number
  took: number
}

// API 响应包装
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

// ES 健康状态
export interface EsHealthStatus {
  status: 'UP' | 'DOWN'
  elasticsearch: 'UP' | 'DOWN'
  indices?: Record<string, {
    exists: boolean
    status: string
    error?: string
  }>
  timestamp: number
  error?: string
}

// 索引统计
export interface IndicesStats {
  [indexName: string]: {
    exists: boolean
    docCount?: number
    error?: string
  }
}

// 搜索 API
export const searchApi = {
  // 聚合搜索
  aggregateSearch(data: AggregateSearchRequest): Promise<ApiResponse<AggregateSearchResponse>> {
    return request.post('/v1/search/aggregate', data)
  },

  // 搜索 API
  searchApis(params: {
    keyword?: string
    status?: string
    groupId?: string
    page?: number
    size?: number
    highlight?: boolean
  }): Promise<ApiResponse<SearchResponse<ApiSearchResult>>> {
    return request.get('/v1/search/api', { params })
  },

  // 获取单个 API
  getApi(id: string): Promise<ApiResponse<ApiSearchResult>> {
    return request.get(`/v1/search/api/${id}`)
  },

  // ES 健康检查
  healthCheck(): Promise<ApiResponse<EsHealthStatus>> {
    return request.get('/v1/search/health')
  },

  // 获取索引统计
  getIndicesStats(): Promise<ApiResponse<IndicesStats>> {
    return request.get('/v1/search/health/indices')
  },

  // 触发全量同步
  triggerFullSync(): Promise<ApiResponse<string>> {
    return request.post('/v1/search/sync/full')
  },

  // 触发 API 全量同步
  triggerApiFullSync(): Promise<ApiResponse<string>> {
    return request.post('/v1/search/sync/api/full')
  },

  // 触发 API 增量同步
  triggerApiIncrementalSync(): Promise<ApiResponse<string>> {
    return request.post('/v1/search/sync/api/incremental')
  },

  // 触发应用全量同步
  triggerAppFullSync(): Promise<ApiResponse<string>> {
    return request.post('/v1/search/sync/app/full')
  },

  // 触发用户全量同步
  triggerUserFullSync(): Promise<ApiResponse<string>> {
    return request.post('/v1/search/sync/user/full')
  },
}

export default searchApi
