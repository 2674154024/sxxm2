<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { sendSmsCode, register } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const phone = ref('')
const code = ref('')
const password = ref('')
const confirmPassword = ref('')
const nickname = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const agreed = ref(false)
const sendingCode = ref(false)
const registering = ref(false)
const countdown = ref(0)

const phoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value))
const codeValid = computed(() => /^\d{4,6}$/.test(code.value))
const passwordValid = computed(() => password.value.length >= 6 && password.value.length <= 20)
const confirmPasswordValid = computed(() => confirmPassword.value === password.value && passwordValid.value)
const nicknameValid = computed(() => nickname.value.length >= 2 && nickname.value.length <= 20)
const canSubmit = computed(() => {
  return phoneValid.value &&
    codeValid.value &&
    passwordValid.value &&
    confirmPasswordValid.value &&
    agreed.value &&
    !registering.value
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

async function handleRegister() {
  if (!canSubmit.value) return

  if (!agreed.value) {
    showToast({
      message: '请先同意用户协议和隐私政策',
    })
    return
  }

  if (!confirmPasswordValid.value) {
    showToast({
      message: '两次密码输入不一致',
      type: 'fail',
    })
    return
  }

  try {
    registering.value = true
    const res = await register(phone.value, code.value, password.value, nickname.value)
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo({
        user_id: res.data.user_info?.userId,
        role: res.data.user_info?.role,
        real_name: res.data.user_info?.realName,
        verify_status: res.data.user_info?.verify_status,
        credit_score: res.data.user_info?.creditScore,
      })

      showToast({
        message: '注册成功',
        type: 'success',
      })

      setTimeout(() => {
        router.replace('/')
      }, 1000)
    }
  } catch (error: any) {
    showToast({
      message: error?.message || '注册失败',
      type: 'fail',
    })
  } finally {
    registering.value = false
  }
}

function goToLogin() {
  router.push('/login')
}

function handleGoBack() {
  router.back()
}
</script>

<template>
  <div class="register-page">
    <NavBar title="注册" show-back @back="handleGoBack" />

    <div class="register-content">
      <div class="header-section">
        <h1 class="page-title">注册账号</h1>
        <p class="page-desc">欢迎加入长沙大学生兼职平台</p>
      </div>

      <div class="form-section">
        <div class="form-item">
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

        <div class="form-item">
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

        <div class="form-item">
          <div class="input-group password-group">
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              class="text-input"
              placeholder="请设置密码（6-20位）"
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

        <div class="form-item">
          <div class="input-group password-group">
            <input
              v-model="confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="text-input"
              placeholder="请再次输入密码"
              maxlength="20"
            />
            <button
              class="eye-btn"
              @click="showConfirmPassword = !showConfirmPassword"
              type="button"
            >
              <span v-if="showConfirmPassword">👁️</span>
              <span v-else>🙈</span>
            </button>
          </div>
        </div>

        <div class="form-item">
          <div class="input-group">
            <input
              v-model="nickname"
              type="text"
              class="text-input"
              placeholder="请输入昵称（选填，2-20字）"
              maxlength="20"
            />
          </div>
        </div>

        <button
          class="register-btn"
          :class="{ active: canSubmit, loading: registering }"
          :disabled="!canSubmit"
          @click="handleRegister"
        >
          <template v-if="registering">注册中...</template>
          <template v-else>立即注册</template>
        </button>

        <div class="form-footer">
          <span class="footer-text">已有账号？</span>
          <span class="link-text" @click="goToLogin">去登录</span>
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
    </div>
  </div>
</template>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  background-color: var(--color-bg);
}

.register-content {
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

.header-section {
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
}

.page-desc {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  margin: 0;
}

.form-section {
  margin-bottom: var(--spacing-xl);
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

.register-btn {
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

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: var(--spacing-base);
}

.footer-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.link-text {
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  cursor: pointer;
  margin-left: 4px;

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
</style>
