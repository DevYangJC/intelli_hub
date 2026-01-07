import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 定义路由 meta 类型
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    layout?: string
    permission?: string  // 权限码（单个）
    permissions?: string[]  // 权限码（多个，满足任一即可）
    icon?: string
    hidden?: boolean
  }
}

// 路由懒加载
const MainLayout = () => import('../layouts/MainLayout.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 主布局（包含导航栏）
    {
      path: '/',
      component: MainLayout,
      children: [
        // 首页
        {
          path: '',
          name: 'Home',
          component: () => import('../views/HomePage.vue'),
          meta: { title: '首页', requiresAuth: false }
        },
        // API市场（所有用户可见）
        {
          path: 'api-market',
          name: 'ApiMarket',
          component: () => import('../views/api-market/ApiMarketPage.vue'),
          meta: { title: 'API市场', requiresAuth: false, permission: 'market:view' }
        },
        // 控制台
        {
          path: 'console',
          name: 'Console',
          component: () => import('../views/console/ConsolePage.vue'),
          meta: { title: '控制台', requiresAuth: true },
          children: [
            // 数据看板
            {
              path: '',
              name: 'Dashboard',
              component: () => import('../views/console/DashboardPage.vue'),
              meta: { title: '数据看板' }
            },
            // API管理
            {
              path: 'api/list',
              name: 'ApiList',
              component: () => import('../views/console/api/ApiListPage.vue'),
              meta: { title: 'API列表', permission: 'api:list' }
            },
            {
              path: 'api/create',
              name: 'ApiCreate',
              component: () => import('../views/console/api/ApiCreatePage.vue'),
              meta: { title: '创建API', permission: 'api:create' }
            },
            {
              path: 'api/:id/edit',
              name: 'ApiEdit',
              component: () => import('../views/console/api/ApiCreatePage.vue'),
              meta: { title: '编辑API', permission: 'api:update' }
            },
            {
              path: 'api/:id',
              name: 'ApiDetail',
              component: () => import('../views/console/api/ApiDetailPage.vue'),
              meta: { title: 'API详情', permission: 'api:list' }
            },
            {
              path: 'api/groups',
              name: 'ApiGroups',
              component: () => import('../views/console/api/ApiGroupPage.vue'),
              meta: { title: 'API分组', permission: 'api:list' }
            },
            // 网关配置
            {
              path: 'gateway',
              name: 'GatewayOverview',
              component: () => import('../views/console/gateway/GatewayPage.vue'),
              meta: { title: '网关概览' }
            },
            {
              path: 'gateway/routes',
              name: 'GatewayRoutes',
              component: () => import('../views/console/gateway/RouteManagePage.vue'),
              meta: { title: '路由管理' }
            },
            {
              path: 'gateway/plugins',
              name: 'GatewayPlugins',
              component: () => import('../views/console/gateway/PluginManagePage.vue'),
              meta: { title: '插件配置' }
            },
            {
              path: 'gateway/ratelimit',
              name: 'GatewayRatelimit',
              component: () => import('../views/console/gateway/RatelimitManagePage.vue'),
              meta: { title: '限流策略' }
            },
            // 应用管理
            {
              path: 'app/list',
              name: 'AppList',
              component: () => import('../views/console/app/AppListPage.vue'),
              meta: { title: '应用列表', permission: 'app:list' }
            },
            // 租户管理（仅超级管理员）
            {
              path: 'tenant',
              name: 'TenantSecurity',
              component: () => import('../views/console/tenant/TenantSecurityPage.vue'),
              meta: { title: '多租户安全', permission: 'tenant:list' }
            },
            {
              path: 'tenant/list',
              name: 'TenantList',
              component: () => import('../views/console/tenant/TenantListPage.vue'),
              meta: { title: '租户列表', permission: 'tenant:list' }
            },
            // 用户管理
            {
              path: 'users/list',
              name: 'UsersList',
              component: () => import('../views/console/user/UserListPage.vue'),
              meta: { title: '用户列表', permission: 'user:list' }
            },
            {
              path: 'users/roles',
              name: 'UsersRoles',
              component: () => import('../views/console/user/RolePermissionPage.vue'),
              meta: { title: '角色权限', permission: 'user:list' }
            },
            // 统计监控
            {
              path: 'stats',
              name: 'Stats',
              component: () => import('../views/console/stats/StatsPage.vue'),
              meta: { title: '调用统计', permission: 'monitor:view' }
            },
            // 日志
            {
              path: 'logs',
              name: 'Logs',
              component: () => import('../views/console/stats/CallLogsPage.vue'),
              meta: { title: '调用日志', permission: 'system:log' }
            },
            // 告警管理
            {
              path: 'alert/rules',
              name: 'AlertRules',
              component: () => import('../views/console/alert/AlertRulesPage.vue'),
              meta: { title: '告警规则', permission: 'monitor:alert' }
            },
            {
              path: 'alert/records',
              name: 'AlertRecords',
              component: () => import('../views/console/alert/AlertRecordsPage.vue'),
              meta: { title: '告警历史', permission: 'monitor:alert' }
            },
            // 系统设置（仅超级管理员）
            {
              path: 'settings',
              name: 'Settings',
              component: () => import('../views/console/settings/SettingsPage.vue'),
              meta: { title: '系统设置', permission: 'system:config' }
            },
            {
              path: 'announcements',
              name: 'Announcements',
              component: () => import('../views/console/settings/AnnouncementsPage.vue'),
              meta: { title: '公告管理', permission: 'system:announcement' }
            },
            // 事件中心
            {
              path: 'event/definitions',
              name: 'EventDefinitions',
              component: () => import('../views/console/event/EventDefinitionsPage.vue'),
              meta: { title: '事件定义', permission: 'event:definition' }
            },
            {
              path: 'event/subscriptions',
              name: 'EventSubscriptions',
              component: () => import('../views/console/event/EventSubscriptionsPage.vue'),
              meta: { title: '事件订阅', permission: 'event:subscription' }
            },
            {
              path: 'event/records',
              name: 'EventRecords',
              component: () => import('../views/console/event/EventRecordsPage.vue'),
              meta: { title: '事件记录', permission: 'event:record' }
            },
            {
              path: 'event/statistics',
              name: 'EventStatistics',
              component: () => import('../views/console/event/EventStatisticsPage.vue'),
              meta: { title: '事件统计', permission: 'event:statistics' }
            },
            // AIGC服务
            {
              path: 'aigc/chat',
              name: 'AigcChat',
              component: () => import('../views/ai/AiChatPage.vue'),
              meta: { title: 'AI对话', permission: 'aigc:chat' }
            },
            {
              path: 'aigc/templates',
              name: 'AigcTemplates',
              component: () => import('../views/ai/AiTemplatesPage.vue'),
              meta: { title: 'Prompt模板', permission: 'aigc:template' }
            },
            {
              path: 'aigc/dashboard',
              name: 'AigcDashboard',
              component: () => import('../views/ai/AiDashboardPage.vue'),
              meta: { title: '使用统计', permission: 'aigc:stats' }
            },
            {
              path: 'aigc/cost',
              name: 'AigcCost',
              component: () => import('../views/ai/AiCostPage.vue'),
              meta: { title: '成本分析', permission: 'aigc:cost' }
            },
          ]
        },
        // 监控中心
        {
          path: 'monitor',
          name: 'Monitor',
          component: () => import('../views/monitor/MonitorPage.vue'),
          meta: { title: '监控中心', requiresAuth: true, permission: 'monitor:view' }
        },
        // 开发文档
        {
          path: 'docs',
          name: 'Docs',
          component: () => import('../views/docs/DocsPage.vue'),
          meta: { title: '开发文档', requiresAuth: false }
        },
        // 个人中心
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/user/ProfilePage.vue'),
          meta: { title: '个人中心', requiresAuth: true }
        },
      ]
    },
    // 404页面
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../views/error/NotFoundView.vue'),
      meta: {
        title: '页面不存在',
        layout: 'error'
      }
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - IntelliHub` : 'IntelliHub'

  // 检查是否需要认证（包括检查父路由的requiresAuth）
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth === true)
  
  if (requiresAuth) {
    if (!authStore.isAuthenticated) {
      // 未登录，重定向到首页并标记需要登录
      next({
        path: '/',
        query: { login: 'required', redirect: to.fullPath }
      })
      return
    }
  }

  // 检查权限码
  const permission = to.meta.permission
  const permissions = to.meta.permissions
  
  if (permission && authStore.isAuthenticated) {
    if (!authStore.hasPermission(permission)) {
      // 权限不足，重定向到控制台首页
      console.warn(`权限不足: 需要 ${permission}`)
      next('/console')
      return
    }
  }
  
  if (permissions && permissions.length > 0 && authStore.isAuthenticated) {
    if (!authStore.hasAnyPermission(permissions)) {
      console.warn(`权限不足: 需要其中之一 ${permissions.join(', ')}`)
      next('/console')
      return
    }
  }

  next()
})

export default router
