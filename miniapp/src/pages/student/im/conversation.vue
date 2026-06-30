<template>
  <view class="page">
    <view class="conversation-list">
      <view class="conversation-item" v-for="item in conversations" :key="item.id" @click="handleChat(item)">
        <view class="avatar" :style="{ background: getAvatarColor(item.id) }">
          <text class="avatar-text">{{ item.peer_name.charAt(0) }}</text>
        </view>
        <view class="conversation-info">
          <view class="conversation-header">
            <text class="conversation-name">{{ item.peer_name }}</text>
            <text class="conversation-time">{{ item.last_time }}</text>
          </view>
          <view class="conversation-footer">
            <text class="conversation-content">{{ item.last_message }}</text>
            <view class="unread-badge" v-if="item.unread_count > 0">
              <text class="unread-num">{{ item.unread_count }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="empty" v-if="conversations.length === 0">
      <text class="empty-icon">💬</text>
      <text class="empty-text">暂无消息</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { imApi, type Conversation } from '../../../api/im'

const conversations = ref<Conversation[]>([
  { id: 'conv_1', peer_id: '1', peer_name: '长沙学思教育', peer_avatar: '', last_message: '您好，您的简历已收到，我们会尽快审核', last_time: '10:30', unread_count: 2 },
  { id: 'conv_2', peer_id: '2', peer_name: '步步高超市', peer_avatar: '', last_message: '下周六有空来面试吗？', last_time: '昨天', unread_count: 0 },
  { id: 'conv_3', peer_id: '3', peer_name: '系统通知', peer_avatar: '', last_message: '您的薪资已到账，请查收', last_time: '昨天', unread_count: 1 },
  { id: 'conv_4', peer_id: '4', peer_name: '芒果传媒', peer_avatar: '', last_message: '收到，我们会在3个工作日内回复', last_time: '周一', unread_count: 0 }
])

const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#a8edea']

const getAvatarColor = (id: string) => {
  const index = parseInt(id) % colors.length
  return colors[index]
}

const handleChat = (item: Conversation) => {
  uni.navigateTo({ url: `/pages/student/im/chat?conversation_id=${item.id}&name=${item.peer_name}` })
}

const loadConversations = async () => {
  try {
    const res = await imApi.getConversationList()
    if (res.data && res.data.list) {
      conversations.value = res.data.list
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
}

.conversation-list {
  padding: 24rpx;
}

.conversation-item {
  display: flex;
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.avatar-text {
  font-size: 36rpx;
  color: #FFFFFF;
  font-weight: bold;
}

.conversation-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.conversation-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1F2329;
}

.conversation-time {
  font-size: 24rpx;
  color: #C9CDD4;
}

.conversation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-content {
  flex: 1;
  font-size: 26rpx;
  color: #86909C;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  background-color: #FF4D4F;
  border-radius: 50%;
  min-width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12rpx;
  margin-left: 16rpx;
}

.unread-num {
  font-size: 22rpx;
  color: #FFFFFF;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #86909C;
}
</style>