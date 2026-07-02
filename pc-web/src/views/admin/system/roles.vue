<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { systemApi, type AdminRoleItem, type AdminUserItem } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { 
  Plus, Edit, Delete, View, User, Avatar, Lock, Key, List, UserFilled, Refresh 
} from '@element-plus/icons-vue'

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

const loadingAdmin = ref(false)
const loadingRoles = ref(false)

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

const roleCodeMap: Record<string, string> = {
  'audit': '审核管理员',
  'risk': '风控管理员',
  'operation': '运营管理员',
  'finance': '财务管理',
  'super': '超级管理员'
}

const statusMap: Record<number, { label: string; color: 'danger' | 'success' }> = {
  0: { label: '禁用', color: 'danger' },
  1: { label: '启用', color: 'success' }
}

const activeAdminCount = computed(() => {
  return adminList.value.filter(item => item.status === 1).length
})

const roleTagType = (roleType: number): 'info' | 'success' | 'warning' | 'danger' => {
  const map: Record<number, 'info' | 'success' | 'warning' | 'danger'> = {
    1: 'info',
    2: 'danger',
    3: 'success',
    4: 'warning',
    5: 'info'
  }
  return map[roleType] || 'info'
}

const roleCodeTagType = (roleCode: string): 'info' | 'success' | 'warning' | 'danger' => {
  const map: Record<string, 'info' | 'success' | 'warning' | 'danger'> = {
    'audit': 'info',
    'risk': 'danger',
    'operation': 'success',
    'finance': 'warning',
    'super': 'info'
  }
  return map[roleCode] || 'info'
}

