import request from './request'

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
export function listRatelimitPolicies(params: RatelimitPolicyQuery) {
  return request({
    url: '/v1/gateway/ratelimit/policies',
    method: 'get',
    params
  })
}

/**
 * 查询限流策略详情
 */
export function getRatelimitPolicy(id: string) {
  return request({
    url: `/v1/gateway/ratelimit/policies/${id}`,
    method: 'get'
  })
}

/**
 * 创建限流策略
 */
export function createRatelimitPolicy(data: RatelimitPolicy) {
  return request({
    url: '/v1/gateway/ratelimit/policies',
    method: 'post',
    data
  })
}

/**
 * 更新限流策略
 */
export function updateRatelimitPolicy(id: string, data: RatelimitPolicy) {
  return request({
    url: `/v1/gateway/ratelimit/policies/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除限流策略
 */
export function deleteRatelimitPolicy(id: string) {
  return request({
    url: `/v1/gateway/ratelimit/policies/${id}`,
    method: 'delete'
  })
}

/**
 * 应用限流策略到路由
 */
export function applyPolicyToRoutes(policyId: string, data: ApplyPolicyRequest) {
  return request({
    url: `/v1/gateway/ratelimit/policies/${policyId}/apply`,
    method: 'post',
    data
  })
}

/**
 * 移除路由的限流策略
 */
export function removePolicyFromRoute(policyId: string, routeId: string) {
  return request({
    url: `/v1/gateway/ratelimit/policies/${policyId}/routes/${routeId}`,
    method: 'delete'
  })
}
