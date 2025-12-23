<template>
  <div class="app-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">应用列表</h2>
        <span class="page-desc">管理您的应用和API访问凭证</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建应用
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filterForm.name"
        placeholder="应用名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-input
        v-model="filterForm.code"
        placeholder="应用编码"
        clearable
        style="width: 160px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select
        v-model="filterForm.appType"
        placeholder="应用类型"
        clearable
        style="width: 120px"
        @change="handleSearch"
      >
        <el-option label="内部应用" value="internal" />
        <el-option label="外部应用" value="external" />
      </el-select>
      <el-select
        v-model="filterForm.status"
        placeholder="状态"
        clearable
        style="width: 120px"
        @change="handleSearch"
      >
        <el-option label="运行中" value="active" />
        <el-option label="已停用" value="disabled" />
        <el-option label="已过期" value="expired" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </div>

    <!-- 应用卡片列表 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :lg="8" v-for="app in appList" :key="app.id">
        <el-card class="app-card" shadow="hover">
          <div class="app-header">
            <div class="app-icon" :style="{ background: app.iconBg }">
              {{ app.name.charAt(0) }}
            </div>
            <div class="app-info">
              <h3 class="app-name">{{ app.name }}</h3>
              <el-tag :type="getStatusType(app.status)" size="small">
                {{ getStatusText(app.status) }}
              </el-tag>
            </div>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, app)">
              <el-button type="text" class="more-btn">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="keys">
                    <el-icon><Key /></el-icon>
                    密钥管理
                  </el-dropdown-item>
                  <el-dropdown-item command="apis">
                    <el-icon><Connection /></el-icon>
                    API授权
                  </el-dropdown-item>
                  <el-dropdown-item command="disable" v-if="app.status === 'active'" divided>
                    <el-icon><CircleClose /></el-icon>
                    停用
                  </el-dropdown-item>
                  <el-dropdown-item command="enable" v-if="app.status === 'disabled'">
                    <el-icon><CircleCheck /></el-icon>
                    启用
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon color="#f56c6c"><Delete /></el-icon>
                    <span style="color: #f56c6c">删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <p class="app-desc">{{ app.description || '暂无描述' }}</p>

          <div class="app-stats">
            <div class="stat-item">
              <span class="stat-value">{{ app.apiCount }}</span>
              <span class="stat-label">已授权API</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ app.todayCalls }}</span>
              <span class="stat-label">今日调用</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ app.keyCount }}</span>
              <span class="stat-label">密钥数量</span>
            </div>
          </div>

          <div class="app-footer">
            <span class="app-id">AppID: {{ app.appId }}</span>
            <span class="app-time">创建于 {{ formatDate(app.createdAt) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="appList.length === 0" description="暂无应用，点击上方按钮创建">
      <el-button type="primary" @click="handleCreate">创建应用</el-button>
    </el-empty>

    <!-- 创建/编辑应用弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑应用' : '创建应用'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入应用名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="应用编码" prop="code" v-if="!isEdit">
          <el-input v-model="formData.code" placeholder="如：my-app" maxlength="50" />
          <div class="form-tip">应用唯一标识，创建后不可修改</div>
        </el-form-item>
        <el-form-item label="应用类型">
          <el-radio-group v-model="formData.appType">
            <el-radio value="external">外部应用</el-radio>
            <el-radio value="internal">内部应用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="应用描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入应用描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="回调地址">
          <el-input v-model="formData.callbackUrl" placeholder="https://your-domain.com/callback" />
          <div class="form-tip">用于接收异步通知的回调地址</div>
        </el-form-item>
        <el-form-item label="IP白名单">
          <el-input
            v-model="formData.ipWhitelist"
            type="textarea"
            :rows="2"
            placeholder="多个IP用逗号分隔，留空表示不限制"
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="formData.contactName" placeholder="联系人姓名" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="formData.contactEmail" placeholder="联系人邮箱" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.contactPhone" placeholder="联系人电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 密钥管理弹窗 -->
    <el-dialog
      v-model="keyDialogVisible"
      title="密钥管理"
      width="700px"
    >
      <div class="key-dialog-content" v-if="currentApp">
        <el-alert
          title="安全提示"
          type="warning"
          description="AppSecret 仅在创建时显示一次，请妥善保管。如果遗失，请重新生成。"
          show-icon
          :closable="false"
          style="margin-bottom: 16px"
        />

        <div class="app-id-row">
          <span class="label">AppID:</span>
          <span class="value">{{ currentApp.appId }}</span>
          <el-button type="primary" link size="small" @click="copyText(currentApp.appId)">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
        </div>

        <el-table :data="keyList" style="width: 100%">
          <el-table-column label="密钥名称" prop="name" width="120" />
          <el-table-column label="AppSecret" min-width="200">
            <template #default="{ row }">
              <div class="secret-cell">
                <span v-if="row.showSecret">{{ row.secret }}</span>
                <span v-else>{{ maskSecret(row.secret) }}</span>
                <el-button type="primary" link size="small" @click="row.showSecret = !row.showSecret">
                  <el-icon>
                    <View v-if="!row.showSecret" />
                    <Hide v-else />
                  </el-icon>
                </el-button>
                <el-button type="primary" link size="small" @click="copyText(row.secret)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                {{ row.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                size="small"
                @click="toggleKeyStatus(row)"
              >
                {{ row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" link size="small" @click="deleteKey(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-button type="primary" style="margin-top: 16px" @click="generateNewKey">
          <el-icon><Plus /></el-icon>
          生成新密钥
        </el-button>
      </div>
    </el-dialog>

    <!-- API授权弹窗 -->
    <el-dialog
      v-model="apiAuthDialogVisible"
      :title="`API授权 - ${currentApp?.name || ''}`"
      width="900px"
      :close-on-click-modal="false"
    >
      <AppApiAuth
        v-if="currentApp"
        :app-id="currentApp.id"
        :app-name="currentApp.name"
        @cancel="apiAuthDialogVisible = false"
        @save="handleApiAuthSave"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  MoreFilled,
  Edit,
  Key,
  Connection,
  CircleClose,
  CircleCheck,
  Delete,
  CopyDocument,
  View,
  Hide,
  Search,
  Refresh,
} from '@element-plus/icons-vue'
import AppApiAuth from './components/AppApiAuth.vue'
import { appApi } from '@/api/app'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 筛选表单
const filterForm = reactive({
  name: '',
  code: '',
  appType: '',
  status: '',
})

// 应用列表
const appList = ref<any[]>([])

// 生成随机渐变背景色
const generateIconBg = (name: string) => {
  const hash = name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
  const hue1 = hash % 360
  const hue2 = (hash * 7) % 360
  return `linear-gradient(135deg, hsl(${hue1}, 70%, 60%) 0%, hsl(${hue2}, 70%, 50%) 100%)`
}

// 获取应用列表
const fetchAppList = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: 1, pageSize: 100 }
    if (filterForm.name) params.name = filterForm.name
    if (filterForm.code) params.code = filterForm.code
    if (filterForm.appType) params.appType = filterForm.appType
    if (filterForm.status) params.status = filterForm.status
    
    const res = await appApi.list(params)
    if (res.code === 200 && res.data) {
      appList.value = (res.data.records || []).map((app: any) => ({
        ...app,
        appId: app.appKey,
        apiCount: app.subscribedApiCount || 0,
        todayCalls: app.quotaUsed || 0,
        keyCount: 1,
        iconBg: generateIconBg(app.name),
      }))
    }
  } catch (error) {
    console.error('获取应用列表失败:', error)
    ElMessage.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAppList()
})

// 搜索
const handleSearch = () => {
  fetchAppList()
}

// 重置筛选
const handleReset = () => {
  filterForm.name = ''
  filterForm.code = ''
  filterForm.appType = ''
  filterForm.status = ''
  fetchAppList()
}

// 弹窗状态
const dialogVisible = ref(false)
const keyDialogVisible = ref(false)
const apiAuthDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const currentApp = ref<any>(null)

// 表单
const formRef = ref<FormInstance>()
const formData = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
  appType: 'external',
  callbackUrl: '',
  ipWhitelist: '',
  contactName: '',
  contactEmail: '',
  contactPhone: '',
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 2, max: 30, message: '名称长度在 2 到 30 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入应用编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/, message: '编码只能包含字母、数字、下划线和横线，且以字母开头', trigger: 'blur' },
  ],
}

