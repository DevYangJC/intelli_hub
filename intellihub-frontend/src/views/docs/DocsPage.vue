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
          <!-- 快速开始 -->
          <template v-if="activeDoc === 'quickstart'">
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

            <h2 id="get-api-key">1. 获取 API Key</h2>
            <p>登录控制台，进入「应用管理」-「密钥管理」，创建新的 API Key。</p>
            
            <div class="code-block">
              <div class="code-header">
                <span>API Key 格式</span>
                <el-button type="text" size="small" @click="copyCode('API-KEY: ihub_sk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx')">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>
              <pre><code>API-KEY: ihub_sk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</code></pre>
            </div>

            <h2 id="first-request">2. 发起第一个请求</h2>
            <p>所有 API 请求都需要在 Header 中携带认证信息。</p>

            <el-tabs v-model="activeTab" class="code-tabs">
              <el-tab-pane label="cURL" name="curl">
                <div class="code-block">
                  <pre><code>curl -X GET "https://api.intellihub.com/open/v1/users/me" \
  -H "X-App-Key: your_app_key" \
  -H "X-Timestamp: 1704067200" \
  -H "X-Nonce: random_string" \
  -H "X-Signature: signature_hash" \
  -H "Content-Type: application/json"</code></pre>
                </div>
              </el-tab-pane>
              <el-tab-pane label="JavaScript" name="js">
                <div class="code-block">
                  <pre><code>import CryptoJS from 'crypto-js';

const appKey = 'your_app_key';
const appSecret = 'your_app_secret';
const timestamp = Math.floor(Date.now() / 1000).toString();
const nonce = Math.random().toString(36).substring(2, 15);

// 生成签名
const signString = `${appKey}${timestamp}${nonce}${appSecret}`;
const signature = CryptoJS.SHA256(signString).toString();

