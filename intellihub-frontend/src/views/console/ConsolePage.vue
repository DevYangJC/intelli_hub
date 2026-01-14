<template>
  <div class="console-page">
    <div class="console-container">
      <!-- 侧边栏 -->
      <aside class="console-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <div class="menu-label">平台管理</div>
          
          <el-sub-menu index="api" v-if="hasPermission('api:list')">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>API管理</span>
            </template>
            <el-menu-item index="api-list">API列表</el-menu-item>
            <el-menu-item index="api-create" v-if="hasPermission('api:create')">创建API</el-menu-item>
            <el-menu-item index="api-groups">API分组</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="gateway">
            <template #title>
              <el-icon><SetUp /></el-icon>
              <span>网关配置</span>
            </template>
            <el-menu-item index="gateway-routes">路由管理</el-menu-item>
            <el-menu-item index="gateway-plugins">插件配置</el-menu-item>
            <el-menu-item index="gateway-ratelimit">限流策略</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="app-list" v-if="hasPermission('app:list')">
            <el-icon><Grid /></el-icon>
            <span>应用管理</span>
          </el-menu-item>

          <template v-if="hasAnyPermission(['monitor:view', 'monitor:alert', 'system:log'])">
            <div class="menu-divider"></div>
            <div class="menu-label">监控统计</div>

            <el-menu-item index="logs" v-if="hasPermission('system:log')">
              <el-icon><Tickets /></el-icon>
              <span>调用日志</span>
            </el-menu-item>

            <el-sub-menu index="alert" v-if="hasPermission('monitor:alert')">
              <template #title>
                <el-icon><Bell /></el-icon>
                <span>告警管理</span>
              </template>
              <el-menu-item index="alert-rules">告警规则</el-menu-item>
              <el-menu-item index="alert-records">告警历史</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="event">
              <template #title>
                <el-icon><Notification /></el-icon>
                <span>事件中心</span>
              </template>
              <el-menu-item index="event-definitions">事件定义</el-menu-item>
              <el-menu-item index="event-subscriptions">事件订阅</el-menu-item>
              <el-menu-item index="event-records">事件记录</el-menu-item>
              <el-menu-item index="event-statistics">事件统计</el-menu-item>
            </el-sub-menu>
          </template>

          <template v-if="hasAnyPermission(['tenant:list', 'user:list', 'system:config'])">
            <div class="menu-divider"></div>
            <div class="menu-label">系统设置</div>

            <!-- 租户管理（仅超级管理员） -->
            <el-sub-menu index="tenant" v-if="hasPermission('tenant:list')">
              <template #title>
                <el-icon><OfficeBuilding /></el-icon>
                <span>租户管理</span>
              </template>
              <el-menu-item index="tenant-list">租户列表</el-menu-item>
            </el-sub-menu>

            <!-- 用户管理 -->
            <el-sub-menu index="users" v-if="hasPermission('user:list')">
              <template #title>
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="users-list">用户列表</el-menu-item>
              <el-menu-item index="users-roles">角色权限</el-menu-item>
            </el-sub-menu>

            <!-- 系统设置（仅超级管理员） -->
            <el-sub-menu index="system" v-if="hasPermission('system:config')">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统设置</span>
              </template>
              <el-menu-item index="settings">基础设置</el-menu-item>
              <el-menu-item index="announcements" v-if="hasPermission('system:announcement')">公告管理</el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </aside>

      <!-- 主内容区 -->
      <main class="console-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" v-if="Component" />
            <DashboardPage v-else />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Connection,
  SetUp,
  Grid,
  OfficeBuilding,
  User,
  Document,
  Setting,
  DataLine,
  TrendCharts,
  Tickets,
  Bell,
  Notification,
  ChatDotRound,
} from '@element-plus/icons-vue'
import DashboardPage from './DashboardPage.vue'
import { useAuthStore } from '@/stores/auth'

// 用户信息
const authStore = useAuthStore()
const userName = ref('张三')
const userRole = ref('开发者')
const userInitial = computed(() => userName.value.charAt(0))

// 权限检查快捷方法
const hasPermission = (code: string) => authStore.hasPermission(code)
const hasAnyPermission = (codes: string[]) => authStore.hasAnyPermission(codes)

const router = useRouter()
const route = useRoute()

// 当前激活的菜单
const activeMenu = ref('dashboard')

// 监听路由变化更新菜单状态
watch(() => route.path, (path) => {
  const segments = path.split('/').filter(Boolean)
  if (segments.length >= 2) {
    activeMenu.value = segments.slice(1).join('-')
  } else {
    activeMenu.value = 'overview'
  }
}, { immediate: true })

// 菜单选择处理
const handleMenuSelect = (index: string) => {
  const routeMap: Record<string, string> = {
    'api-list': '/console/api/list',
    'api-create': '/console/api/create',
    'api-groups': '/console/api/groups',
    'gateway-routes': '/console/gateway/routes',
    'gateway-plugins': '/console/gateway/plugins',
    'gateway-ratelimit': '/console/gateway/ratelimit',
    'app-list': '/console/app/list',
    'tenant-list': '/console/tenant/list',
    'users-list': '/console/users/list',
    'users-roles': '/console/users/roles',
    'logs': '/console/logs',
    'alert-rules': '/console/alert/rules',
    'alert-records': '/console/alert/records',
    'event-definitions': '/console/event/definitions',
    'event-subscriptions': '/console/event/subscriptions',
    'event-records': '/console/event/records',
    'event-statistics': '/console/event/statistics',
    'settings': '/console/settings',
    'announcements': '/console/announcements',
  }
  
  const targetRoute = routeMap[index]
  if (targetRoute) {
    router.push(targetRoute)
  }
}
</script>

<style scoped>
.console-page {
  min-height: calc(100vh - 56px);
  background: #f5f7fa;
}

.console-container {
  display: flex;
  max-width: 1600px;
  margin: 0 auto;
  min-height: calc(100vh - 56px);
  padding: 24px;
  gap: 24px;
}

/* 侧边栏 */
.console-sidebar {
  width: 240px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  flex-shrink: 0;
  padding: 16px 0;
  height: fit-content;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.sidebar-menu {
  border-right: none;
  height: 100%;
  padding: 0 12px;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 240px;
}

/* 菜单项样式 */
.sidebar-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 4px 0;
  border-radius: 8px;
  color: #606266;
  transition: all 0.2s ease;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: #f5f7fa;
  color: #409eff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  margin-right: 8px;
  font-size: 18px;
}

/* 子菜单样式 */
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 4px 0;
  border-radius: 8px;
  color: #606266;
  transition: all 0.2s ease;
}

.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: #f5f7fa;
  color: #409eff;
}

.sidebar-menu :deep(.el-sub-menu__title .el-icon) {
  margin-right: 8px;
  font-size: 18px;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  height: 40px;
  line-height: 40px;
  padding-left: 52px !important;
  margin: 2px 0;
  font-size: 13px;
}

.menu-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 16px 8px;
}

.menu-label {
  font-size: 11px;
  color: #909399;
  padding: 8px 12px;
  font-weight: 500;
  letter-spacing: 1px;
}

/* 主内容区 */
.console-main {
  flex: 1;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 24px;
  overflow-x: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
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

/* 响应式 */
@media (max-width: 768px) {
  .console-sidebar {
    display: none;
  }

  .console-main {
    padding: 16px;
  }
}
</style>
