<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const jobId = route.params.id
</script>

<template>
  <div class="job-detail">
    <h2>岗位详情 - {{ jobId }}</h2>
    <div class="detail-content">岗位详情内容占位</div>
  </div>
</template>

<style scoped>
.job-detail {
  padding: 16px;
}

.job-detail h2 {
  margin-bottom: 16px;
  color: #4E5969;
}

.detail-content {
  background-color: #FFFFFF;
  padding: 12px;
  border-radius: 8px;
}
</style>