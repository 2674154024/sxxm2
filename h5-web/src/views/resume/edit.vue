<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { resumeApi, type Resume } from '@/api/resume'

const router = useRouter()

const form = reactive<any>({
  userId: '',
  avatar: '',
  realName: '',
  gender: 'male',
  schoolName: '',
  phone: '',
  availableTime: '',
  skillTags: '',
  workExperience: '',
  selfIntroduction: '',
  major: '',
  grade: '',
  creditScore: 0
})

const weekdayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const weekdayKeys = ['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun']

const newSkill = ref('')
const showSkillInput = ref(false)
const showRequiredTip = ref(false)

const workTimeList = ref<Array<{ day: string; time: string }>>([])
const workList = ref<Array<{ company: string; position: string; time: string; description: string }>>([])

const skillList = () => {
  return form.skillTags ? form.skillTags.split(',') : []
}

const requiredFields = computed(() => {
  const fields: string[] = []
  if (!form.realName || typeof form.realName !== 'string' || form.realName.trim() === '') {
    fields.push('• 真实姓名')
  }
  if (!form.phone || typeof form.phone !== 'string' || form.phone.trim() === '' || form.phone.length !== 11) {
    fields.push('• 手机号码')
  }
  if (workTimeList.value.length === 0) {
    fields.push('• 可工作时间')
  }
  return fields
})

function addSkill() {
  if (newSkill.value.trim() && !skillList().includes(newSkill.value.trim())) {
    const skills = [...skillList(), newSkill.value.trim()]
    form.skillTags = skills.join(',')
    newSkill.value = ''
    showSkillInput.value = false
  }
}

function removeSkill(index: number) {
  const skills = skillList()
  skills.splice(index, 1)
  form.skillTags = skills.join(',')
}

function addWorkTime() {
  workTimeList.value.push({ day: '', time: '' })
}

function deleteWorkTime(index: number) {
  workTimeList.value.splice(index, 1)
}

function addWork() {
  workList.value.push({ company: '', position: '', time: '', description: '' })
}

function deleteWork(index: number) {
  workList.value.splice(index, 1)
}

function validateForm() {
  if (requiredFields.value.length > 0) {
    showRequiredTip.value = true
    return false
  }
  
  for (let i = 0; i < workTimeList.value.length; i++) {
    const time = workTimeList.value[i]
    if (!time.day) {
      alert('请选择工作时间的日期')
      return false
    }
    if (!time.time || time.time.trim() === '') {
      alert('请填写工作时间的时段')
      return false
    }
  }
  
  return true
}

function onPhoneInput(e: any) {
  form.phone = e.target.value.replace(/\D/g, '').slice(0, 11)
}

function handleSave() {
  if (!validateForm()) {
    return
  }

  const availableTimeStr = JSON.stringify(workTimeList.value)
  
  resumeApi.updateResume({
    avatar: form.avatar,
    realName: form.realName,
    gender: form.gender,
    phone: form.phone,
    availableTime: availableTimeStr,
    skillTags: form.skillTags,
    workExperience: JSON.stringify(workList.value),
    selfIntroduction: form.selfIntroduction,
    major: form.major,
    grade: form.grade
  }).then(() => {
    alert('保存成功')
    router.back()
  }).catch(() => {
    alert('保存失败')
  })
}

async function fetchResume() {
  try {
    const res = await resumeApi.getResume()
    const data = res.data || {}
    
    form.userId = data.userId || ''
    form.avatar = data.avatar || ''
    form.realName = data.realName || ''
    form.gender = data.gender || 'male'
    form.schoolName = data.schoolName || ''
    form.phone = data.phone || ''
    form.availableTime = data.availableTime || ''
    form.skillTags = data.skillTags || ''
    form.workExperience = data.workExperience || ''
    form.selfIntroduction = data.selfIntroduction || ''
    form.major = data.major || ''
    form.grade = data.grade || ''
    form.creditScore = data.creditScore || 0
    
    if (data.availableTime) {
      try {
        const parsed = JSON.parse(data.availableTime)
        if (Array.isArray(parsed)) {
          workTimeList.value = parsed
        } else {
          for (const [key, slots] of Object.entries(parsed)) {
            if (Array.isArray(slots) && slots.length > 0) {
              const dayName = weekdayNames[weekdayKeys.indexOf(key)] || ''
              for (const slot of slots) {
                if (slot) {
                  workTimeList.value.push({ day: dayName, time: slot })
                }
              }
            }
          }
        }
      } catch (e) {
        console.error('解析工作时间失败:', e)
      }
    }
    
    if (data.workExperience) {
      try {
        const parsedWork = JSON.parse(data.workExperience)
        if (Array.isArray(parsedWork)) {
          workList.value = parsedWork
        }
      } catch (e) {
        console.error('解析兼职经历失败:', e)
      }
    }
  } catch (error) {
    console.error('获取简历失败:', error)
  }
}

