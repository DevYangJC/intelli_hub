<template>
  <div class="placeholder-container">
    <div class="placeholder-content">
      <div class="placeholder-icon">
        <el-icon :size="64" color="#ddd"><Tools /></el-icon>
      </div>
      <h2 class="placeholder-title">{{ pageTitle }}</h2>
      <p class="placeholder-desc">该功能模块正在开发中，敬请期待...</p>
      <div class="placeholder-actions">
        <el-button type="primary" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上一页
        </el-button>
        <el-button @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'
import { Tools, ArrowLeft, HomeFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => {
  return route.meta.title || '功能开发中'
})

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.placeholder-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.placeholder-content {
  text-align: center;
  max-width: 400px;
}

.placeholder-icon {
  margin-bottom: 24px;
}

.placeholder-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px;
}

.placeholder-desc {
  font-size: 14px;
  color: #999;
  margin: 0 0 32px;
}

.placeholder-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>