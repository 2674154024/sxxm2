<script setup lang="ts">
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { financeApi, type Statement } from '@/api/finance'

const tableData = ref<Statement[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const monthFilter = ref(dayjs().format('YYYY-MM'))

const statusMap: Record<string, { label: string; class: string }> = {
  pending: { label: '待确认', class: 'warning' },
  confirmed: { label: '已确认', class: 'primary' },
  paid: { label: '已结算', class: 'success' }
}

const loadData = () => {
  financeApi.getStatementList({ page: page.value, size: size.value, month: monthFilter.value }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).catch(() => {
    tableData.value = [
      { statement_id: '1', month: '2026-06', total_income: 15000, total_payment: 12000, balance: 3000, status: 'pending', created_time: '2026-07-01' },
      { statement_id: '2', month: '2026-05', total_income: 18000, total_payment: 16500, balance: 1500, status: 'confirmed', created_time: '2026-06-01' },
      { statement_id: '3', month: '2026-04', total_income: 12000, total_payment: 11000, balance: 1000, status: 'paid', created_time: '2026-05-01' },
      { statement_id: '4', month: '2026-03', total_income: 16000, total_payment: 14800, balance: 1200, status: 'paid', created_time: '2026-04-01' },
      { statement_id: '5', month: '2026-02', total_income: 10000, total_payment: 9500, balance: 500, status: 'paid', created_time: '2026-03-01' }
    ]
    total.value = 5
  })
}

const confirmStatement = (statementId: string) => {
  financeApi.confirmStatement(statementId).then(() => {
    ElMessage.success('确认成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="statement">
    <div class="page-header">
      <h1>对账单</h1>
      <el-date-picker
        v-model="monthFilter"
        type="month"
        value-format="YYYY-MM"
        placeholder="选择月份"
        @change="loadData"
      />
    </div>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="month" label="月份" width="120" />
      <el-table-column label="收入" width="120">
        <template #default="scope">¥{{ scope.row.total_income.toLocaleString() }}</template>
      </el-table-column>
      <el-table-column label="支出" width="120">
        <template #default="scope">¥{{ scope.row.total_payment.toLocaleString() }}</template>
      </el-table-column>
      <el-table-column label="余额" width="120">
        <template #default="scope">¥{{ scope.row.balance.toLocaleString() }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status].class">{{ statusMap[scope.row.status].label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="created_time" label="生成时间" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button size="small" @click="confirmStatement(scope.row.statement_id)" v-if="scope.row.status === 'pending'">确认</el-button>
          <el-button size="small">详情</el-button>
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
.statement {
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
</style>