<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { getCaseList, type CaseItem } from '@/api/safety'

const router = useRouter()

const cases = ref<CaseItem[]>([
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
  {
    id: 5,
    title: '网络钓鱼',
    type: '账号安全',
    desc: '通过虚假网站或短信骗取账号密码，导致账号被盗、资金损失。',
    icon: '🎣',
    color: 'danger',
  },
  {
    id: 6,
    title: '高薪诱惑',
    type: '求职陷阱',
    desc: '以"高薪日结"为诱饵，实际工作内容与承诺不符或涉嫌违法。',
    icon: '💰',
    color: 'warning',
  },
])

onMounted(() => {
  loadCases()
})

async function loadCases() {
  try {
    const res = await getCaseList()
    if (res.data) {
      cases.value = res.data
    }
  } catch (error) {
    console.error('加载案例列表失败', error)
  }
}

function goDetail(id: number) {
  router.push(`/case/${id}`)
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
</script>

<template>
  <div class="case-list-page">
    <NavBar title="典型案例" show-back />

    <div class="case-intro">
      <div class="case-intro__icon">🛡️</div>
      <div class="case-intro__text">
        <div class="case-intro__title">学习防骗知识</div>
        <div class="case-intro__desc">了解常见骗局，保护自身安全</div>
      </div>
    </div>

    <div class="case-list">
      <div
        v-for="item in cases"
        :key="item.id"
        class="case-item"
        @click="goDetail(item.id)"
      >
        <div class="case-item__icon">{{ item.icon }}</div>
        <div class="case-item__content">
          <div class="case-item__header">
            <span class="case-item__title">{{ item.title }}</span>
            <span class="case-item__type" :style="{ backgroundColor: getColorClass(item.color) + '20', color: getColorClass(item.color) }">
              {{ item.type }}
            </span>
          </div>
          <div class="case-item__desc">{{ item.desc }}</div>
        </div>
        <div class="case-item__arrow">›</div>
      </div>
    </div>

    <div class="case-tips">
      <div class="case-tips__title">💡 防骗小贴士</div>
      <div class="case-tips__list">
        <div class="case-tips__item">
          <span class="case-tips__num">1</span>
          <span class="case-tips__text">凡是要求先交钱的都是骗子</span>
        </div>
        <div class="case-tips__item">
          <span class="case-tips__num">2</span>
          <span class="case-tips__text">不轻易相信"高薪日结"等诱惑</span>
        </div>
        <div class="case-tips__item">
          <span class="case-tips__num">3</span>
          <span class="case-tips__text">遇到可疑情况及时举报</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.case-list-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: 24px;
}

.case-intro {
  margin: 12px 16px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  border-radius: var(--radius-lg);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;

  &__icon {
    font-size: 40px;
  }

  &__text {
    flex: 1;
  }

  &__title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 4px;
  }

  &__desc {
    font-size: 13px;
    opacity: 0.9;
  }
}

.case-list {
  margin: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.case-item {
  background: #fff;
  border-radius: var(--radius-base);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }

  &__icon {
    font-size: 32px;
    flex-shrink: 0;
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
  }

  &__type {
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 4px;
    font-weight: 500;
  }

  &__desc {
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__arrow {
    font-size: 20px;
    color: var(--color-text-disabled);
    flex-shrink: 0;
  }
}

.case-tips {
  margin: 24px 16px;
  background: var(--color-primary-bg);
  border-radius: var(--radius-base);
  padding: 16px;

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-primary);
    margin-bottom: 12px;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__num {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
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
    font-size: 14px;
    color: var(--color-text);
  }
}
</style>