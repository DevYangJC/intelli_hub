<template>
  <div class="monitor-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">监控中心</h1>
        <div class="header-actions">
          <el-radio-group v-model="timeRange" size="small">
            <el-radio-button label="1h">1小时</el-radio-button>
            <el-radio-button label="6h">6小时</el-radio-button>
            <el-radio-button label="24h">24小时</el-radio-button>
            <el-radio-button label="7d">7天</el-radio-button>
          </el-radio-group>
          <el-button type="primary" :icon="Refresh" @click="refreshData">刷新</el-button>
        </div>
      </div>

      <!-- 实时指标 -->
      <el-row :gutter="20" class="metrics-row">
        <el-col :xs="24" :sm="12" :md="6" v-for="metric in realTimeMetrics" :key="metric.title">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-content">
              <div class="metric-header">
                <span class="metric-title">{{ metric.title }}</span>
                <el-tag :type="metric.status" size="small">{{ metric.statusText }}</el-tag>
              </div>
              <div class="metric-value">{{ metric.value }}</div>
              <div class="metric-trend" :class="metric.trend > 0 ? 'up' : 'down'">
                <el-icon>
                  <CaretTop v-if="metric.trend > 0" />
                  <CaretBottom v-else />
                </el-icon>
                {{ Math.abs(metric.trend) }}% 较上周期
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20">
        <el-col :xs="24" :lg="16">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>API调用趋势</span>
                <el-select v-model="selectedApi" placeholder="选择API" size="small" style="width: 200px" @change="loadTrendData">
                  <el-option v-for="api in apiList" :key="api.value" :label="api.label" :value="api.value" />
                </el-select>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card class="chart-card">
            <template #header>
              <span>响应状态分布</span>
            </template>
            <div ref="statusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Top API 和最近调用 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :xs="24" :lg="12">
          <el-card class="table-card">
            <template #header>
              <div class="card-header">
                <span>Top 10 API</span>
              </div>
            </template>
            <el-table :data="topApis" stripe size="small" max-height="280">
              <el-table-column prop="apiPath" label="API路径" min-width="180" show-overflow-tooltip />
              <el-table-column prop="totalCount" label="调用量" width="90" align="right">
                <template #default="{ row }">
                  {{ formatNumber(row.totalCount || 0) }}
                </template>
              </el-table-column>
              <el-table-column label="成功率" width="80" align="right">
                <template #default="{ row }">
                  <span :class="getSuccessRateClass(row)">
                    {{ row.totalCount > 0 ? ((row.successCount / row.totalCount) * 100).toFixed(1) : '100.0' }}%
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="avgLatency" label="延迟" width="70" align="right">
                <template #default="{ row }">
                  {{ row.avgLatency }}ms
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card class="table-card">
            <template #header>
              <div class="card-header">
                <span>最近调用</span>
                <el-button type="primary" link size="small" @click="$router.push('/console/logs')">查看全部</el-button>
              </div>
            </template>
            <el-table :data="recentLogs" stripe size="small" max-height="280">
              <el-table-column prop="apiPath" label="API路径" min-width="150" show-overflow-tooltip />
              <el-table-column prop="apiMethod" label="方法" width="70" align="center">
                <template #default="{ row }">
                  <el-tag :type="getMethodTagType(row.apiMethod)" size="small">{{ row.apiMethod }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="65" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.success ? 'success' : 'danger'" size="small">{{ row.statusCode }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="latency" label="延迟" width="70" align="right">
                <template #default="{ row }">{{ row.latency }}ms</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <!-- 告警和日志 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :xs="24" :lg="12">
          <el-card class="alert-card">
            <template #header>
              <div class="card-header">
                <span>最新告警</span>
                <el-button type="primary" link size="small" @click="$router.push('/console/alert/records')">查看全部</el-button>
              </div>
            </template>
            <div class="alert-list">
              <div v-for="alert in alerts" :key="alert.id" class="alert-item" :class="alert.level">
                <el-icon class="alert-icon">
                  <WarningFilled v-if="alert.level === 'critical'" />
                  <Warning v-else-if="alert.level === 'warning'" />
                  <InfoFilled v-else />
                </el-icon>
                <div class="alert-content">
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-desc">{{ alert.description }}</div>
                  <div class="alert-time">{{ alert.time }}</div>
                </div>
              </div>
              <el-empty v-if="alerts.length === 0" description="暂无告警" :image-size="60" />
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card class="log-card">
            <template #header>
              <div class="card-header">
                <span>实时日志</span>
                <el-switch v-model="autoRefresh" active-text="自动刷新" />
              </div>
            </template>
            <div class="log-list">
              <div v-for="log in logs" :key="log.id" class="log-item">
                <span class="log-time">{{ log.time }}</span>
                <el-tag :type="log.level === 'error' ? 'danger' : log.level === 'warn' ? 'warning' : 'info'" size="small">
                  {{ log.level.toUpperCase() }}
                </el-tag>
                <span class="log-message">{{ log.message }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import {
  Refresh,
  CaretTop,
  CaretBottom,
  TrendCharts,
  WarningFilled,
  Warning,
  InfoFilled,
} from '@element-plus/icons-vue'
import { getStatsOverview, getCallLogs, getHourlyTrend, getTopApis, type StatsOverview, type CallLog, type StatsTrend, type TopApiStats } from '@/api/stats'
import { getAlertRecords, type AlertRecord } from '@/api/alert'
import * as echarts from 'echarts'

// 时间范围
const timeRange = ref('24h')
const selectedApi = ref('all')
const autoRefresh = ref(true)
let refreshTimer: number | null = null

// 图表实例
const trendChartRef = ref<HTMLElement | null>(null)
const statusChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null

// API列表
const apiList = ref<{ value: string; label: string }[]>([{ value: 'all', label: '全部API' }])

// 统计概览数据
const overview = ref<StatsOverview>({
  todayTotalCount: 0,
  todaySuccessCount: 0,
  todayFailCount: 0,
  todaySuccessRate: 100,
  todayAvgLatency: 0,
  yesterdayTotalCount: 0,
  dayOverDayRate: 0,
  currentQps: 0
})

// 实时指标
const realTimeMetrics = reactive([
  { title: 'QPS', value: '0', trend: 0, status: 'success' as const, statusText: '正常' },
  { title: '平均响应时间', value: '0ms', trend: 0, status: 'success' as const, statusText: '正常' },
  { title: '错误率', value: '0%', trend: 0, status: 'success' as const, statusText: '正常' },
  { title: '今日调用', value: '0', trend: 0, status: 'success' as const, statusText: '正常' },
])

// 状态分布数据
const statusData = ref([
  { name: '成功 (2xx)', value: 0, itemStyle: { color: '#52c41a' } },
  { name: '客户端错误 (4xx)', value: 0, itemStyle: { color: '#faad14' } },
  { name: '服务端错误 (5xx)', value: 0, itemStyle: { color: '#ff4d4f' } }
])

// 告警列表
const alerts = ref<{ id: number; level: string; title: string; description: string; time: string }[]>([])

// 日志列表
const logs = ref<{ id: number; time: string; level: string; message: string }[]>([])

// Top API 列表
const topApis = ref<TopApiStats[]>([])

// 最近调用日志
const recentLogs = ref<CallLog[]>([])

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toLocaleString()
}

// 更新实时指标
const updateMetrics = () => {
  const data = overview.value
  
  // QPS
  realTimeMetrics[0].value = (data.currentQps || 0).toFixed(2)
  realTimeMetrics[0].status = data.currentQps && data.currentQps > 1000 ? 'warning' : 'success'
  realTimeMetrics[0].statusText = data.currentQps && data.currentQps > 1000 ? '偏高' : '正常'
  
  // 平均响应时间
  realTimeMetrics[1].value = (data.todayAvgLatency || 0) + 'ms'
  realTimeMetrics[1].status = data.todayAvgLatency && data.todayAvgLatency > 500 ? 'warning' : 'success'
  realTimeMetrics[1].statusText = data.todayAvgLatency && data.todayAvgLatency > 500 ? '偏高' : '正常'
  
  // 错误率
  const errorRate = data.todayTotalCount && data.todayTotalCount > 0 
    ? ((data.todayFailCount || 0) / data.todayTotalCount * 100) 
    : 0
  realTimeMetrics[2].value = errorRate.toFixed(2) + '%'
  realTimeMetrics[2].status = errorRate > 1 ? 'danger' : errorRate > 0.5 ? 'warning' : 'success'
  realTimeMetrics[2].statusText = errorRate > 1 ? '异常' : errorRate > 0.5 ? '偏高' : '正常'
  
  // 今日调用
  realTimeMetrics[3].value = formatNumber(data.todayTotalCount || 0)
  realTimeMetrics[3].trend = data.dayOverDayRate || 0
  realTimeMetrics[3].status = 'success'
  realTimeMetrics[3].statusText = '正常'
  
  // 更新状态分布
  const success = data.todaySuccessCount || 0
  const fail = data.todayFailCount || 0
  
  statusData.value = [
    { name: '成功 (2xx)', value: success, itemStyle: { color: '#52c41a' } },
    { name: '客户端错误 (4xx)', value: Math.round(fail * 0.7), itemStyle: { color: '#faad14' } },
    { name: '服务端错误 (5xx)', value: Math.round(fail * 0.3), itemStyle: { color: '#ff4d4f' } }
  ]
  
  renderStatusChart()
}

// 渲染状态分布饼图
const renderStatusChart = () => {
  if (!statusChartRef.value) return
  
  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value)
  }
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 12 }
    },
    series: [
      {
        name: '状态分布',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: statusData.value
      }
    ]
  }
  
  statusChart.setOption(option)
}

