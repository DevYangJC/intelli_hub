<template>
  <div class="api-debugger">
    <!-- 请求配置 -->
    <el-card shadow="never" class="debugger-card">
      <template #header>
        <div class="card-header">
          <span>请求配置</span>
          <el-button type="primary" :loading="sending" @click="sendRequest">
            <el-icon><CaretRight /></el-icon>
            发送请求
          </el-button>
        </div>
      </template>

      <!-- 请求URL -->
      <div class="request-url">
        <el-select v-model="request.method" style="width: 100px">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
          <el-option label="PATCH" value="PATCH" />
        </el-select>
        <el-input v-model="request.url" placeholder="请求URL" class="url-input">
          <template #prepend>{{ baseUrl }}</template>
        </el-input>
      </div>

      <!-- 参数Tab -->
      <el-tabs v-model="activeTab" class="param-tabs">
        <!-- Query参数 -->
        <el-tab-pane label="Query参数" name="query">
          <div class="param-list">
            <div v-for="(param, index) in request.queryParams" :key="index" class="param-row">
              <el-checkbox v-model="param.enabled" />
              <el-input v-model="param.key" placeholder="参数名" size="small" />
              <el-input v-model="param.value" placeholder="参数值" size="small" />
              <el-button type="danger" link size="small" @click="removeParam('query', index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button text type="primary" size="small" @click="addParam('query')">
              <el-icon><Plus /></el-icon> 添加参数
            </el-button>
          </div>
        </el-tab-pane>

        <!-- Header参数 -->
        <el-tab-pane label="Headers" name="headers">
          <div class="param-list">
            <div v-for="(param, index) in request.headers" :key="index" class="param-row">
              <el-checkbox v-model="param.enabled" />
              <el-select 
                v-model="param.key" 
                filterable 
                allow-create
                placeholder="Header名" 
                size="small"
                style="width: 200px"
              >
                <el-option label="Authorization" value="Authorization" />
                <el-option label="Content-Type" value="Content-Type" />
                <el-option label="X-Request-Id" value="X-Request-Id" />
                <el-option label="X-Api-Key" value="X-Api-Key" />
              </el-select>
              <el-input v-model="param.value" placeholder="Header值" size="small" />
              <el-button type="danger" link size="small" @click="removeParam('headers', index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button text type="primary" size="small" @click="addParam('headers')">
              <el-icon><Plus /></el-icon> 添加Header
            </el-button>
          </div>
        </el-tab-pane>

        <!-- Body参数 -->
        <el-tab-pane label="Body" name="body" :disabled="!['POST', 'PUT', 'PATCH'].includes(request.method)">
          <div class="body-type-selector">
            <el-radio-group v-model="request.bodyType" size="small">
              <el-radio-button value="json">JSON</el-radio-button>
              <el-radio-button value="form">Form Data</el-radio-button>
              <el-radio-button value="raw">Raw</el-radio-button>
            </el-radio-group>
          </div>
          <el-input
            v-model="request.body"
            type="textarea"
            :rows="10"
            placeholder='{"key": "value"}'
            class="code-textarea"
          />
        </el-tab-pane>

        <!-- 认证 -->
        <el-tab-pane label="认证" name="auth">
          <el-form label-width="100px">
            <el-form-item label="认证方式">
              <el-select v-model="request.authType" style="width: 200px">
                <el-option label="无认证" value="none" />
                <el-option label="Bearer Token" value="bearer" />
                <el-option label="AppKey/Secret" value="appkey" />
              </el-select>
            </el-form-item>
            <template v-if="request.authType === 'bearer'">
              <el-form-item label="Token">
                <el-input v-model="request.bearerToken" placeholder="输入Bearer Token" />
              </el-form-item>
            </template>
            <template v-if="request.authType === 'appkey'">
              <el-form-item label="AppKey">
                <el-input v-model="request.appKey" placeholder="输入AppKey" />
              </el-form-item>
              <el-form-item label="AppSecret">
                <el-input v-model="request.appSecret" placeholder="输入AppSecret" type="password" show-password />
              </el-form-item>
            </template>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 响应结果 -->
    <el-card shadow="never" class="debugger-card response-card">
      <template #header>
        <div class="card-header">
          <span>响应结果</span>
          <div class="response-meta" v-if="response.status">
            <el-tag :type="getStatusType(response.status)" size="small">
              {{ response.status }} {{ response.statusText }}
            </el-tag>
            <span class="meta-item">耗时: {{ response.time }}ms</span>
            <span class="meta-item">大小: {{ formatSize(response.size) }}</span>
          </div>
        </div>
      </template>

      <el-tabs v-model="responseTab">
        <el-tab-pane label="响应体" name="body">
          <div class="response-body" v-if="response.body">
            <el-button-group class="format-buttons">
              <el-button size="small" @click="formatJson">格式化</el-button>
              <el-button size="small" @click="copyResponse">复制</el-button>
            </el-button-group>
            <pre class="code-block">{{ response.body }}</pre>
          </div>
          <el-empty v-else description="发送请求后查看响应" />
        </el-tab-pane>
        <el-tab-pane label="响应头" name="headers">
          <el-table :data="response.headers" size="small" v-if="response.headers.length">
            <el-table-column prop="key" label="Header名" width="200" />
            <el-table-column prop="value" label="Header值" />
          </el-table>
          <el-empty v-else description="暂无响应头" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CaretRight, Delete, Plus } from '@element-plus/icons-vue'

