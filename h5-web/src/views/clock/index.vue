<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()

const jobInfo = ref({
  job_id: route.query.job_id || 'j001',
  job_title: '茶颜悦色门店店员',
  enterprise_name: '茶颜悦色(五一广场店)',
  work_address: '长沙市天心区黄兴南路步行商业街',
  work_start: '09:00',
  work_end: '18:00',
})

const currentTime = ref(new Date())
const location = ref({
  address: '长沙市天心区黄兴南路...',
  distance: 120,
  isInRange: true,
})

const clockStatus = ref<'before' | 'checked_in' | 'after'>('before')
const checkInTime = ref('')
const checkOutTime = ref('')
const isLocating = ref(false)
const isClocking = ref(false)
const showSuccess = ref(false)
const successType = ref<'checkin' | 'checkout'>('checkin')

const todayRecords = ref([
  {
    id: 1,
    type: 'checkin',
    typeText: '签到',
    time: '08:45',
    status: 'normal',
    statusText: '正常打卡',
  },
  {
    id: 2,
    type: 'checkout',
    typeText: '签退',
    time: '18:00',
    status: 'pending',
    statusText: '待签退',
  },
])

const historyRecords = ref([
  {
    date: '6月29日',
    records: [
      { type: 'checkin', time: '09:02', status: 'late', statusText: '迟到2分钟' },
      { type: 'checkout', time: '18:05', status: 'overtime', statusText: '加班5分钟' },
    ],
  },
  {
    date: '6月28日',
    records: [
      { type: 'checkin', time: '08:55', status: 'normal', statusText: '正常' },
      { type: 'checkout', time: '18:00', status: 'normal', statusText: '正常' },
    ],
  },
])

const currentMonth = ref('2026年6月')

const formattedTime = computed(() => {
  const hours = currentTime.value.getHours().toString().padStart(2, '0')
  const minutes = currentTime.value.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
})

const formattedDate = computed(() => {
  const year = currentTime.value.getFullYear()
  const month = (currentTime.value.getMonth() + 1).toString().padStart(2, '0')
  const day = currentTime.value.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
})

const clockButtonText = computed(() => {
  if (clockStatus.value === 'before') return '签到'
  if (clockStatus.value === 'checked_in') return '签退'
  return '已完成'
})

const clockButtonDisabled = computed(() => {
  return isLocating.value || isClocking.value || clockStatus.value === 'after'
})

let timer: number | null = null

function startTimer() {
  timer = window.setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
}

function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

async function getLocation() {
  isLocating.value = true
  setTimeout(() => {
    isLocating.value = false
    location.value = {
      address: '长沙市天心区黄兴南路步行商业街',
      distance: Math.floor(Math.random() * 200),
      isInRange: true,
    }
  }, 1000)
}

