<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { riskApi, type RiskStats } from '@/api/admin'
import { ElRow, ElCol, ElTable, ElTableColumn, ElTag, ElProgress } from 'element-plus'
import { Bell, Clock, CreditCard, CircleCheck, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const stats = ref<RiskStats>({
  totalComplaints: 25,
  pendingComplaints: 18,
  handledComplaints: 7,
  totalCompensationAmount: 35600
})

const complaintTrend = ref([12, 18, 8, 25, 15, 22, 25])
const complaintTypeData = ref([
  { name: '虚假招聘', value: 45 },
  { name: '薪资拖欠', value: 38 },
  { name: '押金诈骗', value: 22 },
  { name: '未履约', value: 18 },
  { name: '信息泄露', value: 12 }
])

const highRiskEnterprises = ref([
  { name: '某科技有限公司', complaint_count: 12, credit_score: 45, status: '高风险' },
  { name: '某商贸有限公司', complaint_count: 8, credit_score: 58, status: '中风险' },
  { name: '某餐饮管理有限公司', complaint_count: 6, credit_score: 65, status: '中风险' },
  { name: '某教育咨询有限公司', complaint_count: 5, credit_score: 72, status: '低风险' },
  { name: '某文化传媒有限公司', complaint_count: 4, credit_score: 78, status: '低风险' },
])

const pendingComplaints = ref([
  { id: 'CL20260629001', type: '虚假招聘', enterprise: '某科技有限公司', time: '10:30', priority: 'high' },
  { id: 'CL20260629002', type: '薪资拖欠', enterprise: '某商贸有限公司', time: '09:15', priority: 'high' },
  { id: 'CL20260629003', type: '押金诈骗', enterprise: '某餐饮管理有限公司', time: '08:45', priority: 'medium' },
  { id: 'CL20260629004', type: '未履约', enterprise: '某教育咨询有限公司', time: '08:20', priority: 'medium' },
  { id: 'CL20260629005', type: '虚假招聘', enterprise: '某文化传媒有限公司', time: '07:50', priority: 'low' },
])

const riskStatusMap: Record<string, { color: 'danger' | 'warning' | 'success' }> = {
  '高风险': { color: 'danger' },
  '中风险': { color: 'warning' },
  '低风险': { color: 'success' }
}

const loadData = async () => {
  try {
    const response = await riskApi.getRiskDashboard()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载数据失败')
  }
}

const formatAmount = (value?: number | null) => {
  return `¥${(value ?? 0).toLocaleString()}`
}

const statCards = computed(() => [
  { 
    title: '总投诉数', 
    value: stats.value.totalComplaints ?? 0, 
    icon: Bell, 
    color: 'danger',
    trend: '+12%',
    trendUp: true
  },
  { 
    title: '待处理工单', 
    value: stats.value.pendingComplaints ?? 0, 
    icon: Clock, 
    color: 'warning',
    trend: '+8%',
    trendUp: true
  },
  { 
    title: '已处理工单', 
    value: stats.value.handledComplaints ?? 0, 
    icon: CreditCard, 
    color: 'primary',
    trend: '+5%',
    trendUp: false
  },
  { 
    title: '已赔付金额', 
    value: formatAmount(stats.value.totalCompensationAmount), 
    icon: CircleCheck, 
    color: 'success',
    trend: '-3%',
    trendUp: false
  }
])

onMounted(() => {
  loadData()
  
  setTimeout(() => {
    const trendChart = echarts.init(document.getElementById('trendChart'))
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{
        data: complaintTrend.value,
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        lineStyle: { color: '#F53F3F' },
        itemStyle: { color: '#F53F3F' }
      }]
    })

    const typeChart = echarts.init(document.getElementById('typeChart'))
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        data: complaintTypeData.value,
        type: 'pie',
        radius: ['40%', '70%'],
        itemStyle: { borderRadius: 4 },
        label: { formatter: '{b}: {c}件' }
      }]
    })
  }, 100)
})
</script>

