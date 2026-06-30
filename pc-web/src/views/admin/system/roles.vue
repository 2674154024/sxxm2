<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { systemApi, type AdminRoleItem, type AdminUserItem } from '@/api/admin'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElTree, ElMessage, ElTag, ElPagination, ElPopconfirm } from 'element-plus'

const adminList = ref<AdminUserItem[]>([])
const roleList = ref<AdminRoleItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const isEdit = ref(false)
const currentAdmin = ref<AdminUserItem | null>(null)
const currentRole = ref<AdminRoleItem | null>(null)

const adminForm = ref({
  username: '',
  password: '',
  real_name: '',
  phone: '',
  role_type: 1
})

const roleTypeMap: Record<number, string> = {
  1: '审核管理员',
  2: '风控管理员',
  3: '运营管理员',
  4: '财务管理员',
  5: '超级管理员'
}

const statusMap: Record<number, { label: string; color: 'danger' | 'success' }> = {
  0: { label: '禁用', color: 'danger' },
  1: { label: '启用', color: 'success' }
}

const permissionTree = ref([
  { id: 'audit', label: '审核管理', children: [
    { id: 'audit:enterprise', label: '企业审核' },
    { id: 'audit:job', label: '岗位审核' }
  ]},
  { id: 'risk', label: '风控管理', children: [
    { id: 'risk:complaint', label: '投诉工单' },
    { id: 'risk:dashboard', label: '风控看板' }
  ]},
  { id: 'operation', label: '运营管理', children: [
    { id: 'operation:report', label: '数据报表' },
    { id: 'operation:notification', label: '推送管理' }
  ]},
  { id: 'finance', label: '财务管理', children: [
    { id: 'finance:settlement', label: '薪资发放' },
    { id: 'finance:report', label: '财务报表' }
  ]},
  { id: 'system', label: '系统管理', children: [
    { id: 'system:config', label: '系统配置' },
    { id: 'system:roles', label: '角色权限' },
    { id: 'system:audit-log', label: '审计日志' }
  ]}
])

