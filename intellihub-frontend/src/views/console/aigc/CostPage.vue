<template>
  <div class="cost-page">
    <!-- 顶部标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Money /></el-icon>
        成本分析
      </h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          :shortcuts="dateShortcuts"
          @change="loadData"
        />
        <el-button @click="handleExport" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 成本概览卡片 -->
    <div class="cost-overview">
      <div class="cost-card">
        <div class="cost-icon today">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="cost-info">
          <span class="cost-label">今日成本</span>
          <span class="cost-value">¥{{ costOverview.todayCost.toFixed(2) }}</span>
        </div>
      </div>
      <div class="cost-card">
        <div class="cost-icon month">
          <el-icon><DataLine /></el-icon>
        </div>
        <div class="cost-info">
          <span class="cost-label">本月成本</span>
          <span class="cost-value">¥{{ costOverview.monthCost.toFixed(2) }}</span>
        </div>
      </div>
      <div class="cost-card">
        <div class="cost-icon avg">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="cost-info">
          <span class="cost-label">日均成本</span>
          <span class="cost-value">¥{{ costOverview.avgDailyCost.toFixed(2) }}</span>
        </div>
      </div>
      <div class="cost-card forecast">
        <div class="cost-icon forecast-icon">
          <el-icon><Aim /></el-icon>
        </div>
        <div class="cost-info">
          <span class="cost-label">预测月底</span>
          <span class="cost-value">¥{{ costForecast.forecastCost.toFixed(2) }}</span>
          <span class="forecast-tip">还剩 {{ costForecast.daysRemaining }} 天</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 成本趋势 -->
      <div class="chart-card trend-card">
        <h3 class="chart-title">成本趋势</h3>
        <div class="chart-container" ref="trendChartRef"></div>
      </div>

      <!-- 模型成本分布 -->
      <div class="chart-card pie-card">
        <h3 class="chart-title">模型成本分布</h3>
        <div class="chart-container" ref="pieChartRef"></div>
      </div>
    </div>

    <!-- 详细数据表格 -->
    <div class="data-table-card">
      <div class="table-header">
        <h3 class="chart-title">成本明细</h3>
        <el-radio-group v-model="tableView" size="small">
          <el-radio-button value="daily">按日期</el-radio-button>
          <el-radio-button value="model">按模型</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 按日期视图 -->
      <el-table
        v-if="tableView === 'daily'"
        :data="dailyData"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="requests" label="请求数" width="100" align="right">
          <template #default="{ row }">
            {{ formatNumber(row.requests) }}
          </template>
        </el-table-column>
        <el-table-column prop="tokens" label="Token消耗" width="120" align="right">
          <template #default="{ row }">
            {{ formatNumber(row.tokens) }}
          </template>
        </el-table-column>
        <el-table-column prop="cost" label="成本" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.cost.toFixed(4) }}
          </template>
        </el-table-column>
        <el-table-column label="占比" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="getCostPercent(row.cost)"
              :stroke-width="8"
              :show-text="false"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 按模型视图 -->
      <el-table
        v-else
        :data="modelData"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="model" label="模型" min-width="180">
          <template #default="{ row }">
            {{ getModelLabel(row.model) }}
          </template>
        </el-table-column>
        <el-table-column prop="requests" label="请求数" width="100" align="right">
          <template #default="{ row }">
            {{ formatNumber(row.requests) }}
          </template>
        </el-table-column>
        <el-table-column prop="tokens" label="Token消耗" width="120" align="right">
          <template #default="{ row }">
            {{ formatNumber(row.tokens) }}
          </template>
        </el-table-column>
        <el-table-column prop="cost" label="成本" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.cost.toFixed(4) }}
          </template>
        </el-table-column>
        <el-table-column prop="percent" label="占比" width="100" align="right">
          <template #default="{ row }">
            {{ row.percent.toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column label="分布" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="row.percent"
              :stroke-width="8"
              :show-text="false"
              :color="getModelColor(row.model)"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import {
  Money,
  Download,
  Calendar,
  DataLine,
  TrendCharts,
  Aim
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  getCostOverview,
  getCostByModel,
  getCostByDate,
  getCostForecast,
  exportCostReport
} from '@/api/aigc'

// 状态
const loading = ref(false)
const exporting = ref(false)
const tableView = ref<'daily' | 'model'>('daily')
const trendChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 日期范围
const dateRange = ref<[string, string]>(['', ''])
const dateShortcuts = [
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 6 * 24 * 60 * 60 * 1000)
      return [start, end]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 29 * 24 * 60 * 60 * 1000)
      return [start, end]
    }
  },
  {
    text: '本月',
    value: () => {
      const end = new Date()
      const start = new Date(end.getFullYear(), end.getMonth(), 1)
      return [start, end]
    }
  }
]

// 数据
const costOverview = ref({
  totalCost: 0,
  todayCost: 0,
  monthCost: 0,
  avgDailyCost: 0
})

const costForecast = ref({
  currentCost: 0,
  forecastCost: 0,
  daysRemaining: 0,
  avgDailyCost: 0
})

const dailyData = ref<Array<{
  date: string
  requests: number
  tokens: number
  cost: number
}>>([])

