<template>
  <view class="page">
    <view class="clock-header">
      <view class="header-top">
        <view class="date-info">
          <text class="date-text">{{ currentDate }}</text>
          <text class="week-text">{{ currentWeek }}</text>
        </view>
        <view class="status-badge" :class="clockStatus">
          <text class="status-dot"></text>
          <text class="status-text">{{ clockStatusText }}</text>
        </view>
      </view>
    </view>

    <view class="location-card">
      <view class="location-icon-wrap">
        <text class="location-icon">📍</text>
      </view>
      <view class="location-content">
        <text class="location-label">当前位置</text>
        <text class="location-text">{{ currentLocation }}</text>
      </view>
      <view class="location-action" @click="getLocation">
        <text class="action-text">刷新</text>
      </view>
    </view>

    <view class="clock-card" :class="{ abnormal: isAbnormal, show: showSuccess }">
      <view class="clock-circle" :class="clockState" @click="handleClock">
        <view class="circle-ring"></view>
        <view class="circle-ring outer"></view>
        <view class="circle-inner">
          <text class="clock-icon">{{ clockState === 'clocked-out' ? '⏰' : '✅' }}</text>
          <text class="clock-text">{{ clockState === 'clocked-out' ? '上班打卡' : '下班打卡' }}</text>
          <text class="clock-subtext">{{ clockState === 'clocked-out' ? '点击开始工作' : '点击结束工作' }}</text>
        </view>
      </view>

      <view class="clock-stats">
        <view class="stat-item">
          <text class="stat-value">{{ workDuration }}</text>
          <text class="stat-label">今日工作时长</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ clockInTime || '--:--' }}</text>
          <text class="stat-label">上班打卡时间</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">打卡记录</text>
        <text class="section-more">查看全部</text>
      </view>
      <view class="record-list">
        <view class="record-item" v-for="record in clockRecords" :key="record.id">
          <view class="record-date">
            <text class="date-num">{{ record.day }}</text>
            <text class="date-week">{{ record.week }}</text>
          </view>
          <view class="record-info">
            <view class="record-job">
              <text class="job-name">{{ record.job_name }}</text>
              <view class="record-status-dot" :class="record.is_abnormal ? 'abnormal' : 'normal'"></view>
            </view>
            <view class="record-times">
              <view class="time-block">
                <text class="time-label">上班</text>
                <text class="time-value" :class="{ abnormal: !record.clock_in_time }">
                  {{ record.clock_in_time || '未打卡' }}
                </text>
              </view>
              <view class="time-arrow">→</view>
              <view class="time-block">
                <text class="time-label">下班</text>
                <text class="time-value" :class="{ abnormal: !record.clock_out_time }">
                  {{ record.clock_out_time || '未打卡' }}
                </text>
              </view>
            </view>
          </view>
          <view class="record-actions">
            <text 
              class="appeal-btn" 
              v-if="record.is_abnormal && record.appeal_status === 'none'"
              @click="openAppeal(record)"
            >申诉</text>
            <text 
              class="appeal-status" 
              v-else-if="record.appeal_status === 'pending'"
            >申诉中</text>
            <text 
              class="appeal-status approved" 
              v-else-if="record.appeal_status === 'approved'"
            >已通过</text>
            <text 
              class="appeal-status rejected" 
              v-else-if="record.appeal_status === 'rejected'"
            >已驳回</text>
            <text v-else class="record-normal">正常</text>
          </view>
        </view>
      </view>
    </view>

    <view class="success-animation" v-if="showSuccess">
      <view class="success-content">
        <view class="success-icon-wrap">
          <text class="success-icon">✓</text>
        </view>
        <text class="success-title">打卡成功</text>
        <text class="success-time">{{ successTime }}</text>
      </view>
    </view>

    <view class="appeal-modal" v-if="showAppeal">
      <view class="appeal-content">
        <view class="modal-header">
          <text class="appeal-title">异常打卡申诉</text>
          <text class="modal-close" @click="closeAppeal">✕</text>
        </view>
        <view class="appeal-form">
          <view class="form-item">
            <text class="form-label">申诉原因</text>
            <textarea 
              class="form-textarea" 
              placeholder="请输入申诉原因..." 
              v-model="appealReason"
              :maxlength="500"
              placeholder-class="input-placeholder"
            />
          </view>
          <view class="form-item">
            <text class="form-label">证据照片</text>
            <view class="image-upload">
              <view 
                class="upload-item" 
                v-for="(image, index) in appealImages" 
                :key="index"
              >
                <image class="upload-image" :src="image" mode="aspectFill" />
                <view class="upload-remove" @click="removeImage(index)">
                  <text class="remove-icon">×</text>
                </view>
              </view>
              <view 
                class="upload-btn" 
                v-if="appealImages.length < 3"
                @click="chooseImage"
              >
                <text class="upload-icon">+</text>
                <text class="upload-text">上传</text>
              </view>
            </view>
          </view>
        </view>
        <view class="appeal-footer">
          <view class="cancel-btn" @click="closeAppeal">取消</view>
          <view 
            class="submit-btn" 
            :class="{ disabled: !appealReason.trim() }"
            @click="submitAppeal"
          >提交申诉</view>
        </view>
      </view>
    </view>

    <view class="distance-modal" v-if="showDistanceModal">
      <view class="distance-content">
        <view class="distance-icon-wrap">
          <text class="distance-icon">⚠️</text>
        </view>
        <text class="distance-title">位置较远提醒</text>
        <text class="distance-desc">当前位置距岗位地址较远（{{ distanceToJob }}米），是否继续打卡？</text>
        <text class="distance-sub">继续打卡将标记为异常打卡，需填写说明</text>
        <view class="distance-footer">
          <view class="cancel-btn" @click="showDistanceModal = false">取消</view>
          <view class="confirm-btn" @click="confirmAbnormalClock">继续打卡</view>
        </view>
      </view>
    </view>

    <view class="abnormal-reason-modal" v-if="showAbnormalReason">
      <view class="abnormal-content">
        <view class="modal-header">
          <text class="abnormal-title">填写异常说明</text>
          <text class="modal-close" @click="showAbnormalReason = false">✕</text>
        </view>
        <textarea 
          class="abnormal-textarea" 
          placeholder="请输入异常原因（如交通堵塞、临时外出等）..." 
          v-model="abnormalReason"
          :maxlength="200"
          placeholder-class="input-placeholder"
        />
        <view class="abnormal-footer">
          <view class="cancel-btn" @click="showAbnormalReason = false">取消</view>
          <view 
            class="confirm-btn" 
            :class="{ disabled: !abnormalReason.trim() }"
            @click="submitAbnormalClock"
          >确认提交</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { clockApi, type ClockRecord } from '../../../api/clock'
