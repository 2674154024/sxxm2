<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { systemApi } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Refresh, Check, Wallet, Cpu, CreditCard } from '@element-plus/icons-vue'

const configForm = ref({
  min_hourly_wage: 17,
  max_hourly_wage: 200,
  skill_weight: 40,
  time_weight: 30,
  distance_weight: 30,
  credit_score_rules: '初始100分，按时打卡+5分/次，有效投诉-20分/次，分数范围0-200'
})

const loading = ref(false)

const totalWeight = computed(() => {
  return configForm.value.skill_weight + configForm.value.time_weight + configForm.value.distance_weight
})

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
  if (totalWeight.value !== 100) {
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
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">系统配置</h2>
        <p class="page-desc">管理平台核心配置参数，确保系统正常运行</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadConfig" :icon="Refresh">刷新</el-button>
        <el-button type="primary" @click="handleSave" :loading="loading" :icon="Check">保存配置</el-button>
      </div>
    </div>

    <div class="config-card">
      <div class="card-header">
        <div class="card-icon card-icon-warning">
          <Wallet />
        </div>
        <div class="card-title-group">
          <h3 class="card-title">薪资配置</h3>
          <p class="card-desc">设置平台时薪范围限制</p>
        </div>
      </div>
      <el-form :model="configForm" label-position="left" label-width="160px" class="config-form">
        <el-form-item label="最低时薪">
          <el-input-number 
            v-model="configForm.min_hourly_wage" 
            :min="1" 
            :max="100" 
            :controls="true"
            :step="1"
            placeholder="最低时薪"
          />
          <span class="form-hint">长沙最低时薪标准为17元/小时</span>
        </el-form-item>
        <el-form-item label="最高时薪">
          <el-input-number 
            v-model="configForm.max_hourly_wage" 
            :min="17" 
            :max="1000" 
            :controls="true"
            :step="10"
            placeholder="最高时薪"
          />
        </el-form-item>
      </el-form>
    </div>

    <div class="config-card">
      <div class="card-header">
        <div class="card-icon card-icon-primary">
          <Cpu />
        </div>
        <div class="card-title-group">
          <h3 class="card-title">智能匹配算法权重</h3>
          <p class="card-desc">调整岗位推荐算法各项权重，总和需为100%</p>
        </div>
      </div>
      <el-form :model="configForm" label-position="left" label-width="160px" class="config-form">
        <el-form-item label="技能匹配度">
          <el-slider 
            v-model="configForm.skill_weight" 
            :min="0" 
            :max="100" 
            :step="5"
            show-input
            style="width: 300px"
          />
          <span class="form-unit">%</span>
        </el-form-item>
        <el-form-item label="时间可用性">
          <el-slider 
            v-model="configForm.time_weight" 
            :min="0" 
            :max="100" 
            :step="5"
            show-input
            style="width: 300px"
          />
          <span class="form-unit">%</span>
        </el-form-item>
        <el-form-item label="通勤距离">
          <el-slider 
            v-model="configForm.distance_weight" 
            :min="0" 
            :max="100" 
            :step="5"
            show-input
            style="width: 300px"
          />
          <span class="form-unit">%</span>
        </el-form-item>
        <el-form-item label="权重总和">
          <div class="weight-summary">
            <span 
              class="total-weight"
              :class="{ 'is-valid': totalWeight === 100, 'is-invalid': totalWeight !== 100 }"
            >
              {{ totalWeight }}%
            </span>
            <el-tag 
              v-if="totalWeight === 100" 
              type="success" 
              size="small"
              effect="light"
              round
            >
              配置正确
            </el-tag>
            <el-tag 
              v-else 
              type="danger" 
              size="small"
              effect="light"
              round
            >
              必须为100%
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <div class="config-card">
      <div class="card-header">
        <div class="card-icon card-icon-success">
          <CreditCard />
        </div>
        <div class="card-title-group">
          <h3 class="card-title">信用分规则</h3>
          <p class="card-desc">定义用户信用分计算规则</p>
        </div>
      </div>
      <el-form :model="configForm" label-position="left" label-width="160px" class="config-form">
        <el-form-item label="规则描述">
          <el-input 
            v-model="configForm.credit_score_rules" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入信用分规则描述，包括加分项和减分项的详细说明"
          />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.system-config {
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1D2129;
    margin: 0 0 4px 0;
  }

  .page-desc {
    font-size: 13px;
    color: #86909C;
    margin: 0;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.config-card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F2F3F5;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;

  &.card-icon-warning {
    background: #FFF7E8;
    color: #FF7D00;
  }

  &.card-icon-primary {
    background: #E8F3FF;
    color: #165DFF;
  }

  &.card-icon-success {
    background: #E8FFEA;
    color: #00B42A;
  }
}

.card-title-group {
  flex: 1;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 4px 0;
}

.card-desc {
  font-size: 12px;
  color: #86909C;
  margin: 0;
}

.config-form {
  padding: 0;
}

.form-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #86909C;
}

.form-unit {
  margin-left: 10px;
  font-size: 14px;
  color: #4E5969;
}

.weight-summary {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-weight {
  font-size: 18px;
  font-weight: 600;

  &.is-valid {
    color: #00B42A;
  }

  &.is-invalid {
    color: #F53F3F;
  }
}
</style>