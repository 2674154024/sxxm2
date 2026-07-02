<template>
  <div class="verify-page">
    <div class="header-section">
      <div class="header-bg"></div>
      <div class="back-btn" @click="goBack">
        <span class="back-icon">‹</span>
      </div>
      <div class="header-title">实名认证</div>
    </div>

    <div class="verify-content">
      <div class="verify-card">
        <div class="card-icon">🎓</div>
        <div class="card-title">学生实名认证</div>
        <div class="card-desc">完成实名认证后可投递简历，享受更多权益</div>
      </div>

      <div class="form-section">
        <div class="section-title">认证信息</div>
        <div class="form-card">
          <div class="form-item">
            <div class="form-label">
              <span class="label-icon">👤</span>
              <span class="label-text">真实姓名</span>
            </div>
            <div class="form-value">
              <input
                class="form-input"
                v-model="form.realName"
                placeholder="请输入真实姓名"
                maxlength="20"
              />
            </div>
          </div>

          <div class="form-divider"></div>

          <div class="form-item">
            <div class="form-label">
              <span class="label-icon">🏫</span>
              <span class="label-text">学号</span>
            </div>
            <div class="form-value">
              <input
                class="form-input"
                v-model="form.studentNo"
                placeholder="请输入学号"
                maxlength="30"
              />
            </div>
          </div>

          <div class="form-divider"></div>

          <div class="form-item" @click="showSchoolPicker = true">
            <div class="form-label">
              <span class="label-icon">🏛️</span>
              <span class="label-text">学校</span>
            </div>
            <div class="form-value picker-value">
              <span :class="{ placeholder: !form.schoolName }">{{ form.schoolName || '请选择学校' }}</span>
              <span class="picker-arrow">›</span>
            </div>
          </div>

          <div class="form-divider"></div>

          <div class="form-item">
            <div class="form-label">
              <span class="label-icon">🪪</span>
              <span class="label-text">身份证号</span>
            </div>
            <div class="form-value">
              <input
                class="form-input"
                v-model="form.idCard"
                placeholder="请输入身份证号"
                maxlength="18"
              />
            </div>
          </div>

          <div class="form-divider"></div>

          <div class="form-item">
            <div class="form-label">
              <span class="label-icon">📱</span>
              <span class="label-text">手机号</span>
            </div>
            <div class="form-value">
              <input
                class="form-input"
                v-model="form.phone"
                type="tel"
                placeholder="请输入手机号"
                maxlength="11"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="notice-section">
        <div class="notice-title">认证须知</div>
        <div class="notice-list">
          <div class="notice-item">
            <span class="notice-icon">✓</span>
            <span class="notice-text">您的信息将用于学信网验证</span>
          </div>
          <div class="notice-item">
            <span class="notice-icon">✓</span>
            <span class="notice-text">个人信息仅用于认证，我们承诺保护您的隐私</span>
          </div>
          <div class="notice-item">
            <span class="notice-icon">✓</span>
            <span class="notice-text">认证通过后可获得初始信用积分100分</span>
          </div>
        </div>
      </div>

      <button class="submit-btn" :disabled="!canSubmit || submitting" @click="handleSubmit">
        <template v-if="submitting">提交中...</template>
        <template v-else>提交认证</template>
      </button>
    </div>

    <!-- 学校选择器 -->
    <van-popup v-model:show="showSchoolPicker" position="bottom" round :style="{ height: '60%' }">
      <van-picker
        title="选择学校"
        :columns="schoolColumns"
        :visible-item-count="6"
        item-height="50"
        @confirm="onSchoolConfirm"
        @cancel="showSchoolPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { getProfile } from '@/api/user'
import { studentVerify } from '@/api/auth'

const router = useRouter()

const form = ref({
  realName: '',
  studentNo: '',
  schoolId: '',
  schoolName: '',
  idCard: '',
  phone: '',
})

const showSchoolPicker = ref(false)
const submitting = ref(false)

// 长沙主要大学列表
const schoolColumns = [
  { text: '中南大学', value: '1' },
  { text: '湖南大学', value: '2' },
  { text: '湖南师范大学', value: '3' },
  { text: '长沙理工大学', value: '4' },
  { text: '湖南农业大学', value: '5' },
  { text: '中南林业科技大学', value: '6' },
  { text: '湖南商学院', value: '7' },
  { text: '长沙学院', value: '8' },
  { text: '湖南第一师范学院', value: '9' },
  { text: '湖南女子学院', value: '10' },
  { text: '湖南涉外经济学院', value: '11' },
  { text: '长沙医学院', value: '12' },
]

const canSubmit = computed(() => {
  return (
    form.value.realName.length >= 2 &&
    form.value.studentNo.length >= 4 &&
    form.value.schoolId &&
    form.value.idCard.length === 18 &&
    /^1[3-9]\d{9}$/.test(form.value.phone)
  )
})

