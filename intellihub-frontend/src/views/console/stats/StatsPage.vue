<template>
  <div class="stats-page">
    <!-- 统计概览卡片 -->
    <el-row :gutter="16" class="overview-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(overview.todayTotalCount) }}</div>
              <div class="stat-label">今日调用量</div>
              <div class="stat-trend" :class="overview.dayOverDayRate >= 0 ? 'up' : 'down'">
                <el-icon><CaretTop v-if="overview.dayOverDayRate >= 0" /><CaretBottom v-else /></el-icon>
                {{ Math.abs(overview.dayOverDayRate || 0).toFixed(1) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%)">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (overview.todaySuccessRate || 100).toFixed(2) }}%</div>
              <div class="stat-label">成功率</div>
              <div class="stat-sub">成功 {{ formatNumber(overview.todaySuccessCount) }} / 失败 {{ formatNumber(overview.todayFailCount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.todayAvgLatency || 0 }}<span class="unit">ms</span></div>
              <div class="stat-label">平均响应时间</div>
              <div class="stat-sub">当前QPS: {{ (overview.currentQps || 0).toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(overview.yesterdayTotalCount) }}</div>
              <div class="stat-label">昨日调用量</div>
              <div class="stat-sub">环比 {{ overview.dayOverDayRate >= 0 ? '+' : '' }}{{ (overview.dayOverDayRate || 0).toFixed(1) }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>调用趋势</span>
              <el-radio-group v-model="trendType" size="small" @change="loadTrendData">
                <el-radio-button value="hourly">小时</el-radio-button>
                <el-radio-button value="daily">天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>成功率分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Top API 和 最近调用 -->
    <el-row :gutter="16" class="table-row">
      <el-col :span="12">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <div class="card-header">
              <span>Top 10 API</span>
              <el-button type="primary" link size="small">查看全部</el-button>
            </div>
          </template>
          <el-table :data="topApis" stripe size="small" max-height="320">
            <el-table-column prop="apiPath" label="API路径" min-width="180" show-overflow-tooltip />
            <el-table-column prop="totalCount" label="调用量" width="100" align="right">
              <template #default="{ row }">
                {{ formatNumber(row.totalCount) }}
              </template>
            </el-table-column>
            <el-table-column label="成功率" width="100" align="right">
              <template #default="{ row }">
                <span :class="getSuccessRateClass(row)">
                  {{ row.totalCount > 0 ? ((row.successCount / row.totalCount) * 100).toFixed(1) : '100.0' }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="avgLatency" label="平均延迟" width="100" align="right">
              <template #default="{ row }">
                {{ row.avgLatency }}ms
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <div class="card-header">
              <span>最近调用</span>
              <el-button type="primary" link size="small" @click="$router.push('/console/logs')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentLogs" stripe size="small" max-height="320">
            <el-table-column prop="apiPath" label="API路径" min-width="150" show-overflow-tooltip />
            <el-table-column prop="apiMethod" label="方法" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="getMethodTagType(row.apiMethod)" size="small">{{ row.apiMethod }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.success ? 'success' : 'danger'" size="small">
                  {{ row.statusCode }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="latency" label="延迟" width="80" align="right">
              <template #default="{ row }">
                {{ row.latency }}ms
              </template>
            </el-table-column>
            <el-table-column prop="requestTime" label="时间" width="90" align="right">
              <template #default="{ row }">
                {{ formatTime(row.requestTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Connection, CircleCheck, Timer, TrendCharts, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { 
  getStatsOverview, 
  getHourlyTrend, 
  getDailyTrend, 
  getTopApis, 
  getCallLogs,
  type StatsOverview,
  type StatsTrend,
  type TopApiStats,
  type CallLog
} from '@/api/stats'

// 数据
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

const trendType = ref<'hourly' | 'daily'>('hourly')
const topApis = ref<TopApiStats[]>([])
const recentLogs = ref<CallLog[]>([])

// 图表引用
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 定时刷新
let refreshTimer: number | null = null

// 格式化数字
const formatNumber = (num: number | undefined) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
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

// 获取成功率样式类
const getSuccessRateClass = (row: TopApiStats) => {
  if (!row.totalCount || row.totalCount === 0) return 'success-rate-high'
  const rate = (row.successCount / row.totalCount) * 100
  if (rate >= 99) return 'success-rate-high'
  if (rate >= 95) return 'success-rate-mid'
  return 'success-rate-low'
}

// 加载概览数据
const loadOverview = async () => {
  try {
    const res = await getStatsOverview()
    if (res.code === 200 && res.data) {
      overview.value = res.data
      updatePieChart()
    }
  } catch (error) {
    console.error('加载概览数据失败', error)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const now = new Date()
    let res
    
    if (trendType.value === 'hourly') {
      const endTime = now.toISOString().slice(0, 19).replace('T', ' ')
      const startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000).toISOString().slice(0, 19).replace('T', ' ')
      res = await getHourlyTrend(startTime, endTime)
    } else {
      const endDate = now.toISOString().slice(0, 10)
      const startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)
      res = await getDailyTrend(startDate, endDate)
    }
    
    if (res.code === 200 && res.data) {
      updateTrendChart(res.data)
    }
  } catch (error) {
    console.error('加载趋势数据失败', error)
  }
}

// 加载Top API
const loadTopApis = async () => {
  try {
    const res = await getTopApis(10)
    if (res.code === 200 && res.data) {
      topApis.value = res.data
    }
  } catch (error) {
    console.error('加载Top API失败', error)
  }
}

// 加载最近调用
const loadRecentLogs = async () => {
  try {
    const res = await getCallLogs({ page: 1, size: 10 })
    if (res.code === 200 && res.data) {
      recentLogs.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载最近调用失败', error)
  }
}

// 初始化趋势图表
const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['调用量', '成功率'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '40px',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: [
      {
        type: 'value',
        name: '调用量',
        position: 'left'
      },
      {
        type: 'value',
        name: '成功率',
        position: 'right',
        max: 100,
        axisLabel: { formatter: '{value}%' }
      }
    ],
    series: [
      {
        name: '调用量',
        type: 'bar',
        data: [],
        itemStyle: { color: '#409eff' }
      },
      {
        name: '成功率',
        type: 'line',
        yAxisIndex: 1,
        data: [],
        itemStyle: { color: '#67c23a' },
        smooth: true
      }
    ]
  })
}

// 更新趋势图表
const updateTrendChart = (data: StatsTrend) => {
  if (!trendChart) return
  trendChart.setOption({
    xAxis: { data: data.timePoints },
    series: [
      { name: '调用量', data: data.totalCounts },
      { name: '成功率', data: data.successRates }
    ]
  })
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{d}%'
        },
        data: [
          { value: 0, name: '成功', itemStyle: { color: '#67c23a' } },
          { value: 0, name: '失败', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  })
}

// 更新饼图
const updatePieChart = () => {
  if (!pieChart) return
  pieChart.setOption({
    series: [
      {
        data: [
          { value: overview.value.todaySuccessCount, name: '成功' },
          { value: overview.value.todayFailCount, name: '失败' }
        ]
      }
    ]
  })
}

// 刷新所有数据
const refreshData = () => {
  loadOverview()
  loadTrendData()
  loadTopApis()
  loadRecentLogs()
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  initTrendChart()
  initPieChart()
  refreshData()
  
  // 每30秒刷新一次
  refreshTimer = window.setInterval(refreshData, 30000)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped lang="scss">
.stats-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.overview-cards {
  margin-bottom: 16px;
}

.stat-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
  
  .unit {
    font-size: 14px;
    font-weight: normal;
    color: #909399;
    margin-left: 2px;
  }
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-trend {
  font-size: 13px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 2px;
  
  &.up {
    color: #67c23a;
  }
  &.down {
    color: #f56c6c;
  }
}

.stat-sub {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.chart-row, .table-row {
  margin-bottom: 16px;
}

.chart-card, .table-card {
  height: 400px;
  
  :deep(.el-card__header) {
    padding: 12px 20px;
    border-bottom: 1px solid #ebeef5;
  }
  
  :deep(.el-card__body) {
    padding: 16px;
    height: calc(100% - 57px);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  span {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
  }
}

.chart-container {
  width: 100%;
  height: 100%;
}

.success-rate-high {
  color: #67c23a;
  font-weight: 500;
}

.success-rate-mid {
  color: #e6a23c;
  font-weight: 500;
}

.success-rate-low {
  color: #f56c6c;
  font-weight: 500;
}
</style>
