<template>
  <view class="page">
    <view v-if="loading" class="loading-mask">
      <view class="loading-content">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
    </view>

    <view v-else class="content">
      <view class="header-section">
        <view class="header-bg"></view>
        <view class="header-content">
          <view class="job-title-row">
            <text class="job-name">{{ jobData.job_name }}</text>
          </view>
          <view class="job-salary-row">
            <text class="job-salary">¥{{ jobData.salary }}</text>
            <text class="salary-unit">/{{ getSalaryUnit(jobData.salary_type) }}</text>
          </view>

          <view class="company-row">
            <text class="company-name">{{ jobData.company_name }}</text>
            <view v-if="jobData.is_certified" class="certified-tag">
              <text class="certified-icon">✓</text>
              <text class="certified-text">认证企业</text>
            </view>
            <view class="credit-badge">
              <text class="credit-icon">⭐</text>
              <text class="credit-text">{{ jobData.credit_score }}分</text>
            </view>
          </view>

          <view class="info-row">
            <view class="info-item">
              <text class="info-icon">📍</text>
              <text class="info-text">{{ jobData.address }}</text>
            </view>
          </view>

          <view class="info-row">
            <view class="info-item">
              <text class="info-icon">⏰</text>
              <text class="info-text">{{ formatWorkTime(jobData.work_time) }}</text>
            </view>
          </view>

          <view class="info-row">
            <view class="info-item">
              <text class="info-icon">👥</text>
              <text class="info-text">招聘{{ jobData.total_count }}人，已招{{ jobData.hired_count }}人</text>
            </view>
          </view>

          <view class="tag-row">
            <view class="settlement-tag" :class="jobData.settlement_type">
              {{ getSettlementText(jobData.settlement_type) }}
            </view>
            <view v-if="jobData.has_insurance" class="insurance-tag">
              <text class="insurance-tag-icon">🛡️</text>
              <text class="insurance-tag-text">含意外险</text>
            </view>
            <view class="skill-tag" v-for="skill in jobData.skill_tags.slice(0, 3)" :key="skill">
              {{ skill }}
            </view>
          </view>
        </view>
      </view>

      <view class="card-section">
        <view class="section-header">
          <text class="section-title">岗位描述</text>
        </view>
        <view class="description-content" :class="{ expanded: descExpanded }">
          <rich-text :nodes="jobData.description"></rich-text>
        </view>
        <view class="expand-wrap" @click="toggleDescription">
          <text class="expand-btn">{{ descExpanded ? '收起' : '展开' }}</text>
          <text class="expand-arrow">{{ descExpanded ? '↑' : '↓' }}</text>
        </view>
      </view>

      <view class="card-section company-card" @click="handleCompanyClick">
        <view class="company-logo">
          <text class="logo-icon">🏢</text>
        </view>
        <view class="company-info">
          <view class="company-name-row">
            <text class="company-name-text">{{ jobData.company_name }}</text>
            <view v-if="jobData.is_certified" class="certified-badge">
              <text class="badge-icon">✓</text>
              <text class="badge-text">认证</text>
            </view>
          </view>
          <text class="company-desc">信用评分 {{ jobData.credit_score }} 分</text>
        </view>
        <text class="company-arrow">›</text>
      </view>

      <view class="card-section safety-section">
        <view class="section-header">
          <text class="section-title">安全保障</text>
        </view>
        <view class="safety-list">
          <view class="safety-item">
            <view class="safety-icon-wrap blue">
              <text class="safety-icon">💰</text>
            </view>
            <view class="safety-item-info">
              <text class="safety-item-title">薪资托管</text>
              <text class="safety-item-desc">薪资由平台担保发放</text>
            </view>
          </view>
          <view class="safety-item">
            <view class="safety-icon-wrap green" :class="{ gray: !jobData.has_insurance }">
              <text class="safety-icon">🛡️</text>
            </view>
            <view class="safety-item-info">
              <text class="safety-item-title">兼职意外险</text>
              <text class="safety-item-desc">{{ jobData.has_insurance ? '已购买意外险' : '建议企业购买' }}</text>
            </view>
          </view>
          <view class="safety-item">
            <view class="safety-icon-wrap orange">
              <text class="safety-icon">✅</text>
            </view>
            <view class="safety-item-info">
              <text class="safety-item-title">零押金</text>
              <text class="safety-item-desc">平台严禁收取任何费用</text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="jobData.similar_jobs && jobData.similar_jobs.length > 0" class="card-section">
        <view class="section-header">
          <text class="section-title">相似岗位</text>
        </view>
        <scroll-view class="similar-scroll" scroll-x :show-scrollbar="false">
          <view class="similar-list">
            <view
              class="similar-card"
              v-for="item in jobData.similar_jobs.slice(0, 5)"
              :key="item.job_id"
              @click="handleSimilarJobClick(item)"
            >
              <text class="similar-name">{{ item.job_name }}</text>
              <text class="similar-salary">¥{{ item.salary }}/{{ getSalaryUnit(item.salary_type) }}</text>
              <text class="similar-company">{{ item.company_name }}</text>
              <view class="similar-bottom">
                <view class="settlement-tag small" :class="item.settlement_type">
                  {{ getSettlementText(item.settlement_type) }}
                </view>
                <text class="similar-distance">{{ item.distance }}km</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <view class="bottom-bar">
      <view class="bottom-left">
        <view class="favorite-btn" :class="{ active: isFavorite }" @click="handleFavorite">
          <text class="favorite-icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
          <text class="favorite-text">{{ isFavorite ? '已收藏' : '收藏' }}</text>
        </view>
      </view>
      <view class="bottom-right">
        <button
          class="apply-btn"
          :class="applyBtnClass"
          @click="handleApply"
          :disabled="applyBtnDisabled"
        >
          <text>{{ applyBtnText }}</text>
        </button>
      </view>
    </view>

    <view v-if="showApplyAnimation" class="apply-animation">
      <view class="resume-fly">
        <text class="resume-icon">📄</text>
      </view>
      <text class="animation-text">投递成功！</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { jobApi, type JobDetail } from '@/api/job'

