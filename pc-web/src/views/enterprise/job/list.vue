<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { jobApi, type Job } from '@/api/job'


const router = useRouter()
const tableData = ref<Job[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const searchKeyword = ref('')
const statusFilter = ref('')

const statusMap: Record<string, { label: string; class: string }> = {
  pending: { label: '待审核', class: 'warning' },
  approved: { label: '已上架', class: 'success' },
  rejected: { label: '已驳回', class: 'danger' },
  offline: { label: '已下架', class: 'info' }
}

const salaryTypeMap: Record<string, string> = {
  hourly: '小时',
  daily: '日',
  monthly: '月'
}

const loadData = () => {
  jobApi.getJobList({ page: page.value, size: size.value, status: statusFilter.value }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).catch(() => {
    tableData.value = [
      { job_id: '1', job_name: '初中数学家教', company_name: '长沙市XX教育机构', salary: 50, salary_type: 'hourly', settlement_type: 'weekly', address: '岳麓区XX小区', longitude: 112.93, latitude: 28.22, work_time: { weekday: ['周一', '周三', '周五'], time_range: ['18:00-20:00'] }, skill_tags: ['数学', '初中'], description: '辅导初中数学', has_insurance: true, status: 'approved', hired_count: 3, apply_count: 15, created_time: '2026-06-01' },
      { job_id: '2', job_name: '超市促销', company_name: 'XX超市', salary: 15, salary_type: 'hourly', settlement_type: 'daily', address: '芙蓉区XX广场', longitude: 112.98, latitude: 28.26, work_time: { weekday: ['周六', '周日'], time_range: ['10:00-20:00'] }, skill_tags: ['促销', '沟通'], description: '超市促销导购', has_insurance: false, status: 'approved', hired_count: 5, apply_count: 23, created_time: '2026-06-05' },
      { job_id: '3', job_name: '展会协助', company_name: 'XX会展公司', salary: 25, salary_type: 'hourly', settlement_type: 'daily', address: '开福区XX会展中心', longitude: 112.95, latitude: 28.28, work_time: { weekday: ['周六'], time_range: ['09:00-18:00'] }, skill_tags: ['展会', '协助'], description: '展会现场协助', has_insurance: true, status: 'pending', hired_count: 0, apply_count: 8, created_time: '2026-06-28' },
      { job_id: '4', job_name: '英语助教', company_name: 'XX培训机构', salary: 20, salary_type: 'hourly', settlement_type: 'weekly', address: '雨花区XX大厦', longitude: 113.01, latitude: 28.22, work_time: { weekday: ['周二', '周四'], time_range: ['19:00-21:00'] }, skill_tags: ['英语', '教学'], description: '英语教学辅助', has_insurance: true, status: 'approved', hired_count: 2, apply_count: 12, created_time: '2026-06-10' },
      { job_id: '5', job_name: '奶茶店店员', company_name: 'XX奶茶店', salary: 18, salary_type: 'hourly', settlement_type: 'weekly', address: '天心区XX街', longitude: 112.99, latitude: 28.24, work_time: { weekday: ['周五', '周六', '周日'], time_range: ['14:00-22:00'] }, skill_tags: ['服务', '饮品'], description: '奶茶店店员', has_insurance: false, status: 'offline', hired_count: 4, apply_count: 18, created_time: '2026-05-20' }
    ]
    total.value = 5
  })
}

const handleOffline = (jobId: string) => {
  ElMessageBox.confirm('确定要下架该岗位吗？', '确认下架').then(() => {
    jobApi.offlineJob(jobId).then(() => {
      ElMessage.success('已下架')
      loadData()
    })
  })
}

const handlePublish = () => {
  router.push('/enterprise/job/publish')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="job-list">
    <div class="page-header">
      <h1>岗位列表</h1>
      <div class="header-actions">
        <el-input v-model="searchKeyword" placeholder="搜索岗位名称" style="width: 200px; margin-right: 12px;" @keyup.enter="loadData" />
        <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 150px; margin-right: 12px;" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已上架" value="approved" />
          <el-option label="已驳回" value="rejected" />
          <el-option label="已下架" value="offline" />
        </el-select>
        <el-button type="primary" icon="Plus" @click="handlePublish">发布岗位</el-button>
      </div>
    </div>

    <el-table :data="tableData" border style="width: 100%" v-loading="false">
      <el-table-column prop="job_name" label="岗位名称" />
      <el-table-column prop="company_name" label="企业名称" />
      <el-table-column label="薪资">
        <template #default="scope">
          ¥{{ scope.row.salary }}/{{ salaryTypeMap[scope.row.salary_type] }}
        </template>
      </el-table-column>
      <el-table-column prop="address" label="工作地点" />
      <el-table-column prop="skill_tags" label="技能标签">
        <template #default="scope">
          <el-tag v-for="tag in scope.row.skill_tags" :key="tag" size="small">{{ tag }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="apply_count" label="投递数" width="100" />
      <el-table-column prop="hired_count" label="录用数" width="100" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status].class">{{ statusMap[scope.row.status].label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" icon="Edit">编辑</el-button>
          <el-button size="small" icon="Eye">详情</el-button>
          <el-button size="small" icon="Delete" type="danger" @click="handleOffline(scope.row.job_id)">下架</el-button>
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
.job-list {
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