<template>
  <div class="event-statistics-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>事件统计</h2>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="事件编码">
          <el-input v-model="filterForm.eventCode" placeholder="请输入事件编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览卡片 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff)">
            <el-icon :size="28"><Upload /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalStats.publishCount.toLocaleString() }}</div>
            <div class="stat-label">发布总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61)">
            <el-icon :size="28"><Download /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalStats.consumeCount.toLocaleString() }}</div>
            <div class="stat-label">消费总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563)">
            <el-icon :size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ successRate }}%</div>
            <div class="stat-label">成功率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #909399, #a6a9ad)">
            <el-icon :size="28"><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalStats.avgCostTime }}ms</div>
            <div class="stat-label">平均耗时</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>事件趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>消费状态分布</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <span>统计明细</span>
      </template>
      <el-table :data="statistics" v-loading="loading" stripe table-layout="fixed">
        <el-table-column prop="statDate" label="日期" width="120" />
        <el-table-column prop="eventCode" label="事件编码" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="event-code">{{ row.eventCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishCount" label="发布数" width="100" align="right">
          <template #default="{ row }">
            {{ row.publishCount.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="consumeCount" label="消费数" width="100" align="right">
          <template #default="{ row }">
            {{ row.consumeCount.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="successCount" label="成功数" width="100" align="right">
          <template #default="{ row }">
            <span class="text-success">{{ row.successCount.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failedCount" label="失败数" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.failedCount > 0 ? 'text-danger' : ''">{{ row.failedCount.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成功率" width="120" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="calcSuccessRate(row)"
              :color="getProgressColor(calcSuccessRate(row))"
              :stroke-width="6"
              :show-text="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="avgCostTime" label="平均耗时" width="100" align="right">
          <template #default="{ row }">
            {{ row.avgCostTime }}ms
          </template>
        </el-table-column>
        <el-table-column prop="maxCostTime" label="最大耗时" width="100" align="right">
          <template #default="{ row }">
            {{ row.maxCostTime }}ms
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { Upload, Download, CircleCheck, Timer } from '@element-plus/icons-vue'
import { getEventStatistics, type EventStatistics } from '@/api/event'
import * as echarts from 'echarts'

// 数据
const loading = ref(false)
const statistics = ref<EventStatistics[]>([])
const trendChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)

let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 筛选表单
const filterForm = reactive({
  eventCode: '',
  dateRange: null as [string, string] | null
})

// 计算统计汇总
const totalStats = computed(() => {
  const total = {
    publishCount: 0,
    consumeCount: 0,
    successCount: 0,
    failedCount: 0,
    avgCostTime: 0
  }
  
  if (statistics.value.length === 0) return total
  
  let totalCostTime = 0
  let costTimeCount = 0
  
  statistics.value.forEach(stat => {
    total.publishCount += stat.publishCount || 0
    total.consumeCount += stat.consumeCount || 0
    total.successCount += stat.successCount || 0
    total.failedCount += stat.failedCount || 0
    if (stat.avgCostTime > 0) {
      totalCostTime += stat.avgCostTime * (stat.consumeCount || 1)
      costTimeCount += stat.consumeCount || 1
    }
  })
  
  total.avgCostTime = costTimeCount > 0 ? Math.round(totalCostTime / costTimeCount) : 0
  
  return total
})

// 计算成功率
const successRate = computed(() => {
  const { consumeCount, successCount } = totalStats.value
  if (consumeCount === 0) return '0.00'
  return ((successCount / consumeCount) * 100).toFixed(2)
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      eventCode: filterForm.eventCode || undefined,
      startDate: filterForm.dateRange?.[0],
      endDate: filterForm.dateRange?.[1]
    }
    const res = await getEventStatistics(params)
    statistics.value = res.data || []
    updateCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.eventCode = ''
  filterForm.dateRange = null
  loadData()
}

// 计算单行成功率
const calcSuccessRate = (row: EventStatistics) => {
  if (!row.consumeCount) return 0
  return Math.round((row.successCount / row.consumeCount) * 100)
}

// 进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage >= 90) return '#67c23a'
  if (percentage >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 初始化图表
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
}

// 更新图表
const updateCharts = () => {
  updateTrendChart()
  updatePieChart()
}

// 更新趋势图
const updateTrendChart = () => {
  if (!trendChart) return
  
  // 按日期聚合数据
  const dateMap = new Map<string, { publish: number; success: number; failed: number }>()
  statistics.value.forEach(stat => {
    const date = stat.statDate
    if (!dateMap.has(date)) {
      dateMap.set(date, { publish: 0, success: 0, failed: 0 })
    }
    const d = dateMap.get(date)!
    d.publish += stat.publishCount || 0
    d.success += stat.successCount || 0
    d.failed += stat.failedCount || 0
  })
  
  const dates = Array.from(dateMap.keys()).sort()
  const publishData = dates.map(d => dateMap.get(d)!.publish)
  const successData = dates.map(d => dateMap.get(d)!.success)
  const failedData = dates.map(d => dateMap.get(d)!.failed)
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['发布数', '成功消费', '失败消费'],
      top: 0
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
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '发布数',
        type: 'line',
        smooth: true,
        data: publishData,
        itemStyle: { color: '#409eff' },
        areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
      },
      {
        name: '成功消费',
        type: 'line',
        smooth: true,
        data: successData,
        itemStyle: { color: '#67c23a' },
        areaStyle: { color: 'rgba(103, 194, 58, 0.1)' }
      },
      {
        name: '失败消费',
        type: 'line',
        smooth: true,
        data: failedData,
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 更新饼图
const updatePieChart = () => {
  if (!pieChart) return
  
  const { successCount, failedCount } = totalStats.value
  
  const option: echarts.EChartsOption = {
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
          { value: successCount, name: '成功', itemStyle: { color: '#67c23a' } },
          { value: failedCount, name: '失败', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  // 设置默认日期范围（最近7天）
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 7)
  filterForm.dateRange = [
    start.toISOString().split('T')[0],
    end.toISOString().split('T')[0]
  ]
  
  initCharts()
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.event-statistics-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}

/* 统计概览卡片 */
.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-content {
  margin-left: 16px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 图表区域 */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-card :deep(.el-card__header) {
  padding: 12px 20px;
  font-weight: 600;
}

.chart-card :deep(.el-card__body) {
  padding: 12px;
  height: calc(100% - 50px);
}

.chart-container {
  width: 100%;
  height: 100%;
}

/* 表格 */
.table-card :deep(.el-card__header) {
  padding: 12px 20px;
  font-weight: 600;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-card :deep(.el-table th),
.table-card :deep(.el-table td) {
  padding: 12px 8px;
}

.event-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #409eff;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}
</style>
