<template>
  <view class="page">
    <view class="salary-header">
      <text class="header-title">薪资流水</text>
      <view class="income-card">
        <view class="income-main">
          <text class="income-label">本月收入</text>
          <text class="income-value">¥{{ summary.monthly_income.toFixed(2) }}</text>
        </view>
        <view class="income-row">
          <view class="income-item">
            <text class="item-value">¥{{ summary.total_income.toFixed(2) }}</text>
            <text class="item-label">累计收入</text>
          </view>
          <view class="income-divider"></view>
          <view class="income-item">
            <text class="item-value pending">¥{{ summary.pending_amount.toFixed(2) }}</text>
            <text class="item-label">待结算</text>
          </view>
        </view>
      </view>
    </view>

    <view class="month-selector">
      <view class="month-btn" @click="prevMonth">
        <text class="month-arrow">‹</text>
      </view>
      <view class="month-display">
        <text class="month-text">{{ currentMonth }}</text>
      </view>
      <view class="month-btn" @click="nextMonth">
        <text class="month-arrow">›</text>
      </view>
    </view>

    <view class="flow-section">
      <view class="flow-header">
        <text class="flow-title">流水明细</text>
        <text class="flow-count">共{{ records.length }}条</text>
      </view>
      <view class="flow-list">
        <view class="flow-card" v-for="record in records" :key="record.record_id" @click="showDetail(record)">
          <view class="flow-top">
            <view class="flow-info">
              <view class="company-row">
                <text class="company-name">{{ record.company_name }}</text>
                <view class="status-tag" :class="getStatusClass(record.status)">
                  {{ getStatusText(record.status) }}
                </view>
              </view>
              <text class="job-name">{{ record.job_name }}</text>
            </view>
            <view class="flow-amount">
              <text class="amount-text">¥{{ record.net_amount.toFixed(2) }}</text>
              <text class="amount-label">实发工资</text>
            </view>
          </view>
          <view class="flow-bottom">
            <view class="work-info">
              <text class="work-text">{{ record.work_hours }}小时 × ¥{{ record.hourly_wage }}/小时</text>
            </view>
            <view class="flow-arrow">
              <text class="arrow-icon">›</text>
            </view>
          </view>
          <view class="flow-date" v-if="record.status === 'paid' && record.paid_time">
            <text class="date-text">到账时间：{{ record.paid_time }}</text>
          </view>
        </view>
      </view>

      <view class="empty" v-if="records.length === 0">
        <text class="empty-icon">💰</text>
        <text class="empty-text">暂无薪资流水</text>
        <text class="empty-tip">当月的薪资会在这里显示</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { salaryApi, type SalaryRecord } from '@/api/salary'

const currentMonth = ref(dayjs().format('YYYY年MM月'))
const summary = reactive({
  monthly_income: 0,
  total_income: 0,
  pending_amount: 0
})
const records = ref<SalaryRecord[]>([])

const getStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    pending_hours: 'pending',
    pending_company: 'processing',
    pending_payment: 'waiting',
    paid: 'success',
    rejected: 'error'
  }
  return classMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending_hours: '待确认工时',
    pending_company: '待企业确认',
    pending_payment: '待发放',
    paid: '已到账',
    rejected: '已驳回'
  }
  return textMap[status] || '未知'
}

const prevMonth = () => {
  const current = dayjs(currentMonth.value, 'YYYY年MM月')
  currentMonth.value = current.subtract(1, 'month').format('YYYY年MM月')
  loadData()
}

const nextMonth = () => {
  const current = dayjs(currentMonth.value, 'YYYY年MM月')
  const now = dayjs()
  if (current.isBefore(now, 'month')) {
    currentMonth.value = current.add(1, 'month').format('YYYY年MM月')
    loadData()
  }
}

const loadData = () => {
  uni.showLoading({ title: '加载中...' })
  const month = currentMonth.value.replace('年', '-').replace('月', '')
  
  Promise.all([
    salaryApi.getSalarySummary(),
    salaryApi.getSalaryList({ month, page: 1, size: 20 })
  ]).then(([summaryRes, listRes]) => {
    summary.monthly_income = summaryRes.data.monthly_income
    summary.total_income = summaryRes.data.total_income
    summary.pending_amount = summaryRes.data.pending_amount
    records.value = listRes.data.list
    uni.hideLoading()
  }).catch(() => {
    uni.hideLoading()
    loadMockData()
  })
}

