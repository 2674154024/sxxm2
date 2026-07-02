<script setup lang="ts">
import { useRouter } from 'vue-router'

defineProps<{
  title: string
  showBack?: boolean
  rightText?: string
}>()

const emit = defineEmits<{
  (e: 'rightClick'): void
  (e: 'back'): void
}>()

const router = useRouter()

function handleBack() {
  emit('back')
  router.back()
}

function handleRightClick() {
  emit('rightClick')
}
</script>

<template>
  <div class="nav-bar">
    <div class="nav-left" @click="handleBack" v-if="showBack">
      <span class="back-icon">←</span>
    </div>
    <div class="nav-left" v-else></div>
    <div class="nav-title">{{ title }}</div>
    <div class="nav-right" @click="handleRightClick" v-if="rightText">
      <span class="right-text">{{ rightText }}</span>
    </div>
    <div class="nav-right" v-else></div>
  </div>
</template>

<style scoped lang="scss">
.nav-bar {
  position: sticky;
  top: 0;
  z-index: 99;
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-base);
  background-color: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
}

.nav-left,
.nav-right {
  width: 60px;
  display: flex;
  align-items: center;
}

.nav-left {
  justify-content: flex-start;
  cursor: pointer;
}

.nav-right {
  justify-content: flex-end;
  cursor: pointer;
}

.back-icon {
  font-size: 20px;
  color: var(--color-text);
}

.right-text {
  font-size: var(--font-size-base);
  color: var(--color-primary);
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
