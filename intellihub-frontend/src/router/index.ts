import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 定义路由 meta 类型
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    layout?: string
    roles?: string[]
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
        // API市场
        {
          path: 'api-market',
          name: 'ApiMarket',
          component: () => import('../views/api-market/ApiMarketPage.vue'),
          meta: { title: 'API市场', requiresAuth: false }
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
              meta: { title: 'API列表' }
            },
            {
              path: 'api/create',
              name: 'ApiCreate',
              component: () => import('../views/console/api/ApiCreatePage.vue'),
              meta: { title: '创建API' }
            },
            {
              path: 'api/:id/edit',
              name: 'ApiEdit',
              component: () => import('../views/console/api/ApiCreatePage.vue'),
              meta: { title: '编辑API' }
            },
            {
              path: 'api/:id',
              name: 'ApiDetail',
              component: () => import('../views/console/api/ApiDetailPage.vue'),
              meta: { title: 'API详情' }
            },
            {
              path: 'api/groups',
              name: 'ApiGroups',
              component: () => import('../views/console/api/ApiGroupPage.vue'),
              meta: { title: 'API分组' }
            },
            // 网关配置
            {
              path: 'gateway/routes',
              name: 'GatewayRoutes',
              component: () => import('../views/PlaceholderView.vue'),
              meta: { title: '路由管理' }
            },
            {
              path: 'gateway/plugins',
              name: 'GatewayPlugins',
              component: () => import('../views/PlaceholderView.vue'),
              meta: { title: '插件配置' }
            },
            {
              path: 'gateway/ratelimit',
              name: 'GatewayRatelimit',
              component: () => import('../views/PlaceholderView.vue'),
              meta: { title: '限流策略' }
            },
            // 应用管理
            {
              path: 'app/list',
              name: 'AppList',
              component: () => import('../views/console/app/AppListPage.vue'),
              meta: { title: '应用列表' }
            },
            // 租户管理
            {
              path: 'tenant/list',
              name: 'TenantList',
              component: () => import('../views/console/tenant/TenantListPage.vue'),
              meta: { title: '租户列表' }
            },
            {
              path: 'tenant/quota',
              name: 'TenantQuota',
              component: () => import('../views/console/tenant/TenantListPage.vue'),
              meta: { title: '配额管理' }
            },
            // 用户管理
            {
              path: 'users/list',
              name: 'UsersList',
              component: () => import('../views/console/user/UserListPage.vue'),
              meta: { title: '用户列表' }
            },
            {
              path: 'users/roles',
              name: 'UsersRoles',
              component: () => import('../views/console/user/RolePermissionPage.vue'),
              meta: { title: '角色权限' }
            },
            // 日志和设置
            {
              path: 'logs',
              name: 'Logs',
              component: () => import('../views/PlaceholderView.vue'),
              meta: { title: '操作日志' }
            },
            {
              path: 'settings',
              name: 'Settings',
              component: () => import('../views/PlaceholderView.vue'),
              meta: { title: '系统设置' }
            },
          ]
        },
        // 监控中心
        {
          path: 'monitor',
          name: 'Monitor',
          component: () => import('../views/monitor/MonitorPage.vue'),
          meta: { title: '监控中心', requiresAuth: true }
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

    // 检查角色权限
    if (to.meta.roles && !to.meta.roles.includes(authStore.userRole)) {
      // 权限不足，重定向到首页
      next('/')
      return
    }
  }

  next()
})

export default router
