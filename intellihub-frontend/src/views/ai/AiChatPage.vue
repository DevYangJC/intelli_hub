<template>
  <div class="ai-chat-page">
    <!-- 左侧会话历史 -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" class="new-chat-btn" @click="handleNewChat">
          <el-icon><Plus /></el-icon>
          新建对话
        </el-button>
      </div>
      
      <div class="history-list">
        <div
          v-for="session in chatSessions"
          :key="session.id"
          :class="['history-item', { active: session.id === currentSessionId }]"
          @click="selectSession(session)"
        >
          <el-icon class="history-icon"><ChatDotRound /></el-icon>
          <span class="history-title">{{ session.title }}</span>
          <el-dropdown trigger="click" @command="(cmd: string) => handleSessionCommand(cmd, session)">
            <el-button text size="small" class="history-more" @click.stop>
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="rename">重命名</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <el-empty v-if="chatSessions.length === 0" description="暂无对话" :image-size="60" />
      </div>

      <!-- 配额信息 -->
      <div class="quota-section">
        <div class="quota-header">
          <span>今日配额</span>
          <span class="quota-percent">{{ quotaUsage.usagePercent.toFixed(0) }}%</span>
        </div>
        <el-progress
          :percentage="quotaUsage.usagePercent"
          :stroke-width="6"
          :show-text="false"
          :status="quotaUsage.usagePercent > 90 ? 'exception' : quotaUsage.usagePercent > 70 ? 'warning' : ''"
        />
        <div class="quota-detail">
          {{ formatNumber(quotaUsage.usedQuota) }} / {{ formatNumber(quotaUsage.dailyQuota) }} Token
        </div>
      </div>
    </aside>

    <!-- 右侧对话区 -->
    <main class="chat-main">
      <!-- 顶部工具栏 -->
      <div class="chat-toolbar">
        <div class="toolbar-left">
          <h3 class="chat-title">{{ currentSession?.title || 'AI 智能对话' }}</h3>
        </div>
        <div class="toolbar-right">
          <el-select v-model="selectedModel" placeholder="选择模型" size="default" class="model-select">
            <el-option-group label="阿里通义千问">
              <el-option
                v-for="model in modelGroups.aliyun"
                :key="model.id"
                :label="model.name"
                :value="model.id"
              />
            </el-option-group>
            <el-option-group label="百度文心一言">
              <el-option
                v-for="model in modelGroups.baidu"
                :key="model.id"
                :label="model.name"
                :value="model.id"
              />
            </el-option-group>
            <el-option-group label="腾讯混元">
              <el-option
                v-for="model in modelGroups.tencent"
                :key="model.id"
                :label="model.name"
                :value="model.id"
              />
            </el-option-group>
          </el-select>
          <el-button @click="handleClearHistory" :disabled="messages.length === 0">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </div>
      </div>

      <!-- 对话消息区 -->
      <div class="chat-messages" ref="chatMessagesRef">
        <!-- 欢迎消息 -->
        <div class="welcome-section" v-if="messages.length === 0">
          <div class="welcome-icon">
            <el-icon :size="56"><MagicStick /></el-icon>
          </div>
          <h2>欢迎使用 IntelliHub AI 助手</h2>
          <p>我可以帮你：代码生成、问题解答、文本创作、数据分析等</p>
          <div class="quick-prompts">
            <div
              v-for="prompt in quickPrompts"
              :key="prompt.title"
              class="quick-prompt-card"
              @click="handleQuickPrompt(prompt.text)"
            >
              <el-icon><component :is="prompt.icon" /></el-icon>
              <span>{{ prompt.title }}</span>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-item', msg.role]"
        >
          <div class="message-avatar">
            <el-avatar :size="36">
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
            <div class="message-actions" v-if="msg.role === 'assistant'">
              <el-button size="small" text @click="copyMessage(msg.content)">
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
              <el-button size="small" text @click="regenerate(index)">
                <el-icon><RefreshRight /></el-icon>
                重新生成
              </el-button>
            </div>
          </div>
        </div>

        <!-- 流式响应中 -->
        <div class="message-item assistant" v-if="streamingContent">
          <div class="message-avatar">
            <el-avatar :size="36">
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

        <!-- 加载中 -->
        <div class="message-item assistant" v-if="isLoading && !streamingContent">
          <div class="message-avatar">
            <el-avatar :size="36">
              <el-icon><MagicStick /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 5 }"
            placeholder="输入消息，按 Enter 发送，Shift+Enter 换行..."
            @keydown="handleKeydown"
            :disabled="isLoading || isStreaming"
            resize="none"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            :loading="isLoading || isStreaming"
            @click="handleSend"
            :disabled="!inputMessage.trim()"
            class="send-btn"
          >
            发送
          </el-button>
        </div>
        <div class="input-tips">
          <span>模型：{{ getModelLabel(selectedModel) }}</span>
          <span v-if="lastTokens">上次消耗：{{ lastTokens }} Token</span>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch, markRaw } from 'vue'
