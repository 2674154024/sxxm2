<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { getCaseDetail, type CaseDetail } from '@/api/safety'

const router = useRouter()

const route = useRoute()
const caseId = parseInt(route.params.id as string)

const caseDetail = ref<CaseDetail | null>(null)

const mockCases: CaseDetail[] = [
  {
    id: 1,
    title: '刷单诈骗',
    type: '常见骗局',
    icon: '⚠️',
    color: 'danger',
    desc: '以"足不出户，日赚300"为诱饵，先返小利，后要求大额垫付，最终失联。',
    content: '李某是一名大学生，在网上看到"刷单兼职，日赚300+"的广告。对方要求她先垫付资金购买商品，承诺完成任务后返还本金并支付佣金。李某起初只投了50元，很快就收到了55元的返现。尝到甜头后，她陆续投入了8000元，然而这次对方以"任务未完成"为由拒绝返现，并要求继续充值。李某这才意识到被骗，报警后警方介入调查，但资金已被转移。',
    warning: [
      '刷单本身就是违法行为',
      '任何要求先垫资的兼职都是诈骗',
      '低投入高回报的承诺不可信',
    ],
    tips: [
      '正规兼职不会要求你先交钱',
      '如果对方要求下载不知名APP，立即停止',
      '发现被骗后立即报警并保留证据',
    ],
  },
  {
    id: 2,
    title: '押金诈骗',
    type: '入职陷阱',
    icon: '💸',
    color: 'warning',
    desc: '以"服装费""培训费"等名义要求先交钱，交完后拉黑消失。',
    content: '张某通过某平台找到一份"商场导购"兼职，对方称入职需要缴纳300元服装费和200元培训费，承诺工作满一个月后全额退还。张某交了钱后，对方又以"办理上岗证"为由要求再交100元。张某产生怀疑，要求退款，却发现已被对方拉黑。后经调查，该"商场导购"岗位根本不存在。',
    warning: [
      '《劳动合同法》明确禁止用人单位收取押金',
      '服装费、培训费等名义收费都是违法的',
      '要求转账到个人账户的都是骗子',
    ],
    tips: [
      '入职前要求收费的一律拒绝',
      '通过正规平台寻找兼职，签订电子合同',
      '保留聊天记录和转账凭证',
    ],
  },
  {
    id: 3,
    title: '传销陷阱',
    type: '违法活动',
    icon: '🔗',
    color: 'danger',
    desc: '以"创业""加盟"为幌子，要求发展下线获取佣金，本质是传销。',
    content: '王某的高中同学邀请她参加一个"互联网创业项目"，称只要投资3000元成为会员，就能获得推广佣金。王某加入后发现，所谓的"创业项目"根本没有实际产品，赚钱的方式只有发展下线。她意识到这是传销，但已经投入了5000元。后来该组织被警方取缔，王某的资金也无法追回。',
    warning: [
      '要求发展下线获取佣金的可能是传销',
      '没有实际产品的"创业项目"要警惕',
      '高门槛入会、承诺快速致富的多是骗局',
    ],
    tips: [
      '了解项目是否有实际产品和盈利模式',
      '拒绝拉人头式的推广方式',
      '发现传销活动及时报警',
    ],
  },
  {
    id: 4,
    title: '信息泄露',
    type: '隐私风险',
    icon: '🔐',
    color: 'warning',
    desc: '要求提供身份证、银行卡密码等敏感信息，用于盗刷或其他违法用途。',
    content: '陈某在某兼职平台注册时，被要求上传身份证正反面照片和手持身份证照片。几天后，她收到银行短信通知，银行卡被消费了2万元。经调查，她的身份信息被不法分子用于注册网贷并盗刷。原来，该兼职平台存在信息泄露漏洞，她的个人信息被泄露并出售。',
    warning: [
      '不要轻易上传身份证照片',
      '银行卡密码绝不透露给任何人',
      '警惕要求提供验证码的请求',
    ],
    tips: [
      '只在正规平台注册并完善隐私设置',
      '开启银行卡短信提醒和交易限额',
      '定期检查账户交易记录',
    ],
  },
]

