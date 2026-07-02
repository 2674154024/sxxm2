<template>
  <view class="page">
    <view class="safety-banner" @click="handleSafetyClick">
      <view class="horn-icon">
        <text class="horn">📢</text>
      </view>
      <swiper class="safety-swiper" vertical autoplay :interval="3000" circular :display-multiple-items="1">
        <swiper-item>
          <text class="safety-text">平台严禁收取任何押金、培训费、中介费！如遇收费请立即举报</text>
        </swiper-item>
        <swiper-item>
          <text class="safety-text">薪资由平台托管，工作完成后自动发放，保障您的劳动权益</text>
        </swiper-item>
      </swiper>
      <text class="safety-arrow">›</text>
    </view>

    <view class="search-area">
      <view class="search-bar" hover-class="search-bar-hover" @click="handleSearch">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜索兼职岗位/企业/商圈</text>
      </view>
    </view>

    <scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
      <view class="category-list">
        <view
          class="category-item"
          :class="{ active: currentCategory === item.value }"
          v-for="item in categories"
          :key="item.value"
          @click="handleCategoryChange(item.value)"
        >
          <text class="category-text">{{ item.label }}</text>
          <view class="category-indicator" v-if="currentCategory === item.value"></view>
        </view>
      </view>
    </scroll-view>

    <view class="lbs-section">
      <view class="section-header">
        <text class="section-title">附近好工作</text>
        <text class="section-more" @click="handleMore">查看更多 ›</text>
      </view>

      <view v-if="!hasLocation" class="no-location">
        <view class="location-icon">📍</view>
        <text class="location-text">开启定位，查看附近兼职</text>
        <button class="location-btn" @click="handleLocation">立即开启</button>
      </view>

      <view v-else class="job-list">
        <view v-if="skeletonLoading" class="skeleton-list">
          <view class="skeleton-card" v-for="i in 3" :key="i">
            <view class="skeleton-header">
              <view class="skeleton-title"></view>
              <view class="skeleton-salary"></view>
            </view>
            <view class="skeleton-company"></view>
            <view class="skeleton-tags">
              <view class="skeleton-tag"></view>
              <view class="skeleton-tag"></view>
            </view>
            <view class="skeleton-address"></view>
            <view class="skeleton-skills">
              <view class="skeleton-skill"></view>
              <view class="skeleton-skill"></view>
            </view>
          </view>
        </view>

        <template v-else>
          <view v-if="jobList.length === 0" class="empty-state">
            <view class="empty-icon">📭</view>
            <text class="empty-title">暂无相关岗位</text>
            <text class="empty-desc">换个分类试试吧~</text>
          </view>

          <view
            class="job-card"
            v-for="job in jobList"
            :key="job.job_id"
            hover-class="job-card-hover"
            @click="handleJobClick(job)"
          >
            <view class="job-header">
              <text class="job-name">{{ job.job_name }}</text>
              <view class="job-right">
                <text class="job-salary">¥{{ job.salary }}</text>
                <text class="salary-unit">{{ getSalaryUnit(job.salary_type) }}</text>
              </view>
            </view>

            <view class="job-company-row">
              <text class="company-name">{{ job.company_name }}</text>
              <view v-if="job.is_certified" class="certified-tag">
                <text class="certified-icon">✓</text>
                <text class="certified-text">认证企业</text>
              </view>
            </view>

            <view class="job-tags">
              <view class="settlement-tag" :class="job.settlement_type">
                {{ getSettlementText(job.settlement_type) }}
              </view>
              <view class="distance-tag">📍 {{ job.distance }}km</view>
            </view>

            <view class="job-address">
              <text class="address-text">{{ job.address }}</text>
            </view>

            <view class="job-skills">
              <view class="skill-tag" v-for="skill in job.skill_tags" :key="skill">
                {{ skill }}
              </view>
            </view>

            <view v-if="job.has_insurance" class="insurance-badge">
              <view class="insurance-icon-wrap">
                <text class="insurance-icon">🛡️</text>
              </view>
              <text class="insurance-text">含意外险</text>
            </view>
          </view>
        </template>

        <view v-if="loading && !skeletonLoading" class="loading-more">
          <text class="loading-text">加载中...</text>
        </view>

        <view v-if="!loading && !hasMore && jobList.length > 0" class="no-more">
          <text class="no-more-text">— 已加载全部 —</text>
        </view>
      </view>
    </view>

    <view class="safety-footer">
      <view class="safety-footer-content">
        <text class="safety-footer-icon">🛡️</text>
        <text class="safety-footer-text">本平台所有岗位支持薪资托管，无押金风险</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useJobStore } from '../../../store/index'
