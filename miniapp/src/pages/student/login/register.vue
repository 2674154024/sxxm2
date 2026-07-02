<template>
  <view class="page">
    <view class="login-header">
      <view class="logo-wrap">
        <text class="logo-icon">🎓</text>
      </view>
      <text class="title">注册账号</text>
      <text class="subtitle">加入长沙大学生兼职平台</text>
    </view>

    <view class="form-wrap">
      <view class="form">
        <view class="input-group">
          <view class="input-icon">📱</view>
          <input class="form-input" placeholder="请输入手机号码" v-model="phone" type="number" maxlength="11" placeholder-class="input-placeholder" />
        </view>

        <view class="input-group">
          <view class="input-icon">🔢</view>
          <input class="form-input code-input" placeholder="请输入验证码" v-model="code" type="number" maxlength="6" placeholder-class="input-placeholder" />
          <view class="code-btn" :class="{ disabled: !canGetCode }" @click="handleGetCode">
            <text>{{ codeBtnText }}</text>
          </view>
        </view>

        <view class="input-group">
          <view class="input-icon">🔐</view>
          <input class="form-input" placeholder="请设置密码（6-20位）" v-model="password" :password="!showPassword" placeholder-class="input-placeholder" />
          <view class="input-suffix" @click="showPassword = !showPassword">
            <text class="suffix-icon">{{ showPassword ? '👁️' : '👁️‍🗨️' }}</text>
          </view>
        </view>

        <view class="input-group">
          <view class="input-icon">🔐</view>
          <input class="form-input" placeholder="请确认密码" v-model="confirmPassword" :password="!showConfirmPassword" placeholder-class="input-placeholder" />
          <view class="input-suffix" @click="showConfirmPassword = !showConfirmPassword">
            <text class="suffix-icon">{{ showConfirmPassword ? '👁️' : '👁️‍🗨️' }}</text>
          </view>
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

        <button class="register-btn" @click="handleRegister">注册</button>

        <view class="login-link">
          <text class="login-text">已有账号？</text>
          <text class="action-link" @click="goToLogin">立即登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const phone = ref('')
const code = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
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

const handleRegister = () => {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value || code.value.length !== 6) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  if (!password.value || password.value.length < 6 || password.value.length > 20) {
    uni.showToast({ title: '请设置6-20位密码', icon: 'none' })
    return
  }
  if (password.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
    return
  }
  if (!agree.value) {
    uni.showToast({ title: '请同意用户协议', icon: 'none' })
    return
  }
  uni.showLoading({ title: '注册中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1500)
}

const goToLogin = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #165DFF 0%, #4080FF 50%, #F2F3F5 50%);
  display: flex;
  flex-direction: column;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0 80rpx;
}

.logo-wrap {
  width: 140rpx;
  height: 140rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.logo-icon {
  font-size: 80rpx;
}

.title {
  font-size: 44rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 12rpx;
}

.subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.form-wrap {
  flex: 1;
  background-color: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  padding: 48rpx 32rpx;
  padding-bottom: calc(48rpx + env(safe-area-inset-bottom));
  margin: 0 24rpx;
}

.form {
  display: flex;
  flex-direction: column;
}

.input-group {
  display: flex;
  align-items: center;
  height: 88rpx;
  background-color: #F7F8FA;
  border-radius: 16rpx;
  margin-bottom: 28rpx;
  padding: 0 24rpx;
  position: relative;
}

.input-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
  opacity: 0.6;
}

.form-input {
  flex: 1;
  height: 88rpx;
  font-size: 30rpx;
  color: #1F2329;
}

.input-placeholder {
  color: #C9CDD4;
  font-size: 28rpx;
}

.input-suffix {
  padding: 0 12rpx;
}

.suffix-icon {
  font-size: 36rpx;
  opacity: 0.5;
}

.code-input {
  padding-right: 20rpx;
}

.code-btn {
  font-size: 26rpx;
  color: #165DFF;
  background-color: #E8F0FF;
  padding: 0 24rpx;
  height: 56rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
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
  flex-shrink: 0;
}

.agree-checkbox.checked {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
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

.register-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
  background: linear-gradient(90deg, #165DFF 0%, #4080FF 100%);
  border-radius: 24rpx;
  border: none;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(22, 93, 255, 0.3);
}

.register-btn::after {
  border: none;
}

.login-link {
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-text {
  font-size: 26rpx;
  color: #86909C;
}

.action-link {
  font-size: 26rpx;
  color: #165DFF;
}
</style>