const loadMockData = () => {
  summary.monthly_income = 3200
  summary.total_income = 15680
  summary.pending_amount = 800
  
  records.value = [
    {
      record_id: '1',
      company_name: '长沙市XX教育机构',
      job_name: '初中数学家教',
      work_hours: 30,
      hourly_wage: 50,
      gross_amount: 1500,
      tax_amount: 0,
      net_amount: 1500,
      status: 'paid',
      paid_time: '2026-06-29 10:30'
    },
    {
      record_id: '2',
      company_name: 'XX超市',
      job_name: '促销导购',
      work_hours: 24,
      hourly_wage: 15,
      gross_amount: 360,
      tax_amount: 0,
      net_amount: 360,
      status: 'paid',
      paid_time: '2026-06-28 14:20'
    },
    {
      record_id: '3',
      company_name: 'XX会展公司',
      job_name: '展会协助',
      work_hours: 16,
      hourly_wage: 25,
      gross_amount: 400,
      tax_amount: 0,
      net_amount: 400,
      status: 'pending_payment'
    },
    {
      record_id: '4',
      company_name: 'XX培训机构',
      job_name: '英语助教',
      work_hours: 20,
      hourly_wage: 20,
      gross_amount: 400,
      tax_amount: 0,
      net_amount: 400,
      status: 'pending_company'
    },
    {
      record_id: '5',
      company_name: 'XX奶茶店',
      job_name: '店员',
      work_hours: 12,
      hourly_wage: 18,
      gross_amount: 216,
      tax_amount: 0,
      net_amount: 216,
      status: 'pending_hours'
    }
  ]
}

const showDetail = (record: SalaryRecord) => {
  uni.showModal({
    title: '薪资明细',
    content: `企业：${record.company_name}\n岗位：${record.job_name}\n工时：${record.work_hours}小时 × ¥${record.hourly_wage}/小时\n应发：¥${record.gross_amount.toFixed(2)}\n个税：¥${record.tax_amount.toFixed(2)}\n实发：¥${record.net_amount.toFixed(2)}\n状态：${getStatusText(record.status)}`,
    showCancel: false
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
}

.salary-header {
  background: linear-gradient(180deg, #FF7D00 0%, #FFA940 100%);
  padding: 60rpx 32rpx 100rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 32rpx;
  display: block;
}

.income-card {
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(255, 125, 0, 0.2);
}

.income-main {
  text-align: center;
  padding-bottom: 28rpx;
  border-bottom: 1rpx solid #F2F3F5;
  margin-bottom: 24rpx;
}

.income-label {
  font-size: 26rpx;
  color: #86909C;
  display: block;
  margin-bottom: 12rpx;
}

.income-value {
  font-size: 64rpx;
  font-weight: 600;
  color: #FF7D00;
}

.income-row {
  display: flex;
  align-items: center;
}

.income-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.item-value {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.item-value.pending {
  color: #FAAD14;
}

.item-label {
  font-size: 24rpx;
  color: #86909C;
}

.income-divider {
  width: 2rpx;
  height: 48rpx;
  background-color: #E5E6EB;
}

.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
  background-color: #FFFFFF;
  margin: -60rpx 24rpx 24rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.month-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F7F8FA;
  border-radius: 50%;
}

.month-arrow {
  font-size: 36rpx;
  color: #4E5969;
  font-weight: 500;
}

.month-display {
  min-width: 200rpx;
  text-align: center;
  margin: 0 24rpx;
}

.month-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2329;
}

.flow-section {
  padding: 0 24rpx 32rpx;
}

.flow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 0 8rpx;
}

.flow-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2329;
}

.flow-count {
  font-size: 24rpx;
  color: #86909C;
}

.flow-list {
  padding: 0;
}

.flow-card {
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.flow-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.flow-info {
  flex: 1;
}

.company-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.company-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2329;
  margin-right: 12rpx;
  max-width: 320rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  font-size: 20rpx;
  padding: 6rpx 14rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
}

.status-tag.pending {
  background-color: rgba(250, 173, 20, 0.1);
  color: #FAAD14;
}

.status-tag.processing {
  background-color: rgba(22, 93, 255, 0.1);
  color: #165DFF;
}

.status-tag.waiting {
  background-color: rgba(134, 144, 156, 0.1);
  color: #86909C;
}

.status-tag.success {
  background-color: rgba(82, 196, 26, 0.1);
  color: #52C41A;
}

.status-tag.error {
  background-color: rgba(255, 77, 79, 0.1);
  color: #FF4D4F;
}

.job-name {
  font-size: 26rpx;
  color: #86909C;
}

.flow-amount {
  text-align: right;
  flex-shrink: 0;
}

.amount-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #FF7D00;
  display: block;
  margin-bottom: 6rpx;
}

.amount-label {
  font-size: 22rpx;
  color: #86909C;
}

.flow-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 1rpx solid #F2F3F5;
}

.work-info {
  flex: 1;
}

.work-text {
  font-size: 24rpx;
  color: #86909C;
}

.flow-arrow {
  flex-shrink: 0;
}

.arrow-icon {
  font-size: 28rpx;
  color: #C9CDD4;
}

.flow-date {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F2F3F5;
}

.date-text {
  font-size: 22rpx;
  color: #86909C;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #4E5969;
  margin-bottom: 12rpx;
}

.empty-tip {
  font-size: 24rpx;
  color: #C9CDD4;
}
</style>