import {
  Plus,
  ChatDotRound,
  MoreFilled,
  Delete,
  User,
  MagicStick,
  CopyDocument,
  RefreshRight,
  Loading,
  Promotion,
  Edit,
  DataLine,
  Document,
  Search
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getModelInfoList,
  getQuotaUsage,
  streamChat,
  clearConversationHistory,
  type QuotaUsage,
  type ChatMessage,
  type ModelInfo
} from '@/api/aigc'

// 消息类型
interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

// 会话类型
interface ChatSession {
  id: string
  title: string
  createdAt: Date
  messages: Message[]
}

// 状态
const messages = ref<Message[]>([])
const inputMessage = ref('')
const selectedModel = ref('qwen-turbo')
const modelInfoList = ref<ModelInfo[]>([])
const isLoading = ref(false)
const isStreaming = ref(false)
const streamingContent = ref('')
const currentSessionId = ref('')
const chatSessions = ref<ChatSession[]>([])
const quotaUsage = ref<QuotaUsage>({
  tenantId: '',
  dailyQuota: 100000,
  usedQuota: 0,
  remainingQuota: 100000,
  usagePercent: 0,
  totalCost: 0,
  resetTime: ''
})
const lastTokens = ref(0)
const chatMessagesRef = ref<HTMLElement | null>(null)

// 当前会话
const currentSession = ref<ChatSession | null>(null)

// 快捷提示
const quickPrompts = [
  { title: '写代码', text: '帮我写一段Java代码', icon: markRaw(Edit) },
  { title: '数据分析', text: '分析这组数据的趋势', icon: markRaw(DataLine) },
  { title: '文档生成', text: '帮我写一篇技术文档', icon: markRaw(Document) },
  { title: '问题解答', text: '解释什么是微服务架构', icon: markRaw(Search) }
]

// 模型标签（与后端 getSupportedModels 对应）
const modelLabels: Record<string, string> = {
  // 阿里通义千问
  'qwen-turbo': '通义千问 Turbo',
  'qwen-plus': '通义千问 Plus',
  'qwen-max': '通义千问 Max',
  'qwen-max-longcontext': '通义千问 Max 长文本',
  // 百度文心一言（2025最新）
  'ernie-3.5-8k': '文心 3.5 (8K)',
  'ernie-3.5-128k': '文心 3.5 (128K)',
  'ernie-4.0-8k': '文心 4.0 (8K)',
  'ernie-4.0-turbo-8k': '文心 4.0 Turbo',
  'ernie-speed-8k': '文心 Speed',
  'ernie-lite-8k': '文心 Lite',
  // 腾讯混元
  'hunyuan-lite': '混元 Lite',
  'hunyuan-standard': '混元 Standard',
  'hunyuan-standard-256K': '混元 Standard (256K)',
  'hunyuan-pro': '混元 Pro',
  'hunyuan-turbo': '混元 Turbo',
  'hunyuan-turbo-latest': '混元 Turbo Latest'
}

// 从后端模型信息获取显示名称
const getModelLabel = (modelId: string) => {
  const info = modelInfoList.value.find(m => m.id === modelId)
  return info?.name || modelLabels[modelId] || modelId
}

