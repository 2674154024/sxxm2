<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import JobCard from '@/components/JobCard.vue'
import { getFavoriteList, toggleFavorite, type JobItem } from '@/api/job'

const router = useRouter()
const list = ref<JobItem[]>([])
const loading = ref(false)
const finished = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await getFavoriteList({ page: 1, size: 20 })
    if (res.code === 200) {
      list.value = res.data.list
      finished.value = true
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleJobClick(id: string) {
  router.push(`/job/${id}`)
}

async function handleFavorite(id: string) {
  try {
    await toggleFavorite(id)
    list.value = list.value.filter((item) => item.job_id !== id)
  } catch (error) {
    console.error('取消收藏失败:', error)
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="favorite-page">
    <NavBar title="我的收藏" show-back @back="goBack" />

    <div v-if="!loading && list.length > 0" class="job-list">
      <JobCard
        v-for="item in list"
        :key="item.job_id"
        :job="item"
        :show-favorite="true"
        @click="handleJobClick(item.job_id)"
        @favorite="handleFavorite(item.job_id)"
      />
    </div>

    <div v-if="!loading && list.length === 0" class="empty-state">
      <div class="empty-icon">⭐</div>
      <div class="empty-text">暂无收藏</div>
      <div class="empty-sub">去首页收藏喜欢的岗位吧</div>
      <button class="go-btn" @click="router.push('/')">
        去看看
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.favorite-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
}

.job-list {
  padding: var(--spacing-sm);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120px 0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--spacing-base);
}

.empty-text {
  font-size: var(--font-size-lg);
  color: var(--color-text);
  margin-bottom: 8px;
}

.empty-sub {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-lg);
}

.go-btn {
  padding: 10px 32px;
  border: none;
  border-radius: 20px;
  background-color: var(--color-primary);
  color: #fff;
  font-size: var(--font-size-base);
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.96);
    opacity: 0.9;
  }
}
</style>
