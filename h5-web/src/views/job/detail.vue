<script setup lang="ts">
<<<<<<< HEAD
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import JobCard from '@/components/JobCard.vue'
import { mockJobList, type JobItem } from '@/api/job'
import { useUserStore } from '@/stores/user'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const jobId = computed(() => route.params.id as string)
const loading = ref(true)
const jobDetail = ref<JobItem | null>(null)
const similarJobs = ref<JobItem[]>([])
const isFavorite = ref(false)
const applyStatus = ref<'normal' | 'applying' | 'success' | 'applied'>('normal')
const showApplySuccess = ref(false)
const showDescAll = ref(false)

const settlementMap: Record<number, string> = {
  1: '日结',
  2: '周结',
  3: '月结',
}

function getSettlementType(type: number) {
  return settlementMap[type] || '月结'
}

async function loadJobDetail() {
  try {
    loading.value = true
    const job = mockJobList.find((j) => j.job_id === jobId.value) || mockJobList[0]
    jobDetail.value = job
  } catch (error) {
    console.error('加载岗位详情失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadSimilarJobs() {
  try {
    similarJobs.value = mockJobList.filter((j) => j.job_id !== jobId.value).slice(0, 3)
  } catch (error) {
    console.error('加载相似岗位失败:', error)
  }
}

function toggleFavorite() {
  isFavorite.value = !isFavorite.value
  showToast({
    message: isFavorite.value ? '收藏成功' : '已取消收藏',
    duration: 1500,
  })
}

function handleApply() {
  if (applyStatus.value === 'applied' || applyStatus.value === 'applying') {
    return
  }

  if (!userStore.isLoggedIn) {
    showConfirmDialog({
      title: '提示',
      message: '请先登录后再投递',
    }).then(() => {
      router.push('/login')
    }).catch(() => {})
    return
  }

  applyStatus.value = 'applying'

  setTimeout(() => {
    applyStatus.value = 'success'
    showApplySuccess.value = true

    setTimeout(() => {
      showApplySuccess.value = false
      applyStatus.value = 'applied'
    }, 2000)
  }, 800)
}

onMounted(() => {
  loadJobDetail()
  loadSimilarJobs()
})
</script>

<template>
  <div class="job-detail-page">
    <NavBar title="岗位详情" show-back />

    <div v-if="jobDetail && !loading" class="detail-content">
      <div class="job-header">
        <div class="job-title-section">
          <h1 class="job-title">{{ jobDetail.job_title }}</h1>
          <div class="job-salary">
            <span class="salary-symbol">¥</span>
            <span class="salary-amount">{{ jobDetail.salary_amount }}</span>
            <span class="salary-unit">/时</span>
          </div>
        </div>

        <div class="enterprise-section">
          <div class="enterprise-info">
            <span class="enterprise-name">{{ jobDetail.enterprise_info.enterprise_name }}</span>
            <span v-if="jobDetail.enterprise_info.is_certified" class="certified-badge">
              ✓ 认证企业
            </span>
          </div>
          <div class="credit-score">
            信用分: {{ jobDetail.enterprise_info.credit_score }}
          </div>
        </div>

        <div class="job-meta">
          <div class="meta-item">
            <span class="meta-icon">📍</span>
            <span class="meta-text ellipsis-1">{{ jobDetail.work_address }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-icon">👥</span>
            <span class="meta-text">招{{ jobDetail.recruit_num }}人(已招{{ jobDetail.current_num }}人)</span>
          </div>
          <div class="meta-item">
            <span class="meta-icon">💰</span>
            <span class="meta-text">{{ getSettlementType(jobDetail.settlement_type) }}</span>
            <span v-if="jobDetail.is_insured" class="insured-tag">含意外险</span>
          </div>
        </div>

        <div class="job-tags" v-if="jobDetail.skill_require">
          <span class="tag tag--default">{{ jobDetail.skill_require }}</span>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">岗位描述</h3>
        <div class="job-desc" :class="{ 'is-expanded': showDescAll }">
          {{ jobDetail.job_desc }}
        </div>
        <div class="desc-toggle" @click="showDescAll = !showDescAll">
          {{ showDescAll ? '收起' : '展开' }}
          <span class="desc-toggle__arrow">{{ showDescAll ? '↑' : '↓' }}</span>
        </div>
      </div>

      <div class="safety-card">
        <div class="safety-item">
          <span class="safety-icon">🛡️</span>
          <span class="safety-text">本岗位支持薪资托管，薪资由平台担保发放</span>
        </div>
        <div v-if="jobDetail.is_insured" class="safety-item">
          <span class="safety-icon">✅</span>
          <span class="safety-text">已为兼职人员购买意外险</span>
        </div>
      </div>

      <div class="section" v-if="similarJobs.length > 0">
        <h3 class="section-title">相似岗位</h3>
        <div class="similar-scroll">
          <div class="similar-list">
            <div v-for="job in similarJobs" :key="job.job_id" class="similar-item">
              <JobCard :job="job" />
            </div>
          </div>
        </div>
      </div>

      <div class="bottom-placeholder"></div>
    </div>

    <div v-if="loading" class="detail-skeleton">
      <div class="skeleton-card">
        <div class="skeleton skeleton--title"></div>
        <div class="skeleton skeleton--salary"></div>
        <div class="skeleton skeleton--company"></div>
        <div class="skeleton skeleton--meta"></div>
        <div class="skeleton skeleton--meta"></div>
      </div>
      <div class="skeleton-card">
        <div class="skeleton skeleton--section-title"></div>
        <div class="skeleton skeleton--desc"></div>
        <div class="skeleton skeleton--desc"></div>
      </div>
    </div>

    <div class="bottom-bar">
      <div class="action-left" @click="toggleFavorite">
        <span class="action-icon star-btn" :class="{ active: isFavorite }">
          {{ isFavorite ? '⭐' : '☆' }}
        </span>
        <span class="action-text">收藏</span>
      </div>
      <button
        class="apply-btn"
        :class="{
          'btn-applying': applyStatus === 'applying',
          'btn-success': applyStatus === 'success' || applyStatus === 'applied',
          'btn-disabled': applyStatus === 'applied'
        }"
        @click="handleApply"
        :disabled="applyStatus === 'applied'"
      >
        <template v-if="applyStatus === 'applying'">投递中...</template>
        <template v-else-if="applyStatus === 'success'">✓ 投递成功</template>
        <template v-else-if="applyStatus === 'applied'">已投递</template>
        <template v-else>立即投递</template>
      </button>
    </div>

    <transition name="fade">
      <div v-if="showApplySuccess" class="apply-success-overlay">
        <div class="success-content">
          <div class="success-check">✓</div>
          <div class="success-text">投递成功</div>
          <div class="success-sub">企业将尽快与你联系</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped lang="scss">
.job-detail-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
}

.detail-content {
  animation: fadeInUp 0.3s var(--ease-out-expo);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.job-header {
  background-color: #fff;
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
}

.job-title-section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.job-title {
  flex: 1;
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  margin-right: var(--spacing-xs);
  line-height: 1.4;
}

.job-salary {
  display: flex;
  align-items: baseline;
  flex-shrink: 0;
}

.salary-symbol {
  font-size: var(--font-size-base);
  color: var(--color-accent);
  font-weight: 500;
}

.salary-amount {
  font-size: var(--font-size-xxl);
  color: var(--color-accent);
  font-weight: 600;
  line-height: 1;
}

.salary-unit {
  font-size: var(--font-size-sm);
  color: var(--color-accent);
}

.enterprise-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border);
}

