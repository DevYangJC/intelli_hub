<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bgColor, color: stat.color }">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-grid">
        <div class="action-card" @click="$router.push('/console/api/create')">
          <el-icon class="action-icon"><Plus /></el-icon>
          <span>创建API</span>
        </div>
        <div class="action-card" @click="$router.push('/console/app/list')">
          <el-icon class="action-icon"><Grid /></el-icon>
          <span>应用管理</span>
        </div>
        <div class="action-card" @click="$router.push('/console/logs')">
          <el-icon class="action-icon"><Document /></el-icon>
          <span>查看日志</span>
        </div>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activity">
      <div class="section-header">
        <h3 class="section-title">最近API调用</h3>
        <el-button type="primary" link @click="$router.push('/console/logs')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="activity-table">
        <el-table :data="recentCalls" style="width: 100%">
          <el-table-column prop="apiName" label="API名称" min-width="180">
            <template #default="{ row }">
              <div class="api-cell">
                <el-tag :type="getMethodType(row.method)" size="small">{{ row.method }}</el-tag>
                <span>{{ row.apiName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status < 400 ? 'success' : 'danger'" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="latency" label="耗时" width="100" align="center">
            <template #default="{ row }">
              {{ row.latency }}ms
            </template>
          </el-table-column>
          <el-table-column prop="time" label="时间" width="160">
            <template #default="{ row }">
              <span class="time-text">{{ row.time }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw, onMounted } from 'vue'
import {
  Connection,
  Grid,
  Document,
  Key,
  Plus,
  ArrowRight,
  DataLine,
  Ticket,
} from '@element-plus/icons-vue'
import { getStatsOverview, getCallLogs, type CallLog } from '@/api/stats'

// 统计数据
const stats = ref([
  { label: 'API总数', value: '-', icon: markRaw(Connection), bgColor: '#e6f4ff', color: '#1890ff' },
  { label: '应用数量', value: '-', icon: markRaw(Grid), bgColor: '#f0fdf4', color: '#22c55e' },
  { label: '今日调用', value: '-', icon: markRaw(DataLine), bgColor: '#fef3c7', color: '#f59e0b' },
  { label: '成功率', value: '-', icon: markRaw(Ticket), bgColor: '#fce7f3', color: '#ec4899' },
])

// 最近调用
const recentCalls = ref<Array<{apiName: string; method: string; status: number; latency: number; time: string}>>([])

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const time = new Date(timeStr)
  const now = new Date()
  const diff = Math.floor((now.getTime() - time.getTime()) / 1000)
  if (diff < 60) return `${diff}秒前`
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return timeStr.substring(5, 16)
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getStatsOverview()
    if (res.code === 200 && res.data) {
      const data = res.data
      stats.value[0].value = formatNumber(data.apiCount || 0)
      stats.value[1].value = formatNumber(data.appCount || 0)
      stats.value[2].value = formatNumber(data.todayTotalCount || 0)
      stats.value[3].value = (data.todaySuccessRate || 0).toFixed(1) + '%'
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 加载最近调用
const loadRecentCalls = async () => {
  try {
    const res = await getCallLogs({ page: 1, size: 5 })
    if (res.code === 200 && res.data) {
      recentCalls.value = (res.data.records || []).map((log: CallLog) => ({
        apiName: log.apiPath || '未知API',
        method: log.apiMethod || 'GET',
        status: log.statusCode || 0,
        latency: log.latency || 0,
        time: formatTime(log.requestTime)
      }))
    }
  } catch (error) {
    console.error('加载调用日志失败', error)
  }
}

// 获取方法类型
const getMethodType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
  }
  return types[method] || 'info'
}

onMounted(() => {
  loadStats()
  loadRecentCalls()
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100%;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #ebeef5;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 快捷操作 */
.quick-actions {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.2s;
}

.action-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.action-icon {
  font-size: 28px;
  color: #409eff;
}

.action-card span {
  font-size: 14px;
  color: #1a1a1a;
}

/* 最近活动 */
.recent-activity {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.api-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-text {
  color: #909399;
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-grid,
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid,
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
