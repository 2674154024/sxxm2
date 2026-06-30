<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()

const faqList = ref([
  {
    id: 1,
    question: '刷单返现是真的吗？',
    answer: '所有刷单行为都是诈骗！骗子先以小额返现为诱饵，等你投入大额资金后就会拉黑失联。请记住：刷单本身就是违法行为，凡是要求你先垫资的都是诈骗！',
    expanded: false,
  },
  {
    id: 2,
    question: '入职需要交押金/服装费吗？',
    answer: '不需要！《劳动合同法》明确规定，用人单位不得以任何名义向劳动者收取财物。凡是要求先交钱的都是骗子，包括但不限于：押金、服装费、培训费、体检费、工牌费等。',
    expanded: false,
  },
  {
    id: 3,
    question: '高薪日结靠谱吗？',
    answer: '谨防"高薪日结"陷阱。日结工资本身没问题，但如果薪资明显高于市场水平（比如普通兼职日薪500+），就要警惕了。很可能是诈骗的诱饵，或者是违法活动。',
    expanded: false,
  },
  {
    id: 4,
    question: '对方让我下载不知名APP怎么办？',
    answer: '立即停止！很多诈骗分子会让你下载"任务APP""会议APP"等，这些APP往往藏有木马病毒或用于诱导转账。我们平台内的沟通和交易都不需要下载第三方APP。',
    expanded: false,
  },
  {
    id: 5,
    question: '遇到可疑情况怎么办？',
    answer: '1. 立即停止沟通，不要转账、不要透露个人信息\n2. 在平台内点击"举报"按钮，我们会第一时间处理\n3. 如果已经遭受财产损失，请立即拨打110报警\n4. 保留好聊天记录、转账凭证等证据',
    expanded: false,
  },
])

const cases = ref([
  {
    id: 1,
    title: '刷单诈骗',
    type: '常见骗局',
    desc: '以"足不出户，日赚300"为诱饵，先返小利，后要求大额垫付，最终失联。',
    icon: '⚠️',
    color: 'danger',
  },
  {
    id: 2,
    title: '押金诈骗',
    type: '入职陷阱',
    desc: '以"服装费""培训费"等名义要求先交钱，交完后拉黑消失。',
    icon: '💸',
    color: 'warning',
  },
  {
    id: 3,
    title: '传销陷阱',
    type: '违法活动',
    desc: '以"创业""加盟"为幌子，要求发展下线获取佣金，本质是传销。',
    icon: '🔗',
    color: 'danger',
  },
  {
    id: 4,
    title: '信息泄露',
    type: '隐私风险',
    desc: '要求提供身份证、银行卡密码等敏感信息，用于盗刷或其他违法用途。',
    icon: '🔐',
    color: 'warning',
  },
])

function toggleFaq(id: number) {
  const item = faqList.value.find((f) => f.id === id)
  if (item) {
    item.expanded = !item.expanded
  }
}

function goReport() {
  router.push('/complaint/create')
}

function goCaseDetail(id: number) {
  console.log('案例详情', id)
}
</script>

