import { request, type ApiResponse } from './request'

export interface RatelimitPolicy {
  id?: string
  name: string
  description?: string
  type: 'qps' | 'concurrency'
  dimension: 'global' | 'ip' | 'path' | 'ip_path' | 'user'
  limitValue: number
  timeWindow: number
  status?: string
  appliedRoutes?: number
  createdBy?: string
  createdAt?: string
  updatedAt?: string
}

export interface RatelimitPolicyQuery {
  page?: number
  size?: number
  keyword?: string
  status?: string
}

export interface ApplyPolicyRequest {
  routeIds: string[]
}

/**
 * 查询限流策略列表
 */
export function listRatelimitPolicies(params: RatelimitPolicyQuery): Promise<ApiResponse<any>> {
  return request.get('/v1/gateway/ratelimit/policies', { params })
}

/**
 * 查询限流策略详情
 */
export function getRatelimitPolicy(id: string): Promise<ApiResponse<any>> {
  return request.get(`/v1/gateway/ratelimit/policies/${id}`)
}

/**
 * 创建限流策略
 */
export function createRatelimitPolicy(data: RatelimitPolicy): Promise<ApiResponse<any>> {
  return request.post('/v1/gateway/ratelimit/policies', data)
}

/**
 * 更新限流策略
 */
export function updateRatelimitPolicy(id: string, data: RatelimitPolicy): Promise<ApiResponse<any>> {
  return request.put(`/v1/gateway/ratelimit/policies/${id}`, data)
}

/**
 * 删除限流策略
 */
export function deleteRatelimitPolicy(id: string): Promise<ApiResponse<any>> {
  return request.delete(`/v1/gateway/ratelimit/policies/${id}`)
}

/**
 * 应用限流策略到路由
 */
export function applyPolicyToRoutes(policyId: string, data: ApplyPolicyRequest): Promise<ApiResponse<any>> {
  return request.post(`/v1/gateway/ratelimit/policies/${policyId}/apply`, data)
}

/**
 * 移除路由的限流策略
 */
export function removePolicyFromRoute(policyId: string, routeId: string): Promise<ApiResponse<any>> {
  return request.delete(`/v1/gateway/ratelimit/policies/${policyId}/routes/${routeId}`)
}
