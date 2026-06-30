<template>
  <view class="page">
    <view class="login-header">
      <text class="logo">🎓</text>
      <text class="title">长沙大学生兼职平台</text>
      <text class="subtitle">安全可靠的兼职服务</text>
    </view>

    <view class="form-wrap">
      <view class="form-tabs">
        <view class="tab-item" :class="{ active: loginType === 'phone' }" @click="loginType = 'phone'">
          <text class="tab-text">手机号登录</text>
        </view>
        <view class="tab-item" :class="{ active: loginType === 'wechat' }" @click="loginType = 'wechat'">
          <text class="tab-text">微信登录</text>
        </view>
      </view>

      <view class="form" v-if="loginType === 'phone'">
        <view class="form-item">
          <text class="form-label">手机号码</text>
          <input class="form-input" placeholder="请输入手机号码" v-model="phone" type="number" maxlength="11" />
        </view>
        <view class="form-item">
          <text class="form-label">验证码</text>
          <view class="form-row">
            <input class="form-input code" placeholder="请输入验证码" v-model="code" type="number" maxlength="6" />
            <button class="code-btn" :class="{ disabled: !canGetCode }" @click="handleGetCode">
              <text>{{ codeBtnText }}</text>
            </button>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">密码（选填）</text>
          <input class="form-input" placeholder="设置登录密码" v-model="password" password />
        </view>
        <view class="form-agree">
          <view class="agree-checkbox" :class="{ checked: agree }" @click="agree = !agree">
            <text class="checkbox-icon" v-if="agree">✓</text>
          </view>
          <text class="agree-text">我已阅读并同意</text>
          <text class="agree-link">《用户协议》</text>
          <text class="agree-text">和</text>
          <text class="agree-link">《隐私政策》</text>
        </view>
        <button class="login-btn" @click="handlePhoneLogin">登录</button>
      </view>

      <view class="wechat-login" v-else>
        <view class="wechat-btn" @click="handleWechatLogin">
          <text class="wechat-icon">💬</text>
          <text class="wechat-text">微信授权登录</text>
        </view>
        <text class="wechat-tip">登录即表示同意《用户协议》和《隐私政策》</text>
      </view>

      <view class="other-actions">
        <text class="action-link">忘记密码？</text>
        <text class="action-text">|</text>
        <text class="action-link">注册账号</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/store'

const userStore = useUserStore()

const loginType = ref('phone')
const phone = ref('')
const code = ref('')
const password = ref('')
const agree = ref(true)
const canGetCode = ref(true)
const codeBtnText = ref('获取验证码')
const countdown = ref(60)

const handleGetCode = () => {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!canGetCode.value) {
    return
  }
  canGetCode.value = false
  codeBtnText.value = `${countdown.value}s`
  const timer = setInterval(() => {
    countdown.value--
    codeBtnText.value = `${countdown.value}s`
    if (countdown.value <= 0) {
      clearInterval(timer)
      countdown.value = 60
      canGetCode.value = true
      codeBtnText.value = '获取验证码'
    }
  }, 1000)
  uni.showToast({ title: '验证码已发送', icon: 'success' })
}

const handlePhoneLogin = () => {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value || code.value.length !== 6) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  if (!agree.value) {
    uni.showToast({ title: '请同意用户协议', icon: 'none' })
    return
  }
  uni.showLoading({ title: '登录中...' })
  setTimeout(() => {
    uni.hideLoading()
    userStore.login('mock_token_123', {
      user_id: '1',
      real_name: '张三',
      phone: '138****8888',
      school_id: '1',
      school_name: '湖南大学',
      verify_status: 2,
      avatar: ''
    })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1500)
}

const handleWechatLogin = () => {
  uni.showLoading({ title: '授权中...' })
  setTimeout(() => {
    uni.hideLoading()
    userStore.login('mock_token_wx', {
      user_id: '2',
      real_name: '微信用户',
      phone: '',
      school_id: '',
      school_name: '',
      verify_status: 0,
      avatar: ''
    })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1500)
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
}

.login-header {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.logo {
  font-size: 160rpx;
  margin-bottom: 32rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #FFFFFF;
  margin-bottom: 16rpx;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.form-wrap {
  background-color: #FFFFFF;
  border-radius: 40rpx 40rpx 0 0;
  padding: 48rpx 32rpx;
  padding-bottom: calc(48rpx + env(safe-area-inset-bottom));
}

.form-tabs {
  display: flex;
  margin-bottom: 48rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  position: relative;
}

.tab-item.active {
  .tab-text {
    color: #165DFF;
    font-weight: bold;
  }
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 60rpx;
    height: 6rpx;
    background-color: #165DFF;
    border-radius: 3rpx;
  }
}

.tab-text {
  font-size: 32rpx;
  color: #86909C;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  font-size: 28rpx;
  color: #4E5969;
  margin-bottom: 12rpx;
  display: block;
}

.form-input {
  font-size: 30rpx;
  color: #1F2329;
  padding: 24rpx;
  background-color: #F8F9FA;
  border-radius: 12rpx;
}

.form-row {
  display: flex;
  gap: 16rpx;
}

.form-input.code {
  flex: 1;
}

.code-btn {
  font-size: 26rpx;
  color: #165DFF;
  background-color: #E8F0FF;
  padding: 24rpx 32rpx;
  border-radius: 12rpx;
  border: none;
}

.code-btn.disabled {
  color: #C9CDD4;
  background-color: #F2F3F5;
}

.form-agree {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
  flex-wrap: wrap;
}

.agree-checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid #C9CDD4;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12rpx;
}

.agree-checkbox.checked {
  background-color: #165DFF;
  border-color: #165DFF;
}

.checkbox-icon {
  font-size: 22rpx;
  color: #FFFFFF;
}

.agree-text {
  font-size: 24rpx;
  color: #86909C;
}

.agree-link {
  font-size: 24rpx;
  color: #165DFF;
}

.login-btn {
  width: 100%;
  font-size: 32rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 28rpx;
  border-radius: 48rpx;
  border: none;
  margin-bottom: 32rpx;
}

.wechat-login {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wechat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  font-size: 32rpx;
  color: #FFFFFF;
  background-color: #07C160;
  padding: 28rpx;
  border-radius: 48rpx;
  margin-bottom: 24rpx;
}

.wechat-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.wechat-text {
  font-weight: bold;
}

.wechat-tip {
  font-size: 24rpx;
  color: #86909C;
  margin-bottom: 32rpx;
}

.other-actions {
  display: flex;
  justify-content: center;
  align-items: center;
}

.action-link {
  font-size: 26rpx;
  color: #165DFF;
}

.action-text {
  font-size: 26rpx;
  color: #C9CDD4;
  margin: 0 24rpx;
}
</style>