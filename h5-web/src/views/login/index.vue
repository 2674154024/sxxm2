<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { sendSmsCode, phoneLogin, passwordLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginType = ref<'sms' | 'password'>('sms')
const phone = ref('')
const account = ref('')
const code = ref('')
const password = ref('')
const showPassword = ref(false)
const agreed = ref(false)
const sendingCode = ref(false)
const loggingIn = ref(false)
const countdown = ref(0)

const phoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value))
const accountValid = computed(() => account.value.length >= 4 && account.value.length <= 20)
const codeValid = computed(() => /^\d{4,6}$/.test(code.value))
const passwordValid = computed(() => password.value.length >= 6 && password.value.length <= 20)
const canSubmit = computed(() => {
  if (!agreed.value || loggingIn.value) return false
  if (loginType.value === 'sms') return phoneValid.value && codeValid.value
  return accountValid.value && passwordValid.value
})

let timer: any = null

async function handleSendCode() {
  if (!phoneValid.value || sendingCode.value || countdown.value > 0) {
    return
  }

  try {
    sendingCode.value = true
    const res = await sendSmsCode(phone.value)
    if (res.code === 200) {
      showToast({
        message: '验证码已发送',
        type: 'success',
      })
      countdown.value = 60
      startCountdown()
    }
  } catch (error: any) {
    showToast({
      message: error?.message || '发送失败',
      type: 'fail',
    })
  } finally {
    sendingCode.value = false
  }
}

function startCountdown() {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

async function handleLogin() {
  if (!canSubmit.value) return

  if (!agreed.value) {
    showToast({
      message: '请先同意用户协议和隐私政策',
    })
    return
  }

  try {
    loggingIn.value = true
    let res
    if (loginType.value === 'sms') {
      res = await phoneLogin(phone.value, code.value)
    } else {
      res = await passwordLogin(account.value, password.value)
    }
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo({
        user_id: res.data.userId,
        role: res.data.role,
        real_name: res.data.realName,
        verify_status: res.data.verifyStatus,
        credit_score: res.data.creditScore,
      })

      showToast({
        message: '登录成功',
        type: 'success',
      })

      setTimeout(() => {
        const redirect = route.query.redirect as string
        if (redirect) {
          router.replace(redirect)
        } else {
          router.replace('/')
        }
      }, 1000)
    }
  } catch (error: any) {
    showToast({
      message: error?.message || '登录失败',
      type: 'fail',
    })
  } finally {
    loggingIn.value = false
  }
}

function toggleLoginType() {
  loginType.value = loginType.value === 'sms' ? 'password' : 'sms'
}

function goToRegister() {
  router.push('/register')
}

function handleGoBack() {
  router.back()
}
</script>

<template>
  <div class="login-page">
    <NavBar title="登录" show-back @back="handleGoBack" />

    <div class="login-content">
      <div class="logo-section">
        <div class="logo">
          <span class="logo-icon">🎓</span>
        </div>
        <h1 class="app-name">长沙大学生兼职平台</h1>
        <p class="app-slogan">安全、靠谱、免费的大学生兼职平台</p>
      </div>

      <div class="form-section">
        <div class="login-tabs">
          <div
            class="tab-item"
            :class="{ active: loginType === 'sms' }"
            @click="loginType = 'sms'"
          >
            验证码登录
          </div>
          <div
            class="tab-item"
            :class="{ active: loginType === 'password' }"
            @click="loginType = 'password'"
          >
            密码登录
          </div>
        </div>

        <div class="form-item" v-if="loginType === 'sms'">
          <div class="input-group">
            <span class="prefix-text">+86</span>
            <input
              v-model="phone"
              type="tel"
              class="text-input"
              placeholder="请输入手机号"
              maxlength="11"
            />
            <button
              v-if="phone"
              class="clear-btn"
              @click="phone = ''"
              type="button"
            >
              ×
            </button>
          </div>
        </div>

        <div class="form-item" v-else>
          <div class="input-group">
            <span class="prefix-icon">👤</span>
            <input
              v-model="account"
              type="text"
              class="text-input"
              placeholder="请输入用户名"
              maxlength="20"
            />
            <button
              v-if="account"
              class="clear-btn"
              @click="account = ''"
              type="button"
            >
              ×
            </button>
          </div>
        </div>

        <div class="form-item" v-if="loginType === 'sms'">
          <div class="input-group code-group">
            <input
              v-model="code"
              type="text"
              class="text-input"
              placeholder="请输入验证码"
              maxlength="6"
            />
            <button
              class="code-btn"
              :class="{ disabled: !phoneValid || countdown > 0 }"
              :disabled="!phoneValid || countdown > 0 || sendingCode"
              @click="handleSendCode"
              type="button"
            >
              <template v-if="sendingCode">发送中...</template>
              <template v-else-if="countdown > 0">{{ countdown }}s后重发</template>
              <template v-else>获取验证码</template>
            </button>
          </div>
        </div>

        <div class="form-item" v-else>
          <div class="input-group password-group">
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              class="text-input"
              placeholder="请输入密码"
              maxlength="20"
            />
            <button
              class="eye-btn"
              @click="showPassword = !showPassword"
              type="button"
            >
              <span v-if="showPassword">👁️</span>
              <span v-else>🙈</span>
            </button>
          </div>
        </div>

        <button
          class="login-btn"
          :class="{ active: canSubmit, loading: loggingIn }"
          :disabled="!canSubmit"
          @click="handleLogin"
        >
          <template v-if="loggingIn">登录中...</template>
          <template v-else>登录</template>
        </button>

        <div class="form-links">
          <span class="link-text" @click="toggleLoginType">
            {{ loginType === 'sms' ? '密码登录' : '验证码登录' }}
          </span>
          <span class="link-text" @click="goToRegister">
            注册账号
          </span>
        </div>

        <div class="agreement-section">
          <div class="checkbox-wrap" @click="agreed = !agreed">
            <span class="checkbox" :class="{ checked: agreed }">
              <span v-if="agreed" class="check-icon">✓</span>
            </span>
            <span class="agreement-text">
              我已阅读并同意
              <span class="link">《用户协议》</span>
              和
              <span class="link">《隐私政策》</span>
            </span>
          </div>
        </div>
      </div>

      <div class="safety-tip">
        <div class="tip-icon">🛡️</div>
        <div class="tip-text">
          平台承诺：不收取任何费用，所有兼职信息经过严格审核
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background-color: var(--color-bg);
}