const userStore = useUserStore()

const loading = ref(true)
const jobData = ref<JobDetail>({
  job_id: '',
  job_name: '',
  cover_image: '',
  salary: 0,
  salary_type: 'hourly',
  company_name: '',
  is_certified: false,
  credit_score: 0,
  address: '',
  longitude: 0,
  latitude: 0,
  work_time: { weekday: [], time_range: [] },
  settlement_type: 'daily',
  hired_count: 0,
  total_count: 0,
  skill_tags: [],
  description: '',
  has_insurance: false,
  apply_status: 'none',
  similar_jobs: []
})

const descExpanded = ref(false)
const isFavorite = ref(false)
const showApplyAnimation = ref(false)

const applyBtnText = computed(() => {
  const status = jobData.value.apply_status
  if (status === 'applied') return '已投递'
  if (status === 'hired') return '查看进度'
  return '立即投递'
})

const applyBtnClass = computed(() => {
  const status = jobData.value.apply_status
  if (status === 'applied') return 'disabled'
  if (status === 'hired') return 'progress'
  return ''
})

const applyBtnDisabled = computed(() => {
  return jobData.value.apply_status === 'applied'
})

const getSalaryUnit = (type: string) => {
  const map: Record<string, string> = {
    hourly: '时',
    daily: '天',
    monthly: '月'
  }
  return map[type] || ''
}

const getSettlementText = (type: string) => {
  const map: Record<string, string> = {
    daily: '日结',
    weekly: '周结',
    monthly: '月结'
  }
  return map[type] || ''
}

const formatWorkTime = (workTime: { weekday: string[]; time_range: string[] }) => {
  const weekdays = workTime.weekday.join('、')
  const times = workTime.time_range.join(' ')
  return `${weekdays} ${times}`
}

