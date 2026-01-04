<template>
  <div class="api-create-page" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2 class="page-title">{{ isEdit ? '编辑API' : '创建API' }}</h2>
      </div>
      <div class="header-right">
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </div>
    </div>

    <!-- 表单内容 -->
    <div class="form-container">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="right"
      >
        <!-- 基本信息 -->
        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
          </template>
          
          <el-form-item label="API名称" prop="name">
            <el-input
              v-model="formData.name"
              placeholder="请输入API名称，如：用户登录认证"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="API编码" prop="code">
            <el-input
              v-model="formData.code"
              placeholder="请输入API编码，如：user-login"
              maxlength="50"
            />
            <div class="form-tip">编码只能包含小写字母、数字和连字符，且以字母开头</div>
          </el-form-item>

          <el-form-item label="API分组" prop="groupId">
            <el-select v-model="formData.groupId" placeholder="请选择API分组" style="width: 100%">
              <el-option
                v-for="group in apiGroups"
                :key="group.id"
                :label="group.name"
                :value="group.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="版本号" prop="version">
            <el-input v-model="formData.version" placeholder="如：v1.0" style="width: 200px" />
          </el-form-item>

          <el-form-item label="API描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入API功能描述"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="标签">
            <el-select
              v-model="formData.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="添加标签"
              style="width: 100%"
            >
              <el-option label="核心接口" value="核心接口" />
              <el-option label="认证相关" value="认证相关" />
              <el-option label="数据查询" value="数据查询" />
              <el-option label="数据操作" value="数据操作" />
            </el-select>
          </el-form-item>
        </el-card>

        <!-- 后端服务类型 -->
        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Connection /></el-icon>
              <span>后端服务类型</span>
            </div>
          </template>

          <el-form-item label="服务类型" prop="backendType">
            <el-radio-group v-model="formData.backendType" @change="onBackendTypeChange">
              <el-radio-button value="http">
                <el-icon style="margin-right: 4px"><Connection /></el-icon>
                HTTP/HTTPS
              </el-radio-button>
              <el-radio-button value="dubbo">
                <el-icon style="margin-right: 4px"><Setting /></el-icon>
                Dubbo
              </el-radio-button>
              <el-radio-button value="internal">
                <el-icon style="margin-right: 4px"><Link /></el-icon>
                内部API
              </el-radio-button>
              <el-radio-button value="mock">
                <el-icon style="margin-right: 4px"><Document /></el-icon>
                Mock服务
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <!-- HTTP配置 -->
          <template v-if="formData.backendType === 'http'">
            <el-form-item label="后端地址" prop="httpBackend.host">
              <el-input v-model="formData.httpBackend.host" placeholder="如：http://user-service:8080">
                <template #prepend>
                  <el-select v-model="formData.httpBackend.protocol" style="width: 100px">
                    <el-option label="HTTP" value="http" />
                    <el-option label="HTTPS" value="https" />
                  </el-select>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="后端路径">
              <el-input v-model="formData.httpBackend.path" placeholder="后端服务实际路径，如：/internal/users/login" />
            </el-form-item>
            <el-form-item label="请求方式">
              <el-radio-group v-model="formData.httpBackend.method" @change="backendMethodManualChanged = true">
                <el-radio-button value="GET">GET</el-radio-button>
                <el-radio-button value="POST">POST</el-radio-button>
                <el-radio-button value="PUT">PUT</el-radio-button>
                <el-radio-button value="DELETE">DELETE</el-radio-button>
              </el-radio-group>
              <div class="form-tip" v-if="!backendMethodManualChanged">已自动同步请求配置的请求方式</div>
            </el-form-item>
          </template>

          <!-- Dubbo配置 -->
          <template v-if="formData.backendType === 'dubbo'">
            <el-alert type="info" :closable="false" style="margin-bottom: 16px">
              <template #title>配置Dubbo服务，网关将通过泛化调用转发请求</template>
            </el-alert>
            <el-form-item label="注册中心">
              <el-select v-model="formData.dubboBackend.registry" placeholder="选择注册中心" style="width: 100%">
                <el-option label="Nacos (nacos://127.0.0.1:8848)" value="nacos://127.0.0.1:8848" />
                <el-option label="Zookeeper (zookeeper://127.0.0.1:2181)" value="zookeeper://127.0.0.1:2181" />
              </el-select>
            </el-form-item>
            <el-form-item label="服务接口">
              <el-input v-model="formData.dubboBackend.interfaceName" placeholder="如：com.example.UserService" />
            </el-form-item>
            <el-form-item label="方法名">
              <el-input v-model="formData.dubboBackend.methodName" placeholder="如：getUserById" />
            </el-form-item>
            <el-form-item label="版本号">
              <el-input v-model="formData.dubboBackend.version" placeholder="如：1.0.0" style="width: 200px" />
            </el-form-item>
            <el-form-item label="分组">
              <el-input v-model="formData.dubboBackend.group" placeholder="可选，如：default" style="width: 200px" />
            </el-form-item>
          </template>

          <!-- 内部API配置 -->
          <template v-if="formData.backendType === 'internal'">
            <el-alert type="info" :closable="false" style="margin-bottom: 16px">
              <template #title>关联平台内部已有的API接口，实现API编排</template>
            </el-alert>
            <el-form-item label="关联API">
              <el-select 
                v-model="formData.internalBackend.apiId" 
                filterable 
                remote
                placeholder="搜索并选择内部API"
                style="width: 100%"
              >
                <el-option 
                  v-for="api in internalApiList" 
                  :key="api.id" 
                  :label="`${api.name} (${api.method} ${api.path})`" 
                  :value="api.id" 
                />
              </el-select>
            </el-form-item>
          </template>

          <!-- Mock配置 -->
          <template v-if="formData.backendType === 'mock'">
            <el-alert type="info" :closable="false" style="margin-bottom: 16px">
              <template #title>返回Mock数据，适用于开发调试阶段</template>
            </el-alert>
            <el-form-item label="Mock响应">
              <el-input
                v-model="formData.mockBackend.response"
                type="textarea"
                :rows="10"
                placeholder='{"code": 0, "message": "success", "data": {...}}'
                class="code-textarea"
              />
            </el-form-item>
            <el-form-item label="响应延迟">
              <el-input-number v-model="formData.mockBackend.delay" :min="0" :max="10000" :step="100" />
              <span class="form-unit">毫秒</span>
            </el-form-item>
          </template>
        </el-card>

        <!-- 请求配置 -->
        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Connection /></el-icon>
              <span>请求配置</span>
            </div>
          </template>

          <el-form-item label="请求方式" prop="method">
            <el-radio-group v-model="formData.method" @change="onMethodChange">
              <el-radio-button value="GET">GET</el-radio-button>
              <el-radio-button value="POST">POST</el-radio-button>
              <el-radio-button value="PUT">PUT</el-radio-button>
              <el-radio-button value="DELETE">DELETE</el-radio-button>
              <el-radio-button value="PATCH">PATCH</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="请求路径" prop="path">
            <el-input v-model="formData.path" placeholder="/api/v1/xxx">
              <template #prepend>{{ baseUrl }}</template>
            </el-input>
            <div class="form-tip">支持路径参数，如：/users/{userId}</div>
          </el-form-item>

          <el-form-item label="Content-Type">
            <el-select v-model="formData.contentType" style="width: 300px">
              <el-option label="application/json" value="application/json" />
              <el-option label="application/x-www-form-urlencoded" value="application/x-www-form-urlencoded" />
              <el-option label="multipart/form-data" value="multipart/form-data" />
            </el-select>
          </el-form-item>
        </el-card>

        <!-- 请求参数 -->
        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>请求参数</span>
            </div>
          </template>

          <el-tabs v-model="activeParamTab">
            <el-tab-pane label="Query参数" name="query">
              <ParamTable v-model="formData.queryParams" param-type="query" />
            </el-tab-pane>
            <el-tab-pane label="Path参数" name="path">
              <ParamTable v-model="formData.pathParams" param-type="path" />
            </el-tab-pane>
            <el-tab-pane label="Header参数" name="header">
              <ParamTable v-model="formData.headerParams" param-type="header" />
            </el-tab-pane>
            <el-tab-pane label="Body参数" name="body" v-if="['POST', 'PUT', 'PATCH'].includes(formData.method)">
              <div class="body-editor">
                <el-radio-group v-model="formData.bodyType" style="margin-bottom: 12px">
                  <el-radio value="json">JSON Schema</el-radio>
                  <el-radio value="form">表单参数</el-radio>
                </el-radio-group>
                <template v-if="formData.bodyType === 'json'">
                  <el-input
                    v-model="formData.bodySchema"
                    type="textarea"
                    :rows="10"
                    placeholder='{"type": "object", "properties": {...}}'
                    class="code-textarea"
                  />
                </template>
                <template v-else>
                  <ParamTable v-model="formData.bodyParams" param-type="body" />
                </template>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <!-- 响应配置 -->
        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>响应配置</span>
            </div>
          </template>

          <el-form-item label="成功响应示例">
            <el-input
              v-model="formData.successResponse"
              type="textarea"
              :rows="8"
              placeholder='{"code": 0, "message": "success", "data": {...}}'
              class="code-textarea"
            />
          </el-form-item>

          <el-form-item label="错误响应示例">
            <el-input
              v-model="formData.errorResponse"
              type="textarea"
              :rows="5"
              placeholder='{"code": 400, "message": "参数错误", "data": null}'
              class="code-textarea"
            />
          </el-form-item>
        </el-card>

        <!-- 高级配置（可选） -->
        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><SetUp /></el-icon>
              <span>高级配置（可选）</span>
            </div>
          </template>

          <el-form-item label="超时时间">
            <el-input-number v-model="formData.timeout" :min="100" :max="60000" :step="100" />
            <span class="form-unit">毫秒</span>
          </el-form-item>

          <el-form-item label="重试次数">
            <el-input-number v-model="formData.retryCount" :min="0" :max="5" />
          </el-form-item>
        </el-card>

        <!-- 安全配置 -->
        <el-card class="form-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Lock /></el-icon>
              <span>安全配置</span>
            </div>
          </template>

          <el-form-item label="认证方式">
            <el-radio-group v-model="formData.authMethod">
              <el-radio value="jwt">JWT Token</el-radio>
              <el-radio value="appkey">AppKey/Secret</el-radio>
              <el-radio value="none">无需认证</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="限流策略">
            <el-switch v-model="formData.rateLimitEnabled" />
            <template v-if="formData.rateLimitEnabled">
              <div class="rate-limit-config">
                <el-input-number v-model="formData.rateLimit" :min="1" :max="100000" />
                <span class="form-unit">次/</span>
                <el-select v-model="formData.rateLimitUnit" style="width: 100px">
                  <el-option label="秒" value="second" />
                  <el-option label="分钟" value="minute" />
                  <el-option label="小时" value="hour" />
                </el-select>
              </div>
            </template>
          </el-form-item>

          <el-form-item label="IP白名单">
            <el-switch v-model="formData.ipWhitelistEnabled" />
            <template v-if="formData.ipWhitelistEnabled">
              <el-input
                v-model="formData.ipWhitelist"
                type="textarea"
                :rows="3"
                placeholder="每行一个IP或IP段，如：192.168.1.0/24"
                style="margin-top: 8px"
              />
            </template>
          </el-form-item>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft,
  InfoFilled,
  Connection,
  List,
  Document,
  SetUp,
  Lock,
  Setting,
  Link,
} from '@element-plus/icons-vue'
import ParamTable from './components/ParamTable.vue'
import { apiManageApi, apiGroupApi, type ApiGroupResponse, type CreateApiRequest, type UpdateApiRequest } from '@/api/apiManage'

