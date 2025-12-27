import request from './request'

// 统计概览
export interface StatsOverview {
  todayTotalCount: number
  todaySuccessCount: number
  todayFailCount: number
  todaySuccessRate: number
  todayAvgLatency: number
  yesterdayTotalCount: number
  dayOverDayRate: number
  currentQps: number
  apiCount?: number
  appCount?: number
}

// 统计趋势
export interface StatsTrend {
  timePoints: string[]
  totalCounts: number[]
  successCounts: number[]
  failCounts: number[]
  avgLatencies: number[]
  successRates: number[]
}

// 调用日志
export interface CallLog {
  id: number | string  // Long ID 可能是 string
  tenantId: string
  apiId: string
  apiPath: string
  apiMethod: string
  appId: string
  appKey: string
  clientIp: string
  statusCode: number
  success: boolean
  latency: number
  requestTime: string
  errorMessage: string
  userAgent: string
  requestBody?: string   // 请求体（可选）
  responseBody?: string  // 响应体（可选）
}

// Top API统计
export interface TopApiStats {
  id: number | string  // Long ID 可能是 string
  tenantId: string
  apiId: string
  apiPath: string
  appId: string
  statDate: string
  totalCount: number
  successCount: number
  failCount: number
  avgLatency: number
  maxLatency: number
  minLatency: number
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 获取统计概览
 */
export function getStatsOverview() {
  return request.get<StatsOverview>('/governance/v1/stats/overview')
}

/**
 * 获取小时趋势
 */
export function getHourlyTrend(startTime: string, endTime: string) {
  return request.get<StatsTrend>('/governance/v1/stats/trend/hourly', {
    params: { startTime, endTime }
  })
}

/**
 * 获取天趋势
 */
export function getDailyTrend(startDate: string, endDate: string) {
  return request.get<StatsTrend>('/governance/v1/stats/trend/daily', {
    params: { startDate, endDate }
  })
}

/**
 * 获取单个API趋势
 */
export function getApiTrend(apiPath: string, startTime: string, endTime: string) {
  // 将路径中的/替换为_
  const encodedPath = apiPath.replace(/^\//, '').replace(/\//g, '_')
  return request.get<StatsTrend>(`/governance/v1/stats/api/${encodedPath}`, {
    params: { startTime, endTime }
  })
}

/**
 * 获取Top N API
 */
export function getTopApis(limit: number = 10) {
  return request.get<TopApiStats[]>('/governance/v1/stats/top', {
    params: { limit }
  })
}

/**
 * 获取调用日志列表
 */
export function getCallLogs(params: {
  apiId?: string
  appId?: string
  startTime?: string
  endTime?: string
  success?: boolean
  page?: number
  size?: number
}) {
  return request.get<PageResult<CallLog>>('/governance/v1/stats/logs', { params })
}

/**
 * 获取实时调用数
 */
export function getRealtimeCount() {
  return request.get<number>('/governance/v1/stats/realtime')
}
