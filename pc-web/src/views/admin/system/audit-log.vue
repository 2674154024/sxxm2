<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { systemApi, type AuditLogItem } from '@/api/admin'
import { ElTable, ElTableColumn, ElButton, ElSelect, ElOption, ElDatePicker, ElMessage, ElPagination } from 'element-plus'
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
    <div class="header-bar">
      <span class="page-title">操作审计日志</span>
      <ElButton type="primary" icon="Download" @click="exportExcel">导出日志</ElButton>
    </div>

    <div class="search-bar">
      <div class="search-form">
        <ElSelect 
          v-model="searchForm.module" 
          placeholder="模块" 
          clearable
          style="width: 150px"
        >
          <ElOption v-for="(label, value) in moduleMap" :key="value" :label="label" :value="value" />
        </ElSelect>
        <ElSelect 
          v-model="searchForm.role_type" 
          placeholder="角色" 
          clearable
          style="width: 150px"
        >
          <ElOption v-for="(label, value) in roleTypeMap" :key="value" :label="label" :value="String(value)" />
        </ElSelect>
        <ElDatePicker
          v-model="searchForm.start_time"
          type="date"
          placeholder="开始日期"
          value-format="YYYY-MM-DD"
        />
        <span class="date-separator">至</span>
        <ElDatePicker
          v-model="searchForm.end_time"
          type="date"
          placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
      </div>
      <div class="search-actions">
        <ElButton type="primary" @click="handleSearch" icon="Search">搜索</ElButton>
        <ElButton @click="handleReset">重置</ElButton>
        <ElButton @click="loadData" icon="Refresh">刷新</ElButton>
      </div>
    </div>

    <ElTable :data="tableData" border stripe class="audit-table">
      <ElTableColumn prop="log_id" label="日志ID" width="180" />
      <ElTableColumn prop="operator_name" label="操作人" width="100" />
      <ElTableColumn prop="role_type" label="角色" width="120">
        <template #default="scope">
          {{ roleTypeMap[scope.row.role_type] || '未知' }}
        </template>
      </ElTableColumn>
      <ElTableColumn prop="module" label="模块" width="120">
        <template #default="scope">
          {{ moduleMap[scope.row.module] || scope.row.module }}
        </template>
      </ElTableColumn>
      <ElTableColumn prop="action" label="操作" width="120" />
      <ElTableColumn prop="request_params" label="参数" min-width="200" show-overflow-tooltip />
      <ElTableColumn prop="ip_address" label="IP地址" width="150" />
      <ElTableColumn prop="client_type" label="客户端" width="100" />
      <ElTableColumn prop="create_time" label="操作时间" width="180">
        <template #default="scope">
          {{ dayjs(scope.row.create_time).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </ElTableColumn>
    </ElTable>

    <div class="pagination-wrapper">
      <ElPagination
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
</template>

<style scoped lang="scss">
.audit-log-container {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #1E293B;
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
  align-items: center;
  gap: 16px;
}

.date-separator {
  color: #94A3B8;
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