import { jobApi } from '../../../api/job'

const jobStore = useJobStore()

const categories = ref([
  { label: '全部', value: '' },
  { label: '茶饮', value: '茶饮' },
  { label: '零售', value: '零售' },
  { label: '家教', value: '家教' },
  { label: '会展', value: '会展' },
  { label: '上门服务', value: '上门服务' },
  { label: '新媒体', value: '新媒体' },
  { label: '其他', value: '其他' }
])

const currentCategory = ref('')
const hasLocation = ref(true)
const loading = ref(false)
const skeletonLoading = ref(false)
const longitude = ref(112.9388)
const latitude = ref(28.2280)

const mockJobList = ref([
  {
    job_id: '1',
    job_name: '奶茶店店员',
    company_name: '茶颜悦色',
    is_certified: true,
    salary: 18,
    salary_type: 'hourly',
    settlement_type: 'daily',
    address: '长沙市岳麓区麓山南路88号',
    distance: 0.8,
    skill_tags: ['服务', '沟通'],
    has_insurance: true,
    industry_tag: '茶饮'
  },
  {
    job_id: '2',
    job_name: '超市促销',
    company_name: '步步高超市',
    is_certified: true,
    salary: 200,
    salary_type: 'daily',
    settlement_type: 'daily',
    address: '长沙市天心区黄兴南路步行街',
    distance: 2.3,
    skill_tags: ['销售', '促销'],
    has_insurance: true,
    industry_tag: '零售'
  },
  {
    job_id: '3',
    job_name: '初中数学家教',
    company_name: '学思教育',
    is_certified: true,
    salary: 150,
    salary_type: 'hourly',
    settlement_type: 'weekly',
    address: '长沙市芙蓉区解放东路',
    distance: 3.5,
    skill_tags: ['数学', '教学'],
    has_insurance: false,
    industry_tag: '家教'
  },
  {
    job_id: '4',
    job_name: '会展协助',
    company_name: '长沙国际会展中心',
    is_certified: true,
    salary: 220,
    salary_type: 'daily',
    settlement_type: 'daily',
    address: '长沙市长沙县国展路',
    distance: 5.2,
    skill_tags: ['活动执行', '沟通'],
    has_insurance: true,
    industry_tag: '会展'
  },
  {
    job_id: '5',
    job_name: '上门家政',
    company_name: '58到家',
    is_certified: true,
    salary: 40,
    salary_type: 'hourly',
    settlement_type: 'weekly',
    address: '长沙市开福区湘江中路',
    distance: 4.1,
    skill_tags: ['保洁', '服务'],
    has_insurance: true,
    industry_tag: '上门服务'
  },
  {
    job_id: '6',
    job_name: '短视频剪辑',
    company_name: '芒果传媒',
    is_certified: true,
    salary: 100,
    salary_type: 'hourly',
    settlement_type: 'monthly',
    address: '长沙市雨花区韶山南路',
    distance: 3.8,
    skill_tags: ['剪辑', 'PR'],
    has_insurance: false,
    industry_tag: '新媒体'
  }
])

const jobList = ref([...mockJobList.value])

