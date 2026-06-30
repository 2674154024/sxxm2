<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { onMounted } from 'vue'

const userStore = useUserStore()

onMounted(() => {
  userStore.initFromStorage()
})
</script>

<template>
  <RouterView />
</template>

<style>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';
@import '@/styles/animations.scss';
</style>
