<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { riskApi, type ComplaintItem } from '@/api/admin'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElInputNumber, ElMessage, ElPagination, ElImage, ElTimeline, ElTimelineItem } from 'element-plus'
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

const statusMap: Record<number, { label: string; color: 'warning' | 'primary' | 'success' | 'danger' }> = {
  0: { label: '待处理', color: 'warning' },
  1: { label: '处理中', color: 'primary' },
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
  <div class="complaint-container">
    <div class="search-bar">
      <div class="search-form">
        <el-input 
          v-model="searchForm.complainant_name" 
          placeholder="投诉人" 
          clearable
          style="width: 150px"
        />
        <el-input 
          v-model="searchForm.defendant_name" 
          placeholder="被投诉人" 
          clearable
          style="width: 150px"
        />
        <el-select 
          v-model="searchForm.complaint_type" 
          placeholder="投诉类型" 
          clearable
          style="width: 150px"
        >
          <el-option label="虚假招聘" value="fake_recruitment" />
          <el-option label="薪资拖欠" value="salary_delay" />
          <el-option label="押金诈骗" value="deposit_scam" />
          <el-option label="未履约" value="non_fulfillment" />
          <el-option label="信息泄露" value="info_leak" />
        </el-select>
        <el-select 
          v-model="searchForm.status" 
          placeholder="状态" 
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
        <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="loadData" icon="Refresh">刷新</el-button>
      </div>
    </div>

    <el-table :data="tableData" border stripe class="complaint-table">
      <el-table-column prop="complaint_id" label="工单编号" width="160" />
      <el-table-column prop="complainant_name" label="投诉人" width="100" />
      <el-table-column prop="defendant_name" label="被投诉人" width="100" />
      <el-table-column prop="defendant_type" label="被投诉类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.defendant_type === 1 ? 'primary' : 'info'">
            {{ scope.row.defendant_type === 1 ? '企业' : '个人' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="complaint_type" label="投诉类型" width="120">
        <template #default="scope">
          {{ complaintTypeMap[scope.row.complaint_type] || scope.row.complaint_type }}
        </template>
      </el-table-column>
      <el-table-column prop="job_title" label="关联岗位" min-width="150" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status]?.color || 'info'">
            {{ statusMap[scope.row.status]?.label || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="创建时间" width="160">
        <template #default="scope">
          {{ dayjs(scope.row.create_time).format('YYYY-MM-DD HH:mm') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
          <el-button 
            v-if="scope.row.status === 0 || scope.row.status === 1"
            size="small" 
            type="warning" 
            @click="handleComplaintAction(scope.row, 'freeze')" 
          >
            冻结资金
          </el-button>
          <el-button 
            v-if="scope.row.status === 1"
            size="small" 
            type="primary" 
            @click="handleComplaintAction(scope.row, 'unfreeze')" 
          >
            解冻
          </el-button>
          <el-button 
            v-if="scope.row.status === 1"
            size="small" 
            type="danger" 
            @click="handleComplaintAction(scope.row, 'deduct')" 
          >
            划扣补偿
          </el-button>
          <el-button 
            v-if="scope.row.status === 1"
            size="small" 
            type="success" 
            @click="handleComplaintAction(scope.row, 'compensate')" 
          >
            平台赔付
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
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog title="投诉工单详情" v-model="detailDialogVisible" width="800px">
      <el-form v-if="currentItem" label-position="left" label-width="100px">
        <el-form-item label="工单编号">
          {{ currentItem.complaint_id }}
        </el-form-item>
        <el-form-item label="投诉人">
          {{ currentItem.complainant_name }}
        </el-form-item>
        <el-form-item label="被投诉人">
          {{ currentItem.defendant_name }}
        </el-form-item>
        <el-form-item label="投诉类型">
          {{ complaintTypeMap[currentItem.complaint_type] || currentItem.complaint_type }}
        </el-form-item>
        <el-form-item label="关联岗位">
          {{ currentItem.job_title || '-' }}
        </el-form-item>
        <el-form-item label="投诉内容">
          <div class="content-text">{{ currentItem.complaint_content }}</div>
        </el-form-item>
        <el-form-item label="证据">
          <div class="evidence-images" v-if="currentItem.evidence_urls && currentItem.evidence_urls.length">
            <el-image 
              v-for="(url, index) in currentItem.evidence_urls.slice(0, 6)" 
              :key="index"
              :src="url" 
              style="width: 120px; height: 120px; object-fit: cover; margin-right: 8px; margin-bottom: 8px;"
              fit="cover"
            />
          </div>
          <span v-else>无</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="statusMap[currentItem.status]?.color || 'info'">
            {{ statusMap[currentItem.status]?.label || '未知' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="处理结果" v-if="currentItem.handle_result">
          {{ currentItem.handle_result }}
        </el-form-item>
        <el-form-item label="操作记录" :label-width="80">
          <el-timeline>
            <el-timeline-item 
              v-for="(log, index) in handleLogs" 
              :key="index"
              :timestamp="log.time"
            >
              <span>{{ log.action }}</span>
              <span class="operator">（{{ log.operator }}）</span>
            </el-timeline-item>
          </el-timeline>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog 
      :title="actionMap[handleForm.action]?.label || '处理投诉'" 
      v-model="handleDialogVisible" 
      width="450px"
    >
      <el-form v-model="handleForm" label-position="left" label-width="80px">
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
            :rows="3" 
            placeholder="请输入处理结果"
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
.complaint-container {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E2E8F0;
}

.search-form {
  display: flex;
  gap: 16px;
}

.search-actions {
  display: flex;
  gap: 12px;
}

.complaint-table {
  margin-bottom: 20px;
}

.content-text {
  white-space: pre-wrap;
  word-break: break-all;
}

.evidence-images {
  display: flex;
  flex-wrap: wrap;
}

.operator {
  color: #94A3B8;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>