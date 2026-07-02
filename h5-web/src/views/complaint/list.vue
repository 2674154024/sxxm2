<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { getComplaintList, type ComplaintItem } from '@/api/complaint'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const list = ref<ComplaintItem[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = 10

async function loadList() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    loading.value = true
    const res = await getComplaintList({ page: page.value, size })
    if (res.code === 200) {
      const newList = res.data.list || []
      if (page.value === 1) {
        list.value = newList
      } else {
        list.value = [...list.value, ...newList]
      }
      if (newList.length < size) {
        finished.value = true
      }
    }
  } catch (error) {
    console.error('加载投诉列表失败:', error)
    showToast({ message: '加载失败', type: 'fail' })
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="complaint-list-page">
    <NavBar title="我的投诉" show-back @back="goBack" />

    <div class="list-container">
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无投诉记录</div>
      </div>

      <div v-else class="complaint-list">
        <div
          v-for="item in list"
          :key="item.complaint_id"
          class="complaint-card"
        >
          <div class="card-header">
            <span class="complaint-type">{{ item.type_text }}</span>
            <span class="complaint-status" :class="'status-' + item.status">
              {{ item.status_text }}
            </span>
          </div>
          <div v-if="item.job_title" class="job-info">
            <span class="job-label">关联岗位：</span>
            <span class="job-title">{{ item.job_title }}</span>
          </div>
          <div class="complaint-content">{{ item.content }}</div>
          <div v-if="item.images && item.images.length > 0" class="image-list">
            <img
              v-for="(img, idx) in item.images.slice(0, 3)"
              :key="idx"
              :src="img"
              class="preview-image"
              alt="证据图片"
            />
            <div v-if="item.images.length > 3" class="more-images">
              +{{ item.images.length - 3 }}
            </div>
          </div>
          <div class="card-footer">
            <span class="create-time">{{ item.create_time }}</span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-more">加载中...</div>
      <div v-if="finished && list.length > 0" class="no-more">没有更多了</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.complaint-list-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
}

.list-container {
  padding: var(--spacing-sm);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.complaint-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.complaint-card {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.complaint-type {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
}

.complaint-status {
  font-size: var(--font-size-sm);
  padding: 2px 8px;
  border-radius: 10px;

  &.status-0 {
    color: var(--color-warning);
    background-color: rgba(255, 152, 0, 0.1);
  }

  &.status-1 {
    color: var(--color-primary);
    background-color: rgba(25, 137, 250, 0.1);
  }

  &.status-2 {
    color: var(--color-success);
    background-color: rgba(82, 196, 26, 0.1);
  }

  &.status-3 {
    color: var(--color-text-secondary);
    background-color: var(--color-bg-secondary);
  }
}

.job-info {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
}

.job-label {
  color: var(--color-text-secondary);
}

.job-title {
  color: var(--color-text);
}

.complaint-content {
  font-size: var(--font-size-base);
  color: var(--color-text);
  line-height: 1.6;
  margin-bottom: var(--spacing-sm);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-list {
  display: flex;
  gap: 8px;
  margin-bottom: var(--spacing-sm);
}

.preview-image {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.more-images {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-sm);
  background-color: var(--color-bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.card-footer {
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border);
}

.create-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-disabled);
}

.loading-more,
.no-more {
  text-align: center;
  padding: var(--spacing-base);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}
</style>
