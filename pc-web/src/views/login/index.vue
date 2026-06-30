<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { useEnterpriseStore } from '@/stores/enterprise'
import { authApi } from '@/api/admin'
import { ElForm, ElFormItem, ElInput, ElButton, ElRadioGroup, ElRadio, ElMessage } from 'element-plus'

const router = useRouter()
const adminStore = useAdminStore()
const enterpriseStore = useEnterpriseStore()

const loginType = ref<'admin' | 'enterprise'>('admin')
const username = ref('')
const password = ref('')
const adminRole = ref(5)
const loading = ref(false)

const adminRoles = [
  { value: 1, label: '审核管理员' },
  { value: 2, label: '风控管理员' },
  { value: 3, label: '运营管理员' },
  { value: 4, label: '财务管理员' },
  { value: 5, label: '超级管理员' },
]

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true

  try {
    const response = await authApi.adminLogin({
      username: username.value,
      password: password.value
    })

    if (response.code === 200) {
<<<<<<< HEAD
      const { token, id, username: respUsername, realName, role } = response.data

      if (loginType.value === 'admin') {
        adminStore.login(token, {
          admin_id: id || '',
          username: respUsername || '',
          real_name: realName || '',
          phone: '',
          role_type: role || adminRole.value,
          avatar: ''
=======
      const { token, admin } = response.data

      if (loginType.value === 'admin') {
        adminStore.login(token, {
          admin_id: admin.admin_id || '',
          username: admin.username || '',
          real_name: admin.real_name || '',
          phone: admin.phone || '',
          role_type: admin.role_type || adminRole.value,
          avatar: admin.avatar || ''
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
        })
        router.push('/admin/audit/enterprise')
      } else {
        enterpriseStore.login(token, {
<<<<<<< HEAD
          enterprise_id: id || '',
          enterprise_name: realName || '',
=======
          enterprise_id: admin.enterprise_id || '',
          enterprise_name: admin.enterprise_name || '',
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
          credit_code: '',
          legal_person: '',
          phone: '',
          verify_status: 2,
          avatar: ''
        })
        router.push('/enterprise/dashboard')
      }

      ElMessage.success('登录成功')
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error('登录失败，请检查网络')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="logo-section">
        <div class="logo-icon">
          <Building2 v-if="loginType === 'enterprise'" />
          <Shield v-else />
        </div>
        <h2>{{ loginType === 'admin' ? '管理后台' : '企业运营端' }}登录</h2>
        <p class="subtitle">长沙大学生兼职平台</p>
      </div>

      <div class="type-switch">
        <el-radio-group v-model="loginType">
          <el-radio value="admin">管理后台</el-radio>
          <el-radio value="enterprise">企业运营端</el-radio>
        </el-radio-group>
      </div>

      <el-form class="login-form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input 
            v-model="username" 
            placeholder="用户名" 
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-input 
            v-model="password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item v-if="loginType === 'admin'" class="role-select">
          <span class="role-label">选择角色</span>
          <el-radio-group v-model="adminRole" class="role-group">
            <el-radio 
              v-for="role in adminRoles" 
              :key="role.value" 
              :value="role.value"
            >
              {{ role.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="bottom-links">
        <a href="#" class="link">忘记密码</a>
        <span class="divider">|</span>
        <a href="#" class="link">联系管理员</a>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #1E3A5F 0%, #0F172A 100%);
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(22, 93, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 125, 0, 0.1) 0%, transparent 50%);
}

.login-box {
  width: 420px;
  padding: 48px;
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #165DFF 0%, #0F172A 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #FFFFFF;
  font-size: 28px;
}

.logo-section h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #1E293B;
}

.subtitle {
  margin: 0;
  color: #94A3B8;
  font-size: 14px;
}

.type-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  padding: 4px;
  background-color: #F1F5F9;
  border-radius: 8px;

  :deep(.el-radio-button__inner) {
    border: none;
    background-color: transparent;
    color: #64748B;
    padding: 8px 24px;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover {
      color: #165DFF;
    }

    &.is-active {
      background-color: #FFFFFF;
      color: #165DFF;
      box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
    }
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
  }

  :deep(.el-input__inner) {
    height: 44px;
    line-height: 44px;
  }
}

.role-select {
  .role-label {
    display: block;
    margin-bottom: 12px;
    font-size: 14px;
    color: #475569;
  }
}

.role-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  :deep(.el-radio__label) {
    font-size: 13px;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #165DFF 0%, #0F52BA 100%);
  border: none;
}

.bottom-links {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

.link {
  color: #64748B;
  font-size: 13px;
  text-decoration: none;

  &:hover {
    color: #165DFF;
  }
}

.divider {
  margin: 0 16px;
  color: #E2E8F0;
}
</style>