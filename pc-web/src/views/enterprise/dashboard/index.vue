<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { jobApi } from '@/api/job'
import { talentApi } from '@/api/talent'
import {
  Briefcase,
  Document,
  CircleCheck,
  Wallet,
  Clock
} from '@element-plus/icons-vue'

const stats = ref({
  jobCount: 0,
  applyCount: 0,
  hireCount: 0,
  salaryTotal: 0
})

const dateRange = ref([dayjs().subtract(30, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])

const recentApplyList = ref<Array<{
  id: string
  studentName: string
  jobName: string
  applyTime: string
  status: string
}>>([])

const statusMap: Record<string, { label: string; type: string }> = {
  pending: { label: '待处理', type: 'warning' },
  interview: { label: '面试中', type: 'info' },
  hired: { label: '已录用', type: 'success' },
  rejected: { label: '已拒绝', type: 'danger' }
}



const initCharts = () => {
  const dates = []
  for (let i = 29; i >= 0; i--) {
    dates.push(dayjs().subtract(i, 'day').format('MM-DD'))
  }

  const trendChart = echarts.init(document.getElementById('trendChart') as HTMLElement)
  trendChart.setOption({
    title: { text: '近30天投递趋势', left: 16, top: 16, textStyle: { fontSize: 14, color: '#4E5969', fontWeight: 600 } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 60, containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 10, rotate: 45, color: '#86909C' }, axisLine: { lineStyle: { color: '#E5E6EB' } }, axisTick: { show: false } },
    yAxis: { type: 'value', axisLabel: { color: '#86909C' }, splitLine: { lineStyle: { color: '#F2F3F5' } }, axisLine: { show: false }, axisTick: { show: false } },
    series: [{
      name: '投递数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: new Array(30).fill(0),
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(22, 93, 255, 0.25)' },
        { offset: 1, color: 'rgba(22, 93, 255, 0.02)' }
      ]) },
      itemStyle: { color: '#165DFF', borderColor: '#fff', borderWidth: 2 },
      lineStyle: { width: 3, color: '#165DFF' }
    }],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E5E6EB',
      borderWidth: 1,
      textStyle: { color: '#4E5969' },
      padding: [8, 12],
      axisPointer: {
        type: 'line',
        lineStyle: { color: '#165DFF', type: 'dashed' }
      }
    }
  })

  const typeChart = echarts.init(document.getElementById('typeChart') as HTMLElement)
  typeChart.setOption({
    title: { text: '岗位类型分布', left: 'center', top: 16, textStyle: { fontSize: 14, color: '#4E5969', fontWeight: 600 } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)', backgroundColor: '#fff', borderColor: '#E5E6EB', borderWidth: 1, textStyle: { color: '#4E5969' }, padding: [8, 12] },
    legend: { bottom: 10, left: 'center', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: '#86909C' } },
    color: ['#165DFF', '#3C7EFF', '#FF7D00', '#FF9A2E', '#86909C'],
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 12, fontWeight: 600, color: '#4E5969' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.15)' }
      },
      labelLine: { show: false },
      data: [{ value: 1, name: '暂无数据' }]
    }]
  })

  const conversionChart = echarts.init(document.getElementById('conversionChart') as HTMLElement)
  conversionChart.setOption({
    title: { text: '各岗位投递转化率', left: 16, top: 16, textStyle: { fontSize: 14, color: '#4E5969', fontWeight: 600 } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 60, containLabel: true },
    xAxis: { type: 'category', data: ['暂无数据'], axisLabel: { fontSize: 10, rotate: 0, color: '#86909C' }, axisLine: { lineStyle: { color: '#E5E6EB' } }, axisTick: { show: false } },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}%', color: '#86909C' }, splitLine: { lineStyle: { color: '#F2F3F5' } }, axisLine: { show: false }, axisTick: { show: false } },
    series: [{
      type: 'bar',
      barWidth: 32,
      data: [0],
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#FF7D00' },
          { offset: 1, color: '#FFA940' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF9A2E' },
            { offset: 1, color: '#FFB366' }
          ])
        }
      }
    }],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E5E6EB',
      borderWidth: 1,
      textStyle: { color: '#4E5969' },
      padding: [8, 12],
      formatter: '{b}: {c}%'
    }
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
      jobCount: jobRes.data.total || 0,
      applyCount: applyRes.data.total || 0,
      hireCount: hireRes.data.total || 0,
      salaryTotal: 0
    }
  }).catch((error) => {
    console.error('Failed to load dashboard stats:', error)
    stats.value = {
      jobCount: 0,
      applyCount: 0,
      hireCount: 0,
      salaryTotal: 0
    }
  })

  talentApi.getApplyList({ page: 1, size: 5 }).then(res => {
    if (res.data && res.data.list) {
      recentApplyList.value = res.data.list.map((item: any) => ({
        id: item.apply_id || item.applyId || '',
        studentName: item.student_name || item.studentName || '',
        jobName: item.job_name || item.jobTitle || '',
        applyTime: item.apply_time || item.applyTime || '',
        status: item.status || item.statusStr || 'pending'
      }))
    } else {
      recentApplyList.value = []
    }
  }).catch((error) => {
    console.error('Failed to load recent applies:', error)
    recentApplyList.value = []
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
      <div class="header-left">
        <h1 class="page-title">数据看板</h1>
        <p class="page-subtitle">实时掌握企业招聘数据动态</p>
      </div>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        size="default"
        @change="loadData"
      />
    </div>

    <div class="stats-cards">
      <div class="stat-card stat-card--blue">
        <div class="stat-card__icon">
          <el-icon :size="24"><Briefcase /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ stats.jobCount }}</div>
          <div class="stat-card__label">岗位总数</div>
        </div>
        </div>
      <div class="stat-card stat-card--green">
        <div class="stat-card__icon">
          <el-icon :size="24"><Document /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ stats.applyCount }}</div>
          <div class="stat-card__label">投递总数</div>
        </div>
      </div>
      <div class="stat-card stat-card--orange">
        <div class="stat-card__icon">
          <el-icon :size="24"><CircleCheck /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ stats.hireCount }}</div>
          <div class="stat-card__label">录用人数</div>
        </div>
      </div>
      <div class="stat-card stat-card--purple">
        <div class="stat-card__icon">
          <el-icon :size="24"><Wallet /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">¥{{ stats.salaryTotal.toLocaleString() }}</div>
          <div class="stat-card__label">薪资总额</div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card chart-card--large">
        <div id="trendChart" class="chart"></div>
      </div>
      <div class="chart-card">
        <div id="typeChart" class="chart"></div>
      </div>
    </div>

    <div class="charts-row bottom-row">
      <div class="chart-card chart-card--large">
        <div id="conversionChart" class="chart"></div>
      </div>
      <div class="recent-card">
        <div class="recent-card__header">
          <span class="recent-card__title">最新投递</span>
          <el-button type="primary" link size="small">查看全部</el-button>
        </div>
        <div class="recent-list">
          <div class="recent-item" v-for="item in recentApplyList" :key="item.id">
            <div class="recent-item__avatar">
              {{ item.studentName.charAt(0) }}
            </div>
            <div class="recent-item__info">
              <div class="recent-item__name">{{ item.studentName }}</div>
              <div class="recent-item__job">
                <el-icon :size="12"><Briefcase /></el-icon>
                <span>{{ item.jobName }}</span>
              </div>
            </div>
            <div class="recent-item__right">
              <el-tag :type="statusMap[item.status].type" size="small" effect="light">
                {{ statusMap[item.status].label }}
              </el-tag>
              <div class="recent-item__time">
                <el-icon :size="12"><Clock /></el-icon>
                <span>{{ item.applyTime }}</span>
              </div>
            </div>
          </div>
        </div>
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
}

