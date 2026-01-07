<template>
  <div class="event-subscriptions-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>事件订阅</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建订阅
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="事件编码">
          <el-input v-model="filterForm.eventCode" placeholder="请输入事件编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="暂停" value="PAUSED" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe table-layout="fixed">
        <el-table-column prop="subscriberName" label="订阅名称" width="140" show-overflow-tooltip />
        <el-table-column prop="eventCode" label="事件编码" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="event-code">{{ row.eventCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subscriberType" label="订阅类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getSubscriberTypeTag(row.subscriberType)" size="small">
              {{ getSubscriberTypeLabel(row.subscriberType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="回调配置" show-overflow-tooltip>
          <template #default="{ row }">
            <template v-if="row.subscriberType === 'WEBHOOK'">
              <div class="callback-info">
                <el-tag size="small" effect="plain">{{ row.callbackMethod || 'POST' }}</el-tag>
                <span class="callback-url" :title="row.callbackUrl">{{ row.callbackUrl }}</span>
              </div>
            </template>
            <template v-else-if="row.subscriberType === 'MQ'">
              <span>Topic: {{ row.mqTopic }}</span>
              <span v-if="row.mqTag"> / Tag: {{ row.mqTag }}</span>
            </template>
            <template v-else>
              <span class="text-muted">内部服务</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="retryStrategy" label="重试策略" width="100" align="center">
          <template #default="{ row }">
            {{ getRetryStrategyLabel(row.retryStrategy) }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button 
              v-if="row.status === 'ACTIVE'" 
              type="warning" link size="small" 
              @click="handlePause(row)"
            >暂停</el-button>
            <el-button 
              v-if="row.status === 'PAUSED'" 
              type="success" link size="small" 
              @click="handleResume(row)"
            >恢复</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 创建/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑订阅' : '新建订阅'" 
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="订阅名称" prop="subscriberName">
          <el-input v-model="form.subscriberName" placeholder="请输入订阅名称" />
        </el-form-item>
        <el-form-item label="事件编码" prop="eventCode">
          <el-input v-model="form.eventCode" placeholder="要订阅的事件编码，如 user.created" />
        </el-form-item>
        <el-form-item label="订阅类型" prop="subscriberType">
          <el-radio-group v-model="form.subscriberType">
            <el-radio-button v-for="type in SUBSCRIBER_TYPES" :key="type.value" :label="type.value">
              {{ type.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- Webhook 配置 -->
        <template v-if="form.subscriberType === 'WEBHOOK'">
          <el-form-item label="回调地址" prop="callbackUrl">
            <el-input v-model="form.callbackUrl" placeholder="https://your-server.com/webhook">
              <template #prepend>
                <el-select v-model="form.callbackMethod" style="width: 100px">
                  <el-option v-for="m in HTTP_METHODS" :key="m.value" :label="m.label" :value="m.value" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="请求头">
            <el-input 
              v-model="form.callbackHeaders" 
              type="textarea" 
              :rows="2" 
              placeholder='JSON 格式，如: {"Authorization": "Bearer xxx"}'
            />
          </el-form-item>
        </template>

        <!-- MQ 配置 -->
        <template v-if="form.subscriberType === 'MQ'">
          <el-form-item label="Topic" prop="mqTopic">
            <el-input v-model="form.mqTopic" placeholder="目标 Kafka Topic" />
          </el-form-item>
          <el-form-item label="Tag">
            <el-input v-model="form.mqTag" placeholder="可选，消息标签" />
          </el-form-item>
        </template>

        <el-divider />

        <el-form-item label="重试策略" prop="retryStrategy">
          <el-select v-model="form.retryStrategy" style="width: 200px">
            <el-option v-for="s in RETRY_STRATEGIES" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大重试" prop="maxRetryTimes" v-if="form.retryStrategy !== 'NONE'">
          <el-input-number v-model="form.maxRetryTimes" :min="0" :max="10" />
          <span style="margin-left: 8px; color: #909399">次</span>
        </el-form-item>
        <el-form-item label="超时时间" prop="timeoutSeconds">
          <el-input-number v-model="form.timeoutSeconds" :min="5" :max="120" />
          <span style="margin-left: 8px; color: #909399">秒</span>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="100" />
          <span style="margin-left: 8px; color: #909399">数字越大优先级越高</span>
        </el-form-item>
        <el-form-item label="过滤表达式">
          <el-input 
            v-model="form.filterExpression" 
            type="textarea" 
            :rows="2" 
            placeholder="可选，SpEL 表达式，如: #data.type == 'VIP'"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch 
            v-model="form.status" 
            active-value="ACTIVE" 
            inactive-value="INACTIVE"
          />
          <span style="margin-left: 8px; color: #909399">{{ form.status === 'ACTIVE' ? '启用' : '禁用' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getEventSubscriptions,
  createEventSubscription,
  updateEventSubscription,
  deleteEventSubscription,
  pauseEventSubscription,
  resumeEventSubscription,
  SUBSCRIBER_TYPES,
  HTTP_METHODS,
  RETRY_STRATEGIES,
  type EventSubscription
} from '@/api/event'

// 数据
const loading = ref(false)
const tableData = ref<EventSubscription[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  eventCode: '',
  status: ''
})

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<EventSubscription>({
  subscriberName: '',
  eventCode: '',
  subscriberType: 'WEBHOOK',
  callbackUrl: '',
  callbackMethod: 'POST',
  callbackHeaders: '',
  mqTopic: '',
  mqTag: '',
  filterExpression: '',
  retryStrategy: 'EXPONENTIAL',
  maxRetryTimes: 3,
  timeoutSeconds: 30,
  status: 'ACTIVE',
  priority: 0
})

// 表单校验规则
const formRules: FormRules = {
  subscriberName: [{ required: true, message: '请输入订阅名称', trigger: 'blur' }],
  eventCode: [{ required: true, message: '请输入事件编码', trigger: 'blur' }],
  subscriberType: [{ required: true, message: '请选择订阅类型', trigger: 'change' }],
  callbackUrl: [{ required: true, message: '请输入回调地址', trigger: 'blur' }],
  mqTopic: [{ required: true, message: '请输入 Topic', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getEventSubscriptions({
      eventCode: filterForm.eventCode || undefined,
      status: filterForm.status || undefined,
      page: pagination.page,
      size: pagination.size
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载订阅列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.eventCode = ''
  filterForm.status = ''
  pagination.page = 1
  loadData()
}

// 打开创建弹窗
const openCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row: EventSubscription) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.subscriberName = ''
  form.eventCode = ''
  form.subscriberType = 'WEBHOOK'
  form.callbackUrl = ''
  form.callbackMethod = 'POST'
  form.callbackHeaders = ''
  form.mqTopic = ''
  form.mqTag = ''
  form.filterExpression = ''
  form.retryStrategy = 'EXPONENTIAL'
  form.maxRetryTimes = 3
  form.timeoutSeconds = 30
  form.status = 'ACTIVE'
  form.priority = 0
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  
  submitting.value = true
  try {
    if (isEdit.value && form.id) {
      await updateEventSubscription(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createEventSubscription(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    submitting.value = false
  }
}

// 暂停
const handlePause = async (row: EventSubscription) => {
  await ElMessageBox.confirm(`确定要暂停订阅 "${row.subscriberName}" 吗？`, '确认暂停', {
    type: 'warning'
  })
  
  try {
    await pauseEventSubscription(row.id!)
    ElMessage.success('已暂停')
    loadData()
  } catch (error) {
    console.error('暂停失败:', error)
  }
}

// 恢复
const handleResume = async (row: EventSubscription) => {
  try {
    await resumeEventSubscription(row.id!)
    ElMessage.success('已恢复')
    loadData()
  } catch (error) {
    console.error('恢复失败:', error)
  }
}

// 删除
const handleDelete = async (row: EventSubscription) => {
  await ElMessageBox.confirm(`确定要删除订阅 "${row.subscriberName}" 吗？`, '确认删除', {
    type: 'warning'
  })
  
  try {
    await deleteEventSubscription(row.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 工具函数
const getSubscriberTypeLabel = (type: string) => {
  return SUBSCRIBER_TYPES.find(t => t.value === type)?.label || type
}

const getSubscriberTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'WEBHOOK': 'primary',
    'MQ': 'success',
    'SERVICE': 'warning'
  }
  return map[type] || 'info'
}

const getRetryStrategyLabel = (strategy: string) => {
  return RETRY_STRATEGIES.find(s => s.value === strategy)?.label || strategy
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'ACTIVE': '启用',
    'PAUSED': '暂停',
    'INACTIVE': '禁用'
  }
  return map[status] || status
}

const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'ACTIVE': 'success',
    'PAUSED': 'warning',
    'INACTIVE': 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.event-subscriptions-page {
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

.event-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #409eff;
}

.callback-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.callback-url {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.text-muted {
  color: #909399;
}
</style>