.enterprise-info {
  display: flex;
  align-items: center;
}

.enterprise-name {
  font-size: var(--font-size-base);
  color: var(--color-text);
  margin-right: var(--spacing-xs);
}

.certified-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  background-color: var(--color-primary-bg);
  color: var(--color-primary);
  font-size: 12px;
}

.credit-score {
  font-size: var(--font-size-sm);
  color: var(--color-accent);
  font-weight: 500;
}

.job-meta {
  margin-bottom: var(--spacing-xs);
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.meta-icon {
  margin-right: 8px;
  font-size: 16px;
}

.meta-text {
  flex: 1;
}

.insured-tag {
  margin-left: 8px;
  padding: 1px 6px;
  border-radius: 4px;
  background-color: var(--color-success-bg);
  color: var(--color-success);
  font-size: 12px;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.tag--default {
  background-color: var(--color-bg-secondary);
  color: var(--color-text);
}

.section {
  background-color: #fff;
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.job-desc {
  font-size: var(--font-size-base);
  color: var(--color-text);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;

  &.is-expanded {
    -webkit-line-clamp: unset;
    display: block;
  }
}

.desc-toggle {
  margin-top: 8px;
  font-size: 13px;
  color: var(--color-primary);
  text-align: center;
  cursor: pointer;

  &__arrow {
    margin-left: 2px;
  }
}

.safety-card {
  margin: 0 var(--spacing-base) var(--spacing-sm);
  padding: var(--spacing-sm);
  border: 1px solid var(--color-success);
  border-radius: var(--radius-base);
  background-color: var(--color-success-bg);
}

.safety-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }
}

.safety-icon {
  margin-right: 8px;
  font-size: 18px;
}

.safety-text {
  font-size: var(--font-size-sm);
  color: var(--color-success);
}

.similar-scroll {
  margin: 0 calc(var(--spacing-base) * -1);
  padding: 0 var(--spacing-base);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.similar-list {
  display: flex;
  gap: var(--spacing-sm);
  width: max-content;
}

.similar-item {
  width: 260px;
  flex-shrink: 0;
}

.bottom-placeholder {
  height: var(--spacing-lg);
}

.detail-skeleton {
  padding: var(--spacing-base);
}

.skeleton-card {
  background: #fff;
  border-radius: var(--radius-base);
  padding: 16px;
  margin-bottom: 12px;
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
    width: 70%;
    height: 22px;
  }

  &--salary {
    width: 30%;
    height: 24px;
  }

  &--company {
    width: 50%;
    height: 14px;
  }

  &--meta {
    width: 80%;
    height: 14px;
  }

  &--section-title {
    width: 30%;
    height: 18px;
  }

  &--desc {
    width: 100%;
    height: 14px;
  }
}

@keyframes skeletonShimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  padding-bottom: env(safe-area-inset-bottom);
  background-color: #fff;
  display: flex;
  align-items: center;
  padding-left: var(--spacing-base);
  padding-right: var(--spacing-base);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
  z-index: 100;
}

