<script setup lang="ts">
<<<<<<< HEAD
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()

const userInfo = ref({
  name: '张三',
  school: '中南大学',
  major: '计算机学院',
  avatar: '',
  creditScore: 180,
  creditMax: 200,
  isCertified: true,
})

const gridMenus = [
  { icon: '📝', label: '我的简历', path: '/resume/edit' },
  { icon: '📋', label: '投递记录', path: '/apply' },
  { icon: '💰', label: '薪资流水', path: '/salary' },
  { icon: '📄', label: '协议中心', path: '/agreement' },
  { icon: '🛡️', label: '保险记录', path: '/insurance' },
  { icon: '📊', label: '实践报告', path: '/report' },
  { icon: '🛡️', label: '安全中心', path: '/safety' },
  { icon: '⚠️', label: '投诉记录', path: '/complaint' },
  { icon: '⚙️', label: '设置', path: '/settings' },
]

const otherMenus = [
  { icon: '⭐', label: '我的收藏', path: '/favorites' },
  { icon: '👁️', label: '浏览历史', path: '/history' },
  { icon: '💬', label: '意见反馈', path: '/feedback' },
  { icon: 'ℹ️', label: '关于我们', path: '/about' },
]

const showLogoutConfirm = ref(false)

function handleGridClick(item: typeof gridMenus[0]) {
  if (item.path) {
    router.push(item.path)
  }
}

function handleOtherClick(item: typeof otherMenus[0]) {
  if (item.path) {
    router.push(item.path)
  }
}

function handleLogout() {
  showLogoutConfirm.value = true
}

function confirmLogout() {
  localStorage.removeItem('token')
  router.replace('/')
  showLogoutConfirm.value = false
}

onMounted(() => {
})
=======
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
</script>

<template>
  <div class="mine-page">
<<<<<<< HEAD
    <div class="mine-header">
      <div class="mine-header__bg"></div>
      <div class="mine-header__content">
        <div class="mine-avatar">
          <span class="mine-avatar__default">👤</span>
        </div>
        <div class="mine-info">
          <div class="mine-info__name">
            {{ userInfo.name }}
            <span v-if="userInfo.isCertified" class="mine-info__badge">已认证</span>
          </div>
          <div class="mine-info__school">{{ userInfo.school }} · {{ userInfo.major }}</div>
          <div class="mine-credit">
            <span class="mine-credit__label">信用分</span>
            <span class="mine-credit__score">{{ userInfo.creditScore }}</span>
            <span class="mine-credit__max">/{{ userInfo.creditMax }}</span>
            <div class="mine-credit__bar">
              <div
                class="mine-credit__progress"
                :style="{ width: `${(userInfo.creditScore / userInfo.creditMax) * 100}%` }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mine-grid">
      <div
        v-for="item in gridMenus"
        :key="item.label"
        class="mine-grid__item"
        @click="handleGridClick(item)"
      >
        <div class="mine-grid__icon">{{ item.icon }}</div>
        <div class="mine-grid__label">{{ item.label }}</div>
      </div>
    </div>

    <div class="mine-list">
      <div
        v-for="item in otherMenus"
        :key="item.label"
        class="mine-list__item"
        @click="handleOtherClick(item)"
      >
        <div class="mine-list__left">
          <span class="mine-list__icon">{{ item.icon }}</span>
          <span class="mine-list__label">{{ item.label }}</span>
        </div>
        <span class="mine-list__arrow">›</span>
      </div>
    </div>

    <div class="mine-logout">
      <button class="btn btn--block mine-logout__btn" @click="handleLogout">
        退出登录
      </button>
    </div>

    <TabBar />

    <transition name="fade">
      <div v-if="showLogoutConfirm" class="confirm-mask" @click="showLogoutConfirm = false">
        <div class="confirm-dialog" @click.stop>
          <div class="confirm-dialog__title">确认退出登录？</div>
          <div class="confirm-dialog__desc">退出后需要重新登录才能使用完整功能</div>
          <div class="confirm-dialog__actions">
            <button class="btn btn--secondary confirm-dialog__btn" @click="showLogoutConfirm = false">
              取消
            </button>
            <button class="btn btn--danger confirm-dialog__btn" @click="confirmLogout">
              退出
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped lang="scss">
.mine-page {
  min-height: 100vh;
  padding-bottom: calc(50px + env(safe-area-inset-bottom));
  background-color: var(--color-bg-secondary);
}

