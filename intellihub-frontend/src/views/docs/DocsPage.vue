<template>
  <div class="docs-page">
    <div class="docs-container">
      <!-- 侧边栏 -->
      <aside class="docs-sidebar">
        <div class="sidebar-header">
          <h3>开发文档</h3>
        </div>
        <el-menu
          :default-active="activeDoc"
          class="docs-menu"
          @select="handleDocSelect"
        >
          <el-menu-item index="quickstart">
            <el-icon><Promotion /></el-icon>
            <span>快速开始</span>
          </el-menu-item>
          
          <el-sub-menu index="guide">
            <template #title>
              <el-icon><Reading /></el-icon>
              <span>开发指南</span>
            </template>
            <el-menu-item index="guide-auth">认证鉴权</el-menu-item>
            <el-menu-item index="guide-request">请求规范</el-menu-item>
            <el-menu-item index="guide-response">响应格式</el-menu-item>
            <el-menu-item index="guide-error">错误处理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="api-ref">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>API参考</span>
            </template>
            <el-menu-item index="api-user">用户接口</el-menu-item>
            <el-menu-item index="api-order">订单接口</el-menu-item>
            <el-menu-item index="api-payment">支付接口</el-menu-item>
            <el-menu-item index="api-message">消息接口</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="sdk">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>SDK文档</span>
            </template>
            <el-menu-item index="sdk-java">Java SDK</el-menu-item>
            <el-menu-item index="sdk-python">Python SDK</el-menu-item>
            <el-menu-item index="sdk-nodejs">Node.js SDK</el-menu-item>
            <el-menu-item index="sdk-go">Go SDK</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="changelog">
            <el-icon><List /></el-icon>
            <span>更新日志</span>
          </el-menu-item>

          <el-menu-item index="faq">
            <el-icon><QuestionFilled /></el-icon>
            <span>常见问题</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 文档内容 -->
      <main class="docs-main">
        <div class="docs-content">
          <h1>快速开始</h1>
          <p class="lead">欢迎使用 IntelliHub API 平台，本指南将帮助您快速上手。</p>

          <el-alert
            title="提示"
            type="info"
            description="在开始之前，请确保您已注册账号并创建了应用，获取到 API Key。"
            show-icon
            :closable="false"
            style="margin-bottom: 24px"
          />

          <h2>1. 获取 API Key</h2>
          <p>登录控制台，进入「应用管理」-「密钥管理」，创建新的 API Key。</p>
          
          <div class="code-block">
            <div class="code-header">
              <span>API Key 格式</span>
              <el-button type="text" size="small" @click="copyCode">
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
            </div>
            <pre><code>API-KEY: ihub_sk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</code></pre>
          </div>

          <h2>2. 发起第一个请求</h2>
          <p>所有 API 请求都需要在 Header 中携带 API Key 进行认证。</p>

          <el-tabs v-model="activeTab" class="code-tabs">
            <el-tab-pane label="cURL" name="curl">
              <div class="code-block">
                <pre><code>curl -X GET "https://api.intellihub.com/v1/users/me" \
  -H "Authorization: Bearer ihub_sk_xxxxxxxx" \
  -H "Content-Type: application/json"</code></pre>
              </div>
            </el-tab-pane>
            <el-tab-pane label="JavaScript" name="js">
              <div class="code-block">
                <pre><code>const response = await fetch('https://api.intellihub.com/v1/users/me', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ihub_sk_xxxxxxxx',
    'Content-Type': 'application/json'
  }
});

const data = await response.json();
console.log(data);</code></pre>
              </div>
            </el-tab-pane>
            <el-tab-pane label="Python" name="python">
              <div class="code-block">
                <pre><code>import requests

response = requests.get(
    'https://api.intellihub.com/v1/users/me',
    headers={
        'Authorization': 'Bearer ihub_sk_xxxxxxxx',
        'Content-Type': 'application/json'
    }
)

print(response.json())</code></pre>
              </div>
            </el-tab-pane>
          </el-tabs>

          <h2>3. 响应格式</h2>
          <p>API 返回统一的 JSON 格式响应：</p>

          <div class="code-block">
            <pre><code>{
  "code": 0,
  "message": "success",
  "data": {
    "id": "user_123",
    "username": "demo_user",
    "email": "demo@example.com",
    "created_at": "2024-01-15T10:30:00Z"
  },
  "request_id": "req_abc123"
}</code></pre>
          </div>

          <h2>4. 错误处理</h2>
          <p>当请求失败时，API 会返回相应的错误码和错误信息：</p>

          <el-table :data="errorCodes" style="width: 100%">
            <el-table-column prop="code" label="错误码" width="120" />
            <el-table-column prop="message" label="错误信息" width="200" />
            <el-table-column prop="description" label="说明" />
          </el-table>

          <h2>5. 下一步</h2>
          <div class="next-steps">
            <el-card v-for="step in nextSteps" :key="step.title" class="step-card" shadow="hover">
              <div class="step-icon">
                <el-icon :size="24"><component :is="step.icon" /></el-icon>
              </div>
              <h4>{{ step.title }}</h4>
              <p>{{ step.description }}</p>
            </el-card>
          </div>
        </div>
      </main>

      <!-- 右侧目录 -->
      <aside class="docs-toc">
        <div class="toc-header">目录</div>
        <ul class="toc-list">
          <li><a href="#" class="active">1. 获取 API Key</a></li>
          <li><a href="#">2. 发起第一个请求</a></li>
          <li><a href="#">3. 响应格式</a></li>
          <li><a href="#">4. 错误处理</a></li>
          <li><a href="#">5. 下一步</a></li>
        </ul>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Promotion,
  Reading,
  Document,
  Box,
  List,
  QuestionFilled,
  CopyDocument,
  Key,
  Connection,
  Setting,
} from '@element-plus/icons-vue'