.action-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 60px;
  cursor: pointer;
  margin-right: var(--spacing-base);
}

.action-icon {
  font-size: 24px;
  line-height: 1;
  margin-bottom: 2px;
}

.star-btn {
  transition: all 0.3s ease;
}

.star-btn.active {
  animation: starPop 300ms var(--ease-bounce);
}

@keyframes starPop {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.action-text {
  font-size: 10px;
  color: var(--color-text-secondary);
}

.apply-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 22px;
  background-color: var(--color-primary);
  color: #fff;
  font-size: var(--font-size-lg);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);

  &:active:not(:disabled) {
    transform: scale(0.96);
    background-color: var(--color-primary-dark);
  }

  &.btn-applying {
    background-color: var(--color-primary-light);
  }

  &.btn-success {
    background-color: var(--color-success);
  }

  &.btn-disabled {
    background-color: var(--color-text-disabled);
    cursor: not-allowed;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.apply-success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl) var(--spacing-lg);
  background-color: #fff;
  border-radius: var(--radius-lg);
  animation: successPop 0.5s var(--ease-out-expo);
}

@keyframes successPop {
  0% {
    transform: scale(0.5);
    opacity: 0;
  }
  70% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-check {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background-color: var(--color-success);
  color: #fff;
  font-size: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-base);
  animation: checkmarkAppear 0.6s var(--ease-bounce);
}

@keyframes checkmarkAppear {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-text {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.success-sub {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}
</style>
=======
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
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