async function handleClock() {
  if (clockButtonDisabled.value) return

  if (!location.value.isInRange) {
    try {
      await showConfirmDialog({
        title: '距离较远',
        message: '当前位置距岗位地址较远，是否继续打卡？异常打卡需要填写说明。',
        confirmButtonText: '继续打卡',
        cancelButtonText: '取消',
      })
    } catch {
      return
    }
  }

  isClocking.value = true

  setTimeout(() => {
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`

    if (clockStatus.value === 'before') {
      checkInTime.value = timeStr
      clockStatus.value = 'checked_in'
      successType.value = 'checkin'
      todayRecords.value[0].time = timeStr
      todayRecords.value[0].status = 'normal'
      todayRecords.value[0].statusText = '正常打卡'
    } else if (clockStatus.value === 'checked_in') {
      checkOutTime.value = timeStr
      clockStatus.value = 'after'
      successType.value = 'checkout'
      todayRecords.value[1].time = timeStr
      todayRecords.value[1].status = 'normal'
      todayRecords.value[1].statusText = '正常签退'
    }

    isClocking.value = false
    showSuccess.value = true

    setTimeout(() => {
      showSuccess.value = false
    }, 2000)

    showToast({
      message: clockStatus.value === 'after' ? '签退成功' : '签到成功',
      type: 'success',
    })
  }, 1500)
}

function goBack() {
  router.back()
}

onMounted(() => {
  startTimer()
  getLocation()
})

onUnmounted(() => {
  stopTimer()
})
</script>

<template>
  <div class="clock-page">
    <NavBar title="GPS打卡" show-back @back="goBack" />

    <div class="job-info-card">
      <div class="job-title">{{ jobInfo.job_title }}</div>
      <div class="job-enterprise">{{ jobInfo.enterprise_name }}</div>
      <div class="job-detail">
        <span class="detail-item">
          <span class="detail-icon">📍</span>
          {{ jobInfo.work_address }}
        </span>
      </div>
      <div class="job-shift">
        <span class="detail-icon">⏰</span>
        今日班次：{{ jobInfo.work_start }} - {{ jobInfo.work_end }}
      </div>
    </div>

    <div class="clock-main">
      <div class="clock-info">
        <div class="current-location">
          <span class="location-icon">📍</span>
          <span class="location-text">{{ location.address }}</span>
          <span class="location-distance" :class="{ warning: !location.isInRange }">
            · 距岗{{ location.distance }}m
          </span>
        </div>
        <div class="current-time-row">
          <span class="time-label">当前时间：</span>
          <span class="time-value">{{ formattedDate }} {{ formattedTime }}</span>
        </div>
      </div>

      <div class="clock-button-wrapper">
        <div class="clock-ring" v-if="isLocating || isClocking">
          <div class="ring ring-1"></div>
          <div class="ring ring-2"></div>
          <div class="ring ring-3"></div>
        </div>

        <button
          class="clock-button"
          :class="{
            'is-before': clockStatus === 'before',
            'is-working': clockStatus === 'checked_in',
            'is-done': clockStatus === 'after',
            'is-loading': isLocating || isClocking,
          }"
          :disabled="clockButtonDisabled"
          @click="handleClock"
        >
          <span class="clock-text">{{ clockButtonText }}</span>
          <span class="clock-time">{{ formattedTime }}</span>
        </button>

        <div v-if="showSuccess" class="success-overlay">
          <div class="success-check">✓</div>
          <div class="success-text">{{ successType === 'checkin' ? '签到成功' : '签退成功' }}</div>
        </div>
      </div>

      <div v-if="isLocating" class="locating-tip">
        <span class="loading-dot"></span>
        定位中...
      </div>
    </div>

    <div class="records-section">
      <div class="section-header">
        <span class="section-title">今日打卡记录</span>
      </div>
      <div class="today-records">
        <div
          v-for="record in todayRecords"
          :key="record.id"
          class="record-item"
        >
          <div class="record-dot" :class="`dot-${record.status}`"></div>
          <div class="record-info">
            <span class="record-time">{{ record.time }}</span>
            <span class="record-type">{{ record.typeText }}</span>
          </div>
          <div class="record-status" :class="`status-${record.status}`">
            {{ record.statusText }}
          </div>
        </div>
      </div>
    </div>

    <div class="records-section">
      <div class="section-header">
        <span class="section-title">历史打卡</span>
        <div class="month-selector">
          {{ currentMonth }}
          <span class="arrow">▼</span>
        </div>
      </div>

      <div class="history-records">
        <div
          v-for="(day, dayIndex) in historyRecords"
          :key="dayIndex"
          class="history-day"
        >
          <div class="day-date">{{ day.date }}</div>
          <div class="day-records">
            <div
              v-for="(record, recIndex) in day.records"
              :key="recIndex"
              class="record-item"
            >
              <div class="record-dot" :class="`dot-${record.status}`"></div>
              <div class="record-info">
                <span class="record-time">{{ record.time }}</span>
                <span class="record-type">{{ record.type === 'checkin' ? '签到' : '签退' }}</span>
              </div>
              <div class="record-status" :class="`status-${record.status}`">
                {{ record.statusText }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.clock-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: var(--spacing-lg);
}

.job-info-card {
  background-color: var(--color-bg);
  padding: var(--spacing-base);
  margin: var(--spacing-sm);
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-sm);
}

.job-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 6px;
}

.job-enterprise {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-sm);
}

.job-detail {
  margin-bottom: 8px;
}

.detail-item {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.detail-icon {
  margin-right: 4px;
}

.job-shift {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.clock-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl) var(--spacing-base);
  background-color: var(--color-bg);
  margin: 0 var(--spacing-sm);
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-sm);
}

.clock-info {
  width: 100%;
  margin-bottom: var(--spacing-xl);
}

.current-location {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.location-icon {
  margin-right: 4px;
}

.location-text {
  font-size: var(--font-size-sm);
  color: var(--color-text);
}

.location-distance {
  font-size: var(--font-size-sm);
  color: var(--color-success);

  &.warning {
    color: var(--color-warning);
  }
}

.current-time-row {
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.time-label {
  margin-right: 4px;
}

.time-value {
  font-weight: 500;
  color: var(--color-text);
}

.clock-button-wrapper {
  position: relative;
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-lg);
}

.clock-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border: 2px solid var(--color-primary);
  border-radius: 50%;
  opacity: 0;
  animation: ringExpand 2s ease-out infinite;

  &.ring-1 {
    animation-delay: 0s;
  }
  &.ring-2 {
    animation-delay: 0.6s;
  }
  &.ring-3 {
    animation-delay: 1.2s;
  }
}

@keyframes ringExpand {
  0% {
    width: 120px;
    height: 120px;
    opacity: 0.6;
  }
  100% {
    width: 200px;
    height: 200px;
    opacity: 0;
  }
}

.clock-button {
  width: 140px;
  height: 140px;
  border: none;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;

  &.is-before {
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.3);
  }

  &.is-working {
    background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-light) 100%);
    box-shadow: 0 8px 24px rgba(255, 125, 0, 0.3);
  }

  &.is-done {
    background-color: var(--color-bg-tertiary);
    cursor: not-allowed;
  }

  &:active:not(:disabled) {
    transform: scale(0.95);
  }

  &.is-loading {
    opacity: 0.8;
  }
}

.clock-text {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.is-done .clock-text {
  color: var(--color-text-secondary);
}

.clock-time {
  font-size: var(--font-size-base);
  color: rgba(255, 255, 255, 0.9);
}

.is-done .clock-time {
  color: var(--color-text-disabled);
}

.success-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  animation: successPop 0.4s cubic-bezier(0.68, -0.55, 0.27, 1.55);
}

@keyframes successPop {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-check {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: var(--color-success);
  color: #fff;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  box-shadow: 0 4px 12px rgba(0, 180, 42, 0.4);
}

.success-text {
  font-size: var(--font-size-base);
  color: var(--color-success);
  font-weight: 500;
  background-color: var(--color-success-bg);
  padding: 4px 12px;
  border-radius: 12px;
}

.locating-tip {
  display: flex;
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.loading-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--color-primary);
  margin-right: 6px;
  animation: dotPulse 1s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.records-section {
  background-color: var(--color-bg);
  margin: var(--spacing-sm);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
  box-shadow: var(--shadow-sm);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
}

.month-selector {
  display: flex;
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  cursor: pointer;

  .arrow {
    font-size: 10px;
    margin-left: 4px;
  }
}

.today-records,
.day-records {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.record-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) 0;
}

.record-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: var(--spacing-sm);
  flex-shrink: 0;

  &.dot-normal {
    background-color: var(--color-success);
  }
  &.dot-late,
  &.dot-early {
    background-color: var(--color-warning);
  }
  &.dot-overtime {
    background-color: var(--color-success);
  }
  &.dot-pending {
    background-color: var(--color-text-disabled);
  }
}

.record-info {
  flex: 1;
  display: flex;
  align-items: baseline;
  gap: var(--spacing-xs);
}

.record-time {
  font-size: var(--font-size-base);
  color: var(--color-text);
  font-weight: 500;
}

.record-type {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.record-status {
  font-size: var(--font-size-sm);

  &.status-normal,
  &.status-overtime {
    color: var(--color-success);
  }
  &.status-late,
  &.status-early {
    color: var(--color-warning);
  }
  &.status-pending {
    color: var(--color-text-disabled);
  }
}

.history-day {
  margin-bottom: var(--spacing-base);

  &:last-child {
    margin-bottom: 0;
  }
}

.day-date {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
  font-weight: 500;
}
</style>
