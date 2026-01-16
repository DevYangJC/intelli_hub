<template>
  <div class="api-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">API列表</h2>
        <span class="page-desc">管理和维护所有API接口</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建API
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="API名称">
          <el-input
            v-model="filterForm.name"
            placeholder="请输入API名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分组">
          <el-select v-model="filterForm.groupId" placeholder="选择分组" clearable style="width: 160px">
            <el-option
              v-for="group in apiGroups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 120px">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已下线" value="offline" />
            <el-option label="已废弃" value="deprecated" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求方式">
          <el-select v-model="filterForm.method" placeholder="选择方式" clearable style="width: 120px">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- API列表表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="apiList"
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="API信息" min-width="280">
          <template #default="{ row }">
            <div class="api-info">
              <div class="api-name">
                <router-link :to="`/console/api/${row.id}`" class="name-link">
                  {{ row.name }}
                </router-link>
                <el-tag v-if="row.version" size="small" type="info">{{ row.version }}</el-tag>
              </div>
              <div class="api-path">
                <el-tag :type="getMethodTagType(row.method)" size="small" class="method-tag">
                  {{ row.method }}
                </el-tag>
                <span class="path-text">{{ row.path }}</span>
              </div>
              <div class="api-desc">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分组" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.groupName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="今日调用" width="100" align="center">
          <template #default="{ row }">
            <span class="stat-value">{{ row.stats?.todayCalls || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总调用量" width="100" align="center">
          <template #default="{ row }">
            <span class="stat-value">{{ row.stats?.totalCalls || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成功率" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSuccessRateType(row.stats?.successRate || 0)" size="small">
              {{ (row.stats?.successRate || 0).toFixed(2) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="平均响应" width="110" align="center">
          <template #default="{ row }">
            <span class="stat-value">{{ (row.stats?.avgResponseTime || 0).toFixed(2) }}ms</span>
          </template>
        </el-table-column>
        <el-table-column label="创建者" width="100" prop="creatorName" />
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="primary" link size="small" @click="handleViewDoc(row)">
              文档
            </el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
              <el-button type="primary" link size="small">
                更多
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="publish" v-if="row.status === 'draft' || row.status === 'offline'">
                    <el-icon><Upload /></el-icon>
                    发布
                  </el-dropdown-item>
                  <el-dropdown-item command="offline" v-if="row.status === 'published'">
                    <el-icon><Download /></el-icon>
                    下线
                  </el-dropdown-item>
                  <el-dropdown-item command="copy">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-dropdown-item>
                  <el-dropdown-item command="history">
                    <el-icon><Clock /></el-icon>
                    版本历史
                  </el-dropdown-item>
                  <el-dropdown-item command="deprecate" v-if="row.status === 'published' || row.status === 'offline'">
                    <el-icon><Warning /></el-icon>
                    废弃
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon color="#f56c6c"><Delete /></el-icon>
                    <span style="color: #f56c6c">删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 发布确认弹窗 -->
    <el-dialog v-model="publishDialogVisible" title="发布API" width="500px">
      <el-alert
        title="发布后API将对外提供服务，请确认API配置正确"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-form label-width="100px">
        <el-form-item label="API名称">
          <span>{{ currentApi?.name }}</span>
        </el-form-item>
        <el-form-item label="请求路径">
          <span>{{ currentApi?.method }} {{ currentApi?.path }}</span>
        </el-form-item>
        <el-form-item label="发布说明">
          <el-input
            v-model="publishNote"
            type="textarea"
            :rows="3"
            placeholder="请输入发布说明（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  ArrowDown,
  Upload,
  Download,
  CopyDocument,
  Clock,
  Delete,
  Warning,
} from '@element-plus/icons-vue'
import { apiManageApi, apiGroupApi, type ApiInfoResponse, type ApiGroupResponse } from '@/api/apiManage'

const router = useRouter()

// 筛选表单
const filterForm = reactive({
  name: '',
  groupId: '',
  status: '',
  method: '',
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 状态
const loading = ref(false)
const selectedRows = ref<any[]>([])
const publishDialogVisible = ref(false)
const currentApi = ref<any>(null)
const publishNote = ref('')

// API分组
const apiGroups = ref<ApiGroupResponse[]>([])

// API列表数据
const apiList = ref<ApiInfoResponse[]>([])

// 获取API分组列表
const fetchApiGroups = async () => {
  try {
    const res = await apiGroupApi.list()
    if (res.code === 200) {
      apiGroups.value = res.data
    }
  } catch (error) {
    console.error('获取API分组失败:', error)
  }
}

// 获取API列表
const fetchApiList = async () => {
  loading.value = true
  try {
    const res = await apiManageApi.list({
      keyword: filterForm.name || undefined,
      groupId: filterForm.groupId || undefined,
      status: filterForm.status || undefined,
      method: filterForm.method || undefined,
      page: pagination.page,
      size: pagination.pageSize,
    })
    if (res.code === 200) {
      apiList.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('获取API列表失败:', error)
    ElMessage.error('获取API列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchApiGroups()
  fetchApiList()
})

// 获取请求方式标签类型
const getMethodTagType = (method: string) => {
  const typeMap: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info',
  }
  return typeMap[method] || 'info'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    draft: 'info',
    published: 'success',
    offline: 'warning',
    deprecated: 'danger',
  }
  return typeMap[status] || 'info'
}

// 获取成功率标签类型
const getSuccessRateType = (rate: number) => {
  if (rate >= 99) return 'success'
  if (rate >= 95) return 'warning'
  return 'danger'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    offline: '已下线',
    deprecated: '已废弃',
  }
  return textMap[status] || status
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchApiList()
}

// 重置
const handleReset = () => {
  filterForm.name = ''
  filterForm.groupId = ''
  filterForm.status = ''
  filterForm.method = ''
  pagination.page = 1
  fetchApiList()
}

// 创建API
const handleCreate = () => {
  router.push('/console/api/create')
}

// 编辑API
const handleEdit = (row: any) => {
  router.push(`/console/api/${row.id}/edit`)
}

// 查看文档（跳转到详情页的文档Tab）
const handleViewDoc = (row: any) => {
  router.push(`/console/api/${row.id}?tab=doc`)
}

// 选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 分页大小变化
const handleSizeChange = () => {
  pagination.page = 1
  fetchApiList()
}

// 页码变化
const handlePageChange = () => {
  fetchApiList()
}

// 更多操作
const handleCommand = async (command: string, row: ApiInfoResponse) => {
  switch (command) {
    case 'publish':
      currentApi.value = row
      publishNote.value = ''
      publishDialogVisible.value = true
      break
    case 'offline':
      ElMessageBox.confirm('确定要下线该API吗？下线后将停止对外服务。', '下线确认', {
        type: 'warning',
      }).then(async () => {
        try {
          await apiManageApi.offline(row.id)
          ElMessage.success('API已下线')
          fetchApiList()
        } catch (error) {
          ElMessage.error('下线失败')
        }
      })
      break
    case 'copy':
      try {
        await apiManageApi.copy(row.id)
        ElMessage.success('API已复制，请在草稿中查看')
        fetchApiList()
      } catch (error) {
        ElMessage.error('复制失败')
      }
      break
    case 'history':
      ElMessage.info('版本历史功能开发中...')
      break
    case 'deprecate':
      ElMessageBox.confirm('确定要废弃该API吗？废弃后将不再推荐使用。', '废弃确认', {
        type: 'warning',
      }).then(async () => {
        try {
          await apiManageApi.deprecate(row.id)
          ElMessage.success('API已废弃')
          fetchApiList()
        } catch (error) {
          ElMessage.error('废弃失败')
        }
      })
      break
    case 'delete':
      ElMessageBox.confirm('确定要删除该API吗？删除后无法恢复。', '删除确认', {
        type: 'error',
      }).then(async () => {
        try {
          await apiManageApi.delete(row.id)
          ElMessage.success('API已删除')
          fetchApiList()
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
      break
  }
}

// 确认发布
const confirmPublish = async () => {
  if (!currentApi.value) return
  try {
    await apiManageApi.publish(currentApi.value.id)
    publishDialogVisible.value = false
    ElMessage.success('API发布成功')
    fetchApiList()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}
</script>

<style scoped>
.api-list-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.page-desc {
  font-size: 14px;
  color: #999;
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

/* 表格卡片 */
.table-card {
  margin-bottom: 16px;
}

/* API信息单元格 */
.api-info {
  padding: 8px 0;
}

.api-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.name-link {
  font-size: 14px;
  font-weight: 500;
  color: #1890ff;
  text-decoration: none;
}

.name-link:hover {
  text-decoration: underline;
}

.api-path {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.method-tag {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 11px;
}

.path-text {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #666;
}

.api-desc {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

/* 调用统计 */
.call-stats {
  font-size: 12px;
}

.stat-item {
  display: flex;
  gap: 4px;
  margin-bottom: 2px;
}

.stat-label {
  color: #999;
}

.stat-value {
  color: #1a1a1a;
  font-weight: 500;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

/* 操作列按钮对齐已移至全局样式 global.css */
</style>
