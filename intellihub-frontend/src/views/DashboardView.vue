<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statsCards" :key="card.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: card.color }">
              <el-icon :size="24">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- API调用趋势图 -->
      <el-col :xs="24" :lg="16">
        <el-card title="API调用趋势" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>API调用趋势</span>
              <el-radio-group v-model="trendPeriod" size="small">
                <el-radio-button label="7d">最近7天</el-radio-button>
                <el-radio-button label="30d">最近30天</el-radio-button>
                <el-radio-button label="90d">最近90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <APITrendChart :period="trendPeriod" />
          </div>
        </el-card>
      </el-col>

      <!-- 热门API -->
      <el-col :xs="24" :lg="8">
        <el-card title="热门API" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门API</span>
              <el-button type="text" size="small">查看更多</el-button>
            </div>
          </template>
          <div class="hot-api-list">
            <div v-for="(api, index) in hotAPIs" :key="api.id" class="hot-api-item">
              <div class="api-rank">{{ index + 1 }}</div>
              <div class="api-info">
                <div class="api-name">{{ api.name }}</div>
                <div class="api-calls">{{ api.calls }} 次调用</div>
              </div>
              <div class="api-trend" :class="api.trend > 0 ? 'up' : 'down'">
                <el-icon>
                  <CaretTop v-if="api.trend > 0" />
                  <CaretBottom v-else />
                </el-icon>
                {{ Math.abs(api.trend) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 最新活动 -->
      <el-col :xs="24" :lg="12">
        <el-card title="最新活动" class="activity-card">
          <el-timeline>
            <el-timeline-item
              v-for="activity in recentActivities"
              :key="activity.id"
              :timestamp="activity.timestamp"
              :type="activity.type"
            >
              {{ activity.description }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 系统通知 -->
      <el-col :xs="24" :lg="12">
        <el-card title="系统通知" class="notice-card">
          <div v-for="notice in systemNotices" :key="notice.id" class="notice-item">
            <div class="notice-title" :class="notice.type">
              <el-icon>
                <Warning v-if="notice.type === 'warning'" />
                <InfoFilled v-else-if="notice.type === 'info'" />
                <SuccessFilled v-else-if="notice.type === 'success'" />
              </el-icon>
              {{ notice.title }}
            </div>
            <div class="notice-time">{{ notice.time }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  CaretTop,
  CaretBottom,
  Warning,
  InfoFilled,
  SuccessFilled
} from '@element-plus/icons-vue'
import APITrendChart from '@/components/charts/API TrendChart.vue'

// 状态
const trendPeriod = ref<'7d' | '30d' | '90d'>('7d')

// 统计卡片数据
const statsCards = [
  {
    title: 'API总数',
    value: '156',
    icon: 'Connection',
    color: '#409eff'
  },
  {
    title: '今日调用',
    value: '23.5K',
    icon: 'TrendCharts',
    color: '#67c23a'
  },
  {
    title: '活跃应用',
    value: '89',
    icon: 'Applications',
    color: '#e6a23c'
  },
  {
    title: '注册用户',
    value: '1,234',
    icon: 'User',
    color: '#f56c6c'
  }
]

// 热门API数据
const hotAPIs = ref([
  { id: 1, name: '用户认证接口', calls: 45321, trend: 12.5 },
  { id: 2, name: '订单查询接口', calls: 38912, trend: -3.2 },
  { id: 3, name: '商品信息接口', calls: 28743, trend: 8.7 },
  { id: 4, name: '支付回调接口', calls: 19876, trend: 15.3 },
  { id: 5, name: '消息推送接口', calls: 15432, trend: -5.6 }
])

// 最新活动
const recentActivities = [
  {
    id: 1,
    description: '创建了新API "物流查询接口"',
    timestamp: '2024-01-15 10:30',
    type: 'success'
  },
  {
    id: 2,
    description: '更新了API "用户认证接口" 的配置',
    timestamp: '2024-01-15 09:15',
    type: 'primary'
  },
  {
    id: 3,
    description: '应用 "测试APP" 的API密钥已过期',
    timestamp: '2024-01-15 08:45',
    type: 'warning'
  },
  {
    id: 4,
    description: '删除了API "旧版支付接口"',
    timestamp: '2024-01-14 16:20',
    type: 'info'
  }
]

// 系统通知
const systemNotices = [
  {
    id: 1,
    title: '系统将于今晚22:00进行维护升级',
    type: 'warning',
    time: '2小时前'
  },
  {
    id: 2,
    title: '新的API版本管理功能已上线',
    type: 'info',
    time: '1天前'
  },
  {
    id: 3,
    title: '本月API调用配额已更新',
    type: 'success',
    time: '3天前'
  }
]
</script>

<style scoped>
.dashboard {
  padding: 0;
}

/* 统计卡片样式 */
.stat-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1890ff;
  line-height: 1.2;
}

.stat-title {
  font-size: 14px;
  color: #666666;
  margin-top: 4px;
  font-weight: 500;
}

/* 图表卡片样式 */
.chart-card {
  height: 420px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #262626;
}

.chart-container {
  height: 340px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  border-radius: 4px;
}

.hot-api-list {
  padding: 0;
}

.hot-api-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 8px;
  background-color: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
}

.hot-api-item:hover {
  background-color: #f0f0f0;
}

.hot-api-item:last-child {
  margin-bottom: 0;
}

.api-rank {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  margin-right: 16px;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
}

.api-info {
  flex: 1;
}

.api-name {
  font-size: 15px;
  color: #262626;
  margin-bottom: 4px;
  font-weight: 500;
}

.api-calls {
  font-size: 13px;
  color: #8c8c8c;
}

.api-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
}

.api-trend.up {
  color: #52c41a;
  background-color: #f6ffed;
}

.api-trend.down {
  color: #ff4d4f;
  background-color: #fff1f0;
}

/* 活动和通知卡片 */
.activity-card,
.notice-card {
  height: 420px;
  overflow-y: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 8px;
  background-color: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
}

.notice-item:hover {
  background-color: #f0f0f0;
}

.notice-item:last-child {
  margin-bottom: 0;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.notice-title.warning {
  color: #fa8c16;
}

.notice-title.info {
  color: #1890ff;
}

.notice-title.success {
  color: #52c41a;
}

.notice-time {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
}

/* Element Plus 样式覆盖 */
:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #8c8c8c;
}

:deep(.el-empty__description) {
  color: #8c8c8c;
}

/* 响应式布局优化 */
@media (max-width: 768px) {
  .stat-card {
    margin-bottom: 16px;
  }

  .chart-card,
  .activity-card,
  .notice-card {
    height: auto;
    min-height: 300px;
  }

  .stat-content {
    gap: 16px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
  }

  .stat-value {
    font-size: 28px;
  }
}
</style>