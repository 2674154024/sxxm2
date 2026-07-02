<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { auditApi, type EnterpriseAuditItem } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Search, Refresh, RefreshLeft, Clock, Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const tableData = ref<EnterpriseAuditItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const searchForm = ref({
  enterprise_name: '',
  credit_code: '',
  verify_status: '',
})

const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const currentItem = ref<EnterpriseAuditItem | null>(null)
const auditForm = ref({
  action: '',
  reason: ''
})

const statusMap: Record<number, { label: string; color: 'warning' | 'info' | 'success' | 'danger' }> = {
  0: { label: '待审核', color: 'warning' },
  1: { label: '审核中', color: 'info' },
  2: { label: '已通过', color: 'success' },
  3: { label: '已驳回', color: 'danger' },
}

const pendingCount = computed(() => tableData.value.filter(item => item.verify_status === 0).length)
const auditingCount = computed(() => tableData.value.filter(item => item.verify_status === 1).length)
const passedCount = computed(() => tableData.value.filter(item => item.verify_status === 2).length)
const rejectedCount = computed(() => tableData.value.filter(item => item.verify_status === 3).length)

const loadData = async () => {
  try {
    const response = await auditApi.getEnterpriseAuditList({
      page: page.value,
      size: size.value,
      status: searchForm.value.verify_status ? parseInt(searchForm.value.verify_status) : undefined
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
    enterprise_name: '',
    credit_code: '',
    verify_status: '',
  }
  page.value = 1
  loadData()
}

const handleDetail = (row: EnterpriseAuditItem) => {
  currentItem.value = row
  detailDialogVisible.value = true
}

const handleAudit = (row: EnterpriseAuditItem, action: 'pass' | 'reject') => {
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
    await auditApi.auditEnterprise({
      enterpriseId: currentItem.value.enterprise_id,
      status: auditForm.value.action === 'pass' ? 2 : 3,
      remark: auditForm.value.reason
    })
    
    ElMessage.success(auditForm.value.action === 'pass' ? '审核通过' : '驳回成功')
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
  <div class="enterprise-audit">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">企业审核</h2>
        <p class="page-desc">管理企业资质审核，确保平台企业信息真实有效</p>
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
        <div class="stat-card stat-primary">
          <div class="stat-icon">
            <Loading />
          </div>
          <div class="stat-content">
            <div class="stat-value">审核中</div>
            <div class="stat-number">{{ auditingCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-icon">
            <CircleCheck />
          </div>
          <div class="stat-content">
            <div class="stat-value">已通过</div>
            <div class="stat-number">{{ passedCount }}</div>
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
            v-model="searchForm.enterprise_name" 
            placeholder="请输入企业名称" 
            clearable
            style="width: 220px"
            :prefix-icon="Search"
          />
          <el-input 
            v-model="searchForm.credit_code" 
            placeholder="请输入统一社会信用代码" 
            clearable
            style="width: 240px"
          />
          <el-select 
            v-model="searchForm.verify_status" 
            placeholder="审核状态" 
            clearable
            style="width: 140px"
          >
            <el-option label="待审核" value="0" />
            <el-option label="审核中" value="1" />
            <el-option label="已通过" value="2" />
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
        class="audit-table"
        stripe
        :header-cell-style="{ background: '#F7F8FA', color: '#4E5969', fontWeight: 500 }"
      >
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="enterprise_name" label="企业名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="credit_code" label="统一社会信用代码" min-width="200" show-overflow-tooltip />
        <el-table-column prop="legal_person" label="法定代表人" width="110" />
        <el-table-column prop="contact_name" label="联系人" width="100" />
        <el-table-column prop="contact_phone" label="联系电话" width="130" />
        <el-table-column prop="verify_status" label="审核状态" width="110" align="center">
          <template #default="scope">
            <el-tag 
              :type="statusMap[scope.row.verify_status]?.color || 'info'"
              effect="light"
              round
            >
              {{ statusMap[scope.row.verify_status]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submit_time" label="提交时间" width="170">
          <template #default="scope">
            {{ dayjs(scope.row.submit_time).format('YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.verify_status === 0 || scope.row.verify_status === 1"
              type="success" 
              link 
              size="small"
              @click="handleAudit(scope.row, 'pass')" 
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.verify_status === 0 || scope.row.verify_status === 1"
              type="danger" 
              link 
              size="small"
              @click="handleAudit(scope.row, 'reject')" 
            >
              驳回
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

    <el-dialog title="企业审核详情" v-model="detailDialogVisible" width="640px" class="detail-dialog">
      <template v-if="currentItem">
        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="企业名称" :span="2">{{ currentItem.enterprise_name }}</el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码" :span="2">{{ currentItem.credit_code }}</el-descriptions-item>
            <el-descriptions-item label="法定代表人">{{ currentItem.legal_person }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ currentItem.contact_name }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentItem.contact_phone }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="statusMap[currentItem.verify_status]?.color || 'info'" effect="light" round>
                {{ statusMap[currentItem.verify_status]?.label || '未知' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间" :span="2">
              {{ dayjs(currentItem.submit_time).format('YYYY-MM-DD HH:mm:ss') }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">营业执照</div>
          <div class="license-wrapper" v-if="currentItem.business_license">
            <el-image 
              :src="currentItem.business_license" 
              :preview-src-list="[currentItem.business_license]"
              fit="contain"
              class="license-image"
            />
          </div>
          <div v-else class="empty-license">
            <el-empty description="未上传营业执照" :image-size="80" />
          </div>
        </div>

        <div class="detail-section" v-if="currentItem.reject_reason">
          <div class="section-title">驳回理由</div>
          <div class="reject-reason">
            {{ currentItem.reject_reason }}
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      :title="auditForm.action === 'pass' ? '审核通过' : '驳回申请'" 
      v-model="auditDialogVisible" 
      width="480px"
      class="audit-dialog"
    >
      <el-form v-model="auditForm" label-position="left" label-width="90px">
        <el-form-item v-if="auditForm.action === 'reject'" label="驳回理由" required>
          <el-input 
            v-model="auditForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细输入驳回理由，便于企业了解原因并整改"
          />
        </el-form-item>
        <el-form-item v-else label="确认信息">
          <span class="confirm-tip">确认该企业资质审核通过吗？通过后企业将可以发布岗位。</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button 
          :type="auditForm.action === 'pass' ? 'success' : 'danger'" 
          @click="submitAudit"
        >
          {{ auditForm.action === 'pass' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.enterprise-audit {
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

.license-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px;
  background: #F7F8FA;
  border-radius: 8px;
}

.license-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
}

.empty-license {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #F7F8FA;
  border-radius: 8px;
}

.reject-reason {
  padding: 14px 16px;
  background: #FFF2F0;
  border: 1px solid #FFCCC9;
  border-radius: 8px;
  color: #F53F3F;
  font-size: 13px;
  line-height: 1.6;
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