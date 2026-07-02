<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Clock, Check, CircleClose, Plus, Download, Refresh } from '@element-plus/icons-vue'
import { financeApi, type Invoice } from '@/api/finance'

const tableData = ref<Invoice[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const statusFilter = ref('')
const dialogVisible = ref(false)
const dialogLoading = ref(false)

const applyForm = reactive({
  type: 'vat',
  amount: '',
  title: '',
  tax_number: '',
  address: '',
  phone: '',
  bank_name: '',
  bank_account: '',
  receive_name: '',
  receive_phone: '',
  receive_address: '',
  remark: ''
})

const typeMap: Record<string, string> = {
  vat: '增值税专用发票',
  ordinary: '普通发票'
}

const statusMap: Record<string, { label: string; class: string }> = {
  pending: { label: '待审核', class: 'warning' },
  approved: { label: '已审核', class: 'info' },
  rejected: { label: '已驳回', class: 'danger' },
  issued: { label: '已开具', class: 'success' }
}

const stats = computed(() => {
  const list = tableData.value
  const totalAmount = list.reduce((sum, item) => sum + (item.amount || 0), 0)
  const pending = list.filter(item => item.status === 'pending').length
  const issued = list.filter(item => item.status === 'issued').reduce((sum, item) => sum + (item.amount || 0), 0)
  const count = list.length
  return { totalAmount, pending, issued, count }
})

const loadData = () => {
  financeApi.getInvoiceList({ page: page.value, size: size.value, status: statusFilter.value }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).catch(() => {
    tableData.value = [
      { invoice_id: 'INV20260629001', type: 'vat', amount: 15000, status: 'pending', apply_time: '2026-06-29 10:30:00' },
      { invoice_id: 'INV20260628002', type: 'ordinary', amount: 5000, status: 'approved', apply_time: '2026-06-28 14:20:00' },
      { invoice_id: 'INV20260625003', type: 'vat', amount: 8000, status: 'issued', apply_time: '2026-06-25 09:15:00' },
      { invoice_id: 'INV20260620004', type: 'ordinary', amount: 3000, status: 'rejected', apply_time: '2026-06-20 16:45:00' },
      { invoice_id: 'INV20260615005', type: 'vat', amount: 12000, status: 'issued', apply_time: '2026-06-15 11:00:00' }
    ]
    total.value = 5
  })
}

const openApplyDialog = () => {
  dialogVisible.value = true
}

const handleApply = () => {
  if (!applyForm.amount || Number(applyForm.amount) <= 0) {
    ElMessage.warning('请输入有效的开票金额')
    return
  }
  if (!applyForm.title) {
    ElMessage.warning('请输入发票抬头')
    return
  }
  dialogLoading.value = true
  setTimeout(() => {
    dialogLoading.value = false
    dialogVisible.value = false
    ElMessage.success('发票申请提交成功')
    loadData()
  }, 1000)
}

const downloadInvoice = (row: Invoice) => {
  ElMessage.info(`正在下载发票 ${row.invoice_id}`)
}

const reapplyInvoice = (row: Invoice) => {
  applyForm.type = row.type
  applyForm.amount = String(row.amount)
  dialogVisible.value = true
}

const formatMoney = (value: number) => {
  return '¥' + value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="invoice-page">
    <div class="page-header">
      <div class="header-left">
        <h1>发票管理</h1>
        <p class="subtitle">申请和管理企业发票</p>
      </div>
      <div class="header-right">
        <el-select v-model="statusFilter" placeholder="状态筛选" class="status-select" @change="loadData">
          <el-option label="全部状态" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已审核" value="approved" />
          <el-option label="已驳回" value="rejected" />
          <el-option label="已开具" value="issued" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="openApplyDialog">申请开票</el-button>
      </div>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="card-icon icon-primary">
          <el-icon :size="24"><Document /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">开票总金额</div>
          <div class="card-value">{{ formatMoney(stats.totalAmount) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-warning">
          <el-icon :size="24"><Clock /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">待审核</div>
          <div class="card-value text-warning">{{ stats.pending }} 张</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-success">
          <el-icon :size="24"><Check /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">已开具金额</div>
          <div class="card-value text-success">{{ formatMoney(stats.issued) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-info">
          <el-icon :size="24"><CircleClose /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">发票总数</div>
          <div class="card-value">{{ stats.count }} 张</div>
        </div>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table :data="tableData" stripe style="width: 100%" class="invoice-table">
        <el-table-column prop="invoice_id" label="发票编号" width="180" />
        <el-table-column label="发票类型" width="160">
          <template #default="scope">
            <span class="type-tag">{{ typeMap[scope.row.type] }}</span>
          </template>
        </el-table-column>
        <el-table-column label="开票金额" width="140" align="right">
          <template #default="scope">
            <span class="amount-text">{{ formatMoney(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status].class" effect="light" round>
              {{ statusMap[scope.row.status].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apply_time" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" v-if="scope.row.status === 'issued'" @click="downloadInvoice(scope.row)">
              <el-icon style="margin-right: 2px"><Download /></el-icon>下载
            </el-button>
            <el-button type="primary" link size="small" v-if="scope.row.status === 'rejected'" @click="reapplyInvoice(scope.row)">
              <el-icon style="margin-right: 2px"><Refresh /></el-icon>重新申请
            </el-button>
            <el-button type="primary" link size="small">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog 
      v-model="dialogVisible" 
      title="申请开票" 
      width="720px" 
      class="apply-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="applyForm" label-width="120px" class="apply-form">
        <el-form-item label="发票类型">
          <el-radio-group v-model="applyForm.type">
            <el-radio value="vat">增值税专用发票</el-radio>
            <el-radio value="ordinary">普通发票</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开票金额" required>
          <el-input v-model="applyForm.amount" placeholder="请输入开票金额" prefix-icon="¥">
            <template #append>元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="发票抬头" required>
          <el-input v-model="applyForm.title" placeholder="请输入发票抬头" />
        </el-form-item>
        <el-form-item label="税号" v-if="applyForm.type === 'vat'">
          <el-input v-model="applyForm.tax_number" placeholder="请输入纳税人识别号" />
        </el-form-item>
        <el-form-item label="单位地址" v-if="applyForm.type === 'vat'">
          <el-input v-model="applyForm.address" placeholder="请输入注册地址" />
        </el-form-item>
        <el-form-item label="单位电话" v-if="applyForm.type === 'vat'">
          <el-input v-model="applyForm.phone" placeholder="请输入注册电话" />
        </el-form-item>
        <el-form-item label="开户银行" v-if="applyForm.type === 'vat'">
          <el-input v-model="applyForm.bank_name" placeholder="请输入开户银行" />
        </el-form-item>
        <el-form-item label="银行账号" v-if="applyForm.type === 'vat'">
          <el-input v-model="applyForm.bank_account" placeholder="请输入银行账号" />
        </el-form-item>
        <div class="form-divider">
          <span>收票信息</span>
        </div>
        <el-form-item label="收票人">
          <el-input v-model="applyForm.receive_name" placeholder="请输入收票人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="applyForm.receive_phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="收票地址">
          <el-input v-model="applyForm.receive_address" placeholder="请输入收票地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" type="textarea" :rows="3" placeholder="选填，请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.invoice-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;

  .header-left {
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #1F2329;
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: #86909C;
      margin: 0;
    }
  }

  .header-right {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .status-select {
    width: 160px;
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);
    }

    .card-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      &.icon-primary {
        background: rgba(22, 93, 255, 0.1);
        color: #165DFF;
      }

      &.icon-success {
        background: rgba(0, 180, 42, 0.1);
        color: #00B42A;
      }

      &.icon-warning {
        background: rgba(255, 125, 0, 0.1);
        color: #FF7D00;
      }

      &.icon-info {
        background: rgba(134, 144, 156, 0.1);
        color: #4E5969;
      }
    }

    .card-content {
      flex: 1;

      .card-label {
        font-size: 14px;
        color: #86909C;
        margin-bottom: 8px;
      }

      .card-value {
        font-size: 24px;
        font-weight: 600;
        color: #1F2329;

        &.text-success {
          color: #00B42A;
        }

        &.text-warning {
          color: #FF7D00;
        }
      }
    }
  }
}

.table-wrapper {
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;

  .invoice-table {
    :deep(.el-table__header th) {
      background-color: #F7F8FA;
      color: #4E5969;
      font-weight: 500;
    }
  }

  .type-tag {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(22, 93, 255, 0.1);
    color: #165DFF;
    border-radius: 4px;
    font-size: 13px;
  }

  .amount-text {
    color: #1F2329;
    font-weight: 600;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}

.apply-dialog {
  :deep(.el-dialog__body) {
    padding-top: 10px;
  }
}

.apply-form {
  .form-divider {
    margin: 20px 0 16px;
    position: relative;
    text-align: center;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      right: 0;
      top: 50%;
      height: 1px;
      background: #E5E6EB;
    }

    span {
      position: relative;
      display: inline-block;
      padding: 0 16px;
      background: #FFFFFF;
      color: #86909C;
      font-size: 14px;
    }
  }
}
</style>
