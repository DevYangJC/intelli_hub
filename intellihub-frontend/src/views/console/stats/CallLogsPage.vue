<template>
  <div class="call-logs-page">
    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="API路径">
          <el-input v-model="filterForm.apiPath" placeholder="请输入API路径" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="应用ID">
          <el-input v-model="filterForm.appId" placeholder="请输入应用ID" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.success" placeholder="全部" clearable style="width: 100px">
            <el-option label="成功" :value="true" />
            <el-option label="失败" :value="false" />
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
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="logList" v-loading="loading" stripe border>
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="requestTime" label="请求时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.requestTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="apiPath" label="API路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="apiMethod" label="方法" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.apiMethod)" size="small">{{ row.apiMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态码" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">
              {{ row.statusCode }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="latency" label="延迟" width="100" align="right">
          <template #default="{ row }">
            <span :class="getLatencyClass(row.latency)">{{ row.latency }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="clientIp" label="客户端IP" width="140" />
        <el-table-column prop="appKey" label="AppKey" width="120" show-overflow-tooltip />
        <el-table-column prop="errorMessage" label="错误信息" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="error-msg" v-if="row.errorMessage">{{ row.errorMessage }}</span>
            <span class="no-error" v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="调用详情" width="600px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="请求时间" :span="2">{{ formatDateTime(currentLog.requestTime) }}</el-descriptions-item>
        <el-descriptions-item label="API路径" :span="2">{{ currentLog.apiPath }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.apiMethod }}</el-descriptions-item>
        <el-descriptions-item label="状态码">
          <el-tag :type="currentLog.success ? 'success' : 'danger'" size="small">{{ currentLog.statusCode }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="响应时间">{{ currentLog.latency }}ms</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="currentLog.success ? 'success' : 'danger'" size="small">{{ currentLog.success ? '成功' : '失败' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户端IP">{{ currentLog.clientIp }}</el-descriptions-item>
        <el-descriptions-item label="应用ID">{{ currentLog.appId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="AppKey" :span="2">{{ currentLog.appKey || '-' }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent" :span="2">
          <span class="user-agent">{{ currentLog.userAgent || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <span class="error-msg">{{ currentLog.errorMessage }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="请求体" :span="2" v-if="currentLog.requestBody">
          <el-input type="textarea" :rows="4" :value="currentLog.requestBody" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="响应体" :span="2" v-if="currentLog.responseBody">
          <el-input type="textarea" :rows="4" :value="currentLog.responseBody" readonly />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getCallLogs, type CallLog } from '@/api/stats'

// 筛选表单
const filterForm = reactive({
  apiPath: '',
  appId: '',
  success: undefined as boolean | undefined,
  timeRange: [] as string[]
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 数据
const loading = ref(false)
const logList = ref<CallLog[]>([])
const detailVisible = ref(false)
const currentLog = ref<CallLog | null>(null)

// 格式化时间
const formatDateTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 获取方法标签类型
const getMethodTagType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return types[method] || 'info'
}

// 获取延迟样式类
const getLatencyClass = (latency: number) => {
  if (latency < 100) return 'latency-fast'
  if (latency < 500) return 'latency-normal'
  return 'latency-slow'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (filterForm.apiPath) {
      params.apiPath = filterForm.apiPath
    }
    if (filterForm.appId) {
      params.appId = filterForm.appId
    }
    if (filterForm.success !== undefined) {
      params.success = filterForm.success
    }
    if (filterForm.timeRange?.length === 2) {
      params.startTime = filterForm.timeRange[0]
      params.endTime = filterForm.timeRange[1]
    }
    
    const res = await getCallLogs(params)
    if (res.code === 200 && res.data) {
      logList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载日志失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  filterForm.apiPath = ''
  filterForm.appId = ''
  filterForm.success = undefined
  filterForm.timeRange = []
  pagination.page = 1
  loadData()
}

// 分页
const handleSizeChange = () => {
  pagination.page = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

// 显示详情
const showDetail = (row: CallLog) => {
  currentLog.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.call-logs-page {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
  
  :deep(.el-card__body) {
    padding: 16px 20px 0;
  }
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.table-card {
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.latency-fast {
  color: #67c23a;
}

.latency-normal {
  color: #e6a23c;
}

.latency-slow {
  color: #f56c6c;
  font-weight: 500;
}

.error-msg {
  color: #f56c6c;
}

.no-error {
  color: #c0c4cc;
}

.user-agent {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}
</style>
