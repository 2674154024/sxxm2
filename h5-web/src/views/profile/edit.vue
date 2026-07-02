<template>
  <div class="profile-edit">
    <div class="header-section">
      <div class="header-bg"></div>
      <div class="back-btn" @click="goBack">
        <span class="back-icon">‹</span>
      </div>
      <div class="header-title">编辑资料</div>
      <div class="save-btn" @click="handleSave" :class="{ loading: saving }">
        <span v-if="!saving">保存</span>
        <span v-else class="saving-text">保存中...</span>
      </div>
    </div>

    <div class="profile-card">
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <div class="avatar">
            {{ form.nickname?.charAt(0)?.toUpperCase() || '?' }}
          </div>
          <div class="avatar-edit">
            <span>编辑</span>
          </div>
        </div>
      </div>
      <div class="user-name">{{ form.nickname || '未设置昵称' }}</div>
      <div class="user-status" v-if="verifyStatus === 2">
        <span class="status-badge verified">✓ 已实名</span>
      </div>
      <div class="user-status" v-else-if="verifyStatus === 1">
        <span class="status-badge pending">审核中</span>
      </div>
      <div class="user-status" v-else>
        <span class="status-badge unverified">未实名</span>
      </div>
    </div>

    <div class="info-section">
      <div class="section-title">基本信息</div>
      <div class="info-card">
        <div class="info-item">
          <div class="info-label">
            <span class="label-icon">👤</span>
            <span class="label-text">昵称</span>
          </div>
          <div class="info-value">
            <input 
              class="info-input" 
              v-model="form.nickname" 
              placeholder="请输入昵称"
              maxlength="20"
            />
          </div>
        </div>
        <div class="info-divider"></div>
        <div class="info-item" @click="goToVerify">
          <div class="info-label">
            <span class="label-icon">🪪</span>
            <span class="label-text">实名认证</span>
          </div>
          <div class="info-value picker-value">
            <span v-if="verifyStatus === 2" class="verified-text">已认证</span>
            <span v-else-if="verifyStatus === 1" class="pending-text">审核中</span>
            <span v-else class="unverified-text">未认证</span>
            <span class="picker-arrow">›</span>
          </div>
        </div>
      </div>
    </div>

    <div class="info-section">
      <div class="section-title">求职信息</div>
      <div class="info-card">
        <div class="info-item" @click="goToResume">
          <div class="info-label">
            <span class="label-icon">⏰</span>
            <span class="label-text">可用时间</span>
          </div>
          <div class="info-value picker-value">
            <span :class="{ placeholder: !formattedAvailableTime }">{{ formattedAvailableTime || '请设置可用时间' }}</span>
            <span class="picker-arrow">›</span>
          </div>
        </div>
        <div class="info-divider"></div>
        <div class="info-item">
          <div class="info-label">
            <span class="label-icon">💡</span>
            <span class="label-text">技能标签</span>
          </div>
          <div class="info-value">
            <input 
              class="info-input" 
              v-model="form.skillTags" 
              placeholder="如：英语、PS，家教"
            />
          </div>
        </div>
      </div>
      <div class="info-tip">
        <span class="tip-icon">💬</span>
        <span class="tip-text">完善信息可提高简历通过率</span>
      </div>
    </div>

    <div class="info-section" v-if="creditScore">
      <div class="section-title">信用信息</div>
      <div class="info-card">
        <div class="info-item">
          <div class="info-label">
            <span class="label-icon">⭐</span>
            <span class="label-text">信用评分</span>
          </div>
          <div class="info-value credit-score">
            <span class="score-value">{{ creditScore }}</span>
            <span class="score-label">分</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile, updateUserInfo } from '@/api/user'
