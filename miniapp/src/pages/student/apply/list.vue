<template>
  <view class="page">
    <view class="tabs">
      <view class="tab-item" :class="{ active: activeTab === 'all' }" @click="handleTabChange('all')">
        <text class="tab-text">全部</text>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'applied' }" @click="handleTabChange('applied')">
        <text class="tab-text">已投递</text>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'interviewing' }" @click="handleTabChange('interviewing')">
        <text class="tab-text">待面试</text>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'accepted' }" @click="handleTabChange('accepted')">
        <text class="tab-text">已录用</text>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'rejected' }" @click="handleTabChange('rejected')">
        <text class="tab-text">已拒绝</text>
      </view>
    </view>

    <scroll-view
      class="apply-scroll"
      scroll-y
      :show-scrollbar="false"
      @refresherrefresh="onPullDownRefresh"
      :refresher-enabled="true"
      @scrolltolower="onReachBottom"
    >
      <view class="apply-list">
        <view class="apply-card" v-for="apply in applyList" :key="apply.id" @click="handleCardClick(apply)">
          <view class="apply-header">
            <view class="job-info">
              <text class="job-title">{{ apply.job_name }}</text>
              <text class="job-company">{{ apply.company_name }}</text>
            </view>
            <view class="status-tag" :class="apply.status">{{ apply.status_text }}</view>
          </view>
          <view class="apply-body">
            <view class="apply-detail">
              <text class="detail-label">投递时间</text>
              <text class="detail-value">{{ apply.apply_time }}</text>
            </view>
            <view class="apply-detail">
              <text class="detail-label">工作地点</text>
              <text class="detail-value">{{ apply.address }}</text>
            </view>
          </view>
          <view class="apply-footer">
            <text class="salary">¥{{ apply.salary }}/{{ getSalaryUnit(apply.salary_type) }}</text>
            <view class="actions">
              <button class="action-btn cancel" v-if="apply.status === 'applied'" @click.stop="handleCancel(apply)">取消投递</button>
              <button class="action-btn detail" @click.stop="handleViewDetail(apply)">查看详情</button>
            </view>
          </view>

          <view v-if="apply.interview" class="interview-card">
            <view class="interview-header">
              <text class="interview-icon">📅</text>
              <text class="interview-title">面试邀请</text>
            </view>
            <view class="interview-info">
              <view class="interview-item">
                <text class="interview-label">面试时间</text>
                <text class="interview-value">{{ apply.interview.interview_time }}</text>
              </view>
              <view class="interview-item">
                <text class="interview-label">面试方式</text>
                <text class="interview-value">{{ apply.interview.interview_method === 'video' ? '视频面试' : '线下面试' }}</text>
              </view>
              <view v-if="apply.interview.address" class="interview-item">
                <text class="interview-label">面试地点</text>
                <text class="interview-value">{{ apply.interview.address }}</text>
              </view>
            </view>
            <button
              class="interview-btn"
              :class="{ disabled: !canEnterInterview(apply.interview.interview_time) }"
              :disabled="!canEnterInterview(apply.interview.interview_time)"
              @click.stop="handleEnterInterview(apply)"
            >
              {{ canEnterInterview(apply.interview.interview_time) ? '进入面试' : `${getMinutesToInterview(apply.interview.interview_time)}分钟后可进入` }}
            </button>
          </view>
        </view>

        <view v-if="loading" class="loading-more">
          <text class="loading-text">加载中...</text>
        </view>

        <view v-if="!loading && !hasMore && applyList.length > 0" class="no-more">
          <text class="no-more-text">已加载全部</text>
        </view>
      </view>
    </scroll-view>

    <view class="empty" v-if="!loading && applyList.length === 0">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无投递记录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { applyApi, type ApplyRecord, type ApplyStatus } from '@/api/apply'

const activeTab = ref<ApplyStatus | 'all'>('all')
const applyList = ref<ApplyRecord[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)

const hasMore = ref(true)

const getSalaryUnit = (type: string) => {
  const map: Record<string, string> = {
    hourly: '时',
    daily: '天',
    monthly: '月'
  }
  return map[type] || ''
}

