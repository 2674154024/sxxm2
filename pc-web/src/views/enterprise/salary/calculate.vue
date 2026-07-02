<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { salaryApi, type SalaryRecord } from '@/api/salary'
import {
  Wallet,
  User,
  Clock,
  Money,
  Search,
  Refresh
} from '@element-plus/icons-vue'

const currentMonth = ref(dayjs().format('YYYY-MM'))
const tableData = ref<SalaryRecord[]>([])
const editingId = ref<string | null>(null)
const editingHours = ref(0)
const jobFilter = ref('')
const statusFilter = ref('')
const searchKeyword = ref('')

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (jobFilter.value && item.job_name !== jobFilter.value) return false
    if (statusFilter.value && item.status !== statusFilter.value) return false
    if (searchKeyword.value && !item.student_name.includes(searchKeyword.value) && !item.job_name.includes(searchKeyword.value)) return false
    return true
  })
})

const totalGross = computed(() => filteredData.value.reduce((sum, item) => sum + item.gross_amount, 0))
const totalTax = computed(() => filteredData.value.reduce((sum, item) => sum + item.tax_amount, 0))
const totalNet = computed(() => filteredData.value.reduce((sum, item) => sum + item.net_amount, 0))
const totalPeople = computed(() => filteredData.value.length)
const totalHours = computed(() => filteredData.value.reduce((sum, item) => sum + item.work_hours, 0))

const statusMap: Record<string, { label: string; type: string }> = {
  pending: { label: '待核算', type: 'warning' },
  calculated: { label: '已核算', type: 'info' },
  confirmed: { label: '已确认', type: 'info' },
  paid: { label: '已发放', type: 'success' },
  rejected: { label: '已驳回', type: 'danger' }
}

const jobOptions = computed(() => {
  const jobs = [...new Set(tableData.value.map(item => item.job_name))]
  return jobs.map(job => ({ label: job, value: job }))
})

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

