<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { getMessages, sendMessage, type MessageItem } from '@/api/im'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const route = useRoute()
const userStore = useUserStore()

const targetName = computed(() => (route.query.targetName as string) || '聊天')
const targetId = computed(() => route.query.targetId as string)
const conversationId = computed(() => route.query.conversationId as string)

const messages = ref<MessageItem[]>([])
const inputText = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesEnd = ref<HTMLElement | null>(null)

const myUserId = computed(() => userStore.userInfo?.user_id || '')

function scrollToBottom() {
  nextTick(() => {
    if (messagesEnd.value) {
      messagesEnd.value.scrollIntoView({ behavior: 'smooth' })
    }
  })
}

async function loadMessages() {
  if (!conversationId.value) return
  try {
    loading.value = true
    const res = await getMessages({
      conversation_id: conversationId.value,
      page: 1,
      size: 50,
    })
    if (res.code === 200) {
      messages.value = (res.data.list || []).reverse()
      scrollToBottom()
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

onMounted(() => {
  loadMessages()
})
</script>

<template>
  <div class="chat-page">
    <NavBar :title="targetName" show-back />

    <div class="chat-content">
      <div v-if="loading" class="loading-tip">加载中...</div>

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
            <div class="bubble" :class="isMyMessage(msg) ? 'bubble-mine' : 'bubble-other'">
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

.loading-tip {
  text-align: center;
  padding: var(--spacing-base);
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
}

.other-avatar {
  background-color: var(--color-primary);
}

.my-avatar {
  background-color: var(--color-accent);
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
}

.bubble-other {
  background-color: var(--color-bg);
  color: var(--color-text);
  border-top-left-radius: 4px;
}

.bubble-mine {
  background-color: var(--color-primary);
  color: #fff;
  border-top-right-radius: 4px;
}

.bubble-text {
  white-space: pre-wrap;
}

.msg-time {
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.input-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 8px var(--spacing-base);
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  background-color: var(--color-bg);
  border-top: 1px solid var(--color-border);
}

.input-wrap {
  flex: 1;
  background-color: var(--color-bg-secondary);
  border-radius: 20px;
  padding: 0 14px;
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
    color: var(--color-text-placeholder);
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

  &.active {
    background-color: var(--color-primary);
    cursor: pointer;

    &:active {
      transform: scale(0.96);
      background-color: var(--color-primary-dark);
    }
  }
}
</style>