const canEnterInterview = (interviewTime: string) => {
  const now = new Date().getTime()
  const target = new Date(interviewTime).getTime()
  const diffMinutes = (target - now) / (1000 * 60)
  return diffMinutes <= 15
}

const getMinutesToInterview = (interviewTime: string) => {
  const now = new Date().getTime()
  const target = new Date(interviewTime).getTime()
  const diffMinutes = Math.max(0, Math.ceil((target - now) / (1000 * 60)))
  return diffMinutes
}

const handleTabChange = (tab: ApplyStatus | 'all') => {
  if (activeTab.value === tab) return
  activeTab.value = tab
  page.value = 1
  applyList.value = []
  hasMore.value = true
  fetchApplyList()
}

const handleCardClick = (apply: ApplyRecord) => {
  uni.navigateTo({ url: `/pages/student/job-detail/index?job_id=${apply.job_id}` })
}

const handleCancel = (apply: ApplyRecord) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消投递吗？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' })
        try {
          await applyApi.cancelApply(apply.id)
          uni.hideLoading()
          applyList.value = applyList.value.filter(a => a.id !== apply.id)
          uni.showToast({ title: '已取消', icon: 'success' })
        } catch (error) {
          uni.hideLoading()
        }
      }
    }
  })
}

const handleViewDetail = (apply: ApplyRecord) => {
  uni.navigateTo({ url: `/pages/student/job-detail/index?job_id=${apply.job_id}` })
}

const handleEnterInterview = (apply: ApplyRecord) => {
  uni.showModal({
    title: '进入面试',
    content: `即将进入面试房间，面试时间：${apply.interview?.interview_time}`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '进入房间...' })
        try {
          await applyApi.enterInterview(apply.id)
          uni.hideLoading()
          uni.showToast({ title: '已进入面试房间', icon: 'success' })
        } catch (error) {
          uni.hideLoading()
        }
      }
    }
  })
}

const fetchApplyList = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const params = {
      status: activeTab.value === 'all' ? undefined : activeTab.value,
      page: page.value,
      size: size.value
    }
    const res = await applyApi.getApplyList(params)
    if (page.value === 1) {
      applyList.value = res.data.list
      total.value = res.data.total
    } else {
      applyList.value = [...applyList.value, ...res.data.list]
    }
    hasMore.value = applyList.value.length < total.value
    page.value++
  } catch (error) {
    console.error('获取投递记录失败:', error)
    if (page.value === 1) {
      applyList.value = [
        { id: '1', job_id: '1', job_name: '初中数学家教', company_name: '长沙学思教育', salary: 150, salary_type: 'hourly', status: 'applied', status_text: '已投递', apply_time: '2026-06-29 10:30', address: '岳麓区麓山南路' },
        { id: '2', job_id: '2', job_name: '超市促销', company_name: '步步高超市', salary: 18, salary_type: 'hourly', status: 'interviewing', status_text: '待面试', apply_time: '2026-06-28 14:20', address: '天心区黄兴广场', interview: { interview_time: new Date(Date.now() + 10 * 60 * 1000).toISOString().replace('T', ' ').slice(0, 16), interview_method: 'video', room_id: '12345' } },
        { id: '3', job_id: '3', job_name: '展会协助', company_name: '长沙会展中心', salary: 200, salary_type: 'daily', status: 'accepted', status_text: '已录用', apply_time: '2026-06-27 09:15', address: '长沙县国展路' },
        { id: '4', job_id: '4', job_name: '新媒体运营', company_name: '芒果传媒', salary: 3500, salary_type: 'monthly', status: 'rejected', status_text: '已拒绝', apply_time: '2026-06-26 16:45', address: '雨花区韶山南路' },
        { id: '5', job_id: '5', job_name: '奶茶店店员', company_name: '茶颜悦色', salary: 18, salary_type: 'hourly', status: 'applied', status_text: '已投递', apply_time: '2026-06-25 11:00', address: '岳麓区麓山南路' },
        { id: '6', job_id: '6', job_name: '咖啡店店员', company_name: '星巴克', salary: 20, salary_type: 'hourly', status: 'interviewing', status_text: '待面试', apply_time: '2026-06-24 15:30', address: '芙蓉区五一广场', interview: { interview_time: new Date(Date.now() + 30 * 60 * 1000).toISOString().replace('T', ' ').slice(0, 16), interview_method: 'offline', address: '长沙市芙蓉区五一广场星巴克3楼' } }
      ]
      total.value = 6
    }
  } finally {
    loading.value = false
  }
}

