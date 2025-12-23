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
      <div class="api-grid">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8" :xl="6" v-for="api in filteredApis" :key="api.id">
            <el-card class="api-card" shadow="hover" @click="viewApiDetail(api)">
              <div class="api-card-header">
                <div class="api-icon" :style="{ background: api.iconBg }">
                  <el-icon :size="24"><component :is="api.icon" /></el-icon>
                </div>
                <el-tag :type="api.status === 'stable' ? 'success' : 'warning'" size="small">
                  {{ api.status === 'stable' ? '稳定' : '测试中' }}
                </el-tag>
              </div>
              <h3 class="api-name">{{ api.name }}</h3>
              <p class="api-desc">{{ api.description }}</p>
              <div class="api-meta">
                <span class="meta-item">
                  <el-icon><Connection /></el-icon>
                  {{ api.calls }}次调用
                </span>
                <span class="meta-item">
                  <el-icon><Star /></el-icon>
                  {{ api.rating }}
                </span>
              </div>
              <div class="api-tags">
                <el-tag v-for="tag in api.tags" :key="tag" size="small" type="info">
                  {{ tag }}
                </el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="filteredApis.length === 0" description="暂无匹配的API" />

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="filteredApis.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalApis"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search,
  Connection,
  Star,
  User,
  ShoppingCart,
  Location,
  Message,
  Picture,
  Document,
  DataAnalysis,
  Coin,
} from '@element-plus/icons-vue'

const router = useRouter()

// 搜索和筛选状态
const searchKeyword = ref('')
const selectedCategory = ref('')
const sortBy = ref('newest')
const currentPage = ref(1)
const pageSize = ref(12)

// 分类列表
const categories = [
  { label: '全部分类', value: '' },
  { label: '用户认证', value: 'auth' },
  { label: '支付服务', value: 'payment' },
  { label: '数据分析', value: 'analytics' },
  { label: '消息推送', value: 'message' },
  { label: '地图定位', value: 'location' },
  { label: '图像处理', value: 'image' },
  { label: '文档服务', value: 'document' },
]

// 模拟API数据
const apiList = ref([
  {
    id: '1',
    name: '用户认证服务',
    description: '提供OAuth2.0、JWT等多种认证方式，支持单点登录和多因素认证',
    icon: User,
    iconBg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    category: 'auth',
    status: 'stable',
    calls: '125.3K',
    rating: 4.8,
    tags: ['OAuth2.0', 'JWT', 'SSO'],
  },
  {
    id: '2',
    name: '支付网关接口',
    description: '集成微信、支付宝、银联等主流支付渠道，支持退款和对账',
    icon: Coin,
    iconBg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    category: 'payment',
    status: 'stable',
    calls: '89.7K',
    rating: 4.9,
    tags: ['微信支付', '支付宝', '银联'],
  },
  {
    id: '3',
    name: '数据统计分析',
    description: '实时数据采集、多维度分析、可视化报表，助力业务决策',
    icon: DataAnalysis,
    iconBg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    category: 'analytics',
    status: 'stable',
    calls: '67.2K',
    rating: 4.7,
    tags: ['实时分析', '报表', 'BI'],
  },
  {
    id: '4',
    name: '消息推送服务',
    description: '支持APP推送、短信、邮件等多渠道消息触达，高到达率',
    icon: Message,
    iconBg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    category: 'message',
    status: 'stable',
    calls: '156.8K',
    rating: 4.6,
    tags: ['APP推送', '短信', '邮件'],
  },
  {
    id: '5',
    name: '地图定位服务',
    description: '提供地理编码、路径规划、POI搜索等位置服务能力',
    icon: Location,
    iconBg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    category: 'location',
    status: 'stable',
    calls: '45.1K',
    rating: 4.5,
    tags: ['地理编码', '路径规划', 'POI'],
  },
  {
    id: '6',
    name: '图像识别处理',
    description: 'AI驱动的图像识别、OCR文字提取、图片压缩裁剪等功能',
    icon: Picture,
    iconBg: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    category: 'image',
    status: 'beta',
    calls: '32.4K',
    rating: 4.4,
    tags: ['OCR', 'AI识别', '图片处理'],
  },
  {
    id: '7',
    name: '文档转换服务',
    description: '支持PDF、Word、Excel等格式互转，在线预览和编辑',
    icon: Document,
    iconBg: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    category: 'document',
    status: 'stable',
    calls: '28.9K',
    rating: 4.3,
    tags: ['PDF', 'Word', '格式转换'],
  },
  {
    id: '8',
    name: '电商订单接口',
    description: '订单创建、查询、状态流转，支持分布式事务和库存管理',
    icon: ShoppingCart,
    iconBg: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
    category: 'payment',
    status: 'stable',
    calls: '78.5K',
    rating: 4.7,
    tags: ['订单管理', '库存', '事务'],
  },
])

// 过滤后的API列表
const filteredApis = computed(() => {
  let result = [...apiList.value]
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(api => 
      api.name.toLowerCase().includes(keyword) ||
      api.description.toLowerCase().includes(keyword)
    )
  }
  
  // 分类筛选
  if (selectedCategory.value) {
    result = result.filter(api => api.category === selectedCategory.value)
  }
  
  // 排序
  if (sortBy.value === 'popular') {
    result.sort((a, b) => parseFloat(b.calls) - parseFloat(a.calls))
  } else if (sortBy.value === 'rating') {
    result.sort((a, b) => b.rating - a.rating)
  }
  
  return result
})

const totalApis = computed(() => filteredApis.value.length)

// 查看API详情
const viewApiDetail = (api: typeof apiList.value[0]) => {
  router.push(`/api-market/${api.id}`)
}
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