// 加载概览数据
const loadOverview = async () => {
  try {
    const res = await getStatsOverview()
    if (res.code === 200 && res.data) {
      overview.value = res.data
      updateMetrics()
    }
  } catch (error) {
    console.error('加载概览数据失败', error)
  }
}

// 加载告警记录
const loadAlerts = async () => {
  try {
    const res = await getAlertRecords({ pageNum: 1, pageSize: 5, status: 'firing' })
    if (res.code === 200 && res.data) {
      alerts.value = (res.data.records || []).map((record: AlertRecord) => ({
        id: record.id,
        level: record.alertLevel || 'info',
        title: record.ruleName || '告警',
        description: record.alertMessage || '',
        time: formatAlertTime(record.firedAt)
      }))
    }
  } catch (error) {
    console.error('加载告警记录失败', error)
  }
}

// 加载最近日志
const loadLogs = async () => {
  try {
    const res = await getCallLogs({ page: 1, size: 10 })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      // 更新实时日志显示
      logs.value = records.slice(0, 5).map((log: CallLog, index: number) => ({
        id: index,
        time: formatLogTime(log.requestTime),
        level: log.success ? 'info' : 'error',
        message: `[${log.apiMethod}] ${log.apiPath} - ${log.statusCode} (${log.latency}ms)`
      }))
      // 更新最近调用表格
      recentLogs.value = records
    }
  } catch (error) {
    console.error('加载日志失败', error)
  }
}

