<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { riskApi, type ComplaintItem } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Search, Refresh, RefreshLeft, Clock, Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const tableData = ref<ComplaintItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const searchForm = ref({
  complainant_name: '',
  defendant_name: '',
  complaint_type: '',
  status: '',
})

const detailDialogVisible = ref(false)
const handleDialogVisible = ref(false)
const currentItem = ref<ComplaintItem | null>(null)
const handleForm = ref({
  action: '',
  amount: 0,
  handle_result: ''
})

const statusMap: Record<number, { label: string; color: 'warning' | 'info' | 'success' | 'danger' }> = {
  0: { label: '待处理', color: 'warning' },
  1: { label: '处理中', color: 'info' },
  2: { label: '已结案', color: 'success' },
  3: { label: '已驳回', color: 'danger' },
}

const complaintTypeMap: Record<string, string> = {
  'fake_recruitment': '虚假招聘',
  'salary_delay': '薪资拖欠',
  'deposit_scam': '押金诈骗',
  'non_fulfillment': '未履约',
  'info_leak': '信息泄露'
}

const actionMap: Record<string, { label: string; color: 'warning' | 'primary' | 'danger' | 'success' }> = {
  freeze: { label: '冻结资金', color: 'warning' },
  unfreeze: { label: '解冻资金', color: 'primary' },
  deduct: { label: '划扣补偿', color: 'danger' },
  compensate: { label: '平台赔付', color: 'success' }
}

const handleLogs = ref([
  { time: '2026-06-29 10:00', action: '用户提交投诉', operator: '系统' },
  { time: '2026-06-29 10:30', action: '系统自动冻结企业账户', operator: '系统' },
])

const pendingCount = computed(() => tableData.value.filter(item => item.status === 0).length)
const processingCount = computed(() => tableData.value.filter(item => item.status === 1).length)
const closedCount = computed(() => tableData.value.filter(item => item.status === 2).length)
const rejectedCount = computed(() => tableData.value.filter(item => item.status === 3).length)

