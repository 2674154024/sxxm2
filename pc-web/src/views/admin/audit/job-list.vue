<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { auditApi, type JobAuditItem } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Search, Refresh, RefreshLeft, Clock, CircleCheck, Bottom, Warning } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const tableData = ref<JobAuditItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const searchForm = ref({
  keyword: '',
  industry_tag: '',
  status: '',
})

const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const currentItem = ref<JobAuditItem | null>(null)
const auditForm = ref({
  action: '',
  reason: ''
})

const statusMap: Record<number, { label: string; color: 'warning' | 'success' | 'danger' }> = {
  0: { label: '待审核', color: 'warning' },
  1: { label: '已发布', color: 'success' },
  2: { label: '已下架', color: 'danger' },
}

const salaryTypeMap: Record<number, string> = {
  0: '小时',
  1: '日',
  2: '月'
}

const settlementTypeMap: Record<number, string> = {
  0: '日结',
  1: '周结',
  2: '月结'
}

const pendingCount = computed(() => tableData.value.filter(item => item.status === 0).length)
const publishedCount = computed(() => tableData.value.filter(item => item.status === 1).length)
const offlineCount = computed(() => tableData.value.filter(item => item.status === 2).length)
const sensitiveCount = computed(() => tableData.value.filter(item => item.has_sensitive_word).length)

