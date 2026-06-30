<template>
  <view class="page">
    <view class="clock-header">
      <view class="date-info">
        <text class="date-text">{{ currentDate }}</text>
        <text class="week-text">{{ currentWeek }}</text>
      </view>
      <view class="status-badge" :class="clockStatus">
        <text class="status-text">{{ clockStatusText }}</text>
      </view>
    </view>

    <view class="location-info">
      <text class="location-icon">📍</text>
      <text class="location-text">{{ currentLocation }}</text>
      <text class="location-update" @click="getLocation">更新位置</text>
    </view>

    <view class="clock-card" :class="{ abnormal: isAbnormal }">
      <view class="clock-circle" :class="clockState" @click="handleClock">
        <view class="circle-inner">
          <text class="clock-icon">{{ clockState === 'clocked-out' ? '🚀' : '✓' }}</text>
          <text class="clock-text">{{ clockState === 'clocked-out' ? '上班打卡' : '下班打卡' }}</text>
          <text class="clock-subtext">{{ clockState === 'clocked-out' ? '点击开始工作' : '点击结束工作' }}</text>
        </view>
      </view>
      <view class="clock-time" v-if="clockState === 'clocked-in'">
        <text class="time-label">当前工作时长</text>
        <text class="time-value">{{ workDuration }}</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">今日任务</text>
      <view class="task-list">
        <view class="task-item" v-for="task in todayTasks" :key="task.id">
          <view class="task-checkbox" :class="{ checked: task.completed }" @click="toggleTask(task)">
            <text class="checkbox-icon" v-if="task.completed">✓</text>
          </view>
          <view class="task-content">
            <text class="task-name">{{ task.name }}</text>
            <text class="task-time">{{ task.time }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">打卡记录</text>
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
              <text class="time-item" :class="{ abnormal: !record.clock_in_time }">
                {{ record.clock_in_time || '未打卡' }}
              </text>
              <text class="time-arrow">→</text>
              <text class="time-item" :class="{ abnormal: !record.clock_out_time }">
                {{ record.clock_out_time || '未打卡' }}
              </text>
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
          </view>
        </view>
      </view>
    </view>

    <view class="appeal-modal" v-if="showAppeal">
      <view class="appeal-content">
        <text class="appeal-title">异常打卡申诉</text>
        <view class="appeal-form">
          <view class="form-item">
            <text class="form-label">申诉原因</text>
            <textarea 
              class="form-textarea" 
              placeholder="请输入申诉原因..." 
              v-model="appealReason"
              :maxlength="500"
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
                <image class="upload-image" :src="image" mode="widthFix" />
                <text class="upload-remove" @click="removeImage(index)">×</text>
              </view>
              <view 
                class="upload-btn" 
                v-if="appealImages.length < 3"
                @click="chooseImage"
              >
                <text class="upload-icon">+</text>
              </view>
            </view>
          </view>
        </view>
        <view class="appeal-footer">
          <button class="cancel-btn" @click="closeAppeal">取消</button>
          <button 
            class="submit-btn" 
            :class="{ disabled: !appealReason.trim() }"
            @click="submitAppeal"
          >提交申诉</button>
        </view>
      </view>
    </view>

    <view class="distance-modal" v-if="showDistanceModal">
      <view class="distance-content">
        <text class="distance-icon">⚠️</text>
        <text class="distance-title">位置较远提醒</text>
        <text class="distance-desc">当前位置距岗位地址较远（{{ distanceToJob }}米），是否继续打卡？</text>
        <text class="distance-sub">继续打卡将标记为异常打卡，需填写说明</text>
        <view class="distance-footer">
          <button class="cancel-btn" @click="showDistanceModal = false">取消</button>
          <button class="confirm-btn" @click="confirmAbnormalClock">继续打卡</button>
        </view>
      </view>
    </view>

    <view class="abnormal-reason-modal" v-if="showAbnormalReason">
      <view class="abnormal-content">
        <text class="abnormal-title">填写异常说明</text>
        <textarea 
          class="abnormal-textarea" 
          placeholder="请输入异常原因（如交通堵塞、临时外出等）..." 
          v-model="abnormalReason"
          :maxlength="200"
        />
        <view class="abnormal-footer">
          <button class="cancel-btn" @click="showAbnormalReason = false">取消</button>
          <button 
            class="confirm-btn" 
            :class="{ disabled: !abnormalReason.trim() }"
            @click="submitAbnormalClock"
          >确认提交</button>
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

const todayTasks = ref([
  { id: 1, name: '完成初中数学辅导', time: '09:00-12:00', completed: true },
  { id: 2, name: '批改学生作业', time: '14:00-16:00', completed: false },
  { id: 3, name: '准备明天课程', time: '16:00-17:00', completed: false }
])

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

const doClockIn = async (isAbnormal: boolean) => {
  uni.showLoading({ title: '打卡中...' })
  
  try {
    await clockApi.clock({
      job_id: '1',
      clock_type: 'clock_in',
      latitude: currentLatitude.value,
      longitude: currentLongitude.value,
      address: currentLocation.value,
      abnormal_reason: isAbnormal ? abnormalReason.value : undefined
    })
    
    uni.hideLoading()
    clockState.value = 'clocked-in'
    clockInTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    isAbnormal.value = isAbnormal
    
    uni.showToast({ title: '签到成功', icon: 'success' })
    
    workTimer = setInterval(updateWorkDuration, 60000)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '打卡失败', icon: 'none' })
  }
}