// 加载 Top API
const loadTopApis = async () => {
  try {
    const res = await getTopApis(10)
    if (res.code === 200 && res.data) {
      topApis.value = res.data as TopApiStats[]
    }
  } catch (error) {
    console.error('加载 Top API 失败', error)
  }
}

// 获取成功率样式类
const getSuccessRateClass = (row: TopApiStats) => {
  if (!row.totalCount || row.totalCount === 0) return 'success-rate-high'
  const rate = (row.successCount / row.totalCount) * 100
  if (rate >= 99) return 'success-rate-high'
  if (rate >= 95) return 'success-rate-mid'
  return 'success-rate-low'
}

// 获取方法标签类型
const getMethodTagType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return types[method] || 'info'
}

// 格式化告警时间
const formatAlertTime = (time: string) => {
  if (!time) return ''
  const now = new Date()
  const alertTime = new Date(time)
  const diff = Math.floor((now.getTime() - alertTime.getTime()) / 1000 / 60)
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  return `${Math.floor(diff / 1440)}天前`
}

// 格式化日志时间
const formatLogTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`
}

// 获取时间范围
const getTimeRange = () => {
  const now = new Date()
  let startTime: Date
  
  switch (timeRange.value) {
    case '1h':
      startTime = new Date(now.getTime() - 60 * 60 * 1000)
      break
    case '6h':
      startTime = new Date(now.getTime() - 6 * 60 * 60 * 1000)
      break
    case '24h':
      startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000)
      break
    case '7d':
      startTime = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      break
    default:
      startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000)
  }
  
  const formatDate = (date: Date) => {
    return date.toISOString().slice(0, 19).replace('T', ' ')
  }
  
  return {
    startTime: formatDate(startTime),
    endTime: formatDate(now)
  }
}

// 加载 API 列表
const loadApiList = async () => {
  try {
    const res = await getTopApis(50)
    if (res.code === 200 && res.data) {
      const apis = res.data as TopApiStats[]
      apiList.value = [
        { value: 'all', label: '全部API' },
        ...apis.map(api => ({ value: api.apiPath, label: api.apiPath }))
      ]
    }
  } catch (error) {
    console.error('加载API列表失败', error)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const { startTime, endTime } = getTimeRange()
    const res = await getHourlyTrend(startTime, endTime)
    if (res.code === 200 && res.data) {
      renderTrendChart(res.data)
    }
  } catch (error) {
    console.error('加载趋势数据失败', error)
  }
}

// 渲染趋势图表
const renderTrendChart = (data: StatsTrend) => {
  if (!trendChartRef.value) return
  
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['调用量', '成功', '失败'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.timePoints.map(t => {
        const date = new Date(t)
        return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:00`
      })
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '调用量',
        type: 'line',
        smooth: true,
        data: data.totalCounts,
        itemStyle: { color: '#667eea' },
        areaStyle: { color: 'rgba(102, 126, 234, 0.1)' }
      },
      {
        name: '成功',
        type: 'line',
        smooth: true,
        data: data.successCounts,
        itemStyle: { color: '#52c41a' }
      },
      {
        name: '失败',
        type: 'line',
        smooth: true,
        data: data.failCounts,
        itemStyle: { color: '#ff4d4f' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 刷新数据
const refreshData = () => {
  loadOverview()
  loadAlerts()
  loadLogs()
  loadTrendData()
  loadTopApis()
}

// 监听时间范围变化
watch(timeRange, () => {
  loadTrendData()
})

// 监听自动刷新开关
watch(autoRefresh, (val) => {
  if (val) {
    refreshTimer = window.setInterval(refreshData, 30000)
  } else if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

// 窗口大小变化时重绘图表
const handleResize = () => {
  trendChart?.resize()
  statusChart?.resize()
}

onMounted(async () => {
  await loadApiList()
  refreshData()
  if (autoRefresh.value) {
    refreshTimer = window.setInterval(refreshData, 30000)
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  statusChart?.dispose()
})
</script>

<style scoped>
.monitor-page {
  min-height: calc(100vh - 56px);
  background: #f5f7fa;
  padding: 24px;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 指标卡片 */
.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  margin-bottom: 16px;
}

.metric-content {
  padding: 4px 0;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.metric-title {
  font-size: 14px;
  color: #666;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.metric-trend.up {
  color: #52c41a;
}

.metric-trend.down {
  color: #ff4d4f;
}

/* 图表卡片 */
.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.chart-container {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #999;
}

.chart-placeholder p {
  margin-top: 12px;
}

/* 状态分布 */
.status-list {
  width: 100%;
  padding: 0 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.status-info {
  width: 100px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-code {
  font-weight: 600;
  font-size: 14px;
}

.status-code.success { color: #52c41a; }
.status-code.info { color: #1890ff; }
.status-code.warning { color: #faad14; }
.status-code.danger { color: #ff4d4f; }

.status-name {
  font-size: 12px;
  color: #999;
}

.status-bar-wrapper {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.status-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.status-bar.success { background: #52c41a; }
.status-bar.info { background: #1890ff; }
.status-bar.warning { background: #faad14; }
.status-bar.danger { background: #ff4d4f; }

.status-value {
  width: 80px;
  text-align: right;
  font-size: 13px;
  color: #666;
}

/* 告警卡片 */
/* 表格卡片 */
.table-card {
  height: 360px;
}

.success-rate-high { color: #52c41a; font-weight: 500; }
.success-rate-mid { color: #faad14; font-weight: 500; }
.success-rate-low { color: #ff4d4f; font-weight: 500; }

.alert-card,
.log-card {
  height: 360px;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #fafafa;
}

.alert-item.critical {
  background: #fff1f0;
}

.alert-item.warning {
  background: #fffbe6;
}

.alert-icon {
  font-size: 20px;
  margin-top: 2px;
}

.alert-item.critical .alert-icon { color: #ff4d4f; }
.alert-item.warning .alert-icon { color: #faad14; }
.alert-item.info .alert-icon { color: #1890ff; }

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.alert-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.alert-time {
  font-size: 12px;
  color: #999;
}

/* 日志卡片 */
.log-list {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #1e1e1e;
  border-radius: 8px;
  padding: 12px;
  height: 260px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #333;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #888;
}

.log-message {
  color: #d4d4d4;
  flex: 1;
}

/* 响应式 */
@media (max-width: 768px) {
  .monitor-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-card,
  .alert-card,
  .log-card {
    height: auto;
    min-height: 300px;
    margin-bottom: 16px;
  }
}
</style>
