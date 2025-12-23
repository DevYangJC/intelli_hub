<template>
  <div class="app-api-auth">
    <!-- 搜索和筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索API名称或路径"
        clearable
        style="width: 300px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filterGroup" placeholder="选择分组" clearable style="width: 150px" @change="handleSearch">
        <el-option
          v-for="group in apiGroups"
          :key="group.id"
          :label="group.name"
          :value="group.id"
        />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- 已授权API统计 -->
    <div class="auth-stats">
      <el-tag type="success" size="large">
        已授权 {{ authorizedCount }} 个API
      </el-tag>
      <el-button type="primary" link @click="selectAll">全选</el-button>
      <el-button type="primary" link @click="deselectAll">取消全选</el-button>
    </div>

    <!-- API列表 -->
    <div class="api-list" v-loading="loading">
      <div v-for="group in groupedApis" :key="group.id" class="api-group">
        <div class="group-header">
          <el-checkbox
            v-model="group.allSelected"
            :indeterminate="group.indeterminate"
            @change="toggleGroup(group)"
          >
            <span class="group-name">{{ group.name }}</span>
            <el-tag size="small" type="info">{{ group.apis.length }} 个API</el-tag>
          </el-checkbox>
        </div>
        <div class="group-apis">
          <div
            v-for="api in group.apis"
            :key="api.id"
            class="api-item"
            :class="{ selected: api.authorized }"
          >
            <el-checkbox v-model="api.authorized" @change="updateGroupStatus(group)">
              <div class="api-info">
                <el-tag :type="getMethodType(api.method)" size="small" class="method-tag">
                  {{ api.method }}
                </el-tag>
                <span class="api-path">{{ api.path }}</span>
                <span class="api-name">{{ api.name }}</span>
              </div>
            </el-checkbox>
          </div>
        </div>
      </div>

      <el-empty v-if="groupedApis.length === 0 && !loading" description="暂无可授权的API" />
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">
        保存授权
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { apiManageApi, apiGroupApi, type ApiGroupResponse, type ApiInfoResponse } from '@/api/apiManage'
import { appApi } from '@/api/app'

interface Props {
  appId: string
  appName?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'cancel': []
  'save': [authorizedApiIds: string[]]
}>()

// 状态
const loading = ref(false)
const saving = ref(false)
const searchKeyword = ref('')
const filterGroup = ref('')

// API分组
const apiGroups = ref<ApiGroupResponse[]>([])

// 分组后的API列表
interface GroupedApi {
  id: string
  name: string
  allSelected: boolean
  indeterminate: boolean
  apis: Array<ApiInfoResponse & { authorized: boolean }>
}
const groupedApis = ref<GroupedApi[]>([])

// 已授权数量
const authorizedCount = computed(() => {
  let count = 0
  groupedApis.value.forEach(group => {
    group.apis.forEach(api => {
      if (api.authorized) count++
    })
  })
  return count
})

// 获取请求方法标签类型
const getMethodType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info',
  }
  return types[method] || 'info'
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

// 已订阅的API ID集合
const subscribedApiIds = ref<Set<string>>(new Set())

// 获取已订阅的API列表
const fetchSubscriptions = async () => {
  try {
    const res = await appApi.listSubscriptions(props.appId)
    if (res.code === 200) {
      subscribedApiIds.value = new Set(res.data.map((sub: any) => sub.apiId))
    }
  } catch (error) {
    console.error('获取订阅列表失败:', error)
  }
}

// 获取API列表
const fetchApiList = async () => {
  loading.value = true
  try {
    // 先获取已订阅的API
    await fetchSubscriptions()
    
    const res = await apiManageApi.list({
      keyword: searchKeyword.value || undefined,
      groupId: filterGroup.value || undefined,
      status: 'published',
      page: 1,
      size: 500,
    })
    
    if (res.code === 200) {
      const apis = res.data.records || []
      
      // 按分组组织API
      const groupMap = new Map<string, GroupedApi>()
      
      apis.forEach((api: ApiInfoResponse) => {
        const groupId = api.groupId || 'ungrouped'
        const groupName = api.groupName || '未分组'
        
        if (!groupMap.has(groupId)) {
          groupMap.set(groupId, {
            id: groupId,
            name: groupName,
            allSelected: false,
            indeterminate: false,
            apis: [],
          })
        }
        
        groupMap.get(groupId)!.apis.push({
          ...api,
          authorized: subscribedApiIds.value.has(api.id), // 根据已订阅列表设置状态
        })
      })
      
      groupedApis.value = Array.from(groupMap.values())
      
      // 更新分组选中状态
      groupedApis.value.forEach(group => {
        updateGroupStatus(group)
      })
    }
  } catch (error) {
    console.error('获取API列表失败:', error)
    ElMessage.error('获取API列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchApiList()
}

// 更新分组选中状态
const updateGroupStatus = (group: GroupedApi) => {
  const selectedCount = group.apis.filter(api => api.authorized).length
  group.allSelected = selectedCount === group.apis.length && group.apis.length > 0
  group.indeterminate = selectedCount > 0 && selectedCount < group.apis.length
}

// 切换分组选中状态
const toggleGroup = (group: GroupedApi) => {
  group.apis.forEach(api => {
    api.authorized = group.allSelected
  })
  group.indeterminate = false
}

// 全选
const selectAll = () => {
  groupedApis.value.forEach(group => {
    group.allSelected = true
    group.indeterminate = false
    group.apis.forEach(api => {
      api.authorized = true
    })
  })
}

// 取消全选
const deselectAll = () => {
  groupedApis.value.forEach(group => {
    group.allSelected = false
    group.indeterminate = false
    group.apis.forEach(api => {
      api.authorized = false
    })
  })
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 保存
const handleSave = async () => {
  saving.value = true
  try {
    // 收集当前选中的API
    const currentSelectedIds = new Set<string>()
    groupedApis.value.forEach(group => {
      group.apis.forEach(api => {
        if (api.authorized) {
          currentSelectedIds.add(api.id)
        }
      })
    })
    
    // 计算需要新增和取消的订阅
    const toSubscribe = [...currentSelectedIds].filter(id => !subscribedApiIds.value.has(id))
    const toUnsubscribe = [...subscribedApiIds.value].filter(id => !currentSelectedIds.has(id))
    
    // 执行订阅操作
    for (const apiId of toSubscribe) {
      await appApi.subscribe(props.appId, { apiId })
    }
    
    // 执行取消订阅操作
    for (const apiId of toUnsubscribe) {
      await appApi.unsubscribe(props.appId, apiId)
    }
    
    ElMessage.success(`已授权 ${currentSelectedIds.size} 个API`)
    emit('save', [...currentSelectedIds])
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 初始化
onMounted(() => {
  fetchApiGroups()
  fetchApiList()
})

// 监听appId变化
watch(() => props.appId, () => {
  if (props.appId) {
    fetchApiList()
  }
})
</script>

<style scoped>
.app-api-auth {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.auth-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.api-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
}

.api-group {
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.group-header {
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.group-name {
  font-weight: 500;
  margin-right: 8px;
}

.group-apis {
  padding: 8px 16px;
}

.api-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.api-item:last-child {
  border-bottom: none;
}

.api-item.selected {
  background: #f0f9eb;
}

.api-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.method-tag {
  width: 60px;
  text-align: center;
}

.api-path {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #409eff;
}

.api-name {
  color: #909399;
  font-size: 12px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  margin-top: 16px;
}
</style>
