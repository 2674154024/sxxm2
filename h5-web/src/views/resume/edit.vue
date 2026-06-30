<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()

const resume = ref({
  name: '张三',
  gender: 'male',
  phone: '138****8888',
  school: '中南大学',
  major: '计算机科学与技术',
  grade: '大三',
  skills: ['沟通能力强', '执行力强', '团队协作', '学习能力强'],
  workTime: [
    { weekday: 1, available: true, time: '14:00-22:00' },
    { weekday: 2, available: false, time: '' },
    { weekday: 3, available: true, time: '14:00-22:00' },
    { weekday: 4, available: false, time: '' },
    { weekday: 5, available: true, time: '14:00-22:00' },
    { weekday: 6, available: true, time: '09:00-22:00' },
    { weekday: 0, available: true, time: '09:00-18:00' },
  ],
  intro: '我是一名大三学生，性格开朗，善于沟通，做事认真负责。有多次兼职经历，能快速适应新环境。希望能找到一份合适的兼职，锻炼自己的同时赚取生活费。',
  experience: [
    {
      id: 1,
      company: '茶颜悦色',
      position: '门店店员',
      duration: '2026.03 - 2026.05',
      desc: '负责收银、制作饮品、店铺清洁等工作，获得店长好评。',
    },
  ],
})

const weekdayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const newSkill = ref('')
const showSkillInput = ref(false)

function addSkill() {
  if (newSkill.value.trim() && !resume.value.skills.includes(newSkill.value.trim())) {
    resume.value.skills.push(newSkill.value.trim())
    newSkill.value = ''
    showSkillInput.value = false
  }
}

function removeSkill(index: number) {
  resume.value.skills.splice(index, 1)
}

function toggleWorkTime(day: typeof resume.value.workTime[0]) {
  day.available = !day.available
  if (day.available && !day.time) {
    day.time = '09:00-18:00'
  }
}

function handleSave() {
  console.log('保存简历', resume.value)
  router.back()
}
</script>

<template>
  <div class="resume-page">
    <NavBar title="编辑简历" show-back />

    <div class="resume-content">
      <div class="form-section">
        <div class="form-section__title">基本信息</div>

        <div class="form-item">
          <span class="form-item__label">姓名</span>
          <input
            v-model="resume.name"
            class="form-item__input"
            type="text"
            placeholder="请输入姓名"
          />
        </div>

        <div class="form-item">
          <span class="form-item__label">性别</span>
          <div class="form-item__radio-group">
            <div
              class="form-item__radio"
              :class="{ 'is-active': resume.gender === 'male' }"
              @click="resume.gender = 'male'"
            >
              男
            </div>
            <div
              class="form-item__radio"
              :class="{ 'is-active': resume.gender === 'female' }"
              @click="resume.gender = 'female'"
            >
              女
            </div>
          </div>
        </div>

        <div class="form-item">
          <span class="form-item__label">手机号</span>
          <input
            v-model="resume.phone"
            class="form-item__input"
            type="tel"
            placeholder="请输入手机号"
          />
        </div>
      </div>

      <div class="form-section">
        <div class="form-section__title">学校信息</div>

        <div class="form-item">
          <span class="form-item__label">学校</span>
          <input
            v-model="resume.school"
            class="form-item__input"
            type="text"
            placeholder="请输入学校名称"
          />
        </div>

        <div class="form-item">
          <span class="form-item__label">专业</span>
          <input
            v-model="resume.major"
            class="form-item__input"
            type="text"
            placeholder="请输入专业"
          />
        </div>

        <div class="form-item">
          <span class="form-item__label">年级</span>
          <input
            v-model="resume.grade"
            class="form-item__input"
            type="text"
            placeholder="请输入年级"
          />
        </div>
      </div>

      <div class="form-section">
        <div class="form-section__title">可工作时间</div>
        <div class="time-grid">
          <div
            v-for="day in resume.workTime"
            :key="day.weekday"
            class="time-item"
            :class="{ 'is-active': day.available }"
            @click="toggleWorkTime(day)"
          >
            <div class="time-item__name">{{ weekdayNames[day.weekday] }}</div>
            <div v-if="day.available" class="time-item__time">{{ day.time }}</div>
            <div v-else class="time-item__off">休息</div>
          </div>
        </div>
        <div class="form-section__tip">点击切换可工作状态，可设置具体时间段</div>
      </div>

      <div class="form-section">
        <div class="form-section__title">技能标签</div>
        <div class="skill-tags">
          <div
            v-for="(skill, index) in resume.skills"
            :key="skill"
            class="skill-tag"
          >
            {{ skill }}
            <span class="skill-tag__close" @click="removeSkill(index)">×</span>
          </div>
          <div v-if="showSkillInput" class="skill-input">
            <input
              v-model="newSkill"
              class="skill-input__field"
              type="text"
              placeholder="输入技能"
              maxlength="10"
              @blur="addSkill"
              @keyup.enter="addSkill"
            />
          </div>
          <div v-else class="skill-add" @click="showSkillInput = true">
            + 添加
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="form-section__title">自我介绍</div>
        <textarea
          v-model="resume.intro"
          class="form-textarea"
          placeholder="介绍一下自己，让企业更了解你..."
          rows="4"
          maxlength="200"
        ></textarea>
        <div class="form-textarea__count">{{ resume.intro.length }}/200</div>
      </div>

      <div class="form-section">
        <div class="form-section__title">
          兼职经历
          <span class="form-section__add" @click="() => {}">+ 添加</span>
        </div>
        <div
          v-for="exp in resume.experience"
          :key="exp.id"
          class="exp-card"
        >
          <div class="exp-card__header">
            <span class="exp-card__company">{{ exp.company }}</span>
            <span class="exp-card__edit">编辑</span>
          </div>
          <div class="exp-card__position">{{ exp.position }}</div>
          <div class="exp-card__duration">{{ exp.duration }}</div>
          <div class="exp-card__desc">{{ exp.desc }}</div>
        </div>
      </div>
    </div>

    <div class="form-footer">
      <button class="btn btn--block btn--primary btn--lg" @click="handleSave">
        保存简历
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.resume-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: 80px;
}

