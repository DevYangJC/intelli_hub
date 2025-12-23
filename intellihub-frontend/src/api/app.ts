import request from './request'

/**
 * 应用信息响应
 */
export interface AppInfoResponse {
  id: string
  tenantId: string
  name: string
  code: string
  description: string
  appType: string
  appKey: string
  status: string
  quotaLimit: number
  quotaUsed: number
  quotaUsagePercent: number
  callbackUrl: string
  ipWhitelist: string
  expireTime: string
  contactName: string
  contactEmail: string
  contactPhone: string
  subscribedApiCount: number
  createdByName: string
  createdAt: string
  updatedAt: string
}

/**
 * 应用创建响应（包含AppSecret）
 */
export interface AppCreateResponse {
  id: string
  name: string
  code: string
  appKey: string
  appSecret: string
  status: string
  quotaLimit: number
  expireTime: string
  createdAt: string
}

/**
 * 应用订阅响应
 */
export interface AppSubscriptionResponse {
  id: string
  appId: string
  apiId: string
  apiName: string
  apiPath: string
  status: string
  quotaLimit: number
  effectiveTime: string
  expireTime: string
  createdAt: string
}

/**
 * 创建应用请求
 */
export interface CreateAppRequest {
  name: string
  code: string
  description?: string
  appType?: string
  quotaLimit?: number
  callbackUrl?: string
  ipWhitelist?: string
  expireTime?: string
  contactName?: string
  contactEmail?: string
  contactPhone?: string
}

/**
 * 更新应用请求
 */
export interface UpdateAppRequest {
  name?: string
  description?: string
  appType?: string
  quotaLimit?: number
  callbackUrl?: string
  ipWhitelist?: string
  expireTime?: string
  contactName?: string
  contactEmail?: string
  contactPhone?: string
}

/**
 * 应用查询请求
 */
export interface AppQueryRequest {
  name?: string
  code?: string
  appType?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 订阅API请求
 */
export interface SubscribeApiRequest {
  apiId: string
  quotaLimit?: number
  expireTime?: string
}

/**
 * 应用管理API
 */
export const appApi = {
  /**
   * 获取应用列表
   */
  list(params: AppQueryRequest) {
    return request.get<any>('/app-center/v1/apps/list', { params })
  },

  /**
   * 获取应用详情
   */
  getById(id: string) {
    return request.get<any>(`/app-center/v1/apps/${id}/detail`)
  },

  /**
   * 根据AppKey获取应用
   */
  getByAppKey(appKey: string) {
    return request.get<any>('/app-center/v1/apps/by-appkey', { params: { appKey } })
  },

  /**
   * 创建应用
   */
  create(data: CreateAppRequest) {
    return request.post<any>('/app-center/v1/apps/create', data)
  },

  /**
   * 更新应用
   */
  update(id: string, data: UpdateAppRequest) {
    return request.post<any>(`/app-center/v1/apps/${id}/update`, data)
  },

  /**
   * 删除应用
   */
  delete(id: string) {
    return request.post<any>(`/app-center/v1/apps/${id}/delete`)
  },

  /**
   * 启用应用
   */
  enable(id: string) {
    return request.post<any>(`/app-center/v1/apps/${id}/enable`)
  },

  /**
   * 禁用应用
   */
  disable(id: string) {
    return request.post<any>(`/app-center/v1/apps/${id}/disable`)
  },

  /**
   * 重置AppSecret
   */
  resetSecret(id: string) {
    return request.post<any>(`/app-center/v1/apps/${id}/reset-secret`)
  },

  /**
   * 订阅API
   */
  subscribe(id: string, data: SubscribeApiRequest) {
    return request.post<any>(`/app-center/v1/apps/${id}/subscribe`, data)
  },

  /**
   * 取消订阅API
   */
  unsubscribe(id: string, apiId: string) {
    return request.post<any>(`/app-center/v1/apps/${id}/unsubscribe`, null, { params: { apiId } })
  },

  /**
   * 获取订阅列表
   */
  listSubscriptions(id: string) {
    return request.get<any>(`/app-center/v1/apps/${id}/subscriptions`)
  },
}

export default appApi
