<template>
  <view class="page">
    <view v-if="showRequiredTip" class="required-tip" @click="showRequiredTip = false">
      <view class="tip-header">
        <text class="tip-icon">⚠️</text>
        <text class="tip-title">请完善必填信息</text>
      </view>
      <view class="tip-content">
        <view class="tip-item" v-for="(item, index) in requiredFields" :key="index">
          <text class="tip-dot">•</text>
          <text class="tip-text">{{ item }}</text>
        </view>
        <view v-if="requiredFields.length === 0" class="tip-item">
          <text class="tip-text">✓ 所有必填信息已完善</text>
        </view>
      </view>
      <view class="tip-close" @click="showRequiredTip = false">
        <text class="close-icon">✕</text>
      </view>
    </view>

    <view class="form-section">
      <view class="section-header">
        <view class="section-icon">👤</view>
        <text class="section-title">基本信息</text>
        <view class="tip-btn" @click="showRequiredTip = true">
          <text class="tip-btn-icon">❓</text>
        </view>
      </view>
      <view class="avatar-row">
        <view class="avatar-wrap" @click="handleAvatarUpload">
          <image class="avatar" :src="form.avatar || defaultAvatar" mode="aspectFill" />
          <view class="avatar-edit">
            <text class="edit-icon">📷</text>
          </view>
        </view>
        <view class="avatar-tip">点击更换头像</view>
      </view>
      <view class="info-list">
        <view class="info-item">
          <text class="info-label">
            <text class="required-mark">*</text>真实姓名
          </text>
          <input class="info-input" placeholder="请输入真实姓名" v-model="form.realName" placeholder-class="input-placeholder" />
        </view>
        <view class="info-item">
          <text class="info-label">性别</text>
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
        <view class="info-item">
          <text class="info-label">学校</text>
          <text class="info-value">{{ form.schoolName || '请先完成实名认证' }}</text>
          <text class="info-arrow">›</text>
        </view>
        <view class="info-item">
          <text class="info-label">专业</text>
          <input class="info-input" placeholder="请输入专业" v-model="form.major" placeholder-class="input-placeholder" />
        </view>
        <view class="info-item">
          <text class="info-label">年级</text>
          <input class="info-input" placeholder="请输入年级" v-model="form.grade" placeholder-class="input-placeholder" />
        </view>
        <view class="info-item">
          <text class="info-label">
            <text class="required-mark">*</text>手机号码
          </text>
          <input class="info-input" type="number" placeholder="请输入手机号码" v-model="form.phone" placeholder-class="input-placeholder" maxlength="11" @input="onPhoneInput" />
        </view>
      </view>
    </view>

    <view class="form-section">
      <view class="section-header">
        <view class="section-icon">🕐</view>
        <text class="section-title">可工作时间</text>
        <view class="add-btn" @click="addWorkTime">
          <text class="add-icon">+</text>
        </view>
      </view>
      <view class="experience-list">
        <view class="experience-item" v-for="(time, index) in workTimeList" :key="index">
          <view class="exp-header">
            <view class="exp-dot time"></view>
            <text class="exp-school">{{ time.day || '选择日期' }} {{ time.time || '' }}</text>
            <view class="exp-actions">
              <text class="exp-delete" @click="deleteWorkTime(index)">删除</text>
            </view>
          </view>
          <view class="exp-body">
            <view class="exp-row">
              <text class="exp-label">日期</text>
              <view class="exp-day-picker">
                <view 
                  class="day-option" 
                  :class="{ active: time.day === day.name }" 
                  v-for="day in weekDays" 
                  :key="day.key"
                  @click="time.day = day.name"
                >
                  {{ day.name }}
                </view>
              </view>
            </view>
            <view class="exp-row">
              <text class="exp-label">时段</text>
              <input class="exp-input" placeholder="如：9:00-12:00" v-model="time.time" placeholder-class="input-placeholder" />
            </view>
          </view>
        </view>
        <view class="add-experience" @click="addWorkTime" v-if="workTimeList.length === 0">
          <text class="add-exp-icon">+</text>
          <text class="add-exp-text">添加可工作时间</text>
        </view>
      </view>
      <text class="work-time-tip">示例：周一 9:00-12:00，可添加多个时间段</text>
    </view>

    <view class="form-section">
      <view class="section-header">
        <view class="section-icon">💼</view>
        <text class="section-title">兼职经历</text>
        <view class="add-btn" @click="addWork">
          <text class="add-icon">+</text>
        </view>
      </view>
      <view class="experience-list">
        <view class="experience-item" v-for="(work, index) in workList" :key="index">
          <view class="exp-header">
            <view class="exp-dot work"></view>
            <text class="exp-school">{{ work.company || '新增经历' }}</text>
            <view class="exp-actions">
              <text class="exp-edit" @click="editWork(index)">编辑</text>
              <text class="exp-delete" @click="deleteWork(index)">删除</text>
            </view>
          </view>
          <view class="exp-body">
            <view class="exp-row">
              <text class="exp-label">公司</text>
              <input class="exp-input" placeholder="请输入公司名称" v-model="work.company" placeholder-class="input-placeholder" />
            </view>
            <view class="exp-row">
              <text class="exp-label">职位</text>
              <input class="exp-input" placeholder="请输入职位名称" v-model="work.position" placeholder-class="input-placeholder" />
            </view>
            <view class="exp-row">
              <text class="exp-label">时间</text>
              <input class="exp-input" placeholder="如：2025.06 - 2025.09" v-model="work.time" placeholder-class="input-placeholder" />
            </view>
            <view class="exp-row">
              <text class="exp-label">描述</text>
              <textarea class="exp-textarea" placeholder="请描述工作内容和收获" v-model="work.description" placeholder-class="input-placeholder" />
            </view>
          </view>
        </view>
        <view class="add-experience" @click="addWork" v-if="workList.length === 0">
          <text class="exp-exp-icon">+</text>
          <text class="add-exp-text">添加兼职经历</text>
        </view>
      </view>
    </view>

    <view class="form-section">
      <view class="section-header">
        <view class="section-icon">🏷️</view>
        <text class="section-title">技能标签</text>
      </view>
      <view class="skill-tags">
        <view class="skill-tag" :class="{ active: form.skillTags.includes(skill) }" v-for="skill in skillOptions" :key="skill" @click="toggleSkill(skill)">
          {{ skill }}
        </view>
      </view>
      <view class="custom-skill-row">
        <input class="custom-skill-input" placeholder="输入自定义技能" v-model="customSkill" @confirm="addCustomSkill" placeholder-class="input-placeholder" />
        <view class="add-skill-btn" @click="addCustomSkill">添加</view>
      </view>
      <text class="skill-count">{{ skillList.length }}/8</text>
    </view>

    <view class="form-section">
      <view class="section-header">
        <view class="section-icon">📝</view>
        <text class="section-title">自我介绍</text>
      </view>
      <textarea class="form-textarea" placeholder="请简要介绍自己的优势和特长..." v-model="form.selfIntroduction" placeholder-class="input-placeholder" />
      <text class="textarea-count">{{ (form.selfIntroduction || '').length }}/500</text>
    </view>

    <view class="submit-btn-wrap">
      <button class="submit-btn" @click="handleSubmit">保存简历</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { resumeApi, type Resume } from '@/api/resume'