.header-left {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1F2329;
    margin: 0;
  }

  .page-subtitle {
    font-size: 13px;
    color: #86909C;
    margin: 4px 0 0 0;
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  overflow: hidden;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.95;
    z-index: 0;
  }

  &__icon {
    position: relative;
    z-index: 1;
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.25);
    color: #fff;
    margin-right: 16px;
    backdrop-filter: blur(4px);
  }

  &__info {
    position: relative;
    z-index: 1;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  &__value {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    line-height: 1.2;
  }

  &__label {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.85);
    margin-top: 4px;
  }

  

  &--blue::before {
    background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  }

  &--green::before {
    background: linear-gradient(135deg, #00B42A 0%, #23C343 100%);
  }

  &--orange::before {
    background: linear-gradient(135deg, #FF7D00 0%, #FF9A2E 100%);
  }

  &--purple::before {
    background: linear-gradient(135deg, #722ED1 0%, #9254DE 100%);
  }
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 16px;

  &.bottom-row {
    margin-bottom: 0;
  }
}

.chart-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px;

  &--large {
    grid-column: span 1;
  }
}

.bottom-row {
  .chart-card--large {
    grid-column: span 1;
  }
}

.chart {
  width: 100%;
  height: 300px;
}

.recent-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px;
  display: flex;
  flex-direction: column;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #F2F3F5;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: #4E5969;
  }
}

.recent-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.2s;

  &:hover {
    background-color: #F7F8FA;
  }

  &__avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
    margin-right: 12px;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__name {
    font-size: 13px;
    font-weight: 500;
    color: #1F2329;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__job {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #86909C;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
    flex-shrink: 0;
  }

  &__time {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    color: #C9CDD4;
  }
}
</style>