const total = ref(12)
const page = ref(1)
const size = ref(20)
const hasMore = ref(true)

const getSalaryUnit = (type: string) => {
  const map: Record<string, string> = {
    hourly: '时薪',
    daily: '日薪',
    monthly: '月薪'
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

const handleSafetyClick = () => {
  uni.navigateTo({ url: '/pages/student/safety/index' })
}

const handleSearch = () => {
  uni.navigateTo({ url: '/pages/student/job-detail/index?search=true' })
}

const handleCategoryChange = (value: string) => {
  if (currentCategory.value === value) return
  currentCategory.value = value
  jobStore.setIndustryTag(value)
  page.value = 1
  skeletonLoading.value = true
  fetchJobList()
}

const handleLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      longitude.value = res.longitude
      latitude.value = res.latitude
      hasLocation.value = true
      fetchJobList()
    },
    fail: () => {
      uni.showToast({ title: '定位失败', icon: 'none' })
    }
  })
}

const handleMore = () => {
  uni.navigateTo({ url: '/pages/student/job-detail/index?list=true' })
}

const handleJobClick = (job: { job_id: string }) => {
  uni.navigateTo({ url: `/pages/student/job-detail/index?job_id=${job.job_id}` })
}

const fetchJobList = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const params = {
      longitude: longitude.value,
      latitude: latitude.value,
      distance: 5,
      page: page.value,
      size: size.value,
      industry_tag: currentCategory.value
    }
    const res = await jobApi.getJobList(params)
    if (page.value === 1) {
      jobList.value = res.data.list
      total.value = res.data.total
    } else {
      jobList.value = [...jobList.value, ...res.data.list]
    }
    hasMore.value = jobList.value.length < total.value
    page.value++
    jobStore.setJobList(jobList.value, total.value, page.value - 1)
  } catch (error) {
    console.error('获取岗位列表失败:', error)
    // API失败时使用mock数据
    const filtered = currentCategory.value
      ? mockJobList.value.filter(job => job.industry_tag === currentCategory.value)
      : [...mockJobList.value]
    if (page.value === 1) {
      jobList.value = filtered
      total.value = filtered.length
    }
    hasMore.value = false
  } finally {
    loading.value = false
    skeletonLoading.value = false
  }
}

onPullDownRefresh(() => {
  page.value = 1
  jobList.value = []
  skeletonLoading.value = true
  fetchJobList().then(() => {
    uni.stopPullDownRefresh()
  })
})

onReachBottom(() => {
  if (loading.value || jobList.value.length >= total.value) return
  fetchJobList()
})

onMounted(() => {
  // H5环境下直接显示mock数据
  // #ifdef H5
  hasLocation.value = true
  skeletonLoading.value = false
  // #endif
  // #ifndef H5
  setTimeout(() => {
    skeletonLoading.value = false
  }, 1000)
  // #endif

  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      longitude.value = res.longitude
      latitude.value = res.latitude
      hasLocation.value = true
    },
    fail: () => {
      // #ifndef H5
      hasLocation.value = false
      // #endif
    }
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #f5f5f5 200rpx);
  padding-bottom: 180rpx;
}

