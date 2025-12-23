<template>
  <div class="home-page">
    <!-- 顶部导航栏 -->
    <nav class="top-navbar">
      <div class="navbar-container">
        <!-- 左侧 Logo 和搜索框 -->
        <div class="navbar-left">
          <div class="brand">
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
          </div>
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input type="text" placeholder="搜索API、文档..." class="search-input" />
            <span class="search-shortcut">/</span>
          </div>
        </div>

        <!-- 右侧导航菜单 -->
        <div class="navbar-right">
          <ul class="nav-menu">
            <li><a href="#" class="nav-link">API市场</a></li>
            <li><a href="#" class="nav-link">控制台</a></li>
            <li><a href="#" class="nav-link">监控中心</a></li>
            <li><a href="#" class="nav-link">开发文档</a></li>
          </ul>
          <div class="nav-actions">
            <!-- 未登录状态 -->
            <template v-if="!isAuthenticated">
              <el-button type="primary" size="small" @click="showLoginDialog = true">登录</el-button>
              <el-button size="small" @click="showLoginDialog = true">注册</el-button>
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

    <!-- Hero 区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">企业级智能API管理平台</h1>
        <p class="hero-subtitle">
          统一<span class="highlight">API网关</span>，智能<span class="highlight">流量治理</span>，全生命周期管理
        </p>

        <!-- 统计数据 -->
        <div class="stats-row">
          <div class="stat-item" v-for="stat in platformStats" :key="stat.label">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心功能卡片 -->
    <div class="features-section">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :lg="6" v-for="(feature, index) in featureCards" :key="index">
          <div class="feature-card" @click="navigateToModule(feature.path)">
            <div class="feature-visual">
              <component :is="feature.visual" v-if="feature.visual" />
              <div v-else class="feature-placeholder" :class="`visual-${index}`"></div>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.description }}</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 快速入口 -->
    <div class="quick-section">
      <div class="section-header">
        <h2>快速开始</h2>
        <p>常用操作快速入口</p>
      </div>
      <div class="quick-actions">
        <el-button
          v-for="(action, index) in quickActions"
          :key="index"
          class="quick-btn"
          :type="action.type"
          @click="navigateToModule(action.path)"
        >
          <el-icon><component :is="action.icon" /></el-icon>
          {{ action.text }}
        </el-button>
      </div>
    </div>

    <!-- 最新动态 -->
    <div class="activity-section">
      <div class="section-header">
        <h2>最新动态</h2>
        <p>平台最新活动和通知</p>
      </div>
      <div class="timeline-container">
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in timelineEvents"
            :key="index"
            :timestamp="item.timestamp"
            :color="item.color"
            placement="top"
          >
            <div class="timeline-event">
              <h4 class="event-title">{{ item.title }}</h4>
              <p class="event-description">{{ item.description }}</p>
              <span class="event-tag" v-if="item.meta">{{ item.meta }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

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
              <div class="captcha-box">A4B2</div>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoginDialog = false">取消</el-button>
        <el-button type="primary" :loading="loginLoading" @click="handleLogin">登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  Bell,
  Plus,
  Document,
  User,
  Search,
  Setting,
  SwitchButton,
  Menu,
  Monitor,
} from '@element-plus/icons-vue'
import { ref, reactive, computed, markRaw, type Component } from 'vue'
import ModelsVisual from '@/components/visuals/ModelsVisual.vue'
import AvailabilityVisual from '@/components/visuals/AvailabilityVisual.vue'
import PerformanceVisual from '@/components/visuals/PerformanceVisual.vue'
import SecurityVisual from '@/components/visuals/SecurityVisual.vue'

const router = useRouter()
const authStore = useAuthStore()

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
    
    // 模拟登录请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 验证码检查
    if (loginForm.captcha.toLowerCase() !== 'a4b2') {
      ElMessage.error('验证码错误，请输入 A4B2')
      loginLoading.value = false
      return
    }
    
    // 模拟登录成功
    const userData = {
      id: '1',
      username: loginForm.username,
      email: `${loginForm.username}@example.com`,
      role: 'platform_admin' as const,
      avatar: '',
      createdAt: new Date().toISOString(),
      lastLoginAt: new Date().toISOString()
    }
    
    authStore.setAuth({
      token: 'mock-token-' + Date.now(),
      refreshToken: 'mock-refresh-token-' + Date.now(),
      user: userData
    })
    
    ElMessage.success('登录成功')
    showLoginDialog.value = false
    
    // 重置表单
    loginForm.username = ''
    loginForm.password = ''
    loginForm.captcha = ''
  } catch {
    // 表单验证失败
  } finally {
    loginLoading.value = false
  }
}

// 平台统计数据
const platformStats = [
  { value: '1,200+', label: 'API接口' },
  { value: '50万+', label: '日调用量' },
  { value: '200+', label: '活跃应用' },
  { value: '99.9%', label: '服务可用性' },
]

