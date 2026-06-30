<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { auditApi, type JobAuditItem } from '@/api/admin'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElMessage, ElPagination } from 'element-plus'
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

const loadData = async () => {
  try {
    const response = await auditApi.getJobAuditList({
      page: page.value,
      size: size.value,
      status: searchForm.value.status ? parseInt(searchForm.value.status) : undefined,
      keyword: searchForm.value.keyword || undefined
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
  <div class="job-audit-container">
    <div class="search-bar">
      <div class="search-form">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="搜索岗位名称" 
          clearable
          style="width: 250px"
        />
        <el-select 
          v-model="searchForm.industry_tag" 
          placeholder="行业标签" 
          clearable
          style="width: 150px"
        >
          <el-option label="全部" value="" />
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
          style="width: 150px"
        >
          <el-option label="待审核" value="0" />
          <el-option label="已发布" value="1" />
          <el-option label="已下架" value="2" />
        </el-select>
      </div>
      <div class="search-actions">
        <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="loadData" icon="Refresh">刷新</el-button>
      </div>
    </div>

    <el-table :data="tableData" border stripe class="audit-table">
      <el-table-column prop="job_title" label="岗位名称" min-width="180" />
      <el-table-column prop="enterprise_name" label="企业名称" min-width="150" />
      <el-table-column prop="salary_amount" label="时薪(元)" width="100">
        <template #default="scope">
          <span class="salary">{{ scope.row.salary_amount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="work_address" label="工作地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="has_sensitive_word" label="AI审核" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.has_sensitive_word" type="danger">
            含敏感词
          </el-tag>
          <el-tag v-else type="success">
            正常
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sensitive_words" label="敏感词" width="150">
        <template #default="scope">
          <span v-if="scope.row.sensitive_words && scope.row.sensitive_words.length">
            <el-tag 
              v-for="word in scope.row.sensitive_words.slice(0, 3)" 
              :key="word" 
              type="danger" 
              size="small"
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
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status]?.color || 'info'">
            {{ statusMap[scope.row.status]?.label || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submit_time" label="提交时间" width="160">
        <template #default="scope">
          {{ dayjs(scope.row.submit_time).format('YYYY-MM-DD HH:mm') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
          <el-button 
            v-if="scope.row.status === 0"
            size="small" 
            type="success" 
            @click="handleAudit(scope.row, 'pass')" 
          >
            通过
          </el-button>
          <el-button 
            v-if="scope.row.status === 0 || scope.row.status === 1"
            size="small" 
            type="danger" 
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
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog title="岗位审核详情" v-model="detailDialogVisible" width="650px">
      <el-form v-if="currentItem" label-position="left" label-width="100px">
        <el-form-item label="岗位名称">
          {{ currentItem.job_title }}
        </el-form-item>
        <el-form-item label="企业名称">
          {{ currentItem.enterprise_name }}
        </el-form-item>
        <el-form-item label="时薪">
          <span class="salary">{{ currentItem.salary_amount }} 元/小时</span>
        </el-form-item>
        <el-form-item label="工作地址">
          {{ currentItem.work_address }}
        </el-form-item>
        <el-form-item label="AI审核结果">
          <el-tag v-if="currentItem.has_sensitive_word" type="danger" icon="AlertTriangle">
            检测到敏感词
          </el-tag>
          <el-tag v-else type="success">审核通过</el-tag>
        </el-form-item>
        <el-form-item label="敏感词列表" v-if="currentItem.sensitive_words && currentItem.sensitive_words.length">
          <div class="sensitive-tags">
            <el-tag 
              v-for="word in currentItem.sensitive_words" 
              :key="word" 
              type="danger" 
              size="small"
            >
              {{ word }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="statusMap[currentItem.status]?.color || 'info'">
            {{ statusMap[currentItem.status]?.label || '未知' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="提交时间">
          {{ dayjs(currentItem.submit_time).format('YYYY-MM-DD HH:mm:ss') }}
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog 
      :title="auditForm.action === 'pass' ? '审核通过' : '下架岗位'" 
      v-model="auditDialogVisible" 
      width="450px"
    >
      <el-form v-model="auditForm" label-position="left" label-width="80px">
        <el-form-item v-if="auditForm.action === 'reject'" label="下架理由" required>
          <el-input 
            v-model="auditForm.reason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入下架理由"
          />
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
.job-audit-container {
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

.audit-table {
  margin-bottom: 20px;
}

.salary {
  color: #FF7D00;
  font-weight: bold;
}

.more-tag {
  margin-left: 4px;
  color: #94A3B8;
  font-size: 12px;
}

.empty-text {
  color: #94A3B8;
}

.sensitive-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>