<template>
  <view class="page">
    <view class="chat-header">
      <view class="back-btn" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-info">
        <text class="chat-title">{{ chatName }}</text>
        <text class="chat-subtitle" v-if="jobTitle">{{ jobTitle }}</text>
      </view>
      <view class="header-right">
        <text class="more-icon">⋯</text>
      </view>
    </view>

    <scroll-view 
      class="chat-content" 
      scroll-y 
      :scroll-top="scrollTop"
      @scrolltolower="loadMoreMessages"
    >
      <view class="message-list">
        <view class="time-divider" v-for="(group, gIndex) in groupedMessages" :key="'time-' + gIndex">
          <text class="time-text">{{ group.timeLabel }}</text>
        </view>
        
        <view 
          class="message-item" 
          :class="[msg.isMine ? 'right' : 'left']"
          v-for="(msg, mIndex) in messageItems" 
          :key="msg.id"
        >
          <view class="avatar" :style="{ background: msg.isMine ? '#165DFF' : msg.avatarColor }" v-if="!msg.isMine || shouldShowAvatar(mIndex)">
            <text class="avatar-text">{{ msg.isMine ? '我' : msg.sender_name.charAt(0) }}</text>
          </view>
          <view class="avatar-placeholder" v-else></view>
          
          <view class="message-wrapper">
            <view class="message-bubble" :class="[msg.isMine ? 'right' : 'left']">
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
            </view>
            <text class="msg-time" v-if="shouldShowTime(mIndex)">{{ formatMsgTime(msg.created_at) }}</text>
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
        :cursor-spacing="20"
      />
      <button class="send-btn" :class="{ active: inputText.trim() }" @click="handleSend">
        <text>发送</text>
      </button>
    </view>
    <view class="safe-area"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../../../store/index'
import { imApi, type Message } from '../../../api/im'
import dayjs from 'dayjs'

const userStore = useUserStore()
const chatName = ref('')
const jobTitle = ref('')
const conversationId = ref('')
const inputText = ref('')
const scrollTop = ref(0)
const messages = ref<Message[]>([])
const page = ref(1)
const hasMore = ref(true)
const isLoading = ref(false)

interface MessageItem extends Message {
  isMine: boolean
  avatarColor: string
}

const messageItems = computed<MessageItem[]>(() => {
  const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#a8edea']
  return messages.value.map(msg => {
    const index = parseInt(msg.sender_id.replace(/\D/g, '')) % colors.length
    return {
      ...msg,
      isMine: msg.sender_id === userStore.user?.user_id,
      avatarColor: colors[index]
    }
  })
})

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

const shouldShowAvatar = (index: number) => {
  if (index === 0) return true
  const items = messageItems.value
  return items[index].sender_id !== items[index - 1].sender_id
}

const shouldShowTime = (index: number) => {
  if (index === messageItems.value.length - 1) return true
  const items = messageItems.value
  const current = dayjs(items[index].created_at)
  const next = dayjs(items[index + 1].created_at)
  return next.diff(current, 'minute') > 5
}

const formatMsgTime = (time: string) => {
  return dayjs(time).format('HH:mm')
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
    messages.value = [
      { id: '1', conversation_id: '1', sender_id: '1', sender_name: '长沙学思教育', sender_avatar: '', content: '您好，您的简历已收到，我们会尽快审核', message_type: 'text', created_at: dayjs().subtract(1, 'hour').toISOString(), is_read: true },
      { id: '2', conversation_id: '1', sender_id: userStore.user?.user_id || 'me', sender_name: '我', sender_avatar: '', content: '好的，谢谢！大概多久会有结果呢？', message_type: 'text', created_at: dayjs().subtract(50, 'minute').toISOString(), is_read: true },
      { id: '3', conversation_id: '1', sender_id: '1', sender_name: '长沙学思教育', sender_avatar: '', content: '我们会在3个工作日内给您答复，请耐心等待', message_type: 'text', created_at: dayjs().subtract(45, 'minute').toISOString(), is_read: true },
      { id: '4', conversation_id: '1', sender_id: '1', sender_name: '长沙学思教育', sender_avatar: '', content: '另外，方便的话可以先看一下我们公司的介绍哦', message_type: 'text', created_at: dayjs().subtract(10, 'minute').toISOString(), is_read: false }
    ]
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
  jobTitle.value = options.job_title || ''
  
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
  padding-top: calc(24rpx + env(safe-area-inset-top));
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 52rpx;
  color: #1F2329;
  font-weight: 300;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chat-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2329;
  line-height: 1.4;
}

.chat-subtitle {
  font-size: 24rpx;
  color: #86909C;
  margin-top: 4rpx;
}

.header-right {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.more-icon {
  font-size: 40rpx;
  color: #86909C;
}

.chat-content {
  flex: 1;
  padding: 24rpx 32rpx;
}

.message-list {
  padding-bottom: 40rpx;
}

.time-divider {
  display: flex;
  justify-content: center;
  margin: 32rpx 0;
}

.time-text {
  font-size: 24rpx;
  color: #86909C;
  background-color: #E5E6EB;
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
}

.message-item {
  display: flex;
  margin-bottom: 8rpx;
}

.message-item.right {
  flex-direction: row-reverse;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 72rpx;
  height: 72rpx;
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
  font-weight: 600;
}

.message-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-item.right .message-wrapper {
  align-items: flex-end;
}

.message-item.left .message-wrapper {
  align-items: flex-start;
}

.message-bubble {
  padding: 20rpx 28rpx;
  border-radius: 20rpx;
  position: relative;
  word-break: break-all;
}

.message-bubble.left {
  background-color: #FFFFFF;
  color: #1F2329;
  border-bottom-left-radius: 6rpx;
}

.message-bubble.right {
  background-color: #165DFF;
  color: #FFFFFF;
  border-bottom-right-radius: 6rpx;
}

.message-content {
  font-size: 28rpx;
  line-height: 1.6;
}

.message-image {
  max-width: 400rpx;
  max-height: 400rpx;
  border-radius: 12rpx;
}

.message-file {
  display: flex;
  align-items: center;
  padding: 8rpx 0;
}

.file-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.file-name {
  font-size: 26rpx;
  max-width: 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-time {
  font-size: 22rpx;
  color: #C9CDD4;
  margin-top: 8rpx;
}

.chat-footer {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  padding: 20rpx 24rpx;
  border-top: 1rpx solid #F2F3F5;
}

.footer-left {
  display: flex;
  margin-right: 12rpx;
}

.func-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8rpx;
}

.func-icon {
  font-size: 40rpx;
}

.input-box {
  flex: 1;
  font-size: 28rpx;
  padding: 20rpx 28rpx;
  background-color: #F2F3F5;
  border-radius: 40rpx;
  margin-right: 20rpx;
  color: #1F2329;
}

.send-btn {
  font-size: 28rpx;
  color: #FFFFFF;
  background-color: #C9CDD4;
  padding: 18rpx 36rpx;
  border-radius: 40rpx;
  border: none;
  font-weight: 500;
  transition: background-color 0.2s;
}

.send-btn.active {
  background-color: #165DFF;
}

.safe-area {
  height: env(safe-area-inset-bottom);
  background-color: #FFFFFF;
}
</style>
