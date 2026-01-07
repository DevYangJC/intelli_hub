import request from './request'

// ==================== 类型定义 ====================

// 文本生成请求
export interface TextGenerationRequest {
  prompt: string
  model?: string
  maxTokens?: number
  temperature?: number
  topP?: number
}

// 文本生成响应
export interface TextGenerationResponse {
  text: string
  tokensUsed: number
  model: string
  requestId: string
  duration: number
  finishReason: string
}

// 对话请求
export interface ChatRequest {
  message: string
  model: string
  provider?: string  // aliyunQwenProvider / baiduErnieProvider / tencentHunyuanProvider
  conversationId?: string
  systemPrompt?: string
  maxTokens?: number
  temperature?: number
  stream?: boolean
  history?: ChatMessage[]
}

// 对话消息
export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

// 对话响应
export interface ChatResponse {
  message: string
  conversationId: string
  tokensUsed: number
  model: string
  requestId: string
  duration: number
  finishReason: string
}

// 配额使用情况
export interface QuotaUsage {
  tenantId: string
  dailyQuota: number
  usedQuota: number
  remainingQuota: number
  usagePercent: number
  totalCost: number
  resetTime: string
}

// 对话历史
export interface ConversationHistory {
  id: string
  conversationId: string
  role: string
  content: string
  tokens: number
  createdAt: string
}

// Prompt模板
export interface PromptTemplate {
  id?: string
  tenantId?: string
  name: string
  code: string
  type: string
  content: string
  description?: string
  variables?: string
  usageCount?: number
  createdAt?: string
  updatedAt?: string
}

// 统计数据
export interface AigcStatistics {
  totalRequests: number
  successRequests: number
  failedRequests: number
  totalTokens: number
  totalCost: number
  avgLatency: number
  successRate: number
}

// 模型排行
export interface ModelRanking {
  model: string
  count: number
  tokens: number
  cost: number
}

// 成本概览
export interface CostOverview {
  totalCost: number
  todayCost: number
  monthCost: number
  avgDailyCost: number
}

// 成本预测
export interface CostForecast {
  currentCost: number
  forecastCost: number
  daysRemaining: number
  avgDailyCost: number
}

// AI模型详细信息
export interface ModelInfo {
  id: string
  name: string
  provider: string  // aliyun / baidu / tencent
  providerName: string
  description: string
  maxContextLength: number
  supportStream: boolean
  pricePerThousandTokens: number
}

// ==================== 基础生成接口 ====================

/**
 * 文本生成
 */
export function generateText(data: TextGenerationRequest) {
  return request.post<TextGenerationResponse>('/aigc/v1/aigc/text/generate', data)
}

/**
 * 对话聊天
 */
export function chat(data: ChatRequest) {
  return request.post<ChatResponse>('/aigc/v1/aigc/chat/completions', data)
}

/**
 * 获取支持的模型列表（简单版）
 */
export function getSupportedModels() {
  return request.get<string[]>('/aigc/v1/aigc/models')
}

/**
 * 获取模型详细信息列表
 */
export function getModelInfoList() {
  return request.get<ModelInfo[]>('/aigc/v1/aigc/models/info')
}

// ==================== 流式接口 ====================

/**
 * 流式文本生成
 * @returns EventSource URL
 */
export function getStreamTextUrl() {
  return '/api/aigc/v1/aigc/stream/text/generate'
}

/**
 * 流式对话
 * @returns EventSource URL
 */
export function getStreamChatUrl() {
  return '/api/aigc/v1/aigc/stream/chat/completions'
}

/**
 * 创建流式对话请求
 */
export async function streamChat(
  data: ChatRequest,
  onMessage: (text: string) => void,
  onError?: (error: Error) => void,
  onComplete?: () => void
): Promise<void> {
  try {
    const response = await fetch('/api/aigc/v1/aigc/stream/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`,
        'X-Tenant-Id': localStorage.getItem('tenantId') || ''
      },
      body: JSON.stringify(data)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('Response body is null')
    }

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        onComplete?.()
        break
      }

      const chunk = decoder.decode(value, { stream: true })
      // 解析SSE格式
      const lines = chunk.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data && data !== '[DONE]') {
            try {
              const json = JSON.parse(data)
              if (json.content) {
                onMessage(json.content)
              }
            } catch {
              // 非JSON格式，直接使用文本
              onMessage(data)
            }
          }
        }
      }
    }
  } catch (error) {
    onError?.(error as Error)
  }
}

// ==================== 配额接口 ====================

/**
 * 获取配额使用情况
 */
export function getQuotaUsage() {
  return request.get<QuotaUsage>('/aigc/v1/aigc/quota/usage')
}

// ==================== 对话历史接口 ====================

/**
 * 获取对话历史
 */
