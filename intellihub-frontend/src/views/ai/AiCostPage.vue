<template>
  <div class="ai-cost-page">
    <div class="page-container">
      <!-- 顶部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon><Money /></el-icon>
            成本分析
          </h1>
          <p class="page-desc">分析 AI 服务的成本消耗和预测</p>
        </div>
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
            导出
          </el-button>
        </div>
      </div>

      <!-- 成本卡片 -->
      <div class="cost-cards">
        <div class="cost-card">
          <div class="cost-icon blue">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="cost-content">
            <span class="cost-value">¥{{ costOverview.todayCost.toFixed(2) }}</span>
            <span class="cost-label">今日成本</span>
          </div>
        </div>
        <div class="cost-card">
          <div class="cost-icon green">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="cost-content">
            <span class="cost-value">¥{{ costOverview.monthCost.toFixed(2) }}</span>
            <span class="cost-label">本月成本</span>
          </div>
        </div>
        <div class="cost-card">
          <div class="cost-icon orange">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="cost-content">
            <span class="cost-value">¥{{ costOverview.avgDailyCost.toFixed(2) }}</span>
            <span class="cost-label">日均成本</span>
          </div>
        </div>
        <div class="cost-card forecast">
          <div class="cost-icon">
            <el-icon><Aim /></el-icon>
          </div>
          <div class="cost-content">
            <span class="cost-value">¥{{ costForecast.forecastCost.toFixed(2) }}</span>
            <span class="cost-label">预测月底</span>
            <span class="cost-tip">还剩 {{ costForecast.daysRemaining }} 天</span>
          </div>
        </div>
      </div>

      <!-- 图表区 -->
      <div class="charts-section">
        <div class="chart-card trend-card">
          <h3 class="chart-title">成本趋势</h3>
          <div class="chart-body" ref="trendChartRef"></div>
        </div>
        <div class="chart-card pie-card">
          <h3 class="chart-title">模型成本分布</h3>
          <div class="chart-body" ref="pieChartRef"></div>
        </div>
      </div>

      <!-- 明细表格 -->
      <div class="table-section">
        <div class="table-header">
          <h3 class="table-title">成本明细</h3>
          <el-radio-group v-model="tableView" size="small">
            <el-radio-button value="daily">按日期</el-radio-button>
            <el-radio-button value="model">按模型</el-radio-button>
          </el-radio-group>
        </div>

        <el-table :data="tableView === 'daily' ? dailyData : modelData" v-loading="loading">
          <template v-if="tableView === 'daily'">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="requests" label="请求数" width="100" align="right">
              <template #default="{ row }">{{ formatNumber(row.requests) }}</template>
            </el-table-column>
            <el-table-column prop="tokens" label="Token" width="120" align="right">
              <template #default="{ row }">{{ formatNumber(row.tokens) }}</template>
            </el-table-column>
            <el-table-column prop="cost" label="成本" width="100" align="right">
              <template #default="{ row }">¥{{ row.cost.toFixed(4) }}</template>
            </el-table-column>
            <el-table-column label="占比" width="150">
              <template #default="{ row }">
                <el-progress :percentage="getCostPercent(row.cost)" :stroke-width="8" :show-text="false" />
              </template>
            </el-table-column>
          </template>
          <template v-else>
            <el-table-column prop="model" label="模型" min-width="180">
              <template #default="{ row }">{{ getModelLabel(row.model) }}</template>
            </el-table-column>
            <el-table-column prop="cost" label="成本" width="100" align="right">
              <template #default="{ row }">¥{{ row.cost.toFixed(4) }}</template>
            </el-table-column>
            <el-table-column prop="percent" label="占比" width="100" align="right">
              <template #default="{ row }">{{ row.percent.toFixed(1) }}%</template>
            </el-table-column>
            <el-table-column label="分布" width="150">
              <template #default="{ row }">
                <el-progress :percentage="row.percent" :stroke-width="8" :show-text="false" />
              </template>
            </el-table-column>
          </template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
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

const dateRange = ref<[string, string]>(['', ''])
const dateShortcuts = [
  { text: '最近7天', value: () => { const e = new Date(), s = new Date(); s.setDate(s.getDate() - 6); return [s, e] } },
  { text: '最近30天', value: () => { const e = new Date(), s = new Date(); s.setDate(s.getDate() - 29); return [s, e] } },
  { text: '本月', value: () => { const e = new Date(), s = new Date(e.getFullYear(), e.getMonth(), 1); return [s, e] } }
]

