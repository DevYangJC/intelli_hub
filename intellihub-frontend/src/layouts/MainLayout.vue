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
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input type="text" placeholder="搜索API、文档..." class="search-input" />
            <span class="search-shortcut">/</span>
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
} from '@element-plus/icons-vue'
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
