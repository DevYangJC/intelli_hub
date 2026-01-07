import request from './request'

// 事件定义
export interface EventDefinition {
  id?: string
  tenantId?: string
  eventCode: string
  eventName: string
  eventType: string
  description?: string
  schemaDefinition?: string
  status: string
  createdBy?: string
  createdAt?: string
  updatedAt?: string
}

// 事件订阅
export interface EventSubscription {
  id?: string
  tenantId?: string
  eventCode: string
  subscriberType: string
  subscriberName: string
  callbackUrl?: string
  callbackMethod?: string
  callbackHeaders?: string
  mqTopic?: string
  mqTag?: string
  filterExpression?: string
  retryStrategy: string
  maxRetryTimes: number
  timeoutSeconds: number
  status: string
  priority: number
  createdBy?: string
  createdAt?: string
  updatedAt?: string
}

// 事件发布记录
export interface EventPublishRecord {
  id: string
  tenantId: string
  eventCode: string
  eventId: string
  eventData: string
  source?: string
  publishTime: string
  status: string
  errorMessage?: string
  createdAt: string
}

// 事件消费记录
export interface EventConsumeRecord {
  id: string
  tenantId: string
  subscriptionId: string
  eventId: string
  eventCode: string
  eventData: string
  consumeTime: string
  status: string
  retryTimes: number
  nextRetryTime?: string
  responseCode?: number
  responseBody?: string
  errorMessage?: string
  costTime?: number
  createdAt: string
  updatedAt: string
}

// 事件统计
export interface EventStatistics {
  id: string
  tenantId: string
  eventCode: string
  statDate: string
  publishCount: number
  consumeCount: number
  successCount: number
  failedCount: number
  avgCostTime: number
  maxCostTime: number
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 事件类型选项
export const EVENT_TYPES = [
  { value: 'SYSTEM', label: '系统事件', color: '#409eff' },
  { value: 'BUSINESS', label: '业务事件', color: '#67c23a' },
  { value: 'CUSTOM', label: '自定义事件', color: '#e6a23c' }
]

// 订阅者类型选项
export const SUBSCRIBER_TYPES = [
  { value: 'WEBHOOK', label: 'Webhook回调', icon: 'Link' },
  { value: 'MQ', label: '消息队列', icon: 'Message' },
  { value: 'SERVICE', label: '内部服务', icon: 'Connection' }
]

// HTTP 方法选项
export const HTTP_METHODS = [
  { value: 'POST', label: 'POST' },
  { value: 'PUT', label: 'PUT' },
  { value: 'PATCH', label: 'PATCH' },
  { value: 'GET', label: 'GET' }
]

// 重试策略选项
export const RETRY_STRATEGIES = [
  { value: 'NONE', label: '不重试' },
  { value: 'FIXED', label: '固定间隔' },
  { value: 'EXPONENTIAL', label: '指数退避' }
]

// 状态选项
export const STATUS_OPTIONS = [
  { value: 'ACTIVE', label: '启用', type: 'success' },
  { value: 'INACTIVE', label: '禁用', type: 'danger' },
  { value: 'PAUSED', label: '暂停', type: 'warning' }
]

// 消费状态选项
export const CONSUME_STATUS = [
  { value: 'SUCCESS', label: '成功', type: 'success' },
  { value: 'FAILED', label: '失败', type: 'danger' },
  { value: 'RETRYING', label: '重试中', type: 'warning' },
  { value: 'PENDING', label: '待处理', type: 'info' }
]

// ==================== 事件定义 API ====================

/**
 * 获取事件定义列表
 */
export function getEventDefinitions(params?: {
  eventType?: string
  status?: string
  page?: number
  size?: number
}) {
  return request.get<PageResult<EventDefinition>>('/event/v1/event-definitions/list', { params })
}

/**
 * 获取事件定义详情
 */
export function getEventDefinition(id: string) {
  return request.get<EventDefinition>(`/event/v1/event-definitions/${id}`)
}

/**
 * 创建事件定义
 */
export function createEventDefinition(data: EventDefinition) {
  return request.post<string>('/event/v1/event-definitions/create', data)
}

/**
 * 更新事件定义
 */
export function updateEventDefinition(id: string, data: EventDefinition) {
  return request.post<void>(`/event/v1/event-definitions/${id}/update`, data)
}

/**
 * 删除事件定义
 */
export function deleteEventDefinition(id: string) {
  return request.post<void>(`/event/v1/event-definitions/${id}/delete`)
}

// ==================== 事件订阅 API ====================

/**
 * 获取事件订阅列表
 */
export function getEventSubscriptions(params?: {
  eventCode?: string
  status?: string
  page?: number
  size?: number
}) {
  return request.get<PageResult<EventSubscription>>('/event/v1/event-subscriptions/list', { params })
}

/**
 * 获取事件订阅详情
 */
export function getEventSubscription(id: string) {
  return request.get<EventSubscription>(`/event/v1/event-subscriptions/${id}`)
}

/**
 * 创建事件订阅
 */
export function createEventSubscription(data: EventSubscription) {
  return request.post<string>('/event/v1/event-subscriptions/create', data)
}

/**
 * 更新事件订阅
 */
export function updateEventSubscription(id: string, data: EventSubscription) {
  return request.post<void>(`/event/v1/event-subscriptions/${id}/update`, data)
}

/**
 * 删除事件订阅
 */
export function deleteEventSubscription(id: string) {
  return request.post<void>(`/event/v1/event-subscriptions/${id}/delete`)
}

/**
 * 暂停事件订阅
 */
export function pauseEventSubscription(id: string) {
  return request.post<void>(`/event/v1/event-subscriptions/${id}/pause`)
}

/**
 * 恢复事件订阅
 */
export function resumeEventSubscription(id: string) {
  return request.post<void>(`/event/v1/event-subscriptions/${id}/resume`)
}

// ==================== 事件记录 API ====================

/**
 * 获取事件发布记录
 */
export function getPublishRecords(params?: {
  eventCode?: string
  status?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}) {
  return request.get<PageResult<EventPublishRecord>>('/event/v1/records/publish', { params })
}

/**
 * 获取事件消费记录
 */
export function getConsumeRecords(params?: {
  eventCode?: string
  subscriptionId?: string
  status?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}) {
  return request.get<PageResult<EventConsumeRecord>>('/event/v1/records/consume', { params })
}

/**
 * 获取事件统计
 */
export function getEventStatistics(params?: {
  eventCode?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<EventStatistics[]>('/event/v1/statistics', { params })
}

/**
 * 发布事件（测试用）
 */
export function publishEvent(data: {
  eventCode: string
  source?: string
  data: Record<string, unknown>
}) {
  return request.post<string>('/event/v1/events/publish', data)
}
