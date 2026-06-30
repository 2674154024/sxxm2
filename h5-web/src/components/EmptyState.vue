<script setup lang="ts">
defineProps<{
  icon?: string
  title?: string
  description?: string
  btnText?: string
}>()

const emit = defineEmits<{
  (e: 'btn-click'): void
}>()
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__icon">{{ icon || '📭' }}</div>
    <div class="empty-state__title">{{ title || '暂无数据' }}</div>
    <div v-if="description" class="empty-state__desc">{{ description }}</div>
    <button
      v-if="btnText"
      class="empty-state__btn"
      @click="emit('btn-click')"
    >
      {{ btnText }}
    </button>
  </div>
</template>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  text-align: center;

  &__icon {
    font-size: 64px;
    margin-bottom: 16px;
    animation: floatY 3s ease-in-out infinite;
  }

  &__title {
    font-size: 16px;
    color: var(--color-text);
    margin-bottom: 8px;
  }

  &__desc {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-bottom: 20px;
  }

  &__btn {
    height: 36px;
    padding: 0 24px;
    border-radius: 18px;
    background-color: var(--color-primary);
    color: #fff;
    font-size: 14px;
    transition: all var(--transition-fast);

    &:active {
      transform: scale(0.96);
      background-color: var(--color-primary-dark);
    }
  }
}

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
</style>
