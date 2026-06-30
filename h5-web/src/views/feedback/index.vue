<script setup lang="ts">
import { ref } from 'vue'
import NavBar from '@/components/NavBar.vue'
import { showToast } from 'vant'

const feedback = ref('')
const contact = ref('')
const submitting = ref(false)

function handleSubmit() {
  if (!feedback.value.trim()) {
    showToast({
      message: '请输入反馈内容',
      type: 'fail',
    })
    return
  }
  submitting.value = true
  setTimeout(() => {
    showToast({
      message: '提交成功，感谢您的反馈',
      type: 'success',
    })
    feedback.value = ''
    contact.value = ''
    submitting.value = false
  }, 1000)
}
</script>

<template>
  <div class="feedback-page">
    <NavBar title="意见反馈" show-back />
    <div class="feedback-content">
      <div class="form-item">
        <div class="label">问题描述</div>
        <textarea
          v-model="feedback"
          class="textarea"
          placeholder="请描述您遇到的问题或建议..."
          maxlength="500"
        ></textarea>
        <div class="char-count">{{ feedback.length }}/500</div>
      </div>

      <div class="form-item">
        <div class="label">联系方式（选填）</div>
        <input
          v-model="contact"
          type="text"
          class="input"
          placeholder="请留下您的手机号或邮箱"
        />
      </div>

      <button
        class="submit-btn"
        :class="{ active: feedback.trim() }"
        :disabled="!feedback.trim() || submitting"
        @click="handleSubmit"
      >
        {{ submitting ? '提交中...' : '提交反馈' }}
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.feedback-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
}

.feedback-content {
  padding: var(--spacing-base);
}

.form-item {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-base);
}

.label {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.textarea {
  width: 100%;
  min-height: 120px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: var(--spacing-sm);
  font-size: var(--font-size-base);
  color: var(--color-text);
  resize: none;
  outline: none;
  box-sizing: border-box;

  &:focus {
    border-color: var(--color-primary);
  }

  &::placeholder {
    color: var(--color-text-placeholder);
  }
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.input {
  width: 100%;
  height: 44px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: 0 var(--spacing-sm);
  font-size: var(--font-size-base);
  color: var(--color-text);
  outline: none;
  box-sizing: border-box;

  &:focus {
    border-color: var(--color-primary);
  }

  &::placeholder {
    color: var(--color-text-placeholder);
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 24px;
  background-color: var(--color-text-disabled);
  color: #fff;
  font-size: var(--font-size-lg);
  font-weight: 500;
  cursor: not-allowed;
  transition: all 0.2s ease;
  margin-top: var(--spacing-lg);

  &.active {
    background-color: var(--color-primary);
    cursor: pointer;

    &:active {
      transform: scale(0.98);
      background-color: var(--color-primary-dark);
    }
  }
}
</style>
