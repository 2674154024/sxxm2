<template>
  <view class="page">
    <view class="safety-header">
      <text class="header-title">安全中心</text>
      <text class="header-subtitle">守护您的兼职安全</text>
    </view>

    <view class="tip-card">
      <view class="tip-icon-wrap">
        <text class="tip-icon">🔔</text>
      </view>
      <view class="tip-content">
        <text class="tip-title">安全提示</text>
        <text class="tip-desc">兼职前请核实企业信息，切勿缴纳任何押金费用</text>
      </view>
      <view class="tip-arrow">›</view>
    </view>

    <view class="quick-actions">
      <view class="action-item danger" @click="goToComplaint">
        <view class="action-icon-wrap">
          <text class="action-icon">⚠️</text>
        </view>
        <text class="action-text">投诉举报</text>
      </view>
      <view class="action-item warning" @click="goToHelp">
        <view class="action-icon-wrap">
          <text class="action-icon">🆘</text>
        </view>
        <text class="action-text">一键求助</text>
      </view>
      <view class="action-item primary" @click="goToRisk">
        <view class="action-icon-wrap">
          <text class="action-icon">📊</text>
        </view>
        <text class="action-text">风险预警</text>
      </view>
      <view class="action-item success" @click="goToKnowledge">
        <view class="action-icon-wrap">
          <text class="action-icon">📚</text>
        </view>
        <text class="action-text">安全知识</text>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">⚠️ 风险预警</text>
        <text class="section-more">更多</text>
      </view>
      <view class="risk-list">
        <view class="risk-item" v-for="(risk, index) in riskList" :key="index">
          <view class="risk-dot" :class="risk.level"></view>
          <view class="risk-content">
            <text class="risk-title">{{ risk.title }}</text>
            <text class="risk-desc">{{ risk.desc }}</text>
          </view>
          <text class="risk-arrow">›</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">🔍 安全警示</text>
        <text class="section-more">更多</text>
      </view>
      <scroll-view scroll-x class="case-scroll">
        <view class="case-card" v-for="caseItem in cases" :key="caseItem.case_id">
          <view class="case-image">
            <text class="case-icon">{{ getCategoryIcon(caseItem.category) }}</text>
          </view>
          <view class="case-info">
            <text class="case-title">{{ caseItem.title }}</text>
            <text class="case-summary">{{ caseItem.summary }}</text>
          </view>
          <view class="case-category" :class="getCategoryClass(caseItem.category)">
            {{ getCategoryText(caseItem.category) }}
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">📖 安全知识</text>
        <text class="section-more">全部</text>
      </view>
      <view class="faq-list">
        <view class="faq-item" v-for="(faq, index) in faqs" :key="index" @click="toggleFaq(index)">
          <view class="faq-header">
            <view class="faq-icon-wrap">
              <text class="faq-icon">Q</text>
            </view>
            <text class="faq-question">{{ faq.question }}</text>
            <text class="faq-arrow" :class="{ expanded: expandedIndex === index }">›</text>
          </view>
          <view class="faq-content" v-if="expandedIndex === index">
            <text class="faq-answer">{{ faq.answer }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="sos-section">
      <view class="sos-card" @click="handleSOS">
        <view class="sos-icon-wrap">
          <text class="sos-icon">🆘</text>
        </view>
        <view class="sos-content">
          <text class="sos-title">一键求助</text>
          <text class="sos-desc">遇到紧急情况，立即联系平台安全专员</text>
        </view>
        <view class="sos-btn">
          <text class="sos-btn-text">立即求助</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { safetyApi, type SafetyCase, type FAQItem } from '@/api/safety'

const cases = ref<SafetyCase[]>([])
const faqs = ref<FAQItem[]>([])
const expandedIndex = ref<number | null>(null)

const riskList = ref([
  { title: '警惕高佣金刷单', desc: '日赚300元的兼职多为诈骗', level: 'high' },
  { title: '切勿缴纳押金', desc: '正规企业不会收取任何费用', level: 'high' },
  { title: '面试地点需核实', desc: '偏僻地点请勿单独前往', level: 'medium' }
])

const getCategoryIcon = (category: string) => {
  const iconMap: Record<string, string> = {
    deposit_scam: '💰',
    '刷单陷阱': '⚡',
    information_leak: '🔒',
    false_recruitment: '🎭'
  }
  return iconMap[category] || '⚠️'
}

const getCategoryClass = (category: string) => {
  const classMap: Record<string, string> = {
    deposit_scam: 'category-scam',
    '刷单陷阱': 'category-fraud',
    information_leak: 'category-leak',
    false_recruitment: 'category-false'
  }
  return classMap[category] || 'category-default'
}

const getCategoryText = (category: string) => {
  const textMap: Record<string, string> = {
    deposit_scam: '押金诈骗',
    '刷单陷阱': '刷单陷阱',
    information_leak: '信息泄露',
    false_recruitment: '虚假招聘'
  }
  return textMap[category] || '其他'
}

const toggleFaq = (index: number) => {
  expandedIndex.value = expandedIndex.value === index ? null : index
}

const goToComplaint = () => {
  uni.navigateTo({ url: '/pages/student/complaint/create' })
}

const goToHelp = () => {
  uni.showToast({ title: '求助功能开发中', icon: 'none' })
}

const goToRisk = () => {
  uni.showToast({ title: '风险预警开发中', icon: 'none' })
}

const goToKnowledge = () => {
  uni.showToast({ title: '安全知识开发中', icon: 'none' })
}

const handleSOS = () => {
  uni.showModal({
    title: '一键求助',
    content: '确认要发起求助吗？平台安全专员将立即与您联系。',
    confirmText: '确认求助',
    confirmColor: '#FF4D4F',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '求助已发送，专员将尽快联系您', icon: 'success' })
      }
    }
  })
}

