<template>
  <view class="page">
    <view class="safety-header">
      <text class="header-title">安全中心</text>
      <text class="header-subtitle">守护您的兼职安全</text>
    </view>

    <view class="section">
      <text class="section-title">🔍 安全警示</text>
      <view class="case-list">
        <scroll-view scroll-x class="case-scroll">
          <view class="case-card" v-for="caseItem in cases" :key="caseItem.case_id">
            <view class="case-image">
              <text class="case-icon">{{ getCategoryIcon(caseItem.category) }}</text>
            </view>
            <text class="case-title">{{ caseItem.title }}</text>
            <text class="case-summary">{{ caseItem.summary }}</text>
            <view class="case-category" :class="getCategoryClass(caseItem.category)">
              {{ getCategoryText(caseItem.category) }}
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">📖 防骗指南</text>
      <view class="faq-list">
        <view class="faq-item" v-for="(faq, index) in faqs" :key="index">
          <view class="faq-header" @click="toggleFaq(index)">
            <text class="faq-question">{{ faq.question }}</text>
            <text class="faq-arrow" :class="{ expanded: expandedIndex === index }">▼</text>
          </view>
          <view class="faq-content" v-if="expandedIndex === index">
            <text class="faq-answer">{{ faq.answer }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="report-section">
      <button class="report-btn" @click="goToComplaint">
        <text class="report-icon">⚠️</text>
        <text class="report-text">一键举报</text>
      </button>
      <text class="report-tip">如遇欺诈行为，请立即举报</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { safetyApi, type SafetyCase, type FAQItem } from '@/api/safety'

const cases = ref<SafetyCase[]>([])
const faqs = ref<FAQItem[]>([])
const expandedIndex = ref<number | null>(null)

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
    },
    {
      question: '兼职过程中受伤怎么办？',
      answer: '兼职过程中如遇意外受伤，请按以下步骤处理：1. 及时就医并保留医疗记录；2. 联系企业负责人说明情况；3. 在平台提交申诉，提供受伤证明；4. 如购买了意外险，联系保险公司理赔。平台将协助协商处理。'
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
  padding-bottom: 160rpx;
}

.safety-header {
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  padding: 48rpx 32rpx;
  text-align: center;
}

.header-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #FFFFFF;
  display: block;
  margin-bottom: 12rpx;
}

.header-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.section {
  background-color: #FFFFFF;
  margin: 24rpx;
  border-radius: 16rpx;
  padding: 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 24rpx;
  display: block;
}

.case-scroll {
  white-space: nowrap;
}

.case-card {
  display: inline-block;
  width: 280rpx;
  margin-right: 20rpx;
  background-color: #F8F9FA;
  border-radius: 16rpx;
  padding: 24rpx;
  vertical-align: top;
}

.case-image {
  width: 120rpx;
  height: 120rpx;
  background-color: #FFFFFF;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.case-icon {
  font-size: 60rpx;
}

.case-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #1F2329;
  margin-bottom: 8rpx;
  display: block;
  white-space: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.case-summary {
  font-size: 22rpx;
  color: #86909C;
  margin-bottom: 16rpx;
  display: block;
  white-space: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
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
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
}

.faq-question {
  font-size: 28rpx;
  color: #1F2329;
  flex: 1;
}

.faq-arrow {
  font-size: 20rpx;
  color: #C9CDD4;
  transition: transform 0.3s;
}

.faq-arrow.expanded {
  transform: rotate(180deg);
}

.faq-content {
  padding: 0 0 24rpx;
}

.faq-answer {
  font-size: 26rpx;
  color: #4E5969;
  line-height: 1.8;
}

.report-section {
  padding: 32rpx 24rpx;
  background-color: #FFFFFF;
  margin: 24rpx;
  border-radius: 16rpx;
  text-align: center;
}

.report-btn {
  background-color: #F53F3F;
  color: #FFFFFF;
  font-size: 34rpx;
  font-weight: bold;
  padding: 32rpx;
  border-radius: 48rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24rpx;
}

.report-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.report-text {
  font-size: 34rpx;
}

.report-tip {
  font-size: 24rpx;
  color: #86909C;
}
</style>