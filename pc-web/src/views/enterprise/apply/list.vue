<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElTag, ElPagination, ElSelect, ElOption, ElMessage, ElCard } from 'element-plus'
import { useRouter } from 'vue-router'
import { jobApi } from '@/api/job'

const router = useRouter()
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref<number | undefined>(undefined)

const applyList = ref<any[]>([])

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '面试中', type: 'primary' },
  2: { label: '已录用', type: 'success' },
  3: { label: '已拒绝', type: 'danger' }
}

const settlementTypeMap: Record<number, string> = {
  0: '日结',
  1: '周结',
  2: '月结'
}

const fetchApplyList = () => {
  loading.value = true
  jobApi.getApplyList({
    page: currentPage.value,
    size: pageSize.value,
    status: statusFilter.value
  }).then(res => {
    const data = res.data
    if (data) {
      applyList.value = data.list || data.records || []
      total.value = data.total || 0
      currentPage.value = data.page || 1
      pageSize.value = data.size || 10
    }
    loading.value = false
  }).catch((error: any) => {
    console.error('Fetch apply list error:', error)
    loading.value = false
    applyList.value = []
    total.value = 0
  })
}

const handleStatusChange = (val: number | undefined) => {
  statusFilter.value = val
  currentPage.value = 1
  fetchApplyList()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchApplyList()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchApplyList()
}

const handleViewDetail = (applyId: string) => {
  ElMessage.info(`查看投递详情: ${applyId}`)
}

const handleInterview = (applyId: string) => {
  ElMessage.success(`安排面试: ${applyId}`)
}

const handleReject = (applyId: string) => {
  ElMessage.warning(`拒绝投递: ${applyId}`)
}

onMounted(() => {
  fetchApplyList()
})
</script>

<template>
  <div class="apply-list">
    <div class="page-header">
      <div class="header-left">
        <h1>投递管理</h1>
        <p class="subtitle">查看和管理求职者的岗位投递申请</p>
      </div>
    </div>

    <div class="content-container">
      <ElCard class="filter-card" shadow="never">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">状态筛选:</span>
            <ElSelect 
              v-model="statusFilter" 
              placeholder="全部状态" 
              clearable
              size="large"
              style="width: 160px;"
              @change="handleStatusChange"
            >
              <ElOption label="待审核" :value="0" />
              <ElOption label="面试中" :value="1" />
              <ElOption label="已录用" :value="2" />
              <ElOption label="已拒绝" :value="3" />
            </ElSelect>
          </div>
        </div>
      </ElCard>

      <ElCard class="table-card" shadow="never">
        <ElTable 
          :data="applyList" 
          :loading="loading"
          border
          stripe
          size="large"
          class="apply-table"
          v-loading="loading"
        >
          <ElTableColumn prop="apply_time" label="投递时间" width="180" />
          <ElTableColumn prop="jobTitle" label="岗位名称" min-width="180">
            <template #default="scope">
              <span class="job-title">{{ scope.row.jobTitle || scope.row.job_name }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="workAddress" label="工作地点" min-width="150">
            <template #default="scope">
              {{ scope.row.workAddress || scope.row.work_address }}
            </template>
          </ElTableColumn>
          <ElTableColumn label="薪资" width="120">
            <template #default="scope">
              <span class="salary">
                {{ scope.row.salaryAmount || scope.row.salary_amount }}元/时
              </span>
            </template>
          </ElTableColumn>
          <ElTableColumn label="结算周期" width="100">
            <template #default="scope">
              {{ settlementTypeMap[scope.row.settlementType || scope.row.settlement_type] || '周结' }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" label="状态" width="100">
            <template #default="scope">
              <ElTag 
                :type="statusMap[scope.row.applyStatus || scope.row.apply_status]?.type || 'info'"
                size="small"
              >
                {{ statusMap[scope.row.applyStatus || scope.row.apply_status]?.label || scope.row.status || '待审核' }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="200" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <ElButton 
                  size="small" 
                  type="primary" 
                  plain
                  @click="handleViewDetail(scope.row.applyId || scope.row.apply_id)"
                >详情</ElButton>
                <ElButton 
                  v-if="(scope.row.applyStatus || scope.row.apply_status) === 0"
                  size="small" 
                  type="primary" 
                  @click="handleInterview(scope.row.applyId || scope.row.apply_id)"
                >安排面试</ElButton>
                <ElButton 
                  v-if="(scope.row.applyStatus || scope.row.apply_status) === 0"
                  size="small" 
                  type="danger" 
                  plain
                  @click="handleReject(scope.row.applyId || scope.row.apply_id)"
                >拒绝</ElButton>
              </div>
            </template>
          </ElTableColumn>
        </ElTable>

        <div class="pagination-container" v-if="total > 0">
          <ElPagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            size="large"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>

        <div class="empty-state" v-else-if="!loading">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M24 4C12.9543 4 4 12.9543 4 24C4 35.0457 12.9543 44 24 44C35.0457 44 44 35.0457 44 24C44 12.9543 35.0457 4 24 4ZM24 40C14.0589 40 6 31.9411 6 24C6 16.0589 14.0589 8 24 8C33.9411 8 42 16.0589 42 24C42 31.9411 33.9411 40 24 40Z" fill="#C9CDD4"/>
              <path d="M16 20H32V22H16V20ZM16 26H28V28H16V26ZM16 32H24V34H16V32Z" fill="#86909C"/>
            </svg>
          </div>
          <p class="empty-text">暂无投递记录</p>
          <p class="empty-hint">发布岗位后，求职者投递的申请将在这里显示</p>
        </div>
      </ElCard>
    </div>
  </div>
</template>

<style scoped>
.apply-list {
  padding: 0;
  min-height: 100%;
  background-color: #F2F3F5;
}

.page-header {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  padding: 32px 40px;
  margin-bottom: 24px;

  .header-left {
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #FFFFFF;
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.8);
      margin: 0;
    }
  }
}

.content-container {
  padding: 0 40px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;

  :deep(.el-card__body) {
    padding: 16px 24px;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 24px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-size: 14px;
  color: #4E5969;
  font-weight: 500;
}

.table-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  :deep(.el-card__body) {
    padding: 0;
  }
}

.apply-table {
  width: 100%;
}

.job-title {
  color: #165DFF;
  font-weight: 500;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.salary {
  color: #165DFF;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination-container {
  padding: 20px 24px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #F2F3F5;
}

.empty-state {
  padding: 60px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #4E5969;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.empty-hint {
  font-size: 14px;
  color: #86909C;
  margin: 0;
}

:deep(.el-table--border) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background-color: #F7F8FA;

  th {
    background-color: #F7F8FA;
    color: #4E5969;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 1px solid #E5E6EB;
  }
}

:deep(.el-table__body) {
  tr {
    transition: background-color 0.2s;

    &:hover {
      background-color: #F7F8FA;
    }
  }

  td {
    color: #1F2329;
    font-size: 14px;
    border-bottom: 1px solid #F2F3F5;
  }
}

:deep(.el-tag) {
  border-radius: 4px;
}

:deep(.el-button--small) {
  border-radius: 6px;
}
</style>
