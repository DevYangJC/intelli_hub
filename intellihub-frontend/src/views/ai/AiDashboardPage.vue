<template>
  <div class="ai-dashboard-page">
    <div class="page-container">
      <!-- 顶部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon><DataAnalysis /></el-icon>
            使用统计
          </h1>
          <p class="page-desc">查看 AI 服务的使用情况和性能指标</p>
        </div>
        <div class="header-actions">
          <el-select v-model="timeRange" @change="loadData">
            <el-option label="近7天" :value="7" />
            <el-option label="近14天" :value="14" />
            <el-option label="近30天" :value="30" />
          </el-select>
          <el-button @click="loadData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon blue">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ formatNumber(statistics.totalRequests) }}</span>
            <span class="stat-label">总请求数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon green">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statistics.successRate.toFixed(1) }}%</span>
            <span class="stat-label">成功率</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ formatNumber(statistics.totalTokens) }}</span>
            <span class="stat-label">Token 消耗</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon pink">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">¥{{ statistics.totalCost.toFixed(2) }}</span>
            <span class="stat-label">总成本</span>
          </div>
        </div>
      </div>

      <!-- 图表区 -->
      <div class="charts-section">
        <div class="chart-card full-width">
          <h3 class="chart-title">调用趋势</h3>
          <div class="chart-body" ref="trendChartRef"></div>
        </div>
      </div>

      <div class="charts-section">
        <div class="chart-card">
          <h3 class="chart-title">模型使用排行</h3>
          <div class="ranking-list">
            <div
              v-for="(item, index) in modelRanking"
              :key="item.model"
              class="ranking-item"
            >
              <span class="ranking-index" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="ranking-name">{{ getModelLabel(item.model) }}</span>
              <span class="ranking-value">{{ formatNumber(item.count) }}</span>
              <el-progress
                :percentage="getPercent(item.count, maxModelCount)"
                :stroke-width="6"
                :show-text="false"
              />
            </div>
            <el-empty v-if="modelRanking.length === 0" description="暂无数据" :image-size="60" />
          </div>
        </div>

        <div class="chart-card">
          <h3 class="chart-title">用户使用排行</h3>
          <div class="ranking-list">
            <div
              v-for="(item, index) in userRanking"
              :key="item.userId"
              class="ranking-item"
            >
              <span class="ranking-index" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="ranking-name">{{ item.username || item.userId }}</span>
              <span class="ranking-value">{{ formatNumber(item.count) }}</span>
              <el-progress
                :percentage="getPercent(item.count, maxUserCount)"
                :stroke-width="6"
                :show-text="false"
                color="#67c23a"
              />
            </div>
            <el-empty v-if="userRanking.length === 0" description="暂无数据" :image-size="60" />
          </div>
        </div>
      </div>

      <!-- 实时概览 -->
      <div class="realtime-section">
        <h3 class="section-title">实时概览</h3>
        <div class="realtime-cards">
          <div class="realtime-card">
            <span class="realtime-value">{{ formatNumber(realtime.todayRequests) }}</span>
            <span class="realtime-label">今日请求</span>
          </div>
          <div class="realtime-card">
            <span class="realtime-value">{{ formatNumber(realtime.todayTokens) }}</span>
            <span class="realtime-label">今日 Token</span>
          </div>
          <div class="realtime-card">
            <span class="realtime-value">¥{{ realtime.todayCost.toFixed(2) }}</span>
            <span class="realtime-label">今日成本</span>
          </div>
          <div class="realtime-card">
            <span class="realtime-value">{{ formatNumber(realtime.hourRequests) }}</span>
            <span class="realtime-label">最近1小时</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import {
  DataAnalysis,
  Refresh,
  ChatDotRound,
  CircleCheck,
  Coin,
  Money
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getTenantStatistics,
  getModelRanking,
  getUserRanking,
  getDailyTrend,
  getRealTimeOverview
} from '@/api/aigc'

// 状态
const loading = ref(false)
const timeRange = ref(7)
const trendChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null

const statistics = ref({
  totalRequests: 0,
  successRequests: 0,
  failedRequests: 0,
  totalTokens: 0,
  totalCost: 0,
  avgLatency: 0,
  successRate: 0
})

const modelRanking = ref<Array<{ model: string; count: number }>>([])
const userRanking = ref<Array<{ userId: string; username: string; count: number }>>([])
const maxModelCount = ref(0)
const maxUserCount = ref(0)
const realtime = ref({
  todayRequests: 0,
  todayTokens: 0,
  todayCost: 0,
  hourRequests: 0
})

