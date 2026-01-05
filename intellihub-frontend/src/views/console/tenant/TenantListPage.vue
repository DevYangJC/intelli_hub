<template>
  <div class="tenant-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">租户管理</h2>
        <span class="page-desc">管理平台租户和资源配额</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建租户
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="租户名称">
          <el-input v-model="filterForm.name" placeholder="请输入租户名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 120px">
            <el-option label="正常" value="active" />
            <el-option label="已停用" value="disabled" />
            <el-option label="已过期" value="expired" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 租户列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tenantList" style="width: 100%" v-loading="loading" border>
        <el-table-column label="租户信息" min-width="200">
          <template #default="{ row }">
            <div class="tenant-info">
              <div class="tenant-avatar" :style="{ background: row.avatarBg }">
                {{ row.name?.charAt(0) }}
              </div>
              <div class="tenant-detail">
                <div class="tenant-name">{{ row.name }}</div>
                <div class="tenant-id">{{ row.tenantId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="联系人" width="130" align="center">
          <template #default="{ row }">
            <span>{{ row.contactName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配额使用" width="200" align="center">
          <template #default="{ row }">
            <div class="quota-usage-cell">
              <el-progress
                :percentage="row.apiUsagePercent || 0"
                :stroke-width="8"
                :color="getProgressColor(row.apiUsagePercent || 0)"
                :show-text="false"
                style="width: 100px; display: inline-block;"
              />
              <span class="quota-text">{{ row.apiUsage }}/{{ row.apiQuota }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户数" width="80" align="center">
          <template #default="{ row }">
            {{ row.userCount }}
          </template>
        </el-table-column>
        <el-table-column label="应用数" width="80" align="center">
          <template #default="{ row }">
            {{ row.appCount }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="110" align="center">
          <template #default="{ row }">
            <span>{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="primary" link size="small" @click="handleQuota(row)">
              配额
            </el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
              <el-button type="primary" link size="small">
                更多
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="users">
                    <el-icon><User /></el-icon>
                    用户管理
                  </el-dropdown-item>
                  <el-dropdown-item command="apps">
                    <el-icon><Grid /></el-icon>
                    应用管理
                  </el-dropdown-item>
                  <el-dropdown-item command="stats">
                    <el-icon><DataLine /></el-icon>
                    使用统计
                  </el-dropdown-item>
                  <el-dropdown-item command="disable" v-if="row.status === 'active'" divided>
                    <el-icon><CircleClose /></el-icon>
                    停用租户
                  </el-dropdown-item>
                  <el-dropdown-item command="enable" v-if="row.status === 'disabled'">
                    <el-icon><CircleCheck /></el-icon>
                    启用租户
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
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>

    <!-- 创建/编辑租户弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑租户' : '创建租户'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="租户名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入租户名称" />
        </el-form-item>
        <el-form-item label="租户编码" prop="tenantId" v-if="!isEdit">
          <el-input v-model="formData.tenantId" placeholder="唯一标识，如：company_abc" />
          <div class="form-tip">租户唯一标识，创建后不可修改</div>
        </el-form-item>
        
        <!-- 管理员信息（仅创建时显示） -->
        <template v-if="!isEdit">
          <el-divider content-position="left">管理员信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="管理员账号" prop="adminUsername">
                <el-input v-model="formData.adminUsername" placeholder="管理员登录账号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="管理员密码" prop="adminPassword">
                <el-input v-model="formData.adminPassword" type="password" placeholder="管理员登录密码" show-password />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="管理员邮箱" prop="adminEmail">
                <el-input v-model="formData.adminEmail" placeholder="管理员邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="管理员手机" prop="adminPhone">
                <el-input v-model="formData.adminPhone" placeholder="管理员手机号" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
        
        <el-divider content-position="left" v-if="!isEdit">联系信息</el-divider>
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 配额管理弹窗 -->
    <el-dialog
      v-model="quotaDialogVisible"
      title="配额管理"
      width="560px"
    >
      <div v-if="currentTenant" class="quota-dialog" v-loading="quotaLoading">
        <div class="tenant-header">
          <div class="tenant-avatar" :style="{ background: currentTenant.avatarBg || '#409eff' }">
            {{ currentTenant.name?.charAt(0) }}
          </div>
          <div>
            <div class="tenant-name">{{ currentTenant.name }}</div>
            <div class="tenant-id">{{ currentTenant.code || currentTenant.tenantId }}</div>
          </div>
        </div>

        <!-- 使用量概览 -->
        <div class="quota-usage-section" v-if="currentTenant.usage">
          <div class="section-title">使用量概览</div>
          <div class="usage-grid">
            <div class="usage-item">
              <div class="usage-label">用户数</div>
              <el-progress 
                :percentage="currentTenant.usage.userUsagePercent || 0"
                :color="getProgressColor(currentTenant.usage.userUsagePercent || 0)"
              />
              <div class="usage-text">{{ currentTenant.usage.userCount }} / {{ quotaForm.maxUsers }}</div>
            </div>
            <div class="usage-item">
              <div class="usage-label">应用数</div>
              <el-progress 
                :percentage="currentTenant.usage.appUsagePercent || 0"
                :color="getProgressColor(currentTenant.usage.appUsagePercent || 0)"
              />
              <div class="usage-text">{{ currentTenant.usage.appCount }} / {{ quotaForm.maxApps }}</div>
            </div>
            <div class="usage-item">
              <div class="usage-label">API数</div>
              <el-progress 
                :percentage="currentTenant.usage.apiUsagePercent || 0"
                :color="getProgressColor(currentTenant.usage.apiUsagePercent || 0)"
              />
              <div class="usage-text">{{ currentTenant.usage.apiCount }} / {{ quotaForm.maxApis }}</div>
            </div>
            <div class="usage-item">
              <div class="usage-label">今日调用</div>
              <el-progress 
                :percentage="currentTenant.usage.todayCallsPercent || 0"
                :color="getProgressColor(currentTenant.usage.todayCallsPercent || 0)"
              />
              <div class="usage-text">{{ currentTenant.usage.todayCalls }} / {{ quotaForm.maxCallsPerDay }}</div>
            </div>
          </div>
        </div>

        <!-- 配额设置 -->
        <div class="quota-settings-section">
          <div class="section-title">配额设置</div>
          <el-form label-width="120px">
            <el-form-item label="最大用户数">
              <el-input-number v-model="quotaForm.maxUsers" :min="1" :max="10000" />
              <span class="form-unit">人</span>
            </el-form-item>
            <el-form-item label="最大应用数">
              <el-input-number v-model="quotaForm.maxApps" :min="1" :max="1000" />
              <span class="form-unit">个</span>
            </el-form-item>
            <el-form-item label="最大API数">
              <el-input-number v-model="quotaForm.maxApis" :min="1" :max="10000" />
              <span class="form-unit">个</span>
            </el-form-item>
            <el-form-item label="每日调用次数">
              <el-input-number v-model="quotaForm.maxCallsPerDay" :min="1000" :step="10000" />
              <span class="form-unit">次</span>
            </el-form-item>
            <el-form-item label="每月调用次数">
              <el-input-number v-model="quotaForm.maxCallsPerMonth" :min="10000" :step="100000" />
              <span class="form-unit">次</span>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <el-button @click="quotaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuota" :loading="quotaLoading">保存配额</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Search,
  ArrowDown,
  User,
  Grid,
  DataLine,
  CircleClose,
  CircleCheck,
} from '@element-plus/icons-vue'
import { tenantApi, type TenantResponse } from '@/api/tenant'

// 筛选表单
const filterForm = reactive({
  name: '',
  status: '',
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 状态
const loading = ref(false)
const dialogVisible = ref(false)
const quotaDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const currentTenant = ref<any>(null)

// 租户列表
const tenantList = ref<any[]>([])

// 颜色列表
const avatarColors = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
]

// 获取租户列表
const fetchTenantList = async () => {
  loading.value = true
  try {
    const res = await tenantApi.list({
      page: pagination.page,
      size: pagination.pageSize,
      keyword: filterForm.name,
      status: filterForm.status || undefined,
    })
    const data = res.data
    tenantList.value = (data.records || []).map((item: TenantResponse, index: number) => ({
      ...item,
      tenantId: item.code || item.id,
      apiUsage: item.usage?.apiCount || 0,
      apiQuota: item.quota?.maxApis || 100,
      apiUsagePercent: item.usage?.apiUsagePercent || 0,
      userCount: item.usage?.userCount || 0,
      appCount: item.usage?.appCount || 0,
      avatarBg: avatarColors[index % avatarColors.length],
      expireAt: item.updatedAt || item.createdAt,
    }))
    pagination.total = data.total || 0
  } catch (error: any) {
    console.error('获取租户列表失败:', error)
    ElMessage.error(error.message || '获取租户列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchTenantList()
})

// 表单
const formRef = ref<FormInstance>()
const formData = reactive({
  id: '',
  name: '',
  tenantId: '',
  adminUsername: '',
  adminPassword: '',
  adminEmail: '',
  adminPhone: '',
  remark: '',
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  tenantId: [
    { required: true, message: '请输入租户编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '只能包含小写字母、数字和下划线，且以字母开头', trigger: 'blur' },
  ],
  adminUsername: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为3-20个字符', trigger: 'blur' },
  ],
  adminPassword: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
  ],
}

// 配额表单
const quotaForm = reactive({
  maxUsers: 50,
  maxApps: 10,
  maxApis: 100,
  maxCallsPerDay: 100000,
  maxCallsPerMonth: 3000000,
})
const quotaLoading = ref(false)

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    active: 'success',
    disabled: 'info',
    expired: 'danger',
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    active: '正常',
    disabled: '已停用',
    expired: '已过期',
  }
  return map[status] || status
}

// 获取进度条颜色
const getProgressColor = (percent: number) => {
  if (percent >= 90) return '#f56c6c'
  if (percent >= 70) return '#e6a23c'
  return '#67c23a'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 是否即将过期
const isExpiringSoon = (dateStr: string) => {
  const expireDate = new Date(dateStr)
  const now = new Date()
  const diffDays = (expireDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
  return diffDays <= 30 && diffDays > 0
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchTenantList()
}

// 重置
const handleReset = () => {
  filterForm.name = ''
  filterForm.status = ''
  pagination.page = 1
  fetchTenantList()
}

// 创建租户
const handleCreate = () => {
  isEdit.value = false
  formData.id = ''
  formData.name = ''
  formData.tenantId = ''
  formData.adminUsername = ''
  formData.adminPassword = ''
  formData.adminEmail = ''
  formData.adminPhone = ''
  formData.remark = ''
  dialogVisible.value = true
}

// 编辑租户
const handleEdit = (row: any) => {
  isEdit.value = true
  formData.id = row.id
  formData.name = row.name
  formData.tenantId = row.tenantId || row.code
  formData.remark = row.description || ''
  dialogVisible.value = true
}

// 配额管理
const handleQuota = async (row: any) => {
  currentTenant.value = row
  quotaDialogVisible.value = true
  quotaLoading.value = true
  
  try {
    // 从后端获取最新配额数据
    const res = await tenantApi.getQuota(row.id)
    const quota = res.data
    quotaForm.maxUsers = quota.maxUsers || 50
    quotaForm.maxApps = quota.maxApps || 10
    quotaForm.maxApis = quota.maxApis || 100
    quotaForm.maxCallsPerDay = quota.maxCallsPerDay || 100000
    quotaForm.maxCallsPerMonth = quota.maxCallsPerMonth || 3000000
  } catch (error: any) {
    // 如果获取失败，使用列表中的数据
    quotaForm.maxUsers = row.quota?.maxUsers || 50
    quotaForm.maxApps = row.quota?.maxApps || 10
    quotaForm.maxApis = row.quota?.maxApis || 100
    quotaForm.maxCallsPerDay = row.quota?.maxCallsPerDay || 100000
    quotaForm.maxCallsPerMonth = row.quota?.maxCallsPerMonth || 3000000
  } finally {
    quotaLoading.value = false
  }
}

// 更多操作
const handleCommand = async (command: string, row: any) => {
  switch (command) {
    case 'users':
      ElMessage.info('用户管理功能开发中...')
      break
    case 'apps':
      ElMessage.info('应用管理功能开发中...')
      break
    case 'stats':
      ElMessage.info('使用统计功能开发中...')
      break
    case 'disable':
      try {
        await ElMessageBox.confirm('停用后该租户下所有用户将无法访问，确定要停用吗？', '停用确认', {
          type: 'warning',
        })
        await tenantApi.disable(row.id)
        ElMessage.success('租户已停用')
        fetchTenantList()
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '操作失败')
        }
      }
      break
    case 'enable':
      try {
        await tenantApi.enable(row.id)
        ElMessage.success('租户已启用')
        fetchTenantList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
      break
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (isEdit.value) {
      await tenantApi.update(formData.id, {
        name: formData.name,
        description: formData.remark,
      })
    } else {
      await tenantApi.create({
        name: formData.name,
        code: formData.tenantId,
        adminUsername: formData.adminUsername,
        adminPassword: formData.adminPassword,
        adminEmail: formData.adminEmail || undefined,
        adminPhone: formData.adminPhone || undefined,
        description: formData.remark || undefined,
      })
    }
    
    ElMessage.success(isEdit.value ? '租户更新成功' : '租户创建成功')
    dialogVisible.value = false
    fetchTenantList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 保存配额
const saveQuota = async () => {
  if (!currentTenant.value) return
  
  try {
    await tenantApi.updateQuota(currentTenant.value.id, {
      maxUsers: quotaForm.maxUsers,
      maxApps: quotaForm.maxApps,
      maxApis: quotaForm.maxApis,
      maxCallsPerDay: quotaForm.maxCallsPerDay,
      maxCallsPerMonth: quotaForm.maxCallsPerMonth,
    })
    quotaDialogVisible.value = false
    ElMessage.success('配额已更新')
    fetchTenantList()
  } catch (error: any) {
    ElMessage.error(error.message || '更新配额失败')
  }
}
</script>

<style scoped>
.tenant-list-page {
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

/* 表格卡片 */
.table-card {
  margin-bottom: 16px;
}

/* 租户信息 */
.tenant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tenant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.tenant-detail {
  min-width: 0;
}

.tenant-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.tenant-id {
  font-size: 12px;
  color: #999;
  font-family: 'Monaco', 'Menlo', monospace;
}

.sub-text {
  font-size: 12px;
  color: #999;
}

/* 配额使用单元格 */
.quota-usage-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.quota-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-unit {
  margin-left: 8px;
  color: #666;
}

/* 配额弹窗 */
.quota-dialog .tenant-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.quota-usage-section,
.quota-settings-section {
  margin-top: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.usage-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.usage-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.usage-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.usage-text {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
  text-align: right;
}

/* 警告文本 */
.text-warning {
  color: #e6a23c;
}
</style>
