<script setup lang="ts">
import TabBar from '@/components/TabBar.vue'
</script>

<template>
  <div class="main-layout">
    <div class="layout-content">
      <slot />
    </div>
    <TabBar />
  </div>
</template>

<style scoped lang="scss">
.main-layout {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
}

.layout-content {
  min-height: 100vh;
  padding-bottom: calc(var(--tabbar-height) + env(safe-area-inset-bottom));
}
</style>
