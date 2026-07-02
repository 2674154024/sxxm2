<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { jobApi } from '@/api/job'
import { 
  Location,
  Clock, 
  CollectionTag,
  User,
  View,
  Document,
  CircleCheck,
  OfficeBuilding,
  Calendar,
  ArrowLeft
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const jobId = route.query.id as string

interface JobDetail {
  job_id: string
  job_name: string
  company_name: string
  salary: number
  salary_type: 'hourly' | 'daily' | 'monthly'
  settlement_type: 'daily' | 'weekly' | 'monthly'
  address: string
  longitude: number
  latitude: number
  work_time: {
    weekday: string[]
    time_range: string[]
  }
  skill_tags: string[]
  description: string
  has_insurance: boolean
  recruit_num: number
  status: 'pending' | 'approved' | 'rejected' | 'offline'
  apply_count: number
  view_count: number
  created_time: string
}

const job = ref<JobDetail | null>(null)

const salaryTypeMap: Record<string, string> = {
  hourly: '元/小时',
  daily: '元/天',
  monthly: '元/月'
}

const settlementTypeMap: Record<string, string> = {
  daily: '日结',
  weekly: '周结',
  monthly: '月结'
}

const statusMap: Record<string, { label: string; type: string; bgColor: string; textColor: string }> = {
  pending: { label: '待审核', type: 'warning', bgColor: '#FFF7E6', textColor: '#FF7D00' },
  approved: { label: '已上架', type: 'success', bgColor: '#E8FFEA', textColor: '#00B42A' },
  rejected: { label: '已驳回', type: 'danger', bgColor: '#FFF1F0', textColor: '#F53F3F' },
  offline: { label: '已下架', type: 'info', bgColor: '#F2F3F5', textColor: '#86909C' }
}

const loadJobDetail = () => {
  loading.value = true
  jobApi.getJobDetail(jobId).then(res => {
    const data = res.data as any
    if (data) {
      const statusMap: Record<number, string> = {
        0: 'pending',
        1: 'approved',
        2: 'rejected',
        3: 'offline'
      }
      const salaryTypeMap: Record<number, string> = {
        0: 'hourly',
        1: 'daily',
        2: 'monthly'
      }
      const settlementTypeMap: Record<number, string> = {
        0: 'daily',
        1: 'weekly',
        2: 'monthly'
      }
      
      job.value = {
        job_id: data.jobId || data.job_id || '',
        job_name: data.jobTitle || data.job_name || '',
        company_name: data.enterpriseName || data.company_name || '',
        salary: data.salaryAmount != null ? Number(data.salaryAmount) : data.salary || 0,
        salary_type: salaryTypeMap[data.salaryType] || data.salaryTypeStr || data.salary_type || 'hourly',
        settlement_type: settlementTypeMap[data.settlementType] || data.settlementTypeStr || data.settlement_type || 'weekly',
        address: data.workAddress || data.address || '',
        longitude: data.longitude != null ? Number(data.longitude) : data.longitude || 0,
        latitude: data.latitude != null ? Number(data.latitude) : data.latitude || 0,
        work_time: data.workTime ? JSON.parse(data.workTime) : (data.work_time || { weekday: [], time_range: ['', ''] }),
        skill_tags: Array.isArray(data.skillTags) ? data.skillTags : (data.skill_tags || []),
        description: data.description || '',
        has_insurance: data.hasInsurance || data.has_insurance || (data.isInsured != null && data.isInsured === 1),
        recruit_num: data.recruitNum || data.recruit_num || 1,
        status: statusMap[data.status] || data.statusStr || data.status || 'pending',
        apply_count: data.applyCount || data.apply_count || 0,
        view_count: data.viewCount || data.view_count || 0,
        created_time: data.createdAt || data.created_time || ''
      }
    }
    loading.value = false
  }).catch((error: any) => {
    console.error('Load job detail error:', error)
    loading.value = false
  })
}

const handleEdit = () => {
  router.push(`/enterprise/job/edit?id=${jobId}`)
}

onMounted(() => {
  if (jobId) {
    loadJobDetail()
  }
})
</script>

<template>
  <div class="job-detail" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <button class="back-btn" @click="router.push('/enterprise/job/list')">
          <el-icon :size="18"><ArrowLeft /></el-icon>
          <span>返回列表</span>
        </button>
        <h1 class="page-title">{{ job?.job_name || '岗位详情' }}</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleEdit">编辑岗位</el-button>
      </div>
    </div>

    <div class="detail-container">
      <div class="main-card">
        <div class="card-header">
          <div class="job-title-section">
            <h2 class="job-title">{{ job?.job_name }}</h2>
            <span 
              class="status-badge"
              :style="{ backgroundColor: job ? statusMap[job.status].bgColor : '', color: job ? statusMap[job.status].textColor : '' }"
            >
              {{ job ? statusMap[job.status].label : '' }}
            </span>
          </div>
          <div class="salary-section">
            <span class="salary-amount">¥{{ job?.salary }}</span>
            <span class="salary-unit">{{ salaryTypeMap[job?.salary_type || 'hourly'] }}</span>
          </div>
        </div>

        <div class="card-body">
          <div class="info-grid">
            <div class="info-item">
              <div class="info-icon" style="background-color: #E8F0FE;">
                <el-icon :size="16" color="#165DFF"><Building /></el-icon>
              </div>
              <div class="info-content">
                <span class="info-label">企业名称</span>
                <span class="info-value">{{ job?.company_name }}</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon" style="background-color: #E8F0FE;">
                <el-icon :size="16" color="#165DFF"><MapPin /></el-icon>
              </div>
              <div class="info-content">
                <span class="info-label">工作地点</span>
                <span class="info-value">{{ job?.address }}</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon" style="background-color: #FFF7E6;">
                <el-icon :size="16" color="#FF7D00"><Clock /></el-icon>
              </div>
              <div class="info-content">
                <span class="info-label">工作时间</span>
                <span class="info-value">
                  {{ job?.work_time.weekday.join('、') || '全天' }}
                  <span v-if="job?.work_time.time_range[0] && job?.work_time.time_range[1]">
                    {{ job.work_time.time_range[0] }}-{{ job.work_time.time_range[1] }}
                  </span>
                </span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon" style="background-color: #FFF7E6;">
                <el-icon :size="16" color="#FF7D00"><Calendar /></el-icon>
              </div>
              <div class="info-content">
                <span class="info-label">结算周期</span>
                <span class="info-value">{{ settlementTypeMap[job?.settlement_type || 'weekly'] }}</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon" style="background-color: #E8FFEA;">
                <el-icon :size="16" color="#00B42A"><Users /></el-icon>
              </div>
              <div class="info-content">
                <span class="info-label">招聘人数</span>
                <span class="info-value">{{ job?.recruit_num }}人</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon" :style="{ backgroundColor: job?.has_insurance ? '#E8FFEA' : '#F2F3F5' }">
                <el-icon :size="16" :color="job?.has_insurance ? '#00B42A' : '#86909C'"><ShieldCheck /></el-icon>
              </div>
              <div class="info-content">
                <span class="info-label">购买保险</span>
                <span class="info-value">{{ job?.has_insurance ? '是' : '否' }}</span>
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <div class="section">
            <div class="section-header">
              <el-icon :size="18" color="#165DFF"><Tag /></el-icon>
              <h3 class="section-title">技能标签</h3>
            </div>
            <div class="tags-container">
              <el-tag 
                v-for="tag in job?.skill_tags" 
                :key="tag"
                size="default"
                effect="plain"
                type="info"
              >{{ tag }}</el-tag>
              <span v-if="!job?.skill_tags?.length" class="empty-tip">暂无技能标签</span>
            </div>
          </div>

          <div class="section">
            <div class="section-header">
              <el-icon :size="18" color="#165DFF"><FileText /></el-icon>
              <h3 class="section-title">岗位描述</h3>
            </div>
            <div class="description-content">
              {{ job?.description || '暂无描述' }}
            </div>
          </div>
        </div>
      </div>

      <div class="side-card">
        <div class="stats-section">
          <h3 class="stats-title">数据统计</h3>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon" style="background-color: #E8F0FE;">
                <el-icon :size="16" color="#165DFF"><Eye /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ job?.view_count }}</span>
                <span class="stat-label">浏览次数</span>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon" style="background-color: #E8FFEA;">
                <el-icon :size="16" color="#00B42A"><Users /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ job?.apply_count }}</span>
                <span class="stat-label">投递人数</span>
              </div>
            </div>
          </div>
        </div>

        <div class="time-section">
          <h3 class="time-title">发布时间</h3>
          <span class="time-value">{{ job?.created_time || '-' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.job-detail {
  padding: 0;
  min-height: 100%;
  background-color: #F2F3F5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 40px;
  background-color: #FFFFFF;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: none;
  border: none;
  cursor: pointer;
  color: #86909C;
  font-size: 14px;
  transition: all 0.2s;

  &:hover {
    color: #165DFF;
  }
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1F2329;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.detail-container {
  display: flex;
  gap: 24px;
  padding: 0 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.main-card {
  flex: 1;
  background-color: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  border-bottom: 1px solid #F2F3F5;
  background: linear-gradient(135deg, #F7F8FA 0%, #FFFFFF 100%);
}

.job-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.job-title {
  font-size: 24px;
  font-weight: 600;
  color: #1F2329;
  margin: 0;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.salary-section {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.salary-amount {
  font-size: 28px;
  font-weight: 700;
  color: #FF7D00;
}

.salary-unit {
  font-size: 14px;
  color: #86909C;
}

.card-body {
  padding: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.info-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #86909C;
}

.info-value {
  font-size: 14px;
  color: #1F2329;
  font-weight: 500;
}

.divider {
  height: 1px;
  background-color: #F2F3F5;
  margin: 24px 0;
}

.section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2329;
  margin: 0;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty-tip {
  font-size: 14px;
  color: #86909C;
}

.description-content {
  font-size: 14px;
  color: #4E5969;
  line-height: 1.8;
  white-space: pre-wrap;
}

.side-card {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-section {
  background-color: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.stats-title {
  font-size: 14px;
  font-weight: 600;
  color: #1F2329;
  margin: 0 0 16px 0;
}

.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1F2329;
}

.stat-label {
  font-size: 12px;
  color: #86909C;
}

.time-section {
  background-color: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.time-title {
  font-size: 14px;
  font-weight: 600;
  color: #1F2329;
  margin: 0 0 12px 0;
}

.time-value {
  font-size: 14px;
  color: #4E5969;
}

@media (max-width: 1024px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .detail-container {
    flex-direction: column;
  }

  .side-card {
    width: 100%;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>