import { showToast, showSuccessToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = ref({
  nickname: '',
  realName: '',
  availableTime: '',
  skillTags: '',
})
const verifyStatus = ref<number>(0)
const creditScore = ref<number | null>(null)
const saving = ref(false)

const formattedAvailableTime = computed(() => {
  if (!form.value.availableTime) {
    return ''
  }
  try {
    const parsed = JSON.parse(form.value.availableTime)
    if (Array.isArray(parsed)) {
      return parsed.map((item: any) => {
        if (item.day && item.time) {
          return `${item.day} ${item.time}`
        }
        return item.day || item.time || ''
      }).filter(Boolean).join('、')
    }
    return form.value.availableTime
  } catch (e) {
    return form.value.availableTime
  }
})

onMounted(async () => {
  await loadProfile()
})

async function loadProfile() {
  try {
    const res = await getProfile()
    if (res.code === 200 && res.data) {
      form.value.nickname = res.data.nickname || ''
      form.value.realName = res.data.realName || ''
      form.value.availableTime = res.data.availableTime || ''
      form.value.skillTags = res.data.skillTags || ''
      verifyStatus.value = res.data.verifyStatus || 0
      creditScore.value = res.data.creditScore
    }
  } catch (error) {
    console.error('加载个人资料失败:', error)
    showToast('加载失败，请重试')
  }
}

async function handleSave() {
  if (saving.value) return
  
  saving.value = true
  try {
    console.log('保存数据:', JSON.stringify({
      nickname: form.value.nickname,
      availableTime: form.value.availableTime,
      skillTags: form.value.skillTags,
    }))
    const res = await updateUserInfo({
      nickname: form.value.nickname,
      availableTime: form.value.availableTime,
      skillTags: form.value.skillTags,
    })
    console.log('保存响应:', JSON.stringify(res))
    if (res.code === 200) {
      showSuccessToast('保存成功')
      if (userStore.userInfo) {
        userStore.userInfo.nickname = form.value.nickname
        userStore.userInfo.availableTime = form.value.availableTime
        userStore.userInfo.skillTags = form.value.skillTags
        userStore.setUserInfo({ ...userStore.userInfo })
      }
      setTimeout(() => {
        router.back()
      }, 1000)
    } else {
      showToast(res.message || '保存失败')
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    console.error('Error Response:', error.response?.data)
    console.error('Error Status:', error.response?.status)
    showToast(error?.message || error?.response?.data?.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.back()
}

function goToVerify() {
  router.push('/profile/verify')
}

function goToResume() {
  router.push('/resume/edit')
}
</script>

<style scoped lang="scss">
.profile-edit {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
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

.save-btn {
  position: absolute;
  right: var(--spacing-xs);
  top: calc(var(--spacing-lg) + env(safe-area-inset-top));
  padding: 6px 16px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: var(--font-size-sm);
  color: #ffffff;
  z-index: 10;
  cursor: pointer;
  transition: all var(--transition-fast);

  &.loading {
    opacity: 0.7;
  }

  &:active {
    transform: scale(0.96);
    background-color: rgba(255, 255, 255, 0.3);
  }

  .saving-text {
    animation: pulse 1s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.profile-card {
  position: relative;
  margin: 0 var(--spacing-base) var(--spacing-base);
  padding: var(--spacing-sm);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-base);
  text-align: center;
  z-index: 10;
  animation: cardSlideUp 0.3s var(--ease-out-expo);
}

@keyframes cardSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.avatar-section {
  margin-bottom: var(--spacing-xs);
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  
  &:active .avatar-edit {
    opacity: 1;
  }
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #ffffff;
  font-weight: 600;
  margin: 0 auto;
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
  transition: transform var(--transition-fast);
  
  &:active {
    transform: scale(0.95);
  }
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  padding: 3px 8px;
  background-color: var(--color-primary);
  border-radius: 10px;
  font-size: 10px;
  color: #ffffff;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.user-name {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.user-status {
  display: flex;
  justify-content: center;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: var(--font-size-sm);
  
  &.verified {
    background-color: var(--color-success-bg);
    color: var(--color-success);
  }
  
  &.pending {
    background-color: #E3F2FD;
    color: #1976D2;
  }
  
  &.unverified {
    background-color: #FFF3E0;
    color: #FF9800;
  }
}

.info-section {
  margin: 0 var(--spacing-base) var(--spacing-base);
  animation: fadeIn 0.3s var(--ease-out-expo);
  animation-fill-mode: backwards;
  
  &:nth-child(3) { animation-delay: 0.1s; }
  &:nth-child(4) { animation-delay: 0.2s; }
  &:nth-child(5) { animation-delay: 0.3s; }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-title {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
  padding-left: var(--spacing-xs);
}

.info-card {
  background-color: #ffffff;
  border-radius: var(--radius-base);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-base);
  min-height: 56px;
  box-sizing: border-box;
}

.info-label {
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

.info-value {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-left: var(--spacing-sm);
}

.picker-value {
  cursor: pointer;

  .verified-text {
    color: var(--color-success);
    font-size: var(--font-size-base);
  }

  .pending-text {
    color: #1976D2;
    font-size: var(--font-size-base);
  }

  .unverified-text {
    color: var(--color-text-secondary);
    font-size: var(--font-size-base);
  }

  .placeholder {
    color: var(--color-text-disabled);
  }

  .picker-arrow {
    margin-left: var(--spacing-xs);
    color: var(--color-text-disabled);
    font-size: 18px;
  }
}

.info-input {
  width: 100%;
  text-align: right;
  font-size: var(--font-size-base);
  color: var(--color-text);
  border: none;
  background: transparent;
  outline: none;
  
  &.placeholder {
    color: var(--color-text-secondary);
  }
  
  &::placeholder {
    color: var(--color-text-disabled);
  }
}

.info-divider {
  height: 1px;
  background-color: var(--color-border);
  margin: 0 var(--spacing-base);
}

.credit-score {
  .score-value {
    font-size: var(--font-size-xxl);
    font-weight: 600;
    color: var(--color-accent);
  }
  
  .score-label {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    margin-left: 2px;
  }
}

.info-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: var(--spacing-sm);
  padding: var(--spacing-xs);
  background-color: #FFFBE6;
  border-radius: var(--radius-sm);
}

.tip-icon {
  font-size: 14px;
  margin-right: 6px;
}

.tip-text {
  font-size: var(--font-size-xs);
  color: #FF9800;
}
</style>
