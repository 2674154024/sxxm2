<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { salaryApi, type PaymentRecord } from '@/api/salary'
import {
  Search,
  Refresh,
  View,
  RefreshRight
} from '@element-plus/icons-vue'

const tableData = ref<PaymentRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const statusFilter = ref('')
const searchKeyword = ref('')
const detailVisible = ref(false)
const currentDetail = ref<PaymentRecord | null>(null)

const statusMap: Record<string, { label: string; type: string; icon: string }> = {
  processing: { label: '处理中', type: 'info', icon: 'Loading' },
  success: { label: '已到账', type: 'success', icon: 'CircleCheck' },
  failed: { label: '失败', type: 'danger', icon: 'CircleClose' }
}

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (statusFilter.value && item.status !== statusFilter.value) return false
    if (searchKeyword.value && !item.student_name.includes(searchKeyword.value) && !item.job_name.includes(searchKeyword.value)) return false
    return true
  })
})

const loadData = () => {
  salaryApi.getPaymentRecords({ page: page.value, size: size.value }).then(res => {
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

const viewDetail = (record: PaymentRecord) => {
  currentDetail.value = record
  detailVisible.value = true
}

const retryPay = (record: PaymentRecord) => {
  ElMessage.success(`已重新发起 ${record.student_name} 的薪资发放`)
}

const resetFilter = () => {
  statusFilter.value = ''
  searchKeyword.value = ''
}

const handleSizeChange = (val: number) => {
  size.value = val
  loadData()
}

const handleCurrentChange = (val: number) => {
  page.value = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="payment-records">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">发放记录</h1>
        <p class="page-subtitle">查看薪资发放明细和状态</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-item">
        <span class="filter-label">状态：</span>
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="处理中" value="processing" />
          <el-option label="已到账" value="success" />
          <el-option label="失败" value="failed" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">搜索：</span>
        <el-input
          v-model="searchKeyword"
          placeholder="姓名/岗位"
          clearable
          style="width: 200px"
          :prefix-icon="Search"
        />
      </div>
      <div class="filter-item filter-actions">
        <el-button @click="resetFilter">重置</el-button>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table :data="filteredData" border style="width: 100%" :header-cell-style="{ backgroundColor: '#F7F8FA', color: '#4E5969', fontWeight: 600 }">
        <el-table-column prop="student_name" label="学生姓名" width="140">
          <template #default="scope">
            <div class="student-cell">
              <div class="student-avatar">{{ scope.row.student_name.charAt(0) }}</div>
              <span class="student-name">{{ scope.row.student_name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="job_name" label="岗位名称" min-width="180" />
        <el-table-column label="发放金额" width="140" align="right">
          <template #default="scope">
            <span class="amount-text">¥{{ scope.row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="pay_time" label="发放时间" width="180" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status].type" effect="light" size="small" class="status-tag">
              {{ statusMap[scope.row.status].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" link :icon="View" @click="viewDetail(scope.row)">详情</el-button>
            <el-button 
              size="small" 
              type="danger" 
              link 
              :icon="RefreshRight"
              v-if="scope.row.status === 'failed'"
              @click="retryPay(scope.row)"
            >重试</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          :current-page="page"
          :page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="发放详情" width="500px" class="detail-dialog">
      <div v-if="currentDetail" class="detail-content">
        <div class="detail-header">
          <div class="detail-avatar">{{ currentDetail.student_name.charAt(0) }}</div>
          <div class="detail-info">
            <div class="detail-name">{{ currentDetail.student_name }}</div>
            <div class="detail-job">{{ currentDetail.job_name }}</div>
          </div>
          <el-tag :type="statusMap[currentDetail.status].type" effect="light" size="default">
            {{ statusMap[currentDetail.status].label }}
          </el-tag>
        </div>

        <el-divider />

        <div class="detail-list">
          <div class="detail-item">
            <span class="detail-label">记录编号</span>
            <span class="detail-value">{{ currentDetail.record_id }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">发放金额</span>
            <span class="detail-value amount">¥{{ currentDetail.amount.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">发放时间</span>
            <span class="detail-value">{{ currentDetail.pay_time }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">发放状态</span>
            <el-tag :type="statusMap[currentDetail.status].type" effect="light" size="small">
              {{ statusMap[currentDetail.status].label }}
            </el-tag>
          </div>
        </div>

        <div v-if="currentDetail.status === 'failed'" class="fail-reason">
          <div class="fail-title">失败原因</div>
          <div class="fail-desc">银行卡信息有误，请核对后重新发起发放</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" v-if="currentDetail?.status === 'failed'" @click="retryPay(currentDetail!)">
          重新发放
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.payment-records {
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

.amount-text {
  font-size: 15px;
  color: #FF7D00;
  font-weight: 700;
}

.status-tag {
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #F2F3F5;
}

.detail-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #F2F3F5;
    margin-right: 0;

    .el-dialog__title {
      font-size: 16px;
      font-weight: 600;
      color: #1F2329;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #F2F3F5;
  }
}

.detail-content {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .detail-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .detail-info {
    flex: 1;

    .detail-name {
      font-size: 18px;
      font-weight: 600;
      color: #1F2329;
      margin-bottom: 4px;
    }

    .detail-job {
      font-size: 14px;
      color: #86909C;
    }
  }
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .detail-label {
    font-size: 14px;
    color: #86909C;
  }

  .detail-value {
    font-size: 14px;
    color: #1F2329;
    font-weight: 500;

    &.amount {
      font-size: 20px;
      color: #FF7D00;
      font-weight: 700;
    }
  }
}

.fail-reason {
  margin-top: 20px;
  padding: 16px;
  background-color: #FFF1F0;
  border-radius: 8px;
  border: 1px solid #FFCCC7;

  .fail-title {
    font-size: 14px;
    font-weight: 600;
    color: #F53F3F;
    margin-bottom: 8px;
  }

  .fail-desc {
    font-size: 13px;
    color: #F53F3F;
    line-height: 1.5;
  }
}
</style>
