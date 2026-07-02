<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { JobItem } from '@/api/job'

const props = defineProps<{
  job: JobItem
}>()

const router = useRouter()

const settlementMap: Record<number, string> = {
  1: '日结',
  2: '周结',
  3: '月结',
}

function getSettlementType(type: number) {
  return settlementMap[type] || '月结'
}

function handleClick() {
  router.push(`/job/${props.job.job_id}`)
}
</script>

<template>
  <div class="job-card card-item" @click="handleClick">
    <div class="job-header">
      <div class="job-title ellipsis-1">{{ job.job_title }}</div>
      <div class="job-salary">
        <span class="salary-symbol">¥</span>
        <span class="salary-amount">{{ job.salary_amount }}</span>
        <span class="salary-unit">/时</span>
      </div>
    </div>
    <div class="job-enterprise">
      <span class="enterprise-name ellipsis-1">{{ job.enterprise_info?.enterprise_name || '未知企业' }}</span>
      <span v-if="job.enterprise_info?.is_certified" class="certified-tag">
        <span class="certified-icon">✓</span>
        认证企业
      </span>
    </div>
    <div class="job-info">
      <div v-if="job.distance !== undefined" class="job-distance">
        <span class="info-icon">📍</span>
        <span>{{ job.distance }}</span>
      </div>
      <div v-if="job.work_address" class="job-address ellipsis-1">
        {{ job.work_address }}
      </div>
    </div>
    <div class="job-tags">
      <span class="tag tag-orange">{{ getSettlementType(job.settlement_type) }}</span>
      <span v-if="job.is_insured" class="tag tag-success">含意外险</span>
      <span v-if="job.industry_tag" class="tag tag-default">{{ job.skill_require || job.industry_tag }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.job-card {
  background-color: #fff;
  border-radius: var(--radius-base);
  padding: var(--spacing-sm);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--spacing-sm);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}

.job-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--spacing-xs);
}

.job-title {
  flex: 1;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin-right: var(--spacing-xs);
}

.job-salary {
  display: flex;
  align-items: baseline;
  flex-shrink: 0;
}

.salary-symbol {
  font-size: var(--font-size-sm);
  color: var(--color-accent);
  font-weight: 500;
}

.salary-amount {
  font-size: var(--font-size-xl);
  color: var(--color-accent);
  font-weight: 600;
  line-height: 1;
}

.salary-unit {
  font-size: var(--font-size-sm);
  color: var(--color-accent);
}

.job-enterprise {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.enterprise-name {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  margin-right: var(--spacing-xs);
}

.certified-tag {
  display: inline-flex;
  align-items: center;
  padding: 1px 6px;
  border-radius: 4px;
  background-color: var(--color-primary-bg);
  color: var(--color-primary);
  font-size: 10px;
  flex-shrink: 0;

  .certified-icon {
    margin-right: 2px;
  }
}

.job-info {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.job-distance {
  display: flex;
  align-items: center;
  margin-right: var(--spacing-sm);
  flex-shrink: 0;

  .info-icon {
    margin-right: 2px;
  }
}

.job-address {
  flex: 1;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  font-size: var(--font-size-sm);
  line-height: 18px;
}

.tag-orange {
  background-color: var(--color-accent);
  color: #fff;
}

.tag-success {
  background-color: var(--color-success-bg);
  color: var(--color-success);
}

.tag-default {
  background-color: var(--color-bg-secondary);
  color: var(--color-text);
}
</style>
