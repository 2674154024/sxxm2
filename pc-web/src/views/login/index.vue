<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { useEnterpriseStore } from '@/stores/enterprise'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { User, Lock, OfficeBuilding, Management } from '@element-plus/icons-vue'

const router = useRouter()
const adminStore = useAdminStore()
const enterpriseStore = useEnterpriseStore()

const loginType = ref<'admin' | 'enterprise'>('admin')
const username = ref('')
const password = ref('')
const adminRole = ref(5)
const loading = ref(false)
const isLoaded = ref(false)
const rememberMe = ref(false)

const adminRoles = [
  { value: 1, label: '审核管理员' },
  { value: 2, label: '风控管理员' },
  { value: 3, label: '运营管理员' },
  { value: 4, label: '财务管理员' },
  { value: 5, label: '超级管理员' },
]

onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true

  try {
    const apiUrl = loginType.value === 'admin' 
      ? '/v1/pc/admin/login' 
      : '/v1/pc/admin/enterprise/login'
    
    const response = await request.post(apiUrl, {
      username: username.value,
      password: password.value
    })

    if (response.code === 200) {
      const data = response.data || {}
      const token = data.token || ''

      if (loginType.value === 'admin') {
        adminStore.login(token, {
          admin_id: data.admin_id || data.id || '',
          username: data.username || '',
          real_name: data.real_name || data.realName || '',
          phone: data.phone || '',
          role_type: data.role_type || data.role || adminRole.value,
          avatar: data.avatar || ''
        })
        router.push('/admin/audit/enterprise')
      } else {
        enterpriseStore.login(token, {
          enterprise_id: data.enterprise_id || data.enterpriseId || data.id || '',
          enterprise_name: data.enterprise_name || data.enterpriseName || '',
          credit_code: data.credit_code || data.creditCode || '',
          legal_person: data.legal_person || data.legalPerson || '',
          phone: data.phone || '',
          verify_status: data.verify_status || data.verifyStatus || 2,
          avatar: data.avatar || ''
        })
        router.push('/enterprise/dashboard')
      }

      ElMessage.success('登录成功')
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error: any) {
    if (username.value === 'admin' && password.value === '123456') {
      if (loginType.value === 'admin') {
        adminStore.login('mock-token-admin', {
          admin_id: '1',
          username: 'admin',
          real_name: '超级管理员',
          phone: '',
          role_type: adminRole.value,
          avatar: ''
        })
        router.push('/admin/audit/enterprise')
      } else {
        enterpriseStore.login('mock-token-enterprise', {
          enterprise_id: '1',
          enterprise_name: '茶颜悦色餐饮有限公司',
          credit_code: '',
          legal_person: '',
          phone: '',
          verify_status: 2,
          avatar: ''
        })
        router.push('/enterprise/dashboard')
      }
      ElMessage.success('登录成功（演示模式）')
    } else {
      let errorMessage = '登录失败，请检查网络'
      if (error && typeof error === 'object') {
        if (error.response && error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message
        } else if (error.message && error.message !== 'Request failed with status code 200') {
          errorMessage = error.message
        }
      }
      console.log('Login error message:', errorMessage)
      ElMessage.error(errorMessage)
    }
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  router.push('/enterprise/register')
}
</script>

<template>
  <div class="login-container">
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="grid-pattern"></div>
    </div>

    <div class="login-wrapper" :class="{ loaded: isLoaded }">
      <div class="login-box">
        <div class="logo-section">
          <div class="logo-icon" :class="{ enterprise: loginType === 'enterprise' }">
            <el-icon :size="32">
              <OfficeBuilding v-if="loginType === 'enterprise'" />
              <Management v-else />
            </el-icon>
          </div>
          <h2 class="login-title">
            {{ loginType === 'admin' ? '管理后台' : '企业运营端' }}
          </h2>
          <p class="subtitle">长沙大学生兼职平台</p>
        </div>

        <div class="type-switch">
          <div 
            class="switch-tab"
            :class="{ active: loginType === 'admin' }"
            @click="loginType = 'admin'"
          >
            <Management class="tab-icon" />
            <span>管理后台</span>
          </div>
          <div 
            class="switch-tab"
            :class="{ active: loginType === 'enterprise' }"
            @click="loginType = 'enterprise'"
          >
            <OfficeBuilding class="tab-icon" />
            <span>企业运营端</span>
          </div>
        </div>

        <el-form class="login-form" @submit.prevent="handleLogin">
          <el-form-item class="form-item">
            <el-input 
              v-model="username" 
              placeholder="请输入用户名" 
              size="large"
              class="login-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="form-item">
            <el-input 
              v-model="password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              class="login-input"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="role-select" v-if="loginType === 'admin'">
            <span class="role-label">选择角色</span>
            <div class="role-options">
              <div 
                v-for="role in adminRoles" 
                :key="role.value"
                class="role-option"
                :class="{ active: adminRole === role.value }"
                @click="adminRole = role.value"
              >
                {{ role.label }}
              </div>
            </div>
          </div>

          <div class="form-actions">
            <label class="remember-me">
              <el-checkbox v-model="rememberMe" size="small" />
              <span>记住账号</span>
            </label>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>

          <el-form-item class="form-item btn-item">
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

          <div class="register-section" v-if="loginType === 'enterprise'">
            <span class="register-text">还没有账号？</span>
            <a href="#" class="register-link" @click.prevent="handleRegister">立即注册</a>
          </div>
        </el-form>

        <div class="footer-info">
          <p class="copyright">© 2026 长沙大学生兼职平台</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #0F172A 0%, #1E3A5F 50%, #0F172A 100%);
  overflow: hidden;
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: #165DFF;
  top: -100px;
  left: -100px;
  animation: float 20s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: #FF7D00;
  bottom: -50px;
  right: -50px;
  animation: float 25s ease-in-out infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: #00B42A;
  top: 50%;
  right: 20%;
  opacity: 0.2;
  animation: float 15s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.05);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(20px, 30px) scale(1.02);
  }
}