const resetFilter = () => {
  jobFilter.value = ''
  statusFilter.value = ''
  searchKeyword.value = ''
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="salary-calculate">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">薪资核算</h1>
        <p class="page-subtitle">管理和核算学生兼职工资</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        <el-button type="primary" @click="calculateSalary">核算薪资</el-button>
        <el-button type="success" @click="confirmSalary">确认发放</el-button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card stat-card--blue">
        <div class="stat-card__icon">
          <el-icon :size="24"><Wallet /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">¥{{ totalGross.toLocaleString() }}</div>
          <div class="stat-card__label">应发总金额</div>
        </div>
      </div>
      <div class="stat-card stat-card--green">
        <div class="stat-card__icon">
          <el-icon :size="24"><User /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ totalPeople }}</div>
          <div class="stat-card__label">核算人数</div>
        </div>
      </div>
      <div class="stat-card stat-card--orange">
        <div class="stat-card__icon">
          <el-icon :size="24"><Clock /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ totalHours }}</div>
          <div class="stat-card__label">总工时</div>
        </div>
      </div>
      <div class="stat-card stat-card--purple">
        <div class="stat-card__icon">
          <el-icon :size="24"><Money /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">¥{{ totalNet.toLocaleString() }}</div>
          <div class="stat-card__label">实发总金额</div>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-item">
        <span class="filter-label">月份：</span>
        <el-date-picker
          v-model="currentMonth"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择月份"
          @change="loadData"
        />
      </div>
      <div class="filter-item">
        <span class="filter-label">岗位：</span>
        <el-select v-model="jobFilter" placeholder="全部岗位" clearable style="width: 160px">
          <el-option v-for="job in jobOptions" :key="job.value" :label="job.label" :value="job.value" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">状态：</span>
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待核算" value="pending" />
          <el-option label="已核算" value="calculated" />
          <el-option label="已确认" value="confirmed" />
          <el-option label="已发放" value="paid" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">搜索：</span>
        <el-input
          v-model="searchKeyword"
          placeholder="姓名/岗位"
          clearable
          style="width: 180px"
          :prefix-icon="Search"
        />
      </div>
      <div class="filter-item filter-actions">
        <el-button @click="resetFilter">重置</el-button>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table :data="filteredData" border style="width: 100%" :header-cell-style="{ backgroundColor: '#F7F8FA', color: '#4E5969', fontWeight: 600 }">
        <el-table-column prop="student_name" label="学生姓名" width="120">
          <template #default="scope">
            <div class="student-cell">
              <div class="student-avatar">{{ scope.row.student_name.charAt(0) }}</div>
              <span class="student-name">{{ scope.row.student_name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="job_name" label="岗位名称" min-width="150" />
        <el-table-column label="工时" width="140" align="center">
          <template #default="scope">
            <el-input-number 
              v-if="editingId === scope.row.record_id"
              v-model="editingHours"
              :min="0"
              :step="0.5"
              size="small"
              controls-position="right"
            />
            <span v-else class="hours-text">{{ scope.row.work_hours }} 小时</span>
          </template>
        </el-table-column>
        <el-table-column label="时薪" width="100" align="right">
          <template #default="scope">
            <span class="wage-text">¥{{ scope.row.hourly_wage }}</span>
          </template>
        </el-table-column>
        <el-table-column label="应发" width="120" align="right">
          <template #default="scope">
            <span class="gross-amount">¥{{ scope.row.gross_amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="个税" width="100" align="right">
          <template #default="scope">
            <span class="tax-amount">¥{{ scope.row.tax_amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实发" width="130" align="right">
          <template #default="scope">
            <span class="net-amount">¥{{ scope.row.net_amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status].type" effect="light" size="small">
              {{ statusMap[scope.row.status].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <template v-if="editingId === scope.row.record_id">
              <el-button size="small" type="primary" @click="saveEdit(scope.row)">保存</el-button>
              <el-button size="small" @click="cancelEdit">取消</el-button>
            </template>
            <el-button 
              size="small" 
              type="primary" 
              link
              v-if="editingId !== scope.row.record_id && scope.row.status !== 'paid'"
              @click="startEdit(scope.row)"
            >修改工时</el-button>
          </template>
        </el-table-column>
        <template #footer>
          <tr class="table-footer-row">
            <td colspan="2" style="text-align: right; font-weight: 600; color: #4E5969;">合计</td>
            <td style="text-align: center; font-weight: 600; color: #165DFF;">{{ totalHours }} 小时</td>
            <td></td>
            <td style="text-align: right; font-weight: 600; color: #165DFF;">¥{{ totalGross.toFixed(2) }}</td>
            <td style="text-align: right; font-weight: 600; color: #F53F3F;">¥{{ totalTax.toFixed(2) }}</td>
            <td style="text-align: right; font-weight: 700; color: #FF7D00; font-size: 15px;">¥{{ totalNet.toFixed(2) }}</td>
            <td colspan="2"></td>
          </tr>
        </template>
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.salary-calculate {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1F2329;
    margin: 0;
  }

  .page-subtitle {
    font-size: 13px;
    color: #86909C;
    margin: 4px 0 0 0;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  overflow: hidden;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  background-color: #fff;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }

  &__icon {
    position: relative;
    z-index: 1;
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-right: 16px;
    flex-shrink: 0;
  }

  &__info {
    position: relative;
    z-index: 1;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  &__value {
    font-size: 24px;
    font-weight: 700;
    line-height: 1.2;
  }

  &__label {
    font-size: 13px;
    margin-top: 4px;
  }

  &--blue {
    .stat-card__icon {
      background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
    }
    .stat-card__value {
      color: #165DFF;
    }
    .stat-card__label {
      color: #86909C;
    }
  }

  &--green {
    .stat-card__icon {
      background: linear-gradient(135deg, #00B42A 0%, #23C343 100%);
    }
    .stat-card__value {
      color: #00B42A;
    }
    .stat-card__label {
      color: #86909C;
    }
  }

  &--orange {
    .stat-card__icon {
      background: linear-gradient(135deg, #FF7D00 0%, #FF9A2E 100%);
    }
    .stat-card__value {
      color: #FF7D00;
    }
    .stat-card__label {
      color: #86909C;
    }
  }

  &--purple {
    .stat-card__icon {
      background: linear-gradient(135deg, #722ED1 0%, #9254DE 100%);
    }
    .stat-card__value {
      color: #722ED1;
    }
    .stat-card__label {
      color: #86909C;
    }
  }
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;

  &.filter-actions {
    margin-left: auto;
  }
}

.filter-label {
  font-size: 14px;
  color: #4E5969;
  white-space: nowrap;
}

.table-wrapper {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.student-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.student-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.student-name {
  font-size: 14px;
  color: #1F2329;
  font-weight: 500;
}

.hours-text {
  font-size: 14px;
  color: #4E5969;
  font-weight: 500;
}

.wage-text {
  font-size: 14px;
  color: #86909C;
}

.gross-amount {
  font-size: 14px;
  color: #165DFF;
  font-weight: 600;
}

.tax-amount {
  font-size: 14px;
  color: #F53F3F;
}

.net-amount {
  font-size: 15px;
  color: #FF7D00;
  font-weight: 700;
}

.table-footer-row {
  background-color: #F7F8FA;
  
  td {
    padding: 12px 16px;
    border-top: 1px solid #E5E6EB;
  }
}

:deep(.el-table__footer-wrapper) {
  .el-table__footer {
    background-color: #F7F8FA;
  }
}
</style>