const loadData = async () => {
  try {
    const response = await auditApi.getJobAuditList({
      page: page.value,
      size: size.value,
      status: searchForm.value.status ? parseInt(searchForm.value.status) : undefined,
      keyword: searchForm.value.keyword || undefined
    })
    
    if (response.code === 200) {
      tableData.value = response.data.records || response.data.list || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    keyword: '',
    industry_tag: '',
    status: '',
  }
  page.value = 1
  loadData()
}

const handleDetail = (row: JobAuditItem) => {
  currentItem.value = row
  detailDialogVisible.value = true
}

const handleAudit = (row: JobAuditItem, action: 'pass' | 'reject') => {
  currentItem.value = row
  auditForm.value = {
    action,
    reason: ''
  }
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (!currentItem.value) return
  
  if (auditForm.value.action === 'reject' && !auditForm.value.reason.trim()) {
    ElMessage.warning('请填写驳回理由')
    return
  }

  try {
    await auditApi.auditJob({
      job_id: currentItem.value.job_id,
      action: auditForm.value.action as 'pass' | 'reject',
      reason: auditForm.value.reason
    })
    
    ElMessage.success(auditForm.value.action === 'pass' ? '审核通过' : '下架成功')
    auditDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSizeChange = (val: number) => {
  size.value = val
  page.value = 1
  loadData()
}

const handlePageChange = (val: number) => {
  page.value = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="job-audit">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">岗位审核</h2>
        <p class="page-desc">管理岗位信息审核，保障平台岗位内容合规</p>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-warning">
          <div class="stat-icon">
            <Clock />
          </div>
          <div class="stat-content">
            <div class="stat-value">待审核</div>
            <div class="stat-number">{{ pendingCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-icon">
            <CircleCheck />
          </div>
          <div class="stat-content">
            <div class="stat-value">已发布</div>
            <div class="stat-number">{{ publishedCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-danger">
          <div class="stat-icon">
            <Bottom />
          </div>
          <div class="stat-content">
            <div class="stat-value">已下架</div>
            <div class="stat-number">{{ offlineCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-primary">
          <div class="stat-icon">
            <Warning />
          </div>
          <div class="stat-content">
            <div class="stat-value">含敏感词</div>
            <div class="stat-number">{{ sensitiveCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="content-card">
      <div class="search-bar">
        <div class="search-form">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入岗位名称关键词" 
            clearable
            style="width: 240px"
            :prefix-icon="Search"
          />
          <el-select 
            v-model="searchForm.industry_tag" 
            placeholder="行业分类" 
            clearable
            style="width: 140px"
          >
            <el-option label="全部分类" value="" />
            <el-option label="茶饮" value="茶饮" />
            <el-option label="零售" value="零售" />
            <el-option label="家教" value="家教" />
            <el-option label="会展" value="会展" />
            <el-option label="上门服务" value="上门服务" />
            <el-option label="新媒体" value="新媒体" />
          </el-select>
          <el-select 
            v-model="searchForm.status" 
            placeholder="审核状态" 
            clearable
            style="width: 140px"
          >
            <el-option label="待审核" value="0" />
            <el-option label="已发布" value="1" />
            <el-option label="已下架" value="2" />
          </el-select>
        </div>
        <div class="search-actions">
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="RefreshLeft">重置</el-button>
          <el-button @click="loadData" :icon="Refresh">刷新</el-button>
        </div>
      </div>

      <el-table 
        :data="tableData" 
        class="audit-table"
        stripe
        :header-cell-style="{ background: '#F7F8FA', color: '#4E5969', fontWeight: 500 }"
      >
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="job_title" label="岗位名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="enterprise_name" label="企业名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="salary_amount" label="薪资" width="120" align="center">
          <template #default="scope">
            <span class="salary">¥{{ scope.row.salary_amount }}/{{ salaryTypeMap[scope.row.salary_type] }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="settlement_type" label="结算周期" width="100" align="center">
          <template #default="scope">
            <el-tag type="info" effect="light" size="small">
              {{ settlementTypeMap[scope.row.settlement_type] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="work_address" label="工作地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="has_sensitive_word" label="AI审核" width="100" align="center">
          <template #default="scope">
            <el-tag 
              v-if="scope.row.has_sensitive_word" 
              type="danger" 
              effect="light"
              round
            >
              含敏感词
            </el-tag>
            <el-tag 
              v-else 
              type="success" 
              effect="light"
              round
            >
              正常
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sensitive_words" label="敏感词" min-width="160">
          <template #default="scope">
            <span v-if="scope.row.sensitive_words && scope.row.sensitive_words.length" class="sensitive-tags">
              <el-tag 
                v-for="word in scope.row.sensitive_words.slice(0, 3)" 
                :key="word" 
                type="danger" 
                size="small"
                effect="light"
              >
                {{ word }}
              </el-tag>
              <span v-if="scope.row.sensitive_words.length > 3" class="more-tag">
                +{{ scope.row.sensitive_words.length - 3 }}
              </span>
            </span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag 
              :type="statusMap[scope.row.status]?.color || 'info'"
              effect="light"
              round
            >
              {{ statusMap[scope.row.status]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submit_time" label="提交时间" width="160">
          <template #default="scope">
            {{ dayjs(scope.row.submit_time).format('YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.status === 0"
              type="success" 
              link 
              size="small"
              @click="handleAudit(scope.row, 'pass')" 
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === 0 || scope.row.status === 1"
              type="danger" 
              link 
              size="small"
              @click="handleAudit(scope.row, 'reject')" 
            >
              下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog title="岗位审核详情" v-model="detailDialogVisible" width="680px" class="detail-dialog">
      <template v-if="currentItem">
        <div class="detail-section">
          <div class="section-title">岗位基本信息</div>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="岗位名称" :span="2">{{ currentItem.job_title }}</el-descriptions-item>
            <el-descriptions-item label="企业名称" :span="2">{{ currentItem.enterprise_name }}</el-descriptions-item>
            <el-descriptions-item label="薪资">
              <span class="salary-detail">¥{{ currentItem.salary_amount }} /{{ salaryTypeMap[currentItem.salary_type] }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="结算周期">
              <el-tag type="info" effect="light" size="small">
                {{ settlementTypeMap[currentItem.settlement_type] }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="statusMap[currentItem.status]?.color || 'info'" effect="light" round>
                {{ statusMap[currentItem.status]?.label || '未知' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="工作地址" :span="2">{{ currentItem.work_address }}</el-descriptions-item>
            <el-descriptions-item label="提交时间" :span="2">
              {{ dayjs(currentItem.submit_time).format('YYYY-MM-DD HH:mm:ss') }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">AI内容审核</div>
          <div class="ai-audit-result" :class="{ 'has-risk': currentItem.has_sensitive_word }">
            <div class="ai-audit-icon">
              <component :is="currentItem.has_sensitive_word ? Warning : CircleCheck" />
            </div>
            <div class="ai-audit-info">
              <div class="ai-audit-title">
                {{ currentItem.has_sensitive_word ? '检测到敏感内容' : '内容审核通过' }}
              </div>
              <div class="ai-audit-desc">
                {{ currentItem.has_sensitive_word ? '以下敏感词需要人工审核确认' : '未检测到违规内容，可正常发布' }}
              </div>
            </div>
          </div>
          <div v-if="currentItem.sensitive_words && currentItem.sensitive_words.length" class="sensitive-word-list">
            <div class="sensitive-label">敏感词列表：</div>
            <div class="sensitive-tags-detail">
              <el-tag 
                v-for="word in currentItem.sensitive_words" 
                :key="word" 
                type="danger" 
                effect="light"
              >
                {{ word }}
              </el-tag>
            </div>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      :title="auditForm.action === 'pass' ? '审核通过' : '下架岗位'" 
      v-model="auditDialogVisible" 
      width="480px"
      class="audit-dialog"
    >
      <el-form v-model="auditForm" label-position="left" label-width="90px">
        <el-form-item v-if="auditForm.action === 'reject'" label="下架理由" required>
          <el-input 
            v-model="auditForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细输入下架理由，便于企业了解原因并整改"
          />
        </el-form-item>
        <el-form-item v-else label="确认信息">
          <span class="confirm-tip">确认该岗位审核通过并发布吗？通过后岗位将对外展示。</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button 
          :type="auditForm.action === 'pass' ? 'success' : 'danger'" 
          @click="submitAudit"
        >
          {{ auditForm.action === 'pass' ? '确认通过' : '确认下架' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.job-audit {
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
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;

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

  &.stat-warning::before { background: #FF7D00; }
  &.stat-success::before { background: #00B42A; }
  &.stat-danger::before { background: #F53F3F; }
  &.stat-primary::before { background: #165DFF; }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;

  .stat-warning & {
    background: #FFF7E8;
    color: #FF7D00;
  }

  .stat-success & {
    background: #E8FFEA;
    color: #00B42A;
  }

  .stat-danger & {
    background: #FFECE8;
    color: #F53F3F;
  }

  .stat-primary & {
    background: #E8F3FF;
    color: #165DFF;
  }
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 13px;
  color: #86909C;
  margin-bottom: 4px;
}

.stat-number {
  font-size: 26px;
  font-weight: 600;
  color: #1D2129;
  line-height: 1.2;
}

.content-card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F2F3F5;
}

.search-form {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-actions {
  display: flex;
  gap: 10px;
}

.audit-table {
  margin-bottom: 20px;

  :deep(.el-table__row) {
    &:hover > td {
      background-color: #F7F8FA !important;
    }
  }

  :deep(.el-table__cell) {
    padding: 12px 0;
  }
}

.salary {
  color: #FF7D00;
  font-weight: 600;
  font-size: 14px;
}

.sensitive-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.more-tag {
  margin-left: 2px;
  color: #86909C;
  font-size: 12px;
}

.empty-text {
  color: #C9CDD4;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

.detail-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 24px;
  }
}

.detail-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 12px;
  padding-left: 8px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 14px;
    background: #165DFF;
    border-radius: 2px;
  }
}

.salary-detail {
  color: #FF7D00;
  font-weight: 600;
  font-size: 15px;
}

.ai-audit-result {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  border-radius: 8px;
  background: #F2FFF5;
  border: 1px solid #E8FFEA;

  &.has-risk {
    background: #FFF2F0;
    border-color: #FFCCC9;
  }
}

.ai-audit-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  background: #E8FFEA;
  color: #00B42A;

  .has-risk & {
    background: #FFECE8;
    color: #F53F3F;
  }
}

.ai-audit-info {
  flex: 1;
}

.ai-audit-title {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 4px;
}

.ai-audit-desc {
  font-size: 12px;
  color: #86909C;
}

.sensitive-word-list {
  margin-top: 14px;
}

.sensitive-label {
  font-size: 13px;
  color: #4E5969;
  margin-bottom: 8px;
}

.sensitive-tags-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.audit-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 24px;
  }
}

.confirm-tip {
  color: #4E5969;
  font-size: 14px;
  line-height: 1.6;
}
</style>