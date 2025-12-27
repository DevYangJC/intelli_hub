import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { el } from 'element-plus/es/locales.mjs'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 优先从localStorage获取token，确保在store初始化前也能获取到
    const token = localStorage.getItem('token')

    // 添加Token到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 添加租户ID、用户ID、用户名到请求头（如果存在）
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      try {
        const user = JSON.parse(storedUser)
        if (user.tenantId) {
          config.headers['X-Tenant-Id'] = user.tenantId
        }
        if (user.id) {
          config.headers['X-User-Id'] = user.id
        }
        if (user.username) {
          config.headers['X-Username'] = encodeURIComponent(user.username)
        }
      } catch {
        // ignore
      }
    }

    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 清除认证信息的辅助函数
const clearAuthAndRedirect = () => {
  // 1. 清除 LocalStorage
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  
  // 2. 触发未授权事件，让 App.vue 处理 UI 状态更新和跳转
  // 这样可以避免在 request.ts 中引入 store 和 router 导致的循环依赖问题
  window.dispatchEvent(new CustomEvent('auth:unauthorized'))
}

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 如果响应码不是200，说明有错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // Token无效或过期
      if (res.code === 401 || res.code === 4105 || res.code === 4106 || res.code === 4003) {
        clearAuthAndRedirect()
      } else if (res.message === "Token无效或已过期") {
        // 租户无效或过期
        clearAuthAndRedirect()
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    console.error('响应错误:', error)

    if (error.response) {
      const status = error.response.status

      switch (status) {
        case 401:
          // 认证失败时只显示错误信息，不清除token
          // 可能是后端服务暂时不可用导致的401，不应该清除用户登录状态
          ElMessage.error('认证失败，请稍后重试')
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.message.includes('Network Error')) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求失败')
    }

    return Promise.reject(error)
  }
)

// 封装请求方法
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  },
}

export default service
