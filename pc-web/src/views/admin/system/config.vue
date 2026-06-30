<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { systemApi } from '@/api/admin'
import { ElForm, ElFormItem, ElInputNumber, ElInput, ElButton, ElMessage, ElCard } from 'element-plus'

const configForm = ref({
  min_hourly_wage: 17,
  max_hourly_wage: 200,
  skill_weight: 40,
  time_weight: 30,
  distance_weight: 30,
  credit_score_rules: '初始100分，按时打卡+5分/次，有效投诉-20分/次，分数范围0-200'
})

const loading = ref(false)

const loadConfig = async () => {
  try {
    const response = await systemApi.getSystemConfig()
    if (response.code === 200) {
      configForm.value = response.data
    }
  } catch (error) {
    console.error('加载配置失败')
  }
}

const handleSave = async () => {
  const totalWeight = configForm.value.skill_weight + configForm.value.time_weight + configForm.value.distance_weight
  if (totalWeight !== 100) {
    ElMessage.warning('算法权重总和必须为100%')
    return
  }

  loading.value = true

  try {
    await systemApi.updateSystemConfig({
      min_hourly_wage: configForm.value.min_hourly_wage,
      max_hourly_wage: configForm.value.max_hourly_wage,
      skill_weight: configForm.value.skill_weight,
      time_weight: configForm.value.time_weight,
      distance_weight: configForm.value.distance_weight,
      credit_score_rules: configForm.value.credit_score_rules
    })
    
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<template>
  <div class="system-config">
    <div class="header-bar">
      <span class="page-title">系统配置</span>
      <div class="header-actions">
        <ElButton @click="loadConfig" icon="Refresh">刷新</ElButton>
        <ElButton type="primary" @click="handleSave" :loading="loading" icon="Save">保存配置</ElButton>
      </div>
    </div>

    <ElCard title="薪资配置">
      <ElForm :model="configForm" label-position="left" label-width="180px" class="config-form">
        <ElFormItem label="最低时薪（元/小时）">
          <ElInputNumber 
            v-model="configForm.min_hourly_wage" 
            :min="1" 
            :max="100" 
            style="width: 150px"
            placeholder="最低时薪"
          />
          <span class="form-hint">长沙最低时薪标准为17元/小时</span>
        </ElFormItem>
        <ElFormItem label="最高时薪（元/小时）">
          <ElInputNumber 
            v-model="configForm.max_hourly_wage" 
            :min="17" 
            :max="1000" 
            style="width: 150px"
            placeholder="最高时薪"
          />
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard title="智能匹配算法权重" style="margin-top: 20px;">
      <ElForm :model="configForm" label-position="left" label-width="180px" class="config-form">
        <ElFormItem label="技能匹配度权重">
          <ElInputNumber 
            v-model="configForm.skill_weight" 
            :min="0" 
            :max="100" 
            style="width: 100px"
            placeholder="技能权重"
          />
          <span class="form-unit">%</span>
        </ElFormItem>
        <ElFormItem label="时间可用性权重">
          <ElInputNumber 
            v-model="configForm.time_weight" 
            :min="0" 
            :max="100" 
            style="width: 100px"
            placeholder="时间权重"
          />
          <span class="form-unit">%</span>
        </ElFormItem>
        <ElFormItem label="通勤距离权重">
          <ElInputNumber 
            v-model="configForm.distance_weight" 
            :min="0" 
            :max="100" 
            style="width: 100px"
            placeholder="距离权重"
          />
          <span class="form-unit">%</span>
        </ElFormItem>
        <ElFormItem label="权重总和">
          <span class="total-weight">
            {{ configForm.skill_weight + configForm.time_weight + configForm.distance_weight }}%
          </span>
          <span v-if="configForm.skill_weight + configForm.time_weight + configForm.distance_weight !== 100" class="weight-warning">
            （必须为100%）
          </span>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard title="信用分规则" style="margin-top: 20px;">
      <ElForm :model="configForm" label-position="left" label-width="180px" class="config-form">
        <ElFormItem label="规则描述">
          <ElInput 
            v-model="configForm.credit_score_rules" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入信用分规则描述"
          />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<style scoped lang="scss">
.system-config {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #1E293B;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.config-form {
  padding: 20px 0;
}

.form-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #94A3B8;
}

.form-unit {
  margin-left: 8px;
  font-size: 14px;
  color: #64748B;
}

.total-weight {
  font-size: 16px;
  font-weight: bold;
  color: #165DFF;
}

.weight-warning {
  color: #F53F3F;
  margin-left: 8px;
}
</style>