import dayjs from 'dayjs'

const currentDate = ref(dayjs().format('YYYY年MM月DD日'))
const currentWeek = ref(['周日', '周一', '周二', '周三', '周四', '周五', '周六'][dayjs().day()])
const currentLocation = ref('获取位置中...')
const currentLatitude = ref(0)
const currentLongitude = ref(0)
const clockState = ref<'clocked-out' | 'clocked-in'>('clocked-out')
const clockInTime = ref('')
const workDuration = ref('0小时0分钟')
const isAbnormal = ref(false)
const distanceToJob = ref(0)
const showSuccess = ref(false)
const successTime = ref('')

const clockRecords = ref<ClockRecord[]>([])

const showAppeal = ref(false)
const appealReason = ref('')
const appealImages = ref<string[]>([])
const currentAppealRecord = ref<ClockRecord | null>(null)

const showDistanceModal = ref(false)
const showAbnormalReason = ref(false)
const abnormalReason = ref('')

let workTimer: ReturnType<typeof setInterval> | null = null

const clockStatus = computed(() => {
  if (clockState.value === 'clocked-out') return 'not-clock'
  return 'clocked-in'
})

const clockStatusText = computed(() => {
  if (clockState.value === 'clocked-out') return '未打卡'
  return '已上班'
})

const calculateDistance = (lat1: number, lng1: number, lat2: number, lng2: number): number => {
  const R = 6371000
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLng/2) * Math.sin(dLng/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
  return R * c
}