const activeDoc = ref('quickstart')
const activeTab = ref('curl')

// 错误码表格数据
const errorCodes = [
  { code: '400', message: 'Bad Request', description: '请求参数错误' },
  { code: '401', message: 'Unauthorized', description: 'API Key 无效或已过期' },
  { code: '403', message: 'Forbidden', description: '无权访问该资源' },
  { code: '404', message: 'Not Found', description: '请求的资源不存在' },
  { code: '429', message: 'Too Many Requests', description: '请求频率超限' },
  { code: '500', message: 'Internal Error', description: '服务器内部错误' },
]

// 下一步卡片
const nextSteps = [
  { title: '认证鉴权', description: '了解更多认证方式和安全最佳实践', icon: Key },
  { title: 'API 参考', description: '查看完整的 API 接口文档', icon: Connection },
  { title: '配置应用', description: '配置回调地址、IP白名单等', icon: Setting },
]

// 文档选择
const handleDocSelect = (index: string) => {
  activeDoc.value = index
  // TODO: 加载对应文档内容
}

// 复制代码
const copyCode = () => {
  ElMessage.success('代码已复制到剪贴板')
}
</script>

<style scoped>
.docs-page {
  min-height: calc(100vh - 56px);
  background: #fff;
}

.docs-container {
  display: flex;
  max-width: 1600px;
  margin: 0 auto;
}

/* 侧边栏 */
.docs-sidebar {
  width: 260px;
  border-right: 1px solid #e8e8e8;
  flex-shrink: 0;
  height: calc(100vh - 56px);
  position: sticky;
  top: 56px;
  overflow-y: auto;
}

.sidebar-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.docs-menu {
  border-right: none;
}

/* 主内容区 */
.docs-main {
  flex: 1;
  min-width: 0;
  padding: 40px 60px;
}

.docs-content {
  max-width: 800px;
}

.docs-content h1 {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px;
}

.docs-content h2 {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 40px 0 16px;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

.docs-content h2:first-of-type {
  border-top: none;
  padding-top: 0;
}

.lead {
  font-size: 18px;
  color: #666;
  margin-bottom: 24px;
}

.docs-content p {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 16px;
}

/* 代码块 */
.code-block {
  background: #1e1e1e;
  border-radius: 8px;
  margin-bottom: 24px;
  overflow: hidden;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #2d2d2d;
  color: #999;
  font-size: 13px;
}

.code-block pre {
  margin: 0;
  padding: 16px;
  overflow-x: auto;
}

.code-block code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #d4d4d4;
}

/* 代码标签页 */
.code-tabs {
  margin-bottom: 24px;
}

/* 下一步卡片 */
.next-steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.step-card {
  cursor: pointer;
  transition: all 0.3s;
}

.step-card:hover {
  transform: translateY(-4px);
}

.step-icon {
  width: 48px;
  height: 48px;
  background: #f0f7ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
  margin-bottom: 12px;
}

.step-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.step-card p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 右侧目录 */
.docs-toc {
  width: 200px;
  flex-shrink: 0;
  padding: 40px 24px;
  height: calc(100vh - 56px);
  position: sticky;
  top: 56px;
}

.toc-header {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-list li {
  margin-bottom: 8px;
}

.toc-list a {
  font-size: 13px;
  color: #666;
  text-decoration: none;
  display: block;
  padding: 4px 0;
  border-left: 2px solid transparent;
  padding-left: 12px;
  transition: all 0.2s;
}

.toc-list a:hover {
  color: #1890ff;
}

.toc-list a.active {
  color: #1890ff;
  border-left-color: #1890ff;
}

/* 响应式 */
@media (max-width: 1200px) {
  .docs-toc {
    display: none;
  }
}

@media (max-width: 768px) {
  .docs-sidebar {
    display: none;
  }

  .docs-main {
    padding: 24px 16px;
  }

  .next-steps {
    grid-template-columns: 1fr;
  }
}
</style>
