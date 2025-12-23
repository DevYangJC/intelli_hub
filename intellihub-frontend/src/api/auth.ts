import { request } from './request'

// 登录请求参数
export interface LoginRequest {
  username: string
  password: string
  captchaKey?: string
  captcha?: string
}

// 登录响应
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}

// 用户信息
export interface UserInfo {
  id: string
  username: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  role: string
  roleNames?: string[]
  permissions?: string[]
  tenantId: string
  tenantName?: string
  lastLoginAt?: string
  lastLoginIp?: string
}

// 验证码响应
export interface CaptchaResponse {
  captchaKey: string
  captchaImage: string
  expiresIn: number
}

// 刷新Token请求
export interface RefreshTokenRequest {
  refreshToken: string
}

// 注册请求参数
export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  email?: string
  phone?: string
  nickname?: string
  captchaKey?: string
  captcha?: string
}

// API响应包装
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 认证相关API
export const authApi = {
  // 登录
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/iam/v1/auth/login', data)
  },

  // 注册
  register(data: RegisterRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/iam/v1/auth/register', data)
  },

  // 获取验证码
  getCaptcha(): Promise<ApiResponse<CaptchaResponse>> {
    return request.get('/iam/v1/auth/captcha')
  },

  // 刷新Token
  refreshToken(data: RefreshTokenRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/iam/v1/auth/refresh', data)
  },

  // 退出登录
  logout(): Promise<ApiResponse<void>> {
    return request.post('/iam/v1/auth/logout')
  },

  // 获取当前用户信息
  getCurrentUser(): Promise<ApiResponse<UserInfo>> {
    return request.get('/iam/v1/auth/me')
  },

  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string; confirmPassword: string }): Promise<ApiResponse<void>> {
    return request.post('/iam/v1/auth/password', data)
  },
}

export default authApi
