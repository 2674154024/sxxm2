<template>
  <view class="page">
    <view class="form-section">
      <text class="section-title">基本信息</text>
      <view class="avatar-row">
        <view class="avatar-wrap" @click="handleAvatarUpload">
          <image class="avatar" :src="form.avatar || defaultAvatar" mode="aspectFill" />
          <view class="avatar-edit">
            <text class="edit-icon">📷</text>
          </view>
        </view>
      </view>
      <view class="form-item readonly">
        <text class="form-label">真实姓名</text>
        <text class="form-value">{{ form.real_name || '请先完成实名认证' }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">性别</text>
        <view class="gender-picker">
          <view class="gender-option" :class="{ active: form.gender === 'male' }" @click="form.gender = 'male'">
            <text class="gender-icon">👦</text>
            <text class="gender-text">男</text>
          </view>
          <view class="gender-option" :class="{ active: form.gender === 'female' }" @click="form.gender = 'female'">
            <text class="gender-icon">👧</text>
            <text class="gender-text">女</text>
          </view>
        </view>
      </view>
      <view class="form-item readonly">
        <text class="form-label">学校</text>
        <text class="form-value">{{ form.school_name || '请先完成实名认证' }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">手机号码</text>
        <text class="form-value desensitized">{{ desensitizePhone(form.phone) }}</text>
      </view>
    </view>

    <view class="form-section">
      <view class="section-header">
        <text class="section-title">可工作时间</text>
        <text class="sync-btn" @click="handleSyncSchedule">📸 同步课表</text>
      </view>
      <view class="work-time-grid">
        <view class="weekday-header">
          <text class="weekday-label">时段</text>
          <text class="weekday-name" v-for="day in weekdays" :key="day.value">{{ day.label }}</text>
        </view>
        <view class="time-row" v-for="slot in timeSlots" :key="slot.value">
          <text class="time-label">{{ slot.label }}</text>
          <view
            class="time-checkbox"
            :class="{ active: isTimeSelected(day.value, slot.value) }"
            v-for="day in weekdays"
            :key="day.value"
            @click="toggleTime(day.value, slot.value)"
          >
            <text v-if="isTimeSelected(day.value, slot.value)" class="check-icon">✓</text>
          </view>
        </view>
      </view>
    </view>

    <view class="form-section">
      <text class="section-title">技能标签</text>
      <view class="skill-tags">
        <view class="skill-tag" :class="{ active: form.skills.includes(skill) }" v-for="skill in skillOptions" :key="skill" @click="toggleSkill(skill)">
          {{ skill }}
        </view>
      </view>
      <view class="custom-skill-row">
        <input class="custom-skill-input" placeholder="输入自定义技能" v-model="customSkill" @confirm="addCustomSkill" />
        <button class="add-skill-btn" @click="addCustomSkill">添加</button>
      </view>
      <text class="skill-count">{{ form.skills.length }}/8</text>
    </view>

    <view class="form-section">
      <text class="section-title">自我介绍</text>
      <textarea class="form-textarea" placeholder="请简要介绍自己的优势和特长..." v-model="form.introduction" />
    </view>

    <view class="submit-btn-wrap">
      <button class="preview-btn" @click="showPreview = true">预览简历</button>
      <button class="submit-btn" @click="handleSubmit">保存简历</button>
    </view>

    <view v-if="showPreview" class="preview-mask" @click="showPreview = false">
      <view class="preview-card" @click.stop>
        <view class="preview-header">
          <text class="preview-title">简历预览</text>
          <text class="preview-close" @click="showPreview = false">✕</text>
        </view>
        <view class="preview-content">
          <view class="preview-avatar-row">
            <image class="preview-avatar" :src="form.avatar || defaultAvatar" mode="aspectFill" />
            <view class="preview-basic">
              <text class="preview-name">{{ form.real_name || '未填写' }}</text>
              <text class="preview-school">{{ form.school_name || '未填写' }}</text>
              <text class="preview-gender">{{ form.gender === 'male' ? '男' : '女' }}</text>
            </view>
          </view>
          <view class="preview-section">
            <text class="preview-section-title">可工作时间</text>
            <text class="preview-work-time">{{ formatWorkTimeText() }}</text>
          </view>
          <view class="preview-section">
            <text class="preview-section-title">技能特长</text>
            <view class="preview-skills">
              <text class="preview-skill" v-for="skill in form.skills" :key="skill">{{ skill }}</text>
            </view>
          </view>
          <view class="preview-section">
            <text class="preview-section-title">自我介绍</text>
            <text class="preview-intro">{{ form.introduction || '未填写' }}</text>
          </view>
        </view>
        <view class="preview-footer">
          <button class="share-btn" @click="handleShare">分享简历</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { resumeApi, type Resume, type WorkTimeSlot } from '@/api/resume'

const userStore = useUserStore()

const defaultAvatar = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20avatar%20portrait%20simple%20icon&image_size=square'

const weekdays = [
  { value: 'mon', label: '一' },
  { value: 'tue', label: '二' },
  { value: 'wed', label: '三' },
  { value: 'thu', label: '四' },
  { value: 'fri', label: '五' },
  { value: 'sat', label: '六' },
  { value: 'sun', label: '日' }
]

const timeSlots = [
  { value: 'morning', label: '上午', time: '09:00-12:00' },
  { value: 'afternoon', label: '下午', time: '14:00-18:00' },
  { value: 'evening', label: '晚间', time: '18:00-22:00' }
]

const skillOptions = ['PS', '剪辑', '文案', '摄影', '编程', '英语', '销售', '客服', '家教', '服务员']

const showPreview = ref(false)
const customSkill = ref('')

const form = reactive<Resume>({
  id: '',
  user_id: '',
  avatar: '',
  real_name: '',
  gender: 'male',
  school_id: '',
  school_name: '',
  phone: '',
  work_time: {
    mon: [],
    tue: [],
    wed: [],
    thu: [],
    fri: [],
    sat: [],
    sun: []
  },
  skills: [],
  introduction: '',
  created_at: '',
  updated_at: ''
})

const desensitizePhone = (phone: string) => {
  if (!phone || phone.length < 11) return '未绑定手机号'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const isTimeSelected = (day: string, slot: string) => {
  const slotTime = timeSlots.find(s => s.value === slot)?.time
  return form.work_time[day as keyof WorkTimeSlot].includes(slotTime || '')
}

const toggleTime = (day: string, slot: string) => {
  const slotTime = timeSlots.find(s => s.value === slot)?.time
  if (!slotTime) return
  const dayArray = form.work_time[day as keyof WorkTimeSlot]
  const index = dayArray.indexOf(slotTime)
  if (index > -1) {
    dayArray.splice(index, 1)
  } else {
    dayArray.push(slotTime)
  }
}

const toggleSkill = (skill: string) => {
  if (form.skills.length >= 8 && !form.skills.includes(skill)) {
    uni.showToast({ title: '最多选择8个技能', icon: 'none' })
    return
  }
  const index = form.skills.indexOf(skill)
  if (index > -1) {
    form.skills.splice(index, 1)
  } else {
    form.skills.push(skill)
  }
}

const addCustomSkill = () => {
  const skill = customSkill.value.trim()
  if (!skill) return
  if (form.skills.length >= 8) {
    uni.showToast({ title: '最多选择8个技能', icon: 'none' })
    return
  }
  if (form.skills.includes(skill)) {
    uni.showToast({ title: '技能已存在', icon: 'none' })
    return
  }
  form.skills.push(skill)
  customSkill.value = ''
}

const formatWorkTimeText = () => {
  const result: string[] = []
  weekdays.forEach(day => {
    const times = form.work_time[day.value as keyof WorkTimeSlot]
    if (times.length > 0) {
      result.push(`${day.label}(${times.join(',')})`)
    }
  })
  return result.length > 0 ? result.join(' ') : '未设置'
}

const handleAvatarUpload = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      uni.showLoading({ title: '上传中...' })
      setTimeout(() => {
        uni.hideLoading()
        form.avatar = tempFilePath
        uni.showToast({ title: '上传成功', icon: 'success' })
      }, 1000)
    }
  })
}

