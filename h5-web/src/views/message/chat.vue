<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { getMessages, sendMessage, type MessageItem } from '@/api/im'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const targetName = computed(() => {
  const name = route.query.targetName as string
  return name ? decodeURIComponent(name) : '聊天'
})
const targetId = computed(() => route.query.targetId as string)
const conversationId = computed(() => route.query.conversationId as string)

const messages = ref<MessageItem[]>([])
const inputText = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesEnd = ref<HTMLElement | null>(null)
const connecting = ref(false)
const showRiskWarning = ref(false)

let ws: WebSocket | null = null

const myUserId = computed(() => userStore.userInfo?.user_id || '')

function scrollToBottom(smooth = true) {
  nextTick(() => {
    if (messagesEnd.value) {
      messagesEnd.value.scrollIntoView({ behavior: smooth ? 'smooth' : 'auto' })
    }
  })
}

function initWebSocket() {
  const token = localStorage.getItem('token')
  if (!token) return

  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${wsProtocol}//${window.location.host}/api/ws/im?token=${token}`

  try {
    connecting.value = true
    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      connecting.value = false
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'message') {
          const newMsg: MessageItem = {
            message_id: data.messageId || data.message_id,
            from_id: data.fromId || data.from_id,
            to_id: data.toId || data.to_id,
            content: data.content,
            message_type: data.messageType || data.message_type,
            timestamp: data.timestamp,
            is_read: 1,
          }
          messages.value.push(newMsg)
          scrollToBottom()
        } else if (data.type === 'ping') {
          ws?.send(JSON.stringify({ type: 'pong' }))
        }
      } catch (e) {
        console.error('WebSocket message parse error:', e)
      }
    }

    ws.onclose = () => {
      connecting.value = false
    }

    ws.onerror = () => {
      connecting.value = false
    }
  } catch (e) {
    connecting.value = false
  }
}

function closeWebSocket() {
  if (ws) {
    ws.close()
    ws = null
  }
}

async function loadMessages() {
  if (!conversationId.value) return
  try {
    loading.value = true
    const res = await getMessages({
      conversation_id: conversationId.value,
      target_id: targetId.value,
      page: 1,
      size: 50,
    })
    if (res.code === 200) {
      messages.value = (res.data.list || []).reverse()
      scrollToBottom(false)
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  if (!inputText.value.trim() || sending.value) return
  if (!targetId.value) return

  const text = inputText.value.trim()
  inputText.value = ''

  const tempMsg: MessageItem = {
    message_id: 'temp_' + Date.now(),
    from_id: myUserId.value,
    to_id: targetId.value,
    content: text,
    message_type: 'text',
    timestamp: Date.now(),
    is_read: 0,
  }
  messages.value.push(tempMsg)
  scrollToBottom()

  try {
    sending.value = true
    const res = await sendMessage({
      target_id: targetId.value,
      content: text,
      message_type: 'text',
    })
    if (res.code === 200) {
      const idx = messages.value.findIndex((m) => m.message_id === tempMsg.message_id)
      if (idx !== -1) {
        messages.value[idx].message_id = res.data.message_id
      }
    }
  } catch (error: any) {
    showToast({
      message: error?.message || '发送失败',
      type: 'fail',
    })
  } finally {
    sending.value = false
  }
}

function formatTime(timestamp: number) {
  const date = new Date(timestamp)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

function isMyMessage(msg: MessageItem) {
  return msg.from_id === myUserId.value
}

function goBack() {
  router.back()
}

watch([targetId, conversationId], () => {
  messages.value = []
  loadMessages()
})

onMounted(() => {
  loadMessages()
  initWebSocket()
})

onUnmounted(() => {
  closeWebSocket()
})
</script>

<template>
  <div class="chat-page">
    <NavBar :title="targetName" show-back @back="goBack" />

    <div class="chat-content">
      <div v-if="loading" class="loading-container">
        <div class="loading-dots">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
        <span class="loading-text">加载中...</span>
      </div>

      <div v-else-if="messages.length === 0" class="empty-state">
        <div class="empty-illustration">💬</div>
        <div class="empty-text">暂无消息</div>
        <div class="empty-sub">开始与{{ targetName }}聊天吧</div>
      </div>

      <div class="messages-container">
        <div
          v-for="msg in messages"
          :key="msg.message_id"
          class="message-row"
          :class="{ 'is-mine': isMyMessage(msg) }"
        >
          <div v-if="!isMyMessage(msg)" class="avatar other-avatar">
            {{ targetName?.charAt(0) || '?' }}
          </div>
          <div class="bubble-wrap">
            <div 
              class="bubble" 
              :class="[
                isMyMessage(msg) ? 'bubble-mine' : 'bubble-other',
                { 'risk-warning': showRiskWarning }
              ]"
            >
              <span class="bubble-text">{{ msg.content }}</span>
            </div>
            <div class="msg-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
          <div v-if="isMyMessage(msg)" class="avatar my-avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || '我' }}
          </div>
        </div>
      </div>

      <div ref="messagesEnd"></div>
    </div>

    <div class="input-bar">
      <div class="input-wrap">
        <input
          v-model="inputText"
          type="text"
          class="text-input"
          placeholder="输入消息..."
          @keyup.enter="handleSend"
        />
      </div>
      <button
        class="send-btn"
        :class="{ active: inputText.trim() }"
        :disabled="!inputText.trim() || sending"
        @click="handleSend"
      >
        发送
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-secondary);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: var(--spacing-base);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  gap: 12px;
}

