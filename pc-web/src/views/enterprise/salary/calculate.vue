<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { salaryApi, type SalaryRecord } from '@/api/salary'

const currentMonth = ref(dayjs().format('YYYY-MM'))
const tableData = ref<SalaryRecord[]>([])
const editingId = ref<string | null>(null)
const editingHours = ref(0)

const totalGross = computed(() => tableData.value.reduce((sum, item) => sum + item.gross_amount, 0))
const totalTax = computed(() => tableData.value.reduce((sum, item) => sum + item.tax_amount, 0))
const totalNet = computed(() => tableData.value.reduce((sum, item) => sum + item.net_amount, 0))

const statusMap: Record<string, { label: string; class: string }> = {
  pending: { label: '待核算', class: 'warning' },
  calculated: { label: '已核算', class: 'primary' },
  confirmed: { label: '已确认', class: 'info' },
  paid: { label: '已发放', class: 'success' },
  rejected: { label: '已驳回', class: 'danger' }
}

const loadData = () => {
  salaryApi.getSalaryList({ month: currentMonth.value }).then(res => {
    tableData.value = res.data.list
  }).catch(() => {
    tableData.value = [
      { record_id: '1', student_name: '张三', job_name: '初中数学家教', work_hours: 30, hourly_wage: 50, gross_amount: 1500, tax_amount: 0, net_amount: 1500, status: 'calculated', month: currentMonth.value },
      { record_id: '2', student_name: '李四', job_name: '超市促销', work_hours: 24, hourly_wage: 15, gross_amount: 360, tax_amount: 0, net_amount: 360, status: 'calculated', month: currentMonth.value },
      { record_id: '3', student_name: '王五', job_name: '展会协助', work_hours: 16, hourly_wage: 25, gross_amount: 400, tax_amount: 0, net_amount: 400, status: 'pending', month: currentMonth.value },
      { record_id: '4', student_name: '赵六', job_name: '英语助教', work_hours: 20, hourly_wage: 20, gross_amount: 400, tax_amount: 0, net_amount: 400, status: 'calculated', month: currentMonth.value },
      { record_id: '5', student_name: '孙七', job_name: '奶茶店店员', work_hours: 12, hourly_wage: 18, gross_amount: 216, tax_amount: 0, net_amount: 216, status: 'calculated', month: currentMonth.value }
    ]
  })
}

const startEdit = (record: SalaryRecord) => {
  editingId.value = record.record_id
  editingHours.value = record.work_hours
}

const saveEdit = (record: SalaryRecord) => {
  salaryApi.updateWorkHours({ record_id: record.record_id, work_hours: editingHours.value }).then(() => {
    const item = tableData.value.find(i => i.record_id === record.record_id)
    if (item) {
      item.work_hours = editingHours.value
      item.gross_amount = editingHours.value * item.hourly_wage
      item.net_amount = item.gross_amount - item.tax_amount
    }
    editingId.value = null
    ElMessage.success('修改成功')
  })
}

const cancelEdit = () => {
  editingId.value = null
}

const calculateSalary = () => {
  salaryApi.calculateSalary({ month: currentMonth.value }).then(() => {
    ElMessage.success('核算完成')
    loadData()
  })
}

const confirmSalary = () => {
  const recordIds = tableData.value.filter(r => r.status === 'calculated').map(r => r.record_id)
  if (recordIds.length === 0) {
    ElMessage.warning('没有可确认的记录')
    return
  }
  salaryApi.confirmSalary({ record_ids: recordIds }).then(() => {
    ElMessage.success('确认成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="salary-calculate">
    <div class="page-header">
      <h1>薪资核算</h1>
      <div class="header-actions">
        <el-date-picker
          v-model="currentMonth"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择月份"
          @change="loadData"
        />
        <el-button type="primary" @click="calculateSalary">核算薪资</el-button>
        <el-button type="success" @click="confirmSalary">确认发放</el-button>
      </div>
    </div>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="student_name" label="学生姓名" width="120" />
      <el-table-column prop="job_name" label="岗位名称" width="150" />
      <el-table-column label="工时" width="120">
        <template #default="scope">
          <el-input-number 
            v-if="editingId === scope.row.record_id"
            v-model="editingHours"
            :min="0"
            :step="0.5"
          />
          <span v-else>{{ scope.row.work_hours }}小时</span>
        </template>
      </el-table-column>
      <el-table-column label="时薪" width="100">
        <template #default="scope">¥{{ scope.row.hourly_wage }}</template>
      </el-table-column>
      <el-table-column label="应发" width="100">
        <template #default="scope">¥{{ scope.row.gross_amount.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="个税" width="100">
        <template #default="scope">¥{{ scope.row.tax_amount.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="实发" width="100">
        <template #default="scope">¥{{ scope.row.net_amount.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status].class">{{ statusMap[scope.row.status].label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button 
            size="small" 
            v-if="editingId === scope.row.record_id"
            @click="saveEdit(scope.row)"
          >保存</el-button>
          <el-button 
            size="small" 
            v-if="editingId === scope.row.record_id"
            type="text"
            @click="cancelEdit"
          >取消</el-button>
          <el-button 
            size="small" 
            v-if="editingId !== scope.row.record_id && scope.row.status !== 'paid'"
            @click="startEdit(scope.row)"
          >修改工时</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="summary-row">
      <span class="summary-label">合计：</span>
      <span>应发 ¥{{ totalGross.toFixed(2) }}</span>
      <span>个税 ¥{{ totalTax.toFixed(2) }}</span>
      <span class="summary-total">实发 ¥{{ totalNet.toFixed(2) }}</span>
    </div>
  </div>
</template>

<style scoped>
.salary-calculate {
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
  gap: 12px;
}

.summary-row {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  padding: 16px 20px;
  background-color: #FFFFFF;
  border-radius: 8px;
  margin-top: 20px;
  font-size: 14px;
  color: #4E5969;
}

.summary-label {
  font-weight: bold;
}

.summary-total {
  font-weight: bold;
  color: #FF7D00;
}
</style>