<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { systemApi, type AuditLogItem } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Download, Search, RefreshLeft, Refresh, Document, User, Menu, Clock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const tableData = ref<AuditLogItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const searchForm = ref({
  module: '',
  role_type: '',
  start_time: '',
  end_time: ''
})

const roleTypeMap: Record<number, string> = {
  1: '审核管理员',
  2: '风控管理员',
  3: '运营管理员',
  4: '财务管理员',
  5: '超级管理员'
}

const moduleMap: Record<string, string> = {
  audit: '审核管理',
  risk: '风控管理',
  finance: '财务管理',
  operation: '运营管理',
  system: '系统管理'
}

const uniqueOperators = computed(() => {
  const operators = new Set(tableData.value.map(item => item.operator_name))
  return operators.size
})

const moduleCount = computed(() => {
  const modules = new Set(tableData.value.map(item => item.module))
  return modules.size
})

const roleTagType = (roleType: number): 'info' | 'success' | 'warning' | 'danger' | 'info' => {
  const map: Record<number, 'info' | 'success' | 'warning' | 'danger' | 'info'> = {
    1: 'info',
    2: 'danger',
    3: 'success',
    4: 'warning',
    5: 'info'
  }
  return map[roleType] || 'info'
}

const loadData = async () => {
  try {
    const response = await systemApi.getAuditLogList({
      page: page.value,
      size: size.value,
      module: searchForm.value.module || undefined,
      role_type: searchForm.value.role_type ? parseInt(searchForm.value.role_type) : undefined,
      start_time: searchForm.value.start_time,
      end_time: searchForm.value.end_time
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
  searchForm.value = { module: '', role_type: '', start_time: '', end_time: '' }
  page.value = 1
  loadData()
}

const exportExcel = () => {
  console.log('导出审计日志')
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
  <div class="audit-log-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">操作审计日志</h2>
        <p class="page-desc">记录系统所有管理员操作行为，支持追溯和审计</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Download" @click="exportExcel">导出日志</el-button>
      </div>
    </div>

    <div class="stats-row">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon-box stat-icon-primary">
              <Document />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ total }}</div>
              <div class="stat-label">总日志数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon-box stat-icon-success">
              <User />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ uniqueOperators }}</div>
              <div class="stat-label">操作人数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon-box stat-icon-warning">
              <Menu />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ moduleCount }}</div>
              <div class="stat-label">涉及模块</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon-box stat-icon-danger">
              <Clock />
            </div>
            <div class="stat-info">
              <div class="stat-value">今日</div>
              <div class="stat-label">实时更新</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="card">
      <div class="search-bar">
        <div class="search-form">
          <el-select 
            v-model="searchForm.module" 
            placeholder="选择模块" 
            clearable
            style="width: 160px"
          >
            <el-option v-for="(label, value) in moduleMap" :key="value" :label="label" :value="value" />
          </el-select>
          <el-select 
            v-model="searchForm.role_type" 
            placeholder="选择角色" 
            clearable
            style="width: 160px"
          >
            <el-option v-for="(label, value) in roleTypeMap" :key="value" :label="label" :value="String(value)" />
          </el-select>
          <el-date-picker
            v-model="searchForm.start_time"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
          />
          <span class="date-separator">至</span>
          <el-date-picker
            v-model="searchForm.end_time"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </div>
        <div class="search-actions">
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="RefreshLeft">重置</el-button>
          <el-button @click="loadData" :icon="Refresh">刷新</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="audit-table">
        <el-table-column prop="log_id" label="日志ID" width="200" />
        <el-table-column prop="operator_name" label="操作人" width="120" />
        <el-table-column prop="role_type" label="角色" width="140">
          <template #default="scope">
            <el-tag :type="roleTagType(scope.row.role_type)" effect="light" size="small">
              {{ roleTypeMap[scope.row.role_type] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120">
          <template #default="scope">
            <el-tag type="info" effect="light" size="small">
              {{ moduleMap[scope.row.module] || scope.row.module }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="120" />
        <el-table-column prop="request_params" label="请求参数" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ip_address" label="IP地址" width="140" />
        <el-table-column prop="client_type" label="客户端" width="100">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ scope.row.client_type || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="操作时间" width="180">
          <template #default="scope">
            <span class="time-text">{{ dayjs(scope.row.create_time).format('YYYY-MM-DD HH:mm:ss') }}</span>
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
    </div>
  </div>
</template>

<style scoped lang="scss">
.audit-log-container {
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.header-actions {
  display: flex;
  gap: 12px;
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
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-1px);
  }
}

.stat-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.stat-icon-primary {
    background: #E8F3FF;
    color: #165DFF;
  }
  &.stat-icon-success {
    background: #E8FFEA;
    color: #00B42A;
  }
  &.stat-icon-warning {
    background: #FFF7E8;
    color: #FF7D00;
  }
  &.stat-icon-danger {
    background: #FFECE8;
    color: #F53F3F;
  }
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1D2129;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #86909C;
}

.card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
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
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.date-separator {
  color: #C9CDD4;
}

.search-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.audit-table {
  margin-bottom: 16px;

  :deep(.el-table__row) {
    &:hover > td {
      background-color: #F7F8FA !important;
    }
  }
}

.time-text {
  color: #4E5969;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>