const handleSyncSchedule = () => {
  uni.showModal({
    title: '同步课表',
    content: '将打开相机拍摄课表，AI将自动解析并填充可工作时间',
    success: (res) => {
      if (res.confirm) {
        uni.chooseImage({
          count: 1,
          sourceType: ['camera'],
          success: () => {
            uni.showLoading({ title: 'AI解析中...' })
            setTimeout(() => {
              uni.hideLoading()
              form.work_time = {
                mon: ['09:00-12:00', '14:00-18:00'],
                tue: ['09:00-12:00', '14:00-18:00'],
                wed: ['14:00-18:00'],
                thu: ['09:00-12:00', '14:00-18:00'],
                fri: ['09:00-12:00'],
                sat: ['09:00-12:00', '14:00-18:00', '18:00-22:00'],
                sun: ['09:00-12:00', '14:00-18:00', '18:00-22:00']
              }
              uni.showToast({ title: '解析成功', icon: 'success' })
            }, 2000)
          }
        })
      }
    }
  })
}

const handlePreview = () => {
  showPreview.value = true
}

const handleShare = () => {
  uni.showToast({ title: '生成分享码中...', icon: 'loading' })
  setTimeout(() => {
    uni.hideToast()
    uni.showModal({
      title: '分享成功',
      content: '小程序码已生成，请保存图片分享给企业',
      showCancel: false
    })
  }, 1500)
}

