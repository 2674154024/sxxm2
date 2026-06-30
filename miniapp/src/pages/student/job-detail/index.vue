<template>
  <view class="page">
    <view v-if="loading" class="loading-mask">
      <text class="loading-text">加载中...</text>
    </view>

    <view v-else class="content">
      <view class="header-image">
        <image class="header-img" :src="jobData.cover_image || defaultCover" mode="aspectFill" />
        <view class="header-overlay">
          <text class="job-name">{{ jobData.job_name }}</text>
          <text class="job-salary">¥{{ jobData.salary }}/{{ getSalaryUnit(jobData.salary_type) }}</text>
        </view>
      </view>

      <view class="company-info-row">
        <text class="company-name">{{ jobData.company_name }}</text>
        <view v-if="jobData.is_certified" class="certified-tag">认证企业</view>
        <view class="credit-badge">
          <text class="credit-icon">⭐</text>
          <text class="credit-text">{{ jobData.credit_score }}分</text>
        </view>
      </view>

      <view class="info-card">
        <view class="info-item" @click="handleMapNavigation">
          <text class="info-icon">📍</text>
          <text class="info-label">工作地址</text>
          <text class="info-value">{{ jobData.address }}</text>
          <text class="info-arrow">›</text>
        </view>
        <view class="info-item">
          <text class="info-icon">⏰</text>
          <text class="info-label">工作时间</text>
          <text class="info-value">{{ formatWorkTime(jobData.work_time) }}</text>
        </view>
        <view class="info-item">
          <text class="info-icon">💰</text>
          <text class="info-label">结算方式</text>
          <view class="settlement-tag" :class="jobData.settlement_type">
            {{ getSettlementText(jobData.settlement_type) }}
          </view>
        </view>
        <view class="info-item">
          <text class="info-icon">👥</text>
          <text class="info-label">招聘人数</text>
          <text class="info-value">{{ jobData.hired_count }}/{{ jobData.total_count }}人</text>
        </view>
        <view class="info-item skills-item">
          <text class="info-icon">📋</text>
          <text class="info-label">技能要求</text>
          <view class="skill-tags">
            <view class="skill-tag" v-for="skill in jobData.skill_tags" :key="skill">{{ skill }}</view>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">岗位描述</text>
          <text class="expand-btn" @click="toggleDescription">{{ descExpanded ? '收起' : '展开' }}</text>
        </view>
        <view class="description-content" :class="{ expanded: descExpanded }">
          <rich-text :nodes="jobData.description"></rich-text>
        </view>
      </view>

      <view class="safety-card">
        <view class="safety-icon-wrap">
          <text class="safety-icon">🛡️</text>
        </view>
        <view class="safety-content">
          <text class="safety-title">安全保障</text>
          <text class="safety-desc">本岗位支持薪资托管，薪资由平台担保发放</text>
          <text class="insurance-text" :class="{ recommended: !jobData.has_insurance }">
            {{ jobData.has_insurance ? '含兼职意外险' : '建议企业购买意外险' }}
          </text>
        </view>
      </view>

      <view v-if="jobData.similar_jobs && jobData.similar_jobs.length > 0" class="section">
        <text class="section-title">相似岗位推荐</text>
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
              <text class="similar-distance">{{ item.distance }}km</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <view class="bottom-bar">
      <view class="bottom-left">
        <view class="favorite-btn" :class="{ active: isFavorite }" @click="handleFavorite">
          <text class="favorite-icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
          <text class="favorite-text">收藏</text>
        </view>
      </view>
      <button
        class="apply-btn"
        :class="applyBtnClass"
        @click="handleApply"
        :disabled="applyBtnDisabled"
      >
        <text>{{ applyBtnText }}</text>
      </button>
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

const defaultCover = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20office%20workplace%20background%20professional%20clean&image_size=landscape_16_9'
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
  uni.navigateTo({ url: `/pages/student/job-detail/index?job_id=${item.job_id}` })
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
  background-color: #F2F3F5;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.loading-text {
  font-size: 30rpx;
  color: #86909C;
}