interface Props {
  apiInfo?: {
    method: string
    path: string
    queryParams?: any[]
    headerParams?: any[]
  }
}

const props = withDefaults(defineProps<Props>(), {
  apiInfo: () => ({ method: 'GET', path: '' })
})

const baseUrl = 'https://api.intellihub.com'
const activeTab = ref('query')
const responseTab = ref('body')
const sending = ref(false)

// 请求配置
const request = reactive({
  method: 'GET',
  url: '',
  queryParams: [{ enabled: true, key: '', value: '' }] as { enabled: boolean; key: string; value: string }[],
  headers: [
    { enabled: true, key: 'Content-Type', value: 'application/json' }
  ] as { enabled: boolean; key: string; value: string }[],
  bodyType: 'json',
  body: '',
  authType: 'none',
  bearerToken: '',
  appKey: '',
  appSecret: '',
})

// 响应结果
const response = reactive({
  status: 0,
  statusText: '',
  time: 0,
  size: 0,
  body: '',
  headers: [] as { key: string; value: string }[],
})

// 监听API信息变化，初始化请求配置
watch(() => props.apiInfo, (info) => {
  if (info) {
    request.method = info.method || 'GET'
    request.url = info.path || ''
    // 初始化Query参数
    if (info.queryParams?.length) {
      request.queryParams = info.queryParams.map(p => ({
        enabled: true,
        key: p.name,
        value: p.example || ''
      }))
    }
    // 初始化Header参数
    if (info.headerParams?.length) {
      request.headers = [
        { enabled: true, key: 'Content-Type', value: 'application/json' },
        ...info.headerParams.map(p => ({
          enabled: true,
          key: p.name,
          value: p.example || ''
        }))
      ]
    }
  }
}, { immediate: true })

// 添加参数
const addParam = (type: 'query' | 'headers') => {
  if (type === 'query') {
    request.queryParams.push({ enabled: true, key: '', value: '' })
  } else {
    request.headers.push({ enabled: true, key: '', value: '' })
  }
}

// 移除参数
const removeParam = (type: 'query' | 'headers', index: number) => {
  if (type === 'query') {
    request.queryParams.splice(index, 1)
  } else {
    request.headers.splice(index, 1)
  }
}

// 发送请求
const sendRequest = async () => {
  if (!request.url) {
    ElMessage.warning('请输入请求URL')
    return
  }

  sending.value = true
  const startTime = Date.now()

  try {
    // 构建完整URL
    let fullUrl = baseUrl + request.url
    const enabledQueryParams = request.queryParams.filter(p => p.enabled && p.key)
    if (enabledQueryParams.length) {
      const queryString = enabledQueryParams
        .map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value)}`)
        .join('&')
      fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString
    }

    // 构建请求头
    const headers: Record<string, string> = {}
    request.headers.filter(h => h.enabled && h.key).forEach(h => {
      headers[h.key] = h.value
    })

    // 添加认证头
    if (request.authType === 'bearer' && request.bearerToken) {
      headers['Authorization'] = `Bearer ${request.bearerToken}`
    } else if (request.authType === 'appkey' && request.appKey) {
      headers['X-Api-Key'] = request.appKey
      // 实际项目中可能需要签名逻辑
    }

    // 发送请求
    const fetchOptions: RequestInit = {
      method: request.method,
      headers,
    }

    if (['POST', 'PUT', 'PATCH'].includes(request.method) && request.body) {
      fetchOptions.body = request.body
    }

    const res = await fetch(fullUrl, fetchOptions)
    const endTime = Date.now()

    // 解析响应
    response.status = res.status
    response.statusText = res.statusText
    response.time = endTime - startTime
    
    const text = await res.text()
    response.body = text
    response.size = new Blob([text]).size

    // 解析响应头
    response.headers = []
    res.headers.forEach((value, key) => {
      response.headers.push({ key, value })
    })

    // 尝试格式化JSON
    try {
      const json = JSON.parse(text)
      response.body = JSON.stringify(json, null, 2)
    } catch {
      // 非JSON格式，保持原样
    }

  } catch (error: any) {
    response.status = 0
    response.statusText = 'Error'
    response.time = Date.now() - startTime
    response.body = error.message || '请求失败'
    ElMessage.error('请求失败: ' + (error.message || '未知错误'))
  } finally {
    sending.value = false
  }
}

// 获取状态码标签类型
const getStatusType = (status: number) => {
  if (status >= 200 && status < 300) return 'success'
  if (status >= 300 && status < 400) return 'warning'
  if (status >= 400) return 'danger'
  return 'info'
}

// 格式化文件大小
const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

// 格式化JSON
const formatJson = () => {
  try {
    const json = JSON.parse(response.body)
    response.body = JSON.stringify(json, null, 2)
  } catch {
    ElMessage.warning('响应体不是有效的JSON格式')
  }
}

// 复制响应
const copyResponse = async () => {
  try {
    await navigator.clipboard.writeText(response.body)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.api-debugger {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.debugger-card {
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.request-url {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.url-input {
  flex: 1;
}

.param-tabs {
  margin-top: 8px;
}

.param-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.param-row .el-input {
  flex: 1;
}

.body-type-selector {
  margin-bottom: 12px;
}

.code-textarea :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
}

.response-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-item {
  font-size: 12px;
  color: #909399;
}

.response-body {
  position: relative;
}

.format-buttons {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 1;
}

.code-block {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  overflow-x: auto;
  max-height: 400px;
  margin: 0;
}

.response-card :deep(.el-card__body) {
  min-height: 200px;
}
</style>
