<template>
  <view class="page">
    <view class="step-indicator">
      <view class="step" :class="{ active: currentStep >= 1 }">
        <view class="step-dot">1</view>
        <text class="step-text">预览协议</text>
      </view>
      <view class="step-line" :class="{ active: currentStep >= 2 }"></view>
      <view class="step" :class="{ active: currentStep >= 2 }">
        <view class="step-dot">2</view>
        <text class="step-text">手写签名</text>
      </view>
      <view class="step-line" :class="{ active: currentStep >= 3 }"></view>
      <view class="step" :class="{ active: currentStep >= 3 }">
        <view class="step-dot">3</view>
        <text class="step-text">人脸识别</text>
      </view>
      <view class="step-line" :class="{ active: currentStep >= 4 }"></view>
      <view class="step" :class="{ active: currentStep >= 4 }">
        <view class="step-dot">4</view>
        <text class="step-text">提交签署</text>
      </view>
    </view>

    <view class="content-area">
      <view v-if="currentStep === 1" class="step-content">
        <view class="agreement-preview">
          <view class="preview-header">
            <text class="preview-title">服务协议</text>
          </view>
          <scroll-view class="preview-body" scroll-y>
            <view class="agreement-section">
              <text class="section-title">一、岗位信息</text>
              <view class="info-row">
                <text class="info-label">岗位名称：</text>
                <text class="info-value">{{ agreement.job_name }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">企业名称：</text>
                <text class="info-value">{{ agreement.company_name }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">薪资待遇：</text>
                <text class="info-value">{{ agreement.salary }}元/{{ agreement.salary_type === 'hourly' ? '小时' : agreement.salary_type === 'daily' ? '天' : '月' }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">工作期限：</text>
                <text class="info-value">{{ agreement.start_date }} 至 {{ agreement.end_date }}</text>
              </view>
            </view>

            <view class="agreement-section">
              <text class="section-title">二、工作内容</text>
              <text class="section-content">{{ agreement.work_content }}</text>
            </view>

            <view class="agreement-section">
              <text class="section-title">三、学生权责</text>
              <text class="section-content">{{ agreement.student_rights }}</text>
            </view>

            <view class="agreement-section">
              <text class="section-title">四、企业权责</text>
              <text class="section-content">{{ agreement.enterprise_rights }}</text>
            </view>
          </scroll-view>
        </view>
      </view>

      <view v-if="currentStep === 2" class="step-content">
        <view class="signature-board">
          <view class="signature-header">
            <text class="signature-title">请在下方区域手写签名</text>
            <view class="signature-actions">
              <text class="action-btn" @click="clearSignature">清除</text>
              <text class="action-btn" @click="saveSignature">保存</text>
            </view>
          </view>
          <canvas 
            canvas-id="signatureCanvas" 
            id="signatureCanvas"
            class="signature-canvas"
            @touchstart="startDraw"
            @touchmove="drawing"
            @touchend="endDraw"
          ></canvas>
          <image 
            v-if="signatureImage" 
            class="signature-preview" 
            :src="signatureImage" 
            mode="widthFix"
          />
        </view>
      </view>

      <view v-if="currentStep === 3" class="step-content">
        <view class="face-recognition">
          <view class="face-header">
            <text class="face-title">人脸识别验证</text>
            <text class="face-desc">请拍摄您的面部照片进行身份验证</text>
          </view>
          <view class="camera-area">
            <image 
              v-if="faceImage" 
              class="face-preview" 
              :src="faceImage" 
              mode="widthFix"
            />
            <view v-else class="camera-placeholder">
              <text class="camera-icon">📷</text>
              <text class="camera-text">点击拍照</text>
            </view>
          </view>
          <button class="camera-btn" @click="takePhoto">拍摄照片</button>
        </view>
      </view>

      <view v-if="currentStep === 4" class="step-content">
        <view class="confirm-section">
          <view class="confirm-title">确认签署信息</view>
          <view class="confirm-item">
            <text class="confirm-label">协议编号</text>
            <text class="confirm-value">{{ agreement.agreement_id }}</text>
          </view>
          <view class="confirm-item">
            <text class="confirm-label">岗位名称</text>
            <text class="confirm-value">{{ agreement.job_name }}</text>
          </view>
          <view class="confirm-item">
            <text class="confirm-label">签署日期</text>
            <text class="confirm-value">{{ currentDate }}</text>
          </view>
          <view class="signature-preview-box">
            <text class="preview-label">您的签名</text>
            <image v-if="signatureImage" class="signature-thumb" :src="signatureImage" mode="widthFix" />
          </view>
          <view class="face-preview-box">
            <text class="preview-label">人脸照片</text>
            <image v-if="faceImage" class="face-thumb" :src="faceImage" mode="widthFix" />
          </view>
        </view>
      </view>
    </view>

    <view class="footer-area">
      <view class="agree-checkbox" v-if="currentStep === 1">
        <view class="checkbox" :class="{ checked: agreed }" @click="toggleAgree">
          <text class="checkbox-icon" v-if="agreed">✓</text>
        </view>
        <text class="agree-text">我已阅读并同意协议内容</text>
      </view>
      <view class="footer-btn">
        <button 
          v-if="currentStep > 1" 
          class="prev-btn" 
          @click="prevStep"
        >上一步</button>
        <button 
          class="next-btn" 
          :class="{ disabled: !canNext }"
          @click="nextStep"
        >
          {{ currentStep === 4 ? '提交签署' : '下一步' }}
        </button>
      </view>
    </view>

    <view class="success-modal" v-if="showSuccess">
      <view class="success-content">
        <text class="success-icon">✓</text>
        <text class="success-title">签署成功</text>
        <text class="success-desc">您的协议已提交审核</text>
        <button class="success-btn" @click="goToAgreementList">查看协议列表</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { agreementApi, type Agreement } from '../../../api/agreement'
import dayjs from 'dayjs'

const agreementId = ref('')
const agreement = ref<Agreement>({
  agreement_id: '',
  job_id: '',
  job_name: '',
  company_name: '',
  salary: 0,
  salary_type: 'hourly',
  work_content: '',
  student_rights: '',
  enterprise_rights: '',
  start_date: '',
  end_date: '',
  status: 'pending',
  created_at: ''
})

const currentStep = ref(1)
const agreed = ref(false)
const signatureImage = ref('')
const faceImage = ref('')
const showSuccess = ref(false)
const currentDate = ref(dayjs().format('YYYY年MM月DD日'))

let ctx: UniApp.CanvasContext | null = null
let isDrawing = false
let lastX = 0
let lastY = 0

const canNext = computed(() => {
  if (currentStep.value === 1) return agreed.value
  if (currentStep.value === 2) return signatureImage.value !== ''
  if (currentStep.value === 3) return faceImage.value !== ''
  if (currentStep.value === 4) return signatureImage.value !== '' && faceImage.value !== ''
  return true
})

const toggleAgree = () => {
  agreed.value = !agreed.value
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const nextStep = async () => {
  if (!canNext.value) return
  
  if (currentStep.value === 4) {
    await submitSign()
    return
  }
  
  currentStep.value++
}

const initCanvas = () => {
  ctx = uni.createCanvasContext('signatureCanvas')
  ctx.setLineWidth(3)
  ctx.setLineCap('round')
  ctx.setLineJoin('round')
  ctx.setStrokeStyle('#1F2329')
}

const startDraw = (e: TouchEvent) => {
  if (!ctx) return
  isDrawing = true
  const touch = e.touches[0]
  lastX = touch.x
  lastY = touch.y
}

const drawing = (e: TouchEvent) => {
  if (!ctx || !isDrawing) return
  const touch = e.touches[0]
  ctx.beginPath()
  ctx.moveTo(lastX, lastY)
  ctx.lineTo(touch.x, touch.y)
  ctx.stroke()
  ctx.draw(true)
  lastX = touch.x
  lastY = touch.y
}

const endDraw = () => {
  isDrawing = false
}

const clearSignature = () => {
  if (!ctx) return
  ctx.clearRect(0, 0, 750, 400)
  ctx.draw()
  signatureImage.value = ''
}

const saveSignature = () => {
  uni.canvasToTempFilePath({
    canvasId: 'signatureCanvas',
    success: (res) => {
      signatureImage.value = res.tempFilePath
      uni.showToast({ title: '签名已保存', icon: 'success' })
    },
    fail: (err) => {
      console.error('保存签名失败', err)
      uni.showToast({ title: '保存失败', icon: 'none' })
    }
  })
}

const takePhoto = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera'],
    success: (res) => {
      faceImage.value = res.tempFilePaths[0]
      uni.showToast({ title: '照片已拍摄', icon: 'success' })
    },
    fail: (err) => {
      console.error('拍照失败', err)
      uni.showToast({ title: '拍照失败', icon: 'none' })
    }
  })
}

const submitSign = async () => {
  uni.showLoading({ title: '提交中...' })
  
  try {
    await agreementApi.signAgreement({
      agreement_id: agreementId.value,
      signature_image: signatureImage.value,
      face_image: faceImage.value,
      agree: true
    })
    
    uni.hideLoading()
    showSuccess.value = true
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '提交失败', icon: 'none' })
  }
}

