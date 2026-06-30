<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import TabBar from '@/components/TabBar.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'applied', label: '已投递' },
  { key: 'interview', label: '待面试' },
  { key: 'hired', label: '已录用' },
  { key: 'rejected', label: '已拒绝' },
]

const activeTab = ref('all')
const loading = ref(true)

const applyList = ref([
  {
    id: 1,
    jobId: 1,
    title: '茶颜悦色门店店员',
    salary: 18,
    companyName: '茶颜悦色(五一广场店)',
    applyTime: '2026-06-28 14:30',
    status: 'hired',
    statusText: '已录用',
  },
  {
    id: 2,
    jobId: 2,
    title: '星巴克咖啡师',
    salary: 22,
    companyName: '星巴克(IFS店)',
    applyTime: '2026-06-27 10:15',
    status: 'interview',
    statusText: '待面试',
  },
  {
    id: 3,
    jobId: 3,
    title: '优衣库店员',
    salary: 19,
    companyName: '优衣库(德思勤店)',
    applyTime: '2026-06-26 16:20',
    status: 'applied',
    statusText: '已投递',
  },
])

function getStatusClass(status: string) {
  const map: Record<string, string> = {
    applied: 'tag--primary',
    interview: 'tag--warning',
    hired: 'tag--success',
    rejected: 'tag--default',
  }
  return map[status] || 'tag--default'
}

function handleTabChange(key: string) {
  activeTab.value = key
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

function handleCancel(applyId: number) {
  console.log('取消投递', applyId)
}

function goJobDetail(jobId: number) {
  router.push(`/job/detail/${jobId}`)
}

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 600)
})
</script>

<template>
  <div class="apply-page">
    <NavBar title="投递记录" show-back />

    <div class="tab-bar">
      <div class="tab-bar__scroll">
        <div class="tab-bar__list">
          <div
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-bar__item"
            :class="{ 'is-active': activeTab === tab.key }"
            @click="handleTabChange(tab.key)"
          >
            {{ tab.label }}
            <div class="tab-bar__indicator"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div v-if="loading" class="apply-list">
        <div v-for="i in 3" :key="i" class="apply-card">
          <div class="skeleton skeleton--title"></div>
          <div class="skeleton skeleton--company"></div>
          <div class="skeleton skeleton--time"></div>
        </div>
      </div>

      <div v-else-if="applyList.length === 0">
        <EmptyState
          icon="📋"
          title="暂无投递记录"
          description="快去看看有没有合适的岗位吧"
          btn-text="去逛逛"
          @btn-click="router.replace('/')"
        />
      </div>

      <div v-else class="apply-list">
        <div
          v-for="item in applyList"
          :key="item.id"
          class="apply-card"
          @click="goJobDetail(item.jobId)"
        >
          <div class="apply-card__header">
            <div class="apply-card__title ellipsis-1">{{ item.title }}</div>
            <div class="apply-card__salary">¥{{ item.salary }}/时</div>
          </div>
          <div class="apply-card__company ellipsis-1">{{ item.companyName }}</div>
          <div class="apply-card__time">投递时间: {{ item.applyTime }}</div>
          <div class="apply-card__footer">
            <span :class="['tag', getStatusClass(item.status)]">
              {{ item.statusText }}
            </span>
            <div class="apply-card__actions">
              <button
                v-if="item.status === 'applied' || item.status === 'interview'"
                class="apply-card__btn apply-card__btn--cancel"
                @click.stop="handleCancel(item.id)"
              >
                取消投递
              </button>
              <button class="apply-card__btn apply-card__btn--detail">
                查看详情 ›
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <TabBar />
  </div>
</template>

<style scoped lang="scss">
.apply-page {
  min-height: 100vh;
  padding-bottom: calc(50px + env(safe-area-inset-bottom));
  background-color: var(--color-bg-secondary);
}

.tab-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #fff;
  border-bottom: 1px solid var(--color-border);

  &__scroll {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;

    &::-webkit-scrollbar {
      display: none;
    }
  }

  &__list {
    display: inline-flex;
    padding: 0 8px;
  }

  &__item {
    position: relative;
    flex-shrink: 0;
    padding: 12px 16px;
    font-size: 14px;
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: color var(--transition-fast);

    &.is-active {
      color: var(--color-primary);
      font-weight: 500;

      .tab-bar__indicator {
        transform: scaleX(1);
      }
    }
  }

  &__indicator {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 20px;
    height: 3px;
    background: var(--color-primary);
    border-radius: 2px;
    transition: transform 250ms var(--ease-out-expo);
  }
}

.page-content {
  padding: 12px 16px;
}

.apply-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.apply-card {
  background: #fff;
  border-radius: var(--radius-base);
  padding: 14px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  &__title {
    flex: 1;
    font-size: 15px;
    font-weight: 600;
    color: var(--color-text);
    margin-right: 12px;
  }

  &__salary {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-accent);
    white-space: nowrap;
  }

  &__company {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-bottom: 8px;
  }

  &__time {
    font-size: 12px;
    color: var(--color-text-disabled);
    margin-bottom: 12px;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 12px;
    border-top: 1px solid var(--color-border);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__btn {
    font-size: 13px;
    padding: 4px 10px;
    border-radius: 14px;
    cursor: pointer;
    transition: all var(--transition-fast);

    &--cancel {
      background: var(--color-danger-bg);
      color: var(--color-danger);

      &:active {
        opacity: 0.8;
      }
    }

    &--detail {
      background: transparent;
      color: var(--color-primary);

      &:active {
        opacity: 0.7;
      }
    }
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
    margin-bottom: 8px;
  }

  &--company {
    width: 40%;
    height: 13px;
    margin-bottom: 8px;
  }

  &--time {
    width: 30%;
    height: 12px;
  }
}

@keyframes skeletonShimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
