<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <nav class="top-navbar">
      <div class="navbar-container">
        <!-- 左侧 Logo 和搜索框 -->
        <div class="navbar-left">
          <router-link to="/" class="brand">
            <svg class="brand-logo" xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 48 48">
              <circle cx="24" cy="24" r="22" stroke="#1890ff" stroke-width="2" fill="none" opacity="0.3"/>
              <path d="M24 6 L38 15 L38 33 L24 42 L10 33 L10 15 Z" fill="#1890ff" stroke="#096dd9" stroke-width="1.5"/>
              <circle cx="24" cy="6" r="3" fill="#1890ff"/>
              <circle cx="38" cy="15" r="3" fill="#40a9ff"/>
              <circle cx="38" cy="33" r="3" fill="#40a9ff"/>
              <circle cx="24" cy="42" r="3" fill="#1890ff"/>
              <circle cx="10" cy="33" r="3" fill="#40a9ff"/>
              <circle cx="10" cy="15" r="3" fill="#40a9ff"/>
              <rect x="16" y="20" width="16" height="10" rx="2" fill="#fff"/>
              <circle cx="20" cy="25" r="2" fill="#1890ff"/>
              <circle cx="28" cy="25" r="2" fill="#1890ff"/>
              <path d="M22 25 L26 25" stroke="#1890ff" stroke-width="1.5"/>
            </svg>
            <span class="brand-name">智能开放平台</span>
          </router-link>
          <div class="search-box-wrapper">
            <div class="search-box" :class="{ 'is-focused': showSearchDropdown }">
              <el-icon class="search-icon"><Search /></el-icon>
              <input 
                type="text" 
                v-model="globalSearchKeyword"
                placeholder="搜索API、应用、用户..." 
                class="search-input"
                @input="handleSearchInput"
                @keyup.enter="handleSearchEnter"
                @focus="handleSearchFocus"
                @blur="handleSearchBlur"
              />
              <span class="search-shortcut" v-if="!showSearchDropdown">/</span>
              <el-icon v-if="globalSearchKeyword && showSearchDropdown" class="clear-icon" @click.stop="clearSearch"><Close /></el-icon>
            </div>
            <!-- 搜索下拉结果 -->
            <div class="search-dropdown" v-show="showSearchDropdown && globalSearchKeyword">
              <div v-if="searchLoading" class="search-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>搜索中...</span>
              </div>
              <template v-else-if="searchResults.length > 0">
                <div 
                  v-for="item in searchResults" 
                  :key="`${item.type}-${item.id}`"
                  class="search-result-item"
                  @mousedown.prevent="handleResultClick(item)"
                >
                  <el-tag :type="getSearchTagType(item.type)" size="small">{{ getSearchTypeName(item.type) }}</el-tag>
                  <span class="result-name" v-html="item.name"></span>
                </div>
              </template>
              <div v-else class="search-empty">
                没有找到相关结果
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧导航菜单 -->
        <div class="navbar-right">
          <ul class="nav-menu">
            <li v-for="item in navItems" :key="item.path">
              <router-link 
                :to="item.path" 
                class="nav-link"
                :class="{ active: isActiveRoute(item.path) }"
              >
                {{ item.title }}
              </router-link>
            </li>
          </ul>
          <div class="nav-actions">
            <!-- 未登录状态 -->
            <template v-if="!isAuthenticated">
              <el-button type="primary" size="small" @click="openLoginDialog">登录</el-button>
              <el-button size="small" @click="openRegisterDialog">注册</el-button>
            </template>
            <!-- 已登录状态 -->
            <template v-else>
              <el-button type="text" class="action-btn">
                <el-icon><Bell /></el-icon>
              </el-button>
              <el-dropdown @command="handleNavCommand">
                <el-avatar :size="32" :src="userAvatar" class="user-avatar" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>
                      个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="settings">
                      <el-icon><Setting /></el-icon>
                      设置
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>
                      退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 登录弹窗 -->
    <el-dialog
      v-model="showLoginDialog"
      title="登录"
      width="400px"
      :close-on-click-modal="false"
      class="login-dialog"
    >
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-row :gutter="16" style="width: 100%">
            <el-col :span="16">
              <el-input
                v-model="loginForm.captcha"
                placeholder="请输入验证码"
                prefix-icon="Key"
                size="large"
              />
            </el-col>
            <el-col :span="8">
              <div class="captcha-box" @click="fetchCaptcha" style="cursor: pointer;">
                <img v-if="captchaImage" :src="captchaImage" alt="验证码" style="height: 100%; width: 100%;" />
                <span v-else>点击获取</span>
              </div>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoginDialog = false">取消</el-button>
        <el-button type="primary" :loading="loginLoading" @click="handleLogin">登录</el-button>
      </template>
    </el-dialog>

    <!-- 注册弹窗 -->
    <el-dialog
      v-model="showRegisterDialog"
      title="注册"
      width="450px"
      :close-on-click-modal="false"
      class="register-dialog"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        @keyup.enter="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20个字符）"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20个字符）"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱（选填）"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-row :gutter="16" style="width: 100%">
            <el-col :span="16">
              <el-input
                v-model="registerForm.captcha"
                placeholder="请输入验证码"
                prefix-icon="Key"
                size="large"
              />
            </el-col>
            <el-col :span="8">
              <div class="captcha-box" @click="fetchRegisterCaptcha" style="cursor: pointer;">
                <img v-if="registerCaptchaImage" :src="registerCaptchaImage" alt="验证码" style="height: 100%; width: 100%;" />
                <span v-else>点击获取</span>
              </div>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  Bell,
  User,
  Search,
  Setting,
  SwitchButton,
  Close,
  Loading,
} from '@element-plus/icons-vue'
import { searchApi, type SearchItem } from '@/api/search'
import { ref, reactive, computed, onMounted, watch } from 'vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 监听路由参数，自动打开登录弹窗
onMounted(() => {
  if (route.query.login === 'required' && !authStore.isAuthenticated) {
    openLoginDialog()
  }
})

