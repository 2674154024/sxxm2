<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import { getApplyList, cancelApply, type ApplyItem } from '@/api/apply'
import { useUserStore } from '@/stores/user'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const tabs = [
  { key: -1, label: '全部', status: undefined },
  { key: 0, label: '待处理', status: 0 },
  { key: 1, label: '已通过', status: 1 },
  { key: 2, label: '已拒绝', status: 2 },
]

const activeTab = ref(-1)
const applyList = ref<ApplyItem[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 10

const statusMap: Record<number, { text: string; color: string; bgColor: string }> = {
  0: { text: '待审核', color: 'var(--color-accent)', bgColor: 'var(--color-accent-bg)' },
  1: { text: '已通过', color: 'var(--color-success)', bgColor: 'var(--color-success-bg)' },
  2: { text: '已拒绝', color: 'var(--color-danger)', bgColor: 'var(--color-danger-bg)' },
  3: { text: '已取消', color: 'var(--color-text-secondary)', bgColor: 'var(--color-bg-secondary)' },
}

function getStatusStyle(status: number) {
  return statusMap[status] || statusMap[0]
}

async function loadList(refresh = false) {
  if (loading.value) return
  if (refresh) {
    page.value = 1
    finished.value = false
    applyList.value = []
  }
  if (finished.value) return

  loading.value = true
  try {
    const params: { status?: number; page: number; size: number } = {
      page: page.value,
      size: pageSize,
    }
    const currentTab = tabs.find(t => t.key === activeTab.value)
    if (currentTab && currentTab.status !== undefined) {
      params.status = currentTab.status
    }

    const res = await getApplyList(params)
    if (res.code === 200) {
      const newList = res.data.list || []
      if (refresh) {
        applyList.value = newList
      } else {
        applyList.value = [...applyList.value, ...newList]
      }
      if (newList.length < pageSize) {
        finished.value = true
      } else {
        page.value++
      }
    }
  } catch (error) {
    console.error('加载投递记录失败:', error)
  } finally {
    loading.value = false
  }
}

function onTabChange(index: number) {
  activeTab.value = tabs[index].key
  loadList(true)
}

function goToJobDetail(jobId: string) {
  router.push(`/job/${jobId}`)
}

async function handleCancel(applyId: string, event: Event) {
  event.stopPropagation()
  showConfirmDialog({
    title: '确认取消',
    message: '确定要取消这次投递吗？',
  }).then(async () => {
      try {
        const res = await cancelApply(applyId)
        if (res.code === 200) {
          showToast({
            message: '已取消投递',
            type: 'success',
          })
          loadList(true)
        }
      } catch (error: any) {
        showToast({
          message: error?.message || '取消失败',
          type: 'fail',
        })
      }
    }).catch(() => {})
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadList()
  }
})
</script>

<template>
  <div class="apply-page">
    <div class="page-header">
      <h1 class="page-title">投递记录</h1>
    </div>

    <div class="tabs-wrapper">
      <div class="tabs">
        <div
          v-for="(tab, index) in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="onTabChange(index)"
        >
          {{ tab.label }}
          <div v-if="activeTab === tab.key" class="tab-underline"></div>
        </div>
      </div>
    </div>

    <div class="apply-list">
      <template v-if="applyList.length > 0">
        <div
          v-for="item in applyList"
          :key="item.apply_id"
          class="apply-card"
          @click="goToJobDetail(item.job_id)"
        >
          <div class="card-header">
            <div class="job-info">
              <span class="job-title">{{ item.job_title }}</span>
              <div class="salary">
                <span class="salary-symbol">¥</span>
                <span class="salary-amount">{{ item.salary_amount }}</span>
                <span class="salary-unit">/时</span>
              </div>
            </div>
            <div
              class="status-tag"
              :style="{
                color: getStatusStyle(item.apply_status).color,
                backgroundColor: getStatusStyle(item.apply_status).bgColor,
              }"
            >
              {{ statusMap[item.apply_status]?.text || item.apply_status_text }}
            </div>
          </div>
          <div class="card-body">
            <span class="enterprise-name">{{ item.enterprise_name }}</span>
          </div>
          <div class="card-footer">
            <span class="apply-time">投递时间：{{ item.create_time }}</span>
            <button
              v-if="item.apply_status === 0"
              class="cancel-btn"
              @click="handleCancel(item.apply_id, $event)"
            >
              取消投递
            </button>
          </div>
        </div>
      </template>

      <div v-else-if="!loading" class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无投递记录</div>
        <div class="empty-sub">快去首页看看有没有合适的兼职吧</div>
        <button class="empty-btn" @click="router.push('/')">去首页</button>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span class="loading-text">加载中...</span>
      </div>

      <div v-if="finished && applyList.length > 0" class="finished-state">
        — 没有更多了 —
      </div>
    </div>
    <TabBar active="/apply" />
  </div>
</template>

<style scoped lang="scss">
.apply-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
}

.page-header {
  padding: var(--spacing-base);
  padding-bottom: var(--spacing-sm);
  background-color: var(--color-bg);
}

.page-title {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.tabs-wrapper {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: var(--color-bg);
}

.tabs {
  display: flex;
  padding: 0 var(--spacing-xs);
}

.tab-item {
  position: relative;
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;

  &.active {
    color: var(--color-text);
    font-weight: 500;
  }
}

.tab-underline {
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  border-radius: 2px;
  background-color: var(--color-primary);
  animation: tabUnderlineIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes tabUnderlineIn {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 24px;
    opacity: 1;
  }
}

.apply-list {
  padding: var(--spacing-sm) var(--spacing-base);
}

.apply-card {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.98);
    background-color: #f7f8fa;
  }

  animation: cardSlideIn 0.3s ease backwards;
}

@keyframes cardSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.job-info {
  flex: 1;
  margin-right: var(--spacing-xs);
}

.job-title {
  display: block;
  font-size: var(--font-size-lg);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 4px;
}

.salary {
  display: flex;
  align-items: baseline;
}

.salary-symbol {
  font-size: var(--font-size-sm);
  color: var(--color-accent);
}

.salary-amount {
  font-size: var(--font-size-lg);
  color: var(--color-accent);
  font-weight: 600;
}

.salary-unit {
  font-size: var(--font-size-sm);
  color: var(--color-accent);
}

.status-tag {
  font-size: var(--font-size-sm);
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}

.card-body {
  margin-bottom: 8px;
}

.enterprise-name {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
}

.apply-time {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.cancel-btn {
  border: none;
  background: none;
  color: var(--color-text-secondary);
  font-size: 12px;
  padding: 0;
  cursor: pointer;

  &:active {
    color: var(--color-primary);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
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

.empty-btn {
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
    background-color: var(--color-primary-dark);
  }
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spinnerRotate 0.8s linear infinite;
  margin-right: 8px;
}

@keyframes spinnerRotate {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.finished-state {
  text-align: center;
  padding: var(--spacing-base);
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
