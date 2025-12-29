<template>
  <div class="api-detail-page" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="api-title-info">
          <h2 class="page-title">{{ apiDetail.name }}</h2>
          <el-tag :type="getStatusTagType(apiDetail.status)" size="small">
            {{ getStatusText(apiDetail.status) }}
          </el-tag>
          <el-tag type="info" size="small">{{ apiDetail.version }}</el-tag>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button type="primary" v-if="apiDetail.status === 'draft'" @click="handlePublish">
          <el-icon><Upload /></el-icon>
          发布
        </el-button>
        <el-button type="warning" v-if="apiDetail.status === 'published'" @click="handleOffline">
          <el-icon><Download /></el-icon>
          下线
        </el-button>
      </div>
    </div>

    <!-- API基本信息 -->
    <el-card class="info-card" shadow="never">
      <div class="api-path-row">
        <el-tag :type="getMethodTagType(apiDetail.method)" class="method-tag">
          {{ apiDetail.method }}
        </el-tag>
        <span class="api-path">{{ apiDetail.fullPath }}</span>
        <el-button type="primary" link @click="copyPath">
          <el-icon><CopyDocument /></el-icon>
          复制
        </el-button>
      </div>
      <p class="api-description">{{ apiDetail.description }}</p>
      <div class="api-meta">
        <span class="meta-item">
          <el-icon><Folder /></el-icon>
          {{ apiDetail.groupName }}
        </span>
        <span class="meta-item">
          <el-icon><User /></el-icon>
          {{ apiDetail.creatorName }}
        </span>
        <span class="meta-item">
          <el-icon><Clock /></el-icon>
          更新于 {{ formatDate(apiDetail.updatedAt) }}
        </span>
      </div>
    </el-card>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 调用统计 -->
      <el-tab-pane label="调用统计" name="stats">
        <div class="stats-section">
          <!-- 统计卡片 -->
          <el-row :gutter="20" class="stats-cards">
            <el-col :xs="24" :sm="12" :md="6" v-for="stat in statsCards" :key="stat.title">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
                  <div class="stat-title">{{ stat.title }}</div>
                  <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'" v-if="stat.trend !== undefined">
                    <el-icon>
                      <CaretTop v-if="stat.trend > 0" />
                      <CaretBottom v-else />
                    </el-icon>
                    {{ Math.abs(stat.trend) }}%
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 调用趋势图 -->
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>调用趋势</span>
                <el-radio-group v-model="chartPeriod" size="small">
                  <el-radio-button label="24h">24小时</el-radio-button>
                  <el-radio-button label="7d">7天</el-radio-button>
                  <el-radio-button label="30d">30天</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon :size="48" color="#ddd"><TrendCharts /></el-icon>
                <p>调用趋势图表</p>
              </div>
            </div>
          </el-card>

          <!-- 响应状态分布 -->
          <el-row :gutter="20">
            <el-col :xs="24" :lg="12">
              <el-card class="distribution-card" shadow="never">
                <template #header>响应状态分布</template>
                <div class="status-list">
                  <div v-for="status in statusDistribution" :key="status.code" class="status-item">
                    <span class="status-code" :class="status.type">{{ status.code }}</span>
                    <el-progress
                      :percentage="status.percent"
                      :stroke-width="10"
                      :color="status.color"
                      :show-text="false"
                      style="flex: 1"
                    />
                    <span class="status-count">{{ status.count }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-card class="distribution-card" shadow="never">
                <template #header>响应时间分布</template>
                <div class="latency-list">
                  <div v-for="item in latencyDistribution" :key="item.range" class="latency-item">
                    <span class="latency-range">{{ item.range }}</span>
                    <el-progress
                      :percentage="item.percent"
                      :stroke-width="10"
                      :color="item.color"
                      :show-text="false"
                      style="flex: 1"
                    />
                    <span class="latency-count">{{ item.percent }}%</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <!-- API文档 -->
      <el-tab-pane label="API文档" name="doc">
        <div class="doc-section">
          <!-- 请求信息 -->
          <el-card class="doc-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>请求信息</span>
              </div>
            </template>

            <h4>请求URL</h4>
            <div class="code-block">
              <code>{{ apiDetail.method }} {{ apiDetail.fullPath }}</code>
            </div>

            <h4>请求头</h4>
            <el-table :data="requestHeaders" style="width: 100%" size="small">
              <el-table-column prop="name" label="参数名" width="200" />
              <el-table-column prop="required" label="必填" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                    {{ row.required ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" />
            </el-table>

            <h4>Query参数</h4>
            <el-table :data="queryParams" style="width: 100%" size="small">
              <el-table-column prop="name" label="参数名" width="150" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column prop="required" label="必填" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                    {{ row.required ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" />
              <el-table-column prop="example" label="示例" width="150" />
            </el-table>

            <template v-if="['POST', 'PUT', 'PATCH'].includes(apiDetail.method)">
              <h4>请求Body</h4>
              <div class="code-block json">
                <pre>{{ requestBodyExample }}</pre>
              </div>
            </template>
          </el-card>

          <!-- 响应信息 -->
          <el-card class="doc-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>响应信息</span>
              </div>
            </template>

            <h4>成功响应 (200)</h4>
            <div class="code-block json">
              <pre>{{ successResponseExample }}</pre>
            </div>

            <h4>错误响应</h4>
            <el-table :data="errorResponses" style="width: 100%" size="small">
              <el-table-column prop="code" label="错误码" width="100" />
              <el-table-column prop="message" label="错误信息" width="200" />
              <el-table-column prop="description" label="说明" />
            </el-table>
          </el-card>

          <!-- 代码示例 -->
          <el-card class="doc-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>代码示例</span>
              </div>
            </template>

            <el-tabs v-model="codeTab">
              <el-tab-pane label="cURL" name="curl">
                <div class="code-block">
                  <pre>{{ curlExample }}</pre>
                </div>
              </el-tab-pane>
              <el-tab-pane label="JavaScript" name="js">
                <div class="code-block">
                  <pre>{{ jsExample }}</pre>
                </div>
              </el-tab-pane>
              <el-tab-pane label="Python" name="python">
                <div class="code-block">
                  <pre>{{ pythonExample }}</pre>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 在线调试 -->
      <el-tab-pane label="在线调试" name="debug">
        <ApiDebugger :api-info="debugApiInfo" />
      </el-tab-pane>

      <!-- 调用日志 -->
      <el-tab-pane label="调用日志" name="logs">
        <el-card shadow="never">
          <el-table :data="callLogs" style="width: 100%">
            <el-table-column prop="requestId" label="请求ID" width="200">
              <template #default="{ row }">
                <span class="mono-text">{{ row.requestId }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="appName" label="调用应用" width="150" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 200 ? 'success' : 'danger'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="latency" label="耗时" width="100" />
            <el-table-column prop="clientIp" label="客户端IP" width="140" />
            <el-table-column prop="timestamp" label="时间" width="180" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewLogDetail(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 参数管理 -->
      <el-tab-pane label="参数管理" name="params">
        <el-card shadow="never" class="params-card">
          <template #header>
            <div class="card-header">
              <span>请求参数</span>
              <el-button type="primary" size="small" @click="saveRequestParams" :loading="savingParams">
                保存
              </el-button>
            </div>
          </template>
          <el-table :data="requestParams" style="width: 100%" size="small">
            <el-table-column label="参数名" width="150">
              <template #default="{ row }">
                <el-input v-model="row.name" placeholder="参数名" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-select v-model="row.type" size="small">
                  <el-option label="string" value="string" />
                  <el-option label="integer" value="integer" />
                  <el-option label="number" value="number" />
                  <el-option label="boolean" value="boolean" />
                  <el-option label="array" value="array" />
                  <el-option label="object" value="object" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="位置" width="100">
              <template #default="{ row }">
                <el-select v-model="row.location" size="small">
                  <el-option label="query" value="query" />
                  <el-option label="header" value="header" />
                  <el-option label="path" value="path" />
                  <el-option label="body" value="body" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="必填" width="70" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.required" />
              </template>
            </el-table-column>
            <el-table-column label="描述" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.description" placeholder="描述" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="示例" width="120">
              <template #default="{ row }">
                <el-input v-model="row.example" placeholder="示例" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button type="danger" link size="small" @click="removeRequestParam($index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button class="add-btn" text type="primary" @click="addRequestParam">
            <el-icon><Plus /></el-icon>
            添加参数
          </el-button>
        </el-card>

        <el-card shadow="never" class="params-card">
          <template #header>
            <div class="card-header">
              <span>响应参数</span>
              <el-button type="primary" size="small" @click="saveResponseParams" :loading="savingParams">
                保存
              </el-button>
            </div>
          </template>
          <el-table :data="responseParams" style="width: 100%" size="small">
            <el-table-column label="参数名" width="150">
              <template #default="{ row }">
                <el-input v-model="row.name" placeholder="参数名" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-select v-model="row.type" size="small">
                  <el-option label="string" value="string" />
                  <el-option label="integer" value="integer" />
                  <el-option label="number" value="number" />
                  <el-option label="boolean" value="boolean" />
                  <el-option label="array" value="array" />
                  <el-option label="object" value="object" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="描述" min-width="200">
              <template #default="{ row }">
                <el-input v-model="row.description" placeholder="描述" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="示例" width="150">
              <template #default="{ row }">
                <el-input v-model="row.example" placeholder="示例" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button type="danger" link size="small" @click="removeResponseParam($index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button class="add-btn" text type="primary" @click="addResponseParam">
            <el-icon><Plus /></el-icon>
            添加参数
          </el-button>
        </el-card>
      </el-tab-pane>

      <!-- 后端配置 -->
      <el-tab-pane label="后端配置" name="backend">
        <el-card shadow="never">
          <el-form :model="backendConfig" label-width="120px">
            <el-form-item label="后端类型">
              <el-select v-model="backendConfig.type" style="width: 200px">
                <el-option label="HTTP" value="http" />
                <el-option label="Mock" value="mock" />
                <el-option label="Function" value="function" />
              </el-select>
            </el-form-item>
            <el-form-item label="协议">
              <el-select v-model="backendConfig.protocol" style="width: 200px">
                <el-option label="HTTP" value="HTTP" />
                <el-option label="HTTPS" value="HTTPS" />
              </el-select>
            </el-form-item>
            <el-form-item label="请求方法">
              <el-select v-model="backendConfig.method" style="width: 200px">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
            <el-form-item label="后端地址">
              <el-input v-model="backendConfig.host" placeholder="如：api.example.com:8080" />
            </el-form-item>
            <el-form-item label="后端路径">
              <el-input v-model="backendConfig.path" placeholder="如：/internal/api/xxx" />
            </el-form-item>
            <el-form-item label="超时时间">
              <el-input-number v-model="backendConfig.timeout" :min="100" :max="60000" :step="100" />
              <span class="form-unit">毫秒</span>
            </el-form-item>
            <el-form-item label="连接超时">
              <el-input-number v-model="backendConfig.connectTimeout" :min="100" :max="30000" :step="100" />
              <span class="form-unit">毫秒</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBackendConfig" :loading="savingBackend">
                保存配置
              </el-button>
              <el-button @click="testBackendConnection" :loading="testingConnection">
                测试连接
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 版本历史 -->
      <el-tab-pane label="版本历史" name="versions">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>版本历史</span>
              <el-button type="primary" size="small" @click="showCreateVersionDialog">
                <el-icon><Plus /></el-icon>
                创建版本
              </el-button>
            </div>
          </template>
          <el-timeline v-if="versionHistory.length > 0">
            <el-timeline-item
              v-for="version in versionHistory"
              :key="version.id"
              :timestamp="formatDate(version.createdAt)"
              type="primary"
            >
              <div class="version-item">
                <div class="version-header">
                  <span class="version-tag">{{ version.version }}</span>
                  <el-button type="primary" link size="small" @click="rollbackVersion(version)">
                    回滚
                  </el-button>
                  <el-button type="danger" link size="small" @click="deleteVersion(version)">
                    删除
                  </el-button>
                </div>
                <p class="version-desc">{{ version.changeLog || '无变更说明' }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无版本历史" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建版本弹窗 -->
    <el-dialog v-model="createVersionDialogVisible" title="创建版本" width="500px">
      <el-form :model="newVersionForm" label-width="100px">
        <el-form-item label="版本号" required>
          <el-input v-model="newVersionForm.version" placeholder="如：v1.0.1" />
        </el-form-item>
        <el-form-item label="变更说明">
          <el-input v-model="newVersionForm.changeLog" type="textarea" :rows="3" placeholder="请输入变更说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVersionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createVersion" :loading="creatingVersion">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Upload,
  Download,
  CopyDocument,
  Folder,
  User,
  Clock,
  CaretTop,
  CaretBottom,
  TrendCharts,
  Delete,
  Plus,
} from '@element-plus/icons-vue'
import {
  apiManageApi,
  apiParamApi,
  apiVersionApi,
  apiBackendApi,
  type ApiInfoResponse,
  type ApiParamResponse,
  type ApiVersionResponse,
  type ApiBackendResponse,
} from '@/api/apiManage'
import {
  getApiStatsDetail,
  getCallLogs,
  type ApiStatsDetail,
  type StatusDistribution,
  type LatencyDistribution,
  type CallLog,
} from '@/api/stats'
import ApiDebugger from './components/ApiDebugger.vue'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)
const savingParams = ref(false)
const savingBackend = ref(false)
const testingConnection = ref(false)
const creatingVersion = ref(false)

// 标签页 - 支持从URL query参数初始化
const activeTab = ref(route.query.tab as string || 'stats')
const chartPeriod = ref('7d')
const codeTab = ref('curl')

// API详情数据
const apiDetail = reactive<ApiInfoResponse>({
  id: '',
  tenantId: '',
  groupId: '',
  groupName: '',
  name: '',
  code: '',
  version: '',
  description: '',
  method: '',
  path: '',
  protocol: '',
  contentType: '',
  status: '',
  authType: '',
  timeout: 0,
  retryCount: 0,
  cacheEnabled: false,
  cacheTtl: 0,
  rateLimitEnabled: false,
  rateLimitQps: 0,
  mockEnabled: false,
  mockResponse: '',
  todayCalls: 0,
  totalCalls: 0,
  createdBy: '',
  creatorName: '',
  publishedAt: '',
  createdAt: '',
  updatedAt: '',
})

// 请求参数
const requestParams = ref<ApiParamResponse[]>([])

// 响应参数
const responseParams = ref<ApiParamResponse[]>([])

// 后端配置
const backendConfig = reactive({
  type: 'http',
  protocol: 'HTTP',
  method: 'GET',
  host: '',
  path: '',
  timeout: 30000,
  connectTimeout: 5000,
})

// 版本历史
const versionHistory = ref<ApiVersionResponse[]>([])

// 创建版本弹窗
const createVersionDialogVisible = ref(false)
const newVersionForm = reactive({
  version: '',
  changeLog: '',
})

// 调试器API信息
const debugApiInfo = computed(() => ({
  method: apiDetail.method,
  path: apiDetail.path,
  queryParams: requestParams.value.filter(p => p.location === 'query'),
  headerParams: requestParams.value.filter(p => p.location === 'header'),
}))

// 获取API详情
const fetchApiDetail = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  loading.value = true
  try {
    const res = await apiManageApi.getById(apiId)
    if (res.code === 200 && res.data) {
      Object.assign(apiDetail, res.data)
    }
  } catch (error) {
    console.error('获取API详情失败:', error)
    ElMessage.error('获取API详情失败')
  } finally {
    loading.value = false
  }
}

// 获取请求参数
const fetchRequestParams = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  try {
    const res = await apiParamApi.listRequestParams(apiId)
    if (res.code === 200) {
      requestParams.value = res.data || []
    }
  } catch (error) {
    console.error('获取请求参数失败:', error)
  }
}

// 获取响应参数
const fetchResponseParams = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  try {
    const res = await apiParamApi.listResponseParams(apiId)
    if (res.code === 200) {
      responseParams.value = res.data || []
    }
  } catch (error) {
    console.error('获取响应参数失败:', error)
  }
}

// 获取后端配置
const fetchBackendConfig = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  try {
    const res = await apiBackendApi.getByApiId(apiId)
    if (res.code === 200 && res.data) {
      Object.assign(backendConfig, res.data)
    }
  } catch (error) {
    console.error('获取后端配置失败:', error)
  }
}

// 获取版本历史
const fetchVersionHistory = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  try {
    const res = await apiVersionApi.list(apiId)
    if (res.code === 200) {
      versionHistory.value = res.data || []
    }
  } catch (error) {
    console.error('获取版本历史失败:', error)
  }
}

// 添加请求参数
const addRequestParam = () => {
  requestParams.value.push({
    id: '',
    apiId: apiDetail.id,
    name: '',
    type: 'string',
    location: 'query',
    required: false,
    defaultValue: '',
    example: '',
    description: '',
    sort: requestParams.value.length,
  })
}

// 删除请求参数
const removeRequestParam = (index: number) => {
  requestParams.value.splice(index, 1)
}

// 保存请求参数
const saveRequestParams = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  savingParams.value = true
  try {
    await apiParamApi.saveRequestParams(apiId, requestParams.value)
    ElMessage.success('请求参数保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingParams.value = false
  }
}

// 添加响应参数
const addResponseParam = () => {
  responseParams.value.push({
    id: '',
    apiId: apiDetail.id,
    name: '',
    type: 'string',
    location: '',
    required: false,
    defaultValue: '',
    example: '',
    description: '',
    sort: responseParams.value.length,
  })
}

// 删除响应参数
const removeResponseParam = (index: number) => {
  responseParams.value.splice(index, 1)
}

// 保存响应参数
const saveResponseParams = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  savingParams.value = true
  try {
    await apiParamApi.saveResponseParams(apiId, responseParams.value)
    ElMessage.success('响应参数保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingParams.value = false
  }
}

// 保存后端配置
const saveBackendConfig = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  savingBackend.value = true
  try {
    await apiBackendApi.save(apiId, backendConfig)
    ElMessage.success('后端配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingBackend.value = false
  }
}

// 测试后端连接
const testBackendConnection = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  testingConnection.value = true
  try {
    const res = await apiBackendApi.testConnection(apiId, backendConfig)
    if (res.code === 200 && res.data) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.warning('连接失败')
    }
  } catch (error) {
    ElMessage.error('测试连接失败')
  } finally {
    testingConnection.value = false
  }
}

