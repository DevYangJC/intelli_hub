<template>
  <div class="ai-assistant">
    <!-- 悬浮按钮 -->
    <el-tooltip content="智能助手" placement="left" v-if="!isOpen">
      <div class="ai-fab" @click="toggleOpen">
        <el-icon :size="24"><ChatDotRound /></el-icon>
      </div>
    </el-tooltip>

    <!-- 对话面板 -->
    <transition name="slide-up">
      <div class="ai-panel" v-if="isOpen">
        <!-- 头部 -->
        <div class="ai-header">
          <div class="ai-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能助手</span>
          </div>
          <div class="ai-actions">
            <el-tooltip content="清空对话">
              <el-button :icon="Delete" circle size="small" @click="clearHistory" />
            </el-tooltip>
            <el-tooltip content="最小化">
              <el-button :icon="Minus" circle size="small" @click="toggleOpen" />
            </el-tooltip>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="ai-messages" ref="messagesRef">
          <!-- 欢迎消息 -->
          <div class="message assistant" v-if="messages.length === 0">
            <div class="message-content">
              <p>👋 你好！我是 IntelliHub 智能助手。</p>
              <p>我可以帮助你解答以下问题：</p>
              <ul>
                <li>API 使用方法和调用示例</li>
                <li>错误排查和解决方案</li>
                <li>平台功能和最佳实践</li>
              </ul>
              <p>有什么可以帮你的吗？</p>
            </div>
          </div>

          <!-- 对话消息 -->
          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="message-content">
              <div v-if="msg.role === 'assistant'" v-html="renderMarkdown(msg.content)"></div>
              <div v-else>{{ msg.content }}</div>
            </div>
          </div>

          <!-- 加载中 -->
          <div class="message assistant" v-if="loading">
            <div class="message-content loading">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>

        <!-- 建议问题 -->
        <div class="suggested-questions" v-if="suggestedQuestions.length > 0 && !loading">
          <div 
            class="suggested-item" 
            v-for="(q, i) in suggestedQuestions" 
            :key="i"
            @click="askSuggested(q)"
          >
            {{ q }}
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="ai-input">
          <el-input
            v-model="inputText"
            placeholder="输入你的问题..."
            :disabled="loading"
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button 
                :icon="Promotion" 
                @click="sendMessage"
                :loading="loading"
                :disabled="!inputText.trim()"
              />
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { ChatDotRound, Delete, Minus, Promotion } from '@element-plus/icons-vue'
import { askQuestion, type ChatHistory, type QaResponse } from '@/api/aigc'
import { marked } from 'marked'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const isOpen = ref(false)
const inputText = ref('')
const loading = ref(false)
const messages = ref<Message[]>([])
const suggestedQuestions = ref<string[]>([])
const messagesRef = ref<HTMLElement | null>(null)

// 切换打开状态
const toggleOpen = () => {
  isOpen.value = !isOpen.value
}

// 清空历史
const clearHistory = () => {
  messages.value = []
  suggestedQuestions.value = []
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 渲染 Markdown
const renderMarkdown = (content: string): string => {
  try {
    return marked.parse(content) as string
  } catch {
    return content
  }
}

// 发送消息
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  suggestedQuestions.value = []
  scrollToBottom()

  // 构建历史
  const history: ChatHistory[] = messages.value.slice(0, -1).map(m => ({
    role: m.role,
    content: m.content
  }))

  loading.value = true

  try {
    const res = await askQuestion({
      question: text,
      history: history.length > 0 ? history : undefined
    })

    if (res.code === 200 && res.data) {
      messages.value.push({
        role: 'assistant',
        content: res.data.answer
      })
      suggestedQuestions.value = res.data.suggestedQuestions || []
    } else {
      messages.value.push({
        role: 'assistant',
        content: '抱歉，我暂时无法回答这个问题，请稍后重试。'
      })
    }
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '网络错误，请检查连接后重试。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 点击建议问题
const askSuggested = (question: string) => {
  inputText.value = question
  sendMessage()
}

// 监听消息变化，自动滚动
watch(messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

/* 悬浮按钮 */
.ai-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.ai-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

/* 对话面板 */
.ai-panel {
  width: 380px;
  height: 520px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.ai-header {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.ai-actions {
  display: flex;
  gap: 4px;
}

.ai-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.ai-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 消息列表 */
.ai-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 85%;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: #f5f7fa;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message.assistant .message-content ul {
  margin: 8px 0;
  padding-left: 20px;
}

.message.assistant .message-content li {
  margin: 4px 0;
}

.message.assistant .message-content :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 13px;
  margin: 8px 0;
}

.message.assistant .message-content :deep(code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.message.assistant .message-content :deep(pre code) {
  background: transparent;
  padding: 0;
}

/* 加载动画 */
.message-content.loading {
  display: flex;
  gap: 4px;
  padding: 16px 20px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 建议问题 */
.suggested-questions {
  padding: 8px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggested-item {
  padding: 6px 12px;
  background: #f0f2f5;
  border-radius: 16px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.suggested-item:hover {
  background: #667eea;
  color: white;
}

/* 输入区域 */
.ai-input {
  padding: 12px 16px;
  border-top: 1px solid #eee;
}

.ai-input :deep(.el-input-group__append) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.ai-input :deep(.el-input-group__append .el-button) {
  color: white;
}

/* 动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