const getLocation = () => {
  uni.showLoading({ title: '定位中...' })
  
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      uni.hideLoading()
      currentLatitude.value = res.latitude
      currentLongitude.value = res.longitude
      currentLocation.value = `经度: ${res.longitude.toFixed(4)}, 纬度: ${res.latitude.toFixed(4)}`
      
      const jobLat = 28.2280
      const jobLng = 112.9388
      distanceToJob.value = Math.round(calculateDistance(res.latitude, res.longitude, jobLat, jobLng))
    },
    fail: (err) => {
      uni.hideLoading()
      console.error('定位失败', err)
      currentLocation.value = '定位失败，请检查权限'
      uni.showToast({ title: '定位失败', icon: 'none' })
    }
  })
}

const updateWorkDuration = () => {
  if (!clockInTime.value) return
  
  const startTime = dayjs(clockInTime.value)
  const now = dayjs()
  const diff = now.diff(startTime)
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  workDuration.value = `${hours}小时${minutes}分钟`
}

const showSuccessAnimation = () => {
  successTime.value = dayjs().format('HH:mm:ss')
  showSuccess.value = true
  setTimeout(() => {
    showSuccess.value = false
  }, 2000)
}

const handleClock = () => {
  if (currentLocation.value === '获取位置中...' || currentLocation.value.includes('失败')) {
    getLocation()
    return
  }
  
  if (distanceToJob.value > 500) {
    showDistanceModal.value = true
    return
  }
  
  if (clockState.value === 'clocked-out') {
    doClockIn(false)
  } else {
    doClockOut(false)
  }
}

const confirmAbnormalClock = () => {
  showDistanceModal.value = false
  showAbnormalReason.value = true
}

const submitAbnormalClock = () => {
  if (!abnormalReason.value.trim()) return
  
  if (clockState.value === 'clocked-out') {
    doClockIn(true)
  } else {
    doClockOut(true)
  }
  
  showAbnormalReason.value = false
  abnormalReason.value = ''
}

const doClockIn = async (isAbnormalClock: boolean) => {
  uni.showLoading({ title: '打卡中...' })
  
  try {
    await clockApi.clock({
      job_id: '1',
      clock_type: 'clock_in',
      latitude: currentLatitude.value,
      longitude: currentLongitude.value,
      address: currentLocation.value,
      abnormal_reason: isAbnormalClock ? abnormalReason.value : undefined
    })
    
    uni.hideLoading()
    clockState.value = 'clocked-in'
    clockInTime.value = dayjs().format('HH:mm')
    isAbnormal.value = isAbnormalClock
    
    showSuccessAnimation()
    
    workTimer = setInterval(updateWorkDuration, 60000)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '打卡失败', icon: 'none' })
  }
}

const doClockOut = async (isAbnormalClock: boolean) => {
  uni.showModal({
    title: '提示',
    content: '确定要下班打卡吗？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '打卡中...' })
        
        try {
          await clockApi.clock({
            job_id: '1',
            clock_type: 'clock_out',
            latitude: currentLatitude.value,
            longitude: currentLongitude.value,
            address: currentLocation.value,
            abnormal_reason: isAbnormalClock ? abnormalReason.value : undefined
          })
          
          uni.hideLoading()
          clockState.value = 'clocked-out'
          clockInTime.value = ''
          workDuration.value = '0小时0分钟'
          isAbnormal.value = false
          
          showSuccessAnimation()
          
          if (workTimer) {
            clearInterval(workTimer)
            workTimer = null
          }
          
          loadClockRecords()
        } catch (error) {
          uni.hideLoading()
          uni.showToast({ title: '打卡失败', icon: 'none' })
        }
      }
    }
  })
}

const loadClockRecords = async () => {
  try {
    const res = await clockApi.getClockRecords('1', 1, 7)
    if (res.data && res.data.list) {
      clockRecords.value = res.data.list.map((record: ClockRecord) => {
        const date = dayjs(record.clock_time || record.created_at)
        return {
          ...record,
          day: date.format('DD'),
          week: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.day()],
          clock_in_time: record.clock_type === 'clock_in' ? date.format('HH:mm') : undefined,
          clock_out_time: record.clock_type === 'clock_out' ? date.format('HH:mm') : undefined
        }
      })
    }
  } catch (error) {
    console.error('加载打卡记录失败', error)
  }
}

