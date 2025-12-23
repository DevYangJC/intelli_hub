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
import { ref } from 'vue'
import {
  Refresh,
  CaretTop,
  CaretBottom,
  TrendCharts,
  WarningFilled,
  Warning,
  InfoFilled,
} from '@element-plus/icons-vue'

// 时间范围
const timeRange = ref('24h')
const selectedApi = ref('all')
const autoRefresh = ref(true)

// 实时指标
const realTimeMetrics = [
  { title: 'QPS', value: '1,234', trend: 12.5, status: 'success', statusText: '正常' },
  { title: '平均响应时间', value: '45ms', trend: -8.3, status: 'success', statusText: '正常' },
  { title: '错误率', value: '0.12%', trend: 2.1, status: 'warning', statusText: '偏高' },
  { title: '活跃连接', value: '892', trend: 5.7, status: 'success', statusText: '正常' },
]

// 状态分布
const statusDistribution = [
  { code: '2xx', name: '成功', count: '125,432', percent: 85, type: 'success' },
  { code: '3xx', name: '重定向', count: '8,234', percent: 6, type: 'info' },
  { code: '4xx', name: '客户端错误', count: '10,123', percent: 7, type: 'warning' },
  { code: '5xx', name: '服务端错误', count: '2,345', percent: 2, type: 'danger' },
]

// 告警列表
const alerts = [
  {
    id: 1,
    level: 'critical',
    title: '支付接口响应超时',
    description: '支付回调接口平均响应时间超过5秒，已持续10分钟',
    time: '5分钟前',
  },
  {
    id: 2,
    level: 'warning',
    title: 'API调用量异常',
    description: '用户认证接口调用量较平时增长300%',
    time: '15分钟前',
  },
  {
    id: 3,
    level: 'info',
    title: '证书即将过期',
    description: 'SSL证书将于7天后过期，请及时更新',
    time: '1小时前',
  },
]

// 日志列表
const logs = [
  { id: 1, time: '10:30:25', level: 'info', message: '[Gateway] 路由规则已更新' },
  { id: 2, time: '10:30:18', level: 'warn', message: '[RateLimit] 用户 user_123 触发限流' },
  { id: 3, time: '10:30:12', level: 'error', message: '[Payment] 支付回调超时: timeout after 5000ms' },
  { id: 4, time: '10:30:05', level: 'info', message: '[Auth] 新用户注册: user_456' },
  { id: 5, time: '10:29:58', level: 'info', message: '[API] 创建新API: 物流查询接口' },
]

// 刷新数据
const refreshData = () => {
  // TODO: 实现数据刷新
}
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
