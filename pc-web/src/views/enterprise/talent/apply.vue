<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { talentApi, type ApplyRecord } from '@/api/talent'

const tableData = ref<ApplyRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const statusFilter = ref('')

const statusMap: Record<string, { label: string; class: string }> = {
  pending: { label: '待处理', class: 'warning' },
  interview: { label: '面试中', class: 'primary' },
  hired: { label: '已录用', class: 'success' },
  rejected: { label: '已拒绝', class: 'danger' }
}

const loadData = () => {
  talentApi.getApplyList({ page: page.value, size: size.value, status: statusFilter.value }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).catch(() => {
    tableData.value = [
      { apply_id: '1', job_name: '初中数学家教', student_name: '张三', student_phone: '138****1234', status: 'pending', apply_time: '2026-06-29 10:30' },
      { apply_id: '2', job_name: '超市促销', student_name: '李四', student_phone: '139****5678', status: 'interview', apply_time: '2026-06-28 14:20' },
      { apply_id: '3', job_name: '展会协助', student_name: '王五', student_phone: '137****9012', status: 'hired', apply_time: '2026-06-27 09:15' },
      { apply_id: '4', job_name: '英语助教', student_name: '赵六', student_phone: '136****3456', status: 'rejected', apply_time: '2026-06-26 16:40' },
      { apply_id: '5', job_name: '奶茶店店员', student_name: '孙七', student_phone: '135****7890', status: 'pending', apply_time: '2026-06-25 11:25' }
    ]
    total.value = 5
  })
}

const handleApply = (applyId: string, status: 'interview' | 'hired' | 'rejected') => {
  talentApi.handleApply({ apply_id: applyId, status }).then(() => {
    ElMessage.success('操作成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="apply-list">
    <div class="page-header">
      <h1>投递管理</h1>
      <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 150px;" @change="loadData">
        <el-option label="全部" value="" />
        <el-option label="待处理" value="pending" />
        <el-option label="面试中" value="interview" />
        <el-option label="已录用" value="hired" />
        <el-option label="已拒绝" value="rejected" />
      </el-select>
    </div>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="job_name" label="岗位名称" />
      <el-table-column prop="student_name" label="学生姓名" />
      <el-table-column prop="student_phone" label="联系电话" />
      <el-table-column prop="apply_time" label="投递时间" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status].class">{{ statusMap[scope.row.status].label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            v-if="scope.row.status === 'pending'"
            @click="handleApply(scope.row.apply_id, 'interview')"
          >安排面试</el-button>
          <el-button 
            size="small" 
            type="success" 
            v-if="scope.row.status === 'pending' || scope.row.status === 'interview'"
            @click="handleApply(scope.row.apply_id, 'hired')"
          >录用</el-button>
          <el-button 
            size="small" 
            type="danger" 
            v-if="scope.row.status === 'pending'"
            @click="handleApply(scope.row.apply_id, 'rejected')"
          >拒绝</el-button>
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
.apply-list {
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