// 显示创建版本弹窗
const showCreateVersionDialog = () => {
  newVersionForm.version = ''
  newVersionForm.changeLog = ''
  createVersionDialogVisible.value = true
}

// 创建版本
const createVersion = async () => {
  const apiId = route.params.id as string
  if (!apiId || !newVersionForm.version) {
    ElMessage.warning('请输入版本号')
    return
  }

  creatingVersion.value = true
  try {
    await apiVersionApi.create(apiId, newVersionForm)
    ElMessage.success('版本创建成功')
    createVersionDialogVisible.value = false
    fetchVersionHistory()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    creatingVersion.value = false
  }
}

// 回滚版本
const rollbackVersion = (version: ApiVersionResponse) => {
  const apiId = route.params.id as string
  ElMessageBox.confirm(`确定要回滚到版本 ${version.version} 吗？`, '回滚确认', {
    type: 'warning',
  }).then(async () => {
    try {
      await apiVersionApi.rollback(apiId, version.id)
      ElMessage.success('版本回滚成功')
      fetchApiDetail()
    } catch (error) {
      ElMessage.error('回滚失败')
    }
  })
}

// 删除版本
const deleteVersion = (version: ApiVersionResponse) => {
  const apiId = route.params.id as string
  ElMessageBox.confirm(`确定要删除版本 ${version.version} 吗？`, '删除确认', {
    type: 'error',
  }).then(async () => {
    try {
      await apiVersionApi.delete(apiId, version.id)
      ElMessage.success('版本删除成功')
      fetchVersionHistory()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 监听Tab切换，按需加载数据
watch(activeTab, (newTab) => {
  if (newTab === 'params') {
    fetchRequestParams()
    fetchResponseParams()
  } else if (newTab === 'backend') {
    fetchBackendConfig()
  } else if (newTab === 'versions') {
    fetchVersionHistory()
  } else if (newTab === 'logs') {
    fetchCallLogs()
  } else if (newTab === 'stats') {
    fetchApiStats()
  } else if (newTab === 'debug') {
    // 切换到调试Tab时，确保请求参数已加载
    if (requestParams.value.length === 0) {
      fetchRequestParams()
    }
  }
})

// 统计卡片数据
const statsCards = ref([
  { title: '今日调用', value: '0', color: '#1890ff', trend: 0 },
  { title: '总调用量', value: '0', color: '#52c41a', trend: 0 },
  { title: '平均响应', value: '0ms', color: '#722ed1', trend: 0 },
  { title: '成功率', value: '100%', color: '#13c2c2', trend: 0 },
])

// 状态码分布数据
const statusDistribution = ref<StatusDistribution[]>([
  { code: '2xx', count: 0, percent: 0, color: '#52c41a', type: 'success' },
  { code: '4xx', count: 0, percent: 0, color: '#faad14', type: 'warning' },
  { code: '5xx', count: 0, percent: 0, color: '#ff4d4f', type: 'danger' },
])

// 响应时间分布数据
const latencyDistribution = ref<LatencyDistribution[]>([
  { range: '< 50ms', percent: 0, color: '#52c41a' },
  { range: '50-100ms', percent: 0, color: '#1890ff' },
  { range: '100-500ms', percent: 0, color: '#faad14' },
  { range: '> 500ms', percent: 0, color: '#ff4d4f' },
])

// 获取API统计详情
const fetchApiStats = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  try {
    const res = await getApiStatsDetail(apiId) as any
    if (res.code === 200 && res.data) {
      const data = res.data as ApiStatsDetail
      
      // 更新统计卡片
      statsCards.value = [
        { 
          title: '今日调用', 
          value: formatNumber(data.todayCalls), 
          color: '#1890ff', 
          trend: data.todayTrend 
        },
        { 
          title: '总调用量', 
          value: formatNumber(data.totalCalls), 
          color: '#52c41a', 
          trend: data.totalTrend || 0 
        },
        { 
          title: '平均响应', 
          value: `${data.avgLatency}ms`, 
          color: '#722ed1', 
          trend: data.latencyTrend 
        },
        { 
          title: '成功率', 
          value: `${data.successRate.toFixed(1)}%`, 
          color: '#13c2c2', 
          trend: data.successRateTrend 
        },
      ]
      
      // 更新状态码分布
      if (data.statusDistribution && data.statusDistribution.length > 0) {
        statusDistribution.value = data.statusDistribution
      }
      
      // 更新响应时间分布
      if (data.latencyDistribution && data.latencyDistribution.length > 0) {
        latencyDistribution.value = data.latencyDistribution
      }
    }
  } catch (error) {
    console.error('获取API统计详情失败:', error)
  }
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// 初始化
onMounted(() => {
  fetchApiDetail()
  fetchApiStats()
})

// 请求头
const requestHeaders = [
  { name: 'Authorization', required: true, description: 'Bearer Token 认证' },
  { name: 'Content-Type', required: true, description: 'application/json' },
  { name: 'X-Request-Id', required: false, description: '请求追踪ID' },
]

// Query参数
const queryParams = [
  { name: 'redirect', type: 'string', required: false, description: '登录成功后跳转地址', example: '/dashboard' },
]

// 请求Body示例
const requestBodyExample = `{
  "username": "user@example.com",
  "password": "your_password",
  "loginType": "password",
  "captcha": "1234"
}`

// 成功响应示例
const successResponseExample = `{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "user": {
      "id": "user_123",
      "username": "user@example.com",
      "nickname": "用户昵称"
    }
  },
  "requestId": "req_abc123"
}`

// 错误响应
const errorResponses = [
  { code: '400', message: 'Bad Request', description: '请求参数错误' },
  { code: '401', message: 'Unauthorized', description: '用户名或密码错误' },
  { code: '403', message: 'Forbidden', description: '账号已被禁用' },
  { code: '429', message: 'Too Many Requests', description: '登录尝试次数过多' },
]

// 代码示例
const curlExample = `curl -X POST "https://api.intellihub.com/api/v1/auth/login" \\
  -H "Content-Type: application/json" \\
  -d '{
    "username": "user@example.com",
    "password": "your_password",
    "loginType": "password"
  }'`

const jsExample = `const response = await fetch('https://api.intellihub.com/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'user@example.com',
    password: 'your_password',
    loginType: 'password'
  })
});

const data = await response.json();
console.log(data);`

const pythonExample = `import requests

response = requests.post(
    'https://api.intellihub.com/api/v1/auth/login',
    json={
        'username': 'user@example.com',
        'password': 'your_password',
        'loginType': 'password'
    }
)

print(response.json())`

// 调用日志
const callLogs = ref<any[]>([])

// 获取调用日志
const fetchCallLogs = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  try {
    const res = await getCallLogs({ apiId, page: 1, size: 20 }) as any
    if (res.code === 200 && res.data && res.data.records) {
      callLogs.value = res.data.records.map((log: CallLog) => ({
        requestId: log.id?.toString() || '',
        appName: log.appKey || '未知应用',
        status: log.statusCode,
        latency: `${log.latency}ms`,
        clientIp: log.clientIp || '',
        timestamp: log.requestTime || '',
      }))
    }
  } catch (error) {
    console.error('获取调用日志失败:', error)
  }
}


// 获取方法标签类型
const getMethodTagType = (method: string) => {
  const map: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
  }
  return map[method] || 'info'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    draft: 'info',
    published: 'success',
    offline: 'warning',
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    offline: '已下线',
  }
  return map[status] || status
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 返回
const goBack = () => {
  router.push('/console/api/list')
}

// 复制路径
const copyPath = () => {
  navigator.clipboard.writeText(apiDetail.fullPath)
  ElMessage.success('已复制到剪贴板')
}

// 编辑
const handleEdit = () => {
  router.push(`/console/api/${apiDetail.id}/edit`)
}

// 发布
const handlePublish = async () => {
  ElMessageBox.confirm('确定要发布该API吗？', '发布确认').then(async () => {
    try {
      await apiManageApi.publish(apiDetail.id)
      apiDetail.status = 'published'
      ElMessage.success('API已发布')
    } catch (error) {
      ElMessage.error('发布失败')
    }
  })
}

// 下线
const handleOffline = async () => {
  ElMessageBox.confirm('确定要下线该API吗？', '下线确认', { type: 'warning' }).then(async () => {
    try {
      await apiManageApi.offline(apiDetail.id)
      apiDetail.status = 'offline'
      ElMessage.success('API已下线')
    } catch (error) {
      ElMessage.error('下线失败')
    }
  })
}

// 查看日志详情
const viewLogDetail = (row: any) => {
  ElMessage.info('日志详情功能开发中...')
}
</script>

<style scoped>
.api-detail-page {
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
  align-items: center;
  gap: 12px;
}

.api-title-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 8px;
}

/* 基本信息卡片 */
.info-card {
  margin-bottom: 16px;
}

.api-path-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.method-tag {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 600;
}

.api-path {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  color: #1a1a1a;
}

.api-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 12px;
}

