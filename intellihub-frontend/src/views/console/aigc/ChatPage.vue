<template>
  <div class="chat-page">
    <!-- 顶部工具栏 -->
    <div class="chat-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><ChatDotRound /></el-icon>
          AI 智能对话
        </h2>
      </div>
      <div class="header-right">
        <el-select v-model="selectedModel" placeholder="选择模型" style="width: 200px">
          <el-option
            v-for="model in models"
            :key="model"
            :label="getModelLabel(model)"
            :value="model"
          />
        </el-select>
        <el-button @click="handleClearHistory" :disabled="!conversationId">
          <el-icon><Delete /></el-icon>
          清空对话
        </el-button>
      </div>
    </div>

    <!-- 配额提示 -->
    <div class="quota-bar" v-if="quotaUsage">
      <div class="quota-info">
        <span>今日配额：</span>
        <el-progress
          :percentage="quotaUsage.usagePercent"
          :status="quotaUsage.usagePercent > 90 ? 'exception' : quotaUsage.usagePercent > 70 ? 'warning' : ''"
          :stroke-width="10"
          style="width: 200px"
        />
        <span class="quota-text">
          {{ formatNumber(quotaUsage.usedQuota) }} / {{ formatNumber(quotaUsage.dailyQuota) }} Token
        </span>
      </div>
    </div>

    <!-- 对话区域 -->
    <div class="chat-container" ref="chatContainer">
      <div class="chat-messages">
        <!-- 欢迎消息 -->
        <div class="welcome-message" v-if="messages.length === 0">
          <div class="welcome-icon">
            <el-icon :size="48"><MagicStick /></el-icon>
          </div>
          <h3>欢迎使用 IntelliHub AI 助手</h3>
          <p>我可以帮你：代码生成、问题解答、文本创作、数据分析等</p>
          <div class="quick-prompts">
            <el-tag
              v-for="prompt in quickPrompts"
              :key="prompt"
              @click="handleQuickPrompt(prompt)"
              class="quick-prompt-tag"
            >
              {{ prompt }}
            </el-tag>
          </div>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
        >
          <div class="message-avatar">
            <el-avatar :size="36" :class="msg.role">
              <el-icon v-if="msg.role === 'user'"><User /></el-icon>
              <el-icon v-else><MagicStick /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">{{ msg.role === 'user' ? '你' : 'AI 助手' }}</span>
              <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
            </div>
            <div class="message-body" v-html="renderMarkdown(msg.content)"></div>
            <!-- AI消息操作按钮 -->
            <div class="message-actions" v-if="msg.role === 'assistant'">
              <el-button size="small" text @click="copyMessage(msg.content)">
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
            </div>
          </div>
        </div>

        <!-- 加载中 -->
        <div class="message assistant" v-if="isLoading">
          <div class="message-avatar">
            <el-avatar :size="36" class="assistant">
              <el-icon><MagicStick /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">AI 助手</span>
            </div>
            <div class="message-body typing">
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
            </div>
          </div>
        </div>

        <!-- 流式响应 -->
        <div class="message assistant" v-if="streamingContent">
          <div class="message-avatar">
            <el-avatar :size="36" class="assistant">
              <el-icon><MagicStick /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">AI 助手</span>
              <span class="streaming-indicator">
                <el-icon class="is-loading"><Loading /></el-icon>
                生成中...
              </span>
            </div>
            <div class="message-body" v-html="renderMarkdown(streamingContent)"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          :autosize="{ minRows: 2, maxRows: 6 }"
          placeholder="输入消息，按 Enter 发送，Shift+Enter 换行..."
          @keydown="handleKeydown"
          :disabled="isLoading || isStreaming"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          :loading="isLoading || isStreaming"
          @click="handleSend"
          :disabled="!inputMessage.trim()"
          class="send-button"
        >
          发送
        </el-button>
      </div>
      <div class="input-tips">
        <span>当前模型：{{ getModelLabel(selectedModel) }}</span>
        <span v-if="lastTokens">上次消耗：{{ lastTokens }} Token</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import {
  ChatDotRound,
  Delete,
  User,
  MagicStick,
  CopyDocument,
  Loading,
  Promotion
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSupportedModels,
  getQuotaUsage,
  chat,
  streamChat,
  clearConversationHistory,
  type QuotaUsage,
  type ChatMessage
} from '@/api/aigc'

// 消息类型
interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

// 状态
const messages = ref<Message[]>([])
const inputMessage = ref('')
const selectedModel = ref('qwen-turbo')
const models = ref<string[]>([])
const isLoading = ref(false)
const isStreaming = ref(false)
const streamingContent = ref('')
const conversationId = ref('')
const quotaUsage = ref<QuotaUsage | null>(null)
const lastTokens = ref(0)
const chatContainer = ref<HTMLElement | null>(null)

// 快捷提示
const quickPrompts = [
  '帮我写一段Java代码',
  '解释什么是微服务架构',
  '写一篇技术博客',
  '优化这段SQL查询'
]

// 模型标签映射
const modelLabels: Record<string, string> = {
  'qwen-turbo': '通义千问 Turbo',
  'qwen-plus': '通义千问 Plus',
  'qwen-max': '通义千问 Max',
  'qwen-max-longcontext': '通义千问 Max 长文本',
  'ernie-bot-turbo': '文心一言 Turbo',
  'ernie-bot': '文心一言',
  'ernie-bot-4': '文心一言 4.0',
  'hunyuan-lite': '腾讯混元 Lite',
  'hunyuan-standard': '腾讯混元 Standard',
  'hunyuan-pro': '腾讯混元 Pro'
}

