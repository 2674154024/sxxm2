<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const router = useRouter()

interface TabItem {
  name: string
  path: string
  label: string
  icon: string
  activeIcon: string
}

const tabs: TabItem[] = [
  { name: 'Index', path: '/', label: '首页', icon: '🏠', activeIcon: '🏠' },
  { name: 'Apply', path: '/apply', label: '投递', icon: '📋', activeIcon: '📋' },
  { name: 'Message', path: '/message', label: '消息', icon: '💬', activeIcon: '💬' },
  { name: 'Profile', path: '/profile', label: '我的', icon: '👤', activeIcon: '👤' },
]

const activeTab = computed(() => {
  return tabs.find(tab => route.path === tab.path)?.name || 'Index'
})

function handleTabClick(tab: TabItem) {
  if (route.path !== tab.path) {
    router.push(tab.path)
  }
}
</script>

<template>
  <div class="tabbar">
    <div
      v-for="tab in tabs"
      :key="tab.name"
      class="tab-item"
      :class="{ active: activeTab === tab.name }"
      @click="handleTabClick(tab)"
    >
      <div class="tab-icon">{{ activeTab === tab.name ? tab.activeIcon : tab.icon }}</div>
      <div class="tab-label">{{ tab.label }}</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: calc(var(--tabbar-height) + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background-color: var(--color-bg);
  display: flex;
  align-items: center;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
  z-index: 100;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;

  &.active {
    .tab-icon {
      animation: tabBounce 300ms cubic-bezier(0.68, -0.55, 0.27, 1.55);
    }
    .tab-label {
      color: var(--color-primary);
      font-weight: 500;
    }
  }
}

.tab-icon {
  font-size: 22px;
  line-height: 1;
  margin-bottom: 2px;
}

.tab-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1;
}
</style>