const doClockOut = async (isAbnormal: boolean) => {
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
            abnormal_reason: isAbnormal ? abnormalReason.value : undefined
          })
          
          uni.hideLoading()
          clockState.value = 'clocked-out'
          clockInTime.value = ''
          workDuration.value = '0小时0分钟'
          isAbnormal.value = false
          
          if (workTimer) {
            clearInterval(workTimer)
            workTimer = null
          }
          
          uni.showToast({ title: '下班打卡成功', icon: 'success' })
          loadClockRecords()
        } catch (error) {
          uni.hideLoading()
          uni.showToast({ title: '打卡失败', icon: 'none' })
        }
      }
    }
  })
}

const toggleTask = (task: { id: number; completed: boolean }) => {
  task.completed = !task.completed
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
}

.clock-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #FFFFFF;
  padding: 32rpx;
}

.date-info {
  display: flex;
  flex-direction: column;
}

.date-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
}

.week-text {
  font-size: 26rpx;
  color: #86909C;
  margin-top: 8rpx;
}

.status-badge {
  font-size: 24rpx;
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
}

.status-badge.not-clock {
  background-color: #FFF2F0;
  color: #FF4D4F;
}

.status-badge.clocked-in {
  background-color: #F6FFED;
  color: #52C41A;
}

.location-info {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  padding: 24rpx 32rpx;
  margin-top: 20rpx;
}

.location-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.location-text {
  flex: 1;
  font-size: 26rpx;
  color: #4E5969;
}

.location-update {
  font-size: 26rpx;
  color: #165DFF;
}

.clock-card {
  margin: 24rpx;
  background-color: #FFFFFF;
  border-radius: 24rpx;
  padding: 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.clock-card.abnormal {
  border: 2rpx solid #FF4D4F;
}

.clock-circle {
  width: 320rpx;
  height: 320rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.clock-circle.clocked-out {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.clock-circle.clocked-in {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.clock-circle:active {
  transform: scale(0.95);
}

.circle-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.clock-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.clock-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #FFFFFF;
  margin-bottom: 8rpx;
}

.clock-subtext {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.clock-time {
  margin-top: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time-label {
  font-size: 26rpx;
  color: #86909C;
  margin-bottom: 8rpx;
}

.time-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #165DFF;
}

.section {
  background-color: #FFFFFF;
  margin-bottom: 24rpx;
  padding: 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 24rpx;
  display: block;
}

.task-list {
  padding: 0;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F2F3F5;
}

.task-item:last-child {
  border-bottom: none;
}

.task-checkbox {
  width: 48rpx;
  height: 48rpx;
  border: 2rpx solid #C9CDD4;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.task-checkbox.checked {
  background-color: #165DFF;
  border-color: #165DFF;
}

.checkbox-icon {
  font-size: 24rpx;
  color: #FFFFFF;
}

.task-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-name {
  font-size: 28rpx;
  color: #1F2329;
}

.task-time {
  font-size: 24rpx;
  color: #86909C;
}

.record-list {
  padding: 0;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F2F3F5;
}

.record-item:last-child {
  border-bottom: none;
}

.record-date {
  width: 100rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.date-num {
  font-size: 36rpx;
  font-weight: bold;
  color: #1F2329;
}

.date-week {
  font-size: 24rpx;
  color: #86909C;
}

.record-info {
  flex: 1;
  margin-left: 24rpx;
}

.record-job {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.job-name {
  font-size: 28rpx;
  color: #1F2329;
}

.record-status-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-left: 12rpx;
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

.time-item {
  font-size: 26rpx;
  color: #52C41A;
}

.time-item.abnormal {
  color: #FF4D4F;
}

.time-arrow {
  font-size: 24rpx;
  color: #C9CDD4;
  margin: 0 16rpx;
}

.record-actions {
  margin-left: 24rpx;
}

.appeal-btn {
  font-size: 26rpx;
  color: #165DFF;
}

.appeal-status {
  font-size: 24rpx;
  color: #FAAD14;
}

.appeal-status.approved {
  color: #52C41A;
}

.appeal-status.rejected {
  color: #FF4D4F;
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
}

.appeal-content, .distance-content, .abnormal-content {
  width: 680rpx;
  background-color: #FFFFFF;
  border-radius: 24rpx;
  padding: 40rpx;
}

.appeal-title, .distance-title, .abnormal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 24rpx;
  display: block;
}

.appeal-form {
  margin-bottom: 32rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label, .abnormal-label {
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
  background-color: #F2F3F5;
  border-radius: 12rpx;
}

.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.upload-item {
  width: 180rpx;
  height: 180rpx;
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
  width: 40rpx;
  height: 40rpx;
  background-color: rgba(0, 0, 0, 0.5);
  color: #FFFFFF;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.upload-btn {
  width: 180rpx;
  height: 180rpx;
  border: 2rpx dashed #C9CDD4;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 48rpx;
  color: #C9CDD4;
}

.appeal-footer, .distance-footer, .abnormal-footer {
  display: flex;
  gap: 24rpx;
}

.cancel-btn {
  flex: 1;
  font-size: 32rpx;
  color: #86909C;
  background-color: #F2F3F5;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
}

.submit-btn, .confirm-btn {
  flex: 1;
  font-size: 32rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
}

.submit-btn.disabled, .confirm-btn.disabled {
  background-color: #C9CDD4;
}

.distance-icon {
  font-size: 80rpx;
  display: block;
  text-align: center;
  margin-bottom: 24rpx;
}

.distance-desc, .distance-sub {
  font-size: 28rpx;
  color: #4E5969;
  display: block;
  text-align: center;
  margin-bottom: 16rpx;
}

.distance-sub {
  color: #FF4D4F;
  font-size: 26rpx;
}
</style>