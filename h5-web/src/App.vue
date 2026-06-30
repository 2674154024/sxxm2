<script setup lang="ts">
import { RouterView } from 'vue-router'
<<<<<<< HEAD
import { useUserStore } from '@/stores/user'
import { onMounted } from 'vue'

const userStore = useUserStore()

onMounted(() => {
  userStore.initFromStorage()
})
=======
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
</script>

<template>
  <RouterView />
</template>

<style>
<<<<<<< HEAD
@import '@/styles/variables.scss';
@import '@/styles/common.scss';
@import '@/styles/animations.scss';
</style>
=======
:root {
  --color-primary: #165DFF;
  --color-accent: #FF7D00;
  --color-success: #00B42A;
  --color-danger: #F53F3F;
  --color-bg: #FFFFFF;
  --color-card: #F2F3F5;
  --color-text: #4E5969;
  --color-text-secondary: #86909C;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
  background-color: #F2F3F5;
  color: #4E5969;
}
</style>
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
