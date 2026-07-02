<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { jobApi } from '@/api/job'
import { Check, Location } from '@element-plus/icons-vue'

const router = useRouter()
const activeStep = ref(0)
const formRef = ref()

const form = reactive({
  job_name: '',
  salary: 0,
  salary_type: 'hourly' as 'hourly' | 'daily' | 'monthly',
  address: '',
  longitude: 0,
  latitude: 0,
  work_time: {
    weekday: [] as string[],
    time_range: ['', '']
  },
  skill_tags: '',
  description: '',
  has_insurance: false,
  recruit_num: 1
})

const settlementType = ref<'daily' | 'weekly' | 'monthly'>('weekly')

const rules = {
  job_name: [
    { required: true, message: '请输入岗位名称', trigger: 'blur' },
    { min: 2, max: 50, message: '岗位名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  salary: [
    { required: true, message: '请输入薪资', trigger: 'blur' },
    { type: 'number', min: 0, message: '薪资不能小于 0', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入工作地点', trigger: 'blur' }
  ],
  recruit_num: [
    { required: true, message: '请输入招聘人数', trigger: 'blur' },
    { type: 'number', min: 1, message: '招聘人数至少为1', trigger: 'blur' }
  ]
}

const steps = [
  { title: '基本信息', icon: 'Edit' },
  { title: '工作地点', icon: 'Location' },
  { title: '工作时间', icon: 'Clock' },
  { title: '岗位详情', icon: 'Document' }
]

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const canNext = computed(() => {
  if (activeStep.value === 0) {
    return form.job_name && form.salary > 0
  }
  if (activeStep.value === 1) {
    return !!form.address
  }
  return true
})

const toggleWeekday = (day: string) => {
  const index = form.work_time.weekday.indexOf(day)
  if (index > -1) {
    form.work_time.weekday.splice(index, 1)
  } else {
    form.work_time.weekday.push(day)
  }
}

const nextStep = () => {
  if (activeStep.value < steps.length - 1) {
    activeStep.value++
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const submitForm = () => {
  if (!form.job_name || !form.salary || !form.address || !form.recruit_num) {
    ElMessage.warning('请填写必填项')
    return
  }

  const salaryTypeMap: Record<string, number> = { hourly: 0, daily: 1, monthly: 2 }
  const settlementTypeMap: Record<string, number> = { daily: 0, weekly: 1, monthly: 2 }
  
  jobApi.publishJob({
    jobTitle: form.job_name,
    salaryAmount: form.salary,
    salaryType: salaryTypeMap[form.salary_type] || 0,
    settlementType: settlementTypeMap[settlementType.value] || 1,
    workAddress: form.address,
    workTime: JSON.stringify(form.work_time),
    skillRequire: form.skill_tags,
    recruitNum: form.recruit_num,
    isInsured: form.has_insurance ? 1 : 0
  }).then(() => {
    ElMessage.success('发布成功，等待审核')
    router.push('/enterprise/job/list')
  }).catch((error: any) => {
    console.error('Publish job error:', error)
    ElMessage.error(error.response?.data?.message || '发布失败')
  })
}
</script>

<template>
  <div class="job-publish">
    <div class="page-header">
      <div class="header-left">
        <h1>发布岗位</h1>
        <p class="subtitle">完善岗位信息，提交审核后即可发布</p>
      </div>
    </div>

    <div class="publish-container">
      <div class="sidebar">
        <div class="step-list">
          <div 
            v-for="(step, index) in steps" 
            :key="index"
            class="step-item"
            :class="{ active: activeStep === index, completed: activeStep > index }"
            @click="activeStep = index"
          >
            <div class="step-icon">
              <span class="step-number">{{ index + 1 }}</span>
              <el-icon v-if="activeStep > index" class="check-icon"><Check /></el-icon>
            </div>
            <div class="step-content">
              <span class="step-title">{{ step.title }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="main-content">
        <el-card class="form-card" shadow="never">
          <div class="card-header">
            <h2>{{ steps[activeStep].title }}</h2>
            <span class="step-indicator">第 {{ activeStep + 1 }} / {{ steps.length }} 步</span>
          </div>

          <el-form 
            ref="formRef"
            :model="form" 
            :rules="rules"
            label-width="120px" 
            class="publish-form"
          >
            <div v-show="activeStep === 0" class="step-panel">
              <el-form-item label="岗位名称" prop="job_name">
                <el-input 
                  v-model="form.job_name" 
                  placeholder="请输入岗位名称" 
                  size="large"
                  clearable
                />
              </el-form-item>

              <el-form-item label="薪资" prop="salary">
                <div class="salary-input-group">
                  <el-input-number 
                    v-model="form.salary" 
                    :min="0" 
                    :step="1" 
                    size="large"
                    style="width: 200px;"
                  />
                  <el-select v-model="form.salary_type" size="large" style="width: 140px; margin-left: 12px;">
                    <el-option label="元/小时" value="hourly" />
                    <el-option label="元/天" value="daily" />
                    <el-option label="元/月" value="monthly" />
                  </el-select>
                </div>
              </el-form-item>

              <el-form-item label="结算周期">
                <el-radio-group v-model="settlementType" size="large">
                  <el-radio label="daily">日结</el-radio>
                  <el-radio label="weekly">周结</el-radio>
                  <el-radio label="monthly">月结</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="招聘人数" prop="recruit_num">
                <el-input-number 
                  v-model="form.recruit_num" 
                  :min="1" 
                  :max="100" 
                  :step="1" 
                  size="large"
                  style="width: 200px;"
                  placeholder="请输入招聘人数"
                />
              </el-form-item>
            </div>

            <div v-show="activeStep === 1" class="step-panel">
              <el-form-item label="工作地点" prop="address">
                <el-input 
                  v-model="form.address" 
                  placeholder="请输入详细地址" 
                  size="large"
                  clearable
                />
              </el-form-item>

              <el-form-item label="地图选点">
                <div class="map-placeholder">
                  <el-icon class="map-icon"><Location /></el-icon>
                  <span>点击地图选择位置（暂未开放）</span>
                </div>
              </el-form-item>
            </div>

            <div v-show="activeStep === 2" class="step-panel">
              <el-form-item label="工作日期">
                <div class="weekday-select">
                  <div 
                    v-for="day in weekdays" 
                    :key="day"
                    class="weekday-item"
                    :class="{ active: form.work_time.weekday.includes(day) }"
                    @click="toggleWeekday(day)"
                  >
                    {{ day }}
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="工作时段">
                <div class="time-range">
                  <el-time-picker 
                    v-model="form.work_time.time_range[0]" 
                    placeholder="开始时间" 
                    size="large"
                    style="width: 180px;"
                    format="HH:mm"
                    value-format="HH:mm"
                  />
                  <span class="time-divider">至</span>
                  <el-time-picker 
                    v-model="form.work_time.time_range[1]" 
                    placeholder="结束时间" 
                    size="large"
                    style="width: 180px;"
                    format="HH:mm"
                    value-format="HH:mm"
                  />
                </div>
              </el-form-item>
            </div>

            <div v-show="activeStep === 3" class="step-panel">
              <el-form-item label="技能标签">
                <el-input 
                  v-model="form.skill_tags" 
                  placeholder="多个标签用逗号分隔，如：电工,焊工" 
                  size="large"
                  clearable
                />
              </el-form-item>

              <el-form-item label="岗位描述">
                <el-input 
                  type="textarea" 
                  v-model="form.description" 
                  :rows="5" 
                  placeholder="请详细描述岗位职责、要求等信息"
                />
              </el-form-item>

              <el-form-item label="购买保险">
                <el-switch 
                  v-model="form.has_insurance" 
                  active-text="是"
                  inactive-text="否"
                />
              </el-form-item>
            </div>
          </el-form>
        </el-card>

        <div class="footer-actions">
          <el-button 
            size="large" 
            :disabled="activeStep === 0"
            @click="prevStep"
          >上一步</el-button>
          <div class="actions-right">
            <el-button size="large" @click="router.push('/enterprise/job/list')">取消</el-button>
            <el-button 
              v-if="activeStep < steps.length - 1"
              type="primary" 
              size="large"
              :disabled="!canNext"
              @click="nextStep"
            >下一步</el-button>
            <el-button 
              v-else
              type="primary" 
              size="large"
              @click="submitForm"
            >提交审核</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.job-publish {
  padding: 0;
  min-height: 100%;
  background-color: #F2F3F5;
}

.page-header {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  padding: 32px 40px;
  margin-bottom: 24px;

  .header-left {
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #FFFFFF;
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.8);
      margin: 0;
    }
  }
}

.publish-container {
  display: flex;
  gap: 24px;
  padding: 0 40px 120px;
  max-width: 1400px;
  margin: 0 auto;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;

  .step-list {
    background: #FFFFFF;
    border-radius: 8px;
    padding: 16px 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .step-item {
    display: flex;
    align-items: center;
    padding: 16px 24px;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background-color: transparent;
      transition: background-color 0.3s;
    }

    &:hover {
      background-color: #F2F3F5;
    }

    &.active {
      &::before {
        background-color: #165DFF;
      }

      .step-icon {
        background-color: #165DFF;
        border-color: #165DFF;

        .step-number {
          color: #FFFFFF;
        }
      }

      .step-title {
        color: #165DFF;
        font-weight: 600;
      }
    }

    &.completed {
      .step-icon {
        background-color: #00B42A;
        border-color: #00B42A;
      }

      .step-title {
        color: #00B42A;
      }
    }
  }

  .step-icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background-color: #F2F3F5;
    border: 2px solid #D9D9D9;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;
    transition: all 0.3s;

    .step-number {
      font-size: 14px;
      font-weight: 600;
      color: #86909C;
    }

    .check-icon {
      color: #FFFFFF;
      font-size: 16px;
    }
  }

  .step-content {
    flex: 1;
  }

  .step-title {
    font-size: 14px;
    color: #4E5969;
    transition: color 0.3s;
  }
}

.main-content {
  flex: 1;
  min-width: 0;
}

.form-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  :deep(.el-card__body) {
    padding: 32px 40px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid #F2F3F5;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: #1F2329;
    margin: 0;
  }

  .step-indicator {
    font-size: 14px;
    color: #86909C;
  }
}

.publish-form {
  max-width: 600px;
}

.step-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.salary-input-group {
  display: flex;
  align-items: center;
}

.weekday-select {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.weekday-item {
  padding: 10px 24px;
  border: 1px solid #D9D9D9;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #4E5969;
  background-color: #FFFFFF;
  transition: all 0.3s;

  &:hover {
    border-color: #165DFF;
    color: #165DFF;
  }

  &.active {
    background-color: #165DFF;
    border-color: #165DFF;
    color: #FFFFFF;
  }
}

.time-range {
  display: flex;
  align-items: center;

  .time-divider {
    margin: 0 16px;
    color: #86909C;
    font-size: 14px;
  }
}

.map-placeholder {
  height: 200px;
  background-color: #F7F8FA;
  border: 1px dashed #D9D9D9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #86909C;
  font-size: 14px;

  .map-icon {
    font-size: 32px;
    margin-bottom: 12px;
    color: #C9CDD4;
  }
}

.footer-actions {
  position: fixed;
  bottom: 0;
  left: 240px;
  right: 0;
  background-color: #FFFFFF;
  padding: 16px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;

  .actions-right {
    display: flex;
    gap: 12px;
  }
}

:deep(.el-input--large .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button--large) {
  height: 36px;
  border-radius: 8px;
  padding: 0 20px;
}

:deep(.el-input-number--large) {
  height: 36px;
}

:deep(.el-select--large .el-select__wrapper) {
  border-radius: 8px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
}

:deep(.el-switch) {
  --el-switch-on-color: #165DFF;
}
</style>