watch(() => route.query.login, (newVal) => {
  if (newVal === 'required' && !authStore.isAuthenticated) {
    openLoginDialog()
  }
})

// 导航菜单配置
const navItems = [
  { title: '首页', path: '/' },
  { title: 'API市场', path: '/api-market' },
  { title: '控制台', path: '/console' },
  { title: '监控中心', path: '/monitor' },
  { title: '开发文档', path: '/docs' },
]

// 判断当前路由是否激活
const isActiveRoute = (path: string) => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

// 登录状态
const isAuthenticated = computed(() => authStore.isAuthenticated)

// 登录弹窗
const showLoginDialog = ref(false)
const loginLoading = ref(false)
const loginFormRef = ref<FormInstance>()
const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true
    
    // 调用真实登录API
    await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
      captchaKey: captchaKey.value,
      captcha: loginForm.captcha,
    })
    
    ElMessage.success('登录成功')
    showLoginDialog.value = false
    
    // 重置表单
    loginForm.username = ''
    loginForm.password = ''
    loginForm.captcha = ''
    captchaKey.value = ''
    
    // 登录成功后跳转到redirect指定的页面，或清除login参数
    const redirect = route.query.redirect as string
    if (redirect) {
      router.push(redirect)
    } else if (route.query.login) {
      // 清除URL中的login参数
      router.replace({ path: route.path, query: {} })
    }
  } catch (error: any) {
    // 登录失败，刷新验证码
    await fetchCaptcha()
  } finally {
    loginLoading.value = false
  }
}

// 验证码相关
const captchaKey = ref('')
const captchaImage = ref('')

// 获取验证码
const fetchCaptcha = async () => {
  try {
    const { authApi } = await import('@/api/auth')
    const res = await authApi.getCaptcha()
    captchaKey.value = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
  } catch (error) {
    console.error('获取验证码失败:', error)
    // 如果获取验证码失败，使用模拟验证码
    captchaImage.value = ''
  }
}

// 打开登录弹窗时获取验证码
const openLoginDialog = () => {
  showLoginDialog.value = true
  fetchCaptcha()
}

// 注册弹窗
const showRegisterDialog = ref(false)
const registerLoading = ref(false)
const registerFormRef = ref<FormInstance>()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  captcha: ''
})

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 注册验证码
const registerCaptchaKey = ref('')
const registerCaptchaImage = ref('')

// 获取注册验证码
const fetchRegisterCaptcha = async () => {
  try {
    const { authApi } = await import('@/api/auth')
    const res = await authApi.getCaptcha()
    registerCaptchaKey.value = res.data.captchaKey
    registerCaptchaImage.value = res.data.captchaImage
  } catch (error) {
    console.error('获取验证码失败:', error)
    registerCaptchaImage.value = ''
  }
}

// 打开注册弹窗
const openRegisterDialog = () => {
  showRegisterDialog.value = true
  fetchRegisterCaptcha()
}

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    await authStore.register({
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      email: registerForm.email || undefined,
      captchaKey: registerCaptchaKey.value,
      captcha: registerForm.captcha,
    })
    
    ElMessage.success('注册成功')
    showRegisterDialog.value = false
    
    // 重置表单
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.email = ''
    registerForm.captcha = ''
    registerCaptchaKey.value = ''
  } catch (error: any) {
    // 注册失败，刷新验证码
    await fetchRegisterCaptcha()
  } finally {
    registerLoading.value = false
  }
}

// 用户头像
const userAvatar = ref('https://secure.gravatar.com/avatar/placeholder')