export function getConversationHistory(conversationId: string, limit: number = 20) {
  return request.get<ConversationHistory[]>(
    `/aigc/v1/aigc/conversation/${conversationId}/history`,
    { params: { limit } }
  )
}

/**
 * 清空对话历史
 */
export function clearConversationHistory(conversationId: string) {
  return request.delete(`/aigc/v1/aigc/conversation/${conversationId}/history`)
}

// ==================== 模板管理接口 ====================

/**
 * 创建模板
 */
export function createTemplate(data: PromptTemplate) {
  return request.post<string>('/aigc/v1/aigc/templates', data)
}

/**
 * 更新模板
 */
export function updateTemplate(id: string, data: PromptTemplate) {
  return request.put(`/aigc/v1/aigc/templates/${id}`, data)
}

/**
 * 删除模板
 */
export function deleteTemplate(id: string) {
  return request.delete(`/aigc/v1/aigc/templates/${id}`)
}

/**
 * 获取模板详情
 */
export function getTemplate(id: string) {
  return request.get<PromptTemplate>(`/aigc/v1/aigc/templates/${id}`)
}

/**
 * 获取模板列表
 */
export function getTemplateList(type?: string) {
  return request.get<PromptTemplate[]>('/aigc/v1/aigc/templates', {
    params: type ? { type } : {}
  })
}

/**
 * 渲染模板
 */
export function renderTemplate(id: string, variables: Record<string, unknown>) {
  return request.post<string>(`/aigc/v1/aigc/templates/${id}/render`, variables)
}

/**
 * 根据代码渲染模板
 */
export function renderTemplateByCode(code: string, variables: Record<string, unknown>) {
  return request.post<string>(`/aigc/v1/aigc/templates/render/${code}`, variables)
}

// ==================== 统计Dashboard接口 ====================

/**
 * 获取租户统计
 */
export function getTenantStatistics(days: number = 7) {
  return request.get<AigcStatistics>('/aigc/v1/aigc/dashboard/statistics', {
    params: { days }
  })
}

/**
 * 获取模型排行
 */
export function getModelRanking(limit: number = 10) {
  return request.get<{ ranking: ModelRanking[] }>('/aigc/v1/aigc/dashboard/model-ranking', {
    params: { limit }
  })
}

/**
 * 获取每日趋势
 */
export function getDailyTrend(days: number = 7) {
  return request.get<{
    dates: string[]
    counts: number[]
    tokens: number[]
    costs: number[]
  }>('/aigc/v1/aigc/dashboard/daily-trend', {
    params: { days }
  })
}

/**
 * 获取实时概览
 */
export function getRealTimeOverview() {
  return request.get<{
    todayRequests: number
    todayTokens: number
    todayCost: number
    hourRequests: number
  }>('/aigc/v1/aigc/dashboard/overview')
}

/**
 * 获取用户排行
 */
export function getUserRanking(limit: number = 10) {
  return request.get<{
    ranking: Array<{
      userId: string
      username: string
      count: number
      tokens: number
    }>
  }>('/aigc/v1/aigc/dashboard/user-ranking', {
    params: { limit }
  })
}

// ==================== 成本分析接口 ====================

/**
 * 获取成本概览
 */
export function getCostOverview(days: number = 7) {
  return request.get<CostOverview>('/aigc/v1/aigc/cost/overview', {
    params: { days }
  })
}

/**
 * 按模型分析成本
 */
export function getCostByModel(days: number = 30) {
  return request.get<{
    models: Array<{
      model: string
      cost: number
      percent: number
    }>
  }>('/aigc/v1/aigc/cost/by-model', {
    params: { days }
  })
}

/**
 * 按日期分析成本
 */
export function getCostByDate(days: number = 30) {
  return request.get<{
    dates: string[]
    costs: number[]
  }>('/aigc/v1/aigc/cost/by-date', {
    params: { days }
  })
}

/**
 * 获取成本预测
 */
export function getCostForecast() {
  return request.get<CostForecast>('/aigc/v1/aigc/cost/forecast')
}

/**
 * 导出成本报表
 */
export function exportCostReport(startDate: string, endDate: string) {
  return request.get<{
    data: Array<{
      date: string
      model: string
      requests: number
      tokens: number
      cost: number
    }>
    summary: {
      totalRequests: number
      totalTokens: number
      totalCost: number
    }
  }>('/aigc/v1/aigc/cost/export', {
    params: { startDate, endDate }
  })
}

// ==================== 监控接口 ====================

/**
 * 获取性能统计
 */
export function getMonitorStats() {
  return request.get<{
    models: Record<string, {
      requestCount: number
      successCount: number
      failureCount: number
      avgTime: string
      successRate: string
      totalTokens: number
    }>
  }>('/aigc/v1/aigc/monitor/stats')
}