// 密钥列表（当前应用的AppSecret）
const keyList = ref<any[]>([])

// 获取状态类型
const getStatusType = (status: string) => {
  return status === 'active' ? 'success' : 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  return status === 'active' ? '运行中' : '已停用'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 遮蔽密钥
const maskSecret = (secret: string) => {
  if (!secret) return ''
  return secret.substring(0, 10) + '****' + secret.substring(secret.length - 4)
}

// 复制文本
const copyText = (text: string) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制到剪贴板')
}

// 创建应用
const handleCreate = () => {
  isEdit.value = false
  formData.id = ''
  formData.name = ''
  formData.code = ''
  formData.description = ''
  formData.appType = 'external'
  formData.callbackUrl = ''
  formData.ipWhitelist = ''
  formData.contactName = ''
  formData.contactEmail = ''
  formData.contactPhone = ''
  dialogVisible.value = true
}

// 更多操作
const handleCommand = async (command: string, app: any) => {
  switch (command) {
    case 'edit':
      isEdit.value = true
      formData.id = app.id
      formData.name = app.name
      formData.code = app.code
      formData.description = app.description
      formData.appType = app.appType
      formData.callbackUrl = app.callbackUrl
      formData.ipWhitelist = app.ipWhitelist
      formData.contactName = app.contactName
      formData.contactEmail = app.contactEmail
      formData.contactPhone = app.contactPhone
      dialogVisible.value = true
      break
    case 'keys':
      currentApp.value = app
      // 构建密钥列表（只有一个AppSecret）
      keyList.value = [{
        id: app.id,
        name: '主密钥',
        secret: '••••••••••••••••••••••••••••••••',
        status: app.status,
        showSecret: false,
        createdAt: app.createdAt,
      }]
      keyDialogVisible.value = true
      break
    case 'apis':
      currentApp.value = app
      apiAuthDialogVisible.value = true
      break
    case 'disable':
      ElMessageBox.confirm('停用后该应用将无法调用API，确定要停用吗？', '停用确认', {
        type: 'warning',
      }).then(async () => {
        try {
          await appApi.disable(app.id)
          app.status = 'disabled'
          ElMessage.success('应用已停用')
        } catch (error) {
          ElMessage.error('操作失败')
        }
      })
      break
    case 'enable':
      try {
        await appApi.enable(app.id)
        app.status = 'active'
        ElMessage.success('应用已启用')
      } catch (error) {
        ElMessage.error('操作失败')
      }
      break
    case 'delete':
      ElMessageBox.confirm('删除后无法恢复，确定要删除该应用吗？', '删除确认', {
        type: 'error',
      }).then(async () => {
        try {
          await appApi.delete(app.id)
          const index = appList.value.findIndex(a => a.id === app.id)
          if (index > -1) {
            appList.value.splice(index, 1)
          }
          ElMessage.success('应用已删除')
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
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
      const res = await appApi.update(formData.id, {
        name: formData.name,
        description: formData.description,
        appType: formData.appType,
        callbackUrl: formData.callbackUrl,
        ipWhitelist: formData.ipWhitelist,
        contactName: formData.contactName,
        contactEmail: formData.contactEmail,
        contactPhone: formData.contactPhone,
      })
      if (res.code === 200) {
        ElMessage.success('应用更新成功')
        fetchAppList()
      }
    } else {
      const res = await appApi.create({
        name: formData.name,
        code: formData.code,
        description: formData.description,
        appType: formData.appType,
        callbackUrl: formData.callbackUrl,
        ipWhitelist: formData.ipWhitelist,
        contactName: formData.contactName,
        contactEmail: formData.contactEmail,
        contactPhone: formData.contactPhone,
      })
      if (res.code === 200) {
        // 显示AppSecret（只显示一次）
        ElMessageBox.alert(
          `<div style="word-break: break-all;">
            <p><strong>AppKey:</strong> ${res.data.appKey}</p>
            <p><strong>AppSecret:</strong> ${res.data.appSecret}</p>
            <p style="color: #e6a23c; margin-top: 12px;">⚠️ AppSecret仅显示一次，请妥善保管！</p>
          </div>`,
          '应用创建成功',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '我已保存',
            type: 'success',
          }
        )
        fetchAppList()
      }
    }
    
    dialogVisible.value = false
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 切换密钥状态
const toggleKeyStatus = (key: any) => {
  key.status = key.status === 'active' ? 'disabled' : 'active'
  ElMessage.success(key.status === 'active' ? '密钥已启用' : '密钥已禁用')
}

// 删除密钥
const deleteKey = (key: any) => {
  ElMessageBox.confirm('删除后无法恢复，确定要删除该密钥吗？', '删除确认', {
    type: 'error',
  }).then(() => {
    const index = keyList.value.findIndex(k => k.id === key.id)
    if (index > -1) {
      keyList.value.splice(index, 1)
    }
    ElMessage.success('密钥已删除')
  })
}

// 重置密钥
const generateNewKey = async () => {
  if (!currentApp.value) return
  
  ElMessageBox.confirm('重置后旧密钥将立即失效，确定要重置吗？', '重置密钥', {
    type: 'warning',
  }).then(async () => {
    try {
      const res = await appApi.resetSecret(currentApp.value.id)
      if (res.code === 200) {
        // 更新密钥列表显示新密钥
        keyList.value = [{
          id: currentApp.value.id,
          name: '主密钥',
          secret: res.data,
          status: 'active',
          showSecret: true,
          createdAt: new Date().toISOString(),
        }]
        ElMessage.success('密钥已重置，请立即复制保存')
      }
    } catch (error) {
      ElMessage.error('重置失败')
    }
  })
}

// API授权保存
const handleApiAuthSave = (authorizedApiIds: string[]) => {
  if (currentApp.value) {
    currentApp.value.apiCount = authorizedApiIds.length
  }
  apiAuthDialogVisible.value = false
}
</script>

<style scoped>
.app-list-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

/* 筛选栏 */
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 应用卡片 */
.app-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.app-card:hover {
  transform: translateY(-4px);
}

.app-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.app-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  flex-shrink: 0;
}

.app-info {
  flex: 1;
  min-width: 0;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.more-btn {
  padding: 4px;
  color: #999;
}

.app-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.app-stats {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.app-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.app-id {
  font-family: 'Monaco', 'Menlo', monospace;
}

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 密钥弹窗 */
.app-id-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.app-id-row .label {
  color: #666;
}

.app-id-row .value {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 500;
}

.secret-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}
</style>
