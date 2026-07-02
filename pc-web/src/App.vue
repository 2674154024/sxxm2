<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

body {
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
  background-color: var(--color-bg-secondary);
  color: var(--color-text);
  font-size: var(--font-size-base);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  color: var(--color-primary);
  text-decoration: none;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--color-primary-light);
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--color-bg-tertiary);
  border-radius: 3px;

  &:hover {
    background: var(--color-text-disabled);
  }
}
</style>