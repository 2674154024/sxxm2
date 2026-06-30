<template>
  <view class="page">
    <view class="form-section">
      <view class="form-item">
        <text class="form-label">投诉类型 <text class="required">*</text></text>
        <view class="type-options">
          <view 
            class="type-option" 
            :class="{ active: complaintType === type.value }"
            v-for="type in complaintTypes" 
            :key="type.value"
            @click="complaintType = type.value"
          >
            {{ type.label }}
          </view>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">关联岗位</text>
        <picker mode="selector" :range="jobOptions" @change="onJobChange">
          <view class="picker-value">
            {{ selectedJob ? selectedJob.job_name + ' - ' + selectedJob.company_name : '请选择（可选）' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">被投诉人 <text class="required">*</text></text>
        <input class="form-input" v-model="accusedName" placeholder="请输入被投诉人姓名或企业名称" />
      </view>

      <view class="form-item">
        <text class="form-label">投诉内容 <text class="required">*</text></text>
        <textarea 
          class="form-textarea" 
          v-model="content" 
          placeholder="请详细描述投诉情况（最少20字）"
          :maxlength="2000"
        />
        <text class="text-count">{{ content.length }}/2000</text>
      </view>

      <view class="form-item">
        <text class="form-label">证据上传</text>
        <view class="image-upload">
          <view class="image-list">
            <view class="image-item" v-for="(img, index) in images" :key="index">
              <image :src="img" mode="aspectFill" class="preview-image" />
              <view class="delete-btn" @click="removeImage(index)">×</view>
            </view>
            <view class="upload-btn" @click="chooseImage" v-if="images.length < 9">
              <text class="upload-icon">+</text>
              <text class="upload-text">上传</text>
            </view>
          </view>
          <text class="upload-tip">最多上传9张图片</text>
        </view>
      </view>
    </view>

    <view class="submit-section">
      <button class="submit-btn" :class="{ disabled: !canSubmit }" @click="handleSubmit">
        提交投诉
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { complaintApi, type AppliedJob } from '@/api/complaint'

const complaintTypes = ref([
  { value: 'false_recruitment', label: '虚假招聘' },
  { value: 'salary_delay', label: '薪资拖欠' },
  { value: 'deposit_scam', label: '押金诈骗' },
  { value: 'non_fulfillment', label: '未履约' },
  { value: 'information_leak', label: '信息泄露' }
])

const appliedJobs = ref<AppliedJob[]>([])
const jobOptions = computed(() => {
  return appliedJobs.value.map(job => `${job.job_name} - ${job.company_name}`)
})

const complaintType = ref('')
const selectedJobIndex = ref(-1)
const accusedName = ref('')
const content = ref('')
const images = ref<string[]>([])

const selectedJob = computed(() => {
  if (selectedJobIndex.value >= 0) {
    return appliedJobs.value[selectedJobIndex.value]
  }
  return null
})

const canSubmit = computed(() => {
  return complaintType.value && accusedName.value.trim() && content.value.length >= 20
})

const onJobChange = (e: any) => {
  selectedJobIndex.value = e.detail.value
}

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - images.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      images.value = [...images.value, ...res.tempFilePaths]
    }
  })
}

const removeImage = (index: number) => {
  images.value.splice(index, 1)
}

const handleSubmit = () => {
  if (!canSubmit.value) {
    if (!complaintType.value) {
      uni.showToast({ title: '请选择投诉类型', icon: 'none' })
    } else if (!accusedName.value.trim()) {
      uni.showToast({ title: '请输入被投诉人', icon: 'none' })
    } else if (content.value.length < 20) {
      uni.showToast({ title: '投诉内容最少20字', icon: 'none' })
    }
    return
  }

  uni.showModal({
    title: '确认提交',
    content: '投诉提交后，平台将在24小时内处理，请确保信息真实有效。',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '提交中...' })
        
        complaintApi.submitComplaint({
          complaint_type: complaintType.value,
          apply_id: selectedJob.value?.apply_id,
          accused_name: accusedName.value.trim(),
          content: content.value.trim(),
          evidence_images: images.value
        }).then(() => {
          uni.hideLoading()
          uni.showToast({ title: '投诉已提交，平台将在24小时内处理', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 2000)
        }).catch(() => {
          uni.hideLoading()
          uni.showToast({ title: '提交失败，请重试', icon: 'none' })
        })
      }
    }
  })
}

const loadData = () => {
  complaintApi.getAppliedJobs().then((res) => {
    appliedJobs.value = res.data
  }).catch(() => {
    appliedJobs.value = [
      { apply_id: '1', job_name: '初中数学家教', company_name: '长沙市XX教育机构' },
      { apply_id: '2', job_name: '促销导购', company_name: 'XX超市' },
      { apply_id: '3', job_name: '展会协助', company_name: 'XX会展公司' }
    ]
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
  padding-bottom: 160rpx;
}

.form-section {
  background-color: #FFFFFF;
  margin: 24rpx;
  border-radius: 16rpx;
  padding: 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 28rpx;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 16rpx;
  display: block;
}

.required {
  color: #FF4D4F;
}

.type-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.type-option {
  font-size: 26rpx;
  padding: 16rpx 24rpx;
  background-color: #F8F9FA;
  border-radius: 24rpx;
  color: #4E5969;
  border: 2rpx solid transparent;
}

.type-option.active {
  background-color: rgba(22, 93, 255, 0.1);
  color: #165DFF;
  border-color: #165DFF;
}

.picker-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background-color: #F8F9FA;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #4E5969;
}

.picker-arrow {
  font-size: 32rpx;
  color: #C9CDD4;
}

.form-input {
  width: 100%;
  padding: 24rpx;
  background-color: #F8F9FA;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.form-textarea {
  width: 100%;
  height: 240rpx;
  padding: 24rpx;
  background-color: #F8F9FA;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.text-count {
  display: block;
  text-align: right;
  font-size: 22rpx;
  color: #86909C;
  margin-top: 8rpx;
}

.image-upload {
  margin-top: 16rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.image-item {
  width: 160rpx;
  height: 160rpx;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  background-color: rgba(0, 0, 0, 0.5);
  color: #FFFFFF;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-btn {
  width: 160rpx;
  height: 160rpx;
  background-color: #F8F9FA;
  border-radius: 12rpx;
  border: 2rpx dashed #D9D9D9;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 48rpx;
  color: #C9CDD4;
  margin-bottom: 8rpx;
}

.upload-text {
  font-size: 24rpx;
  color: #86909C;
}

.upload-tip {
  font-size: 22rpx;
  color: #86909C;
  margin-top: 16rpx;
  display: block;
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx;
  background-color: #FFFFFF;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.submit-btn {
  width: 100%;
  font-size: 32rpx;
  font-weight: bold;
  padding: 28rpx;
  border-radius: 48rpx;
  background-color: #165DFF;
  color: #FFFFFF;
  border: none;
}

.submit-btn.disabled {
  background-color: #C9CDD4;
  color: #FFFFFF;
}
</style>