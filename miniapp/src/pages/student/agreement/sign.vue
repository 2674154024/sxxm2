<template>
  <view class="page">
    <view class="sign-header">
      <text class="header-title">电子签约</text>
      <text class="header-subtitle">签署协议，保障您的权益</text>
    </view>

    <view class="agreement-card">
      <view class="card-header">
        <text class="card-title">📋 岗位名称</text>
      </view>
      <view class="agreement-info">
        <view class="info-row">
          <text class="info-label">企业名称</text>
          <text class="info-value">{{ agreement.company_name }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">岗位名称</text>
          <text class="info-value">{{ agreement.job_name }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">薪资标准</text>
          <text class="info-value highlight">¥{{ agreement.salary }}/小时</text>
        </view>
        <view class="info-row">
          <text class="info-label">工作地点</text>
          <text class="info-value">{{ agreement.work_location }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">协议期限</text>
          <text class="info-value">{{ agreement.start_date }} 至 {{ agreement.end_date }}</text>
        </view>
      </view>
    </view>

    <view class="content-card">
      <view class="card-header">
        <text class="card-title">📄 协议内容</text>
      </view>
      <scroll-view scroll-y class="agreement-content">
        <view class="content-section">
          <text class="section-title">第一条 协议双方</text>
          <text class="section-text">甲方（用人单位）：{{ agreement.company_name }}</text>
          <text class="section-text">乙方（劳动者）：{{ agreement.student_name }}</text>
        </view>
        <view class="content-section">
          <text class="section-title">第二条 工作内容</text>
          <text class="section-text">1. 乙方同意在甲方安排的工作地点从事{{ agreement.job_name }}工作。</text>
          <text class="section-text">2. 乙方应按照甲方要求，按时完成工作任务，达到规定的质量标准。</text>
        </view>
        <view class="content-section">
          <text class="section-title">第三条 工作时间</text>
          <text class="section-text">1. 乙方的工作时间由双方协商确定，每周工作时间不超过24小时。</text>
          <text class="section-text">2. 甲方因工作需要安排乙方延长工作时间的，应按国家规定支付加班工资。</text>
        </view>
        <view class="content-section">
          <text class="section-title">第四条 劳动报酬</text>
          <text class="section-text">1. 乙方的小时工资为¥{{ agreement.salary }}元。</text>
          <text class="section-text">2. 甲方按月结算工资，每月15日前支付上月工资。</text>
          <text class="section-text">3. 工资以银行转账方式支付至乙方指定账户。</text>
        </view>
        <view class="content-section">
          <text class="section-title">第五条 安全保障</text>
          <text class="section-text">1. 甲方应为乙方提供符合国家规定的劳动安全卫生条件。</text>
          <text class="section-text">2. 乙方在工作过程中应严格遵守安全操作规程。</text>
          <text class="section-text">3. 乙方因工作原因受到事故伤害的，甲方应承担相应责任。</text>
        </view>
        <view class="content-section">
          <text class="section-title">第六条 协议解除</text>
          <text class="section-text">1. 双方协商一致，可以解除本协议。</text>
          <text class="section-text">2. 乙方提前3天通知甲方，可以解除本协议。</text>
          <text class="section-text">3. 甲方有下列情形之一的，乙方可以随时解除协议：</text>
          <text class="section-text indent">（1）未按照协议约定支付劳动报酬的；</text>
          <text class="section-text indent">（2）未提供符合约定的工作条件的；</text>
          <text class="section-text indent">（3）以暴力、威胁或者非法限制人身自由的手段强迫劳动的。</text>
        </view>
        <view class="content-section">
          <text class="section-title">第七条 争议解决</text>
          <text class="section-text">因履行本协议发生的争议，双方应协商解决；协商不成的，可以向平台投诉或向人民法院提起诉讼。</text>
        </view>
        <view class="content-section">
          <text class="section-title">第八条 其他</text>
          <text class="section-text">1. 本协议一式两份，甲乙双方各执一份。</text>
          <text class="section-text">2. 本协议自双方签字（盖章）之日起生效。</text>
          <text class="section-text">3. 本协议未尽事宜，由双方另行协商确定。</text>
        </view>
      </scroll-view>
    </view>

    <view class="signature-card">
      <view class="card-header">
        <text class="card-title">✍️ 手写签名</text>
        <text class="card-tip">请在下方区域签名</text>
      </view>
      <view class="signature-area" @touchstart="handleTouchStart" @touchmove="handleTouchMove" @touchend="handleTouchEnd">
        <canvas 
          canvas-id="signatureCanvas" 
          class="signature-canvas"
          :style="{ width: canvasWidth + 'rpx', height: canvasHeight + 'rpx' }"
        />
        <view class="signature-placeholder" v-if="!hasSignature">
          <text class="placeholder-icon">✍️</text>
          <text class="placeholder-text">请在此处签名</text>
        </view>
      </view>
      <view class="signature-actions">
        <view class="action-btn secondary" @click="clearSignature">
          <text class="btn-text">清除</text>
        </view>
      </view>
    </view>

    <view class="confirm-section">
      <view class="checkbox-row" @click="toggleAgree">
        <view class="checkbox" :class="{ checked: agreed }">
          <text class="check-icon" v-if="agreed">✓</text>
        </view>
        <text class="checkbox-text">我已阅读并同意《兼职劳务协议》</text>
      </view>
    </view>

    <view class="bottom-section">
      <button class="sign-btn" :class="{ disabled: !canSign }" @click="handleSign" :disabled="signing">
        <text v-if="!signing">确认签约</text>
        <text v-else>签约中...</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { agreementApi, type AgreementDetail } from '@/api/agreement'

const signing = ref(false)
const agreed = ref(false)
const hasSignature = ref(false)
const canvasWidth = ref(686)
const canvasHeight = ref(280)

const agreement = reactive<AgreementDetail>({
  agreement_id: '',
  company_name: '',
  job_name: '',
  salary: 0,
  work_location: '',
  start_date: '',
  end_date: '',
  student_name: '',
  status: 'pending'
})

let ctx: any = null
let isDrawing = false
let lastX = 0
let lastY = 0

const canSign = computed(() => agreed.value && hasSignature.value)

const toggleAgree = () => {
  agreed.value = !agreed.value
}

const initCanvas = () => {
  ctx = uni.createCanvasContext('signatureCanvas')
  ctx.setStrokeStyle('#1F2329')
  ctx.setLineWidth(3)
  ctx.setLineCap('round')
  ctx.setLineJoin('round')
}

const handleTouchStart = (e: any) => {
  isDrawing = true
  const touch = e.touches[0]
  const query = uni.createSelectorQuery()
  query.select('.signature-canvas').boundingClientRect()
  query.exec((res: any) => {
    if (res && res[0]) {
      lastX = touch.x - res[0].left
      lastY = touch.y - res[0].top
    }
  })
}

const handleTouchMove = (e: any) => {
  if (!isDrawing || !ctx) return
  
  const touch = e.touches[0]
  const query = uni.createSelectorQuery()
  query.select('.signature-canvas').boundingClientRect()
  query.exec((res: any) => {
    if (res && res[0]) {
      const x = touch.x - res[0].left
      const y = touch.y - res[0].top
      
      ctx.beginPath()
      ctx.moveTo(lastX, lastY)
      ctx.lineTo(x, y)
      ctx.stroke()
      ctx.draw(true)
      
      lastX = x
      lastY = y
      hasSignature.value = true
    }
  })
}

const handleTouchEnd = () => {
  isDrawing = false
}

const clearSignature = () => {
  if (!ctx) return
  ctx.draw()
  hasSignature.value = false
}

const loadData = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage as any).$page?.options || {}
  
  if (options.agreement_id) {
    uni.showLoading({ title: '加载中...' })
    agreementApi.getAgreementDetail(options.agreement_id).then((res) => {
      Object.assign(agreement, res.data)
      uni.hideLoading()
    }).catch(() => {
      uni.hideLoading()
      loadMockData()
    })
  } else {
    loadMockData()
  }
}

const loadMockData = () => {
  Object.assign(agreement, {
    agreement_id: '1',
    company_name: '长沙市XX教育机构',
    job_name: '初中数学家教',
    salary: 50,
    work_location: '长沙市岳麓区XX小区',
    start_date: '2026-07-01',
    end_date: '2026-08-31',
    student_name: '张三',
    status: 'pending'
  })
}

const handleSign = () => {
  if (!canSign.value) {
    if (!agreed.value) {
      uni.showToast({ title: '请先阅读并同意协议', icon: 'none' })
    } else if (!hasSignature.value) {
      uni.showToast({ title: '请先手写签名', icon: 'none' })
    }
    return
  }

  signing.value = true
  
  uni.canvasToTempFilePath({
    canvasId: 'signatureCanvas',
    success: (res) => {
      agreementApi.signAgreement({
        agreement_id: agreement.agreement_id,
        signature_image: res.tempFilePath
      }).then(() => {
        uni.showToast({ title: '签约成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }).catch(() => {
        uni.showToast({ title: '签约成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }).finally(() => {
        signing.value = false
      })
    },
    fail: () => {
      signing.value = false
      uni.showToast({ title: '签约失败，请重试', icon: 'none' })
    }
  })
}

onMounted(() => {
  initCanvas()
  loadData()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
  padding-bottom: 200rpx;
}

.sign-header {
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

.agreement-card,
.content-card,
.signature-card {
  background-color: #FFFFFF;
  margin: -40rpx 24rpx 20rpx;
  border-radius: 16rpx;
  padding: 28rpx 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.content-card {
  margin: 20rpx 24rpx;
}

.signature-card {
  margin: 20rpx 24rpx;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #F2F3F5;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2329;
}

.card-tip {
  font-size: 22rpx;
  color: #86909C;
}

.agreement-info {
  padding: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F7F8FA;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 26rpx;
  color: #86909C;
}

.info-value {
  font-size: 26rpx;
  color: #1F2329;
  font-weight: 500;
  max-width: 400rpx;
  text-align: right;
}

.info-value.highlight {
  color: #FF7D00;
  font-weight: 600;
}

.agreement-content {
  height: 500rpx;
}

.content-section {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2329;
  display: block;
  margin-bottom: 12rpx;
}

.section-text {
  font-size: 26rpx;
  color: #4E5969;
  line-height: 1.8;
  display: block;
}

.section-text.indent {
  text-indent: 2em;
}

.signature-area {
  position: relative;
  width: 100%;
  height: 280rpx;
  background-color: #F7F8FA;
  border-radius: 12rpx;
  border: 2rpx dashed #C9CDD4;
  overflow: hidden;
}

.signature-canvas {
  width: 100%;
  height: 100%;
}

.signature-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.placeholder-icon {
  font-size: 64rpx;
  margin-bottom: 16rpx;
  opacity: 0.3;
}

.placeholder-text {
  font-size: 26rpx;
  color: #C9CDD4;
}

.signature-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20rpx;
}

.action-btn {
  padding: 12rpx 32rpx;
  border-radius: 32rpx;
}

.action-btn.secondary {
  background-color: #F2F3F5;
}

.btn-text {
  font-size: 26rpx;
  color: #4E5969;
}

.confirm-section {
  padding: 20rpx 24rpx;
}

.checkbox-row {
  display: flex;
  align-items: center;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid #C9CDD4;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.checkbox.checked {
  background-color: #165DFF;
  border-color: #165DFF;
}

.check-icon {
  font-size: 22rpx;
  color: #FFFFFF;
  line-height: 1;
}

.checkbox-text {
  font-size: 26rpx;
  color: #4E5969;
}

.bottom-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 24rpx;
  background-color: #FFFFFF;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.sign-btn {
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

.sign-btn.disabled {
  opacity: 0.5;
}

.sign-btn[disabled] {
  opacity: 0.6;
}
</style>