<template>
  <div class="safety-page">
    <NavBar title="安全中心" show-back />

    <div class="safety-banner">
      <div class="safety-banner__bg"></div>
      <div class="safety-banner__content">
        <div class="safety-banner__icon">🛡️</div>
        <div class="safety-banner__text">
          <div class="safety-banner__title">安全兼职，放心工作</div>
          <div class="safety-banner__desc">平台三重安全保障，守护你的每一次兼职</div>
        </div>
      </div>
    </div>

    <div class="safety-section">
      <div class="section-title">
        <span class="section-title__text">典型案例</span>
        <span class="section-title__link" @click="goCaseDetail(0)">查看全部 ›</span>
      </div>

      <div class="case-grid">
        <div
          v-for="item in cases"
          :key="item.id"
          class="case-card"
          @click="goCaseDetail(item.id)"
        >
          <div class="case-card__icon">{{ item.icon }}</div>
          <div class="case-card__info">
            <div class="case-card__title">{{ item.title }}</div>
            <div class="case-card__type">{{ item.type }}</div>
          </div>
          <div class="case-card__desc ellipsis-2">{{ item.desc }}</div>
        </div>
      </div>
    </div>

    <div class="safety-section">
      <div class="section-title">
        <span class="section-title__text">防骗FAQ</span>
      </div>

      <div class="faq-list">
        <div
          v-for="item in faqList"
          :key="item.id"
          class="faq-item"
          :class="{ 'is-expanded': item.expanded }"
        >
          <div class="faq-item__header" @click="toggleFaq(item.id)">
            <div class="faq-item__q">
              <span class="faq-item__badge">Q</span>
              <span class="faq-item__question">{{ item.question }}</span>
            </div>
            <div class="faq-item__arrow">{{ item.expanded ? '−' : '+' }}</div>
          </div>
          <transition name="expand">
            <div v-if="item.expanded" class="faq-item__answer">
              <span class="faq-item__badge faq-item__badge--a">A</span>
              <span class="faq-item__answer-text">{{ item.answer }}</span>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <div class="safety-actions">
      <button class="btn btn--block btn--danger btn--lg safety-actions__btn" @click="goReport">
        <span>🚨</span>
        一键举报可疑岗位
      </button>
      <div class="safety-actions__tip">
        如遇紧急情况，请直接拨打 <span class="safety-actions__phone">110</span> 报警
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.safety-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: 24px;
}

.safety-banner {
  position: relative;
  margin: 12px 16px;
  height: 100px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  color: #fff;

  &__bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  }

  &__content {
    position: relative;
    display: flex;
    align-items: center;
    height: 100%;
    padding: 0 20px;
    gap: 16px;
  }

  &__icon {
    font-size: 40px;
  }

  &__title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 6px;
  }

  &__desc {
    font-size: 13px;
    opacity: 0.9;
  }
}

.safety-section {
  margin: 16px;
  background: #fff;
  border-radius: var(--radius-base);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;

  &__text {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
  }

  &__link {
    font-size: 13px;
    color: var(--color-primary);
    cursor: pointer;
  }
}

.case-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.case-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-base);
  padding: 12px;
  cursor: pointer;
  transition: all var(--transition-fast);

  &:active {
    transform: scale(0.97);
  }

  &__icon {
    font-size: 24px;
    margin-bottom: 8px;
  }

  &__info {
    display: flex;
    align-items: baseline;
    gap: 6px;
    margin-bottom: 6px;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text);
  }

  &__type {
    font-size: 11px;
    color: var(--color-text-disabled);
  }

  &__desc {
    font-size: 12px;
    color: var(--color-text-secondary);
    line-height: 1.5;
  }
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.faq-item {
  border-radius: var(--radius-base);
  background: var(--color-bg-secondary);
  overflow: hidden;
  transition: background-color var(--transition-fast);

  &.is-expanded {
    background: #f0f7ff;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    cursor: pointer;
  }

  &__q {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  &__badge {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    background: var(--color-primary);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;

    &--a {
      background: var(--color-success);
    }
  }

  &__question {
    flex: 1;
    font-size: 14px;
    color: var(--color-text);
    font-weight: 500;
  }

  &__arrow {
    flex-shrink: 0;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: var(--color-text-secondary);
  }

  &__answer {
    display: flex;
    gap: 8px;
    padding: 0 14px 14px;
  }

  &__answer-text {
    flex: 1;
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.6;
    white-space: pre-line;
  }
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s var(--ease-out-expo);
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 200px;
}

.safety-actions {
  padding: 16px;
  text-align: center;

  &__btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  &__tip {
    font-size: 12px;
    color: var(--color-text-secondary);
    margin-top: 12px;
  }

  &__phone {
    color: var(--color-danger);
    font-weight: 500;
  }
}
</style>