.loading-dots {
  display: flex;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--color-primary);
  animation: loadingDot 1.4s ease-in-out infinite both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
.dot:nth-child(3) { animation-delay: 0s; }

@keyframes loadingDot {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.loading-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-base);
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;

  &.is-mine {
    flex-direction: row-reverse;
  }

  animation: messageFadeIn 0.3s ease backwards;
}

@keyframes messageFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  color: #fff;
  flex-shrink: 0;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.9);
  }
}

.other-avatar {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
}

.my-avatar {
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-light) 100%);
}

.bubble-wrap {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.is-mine .bubble-wrap {
  align-items: flex-end;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-all;
  font-size: var(--font-size-base);
  line-height: 1.4;
  position: relative;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.98);
  }
}

.bubble-other {
  background-color: var(--color-bg);
  color: var(--color-text);
  border-top-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}

.bubble-mine {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: #fff;
  border-top-right-radius: 4px;
}

.bubble-text {
  white-space: pre-wrap;
}

.risk-warning {
  animation: riskFlash 0.2s ease-in-out 3;
}

@keyframes riskFlash {
  0%, 100% {
    border-color: transparent;
    box-shadow: 0 0 0 0 rgba(245, 63, 63, 0);
  }
  50% {
    border-color: var(--color-danger);
    box-shadow: 0 0 0 4px rgba(245, 63, 63, 0.2);
  }
}

.msg-time {
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}

.empty-illustration {
  font-size: 80px;
  margin-bottom: var(--spacing-base);
  animation: floatY 3s ease-in-out infinite;
}

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.empty-text {
  font-size: var(--font-size-lg);
  color: var(--color-text);
  margin-bottom: 8px;
}

.empty-sub {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.input-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 8px var(--spacing-base);
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  background-color: var(--color-bg);
  border-top: 1px solid var(--color-border);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.input-wrap {
  flex: 1;
  background-color: var(--color-bg-secondary);
  border-radius: 20px;
  padding: 0 14px;
  transition: all 0.2s ease;

  &:active {
    background-color: var(--color-bg-tertiary);
  }
}

.text-input {
  width: 100%;
  height: 36px;
  border: none;
  background: transparent;
  font-size: var(--font-size-base);
  color: var(--color-text);
  outline: none;

  &::placeholder {
    color: var(--color-text-secondary);
  }
}

.send-btn {
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 18px;
  background-color: var(--color-text-disabled);
  color: #fff;
  font-size: var(--font-size-base);
  cursor: not-allowed;
  transition: all 0.2s ease;
  min-width: 56px;

  &.active {
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
    cursor: pointer;

    &:active {
      transform: scale(0.96);
      background: linear-gradient(135deg, var(--color-primary-dark) 0%, var(--color-primary) 100%);
    }
  }
}
</style>
