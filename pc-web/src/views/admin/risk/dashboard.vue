<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { riskApi, type RiskStats } from '@/api/admin'
import { ElCard, ElRow, ElCol, ElTable, ElTableColumn, ElTag, ElProgress } from 'element-plus'
import { Bell, Clock, CreditCard, CircleCheck, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const stats = ref<RiskStats>({
  today_complaint_count: 25,
  pending_count: 18,
  frozen_amount: 125800,
  compensated_amount: 35600
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

const priorityMap: Record<string, { label: string; color: 'danger' | 'warning' | 'info' }> = {
  high: { label: '高', color: 'danger' },
  medium: { label: '中', color: 'warning' },
  low: { label: '低', color: 'info' }
}

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

const statCards = computed(() => [
  { 
    title: '今日投诉数', 
    value: stats.value.today_complaint_count, 
    icon: Bell, 
    color: 'danger',
    trend: '+12%',
    trendUp: true
  },
  { 
    title: '待处理工单', 
    value: stats.value.pending_count, 
    icon: Clock, 
    color: 'warning',
    trend: '+8%',
    trendUp: true
  },
  { 
    title: '冻结金额', 
    value: `¥${stats.value.frozen_amount.toLocaleString()}`, 
    icon: CreditCard, 
    color: 'primary',
    trend: '+5%',
    trendUp: false
  },
  { 
    title: '已赔付金额', 
    value: `¥${stats.value.compensated_amount.toLocaleString()}`, 
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
    <ElRow :gutter="20">
      <ElCol 
        v-for="(card, index) in statCards" 
        :key="index" 
        :span="6"
      >
        <ElCard class="stat-card" :class="`stat-card-${card.color}`">
          <div class="stat-header">
            <component :is="card.icon" class="stat-icon" />
            <span class="stat-title">{{ card.title }}</span>
          </div>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-trend" :class="{ 'trend-up': card.trendUp }">
            <component :is="card.trendUp ? ArrowUp : ArrowDown" class="trend-icon" />
            <span>{{ card.trend }}</span>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="20" style="margin-top: 20px;">
      <ElCol :span="14">
        <ElCard title="投诉趋势（近7天）">
          <div id="trendChart" class="chart-container"></div>
        </ElCard>
      </ElCol>
      <ElCol :span="10">
        <ElCard title="投诉类型分布">
          <div id="typeChart" class="chart-container-small"></div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="20" style="margin-top: 20px;">
      <ElCol :span="14">
        <ElCard title="高风险企业预警">
          <ElTable :data="highRiskEnterprises" stripe>
            <ElTableColumn prop="name" label="企业名称" min-width="180" />
            <ElTableColumn prop="complaint_count" label="投诉次数" width="100" />
            <ElTableColumn prop="credit_score" label="信用分" width="120">
              <template #default="scope">
                <ElProgress 
                  :percentage="scope.row.credit_score" 
                  :color="scope.row.credit_score < 60 ? '#F53F3F' : scope.row.credit_score < 80 ? '#FF7D00' : '#00B42A'"
                  :stroke-width="8"
                  show-text
                />
              </template>
            </ElTableColumn>
            <ElTableColumn prop="status" label="风险等级" width="100">
              <template #default="scope">
                <ElTag :type="riskStatusMap[scope.row.status]?.color">
                  {{ scope.row.status }}
                </ElTag>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </ElCol>
      <ElCol :span="10">
        <ElCard title="待处理工单">
          <div class="pending-list">
            <div 
              v-for="item in pendingComplaints" 
              :key="item.id" 
              class="pending-item"
            >
              <div class="pending-info">
                <span class="pending-id">{{ item.id }}</span>
                <span class="pending-type">{{ item.type }}</span>
                <span class="pending-enterprise">{{ item.enterprise }}</span>
              </div>
              <div class="pending-meta">
                <span class="pending-time">{{ item.time }}</span>
                <ElTag :type="priorityMap[item.priority]?.color" size="small">
                  {{ priorityMap[item.priority]?.label }}
                </ElTag>
              </div>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped lang="scss">
.risk-dashboard {
  padding: 0;
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

  &.stat-card-danger::before { background-color: #F53F3F; }
  &.stat-card-warning::before { background-color: #FF7D00; }
  &.stat-card-primary::before { background-color: #165DFF; }
  &.stat-card-success::before { background-color: #00B42A; }
}

.stat-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.stat-icon {
  font-size: 20px;
  margin-right: 8px;
  color: #64748B;
}

.stat-title {
  font-size: 14px;
  color: #64748B;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1E293B;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #64748B;

  &.trend-up {
    color: #F53F3F;
  }
}

.trend-icon {
  font-size: 12px;
  margin-right: 4px;
}

.chart-container {
  height: 280px;
}

.chart-container-small {
  height: 280px;
}

.pending-list {
  max-height: 320px;
  overflow-y: auto;
}

.pending-item {
  padding: 12px 0;
  border-bottom: 1px solid #E2E8F0;

  &:last-child {
    border-bottom: none;
  }
}

.pending-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.pending-id {
  font-size: 13px;
  font-weight: bold;
  color: #475569;
}

.pending-type {
  font-size: 12px;
  color: #F53F3F;
}

.pending-enterprise {
  font-size: 12px;
  color: #94A3B8;
}

.pending-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pending-time {
  font-size: 12px;
  color: #94A3B8;
}
</style>