// 获取模型标签
const getModelLabel = (model: string) => modelLabels[model] || model

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 渲染Markdown（简化版本）
const renderMarkdown = (content: string) => {
  if (!content) return ''
  
  // 代码块
  let html = content.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    return `<pre class="code-block"><code class="language-${lang}">${escapeHtml(code.trim())}</code></pre>`
  })
  
  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  
  // 加粗
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  
  // 斜体
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  
  // 换行
  html = html.replace(/\n/g, '<br>')
  
  return html
}

// HTML转义
const escapeHtml = (text: string) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 加载模型列表
const loadModels = async () => {
  try {
    const res = await getSupportedModels()
    if (res.code === 200 && res.data) {
      models.value = res.data
    }
  } catch (error) {
    console.error('加载模型列表失败', error)
    // 使用默认列表
    models.value = Object.keys(modelLabels)
  }
}

// 加载配额
const loadQuota = async () => {
  try {
    const res = await getQuotaUsage()
    if (res.code === 200 && res.data) {
      quotaUsage.value = res.data
    }
  } catch (error) {
    console.error('加载配额失败', error)
  }
}

// 发送消息
const handleSend = async () => {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value || isStreaming.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    timestamp: new Date()
  })
  inputMessage.value = ''
  scrollToBottom()

  // 生成会话ID
  if (!conversationId.value) {
    conversationId.value = crypto.randomUUID()
  }

  // 使用流式响应
  isStreaming.value = true
  streamingContent.value = ''

  try {
    await streamChat(
      {
        message,
        model: selectedModel.value,
        conversationId: conversationId.value,
        history: messages.value.slice(-10).map(m => ({
          role: m.role,
          content: m.content
        })) as ChatMessage[]
      },
      (text) => {
        streamingContent.value += text
        scrollToBottom()
      },
      (error) => {
        ElMessage.error('生成失败: ' + error.message)
        isStreaming.value = false
        streamingContent.value = ''
      },
      () => {
        // 完成后添加到消息列表
        if (streamingContent.value) {
          messages.value.push({
            role: 'assistant',
            content: streamingContent.value,
            timestamp: new Date()
          })
        }
        streamingContent.value = ''
        isStreaming.value = false
        loadQuota() // 刷新配额
        scrollToBottom()
      }
    )
  } catch (error) {
    // 降级到非流式
    isStreaming.value = false
    isLoading.value = true
    
    try {
      const res = await chat({
        message,
        model: selectedModel.value,
        conversationId: conversationId.value
      })
      
      if (res.code === 200 && res.data) {
        messages.value.push({
          role: 'assistant',
          content: res.data.message,
          timestamp: new Date()
        })
        lastTokens.value = res.data.tokensUsed
        loadQuota()
      } else {
        ElMessage.error(res.message || '请求失败')
      }
    } catch (err) {
      ElMessage.error('请求失败，请稍后重试')
    } finally {
      isLoading.value = false
      scrollToBottom()
    }
  }
}

// 键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 快捷提示
const handleQuickPrompt = (prompt: string) => {
  inputMessage.value = prompt
  handleSend()
}

// 清空历史
const handleClearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前对话历史吗？', '提示', {
      type: 'warning'
    })
    
    if (conversationId.value) {
      await clearConversationHistory(conversationId.value)
    }
    
    messages.value = []
    conversationId.value = ''
    ElMessage.success('对话已清空')
  } catch {
    // 取消
  }
}

// 复制消息
const copyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

// 监听消息变化自动滚动
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

onMounted(() => {
  loadModels()
  loadQuota()
})
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 配额条 */
.quota-bar {
  padding: 8px 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.quota-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #606266;
}

.quota-text {
  color: #909399;
}

/* 对话容器 */
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-messages {
  max-width: 900px;
  margin: 0 auto;
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: #606266;
}

.welcome-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.welcome-message h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #303133;
}

.welcome-message p {
  margin: 0 0 24px;
  color: #909399;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-prompt-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.quick-prompt-tag:hover {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

/* 消息 */
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar .el-avatar {
  background: #e6f4ff;
  color: #409eff;
}

.message-avatar .el-avatar.assistant {
  background: #f0f9eb;
  color: #67c23a;
}

.message-content {
  max-width: 75%;
  min-width: 100px;
}

.message.user .message-content {
  align-items: flex-end;
}

.message-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
}

.message.user .message-header {
  flex-direction: row-reverse;
}

.message-role {
  font-weight: 500;
  color: #303133;
}

.message-time {
  color: #909399;
  font-size: 12px;
}

.message-body {
  padding: 12px 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  line-height: 1.6;
  word-break: break-word;
}

.message.user .message-body {
  background: #409eff;
  color: #fff;
}

.message-actions {
  margin-top: 8px;
}

/* 代码块样式 */
:deep(.code-block) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
}

:deep(.inline-code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: #e83e8c;
}

.message.user :deep(.inline-code) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 4px;
  padding: 16px !important;
}

.typing-dot {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

/* 流式指示器 */
.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
  font-size: 12px;
}

/* 输入区域 */
.chat-input-area {
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.input-wrapper {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper .el-textarea {
  flex: 1;
}

.send-button {
  height: 40px;
  padding: 0 24px;
}

.input-tips {
  max-width: 900px;
  margin: 8px auto 0;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}
</style>
