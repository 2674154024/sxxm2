<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { auditApi, type EnterpriseAuditItem } from '@/api/admin'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElMessage, ElPagination } from 'element-plus'
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

const statusMap: Record<number, { label: string; color: 'warning' | 'primary' | 'success' | 'danger' }> = {
  0: { label: '待审核', color: 'warning' },
  1: { label: '审核中', color: 'primary' },
  2: { label: '已通过', color: 'success' },
  3: { label: '已驳回', color: 'danger' },
}

const loadData = async () => {
  try {
    const response = await auditApi.getEnterpriseAuditList({
      page: page.value,
      size: size.value,
      status: searchForm.value.verify_status ? parseInt(searchForm.value.verify_status) : undefined
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
      enterprise_id: currentItem.value.enterprise_id,
      action: auditForm.value.action as 'pass' | 'reject',
      reason: auditForm.value.reason
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
  <div class="audit-container">
    <div class="search-bar">
      <div class="search-form">
        <el-input 
          v-model="searchForm.enterprise_name" 
          placeholder="企业名称" 
          clearable
          style="width: 200px"
        />
        <el-input 
          v-model="searchForm.credit_code" 
          placeholder="信用代码" 
          clearable
          style="width: 200px"
        />
        <el-select 
          v-model="searchForm.verify_status" 
          placeholder="审核状态" 
          clearable
          style="width: 150px"
        >
          <el-option label="待审核" value="0" />
          <el-option label="审核中" value="1" />
          <el-option label="已通过" value="2" />
          <el-option label="已驳回" value="3" />
        </el-select>
      </div>
      <div class="search-actions">
        <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="loadData" icon="Refresh">刷新</el-button>
      </div>
    </div>

    <el-table :data="tableData" border stripe class="audit-table">
      <el-table-column prop="enterprise_name" label="企业名称" min-width="150" />
      <el-table-column prop="credit_code" label="信用代码" min-width="180" />
      <el-table-column prop="legal_person" label="法人" width="100" />
      <el-table-column prop="contact_name" label="联系人" width="100" />
      <el-table-column prop="contact_phone" label="联系电话" width="120">
        <template #default="scope">
          <span>{{ scope.row.contact_phone }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="verify_status" label="审核状态" width="100">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.verify_status]?.color || 'info'">
            {{ statusMap[scope.row.verify_status]?.label || '未知' }}
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
            v-if="scope.row.verify_status === 0 || scope.row.verify_status === 1"
            size="small" 
            type="success" 
            @click="handleAudit(scope.row, 'pass')" 
          >
            通过
          </el-button>
          <el-button 
            v-if="scope.row.verify_status === 0 || scope.row.verify_status === 1"
            size="small" 
            type="danger" 
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
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog title="企业审核详情" v-model="detailDialogVisible" width="600px">
      <el-form v-if="currentItem" label-position="left" label-width="100px">
        <el-form-item label="企业名称">
          {{ currentItem.enterprise_name }}
        </el-form-item>
        <el-form-item label="信用代码">
          {{ currentItem.credit_code }}
        </el-form-item>
        <el-form-item label="法人">
          {{ currentItem.legal_person }}
        </el-form-item>
        <el-form-item label="联系人">
          {{ currentItem.contact_name }}
        </el-form-item>
        <el-form-item label="联系电话">
          {{ currentItem.contact_phone }}
        </el-form-item>
        <el-form-item label="营业执照">
          <el-image 
            v-if="currentItem.business_license" 
            :src="currentItem.business_license" 
            style="width: 200px; height: 150px; object-fit: contain;"
            fit="contain"
          />
          <span v-else>未上传</span>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-tag :type="statusMap[currentItem.verify_status]?.color || 'info'">
            {{ statusMap[currentItem.verify_status]?.label || '未知' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="驳回理由" v-if="currentItem.reject_reason">
          {{ currentItem.reject_reason }}
        </el-form-item>
        <el-form-item label="提交时间">
          {{ dayjs(currentItem.submit_time).format('YYYY-MM-DD HH:mm:ss') }}
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog 
      :title="auditForm.action === 'pass' ? '审核通过' : '驳回申请'" 
      v-model="auditDialogVisible" 
      width="450px"
    >
      <el-form v-model="auditForm" label-position="left" label-width="80px">
        <el-form-item v-if="auditForm.action === 'reject'" label="驳回理由" required>
          <el-input 
            v-model="auditForm.reason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入驳回理由"
          />
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
.audit-container {
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>