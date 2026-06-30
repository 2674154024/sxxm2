<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()

const agreementInfo = ref({
  agreement_id: 'agr_' + Date.now(),
  job_title: '茶颜悦色门店店员',
  enterprise_name: '茶颜悦色(五一广场店)',
  salary_amount: 25,
  salary_type: '时薪',
  work_start_date: '2026-07-01',
  work_end_date: '2026-07-31',
  work_time: '09:00 - 18:00',
  work_address: '长沙市天心区黄兴南路步行商业街',
  employer_name: '茶颜悦色人力资源部',
  worker_name: '张同学',
  worker_id: '4301**********1234',
})

const steps = [
  { key: 'read', label: '阅读协议' },
  { key: 'confirm', label: '信息确认' },
  { key: 'sign', label: '电子签名' },
  { key: 'done', label: '签约完成' },
]

const currentStep = ref(0)
const hasRead = ref(false)
const agreeTerms = ref(false)
const workerConfirmed = ref(false)
const signatureData = ref('')
const isSigning = ref(false)
const signaturePad = ref<HTMLCanvasElement | null>(null)
const isDrawing = ref(false)

const currentStepKey = computed(() => steps[currentStep.value]?.key || '')

const canProceed = computed(() => {
  if (currentStep.value === 0) return hasRead.value && agreeTerms.value
  if (currentStep.value === 1) return workerConfirmed.value
  if (currentStep.value === 2) return signatureData.value.length > 0
  return false
})

const nextButtonText = computed(() => {
  if (currentStep.value === 0) return '下一步：信息确认'
  if (currentStep.value === 1) return '下一步：电子签名'
  if (currentStep.value === 2) return '确认签约'
  return '完成'
})

function nextStep() {
  if (!canProceed.value && currentStep.value < 3) {
    if (currentStep.value === 0) {
      showToast({ message: '请阅读并同意协议条款', type: 'fail' })
    } else if (currentStep.value === 1) {
      showToast({ message: '请确认个人信息无误', type: 'fail' })
    } else if (currentStep.value === 2) {
      showToast({ message: '请进行电子签名', type: 'fail' })
    }
    return
  }

  if (currentStep.value === 2) {
    handleSign()
    return
  }

  if (currentStep.value < steps.length - 1) {
    currentStep.value++
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  } else {
    router.back()
  }
}

function handleSign() {
  showConfirmDialog({
    title: '确认签约',
    message: '确认签署本电子合同后，合同即生效，请确认信息无误。',
    confirmButtonText: '确认签约',
    cancelButtonText: '再想想',
  }).then(() => {
    isSigning.value = true
    setTimeout(() => {
      isSigning.value = false
      currentStep.value = 3
      showToast({ message: '签约成功！', type: 'success' })
    }, 2000)
  }).catch(() => {
  })
}

function finishSign() {
  router.back()
}

function goBack() {
  if (currentStep.value > 0) {
    prevStep()
  } else {
    router.back()
  }
}

function initSignaturePad() {
  if (!signaturePad.value) return
  const canvas = signaturePad.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const rect = canvas.getBoundingClientRect()
  canvas.width = rect.width * 2
  canvas.height = rect.height * 2
  ctx.scale(2, 2)

  ctx.lineWidth = 2
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  ctx.strokeStyle = '#333'
}

function getPos(e: MouseEvent | TouchEvent) {
  if (!signaturePad.value) return { x: 0, y: 0 }
  const canvas = signaturePad.value
  const rect = canvas.getBoundingClientRect()
  let clientX: number, clientY: number

  if ('touches' in e) {
    clientX = e.touches[0].clientX
    clientY = e.touches[0].clientY
  } else {
    clientX = e.clientX
    clientY = e.clientY
  }

  return {
    x: clientX - rect.left,
    y: clientY - rect.top,
  }
}

function startDraw(e: MouseEvent | TouchEvent) {
  if (!signaturePad.value) return
  isDrawing.value = true
  const ctx = signaturePad.value.getContext('2d')
  if (!ctx) return
  const pos = getPos(e)
  ctx.beginPath()
  ctx.moveTo(pos.x, pos.y)
  signatureData.value = 'signed'
}

function draw(e: MouseEvent | TouchEvent) {
  if (!isDrawing.value || !signaturePad.value) return
  const ctx = signaturePad.value.getContext('2d')
  if (!ctx) return
  const pos = getPos(e)
  ctx.lineTo(pos.x, pos.y)
  ctx.stroke()
}

function endDraw() {
  isDrawing.value = false
}

function clearSignature() {
  if (!signaturePad.value) return
  const ctx = signaturePad.value.getContext('2d')
  if (!ctx) return
  const canvas = signaturePad.value
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  signatureData.value = ''
}