.api-meta {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #999;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 统计卡片 */
.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  margin-bottom: 16px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-title {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.stat-trend.up {
  color: #52c41a;
}

.stat-trend.down {
  color: #ff4d4f;
}

/* 图表卡片 */
.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #999;
}

/* 分布卡片 */
.distribution-card {
  margin-bottom: 20px;
}

.status-list,
.latency-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item,
.latency-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-code {
  width: 40px;
  font-weight: 600;
  font-size: 13px;
}

.status-code.success { color: #52c41a; }
.status-code.warning { color: #faad14; }
.status-code.danger { color: #ff4d4f; }

.status-count,
.latency-count {
  width: 80px;
  text-align: right;
  font-size: 13px;
  color: #666;
}

.latency-range {
  width: 80px;
  font-size: 13px;
  color: #666;
}

/* 文档卡片 */
.doc-card {
  margin-bottom: 16px;
}

.doc-card h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 20px 0 12px;
}

.doc-card h4:first-child {
  margin-top: 0;
}

.code-block {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
}

.code-block code,
.code-block pre {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #d4d4d4;
  margin: 0;
  white-space: pre-wrap;
}

/* 日志 */
.mono-text {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

/* 版本历史 */
.version-item {
  padding: 8px 0;
}

.version-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.version-tag {
  font-weight: 600;
  color: #1890ff;
}

.version-author {
  font-size: 12px;
  color: #999;
}

.version-desc {
  font-size: 13px;
  color: #666;
  margin: 0;
}

/* 参数管理 */
.params-card {
  margin-bottom: 16px;
}

.add-btn {
  margin-top: 12px;
}

.form-unit {
  margin-left: 8px;
  color: #666;
}
</style>
