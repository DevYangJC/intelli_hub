import request from './request'

// 获取所有配置
export function getAllSettings() {
  return request.get<Record<string, any>>('/platform/v1/settings')
}

// 获取单个配置
export function getSetting(key: string) {
  return request.get<string>(`/platform/v1/settings/${key}`)
}

// 批量保存配置
export function saveSettings(configs: Record<string, any>) {
  return request.post<void>('/platform/v1/settings', configs)
}

// 设置单个配置
export function setSetting(key: string, value: string, type?: string, description?: string) {
  return request.put<void>(`/platform/v1/settings/${key}`, { value, type, description })
}

// 删除配置
export function deleteSetting(key: string) {
  return request.delete<void>(`/platform/v1/settings/${key}`)
}