onMounted(() => {
  if (route.query.step === 'sign') {
    currentStep.value = 2
  }
  setTimeout(() => {
    initSignaturePad()
  }, 100)
})
</script>

<template>
  <div class="agreement-sign-page">
    <NavBar title="电子签约" show-back @back="goBack" />

    <div class="steps-bar">
      <div
        v-for="(step, index) in steps"
        :key="step.key"
        class="step-item"
        :class="{ active: index <= currentStep, done: index < currentStep }"
      >
        <div class="step-circle">
          <span v-if="index < currentStep">✓</span>
          <span v-else>{{ index + 1 }}</span>
        </div>
        <span class="step-label">{{ step.label }}</span>
        <div v-if="index < steps.length - 1" class="step-line"></div>
      </div>
    </div>

    <div v-if="currentStepKey === 'read'" class="step-content">
      <div class="agreement-card">
        <div class="agreement-header">
          <div class="agreement-title">劳务用工协议</div>
          <div class="agreement-subtitle">协议编号：{{ agreementInfo.agreement_id }}</div>
        </div>

        <div class="agreement-body">
          <div class="agreement-section">
            <div class="section-title">一、甲乙双方信息</div>
            <div class="info-row">
              <span class="info-label">甲方（用人单位）：</span>
              <span class="info-value">{{ agreementInfo.enterprise_name }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">乙方（劳动者）：</span>
              <span class="info-value">{{ agreementInfo.worker_name }}</span>
            </div>
          </div>

          <div class="agreement-section">
            <div class="section-title">二、工作内容与地点</div>
            <div class="info-row">
              <span class="info-label">岗位名称：</span>
              <span class="info-value">{{ agreementInfo.job_title }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">工作地点：</span>
              <span class="info-value">{{ agreementInfo.work_address }}</span>
            </div>
          </div>

          <div class="agreement-section">
            <div class="section-title">三、工作时间</div>
            <div class="info-row">
              <span class="info-label">工作日期：</span>
              <span class="info-value">{{ agreementInfo.work_start_date }} 至 {{ agreementInfo.work_end_date }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">每日工时：</span>
              <span class="info-value">{{ agreementInfo.work_time }}</span>
            </div>
          </div>

          <div class="agreement-section">
            <div class="section-title">四、劳动报酬</div>
            <div class="info-row">
              <span class="info-label">薪资标准：</span>
              <span class="info-value highlight">¥{{ agreementInfo.salary_amount }}/{{ agreementInfo.salary_type }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">结算方式：</span>
              <span class="info-value">日结，每日工作结束后次日到账</span>
            </div>
          </div>

          <div class="agreement-section">
            <div class="section-title">五、双方权利与义务</div>
            <p class="paragraph">1. 甲方应按约定支付劳动报酬，提供必要的工作条件。</p>
            <p class="paragraph">2. 乙方应遵守甲方的规章制度，按时完成工作任务。</p>
            <p class="paragraph">3. 乙方在工作期间应注意安全，如发生工伤按相关规定处理。</p>
            <p class="paragraph">4. 任何一方违约，应承担相应的违约责任。</p>
          </div>

          <div class="agreement-section">
            <div class="section-title">六、安全保障</div>
            <p class="paragraph">平台承诺：薪资由平台第三方监管，确保按时足额发放，如遇拖欠平台先行赔付。</p>
          </div>
        </div>
      </div>

      <div class="agreement-footer">
        <label class="checkbox-label" @click="hasRead = !hasRead">
          <span class="checkbox" :class="{ checked: hasRead }">
            <span v-if="hasRead">✓</span>
          </span>
          <span class="checkbox-text">我已阅读并了解上述协议内容</span>
        </label>

        <label class="checkbox-label" @click="agreeTerms = !agreeTerms">
          <span class="checkbox" :class="{ checked: agreeTerms }">
            <span v-if="agreeTerms">✓</span>
          </span>
          <span class="checkbox-text">
            我同意
            <span class="link">《平台服务协议》</span>
            和
            <span class="link">《电子签名授权书》</span>
          </span>
        </label>
      </div>
    </div>

    <div v-else-if="currentStepKey === 'confirm'" class="step-content">
      <div class="confirm-card">
        <div class="confirm-title">请确认以下信息</div>
        <div class="confirm-tip">请仔细核对个人信息，签约后信息不可修改</div>

        <div class="info-block">
          <div class="block-title">岗位信息</div>
          <div class="info-list">
            <div class="info-item">
              <span class="item-label">岗位名称</span>
              <span class="item-value">{{ agreementInfo.job_title }}</span>
            </div>
            <div class="info-item">
              <span class="item-label">用人单位</span>
              <span class="item-value">{{ agreementInfo.enterprise_name }}</span>
            </div>
            <div class="info-item">
              <span class="item-label">工作地点</span>
              <span class="item-value">{{ agreementInfo.work_address }}</span>
            </div>
            <div class="info-item">
              <span class="item-label">工作时间</span>
              <span class="item-value">{{ agreementInfo.work_time }}</span>
            </div>
            <div class="info-item">
              <span class="item-label">工作期限</span>
              <span class="item-value">{{ agreementInfo.work_start_date }} 至 {{ agreementInfo.work_end_date }}</span>
            </div>
            <div class="info-item highlight">
              <span class="item-label">薪资标准</span>
              <span class="item-value">¥{{ agreementInfo.salary_amount }}/{{ agreementInfo.salary_type }}</span>
            </div>
          </div>
        </div>

        <div class="info-block">
          <div class="block-title">个人信息</div>
          <div class="info-list">
            <div class="info-item">
              <span class="item-label">姓名</span>
              <span class="item-value">{{ agreementInfo.worker_name }}</span>
            </div>
            <div class="info-item">
              <span class="item-label">身份证号</span>
              <span class="item-value">{{ agreementInfo.worker_id }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="confirm-footer">
        <label class="checkbox-label" @click="workerConfirmed = !workerConfirmed">
          <span class="checkbox" :class="{ checked: workerConfirmed }">
            <span v-if="workerConfirmed">✓</span>
          </span>
          <span class="checkbox-text">我确认以上信息准确无误</span>
        </label>
      </div>
    </div>

    <div v-else-if="currentStepKey === 'sign'" class="step-content">
      <div class="sign-card">
        <div class="sign-title">请在下方签名</div>
        <div class="sign-tip">请用手指在下方区域签名，签名将作为合同生效的依据</div>

        <div class="signature-wrapper">
          <canvas
            ref="signaturePad"
            class="signature-pad"
            @mousedown="startDraw"
            @mousemove="draw"
            @mouseup="endDraw"
            @mouseleave="endDraw"
            @touchstart.prevent="startDraw"
            @touchmove.prevent="draw"
            @touchend.prevent="endDraw"
          ></canvas>
          <div v-if="!signatureData" class="signature-placeholder">
            请在此处签名
          </div>
        </div>

        <button class="clear-btn" @click="clearSignature">
          清除重签
        </button>
      </div>

      <div class="sign-footer">
        <div class="sign-tip-bottom">
          点击"确认签约"即表示您同意本协议的全部条款
        </div>
      </div>
    </div>

    <div v-else-if="currentStepKey === 'done'" class="step-content">
      <div class="done-card">
        <div class="done-icon">✓</div>
        <div class="done-title">签约成功</div>
        <div class="done-subtitle">协议已生效，祝您工作顺利</div>

        <div class="done-info">
          <div class="done-info-item">
            <span class="done-label">岗位名称</span>
            <span class="done-value">{{ agreementInfo.job_title }}</span>
          </div>
          <div class="done-info-item">
            <span class="done-label">薪资标准</span>
            <span class="done-value">¥{{ agreementInfo.salary_amount }}/{{ agreementInfo.salary_type }}</span>
          </div>
          <div class="done-info-item">
            <span class="done-label">工作期限</span>
            <span class="done-value">{{ agreementInfo.work_start_date }} 至 {{ agreementInfo.work_end_date }}</span>
          </div>
        </div>

        <div class="done-tips">
          <div class="tip-item">💡 薪资由平台第三方监管，确保按时发放</div>
          <div class="tip-item">💡 工作期间遇到问题可随时联系平台客服</div>
        </div>
      </div>
    </div>

    <div class="bottom-bar">
      <button
        v-if="currentStepKey !== 'done'"
        class="btn-secondary"
        @click="prevStep"
      >
        {{ currentStep === 0 ? '取消' : '上一步' }}
      </button>
      <button
        class="btn-primary"
        :class="{ active: canProceed || currentStepKey === 'done' }"
        :disabled="!canProceed && currentStepKey !== 'done'"
        :loading="isSigning"
        @click="currentStepKey === 'done' ? finishSign() : nextStep()"
      >
        {{ nextButtonText }}
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.agreement-sign-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}

.steps-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: var(--spacing-base);
  background-color: var(--color-bg);
  position: relative;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
  z-index: 1;
}

.step-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--color-bg-tertiary);
  color: var(--color-text-disabled);
  font-size: var(--font-size-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
  transition: all 0.3s ease;

  .active &,
  .done & {
    background-color: var(--color-primary);
    color: #fff;
  }
}

.step-label {
  font-size: 12px;
  color: var(--color-text-disabled);
  white-space: nowrap;

  .active &,
  .done & {
    color: var(--color-text);
  }
}

.step-line {
  position: absolute;
  top: 16px;
  left: 60%;
  right: -40%;
  height: 2px;
  background-color: var(--color-border);
  z-index: 0;

  .done & {
    background-color: var(--color-primary);
  }
}

.step-content {
  padding: var(--spacing-sm);
}

.agreement-card,
.confirm-card,
.sign-card,
.done-card {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
}

.agreement-header {
  text-align: center;
  padding-bottom: var(--spacing-base);
  border-bottom: 1px solid var(--color-border);
  margin-bottom: var(--spacing-base);
}

.agreement-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.agreement-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.agreement-body {
  max-height: 50vh;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.agreement-section {
  margin-bottom: var(--spacing-base);
}

.section-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.info-row {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: 4px;
  line-height: 1.6;

  .highlight {
    color: var(--color-accent);
    font-weight: 600;
  }
}

.info-label {
  color: var(--color-text-secondary);
}

.info-value {
  color: var(--color-text);
}

.paragraph {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 4px;
}

.agreement-footer,
.confirm-footer,
.sign-footer {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  margin-bottom: var(--spacing-sm);
  cursor: pointer;

  &:last-child {
    margin-bottom: 0;
  }
}

.checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  margin-right: 8px;
  margin-top: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 12px;
  color: #fff;
  transition: all 0.2s ease;

  &.checked {
    background-color: var(--color-primary);
    border-color: var(--color-primary);
  }
}

.checkbox-text {
  font-size: var(--font-size-sm);
  color: var(--color-text);
  line-height: 1.5;
}

.link {
  color: var(--color-primary);
  cursor: pointer;
}

.confirm-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.confirm-tip {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-base);
}

.info-block {
  margin-bottom: var(--spacing-base);

  &:last-child {
    margin-bottom: 0;
  }
}

.block-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
  padding-left: 8px;
  border-left: 3px solid var(--color-primary);
}

