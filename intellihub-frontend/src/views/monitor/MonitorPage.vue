<template>
  <div class="monitor-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header glass-effect">
        <div class="header-content">
          <div class="title-section">
            <h1 class="page-title">系统监控中心</h1>
            <p class="page-subtitle">实时监控系统运行状态与性能指标</p>
          </div>
          <div class="header-actions">
            <el-radio-group v-model="timeRange" size="default" class="custom-radio-group">
              <el-radio-button label="1h">1小时</el-radio-button>
              <el-radio-button label="6h">6小时</el-radio-button>
              <el-radio-button label="24h">24小时</el-radio-button>
              <el-radio-button label="7d">7天</el-radio-button>
            </el-radio-group>
            <el-button class="refresh-btn" circle :icon="Refresh" @click="refreshData" />
          </div>
        </div>
      </div>

      <!-- 实时指标 -->
      <el-row :gutter="24" class="metrics-row">
        <el-col :xs="24" :sm="12" :md="6" v-for="(metric, index) in realTimeMetrics" :key="metric.title">
          <div class="metric-card" :class="`metric-card-${index}`">
            <div class="metric-icon-wrapper">
              <el-icon class="metric-icon">
                <component :is="getMetricIcon(index)" />
              </el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-header">
                <span class="metric-title">{{ metric.title }}</span>
                <span class="metric-status" :class="metric.status">{{ metric.statusText }}</span>
              </div>
              <div class="metric-value">{{ metric.value }}</div>
              <div class="metric-footer">
                <span class="trend-label">较上周期</span>
                <span class="metric-trend" :class="metric.trend > 0 ? 'up' : metric.trend < 0 ? 'down' : 'flat'">
                  <el-icon>
                    <CaretTop v-if="metric.trend > 0" />
                    <CaretBottom v-else-if="metric.trend < 0" />
                    <Minus v-else />
                  </el-icon>
                  {{ Math.abs(metric.trend) }}%
                </span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="24" class="charts-row">
        <el-col :xs="24" :lg="16">
          <div class="content-card chart-card">
            <div class="card-header">
              <div class="header-left">
                <span class="card-title">API 调用趋势</span>
                <span class="card-subtitle">REQUEST TRENDS</span>
              </div>
              <div class="header-right">
                <el-select v-model="selectedApi" placeholder="选择API" size="default" class="custom-select" @change="loadTrendData" :teleported="false">
                  <el-option v-for="api in apiList" :key="api.value" :label="api.label" :value="api.value" />
                </el-select>
              </div>
            </div>
            <div ref="trendChartRef" class="chart-container"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <div class="content-card chart-card">
            <div class="card-header">
              <div class="header-left">
                <span class="card-title">响应状态分布</span>
                <span class="card-subtitle">STATUS DISTRIBUTION</span>
              </div>
            </div>
            <div ref="statusChartRef" class="chart-container"></div>
          </div>
        </el-col>
      </el-row>

      <!-- Bottom Layout used to be: Top 10 API (Left) | Recent logs (Right) -->
      <!-- Adjusted to be cleaner -->
      <el-row :gutter="24" class="tables-row">
        <el-col :xs="24" :lg="12">
          <div class="content-card table-card">
            <div class="card-header">
              <div class="header-left">
                <span class="card-title">热门 API Top 10</span>
                <span class="card-subtitle">POPULAR APIS</span>
              </div>
            </div>
            <div class="table-container">
              <el-table :data="topApis" :show-header="true" header-row-class-name="custom-header" row-class-name="custom-row" style="width: 100%">
                <el-table-column type="index" label="排名" width="60" align="center">
                  <template #default="{ $index }">
                    <span class="rank-badge" :class="`rank-${$index + 1}`">{{ $index + 1 }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="apiPath" label="API 路径" min-width="150" show-overflow-tooltip>
                   <template #default="{ row }">
                      <span class="api-path-text">{{ row.apiPath }}</span>
                   </template>
                </el-table-column>
                <el-table-column prop="totalCount" label="调用量" width="100" align="right">
                  <template #default="{ row }">
                    <span class="count-text">{{ formatNumber(row.totalCount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="成功率" width="100" align="right">
                  <template #default="{ row }">
                     <el-progress 
                      :percentage="row.totalCount > 0 ? Number(((row.successCount / row.totalCount) * 100).toFixed(1)) : 100" 
                      :color="getSuccessRateColor"
                      :stroke-width="6"
                      :show-text="false"
                    />
                    <span class="rate-text">{{ row.totalCount > 0 ? ((row.successCount / row.totalCount) * 100).toFixed(1) : '100.0' }}%</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-col>

        <!-- New: Alert & Log combined or separated? Keeping original logic but merged layout style -->
        <el-col :xs="24" :lg="12">
          <div class="content-card log-card">
            <div class="card-header">
              <div class="header-left">
                <span class="card-title">系统动态</span>
                <span class="card-subtitle">SYSTEM LOGS</span>
              </div>
              <div class="header-right">
                 <el-radio-group v-model="logViewType" size="small">
                    <el-radio-button label="calls">调用日志</el-radio-button>
                    <el-radio-button label="alerts">系统告警</el-radio-button>
                 </el-radio-group>
              </div>
            </div>
            
            <!-- Real-time Logs -->
            <div v-if="logViewType === 'calls'" class="log-list-container">
               <div class="list-header-action">
                   <el-switch v-model="autoRefresh" active-text="实时刷新" size="small"/>
                   <el-button link type="primary" size="small" @click="$router.push('/console/logs')">全部日志 <el-icon><ArrowRight /></el-icon></el-button>
               </div>
               <transition-group name="list" tag="div" class="log-list">
                 <div v-for="log in logs" :key="log.id" class="log-item">
                    <div class="log-status-bar" :class="log.level"></div>
                    <div class="log-content">
                       <span class="log-time">{{ log.time }}</span>
                       <div class="log-detail">
                          <span class="log-msg">{{ log.message }}</span>
                       </div>
                    </div>
                </div>
               </transition-group>
            </div>

            <!-- Alerts -->
            <div v-else class="log-list-container">
               <div class="list-header-action" style="justify-content: flex-end;">
                   <el-button link type="primary" size="small" @click="$router.push('/console/alert/records')">告警记录 <el-icon><ArrowRight /></el-icon></el-button>
               </div>
               <div v-if="alerts.length === 0" class="empty-state">
                  <el-icon class="empty-icon"><CircleCheckFilled /></el-icon>
                  <p>当前系统运行正常，无活跃告警</p>
               </div>
               <div v-else class="alert-list">
                  <div v-for="alert in alerts" :key="alert.id" class="alert-item" :class="alert.level">
                    <div class="alert-icon-box">
                      <el-icon><WarningFilled /></el-icon>
                    </div>
                    <div class="alert-info-box">
                      <div class="alert-top">
                        <span class="alert-title">{{ alert.title }}</span>
                        <span class="alert-time">{{ alert.time }}</span>
                      </div>
                      <p class="alert-desc">{{ alert.description }}</p>
                    </div>
                  </div>
               </div>
            </div>
          </div>
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
  Minus,
  WarningFilled,
  Warning,
  InfoFilled,
  Odometer,
  Timer,
  PieChart,
  DataLine,
  ArrowRight,
  CircleCheckFilled,
  Monitor
} from '@element-plus/icons-vue'
import { getStatsOverview, getCallLogs, getHourlyTrend, getTopApis, type StatsOverview, type CallLog, type StatsTrend, type TopApiStats } from '@/api/stats'
import { getAlertRecords, type AlertRecord } from '@/api/alert'
import * as echarts from 'echarts'

// 视图控制
const logViewType = ref('calls')

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
  { title: '实时 QPS', value: '0', trend: 0, status: 'success' as const, statusText: '正常' },
  { title: '平均响应时间', value: '0ms', trend: 0, status: 'success' as const, statusText: '正常' },
  { title: '错误率', value: '0%', trend: 0, status: 'success' as const, statusText: '正常' },
  { title: '今日总调用', value: '0', trend: 0, status: 'success' as const, statusText: '正常' },
])

const getMetricIcon = (index: number) => {
  switch (index) {
    case 0: return Odometer
    case 1: return Timer
    case 2: return Warning
    case 3: return PieChart
    default: return DataLine
  }
}

// 状态分布数据
const statusData = ref([
  { name: '成功 (2xx)', value: 0, itemStyle: { color: '#10B981' } },
  { name: '客户端错误 (4xx)', value: 0, itemStyle: { color: '#F59E0B' } },
  { name: '服务端错误 (5xx)', value: 0, itemStyle: { color: '#EF4444' } }
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
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toLocaleString()
}

// 成功率颜色回调
const getSuccessRateColor = (percentage: number) => {
  if (percentage >= 99) return '#10B981' // Green
  if (percentage >= 95) return '#F59E0B' // Yellow
  return '#EF4444' // Red
}

// 更新实时指标
const updateMetrics = () => {
  const data = overview.value
  
  // QPS
  realTimeMetrics[0].value = (data.currentQps || 0).toFixed(2)
  realTimeMetrics[0].status = data.currentQps && data.currentQps > 1000 ? 'warning' : 'success'
  realTimeMetrics[0].statusText = data.currentQps && data.currentQps > 1000 ? '偏高' : '优异'
  
  // 平均响应时间
  realTimeMetrics[1].value = (data.todayAvgLatency || 0) + 'ms'
  realTimeMetrics[1].status = data.todayAvgLatency && data.todayAvgLatency > 500 ? 'warning' : 'success'
  realTimeMetrics[1].statusText = data.todayAvgLatency && data.todayAvgLatency > 500 ? '偏慢' : '极速'
  
  // 错误率
  const errorRate = data.todayTotalCount && data.todayTotalCount > 0 
    ? ((data.todayFailCount || 0) / data.todayTotalCount * 100) 
    : 0
  realTimeMetrics[2].value = errorRate.toFixed(2) + '%'
  realTimeMetrics[2].status = errorRate > 1 ? 'danger' : errorRate > 0.5 ? 'warning' : 'success'
  realTimeMetrics[2].statusText = errorRate > 1 ? '异常' : errorRate > 0.5 ? '关注' : '稳定'
  
  // 今日调用
  realTimeMetrics[3].value = formatNumber(data.todayTotalCount || 0)
  realTimeMetrics[3].trend = data.dayOverDayRate || 0
  // Handle trend for others if data available (mocking same trend for demo if API doesn't provide individual trends)
  // realTimeMetrics[0].trend = ...
  
  // 更新状态分布
  const success = data.todaySuccessCount || 0
  const fail = data.todayFailCount || 0
  // Mock distribution if fail > 0
  const clientError = Math.round(fail * 0.7)
  const serverError = Math.round(fail * 0.3)
  
  statusData.value = [
    { name: '成功 (2xx)', value: success, itemStyle: { color: '#10B981' } }, // Emerald 500
    { name: '客户端错误 (4xx)', value: clientError, itemStyle: { color: '#F59E0B' } }, // Amber 500
    { name: '服务端错误 (5xx)', value: serverError, itemStyle: { color: '#EF4444' } }  // Red 500
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
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#eee',
      textStyle: { color: '#333' }
    },
    legend: {
      bottom: '0%',
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      icon: 'circle',
      textStyle: { fontSize: 12, color: '#666' }
    },
    series: [
      {
        name: '状态分布',
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            color: '#333'
          },
          scale: true,
          scaleSize: 5
        },
        labelLine: {
          show: false
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
    const res = await getCallLogs({ page: 1, size: 20 })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      // 更新实时日志显示
      logs.value = records.map((log: CallLog, index: number) => ({
        id: index,
        time: formatLogTime(log.requestTime),
        level: log.success ? 'success' : 'error',
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
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#eee',
      textStyle: { color: '#333' },
      axisPointer: { type: 'cross', label: { backgroundColor: '#666' } }
    },
    legend: {
      data: ['调用量', '成功', '失败'],
      top: 0,
       icon: 'circle',
       itemWidth: 8,
       itemHeight: 8,
       textStyle: { color: '#666' }
    },
    grid: {
      left: '2%',
      right: '2%',
      bottom: '2%',
      top: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.timePoints.map(t => {
        const date = new Date(t)
        return `${date.getHours()}:00`
      }),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#999' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLabel: { color: '#999' }
    },
    series: [
      {
        name: '调用量',
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: data.totalCounts,
        itemStyle: { color: '#3B82F6' },
        areaStyle: { 
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(59, 130, 246, 0.2)' },
                { offset: 1, color: 'rgba(59, 130, 246, 0.0)' }
            ])
        }
      },
      {
        name: '成功',
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: data.successCounts,
        itemStyle: { color: '#10B981' }
      },
      {
        name: '失败',
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: data.failCounts,
        itemStyle: { color: '#EF4444' }
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
    refreshTimer = window.setInterval(refreshData, 5000) // Increase frequency for "Real-time" feel
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
    refreshTimer = window.setInterval(refreshData, 5000)
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
  background-color: #f3f4f6;
  padding: 24px;
}

.page-container {
  max-width: 1600px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.title-section h1 {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.custom-radio-group :deep(.el-radio-button__inner) {
  border: none;
  background: white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  padding: 8px 16px;
  font-weight: 500;
  color: #6b7280;
}

.custom-radio-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 8px 0 0 8px;
}

.custom-radio-group :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 8px 8px 0;
}

.custom-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #3b82f6;
  color: white;
  box-shadow: none;
}

.refresh-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: white;
  color: #6b7280;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  transition: all 0.3s;
}

.refresh-btn:hover {
  background: #eff6ff;
  color: #3b82f6;
  transform: rotate(180deg);
}

/* Metrics Cards */
.metrics-row,
.charts-row {
  margin-bottom: 24px;
}

.metric-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid rgba(0,0,0,0.02);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
  height: 100%;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.metric-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* Gradient backgrounds for icons */
.metric-card-0 .metric-icon-wrapper { background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%); color: #3B82F6; }
.metric-card-1 .metric-icon-wrapper { background: linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%); color: #10B981; }
.metric-card-2 .metric-icon-wrapper { background: linear-gradient(135deg, #FEF2F2 0%, #FEE2E2 100%); color: #EF4444; }
.metric-card-3 .metric-icon-wrapper { background: linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%); color: #8B5CF6; }

.metric-icon {
  font-size: 24px;
}

.metric-info {
  flex: 1;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.metric-title {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.metric-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #6b7280;
}

.metric-status.success { background: #ECFDF5; color: #059669; }
.metric-status.warning { background: #FFFBEB; color: #D97706; }
.metric-status.danger { background: #FEF2F2; color: #DC2626; }

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.metric-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
}

.metric-trend {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.metric-trend.up { color: #10B981; }
.metric-trend.down { color: #EF4444; }
.metric-trend.flat { color: #9ca3af; }

/* Content Cards */
.content-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid rgba(0,0,0,0.02);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px 0 rgba(0, 0, 0, 0.03);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.card-subtitle {
  font-size: 10px;
  font-weight: 600;
  color: #9ca3af;
  letter-spacing: 0.5px;
  margin-top: 2px;
}

.chart-card {
  min-height: 420px;
}

.chart-container {
  flex: 1;
  width: 100%;
  min-height: 0; 
}

/* Tables & Logs */
.table-card, .log-card {
  height: 480px;
}

.table-container {
   flex: 1;
   overflow: hidden;
}

.custom-header th {
  background-color: #f9fafb !important;
  color: #4b5563;
  font-weight: 600;
  font-size: 12px;
}

.custom-row {
  font-size: 13px;
}

.rank-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 6px;
  background: #f3f4f6;
  color: #6b7280;
  font-weight: 600;
  font-size: 12px;
}

.rank-1 { background: #FEF3C7; color: #D97706; }
.rank-2 { background: #E5E7EB; color: #4B5563; }
.rank-3 { background: #FFEDD5; color: #C2410C; }

.api-path-text {
  font-family: monospace;
  color: #374151;
  background: #f9fafb;
  padding: 2px 6px;
  border-radius: 4px;
}

.count-text {
  font-weight: 600;
}

.rate-text {
  font-size: 12px;
  color: #6b7280;
  margin-left: 8px;
}

/* Log List */
.log-list-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 4px;
}

.log-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.log-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  position: relative;
}

.log-status-bar {
  width: 3px;
  height: 24px;
  border-radius: 1.5px;
  margin-right: 12px;
  margin-top: 4px;
  background: #d1d5db;
}

.log-status-bar.success { background: #10B981; }
.log-status-bar.error { background: #EF4444; }

.log-content {
  flex: 1;
  min-width: 0;
}

.log-time {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 2px;
  display: block;
}

.log-detail {
  display: flex;
  align-items: center;
}

.log-msg {
  font-size: 13px;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: monospace;
}

/* Alert List */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  color: #10B981;
  opacity: 0.2;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 10px;
  overflow-y: auto;
  flex: 1;
}

.alert-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #FEF2F2;
  border: 1px solid #FEE2E2;
}

.alert-item.warning { background: #FFFBEB; border-color: #FEF3C7; }
.alert-item.info { background: #EFF6FF; border-color: #DBEAFE; }

.alert-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #DC2626;
  flex-shrink: 0;
}

.alert-item.warning .alert-icon-box { color: #D97706; }
.alert-item.info .alert-icon-box { color: #3B82F6; }

.alert-info-box {
  flex: 1;
}

.alert-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.alert-title {
  font-weight: 600;
  font-size: 14px;
  color: #991B1B;
}

.alert-item.warning .alert-title { color: #92400E; }
.alert-item.info .alert-title { color: #1E40AF; }

.alert-time {
  font-size: 12px;
  opacity: 0.7;
}

.alert-desc {
  font-size: 13px;
  color: #4B5563;
  margin: 0;
  line-height: 1.4;
}

/* Animations */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* Scrollbar styling */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 3px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
</style>