<template>
  <div class="risk-dashboard">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">风控看板</h2>
        <p class="page-desc">实时监控平台风险数据，保障平台安全运营</p>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col 
        v-for="(card, index) in statCards" 
        :key="index" 
        :span="6"
      >
        <div class="stat-card" :class="`stat-card-${card.color}`">
          <div class="stat-header">
            <div class="stat-icon-box">
              <component :is="card.icon" class="stat-icon" />
            </div>
            <span class="stat-title">{{ card.title }}</span>
          </div>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-trend" :class="{ 'trend-up': card.trendUp, 'trend-down': !card.trendUp }">
            <component :is="card.trendUp ? ArrowUp : ArrowDown" class="trend-icon" />
            <span>{{ card.trend }}</span>
            <span class="trend-label">较昨日</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">投诉趋势（近7天）</span>
          </div>
          <div id="trendChart" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">投诉类型分布</span>
          </div>
          <div id="typeChart" class="chart-container-small"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="bottom-row">
      <el-col :span="16">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">高风险企业预警</span>
          </div>
          <el-table 
            :data="highRiskEnterprises" 
            class="risk-table"
            stripe
            :header-cell-style="{ background: '#F7F8FA', color: '#4E5969', fontWeight: 500 }"
          >
            <el-table-column prop="name" label="企业名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="complaint_count" label="投诉次数" width="100" align="center" />
            <el-table-column prop="credit_score" label="信用分" width="140">
              <template #default="scope">
                <el-progress 
                  :percentage="scope.row.credit_score" 
                  :color="scope.row.credit_score < 60 ? '#F53F3F' : scope.row.credit_score < 80 ? '#FF7D00' : '#00B42A'"
                  :stroke-width="8"
                  :text-inside="false"
                  :show-text="true"
                />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="风险等级" width="100" align="center">
              <template #default="scope">
                <el-tag :type="riskStatusMap[scope.row.status]?.color" effect="light" round size="small">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">待处理工单</span>
            <el-tag type="danger" effect="light" size="small">{{ pendingComplaints.length }} 条</el-tag>
          </div>
          <div class="pending-list">
            <div 
              v-for="item in pendingComplaints" 
              :key="item.id" 
              class="pending-item"
            >
              <div class="pending-left">
                <div class="priority-dot" :class="`priority-${item.priority}`"></div>
                <div class="pending-info">
                  <span class="pending-id">{{ item.id }}</span>
                  <span class="pending-type">{{ item.type }}</span>
                </div>
              </div>
              <div class="pending-right">
                <span class="pending-time">{{ item.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.risk-dashboard {
  min-height: 100%;
}

.page-header {
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

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
  }

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-1px);
  }

  &.stat-card-danger::before { background-color: #F53F3F; }
  &.stat-card-warning::before { background-color: #FF7D00; }
  &.stat-card-primary::before { background-color: #165DFF; }
  &.stat-card-success::before { background-color: #00B42A; }
}

.stat-header {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
  gap: 12px;
}

.stat-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .stat-card-danger & {
    background: #FFECE8;
    color: #F53F3F;
  }
  .stat-card-warning & {
    background: #FFF7E8;
    color: #FF7D00;
  }
  .stat-card-primary & {
    background: #E8F3FF;
    color: #165DFF;
  }
  .stat-card-success & {
    background: #E8FFEA;
    color: #00B42A;
  }
}

.stat-icon {
  font-size: 22px;
}

.stat-title {
  font-size: 13px;
  color: #86909C;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 8px;
  line-height: 1.2;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #86909C;
  gap: 4px;

  &.trend-up {
    color: #F53F3F;
  }

  &.trend-down {
    color: #00B42A;
  }
}

.trend-icon {
  font-size: 12px;
}

.trend-label {
  color: #C9CDD4;
  margin-left: 4px;
}

.chart-row {
  margin-bottom: 16px;
}

.bottom-row {
  margin-bottom: 0;
}

.chart-card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F2F3F5;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
}

.chart-container {
  height: 280px;
}

.chart-container-small {
  height: 280px;
}

.risk-table {
  :deep(.el-table__row) {
    &:hover > td {
      background-color: #F7F8FA !important;
    }
  }

  :deep(.el-table__cell) {
    padding: 10px 0;
  }
}

.pending-list {
  max-height: 320px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #C9CDD4;
    border-radius: 2px;
  }
}

.pending-item {
  padding: 12px 0;
  border-bottom: 1px solid #F2F3F5;
  display: flex;
  justify-content: space-between;
  align-items: center;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: #F7F8FA;
    margin: 0 -12px;
    padding-left: 12px;
    padding-right: 12px;
    border-radius: 6px;
  }
}

.pending-left {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.priority-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;

  &.priority-high { background: #F53F3F; box-shadow: 0 0 6px rgba(245, 63, 63, 0.4); }
  &.priority-medium { background: #FF7D00; box-shadow: 0 0 6px rgba(255, 125, 0, 0.4); }
  &.priority-low { background: #165DFF; box-shadow: 0 0 6px rgba(22, 93, 255, 0.4); }
}

.pending-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pending-id {
  font-size: 13px;
  font-weight: 500;
  color: #4E5969;
}

.pending-type {
  font-size: 12px;
  color: #86909C;
}

.pending-right {
  flex-shrink: 0;
}

.pending-time {
  font-size: 12px;
  color: #C9CDD4;
}
</style>