.mine-header {
  position: relative;
  padding: 24px 16px 40px;
  color: #fff;
  overflow: hidden;

  &__bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  }

  &__content {
    position: relative;
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }
}

.mine-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  transition: all var(--transition-fast);

  &:active {
    transform: scale(0.95);
  }

  &__default {
    font-size: 32px;
  }
}

.mine-info {
  flex: 1;
  min-width: 0;

  &__name {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__school {
    font-size: 13px;
    opacity: 0.9;
    margin-bottom: 12px;
  }
}

.mine-info__badge {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  background-color: var(--color-success);
  font-size: 11px;
  font-weight: 400;
  border-radius: 10px;
}

.mine-credit {
  position: relative;

  &__label {
    font-size: 12px;
    opacity: 0.8;
    margin-right: 6px;
  }

  &__score {
    font-size: 16px;
    font-weight: 600;
  }

  &__max {
    font-size: 12px;
    opacity: 0.7;
  }

  &__bar {
    height: 6px;
    margin-top: 6px;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 3px;
    overflow: hidden;
  }

  &__progress {
    height: 100%;
    background: #fff;
    border-radius: 3px;
    transition: width 0.8s var(--ease-out-expo);
  }
}

.mine-grid {
  margin: -24px 16px 12px;
  padding: 16px 8px;
  background: #fff;
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-base);
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  position: relative;
  z-index: 1;
  animation: slideUp 0.4s var(--ease-out-expo);

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px 4px;
    cursor: pointer;
    transition: all var(--transition-fast);

    &:active {
      transform: scale(0.92);
    }
  }

  &__icon {
    font-size: 28px;
    margin-bottom: 6px;
  }

  &__label {
    font-size: 12px;
    color: var(--color-text);
  }
}

@keyframes slideUp {
  from { transform: translateY(16px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.mine-list {
  margin: 12px 16px;
  background: #fff;
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-sm);
  overflow: hidden;

  &__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 48px;
    padding: 0 16px;
    cursor: pointer;
    transition: background-color var(--transition-fast);

    &:active {
      background-color: var(--color-bg-secondary);
    }

    &:not(:last-child) {
      border-bottom: 1px solid var(--color-border);
    }
  }

  &__left {
    display: flex;
    align-items: center;
  }

  &__icon {
    font-size: 18px;
    margin-right: 12px;
  }

  &__label {
    font-size: 14px;
    color: var(--color-text);
  }

  &__arrow {
    font-size: 18px;
    color: var(--color-text-disabled);
  }
}

.mine-logout {
  margin: 24px 16px;

  &__btn {
    background-color: #fff;
    color: var(--color-danger);
    border: 1px solid var(--color-border);

    &:active {
      background-color: var(--color-danger-bg);
      transform: scale(0.98);
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.confirm-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
}

.confirm-dialog {
  width: 280px;
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 24px 20px 16px;
  animation: popIn 0.2s var(--ease-out-expo);

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
    text-align: center;
    margin-bottom: 8px;
  }

  &__desc {
    font-size: 13px;
    color: var(--color-text-secondary);
    text-align: center;
    margin-bottom: 20px;
  }

  &__actions {
    display: flex;
    gap: 12px;
  }

  &__btn {
    flex: 1;
    height: 40px;
    font-size: 14px;
  }
}

@keyframes popIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>
=======
    <h2>个人中心</h2>
    <div class="profile-card">个人信息卡片占位</div>
  </div>
</template>

<style scoped>
.mine-page {
  padding: 16px;
}

.mine-page h2 {
  margin-bottom: 16px;
  color: #4E5969;
}

.profile-card {
  background-color: #FFFFFF;
  padding: 12px;
  border-radius: 8px;
}
</style>
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