const loadAdminList = async () => {
  try {
    const response = await systemApi.getAdminList({ page: page.value, size: size.value })
    if (response.code === 200) {
      adminList.value = response.data.list || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const loadRoleList = async () => {
  try {
    const response = await systemApi.getRoleList()
    if (response.code === 200) {
      roleList.value = response.data
    }
  } catch (error) {
    console.error('加载角色失败')
  }
}

const handleAdd = () => {
  isEdit.value = false
  adminForm.value = { username: '', password: '', real_name: '', phone: '', role_type: 1 }
  dialogVisible.value = true
}

const handleEdit = (row: AdminUserItem) => {
  isEdit.value = true
  currentAdmin.value = row
  adminForm.value = { 
    username: row.username, 
    password: '', 
    real_name: row.real_name, 
    phone: row.phone, 
    role_type: row.role_type 
  }
  dialogVisible.value = true
}

const handleDelete = async (admin_id: string) => {
  try {
    await systemApi.deleteAdmin(admin_id)
    ElMessage.success('删除成功')
    loadAdminList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleToggleStatus = async (row: AdminUserItem) => {
  try {
    await systemApi.updateAdmin({
      admin_id: row.admin_id,
      status: row.status === 1 ? 0 : 1
    })
    ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
    loadAdminList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSubmit = async () => {
  if (!adminForm.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }

  if (!isEdit.value && !adminForm.value.password) {
    ElMessage.warning('请输入密码')
    return
  }

  try {
    if (isEdit.value && currentAdmin.value) {
      await systemApi.updateAdmin({
        admin_id: currentAdmin.value.admin_id,
        real_name: adminForm.value.real_name,
        phone: adminForm.value.phone,
        role_type: adminForm.value.role_type
      })
    } else {
      await systemApi.createAdmin({
        username: adminForm.value.username,
        password: adminForm.value.password,
        real_name: adminForm.value.real_name,
        phone: adminForm.value.phone,
        role_type: adminForm.value.role_type
      })
    }
    
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadAdminList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleRoleDetail = (row: AdminRoleItem) => {
  currentRole.value = row
  roleDialogVisible.value = true
}

const handleSizeChange = (val: number) => {
  size.value = val
  page.value = 1
  loadAdminList()
}

const handlePageChange = (val: number) => {
  page.value = val
  loadAdminList()
}

onMounted(() => {
  loadAdminList()
  loadRoleList()
})
</script>

<template>
  <div class="roles-container">
    <div class="header-bar">
      <span class="page-title">角色权限管理</span>
      <ElButton type="primary" icon="Plus" @click="handleAdd">添加管理员</ElButton>
    </div>

    <ElCard title="管理员列表">
      <ElTable :data="adminList" stripe>
        <ElTableColumn prop="admin_id" label="管理员ID" width="160" />
        <ElTableColumn prop="username" label="用户名" width="120" />
        <ElTableColumn prop="real_name" label="真实姓名" width="120" />
        <ElTableColumn prop="phone" label="手机号" width="120" />
        <ElTableColumn prop="role_type" label="角色" width="120">
          <template #default="scope">
            {{ roleTypeMap[scope.row.role_type] || '未知' }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="100">
          <template #default="scope">
            <ElTag :type="statusMap[scope.row.status]?.color">
              {{ statusMap[scope.row.status]?.label }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="create_time" label="创建时间" width="160" />
        <ElTableColumn label="操作" width="250" fixed="right">
          <template #default="scope">
            <ElButton size="small" @click="handleEdit(scope.row)">编辑</ElButton>
            <ElButton 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </ElButton>
            <ElPopconfirm
              title="确定删除该管理员吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(scope.row.admin_id)"
            >
              <ElButton size="small" type="danger">删除</ElButton>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
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

    <ElCard title="角色权限" style="margin-top: 20px;">
      <ElTable :data="roleList" stripe>
        <ElTableColumn prop="role_id" label="角色ID" width="160" />
        <ElTableColumn prop="role_name" label="角色名称" width="120" />
        <ElTableColumn prop="role_type" label="角色类型" width="120">
          <template #default="scope">
            {{ roleTypeMap[scope.row.role_type] || '未知' }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="permissions" label="权限数量" width="100">
          <template #default="scope">
            {{ scope.row.permissions?.length || 0 }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="create_time" label="创建时间" width="160" />
        <ElTableColumn label="操作" width="120">
          <template #default="scope">
            <ElButton size="small" @click="handleRoleDetail(scope.row)">查看权限</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDialog :title="isEdit ? '编辑管理员' : '添加管理员'" v-model="dialogVisible" width="450px">
      <ElForm :model="adminForm" label-position="left" label-width="80px">
        <ElFormItem label="用户名" required>
          <ElInput v-model="adminForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </ElFormItem>
        <ElFormItem v-if="!isEdit" label="密码" required>
          <ElInput v-model="adminForm.password" type="password" placeholder="请输入密码" />
        </ElFormItem>
        <ElFormItem label="真实姓名">
          <ElInput v-model="adminForm.real_name" placeholder="请输入真实姓名" />
        </ElFormItem>
        <ElFormItem label="手机号">
          <ElInput v-model="adminForm.phone" placeholder="请输入手机号" />
        </ElFormItem>
        <ElFormItem label="角色" required>
          <ElSelect v-model="adminForm.role_type">
            <ElOption v-for="(label, value) in roleTypeMap" :key="value" :label="label" :value="Number(value)" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>

    <ElDialog title="角色权限详情" v-model="roleDialogVisible" width="500px">
      <div v-if="currentRole" class="role-detail">
        <div class="detail-row">
          <span class="detail-label">角色名称:</span>
          <span class="detail-value">{{ currentRole.role_name }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">角色类型:</span>
          <span class="detail-value">{{ roleTypeMap[currentRole.role_type] }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">权限列表:</span>
        </div>
        <ElTree 
          :data="permissionTree" 
          show-checkbox
          :default-checked-keys="currentRole.permissions || []"
          node-key="id"
          :props="{ label: 'label', children: 'children' }"
          :disabled="true"
        />
      </div>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
.roles-container {
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

.role-detail {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 12px;
}

.detail-label {
  width: 80px;
  color: #64748B;
}

.detail-value {
  flex: 1;
  color: #1E293B;
}
</style>