// 功能卡片
const featureCards: Array<{
  title: string
  description: string
  path: string
  visual: Component | null
}> = [
  {
    title: '统一API管理',
    description: '一站式管理所有API接口，支持版本控制、自动生成文档、在线调试测试。',
    path: '/api/list',
    visual: markRaw(ModelsVisual),
  },
  {
    title: '高可用网关',
    description: '分布式网关架构，智能负载均衡，故障自动切换，保障服务稳定运行。',
    path: '/gateway/config',
    visual: markRaw(AvailabilityVisual),
  },
  {
    title: '流量监控分析',
    description: '实时监控API调用情况，多维度数据分析，智能告警通知，性能瓶颈一目了然。',
    path: '/gateway/monitor',
    visual: markRaw(PerformanceVisual),
  },
  {
    title: '多租户安全隔离',
    description: '企业级多租户架构，细粒度权限控制，数据安全隔离，满足合规要求。',
    path: '/admin/tenant',
    visual: markRaw(SecurityVisual),
  },
]

// 快速操作
const quickActions = [
  { text: '创建API', type: 'primary' as const, icon: Plus, path: '/api/create' },
  { text: '查看文档', type: 'default' as const, icon: Document, path: '/api/document' },
  { text: '监控面板', type: 'default' as const, icon: Monitor, path: '/gateway/monitor' },
  { text: '用户管理', type: 'default' as const, icon: User, path: '/tenant/users' },
]

// 时间线事件
const timelineEvents = [
  {
    timestamp: '2024-01-15 14:30',
    title: '系统维护通知',
    description: '系统将于今晚22:00进行维护升级，预计耗时2小时',
    color: '#faad14',
    meta: '重要',
  },
  {
    timestamp: '2024-01-14 10:15',
    title: '新功能发布',
    description: '新的API版本管理功能已上线，支持自动化版本控制',
    color: '#52c41a',
    meta: '功能更新',
  },
  {
    timestamp: '2024-01-13 09:00',
    title: '配额更新',
    description: '本月API调用配额已更新，当前可用配额为：100,000 次',
    color: '#1890ff',
    meta: '账户信息',
  },
]

// 用户头像
const userAvatar = ref('https://secure.gravatar.com/avatar/placeholder')

// 导航方法
const navigateToModule = (path: string) => {
  router.push(path)
}

// 导航栏命令处理
const handleNavCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info('设置功能开发中...')
      break
    case 'logout':
      authStore.logout()
      ElMessage.success('已退出登录')
      break
  }
}
</script>

<style scoped>
.home-page {
  background: #ffffff;
  min-height: 100vh;
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
}

.nav-link:hover {
  color: #1a1a1a;
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

/* Hero 区域 */
.hero-section {
  background: #ffffff;
  padding: 80px 24px 60px;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: 18px;
  color: #666;
  margin: 0 0 48px;
}

.hero-subtitle .highlight {
  color: #1890ff;
  text-decoration: underline;
  text-underline-offset: 4px;
}

/* 统计数据行 */
.stats-row {
  display: flex;
  justify-content: center;
  gap: 64px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stat-value:last-child {
  color: #1890ff;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

/* 功能卡片区域 */
.features-section {
  padding: 40px 24px 80px;
  max-width: 1400px;
  margin: 0 auto;
}

.feature-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 16px;
  padding: 24px;
  height: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.feature-card:hover {
  border-color: #d0d0d0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.feature-visual {
  height: 160px;
  background: #fafafa;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.feature-placeholder {
  width: 100%;
  height: 100%;
}

.visual-0 {
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f0fa 100%);
}

.visual-1 {
  background: linear-gradient(135deg, #f0fff4 0%, #e6faf0 100%);
}

.visual-2 {
  background: linear-gradient(135deg, #fff7f0 0%, #faf0e6 100%);
}

.visual-3 {
  background: linear-gradient(135deg, #f5f0ff 0%, #efe6fa 100%);
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.feature-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
  flex: 1;
}

/* 快速入口区域 */
.quick-section {
  padding: 60px 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: #fafafa;
}

.section-header {
  text-align: center;
  margin-bottom: 32px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.section-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.quick-btn {
  min-width: 140px;
  height: 44px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 最新动态区域 */
.activity-section {
  padding: 60px 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.timeline-container {
  max-width: 800px;
  margin: 0 auto;
}

.timeline-event {
  padding: 16px 20px;
  background: #f9f9f9;
  border-radius: 8px;
  transition: all 0.2s;
}

.timeline-event:hover {
  background: #f0f7ff;
}

.event-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px;
}

.event-description {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 8px;
}

.event-tag {
  display: inline-block;
  font-size: 12px;
  color: #999;
  background: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
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

  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .stats-row {
    gap: 32px;
  }

  .stat-value {
    font-size: 24px;
  }

  .features-section {
    padding: 24px 16px 48px;
  }

  .feature-card {
    margin-bottom: 16px;
  }

  .quick-section,
  .activity-section {
    padding: 40px 16px;
  }
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
</style>
