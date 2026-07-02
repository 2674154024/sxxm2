<template>
  <view class="page">
    <view class="system-message" @click="handleSystemMessage">
      <view class="system-icon-wrap">
        <text class="system-icon">🔔</text>
      </view>
      <view class="system-info">
        <text class="system-title">系统通知</text>
        <text class="system-desc">您的薪资已到账，请查收</text>
      </view>
      <view class="system-badge" v-if="systemUnread > 0">
        <text class="badge-text">{{ systemUnread }}</text>
      </view>
      <text class="arrow">›</text>
    </view>

    <view class="section-title">
      <text class="title-text">消息列表</text>
    </view>

    <view class="conversation-list">
      <view 
        class="conversation-item" 
        v-for="item in conversations" 
        :key="item.id" 
        @click="handleChat(item)"
      >
        <view class="avatar" :style="{ background: getAvatarColor(item.id) }">
          <text class="avatar-text">{{ item.peer_name.charAt(0) }}</text>
        </view>
        <view class="conversation-content">
          <view class="conversation-header">
            <text class="conversation-name">{{ item.peer_name }}</text>
            <text class="conversation-time">{{ item.last_time }}</text>
          </view>
          <view class="conversation-footer">
            <text class="conversation-msg">{{ item.last_message }}</text>
            <view class="unread-dot" v-if="item.unread_count > 0">
              <text class="unread-text" v-if="item.unread_count <= 99">{{ item.unread_count }}</text>
              <text class="unread-text" v-else>99+</text>
            </view>
          </view>
        </view>
      </view>
      <view class="divider" v-for="(_, index) in conversations.slice(0, -1)" :key="'div-' + index"></view>
    </view>

    <view class="empty" v-if="conversations.length === 0">
      <text class="empty-icon">💬</text>
      <text class="empty-text">暂无消息</text>
      <text class="empty-tip">快去投递心仪的岗位吧</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { imApi, type Conversation } from '../../../api/im'

const systemUnread = ref(1)
const conversations = ref<Conversation[]>([
  { id: 'conv_1', peer_id: '1', peer_name: '长沙学思教育', peer_avatar: '', last_message: '您好，您的简历已收到，我们会尽快审核', last_time: '10:30', unread_count: 2 },
  { id: 'conv_2', peer_id: '2', peer_name: '步步高超市', peer_avatar: '', last_message: '下周六有空来面试吗？', last_time: '昨天', unread_count: 0 },
  { id: 'conv_4', peer_id: '4', peer_name: '芒果传媒', peer_avatar: '', last_message: '收到，我们会在3个工作日内回复', last_time: '周一', unread_count: 0 }
])

const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#a8edea']

const getAvatarColor = (id: string) => {
  const index = parseInt(id.replace(/\D/g, '')) % colors.length
  return colors[index]
}

const handleChat = (item: Conversation) => {
  uni.navigateTo({ url: `/pages/student/im/chat?conversation_id=${item.id}&name=${item.peer_name}` })
}

const handleSystemMessage = () => {
  uni.showToast({ title: '系统通知', icon: 'none' })
}

const loadConversations = async () => {
  try {
    const res = await imApi.getConversationList()
    if (res.data && res.data.list) {
      conversations.value = res.data.list.filter((item: Conversation) => item.peer_name !== '系统通知')
    }
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
}

onMounted(() => {
  loadConversations()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
  padding-bottom: 120rpx;
}

.system-message {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  padding: 32rpx;
  margin: 24rpx 32rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.system-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.system-icon {
  font-size: 40rpx;
}

.system-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.system-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.system-desc {
  font-size: 26rpx;
  color: #86909C;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.system-badge {
  background-color: #FF4D4F;
  border-radius: 20rpx;
  min-width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10rpx;
  margin-right: 16rpx;
}

.badge-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.arrow {
  font-size: 36rpx;
  color: #C9CDD4;
  font-weight: 300;
}

.section-title {
  padding: 0 32rpx;
  margin-bottom: 16rpx;
  margin-top: 8rpx;
}

.title-text {
  font-size: 26rpx;
  color: #86909C;
  font-weight: 500;
}

.conversation-list {
  background-color: #FFFFFF;
  margin: 0 32rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 28rpx 32rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 36rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.conversation-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.conversation-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2329;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 60%;
}

.conversation-time {
  font-size: 24rpx;
  color: #C9CDD4;
  flex-shrink: 0;
}

.conversation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-msg {
  flex: 1;
  font-size: 26rpx;
  color: #86909C;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}

.unread-dot {
  background-color: #FF4D4F;
  border-radius: 50%;
  min-width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10rpx;
  flex-shrink: 0;
}

.unread-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.divider {
  height: 1rpx;
  background-color: #F2F3F5;
  margin-left: 152rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 0;
}

.empty-icon {
  font-size: 160rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #1F2329;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: #86909C;
}
</style>
