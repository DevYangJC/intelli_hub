<template>
  <div class="console-overview">
    <h2 class="page-title">控制台概览</h2>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statsCards" :key="card.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: card.color }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="quick-actions-card">
      <template #header>
        <span class="card-title">快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/console/api/create')">
          <el-icon><Plus /></el-icon>
          创建API
        </el-button>
        <el-button @click="$router.push('/console/app/create')">
          <el-icon><Grid /></el-icon>
          创建应用
        </el-button>
        <el-button @click="$router.push('/console/gateway/routes')">
          <el-icon><SetUp /></el-icon>
          配置路由
        </el-button>
        <el-button @click="$router.push('/monitor')">
          <el-icon><DataLine /></el-icon>
          查看监控
        </el-button>
      </div>
    </el-card>

    <!-- 最近活动 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card class="activity-card">
          <template #header>
            <span class="card-title">最近API调用</span>
          </template>
          <el-table :data="recentCalls" style="width: 100%">
            <el-table-column prop="api" label="API名称" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 200 ? 'success' : 'danger'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="响应时间" width="100" />
            <el-table-column prop="timestamp" label="时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="activity-card">
          <template #header>
            <span class="card-title">系统通知</span>
          </template>
          <div class="notice-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <el-icon :class="notice.type"><component :is="notice.icon" /></el-icon>
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-time">{{ notice.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import {
  Connection,
  Grid,
  User,
  TrendCharts,
  Plus,
  SetUp,
  DataLine,
  Warning,
  InfoFilled,
  SuccessFilled,
} from '@element-plus/icons-vue'

// 统计卡片数据
const statsCards = [
  { title: 'API总数', value: '156', icon: Connection, color: '#409eff' },
  { title: '应用数量', value: '89', icon: Grid, color: '#67c23a' },
  { title: '今日调用', value: '23.5K', icon: TrendCharts, color: '#e6a23c' },
  { title: '活跃用户', value: '1,234', icon: User, color: '#f56c6c' },
]

// 最近调用数据
const recentCalls = [
  { api: '用户认证接口', status: 200, time: '45ms', timestamp: '2024-01-15 10:30:25' },
  { api: '订单查询接口', status: 200, time: '128ms', timestamp: '2024-01-15 10:30:18' },
  { api: '支付回调接口', status: 500, time: '2.3s', timestamp: '2024-01-15 10:30:12' },
  { api: '商品信息接口', status: 200, time: '67ms', timestamp: '2024-01-15 10:30:05' },
  { api: '消息推送接口', status: 200, time: '89ms', timestamp: '2024-01-15 10:29:58' },
]

// 系统通知
const notices = [
  { id: 1, title: '系统将于今晚22:00进行维护升级', type: 'warning', icon: Warning, time: '2小时前' },
  { id: 2, title: '新的API版本管理功能已上线', type: 'info', icon: InfoFilled, time: '1天前' },
  { id: 3, title: '本月API调用配额已更新', type: 'success', icon: SuccessFilled, time: '3天前' },
]
</script>

<style scoped>
.console-overview {
  max-width: 1200px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 24px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
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
  font-weight: 700;
  color: #1a1a1a;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

/* 快捷操作 */
.quick-actions-card {
  margin-bottom: 24px;
}

.card-title {
  font-weight: 600;
  color: #1a1a1a;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 活动卡片 */
.activity-card {
  margin-bottom: 24px;
}

/* 通知列表 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.notice-item .el-icon {
  font-size: 20px;
  margin-top: 2px;
}

.notice-item .el-icon.warning {
  color: #e6a23c;
}

.notice-item .el-icon.info {
  color: #409eff;
}

.notice-item .el-icon.success {
  color: #67c23a;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 14px;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.notice-time {
  font-size: 12px;
  color: #999;
}
</style>
