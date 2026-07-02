<template>
  <view class="page">
    <view class="login-header">
      <view class="logo-wrap">
        <text class="logo-icon">🎓</text>
      </view>
      <text class="title">长沙大学生兼职平台</text>
      <text class="subtitle">安全可靠的兼职服务平台</text>
    </view>

    <view class="form-wrap">
      <view class="form-tabs">
        <view class="tab-item" :class="{ active: loginType === 'password' }" @click="loginType = 'password'">
          <text class="tab-text">密码登录</text>
        </view>
        <view class="tab-item" :class="{ active: loginType === 'code' }" @click="loginType = 'code'">
          <text class="tab-text">验证码登录</text>
        </view>
        <view class="tab-item" :class="{ active: loginType === 'wechat' }" @click="loginType = 'wechat'">
          <text class="tab-text">微信登录</text>
        </view>
      </view>

      <view class="form" v-if="loginType === 'password'">
        <view class="input-group">
          <view class="input-icon">📱</view>
          <input class="form-input" placeholder="请输入手机号/用户名" v-model="account" type="text" maxlength="20" placeholder-class="input-placeholder" />
        </view>

        <view class="input-group">
          <view class="input-icon">🔐</view>
          <input class="form-input" placeholder="请输入密码" v-model="password" :password="!showPassword" placeholder-class="input-placeholder" />
          <view class="input-suffix" @click="showPassword = !showPassword">
            <text class="suffix-icon">{{ showPassword ? '👁️' : '👁️‍🗨️' }}</text>
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

        <button class="login-btn" @click="handlePasswordLogin">登录</button>

        <view class="other-actions">
          <text class="action-link" @click="goToForget">忘记密码？</text>
          <text class="action-link" @click="loginType = 'code'">验证码登录</text>
        </view>
      </view>

      <view class="form" v-else-if="loginType === 'code'">
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

        <view class="form-agree">
          <view class="agree-checkbox" :class="{ checked: agree }" @click="agree = !agree">
            <text class="checkbox-icon" v-if="agree">✓</text>
          </view>
          <text class="agree-text">我已阅读并同意</text>
          <text class="agree-link">《用户协议》</text>
          <text class="agree-text">和</text>
          <text class="agree-link">《隐私政策》</text>
        </view>

        <button class="login-btn" @click="handleCodeLogin">登录</button>

        <view class="other-actions">
          <text class="action-link" @click="loginType = 'password'">密码登录</text>
          <text class="action-link" @click="goToRegister">注册账号</text>
        </view>
      </view>

      <view class="wechat-login" v-else>
        <view class="wechat-btn" @click="handleWechatLogin">
          <text class="wechat-icon">💬</text>
          <text class="wechat-text">微信授权登录</text>
        </view>
        <text class="wechat-tip">登录即表示同意《用户协议》和《隐私政策》</text>

        <view class="other-login-ways">
          <text class="other-text">其他登录方式</text>
          <view class="other-links">
            <text class="action-link" @click="loginType = 'password'">密码登录</text>
            <text class="divider">|</text>
            <text class="action-link" @click="loginType = 'code'">验证码登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/store'
import { login } from '@/api/auth'

const userStore = useUserStore()

const loginType = ref('password')
const account = ref('')
const phone = ref('')
const code = ref('')
const password = ref('')
const showPassword = ref(false)
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

const handleLoginSuccess = (token: string, userInfo: Record<string, any>) => {
  userStore.login(token, userInfo)
  uni.showToast({ title: '登录成功', icon: 'success' })
  setTimeout(() => {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      uni.navigateBack()
    } else {
      uni.switchTab({ url: '/pages/student/index/index' })
    }
  }, 1500)
}

const handlePasswordLogin = () => {
  if (!account.value) {
    uni.showToast({ title: '请输入手机号或用户名', icon: 'none' })
    return
  }
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  if (!agree.value) {
    uni.showToast({ title: '请同意用户协议', icon: 'none' })
    return
  }
  uni.showLoading({ title: '登录中...' })
  login.passwordLogin({ account: account.value, password: password.value }).then(res => {
    uni.hideLoading()
    handleLoginSuccess(res.data.token, {
      user_id: res.data.userId,
      real_name: res.data.realName || '',
      phone: '',
      school_id: '',
      school_name: '',
      verify_status: res.data.verifyStatus,
      avatar: ''
    })
  }).catch(() => {
    uni.hideLoading()
  })
}

const handleCodeLogin = () => {
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
    handleLoginSuccess('mock_token_code', {
      user_id: '1',
      real_name: '张三',
      phone: phone.value.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2'),
      school_id: '1',
      school_name: '湖南大学',
      verify_status: 2,
      avatar: ''
    })
  }, 1500)
}

const handleWechatLogin = () => {
  uni.showLoading({ title: '授权中...' })
  setTimeout(() => {
    uni.hideLoading()
    handleLoginSuccess('mock_token_wx', {
      user_id: '2',
      real_name: '微信用户',
      phone: '',
      school_id: '',
      school_name: '',
      verify_status: 0,
      avatar: ''
    })
  }, 1500)
}

const goToForget = () => {
  uni.showToast({ title: '忘记密码功能开发中', icon: 'none' })
}

const goToRegister = () => {
  uni.navigateTo({ url: '/pages/student/login/register' })
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

.form-tabs {
  display: flex;
  margin-bottom: 48rpx;
  position: relative;
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
    font-weight: 600;
    font-size: 34rpx;
  }
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 48rpx;
    height: 6rpx;
    background: linear-gradient(90deg, #165DFF 0%, #4080FF 100%);
    border-radius: 3rpx;
  }
}

.tab-text {
  font-size: 30rpx;
  color: #86909C;
  transition: all 0.3s;
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

.login-btn {
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

.login-btn::after {
  border: none;
}

.other-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8rpx;
}

.action-link {
  font-size: 26rpx;
  color: #165DFF;
}

.wechat-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40rpx;
}

.wechat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 88rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
  background-color: #07C160;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.3);
}

.wechat-icon {
  font-size: 40rpx;
  margin-right: 12rpx;
}

.wechat-text {
  font-weight: 500;
}

.wechat-tip {
  font-size: 24rpx;
  color: #86909C;
  margin-bottom: 48rpx;
}

.other-login-ways {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin-top: 40rpx;
  padding-top: 40rpx;
  border-top: 1rpx solid #F2F3F5;
}

.other-text {
  font-size: 24rpx;
  color: #C9CDD4;
  margin-bottom: 24rpx;
}

.other-links {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.divider {
  font-size: 24rpx;
  color: #E5E6EB;
}
</style>