const toggleDescription = () => {
  descExpanded.value = !descExpanded.value
}

const handleCompanyClick = () => {
  uni.showToast({ title: '企业主页开发中', icon: 'none' })
}

const handleMapNavigation = () => {
  if (jobData.value.latitude && jobData.value.longitude) {
    uni.openLocation({
      latitude: jobData.value.latitude,
      longitude: jobData.value.longitude,
      name: jobData.value.address,
      address: jobData.value.address
    })
  }
}

const handleSimilarJobClick = (item: { job_id: string }) => {
  uni.redirectTo({ url: `/pages/student/job-detail/index?job_id=${item.job_id}` })
}

const handleFavorite = () => {
  isFavorite.value = !isFavorite.value
  const favorites = uni.getStorageSync('favorites') || []
  if (isFavorite.value) {
    if (!favorites.includes(jobData.value.job_id)) {
      favorites.push(jobData.value.job_id)
    }
    uni.showToast({ title: '已收藏', icon: 'success' })
  } else {
    const index = favorites.indexOf(jobData.value.job_id)
    if (index > -1) {
      favorites.splice(index, 1)
    }
    uni.showToast({ title: '已取消收藏', icon: 'none' })
  }
  uni.setStorageSync('favorites', favorites)
}

const handleApply = () => {
  if (jobData.value.apply_status === 'hired') {
    uni.navigateTo({ url: '/pages/student/apply/list' })
    return
  }

  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/student/login/index' })
    return
  }

  if (userStore.user?.verify_status !== 2) {
    uni.showModal({
      title: '提示',
      content: '您还未完成实名认证，是否前往认证？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/student/resume/edit' })
        }
      }
    })
    return
  }

  uni.showModal({
    title: '确认投递',
    content: '已知晓岗位要求，确认岗位无押金/培训费等收费项目',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '投递中...' })
        try {
          await jobApi.applyJob(jobData.value.job_id)
          uni.hideLoading()
          jobData.value.apply_status = 'applied'
          showApplyAnimation.value = true
          setTimeout(() => {
            showApplyAnimation.value = false
          }, 2000)
        } catch (error) {
          uni.hideLoading()
        }
      }
    }
  })
}

const fetchJobDetail = async (jobId: string) => {
  try {
    const res = await jobApi.getJobDetail(jobId)
    jobData.value = res.data
    const favorites = uni.getStorageSync('favorites') || []
    isFavorite.value = favorites.includes(jobId)
  } catch (error) {
    console.error('获取岗位详情失败:', error)
    jobData.value = {
      job_id: jobId,
      job_name: '奶茶店店员',
      cover_image: '',
      salary: 18,
      salary_type: 'hourly',
      company_name: '茶颜悦色',
      is_certified: true,
      credit_score: 98,
      address: '长沙市岳麓区麓山南路88号',
      longitude: 112.9388,
      latitude: 28.2280,
      work_time: { weekday: ['周一', '周二', '周三', '周四', '周五'], time_range: ['09:00-18:00'] },
      settlement_type: 'daily',
      hired_count: 3,
      total_count: 10,
      skill_tags: ['服务', '沟通', '团队协作'],
      description: '<p>负责奶茶店日常运营工作，包括点单、制作饮品、清洁卫生等。要求有良好的服务态度，能够快速学习新品制作。</p><p>工作环境舒适，团队氛围好，提供免费饮品。</p>',
      has_insurance: true,
      apply_status: 'none',
      similar_jobs: [
        { job_id: '2', job_name: '咖啡店店员', company_name: '星巴克', is_certified: true, salary: 20, salary_type: 'hourly', settlement_type: 'daily', address: '天心区黄兴广场', distance: 2.1, skill_tags: ['咖啡', '服务'], has_insurance: true, industry_tag: '茶饮' },
        { job_id: '3', job_name: '甜品店导购', company_name: '哈根达斯', is_certified: true, salary: 22, salary_type: 'hourly', settlement_type: 'daily', address: '芙蓉区五一广场', distance: 2.5, skill_tags: ['销售', '服务'], has_insurance: true, industry_tag: '零售' }
      ]
    }
    const favorites = uni.getStorageSync('favorites') || []
    isFavorite.value = favorites.includes(jobId)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const options = currentPage.$page?.options || {}
  const jobId = options.job_id || '1'
  fetchJobDetail(jobId)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #e8f0ff;
  border-top-color: #165dff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 28rpx;
  color: #86909c;
}

.header-section {
  position: relative;
  padding-bottom: 32rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 400rpx;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 50%, #69b1ff 100%);
  border-radius: 0 0 48rpx 48rpx;
}

.header-content {
  position: relative;
  z-index: 1;
  padding: 60rpx 32rpx 0;
}

.job-title-row {
  margin-bottom: 16rpx;
}

.job-name {
  font-size: 42rpx;
  font-weight: bold;
  color: #ffffff;
  line-height: 1.3;
}

.job-salary-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 32rpx;
}

