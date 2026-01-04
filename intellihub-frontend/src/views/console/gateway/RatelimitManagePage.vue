<template>
  <div class="ratelimit-manage-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">限流策略管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建策略
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="策略名称或描述"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable @clear="handleSearch">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 策略列表 -->
      <el-table :data="policyList" v-loading="loading" border stripe>
        <el-table-column prop="name" label="策略名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="限流类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'qps' ? 'primary' : 'success'">
              {{ row.type === 'qps' ? 'QPS' : '并发数' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dimension" label="限流维度" width="120">
          <template #default="{ row }">
            <el-tag>{{ getDimensionLabel(row.dimension) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="limitValue" label="限流阈值" width="100" />
        <el-table-column prop="timeWindow" label="时间窗口(秒)" width="120" />
        <el-table-column prop="appliedRoutes" label="应用路由数" width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewRoutes(row)">
              {{ row.appliedRoutes || 0 }} 个
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleApply(row)">
              应用
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        class="pagination"
      />
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="策略名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入策略名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入策略描述"
          />
        </el-form-item>
        <el-form-item label="限流类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio label="qps">QPS限流</el-radio>
            <el-radio label="concurrency">并发数限流</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="限流维度" prop="dimension">
          <el-select v-model="formData.dimension" placeholder="请选择限流维度">
            <el-option label="全局" value="global" />
            <el-option label="IP" value="ip" />
            <el-option label="路径" value="path" />
            <el-option label="IP+路径" value="ip_path" />
            <el-option label="用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="限流阈值" prop="limitValue">
          <el-input-number
            v-model="formData.limitValue"
            :min="1"
            :max="100000"
            placeholder="请输入限流阈值"
          />
        </el-form-item>
        <el-form-item label="时间窗口(秒)" prop="timeWindow">
          <el-input-number
            v-model="formData.timeWindow"
            :min="1"
            :max="3600"
            placeholder="请输入时间窗口"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 应用策略对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="应用限流策略"
      width="600px"
    >
      <el-alert
        title="提示"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        选择要应用此限流策略的路由,已应用的路由将被覆盖
      </el-alert>
      <el-transfer
        v-model="selectedRoutes"
        :data="availableRoutes"
        :titles="['可用路由', '已选路由']"
        filterable
        filter-placeholder="搜索路由"
      />
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApplySubmit" :loading="applying">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listRatelimitPolicies,
  createRatelimitPolicy,
  updateRatelimitPolicy,
  deleteRatelimitPolicy,
  applyPolicyToRoutes,
  RatelimitPolicy
} from '@/api/ratelimit'
import { apiManageApi } from '@/api/apiManage'

const loading = ref(false)
const submitting = ref(false)
const applying = ref(false)
const policyList = ref<RatelimitPolicy[]>([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('创建限流策略')
const formRef = ref<FormInstance>()
const formData = ref<RatelimitPolicy>({
  name: '',
  description: '',
  type: 'qps',
  dimension: 'global',
  limitValue: 100,
  timeWindow: 1
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择限流类型', trigger: 'change' }],
  dimension: [{ required: true, message: '请选择限流维度', trigger: 'change' }],
  limitValue: [{ required: true, message: '请输入限流阈值', trigger: 'blur' }],
  timeWindow: [{ required: true, message: '请输入时间窗口', trigger: 'blur' }]
}

const applyDialogVisible = ref(false)
const currentPolicy = ref<RatelimitPolicy | null>(null)
const selectedRoutes = ref<string[]>([])
const availableRoutes = ref<any[]>([])

onMounted(() => {
  loadPolicies()
})

const loadPolicies = async () => {
  loading.value = true
  try {
    const res = await listRatelimitPolicies(queryForm)
    if (res.code === 200) {
      policyList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载限流策略失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.page = 1
  loadPolicies()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.status = ''
  handleSearch()
}

const handleCreate = () => {
  dialogTitle.value = '创建限流策略'
  formData.value = {
    name: '',
    description: '',
    type: 'qps',
    dimension: 'global',
    limitValue: 100,
    timeWindow: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row: RatelimitPolicy) => {
  dialogTitle.value = '编辑限流策略'
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  
  submitting.value = true
  try {
    if (formData.value.id) {
      await updateRatelimitPolicy(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createRatelimitPolicy(formData.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadPolicies()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row: RatelimitPolicy) => {
  ElMessageBox.confirm(
    `确定要删除限流策略"${row.name}"吗?`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteRatelimitPolicy(row.id!)
      ElMessage.success('删除成功')
      loadPolicies()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleApply = async (row: RatelimitPolicy) => {
  currentPolicy.value = row
  selectedRoutes.value = []
  
  try {
    const res = await apiManageApi.list({ page: 1, size: 1000, status: 'published' })
    if (res.code === 200) {
      availableRoutes.value = res.data.records.map((api: any) => ({
        key: api.id,
        label: `${api.method} ${api.path} - ${api.name}`
      }))
    }
  } catch (error) {
    ElMessage.error('加载路由列表失败')
  }
  
  applyDialogVisible.value = true
}

const handleApplySubmit = async () => {
  if (!currentPolicy.value) return
  
  applying.value = true
  try {
    await applyPolicyToRoutes(currentPolicy.value.id!, {
      routeIds: selectedRoutes.value
    })
    ElMessage.success('应用成功')
    applyDialogVisible.value = false
    loadPolicies()
  } catch (error) {
    ElMessage.error('应用失败')
  } finally {
    applying.value = false
  }
}

const handleViewRoutes = (row: RatelimitPolicy) => {
  ElMessage.info('查看应用路由功能开发中...')
}

const getDimensionLabel = (dimension: string) => {
  const labels: Record<string, string> = {
    global: '全局',
    ip: 'IP',
    path: '路径',
    ip_path: 'IP+路径',
    user: '用户'
  }
  return labels[dimension] || dimension
}
</script>

<style scoped lang="scss">
.ratelimit-manage-page {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
