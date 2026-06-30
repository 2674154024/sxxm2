<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { salaryApi, type PaymentRecord } from '@/api/salary'

const tableData = ref<PaymentRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const statusMap: Record<string, { label: string; class: string }> = {
  processing: { label: '处理中', class: 'primary' },
  success: { label: '已到账', class: 'success' },
  failed: { label: '失败', class: 'danger' }
}

const loadData = () => {
  salaryApi.getPaymentRecords({ page: page.value, size: page.value }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).catch(() => {
    tableData.value = [
      { record_id: '1', student_name: '张三', job_name: '初中数学家教', amount: 1500, pay_time: '2026-06-29 10:30', status: 'success' },
      { record_id: '2', student_name: '李四', job_name: '超市促销', amount: 360, pay_time: '2026-06-28 14:20', status: 'success' },
      { record_id: '3', student_name: '王五', job_name: '展会协助', amount: 400, pay_time: '2026-06-27 09:15', status: 'processing' },
      { record_id: '4', student_name: '赵六', job_name: '英语助教', amount: 400, pay_time: '2026-06-26 16:40', status: 'success' },
      { record_id: '5', student_name: '孙七', job_name: '奶茶店店员', amount: 216, pay_time: '2026-06-25 11:25', status: 'failed' }
    ]
    total.value = 5
  })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="payment-records">
    <div class="page-header">
      <h1>发放记录</h1>
    </div>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="student_name" label="学生姓名" width="120" />
      <el-table-column prop="job_name" label="岗位名称" width="150" />
      <el-table-column label="金额" width="120">
        <template #default="scope">¥{{ scope.row.amount.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="pay_time" label="发放时间" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status].class">{{ statusMap[scope.row.status].label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button size="small" v-if="scope.row.status === 'failed'">重试发放</el-button>
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
.payment-records {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;

  h1 {
    font-size: 20px;
    font-weight: bold;
    color: #1F2329;
  }
}
</style>