.info-list {
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border);

  &:last-child {
    border-bottom: none;
  }

  &.highlight .item-value {
    color: var(--color-accent);
    font-weight: 600;
  }
}

.item-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.item-value {
  font-size: var(--font-size-sm);
  color: var(--color-text);
  text-align: right;
}

.sign-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
  text-align: center;
}

.sign-tip {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-base);
  text-align: center;
}

.signature-wrapper {
  position: relative;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-sm);
  margin-bottom: var(--spacing-base);
  background-color: #fafafa;
}

.signature-pad {
  width: 100%;
  height: 200px;
  display: block;
}

.signature-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: var(--font-size-base);
  color: var(--color-text-disabled);
  pointer-events: none;
}

.clear-btn {
  display: block;
  margin: 0 auto;
  padding: 8px 20px;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  background-color: var(--color-bg);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    background-color: var(--color-bg-secondary);
  }
}

.sign-tip-bottom {
  font-size: 12px;
  color: var(--color-text-secondary);
  text-align: center;
  line-height: 1.5;
}

.done-card {
  text-align: center;
  padding: var(--spacing-xl) var(--spacing-base);
}

.done-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: var(--color-success-bg);
  color: var(--color-success);
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-base);
  animation: donePop 0.5s cubic-bezier(0.68, -0.55, 0.27, 1.55);
}

