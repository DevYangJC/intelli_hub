import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginRequest, type RegisterRequest, type UserInfo } from '@/api/auth'

export type UserRole = 'platform_admin' | 'tenant_admin' | 'api_developer' | 'api_consumer' | 'user'

export interface User {
  id: string
  username: string
  email?: string
  nickname?: string
  phone?: string
  role: UserRole
  tenantId?: string
  tenantName?: string
  avatar?: string
  permissions?: string[]
  createdAt?: string
  lastLoginAt?: string
}

// 从localStorage恢复用户信息
const getStoredUser = (): User | null => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      return JSON.parse(storedUser)
    } catch {
      return null
    }
  }
  return null
}

export const useAuthStore = defineStore('auth', () => {
  // 状态 - 初始化时直接从localStorage恢复
  const user = ref<User | null>(getStoredUser())
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const loading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const userRole = computed(() => user.value?.role || 'user')
  const isAdmin = computed(() => userRole.value === 'platform_admin')
  const isTenantAdmin = computed(() => userRole.value === 'tenant_admin')
  const isDeveloper = computed(() => userRole.value === 'api_developer')

  // 登录
  const login = async (loginData: LoginRequest): Promise<void> => {
    loading.value = true
    try {
      const res = await authApi.login(loginData)
      const { accessToken, refreshToken: rToken, user: userInfo } = res.data
      
      // 转换用户信息
      const userData: User = {
        id: userInfo.id,
        username: userInfo.username,
        email: userInfo.email,
        nickname: userInfo.nickname,
        phone: userInfo.phone,
        avatar: userInfo.avatar,
        role: userInfo.role as UserRole,
        tenantId: userInfo.tenantId,
        tenantName: userInfo.tenantName,
        permissions: userInfo.permissions,
        lastLoginAt: userInfo.lastLoginAt,
      }
      
      setAuth({ token: accessToken, refreshToken: rToken, user: userData })
    } finally {
      loading.value = false
    }
  }

  // 注册
  const register = async (registerData: RegisterRequest): Promise<void> => {
    loading.value = true
    try {
      const res = await authApi.register(registerData)
      const { accessToken, refreshToken: rToken, user: userInfo } = res.data
      
      // 转换用户信息
      const userData: User = {
        id: userInfo.id,
        username: userInfo.username,
        email: userInfo.email,
        nickname: userInfo.nickname,
        phone: userInfo.phone,
        avatar: userInfo.avatar,
        role: userInfo.role as UserRole,
        tenantId: userInfo.tenantId,
        tenantName: userInfo.tenantName,
        permissions: userInfo.permissions,
        lastLoginAt: userInfo.lastLoginAt,
      }
      
      setAuth({ token: accessToken, refreshToken: rToken, user: userData })
    } finally {
      loading.value = false
    }
  }

  // 退出登录
  const logout = async (): Promise<void> => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout API error:', error)
    } finally {
      clearAuth()
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async (): Promise<void> => {
    if (!token.value) return
    
    try {
      const res = await authApi.getCurrentUser()
      const userInfo = res.data
      
      user.value = {
        id: userInfo.id,
        username: userInfo.username,
        email: userInfo.email,
        nickname: userInfo.nickname,
        phone: userInfo.phone,
        avatar: userInfo.avatar,
        role: userInfo.role as UserRole,
        tenantId: userInfo.tenantId,
        tenantName: userInfo.tenantName,
        permissions: userInfo.permissions,
        lastLoginAt: userInfo.lastLoginAt,
      }
      
      localStorage.setItem('user', JSON.stringify(user.value))
    } catch (error) {
      console.error('Failed to fetch current user:', error)
      clearAuth()
    }
  }

  // 设置认证信息
  const setAuth = (authData: { token: string; refreshToken: string; user: User }) => {
    token.value = authData.token
    refreshToken.value = authData.refreshToken
    user.value = authData.user

    // 持久化存储
    localStorage.setItem('token', authData.token)
    localStorage.setItem('refreshToken', authData.refreshToken)
    localStorage.setItem('user', JSON.stringify(authData.user))
  }

  // 清除认证信息
  const clearAuth = () => {
    token.value = null
    refreshToken.value = null
    user.value = null

    // 清除持久化存储
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  // 更新用户信息
  const updateUser = (userData: Partial<User>) => {
    if (user.value) {
      user.value = { ...user.value, ...userData }
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  // 初始化认证状态（可选：验证token有效性）
  const initAuth = async () => {
    // 如果有token但没有用户信息，尝试从服务器获取
    if (token.value && !user.value) {
      try {
        await fetchCurrentUser()
      } catch (error) {
        console.error('Failed to restore auth state:', error)
        clearAuth()
      }
    }
    // 如果token和用户信息不一致，清除认证状态
    if ((token.value && !user.value) || (!token.value && user.value)) {
      clearAuth()
    }
  }

  // 检查权限
  const hasPermission = (requiredRoles: string[] | UserRole[]) => {
    if (!isAuthenticated.value) return false
    return requiredRoles.includes(userRole.value)
  }

  return {
    // 状态
    user,
    token,
    refreshToken,
    loading,

    // 计算属性
    isAuthenticated,
    userRole,
    isAdmin,
    isTenantAdmin,
    isDeveloper,

    // 方法
    login,
    register,
    logout,
    fetchCurrentUser,
    setAuth,
    clearAuth,
    updateUser,
    initAuth,
    hasPermission
  }
})