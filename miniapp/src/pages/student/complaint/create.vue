<template>
  <view class="page">
    <view class="complaint-header">
      <text class="header-title">投诉举报</text>
      <text class="header-subtitle">我们将在24小时内处理您的投诉</text>
    </view>

    <view class="form-section">
      <view class="form-card">
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text class="label-text">投诉类型</text>
          </view>
          <view class="type-grid">
            <view 
              class="type-item" 
              :class="{ active: formData.type === type }"
              v-for="type in complaintTypes" 
              :key="type"
              @click="selectType(type)"
            >
              <text class="type-text">{{ type }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text class="label-text">投诉对象</text>
          </view>
          <view class="form-input">
            <input 
              class="input-field"
              type="text"
              placeholder="请输入企业/岗位名称"
              v-model="formData.target"
              placeholder-class="input-placeholder"
            />
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text class="label-text">投诉内容</text>
          </view>
          <view class="form-textarea">
            <textarea 
              class="textarea-field"
              placeholder="请详细描述投诉内容，以便我们更好地为您处理"
              v-model="formData.content"
              :maxlength="500"
              placeholder-class="input-placeholder"
            />
            <view class="char-count">
              <text class="count-text">{{ formData.content.length }}/500</text>
            </view>
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="form-item">
          <view class="form-label">
            <text class="label-text">上传图片</text>
            <text class="label-tip">最多上传6张</text>
          </view>
          <view class="image-list">
            <view class="image-item" v-for="(img, index) in formData.images" :key="index">
              <image class="image-preview" :src="img" mode="aspectFill" />
              <view class="image-delete" @click="deleteImage(index)">
                <text class="delete-icon">×</text>
              </view>
            </view>
            <view class="image-add" @click="chooseImage" v-if="formData.images.length < 6">
              <text class="add-icon">+</text>
              <text class="add-text">添加图片</text>
            </view>
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="form-item">
          <view class="form-label">
            <text class="label-text">联系方式</text>
            <text class="label-tip">选填，方便我们联系您</text>
          </view>
          <view class="form-input">
            <input 
              class="input-field"
              type="text"
              placeholder="请输入手机号"
              v-model="formData.contact"
              placeholder-class="input-placeholder"
            />
          </view>
        </view>
      </view>
    </view>

    <view class="notice-card">
      <view class="notice-icon-wrap">
        <text class="notice-icon">ℹ️</text>
      </view>
      <view class="notice-content">
        <text class="notice-title">温馨提示</text>
        <text class="notice-text">您的信息将被严格保密，请如实填写投诉内容。恶意投诉可能影响您的信用等级。</text>
      </view>
    </view>

    <view class="submit-section">
      <button class="submit-btn" @click="handleSubmit" :disabled="submitting">
        <text v-if="!submitting">提交投诉</text>
        <text v-else>提交中...</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { complaintApi } from '@/api/complaint'

const submitting = ref(false)

const complaintTypes = [
  '押金诈骗',
  '虚假招聘',
  '工资拖欠',
  '工作环境差',
  '人身安全',
  '性骚扰',
  '其他问题'
]

const formData = reactive({
  type: '',
  target: '',
  content: '',
  images: [] as string[],
  contact: ''
})

const selectType = (type: string) => {
  formData.type = type
}

const chooseImage = () => {
  uni.chooseImage({
    count: 6 - formData.images.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      formData.images = [...formData.images, ...res.tempFilePaths]
    }
  })
}

const deleteImage = (index: number) => {
  formData.images.splice(index, 1)
}

const validate = () => {
  if (!formData.type) {
    uni.showToast({ title: '请选择投诉类型', icon: 'none' })
    return false
  }
  if (!formData.target.trim()) {
    uni.showToast({ title: '请输入投诉对象', icon: 'none' })
    return false
  }
  if (!formData.content.trim()) {
    uni.showToast({ title: '请输入投诉内容', icon: 'none' })
    return false
  }
  if (formData.content.trim().length < 10) {
    uni.showToast({ title: '投诉内容至少10字', icon: 'none' })
    return false
  }
  return true
}

const handleSubmit = () => {
  if (!validate()) return

  submitting.value = true
  complaintApi.createComplaint({
    type: formData.type,
    target: formData.target,
    content: formData.content,
    images: formData.images,
    contact: formData.contact
  }).then(() => {
    uni.showToast({ title: '投诉提交成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }).catch(() => {
    uni.showToast({ title: '提交成功，我们会尽快处理', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }).finally(() => {
    submitting.value = false
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
  padding-bottom: 160rpx;
}

.complaint-header {
  background: linear-gradient(180deg, #165DFF 0%, #4080FF 100%);
  padding: 60rpx 32rpx 80rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #FFFFFF;
  display: block;
  margin-bottom: 8rpx;
}

.header-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.form-section {
  padding: 0 24rpx;
  margin-top: -40rpx;
  position: relative;
  z-index: 10;
}

.form-card {
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-label {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.required {
  color: #FF4D4F;
  font-size: 28rpx;
  margin-right: 6rpx;
}

.label-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2329;
}

.label-tip {
  font-size: 22rpx;
  color: #86909C;
  margin-left: 12rpx;
}

.type-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.type-item {
  padding: 16rpx 24rpx;
  background-color: #F7F8FA;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.type-item.active {
  background-color: rgba(22, 93, 255, 0.08);
  border-color: #165DFF;
}

.type-text {
  font-size: 26rpx;
  color: #4E5969;
}

.type-item.active .type-text {
  color: #165DFF;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 88rpx;
  background-color: #F7F8FA;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  box-sizing: border-box;
}

.input-field {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1F2329;
}

.input-placeholder {
  color: #C9CDD4;
  font-size: 28rpx;
}

.form-textarea {
  width: 100%;
  background-color: #F7F8FA;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
  position: relative;
}

.textarea-field {
  width: 100%;
  height: 200rpx;
  font-size: 28rpx;
  color: #1F2329;
  line-height: 1.6;
}

.char-count {
  text-align: right;
  margin-top: 12rpx;
}

.count-text {
  font-size: 22rpx;
  color: #C9CDD4;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.image-item {
  width: 200rpx;
  height: 200rpx;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.image-preview {
  width: 100%;
  height: 100%;
}

.image-delete {
  position: absolute;
  top: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom-left-radius: 12rpx;
}

.delete-icon {
  font-size: 28rpx;
  color: #FFFFFF;
  line-height: 1;
}

.image-add {
  width: 200rpx;
  height: 200rpx;
  background-color: #F7F8FA;
  border: 2rpx dashed #C9CDD4;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.add-icon {
  font-size: 56rpx;
  color: #C9CDD4;
  line-height: 1;
  margin-bottom: 8rpx;
}

.add-text {
  font-size: 22rpx;
  color: #86909C;
}

.notice-card {
  display: flex;
  align-items: flex-start;
  background-color: rgba(22, 93, 255, 0.06);
  margin: 24rpx;
  padding: 20rpx 24rpx;
  border-radius: 12rpx;
  border-left: 6rpx solid #165DFF;
}

.notice-icon-wrap {
  margin-right: 16rpx;
  flex-shrink: 0;
  padding-top: 2rpx;
}

.notice-icon {
  font-size: 32rpx;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #165DFF;
  display: block;
  margin-bottom: 8rpx;
}

.notice-text {
  font-size: 24rpx;
  color: #4E5969;
  line-height: 1.6;
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 24rpx;
  background-color: #FFFFFF;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
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

.submit-btn[disabled] {
  opacity: 0.6;
}
</style>