@keyframes donePop {
  0% { transform: scale(0); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.done-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.done-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-lg);
}

.done-info {
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  padding: var(--spacing-sm);
  margin-bottom: var(--spacing-base);
  text-align: left;
}

.done-info-item {
  display: flex;
  justify-content: space-between;
  padding: var(--spacing-xs) 0;
}

.done-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.done-value {
  font-size: var(--font-size-sm);
  color: var(--color-text);
  font-weight: 500;
}

.done-tips {
  text-align: left;
}

.tip-item {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: 6px;
  line-height: 1.5;

  &:last-child {
    margin-bottom: 0;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-base);
  padding-bottom: calc(var(--spacing-sm) + env(safe-area-inset-bottom));
  background-color: var(--color-bg);
  border-top: 1px solid var(--color-border);
}

.btn-secondary {
  flex: 1;
  height: 48px;
  border: 1px solid var(--color-border);
  border-radius: 24px;
  background-color: var(--color-bg);
  color: var(--color-text);
  font-size: var(--font-size-lg);
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    background-color: var(--color-bg-secondary);
  }
}

.btn-primary {
  flex: 2;
  height: 48px;
  border: none;
  border-radius: 24px;
  background-color: var(--color-text-disabled);
  color: #fff;
  font-size: var(--font-size-lg);
  font-weight: 500;
  cursor: not-allowed;
  transition: all 0.2s ease;

  &.active {
    background-color: var(--color-primary);
    cursor: pointer;

    &:active {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
}
</style>