onMounted(() => {
  fetchResume()
})
</script>

<template>
  <div class="resume-page">
    <NavBar title="编辑简历" show-back />

    <div v-if="showRequiredTip" class="required-tip" @click="showRequiredTip = false">
      <div class="tip-content">
        <div class="tip-header">
          <span class="tip-icon">⚠️</span>
          <span class="tip-title">请完善必填信息</span>
        </div>
        <div class="tip-list">
          <div v-for="(item, index) in requiredFields" :key="index" class="tip-item">
            {{ item }}
          </div>
          <div v-if="requiredFields.length === 0" class="tip-item success">
            ✓ 所有必填信息已完善
          </div>
        </div>
        <span class="tip-close" @click="showRequiredTip = false">×</span>
      </div>
    </div>

    <div class="resume-content">
      <div class="form-section">
        <div class="form-section__title">
          基本信息
          <span class="tip-btn" @click="showRequiredTip = true">❓</span>
        </div>

        <div class="form-item">
          <span class="form-item__label">
            <span class="required-mark">*</span>真实姓名
          </span>
          <input
            v-model="form.realName"
            class="form-item__input"
            type="text"
            placeholder="请输入真实姓名"
          />
        </div>

        <div class="form-item">
          <span class="form-item__label">性别</span>
          <div class="form-item__radio-group">
            <div
              class="form-item__radio"
              :class="{ 'is-active': form.gender === 'male' }"
              @click="form.gender = 'male'"
            >
              男
            </div>
            <div
              class="form-item__radio"
              :class="{ 'is-active': form.gender === 'female' }"
              @click="form.gender = 'female'"
            >
              女
            </div>
          </div>
        </div>

        <div class="form-item">
          <span class="form-item__label">学校</span>
          <span class="form-item__value">{{ form.schoolName || '请先完成实名认证' }}</span>
        </div>

        <div class="form-item">
          <span class="form-item__label">专业</span>
          <input
            v-model="form.major"
            class="form-item__input"
            type="text"
            placeholder="请输入专业"
          />
        </div>

        <div class="form-item">
          <span class="form-item__label">年级</span>
          <input
            v-model="form.grade"
            class="form-item__input"
            type="text"
            placeholder="请输入年级"
          />
        </div>

        <div class="form-item">
          <span class="form-item__label">
            <span class="required-mark">*</span>手机号码
          </span>
          <input
            :value="form.phone"
            class="form-item__input"
            type="tel"
            placeholder="请输入手机号码"
            maxlength="11"
            @input="onPhoneInput"
          />
        </div>
      </div>

      <div class="form-section">
        <div class="form-section__title">
          可工作时间
          <span class="form-section__add" @click="addWorkTime">+ 添加</span>
        </div>
        <div v-for="(time, index) in workTimeList" :key="index" class="exp-card">
          <div class="exp-card__header">
            <span class="exp-card__company">{{ time.day || '选择日期' }} {{ time.time || '' }}</span>
            <span class="exp-card__delete" @click="deleteWorkTime(index)">删除</span>
          </div>
          <div class="exp-card__body">
            <div class="exp-row">
              <span class="exp-label">日期</span>
              <div class="exp-day-picker">
                <span
                  v-for="day in weekdayNames"
                  :key="day"
                  class="day-option"
                  :class="{ active: time.day === day }"
                  @click="time.day = day"
                >
                  {{ day }}
                </span>
              </div>
            </div>
            <div class="exp-row">
              <span class="exp-label">时段</span>
              <input v-model="time.time" class="exp-input" placeholder="如：9:00-12:00" />
            </div>
          </div>
        </div>
        <div v-if="workTimeList.length === 0" class="exp-empty" @click="addWorkTime">
          + 添加可工作时间
        </div>
        <div class="work-time-tip">示例：周一 9:00-12:00，可添加多个时间段</div>
      </div>

      <div class="form-section">
        <div class="form-section__title">
          兼职经历
          <span class="form-section__add" @click="addWork">+ 添加</span>
        </div>
        <div v-for="(work, index) in workList" :key="index" class="exp-card">
          <div class="exp-card__header">
            <span class="exp-card__company">{{ work.company || '新增经历' }}</span>
            <span class="exp-card__delete" @click="deleteWork(index)">删除</span>
          </div>
          <div class="exp-card__body">
            <div class="exp-row">
              <span class="exp-label">公司</span>
              <input v-model="work.company" class="exp-input" placeholder="请输入公司名称" />
            </div>
            <div class="exp-row">
              <span class="exp-label">职位</span>
              <input v-model="work.position" class="exp-input" placeholder="请输入职位名称" />
            </div>
            <div class="exp-row">
              <span class="exp-label">时间</span>
              <input v-model="work.time" class="exp-input" placeholder="如：2025.06 - 2025.09" />
            </div>
            <div class="exp-row">
              <span class="exp-label">描述</span>
              <textarea v-model="work.description" class="exp-textarea" placeholder="请描述工作内容和收获" />
            </div>
          </div>
        </div>
        <div v-if="workList.length === 0" class="exp-empty" @click="addWork">
          + 添加兼职经历
        </div>
      </div>

      <div class="form-section">
        <div class="form-section__title">技能标签</div>
        <div class="skill-tags">
          <div
            v-for="(skill, index) in skillList()"
            :key="skill"
            class="skill-tag"
          >
            {{ skill }}
            <span class="skill-tag__close" @click="removeSkill(index as number)">×</span>
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
        <div class="skill-count">{{ skillList().length }}/8</div>
      </div>

      <div class="form-section">
        <div class="form-section__title">自我介绍</div>
        <textarea
          v-model="form.selfIntroduction"
          class="form-textarea"
          placeholder="介绍一下自己，让企业更了解你..."
          rows="4"
          maxlength="500"
        ></textarea>
        <div class="form-textarea__count">{{ (form.selfIntroduction || '').length }}/500</div>
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