const costOverview = ref({ totalCost: 0, todayCost: 0, monthCost: 0, avgDailyCost: 0 })
const costForecast = ref({ currentCost: 0, forecastCost: 0, daysRemaining: 0, avgDailyCost: 0 })
const dailyData = ref<Array<{ date: string; requests: number; tokens: number; cost: number }>>([])
const modelData = ref<Array<{ model: string; cost: number; percent: number }>>([])

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

const maxDailyCost = computed(() => dailyData.value.length ? Math.max(...dailyData.value.map(d => d.cost)) : 0)
const getCostPercent = (cost: number) => maxDailyCost.value ? (cost / maxDailyCost.value) * 100 : 0

// 图表
const initCharts = () => {
  if (trendChartRef.value) trendChart = echarts.init(trendChartRef.value)
  if (pieChartRef.value) pieChart = echarts.init(pieChartRef.value)
  updateCharts()
}

const updateCharts = () => {
  if (trendChart) {
    trendChart.setOption({
      tooltip: { trigger: 'axis', formatter: (params: any) => `${params[0].name}<br/>成本: ¥${params[0].value.toFixed(4)}` },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: dailyData.value.map(d => d.date.substring(5)) },
      yAxis: { type: 'value', name: '成本 (元)', axisLabel: { formatter: '¥{value}' } },
      series: [{
        name: '成本',
        type: 'line',
        smooth: true,
        data: dailyData.value.map(d => d.cost),
        areaStyle: { opacity: 0.3, color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ])},
        itemStyle: { color: '#409eff' }
      }]
    })
  }

  if (pieChart) {
    const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#b37feb', '#36cfc9', '#ff85c0']
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
      legend: { orient: 'vertical', right: '5%', top: 'center' },
      series: [{
        name: '模型成本',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: modelData.value.map((d, i) => ({
          value: d.cost,
          name: getModelLabel(d.model),
          itemStyle: { color: colors[i % colors.length] }
        }))
      }]
    })
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const [overviewRes, forecastRes, dateRes, modelRes] = await Promise.all([
      getCostOverview(30),
      getCostForecast(),
      getCostByDate(30),
      getCostByModel(30)
    ])

    if (overviewRes.code === 200 && overviewRes.data) costOverview.value = overviewRes.data
    if (forecastRes.code === 200 && forecastRes.data) costForecast.value = forecastRes.data
    if (dateRes.code === 200 && dateRes.data) {
      dailyData.value = dateRes.data.dates.map((date: string, i: number) => ({
        date, requests: 0, tokens: 0, cost: dateRes.data.costs[i] || 0
      }))
    }
    if (modelRes.code === 200 && modelRes.data?.models) modelData.value = modelRes.data.models

    updateCharts()
  } finally {
    loading.value = false
  }
}

// 导出
const handleExport = async () => {
  if (!dateRange.value[0] || !dateRange.value[1]) {
    ElMessage.warning('请选择日期范围')
    return
  }
  exporting.value = true
  try {
    const res = await exportCostReport(dateRange.value[0], dateRange.value[1])
    if (res.code === 200 && res.data) {
      const { data, summary } = res.data
      let csv = '日期,模型,请求数,Token,成本\n'
      data.forEach(row => { csv += `${row.date},${row.model},${row.requests},${row.tokens},${row.cost}\n` })
      csv += `\n汇总,总请求: ${summary.totalRequests},总Token: ${summary.totalTokens},总成本: ${summary.totalCost}\n`

      const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `AIGC成本报表_${dateRange.value[0]}_${dateRange.value[1]}.csv`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const handleResize = () => { trendChart?.resize(); pieChart?.resize() }

onMounted(() => {
  const end = new Date(), start = new Date()
  start.setDate(start.getDate() - 29)
  dateRange.value = [start.toISOString().substring(0, 10), end.toISOString().substring(0, 10)]
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
.ai-cost-page {
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

/* 成本卡片 */
.cost-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.cost-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.cost-card.forecast {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.cost-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.cost-icon.blue { background: #e6f4ff; color: #1890ff; }
.cost-icon.green { background: #f0fdf4; color: #22c55e; }
.cost-icon.orange { background: #fef3c7; color: #f59e0b; }
.cost-card.forecast .cost-icon { background: rgba(255,255,255,0.2); color: #fff; }

.cost-content {
  display: flex;
  flex-direction: column;
}

.cost-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.cost-card.forecast .cost-value { color: #fff; }

.cost-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.cost-card.forecast .cost-label { color: rgba(255,255,255,0.8); }

.cost-tip {
  font-size: 11px;
  color: rgba(255,255,255,0.7);
  margin-top: 2px;
}

/* 图表区 */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
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

/* 表格区 */
.table-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 1200px) {
  .cost-cards { grid-template-columns: repeat(2, 1fr); }
  .charts-section { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .cost-cards { grid-template-columns: 1fr; }
}
</style>