.safety-banner {
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  height: 72rpx;
  background: linear-gradient(90deg, #ff4d4f 0%, #ff7875 50%, #ff4d4f 100%);
  background-size: 200% 100%;
  animation: gradientMove 3s ease infinite;
}

@keyframes gradientMove {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.horn-icon {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.horn {
  font-size: 32rpx;
  animation: hornShake 1.5s ease-in-out infinite;
}

@keyframes hornShake {
  0%, 100% {
    transform: rotate(0deg);
  }
  20% {
    transform: rotate(-15deg);
  }
  40% {
    transform: rotate(15deg);
  }
  60% {
    transform: rotate(-10deg);
  }
  80% {
    transform: rotate(10deg);
  }
}

.safety-swiper {
  flex: 1;
  height: 40rpx;
}

.safety-text {
  font-size: 26rpx;
  color: #ffffff;
  line-height: 40rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.safety-arrow {
  font-size: 36rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-left: 12rpx;
}

.search-area {
  padding: 24rpx 32rpx 16rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  height: 88rpx;
  background-color: #ffffff;
  border-radius: 44rpx;
  padding: 0 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(22, 93, 255, 0.08);
  transition: all 0.2s ease;
}

.search-bar-hover {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(22, 93, 255, 0.12);
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
  color: #165dff;
}

.search-placeholder {
  font-size: 28rpx;
  color: #86909c;
}

.category-scroll {
  white-space: nowrap;
  background: transparent;
}

.category-list {
  display: inline-flex;
  padding: 16rpx 32rpx 24rpx;
  gap: 0;
}

.category-item {
  position: relative;
  padding: 16rpx 32rpx;
  margin-right: 8rpx;
  transition: all 0.3s ease;
}

.category-item.active {
  background-color: transparent;
}

.category-text {
  font-size: 30rpx;
  color: #4e5969;
  font-weight: 500;
  transition: all 0.3s ease;
}

.category-item.active .category-text {
  color: #165dff;
  font-size: 34rpx;
  font-weight: bold;
}

.category-indicator {
  position: absolute;
  bottom: 4rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background: linear-gradient(90deg, #165dff 0%, #4080ff 100%);
  border-radius: 3rpx;
}

.lbs-section {
  margin: 0 32rpx;
  background-color: transparent;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
}

.section-title {
  font-size: 36rpx;
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
  height: 32rpx;
  background: linear-gradient(180deg, #165dff 0%, #4080ff 100%);
  border-radius: 4rpx;
}

.section-more {
  font-size: 26rpx;
  color: #165dff;
}

.no-location {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
  background-color: #ffffff;
  border-radius: 16rpx;
}

.location-icon {
  font-size: 100rpx;
  margin-bottom: 32rpx;
}

.location-text {
  font-size: 28rpx;
  color: #86909c;
  margin-bottom: 40rpx;
}

.location-btn {
  width: 360rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(90deg, #165dff 0%, #4080ff 100%);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 44rpx;
  border: none;
}

.job-list {
  padding: 0;
}

.skeleton-list {
  padding: 0;
}

.skeleton-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.skeleton-title {
  width: 240rpx;
  height: 36rpx;
  background: linear-gradient(90deg, #f2f3f5 25%, #e8e9eb 50%, #f2f3f5 75%);
  background-size: 200% 100%;
  animation: skeletonLoading 1.5s ease-in-out infinite;
  border-radius: 8rpx;
}

.skeleton-salary {
  width: 160rpx;
  height: 36rpx;
  background: linear-gradient(90deg, #f2f3f5 25%, #e8e9eb 50%, #f2f3f5 75%);
  background-size: 200% 100%;
  animation: skeletonLoading 1.5s ease-in-out infinite;
  border-radius: 8rpx;
}

.skeleton-company {
  width: 200rpx;
  height: 26rpx;
  background: linear-gradient(90deg, #f2f3f5 25%, #e8e9eb 50%, #f2f3f5 75%);
  background-size: 200% 100%;
  animation: skeletonLoading 1.5s ease-in-out infinite;
  border-radius: 6rpx;
  margin-bottom: 20rpx;
}

.skeleton-tags {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.skeleton-tag {
  width: 80rpx;
  height: 32rpx;
  background: linear-gradient(90deg, #f2f3f5 25%, #e8e9eb 50%, #f2f3f5 75%);
  background-size: 200% 100%;
  animation: skeletonLoading 1.5s ease-in-out infinite;
  border-radius: 8rpx;
}

.skeleton-address {
  width: 400rpx;
  height: 26rpx;
  background: linear-gradient(90deg, #f2f3f5 25%, #e8e9eb 50%, #f2f3f5 75%);
  background-size: 200% 100%;
  animation: skeletonLoading 1.5s ease-in-out infinite;
  border-radius: 6rpx;
  margin-bottom: 20rpx;
}

.skeleton-skills {
  display: flex;
  gap: 12rpx;
}

.skeleton-skill {
  width: 70rpx;
  height: 28rpx;
  background: linear-gradient(90deg, #f2f3f5 25%, #e8e9eb 50%, #f2f3f5 75%);
  background-size: 200% 100%;
  animation: skeletonLoading 1.5s ease-in-out infinite;
  border-radius: 6rpx;
}

@keyframes skeletonLoading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
  background-color: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.empty-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2329;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #86909c;
}

.job-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
}

.job-card-hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.job-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #1f2329;
  flex: 1;
  margin-right: 16rpx;
  line-height: 1.4;
}

.job-right {
  display: flex;
  align-items: baseline;
  flex-shrink: 0;
}

.job-salary {
  font-size: 42rpx;
  font-weight: bold;
  color: #ff7d00;
  line-height: 1;
}

.salary-unit {
  font-size: 24rpx;
  color: #ff7d00;
  margin-left: 8rpx;
}

.job-company-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.company-name {
  font-size: 26rpx;
  color: #4e5969;
}

.certified-tag {
  display: flex;
  align-items: center;
  font-size: 20rpx;
  color: #165dff;
  background: linear-gradient(90deg, #e8f0ff 0%, #f0f4ff 100%);
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
}

.certified-icon {
  font-size: 18rpx;
  margin-right: 6rpx;
  color: #165dff;
  font-weight: bold;
}

.certified-text {
  font-size: 20rpx;
}

.job-tags {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
  flex-wrap: wrap;
}

.settlement-tag {
  font-size: 22rpx;
  padding: 8rpx 18rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.settlement-tag.daily {
  color: #ff7d00;
  background: linear-gradient(90deg, #fff7e6 0%, #ffe7ba 100%);
}

.settlement-tag.weekly {
  color: #165dff;
  background: linear-gradient(90deg, #e8f0ff 0%, #d6e4ff 100%);
}

.settlement-tag.monthly {
  color: #646a73;
  background: linear-gradient(90deg, #f2f3f5 0%, #e5e6eb 100%);
}

.distance-tag {
  font-size: 22rpx;
  color: #4e5969;
  background-color: #f2f3f5;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.job-address {
  margin-bottom: 20rpx;
}

.address-text {
  font-size: 26rpx;
  color: #86909c;
  line-height: 1.5;
}

.job-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.skill-tag {
  font-size: 22rpx;
  color: #4e5969;
  background-color: #f2f3f5;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.insurance-badge {
  display: flex;
  align-items: center;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f2f3f5;
}

.insurance-icon-wrap {
  width: 36rpx;
  height: 36rpx;
  background: linear-gradient(135deg, #d9f7be 0%, #95de64 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12rpx;
}

.insurance-icon {
  font-size: 20rpx;
}

.insurance-text {
  font-size: 24rpx;
  color: #00b42a;
  font-weight: 500;
}

.loading-more {
  padding: 40rpx 0;
  text-align: center;
}

.loading-text {
  font-size: 26rpx;
  color: #86909c;
}

.no-more {
  padding: 32rpx 0;
  text-align: center;
}

.no-more-text {
  font-size: 24rpx;
  color: #bbbfc4;
}

.safety-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 99;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, #ffffff 40%);
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.safety-footer-content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  background: linear-gradient(90deg, #e8f0ff 0%, #f0f4ff 50%, #e8f0ff 100%);
  border-radius: 36rpx;
  padding: 0 32rpx;
}

.safety-footer-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.safety-footer-text {
  font-size: 26rpx;
  color: #165dff;
  font-weight: 500;
}
</style>
