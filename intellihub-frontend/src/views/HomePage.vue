<template>
  <div class="home-page">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, markRaw, type Component } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus,
  Document,
  User,
  Monitor,
} from '@element-plus/icons-vue'
import ModelsVisual from '@/components/visuals/ModelsVisual.vue'
import AvailabilityVisual from '@/components/visuals/AvailabilityVisual.vue'
import PerformanceVisual from '@/components/visuals/PerformanceVisual.vue'
import SecurityVisual from '@/components/visuals/SecurityVisual.vue'
import { getPublishedAnnouncements, type AnnouncementDTO } from '@/api/announcement'
import { getStatsOverview } from '@/api/stats'

const router = useRouter()

// 平台统计数据
const platformStats = ref([
  { value: '-', label: 'API接口' },
  { value: '-', label: '日调用量' },
  { value: '-', label: '活跃应用' },
  { value: '99.9%', label: '服务可用性' },
])

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
    path: '/api-market',
    visual: markRaw(ModelsVisual),
  },
  {
    title: '高可用网关',
    description: '分布式网关架构，智能负载均衡，故障自动切换，保障服务稳定运行。',
    path: '/console/gateway',
    visual: markRaw(AvailabilityVisual),
  },
  {
    title: '流量监控分析',
    description: '实时监控API调用情况，多维度数据分析，智能告警通知，性能瓶颈一目了然。',
    path: '/monitor',
    visual: markRaw(PerformanceVisual),
  },
  {
    title: '多租户安全隔离',
    description: '企业级多租户架构，细粒度权限控制，数据安全隔离，满足合规要求。',
    path: '/console/tenant',
    visual: markRaw(SecurityVisual),
  },
]

// 快速操作
const quickActions = [
  { text: '创建API', type: 'primary' as const, icon: Plus, path: '/console/api/create' },
  { text: '查看文档', type: 'default' as const, icon: Document, path: '/docs' },
  { text: '监控面板', type: 'default' as const, icon: Monitor, path: '/monitor' },
  { text: '用户管理', type: 'default' as const, icon: User, path: '/console/users/list' },
]

// 时间线事件
interface TimelineEvent {
  timestamp: string
  title: string
  description: string
  color: string
  meta: string
}
const timelineEvents = ref<TimelineEvent[]>([])

// 获取类型对应的颜色
const getTypeColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    notice: '#1890ff',
    feature: '#52c41a',
    maintenance: '#faad14',
    warning: '#ff4d4f'
  }
  return colorMap[type] || '#1890ff'
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万+'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k+'
  return num.toString()
}

// 加载平台统计数据
const loadStats = async () => {
  try {
    const res = await getStatsOverview()
    if (res.code === 200 && res.data) {
      platformStats.value[0].value = formatNumber(res.data.apiCount || 0)
      platformStats.value[1].value = formatNumber(res.data.todayTotalCount || 0)
      platformStats.value[2].value = formatNumber(res.data.appCount || 0)
      // 可用性保持默认值
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 加载公告
const loadAnnouncements = async () => {
  try {
    const res = await getPublishedAnnouncements(5)
    if (res.code === 200 && res.data) {
      timelineEvents.value = (res.data || []).map((item: AnnouncementDTO) => ({
        timestamp: item.publishTime || '',
        title: item.title,
        description: item.description,
        color: getTypeColor(item.type),
        meta: item.meta || ''
      }))
    }
  } catch (error) {
    console.error('加载公告失败', error)
    // 加载失败时显示默认数据
    timelineEvents.value = [
      {
        timestamp: new Date().toISOString().slice(0, 16).replace('T', ' '),
        title: '欢迎使用 IntelliHub',
        description: '智能API管理平台，提供API全生命周期管理能力',
        color: '#1890ff',
        meta: '系统通知'
      }
    ]
  }
}

// 导航方法
const navigateToModule = (path: string) => {
  router.push(path)
}

onMounted(() => {
  loadStats()
  loadAnnouncements()
})
</script>

<style scoped>
.home-page {
  background: #ffffff;
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
</style>
