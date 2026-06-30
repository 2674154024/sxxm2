<template>
  <view class="page">
    <view class="safety-scroll">
      <swiper class="safety-swiper" vertical autoplay :interval="3000" circular>
        <swiper-item>
          <text class="safety-text">平台严禁收取任何押金、培训费、中介费！如遇收费请立即举报</text>
        </swiper-item>
      </swiper>
    </view>

    <view class="search-area">
      <view class="search-bar" @click="handleSearch">
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
        </view>
      </view>
    </scroll-view>

    <view class="lbs-section">
      <view class="section-header">
        <text class="section-title">附近好工作</text>
        <text class="section-more" @click="handleMore">查看更多</text>
      </view>

      <view v-if="!hasLocation" class="no-location">
        <view class="location-icon">📍</view>
        <text class="location-text">开启定位，查看附近兼职</text>
        <button class="location-btn" @click="handleLocation">立即开启</button>
      </view>

      <view v-else class="job-list">
        <view
          class="job-card"
          v-for="job in jobList"
          :key="job.job_id"
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
            <view v-if="job.is_certified" class="certified-tag">认证企业</view>
          </view>

          <view class="job-tags">
            <view class="settlement-tag" :class="job.settlement_type">
              {{ getSettlementText(job.settlement_type) }}
            </view>
            <view class="distance-tag">{{ job.distance }}km</view>
          </view>

          <view class="job-address">
            <text class="address-icon">📍</text>
            <text class="address-text">{{ job.address }}</text>
          </view>

          <view class="job-skills">
            <view class="skill-tag" v-for="skill in job.skill_tags" :key="skill">
              {{ skill }}
            </view>
          </view>

          <view v-if="job.has_insurance" class="insurance-badge">
            <text class="insurance-icon">🛡️</text>
            <text class="insurance-text">含意外险</text>
          </view>
        </view>

        <view v-if="loading" class="loading-more">
          <text class="loading-text">加载中...</text>
        </view>

        <view v-if="!loading && !hasMore && jobList.length > 0" class="no-more">
          <text class="no-more-text">已加载全部</text>
        </view>
      </view>
    </view>

    <view class="safety-fixed">
      <text class="safety-icon">🛡️</text>
      <text class="safety-tip">本平台所有岗位支持薪资托管，无押金风险</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
const longitude = ref(112.9388)
const latitude = ref(28.2280)

const jobList = ref([
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

const total = ref(12)
const page = ref(1)
const size = ref(20)

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

const handleSearch = () => {
  uni.navigateTo({ url: '/pages/student/job-detail/index?search=true' })
}

const handleCategoryChange = (value: string) => {
  if (currentCategory.value === value) return
  currentCategory.value = value
  jobStore.setIndustryTag(value)
  page.value = 1
  jobList.value = []
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
    page.value++
    jobStore.setJobList(jobList.value, total.value, page.value - 1)
  } catch (error) {
    console.error('获取岗位列表失败:', error)
  } finally {
    loading.value = false
  }
}

const onPullDownRefresh = () => {
  page.value = 1
  jobList.value = []
  fetchJobList().then(() => {
    uni.stopPullDownRefresh()
  })
}

const onReachBottom = () => {
  if (loading.value || jobList.value.length >= total.value) return
  fetchJobList()
}

onMounted(() => {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      longitude.value = res.longitude
      latitude.value = res.latitude
      hasLocation.value = true
    },
    fail: () => {
      hasLocation.value = false
    }
  })
})
</script>

<script lang="ts">
export default {
  onPullDownRefresh() {
    ;(this as any).onPullDownRefresh()
  },
  onReachBottom() {
    ;(this as any).onReachBottom()
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.safety-scroll {
  background-color: #fff1f0;
  padding: 12rpx 0;
}

.safety-swiper {
  height: 48rpx;
}

.safety-text {
  font-size: 24rpx;
  color: #f53f3f;
}

.search-area {
  background-color: #ffffff;
  padding: 20rpx 32rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  background-color: #f2f3f5;
  border-radius: 48rpx;
  padding: 20rpx 32rpx;
}

.search-icon {
  font-size: 28rpx;
  margin-right: 16rpx;
}

.search-placeholder {
  font-size: 28rpx;
  color: #86909c;
}

.category-scroll {
  background-color: #ffffff;
  white-space: nowrap;
}

.category-list {
  display: inline-flex;
  padding: 20rpx 32rpx;
  gap: 32rpx;
}

.category-item {
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background-color: #f2f3f5;
}

.category-item.active {
  background-color: #165dff;
}

.category-text {
  font-size: 26rpx;
  color: #1f2329;
}

.category-item.active .category-text {
  color: #ffffff;
}

.lbs-section {
  margin-top: 24rpx;
  background-color: #ffffff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #f2f3f5;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2329;
}

.section-more {
  font-size: 26rpx;
  color: #165dff;
}

.no-location {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
}

.location-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.location-text {
  font-size: 28rpx;
  color: #86909c;
  margin-bottom: 32rpx;
}

.location-btn {
  width: 320rpx;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #165dff;
  color: #ffffff;
  font-size: 28rpx;
  border-radius: 44rpx;
  border: none;
}

.job-list {
  padding: 0 32rpx;
}

.job-card {
  padding: 24rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.job-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2329;
  flex: 1;
}

.job-right {
  display: flex;
  align-items: baseline;
}

.job-salary {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff7d00;
}

.salary-unit {
  font-size: 24rpx;
  color: #ff7d00;
  margin-left: 8rpx;
}

.job-company-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.company-name {
  font-size: 26rpx;
  color: #646a73;
}

.certified-tag {
  font-size: 20rpx;
  color: #165dff;
  background-color: #e8f0ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  margin-left: 12rpx;
}

.job-tags {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.settlement-tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
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

.distance-tag {
  font-size: 22rpx;
  color: #86909c;
  background-color: #f2f3f5;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.job-address {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.address-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.address-text {
  font-size: 24rpx;
  color: #86909c;
}

.job-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.skill-tag {
  font-size: 22rpx;
  color: #86909c;
  background-color: #f2f3f5;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.insurance-badge {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f2f3f5;
}

.insurance-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.insurance-text {
  font-size: 22rpx;
  color: #00b42a;
}

.loading-more {
  padding: 32rpx 0;
  text-align: center;
}

.loading-text {
  font-size: 26rpx;
  color: #86909c;
}

.no-more {
  padding: 24rpx 0;
  text-align: center;
}

.no-more-text {
  font-size: 24rpx;
  color: #bbbfc4;
}

.safety-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  background-color: rgba(255, 255, 255, 0.95);
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.safety-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.safety-tip {
  font-size: 26rpx;
  color: #00b42a;
}
</style>