.header-image {
  position: relative;
  height: 320rpx;
  overflow: hidden;
}

.header-img {
  width: 100%;
  height: 100%;
}

.header-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 32rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
}

.job-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #FFFFFF;
  display: block;
  margin-bottom: 8rpx;
}

.job-salary {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF7D00;
}

.company-info-row {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  padding: 24rpx 32rpx;
}

.company-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1F2329;
}

.certified-tag {
  font-size: 22rpx;
  color: #165DFF;
  background-color: #E8F0FF;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
}

.credit-badge {
  display: flex;
  align-items: center;
  margin-left: auto;
  padding: 8rpx 16rpx;
  background-color: #FFF7E6;
  border-radius: 24rpx;
}

.credit-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.credit-text {
  font-size: 24rpx;
  color: #FF7D00;
}

.info-card {
  background-color: #FFFFFF;
  margin: 24rpx;
  border-radius: 16rpx;
  padding: 0 32rpx;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #F2F3F5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.info-label {
  font-size: 28rpx;
  color: #86909C;
  width: 160rpx;
}

.info-value {
  font-size: 28rpx;
  color: #1F2329;
  flex: 1;
}

.info-arrow {
  font-size: 32rpx;
  color: #C9CDD4;
}

.settlement-tag {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
}

.settlement-tag.daily {
  color: #FF7D00;
  background-color: #FFF7E6;
}

.settlement-tag.weekly {
  color: #165DFF;
  background-color: #E8F0FF;
}

.settlement-tag.monthly {
  color: #646a73;
  background-color: #F2F3F5;
}

.skills-item {
  flex-wrap: wrap;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  flex: 1;
}

.skill-tag {
  font-size: 24rpx;
  color: #86909C;
  background-color: #F2F3F5;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.section {
  background-color: #FFFFFF;
  margin: 24rpx;
  border-radius: 16rpx;
  padding: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
}

.expand-btn {
  font-size: 26rpx;
  color: #165DFF;
}

.description-content {
  font-size: 28rpx;
  color: #4E5969;
  line-height: 1.8;
  max-height: 240rpx;
  overflow: hidden;
}

.description-content.expanded {
  max-height: none;
}

.safety-card {
  display: flex;
  align-items: flex-start;
  margin: 24rpx;
  padding: 28rpx;
  background-color: #FFFFFF;
  border-radius: 16rpx;
  border: 2rpx solid #D9F7BE;
}

.safety-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  background-color: #D9F7BE;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.safety-icon {
  font-size: 36rpx;
}

.safety-content {
  flex: 1;
}

.safety-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1F2329;
  display: block;
  margin-bottom: 8rpx;
}

.safety-desc {
  font-size: 26rpx;
  color: #4E5969;
  display: block;
  margin-bottom: 8rpx;
}

.insurance-text {
  font-size: 24rpx;
  color: #00B42A;
}

.insurance-text.recommended {
  color: #FF7D00;
}

.similar-scroll {
  white-space: nowrap;
}

.similar-list {
  display: inline-flex;
  gap: 20rpx;
}

.similar-card {
  width: 240rpx;
  background-color: #F8F9FA;
  border-radius: 12rpx;
  padding: 20rpx;
  display: inline-block;
}

.similar-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #1F2329;
  display: block;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.similar-salary {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF7D00;
  display: block;
  margin-bottom: 8rpx;
}

.similar-company {
  font-size: 22rpx;
  color: #86909C;
  display: block;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.similar-distance {
  font-size: 22rpx;
  color: #BBBFc4;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.bottom-left {
  padding-right: 24rpx;
}

.favorite-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8rpx 32rpx;
}

.favorite-icon {
  font-size: 40rpx;
  margin-bottom: 4rpx;
}

.favorite-text {
  font-size: 22rpx;
  color: #86909C;
}

.apply-btn {
  flex: 1;
  font-size: 30rpx;
  font-weight: bold;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
}

.apply-btn.disabled {
  background-color: #C9CDD4;
  color: #FFFFFF;
}

.apply-btn.progress {
  background-color: #00B42A;
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
  color: #FFFFFF;
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