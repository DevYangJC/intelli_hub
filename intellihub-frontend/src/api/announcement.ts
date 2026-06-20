import { request, type ApiResponse } from './request'

export interface AnnouncementDTO {
  id?: string
  title: string
  description: string
  type: string
  meta?: string
  status?: string
  publishTime?: string
  createdAt?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

// 获取公告列表
export function getAnnouncementList(params: { page?: number; size?: number }): Promise<ApiResponse<PageResult<AnnouncementDTO>>> {
  return request.get('/platform/v1/announcements', { params })
}

// 获取已发布的公告
export function getPublishedAnnouncements(limit: number = 10): Promise<ApiResponse<AnnouncementDTO[]>> {
  return request.get('/platform/v1/announcements/published', { params: { limit } })
}

// 获取公告详情
export function getAnnouncementById(id: string): Promise<ApiResponse<AnnouncementDTO>> {
  return request.get(`/platform/v1/announcements/${id}`)
}

// 创建公告
export function createAnnouncement(data: AnnouncementDTO): Promise<ApiResponse<AnnouncementDTO>> {
  return request.post('/platform/v1/announcements', data)
}

// 更新公告
export function updateAnnouncement(id: string, data: AnnouncementDTO): Promise<ApiResponse<AnnouncementDTO>> {
  return request.put(`/platform/v1/announcements/${id}`, data)
}

// 发布公告
export function publishAnnouncement(id: string): Promise<ApiResponse<void>> {
  return request.post(`/platform/v1/announcements/${id}/publish`)
}

// 下线公告
export function unpublishAnnouncement(id: string): Promise<ApiResponse<void>> {
  return request.post(`/platform/v1/announcements/${id}/unpublish`)
}

// 删除公告
export function deleteAnnouncement(id: string): Promise<ApiResponse<void>> {
  return request.delete(`/platform/v1/announcements/${id}`)
}
