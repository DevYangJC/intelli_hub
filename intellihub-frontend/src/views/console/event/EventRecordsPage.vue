<template>
  <div class="event-records-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>事件记录</h2>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="发布记录" name="publish" />
      <el-tab-pane label="消费记录" name="consume" />
    </el-tabs>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="事件编码">
          <el-input v-model="filterForm.eventCode" placeholder="请输入事件编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <template v-if="activeTab === 'publish'">
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="失败" value="FAILED" />
            </template>
            <template v-else>
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAILED" />
              <el-option label="重试中" value="RETRYING" />
              <el-option label="待处理" value="PENDING" />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发布记录列表 -->
    <el-card v-if="activeTab === 'publish'" shadow="never" class="table-card">
      <el-table :data="publishRecords" v-loading="loading" stripe table-layout="fixed">
        <el-table-column prop="eventId" label="事件ID" width="160">
          <template #default="{ row }">
            <span class="event-id" :title="row.eventId">{{ row.eventId.substring(0, 16) }}...</span>
          </template>
        </el-table-column>
        <el-table-column prop="eventCode" label="事件编码" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="event-code">{{ row.eventCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'danger'" size="small">
              {{ row.status === 'PUBLISHED' ? '已发布' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewEventData(row)">查看数据</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 消费记录列表 -->
    <el-card v-if="activeTab === 'consume'" shadow="never" class="table-card">
      <el-table :data="consumeRecords" v-loading="loading" stripe table-layout="fixed">
        <el-table-column prop="eventId" label="事件ID" width="160">
          <template #default="{ row }">
            <span class="event-id" :title="row.eventId">{{ row.eventId.substring(0, 16) }}...</span>
          </template>
        </el-table-column>
        <el-table-column prop="eventCode" label="事件编码" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="event-code">{{ row.eventCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getConsumeStatusTag(row.status)" size="small">
              {{ getConsumeStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retryTimes" label="重试次数" width="90" align="center" />
        <el-table-column prop="costTime" label="耗时" width="90" align="center">
          <template #default="{ row }">
            {{ row.costTime ? row.costTime + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="responseCode" label="响应码" width="90" align="center">
          <template #default="{ row }">
            <el-tag 
              v-if="row.responseCode" 
              :type="row.responseCode >= 200 && row.responseCode < 300 ? 'success' : 'danger'" 
              size="small"
            >
              {{ row.responseCode }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="consumeTime" label="消费时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.consumeTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="nextRetryTime" label="下次重试" width="160">
          <template #default="{ row }">
            {{ row.nextRetryTime ? formatDateTime(row.nextRetryTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewConsumeDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 事件数据查看弹窗 -->
    <el-dialog v-model="dataDialogVisible" title="事件数据" width="700px">
      <pre class="data-content">{{ currentEventData }}</pre>
    </el-dialog>

    <!-- 消费详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="消费详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="事件ID">{{ currentRecord?.eventId }}</el-descriptions-item>
        <el-descriptions-item label="事件编码">{{ currentRecord?.eventCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getConsumeStatusTag(currentRecord?.status || '')" size="small">
            {{ getConsumeStatusLabel(currentRecord?.status || '') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentRecord?.retryTimes }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentRecord?.costTime ? currentRecord.costTime + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="响应码">{{ currentRecord?.responseCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="消费时间">{{ formatDateTime(currentRecord?.consumeTime) }}</el-descriptions-item>
        <el-descriptions-item label="下次重试">{{ currentRecord?.nextRetryTime ? formatDateTime(currentRecord.nextRetryTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2">{{ currentRecord?.errorMessage || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>事件数据</el-divider>
      <pre class="data-content">{{ formatEventData(currentRecord?.eventData) }}</pre>
      
      <template v-if="currentRecord?.responseBody">
        <el-divider>响应内容</el-divider>
        <pre class="data-content response-body">{{ currentRecord.responseBody }}</pre>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getPublishRecords,
  getConsumeRecords,
  CONSUME_STATUS,
  type EventPublishRecord,
  type EventConsumeRecord
} from '@/api/event'

// 数据
const loading = ref(false)
const activeTab = ref('publish')
const publishRecords = ref<EventPublishRecord[]>([])
const consumeRecords = ref<EventConsumeRecord[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  eventCode: '',
  status: '',
  dateRange: null as [Date, Date] | null
})

// 弹窗
const dataDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentEventData = ref('')
const currentRecord = ref<EventConsumeRecord | null>(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      eventCode: filterForm.eventCode || undefined,
      status: filterForm.status || undefined,
      startTime: filterForm.dateRange?.[0]?.toISOString(),
      endTime: filterForm.dateRange?.[1]?.toISOString(),
      page: pagination.page,
      size: pagination.size
    }

    if (activeTab.value === 'publish') {
      const res = await getPublishRecords(params)
      publishRecords.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    } else {
      const res = await getConsumeRecords(params)
      consumeRecords.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载记录失败:', error)
  } finally {
    loading.value = false
  }
}

// Tab 切换
const handleTabChange = () => {
  pagination.page = 1
  filterForm.status = ''
  loadData()
}

// 重置筛选
const resetFilter = () => {
  filterForm.eventCode = ''
  filterForm.status = ''
  filterForm.dateRange = null
  pagination.page = 1
  loadData()
}

// 查看事件数据
const viewEventData = (row: EventPublishRecord) => {
  currentEventData.value = formatEventData(row.eventData)
  dataDialogVisible.value = true
}

// 查看消费详情
const viewConsumeDetail = (row: EventConsumeRecord) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

// 格式化事件数据
const formatEventData = (data?: string) => {
  if (!data) return '无数据'
  try {
    return JSON.stringify(JSON.parse(data), null, 2)
  } catch {
    return data
  }
}

// 工具函数
const getConsumeStatusLabel = (status: string) => {
  return CONSUME_STATUS.find(s => s.value === status)?.label || status
}

const getConsumeStatusTag = (status: string) => {
  return CONSUME_STATUS.find(s => s.value === status)?.type || 'info'
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.event-records-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
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

.table-card :deep(.el-card__body) {
  padding: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
}

.table-card :deep(.el-table th),
.table-card :deep(.el-table td) {
  padding: 12px 8px;
}

.event-id {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #606266;
}

.event-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #409eff;
}

.data-content {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  overflow: auto;
  max-height: 400px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.response-body {
  background: #fef0f0;
}
</style>
