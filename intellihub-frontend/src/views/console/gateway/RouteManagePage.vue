<template>
  <div class="route-manage-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">路由管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建路由
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="路由名称或路径"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable @clear="handleSearch">
            <el-option label="已发布" value="published" />
            <el-option label="草稿" value="draft" />
            <el-option label="已下线" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求方法">
          <el-select v-model="queryForm.method" placeholder="全部" clearable @clear="handleSearch">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="routeList" v-loading="loading" border stripe>
        <el-table-column prop="name" label="路由名称" min-width="150" />
        <el-table-column prop="method" label="请求方法" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodType(row.method)">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="请求路径" min-width="200" />
        <el-table-column prop="backendHost" label="后端地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="authType" label="认证方式" width="100">
          <template #default="{ row }">
            <el-tag>{{ getAuthTypeLabel(row.authType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timeout" label="超时(ms)" width="100" />
        <el-table-column prop="rateLimitEnabled" label="限流" width="80">
          <template #default="{ row }">
            <el-tag :type="row.rateLimitEnabled ? 'success' : 'info'">
              {{ row.rateLimitEnabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              link 
              :type="row.status === 'published' ? 'warning' : 'success'" 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'published' ? '下线' : '发布' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-alert
        title="提示"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        路由配置将通过事件机制自动同步到网关,无需手动刷新
      </el-alert>
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="路由名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入路由名称" />
        </el-form-item>
        <el-form-item label="请求方法" prop="method">
          <el-select v-model="formData.method" placeholder="请选择请求方法">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求路径" prop="path">
          <el-input v-model="formData.path" placeholder="/api/v1/example" />
        </el-form-item>
        <el-form-item label="认证方式" prop="authType">
          <el-radio-group v-model="formData.authType">
            <el-radio label="none">无认证</el-radio>
            <el-radio label="token">Token认证</el-radio>
            <el-radio label="signature">签名认证</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="后端地址" prop="backendHost">
          <el-input v-model="formData.backendHost" placeholder="http://service-name:8080" />
        </el-form-item>
        <el-form-item label="后端路径" prop="backendPath">
          <el-input v-model="formData.backendPath" placeholder="/internal/api/example" />
        </el-form-item>
        <el-form-item label="超时时间(ms)" prop="timeout">
          <el-input-number v-model="formData.timeout" :min="1000" :max="60000" />
        </el-form-item>
        <el-form-item label="启用限流">
          <el-switch v-model="formData.rateLimitEnabled" />
        </el-form-item>
        <el-form-item label="限流QPS" v-if="formData.rateLimitEnabled" prop="rateLimitQps">
          <el-input-number v-model="formData.rateLimitQps" :min="1" :max="10000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
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
import { apiManageApi } from '@/api/apiManage'

const loading = ref(false)
const submitting = ref(false)
const routeList = ref<any[]>([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: '',
  method: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('创建路由')
const formRef = ref<FormInstance>()
const formData = ref<any>({
  name: '',
  method: 'GET',
  path: '',
  authType: 'token',
  backendHost: '',
  backendPath: '',
  timeout: 30000,
  rateLimitEnabled: false,
  rateLimitQps: 100
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入路由名称', trigger: 'blur' }],
  method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  path: [
    { required: true, message: '请输入请求路径', trigger: 'blur' },
    { pattern: /^\/.*/, message: '路径必须以/开头', trigger: 'blur' }
  ],
  backendHost: [{ required: true, message: '请输入后端地址', trigger: 'blur' }],
  backendPath: [{ required: true, message: '请输入后端路径', trigger: 'blur' }]
}

onMounted(() => {
  loadRoutes()
})

const loadRoutes = async () => {
  loading.value = true
  try {
    const res = await apiManageApi.list(queryForm)
    if (res.code === 200) {
      routeList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载路由列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.page = 1
  loadRoutes()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.status = ''
  queryForm.method = ''
  handleSearch()
}

const handleCreate = () => {
  dialogTitle.value = '创建路由'
  formData.value = {
    name: '',
    method: 'GET',
    path: '',
    authType: 'token',
    backendHost: '',
    backendPath: '',
    timeout: 30000,
    rateLimitEnabled: false,
    rateLimitQps: 100
  }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑路由'
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
      await apiManageApi.update(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await apiManageApi.create(formData.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRoutes()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row: any) => {
  const action = row.status === 'published' ? '下线' : '发布'
  try {
    if (row.status === 'published') {
      await apiManageApi.offline(row.id)
    } else {
      await apiManageApi.publish(row.id)
    }
    ElMessage.success(`${action}成功`)
    loadRoutes()
  } catch (error) {
    ElMessage.error(`${action}失败`)
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除路由"${row.name}"吗?`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await apiManageApi.delete(row.id)
      ElMessage.success('删除成功')
      loadRoutes()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const getMethodType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger'
  }
  return types[method] || 'info'
}

const getAuthTypeLabel = (authType: string) => {
  const labels: Record<string, string> = {
    none: '无认证',
    token: 'Token',
    signature: '签名'
  }
  return labels[authType] || authType
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    published: 'success',
    draft: 'info',
    offline: 'warning'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    published: '已发布',
    draft: '草稿',
    offline: '已下线'
  }
  return labels[status] || status
}
</script>

<style scoped lang="scss">
.route-manage-page {
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