const openAppeal = (record: ClockRecord) => {
  currentAppealRecord.value = record
  appealReason.value = ''
  appealImages.value = []
  showAppeal.value = true
}

const closeAppeal = () => {
  showAppeal.value = false
  appealReason.value = ''
  appealImages.value = []
  currentAppealRecord.value = null
}

const chooseImage = () => {
  uni.chooseImage({
    count: 3 - appealImages.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      appealImages.value = [...appealImages.value, ...res.tempFilePaths]
    }
  })
}

const removeImage = (index: number) => {
  appealImages.value.splice(index, 1)
}

const submitAppeal = async () => {
  if (!appealReason.value.trim()) return
  if (!currentAppealRecord.value) return
  
  uni.showLoading({ title: '提交中...' })
  
  try {
    await clockApi.appeal({
      record_id: currentAppealRecord.value.id,
      reason: appealReason.value,
      evidence_images: appealImages.value
    })
    
    uni.hideLoading()
    uni.showToast({ title: '申诉已提交', icon: 'success' })
    closeAppeal()
    loadClockRecords()
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '提交失败', icon: 'none' })
  }
}

onMounted(() => {
  getLocation()
  loadClockRecords()
})

onUnmounted(() => {
  if (workTimer) {
    clearInterval(workTimer)
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
  padding-bottom: 40rpx;
}

.clock-header {
  background: linear-gradient(180deg, #165DFF 0%, #4080FF 100%);
  padding: 60rpx 32rpx 80rpx;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.date-info {
  display: flex;
  flex-direction: column;
}

.date-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #FFFFFF;
}

.week-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
}

.status-badge {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background-color: rgba(255, 255, 255, 0.2);
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 8rpx;
  background-color: #FF7D00;
}

.status-badge.clocked-in .status-dot {
  background-color: #52C41A;
}

.status-text {
  color: #FFFFFF;
  font-size: 24rpx;
}

.location-card {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  margin: -40rpx 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.location-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.location-icon {
  font-size: 32rpx;
}

.location-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.location-label {
  font-size: 24rpx;
  color: #86909C;
  margin-bottom: 6rpx;
}

.location-text {
  font-size: 26rpx;
  color: #1F2329;
}

.location-action {
  padding: 12rpx 24rpx;
  background-color: #E8F0FF;
  border-radius: 24rpx;
}

.action-text {
  font-size: 24rpx;
  color: #165DFF;
}

.clock-card {
  background-color: #FFFFFF;
  margin: 0 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 48rpx 32rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.clock-card.abnormal {
  border: 2rpx solid #FF4D4F;
}

.clock-circle {
  position: relative;
  width: 320rpx;
  height: 320rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
}

.circle-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  opacity: 0.2;
  transform: scale(1.15);
}

.circle-ring.outer {
  opacity: 0.1;
  transform: scale(1.3);
}

.circle-inner {
  position: relative;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1;
  box-shadow: 0 12rpx 40rpx rgba(22, 93, 255, 0.4);
}

.clock-circle.clocked-in .circle-ring,
.clock-circle.clocked-in .circle-inner {
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
}

.clock-circle.clocked-in .circle-inner {
  box-shadow: 0 12rpx 40rpx rgba(82, 196, 26, 0.4);
}

.clock-circle:active .circle-inner {
  transform: scale(0.95);
  transition: transform 0.2s;
}

.clock-icon {
  font-size: 72rpx;
  margin-bottom: 12rpx;
}

.clock-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 8rpx;
}

.clock-subtext {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.clock-stats {
  display: flex;
  width: 100%;
  padding: 0 24rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #86909C;
}

.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background-color: #E5E6EB;
  margin: 0 16rpx;
}

.section {
  background-color: #FFFFFF;
  margin: 0 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 28rpx 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2329;
}

.section-more {
  font-size: 24rpx;
  color: #86909C;
}

.record-list {
  padding: 0;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F2F3F5;
}

.record-item:last-child {
  border-bottom: none;
}

.record-date {
  width: 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.date-num {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2329;
}

.date-week {
  font-size: 22rpx;
  color: #86909C;
}

.record-info {
  flex: 1;
  margin-left: 16rpx;
}

.record-job {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.job-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F2329;
}

.record-status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-left: 10rpx;
}

.record-status-dot.normal {
  background-color: #52C41A;
}

.record-status-dot.abnormal {
  background-color: #FF4D4F;
}

.record-times {
  display: flex;
  align-items: center;
}

.time-block {
  display: flex;
  flex-direction: column;
}

.time-label {
  font-size: 20rpx;
  color: #C9CDD4;
  margin-bottom: 4rpx;
}

.time-value {
  font-size: 26rpx;
  color: #52C41A;
  font-weight: 500;
}

.time-value.abnormal {
  color: #FF4D4F;
}

.time-arrow {
  font-size: 24rpx;
  color: #C9CDD4;
  margin: 0 16rpx;
}

.record-actions {
  margin-left: 16rpx;
}

.appeal-btn {
  font-size: 24rpx;
  color: #165DFF;
  padding: 8rpx 20rpx;
  background-color: #E8F0FF;
  border-radius: 20rpx;
}

.appeal-status {
  font-size: 22rpx;
  color: #FAAD14;
}

.appeal-status.approved {
  color: #52C41A;
}

.appeal-status.rejected {
  color: #FF4D4F;
}

.record-normal {
  font-size: 22rpx;
  color: #52C41A;
}

.success-animation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.success-icon-wrap {
  width: 160rpx;
  height: 160rpx;
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  box-shadow: 0 12rpx 40rpx rgba(82, 196, 26, 0.4);
}

.success-icon {
  font-size: 80rpx;
  color: #FFFFFF;
  font-weight: bold;
}

.success-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 12rpx;
}

.success-time {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.appeal-modal, .distance-modal, .abnormal-reason-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 48rpx;
}

.appeal-content, .distance-content, .abnormal-content {
  width: 100%;
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.appeal-title, .distance-title, .abnormal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2329;
}

.modal-close {
  font-size: 32rpx;
  color: #C9CDD4;
}

.appeal-form {
  margin-bottom: 32rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label {
  font-size: 28rpx;
  color: #4E5969;
  margin-bottom: 16rpx;
  display: block;
}

.form-textarea, .abnormal-textarea {
  width: 100%;
  height: 200rpx;
  font-size: 28rpx;
  padding: 20rpx;
  background-color: #F7F8FA;
  border-radius: 16rpx;
  color: #1F2329;
  box-sizing: border-box;
  line-height: 1.6;
}

.input-placeholder {
  color: #C9CDD4;
}

.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.upload-item {
  width: 160rpx;
  height: 160rpx;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.upload-image {
  width: 100%;
  height: 100%;
}

.upload-remove {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 36rpx;
  height: 36rpx;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-icon {
  font-size: 28rpx;
  color: #FFFFFF;
}

.upload-btn {
  width: 160rpx;
  height: 160rpx;
  border: 2rpx dashed #D9D9D9;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #F7F8FA;
}

.upload-icon {
  font-size: 40rpx;
  color: #C9CDD4;
  margin-bottom: 4rpx;
}

.upload-text {
  font-size: 22rpx;
  color: #86909C;
}

.appeal-footer, .distance-footer, .abnormal-footer {
  display: flex;
  gap: 24rpx;
}

.cancel-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  color: #4E5969;
  background-color: #F2F3F5;
  border-radius: 24rpx;
  text-align: center;
}

.submit-btn, .confirm-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #FFFFFF;
  background: linear-gradient(90deg, #165DFF 0%, #4080FF 100%);
  border-radius: 24rpx;
  text-align: center;
}

.submit-btn.disabled, .confirm-btn.disabled {
  background: #C9CDD4;
}

.distance-content {
  text-align: center;
}

.distance-icon-wrap {
  width: 100rpx;
  height: 100rpx;
  background-color: #FFF7E6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24rpx;
}

.distance-icon {
  font-size: 48rpx;
}

.distance-desc {
  font-size: 28rpx;
  color: #4E5969;
  display: block;
  margin-bottom: 12rpx;
  line-height: 1.6;
}

.distance-sub {
  font-size: 24rpx;
  color: #FF4D4F;
  display: block;
  margin-bottom: 32rpx;
}
</style>
