<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <img src="/404.svg" alt="404" class="not-found-image" />
      <h1 class="not-found-title">404</h1>
      <h2 class="not-found-subtitle">页面不存在</h2>
      <p class="not-found-description">
        抱歉，您访问的页面不存在或已被删除
      </p>
      <div class="not-found-actions">
        <el-button type="primary" size="large" @click="goHome">
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px;
}

.not-found-image {
  width: 300px;
  max-width: 100%;
  margin-bottom: 40px;
}

.not-found-title {
  font-size: 80px;
  font-weight: 600;
  color: #409eff;
  margin: 0 0 20px 0;
  line-height: 1;
}

.not-found-subtitle {
  font-size: 32px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 16px 0;
}

.not-found-description {
  font-size: 16px;
  color: #606266;
  margin: 0 0 40px 0;
}

.not-found-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 640px) {
  .not-found-content {
    padding: 20px;
  }

  .not-found-image {
    width: 200px;
  }

  .not-found-title {
    font-size: 60px;
  }

  .not-found-subtitle {
    font-size: 24px;
  }

  .not-found-actions {
    flex-direction: column;
    width: 100%;
  }

  .not-found-actions .el-button {
    width: 100%;
  }
}
</style>