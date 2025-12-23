<template>
  <div class="performance-visual">
    <div class="chart-header">
      <span class="chart-label">吞吐量</span>
      <span class="chart-sublabel">延迟</span>
    </div>
    <div class="chart-area">
      <svg viewBox="0 0 200 80" class="line-chart">
        <defs>
          <linearGradient id="lineGradient" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color: #3b82f6; stop-opacity: 0.3" />
            <stop offset="100%" style="stop-color: #3b82f6; stop-opacity: 0" />
          </linearGradient>
        </defs>
        <path
          :d="areaPath"
          fill="url(#lineGradient)"
        />
        <path
          :d="linePath"
          fill="none"
          stroke="#3b82f6"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const dataPoints = [30, 45, 35, 55, 40, 60, 50, 65, 55, 70]

const linePath = computed(() => {
  const width = 200
  const height = 80
  const padding = 10
  const stepX = (width - padding * 2) / (dataPoints.length - 1)
  
  return dataPoints
    .map((point, index) => {
      const x = padding + index * stepX
      const y = height - padding - (point / 100) * (height - padding * 2)
      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`
    })
    .join(' ')
})

const areaPath = computed(() => {
  const width = 200
  const height = 80
  const padding = 10
  const stepX = (width - padding * 2) / (dataPoints.length - 1)
  
  const points = dataPoints
    .map((point, index) => {
      const x = padding + index * stepX
      const y = height - padding - (point / 100) * (height - padding * 2)
      return `${x} ${y}`
    })
    .join(' L ')
  
  const startX = padding
  const endX = padding + (dataPoints.length - 1) * stepX
  
  return `M ${startX} ${height - padding} L ${points} L ${endX} ${height - padding} Z`
})
</script>

<style scoped>
.performance-visual {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
  box-sizing: border-box;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.chart-label {
  font-size: 12px;
  font-weight: 500;
  color: #333;
}

.chart-sublabel {
  font-size: 12px;
  color: #999;
}

.chart-area {
  flex: 1;
  display: flex;
  align-items: center;
}

.line-chart {
  width: 100%;
  height: 100%;
}
</style>
