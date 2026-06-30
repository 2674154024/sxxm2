<template>
  <view class="page">
    <view class="chat-header">
      <text class="back-btn" @click="handleBack">‹</text>
      <text class="chat-title">{{ chatName }}</text>
      <text class="header-right">...</text>
    </view>

    <scroll-view 
      class="chat-content" 
      scroll-y 
      :scroll-top="scrollTop"
      @scrolltolower="loadMoreMessages"
    >
      <view class="message-list">
        <view class="time-divider" v-for="(item, index) in groupedMessages" :key="'time-' + index">
          <text class="time-text">{{ item.timeLabel }}</text>
        </view>
        <view 
          class="message-item" 
          :class="[item.isMine ? 'right' : 'left']"
          v-for="msg in messages" 
          :key="msg.id"
        >
          <view class="avatar" :style="{ background: item.isMine ? '#165DFF' : msg.avatarColor }">
            <text class="avatar-text">{{ item.isMine ? '我' : msg.sender_name.charAt(0) }}</text>
          </view>
          <view class="message-bubble" :class="[item.isMine ? 'right' : 'left']">
            <text class="message-content" v-if="msg.message_type === 'text'">{{ msg.content }}</text>
            <image 
              class="message-image" 
              v-else-if="msg.message_type === 'image'" 
              :src="msg.file_url" 
              mode="widthFix"
              @click="previewImage(msg.file_url)"
            />
            <view class="message-file" v-else-if="msg.message_type === 'file'">
              <text class="file-icon">📄</text>
              <text class="file-name">{{ msg.file_name }}</text>
            </view>
            <view class="message-status" v-if="item.isMine">
              <text class="status-icon" :class="{ read: msg.is_read }">{{ msg.is_read ? '✓✓' : '✓' }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="chat-footer">
      <view class="footer-left">
        <view class="func-btn" @click="handleImage">
          <text class="func-icon">🖼️</text>
        </view>
        <view class="func-btn" @click="handleFile">
          <text class="func-icon">📎</text>
        </view>
      </view>
      <input 
        class="input-box" 
        placeholder="输入消息..." 
        v-model="inputText" 
        @confirm="handleSend" 
        :adjust-position="true"
      />
      <button class="send-btn" :class="{ disabled: !inputText.trim() }" @click="handleSend">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../../../store/index'
import { imApi, type Message } from '../../../api/im'
import dayjs from 'dayjs'

const userStore = useUserStore()
const chatName = ref('')
const conversationId = ref('')
const inputText = ref('')
const scrollTop = ref(0)
const messages = ref<Message[]>([])
const page = ref(1)
const hasMore = ref(true)
const isLoading = ref(false)

const groupedMessages = computed(() => {
  const groups: { timeLabel: string; messages: Message[] }[] = []
  let lastDate = ''
  
  messages.value.forEach((msg) => {
    const msgDate = dayjs(msg.created_at)
    const now = dayjs()
    let timeLabel = ''
    
    if (msgDate.isSame(now, 'day')) {
      timeLabel = msgDate.format('HH:mm')
    } else if (msgDate.isSame(now.subtract(1, 'day'), 'day')) {
      timeLabel = '昨天'
    } else {
      timeLabel = msgDate.format('MM月DD日')
    }
    
    if (timeLabel !== lastDate) {
      groups.push({ timeLabel, messages: [] })
      lastDate = timeLabel
    }
    groups[groups.length - 1].messages.push(msg)
  })
  
  return groups
})

const getMessageItem = (msg: Message) => {
  const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#a8edea']
  const index = parseInt(msg.sender_id) % colors.length
  return {
    ...msg,
    isMine: msg.sender_id === userStore.user?.user_id,
    avatarColor: colors[index]
  }
}

const handleBack = () => {
  uni.navigateBack()
}

const handleSend = async () => {
  if (!inputText.value.trim()) return
  
  const newMsg: Message = {
    id: Date.now().toString(),
    conversation_id: conversationId.value,
    sender_id: userStore.user?.user_id || '',
    sender_name: '我',
    sender_avatar: '',
    content: inputText.value,
    message_type: 'text',
    created_at: dayjs().toISOString(),
    is_read: false
  }
  
  messages.value.push(newMsg)
  inputText.value = ''
  scrollToBottom()
  
  try {
    await imApi.sendMessage({
      conversation_id: conversationId.value,
      content: newMsg.content,
      message_type: 'text'
    })
  } catch (error) {
    uni.showToast({ title: '发送失败', icon: 'none' })
    messages.value.pop()
  }
}

const handleImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      
      const newMsg: Message = {
        id: Date.now().toString(),
        conversation_id: conversationId.value,
        sender_id: userStore.user?.user_id || '',
        sender_name: '我',
        sender_avatar: '',
        content: '',
        message_type: 'image',
        file_url: tempFilePath,
        created_at: dayjs().toISOString(),
        is_read: false
      }
      
      messages.value.push(newMsg)
      scrollToBottom()
      
      try {
        await imApi.sendMessage({
          conversation_id: conversationId.value,
          content: '',
          message_type: 'image',
          file_url: tempFilePath
        })
      } catch (error) {
        uni.showToast({ title: '发送失败', icon: 'none' })
        messages.value.pop()
      }
    }
  })
}