const parsePermissions = (permissions: string): string[] => {
  if (!permissions) return []
  try {
    return JSON.parse(permissions) as string[]
  } catch {
    return []
  }
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
  loadingAdmin.value = true
  try {
    const response = await systemApi.getAdminList({ page: page.value, size: size.value })
    if (response.code === 200) {
      adminList.value = response.data.list || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载管理员数据失败')
  } finally {
    loadingAdmin.value = false
  }
}

const loadRoleList = async () => {
  loadingRoles.value = true
  try {
    const response = await systemApi.getRoleList()
    if (response.code === 200) {
      roleList.value = response.data || []
    }
  } catch (error) {
    ElMessage.error('加载角色数据失败')
  } finally {
    loadingRoles.value = false
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
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">角色权限管理</h2>
        <p class="page-desc">管理系统管理员账号和角色权限配置</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">添加管理员</el-button>
        <el-button :icon="Refresh" @click="loadRoleList" :loading="loadingRoles">刷新角色</el-button>
      </div>
    </div>

    <div class="stats-row">
      <el-row :gutter="24">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon-box stat-icon-primary">
              <User />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ adminList.length }}</div>
              <div class="stat-label">管理员总数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon-box stat-icon-success">
              <Avatar />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ activeAdminCount }}</div>
              <div class="stat-label">启用中</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon-box stat-icon-warning">
              <Lock />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ roleList.length }}</div>
              <div class="stat-label">角色数量</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon-box stat-icon-danger">
              <Key />
            </div>
            <div class="stat-info">
              <div class="stat-value">15</div>
              <div class="stat-label">权限项总数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <List />
          <span>管理员列表</span>
        </div>
        <div class="card-actions">
          <el-button :icon="Refresh" @click="loadAdminList" :loading="loadingAdmin" size="small">刷新</el-button>
        </div>
      </div>
      <el-table :data="adminList" stripe style="width: 100%" class="admin-table" :loading="loadingAdmin">
        <el-table-column prop="admin_id" label="管理员ID" width="180" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="real_name" label="真实姓名" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="role_type" label="角色" width="140">
          <template #default="scope">
            <el-tag :type="roleTagType(scope.row.role_type)" effect="light" size="small">
              {{ roleTypeMap[scope.row.role_type] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status]?.color" effect="light" size="small">
              {{ statusMap[scope.row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)" :icon="Edit">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm
              title="确定删除该管理员吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(scope.row.admin_id)"
            >
              <template #reference>
                <el-button size="small" type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <UserFilled />
          <span>角色权限</span>
        </div>
      </div>
      <el-table :data="roleList" stripe style="width: 100%" class="role-table" :loading="loadingRoles">
        <el-table-column prop="id" label="角色ID" width="120" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="roleCode" label="角色标识" width="140">
          <template #default="scope">
            <el-tag :type="roleCodeTagType(scope.row.roleCode)" effect="light" size="small">
              {{ roleCodeMap[scope.row.roleCode] || scope.row.roleCode }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限数量" width="120">
          <template #default="scope">
            <span class="perm-count">{{ parsePermissions(scope.row.permissions).length || 0 }}</span> 项
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'" effect="light" size="small">
              {{ scope.row.isEnabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="handleRoleDetail(scope.row)" :icon="View">
              查看权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="isEdit ? '编辑管理员' : '添加管理员'" v-model="dialogVisible" width="480px" :close-on-click-modal="false">
      <el-form :model="adminForm" label-position="left" label-width="90px" class="admin-form">
        <el-form-item label="用户名" required>
          <el-input v-model="adminForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" required>
          <el-input v-model="adminForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="adminForm.real_name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="adminForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="adminForm.role_type" style="width: 100%">
            <el-option v-for="(label, value) in roleTypeMap" :key="value" :label="label" :value="Number(value)" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="角色权限详情" v-model="roleDialogVisible" width="520px">
      <div v-if="currentRole" class="role-detail">
        <div class="detail-header">
          <div class="detail-info">
            <h4 class="detail-title">{{ currentRole.roleName }}</h4>
            <el-tag :type="roleCodeTagType(currentRole.roleCode)" effect="light" size="small">
              {{ roleCodeMap[currentRole.roleCode] || currentRole.roleCode }}
            </el-tag>
          </div>
          <div class="perm-badge">
            <span class="perm-num">{{ parsePermissions(currentRole.permissions).length || 0 }}</span>
            <span class="perm-label">项权限</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title">权限列表</div>
          <el-tree 
            :data="permissionTree" 
            show-checkbox
            :default-checked-keys="parsePermissions(currentRole.permissions)"
            node-key="id"
            :props="{ label: 'label', children: 'children' }"
            :disabled="true"
            class="perm-tree"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.roles-container {
  min-height: 100%;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;

  .header-left {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #1D2129;
    margin: 0;
    line-height: 32px;
  }

  .page-desc {
    font-size: 14px;
    color: #86909C;
    margin: 0;
    line-height: 22px;
  }
}

.header-actions {
  display: flex;
  gap: 16px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }
}

.stat-icon-box {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.25s ease;

  &.stat-icon-primary {
    background: linear-gradient(135deg, #E8F3FF 0%, #D0E7FF 100%);
    color: #165DFF;
  }
  &.stat-icon-success {
    background: linear-gradient(135deg, #E8FFEA 0%, #D0FFE0 100%);
    color: #00B42A;
  }
  &.stat-icon-warning {
    background: linear-gradient(135deg, #FFF7E8 0%, #FFEDD0 100%);
    color: #FF7D00;
  }
  &.stat-icon-danger {
    background: linear-gradient(135deg, #FFECE8 0%, #FFD6CF 100%);
    color: #F53F3F;
  }
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.2;
  margin-bottom: 6px;
  transition: all 0.3s ease;
}

.stat-label {
  font-size: 14px;
  color: #86909C;
  line-height: 22px;
}

.card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.25s ease;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #F2F3F5;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.admin-table,
.role-table {
  :deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;
  }

  :deep(.el-table__header-wrapper) {
    background-color: #F7F8FA;
  }

  :deep(.el-table__header th) {
    background-color: #F7F8FA;
    color: #4E5969;
    font-weight: 600;
    font-size: 14px;
    padding: 14px 12px;
    border-bottom: 2px solid #E5E6EB;
  }

  :deep(.el-table__body td) {
    padding: 14px 12px;
    border-bottom: 1px solid #F2F3F5;
    font-size: 14px;
    color: #4E5969;
  }

  :deep(.el-table__row) {
    transition: background-color 0.15s ease;

    &:hover > td {
      background-color: #F7F8FA !important;
    }

    &.el-table__row--striped {
      background-color: #FAFBFC;
    }
  }
}

.perm-count {
  font-weight: 600;
  color: #165DFF;
  font-size: 15px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #F2F3F5;
}

.admin-form {
  padding: 16px 0;
}

.role-detail {
  padding: 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #F7F8FA 0%, #F0F2F5 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.detail-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1D2129;
  margin: 0;
}

.perm-badge {
  text-align: center;
  padding: 8px 20px;
  background: #E8F3FF;
  border-radius: 8px;
}

.perm-num {
  font-size: 28px;
  font-weight: 700;
  color: #165DFF;
}

.perm-label {
  font-size: 12px;
  color: #86909C;
  margin-left: 4px;
}

.detail-section {
  padding: 0;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #4E5969;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #F2F3F5;
}

.perm-tree {
  background: #F7F8FA;
  border-radius: 8px;
  padding: 16px;

  :deep(.el-tree-node__content) {
    height: 36px;
    line-height: 36px;
    font-size: 14px;
    color: #4E5969;
  }

  :deep(.el-tree-node__label) {
    font-size: 14px;
  }

  :deep(.el-checkbox__inner) {
    border-radius: 4px;
  }
}

@media (max-width: 768px) {
  .roles-container {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 24px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-icon-box {
    width: 48px;
    height: 48px;
  }

  .stat-value {
    font-size: 24px;
  }

  .card {
    padding: 16px;
  }
}
</style>