// 按厂商分组的模型（从后端动态获取）
const modelGroups = computed(() => {
  const aliyun = modelInfoList.value.filter(m => m.provider === 'aliyun')
  const baidu = modelInfoList.value.filter(m => m.provider === 'baidu')
  const tencent = modelInfoList.value.filter(m => m.provider === 'tencent')
  
  // 如果后端没返回数据，使用默认列表
  if (modelInfoList.value.length === 0) {
    return {
      aliyun: [
        { id: 'qwen-turbo', name: '通义千问 Turbo' },
        { id: 'qwen-plus', name: '通义千问 Plus' },
        { id: 'qwen-max', name: '通义千问 Max' },
        { id: 'qwen-max-longcontext', name: '通义千问 Max 长文本' }
      ],
      baidu: [
        { id: 'ernie-3.5-8k', name: '文心 3.5 (8K)' },
        { id: 'ernie-3.5-128k', name: '文心 3.5 (128K)' },
        { id: 'ernie-4.0-8k', name: '文心 4.0 (8K)' },
        { id: 'ernie-4.0-turbo-8k', name: '文心 4.0 Turbo' },
        { id: 'ernie-speed-8k', name: '文心 Speed' },
        { id: 'ernie-lite-8k', name: '文心 Lite' }
      ],
      tencent: [
        { id: 'hunyuan-lite', name: '混元 Lite' },
        { id: 'hunyuan-standard', name: '混元 Standard' },
        { id: 'hunyuan-standard-256K', name: '混元 Standard (256K)' },
        { id: 'hunyuan-pro', name: '混元 Pro' },
        { id: 'hunyuan-turbo', name: '混元 Turbo' },
        { id: 'hunyuan-turbo-latest', name: '混元 Turbo Latest' }
      ]
    }
  }
  
  return { aliyun, baidu, tencent }
})

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

// 渲染Markdown
const renderMarkdown = (content: string) => {
  if (!content) return ''
  
  let html = content.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    return `<pre class="code-block"><code class="language-${lang}">${escapeHtml(code.trim())}</code></pre>`
  })
  
  html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  html = html.replace(/\n/g, '<br>')
  
  return html
}