.job-salary {
  font-size: 64rpx;
  font-weight: bold;
  color: #ffd666;
  line-height: 1;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.salary-unit {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-left: 8rpx;
}

.company-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.company-name {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 500;
}

.certified-tag {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.25);
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
}

.certified-icon {
  font-size: 18rpx;
  color: #ffffff;
  margin-right: 6rpx;
  font-weight: bold;
}

.certified-text {
  font-size: 20rpx;
  color: #ffffff;
}

.credit-badge {
  display: flex;
  align-items: center;
  background: rgba(255, 214, 102, 0.3);
  padding: 6rpx 14rpx;
  border-radius: 20rpx;
}

.credit-icon {
  font-size: 20rpx;
  margin-right: 6rpx;
}

.credit-text {
  font-size: 22rpx;
  color: #ffd666;
  font-weight: 500;
}

.info-row {
  margin-bottom: 16rpx;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.info-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  flex: 1;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 24rpx;
}

.settlement-tag {
  font-size: 22rpx;
  padding: 8rpx 18rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.settlement-tag.small {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
}

.settlement-tag.daily {
  color: #ff7d00;
  background-color: #fff7e6;
}

.settlement-tag.weekly {
  color: #165dff;
  background-color: #e8f0ff;
}

.settlement-tag.monthly {
  color: #646a73;
  background-color: #f2f3f5;
}

.insurance-tag {
  display: flex;
  align-items: center;
  background: linear-gradient(90deg, #f6ffed 0%, #d9f7be 100%);
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.insurance-tag-icon {
  font-size: 20rpx;
  margin-right: 6rpx;
}

.insurance-tag-text {
  font-size: 22rpx;
  color: #00b42a;
  font-weight: 500;
}

.skill-tag {
  font-size: 22rpx;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.2);
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.content {
  position: relative;
  z-index: 1;
  margin-top: -8rpx;
}

.card-section {
  margin: 24rpx 32rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2329;
  position: relative;
  padding-left: 20rpx;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8rpx;
  height: 28rpx;
  background: linear-gradient(180deg, #165dff 0%, #4080ff 100%);
  border-radius: 4rpx;
}

.description-content {
  font-size: 28rpx;
  color: #4e5969;
  line-height: 1.8;
  max-height: 240rpx;
  overflow: hidden;
}

.description-content.expanded {
  max-height: none;
}

.expand-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 20rpx;
  margin-top: 20rpx;
  border-top: 1rpx solid #f2f3f5;
}

.expand-btn {
  font-size: 26rpx;
  color: #165dff;
  margin-right: 8rpx;
}

.expand-arrow {
  font-size: 24rpx;
  color: #165dff;
}

.company-card {
  display: flex;
  align-items: center;
  padding: 28rpx;
}

.company-logo {
  width: 96rpx;
  height: 96rpx;
  background: linear-gradient(135deg, #e8f0ff 0%, #d6e4ff 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 48rpx;
}

.company-info {
  flex: 1;
  min-width: 0;
}

.company-name-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.company-name-text {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f2329;
  margin-right: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.certified-badge {
  display: flex;
  align-items: center;
  background: linear-gradient(90deg, #e8f0ff 0%, #f0f4ff 100%);
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  flex-shrink: 0;
}

.badge-icon {
  font-size: 16rpx;
  color: #165dff;
  margin-right: 4rpx;
  font-weight: bold;
}

.badge-text {
  font-size: 18rpx;
  color: #165dff;
}

.company-desc {
  font-size: 24rpx;
  color: #86909c;
}

.company-arrow {
  font-size: 40rpx;
  color: #c9cdd4;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.safety-section {
  padding: 28rpx;
}

.safety-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.safety-item {
  display: flex;
  align-items: center;
}

.safety-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.safety-icon-wrap.blue {
  background: linear-gradient(135deg, #e8f0ff 0%, #d6e4ff 100%);
}

.safety-icon-wrap.green {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
}

.safety-icon-wrap.gray {
  background: linear-gradient(135deg, #f2f3f5 0%, #e5e6eb 100%);
}

.safety-icon-wrap.orange {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
}

.safety-icon {
  font-size: 36rpx;
}

.safety-item-info {
  flex: 1;
}

.safety-item-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #1f2329;
  display: block;
  margin-bottom: 6rpx;
}

.safety-item-desc {
  font-size: 24rpx;
  color: #86909c;
}

.similar-scroll {
  white-space: nowrap;
  margin: 0 -28rpx;
  padding: 0 28rpx;
}

.similar-list {
  display: inline-flex;
  gap: 20rpx;
}

.similar-card {
  width: 280rpx;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16rpx;
  padding: 24rpx;
  display: inline-block;
  border: 1rpx solid #f2f3f5;
  vertical-align: top;
}

.similar-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #1f2329;
  display: block;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.similar-salary {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff7d00;
  display: block;
  margin-bottom: 12rpx;
}

.similar-company {
  font-size: 24rpx;
  color: #86909c;
  display: block;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.similar-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.similar-distance {
  font-size: 22rpx;
  color: #bbbfc4;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  background-color: #ffffff;
  padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.bottom-left {
  padding-right: 32rpx;
  border-right: 1rpx solid #f2f3f5;
}

.favorite-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8rpx 24rpx;
}

.favorite-icon {
  font-size: 44rpx;
  margin-bottom: 4rpx;
}

.favorite-text {
  font-size: 22rpx;
  color: #86909c;
}

.favorite-btn.active .favorite-text {
  color: #ff4d4f;
}

.bottom-right {
  flex: 1;
  padding-left: 32rpx;
}

.apply-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
  background: linear-gradient(90deg, #165dff 0%, #4080ff 100%);
  border-radius: 44rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(22, 93, 255, 0.3);
}

.apply-btn.disabled {
  background: linear-gradient(90deg, #c9cdd4 0%, #e5e6eb 100%);
  color: #ffffff;
  box-shadow: none;
}

.apply-btn.progress {
  background: linear-gradient(90deg, #00b42a 0%, #23c343 100%);
  box-shadow: 0 8rpx 24rpx rgba(0, 180, 42, 0.3);
}

.apply-animation {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 80rpx;
  background-color: rgba(0, 0, 0, 0.8);
  border-radius: 24rpx;
  z-index: 1000;
}

.resume-fly {
  animation: flyIn 0.5s ease-out;
}

.resume-icon {
  font-size: 80rpx;
}

.animation-text {
  font-size: 32rpx;
  color: #ffffff;
  margin-top: 24rpx;
}

@keyframes flyIn {
  0% {
    opacity: 0;
    transform: translateY(-100rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