const userStore = useUserStore()

const defaultAvatar = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20avatar%20portrait%20simple%20icon&image_size=square'

const skillOptions = ['PS', '剪辑', '文案', '摄影', '编程', '英语', '销售', '客服', '家教', '服务员']

const weekDays = [
  { key: 'mon', name: '周一' },
  { key: 'tue', name: '周二' },
  { key: 'wed', name: '周三' },
  { key: 'thu', name: '周四' },
  { key: 'fri', name: '周五' },
  { key: 'sat', name: '周六' },
  { key: 'sun', name: '周日' }
]

const showRequiredTip = ref(false)
const customSkill = ref('')

const form = reactive<any>({
  userId: '',
  avatar: '',
  realName: '',
  gender: 'male',
  schoolName: '',
  phone: '',
  availableTime: '',
  skillTags: '',
  workExperience: '',
  selfIntroduction: '',
  major: '',
  grade: '',
  creditScore: 0
})

const workTimeList = ref<Array<{ day: string; time: string }>>([])
const workList = ref<Array<{ company: string; position: string; time: string; description: string }>>([])

const skillList = computed(() => {
  return form.skillTags ? form.skillTags.split(',') : []
})

const requiredFields = computed(() => {
  const fields: string[] = []
  if (!form.realName || typeof form.realName !== 'string' || form.realName.trim() === '') {
    fields.push('• 真实姓名')
  }
  if (!form.phone || typeof form.phone !== 'string' || form.phone.trim() === '' || form.phone.length !== 11) {
    fields.push('• 手机号码')
  }
  if (workTimeList.value.length === 0) {
    fields.push('• 可工作时间')
  }
  return fields
})

