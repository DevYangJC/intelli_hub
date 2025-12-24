import request from './request'

// 告警规则
export interface AlertRule {
  id?: number
  tenantId?: string
  name: string
  ruleType: string
  apiId?: string
  apiPath?: string
  threshold: number
  operator: string
  duration: number
  notifyChannels?: string
  notifyTargets?: string
  status: string
  createdBy?: string
  createdAt?: string
  updatedAt?: string
}

// 告警记录
export interface AlertRecord {
  id: number
  tenantId: string
  ruleId: number
  ruleName: string
  apiId?: string
  apiPath?: string
  alertLevel: string
  alertMessage: string
  currentValue: number
  thresholdValue: number
  status: string
  firedAt: string
  resolvedAt?: string
  notified: boolean
  createdAt: string
}

// 告警统计
export interface AlertStats {
  total: number
  firing: number
  resolved: number
  critical: number
  warning: number
  info: number
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 规则类型选项
export const RULE_TYPES = [
  { value: 'error_rate', label: '错误率', unit: '%' },
  { value: 'latency', label: '平均延迟', unit: 'ms' },
  { value: 'qps', label: 'QPS', unit: '/s' }
]

// 比较运算符选项
export const OPERATORS = [
  { value: 'gt', label: '大于 (>)' },
  { value: 'gte', label: '大于等于 (>=)' },
  { value: 'lt', label: '小于 (<)' },
  { value: 'lte', label: '小于等于 (<=)' },
  { value: 'eq', label: '等于 (=)' }
]

// 告警级别
export const ALERT_LEVELS = [
  { value: 'critical', label: '严重', color: '#f56c6c' },
  { value: 'warning', label: '警告', color: '#e6a23c' },
  { value: 'info', label: '信息', color: '#409eff' }
]

// 通知渠道选项
export const NOTIFY_CHANNELS = [
  { value: 'email', label: '邮件' },
  { value: 'webhook', label: 'Webhook' },
  { value: 'dingtalk', label: '钉钉' },
  { value: 'kafka', label: 'Kafka' }
]

/**
 * 获取告警规则列表
 */
export function getAlertRules(params?: {
  ruleType?: string
  status?: string
  pageNum?: number
  pageSize?: number
}) {
  return request.get<PageResult<AlertRule>>('/governance/api/v1/alert/rules', { params })
}

/**
 * 获取告警规则详情
 */
export function getAlertRule(id: number) {
  return request.get<AlertRule>(`/governance/api/v1/alert/rules/${id}`)
}

/**
 * 创建告警规则
 */
export function createAlertRule(data: AlertRule) {
  return request.post<AlertRule>('/governance/api/v1/alert/rules', data)
}

/**
 * 更新告警规则
 */
export function updateAlertRule(id: number, data: AlertRule) {
  return request.put<AlertRule>(`/governance/api/v1/alert/rules/${id}`, data)
}

/**
 * 删除告警规则
 */
export function deleteAlertRule(id: number) {
  return request.delete(`/governance/api/v1/alert/rules/${id}`)
}

/**
 * 启用告警规则
 */
export function enableAlertRule(id: number) {
  return request.post(`/governance/api/v1/alert/rules/${id}/enable`)
}

/**
 * 禁用告警规则
 */
export function disableAlertRule(id: number) {
  return request.post(`/governance/api/v1/alert/rules/${id}/disable`)
}

/**
 * 获取告警记录列表
 */
export function getAlertRecords(params?: {
  status?: string
  alertLevel?: string
  ruleId?: number
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}) {
  return request.get<PageResult<AlertRecord>>('/governance/api/v1/alert/records', { params })
}

/**
 * 获取告警统计
 */
export function getAlertStats(params?: {
  startTime?: string
  endTime?: string
}) {
  return request.get<AlertStats>('/governance/api/v1/alert/records/stats', { params })
}

/**
 * 手动恢复告警
 */
export function resolveAlert(id: number) {
  return request.post(`/governance/api/v1/alert/records/${id}/resolve`)
}
