import { request, type ApiResponse } from './request'

// 获取所有配置
export function getAllSettings(): Promise<ApiResponse<Record<string, any>>> {
  return request.get('/platform/v1/settings')
}

// 获取单个配置
export function getSetting(key: string): Promise<ApiResponse<string>> {
  return request.get(`/platform/v1/settings/${key}`)
}

// 批量保存配置
export function saveSettings(configs: Record<string, any>): Promise<ApiResponse<void>> {
  return request.post('/platform/v1/settings', configs)
}

// 设置单个配置
export function setSetting(key: string, value: string, type?: string, description?: string): Promise<ApiResponse<void>> {
  return request.put(`/platform/v1/settings/${key}`, { value, type, description })
}

// 删除配置
export function deleteSetting(key: string): Promise<ApiResponse<void>> {
  return request.delete(`/platform/v1/settings/${key}`)
}