const handleFile = () => {
  uni.chooseFile({
    count: 1,
    type: 'all',
    success: async (res) => {
      const tempFilePath = res.tempFiles[0].path
      const fileName = res.tempFiles[0].name
      
      const newMsg: Message = {
        id: Date.now().toString(),
        conversation_id: conversationId.value,
        sender_id: userStore.user?.user_id || '',
        sender_name: '我',
        sender_avatar: '',
        content: '',
        message_type: 'file',
        file_url: tempFilePath,
        file_name: fileName,
        created_at: dayjs().toISOString(),
        is_read: false
      }
      
      messages.value.push(newMsg)
      scrollToBottom()
      
      try {
        await imApi.sendMessage({
          conversation_id: conversationId.value,
          content: '',
          message_type: 'file',
          file_url: tempFilePath,
          file_name: fileName
        })
      } catch (error) {
        uni.showToast({ title: '发送失败', icon: 'none' })
        messages.value.pop()
      }
    }
  })
}

const previewImage = (url: string) => {
  uni.previewImage({
    urls: [url],
    current: url
  })
}

const scrollToBottom = () => {
  setTimeout(() => {
    scrollTop.value = 999999
  }, 100)
}

const loadMessages = async () => {
  if (!conversationId.value) return
  
  try {
    const res = await imApi.getMessages({
      conversation_id: conversationId.value,
      page: page.value,
      size: 20
    })
    
    if (res.data && res.data.list) {
      const newMessages = res.data.list.reverse()
      messages.value = [...messages.value, ...newMessages]
      hasMore.value = newMessages.length === 20
    }
  } catch (error) {
    console.error('加载消息失败', error)
  }
  
  setTimeout(() => {
    scrollTop.value = 999999
  }, 100)
}

const loadMoreMessages = () => {
  if (!hasMore.value || isLoading.value) return
  
  isLoading.value = true
  page.value++
  loadMessages().finally(() => {
    isLoading.value = false
  })
}

const initWebSocket = () => {
  const token = userStore.token || uni.getStorageSync('token')
  if (!token) return
  
  const wsUrl = `wss://api.parttime-cs.com/ws/im?token=${token}`
  
  uni.connectSocket({
    url: wsUrl,
    success: () => {
      console.log('WebSocket连接成功')
    },
    fail: (err) => {
      console.error('WebSocket连接失败', err)
    }
  })
  
  uni.onSocketMessage((res) => {
    try {
      const msg: Message = JSON.parse(res.data)
      if (msg.conversation_id === conversationId.value) {
        messages.value.push(msg)
        scrollToBottom()
      }
    } catch (error) {
      console.error('解析消息失败', error)
    }
  })
  
  uni.onSocketError((err) => {
    console.error('WebSocket错误', err)
  })
  
  uni.onSocketClose(() => {
    console.log('WebSocket关闭')
  })
}

const closeWebSocket = () => {
  uni.closeSocket({
    success: () => {
      console.log('WebSocket关闭成功')
    }
  })
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage as any).$page?.options || {}
  
  conversationId.value = options.id || options.conversation_id || ''
  chatName.value = options.name || '聊天'
  
  loadMessages()
  initWebSocket()
})

onUnmounted(() => {
  closeWebSocket()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F2F3F5;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #FFFFFF;
  padding: 24rpx 32rpx;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  font-size: 48rpx;
  color: #1F2329;
}

.chat-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
}

.header-right {
  font-size: 36rpx;
  color: #86909C;
}

.chat-content {
  flex: 1;
  padding: 24rpx;
}

.message-list {
  padding-bottom: 40rpx;
}

.time-divider {
  display: flex;
  justify-content: center;
  margin: 24rpx 0;
}

.time-text {
  font-size: 24rpx;
  color: #C9CDD4;
  background-color: #E5E6EB;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.message-item {
  display: flex;
  margin-bottom: 32rpx;
}

.message-item.right {
  flex-direction: row-reverse;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-item.left .avatar {
  margin-right: 16rpx;
}

.message-item.right .avatar {
  margin-left: 16rpx;
}

.avatar-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: bold;
}

.message-bubble {
  max-width: 70%;
  background-color: #FFFFFF;
  padding: 20rpx 28rpx;
  border-radius: 20rpx;
  border-bottom-left-radius: 4rpx;
  position: relative;
}

.message-bubble.right {
  background-color: #165DFF;
  border-bottom-right-radius: 4rpx;
  border-bottom-left-radius: 20rpx;
}

.message-content {
  font-size: 28rpx;
  line-height: 1.6;
}

.message-bubble.right .message-content {
  color: #FFFFFF;
}

.message-bubble.left .message-content {
  color: #1F2329;
}

.message-image {
  max-width: 400rpx;
  max-height: 400rpx;
  border-radius: 12rpx;
}

.message-file {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
}

.file-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.file-name {
  font-size: 26rpx;
  color: #1F2329;
  max-width: 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-bubble.right .file-name {
  color: #FFFFFF;
}

.message-status {
  position: absolute;
  right: 12rpx;
  bottom: 12rpx;
}

.status-icon {
  font-size: 22rpx;
  color: #C9CDD4;
}

.status-icon.read {
  color: #86909C;
}

.chat-footer {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.footer-left {
  display: flex;
  margin-right: 16rpx;
}

.func-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.func-icon {
  font-size: 36rpx;
}

.input-box {
  flex: 1;
  font-size: 28rpx;
  padding: 20rpx 28rpx;
  background-color: #F2F3F5;
  border-radius: 40rpx;
  margin-right: 20rpx;
}

.send-btn {
  font-size: 28rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 20rpx 40rpx;
  border-radius: 40rpx;
  border: none;
}

.send-btn.disabled {
  background-color: #C9CDD4;
}
</style>