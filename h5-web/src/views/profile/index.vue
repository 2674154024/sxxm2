<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import { useUserStore } from '@/stores/user'
import { showConfirmDialog, showToast } from 'vant'
import { getProfile } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menuItems = [
  {
    key: 'profile',
    icon: '👤',
    label: '个人资料',
    path: '/profile/edit',
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
    key: 'complaint',
    icon: '📝',
    label: '我的投诉',
    path: '/complaint/list',
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
  router.push('/profile/edit')
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

async function loadUserProfile() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getProfile()
    if (res.code === 200 && res.data) {
      if (userStore.userInfo) {
        userStore.userInfo.nickname = res.data.nickname || ''
        userStore.userInfo.realName = res.data.realName || ''
        userStore.userInfo.phone = res.data.phone || ''
        userStore.userInfo.avatarUrl = res.data.avatarUrl || ''
        userStore.userInfo.verifyStatus = res.data.verifyStatus || 0
        userStore.userInfo.creditScore = res.data.creditScore || 0
        userStore.setUserInfo({ ...userStore.userInfo })
      } else {
        userStore.setUserInfo(res.data)
      }
    }
  } catch (error) {
    console.error('加载用户资料失败:', error)
  }
}

watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/profile' && userStore.isLoggedIn) {
      loadUserProfile()
    }
  }
)

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadUserProfile()
  }
})
</script>

<template>
  <div class="profile-page">
    <div class="header-section">
      <div class="header-bg">
        <div class="bg-glow glow-1"></div>
        <div class="bg-glow glow-2"></div>
      </div>
      
      <div class="user-card" v-if="userStore.isLoggedIn && userStore.userInfo">
        <div class="avatar-wrapper">
          <div class="avatar">
            {{ userStore.userInfo.nickname?.charAt(0) || userStore.userInfo.phone?.charAt(0) || '?' }}
          </div>
          <div class="avatar-shine"></div>
          <div class="verify-badge" v-if="userStore.userInfo.verifyStatus === 1">
            ✅
          </div>
        </div>
        
        <div class="user-info" @click="goToEditProfile">
          <div class="nickname-row">
            <span class="nickname">{{ userStore.userInfo.nickname || '未设置昵称' }}</span>
            <span class="verify-tag" v-if="userStore.userInfo.verifyStatus === 1">已认证</span>
          </div>
          <div class="phone">{{ userStore.userInfo.phone }}</div>
          <div class="credit-score" v-if="userStore.userInfo.creditScore">
            <span class="credit-icon">💳</span>
            <span class="credit-label">信用分</span>
            <span class="credit-value">{{ userStore.userInfo.creditScore }}</span>
          </div>
        </div>
        
        <div class="arrow-icon">›</div>
      </div>

      <div class="user-card not-login" v-else @click="goToLogin">
        <div class="avatar-wrapper">
          <div class="avatar default-avatar">👤</div>
          <div class="avatar-shine"></div>
        </div>
        <div class="user-info">
          <div class="nickname">点击登录</div>
          <div class="phone">登录后体验更多功能</div>
        </div>
        <div class="arrow-icon">›</div>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-group">
        <div
          v-for="(item, index) in menuItems.slice(0, 3)"
          :key="item.key"
          class="menu-item"
          @click="handleMenuClick(item)"
          :style="{ animationDelay: `${index * 0.05}s` }"
        >
          <div class="menu-icon-wrapper">
            <span class="menu-icon">{{ item.icon }}</span>
          </div>
          <span class="menu-label">{{ item.label }}</span>
          <span class="menu-arrow">›</span>
        </div>
      </div>

      <div class="menu-group">
        <div
          v-for="(item, index) in menuItems.slice(3)"
          :key="item.key"
          class="menu-item"
          @click="handleMenuClick(item)"
          :style="{ animationDelay: `${(index + 3) * 0.05}s` }"
        >
          <div class="menu-icon-wrapper">
            <span class="menu-icon">{{ item.icon }}</span>
          </div>
          <span class="menu-label">{{ item.label }}</span>
          <span class="menu-arrow">›</span>
        </div>
      </div>

      <div class="menu-group logout-group" v-if="userStore.isLoggedIn">
        <div class="menu-item logout-item" @click="handleLogout">
          <div class="menu-icon-wrapper danger">
            <span class="menu-icon">🚪</span>
          </div>
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
  padding-bottom: 24px;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #3C7EFF 100%);
  border-radius: 0 0 20px 20px;
  overflow: hidden;
}

