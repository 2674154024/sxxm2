<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { jobApi, type Job } from '@/api/job'
import {
  Plus,
  Search,
  List,
  Grid,
  Edit,
  View,
  Bottom,
  Location,
  Money,
  User,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const tableData = ref<Job[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const searchKeyword = ref('')
const statusFilter = ref('')
const viewMode = ref<'table' | 'card'>('table')

const statusMap: Record<string, { label: string; type: string }> = {
  pending: { label: '待审核', type: 'warning' },
  approved: { label: '已上架', type: 'success' },
  rejected: { label: '已驳回', type: 'danger' },
  offline: { label: '已下架', type: 'info' }
}

const salaryTypeMap: Record<string, string> = {
  hourly: '小时',
  daily: '日',
  monthly: '月'
}

const settlementTypeMap: Record<string, string> = {
  weekly: '周结',
  daily: '日结',
  monthly: '月结'
}

const loadData = () => {
  jobApi.getJobList({ page: page.value, size: size.value, status: statusFilter.value }).then(res => {
    const rawList = res.data.list || []
    tableData.value = rawList.map((item: any) => ({
      job_id: String(item.jobId || item.job_id || ''),
      job_name: item.jobTitle || item.job_name || '',
      company_name: item.enterpriseName || item.company_name || '',
      salary: item.salaryAmount != null ? Number(item.salaryAmount) : item.salary || 0,
      salary_type: item.salaryTypeStr || item.salary_type || 'hourly',
      settlement_type: item.settlementTypeStr || item.settlement_type || 'weekly',
      address: item.workAddress || item.address || '',
      longitude: item.longitude != null ? Number(item.longitude) : item.longitude || 0,
      latitude: item.latitude != null ? Number(item.latitude) : item.latitude || 0,
      work_time: item.workTime ? JSON.parse(item.workTime) : { weekday: [], time_range: ['', ''] },
      skill_tags: Array.isArray(item.skillTags) ? item.skillTags : (item.skill_tags || []),
      description: item.description || '',
      has_insurance: item.hasInsurance || item.has_insurance || false,
      status: (() => {
        const statusNum = item.status
        if (statusNum === 0) return 'pending'
        if (statusNum === 1) return 'approved'
        if (statusNum === 2) return 'rejected'
        if (statusNum === 3) return 'offline'
        return item.statusStr || item.status || 'pending'
      })(),
      hired_count: item.hiredCount || item.hired_count || 0,
      apply_count: item.applyCount || item.apply_count || 0,
      view_count: item.viewCount || item.view_count || 0,
      created_time: item.createdAt || item.created_time || ''
    }))
    total.value = res.data.total || 0
  }).catch((error) => {
    console.error('Failed to load job list:', error)
    tableData.value = []
    total.value = 0
  })
}

const handleOffline = (jobId: string) => {
  ElMessageBox.confirm('确定要下架该岗位吗？', '确认下架', {
    confirmButtonText: '确定下架',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    jobApi.offlineJob(jobId).then(() => {
      ElMessage.success('已下架')
      loadData()
    })
  })
}

const handleEdit = (jobId: string) => {
  router.push(`/enterprise/job/edit?id=${jobId}`)
}

const handleDetail = (jobId: string) => {
  router.push(`/enterprise/job/detail?id=${jobId}`)
}

const handlePublish = () => {
  router.push('/enterprise/job/publish')
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="job-list">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">岗位列表</h1>
        <p class="page-subtitle">管理您发布的所有兼职岗位</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" size="default" @click="handlePublish">
          发布岗位
        </el-button>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索岗位名称"
            :prefix-icon="Search"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-item">
          <el-select
            v-model="statusFilter"
            placeholder="状态筛选"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option label="全部状态" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已上架" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已下架" value="offline" />
          </el-select>
        </div>
        <div class="filter-item flex-1">
          <div class="view-toggle">
            <span class="view-toggle__label">视图：</span>
            <div class="view-toggle__btns">
              <button
                class="view-toggle__btn"
                :class="{ active: viewMode === 'table' }"
                @click="viewMode = 'table'"
              >
                <el-icon :size="16"><List /></el-icon>
              </button>
              <button
                class="view-toggle__btn"
                :class="{ active: viewMode === 'card' }"
                @click="viewMode = 'card'"
              >
                <el-icon :size="16"><Grid /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template v-if="viewMode === 'table'">
      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="false" stripe style="width: 100%">
          <el-table-column prop="job_name" label="岗位名称" min-width="180">
            <template #default="scope">
              <div class="job-name-cell">
                <span class="job-name">{{ scope.row.job_name }}</span>
                <el-tag v-if="scope.row.has_insurance" type="success" size="small" effect="light">有保险</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="company_name" label="企业名称" min-width="180" />
          <el-table-column label="薪资" width="140">
            <template #default="scope">
              <div class="salary-cell">
                <span class="salary-amount">¥{{ scope.row.salary }}</span>
                <span class="salary-unit">/{{ salaryTypeMap[scope.row.salary_type] }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="settlement_type" label="结算周期" width="100">
            <template #default="scope">
              <el-tag type="info" effect="light" size="small">
                {{ settlementTypeMap[scope.row.settlement_type] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="工作地点" min-width="160">
            <template #default="scope">
              <div class="address-cell">
                <el-icon :size="14" color="#86909C"><Location /></el-icon>
                <span>{{ scope.row.address }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="skill_tags" label="技能标签" min-width="160">
            <template #default="scope">
              <div class="tags-cell">
                <el-tag
                  v-for="tag in scope.row.skill_tags"
                  :key="tag"
                  size="small"
                  effect="plain"
                  type="info"
                  style="margin-right: 4px"
                >{{ tag }}</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="数据统计" width="140">
            <template #default="scope">
              <div class="stats-cell">
                <div class="stat-item">
                  <el-icon :size="12" color="#165DFF"><Document /></el-icon>
                  <span>投递 {{ scope.row.apply_count }}</span>
                </div>
                <div class="stat-item">
                  <el-icon :size="12" color="#00B42A"><User /></el-icon>
                  <span>录用 {{ scope.row.hired_count }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="110">
            <template #default="scope">
              <el-tag :type="statusMap[scope.row.status].type" effect="light">
                {{ statusMap[scope.row.status].label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <div class="action-btns">
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="Edit"
                  @click="handleEdit(scope.row.job_id)"
                >编辑</el-button>
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="View"
                  @click="handleDetail(scope.row.job_id)"
                >详情</el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  :icon="Bottom"
                  @click="handleOffline(scope.row.job_id)"
                >下架</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <template v-else>
      <div class="job-cards">
        <div class="job-card" v-for="job in tableData" :key="job.job_id">
          <div class="job-card__header">
            <div class="job-card__title">
              <span class="job-name">{{ job.job_name }}</span>
              <el-tag :type="statusMap[job.status].type" effect="light" size="small">
                {{ statusMap[job.status].label }}
              </el-tag>
            </div>
            <div class="job-card__salary">
              <span class="amount">¥{{ job.salary }}</span>
              <span class="unit">/{{ salaryTypeMap[job.salary_type] }}</span>
            </div>
          </div>
          <div class="job-card__body">
            <div class="info-row">
              <el-icon :size="14" color="#86909C"><Location /></el-icon>
              <span>{{ job.address }}</span>
            </div>
            <div class="info-row">
              <el-icon :size="14" color="#86909C"><Money /></el-icon>
              <span>{{ settlementTypeMap[job.settlement_type] }}</span>
              <el-tag v-if="job.has_insurance" type="success" size="small" effect="light" style="margin-left: 8px">
                有保险
              </el-tag>
            </div>
            <div class="tags-row">
              <el-tag
                v-for="tag in job.skill_tags"
                :key="tag"
                size="small"
                effect="plain"
                type="info"
              >{{ tag }}</el-tag>
            </div>
          </div>
          <div class="job-card__footer">
            <div class="footer-stats">
              <span class="stat">
                <el-icon :size="12" color="#165DFF"><Document /></el-icon>
                投递 {{ job.apply_count }}
              </span>
              <span class="stat">
                <el-icon :size="12" color="#00B42A"><User /></el-icon>
                录用 {{ job.hired_count }}
              </span>
            </div>
            <div class="footer-actions">
              <el-button size="small" type="primary" plain @click="handleDetail(job.job_id)">详情</el-button>
              <el-button size="small" type="primary" plain @click="handleEdit(job.job_id)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleOffline(job.job_id)">下架</el-button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next, jumper"
        background
        @current-change="(val: number) => { page = val; loadData() }"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.job-list {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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

.filter-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
}

.flex-1 {
  flex: 1;
  justify-content: flex-end;
}

.view-toggle {
  display: flex;
  align-items: center;
  gap: 8px;

  &__label {
    font-size: 13px;
    color: #86909C;
  }

  &__btns {
    display: flex;
    background-color: #F2F3F5;
    border-radius: 6px;
    padding: 2px;
  }

  &__btn {
    width: 32px;
    height: 28px;
    border: none;
    background: none;
    border-radius: 4px;
    cursor: pointer;
    color: #86909C;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
      color: #4E5969;
    }

    &.active {
      background-color: #fff;
      color: #165DFF;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }
  }
}

.table-wrapper {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;

  :deep(.el-table) {
    --el-table-header-bg-color: #F7F8FA;
    --el-table-header-text-color: #4E5969;
    --el-table-row-hover-bg-color: #F7F8FA;
    --el-table-border-color: #F2F3F5;
  }

  :deep(.el-table th.el-table__cell) {
    font-weight: 600;
  }
}

.job-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.job-name {
  font-weight: 500;
  color: #1F2329;
}

.salary-cell {
  .salary-amount {
    font-size: 16px;
    font-weight: 600;
    color: #FF7D00;
  }

  .salary-unit {
    font-size: 12px;
    color: #86909C;
  }
}

.address-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #4E5969;
}

.tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.stats-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #86909C;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btns {
  display: flex;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  margin-top: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.job-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.job-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 16px;
    border-bottom: 1px solid #F2F3F5;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;

    .job-name {
      font-size: 16px;
      font-weight: 600;
      color: #1F2329;
    }
  }

  &__salary {
    text-align: right;

    .amount {
      font-size: 20px;
      font-weight: 700;
      color: #FF7D00;
    }

    .unit {
      font-size: 12px;
      color: #86909C;
    }
  }

  &__body {
    padding: 16px;

    .info-row {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #4E5969;
      margin-bottom: 10px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .tags-row {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 12px;
    }
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: #F7F8FA;
    border-top: 1px solid #F2F3F5;

    .footer-stats {
      display: flex;
      gap: 16px;

      .stat {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #86909C;
      }
    }

    .footer-actions {
      display: flex;
      gap: 8px;
    }
  }
}
</style>
