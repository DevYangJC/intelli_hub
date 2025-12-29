<template>
  <div class="gateway-page">
    <div class="page-header">
      <h1 class="page-title">API 网关</h1>
      <p class="page-desc">高可用分布式网关，提供流量管理、安全防护、监控告警等能力</p>
    </div>

    <!-- 状态概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in gatewayStats" :key="stat.label">
        <div class="stat-card" :class="stat.type">
          <div class="stat-icon">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 功能模块 -->
    <div class="module-section">
      <h2 class="section-title">核心功能</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :lg="8" v-for="module in gatewayModules" :key="module.title">
          <el-card class="module-card" shadow="hover" @click="navigateTo(module.path)">
            <div class="module-header">
              <el-icon :size="32" :color="module.color"><component :is="module.icon" /></el-icon>
              <span class="module-title">{{ module.title }}</span>
            </div>
            <p class="module-desc">{{ module.description }}</p>
            <div class="module-features">
              <el-tag v-for="feature in module.features" :key="feature" size="small" type="info">
                {{ feature }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 网关架构 -->
    <div class="architecture-section">
      <h2 class="section-title">架构特性</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :lg="6" v-for="arch in architectureFeatures" :key="arch.title">
          <div class="arch-card">
            <el-icon :size="40" :color="arch.color"><component :is="arch.icon" /></el-icon>
            <h3>{{ arch.title }}</h3>
            <p>{{ arch.description }}</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h2 class="section-title">快速操作</h2>
      <el-button-group>
        <el-button type="primary" @click="navigateTo('/console/gateway/routes')">
          <el-icon><Guide /></el-icon>
          路由管理
        </el-button>
        <el-button @click="navigateTo('/console/gateway/ratelimit')">
          <el-icon><Stopwatch /></el-icon>
          限流配置
        </el-button>
        <el-button @click="navigateTo('/console/gateway/plugins')">
          <el-icon><Box /></el-icon>
          插件配置
        </el-button>
        <el-button @click="navigateTo('/monitor')">
          <el-icon><DataLine /></el-icon>
          实时监控
        </el-button>
      </el-button-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import {
  Connection,
  Stopwatch,
  Lock,
  DataLine,
  Guide,
  Box,
  Timer,
  CircleCheck,
  Warning,
  Cpu
} from '@element-plus/icons-vue'

const router = useRouter()

const gatewayStats = ref([
  { label: '活跃路由', value: '126', icon: markRaw(Guide), type: 'primary' },
  { label: '今日请求', value: '1.2M', icon: markRaw(Connection), type: 'success' },
  { label: '成功率', value: '99.8%', icon: markRaw(CircleCheck), type: 'success' },
  { label: '平均延迟', value: '28ms', icon: markRaw(Timer), type: 'info' }
])

const gatewayModules = ref([
  {
    title: '路由管理',
    description: '配置API路由规则，支持路径匹配、请求转发、负载均衡等功能',
    icon: markRaw(Guide),
    color: '#409EFF',
    path: '/console/gateway/routes',
    features: ['动态路由', '负载均衡', '路径重写']
  },
  {
    title: '限流熔断',
    description: '保护后端服务，防止流量突增导致服务崩溃，支持多种限流策略',
    icon: markRaw(Stopwatch),
    color: '#E6A23C',
    path: '/console/gateway/ratelimit',
    features: ['令牌桶', '滑动窗口', '熔断降级']
  },
  {
    title: '安全防护',
    description: '提供多层安全机制，包括认证鉴权、IP黑白名单、签名验证等',
    icon: markRaw(Lock),
    color: '#67C23A',
    path: '/console/gateway/plugins',
    features: ['JWT认证', 'IP过滤', '签名校验']
  },
  {
    title: '流量监控',
    description: '实时监控网关流量、响应时间、错误率等关键指标',
    icon: markRaw(DataLine),
    color: '#909399',
    path: '/monitor',
    features: ['实时监控', '趋势分析', '告警通知']
  },
  {
    title: '日志追踪',
    description: '完整的请求日志记录，支持链路追踪和问题排查',
    icon: markRaw(Box),
    color: '#F56C6C',
    path: '/console/stats/logs',
    features: ['访问日志', '链路追踪', '错误分析']
  },
  {
    title: '插件扩展',
    description: '灵活的插件机制，支持自定义请求/响应处理逻辑',
    icon: markRaw(Cpu),
    color: '#8B5CF6',
    path: '/console/gateway/plugins',
    features: ['请求改写', '响应处理', '自定义逻辑']
  }
])

const architectureFeatures = ref([
  {
    title: '高可用',
    description: '多节点部署，自动故障转移，保障服务不中断',
    icon: markRaw(CircleCheck),
    color: '#67C23A'
  },
  {
    title: '高性能',
    description: '基于异步非阻塞架构，支持百万级并发请求',
    icon: markRaw(Timer),
    color: '#409EFF'
  },
  {
    title: '可扩展',
    description: '支持水平扩展，动态扩缩容，灵活应对流量变化',
    icon: markRaw(Connection),
    color: '#E6A23C'
  },
  {
    title: '安全可靠',
    description: '多层安全防护，数据加密传输，审计日志完整',
    icon: markRaw(Lock),
    color: '#F56C6C'
  }
])

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.gateway-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.stats-row {
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.stat-card.primary .stat-icon {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.stat-card.success .stat-icon {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.stat-card.info .stat-icon {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px;
}

.module-section {
  margin-bottom: 32px;
}

.module-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  border-radius: 12px;
}

.module-card:hover {
  transform: translateY(-4px);
}

.module-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.module-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.module-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px;
  line-height: 1.6;
}

.module-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.architecture-section {
  margin-bottom: 32px;
}

.arch-card {
  background: linear-gradient(135deg, #f8fafc 0%, #fff 100%);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  border: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.arch-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 12px 0 8px;
}

.arch-card p {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

.quick-actions {
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
}

.quick-actions .el-button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-actions .el-button {
  margin-left: 0 !important;
}
</style>