// 全局搜索
const globalSearchKeyword = ref('')
const showSearchDropdown = ref(false)
const searchLoading = ref(false)
const searchResults = ref<SearchItem[]>([])
let searchTimer: ReturnType<typeof setTimeout> | null = null

const handleSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  if (!globalSearchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  searchTimer = setTimeout(() => {
    performSearch()
  }, 300)
}

const performSearch = async () => {
  if (!globalSearchKeyword.value.trim()) return
  searchLoading.value = true
  try {
    const res = await searchApi.aggregateSearch({
      keyword: globalSearchKeyword.value,
      types: ['api', 'app', 'user'],
      page: 1,
      size: 8,
      highlight: true
    })
    searchResults.value = res.data.items || []
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

const handleSearchFocus = () => {
  showSearchDropdown.value = true
  if (globalSearchKeyword.value.trim() && searchResults.value.length === 0) {
    performSearch()
  }
}

const handleSearchBlur = () => {
  setTimeout(() => {
    showSearchDropdown.value = false
  }, 200)
}

const handleSearchEnter = () => {
  if (globalSearchKeyword.value.trim()) {
    performSearch()
  }
}

const clearSearch = () => {
  globalSearchKeyword.value = ''
  searchResults.value = []
}

const handleResultClick = (item: SearchItem) => {
  showSearchDropdown.value = false
  globalSearchKeyword.value = ''
  switch (item.type) {
    case 'api':
      router.push(`/console/api/${item.id}`)
      break
    case 'app':
      router.push(`/console/app/list`)
      break
    case 'user':
      router.push(`/console/users/list`)
      break
  }
}

const getSearchTypeName = (type: string): string => {
  const typeNames: Record<string, string> = { api: 'API', app: '应用', user: '用户' }
  return typeNames[type] || type
}

const getSearchTagType = (type: string): 'primary' | 'success' | 'warning' => {
  const tagTypes: Record<string, 'primary' | 'success' | 'warning'> = { api: 'primary', app: 'success', user: 'warning' }
  return tagTypes[type] || 'primary'
}

// 键盘快捷键
onMounted(() => {
  document.addEventListener('keydown', handleKeyDown)
})

const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === '/' && !['INPUT', 'TEXTAREA'].includes((e.target as HTMLElement).tagName)) {
    e.preventDefault()
    const searchInput = document.querySelector('.search-input') as HTMLInputElement
    if (searchInput) searchInput.focus()
  }
}

// 导航栏命令处理
const handleNavCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info('设置功能开发中...')
      break
    case 'logout':
      await authStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
      break
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 顶部导航栏 */
.top-navbar {
  position: sticky;
  top: 0;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  z-index: 1000;
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-decoration: none;
}

.brand-logo {
  width: 28px;
  height: 28px;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

/* 搜索框 */
.search-box {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 8px 12px;
  gap: 8px;
  min-width: 200px;
  transition: all 0.2s;
}

.search-box:focus-within {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
}

.search-icon {
  color: #999;
  font-size: 14px;
}

.search-input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  color: #333;
  flex: 1;
}

.search-input::placeholder {
  color: #999;
}

.search-shortcut {
  background: #e8e8e8;
  color: #666;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
}

.search-box-wrapper {
  position: relative;
}

.search-box.is-focused {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.clear-icon {
  cursor: pointer;
  color: #999;
  font-size: 14px;
}

.clear-icon:hover {
  color: #666;
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
}

.search-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: #999;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-result-item:hover {
  background: #f5f7fa;
}

.search-result-item .result-name {
  flex: 1;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-result-item .result-name :deep(em) {
  color: #1890ff;
  font-style: normal;
  font-weight: bold;
}

.search-footer {
  padding: 12px 16px;
  text-align: center;
  color: #1890ff;
  font-size: 14px;
  cursor: pointer;
  border-top: 1px solid #f0f0f0;
}

.search-footer:hover {
  background: #f5f7fa;
}

.search-empty {
  padding: 24px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-menu {
  list-style: none;
  display: flex;
  gap: 32px;
  margin: 0;
  padding: 0;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
  padding: 8px 0;
  position: relative;
}

.nav-link:hover {
  color: #1a1a1a;
}

.nav-link.active {
  color: #1890ff;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #1890ff;
  border-radius: 1px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  font-size: 18px;
  color: #666;
  padding: 8px;
}

.action-btn:hover {
  color: #1a1a1a;
}

.user-avatar {
  cursor: pointer;
}

/* 主内容区域 */
.main-content {
  flex: 1;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 登录弹窗样式 */
.captcha-box {
  height: 40px;
  line-height: 40px;
  text-align: center;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 18px;
  font-weight: bold;
  color: #666;
  letter-spacing: 4px;
  cursor: pointer;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-container {
    padding: 0 16px;
  }

  .search-box {
    display: none;
  }

  .nav-menu {
    display: none;
  }
}
</style>
