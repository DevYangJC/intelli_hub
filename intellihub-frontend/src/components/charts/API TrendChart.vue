<template>
  <div ref="chartRef" class="trend-chart"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

interface ChartData {
  dates: string[]
  values: number[]
}

interface TooltipParams {
  axisValue: string
  value: number
}

const props = defineProps<{
  period: '7d' | '30d' | '90d'
}>()

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 生成模拟数据
const generateData = (period: string): ChartData => {
  const now = new Date()
  const dates: string[] = []
  const values: number[] = []

  let days = 7
  if (period === '30d') days = 30
  if (period === '90d') days = 90

  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)

    // 格式化日期
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    dates.push(`${month}-${day}`)

    // 生成模拟值（基础值 + 随机波动）
    const baseValue = 20000 + Math.random() * 10000
    const trend = i * 100 // 增长趋势
    const random = Math.random() * 5000 - 2500 // 随机波动
    values.push(Math.round(baseValue + trend + random))
  }

  return { dates, values }
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  updateChart()

  // 响应式调整
  window.addEventListener('resize', handleResize)
}

// 更新图表数据
const updateChart = () => {
  if (!chartInstance) return

  const data = generateData(props.period)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      },
      formatter: (params: TooltipParams[]) => {
        if (!params || params.length === 0) return ''
        const date = params[0]!.axisValue
        const value = params[0]!.value
        return `
          <div style="padding: 8px">
            <div style="margin-bottom: 4px; font-weight: 600">${date}</div>
            <div>API调用次数: <span style="color: #1890ff; font-weight: 600">${value.toLocaleString()}</span></div>
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '8%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates,
      axisLine: {
        lineStyle: {
          color: '#e8e8e8'
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        rotate: props.period === '7d' ? 0 : 45
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        formatter: (value: number) => {
          if (value >= 1000) {
            return (value / 1000).toFixed(1) + 'k'
          }
          return value.toString()
        }
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: 'API调用次数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        sampling: 'average',
        itemStyle: {
          color: '#1890ff',
          borderColor: '#fff',
          borderWidth: 2
        },
        lineStyle: {
          color: '#1890ff',
          width: 3,
          shadowColor: 'rgba(24, 144, 255, 0.3)',
          shadowBlur: 10,
          shadowOffsetY: 5
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(24, 144, 255, 0.3)'
              },
              {
                offset: 1,
                color: 'rgba(24, 144, 255, 0.05)'
              }
            ]
          }
        },
        data: data.values
      }
    ]
  }

  chartInstance.setOption(option)
}

// 处理窗口大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 监听period变化
watch(() => props.period, () => {
  updateChart()
})

onMounted(() => {
  // 延迟初始化，确保DOM已渲染
  setTimeout(() => {
    initChart()
  }, 100)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.trend-chart {
  width: 100%;
  height: 100%;
}
</style>