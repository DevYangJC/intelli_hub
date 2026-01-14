import { request } from './request'

// 问答请求参数
export interface QaRequest {
  question: string
  history?: ChatHistory[]
  apiId?: string
  questionType?: 'usage' | 'error' | 'best_practice' | 'general'
}

// 对话历史
export interface ChatHistory {
  role: 'user' | 'assistant'
  content: string
}

// 问答响应
export interface QaResponse {
  answer: string
  relatedApis?: RelatedApi[]
  codeExample?: string
  references?: string[]
  suggestedQuestions?: string[]
  confidence?: number
  model?: string
  tokensUsed?: number
  responseTimeMs?: number
}

// 相关 API
export interface RelatedApi {
  id: string
  name: string
  path: string
  method: string
  description: string
}

// API 响应包装
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/**
 * 智能问答
 */
export function askQuestion(data: QaRequest): Promise<ApiResponse<QaResponse>> {
  return request.post('/v1/aigc/qa/ask', data)
}

/**
 * 快速提问
 */
export function quickAsk(question: string, questionType?: string): Promise<ApiResponse<QaResponse>> {
  const params = new URLSearchParams({ question })
  if (questionType) {
    params.append('questionType', questionType)
  }
  return request.get(`/v1/aigc/qa/quick?${params.toString()}`)
}
