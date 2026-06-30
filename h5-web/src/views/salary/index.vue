<script setup lang="ts">
import { ref, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(true)
const currentMonth = ref('2026-06')

const summary = ref({
  monthIncome: 2680,
  totalIncome: 12580,
  pendingAmount: 540,
})

const salaryList = ref([
  {
    id: 1,
    companyName: '茶颜悦色(五一广场店)',
    jobTitle: '门店店员',
    hours: 20,
    hourlyRate: 18,
    shouldPay: 360,
    tax: 0,
    actualPay: 360,
    status: 'paid',
    statusText: '已到账',
    payTime: '06-29 15:30',
  },
  {
    id: 2,
    companyName: '星巴克(IFS店)',
    jobTitle: '咖啡师',
    hours: 16,
    hourlyRate: 22,
    shouldPay: 352,
    tax: 0,
    actualPay: 352,
    status: 'pending',
    statusText: '待发放',
    payTime: '预计 07-01',
  },
  {
    id: 3,
    companyName: '优衣库(德思勤店)',
    jobTitle: '店员',
    hours: 8,
    hourlyRate: 19,
    shouldPay: 152,
    tax: 0,
    actualPay: 152,
    status: 'confirm',
    statusText: '待确认工时',
    payTime: '',
  },
])

function getStatusClass(status: string) {
  const map: Record<string, string> = {
    paid: 'tag--success',
    pending: 'tag--warning',
    confirm: 'tag--default',
    rejected: 'tag--danger',
  }
  return map[status] || 'tag--default'
}

function prevMonth() {
  const date = new Date(currentMonth.value + '-01')
  date.setMonth(date.getMonth() - 1)
  currentMonth.value = date.toISOString().slice(0, 7)
  loadData()
}

function nextMonth() {
  const date = new Date(currentMonth.value + '-01')
  date.setMonth(date.getMonth() + 1)
  currentMonth.value = date.toISOString().slice(0, 7)
  loadData()
}

function formatMonth(monthStr: string) {
  const [year, month] = monthStr.split('-')
  return `${year}年${parseInt(month)}月`
}

function loadData() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

function goDetail(id: number) {
  console.log('薪资明细', id)
}

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 600)
})
</script>

<template>
  <div class="salary-page">
    <NavBar title="薪资流水" show-back />

    <div class="salary-header">
      <div class="salary-header__bg"></div>
      <div class="salary-header__content">
        <div class="salary-header__label">本月收入</div>
        <div class="salary-header__amount">
          ¥<span class="salary-header__num">{{ summary.monthIncome.toFixed(2) }}</span>
        </div>
        <div class="salary-header__divider"></div>
        <div class="salary-header__stats">
          <div class="salary-header__stat">
            <div class="salary-header__stat-label">累计收入</div>
            <div class="salary-header__stat-value">¥{{ summary.totalIncome.toFixed(2) }}</div>
          </div>
          <div class="salary-header__stat">
            <div class="salary-header__stat-label">待结算</div>
            <div class="salary-header__stat-value">¥{{ summary.pendingAmount.toFixed(2) }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="month-filter">
      <div class="month-filter__arrow" @click="prevMonth">‹</div>
      <div class="month-filter__text">{{ formatMonth(currentMonth) }}</div>
      <div class="month-filter__arrow" @click="nextMonth">›</div>
    </div>

    <div class="salary-list">
      <div v-if="loading">
        <div v-for="i in 3" :key="i" class="salary-card">
          <div class="skeleton skeleton--company"></div>
          <div class="skeleton skeleton--amount"></div>
          <div class="skeleton skeleton--detail"></div>
        </div>
      </div>

      <div v-else-if="salaryList.length === 0">
        <EmptyState
          icon="💰"
          title="暂无薪资记录"
          description="完成工作后薪资将自动结算到这里"
        />
      </div>

      <div
        v-for="item in salaryList"
        :key="item.id"
        class="salary-card"
        @click="goDetail(item.id)"
      >
        <div class="salary-card__header">
          <div class="salary-card__company">
            {{ item.companyName }} · {{ item.jobTitle }}
          </div>
          <span :class="['tag', getStatusClass(item.status)]">
            {{ item.statusText }}
          </span>
        </div>
        <div class="salary-card__hours">
          {{ item.hours }}小时 × {{ item.hourlyRate }}元/时
        </div>
        <div class="salary-card__detail">
          <span>应发 ¥{{ item.shouldPay.toFixed(2) }}</span>
          <span>个税 ¥{{ item.tax.toFixed(2) }}</span>
          <span class="salary-card__actual">实发 ¥{{ item.actualPay.toFixed(2) }}</span>
        </div>
        <div v-if="item.payTime" class="salary-card__time">
          {{ item.status === 'paid' ? '到账' : '' }}: {{ item.payTime }}
        </div>
        <div v-if="item.status === 'confirm'" class="salary-card__action">
          <button class="salary-card__btn">确认工时 ›</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.salary-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: 24px;
}

.salary-header {
  position: relative;
  padding: 20px 16px 28px;
  color: #fff;
  overflow: hidden;

  &__bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  }

  &__content {
    position: relative;
  }

  &__label {
    font-size: 13px;
    opacity: 0.9;
    margin-bottom: 8px;
  }

  &__amount {
    font-size: 14px;
    margin-bottom: 16px;
  }

  &__num {
    font-size: 36px;
    font-weight: 600;
    color: var(--color-accent);
    margin-left: 2px;
  }

  &__divider {
    height: 1px;
    background: rgba(255, 255, 255, 0.2);
    margin-bottom: 16px;
  }

  &__stats {
    display: flex;
  }

  &__stat {
    flex: 1;

    &-label {
      font-size: 12px;
      opacity: 0.8;
      margin-bottom: 4px;
    }

    &-value {
      font-size: 15px;
      font-weight: 500;
    }
  }
}

.month-filter {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  background: #fff;
  margin-bottom: 12px;
  box-shadow: var(--shadow-sm);

  &__arrow {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: var(--color-text-secondary);
    cursor: pointer;
    border-radius: 50%;
    transition: all var(--transition-fast);

    &:active {
      background: var(--color-bg-secondary);
    }
  }

  &__text {
    font-size: 15px;
    font-weight: 500;
    color: var(--color-text);
    margin: 0 16px;
  }
}

.salary-list {
  padding: 0 16px;
}

.salary-card {
  background: #fff;
  border-radius: var(--radius-base);
  padding: 14px;
  margin-bottom: 12px;
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

  &__company {
    flex: 1;
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text);
    margin-right: 12px;
  }

  &__hours {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-bottom: 10px;
  }

  &__detail {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
    color: var(--color-text-secondary);
    margin-bottom: 10px;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--color-border);
  }

  &__actual {
    color: var(--color-text);
    font-weight: 500;
    margin-left: auto;
  }

  &__time {
    font-size: 12px;
    color: var(--color-text-disabled);
  }

  &__action {
    text-align: right;
  }

  &__btn {
    font-size: 13px;
    color: var(--color-primary);
    background: transparent;
    padding: 0;
    cursor: pointer;
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
  margin-bottom: 8px;

  &--company {
    width: 70%;
    height: 15px;
  }

  &--amount {
    width: 40%;
    height: 13px;
  }

  &--detail {
    width: 90%;
    height: 12px;
  }
}

@keyframes skeletonShimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