.required-tip {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;

  .tip-content {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    width: 320px;
    position: relative;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }

  .tip-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;

    .tip-icon {
      font-size: 24px;
      margin-right: 8px;
    }

    .tip-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text);
    }
  }

  .tip-list {
    padding-left: 8px;
  }

  .tip-item {
    font-size: 14px;
    color: #ff4d4f;
    margin-bottom: 8px;

    &.success {
      color: #52c41a;
    }
  }

  .tip-close {
    position: absolute;
    top: 12px;
    right: 12px;
    font-size: 20px;
    color: var(--color-text-disabled);
    cursor: pointer;
    line-height: 1;
  }
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

.tip-btn {
  font-size: 14px;
  background: #fff7e6;
  color: #ff7d00;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-right: 8px;
}

.required-mark {
  color: #ff4d4f;
  margin-right: 4px;
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

  &__value {
    flex: 1;
    font-size: 14px;
    color: var(--color-text-secondary);
    text-align: right;
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

.exp-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-base);
  margin-bottom: 10px;
  overflow: hidden;

  &:last-child {
    margin-bottom: 0;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    background: #fff;
    border-bottom: 1px solid var(--color-border);
  }

  &__company {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text);
  }

  &__delete {
    font-size: 13px;
    color: #ff4d4f;
    cursor: pointer;
  }

  &__body {
    padding: 12px;
  }
}

.exp-row {
  display: flex;
  align-items: flex-start;
  padding: 8px 0;
}

.exp-label {
  width: 50px;
  font-size: 13px;
  color: var(--color-text-disabled);
  flex-shrink: 0;
  padding-top: 4px;
}

.exp-input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--color-text);
  outline: none;
}

.exp-day-picker {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.day-option {
  font-size: 12px;
  color: var(--color-text-secondary);
  background: #fff;
  padding: 4px 10px;
  border-radius: 10px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all var(--transition-fast);

  &.active {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: #fff;
  }
}

.exp-textarea {
  flex: 1;
  min-height: 80px;
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--color-text);
  outline: none;
  resize: none;
  font-family: inherit;
  line-height: 1.5;
}

.exp-empty {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-disabled);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-base);
  border: 1px dashed var(--color-border);
  cursor: pointer;
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

.skill-count {
  text-align: right;
  font-size: 12px;
  color: var(--color-text-disabled);
  margin-top: 8px;
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

.work-time-tip {
  font-size: 11px;
  color: var(--color-text-disabled);
  margin-top: 8px;
}

.form-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style>