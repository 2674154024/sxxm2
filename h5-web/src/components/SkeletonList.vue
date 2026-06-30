<script setup lang="ts">
defineProps<{
  count?: number
}>()
</script>

<template>
  <div class="skeleton-list">
    <div v-for="i in (count || 3)" :key="i" class="skeleton-card">
      <div class="skeleton-header">
        <div class="skeleton-title skeleton-item" style="width: 60%; height: 18px;"></div>
        <div class="skeleton-salary skeleton-item" style="width: 80px; height: 20px;"></div>
      </div>
      <div class="skeleton-enterprise">
        <div class="skeleton-item" style="width: 40%; height: 14px;"></div>
      </div>
      <div class="skeleton-info">
        <div class="skeleton-item" style="width: 30%; height: 12px;"></div>
      </div>
      <div class="skeleton-tags">
        <div class="skeleton-item" style="width: 50px; height: 20px;"></div>
        <div class="skeleton-item" style="width: 60px; height: 20px;"></div>
        <div class="skeleton-item" style="width: 40px; height: 20px;"></div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.skeleton-list {
  padding: 0 var(--spacing-base);
}

.skeleton-card {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-sm);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--spacing-sm);
}

.skeleton-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xs);
}

.skeleton-enterprise {
  margin-bottom: var(--spacing-xs);
}

.skeleton-info {
  margin-bottom: var(--spacing-xs);
}

.skeleton-tags {
  display: flex;
  gap: 6px;
}
</style>
