<template>
  <div class="alert-rules-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>告警规则</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建规则
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="规则类型">
          <el-select v-model="filterForm.ruleType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="type in RULE_TYPES" :key="type.value" :label="type.label" :value="type.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRules">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 规则列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="rules" v-loading="loading" stripe>
        <el-table-column prop="name" label="规则名称" min-width="150" />
        <el-table-column prop="ruleType" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRuleTypeTag(row.ruleType)">{{ getRuleTypeLabel(row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="条件" width="180">
          <template #default="{ row }">
            {{ getRuleTypeLabel(row.ruleType) }} {{ getOperatorLabel(row.operator) }} {{ row.threshold }}{{ getRuleTypeUnit(row.ruleType) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="持续时间" width="100" align="center">
          <template #default="{ row }">
            {{ row.duration }}秒
          </template>
        </el-table-column>
        <el-table-column prop="notifyChannels" label="通知渠道" width="150">
          <template #default="{ row }">
            <el-tag v-for="channel in parseChannels(row.notifyChannels)" :key="channel" size="small" style="margin-right: 4px">
              {{ getChannelLabel(channel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              active-value="active" 
              inactive-value="disabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadRules"
          @current-change="loadRules"
        />
      </div>
    </el-card>

    <!-- 创建/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑规则' : '新建规则'" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option v-for="type in RULE_TYPES" :key="type.value" :label="type.label" :value="type.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发条件" required>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item prop="operator">
                <el-select v-model="form.operator" placeholder="运算符">
                  <el-option v-for="op in OPERATORS" :key="op.value" :label="op.label" :value="op.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item prop="threshold">
                <el-input-number v-model="form.threshold" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <span class="unit-text">{{ getRuleTypeUnit(form.ruleType) }}</span>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="持续时间" prop="duration">
          <el-input-number v-model="form.duration" :min="10" :max="3600" :step="10" />
          <span style="margin-left: 8px; color: #909399">秒</span>
        </el-form-item>
        <el-form-item label="通知渠道">
          <el-checkbox-group v-model="selectedChannels">
            <el-checkbox v-for="channel in NOTIFY_CHANNELS" :key="channel.value" :label="channel.value">
              {{ channel.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="通知目标" v-if="selectedChannels.length > 0">
          <el-input v-model="form.notifyTargets" type="textarea" :rows="2" placeholder="多个目标用逗号分隔，如邮箱地址、Webhook URL等" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-value="active" inactive-value="disabled" />
          <span style="margin-left: 8px; color: #909399">{{ form.status === 'active' ? '启用' : '禁用' }}</span>
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
  getAlertRules,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  enableAlertRule,
  disableAlertRule,
  RULE_TYPES,
  OPERATORS,
  NOTIFY_CHANNELS,
  type AlertRule
} from '@/api/alert'

// 数据
const loading = ref(false)
const rules = ref<AlertRule[]>([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  ruleType: '',
  status: ''
})

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const selectedChannels = ref<string[]>([])

const form = reactive<AlertRule>({
  name: '',
  ruleType: 'error_rate',
  threshold: 5,
  operator: 'gt',
  duration: 60,
  notifyChannels: '',
  notifyTargets: '',
  status: 'active'
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  operator: [{ required: true, message: '请选择运算符', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入持续时间', trigger: 'blur' }]
}

// 加载规则列表
const loadRules = async () => {
  loading.value = true
  try {
    const res = await getAlertRules({
      ruleType: filterForm.ruleType || undefined,
      status: filterForm.status || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    if (res.data.code === 200 && res.data.data) {
      rules.value = res.data.data.records || []
      pagination.total = res.data.data.total || 0
    }
  } catch (error) {
    console.error('加载规则列表失败', error)
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.ruleType = ''
  filterForm.status = ''
  pagination.pageNum = 1
  loadRules()
}

// 打开创建弹窗
const openCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row: AlertRule) => {
  isEdit.value = true
  Object.assign(form, row)
  selectedChannels.value = parseChannels(row.notifyChannels)
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.ruleType = 'error_rate'
  form.threshold = 5
  form.operator = 'gt'
  form.duration = 60
  form.notifyChannels = ''
  form.notifyTargets = ''
  form.status = 'active'
  selectedChannels.value = []
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value?.validate()
  
  submitting.value = true
  try {
    form.notifyChannels = selectedChannels.value.join(',')
    
    if (isEdit.value && form.id) {
      await updateAlertRule(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createAlertRule(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRules()
  } catch (error) {
    console.error('保存失败', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 状态变更
const handleStatusChange = async (row: AlertRule) => {
  try {
    if (row.status === 'active') {
      await enableAlertRule(row.id!)
      ElMessage.success('已启用')
    } else {
      await disableAlertRule(row.id!)
      ElMessage.success('已禁用')
    }
  } catch (error) {
    // 恢复原状态
    row.status = row.status === 'active' ? 'disabled' : 'active'
    ElMessage.error('操作失败')
  }
}

// 删除规则
const handleDelete = async (row: AlertRule) => {
  await ElMessageBox.confirm(`确定要删除规则「${row.name}」吗？`, '提示', {
    type: 'warning'
  })
  
  try {
    await deleteAlertRule(row.id!)
    ElMessage.success('删除成功')
    loadRules()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 工具函数
const getRuleTypeLabel = (type: string) => {
  return RULE_TYPES.find(t => t.value === type)?.label || type
}

const getRuleTypeUnit = (type: string) => {
  return RULE_TYPES.find(t => t.value === type)?.unit || ''
}

const getRuleTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    error_rate: 'danger',
    latency: 'warning',
    qps: 'primary'
  }
  return tags[type] || 'info'
}

const getOperatorLabel = (op: string) => {
  const labels: Record<string, string> = {
    gt: '>',
    gte: '>=',
    lt: '<',
    lte: '<=',
    eq: '='
  }
  return labels[op] || op
}

const parseChannels = (channels?: string) => {
  if (!channels) return []
  return channels.split(',').filter(c => c.trim())
}

const getChannelLabel = (channel: string) => {
  return NOTIFY_CHANNELS.find(c => c.value === channel)?.label || channel
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped lang="scss">
.alert-rules-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
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

.unit-text {
  line-height: 32px;
  color: #909399;
}
</style>