const modelData = ref<Array<{
  model: string
  requests: number
  tokens: number
  cost: number
  percent: number
}>>([])

// 模型标签和颜色
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

const modelColors: Record<string, string> = {
  'qwen-turbo': '#409eff',
  'qwen-plus': '#67c23a',
  'qwen-max': '#e6a23c',
  'ernie-bot-turbo': '#f56c6c',
  'ernie-bot': '#909399',
  'ernie-bot-4': '#b37feb',
  'hunyuan-lite': '#36cfc9',
  'hunyuan-standard': '#ff85c0',
  'hunyuan-pro': '#ffc53d'
}

const getModelLabel = (model: string) => modelLabels[model] || model
const getModelColor = (model: string) => modelColors[model] || '#409eff'

// 格式化数字
const formatNumber = (num: number) => {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

// 计算最大成本
const maxDailyCost = computed(() => {
  if (dailyData.value.length === 0) return 0
  return Math.max(...dailyData.value.map(d => d.cost))
})

const getCostPercent = (cost: number) => {
  if (!maxDailyCost.value) return 0
  return (cost / maxDailyCost.value) * 100
}

// 初始化图表
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
  updateCharts()
}

// 更新图表
const updateCharts = () => {
  // 趋势图
  if (trendChart) {
    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const data = params[0]
          return `${data.name}<br/>成本: ¥${data.value.toFixed(4)}`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dailyData.value.map(d => d.date.substring(5))
      },
      yAxis: {
        type: 'value',
        name: '成本 (元)',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [
        {
          name: '成本',
          type: 'line',
          smooth: true,
          data: dailyData.value.map(d => d.cost),
          areaStyle: {
            opacity: 0.3,
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ])
          },
          itemStyle: {
            color: '#409eff'
          }
        }
      ]
    }
    trendChart.setOption(option)
  }

  // 饼图
  if (pieChart) {
    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: ¥{c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center'
      },
      series: [
        {
          name: '模型成本',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['35%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 4,
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
          data: modelData.value.map(d => ({
            value: d.cost,
            name: getModelLabel(d.model),
            itemStyle: {
              color: getModelColor(d.model)
            }
          }))
        }
      ]
    }
    pieChart.setOption(option)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  
  try {
    // 加载成本概览
    const overviewRes = await getCostOverview(30)
    if (overviewRes.code === 200 && overviewRes.data) {
      costOverview.value = overviewRes.data
    }

    // 加载成本预测
    const forecastRes = await getCostForecast()
    if (forecastRes.code === 200 && forecastRes.data) {
      costForecast.value = forecastRes.data
    }

    // 加载按日期成本
    const dateRes = await getCostByDate(30)
    if (dateRes.code === 200 && dateRes.data) {
      dailyData.value = dateRes.data.dates.map((date: string, i: number) => ({
        date,
        requests: 0,
        tokens: 0,
        cost: dateRes.data.costs[i] || 0
      }))
    }

    // 加载按模型成本
    const modelRes = await getCostByModel(30)
    if (modelRes.code === 200 && modelRes.data?.models) {
      modelData.value = modelRes.data.models
    }

    updateCharts()
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

// 导出报表
const handleExport = async () => {
  if (!dateRange.value[0] || !dateRange.value[1]) {
    ElMessage.warning('请选择导出的日期范围')
    return
  }

  exporting.value = true
  try {
    const res = await exportCostReport(dateRange.value[0], dateRange.value[1])
    if (res.code === 200 && res.data) {
      // 简单导出为CSV
      const { data, summary } = res.data
      let csv = '日期,模型,请求数,Token消耗,成本\n'
      data.forEach(row => {
        csv += `${row.date},${row.model},${row.requests},${row.tokens},${row.cost}\n`
      })
      csv += `\n汇总,总请求: ${summary.totalRequests},总Token: ${summary.totalTokens},总成本: ${summary.totalCost}\n`

      const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `AIGC成本报表_${dateRange.value[0]}_${dateRange.value[1]}.csv`
      link.click()
      URL.revokeObjectURL(url)

      ElMessage.success('导出成功')
    }
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 窗口大小变化
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  // 设置默认日期范围
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 29 * 24 * 60 * 60 * 1000)
  dateRange.value = [
    start.toISOString().substring(0, 10),
    end.toISOString().substring(0, 10)
  ]

  loadData()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.cost-page {
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

/* 成本概览 */
.cost-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.cost-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #ebeef5;
}

.cost-card.forecast {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
}

.cost-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.cost-icon.today {
  background: #e6f4ff;
  color: #1890ff;
}

.cost-icon.month {
  background: #f0fdf4;
  color: #22c55e;
}

.cost-icon.avg {
  background: #fef3c7;
  color: #f59e0b;
}

.cost-icon.forecast-icon {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.cost-info {
  display: flex;
  flex-direction: column;
}

.cost-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.cost-card.forecast .cost-label {
  color: rgba(255, 255, 255, 0.8);
}

.cost-value {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.cost-card.forecast .cost-value {
  color: #fff;
}

.forecast-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

/* 图表 */
.charts-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
}

.chart-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 280px;
}

/* 数据表格 */
.data-table-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .cost-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .cost-overview {
    grid-template-columns: 1fr;
  }
}
</style>
