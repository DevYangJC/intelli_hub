<template>
  <div class="event-definitions-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>事件定义</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建事件
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="事件类型">
          <el-select v-model="filterForm.eventType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="type in EVENT_TYPES" :key="type.value" :label="type.label" :value="type.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" value="ACTIVE" />
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
        <el-table-column prop="eventCode" label="事件编码" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="event-code">{{ row.eventCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="eventName" label="事件名称" width="180" show-overflow-tooltip />
        <el-table-column prop="eventType" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :color="getEventTypeColor(row.eventType)" effect="dark" size="small">
              {{ getEventTypeLabel(row.eventType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="info" link size="small" @click="viewSchema(row)">Schema</el-button>
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
      :title="isEdit ? '编辑事件定义' : '新建事件定义'" 
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="事件编码" prop="eventCode">
          <el-input v-model="form.eventCode" placeholder="例如: user.created, order.paid" :disabled="isEdit" />
          <div class="form-tip">事件唯一标识，建议使用点分命名法</div>
        </el-form-item>
        <el-form-item label="事件名称" prop="eventName">
          <el-input v-model="form.eventName" placeholder="请输入事件名称" />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="form.eventType" placeholder="请选择事件类型" style="width: 100%">
            <el-option v-for="type in EVENT_TYPES" :key="type.value" :label="type.label" :value="type.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入事件描述" />
        </el-form-item>
        <el-form-item label="数据结构">
          <el-input 
            v-model="form.schemaDefinition" 
            type="textarea" 
            :rows="6" 
            placeholder='JSON Schema 格式，例如:
{
  "type": "object",
  "properties": {
    "userId": { "type": "string" },
    "action": { "type": "string" }
  }
}'
          />
          <div class="form-tip">可选，用于描述事件数据的结构</div>
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

    <!-- Schema 查看弹窗 -->
    <el-dialog v-model="schemaDialogVisible" title="数据结构定义" width="600px">
      <pre class="schema-content">{{ currentSchema }}</pre>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getEventDefinitions,
  createEventDefinition,
  updateEventDefinition,
  deleteEventDefinition,
  EVENT_TYPES,
  type EventDefinition
} from '@/api/event'

// 数据
const loading = ref(false)
const tableData = ref<EventDefinition[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  eventType: '',
  status: ''
})

// 弹窗
const dialogVisible = ref(false)
const schemaDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const currentSchema = ref('')

const form = reactive<EventDefinition>({
  eventCode: '',
  eventName: '',
  eventType: 'BUSINESS',
  description: '',
  schemaDefinition: '',
  status: 'ACTIVE'
})

// 表单校验规则
const formRules: FormRules = {
  eventCode: [
    { required: true, message: '请输入事件编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$/, message: '格式不正确，使用小写字母、数字和点', trigger: 'blur' }
  ],
  eventName: [{ required: true, message: '请输入事件名称', trigger: 'blur' }],
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getEventDefinitions({
      eventType: filterForm.eventType || undefined,
      status: filterForm.status || undefined,
      page: pagination.page,
      size: pagination.size
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载事件定义失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.eventType = ''
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
const openEditDialog = (row: EventDefinition) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 查看 Schema
const viewSchema = (row: EventDefinition) => {
  try {
    currentSchema.value = row.schemaDefinition 
      ? JSON.stringify(JSON.parse(row.schemaDefinition), null, 2)
      : '未定义'
  } catch {
    currentSchema.value = row.schemaDefinition || '未定义'
  }
  schemaDialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.eventCode = ''
  form.eventName = ''
  form.eventType = 'BUSINESS'
  form.description = ''
  form.schemaDefinition = ''
  form.status = 'ACTIVE'
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  
  submitting.value = true
  try {
    if (isEdit.value && form.id) {
      await updateEventDefinition(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createEventDefinition(form)
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

// 删除
const handleDelete = async (row: EventDefinition) => {
  await ElMessageBox.confirm(`确定要删除事件 "${row.eventName}" 吗？`, '确认删除', {
    type: 'warning'
  })
  
  try {
    await deleteEventDefinition(row.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 工具函数
const getEventTypeLabel = (type: string) => {
  return EVENT_TYPES.find(t => t.value === type)?.label || type
}

const getEventTypeColor = (type: string) => {
  return EVENT_TYPES.find(t => t.value === type)?.color || '#409eff'
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
.event-definitions-page {
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

.table-card :deep(.el-table) {
  border-radius: 4px;
}

.table-card :deep(.el-table th),
.table-card :deep(.el-table td) {
  padding: 12px 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
}

.event-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #409eff;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.schema-content {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  overflow: auto;
  max-height: 400px;
  margin: 0;
}
</style>