const loadData = () => {
  uni.showLoading({ title: '加载中...' })
  
  Promise.all([
    safetyApi.getSafetyCases(),
    safetyApi.getFAQ()
  ]).then(([casesRes, faqRes]) => {
    cases.value = casesRes.data
    faqs.value = faqRes.data
    uni.hideLoading()
  }).catch(() => {
    uni.hideLoading()
    loadMockData()
  })
}

const loadMockData = () => {
  cases.value = [
    {
      case_id: '1',
      title: '警惕押金诈骗',
      summary: '某学生应聘兼职时被要求缴纳300元押金后失联',
      image_url: '',
      category: 'deposit_scam'
    },
    {
      case_id: '2',
      title: '刷单返利陷阱',
      summary: '高佣金刷单实为诈骗，多人被骗金额数千元',
      image_url: '',
      category: '刷单陷阱'
    },
    {
      case_id: '3',
      title: '个人信息泄露',
      summary: '虚假招聘套取身份证、银行卡信息',
      image_url: '',
      category: 'information_leak'
    },
    {
      case_id: '4',
      title: '虚假招聘曝光',
      summary: '岗位信息与实际不符，求职者被骗',
      image_url: '',
      category: 'false_recruitment'
    }
  ]

  faqs.value = [
    {
      question: '如何识别虚假招聘？',
      answer: '虚假招聘通常具有以下特征：1. 以高薪诱惑，承诺日赚数百元；2. 要求先缴纳押金、培训费等费用；3. 面试地点偏僻，要求单独面谈；4. 公司名称模糊，无法提供营业执照。建议通过平台认证企业投递，不向陌生账号转账。'
    },
    {
      question: '遇到押金诈骗怎么办？',
      answer: '如遇押金诈骗，请立即采取以下措施：1. 保留转账记录、聊天记录等证据；2. 在平台内举报该企业；3. 拨打110报警或向当地派出所报案；4. 联系支付平台申请冻结交易。平台将协助警方调查。'
    },
    {
      question: '如何保护个人信息安全？',
      answer: '保护个人信息安全建议：1. 不在公开场合泄露身份证号、银行卡号等敏感信息；2. 不点击不明链接，不下载可疑APP；3. 设置复杂密码，定期更换；4. 开启账户安全通知；5. 仅向可信企业提供必要信息。'
    }
  ]
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #F2F3F5;
  padding-bottom: 40rpx;
}

