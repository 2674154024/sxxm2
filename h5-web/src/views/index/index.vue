<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import JobCard from '@/components/JobCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import FilterPanel, { type FilterData } from '@/components/FilterPanel.vue'
import { mockJobList, type JobItem, getCategories, type CategoryItem } from '@/api/job'

const router = useRouter()

const loading = ref(true)
const jobList = ref<JobItem[]>([])
const categories = ref<CategoryItem[]>([])
const activeCategory = ref('all')
const showFilter = ref(false)
const filterData = ref<FilterData>({})

const hasFilter = computed(() => {
  const d = filterData.value
  return !!(
    d.salary_min !== undefined ||
    d.salary_max !== undefined ||
    d.settlement_type !== undefined ||
    d.distance !== undefined ||
    d.is_insured ||
    d.work_time
  )
})

async function loadCategories() {
  const res = await getCategories()
  if (res.code === 200) {
    categories.value = res.data
  }
}

function handleCategoryChange(key: string) {
  activeCategory.value = key
  applyFilters()
}

function applyFilters() {
  loading.value = true
  setTimeout(() => {
    let list = activeCategory.value === 'all'
      ? [...mockJobList]
      : mockJobList.filter((job) => job.industry_tag === activeCategory.value)

    const d = filterData.value

    if (d.salary_min !== undefined) {
      list = list.filter((job) => job.salary_amount >= d.salary_min!)
    }
    if (d.salary_max !== undefined) {
      list = list.filter((job) => job.salary_amount <= d.salary_max!)
    }
    if (d.settlement_type !== undefined) {
      list = list.filter((job) => job.settlement_type === d.settlement_type)
    }
    if (d.is_insured) {
      list = list.filter((job) => job.is_insured)
    }

    jobList.value = list
    loading.value = false
  }, 300)
}

function handleFilterConfirm(data: FilterData) {
  filterData.value = data
  applyFilters()
}

function handleFilterReset() {
  filterData.value = {}
  applyFilters()
}

function openFilter() {
  showFilter.value = true
}

function goToSearch() {
  console.log('跳转到搜索页')
}

function goToSafety() {
  router.push('/safety')
}

onMounted(() => {
  loadCategories()
  setTimeout(() => {
    jobList.value = mockJobList
    loading.value = false
  }, 600)
})
</script>

<template>
  <div class="home-page">
    <div class="notice-bar">
      <div class="notice-icon">⚠️</div>
      <div class="notice-content">
        <div class="notice-text">平台严禁收取任何押金、培训费、中介费！如遇收费请立即举报</div>
      </div>
      <div class="notice-arrow" @click="goToSafety">›</div>
    </div>

    <div class="search-bar" @click="goToSearch">
      <span class="search-icon">🔍</span>
      <span class="search-placeholder">搜索兼职岗位/企业/商圈</span>
    </div>

    <div class="category-scroll">
      <div class="category-list">
        <div
          v-for="cat in categories"
          :key="cat.key"
          class="category-item"
          :class="{ active: activeCategory === cat.key }"
          @click="handleCategoryChange(cat.key)"
        >
          {{ cat.label }}
        </div>
      </div>
    </div>

    <div class="section-header">
      <span class="section-title">附近好工作</span>
      <div class="section-actions">
        <span class="filter-btn" :class="{ active: hasFilter }" @click="openFilter">
          <span class="filter-icon">⚙️</span>
          筛选
        </span>
        <span class="section-more" @click="handleCategoryChange(activeCategory)">
          刷新
          <span class="more-icon">🔄</span>
        </span>
      </div>
    </div>

    <div class="job-list-wrapper">
      <div v-if="loading" class="skeleton-list">
        <div v-for="i in 3" :key="i" class="skeleton-card">
          <div class="skeleton skeleton--title"></div>
          <div class="skeleton skeleton--company"></div>
          <div class="skeleton skeleton--tags"></div>
        </div>
      </div>

      <div v-else-if="jobList.length > 0">
        <JobCard v-for="job in jobList" :key="job.job_id" :job="job" />
      </div>

      <div v-else>
        <EmptyState
          icon="📭"
          title="暂无附近岗位"
          description="换个分类看看吧"
          btn-text="全部岗位"
          @btn-click="handleCategoryChange('all')"
        />
      </div>
    </div>

    <div class="safety-tip">
      <span class="safety-icon">🛡️</span>
      <span class="safety-text">本平台所有岗位支持薪资托管，无押金风险</span>
    </div>

    <FilterPanel
      v-model:visible="showFilter"
      :default-data="filterData"
      @confirm="handleFilterConfirm"
      @reset="handleFilterReset"
    />

    <TabBar />
  </div>