const router = useRouter()
const route = useRoute()

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)
const baseUrl = 'https://api.intellihub.com'
const loading = ref(false)
const submitting = ref(false)

// 表单引用
const formRef = ref<FormInstance>()

// 参数标签页
const activeParamTab = ref('query')

// API分组
const apiGroups = ref<ApiGroupResponse[]>([])

// 内部API列表（用于内部API类型选择）
const internalApiList = ref<any[]>([])

// 后端类型切换
const onBackendTypeChange = (type: string) => {
  console.log('后端类型切换为:', type)
  if (type === 'internal') {
    fetchInternalApiList()
  }
  // 切换后端类型时，重置手动修改标记
  backendMethodManualChanged.value = false
  // 同步请求方式
  if (type === 'http') {
    formData.httpBackend.method = formData.method
  }
}

// 后端请求方式是否手动修改过
const backendMethodManualChanged = ref(false)

// 请求方式变化时自动同步到后端配置
const onMethodChange = (method: string) => {
  // 如果后端请求方式没有手动修改过，则自动同步
  if (!backendMethodManualChanged.value && formData.backendType === 'http') {
    formData.httpBackend.method = method
  }
}

// 获取内部API列表
const fetchInternalApiList = async () => {
  try {
    const res = await apiManageApi.list({ page: 1, size: 100, status: 'published' })
    if (res.code === 200) {
      internalApiList.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取内部API列表失败:', error)
  }
}

// 获取API分组列表
const fetchApiGroups = async () => {
  try {
    const res = await apiGroupApi.list()
    if (res.code === 200) {
      apiGroups.value = res.data || []
    }
  } catch (error) {
    console.error('获取API分组失败:', error)
  }
}

// 获取API详情（编辑模式）
const fetchApiDetail = async () => {
  const apiId = route.params.id as string
  if (!apiId) return

  loading.value = true
  try {
    const res = await apiManageApi.getById(apiId)
    if (res.code === 200 && res.data) {
      const data = res.data
      // 基本信息
      formData.name = data.name || ''
      formData.code = data.code || ''
      formData.groupId = data.groupId || ''
      formData.version = data.version || 'v1.0'
      formData.description = data.description || ''
      formData.tags = data.tags || []
      
      // 请求配置
      formData.method = data.method || 'GET'
      formData.path = data.path || ''
      formData.contentType = data.contentType || 'application/json'
      
      // 高级配置
      formData.timeout = data.timeout || 5000
      formData.retryCount = data.retryCount || 0
      
      // 安全配置
      formData.rateLimitEnabled = data.rateLimitEnabled || false
      formData.rateLimit = data.rateLimitQps || 100
      formData.ipWhitelistEnabled = data.ipWhitelistEnabled || false
      formData.ipWhitelist = data.ipWhitelist || ''
      
      // 认证方式转换
      if (data.authType === 'none') {
        formData.authMethod = 'none'
      } else if (data.authType === 'signature') {
        formData.authMethod = 'appkey'
      } else {
        formData.authMethod = 'jwt'
      }
      
      // 响应示例
      formData.successResponse = data.successResponse || formData.successResponse
      formData.errorResponse = data.errorResponse || formData.errorResponse
      
      // 后端配置
      if (data.backend) {
        const backend = data.backend
        // 根据后端类型设置
        if (backend.type === 'mock' || data.mockEnabled) {
          formData.backendType = 'mock'
          formData.mockBackend.response = data.mockResponse || backend.mockResponse || ''
          formData.mockBackend.delay = backend.mockDelay || 0
        } else if (backend.type === 'dubbo') {
          formData.backendType = 'dubbo'
          formData.dubboBackend.registry = backend.registry || ''
          formData.dubboBackend.interfaceName = backend.interfaceName || ''
          formData.dubboBackend.methodName = backend.methodName || ''
          formData.dubboBackend.version = backend.dubboVersion || '1.0.0'
          formData.dubboBackend.group = backend.dubboGroup || ''
        } else if (backend.refApiId) {
          formData.backendType = 'internal'
          formData.internalBackend.apiId = backend.refApiId
          fetchInternalApiList()
        } else {
          formData.backendType = 'http'
          formData.httpBackend.protocol = backend.protocol?.toLowerCase() || 'http'
          formData.httpBackend.host = backend.host || ''
          formData.httpBackend.path = backend.path || ''
          formData.httpBackend.method = backend.method || data.method || 'GET'
        }
      }
      
      // 请求参数分类填充
      if (data.requestParams && data.requestParams.length > 0) {
        formData.queryParams = data.requestParams
          .filter((p: any) => p.location === 'query')
          .map((p: any) => ({
            name: p.name,
            type: p.type || 'string',
            required: p.required || false,
            defaultValue: p.defaultValue || '',
            example: p.example || '',
            description: p.description || '',
          }))
        formData.headerParams = data.requestParams
          .filter((p: any) => p.location === 'header')
          .map((p: any) => ({
            name: p.name,
            type: p.type || 'string',
            required: p.required || false,
            defaultValue: p.defaultValue || '',
            example: p.example || '',
            description: p.description || '',
          }))
        formData.pathParams = data.requestParams
          .filter((p: any) => p.location === 'path')
          .map((p: any) => ({
            name: p.name,
            type: p.type || 'string',
            required: p.required || false,
            defaultValue: p.defaultValue || '',
            example: p.example || '',
            description: p.description || '',
          }))
        formData.bodyParams = data.requestParams
          .filter((p: any) => p.location === 'body')
          .map((p: any) => ({
            name: p.name,
            type: p.type || 'string',
            required: p.required || false,
            defaultValue: p.defaultValue || '',
            example: p.example || '',
            description: p.description || '',
          }))
        
        // 如果有body参数，设置bodyType为form
        if (formData.bodyParams.length > 0) {
          formData.bodyType = 'form'
        }
      }
    }
  } catch (error) {
    console.error('获取API详情失败:', error)
    ElMessage.error('获取API详情失败')
  } finally {
    loading.value = false
  }
}

// 表单数据
const formData = reactive({
  name: '',
  code: '',
  groupId: '',
  version: 'v1.0',
  description: '',
  tags: [] as string[],
  // 后端服务类型
  backendType: 'http' as 'http' | 'dubbo' | 'internal' | 'mock',
  // HTTP后端配置
  httpBackend: {
    protocol: 'http',
    host: '',
    path: '',
    method: 'GET',
  },
  // Dubbo后端配置
  dubboBackend: {
    registry: '',
    interfaceName: '',
    methodName: '',
    version: '1.0.0',
    group: '',
  },
  // 内部API配置
  internalBackend: {
    apiId: '',
  },
  // Mock配置
  mockBackend: {
    response: '{\n  "code": 0,\n  "message": "success",\n  "data": {}\n}',
    delay: 0,
  },
  method: 'GET',
  path: '',
  contentType: 'application/json',
  queryParams: [] as any[],
  pathParams: [] as any[],
  headerParams: [] as any[],
  bodyType: 'json',
  bodySchema: '',
  bodyParams: [] as any[],
  successResponse: `{
  "code": 0,
  "message": "success",
  "data": {
    
  }
}`,
  errorResponse: `{
  "code": 400,
  "message": "参数错误",
  "data": null
}`,
  backendService: '',
  backendPath: '',
  timeout: 5000,
  retryCount: 0,
  authMethod: 'jwt',
  rateLimitEnabled: true,
  rateLimit: 100,
  rateLimitUnit: 'second',
  ipWhitelistEnabled: false,
  ipWhitelist: '',
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入API名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入API编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9-]*$/, message: '编码只能包含小写字母、数字和连字符，且以字母开头', trigger: 'blur' },
  ],
  groupId: [
    { required: true, message: '请选择API分组', trigger: 'change' },
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
  ],
  method: [
    { required: true, message: '请选择请求方式', trigger: 'change' },
  ],
  path: [
    { required: true, message: '请输入请求路径', trigger: 'blur' },
    { pattern: /^\//, message: '路径必须以 / 开头', trigger: 'blur' },
  ],
}