const goToAgreementList = () => {
  uni.navigateBack()
}

const loadAgreement = async () => {
  if (!agreementId.value) return
  
  try {
    const res = await agreementApi.getAgreementDetail(agreementId.value)
    if (res.data) {
      agreement.value = res.data
    }
  } catch (error) {
    console.error('加载协议失败', error)
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage as any).$page?.options || {}
  
  agreementId.value = options.agreement_id || options.id || ''
  
  loadAgreement()
  initCanvas()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
  display: flex;
  flex-direction: column;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #FFFFFF;
  padding: 32rpx 24rpx;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.step-dot {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background-color: #E5E6EB;
  color: #86909C;
  font-size: 28rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.step.active .step-dot {
  background-color: #165DFF;
  color: #FFFFFF;
}

.step-text {
  font-size: 22rpx;
  color: #86909C;
}

.step.active .step-text {
  color: #165DFF;
  font-weight: bold;
}

.step-line {
  width: 80rpx;
  height: 4rpx;
  background-color: #E5E6EB;
  margin: 0 16rpx;
}

.step-line.active {
  background-color: #165DFF;
}

.content-area {
  flex: 1;
  padding: 24rpx;
  overflow-y: auto;
}

.step-content {
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
}

.agreement-preview {
  height: 100%;
}

.preview-header {
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #F2F3F5;
  margin-bottom: 24rpx;
}

.preview-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1F2329;
}

.preview-body {
  max-height: 800rpx;
}

.agreement-section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 16rpx;
  display: block;
}