.safety-header {
  background: linear-gradient(180deg, #52C41A 0%, #73D13D 100%);
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

.tip-card {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  margin: -40rpx 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.tip-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.tip-icon {
  font-size: 36rpx;
}

.tip-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tip-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 6rpx;
}

.tip-desc {
  font-size: 24rpx;
  color: #86909C;
  line-height: 1.4;
}

.tip-arrow {
  font-size: 32rpx;
  color: #C9CDD4;
  margin-left: 12rpx;
}

.quick-actions {
  display: flex;
  justify-content: space-between;
  padding: 0 24rpx;
  margin-bottom: 24rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 160rpx;
}

.action-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.action-item.danger .action-icon-wrap {
  background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%);
}

.action-item.warning .action-icon-wrap {
  background: linear-gradient(135deg, #FAAD14 0%, #FFC53D 100%);
}

.action-item.primary .action-icon-wrap {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
}

.action-item.success .action-icon-wrap {
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
}

.action-icon {
  font-size: 44rpx;
}

.action-text {
  font-size: 24rpx;
  color: #4E5969;
  font-weight: 500;
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
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2329;
}

.section-more {
  font-size: 24rpx;
  color: #86909C;
}

.risk-list {
  padding: 0;
}

.risk-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F2F3F5;
}

.risk-item:last-child {
  border-bottom: none;
}

.risk-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.risk-dot.high {
  background-color: #FF4D4F;
}

.risk-dot.medium {
  background-color: #FAAD14;
}

.risk-dot.low {
  background-color: #52C41A;
}

.risk-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.risk-title {
  font-size: 28rpx;
  color: #1F2329;
  margin-bottom: 6rpx;
}

.risk-desc {
  font-size: 24rpx;
  color: #86909C;
}

.risk-arrow {
  font-size: 28rpx;
  color: #C9CDD4;
  margin-left: 12rpx;
}

.case-scroll {
  white-space: nowrap;
  margin: 0 -24rpx;
  padding: 0 24rpx;
}

.case-card {
  display: inline-block;
  width: 320rpx;
  margin-right: 20rpx;
  background-color: #F7F8FA;
  border-radius: 16rpx;
  padding: 24rpx;
  vertical-align: top;
  white-space: normal;
}

.case-image {
  width: 100%;
  height: 160rpx;
  background-color: #FFFFFF;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.case-icon {
  font-size: 56rpx;
}

.case-info {
  margin-bottom: 16rpx;
}

.case-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 8rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.case-summary {
  font-size: 22rpx;
  color: #86909C;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.case-category {
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 16rpx;
  display: inline-block;
}

.case-category.category-scam {
  background-color: rgba(255, 77, 79, 0.1);
  color: #FF4D4F;
}

.case-category.category-fraud {
  background-color: rgba(250, 173, 20, 0.1);
  color: #FAAD14;
}

.case-category.category-leak {
  background-color: rgba(22, 93, 255, 0.1);
  color: #165DFF;
}

.case-category.category-false {
  background-color: rgba(134, 144, 156, 0.1);
  color: #86909C;
}

.faq-list {
  padding: 0;
}

.faq-item {
  border-bottom: 1rpx solid #F2F3F5;
}

.faq-item:last-child {
  border-bottom: none;
}

.faq-header {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
}

.faq-icon-wrap {
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.faq-icon {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: bold;
}

.faq-question {
  flex: 1;
  font-size: 28rpx;
  color: #1F2329;
  line-height: 1.4;
}

.faq-arrow {
  font-size: 28rpx;
  color: #C9CDD4;
  margin-left: 12rpx;
  transition: transform 0.3s;
}

.faq-arrow.expanded {
  transform: rotate(90deg);
}

.faq-content {
  padding: 0 0 20rpx 56rpx;
}

.faq-answer {
  font-size: 26rpx;
  color: #4E5969;
  line-height: 1.8;
}

.sos-section {
  padding: 0 24rpx;
}

.sos-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%);
  border-radius: 16rpx;
  padding: 28rpx 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 77, 79, 0.3);
}

.sos-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.sos-icon {
  font-size: 40rpx;
}

.sos-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.sos-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 6rpx;
}

.sos-desc {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.4;
}

.sos-btn {
  background-color: #FFFFFF;
  padding: 16rpx 28rpx;
  border-radius: 32rpx;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.sos-btn-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #FF4D4F;
}
</style>
