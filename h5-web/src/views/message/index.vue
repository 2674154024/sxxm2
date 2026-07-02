<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import { getConversations, type ConversationItem } from '@/api/im'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const conversations = ref<ConversationItem[]>([])
const loading = ref(false)
const totalUnread = ref(0)

const systemMessages = [
  {
    id: 'system',
    icon: '🔔',
    title: '系统通知',
    desc: '欢迎使用长沙大学生兼职平台',
    time: '刚刚',
    unread: 0,
  },
]

function calcTotalUnread() {
  let total = 0
  conversations.value.forEach((item) => {
    total += item.unread_count
  })
  totalUnread.value = total
}

async function loadConversations() {
  if (!userStore.isLoggedIn) return
  try {
    loading.value = true
    const res = await getConversations()
    if (res.code === 200) {
      conversations.value = res.data || []
      calcTotalUnread()
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

function goToChat(conv: ConversationItem) {
  console.log('goToChat called with:', conv)
  if (!conv.conversation_id || !conv.target_user_id) {
    console.error('Invalid conversation data:', conv)
    return
  }
  router.push({
    path: '/chat',
    query: {
      conversationId: conv.conversation_id,
      targetId: conv.target_user_id,
      targetName: conv.target_name || '聊天',
    },
  }).catch(err => {
    console.error('Router push failed:', err)
  })
}

function formatTime(time: string) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const day = 24 * 60 * 60 * 1000

  if (diff < 60 * 1000) {
    return '刚刚'
  } else if (diff < 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 1000)) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
  } else if (diff < day * 7) {
    return Math.floor(diff / day) + '天前'
  } else {
    const month = date.getMonth() + 1
    const dayNum = date.getDate()
    return `${month}-${dayNum}`
  }
}

onMounted(() => {
  loadConversations()
})
</script>

<template>
  <div class="message-page">
    <div class="page-header">
      <h1 class="page-title">消息</h1>
    </div>

    <div class="message-list">
      <div class="system-section">
        <div
          v-for="item in systemMessages"
          :key="item.id"
          class="message-item"
        >
          <div class="avatar-wrap system-avatar">
            <span class="avatar-icon">{{ item.icon }}</span>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="name">{{ item.title }}</span>
              <span class="time">{{ item.time }}</span>
            </div>
            <div class="last-message">
              <span class="text ellipsis">{{ item.desc }}</span>
            </div>
          </div>
          <div v-if="item.unread > 0" class="unread-badge">
            {{ item.unread > 99 ? '99+' : item.unread }}
          </div>
        </div>
      </div>

      <div class="divider-line"></div>

      <div v-if="conversations.length > 0">
        <div
          v-for="conv in conversations"
          :key="conv.conversation_id"
          class="message-item"
          @click="goToChat(conv)"
        >
          <div class="avatar-wrap">
            <div class="avatar">
              {{ conv.target_name?.charAt(0) || '?' }}
            </div>
            <div v-if="conv.is_online" class="online-dot"></div>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="name">{{ conv.target_name }}</span>
              <span class="time">{{ formatTime(conv.last_time) }}</span>
            </div>
            <div class="last-message">
              <span class="text ellipsis">{{ conv.last_message }}</span>
            </div>
          </div>
          <div v-if="conv.unread_count > 0" class="unread-badge">
            {{ conv.unread_count > 99 ? '99+' : conv.unread_count }}
          </div>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-text">暂无消息</div>
        <div class="empty-sub">投递岗位后，企业会主动联系你哦</div>
      </div>
    </div>
    <TabBar active="/message" />
  </div>
</template>

<style scoped lang="scss">
.message-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
}

.page-header {
  padding: var(--spacing-base);
  padding-bottom: var(--spacing-sm);
  background-color: var(--color-bg);
}

.page-title {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.message-list {
  background-color: var(--color-bg);
}

.system-section {
  padding-top: var(--spacing-sm);
}

.divider-line {
  height: 1px;
  margin: 0 var(--spacing-base);
  background-color: var(--color-border);
}

.message-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 12px var(--spacing-base);
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:active {
    background-color: #f7f8fa;
  }

  animation: messageSlideIn 0.3s ease backwards;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.avatar-wrap {
  position: relative;
  margin-right: var(--spacing-sm);
  flex-shrink: 0;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-lg);
  font-weight: 500;
}

.system-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--color-primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 24px;
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: var(--color-success);
  border: 2px solid #fff;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.name {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text);
}

.time {
  font-size: 12px;
  color: var(--color-text-secondary);
  flex-shrink: 0;
  margin-left: 8px;
}

.last-message {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.text {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.unread-badge {
  position: absolute;
  right: var(--spacing-base);
  top: 50%;
  transform: translateY(-50%);
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background-color: var(--color-danger);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: badgeBounce 0.4s cubic-bezier(0.68, -0.55, 0.27, 1.55);
}

@keyframes badgeBounce {
  0% { transform: translateY(-50%) scale(0); }
  50% { transform: translateY(-50%) scale(1.2); }
  100% { transform: translateY(-50%) scale(1); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--spacing-base);
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

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