const loadData = async () => {
  try {
    const response = await riskApi.getComplaintList({
      page: page.value,
      size: size.value,
      status: searchForm.value.status ? parseInt(searchForm.value.status) : undefined
    })
    
    if (response.code === 200) {
      tableData.value = response.data.list || []
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
    complainant_name: '',
    defendant_name: '',
    complaint_type: '',
    status: '',
  }
  page.value = 1
  loadData()
}

const handleDetail = (row: ComplaintItem) => {
  currentItem.value = row
  detailDialogVisible.value = true
}

const handleComplaintAction = (row: ComplaintItem, action: string) => {
  currentItem.value = row
  handleForm.value = {
    action,
    amount: 0,
    handle_result: ''
  }
  handleDialogVisible.value = true
}

const submitHandle = async () => {
  if (!currentItem.value) return
  
  if (!handleForm.value.handle_result.trim()) {
    ElMessage.warning('请填写处理结果')
    return
  }

  if ((handleForm.value.action === 'freeze' || handleForm.value.action === 'deduct' || handleForm.value.action === 'compensate') && handleForm.value.amount <= 0) {
    ElMessage.warning('请输入金额')
    return
  }

  try {
    await riskApi.handleComplaint({
      complaint_id: currentItem.value.complaint_id,
      action: handleForm.value.action as 'freeze' | 'unfreeze' | 'deduct' | 'compensate',
      amount: handleForm.value.amount,
      handle_result: handleForm.value.handle_result
    })
    
    ElMessage.success('处理成功')
    handleDialogVisible.value = false
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
  <div class="complaint-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">投诉工单</h2>
        <p class="page-desc">处理用户投诉，保障平台用户权益</p>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-warning">
          <div class="stat-icon">
            <Clock />
          </div>
          <div class="stat-content">
            <div class="stat-value">待处理</div>
            <div class="stat-number">{{ pendingCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-primary">
          <div class="stat-icon">
            <Loading />
          </div>
          <div class="stat-content">
            <div class="stat-value">处理中</div>
            <div class="stat-number">{{ processingCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-icon">
            <CircleCheck />
          </div>
          <div class="stat-content">
            <div class="stat-value">已结案</div>
            <div class="stat-number">{{ closedCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-danger">
          <div class="stat-icon">
            <CircleClose />
          </div>
          <div class="stat-content">
            <div class="stat-value">已驳回</div>
            <div class="stat-number">{{ rejectedCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="content-card">
      <div class="search-bar">
        <div class="search-form">
          <el-input 
            v-model="searchForm.complainant_name" 
            placeholder="投诉人姓名" 
            clearable
            style="width: 140px"
            :prefix-icon="Search"
          />
          <el-input 
            v-model="searchForm.defendant_name" 
            placeholder="被投诉方" 
            clearable
            style="width: 140px"
          />
          <el-select 
            v-model="searchForm.complaint_type" 
            placeholder="投诉类型" 
            clearable
            style="width: 130px"
          >
            <el-option label="虚假招聘" value="fake_recruitment" />
            <el-option label="薪资拖欠" value="salary_delay" />
            <el-option label="押金诈骗" value="deposit_scam" />
            <el-option label="未履约" value="non_fulfillment" />
            <el-option label="信息泄露" value="info_leak" />
          </el-select>
          <el-select 
            v-model="searchForm.status" 
            placeholder="处理状态" 
            clearable
            style="width: 120px"
          >
            <el-option label="待处理" value="0" />
            <el-option label="处理中" value="1" />
            <el-option label="已结案" value="2" />
            <el-option label="已驳回" value="3" />
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
        class="complaint-table"
        stripe
        :header-cell-style="{ background: '#F7F8FA', color: '#4E5969', fontWeight: 500 }"
      >
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="complaint_id" label="工单编号" width="160" />
        <el-table-column prop="complainant_name" label="投诉人" width="100" />
        <el-table-column prop="defendant_name" label="被投诉方" width="110" />
        <el-table-column prop="defendant_type" label="类型" width="80" align="center">
          <template #default="scope">
            <el-tag 
              :type="scope.row.defendant_type === 1 ? 'info' : 'info'" 
              size="small"
              effect="light"
            >
              {{ scope.row.defendant_type === 1 ? '企业' : '个人' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="complaint_type" label="投诉类型" width="110">
          <template #default="scope">
            <span class="complaint-type">
              {{ complaintTypeMap[scope.row.complaint_type] || scope.row.complaint_type }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="job_title" label="关联岗位" min-width="140" show-overflow-tooltip />
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
        <el-table-column prop="create_time" label="创建时间" width="160">
          <template #default="scope">
            {{ dayjs(scope.row.create_time).format('YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.status === 0 || scope.row.status === 1"
              type="warning" 
              link 
              size="small"
              @click="handleComplaintAction(scope.row, 'freeze')" 
            >
              冻结
            </el-button>
            <el-button 
              v-if="scope.row.status === 1"
              type="primary" 
              link 
              size="small"
              @click="handleComplaintAction(scope.row, 'unfreeze')" 
            >
              解冻
            </el-button>
            <el-button 
              v-if="scope.row.status === 1"
              type="danger" 
              link 
              size="small"
              @click="handleComplaintAction(scope.row, 'deduct')" 
            >
              划扣
            </el-button>
            <el-button 
              v-if="scope.row.status === 1"
              type="success" 
              link 
              size="small"
              @click="handleComplaintAction(scope.row, 'compensate')" 
            >
              赔付
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

    <el-dialog title="投诉工单详情" v-model="detailDialogVisible" width="760px" class="detail-dialog">
      <template v-if="currentItem">
        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="工单编号" :span="2">{{ currentItem.complaint_id }}</el-descriptions-item>
            <el-descriptions-item label="投诉人">{{ currentItem.complainant_name }}</el-descriptions-item>
            <el-descriptions-item label="被投诉方">{{ currentItem.defendant_name }}</el-descriptions-item>
            <el-descriptions-item label="被投诉类型">
              <el-tag :type="currentItem.defendant_type === 1 ? 'info' : 'info'" size="small" effect="light">
                {{ currentItem.defendant_type === 1 ? '企业' : '个人' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="投诉类型">
              {{ complaintTypeMap[currentItem.complaint_type] || currentItem.complaint_type }}
            </el-descriptions-item>
            <el-descriptions-item label="关联岗位" :span="2">{{ currentItem.job_title || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="statusMap[currentItem.status]?.color || 'info'" effect="light" round>
                {{ statusMap[currentItem.status]?.label || '未知' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ dayjs(currentItem.create_time).format('YYYY-MM-DD HH:mm:ss') }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">投诉内容</div>
          <div class="complaint-content-box">
            {{ currentItem.complaint_content }}
          </div>
        </div>

        <div class="detail-section" v-if="currentItem.evidence_urls && currentItem.evidence_urls.length">
          <div class="section-title">证据材料</div>
          <div class="evidence-images">
            <el-image 
              v-for="(url, index) in currentItem.evidence_urls.slice(0, 6)" 
              :key="index"
              :src="url" 
              :preview-src-list="currentItem.evidence_urls"
              fit="cover"
              class="evidence-image"
            />
          </div>
        </div>

        <div class="detail-section" v-if="currentItem.handle_result">
          <div class="section-title">处理结果</div>
          <div class="handle-result-box">
            {{ currentItem.handle_result }}
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">操作记录</div>
          <el-timeline class="timeline-custom">
            <el-timeline-item 
              v-for="(log, index) in handleLogs" 
              :key="index"
              :timestamp="log.time"
              placement="top"
            >
              <span class="log-action">{{ log.action }}</span>
              <span class="log-operator">（{{ log.operator }}）</span>
            </el-timeline-item>
          </el-timeline>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      :title="actionMap[handleForm.action]?.label || '处理投诉'" 
      v-model="handleDialogVisible" 
      width="480px"
      class="handle-dialog"
    >
      <el-form v-model="handleForm" label-position="left" label-width="90px">
        <el-form-item v-if="handleForm.action === 'freeze' || handleForm.action === 'deduct' || handleForm.action === 'compensate'" label="金额" required>
          <el-input-number 
            v-model="handleForm.amount" 
            :min="0.01" 
            :step="0.01" 
            :precision="2"
            style="width: 100%"
            placeholder="请输入金额"
          />
        </el-form-item>
        <el-form-item label="处理结果" required>
          <el-input 
            v-model="handleForm.handle_result" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细输入处理结果"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button 
          :type="actionMap[handleForm.action]?.color === 'danger' ? 'danger' : 'primary'" 
          @click="submitHandle"
        >
          确认处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.complaint-list {
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
  &.stat-primary::before { background: #165DFF; }
  &.stat-success::before { background: #00B42A; }
  &.stat-danger::before { background: #F53F3F; }
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

  .stat-primary & {
    background: #E8F3FF;
    color: #165DFF;
  }

  .stat-success & {
    background: #E8FFEA;
    color: #00B42A;
  }

  .stat-danger & {
    background: #FFECE8;
    color: #F53F3F;
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

.complaint-table {
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

.complaint-type {
  color: #4E5969;
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

.complaint-content-box {
  padding: 16px;
  background: #F7F8FA;
  border-radius: 8px;
  color: #4E5969;
  font-size: 13px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

.evidence-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.evidence-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.05);
  }
}

.handle-result-box {
  padding: 16px;
  background: #E8FFEA;
  border: 1px solid #BAF3C7;
  border-radius: 8px;
  color: #00B42A;
  font-size: 13px;
  line-height: 1.8;
}

.timeline-custom {
  margin: 0;
  padding: 10px 0 0 0;
}

.log-action {
  color: #4E5969;
  font-size: 13px;
}

.log-operator {
  color: #86909C;
  font-size: 12px;
}

.handle-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 24px;
  }
}
</style>