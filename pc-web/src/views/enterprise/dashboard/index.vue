<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { jobApi } from '@/api/job'
import { talentApi } from '@/api/talent'

const stats = ref({
  jobCount: 0,
  applyCount: 0,
  hireCount: 0,
  salaryTotal: 0
})

const dateRange = ref([dayjs().subtract(30, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])

const generateMockData = () => {
  const dates = []
  const applyTrend = []
  for (let i = 29; i >= 0; i--) {
    dates.push(dayjs().subtract(i, 'day').format('MM-DD'))
    applyTrend.push(Math.floor(Math.random() * 20) + 5)
  }

  const typeData = [
    { value: 35, name: '家教辅导' },
    { value: 25, name: '促销导购' },
    { value: 20, name: '会展协助' },
    { value: 15, name: '客服外包' },
    { value: 5, name: '其他' }
  ]

  const conversionData = [
    { name: '初中数学家教', value: 75 },
    { name: '超市促销', value: 60 },
    { name: '展会协助', value: 85 },
    { name: '英语助教', value: 70 },
    { name: '奶茶店店员', value: 55 }
  ]

  return { dates, applyTrend, typeData, conversionData }
}

const initCharts = () => {
  const mock = generateMockData()

  const trendChart = echarts.init(document.getElementById('trendChart') as HTMLElement)
  trendChart.setOption({
    title: { text: '近30天投递趋势', left: 'center', textStyle: { fontSize: 14, color: '#4E5969' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: mock.dates, axisLabel: { fontSize: 10, rotate: 45 } },
    yAxis: { type: 'value' },
    series: [{
      name: '投递数',
      type: 'line',
      smooth: true,
      data: mock.applyTrend,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(22, 93, 255, 0.3)' },
        { offset: 1, color: 'rgba(22, 93, 255, 0.05)' }
      ]) },
      itemStyle: { color: '#165DFF' }
    }]
  })

  const typeChart = echarts.init(document.getElementById('typeChart') as HTMLElement)
  typeChart.setOption({
    title: { text: '岗位类型分布', left: 'center', textStyle: { fontSize: 14, color: '#4E5969' } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, fontSize: 12 },
      data: mock.typeData
    }]
  })

  const conversionChart = echarts.init(document.getElementById('conversionChart') as HTMLElement)
  conversionChart.setOption({
    title: { text: '各岗位投递转化率', left: 'center', textStyle: { fontSize: 14, color: '#4E5969' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: mock.conversionData.map(d => d.name), axisLabel: { fontSize: 10, rotate: 30 } },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
    series: [{
      type: 'bar',
      data: mock.conversionData.map(d => d.value),
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#FF7D00' },
          { offset: 1, color: '#FFA940' }
        ])
      }
    }]
  })

  window.addEventListener('resize', () => {
    trendChart.resize()
    typeChart.resize()
    conversionChart.resize()
  })
}

const loadData = () => {
  Promise.all([
    jobApi.getJobList({ page: 1, size: 1 }),
    talentApi.getApplyList({ page: 1, size: 1 }),
    talentApi.getApplyList({ page: 1, size: 1, status: 'hired' })
  ]).then(([jobRes, applyRes, hireRes]) => {
    stats.value = {
      jobCount: jobRes.data.total,
      applyCount: applyRes.data.total,
      hireCount: hireRes.data.total,
      salaryTotal: 15680
    }
  }).catch(() => {
    stats.value = {
      jobCount: 8,
      applyCount: 65,
      hireCount: 23,
      salaryTotal: 15680
    }
  })
}

onMounted(() => {
  loadData()
  setTimeout(() => {
    initCharts()
  }, 100)
})
</script>

<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>数据看板</h1>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="loadData"
      />
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon job">📋</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.jobCount }}</span>
          <span class="stat-label">岗位数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon apply">📥</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.applyCount }}</span>
          <span class="stat-label">投递数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon hire">✅</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.hireCount }}</span>
          <span class="stat-label">录用数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon salary">💰</div>
        <div class="stat-info">
          <span class="stat-value">¥{{ stats.salaryTotal.toLocaleString() }}</span>
          <span class="stat-label">薪资总额</span>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-item large">
        <div id="trendChart" class="chart"></div>
      </div>
      <div class="chart-item">
        <div id="typeChart" class="chart"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-item large">
        <div id="conversionChart" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
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

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background-color: #FFFFFF;
  padding: 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 16px;

  &.job { background-color: rgba(22, 93, 255, 0.1); }
  &.apply { background-color: rgba(82, 196, 26, 0.1); }
  &.hire { background-color: rgba(250, 173, 20, 0.1); }
  &.salary { background-color: rgba(255, 125, 0, 0.1); }
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1F2329;
}

.stat-label {
  font-size: 13px;
  color: #86909C;
  margin-top: 4px;
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;

  &:last-child {
    grid-template-columns: 1fr;
  }
}

.chart-item {
  background-color: #FFFFFF;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  &.large {
    grid-column: span 2;
  }
}

.chart {
  width: 100%;
  height: 300px;
}
</style>