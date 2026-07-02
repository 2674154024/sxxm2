<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { Money, Wallet, Clock, Document } from '@element-plus/icons-vue'
import { financeApi, type Statement } from '@/api/finance'

const tableData = ref<Statement[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const monthFilter = ref(dayjs().format('YYYY-MM'))

const statusMap: Record<string, { label: string; class: string }> = {
  pending: { label: '待确认', class: 'warning' },
  confirmed: { label: '已确认', class: 'info' },
  paid: { label: '已结算', class: 'success' }
}

const stats = computed(() => {
  const list = tableData.value
  const totalAmount = list.reduce((sum, item) => sum + (item.total_income || 0), 0)
  const settled = list.filter(item => item.status === 'paid').reduce((sum, item) => sum + (item.balance || 0), 0)
  const pending = list.filter(item => item.status !== 'paid').reduce((sum, item) => sum + (item.balance || 0), 0)
  const count = list.length
  return { totalAmount, settled, pending, count }
})

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

const viewDetail = (row: Statement) => {
  ElMessage.info(`查看对账单 ${row.statement_id} 详情`)
}

const formatMoney = (value: number) => {
  return '¥' + value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="statement-page">
    <div class="page-header">
      <div class="header-left">
        <h1>对账单</h1>
        <p class="subtitle">查看企业收支明细与结算记录</p>
      </div>
      <div class="header-right">
        <el-date-picker
          v-model="monthFilter"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择月份"
          @change="loadData"
          class="month-picker"
        />
      </div>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="card-icon icon-primary">
          <el-icon :size="24"><Money /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">总金额</div>
          <div class="card-value">{{ formatMoney(stats.totalAmount) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-success">
          <el-icon :size="24"><Wallet /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">已结算</div>
          <div class="card-value text-success">{{ formatMoney(stats.settled) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-warning">
          <el-icon :size="24"><Clock /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">待结算</div>
          <div class="card-value text-warning">{{ formatMoney(stats.pending) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-info">
          <el-icon :size="24"><Document /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">账单笔数</div>
          <div class="card-value">{{ stats.count }} 笔</div>
        </div>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table :data="tableData" stripe style="width: 100%" class="statement-table">
        <el-table-column prop="statement_id" label="账单编号" width="140" />
        <el-table-column prop="month" label="账期月份" width="120">
          <template #default="scope">
            <span class="month-tag">{{ scope.row.month }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收入金额" width="140" align="right">
          <template #default="scope">
            <span class="amount-income">{{ formatMoney(scope.row.total_income) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支出金额" width="140" align="right">
          <template #default="scope">
            <span class="amount-payment">{{ formatMoney(scope.row.total_payment) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结算余额" width="140" align="right">
          <template #default="scope">
            <span class="amount-balance">{{ formatMoney(scope.row.balance) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status].class" effect="light" round>
              {{ statusMap[scope.row.status].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_time" label="生成时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="viewDetail(scope.row)">查看详情</el-button>
            <el-button 
              type="primary" 
              link 
              size="small" 
              @click="confirmStatement(scope.row.statement_id)" 
              v-if="scope.row.status === 'pending'"
            >
              确认账单
            </el-button>
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
  </div>
</template>

<style scoped lang="scss">
.statement-page {
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

  .month-picker {
    width: 200px;
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

  .statement-table {
    :deep(.el-table__header th) {
      background-color: #F7F8FA;
      color: #4E5969;
      font-weight: 500;
    }
  }

  .month-tag {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(22, 93, 255, 0.1);
    color: #165DFF;
    border-radius: 4px;
    font-size: 13px;
  }

  .amount-income {
    color: #00B42A;
    font-weight: 500;
  }

  .amount-payment {
    color: #F53F3F;
    font-weight: 500;
  }

  .amount-balance {
    color: #1F2329;
    font-weight: 600;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
