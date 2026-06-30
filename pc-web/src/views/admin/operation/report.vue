<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElButton, ElDatePicker } from 'element-plus'
import * as echarts from 'echarts'

const dateRange = ref<[string, string]>(['', ''])

const dailyStats = ref([
  { date: '6/23', users: 1250, jobs: 85, applies: 320, complaints: 8 },
  { date: '6/24', users: 1380, jobs: 92, applies: 356, complaints: 12 },
  { date: '6/25', users: 1120, jobs: 78, applies: 285, complaints: 6 },
  { date: '6/26', users: 1450, jobs: 105, applies: 410, complaints: 10 },
  { date: '6/27', users: 1680, jobs: 118, applies: 485, complaints: 15 },
  { date: '6/28', users: 1820, jobs: 132, applies: 520, complaints: 18 },
  { date: '6/29', users: 1950, jobs: 145, applies: 568, complaints: 12 },
])

const totalStats = ref({
  totalUsers: 58600,
  totalJobs: 3250,
  totalApplies: 125800,
  totalComplaints: 890,
  userGrowth: '+12.5%',
  jobGrowth: '+8.3%',
  applyGrowth: '+15.8%',
  complaintGrowth: '-5.2%'
})

const industryData = ref([
  { name: '茶饮', value: 350 },
  { name: '零售', value: 280 },
  { name: '家教', value: 420 },
  { name: '会展', value: 180 },
  { name: '上门服务', value: 220 },
  { name: '新媒体', value: 150 },
])

const exportExcel = () => {
  console.log('导出运营报表')
}

onMounted(() => {
  setTimeout(() => {
    const userChart = echarts.init(document.getElementById('userChart'))
    userChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '岗位发布', '投递量', '投诉'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: dailyStats.value.map(item => item.date) },
      yAxis: [{ type: 'value', name: '人数/次数', position: 'left' }],
      series: [
        { name: '新增用户', type: 'line', data: dailyStats.value.map(item => item.users), smooth: true, lineStyle: { color: '#165DFF' } },
        { name: '岗位发布', type: 'line', data: dailyStats.value.map(item => item.jobs * 10), smooth: true, lineStyle: { color: '#00B42A' } },
        { name: '投递量', type: 'line', data: dailyStats.value.map(item => item.applies), smooth: true, lineStyle: { color: '#FF7D00' } },
        { name: '投诉', type: 'line', data: dailyStats.value.map(item => item.complaints * 10), smooth: true, lineStyle: { color: '#F53F3F' } }
      ]
    })

    const industryChart = echarts.init(document.getElementById('industryChart'))
    industryChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: industryData.value.map(item => item.name) },
      series: [{
        type: 'bar',
        data: industryData.value.map(item => item.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#165DFF' },
            { offset: 1, color: '#0EA5E9' }
          ]),
          borderRadius: [0, 4, 4, 0]
        }
      }]
    })
  }, 100)
})
</script>

<template>
  <div class="operation-report">
    <div class="header-bar">
      <div class="date-filter">
        <span class="filter-label">时间范围:</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </div>
      <el-button type="primary" icon="Download" @click="exportExcel">导出报表</el-button>
    </div>

    <ElRow :gutter="20">
      <ElCol :span="6">
        <ElCard class="stat-card">
          <div class="stat-icon-wrap bg-blue">
            <span class="stat-icon">👥</span>
          </div>
          <div class="stat-content">
            <div class="stat-title">累计用户</div>
            <div class="stat-value">{{ totalStats.totalUsers.toLocaleString() }}</div>
            <div class="stat-trend">{{ totalStats.userGrowth }}</div>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card">
          <div class="stat-icon-wrap bg-green">
            <span class="stat-icon">💼</span>
          </div>
          <div class="stat-content">
            <div class="stat-title">累计岗位</div>
            <div class="stat-value">{{ totalStats.totalJobs.toLocaleString() }}</div>
            <div class="stat-trend">{{ totalStats.jobGrowth }}</div>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card">
          <div class="stat-icon-wrap bg-orange">
            <span class="stat-icon">📤</span>
          </div>
          <div class="stat-content">
            <div class="stat-title">累计投递</div>
            <div class="stat-value">{{ totalStats.totalApplies.toLocaleString() }}</div>
            <div class="stat-trend">{{ totalStats.applyGrowth }}</div>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card">
          <div class="stat-icon-wrap bg-red">
            <span class="stat-icon">⚠️</span>
          </div>
          <div class="stat-content">
            <div class="stat-title">累计投诉</div>
            <div class="stat-value">{{ totalStats.totalComplaints.toLocaleString() }}</div>
            <div class="stat-trend">{{ totalStats.complaintGrowth }}</div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="20" style="margin-top: 20px;">
      <ElCol :span="16">
        <ElCard title="每日数据趋势">
          <div id="userChart" class="chart-container"></div>
        </ElCard>
      </ElCol>
      <ElCol :span="8">
        <ElCard title="行业分布">
          <div id="industryChart" class="chart-container-small"></div>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped lang="scss">
.operation-report {
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

.date-filter {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 12px;
  font-size: 14px;
  color: #64748B;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
}

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;

  &.bg-blue { background-color: #EFF6FF; }
  &.bg-green { background-color: #ECFDF5; }
  &.bg-orange { background-color: #FFF7ED; }
  &.bg-red { background-color: #FEF2F2; }
}

.stat-icon {
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 13px;
  color: #64748B;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1E293B;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #00B42A;

  &:not(:contains('+')) {
    color: #F53F3F;
  }
}

.chart-container {
  height: 300px;
}

.chart-container-small {
  height: 300px;
}
</style>