.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
  
  &.glow-1 {
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.3);
    top: -50px;
    right: -50px;
    animation: floatGlow 8s ease-in-out infinite;
  }
  
  &.glow-2 {
    width: 150px;
    height: 150px;
    background: rgba(99, 176, 255, 0.4);
    bottom: -30px;
    left: -30px;
    animation: floatGlow 6s ease-in-out infinite reverse;
  }
}

@keyframes floatGlow {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(20px, -20px) scale(1.1);
  }
}

.user-card {
  position: relative;
  display: flex;
  align-items: center;
  padding: 20px var(--spacing-base);
  margin: 0 var(--spacing-base);
  margin-top: 100px;
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, var(--color-primary), var(--color-accent), var(--color-primary));
    background-size: 200% 100%;
    animation: gradientSlide 3s linear infinite;
  }

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }

  &.not-login {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    
    .avatar {
      background-color: rgba(255, 255, 255, 0.2);
      border: 2px solid rgba(255, 255, 255, 0.3);
    }
    
    .nickname {
      color: #fff;
    }
    
    .phone {
      color: rgba(255, 255, 255, 0.8);
    }
    
    .arrow-icon {
      color: rgba(255, 255, 255, 0.6);
    }
  }
}

@keyframes gradientSlide {
  0% {
    background-position: 0% 0%;
  }
  100% {
    background-position: 200% 0%;
  }
}

.avatar-wrapper {
  position: relative;
  margin-right: var(--spacing-base);
  flex-shrink: 0;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-bg) 0%, #fff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: var(--color-primary);
  font-weight: 600;
  border: 3px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(22, 93, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.avatar-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transform: skewX(-25deg);
  animation: shineMove 3s ease-in-out infinite;
}

@keyframes shineMove {
  0% {
    left: -100%;
  }
  50%, 100% {
    left: 200%;
  }
}

.verify-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 24px;
  height: 24px;
  background: var(--color-success);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 180, 42, 0.4);
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(0, 180, 42, 0.4);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 4px 16px rgba(0, 180, 42, 0.6);
  }
}

.default-avatar {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 28px;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.nickname-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.nickname {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
}

.verify-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: var(--color-success-bg);
  color: var(--color-success);
  border-radius: 10px;
  font-weight: 500;
}

.phone {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.credit-score {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  
  .credit-icon {
    font-size: 14px;
  }
  
  .credit-label {
    color: var(--color-text-secondary);
  }
  
  .credit-value {
    color: var(--color-accent);
    font-weight: 600;
    font-size: 14px;
  }
}

.arrow-icon {
  color: var(--color-text-secondary);
  font-size: 24px;
  margin-left: 8px;
  flex-shrink: 0;
}

.menu-section {
  padding: var(--spacing-base) var(--spacing-base) 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.menu-group {
  background-color: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px var(--spacing-base);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;

  &:active {
    background-color: var(--color-bg-secondary);
    transform: scale(0.99);
  }

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    left: calc(var(--spacing-base) + 32px);
    right: 0;
    bottom: 0;
    height: 1px;
    background-color: var(--color-border);
    transform: scaleY(0.5);
  }

  animation: menuItemIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) backwards;
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

.menu-icon-wrapper {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-base);
  background: var(--color-primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  transition: all 0.2s ease;
  
  &.danger {
    background: var(--color-danger-bg);
    
    .menu-icon {
      color: var(--color-danger);
    }
  }
  
  .menu-item:active & {
    transform: scale(0.9);
  }
}

.menu-icon {
  font-size: 18px;
}

.menu-label {
  flex: 1;
  font-size: var(--font-size-base);
  color: var(--color-text);
  font-weight: 500;
}

.menu-arrow {
  font-size: 18px;
  color: var(--color-text-disabled);
}

.logout-group {
  margin-top: var(--spacing-sm);
}

.logout-item {
  justify-content: center;

  .menu-icon-wrapper {
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
  color: var(--color-text-disabled);
}
</style>