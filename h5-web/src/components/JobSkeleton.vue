<script setup lang="ts">
defineProps<{
  count?: number
}>()
</script>

<template>
  <div class="skeleton-list">
    <div v-for="i in (count || 3)" :key="i" class="skeleton-card">
      <div class="skeleton-card__header">
        <div class="skeleton skeleton--title"></div>
        <div class="skeleton skeleton--salary"></div>
      </div>
      <div class="skeleton skeleton--company"></div>
      <div class="skeleton-card__tags">
        <div class="skeleton skeleton--tag"></div>
        <div class="skeleton skeleton--tag"></div>
        <div class="skeleton skeleton--tag"></div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  background-color: #fff;
  border-radius: var(--radius-base);
  padding: 12px;
  box-shadow: var(--shadow-sm);

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  &__tags {
    display: flex;
    gap: 6px;
    margin-top: 10px;
  }
}

.skeleton {
  background: linear-gradient(
    90deg,
    #f2f3f5 0%,
    #e5e6eb 50%,
    #f2f3f5 100%
  );
  background-size: 200% 100%;
  animation: skeletonShimmer 1.5s ease-in-out infinite;
  border-radius: 4px;

  &--title {
    width: 60%;
    height: 16px;
  }

  &--salary {
    width: 60px;
    height: 20px;
  }

  &--company {
    width: 50%;
    height: 13px;
  }

  &--tag {
    width: 50px;
    height: 20px;
  }
}

@keyframes skeletonShimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