const addWork = () => {
  workList.value.push({ company: '', position: '', time: '', description: '' })
}

const deleteWork = (index: number) => {
  workList.value.splice(index, 1)
}

const editWork = (index: number) => {
  uni.showToast({ title: '编辑兼职经历', icon: 'none' })
}

const addWorkTime = () => {
  workTimeList.value.push({ day: '', time: '' })
}

const deleteWorkTime = (index: number) => {
  workTimeList.value.splice(index, 1)
}

const onPhoneInput = (e: any) => {
  form.phone = e.detail.value.replace(/\D/g, '').slice(0, 11)
}

const toggleSkill = (skill: string) => {
  const skills = skillList.value
  if (skills.length >= 8 && !skills.includes(skill)) {
    uni.showToast({ title: '最多选择8个技能', icon: 'none' })
    return
  }
  const index = skills.indexOf(skill)
  if (index > -1) {
    skills.splice(index, 1)
  } else {
    skills.push(skill)
  }
  form.skillTags = skills.join(',')
}

const addCustomSkill = () => {
  const skill = customSkill.value.trim()
  if (!skill) return
  if (skillList.value.length >= 8) {
    uni.showToast({ title: '最多选择8个技能', icon: 'none' })
    return
  }
  if (skillList.value.includes(skill)) {
    uni.showToast({ title: '技能已存在', icon: 'none' })
    return
  }
  const skills = [...skillList.value, skill]
  form.skillTags = skills.join(',')
  customSkill.value = ''
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

const validateForm = () => {
  if (requiredFields.value.length > 0) {
    showRequiredTip.value = true
    return false
  }
  
  for (let i = 0; i < workTimeList.value.length; i++) {
    const time = workTimeList.value[i]
    if (!time.day) {
      uni.showToast({ title: '请选择工作时间的日期', icon: 'none' })
      return false
    }
    if (!time.time || time.time.trim() === '') {
      uni.showToast({ title: '请填写工作时间的时段', icon: 'none' })
      return false
    }
  }
  
  return true
}

const handleSubmit = () => {
  if (!validateForm()) {
    return
  }

  const availableTimeStr = JSON.stringify(workTimeList.value)
  
  uni.showLoading({ title: '保存中...' })
  resumeApi.updateResume({
    avatar: form.avatar,
    realName: form.realName,
    gender: form.gender,
    phone: form.phone,
    availableTime: availableTimeStr,
    skillTags: form.skillTags,
    workExperience: JSON.stringify(workList.value),
    selfIntroduction: form.selfIntroduction,
    major: form.major,
    grade: form.grade
  }).then(() => {
    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })
  }).catch(() => {
    uni.hideLoading()
    uni.showToast({ title: '保存失败', icon: 'none' })
  })
}