const trendData = ref<{ dates: string[]; counts: number[]; tokens: number[] }>({
  dates: [],
  counts: [],
  tokens: []
})

// 模型标签（与后端保持一致）
const modelLabels: Record<string, string> = {
  // 阿里通义千问
  'qwen-turbo': '通义千问 Turbo',
  'qwen-plus': '通义千问 Plus',
  'qwen-max': '通义千问 Max',
  'qwen-max-longcontext': '通义千问 Max 长文本',
  // 百度文心一言（2025最新）
  'ernie-3.5-8k': '文心 3.5 (8K)',
  'ernie-3.5-128k': '文心 3.5 (128K)',
  'ernie-4.0-8k': '文心 4.0 (8K)',
  'ernie-4.0-turbo-8k': '文心 4.0 Turbo',
  'ernie-speed-8k': '文心 Speed',
  'ernie-lite-8k': '文心 Lite',
  // 腾讯混元
  'hunyuan-lite': '混元 Lite',
  'hunyuan-standard': '混元 Standard',
  'hunyuan-standard-256K': '混元 Standard (256K)',
  'hunyuan-pro': '混元 Pro',
  'hunyuan-turbo': '混元 Turbo',
  'hunyuan-turbo-latest': '混元 Turbo Latest'
}

const getModelLabel = (model: string) => modelLabels[model] || model

const formatNumber = (num: number) => {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

const getPercent = (value: number, max: number) => {
  if (!max) return 0
  return (value / max) * 100
}

// 初始化图表
const initChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!trendChart) return
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['请求数', 'Token 消耗'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.value.dates.map(d => d.substring(5))
    },
    yAxis: [
      { type: 'value', name: '请求数', position: 'left' },
      { type: 'value', name: 'Token', position: 'right' }
    ],
    series: [
      {
        name: '请求数',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        data: trendData.value.counts,
        areaStyle: { opacity: 0.1 },
        itemStyle: { color: '#409eff' }
      },
      {
        name: 'Token 消耗',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: trendData.value.tokens,
        areaStyle: { opacity: 0.1 },
        itemStyle: { color: '#67c23a' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const [statsRes, modelRes, userRes, trendRes, realtimeRes] = await Promise.all([
      getTenantStatistics(timeRange.value),
      getModelRanking(10),
      getUserRanking(10),
      getDailyTrend(timeRange.value),
      getRealTimeOverview()
    ])

    if (statsRes.code === 200 && statsRes.data) {
      statistics.value = statsRes.data
    }

    if (modelRes.code === 200 && modelRes.data?.ranking) {
      modelRanking.value = modelRes.data.ranking
      maxModelCount.value = modelRanking.value[0]?.count || 0
    }

    if (userRes.code === 200 && userRes.data?.ranking) {
      userRanking.value = userRes.data.ranking
      maxUserCount.value = userRanking.value[0]?.count || 0
    }

    if (trendRes.code === 200 && trendRes.data) {
      trendData.value = trendRes.data
      updateChart()
    }

    if (realtimeRes.code === 200 && realtimeRes.data) {
      realtime.value = realtimeRes.data
    }
  } finally {
    loading.value = false
  }
}

const handleResize = () => trendChart?.resize()

onMounted(() => {
  loadData()
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped>
.ai-dashboard-page {
  min-height: calc(100vh - 56px);
  background: #f5f7fa;
  padding: 24px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.page-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.blue { background: #e6f4ff; color: #1890ff; }
.stat-icon.green { background: #f0fdf4; color: #22c55e; }
.stat-icon.orange { background: #fef3c7; color: #f59e0b; }
.stat-icon.pink { background: #fce7f3; color: #ec4899; }

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 图表区 */
.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-body {
  height: 280px;
}

/* 排行列表 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ranking-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.ranking-index.top {
  background: linear-gradient(135deg, #409eff, #79bbff);
  color: #fff;
}

.ranking-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.ranking-value {
  font-size: 13px;
  color: #909399;
  min-width: 50px;
  text-align: right;
}

.ranking-item .el-progress {
  width: 80px;
}

/* 实时概览 */
.realtime-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.realtime-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.realtime-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.realtime-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.realtime-label {
  font-size: 13px;
  color: #909399;
}

@media (max-width: 1200px) {
  .stats-cards, .realtime-cards { grid-template-columns: repeat(2, 1fr); }
  .charts-section { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .stats-cards, .realtime-cards { grid-template-columns: 1fr; }
}
</style>