</template>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  padding-bottom: calc(50px + env(safe-area-inset-bottom));
  background-color: var(--color-bg-secondary);
}

.notice-bar {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 var(--spacing-base);
  background-color: var(--color-danger-bg);
  overflow: hidden;
}

.notice-icon {
  font-size: 14px;
  margin-right: 8px;
  flex-shrink: 0;
}

.notice-content {
  flex: 1;
  overflow: hidden;
}

.notice-text {
  font-size: 13px;
  color: var(--color-danger);
  white-space: nowrap;
  animation: noticeScroll 15s linear infinite;
}

@keyframes noticeScroll {
  0% { transform: translateX(100%); }
  100% { transform: translateX(-100%); }
}

.notice-arrow {
  font-size: 18px;
  color: var(--color-danger);
  margin-left: 8px;
  flex-shrink: 0;
  cursor: pointer;
}

.search-bar {
  display: flex;
  align-items: center;
  height: 40px;
  margin: var(--spacing-sm) var(--spacing-base);
  padding: 0 var(--spacing-base);
  background-color: #fff;
  border-radius: 20px;
  cursor: pointer;
  transition: all var(--transition-fast);

  &:active {
    opacity: 0.8;
  }
}

.search-icon {
  font-size: 16px;
  margin-right: 8px;
}

.search-placeholder {
  font-size: var(--font-size-base);
  color: var(--color-text-disabled);
}

.category-scroll {
  margin-bottom: var(--spacing-sm);
}

.category-list {
  display: flex;
  gap: var(--spacing-xs);
  padding: 0 var(--spacing-base);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.category-item {
  flex-shrink: 0;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  background-color: #fff;
  cursor: pointer;
  transition: all var(--transition-fast);

  &.active {
    color: #fff;
    background-color: var(--color-primary);
    font-weight: 500;
  }

  &:active {
    transform: scale(0.95);
  }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-base);
  margin-bottom: var(--spacing-xs);
}

.section-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.filter-btn {
  display: flex;
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.2s ease;

  &.active {
    color: var(--color-primary);
  }

  &:active {
    opacity: 0.7;
  }
}

.filter-icon {
  margin-right: 2px;
  font-size: 14px;
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
}

.section-more {
  display: flex;
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  cursor: pointer;
}

.more-icon {
  margin-left: 2px;
  font-size: 12px;
}

.job-list-wrapper {
  padding: 0 var(--spacing-base);
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  background: #fff;
  border-radius: var(--radius-base);
  padding: 14px;
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
  margin-bottom: 10px;

  &--title {
    width: 60%;
    height: 18px;
  }

  &--company {
    width: 40%;
    height: 13px;
  }

  &--tags {
    width: 50%;
    height: 16px;
    margin-bottom: 0;
  }
}

@keyframes skeletonShimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.safety-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px var(--spacing-base);
  margin: var(--spacing-sm) var(--spacing-base) var(--spacing-lg);
  background-color: var(--color-primary-bg);
  border-radius: var(--radius-base);
}

.safety-icon {
  font-size: 18px;
  margin-right: 8px;
}

.safety-text {
  font-size: var(--font-size-sm);
  color: var(--color-primary);
}
</style>
