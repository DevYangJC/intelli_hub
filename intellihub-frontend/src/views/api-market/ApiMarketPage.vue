<template>
  <div class="api-market-page">
    <!-- Hero 区域 -->
    <div class="hero-section">
      <div class="hero-bg">
        <div class="hero-shape shape-1"></div>
        <div class="hero-shape shape-2"></div>
        <div class="hero-shape shape-3"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <el-icon><Connection /></el-icon>
          <span>开放平台</span>
        </div>
        <h1 class="hero-title">探索 <span class="highlight">API</span> 市场</h1>
        <p class="hero-desc">发现和订阅丰富的 API 服务，加速您的应用开发</p>
        <div class="hero-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索 API 名称、描述、路径..."
            class="search-input"
            size="large"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </template>
          </el-input>
        </div>
        <div class="hero-stats">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Link /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ totalApis }}</span>
              <span class="stat-label">可用 API</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ categories.length - 1 }}</span>
              <span class="stat-label">API 分类</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatCalls(totalCalls) }}</span>
              <span class="stat-label">累计调用</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="page-container">
      <!-- 分类标签 -->
      <div class="category-section">
        <div class="category-tags">
          <el-check-tag
            v-for="cat in categories"
            :key="cat.value"
            :checked="selectedCategory === cat.value"
            @change="handleCategoryChange(cat.value)"
          >
            {{ cat.label }}
          </el-check-tag>
        </div>
        <div class="sort-options">
          <span class="sort-label">排序：</span>
          <el-radio-group v-model="sortBy" size="small">
            <el-radio-button label="newest">最新</el-radio-button>
            <el-radio-button label="popular">热门</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- API列表 -->
      <div class="api-grid" v-loading="loading">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :lg="8" :xl="6" v-for="(api, index) in apiList" :key="api.id">
            <el-card class="api-card" shadow="hover" @click="viewApiDetail(api)">
              <div class="api-card-header">
                <div class="api-icon" :style="{ background: getIconBg(index) }">
                  <el-icon :size="24"><Link /></el-icon>
                </div>
                <el-tag :type="getMethodType(api.method)" size="small" effect="dark">
                  {{ api.method }}
                </el-tag>
              </div>
              <h3 class="api-name">{{ api.name }}</h3>
              <p class="api-path">{{ api.path }}</p>
              <p class="api-desc">{{ api.description || '暂无描述' }}</p>
              <div class="api-footer">
                <div class="api-stats">
                  <span class="stat">
                    <el-icon><Connection /></el-icon>
                    {{ formatCalls(api.totalCalls || 0) }}
                  </span>
                  <span class="stat">
                    <el-icon><Clock /></el-icon>
                    {{ api.avgLatency || 0 }}ms
                  </span>
                </div>
                <el-tag size="small" type="info" effect="plain">{{ api.groupName || '未分组' }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && apiList.length === 0" description="暂无匹配的API">
        <el-button type="primary" @click="resetFilters">清除筛选</el-button>
      </el-empty>

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
  Clock,
  Link,
  Grid,
  DataLine,
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
const categories = ref<{ label: string; value: string }[]>([{ label: '全部', value: '' }])

// API列表
const apiList = ref<ApiInfoResponse[]>([])
const totalApis = ref(0)
const totalCalls = ref(0)

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

// 获取方法类型标签颜色
const getMethodType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return types[method] || 'info'
}

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
        { label: '全部', value: '' },
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
      // 计算累计调用次数
      totalCalls.value = apiList.value.reduce((sum, api) => sum + (api.totalCalls || 0), 0)
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

// 分类切换
const handleCategoryChange = (value: string) => {
  selectedCategory.value = value
  currentPage.value = 1
  loadApiList()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  selectedCategory.value = ''
  sortBy.value = 'newest'
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

// 监听排序变化
watch(sortBy, () => {
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
}

/* Hero 区域 */
.hero-section {
  background: #fff;
  padding: 80px 24px 60px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.6;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  top: -150px;
  right: -100px;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  bottom: -100px;
  left: -50px;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #e0f2fe 0%, #7dd3fc 100%);
  top: 50%;
  left: 15%;
  opacity: 0.3;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  color: #fff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 16px;
  letter-spacing: -0.02em;
}

.hero-title .highlight {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-desc {
  font-size: 18px;
  color: #64748b;
  margin: 0 0 36px;
  line-height: 1.6;
}

.hero-search {
  max-width: 560px;
  margin: 0 auto 40px;
}

.hero-search .search-input {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
}

.hero-search .search-input :deep(.el-input__wrapper) {
  padding: 8px 16px;
  border-radius: 12px 0 0 12px;
}

.hero-search .search-input :deep(.el-input-group__append) {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  border-radius: 0 12px 12px 0;
}

.hero-search .search-input :deep(.el-input-group__append .el-button) {
  color: #fff;
  padding: 0 20px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.stat-card:hover {
  border-color: #0ea5e9;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.15);
}

.stat-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0284c7;
  font-size: 20px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}

/* 页面容器 */
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

/* 分类区域 */
.category-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.category-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-tags .el-check-tag {
  border-radius: 16px;
  padding: 6px 16px;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  font-size: 14px;
  color: #666;
}

/* API卡片 */
.api-grid {
  margin-bottom: 24px;
}

.api-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  margin-bottom: 20px;
  border-radius: 12px;
}

.api-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.api-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.api-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.api-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.api-path {
  font-size: 12px;
  color: #999;
  font-family: 'Monaco', 'Menlo', monospace;
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.api-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.api-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.api-stats {
  display: flex;
  gap: 12px;
}

.api-stats .stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-section {
    padding: 50px 16px 40px;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-desc {
    font-size: 15px;
    margin-bottom: 24px;
  }

  .hero-stats {
    flex-direction: column;
    gap: 12px;
  }

  .stat-card {
    padding: 12px 16px;
  }

  .stat-value {
    font-size: 20px;
  }

  .page-container {
    padding: 16px;
  }

  .category-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
