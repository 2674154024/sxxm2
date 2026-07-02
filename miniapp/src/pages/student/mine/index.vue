<template>
  <view class="page">
    <view class="header-bg">
      <view class="user-header">
        <view class="user-info">
          <view class="avatar" :style="{ background: avatarColor }">
            <text class="avatar-text">{{ user.real_name ? user.real_name.charAt(0) : '?' }}</text>
          </view>
          <view class="user-detail">
            <view class="name-row">
              <text class="user-name">{{ user.real_name || '未登录' }}</text>
              <view class="credit-badge" v-if="isLogin">
                <text class="credit-icon">⭐</text>
                <text class="credit-text">{{ creditScore }}分</text>
              </view>
            </view>
            <text class="user-school" v-if="isLogin">{{ user.school || '湖南大学' }} · {{ user.major || '计算机科学与技术' }}</text>
            <text class="user-tip" v-else>点击登录享受更多服务</text>
          </view>
        </view>
        <view class="edit-btn" @click="handleEditProfile" v-if="isLogin">
          <text class="edit-icon">✏️</text>
        </view>
      </view>

      <view class="stats-card" v-if="isLogin">
        <view class="stat-item" @click="navigateTo('/pages/student/apply/list')">
          <text class="stat-num">{{ stats.applyCount }}</text>
          <text class="stat-label">投递数</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @click="handleFavorite">
          <text class="stat-num">{{ stats.favoriteCount }}</text>
          <text class="stat-label">收藏数</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @click="navigateTo('/pages/student/im/conversation')">
          <text class="stat-num">{{ stats.chatCount }}</text>
          <text class="stat-label">沟通数</text>
        </view>
      </view>
    </view>

    <view class="menu-section">
      <view class="section-title">
        <text class="title-text">我的服务</text>
      </view>
      
      <view class="menu-list">
        <view class="menu-item" @click="navigateTo('/pages/student/resume/edit')">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="menu-icon">📝</text>
            </view>
            <text class="menu-title">我的简历</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/student/salary/index')">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="menu-icon">💰</text>
            </view>
            <text class="menu-title">薪资流水</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/student/clock/index')">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="menu-icon">⏰</text>
            </view>
            <text class="menu-title">打卡记录</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="handleFavorite">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="menu-icon">❤️</text>
            </view>
            <text class="menu-title">我的收藏</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <view class="menu-section">
      <view class="section-title">
        <text class="title-text">其他</text>
      </view>
      
      <view class="menu-list">
        <view class="menu-item" @click="navigateTo('/pages/student/safety/index')">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="menu-icon">🔒</text>
            </view>
            <text class="menu-title">安全中心</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="handleFeedback">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="menu-icon">💬</text>
            </view>
            <text class="menu-title">意见反馈</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="handleSettings">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="menu-icon">⚙️</text>
            </view>
            <text class="menu-title">设置</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <view class="logout-section" v-if="isLogin">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <view class="login-section" v-else>
      <button class="login-btn" @click="handleLogin">立即登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useUserStore } from '@/store'

const userStore = useUserStore()

const user = computed(() => userStore.user || {})
const isLogin = computed(() => userStore.isLogin)
const creditScore = ref(85)

const stats = ref({
  applyCount: 12,
  favoriteCount: 8,
  chatCount: 5
})

const avatarColor = computed(() => {
  const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a']
  if (user.value.real_name) {
    let hash = 0
    for (let i = 0; i < user.value.real_name.length; i++) {
      hash = user.value.real_name.charCodeAt(i) + ((hash << 5) - hash)
    }
    return colors[Math.abs(hash) % colors.length]
  }
  return '#86909C'
})

const navigateTo = (url: string) => {
  if (!isLogin.value && url !== '/pages/student/login/index') {
    uni.navigateTo({ url: '/pages/student/login/index' })
    return
  }
  uni.navigateTo({ url })
}

const handleLogin = () => {
  uni.navigateTo({ url: '/pages/student/login/index' })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.showToast({ title: '已退出', icon: 'success' })
      }
    }
  })
}

const handleEditProfile = () => {
  uni.navigateTo({ url: '/pages/student/resume/edit' })
}

const handleFavorite = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const handleFeedback = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const handleSettings = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const loadCreditScore = () => {
  if (isLogin.value) {
    creditScore.value = 85 + Math.floor(Math.random() * 15)
  }
}

const loadStats = () => {
  if (isLogin.value) {
    stats.value = {
      applyCount: Math.floor(Math.random() * 20) + 5,
      favoriteCount: Math.floor(Math.random() * 15) + 3,
      chatCount: Math.floor(Math.random() * 10) + 1
    }
  }
}

onMounted(() => {
  loadCreditScore()
  loadStats()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #165DFF;
  padding-bottom: 60rpx;
}

.header-bg {
  background: #165DFF;
  padding-bottom: 100rpx;
  padding-top: env(safe-area-inset-top);
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 48rpx 32rpx 32rpx;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 28rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.avatar-text {
  font-size: 52rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.user-detail {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.user-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-right: 16rpx;
}

.credit-badge {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.credit-icon {
  font-size: 24rpx;
  margin-right: 6rpx;
}

.credit-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.user-school {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
}

.user-tip {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
}

.edit-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  flex-shrink: 0;
}

.edit-icon {
  font-size: 28rpx;
}

.stats-card {
  display: flex;
  background-color: rgba(255, 255, 255, 0.2);
  margin: 0 32rpx;
  margin-top: -20rpx;
  border-radius: 16rpx;
  padding: 32rpx 0;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 40rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background-color: rgba(255, 255, 255, 0.3);
  align-self: center;
}

.menu-section {
  margin-top: 32rpx;
  padding: 0 32rpx;
}

.section-title {
  margin-bottom: 16rpx;
}

.title-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.menu-list {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  height: 96rpx;
}

.menu-item + .menu-item {
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  background-color: rgba(255, 255, 255, 0.2);
}

.menu-icon {
  font-size: 28rpx;
}

.menu-title {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.menu-arrow {
  font-size: 36rpx;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
}

.logout-section {
  margin-top: 48rpx;
  padding: 0 32rpx;
}

.logout-btn {
  width: 100%;
  font-size: 30rpx;
  color: #FFFFFF;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 28rpx;
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  font-weight: 500;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.login-section {
  margin-top: 48rpx;
  padding: 0 32rpx;
}

.login-btn {
  width: 100%;
  font-size: 30rpx;
  color: #165DFF;
  background-color: #FFFFFF;
  padding: 28rpx;
  border-radius: 16rpx;
  border: none;
  font-weight: 500;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
}
</style>