// 返回
const goBack = () => {
  router.push('/console/api/list')
}

// 保存草稿
const handleSaveDraft = async () => {
  ElMessage.success('草稿已保存')
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true

    // 组装后端配置
    let backend: any = undefined
    if (formData.backendType === 'http') {
      backend = {
        type: 'http',
        protocol: formData.httpBackend.protocol,
        method: formData.httpBackend.method || formData.method,
        host: formData.httpBackend.host,
        path: formData.httpBackend.path || formData.path,
        timeout: formData.timeout,
      }
    } else if (formData.backendType === 'dubbo') {
      backend = {
        type: 'dubbo',
        registry: formData.dubboBackend.registry,
        interfaceName: formData.dubboBackend.interfaceName,
        methodName: formData.dubboBackend.methodName,
        dubboVersion: formData.dubboBackend.version,
        dubboGroup: formData.dubboBackend.group,
        timeout: formData.timeout,
      }
    } else if (formData.backendType === 'internal') {
      backend = {
        type: 'internal',
        refApiId: formData.internalBackend.apiId,
      }
    } else if (formData.backendType === 'mock') {
      backend = {
        type: 'mock',
        mockResponse: formData.mockBackend.response,
        mockDelay: formData.mockBackend.delay,
      }
    }

    // 组装请求参数
    const requestParams = [
      ...formData.queryParams.map((p: any, i: number) => ({ ...p, location: 'query', sort: i })),
      ...formData.headerParams.map((p: any, i: number) => ({ ...p, location: 'header', sort: i })),
      ...formData.pathParams.map((p: any, i: number) => ({ ...p, location: 'path', sort: i })),
      ...formData.bodyParams.map((p: any, i: number) => ({ ...p, location: 'body', sort: i })),
    ].filter((p: any) => p.name)

    if (isEdit.value) {
      // 更新API
      const updateData: UpdateApiRequest = {
        groupId: formData.groupId,
        name: formData.name,
        version: formData.version,
        description: formData.description,
        method: formData.method,
        path: formData.path,
        contentType: formData.contentType,
        authType: formData.authMethod === 'none' ? 'none' : (formData.authMethod === 'jwt' ? 'token' : 'signature'),
        timeout: formData.timeout,
        retryCount: formData.retryCount,
        rateLimitEnabled: formData.rateLimitEnabled,
        rateLimitQps: formData.rateLimitUnit === 'second' ? formData.rateLimit : (formData.rateLimitUnit === 'minute' ? Math.ceil(formData.rateLimit / 60) : Math.ceil(formData.rateLimit / 3600)),
        mockEnabled: formData.backendType === 'mock',
        mockResponse: formData.backendType === 'mock' ? formData.mockBackend.response : undefined,
        backend: backend,
        requestParams: requestParams.length > 0 ? requestParams : undefined,
      }
      await apiManageApi.update(route.params.id as string, updateData)
      ElMessage.success('API更新成功')
    } else {
      // 创建API
      const createData: CreateApiRequest = {
        groupId: formData.groupId,
        name: formData.name,
        code: formData.code,
        version: formData.version,
        description: formData.description,
        method: formData.method,
        path: formData.path,
        contentType: formData.contentType,
        authType: formData.authMethod === 'none' ? 'none' : (formData.authMethod === 'jwt' ? 'token' : 'signature'),
        timeout: formData.timeout,
        retryCount: formData.retryCount,
        rateLimitEnabled: formData.rateLimitEnabled,
        rateLimitQps: formData.rateLimitUnit === 'second' ? formData.rateLimit : (formData.rateLimitUnit === 'minute' ? Math.ceil(formData.rateLimit / 60) : Math.ceil(formData.rateLimit / 3600)),
        mockEnabled: formData.backendType === 'mock',
        mockResponse: formData.backendType === 'mock' ? formData.mockBackend.response : undefined,
        backend: backend,
        requestParams: requestParams.length > 0 ? requestParams : undefined,
      }
      await apiManageApi.create(createData)
      ElMessage.success('API创建成功')
    }
    router.push('/console/api/list')
  } catch (error: any) {
    if (error?.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('请检查表单填写是否正确')
    }
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  fetchApiGroups()
  if (isEdit.value) {
    fetchApiDetail()
  }
})
</script>

<style scoped>
.api-create-page {
  padding: 0;
  max-width: 1000px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
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

/* 表单卡片 */
.form-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1a1a1a;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-unit {
  margin-left: 8px;
  color: #666;
}

/* 代码输入框 */
.code-textarea :deep(textarea) {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
}

/* Body编辑器 */
.body-editor {
  padding: 8px 0;
}

/* 限流配置 */
.rate-limit-config {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
</style>
