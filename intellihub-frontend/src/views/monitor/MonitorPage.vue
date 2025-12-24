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
                <el-select v-model="selectedApi" placeholder="选择API" size="small" style="width: 200px">
                  <el-option label="全部API" value="all" />
                  <el-option label="用户认证接口" value="auth" />
                  <el-option label="订单查询接口" value="order" />
                  <el-option label="支付回调接口" value="payment" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon :size="48" color="#ddd"><TrendCharts /></el-icon>
                <p>API调用趋势图表</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card class="chart-card">
            <template #header>
              <span>响应状态分布</span>
            </template>
            <div class="chart-container">
              <div class="status-list">
                <div v-for="status in statusDistribution" :key="status.code" class="status-item">
                  <div class="status-info">
                    <span class="status-code" :class="status.type">{{ status.code }}</span>
                    <span class="status-name">{{ status.name }}</span>
                  </div>
                  <div class="status-bar-wrapper">
                    <div class="status-bar" :class="status.type" :style="{ width: status.percent + '%' }"></div>
                  </div>
                  <span class="status-value">{{ status.count }}</span>
                </div>
              </div>
            </div>
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
                <el-badge :value="3" class="alert-badge">
                  <el-button type="text" size="small">查看全部</el-button>
                </el-badge>
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
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import {
  Refresh,
  CaretTop,
  CaretBottom,
  TrendCharts,
  WarningFilled,
  Warning,
  InfoFilled,
} from '@element-plus/icons-vue'
import { getStatsOverview, getCallLogs, type StatsOverview, type CallLog } from '@/api/stats'
import { getAlertRecords, type AlertRecord } from '@/api/alert'

// 时间范围
const timeRange = ref('24h')
const selectedApi = ref('all')
const autoRefresh = ref(true)
let refreshTimer: number | null = null

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

// 状态分布
const statusDistribution = reactive([
  { code: '2xx', name: '成功', count: '0', percent: 100, type: 'success' },
  { code: '4xx', name: '客户端错误', count: '0', percent: 0, type: 'warning' },
  { code: '5xx', name: '服务端错误', count: '0', percent: 0, type: 'danger' },
])

// 告警列表
const alerts = ref<{ id: number; level: string; title: string; description: string; time: string }[]>([])

// 日志列表
const logs = ref<{ id: number; time: string; level: string; message: string }[]>([])

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
  const total = data.todayTotalCount || 0
  const success = data.todaySuccessCount || 0
  const fail = data.todayFailCount || 0
  
  if (total > 0) {
    statusDistribution[0].count = formatNumber(success)
    statusDistribution[0].percent = Math.round(success / total * 100)
    statusDistribution[1].count = formatNumber(Math.round(fail * 0.7))
    statusDistribution[1].percent = Math.round(fail * 0.7 / total * 100)
    statusDistribution[2].count = formatNumber(Math.round(fail * 0.3))
    statusDistribution[2].percent = Math.round(fail * 0.3 / total * 100)
  }
}

// 加载概览数据
const loadOverview = async () => {
  try {
    const res = await getStatsOverview()
    if (res.data.code === 200 && res.data.data) {
      overview.value = res.data.data
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
    if (res.data.code === 200 && res.data.data) {
      alerts.value = (res.data.data.records || []).map((record: AlertRecord) => ({
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
    const res = await getCallLogs({ page: 1, size: 5 })
    if (res.data.code === 200 && res.data.data) {
      logs.value = (res.data.data.records || []).map((log: CallLog, index: number) => ({
        id: index,
        time: formatLogTime(log.requestTime),
        level: log.success ? 'info' : 'error',
        message: `[${log.apiMethod}] ${log.apiPath} - ${log.statusCode} (${log.latency}ms)`
      }))
    }
  } catch (error) {
    console.error('加载日志失败', error)
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

// 刷新数据
const refreshData = () => {
  loadOverview()
  loadAlerts()
  loadLogs()
}

// 监听自动刷新开关
watch(autoRefresh, (val) => {
  if (val) {
    refreshTimer = window.setInterval(refreshData, 30000)
  } else if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

onMounted(() => {
  refreshData()
  if (autoRefresh.value) {
    refreshTimer = window.setInterval(refreshData, 30000)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
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