onMounted(() => {
  loadCaseDetail()
})

async function loadCaseDetail() {
  try {
    const res = await getCaseDetail(caseId)
    if (res.data) {
      caseDetail.value = res.data
      return
    }
  } catch (error) {
    console.error('加载案例详情失败', error)
  }
  caseDetail.value = mockCases.find((c) => c.id === caseId) || mockCases[0]
}

function getColorClass(color: string) {
  const colors: Record<string, string> = {
    danger: 'var(--color-danger)',
    warning: 'var(--color-warning)',
    success: 'var(--color-success)',
    primary: 'var(--color-primary)',
  }
  return colors[color] || colors.primary
}

function goReport() {
  router.push('/complaint/create')
}
</script>

<template>
  <div class="case-detail-page">
    <NavBar :title="caseDetail?.title || '案例详情'" show-back />

    <div v-if="caseDetail" class="case-content">
      <div class="case-header">
        <div class="case-header__icon">{{ caseDetail.icon }}</div>
        <div class="case-header__info">
          <div class="case-header__title">{{ caseDetail.title }}</div>
          <div class="case-header__type" :style="{ backgroundColor: getColorClass(caseDetail.color) + '20', color: getColorClass(caseDetail.color) }">
            {{ caseDetail.type }}
          </div>
        </div>
      </div>

      <div class="case-section">
        <div class="section-title">
          <span class="section-title__text">案例详情</span>
        </div>
        <div class="case-content__desc">{{ caseDetail.desc }}</div>
        <div class="case-content__body">{{ caseDetail.content }}</div>
      </div>

      <div class="case-section">
        <div class="section-title">
          <span class="section-title__text">⚠️ 风险警示</span>
        </div>
        <div class="warning-list">
          <div v-for="(item, index) in caseDetail.warning" :key="index" class="warning-item">
            <span class="warning-item__dot"></span>
            <span class="warning-item__text">{{ item }}</span>
          </div>
        </div>
      </div>

      <div class="case-section">
        <div class="section-title">
          <span class="section-title__text">💡 防骗提示</span>
        </div>
        <div class="tips-list">
          <div v-for="(item, index) in caseDetail.tips" :key="index" class="tips-item">
            <span class="tips-item__num">{{ index + 1 }}</span>
            <span class="tips-item__text">{{ item }}</span>
          </div>
        </div>
      </div>

      <div class="case-actions">
        <button class="btn btn--block btn--primary btn--lg" @click="goReport">
          遇到类似情况？立即举报
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.case-detail-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: 24px;
}

.case-header {
  margin: 12px 16px;
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: var(--shadow-sm);

  &__icon {
    font-size: 48px;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__title {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text);
  }

  &__type {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    width: fit-content;
  }
}

.case-section {
  margin: 12px 16px;
  background: #fff;
  border-radius: var(--radius-base);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.section-title {
  margin-bottom: 12px;

  &__text {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
  }
}

.case-content {
  &__desc {
    font-size: 14px;
    color: var(--color-text-secondary);
    line-height: 1.6;
    margin-bottom: 12px;
    padding: 12px;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-base);
  }

  &__body {
    font-size: 14px;
    color: var(--color-text);
    line-height: 1.8;
    text-indent: 2em;
  }
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.warning-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  background: var(--color-danger-bg);
  border-radius: var(--radius-base);

  &__dot {
    flex-shrink: 0;
    width: 6px;
    height: 6px;
    background: var(--color-danger);
    border-radius: 50%;
    margin-top: 7px;
  }

  &__text {
    font-size: 14px;
    color: var(--color-danger);
    line-height: 1.6;
  }
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tips-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;

  &__num {
    flex-shrink: 0;
    width: 24px;
    height: 24px;
    background: var(--color-primary);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__text {
    flex: 1;
    font-size: 14px;
    color: var(--color-text);
    line-height: 1.6;
    padding-top: 3px;
  }
}

.case-actions {
  padding: 16px;
  margin-top: 12px;

  .btn {
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  }
}
</style>