.section-content {
  font-size: 28rpx;
  color: #4E5969;
  line-height: 1.8;
}

.info-row {
  display: flex;
  padding: 12rpx 0;
}

.info-label {
  font-size: 28rpx;
  color: #86909C;
  width: 200rpx;
}

.info-value {
  font-size: 28rpx;
  color: #1F2329;
  flex: 1;
}

.signature-board {
  display: flex;
  flex-direction: column;
}

.signature-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.signature-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
}

.signature-actions {
  display: flex;
}

.action-btn {
  font-size: 28rpx;
  color: #165DFF;
  margin-left: 32rpx;
}

.signature-canvas {
  width: 100%;
  height: 400rpx;
  background-color: #FFFFFF;
  border: 2rpx dashed #C9CDD4;
  border-radius: 12rpx;
}

.signature-preview {
  width: 100%;
  margin-top: 24rpx;
  border-radius: 8rpx;
}

.face-recognition {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.face-header {
  text-align: center;
  margin-bottom: 32rpx;
}

.face-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
  display: block;
  margin-bottom: 12rpx;
}

.face-desc {
  font-size: 26rpx;
  color: #86909C;
}

.camera-area {
  width: 500rpx;
  height: 500rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background-color: #F2F3F5;
  margin-bottom: 32rpx;
}

.camera-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.camera-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
}

.camera-text {
  font-size: 28rpx;
  color: #86909C;
}

.face-preview {
  width: 100%;
  height: 100%;
}

.camera-btn {
  font-size: 32rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 24rpx 80rpx;
  border-radius: 48rpx;
  border: none;
}

.confirm-section {
  display: flex;
  flex-direction: column;
}

.confirm-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 24rpx;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F2F3F5;
}

.confirm-label {
  font-size: 28rpx;
  color: #86909C;
}

.confirm-value {
  font-size: 28rpx;
  color: #1F2329;
}

.signature-preview-box, .face-preview-box {
  margin-top: 32rpx;
}

.preview-label {
  font-size: 28rpx;
  color: #86909C;
  margin-bottom: 16rpx;
  display: block;
}

.signature-thumb, .face-thumb {
  width: 300rpx;
  border-radius: 8rpx;
}

.footer-area {
  background-color: #FFFFFF;
  padding: 24rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.agree-checkbox {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.checkbox {
  width: 48rpx;
  height: 48rpx;
  border: 2rpx solid #C9CDD4;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.checkbox.checked {
  background-color: #165DFF;
  border-color: #165DFF;
}

.checkbox-icon {
  font-size: 28rpx;
  color: #FFFFFF;
}

.agree-text {
  font-size: 28rpx;
  color: #4E5969;
}

.footer-btn {
  display: flex;
  gap: 24rpx;
}

.prev-btn {
  flex: 1;
  font-size: 32rpx;
  color: #86909C;
  background-color: #F2F3F5;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
}

.next-btn {
  flex: 2;
  font-size: 32rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
}

.next-btn.disabled {
  background-color: #C9CDD4;
}

.success-modal {
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

.success-content {
  width: 600rpx;
  background-color: #FFFFFF;
  border-radius: 24rpx;
  padding: 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.success-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: #52C41A;
  color: #FFFFFF;
  font-size: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.success-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 12rpx;
}

.success-desc {
  font-size: 28rpx;
  color: #86909C;
  margin-bottom: 40rpx;
}

.success-btn {
  font-size: 32rpx;
  color: #FFFFFF;
  background-color: #165DFF;
  padding: 24rpx 80rpx;
  border-radius: 48rpx;
  border: none;
}
</style>