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
        <div class="action-card" @click="$router.push('/console/tokens')">
          <el-icon class="action-icon"><Key /></el-icon>
          <span>管理令牌</span>
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
import { ref, markRaw } from 'vue'
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

// 统计数据
const stats = ref([
  { label: 'API总数', value: '28', icon: markRaw(Connection), bgColor: '#e6f4ff', color: '#1890ff' },
  { label: '应用数量', value: '12', icon: markRaw(Grid), bgColor: '#f0fdf4', color: '#22c55e' },
  { label: '今日调用', value: '15,680', icon: markRaw(DataLine), bgColor: '#fef3c7', color: '#f59e0b' },
  { label: 'API令牌', value: '5', icon: markRaw(Ticket), bgColor: '#fce7f3', color: '#ec4899' },
])

// 最近调用
const recentCalls = ref([
  { apiName: '用户认证接口', method: 'POST', status: 200, latency: 45, time: '2分钟前' },
  { apiName: '订单查询接口', method: 'GET', status: 200, latency: 128, time: '5分钟前' },
  { apiName: '支付回调接口', method: 'POST', status: 500, latency: 2300, time: '8分钟前' },
  { apiName: '商品信息接口', method: 'GET', status: 200, latency: 67, time: '12分钟前' },
  { apiName: '消息推送接口', method: 'POST', status: 200, latency: 89, time: '15分钟前' },
])

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
