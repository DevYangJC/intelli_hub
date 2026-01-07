<template>
  <div class="aigc-dashboard-page">
    <!-- 顶部标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><DataAnalysis /></el-icon>
        AIGC 统计分析
      </h2>
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
        <div class="stat-icon" style="background: #e6f4ff; color: #1890ff">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(statistics.totalRequests) }}</span>
          <span class="stat-label">总请求数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #f0fdf4; color: #22c55e">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ statistics.successRate.toFixed(1) }}%</span>
          <span class="stat-label">成功率</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fef3c7; color: #f59e0b">
          <el-icon><Coin /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(statistics.totalTokens) }}</span>
          <span class="stat-label">Token消耗</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fce7f3; color: #ec4899">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">¥{{ statistics.totalCost.toFixed(2) }}</span>
          <span class="stat-label">总成本</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 趋势图 -->
      <div class="chart-card trend-chart">
        <h3 class="chart-title">调用趋势</h3>
        <div class="chart-container" ref="trendChartRef"></div>
      </div>
    </div>

    <div class="charts-row">
      <!-- 模型排行 -->
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
            <span class="ranking-count">{{ formatNumber(item.count) }} 次</span>
            <el-progress
              :percentage="getModelPercent(item.count)"
              :stroke-width="6"
              :show-text="false"
              style="width: 100px"
            />
          </div>
          <el-empty v-if="modelRanking.length === 0" description="暂无数据" :image-size="60" />
        </div>
      </div>

      <!-- 用户排行 -->
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
            <span class="ranking-count">{{ formatNumber(item.count) }} 次</span>
            <el-progress
              :percentage="getUserPercent(item.count)"
              :stroke-width="6"
              :show-text="false"
              style="width: 100px"
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
          <span class="realtime-label">今日请求</span>
          <span class="realtime-value">{{ formatNumber(realtime.todayRequests) }}</span>
        </div>
        <div class="realtime-card">
          <span class="realtime-label">今日Token</span>
          <span class="realtime-value">{{ formatNumber(realtime.todayTokens) }}</span>
        </div>
        <div class="realtime-card">
          <span class="realtime-label">今日成本</span>
          <span class="realtime-value">¥{{ realtime.todayCost.toFixed(2) }}</span>
        </div>
        <div class="realtime-card">
          <span class="realtime-label">最近1小时</span>
          <span class="realtime-value">{{ formatNumber(realtime.hourRequests) }}</span>
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

// 数据
const statistics = ref({
  totalRequests: 0,
  successRequests: 0,
  failedRequests: 0,
  totalTokens: 0,
  totalCost: 0,
  avgLatency: 0,
  successRate: 0
})

const modelRanking = ref<Array<{ model: string; count: number; tokens: number }>>([])
const userRanking = ref<Array<{ userId: string; username: string; count: number }>>([])
const trendData = ref<{ dates: string[]; counts: number[]; tokens: number[] }>({
  dates: [],
  counts: [],
  tokens: []
})
const realtime = ref({
  todayRequests: 0,
  todayTokens: 0,
  todayCost: 0,
  hourRequests: 0
})

// 模型标签
const modelLabels: Record<string, string> = {
  'qwen-turbo': '通义千问 Turbo',
  'qwen-plus': '通义千问 Plus',
  'qwen-max': '通义千问 Max',
  'ernie-bot-turbo': '文心一言 Turbo',
  'ernie-bot': '文心一言',
  'ernie-bot-4': '文心一言 4.0',
  'hunyuan-lite': '混元 Lite',
  'hunyuan-standard': '混元 Standard',
  'hunyuan-pro': '混元 Pro'
}

const getModelLabel = (model: string) => modelLabels[model] || model

// 格式化数字
const formatNumber = (num: number) => {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

// 计算百分比
const maxModelCount = ref(0)
const maxUserCount = ref(0)

const getModelPercent = (count: number) => {
  if (!maxModelCount.value) return 0
  return (count / maxModelCount.value) * 100
}

const getUserPercent = (count: number) => {
  if (!maxUserCount.value) return 0
  return (count / maxUserCount.value) * 100
}

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

// 更新趋势图
const updateTrendChart = () => {
  if (!trendChart) return
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['请求数', 'Token消耗'],
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
      {
        type: 'value',
        name: '请求数',
        position: 'left'
      },
      {
        type: 'value',
        name: 'Token',
        position: 'right'
      }
    ],
    series: [
      {
        name: '请求数',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        data: trendData.value.counts,
        areaStyle: {
          opacity: 0.1
        },
        itemStyle: {
          color: '#409eff'
        }
      },
      {
        name: 'Token消耗',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: trendData.value.tokens,
        areaStyle: {
          opacity: 0.1
        },
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  
  try {
    // 加载统计数据
    const statsRes = await getTenantStatistics(timeRange.value)
    if (statsRes.code === 200 && statsRes.data) {
      statistics.value = statsRes.data
    }

    // 加载模型排行
    const modelRes = await getModelRanking(10)
    if (modelRes.code === 200 && modelRes.data?.ranking) {
      modelRanking.value = modelRes.data.ranking
      maxModelCount.value = modelRanking.value[0]?.count || 0
    }

    // 加载用户排行
    const userRes = await getUserRanking(10)
    if (userRes.code === 200 && userRes.data?.ranking) {
      userRanking.value = userRes.data.ranking
      maxUserCount.value = userRanking.value[0]?.count || 0
    }

    // 加载趋势数据
    const trendRes = await getDailyTrend(timeRange.value)
    if (trendRes.code === 200 && trendRes.data) {
      trendData.value = trendRes.data
      updateTrendChart()
    }

    // 加载实时概览
    const realtimeRes = await getRealTimeOverview()
    if (realtimeRes.code === 200 && realtimeRes.data) {
      realtime.value = realtimeRes.data
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

// 窗口大小变化
const handleResize = () => {
  trendChart?.resize()
}

onMounted(() => {
  loadData()
  initTrendChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped>
.aigc-dashboard-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
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
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #ebeef5;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.charts-row:first-of-type {
  grid-template-columns: 1fr;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
}

.trend-chart .chart-container {
  height: 300px;
}

.chart-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
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

.ranking-count {
  font-size: 13px;
  color: #909399;
  min-width: 60px;
  text-align: right;
}

/* 实时概览 */
.realtime-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
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

.realtime-label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.realtime-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-cards,
  .realtime-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-cards,
  .realtime-cards {
    grid-template-columns: 1fr;
  }
}
</style>