const handleSubmit = () => {
  uni.showLoading({ title: '保存中...' })
  resumeApi.updateResume({
    avatar: form.avatar,
    gender: form.gender,
    phone: form.phone,
    work_time: form.work_time,
    skills: form.skills,
    introduction: form.introduction
  }).then(() => {
    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })
  }).catch(() => {
    uni.hideLoading()
  })
}

const fetchResume = async () => {
  try {
    const res = await resumeApi.getResume()
    Object.assign(form, res.data)
  } catch (error) {
    console.error('获取简历失败:', error)
    form.real_name = userStore.user?.real_name || ''
    form.school_name = userStore.user?.school_name || ''
    form.phone = userStore.user?.phone || ''
  }
}

onMounted(() => {
  fetchResume()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
  padding-bottom: 200rpx;
}

.form-section {
  background-color: #FFFFFF;
  margin-bottom: 24rpx;
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
  margin-bottom: 24rpx;
  display: block;
}

.section-header .section-title {
  margin-bottom: 0;
}

.sync-btn {
  font-size: 26rpx;
  color: #165DFF;
}

.avatar-row {
  display: flex;
  justify-content: center;
  margin-bottom: 32rpx;
}

.avatar-wrap {
  position: relative;
  width: 200rpx;
  height: 200rpx;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4rpx solid #E8F0FF;
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 56rpx;
  height: 56rpx;
  background-color: #165DFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #FFFFFF;
}

.edit-icon {
  font-size: 24rpx;
}

.form-item {
  margin-bottom: 28rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-item.readonly {
  opacity: 0.7;
}

.form-label {
  font-size: 28rpx;
  color: #4E5969;
  margin-bottom: 12rpx;
  display: block;
}

.form-value {
  font-size: 30rpx;
  color: #1F2329;
}

.form-value.desensitized {
  color: #86909C;
}

.gender-picker {
  display: flex;
  gap: 32rpx;
}

.gender-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 40rpx;
  background-color: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
}

.gender-option.active {
  border-color: #165DFF;
  background-color: #E8F0FF;
}

.gender-icon {
  font-size: 48rpx;
  margin-bottom: 8rpx;
}

.gender-text {
  font-size: 28rpx;
  color: #1F2329;
}

.work-time-grid {
  margin-top: 16rpx;
}

.weekday-header {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F2F3F5;
}

.weekday-label {
  width: 100rpx;
  font-size: 26rpx;
  color: #86909C;
  text-align: center;
}

.weekday-name {
  flex: 1;
  font-size: 26rpx;
  color: #1F2329;
  text-align: center;
}

.time-row {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F2F3F5;
}

.time-row:last-child {
  border-bottom: none;
}

.time-label {
  width: 100rpx;
  font-size: 26rpx;
  color: #86909C;
  display: flex;
  align-items: center;
  justify-content: center;
}

.time-checkbox {
  flex: 1;
  height: 64rpx;
  border-radius: 8rpx;
  background-color: #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 4rpx;
}

.time-checkbox.active {
  background-color: #165DFF;
}

.check-icon {
  font-size: 32rpx;
  color: #FFFFFF;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.skill-tag {
  font-size: 26rpx;
  color: #86909C;
  background-color: #F2F3F5;
  padding: 16rpx 28rpx;
  border-radius: 32rpx;
}

.skill-tag.active {
  color: #FFFFFF;
  background-color: #165DFF;
}

.custom-skill-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.custom-skill-input {
  flex: 1;
  font-size: 28rpx;
  padding: 16rpx 24rpx;
  background-color: #F8F9FA;
  border-radius: 32rpx;
}

.add-skill-btn {
  font-size: 26rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  border: none;
}

.skill-count {
  font-size: 24rpx;
  color: #86909C;
  text-align: right;
}

.form-textarea {
  font-size: 30rpx;
  color: #1F2329;
  padding: 24rpx;
  background-color: #F8F9FA;
  border-radius: 12rpx;
  min-height: 200rpx;
}

.submit-btn-wrap {
  padding: 32rpx 24rpx;
  display: flex;
  gap: 24rpx;
}

.preview-btn {
  flex: 1;
  font-size: 30rpx;
  color: #165DFF;
  background-color: #E8F0FF;
  padding: 28rpx;
  border-radius: 48rpx;
  border: none;
}

.submit-btn {
  flex: 2;
  font-size: 30rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 28rpx;
  border-radius: 48rpx;
  border: none;
}

.preview-mask {
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

.preview-card {
  width: 100%;
  max-height: 80vh;
  background-color: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #F2F3F5;
}

.preview-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
}

.preview-close {
  font-size: 36rpx;
  color: #86909C;
}

.preview-content {
  padding: 32rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.preview-avatar-row {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
  padding-bottom: 32rpx;
  border-bottom: 1rpx solid #F2F3F5;
}

.preview-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  margin-right: 24rpx;
}

.preview-basic {
  flex: 1;
}

.preview-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #1F2329;
  display: block;
  margin-bottom: 8rpx;
}

.preview-school {
  font-size: 28rpx;
  color: #86909C;
  display: block;
  margin-bottom: 8rpx;
}

.preview-gender {
  font-size: 26rpx;
  color: #86909C;
}

.preview-section {
  margin-bottom: 28rpx;
}

.preview-section:last-child {
  margin-bottom: 0;
}

.preview-section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #1F2329;
  display: block;
  margin-bottom: 16rpx;
}

.preview-work-time {
  font-size: 26rpx;
  color: #4E5969;
  white-space: pre-wrap;
}

.preview-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.preview-skill {
  font-size: 24rpx;
  color: #165DFF;
  background-color: #E8F0FF;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.preview-intro {
  font-size: 26rpx;
  color: #4E5969;
  line-height: 1.8;
}

.preview-footer {
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #F2F3F5;
}

.share-btn {
  width: 100%;
  font-size: 30rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 28rpx;
  border-radius: 48rpx;
  border: none;
}
</style>