const response = await fetch('https://api.intellihub.com/open/v1/users/me', {
  method: 'GET',
  headers: {
    'X-App-Key': appKey,
    'X-Timestamp': timestamp,
    'X-Nonce': nonce,
    'X-Signature': signature,
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
import hashlib
import time
import uuid

app_key = 'your_app_key'
app_secret = 'your_app_secret'
timestamp = str(int(time.time()))
nonce = str(uuid.uuid4())[:16]

# 生成签名
sign_string = f'{app_key}{timestamp}{nonce}{app_secret}'
signature = hashlib.sha256(sign_string.encode()).hexdigest()

response = requests.get(
    'https://api.intellihub.com/open/v1/users/me',
    headers={
        'X-App-Key': app_key,
        'X-Timestamp': timestamp,
        'X-Nonce': nonce,
        'X-Signature': signature,
        'Content-Type': 'application/json'
    }
)

print(response.json())</code></pre>
                </div>
              </el-tab-pane>
            </el-tabs>

            <h2 id="response-format">3. 响应格式</h2>
            <p>API 返回统一的 JSON 格式响应：</p>

            <div class="code-block">
              <pre><code>{
  "code": 200,
  "message": "success",
  "data": {
    "id": "user_123",
    "username": "demo_user",
    "email": "demo@example.com",
    "created_at": "2024-01-15T10:30:00Z"
  },
  "requestId": "req_abc123"
}</code></pre>
            </div>

            <h2 id="error-handling">4. 错误处理</h2>
            <p>当请求失败时，API 会返回相应的错误码和错误信息：</p>

            <el-table :data="errorCodes" style="width: 100%">
              <el-table-column prop="code" label="错误码" width="120" />
              <el-table-column prop="message" label="错误信息" width="200" />
              <el-table-column prop="description" label="说明" />
            </el-table>

            <h2 id="next-steps">5. 下一步</h2>
            <div class="next-steps">
              <el-card v-for="step in nextSteps" :key="step.title" class="step-card" shadow="hover" @click="handleDocSelect(step.link)">
                <div class="step-icon">
                  <el-icon :size="24"><component :is="step.icon" /></el-icon>
                </div>
                <h4>{{ step.title }}</h4>
                <p>{{ step.description }}</p>
              </el-card>
            </div>
          </template>

          <!-- 认证鉴权 -->
          <template v-else-if="activeDoc === 'guide-auth'">
            <h1>认证鉴权</h1>
            <p class="lead">了解 IntelliHub API 的认证机制和安全最佳实践。</p>

            <h2>认证方式</h2>
            <p>IntelliHub API 使用签名认证方式，每个请求需要在 Header 中携带以下信息：</p>

            <el-table :data="authHeaders" style="width: 100%; margin-bottom: 24px">
              <el-table-column prop="header" label="Header" width="150" />
              <el-table-column prop="required" label="必填" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.required ? 'danger' : 'info'" size="small">{{ row.required ? '是' : '否' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" />
            </el-table>

            <h2>签名算法</h2>
            <p>签名使用 SHA256 算法生成，签名字符串格式为：</p>
            <div class="code-block">
              <pre><code>signString = appKey + timestamp + nonce + appSecret
signature = SHA256(signString)</code></pre>
            </div>

            <h2>签名示例</h2>
            <el-tabs v-model="activeTab" class="code-tabs">
              <el-tab-pane label="Java" name="java">
                <div class="code-block">
                  <pre><code>import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SignatureUtil {
    public static String generateSignature(String appKey, String timestamp, 
                                           String nonce, String appSecret) {
        String signString = appKey + timestamp + nonce + appSecret;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}</code></pre>
                </div>
              </el-tab-pane>
              <el-tab-pane label="Python" name="python">
                <div class="code-block">
                  <pre><code>import hashlib

def generate_signature(app_key: str, timestamp: str, nonce: str, app_secret: str) -> str:
    sign_string = f'{app_key}{timestamp}{nonce}{app_secret}'
    return hashlib.sha256(sign_string.encode()).hexdigest()</code></pre>
                </div>
              </el-tab-pane>
              <el-tab-pane label="Go" name="go">
                <div class="code-block">
                  <pre><code>import (
    "crypto/sha256"
    "encoding/hex"
)

func GenerateSignature(appKey, timestamp, nonce, appSecret string) string {
    signString := appKey + timestamp + nonce + appSecret
    hash := sha256.Sum256([]byte(signString))
    return hex.EncodeToString(hash[:])
}</code></pre>
                </div>
              </el-tab-pane>
            </el-tabs>

            <h2>安全建议</h2>
            <ul class="doc-list">
              <li><strong>保护密钥</strong>：AppSecret 应妥善保管，不要在客户端代码中暴露</li>
              <li><strong>使用 HTTPS</strong>：所有 API 调用必须使用 HTTPS 协议</li>
              <li><strong>IP 白名单</strong>：建议配置 IP 白名单，限制 API 调用来源</li>
              <li><strong>定期轮换</strong>：定期更换 AppSecret，降低密钥泄露风险</li>
            </ul>
          </template>

          <!-- 请求规范 -->
          <template v-else-if="activeDoc === 'guide-request'">
            <h1>请求规范</h1>
            <p class="lead">了解如何正确构造 API 请求。</p>

            <h2>基础信息</h2>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="Base URL">https://api.intellihub.com</el-descriptions-item>
              <el-descriptions-item label="协议">HTTPS</el-descriptions-item>
              <el-descriptions-item label="编码">UTF-8</el-descriptions-item>
              <el-descriptions-item label="格式">JSON</el-descriptions-item>
            </el-descriptions>

            <h2>请求方法</h2>
            <el-table :data="httpMethods" style="width: 100%; margin-bottom: 24px">
              <el-table-column prop="method" label="方法" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.type" size="small">{{ row.method }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="usage" label="用途" />
              <el-table-column prop="example" label="示例" />
            </el-table>

            <h2>请求头</h2>
            <div class="code-block">
              <pre><code>Content-Type: application/json
Accept: application/json
X-App-Key: your_app_key
X-Timestamp: 1704067200
X-Nonce: random_string_16
X-Signature: sha256_hash</code></pre>
            </div>

            <h2>请求体</h2>
            <p>对于 POST、PUT、PATCH 请求，请求体使用 JSON 格式：</p>
            <div class="code-block">
              <pre><code>{
  "name": "示例名称",
  "description": "示例描述",
  "config": {
    "timeout": 30,
    "retries": 3
  }
}</code></pre>
            </div>

            <h2>分页参数</h2>
            <p>列表接口支持分页查询，使用以下参数：</p>
            <el-table :data="paginationParams" style="width: 100%">
              <el-table-column prop="param" label="参数" width="120" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column prop="default" label="默认值" width="100" />
              <el-table-column prop="description" label="说明" />
            </el-table>
          </template>

          <!-- 响应格式 -->
          <template v-else-if="activeDoc === 'guide-response'">
            <h1>响应格式</h1>
            <p class="lead">了解 API 响应的标准格式。</p>

            <h2>响应结构</h2>
            <p>所有 API 响应都采用统一的 JSON 格式：</p>
            <div class="code-block">
              <pre><code>{
  "code": 200,           // 业务状态码
  "message": "success",  // 状态信息
  "data": { ... },       // 响应数据
  "requestId": "..."     // 请求追踪ID
}</code></pre>
            </div>

            <h2>状态码说明</h2>
            <el-table :data="statusCodes" style="width: 100%; margin-bottom: 24px">
              <el-table-column prop="code" label="状态码" width="120" />
              <el-table-column prop="meaning" label="含义" width="150" />
              <el-table-column prop="description" label="说明" />
            </el-table>

            <h2>分页响应</h2>
            <p>列表接口的分页响应格式：</p>
            <div class="code-block">
              <pre><code>{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],    // 数据列表
    "total": 100,        // 总记录数
    "pageNum": 1,        // 当前页码
    "pageSize": 10,      // 每页条数
    "pages": 10          // 总页数
  }
}</code></pre>
            </div>
          </template>

          <!-- 错误处理 -->
          <template v-else-if="activeDoc === 'guide-error'">
            <h1>错误处理</h1>
            <p class="lead">了解如何处理 API 返回的错误。</p>

            <h2>错误响应格式</h2>
            <div class="code-block">
              <pre><code>{
  "code": 400,
  "message": "参数校验失败",
  "errors": [
    {
      "field": "name",
      "message": "名称不能为空"
    }
  ],
  "requestId": "req_abc123"
}</code></pre>
            </div>

            <h2>HTTP 状态码</h2>
            <el-table :data="httpStatusCodes" style="width: 100%; margin-bottom: 24px">
              <el-table-column prop="code" label="状态码" width="100" />
              <el-table-column prop="name" label="名称" width="180" />
              <el-table-column prop="description" label="说明" />
            </el-table>

            <h2>业务错误码</h2>
            <el-table :data="businessErrorCodes" style="width: 100%">
              <el-table-column prop="code" label="错误码" width="120" />
              <el-table-column prop="message" label="错误信息" width="200" />
              <el-table-column prop="solution" label="解决方案" />
            </el-table>

            <h2>错误处理建议</h2>
            <ul class="doc-list">
              <li><strong>记录请求ID</strong>：保存 requestId 便于问题排查</li>
              <li><strong>重试机制</strong>：对于 5xx 错误，建议实现指数退避重试</li>
              <li><strong>优雅降级</strong>：API 不可用时提供备用方案</li>
              <li><strong>监控告警</strong>：监控错误率，及时发现问题</li>
            </ul>
          </template>

          <!-- SDK 文档 -->
          <template v-else-if="activeDoc.startsWith('sdk-')">
            <h1>{{ sdkDocs[activeDoc]?.title || 'SDK 文档' }}</h1>
            <p class="lead">{{ sdkDocs[activeDoc]?.description || '' }}</p>

            <h2>安装</h2>
            <div class="code-block">
              <div class="code-header">
                <span>{{ sdkDocs[activeDoc]?.installTitle || '安装命令' }}</span>
                <el-button type="text" size="small" @click="copyCode(sdkDocs[activeDoc]?.installCmd || '')">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>
              <pre><code>{{ sdkDocs[activeDoc]?.installCmd || '' }}</code></pre>
            </div>

            <h2>快速开始</h2>
            <div class="code-block">
              <pre><code>{{ sdkDocs[activeDoc]?.quickStart || '' }}</code></pre>
            </div>

            <h2>更多示例</h2>
            <div class="code-block">
              <pre><code>{{ sdkDocs[activeDoc]?.examples || '' }}</code></pre>
            </div>
          </template>

          <!-- 更新日志 -->
          <template v-else-if="activeDoc === 'changelog'">
            <h1>更新日志</h1>
            <p class="lead">IntelliHub API 平台版本更新记录。</p>

            <el-timeline>
              <el-timeline-item v-for="log in changelogData" :key="log.version" :timestamp="log.date" placement="top">
                <el-card>
                  <template #header>
                    <div class="changelog-header">
                      <span class="version">{{ log.version }}</span>
                      <el-tag :type="log.type" size="small">{{ log.typeText }}</el-tag>
                    </div>
                  </template>
                  <ul class="changelog-list">
                    <li v-for="(item, index) in log.changes" :key="index">{{ item }}</li>
                  </ul>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </template>

          <!-- 常见问题 -->
          <template v-else-if="activeDoc === 'faq'">
            <h1>常见问题</h1>
            <p class="lead">解答开发者常见的问题。</p>

            <el-collapse v-model="activeFaq" accordion>
              <el-collapse-item v-for="faq in faqData" :key="faq.id" :title="faq.question" :name="faq.id">
                <div class="faq-answer" v-html="faq.answer"></div>
              </el-collapse-item>
            </el-collapse>
          </template>

          <!-- 默认内容 -->
          <template v-else>
            <div class="empty-doc">
              <el-empty description="文档内容正在完善中..." />
            </div>
          </template>
        </div>
      </main>

      <!-- 右侧目录 -->
      <aside class="docs-toc" v-if="currentToc.length > 0">
        <div class="toc-header">目录</div>
        <ul class="toc-list">
          <li v-for="item in currentToc" :key="item.id">
            <a :href="'#' + item.id" :class="{ active: activeTocItem === item.id }">{{ item.title }}</a>
          </li>
        </ul>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
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
const activeFaq = ref('1')
const activeTocItem = ref('')

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
  { title: '认证鉴权', description: '了解更多认证方式和安全最佳实践', icon: Key, link: 'guide-auth' },
  { title: 'SDK 文档', description: '使用 SDK 快速集成', icon: Box, link: 'sdk-java' },
  { title: '常见问题', description: '查看开发者常见问题', icon: QuestionFilled, link: 'faq' },
]

// 认证请求头
const authHeaders = [
  { header: 'X-App-Key', required: true, description: '应用的 AppKey，在控制台应用管理中获取' },
  { header: 'X-Timestamp', required: true, description: '请求时间戳（秒级），与服务器时间误差不超过5分钟' },
  { header: 'X-Nonce', required: true, description: '随机字符串，用于防重放攻击，建议16位以上' },
  { header: 'X-Signature', required: true, description: 'SHA256签名，签名算法见下文' },
]

// HTTP 方法
const httpMethods = [
  { method: 'GET', type: 'success', usage: '获取资源', example: 'GET /api/users/123' },
  { method: 'POST', type: 'primary', usage: '创建资源', example: 'POST /api/users' },
  { method: 'PUT', type: 'warning', usage: '更新资源（全量）', example: 'PUT /api/users/123' },
  { method: 'PATCH', type: 'info', usage: '更新资源（部分）', example: 'PATCH /api/users/123' },
  { method: 'DELETE', type: 'danger', usage: '删除资源', example: 'DELETE /api/users/123' },
]

// 分页参数
const paginationParams = [
  { param: 'pageNum', type: 'Integer', default: '1', description: '页码，从1开始' },
  { param: 'pageSize', type: 'Integer', default: '10', description: '每页条数，最大100' },
  { param: 'orderBy', type: 'String', default: '-', description: '排序字段' },
  { param: 'orderDir', type: 'String', default: 'desc', description: '排序方向：asc/desc' },
]

// 状态码说明
const statusCodes = [
  { code: '200', meaning: '成功', description: '请求成功处理' },
  { code: '201', meaning: '已创建', description: '资源创建成功' },
  { code: '204', meaning: '无内容', description: '请求成功但无返回内容' },
  { code: '400', meaning: '请求错误', description: '请求参数有误' },
  { code: '401', meaning: '未授权', description: '认证失败或未提供认证信息' },
  { code: '403', meaning: '禁止访问', description: '无权访问该资源' },
  { code: '404', meaning: '未找到', description: '请求的资源不存在' },
  { code: '500', meaning: '服务器错误', description: '服务器内部错误' },
]

// HTTP 状态码
const httpStatusCodes = [
  { code: '400', name: 'Bad Request', description: '请求参数错误，检查请求体格式和必填字段' },
  { code: '401', name: 'Unauthorized', description: '认证失败，检查 AppKey 和签名是否正确' },
  { code: '403', name: 'Forbidden', description: '无权限，检查应用是否已订阅该 API' },
  { code: '404', name: 'Not Found', description: '资源不存在，检查请求路径是否正确' },
  { code: '429', name: 'Too Many Requests', description: '请求频率超限，稍后重试' },
  { code: '500', name: 'Internal Server Error', description: '服务器内部错误，联系技术支持' },
]

// 业务错误码
const businessErrorCodes = [
  { code: '10001', message: '参数校验失败', solution: '检查请求参数是否符合要求' },
  { code: '10002', message: '签名验证失败', solution: '检查签名算法和参数顺序' },
  { code: '10003', message: '时间戳过期', solution: '确保请求时间戳与服务器时间误差在5分钟内' },
  { code: '10004', message: 'Nonce已使用', solution: '每次请求使用不同的Nonce值' },
  { code: '20001', message: '应用不存在', solution: '检查 AppKey 是否正确' },
  { code: '20002', message: '应用已禁用', solution: '联系管理员启用应用' },
  { code: '30001', message: 'API不存在', solution: '检查请求的API路径是否正确' },
  { code: '30002', message: '未订阅该API', solution: '在控制台订阅该API后重试' },
]

// SDK 文档数据
const sdkDocs: Record<string, { title: string; description: string; installTitle: string; installCmd: string; quickStart: string; examples: string }> = {
  'sdk-java': {
    title: 'Java SDK',
    description: '适用于 Java 8+ 的官方 SDK，支持 Spring Boot 集成。',
    installTitle: 'Maven 依赖',
    installCmd: `<dependency>
    <groupId>com.intellihub</groupId>
    <artifactId>intellihub-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>`,
    quickStart: `import com.intellihub.sdk.IntelliHubClient;
import com.intellihub.sdk.model.ApiResponse;

public class Example {
    public static void main(String[] args) {
        IntelliHubClient client = IntelliHubClient.builder()
            .appKey("your_app_key")
            .appSecret("your_app_secret")
            .build();
        
        ApiResponse response = client.get("/open/v1/users/me");
        System.out.println(response.getData());
    }
}`,
    examples: `// POST 请求示例
Map<String, Object> body = new HashMap<>();
body.put("name", "test");
ApiResponse response = client.post("/open/v1/resources", body);

// 带查询参数的 GET 请求
Map<String, String> params = new HashMap<>();
params.put("pageNum", "1");
params.put("pageSize", "10");
ApiResponse response = client.get("/open/v1/resources", params);`
  },
  'sdk-python': {
    title: 'Python SDK',
    description: '适用于 Python 3.7+ 的官方 SDK，支持异步调用。',
    installTitle: 'pip 安装',
    installCmd: 'pip install intellihub-sdk',
    quickStart: `from intellihub import IntelliHubClient

client = IntelliHubClient(
    app_key='your_app_key',
    app_secret='your_app_secret'
)

response = client.get('/open/v1/users/me')
print(response.data)`,
    examples: `# POST 请求示例
response = client.post('/open/v1/resources', data={
    'name': 'test',
    'description': '示例'
})

# 异步调用
import asyncio
from intellihub import AsyncIntelliHubClient

async def main():
    async with AsyncIntelliHubClient(app_key, app_secret) as client:
        response = await client.get('/open/v1/users/me')
        print(response.data)

asyncio.run(main())`
  },
  'sdk-nodejs': {
    title: 'Node.js SDK',
    description: '适用于 Node.js 14+ 的官方 SDK，支持 TypeScript。',
    installTitle: 'npm 安装',
    installCmd: 'npm install @intellihub/sdk',
    quickStart: `const { IntelliHubClient } = require('@intellihub/sdk');

const client = new IntelliHubClient({
  appKey: 'your_app_key',
  appSecret: 'your_app_secret'
});

const response = await client.get('/open/v1/users/me');
console.log(response.data);`,
    examples: `// POST 请求示例
const response = await client.post('/open/v1/resources', {
  name: 'test',
  description: '示例'
});

// TypeScript 示例
import { IntelliHubClient, ApiResponse } from '@intellihub/sdk';

const client = new IntelliHubClient({ appKey, appSecret });
const response: ApiResponse<User> = await client.get<User>('/open/v1/users/me');`
  },
  'sdk-go': {
    title: 'Go SDK',
    description: '适用于 Go 1.18+ 的官方 SDK。',
    installTitle: 'go get 安装',
    installCmd: 'go get github.com/intellihub/intellihub-go-sdk',
    quickStart: `package main

import (
    "fmt"
    intellihub "github.com/intellihub/intellihub-go-sdk"
)

func main() {
    client := intellihub.NewClient(
        intellihub.WithAppKey("your_app_key"),
        intellihub.WithAppSecret("your_app_secret"),
    )
    
    resp, err := client.Get("/open/v1/users/me")
    if err != nil {
        panic(err)
    }
    fmt.Println(resp.Data)
}`,
    examples: `// POST 请求示例
body := map[string]interface{}{
    "name": "test",
    "description": "示例",
}
resp, err := client.Post("/open/v1/resources", body)

// 带查询参数的 GET 请求
params := url.Values{}
params.Set("pageNum", "1")
params.Set("pageSize", "10")
resp, err := client.GetWithParams("/open/v1/resources", params)`
  }
}

// 更新日志数据
const changelogData = [
  {
    version: 'v1.2.0',
    date: '2024-01-15',
    type: 'success',
    typeText: '新功能',
    changes: [
      '新增 API 市场功能，支持 API 订阅',
      '新增 SDK 文档，支持 Java/Python/Node.js/Go',
      '优化签名算法，提升安全性',
      '新增 IP 白名单功能'
    ]
  },
  {
    version: 'v1.1.0',
    date: '2023-12-01',
    type: 'primary',
    typeText: '优化',
    changes: [
      '优化 API 响应时间',
      '增加请求限流功能',
      '完善错误码体系',
      '修复若干已知问题'
    ]
  },
  {
    version: 'v1.0.0',
    date: '2023-10-15',
    type: 'info',
    typeText: '发布',
    changes: [
      'IntelliHub API 平台正式发布',
      '支持 API 管理、应用管理',
      '支持签名认证',
      '提供开发文档'
    ]
  }
]

// 常见问题数据
const faqData = [
  {
    id: '1',
    question: '如何获取 AppKey 和 AppSecret？',
    answer: '登录控制台，进入「应用管理」页面，点击「创建应用」按钮，填写应用信息后即可获取 AppKey 和 AppSecret。<br/><br/>注意：AppSecret 只在创建时显示一次，请妥善保管。'
  },
  {
    id: '2',
    question: '签名验证一直失败怎么办？',
    answer: '请检查以下几点：<br/>1. 确认签名字符串拼接顺序：appKey + timestamp + nonce + appSecret<br/>2. 确认时间戳为秒级（10位数字）<br/>3. 确认时间戳与服务器时间误差在5分钟内<br/>4. 确认使用 SHA256 算法，输出为小写十六进制'
  },
  {
    id: '3',
    question: '如何提高 API 调用限额？',
    answer: '默认限额为每分钟 60 次请求。如需提高限额，请联系平台管理员或提交工单申请。'
  },
  {
    id: '4',
    question: 'API 返回 403 错误怎么处理？',
    answer: '403 错误表示无权访问。请检查：<br/>1. 应用是否已订阅该 API<br/>2. 应用状态是否正常（未被禁用）<br/>3. 如配置了 IP 白名单，检查请求 IP 是否在白名单中'
  },
  {
    id: '5',
    question: '如何处理请求超时？',
    answer: '建议：<br/>1. 设置合理的超时时间（推荐 30 秒）<br/>2. 实现重试机制，使用指数退避策略<br/>3. 对于长时间运行的操作，使用异步接口'
  }
]

// 当前文档的目录
const currentToc = computed(() => {
  const tocMap: Record<string, { id: string; title: string }[]> = {
    'quickstart': [
      { id: 'get-api-key', title: '1. 获取 API Key' },
      { id: 'first-request', title: '2. 发起第一个请求' },
      { id: 'response-format', title: '3. 响应格式' },
      { id: 'error-handling', title: '4. 错误处理' },
      { id: 'next-steps', title: '5. 下一步' }
    ]
  }
  return tocMap[activeDoc.value] || []
})

// 文档选择
const handleDocSelect = (index: string) => {
  activeDoc.value = index
}

// 复制代码
const copyCode = (code: string) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('代码已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
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

/* 文档列表 */
.doc-list {
  list-style: none;
  padding: 0;
  margin: 16px 0;
}

.doc-list li {
  padding: 12px 16px;
  margin-bottom: 8px;
  background: #f9f9f9;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.doc-list li strong {
  color: #1a1a1a;
}

/* 更新日志 */
.changelog-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.changelog-header .version {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.changelog-list {
  list-style: disc;
  padding-left: 20px;
  margin: 0;
}

.changelog-list li {
  padding: 4px 0;
  font-size: 14px;
  color: #666;
}

/* 常见问题 */
.faq-answer {
  font-size: 14px;
  line-height: 1.8;
  color: #666;
  padding: 8px 0;
}

/* 空文档 */
.empty-doc {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
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
