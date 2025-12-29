<template>
  <div class="tenant-security-page">
    <div class="page-header">
      <h1 class="page-title">多租户安全隔离</h1>
      <p class="page-desc">企业级多租户架构，细粒度权限控制，数据安全隔离，满足合规要求</p>
    </div>

    <!-- 安全状态 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in securityStats" :key="stat.label">
        <div class="stat-card" :class="stat.type">
          <div class="stat-icon">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 安全特性 -->
    <div class="feature-section">
      <h2 class="section-title">安全特性</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :lg="8" v-for="feature in securityFeatures" :key="feature.title">
          <el-card class="feature-card" shadow="hover">
            <div class="feature-header">
              <el-icon :size="32" :color="feature.color"><component :is="feature.icon" /></el-icon>
              <span class="feature-title">{{ feature.title }}</span>
            </div>
            <p class="feature-desc">{{ feature.description }}</p>
            <div class="feature-items">
              <div v-for="item in feature.items" :key="item" class="feature-item">
                <el-icon color="#67C23A"><CircleCheck /></el-icon>
                <span>{{ item }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据隔离架构 -->
    <div class="architecture-section">
      <h2 class="section-title">数据隔离架构</h2>
      <el-card class="arch-card">
        <div class="arch-diagram">
          <div class="arch-layer">
            <div class="layer-title">应用层</div>
            <div class="layer-items">
              <div class="layer-item">租户A控制台</div>
              <div class="layer-item">租户B控制台</div>
              <div class="layer-item">租户C控制台</div>
            </div>
          </div>
          <div class="arch-arrow">
            <el-icon :size="24"><Bottom /></el-icon>
          </div>
          <div class="arch-layer gateway">
            <div class="layer-title">网关层 - 统一入口</div>
            <div class="layer-items">
              <div class="layer-item highlight">身份认证</div>
              <div class="layer-item highlight">租户识别</div>
              <div class="layer-item highlight">权限校验</div>
            </div>
          </div>
          <div class="arch-arrow">
            <el-icon :size="24"><Bottom /></el-icon>
          </div>
          <div class="arch-layer service">
            <div class="layer-title">服务层 - 数据隔离</div>
            <div class="layer-items">
              <div class="layer-item">租户上下文</div>
              <div class="layer-item">数据过滤</div>
              <div class="layer-item">资源配额</div>
            </div>
          </div>
          <div class="arch-arrow">
            <el-icon :size="24"><Bottom /></el-icon>
          </div>
          <div class="arch-layer storage">
            <div class="layer-title">存储层 - 物理/逻辑隔离</div>
            <div class="layer-items">
              <div class="layer-item">租户A数据</div>
              <div class="layer-item">租户B数据</div>
              <div class="layer-item">租户C数据</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 管理功能 -->
    <div class="management-section">
      <h2 class="section-title">管理功能</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :lg="6" v-for="module in managementModules" :key="module.title">
          <div class="mgmt-card" @click="navigateTo(module.path)">
            <el-icon :size="36" :color="module.color"><component :is="module.icon" /></el-icon>
            <h3>{{ module.title }}</h3>
            <p>{{ module.description }}</p>
            <el-button type="primary" text>
              进入管理 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 合规认证 -->
    <div class="compliance-section">
      <h2 class="section-title">合规与认证</h2>
      <div class="compliance-cards">
        <div class="compliance-card" v-for="cert in certifications" :key="cert.name">
          <el-icon :size="32" color="#409EFF"><component :is="cert.icon" /></el-icon>
          <div class="cert-info">
            <div class="cert-name">{{ cert.name }}</div>
            <div class="cert-desc">{{ cert.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import {
  Lock,
  Key,
  User,
  Setting,
  DataAnalysis,
  Document,
  CircleCheck,
  Bottom,
  ArrowRight,
  OfficeBuilding,
  UserFilled,
  Stamp,
  View
} from '@element-plus/icons-vue'

const router = useRouter()

const securityStats = ref([
  { label: '活跃租户', value: '28', icon: markRaw(OfficeBuilding), type: 'primary' },
  { label: '用户总数', value: '1,256', icon: markRaw(UserFilled), type: 'info' },
  { label: '安全事件', value: '0', icon: markRaw(Lock), type: 'success' },
  { label: '合规检查', value: '通过', icon: markRaw(CircleCheck), type: 'success' }
])

const securityFeatures = ref([
  {
    title: '身份认证',
    description: '支持多种认证方式，确保用户身份真实可信',
    icon: markRaw(Key),
    color: '#409EFF',
    items: ['JWT Token 认证', '多因素认证 (MFA)', 'SSO 单点登录', 'OAuth2.0 集成']
  },
  {
    title: '权限控制',
    description: '基于 RBAC 的细粒度权限管理，精确控制资源访问',
    icon: markRaw(Lock),
    color: '#67C23A',
    items: ['角色权限管理', '资源级权限控制', '操作审计日志', '权限继承机制']
  },
  {
    title: '数据隔离',
    description: '租户间数据完全隔离，保障数据安全与隐私',
    icon: markRaw(Lock),
    color: '#E6A23C',
    items: ['逻辑数据隔离', '物理存储隔离', '网络访问隔离', '加密存储传输']
  },
  {
    title: '资源配额',
    description: '精细化资源配额管理，防止资源滥用',
    icon: markRaw(DataAnalysis),
    color: '#F56C6C',
    items: ['API 调用配额', '存储空间限制', '并发连接控制', '带宽流量限制']
  },
  {
    title: '安全审计',
    description: '完整的操作审计追踪，满足合规要求',
    icon: markRaw(Document),
    color: '#909399',
    items: ['操作日志记录', '登录行为追踪', '敏感操作告警', '审计报表导出']
  },
  {
    title: '安全策略',
    description: '灵活配置安全策略，满足不同安全需求',
    icon: markRaw(Setting),
    color: '#8B5CF6',
    items: ['密码复杂度要求', '会话超时控制', 'IP 白名单限制', '登录失败锁定']
  }
])

const managementModules = ref([
  {
    title: '租户管理',
    description: '创建和管理租户，配置租户基本信息',
    icon: markRaw(OfficeBuilding),
    color: '#409EFF',
    path: '/console/tenant/list'
  },
  {
    title: '用户管理',
    description: '管理租户下的用户和角色权限',
    icon: markRaw(User),
    color: '#67C23A',
    path: '/console/users/list'
  },
  {
    title: '配额管理',
    description: '配置租户的资源使用配额限制',
    icon: markRaw(DataAnalysis),
    color: '#E6A23C',
    path: '/console/tenant/quota'
  },
  {
    title: '安全设置',
    description: '配置平台安全策略和认证方式',
    icon: markRaw(Setting),
    color: '#F56C6C',
    path: '/console/settings'
  }
])

const certifications = ref([
  { name: 'ISO 27001', description: '信息安全管理体系认证', icon: markRaw(Stamp) },
  { name: 'SOC 2', description: '服务组织控制报告', icon: markRaw(Document) },
  { name: 'GDPR', description: '欧盟通用数据保护条例', icon: markRaw(Lock) },
  { name: '等保三级', description: '网络安全等级保护', icon: markRaw(View) }
])

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.tenant-security-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.stats-row {
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.stat-card.primary .stat-icon {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.stat-card.success .stat-icon {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.stat-card.info .stat-icon {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px;
}

.feature-section {
  margin-bottom: 32px;
}

.feature-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.feature-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px;
  line-height: 1.6;
}

.feature-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #333;
}

.architecture-section {
  margin-bottom: 32px;
}

.arch-card {
  border-radius: 12px;
}

.arch-diagram {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.arch-layer {
  width: 100%;
  max-width: 600px;
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.arch-layer.gateway {
  background: linear-gradient(135deg, #e6f4ff 0%, #f0f9ff 100%);
  border: 1px solid #91caff;
}

.arch-layer.service {
  background: linear-gradient(135deg, #f6ffed 0%, #f0fff4 100%);
  border: 1px solid #b7eb8f;
}

.arch-layer.storage {
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
  border: 1px solid #ffd591;
}

.layer-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.layer-items {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.layer-item {
  background: #fff;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  border: 1px solid #e8e8e8;
}

.layer-item.highlight {
  background: #409EFF;
  color: #fff;
  border: none;
}

.arch-arrow {
  padding: 8px 0;
  color: #c0c4cc;
}

.management-section {
  margin-bottom: 32px;
}

.mgmt-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.mgmt-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.mgmt-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 12px 0 8px;
}

.mgmt-card p {
  font-size: 13px;
  color: #666;
  margin: 0 0 16px;
}

.compliance-section {
  margin-bottom: 32px;
}

.compliance-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.compliance-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #f0f0f0;
}

.cert-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.cert-desc {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
</style>