const onPullDownRefresh = () => {
  page.value = 1
  applyList.value = []
  hasMore.value = true
  fetchApplyList().then(() => {
    uni.stopPullDownRefresh()
  })
}

const onReachBottom = () => {
  if (loading.value || !hasMore.value) return
  fetchApplyList()
}

onMounted(() => {
  fetchApplyList()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
}

.tabs {
  display: flex;
  background-color: #FFFFFF;
  padding: 0 16rpx;
  position: sticky;
  top: 0;
  z-index: 100;
  overflow-x: auto;
  white-space: nowrap;
}

.tab-item {
  flex: 1;
  padding: 32rpx 16rpx;
  text-align: center;
  position: relative;
}

.tab-item.active {
  .tab-text {
    color: #165DFF;
    font-weight: bold;
  }
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 48rpx;
    height: 6rpx;
    background-color: #165DFF;
    border-radius: 3rpx;
  }
}

.tab-text {
  font-size: 26rpx;
  color: #86909C;
}

.apply-scroll {
  height: calc(100vh - 100rpx);
}

.apply-list {
  padding: 24rpx;
}

.apply-card {
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.apply-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.job-info {
  flex: 1;
}

.job-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1F2329;
  display: block;
  margin-bottom: 8rpx;
}

.job-company {
  font-size: 26rpx;
  color: #86909C;
}

.status-tag {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.status-tag.applied {
  color: #165DFF;
  background-color: #E8F0FF;
}

.status-tag.interviewing {
  color: #FAAD14;
  background-color: #FFFBE6;
}

.status-tag.accepted {
  color: #52C41A;
  background-color: #F6FFED;
}

.status-tag.rejected {
  color: #FF4D4F;
  background-color: #FFF2F0;
}

.apply-body {
  background-color: #F8F9FA;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.apply-detail {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.apply-detail:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 26rpx;
  color: #86909C;
}

.detail-value {
  font-size: 26rpx;
  color: #1F2329;
}

.apply-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.salary {
  font-size: 30rpx;
  font-weight: bold;
  color: #FF7D00;
}

.actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  font-size: 26rpx;
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  border: none;
}

.action-btn.cancel {
  background-color: #F2F3F5;
  color: #86909C;
}

.action-btn.detail {
  background-color: #165DFF;
  color: #FFFFFF;
}

.interview-card {
  margin-top: 20rpx;
  padding: 20rpx;
  background-color: #FFF7E6;
  border-radius: 12rpx;
  border: 1rpx solid #FFE58F;
}

.interview-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.interview-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.interview-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #FAAD14;
}

.interview-info {
  margin-bottom: 16rpx;
}

.interview-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.interview-item:last-child {
  margin-bottom: 0;
}

.interview-label {
  font-size: 26rpx;
  color: #86909C;
}

.interview-value {
  font-size: 26rpx;
  color: #1F2329;
}

.interview-btn {
  width: 100%;
  font-size: 28rpx;
  color: #FFFFFF;
  background-color: #FAAD14;
  padding: 20rpx;
  border-radius: 32rpx;
  border: none;
}

.interview-btn.disabled {
  background-color: #C9CDD4;
}

.loading-more {
  padding: 32rpx 0;
  text-align: center;
}

.loading-text {
  font-size: 26rpx;
  color: #86909C;
}

.no-more {
  padding: 24rpx 0;
  text-align: center;
}

.no-more-text {
  font-size: 24rpx;
  color: #BBBFc4;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #86909C;
}
</style>