.login-content {
  padding: var(--spacing-xl) var(--spacing-lg);
  animation: fadeInUp 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.logo {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #4080ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-base);
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.3);
}

.logo-icon {
  font-size: 36px;
}

.app-name {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
}

.app-slogan {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin: 0;
}

.form-section {
  margin-bottom: var(--spacing-xl);
}

.login-tabs {
  display: flex;
  margin-bottom: var(--spacing-lg);
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-base);
  padding: 4px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: calc(var(--radius-base) - 4px);
  transition: all 0.2s ease;

  &.active {
    background-color: var(--color-bg);
    color: var(--color-primary);
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.form-item {
  margin-bottom: var(--spacing-base);
}

.input-group {
  display: flex;
  align-items: center;
  height: 48px;
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-base);
  padding: 0 var(--spacing-base);
  transition: all 0.2s ease;
  border: 2px solid transparent;

  &:focus-within {
    background-color: var(--color-bg);
    border-color: var(--color-primary);
  }
}

.prefix-text {
  font-size: var(--font-size-base);
  color: var(--color-text);
  margin-right: 8px;
  flex-shrink: 0;
}

.prefix-icon {
  font-size: 18px;
  margin-right: 8px;
  flex-shrink: 0;
}

.text-input {
  flex: 1;
  height: 100%;
  border: none;
  background: transparent;
  font-size: var(--font-size-base);
  color: var(--color-text);
  outline: none;

  &::placeholder {
    color: var(--color-text-placeholder);
  }
}

.clear-btn {
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background-color: var(--color-text-disabled);
  color: #fff;
  font-size: 14px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  padding: 0;

  &:active {
    background-color: var(--color-text-secondary);
  }
}

.code-group {
  .text-input {
    margin-right: 8px;
  }
}

.password-group {
  .text-input {
    margin-right: 8px;
  }
}

.eye-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  flex-shrink: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.code-btn {
  flex-shrink: 0;
  height: 32px;
  padding: 0 12px;
  border: none;
  border-radius: 16px;
  background-color: var(--color-primary-bg);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all 0.2s ease;

  &:active:not(.disabled):not(:disabled) {
    opacity: 0.8;
  }

  &.disabled,
  &:disabled {
    color: var(--color-text-disabled);
    background-color: var(--color-bg-secondary);
    cursor: not-allowed;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 24px;
  background-color: var(--color-text-disabled);
  color: #fff;
  font-size: var(--font-size-lg);
  font-weight: 500;
  cursor: not-allowed;
  transition: all 0.3s ease;
  margin-top: var(--spacing-lg);

  &.active {
    background-color: var(--color-primary);
    cursor: pointer;

    &:active {
      transform: scale(0.98);
      background-color: var(--color-primary-dark);
    }
  }

  &.loading {
    opacity: 0.7;
  }
}

.form-links {
  display: flex;
  justify-content: space-between;
  margin-top: var(--spacing-base);
}

.link-text {
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  cursor: pointer;

  &:active {
    opacity: 0.7;
  }
}

.agreement-section {
  margin-top: var(--spacing-base);
}

.checkbox-wrap {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
}

.checkbox {
  width: 18px;
  height: 18px;
  border: 1.5px solid var(--color-text-disabled);
  border-radius: 4px;
  margin-right: 8px;
  margin-top: 1px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &.checked {
    background-color: var(--color-primary);
    border-color: var(--color-primary);
    animation: checkBounce 0.3s cubic-bezier(0.68, -0.55, 0.27, 1.55);
  }
}

@keyframes checkBounce {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.check-icon {
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  line-height: 1;
}

.agreement-text {
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.link {
  color: var(--color-primary);
}

.safety-tip {
  display: flex;
  align-items: flex-start;
  padding: var(--spacing-base);
  background-color: var(--color-success-bg);
  border-radius: var(--radius-base);
}

.tip-icon {
  font-size: 20px;
  margin-right: 8px;
  flex-shrink: 0;
}

.tip-text {
  flex: 1;
  font-size: 12px;
  color: var(--color-success);
  line-height: 1.5;
}
</style>