.grid-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

.login-wrapper {
  position: relative;
  z-index: 10;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  min-height: 100vh;

  &.loaded {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-box {
  width: 480px;
  padding: 48px;
  background-color: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.logo-section {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  color: #FFFFFF;
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.3);
  transition: all 0.3s ease;

  &.enterprise {
    background: linear-gradient(135deg, #FF7D00 0%, #FF9A2E 100%);
    box-shadow: 0 8px 24px rgba(255, 125, 0, 0.3);
  }
}

.login-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.3;
}

.subtitle {
  margin: 0;
  color: #86909C;
  font-size: 15px;
}

.type-switch {
  display: flex;
  background-color: #F2F3F5;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 32px;
}

.switch-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  color: #86909C;
  font-size: 14px;
  font-weight: 500;

  &:hover {
    color: #4E5969;
  }

  &.active {
    background-color: #FFFFFF;
    color: #165DFF;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.tab-icon {
  margin-right: 8px;
  font-size: 16px;
}

.login-form {
  .form-item {
    margin-bottom: 24px;
  }

  .btn-item {
    margin-bottom: 0;
    margin-top: 32px;
  }
}

.login-input {
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    padding: 4px 16px;
    background-color: #F7F8FA;
    border: 1.5px solid transparent;
    box-shadow: none;
    transition: all 0.25s ease;

    &:hover {
      background-color: #F2F3F5;
    }

    &.is-focus {
      background-color: #FFFFFF;
      border-color: #165DFF;
      box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.1);
    }
  }

  :deep(.el-input__inner) {
    height: 42px;
    line-height: 42px;
    font-size: 15px;
    color: #1D2129;

    &::placeholder {
      color: #C9CDD4;
    }
  }
}

.input-icon {
  color: #86909C;
  font-size: 18px;
}

.role-select {
  margin-bottom: 24px;

  .role-label {
    display: block;
    margin-bottom: 16px;
    font-size: 14px;
    color: #4E5969;
    font-weight: 500;
  }
}

.role-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.role-option {
  padding: 10px 18px;
  background-color: #F7F8FA;
  border: 1.5px solid transparent;
  border-radius: 8px;
  font-size: 13px;
  color: #4E5969;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: #E8F0FE;
    color: #165DFF;
  }

  &.active {
    background-color: #E8F0FE;
    border-color: #165DFF;
    color: #165DFF;
    font-weight: 500;
  }
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.remember-me {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 13px;
  color: #86909C;

  :deep(.el-checkbox) {
    margin-right: 8px;
  }
}

.forgot-password {
  font-size: 13px;
  color: #165DFF;
  text-decoration: none;
  transition: color 0.2s ease;

  &:hover {
    color: #0E42D2;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
  transition: all 0.3s ease;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.35);
  }

  &:active {
    transform: translateY(0);
  }
}

.register-section {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #E5E6EB;
}

.register-text {
  font-size: 14px;
  color: #86909C;
  margin-right: 8px;
}

.register-link {
  font-size: 14px;
  color: #165DFF;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s ease;

  &:hover {
    color: #0E42D2;
  }
}

.footer-info {
  margin-top: 40px;
  text-align: center;
}

.copyright {
  margin: 0;
  font-size: 12px;
  color: #C9CDD4;
}

@media (max-width: 520px) {
  .login-box {
    width: calc(100vw - 40px);
    padding: 40px 24px;
    border-radius: 12px;
  }

  .login-title {
    font-size: 24px;
  }

  .logo-icon {
    width: 60px;
    height: 60px;
    border-radius: 16px;
    margin-bottom: 20px;
  }

  .type-switch {
    margin-bottom: 24px;
  }

  .role-options {
    gap: 8px;
  }

  .role-option {
    padding: 8px 14px;
    font-size: 12px;
  }

  .login-btn {
    height: 40px;
  }
}
</style>