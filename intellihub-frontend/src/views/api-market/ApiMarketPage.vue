<template>
  <div class="api-market-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">API市场</h1>
        <p class="page-desc">发现和订阅丰富的API服务，快速构建您的应用</p>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索API名称、描述..."
          class="search-input"
          size="large"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="selectedCategory" placeholder="选择分类" size="large" clearable>
          <el-option
            v-for="cat in categories"
            :key="cat.value"
            :label="cat.label"
            :value="cat.value"
          />
        </el-select>
        <el-select v-model="sortBy" placeholder="排序方式" size="large">
          <el-option label="最新发布" value="newest" />
          <el-option label="最多调用" value="popular" />
          <el-option label="评分最高" value="rating" />
        </el-select>
      </div>

      <!-- API列表 -->
      <div class="api-grid" v-loading="loading">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8" :xl="6" v-for="(api, index) in apiList" :key="api.id">
            <el-card class="api-card" shadow="hover" @click="viewApiDetail(api)">
              <div class="api-card-header">
                <div class="api-icon" :style="{ background: getIconBg(index) }">
                  <el-icon :size="24"><Link /></el-icon>
                </div>
                <el-tag :type="api.status === 'published' ? 'success' : 'warning'" size="small">
                  {{ api.status === 'published' ? '已发布' : api.status === 'draft' ? '草稿' : '已下线' }}
                </el-tag>
              </div>
              <h3 class="api-name">{{ api.name }}</h3>
              <p class="api-desc">{{ api.description || '暂无描述' }}</p>
              <div class="api-meta">
                <span class="meta-item">
                  <el-icon><Connection /></el-icon>
                  {{ formatCalls(api.totalCalls || 0) }}次调用
                </span>
                <span class="meta-item">
                  <el-tag size="small" :type="api.method === 'GET' ? 'success' : api.method === 'POST' ? 'primary' : 'warning'">
                    {{ api.method }}
                  </el-tag>
                </span>
              </div>
              <div class="api-tags">
                <el-tag size="small" type="info">{{ api.groupName || '未分组' }}</el-tag>
                <el-tag size="small" type="info">v{{ api.version || '1.0' }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && apiList.length === 0" description="暂无匹配的API" />

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="apiList.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalApis"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search,
  Connection,
  Star,
  Link,
} from '@element-plus/icons-vue'
import { publicApiApi, apiGroupApi, type ApiInfoResponse, type ApiGroupResponse } from '@/api/apiManage'

const router = useRouter()

// 搜索和筛选状态
const searchKeyword = ref('')
const selectedCategory = ref('')
const sortBy = ref('newest')
const currentPage = ref(1)
const pageSize = ref(12)
const loading = ref(false)

// 分类列表（从后端获取）
const categories = ref<{ label: string; value: string }[]>([{ label: '全部分类', value: '' }])

// API列表
const apiList = ref<ApiInfoResponse[]>([])
const totalApis = ref(0)

// 图标背景颜色列表
const iconBgList = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
  'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
]

// 获取图标背景
const getIconBg = (index: number) => iconBgList[index % iconBgList.length]

// 格式化调用次数
const formatCalls = (calls: number) => {
  if (calls >= 10000) return (calls / 10000).toFixed(1) + 'w'
  if (calls >= 1000) return (calls / 1000).toFixed(1) + 'k'
  return calls.toString()
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await apiGroupApi.list()
    if (res.code === 200 && res.data) {
      categories.value = [
        { label: '全部分类', value: '' },
        ...res.data.map((g: ApiGroupResponse) => ({ label: g.name, value: g.id }))
      ]
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 加载API列表
const loadApiList = async () => {
  loading.value = true
  try {
    const res = await publicApiApi.list({
      keyword: searchKeyword.value || undefined,
      groupId: selectedCategory.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    if (res.code === 200 && res.data) {
      apiList.value = res.data.records || []
      totalApis.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载API列表失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadApiList()
}

// 分页变化
const handlePageChange = () => {
  loadApiList()
}

// 查看API详情
const viewApiDetail = (api: ApiInfoResponse) => {
  router.push(`/api-market/${api.id}`)
}

// 监听筛选变化
watch([selectedCategory, sortBy], () => {
  currentPage.value = 1
  loadApiList()
})

onMounted(() => {
  loadCategories()
  loadApiList()
})
</script>

<style scoped>
.api-market-page {
  min-height: calc(100vh - 56px);
  background: #f5f7fa;
  padding: 24px;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 筛选区域 */
.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 300px;
}

/* API卡片 */
.api-grid {
  margin-bottom: 24px;
}

.api-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  margin-bottom: 24px;
}

.api-card:hover {
  transform: translateY(-4px);
}

.api-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.api-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.api-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.api-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.api-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}

.api-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .api-market-page {
    padding: 16px;
  }

  .filter-section {
    flex-direction: column;
  }

  .search-input {
    min-width: 100%;
  }

  .page-title {
    font-size: 24px;
  }
}
</style>
