<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { financeApi, type Invoice } from '@/api/finance'

const tableData = ref<Invoice[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const statusFilter = ref('')

const typeMap: Record<string, string> = {
  vat: '增值税专用发票',
  ordinary: '普通发票'
}

const statusMap: Record<string, { label: string; class: string }> = {
  pending: { label: '待审核', class: 'warning' },
  approved: { label: '已审核', class: 'primary' },
  rejected: { label: '已驳回', class: 'danger' },
  issued: { label: '已开具', class: 'success' }
}

const loadData = () => {
  financeApi.getInvoiceList({ page: page.value, size: size.value, status: statusFilter.value }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).catch(() => {
    tableData.value = [
      { invoice_id: '1', type: 'vat', amount: 15000, status: 'pending', apply_time: '2026-06-29' },
      { invoice_id: '2', type: 'ordinary', amount: 5000, status: 'approved', apply_time: '2026-06-28' },
      { invoice_id: '3', type: 'vat', amount: 8000, status: 'issued', apply_time: '2026-06-25' },
      { invoice_id: '4', type: 'ordinary', amount: 3000, status: 'rejected', apply_time: '2026-06-20' },
      { invoice_id: '5', type: 'vat', amount: 12000, status: 'issued', apply_time: '2026-06-15' }
    ]
    total.value = 5
  })
}

const applyInvoice = () => {
  ElMessage.info('申请发票功能开发中')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="invoice">
    <div class="page-header">
      <h1>发票管理</h1>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 150px; margin-right: 12px;" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已审核" value="approved" />
          <el-option label="已驳回" value="rejected" />
          <el-option label="已开具" value="issued" />
        </el-select>
        <el-button type="primary" @click="applyInvoice">申请开票</el-button>
      </div>
    </div>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column label="发票类型" width="150">
        <template #default="scope">{{ typeMap[scope.row.type] }}</template>
      </el-table-column>
      <el-table-column label="金额" width="120">
        <template #default="scope">¥{{ scope.row.amount.toLocaleString() }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status].class">{{ statusMap[scope.row.status].label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="apply_time" label="申请时间" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button size="small" v-if="scope.row.status === 'issued'">下载发票</el-button>
          <el-button size="small" v-if="scope.row.status === 'rejected'">重新申请</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="(val: number) => { page = val; loadData() }"
      style="margin-top: 20px; text-align: right;"
    />
  </div>
</template>

<style scoped>
.invoice {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h1 {
    font-size: 20px;
    font-weight: bold;
    color: #1F2329;
  }
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>