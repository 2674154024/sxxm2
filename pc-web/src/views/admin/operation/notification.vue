<script setup lang="ts">
import { ref } from 'vue'
import { operationApi } from '@/api/admin'
import { ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElButton, ElDialog, ElTable, ElTableColumn, ElMessage, ElTag, ElPagination } from 'element-plus'

const formVisible = ref(false)
const form = ref({
  title: '',
  content: '',
  target_type: 'all',
  target_ids: []
})

const notificationList = ref([
  { id: 'NTF20260629001', title: '平台安全公告', target_type: 'all', status: '已发送', send_time: '2026-06-29 10:00', count: 58600 },
  { id: 'NTF20260628002', title: '暑期兼职活动通知', target_type: 'student', status: '已发送', send_time: '2026-06-28 14:30', count: 42000 },
  { id: 'NTF20260627003', title: '企业认证流程优化', target_type: 'enterprise', status: '已发送', send_time: '2026-06-27 09:00', count: 3250 },
  { id: 'NTF20260626004', title: '新功能上线通知', target_type: 'all', status: '已发送', send_time: '2026-06-26 16:00', count: 58600 },
  { id: 'NTF20260625005', title: '安全警示提醒', target_type: 'student', status: '已发送', send_time: '2026-06-25 11:00', count: 42000 },
])

const total = ref(5)
const page = ref(1)
const size = ref(20)

const targetTypeMap: Record<string, string> = {
  all: '全部用户',
  student: '学生用户',
  enterprise: '企业用户'
}

const statusMap: Record<string, { label: string; color: 'success' | 'info' | 'danger' }> = {
  '已发送': { label: '已发送', color: 'success' },
  '发送中': { label: '发送中', color: 'info' },
  '失败': { label: '失败', color: 'danger' }
}

const handleSubmit = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  try {
    await operationApi.sendNotification({
      title: form.value.title,
      content: form.value.content,
      target_type: form.value.target_type as 'all' | 'student' | 'enterprise',
      target_ids: form.value.target_type !== 'all' ? form.value.target_ids : undefined
    })
    
    ElMessage.success('发送成功')
    formVisible.value = false
    form.value = { title: '', content: '', target_type: 'all', target_ids: [] }
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

const handleSizeChange = (val: number) => {
  size.value = val
  page.value = 1
}

const handlePageChange = (val: number) => {
  page.value = val
}
</script>

<template>
  <div class="notification-container">
    <div class="header-bar">
      <span class="page-title">推送管理</span>
      <el-button type="primary" icon="Send" @click="formVisible = true">发送通知</el-button>
    </div>

    <ElCard>
      <ElTable :data="notificationList" stripe>
        <ElTableColumn prop="id" label="通知编号" width="160" />
        <ElTableColumn prop="title" label="通知标题" min-width="200" />
        <ElTableColumn prop="target_type" label="推送对象" width="120">
          <template #default="scope">
            {{ targetTypeMap[scope.row.target_type] || scope.row.target_type }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="count" label="发送数量" width="120">
          <template #default="scope">
            {{ scope.row.count.toLocaleString() }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="100">
          <template #default="scope">
            <ElTag :type="statusMap[scope.row.status]?.color">
              {{ statusMap[scope.row.status]?.label }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="send_time" label="发送时间" width="160" />
      </ElTable>

      <div class="pagination-wrapper">
        <ElPagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </ElCard>

    <ElDialog title="发送通知" v-model="formVisible" width="500px">
      <ElForm v-model="form" label-position="left" label-width="80px">
        <ElFormItem label="通知标题" required>
          <ElInput v-model="form.title" placeholder="请输入通知标题" />
        </ElFormItem>
        <ElFormItem label="推送对象" required>
          <ElSelect v-model="form.target_type">
            <ElOption label="全部用户" value="all" />
            <ElOption label="学生用户" value="student" />
            <ElOption label="企业用户" value="enterprise" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="通知内容" required>
          <ElInput v-model="form.content" type="textarea" :rows="4" placeholder="请输入通知内容" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="formVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">发送</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
.notification-container {
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>