const escapeHtml = (text: string) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// 加载模型列表
const loadModels = async () => {
  try {
    const res = await getModelInfoList()
    if (res.code === 200 && res.data) {
      modelInfoList.value = res.data
    }
  } catch {
    // 加载失败时使用空数组，modelGroups会使用默认值
    modelInfoList.value = []
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

// 新建对话
const handleNewChat = () => {
  const newSession: ChatSession = {
    id: crypto.randomUUID(),
    title: '新对话',
    createdAt: new Date(),
    messages: []
  }
  chatSessions.value.unshift(newSession)
  selectSession(newSession)
}

// 选择会话
const selectSession = (session: ChatSession) => {
  currentSessionId.value = session.id
  currentSession.value = session
  messages.value = session.messages
  scrollToBottom()
}

// 会话命令处理
const handleSessionCommand = async (command: string, session: ChatSession) => {
  if (command === 'rename') {
    const { value } = await ElMessageBox.prompt('请输入新的对话名称', '重命名', {
      inputValue: session.title,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    if (value) {
      session.title = value
    }
  } else if (command === 'delete') {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', { type: 'warning' })
    const index = chatSessions.value.findIndex(s => s.id === session.id)
    if (index > -1) {
      chatSessions.value.splice(index, 1)
      if (currentSessionId.value === session.id) {
        if (chatSessions.value.length > 0) {
          selectSession(chatSessions.value[0])
        } else {
          currentSessionId.value = ''
          currentSession.value = null
          messages.value = []
        }
      }
    }
  }
}

// 发送消息
const handleSend = async () => {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value || isStreaming.value) return

  // 确保有当前会话
  if (!currentSession.value) {
    handleNewChat()
  }

  // 添加用户消息
  const userMsg: Message = { role: 'user', content: message, timestamp: new Date() }
  messages.value.push(userMsg)
  currentSession.value!.messages = messages.value
  
  // 更新会话标题
  if (currentSession.value!.title === '新对话') {
    currentSession.value!.title = message.substring(0, 20) + (message.length > 20 ? '...' : '')
  }

  inputMessage.value = ''
  scrollToBottom()

  // 流式请求
  isStreaming.value = true
  streamingContent.value = ''

  try {
    await streamChat(
      {
        message,
        model: selectedModel.value,
        conversationId: currentSessionId.value,
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
        if (streamingContent.value) {
          const aiMsg: Message = {
            role: 'assistant',
            content: streamingContent.value,
            timestamp: new Date()
          }
          messages.value.push(aiMsg)
          currentSession.value!.messages = messages.value
        }
        streamingContent.value = ''
        isStreaming.value = false
        loadQuota()
        scrollToBottom()
      }
    )
  } catch {
    isStreaming.value = false
    ElMessage.error('请求失败，请稍后重试')
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
const handleQuickPrompt = (text: string) => {
  inputMessage.value = text
  handleSend()
}

// 清空历史
const handleClearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前对话历史吗？', '提示', { type: 'warning' })
    if (currentSessionId.value) {
      await clearConversationHistory(currentSessionId.value)
    }
    messages.value = []
    if (currentSession.value) {
      currentSession.value.messages = []
    }
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

// 重新生成
const regenerate = async (index: number) => {
  // 找到对应的用户消息
  if (index > 0 && messages.value[index - 1].role === 'user') {
    const userMessage = messages.value[index - 1].content
    // 移除当前AI回复
    messages.value.splice(index, 1)
    // 重新发送
    inputMessage.value = userMessage
    handleSend()
  }
}

// 监听消息变化
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

onMounted(() => {
  loadModels()
  loadQuota()
})
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  height: calc(100vh - 56px);
  background: #f5f7fa;
}

/* 左侧边栏 */
.chat-sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.new-chat-btn {
  width: 100%;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.history-item:hover {
  background: #f5f7fa;
}

.history-item.active {
  background: #e6f4ff;
}

.history-icon {
  color: #909399;
  flex-shrink: 0;
}

.history-title {
  flex: 1;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-more {
  opacity: 0;
  transition: opacity 0.2s;
}

.history-item:hover .history-more {
  opacity: 1;
}

/* 配额信息 */
.quota-section {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.quota-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
}

.quota-percent {
  font-weight: 600;
}

.quota-detail {
  margin-top: 8px;
  font-size: 11px;
  color: #909399;
  text-align: center;
}

/* 右侧主区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 工具栏 */
.chat-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

/* 模型选择器 */
.model-select {
  width: 240px;
}

.model-select :deep(.el-input__wrapper) {
  padding-right: 30px;
}

.model-select :deep(.el-select-dropdown) {
  min-width: 260px !important;
}

.model-select :deep(.el-select-group__title) {
  font-weight: 600;
  color: #1890ff;
  font-size: 12px;
  padding: 8px 12px 4px;
}

.model-select :deep(.el-select-dropdown__item) {
  font-size: 13px;
  padding: 8px 20px;
}

.chat-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 消息区 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* 欢迎区 */
.welcome-section {
  text-align: center;
  padding: 60px 20px;
  max-width: 600px;
  margin: 0 auto;
}

.welcome-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.welcome-section h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.welcome-section p {
  margin: 0 0 32px;
  color: #909399;
}

.quick-prompts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-prompt-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-prompt-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.quick-prompt-card .el-icon {
  font-size: 20px;
  color: #409eff;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar .el-avatar {
  background: #e6f4ff;
  color: #409eff;
}

.message-item.assistant .message-avatar .el-avatar {
  background: #f0f9eb;
  color: #67c23a;
}

.message-content {
  flex: 1;
  max-width: 80%;
}

.message-item.user .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
}

.message-item.user .message-header {
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

.message-item.user .message-body {
  background: #409eff;
  color: #fff;
}

.message-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

/* 代码块 */
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

.message-item.user :deep(.inline-code) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

/* 流式指示器 */
.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
  font-size: 12px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

/* 输入区 */
.chat-input-area {
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  max-width: 900px;
  margin: 0 auto;
}

.input-container .el-textarea {
  flex: 1;
}

.input-container :deep(.el-textarea__inner) {
  border-radius: 8px;
  padding: 10px 14px;
}

.send-btn {
  height: 40px;
  padding: 0 20px;
}

.input-tips {
  max-width: 900px;
  margin: 8px auto 0;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-sidebar {
    display: none;
  }
  
  .quick-prompts {
    grid-template-columns: 1fr;
  }
}
</style>