onMounted(async () => {
  await loadProfile()
})

async function loadProfile() {
  try {
    const res = await getProfile()
    if (res.code === 200 && res.data) {
      form.value.realName = res.data.realName || ''
      if (res.data.phone && !res.data.phone.includes('*')) {
        form.value.phone = res.data.phone
      } else {
        form.value.phone = ''
      }
      if (res.data.schoolId) {
        form.value.schoolId = String(res.data.schoolId)
        const school = schoolColumns.find(s => s.value === String(res.data.schoolId))
        if (school) {
          form.value.schoolName = school.text
        }
      }
      if (res.data.studentNo) {
        form.value.studentNo = res.data.studentNo
      }
      if (res.data.idCard && !res.data.idCard.includes('*')) {
        form.value.idCard = res.data.idCard
      }
    }
  } catch (error) {
    console.error('加载个人资料失败:', error)
  }
}

function onSchoolConfirm({ selectedOptions }: any) {
  const selected = selectedOptions[0]
  form.value.schoolId = selected.value
  form.value.schoolName = selected.text
  showSchoolPicker.value = false
}

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) return

  submitting.value = true
  try {
    const res = await studentVerify({
      realName: form.value.realName,
      studentNo: form.value.studentNo,
      schoolId: form.value.schoolId,
      idCard: form.value.idCard.toUpperCase(),
      phone: form.value.phone,
    })

    if (res.code === 200) {
      showSuccessToast('认证提交成功')
      setTimeout(() => {
        router.back()
      }, 1500)
    } else {
      showToast(res.message || '认证失败')
    }
  } catch (error: any) {
    console.error('认证失败:', error)
    showToast(error?.message || '认证失败，请重试')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped lang="scss">
.verify-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}

.header-section {
  position: relative;
  padding: var(--spacing-lg) var(--spacing-base);
  padding-top: calc(var(--spacing-lg) + env(safe-area-inset-top));
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.1;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.back-btn {
  position: absolute;
  left: var(--spacing-xs);
  top: calc(var(--spacing-lg) + env(safe-area-inset-top));
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  cursor: pointer;

  &:active {
    opacity: 0.7;
  }
}

.back-icon {
  font-size: 32px;
  color: #ffffff;
  line-height: 1;
}

.header-title {
  position: relative;
  text-align: center;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: #ffffff;
}

.verify-content {
  padding: var(--spacing-base);
}

.verify-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  text-align: center;
  margin-bottom: var(--spacing-base);
  box-shadow: var(--shadow-base);
}

.card-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-sm);
}

.card-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.card-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.form-section {
  margin-bottom: var(--spacing-base);
}

.section-title {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
  padding-left: var(--spacing-xs);
}

.form-card {
  background-color: #ffffff;
  border-radius: var(--radius-base);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-base);
  min-height: 56px;
  box-sizing: border-box;
}

.form-label {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.label-icon {
  font-size: 20px;
  margin-right: var(--spacing-xs);
}

.label-text {
  font-size: var(--font-size-base);
  color: var(--color-text);
}

.form-value {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-left: var(--spacing-sm);
}

.form-input {
  width: 100%;
  text-align: right;
  font-size: var(--font-size-base);
  color: var(--color-text);
  border: none;
  background: transparent;
  outline: none;

  &::placeholder {
    color: var(--color-text-disabled);
  }
}

.picker-value {
  cursor: pointer;

  .placeholder {
    color: var(--color-text-disabled);
  }

  .picker-arrow {
    margin-left: var(--spacing-xs);
    color: var(--color-text-disabled);
    font-size: 18px;
  }
}

.form-divider {
  height: 1px;
  background-color: var(--color-border);
  margin: 0 var(--spacing-base);
}

.notice-section {
  background-color: #ffffff;
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-base);
  box-shadow: var(--shadow-sm);
}

.notice-title {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.notice-item {
  display: flex;
  align-items: center;
  animation: none !important;
  transform: none !important;
}

.notice-icon {
  color: var(--color-success);
  font-size: 14px;
  margin-right: var(--spacing-xs);
}

.notice-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.submit-btn {
  position: fixed;
  bottom: env(safe-area-inset-bottom);
  left: var(--spacing-base);
  right: var(--spacing-base);
  height: 48px;
  border: none;
  border-radius: 24px;
  background-color: var(--color-primary);
  color: #ffffff;
  font-size: var(--font-size-lg);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 100;

  &:active:not(:disabled) {
    transform: scale(0.98);
    background-color: var(--color-primary-dark);
  }

  &:disabled {
    background-color: var(--color-text-disabled);
    cursor: not-allowed;
  }
}
</style>