<template>
  <div class="alert-records-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>告警历史</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总告警数</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card firing">
          <div class="stat-value">{{ stats.firing }}</div>
          <div class="stat-label">触发中</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card resolved">
          <div class="stat-value">{{ stats.resolved }}</div>
          <div class="stat-label">已恢复</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card critical">
          <div class="stat-value">{{ stats.critical }}</div>
          <div class="stat-label">严重</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ stats.warning }}</div>
          <div class="stat-label">警告</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card info">
          <div class="stat-value">{{ stats.info }}</div>
          <div class="stat-label">信息</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="告警级别">
          <el-select v-model="filterForm.alertLevel" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="level in ALERT_LEVELS" :key="level.value" :label="level.label" :value="level.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="触发中" value="firing" />
            <el-option label="已恢复" value="resolved" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRecords">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 告警记录列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="alertLevel" label="级别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :color="getLevelColor(row.alertLevel)" effect="dark" size="small">
              {{ getLevelLabel(row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleName" label="规则名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="alertMessage" label="告警内容" min-width="250" show-overflow-tooltip />
        <el-table-column label="当前值/阈值" width="140" align="center">
          <template #default="{ row }">
            <span class="current-value">{{ row.currentValue }}</span>
            <span class="threshold-divider">/</span>
            <span class="threshold-value">{{ row.thresholdValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'firing' ? 'danger' : 'success'" size="small">
              {{ row.status === 'firing' ? '触发中' : '已恢复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notified" label="已通知" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.notified" color="#67c23a"><CircleCheck /></el-icon>
            <el-icon v-else color="#c0c4cc"><CircleClose /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="firedAt" label="触发时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.firedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="resolvedAt" label="恢复时间" width="160">
          <template #default="{ row }">
            {{ row.resolvedAt ? formatDateTime(row.resolvedAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetails(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'firing'" 
              type="warning" 
              link 
              size="small" 
              @click="handleResolve(row)"
            >
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </el-card>

    <!-- 告警详情弹窗 -->
    <el-dialog v-model="detailsDialogVisible" title="告警详情" width="900px" destroy-on-close>
      <div v-loading="detailsLoading">
        <!-- 告警基本信息 -->
        <el-descriptions :column="2" border class="alert-info">
          <el-descriptions-item label="规则名称">{{ alertDetails?.record?.ruleName }}</el-descriptions-item>
          <el-descriptions-item label="告警级别">
            <el-tag :color="getLevelColor(alertDetails?.record?.alertLevel || '')" effect="dark" size="small">
              {{ getLevelLabel(alertDetails?.record?.alertLevel || '') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前值">{{ alertDetails?.record?.currentValue }}</el-descriptions-item>
          <el-descriptions-item label="阈值">{{ alertDetails?.record?.thresholdValue }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="alertDetails?.record?.status === 'firing' ? 'danger' : 'success'" size="small">
              {{ alertDetails?.record?.status === 'firing' ? '触发中' : '已恢复' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="触发时间">{{ formatDateTime(alertDetails?.record?.firedAt) }}</el-descriptions-item>
          <el-descriptions-item label="告警内容" :span="2">{{ alertDetails?.record?.alertMessage }}</el-descriptions-item>
        </el-descriptions>

        <!-- 触发请求列表 -->
        <div class="request-list-header">
          <h4>触发告警的请求 ({{ alertDetails?.requestCount || 0 }} 条)</h4>
        </div>
        <el-table :data="alertDetails?.requestDetails || []" max-height="400" stripe size="small">
          <el-table-column prop="apiPath" label="API路径" min-width="200" show-overflow-tooltip />
          <el-table-column prop="statusCode" label="状态码" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.statusCode >= 400 ? 'danger' : 'success'" size="small">
                {{ row.statusCode || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="latency" label="延迟" width="80" align="center">
            <template #default="{ row }">
              <span :class="{ 'high-latency': row.latency > 500 }">{{ row.latency }}ms</span>
            </template>
          </el-table-column>
          <el-table-column prop="success" label="结果" width="70" align="center">
            <template #default="{ row }">
              <el-icon v-if="row.success" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="errorMessage" label="错误信息" min-width="150" show-overflow-tooltip />
          <el-table-column prop="requestTime" label="请求时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.requestTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAlertRecords,
  getAlertStats,
  getAlertDetails,
  resolveAlert,
  ALERT_LEVELS,
  type AlertRecord,
  type AlertStats,
  type AlertRecordDetail
} from '@/api/alert'

// 数据
const loading = ref(false)
const records = ref<AlertRecord[]>([])
const stats = ref<AlertStats>({
  total: 0,
  firing: 0,
  resolved: 0,
  critical: 0,
  warning: 0,
  info: 0
})

// 告警详情弹窗
const detailsDialogVisible = ref(false)
const detailsLoading = ref(false)
const alertDetails = ref<AlertRecordDetail | null>(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  alertLevel: '',
  status: '',
  timeRange: [] as string[]
})

// 定时刷新
let refreshTimer: number | null = null

// 加载告警统计
const loadStats = async () => {
  try {
    const params: any = {}
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startTime = filterForm.timeRange[0]
      params.endTime = filterForm.timeRange[1]
    }
    const res = await getAlertStats(params)
    if (res.code === 200 && res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载告警统计失败', error)
  }
}

// 加载告警记录
const loadRecords = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (filterForm.alertLevel) {
      params.alertLevel = filterForm.alertLevel
    }
    if (filterForm.status) {
      params.status = filterForm.status
    }
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startTime = filterForm.timeRange[0]
      params.endTime = filterForm.timeRange[1]
    }
    
    const res = await getAlertRecords(params)
    if (res.code === 200 && res.data) {
      records.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载告警记录失败', error)
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.alertLevel = ''
  filterForm.status = ''
  filterForm.timeRange = []
  pagination.pageNum = 1
  loadRecords()
  loadStats()
}

// 手动恢复告警
const handleResolve = async (row: AlertRecord) => {
  await ElMessageBox.confirm('确定要手动恢复此告警吗？', '提示', {
    type: 'warning'
  })
  
  try {
    await resolveAlert(row.id)
    ElMessage.success('已恢复')
    loadRecords()
    loadStats()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 查看告警详情
const handleViewDetails = async (row: AlertRecord) => {
  detailsDialogVisible.value = true
  detailsLoading.value = true
  alertDetails.value = null
  
  try {
    const res = await getAlertDetails(row.id) as any
    console.log('告警详情API响应:', res)
    
    // 响应拦截器已经处理过，res 就是 { code, data, message }
    if (res && res.data) {
      alertDetails.value = res.data
      console.log('设置告警详情:', alertDetails.value)
    } else if (res && !res.code) {
      // 如果响应直接是数据（没有 code 包装）
      alertDetails.value = res
      console.log('设置告警详情(直接数据):', alertDetails.value)
    } else {
      console.warn('告警详情响应格式异常:', res)
      ElMessage.warning('告警详情数据为空')
    }
  } catch (error) {
    console.error('获取告警详情失败', error)
    ElMessage.error('获取告警详情失败')
  } finally {
    detailsLoading.value = false
  }
}

// 工具函数
const getLevelLabel = (level: string) => {
  return ALERT_LEVELS.find(l => l.value === level)?.label || level
}

const getLevelColor = (level: string) => {
  return ALERT_LEVELS.find(l => l.value === level)?.color || '#909399'
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 刷新数据
const refreshData = () => {
  loadStats()
  loadRecords()
}

onMounted(() => {
  refreshData()
  // 每30秒刷新一次
  refreshTimer = window.setInterval(refreshData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped lang="scss">
.alert-records-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 16px;
  
  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }
}

.stats-cards {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  
  :deep(.el-card__body) {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
  }
  
  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-top: 4px;
  }
  
  &.firing .stat-value {
    color: #f56c6c;
  }
  
  &.resolved .stat-value {
    color: #67c23a;
  }
  
  &.critical .stat-value {
    color: #f56c6c;
  }
  
  &.warning .stat-value {
    color: #e6a23c;
  }
  
  &.info .stat-value {
    color: #409eff;
  }
}

.filter-card {
  margin-bottom: 16px;
  
  :deep(.el-card__body) {
    padding: 16px 20px 0;
  }
}

.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.pagination-wrapper {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
}

.current-value {
  color: #f56c6c;
  font-weight: 500;
}

.threshold-divider {
  color: #c0c4cc;
  margin: 0 4px;
}

.threshold-value {
  color: #909399;
}

.resolved-text {
  color: #c0c4cc;
}

.alert-info {
  margin-bottom: 20px;
}

.request-list-header {
  margin: 16px 0 12px;
  
  h4 {
    margin: 0;
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }
}

.high-latency {
  color: #f56c6c;
  font-weight: 500;
}
</style>