const fetchResume = async () => {
  try {
    const res = await resumeApi.getResume()
    const data = res.data || {}
    
    form.userId = data.userId || ''
    form.avatar = data.avatar || ''
    form.realName = data.realName || ''
    form.gender = data.gender || 'male'
    form.schoolName = data.schoolName || ''
    form.phone = data.phone || ''
    form.availableTime = data.availableTime || ''
    form.skillTags = data.skillTags || ''
    form.workExperience = data.workExperience || ''
    form.selfIntroduction = data.selfIntroduction || ''
    form.major = data.major || ''
    form.grade = data.grade || ''
    form.creditScore = data.creditScore || 0
    
    if (data.availableTime) {
      try {
        const parsed = JSON.parse(data.availableTime)
        if (Array.isArray(parsed)) {
          workTimeList.value = parsed
        } else {
          for (const [key, slots] of Object.entries(parsed)) {
            if (Array.isArray(slots) && slots.length > 0) {
              const dayName = weekDays.find(d => d.key === key)?.name || ''
              for (const slot of slots) {
                if (slot) {
                  workTimeList.value.push({ day: dayName, time: slot })
                }
              }
            }
          }
        }
      } catch (e) {
        console.error('解析工作时间失败:', e)
      }
    }
    
    if (data.workExperience) {
      try {
        const parsedWork = JSON.parse(data.workExperience)
        if (Array.isArray(parsedWork)) {
          workList.value = parsedWork
        }
      } catch (e) {
        console.error('解析兼职经历失败:', e)
      }
    }
  } catch (error) {
    console.error('获取简历失败:', error)
    form.realName = userStore.user?.real_name || ''
    form.schoolName = userStore.user?.school_name || ''
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

.required-tip {
  position: fixed;
  top: 200rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 600rpx;
  background-color: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  z-index: 1000;
  padding: 32rpx;
}

.tip-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.tip-icon {
  font-size: 40rpx;
  margin-right: 12rpx;
}

.tip-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2329;
}

.tip-content {
  padding-left: 16rpx;
}

.tip-item {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.tip-dot {
  color: #FF4D4F;
  font-size: 28rpx;
  margin-right: 12rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #4E5969;
}

.tip-close {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  font-size: 32rpx;
  color: #86909C;
}

.form-section {
  background-color: #FFFFFF;
  margin-bottom: 24rpx;
  padding: 32rpx;
  border-radius: 16rpx;
  margin: 24rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
  position: relative;
}

.section-icon {
  width: 64rpx;
  height: 64rpx;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  margin-right: 16rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F2329;
  flex: 1;
}

.tip-btn {
  width: 48rpx;
  height: 48rpx;
  background-color: #FFF7E6;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.tip-btn-icon {
  font-size: 28rpx;
  color: #FF7D00;
}

.add-btn {
  width: 48rpx;
  height: 48rpx;
  background-color: #E8F0FF;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-icon {
  font-size: 32rpx;
  color: #165DFF;
  font-weight: 600;
}

.avatar-row {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32rpx;
  padding: 24rpx 0;
  background-color: #F7F8FA;
  border-radius: 16rpx;
}

.avatar-wrap {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  margin-bottom: 16rpx;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 6rpx solid #FFFFFF;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 56rpx;
  height: 56rpx;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #FFFFFF;
}

.edit-icon {
  font-size: 24rpx;
}

.avatar-tip {
  font-size: 24rpx;
  color: #86909C;
}

.info-list {
  background-color: #F7F8FA;
  border-radius: 16rpx;
  overflow: hidden;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  min-height: 88rpx;
  border-bottom: 1rpx solid #E5E6EB;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 28rpx;
  color: #4E5969;
  width: 140rpx;
  flex-shrink: 0;
}

.required-mark {
  color: #FF4D4F;
  margin-right: 4rpx;
}

.info-value {
  flex: 1;
  font-size: 28rpx;
  color: #1F2329;
  text-align: right;
}

.info-value.desensitized {
  color: #86909C;
}

.info-input {
  flex: 1;
  height: 88rpx;
  font-size: 28rpx;
  color: #1F2329;
  text-align: right;
}

.input-placeholder {
  color: #C9CDD4;
}

.info-arrow {
  font-size: 32rpx;
  color: #C9CDD4;
  margin-left: 8rpx;
}

.gender-picker {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.gender-option {
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx;
  background-color: #FFFFFF;
  border-radius: 32rpx;
  border: 2rpx solid #E5E6EB;
}

.gender-option.active {
  background-color: #E8F0FF;
  border-color: #165DFF;
}

.gender-icon {
  font-size: 32rpx;
  margin-right: 8rpx;
}

.gender-text {
  font-size: 26rpx;
  color: #1F2329;
}

.experience-list {
  margin-top: 8rpx;
}

.experience-item {
  background-color: #F7F8FA;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.exp-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background-color: #FFFFFF;
  border-bottom: 1rpx solid #E5E6EB;
}

.exp-dot {
  width: 16rpx;
  height: 16rpx;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border-radius: 50%;
  margin-right: 12rpx;
}

.exp-dot.work {
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
}

.exp-dot.time {
  background: linear-gradient(135deg, #00B42A 0%, #52C41A 100%);
}

.exp-school {
  flex: 1;
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2329;
}

.exp-actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.exp-edit {
  font-size: 24rpx;
  color: #165DFF;
}

.exp-delete {
  font-size: 24rpx;
  color: #FF4D4F;
}

.exp-body {
  padding: 16rpx 24rpx 24rpx;
}

.exp-row {
  display: flex;
  align-items: flex-start;
  padding: 12rpx 0;
}

.exp-label {
  font-size: 26rpx;
  color: #86909C;
  width: 100rpx;
  flex-shrink: 0;
  padding-top: 8rpx;
}

.exp-input {
  flex: 1;
  height: 56rpx;
  font-size: 26rpx;
  color: #1F2329;
  background-color: #FFFFFF;
  border-radius: 12rpx;
  padding: 0 16rpx;
}

.exp-day-picker {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.day-option {
  font-size: 24rpx;
  color: #4E5969;
  background-color: #FFFFFF;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  border: 2rpx solid #E5E6EB;
}

.day-option.active {
  color: #FFFFFF;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border-color: transparent;
}

.exp-textarea {
  flex: 1;
  min-height: 120rpx;
  font-size: 26rpx;
  color: #1F2329;
  background-color: #FFFFFF;
  border-radius: 12rpx;
  padding: 16rpx;
  line-height: 1.6;
}

.add-experience {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
  background-color: #F7F8FA;
  border-radius: 16rpx;
  border: 2rpx dashed #E5E6EB;
}

.add-exp-icon {
  font-size: 48rpx;
  color: #C9CDD4;
  margin-bottom: 12rpx;
}

.add-exp-text {
  font-size: 26rpx;
  color: #86909C;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.skill-tag {
  font-size: 26rpx;
  color: #4E5969;
  background-color: #F7F8FA;
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  border: 2rpx solid #E5E6EB;
  transition: all 0.3s;
}

.skill-tag.active {
  color: #FFFFFF;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border-color: transparent;
  box-shadow: 0 4rpx 12rpx rgba(22, 93, 255, 0.3);
}

.custom-skill-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.custom-skill-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
  padding: 0 24rpx;
  background-color: #F7F8FA;
  border-radius: 36rpx;
  color: #1F2329;
}

.add-skill-btn {
  height: 72rpx;
  padding: 0 32rpx;
  font-size: 28rpx;
  color: #FFFFFF;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.skill-count {
  font-size: 24rpx;
  color: #86909C;
  text-align: right;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  font-size: 28rpx;
  color: #1F2329;
  padding: 24rpx;
  background-color: #F7F8FA;
  border-radius: 16rpx;
  line-height: 1.6;
  box-sizing: border-box;
}

.textarea-count {
  display: block;
  text-align: right;
  font-size: 22rpx;
  color: #C9CDD4;
  margin-top: 12rpx;
}

.work-time-tip {
  font-size: 22rpx;
  color: #C9CDD4;
  margin-top: 16rpx;
  display: block;
}

.submit-btn-wrap {
  padding: 32rpx 24rpx;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #FFFFFF;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
  background: linear-gradient(90deg, #165DFF 0%, #4080FF 100%);
  border-radius: 24rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(22, 93, 255, 0.3);
}

.submit-btn::after {
  border: none;
}
</style>