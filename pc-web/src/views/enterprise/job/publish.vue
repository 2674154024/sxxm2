<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { jobApi } from '@/api/job'

const router = useRouter()
const form = reactive({
  job_name: '',
  salary: 0,
  salary_type: 'hourly' as 'hourly' | 'daily' | 'monthly',
  settlement_type: 'weekly' as 'daily' | 'weekly' | 'monthly',
  address: '',
  longitude: 0,
  latitude: 0,
  work_time: {
    weekday: [] as string[],
    time_range: ['', '']
  },
  skill_tags: '',
  description: '',
  has_insurance: false
})

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const submitForm = () => {
  if (!form.job_name || !form.salary || !form.address) {
    ElMessage.warning('请填写必填项')
    return
  }

  const tags = form.skill_tags.split(/[,，、]/).filter(t => t.trim())
  
  jobApi.publishJob({
    ...form,
    skill_tags: tags
  }).then(() => {
    ElMessage.success('发布成功')
    router.push('/enterprise/job/list')
  }).catch(() => {
    ElMessage.error('发布失败')
  })
}

const toggleWeekday = (day: string) => {
  const index = form.work_time.weekday.indexOf(day)
  if (index > -1) {
    form.work_time.weekday.splice(index, 1)
  } else {
    form.work_time.weekday.push(day)
  }
}
</script>

<template>
  <div class="job-publish">
    <div class="page-header">
      <h1>发布岗位</h1>
      <el-button type="primary" @click="submitForm">提交审核</el-button>
    </div>

    <el-form label-width="120px" style="max-width: 800px;">
      <el-form-item label="岗位名称" required>
        <el-input v-model="form.job_name" placeholder="请输入岗位名称" />
      </el-form-item>

      <el-form-item label="薪资" required>
        <el-input-number v-model="form.salary" :min="0" :step="1" />
        <el-select v-model="form.salary_type" style="margin-left: 12px; width: 120px;">
          <el-option label="小时" value="hourly" />
          <el-option label="日" value="daily" />
          <el-option label="月" value="monthly" />
        </el-select>
      </el-form-item>

      <el-form-item label="结算周期">
        <el-select v-model="form.settlement_type">
          <el-option label="日结" value="daily" />
          <el-option label="周结" value="weekly" />
          <el-option label="月结" value="monthly" />
        </el-select>
      </el-form-item>

      <el-form-item label="工作地点" required>
        <el-input v-model="form.address" placeholder="请输入详细地址" />
      </el-form-item>

      <el-form-item label="工作时间">
        <div class="weekday-select">
          <el-button 
            v-for="day in weekdays" 
            :key="day"
            :type="form.work_time.weekday.includes(day) ? 'primary' : 'default'"
            @click="toggleWeekday(day)"
          >{{ day }}</el-button>
        </div>
        <div class="time-range">
          <el-input v-model="form.work_time.time_range[0]" placeholder="开始时间" style="width: 150px; margin-right: 12px;" />
          <span>至</span>
          <el-input v-model="form.work_time.time_range[1]" placeholder="结束时间" style="width: 150px; margin-left: 12px;" />
        </div>
      </el-form-item>

      <el-form-item label="技能标签">
        <el-input v-model="form.skill_tags" placeholder="多个标签用逗号分隔" />
      </el-form-item>

      <el-form-item label="岗位描述">
        <el-input type="textarea" v-model="form.description" :rows="4" placeholder="请详细描述岗位要求" />
      </el-form-item>

      <el-form-item label="购买保险">
        <el-switch v-model="form.has_insurance" />
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.job-publish {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h1 {
    font-size: 20px;
    font-weight: bold;
    color: #1F2329;
  }
}

.weekday-select {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.time-range {
  display: flex;
  align-items: center;
}
</style>