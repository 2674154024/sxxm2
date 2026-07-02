<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElTable, ElTableColumn, ElButton, ElDatePicker, ElTag } from 'element-plus'
import { ArrowUp } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const dateRange = ref<[string, string]>(['', ''])

const incomeData = ref([120000, 150000, 135000, 180000, 165000, 200000, 190000, 220000, 210000, 240000, 230000, 260000])
const expenseData = ref([80000, 95000, 85000, 110000, 100000, 120000, 115000, 135000, 130000, 145000, 140000, 155000])

const revenueStats = ref({
  totalIncome: 2500000,
  totalExpense: 1500000,
  profit: 1000000,
  profitRate: 40
})

const settlementStats = ref([
  { month: '1月', total: 350000, count: 1200 },
  { month: '2月', total: 320000, count: 1100 },
  { month: '3月', total: 380000, count: 1300 },
  { month: '4月', total: 420000, count: 1450 },
  { month: '5月', total: 450000, count: 1500 },
  { month: '6月', total: 500000, count: 1650 },
])

const exportExcel = () => {
  console.log('导出财务报表')
}

onMounted(() => {
  setTimeout(() => {
    const revenueChart = echarts.init(document.getElementById('revenueChart'))
    revenueChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['收入', '支出'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'] },
      yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
      series: [
        { name: '收入', type: 'bar', data: incomeData.value, itemStyle: { color: '#00B42A' } },
        { name: '支出', type: 'bar', data: expenseData.value, itemStyle: { color: '#F53F3F' } }
      ]
    })

    const profitChart = echarts.init(document.getElementById('profitChart'))
    profitChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
      yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
      series: [{
        data: [40000, 55000, 50000, 70000, 65000, 80000],
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        lineStyle: { color: '#165DFF' },
        itemStyle: { color: '#165DFF' }
      }]
    })
  }, 100)
})
</script>

<template>
  <div class="finance-report">
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
        <ElCard class="stat-card stat-card-success">
          <div class="stat-header">总收入</div>
          <div class="stat-value">¥{{ revenueStats.totalIncome.toLocaleString() }}</div>
          <div class="stat-trend trend-up">
            <ArrowUp />
            <span>+15.2%</span>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card stat-card-danger">
          <div class="stat-header">总支出</div>
          <div class="stat-value">¥{{ revenueStats.totalExpense.toLocaleString() }}</div>
          <div class="stat-trend">
            <ArrowUp />
            <span>+8.5%</span>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card stat-card-primary">
          <div class="stat-header">净利润</div>
          <div class="stat-value">¥{{ revenueStats.profit.toLocaleString() }}</div>
          <div class="stat-trend trend-up">
            <ArrowUp />
            <span>+25.8%</span>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card stat-card-info">
          <div class="stat-header">利润率</div>
          <div class="stat-value">{{ revenueStats.profitRate }}%</div>
          <div class="stat-trend trend-up">
            <ArrowUp />
            <span>+5.3%</span>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="20" style="margin-top: 20px;">
      <ElCol :span="16">
        <ElCard title="收支趋势">
          <div id="revenueChart" class="chart-container"></div>
        </ElCard>
      </ElCol>
      <ElCol :span="8">
        <ElCard title="利润趋势">
          <div id="profitChart" class="chart-container-small"></div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="20" style="margin-top: 20px;">
      <ElCol :span="24">
        <ElCard title="月度结算统计">
          <ElTable :data="settlementStats" stripe>
            <ElTableColumn prop="month" label="月份" width="80" />
            <ElTableColumn prop="total" label="结算总额" width="150">
              <template #default="scope">
                <span class="amount">¥{{ scope.row.total.toLocaleString() }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="count" label="结算笔数" width="120" />
            <ElTableColumn label="单笔平均" width="150">
              <template #default="scope">
                ¥{{ Math.round(scope.row.total / scope.row.count).toLocaleString() }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="同比增长" width="120">
              <template #default>
                <ElTag type="success">+12.5%</ElTag>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped lang="scss">
.finance-report {
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
  border-radius: 8px;
  padding: 20px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
  }

  &.stat-card-success::before { background-color: #00B42A; }
  &.stat-card-danger::before { background-color: #F53F3F; }
  &.stat-card-primary::before { background-color: #165DFF; }
  &.stat-card-info::before { background-color: #0EA5E9; }
}

.stat-header {
  font-size: 14px;
  color: #64748B;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1E293B;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #94A3B8;

  &.trend-up {
    color: #00B42A;
  }

  .el-icon {
    font-size: 12px;
    margin-right: 4px;
  }
}

.chart-container {
  height: 300px;
}

.chart-container-small {
  height: 300px;
}

.amount {
  color: #165DFF;
  font-weight: bold;
}
</style>