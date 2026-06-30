<script setup lang="ts">
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import { useUserStore } from '@/stores/user'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const menuItems = [
  {
    key: 'profile',
    icon: '👤',
    label: '个人资料',
    path: '/profile',
    needLogin: true,
  },
  {
    key: 'resume',
    icon: '📄',
    label: '我的简历',
    path: '/resume',
    needLogin: true,
  },
  {
    key: 'favorite',
    icon: '⭐',
    label: '我的收藏',
    path: '/favorite',
    needLogin: true,
  },
  {
    key: 'feedback',
    icon: '💬',
    label: '意见反馈',
    path: '/feedback',
    needLogin: false,
  },
  {
    key: 'about',
    icon: 'ℹ️',
    label: '关于我们',
    path: '/about',
    needLogin: false,
  },
  {
    key: 'safety',
    icon: '🛡️',
    label: '防骗指南',
    path: '/safety',
    needLogin: false,
  },
]

function handleMenuClick(item: any) {
  if (item.needLogin && !userStore.isLoggedIn) {
    showToast({
      message: '请先登录',
      type: 'fail',
    })
    return
  }
  if (item.path) {
    router.push(item.path)
  }
}

function goToLogin() {
  router.push('/login')
}

function goToEditProfile() {
  router.push('/profile')
}

function handleLogout() {
  showConfirmDialog({
    title: '确认退出',
    message: '确定要退出登录吗？',
  })
    .then(() => {
      userStore.logout()
      showToast({
        message: '已退出登录',
        type: 'success',
      })
    })
    .catch(() => {})
}
</script>

<template>
  <div class="profile-page">
    <div class="header-section">
      <div class="header-bg"></div>
      <div class="user-card" v-if="userStore.isLoggedIn && userStore.userInfo">
        <div class="avatar">
          {{ userStore.userInfo.nickname?.charAt(0) || userStore.userInfo.phone?.charAt(0) || '?' }}
        </div>
        <div class="user-info" @click="goToEditProfile">
          <div class="nickname">{{ userStore.userInfo.nickname || '未设置昵称' }}</div>
          <div class="phone">{{ userStore.userInfo.phone }}</div>
        </div>
        <div class="arrow-icon">›</div>
      </div>

      <div class="user-card not-login" v-else @click="goToLogin">
        <div class="avatar default-avatar">👤</div>
        <div class="user-info">
          <div class="nickname">点击登录</div>
          <div class="phone">登录后体验更多功能</div>
        </div>
        <div class="arrow-icon">›</div>
      </div>

      <div class="stats-row" v-if="userStore.isLoggedIn">
        <div class="stat-item">
          <div class="stat-num">0</div>
          <div class="stat-label">待处理</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-num">0</div>
          <div class="stat-label">已通过</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-num">0</div>
          <div class="stat-label">收藏</div>
        </div>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-group">
        <div
          v-for="item in menuItems.slice(0, 3)"
          :key="item.key"
          class="menu-item"
          @click="handleMenuClick(item)"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span class="menu-label">{{ item.label }}</span>
          <span class="menu-arrow">›</span>
        </div>
      </div>

      <div class="menu-group">
        <div
          v-for="item in menuItems.slice(3)"
          :key="item.key"
          class="menu-item"
          @click="handleMenuClick(item)"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span class="menu-label">{{ item.label }}</span>
          <span class="menu-arrow">›</span>
        </div>
      </div>

      <div class="menu-group" v-if="userStore.isLoggedIn">
        <div class="menu-item logout-item" @click="handleLogout">
          <span class="menu-icon">🚪</span>
          <span class="menu-label">退出登录</span>
        </div>
      </div>
    </div>

    <div class="footer-tip">
      长沙大学生兼职平台 v1.0.0
    </div>
    <TabBar active="/profile" />
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
}

.header-section {
  position: relative;
  padding-bottom: var(--spacing-sm);
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 160px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #4080ff 100%);
}

.user-card {
  position: relative;
  display: flex;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-base);
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    opacity: 0.8;
  }

  &.not-login .avatar {
    background-color: rgba(255, 255, 255, 0.2);
  }
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--color-primary);
  font-weight: 500;
  margin-right: var(--spacing-base);
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.default-avatar {
  background-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.nickname {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.not-login .nickname {
  color: #fff;
}

.phone {
  font-size: var(--font-size-base);
  color: rgba(255, 255, 255, 0.8);
}

.arrow-icon {
  color: rgba(255, 255, 255, 0.6);
  font-size: 24px;
  margin-left: 8px;
}

.stats-row {
  position: relative;
  display: flex;
  align-items: center;
  margin: 0 var(--spacing-base);
  padding: var(--spacing-sm) 0;
  background-color: #fff;
  border-radius: var(--radius-base);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.stat-item {
  flex: 1;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.96);
  }
}

.stat-num {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background-color: var(--color-border);
}

.menu-section {
  padding: var(--spacing-sm) 0;
}

.menu-group {
  background-color: var(--color-bg);
  margin-bottom: var(--spacing-sm);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px var(--spacing-base);
  cursor: pointer;
  transition: background-color 0.2s ease;
  position: relative;

  &:active {
    background-color: #f7f8fa;
  }

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    left: var(--spacing-base);
    right: 0;
    bottom: 0;
    height: 1px;
    background-color: var(--color-border);
    transform: scaleY(0.5);
  }

  animation: menuItemIn 0.3s ease backwards;
}

@keyframes menuItemIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.menu-item:nth-child(1) { animation-delay: 0.02s; }
.menu-item:nth-child(2) { animation-delay: 0.04s; }
.menu-item:nth-child(3) { animation-delay: 0.06s; }
.menu-item:nth-child(4) { animation-delay: 0.08s; }
.menu-item:nth-child(5) { animation-delay: 0.10s; }
.menu-item:nth-child(6) { animation-delay: 0.12s; }

.menu-icon {
  font-size: 20px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.menu-label {
  flex: 1;
  font-size: var(--font-size-base);
  color: var(--color-text);
}

.menu-arrow {
  font-size: 20px;
  color: var(--color-text-secondary);
}

.logout-item {
  justify-content: center;

  .menu-icon {
    margin-right: 8px;
  }

  .menu-label {
    flex: none;
    color: var(--color-danger);
  }
}

.footer-tip {
  text-align: center;
  padding: var(--spacing-lg) 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
