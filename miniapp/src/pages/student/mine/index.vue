<template>
  <view class="page">
    <view class="user-header">
      <view class="user-info">
        <view class="avatar" :style="{ background: avatarColor }">
          <text class="avatar-text">{{ user.real_name ? user.real_name.charAt(0) : '?' }}</text>
        </view>
        <view class="user-detail">
          <text class="user-name">{{ user.real_name || '未登录' }}</text>
          <text class="user-phone">{{ user.phone || '点击登录' }}</text>
        </view>
      </view>
      <view class="verify-badge" :class="verifyClass">
        <text class="verify-text">{{ verifyText }}</text>
      </view>
    </view>

    <view class="credit-section" v-if="isLogin">
      <view class="credit-card">
        <view class="credit-info">
          <text class="credit-label">信用分</text>
          <text class="credit-score">{{ creditScore }}</text>
        </view>
        <view class="credit-bar">
          <view class="credit-progress" :style="{ width: creditPercent + '%', background: creditColor }"></view>
        </view>
        <text class="credit-tip">{{ creditTip }}</text>
      </view>
    </view>

    <view class="menu-grid">
      <view class="grid-item" @click="navigateTo('/pages/student/resume/edit')">
        <view class="grid-icon">📝</view>
        <text class="grid-text">我的简历</text>
      </view>
      <view class="grid-item" @click="navigateTo('/pages/student/apply/list')">
        <view class="grid-icon">📋</view>
        <text class="grid-text">投递记录</text>
      </view>
      <view class="grid-item" @click="navigateTo('/pages/student/salary/index')">
        <view class="grid-icon">💰</view>
        <text class="grid-text">薪资流水</text>
      </view>
      <view class="grid-item" @click="handleAgreement">
        <view class="grid-icon">📄</view>
        <text class="grid-text">协议中心</text>
      </view>
      <view class="grid-item" @click="handleInsurance">
        <view class="grid-icon">🛡️</view>
        <text class="grid-text">保险记录</text>
      </view>
      <view class="grid-item" @click="handleReport">
        <view class="grid-icon">📊</view>
        <text class="grid-text">实践报告</text>
      </view>
      <view class="grid-item" @click="navigateTo('/pages/student/safety/index')">
        <view class="grid-icon">🔒</view>
        <text class="grid-text">安全中心</text>
      </view>
      <view class="grid-item" @click="navigateTo('/pages/student/complaint/create')">
        <view class="grid-icon">⚠️</view>
        <text class="grid-text">投诉记录</text>
      </view>
      <view class="grid-item" @click="handleSettings">
        <view class="grid-icon">⚙️</view>
        <text class="grid-text">设置</text>
      </view>
    </view>

    <view class="logout-btn-wrap">
      <button class="logout-btn" @click="handleLogout" v-if="isLogin">退出登录</button>
      <button class="login-btn" @click="handleLogin" v-else>去登录</button>
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

const verifyClass = computed(() => {
  const status = user.value.verify_status || 0
  switch (status) {
    case 2:
      return 'verified'
    case 1:
      return 'pending'
    case 3:
      return 'rejected'
    default:
      return 'unverified'
  }
})

const verifyText = computed(() => {
  const status = user.value.verify_status || 0
  switch (status) {
    case 2:
      return '已认证'
    case 1:
      return '审核中'
    case 3:
      return '认证失败'
    default:
      return '未认证'
  }
})

const creditPercent = computed(() => {
  return Math.min((creditScore.value / 100) * 100, 100)
})

const creditColor = computed(() => {
  if (creditScore.value >= 80) return '#52C41A'
  if (creditScore.value >= 60) return '#FAAD14'
  return '#FF4D4F'
})

const creditTip = computed(() => {
  if (creditScore.value >= 90) return '信用优秀，享受优先推荐'
  if (creditScore.value >= 80) return '信用良好，请继续保持'
  if (creditScore.value >= 60) return '信用一般，注意履约'
  return '信用较低，请注意提升'
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

const handleAgreement = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const handleInsurance = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const handleReport = () => {
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

onMounted(() => {
  loadCreditScore()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
}

.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.avatar-text {
  font-size: 48rpx;
  color: #FFFFFF;
  font-weight: bold;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #FFFFFF;
  margin-bottom: 8rpx;
}

.user-phone {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.verify-badge {
  font-size: 24rpx;
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
}

.verify-badge.verified {
  background-color: rgba(82, 196, 26, 0.2);
  color: #52C41A;
}

.verify-badge.pending {
  background-color: rgba(250, 173, 20, 0.2);
  color: #FAAD14;
}

.verify-badge.rejected {
  background-color: rgba(255, 77, 79, 0.2);
  color: #FF4D4F;
}

.verify-badge.unverified {
  background-color: rgba(255, 255, 255, 0.2);
  color: #FFFFFF;
}

.credit-section {
  padding: 0 24rpx;
  margin-top: -40rpx;
}

.credit-card {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.credit-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.credit-label {
  font-size: 28rpx;
  color: #86909C;
}

.credit-score {
  font-size: 56rpx;
  font-weight: bold;
  color: #165DFF;
}

.credit-bar {
  height: 12rpx;
  background-color: #F2F3F5;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.credit-progress {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.5s;
}

.credit-tip {
  font-size: 24rpx;
  color: #86909C;
}

.menu-grid {
  display: flex;
  flex-wrap: wrap;
  background-color: #FFFFFF;
  margin: 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.grid-item {
  width: 33.33%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 0;
}

.grid-icon {
  font-size: 48rpx;
  margin-bottom: 16rpx;
}

.grid-text {
  font-size: 26rpx;
  color: #4E5969;
}

.logout-btn-wrap {
  padding: 48rpx 24rpx;
}

.logout-btn, .login-btn {
  width: 100%;
  font-size: 32rpx;
  padding: 28rpx;
  border-radius: 48rpx;
  border: 2rpx solid #165DFF;
}

.logout-btn {
  background-color: #FFFFFF;
  color: #165DFF;
}

.login-btn {
  background-color: #165DFF;
  color: #FFFFFF;
  border: none;
}
</style>