.resume-content {
  padding: 12px 0;
}

.form-section {
  background: #fff;
  margin-bottom: 12px;
  padding: 16px;

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__add {
    font-size: 13px;
    color: var(--color-primary);
    font-weight: 400;
    cursor: pointer;
  }

  &__tip {
    font-size: 12px;
    color: var(--color-text-disabled);
    margin-top: 10px;
  }
}

.form-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border);

  &:last-child {
    border-bottom: none;
  }

  &__label {
    flex-shrink: 0;
    width: 80px;
    font-size: 14px;
    color: var(--color-text);
  }

  &__input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 14px;
    color: var(--color-text);
    background: transparent;
    text-align: right;

    &::placeholder {
      color: var(--color-text-disabled);
    }
  }

  &__radio-group {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }

  &__radio {
    padding: 6px 16px;
    border-radius: 16px;
    border: 1px solid var(--color-border);
    font-size: 13px;
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: all var(--transition-fast);

    &.is-active {
      background: var(--color-primary);
      border-color: var(--color-primary);
      color: #fff;
    }
  }
}

.time-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.time-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 4px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-base);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;

  &.is-active {
    background: #e8f3ff;
    border-color: var(--color-primary);
  }

  &__name {
    font-size: 13px;
    color: var(--color-text);
    margin-bottom: 4px;
  }

  &__time {
    font-size: 11px;
    color: var(--color-primary);
  }

  &__off {
    font-size: 11px;
    color: var(--color-text-disabled);
  }
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: #e8f3ff;
  color: var(--color-primary);
  font-size: 13px;
  border-radius: 14px;

  &__close {
    cursor: pointer;
    font-size: 16px;
    line-height: 1;
  }
}

.skill-add {
  padding: 6px 12px;
  background: var(--color-bg-secondary);
  color: var(--color-text-secondary);
  font-size: 13px;
  border-radius: 14px;
  cursor: pointer;
  border: 1px dashed var(--color-border);
}

.skill-input {
  &__field {
    width: 80px;
    padding: 6px 10px;
    background: #fff;
    border: 1px solid var(--color-primary);
    border-radius: 14px;
    font-size: 13px;
    outline: none;
  }
}

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-base);
  font-size: 14px;
  color: var(--color-text);
  outline: none;
  resize: none;
  box-sizing: border-box;
  font-family: inherit;
  line-height: 1.6;

  &::placeholder {
    color: var(--color-text-disabled);
  }

  &__count {
    text-align: right;
    font-size: 12px;
    color: var(--color-text-disabled);
    margin-top: 6px;
  }
}

.exp-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-base);
  padding: 12px;
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 6px;
  }

  &__company {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text);
  }

  &__edit {
    font-size: 13px;
    color: var(--color-primary);
    cursor: pointer;
  }

  &__position {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-bottom: 4px;
  }

  &__duration {
    font-size: 12px;
    color: var(--color-text-disabled);
    margin-bottom: 8px;
  }

  &__desc {
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.5;
  }
}

.form-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style>
