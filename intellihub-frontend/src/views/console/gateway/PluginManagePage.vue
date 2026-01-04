<template>
  <div class="plugin-manage-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">插件配置</span>
        </div>
      </template>

      <el-alert
        title="功能说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        网关插件用于扩展网关功能,如日志记录、请求转换、响应处理等。插件按顺序执行,可拖拽调整执行顺序。
      </el-alert>

      <el-table :data="pluginList" v-loading="loading" border stripe>
        <el-table-column prop="order" label="执行顺序" width="100" />
        <el-table-column prop="name" label="插件名称" min-width="150" />
        <el-table-column prop="type" label="插件类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getPluginTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleTogglePlugin(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleConfig(row)">
              配置
            </el-button>
            <el-button link type="primary" size="small" @click="handleMoveUp(row)" :disabled="row.order === 1">
              上移
            </el-button>
            <el-button link type="primary" size="small" @click="handleMoveDown(row)" :disabled="row.order === pluginList.length">
              下移
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="configDialogVisible"
      title="插件配置"
      width="600px"
    >
      <el-form
        ref="configFormRef"
        :model="configData"
        label-width="120px"
      >
        <el-form-item label="插件名称">
          <el-input v-model="currentPlugin.name" disabled />
        </el-form-item>
        <el-form-item label="插件类型">
          <el-input v-model="currentPlugin.type" disabled />
        </el-form-item>
        <el-form-item label="配置参数">
          <el-input
            v-model="configData.config"
            type="textarea"
            :rows="10"
            placeholder="请输入JSON格式的配置参数"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, FormInstance } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const configDialogVisible = ref(false)
const configFormRef = ref<FormInstance>()
const currentPlugin = ref<any>({})
const configData = reactive({
  config: ''
})

const pluginList = ref([
  {
    id: '1',
    order: 1,
    name: 'AuthenticationPlugin',
    type: 'auth',
    description: 'JWT Token认证插件',
    enabled: true,
    config: '{}'
  },
  {
    id: '2',
    order: 2,
    name: 'SignaturePlugin',
    type: 'auth',
    description: 'AppKey签名认证插件',
    enabled: true,
    config: '{}'
  },
  {
    id: '3',
    order: 3,
    name: 'RateLimitPlugin',
    type: 'limit',
    description: '限流插件',
    enabled: true,
    config: '{}'
  },
  {
    id: '4',
    order: 4,
    name: 'LoggingPlugin',
    type: 'log',
    description: '访问日志记录插件',
    enabled: true,
    config: '{}'
  },
  {
    id: '5',
    order: 5,
    name: 'ResponseTransformPlugin',
    type: 'transform',
    description: '响应转换插件',
    enabled: false,
    config: '{}'
  }
])

onMounted(() => {
  loadPlugins()
})

const loadPlugins = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleTogglePlugin = (row: any) => {
  ElMessage.success(`插件${row.enabled ? '启用' : '禁用'}成功`)
}

const handleConfig = (row: any) => {
  currentPlugin.value = row
  configData.config = row.config || '{}'
  configDialogVisible.value = true
}

const handleSaveConfig = () => {
  try {
    JSON.parse(configData.config)
    saving.value = true
    setTimeout(() => {
      currentPlugin.value.config = configData.config
      ElMessage.success('配置保存成功')
      configDialogVisible.value = false
      saving.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('配置格式错误,请输入有效的JSON')
  }
}

const handleMoveUp = (row: any) => {
  const index = pluginList.value.findIndex(p => p.id === row.id)
  if (index > 0) {
    const temp = pluginList.value[index - 1]
    pluginList.value[index - 1] = row
    pluginList.value[index] = temp
    
    row.order = index
    temp.order = index + 1
    
    ElMessage.success('调整成功')
  }
}

const handleMoveDown = (row: any) => {
  const index = pluginList.value.findIndex(p => p.id === row.id)
  if (index < pluginList.value.length - 1) {
    const temp = pluginList.value[index + 1]
    pluginList.value[index + 1] = row
    pluginList.value[index] = temp
    
    row.order = index + 2
    temp.order = index + 1
    
    ElMessage.success('调整成功')
  }
}

const getPluginTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    auth: '认证',
    limit: '限流',
    log: '日志',
    transform: '转换',
    cache: '缓存'
  }
  return labels[type] || type
}
</script>

<style scoped lang="scss">
.plugin-manage-page {
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
}
</style>
