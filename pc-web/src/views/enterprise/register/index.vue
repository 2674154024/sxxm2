<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton, ElCheckbox, type FormInstance } from 'element-plus'
import { OfficeBuilding, User, Phone, CreditCard, Message, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { authApi } from '@/api/admin'

const router = useRouter()

const isLoaded = ref(false)
const loading = ref(false)
const agreed = ref(false)
const formRef = ref<FormInstance>();

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  enterpriseName: '',
  creditCode: '',
  legalPerson: '',
  contactName: '',
  contactPhone: '',
  email: '',
  address: ''
})

const rules: Record<string, any> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度在4到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_rule: any, value: string, callback: any) => {
      if (value !== form.value.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  enterpriseName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' },
    { min: 2, max: 50, message: '企业名称长度在2到50个字符', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { len: 18, message: '统一社会信用代码必须是18位', trigger: 'blur' },
    { pattern: /^[0-9a-zA-Z]{18}$/, message: '统一社会信用代码格式不正确', trigger: 'blur' }
  ],
  legalPerson: [
    { required: true, message: '请输入法定代表人', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})

const handleSubmit = async () => {
  if (!agreed.value) {
    ElMessage.warning('请阅读并同意用户协议和隐私政策')
    return
  }

  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()
  } catch (error) {
    ElMessage.warning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const response = await authApi.enterpriseRegister({
      username: form.value.username,
      password: form.value.password,
      enterpriseName: form.value.enterpriseName,
      creditCode: form.value.creditCode,
      legalPerson: form.value.legalPerson,
      contactName: form.value.contactName,
      contactPhone: form.value.contactPhone,
      email: form.value.email,
      address: form.value.address
    })

    if (response.code === 200) {
      ElMessage.success('注册成功，请等待审核')
      setTimeout(() => {
        router.push('/')
      }, 1500)
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/')
}
</script>

<template>
  <div class="register-container">
    <div class="register-wrapper" :class="{ loaded: isLoaded }">
      <div class="register-box">
        <div class="header-section">
          <button class="back-btn" @click="handleBack">
            <el-icon :size="18"><ArrowLeft /></el-icon>
            <span>返回登录</span>
          </button>
          <div class="logo-icon">
            <el-icon :size="32">
              <OfficeBuilding />
            </el-icon>
          </div>
          <h2 class="register-title">企业注册</h2>
          <p class="subtitle">填写企业信息，开启合作之旅</p>
        </div>

        <el-form 
          ref="formRef"
          class="register-form" 
          :model="form" 
          :rules="rules"
          label-width="120px"
          @submit.prevent="handleSubmit"
        >
          <div class="form-section">
            <h3 class="section-title">登录信息</h3>
            <div class="section-divider"></div>
            
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入用户名" 
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="请输入密码" 
                show-password
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="form.confirmPassword" 
                type="password" 
                placeholder="请再次输入密码" 
                show-password
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <div class="form-section">
            <h3 class="section-title">企业基本信息</h3>
            <div class="section-divider"></div>

            <el-form-item label="企业名称" prop="enterpriseName">
              <el-input 
                v-model="form.enterpriseName" 
                placeholder="请输入企业全称" 
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><OfficeBuilding /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input 
                v-model="form.creditCode" 
                placeholder="请输入18位信用代码" 
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><CreditCard /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="法定代表人" prop="legalPerson">
              <el-input 
                v-model="form.legalPerson" 
                placeholder="请输入法定代表人姓名" 
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <div class="form-section">
            <h3 class="section-title">联系人信息</h3>
            <div class="section-divider"></div>

            <el-form-item label="联系人姓名" prop="contactName">
              <el-input 
                v-model="form.contactName" 
                placeholder="请输入联系人姓名" 
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="联系电话" prop="contactPhone">
              <el-input 
                v-model="form.contactPhone" 
                placeholder="请输入手机号码" 
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="电子邮箱" prop="email">
              <el-input 
                v-model="form.email" 
                placeholder="请输入邮箱地址" 
                class="register-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="企业地址">
              <el-input 
                v-model="form.address" 
                placeholder="请输入企业地址（选填）" 
                class="register-input"
              >
              </el-input>
            </el-form-item>
          </div>

          <div class="agreement-section">
            <ElCheckbox v-model="agreed">
              <span>我已阅读并同意</span>
              <a href="#" class="agreement-link">《用户协议》</a>
              <span>和</span>
              <a href="#" class="agreement-link">《隐私政策》</a>
            </ElCheckbox>
          </div>

          <el-form-item class="submit-item">
            <el-button 
              type="primary" 
              size="large" 
              class="submit-btn"
              :loading="loading"
              @click="handleSubmit"
            >
              提交注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="footer-info">
          <p class="tips">注册后需等待平台审核，审核通过后即可登录使用</p>
          <p class="copyright">© 2026 长沙大学生兼职平台</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.register-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  width: 100%;
  padding: 48px 24px;
  background-color: #F2F3F5;
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.register-wrapper {
  position: relative;
  z-index: 10;
  opacity: 0;
  transform: translateY(20px);
  transition: all 300ms cubic-bezier(0.16, 1, 0.3, 1);
  width: 100%;
  max-width: 620px;

  &.loaded {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-box {
  width: 100%;
  padding: 32px 48px;
  background-color: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  max-height: calc(100vh - 96px);
  overflow-y: auto;
}

.header-section {
  text-align: center;
  margin-bottom: 32px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  color: #86909C;
  font-size: 14px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color 200ms ease;
  margin-bottom: 16px;

  &:hover {
    color: #4E5969;
  }
}

.logo-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: #FFFFFF;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.25);
  transition: transform 200ms ease;

  &:hover {
    transform: scale(1.05);
  }
}

.register-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #4E5969;
  line-height: 32px;
}

.subtitle {
  margin: 0;
  color: #86909C;
  font-size: 14px;
}

.register-form {
  margin-bottom: 16px;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 500;
  color: #4E5969;
}

.section-divider {
  height: 2px;
  background: linear-gradient(90deg, #165DFF 0%, transparent 100%);
  margin-bottom: 20px;
}

.register-input {
  :deep(.el-input__wrapper) {
    border-radius: 6px;
    padding: 0 16px;
    background-color: #F7F8FA;
    border: 1px solid #E5E6EB;
    box-shadow: none;
    transition: all 200ms ease;

    &:hover {
      border-color: #C9CDD4;
    }

    &.is-focus {
      background-color: #FFFFFF;
      border-color: #165DFF;
      box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.1);
    }

    &.is-error {
      border-color: #F53F3F;
      background-color: #FFF1F0;
    }
  }

  :deep(.el-input__inner) {
    height: 36px;
    line-height: 36px;
    font-size: 14px;
    color: #4E5969;

    &::placeholder {
      color: #C9CDD4;
    }
  }

  :deep(.el-input__prefix) {
    margin-right: 8px;
  }
}

.input-icon {
  color: #86909C;
  font-size: 16px;
}

.agreement-section {
  margin-bottom: 20px;
  padding: 0 2px;

  :deep(.el-checkbox) {
    font-size: 13px;
    color: #86909C;
  }

  :deep(.el-checkbox__inner) {
    border-radius: 4px;
  }

  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: #165DFF;
    border-color: #165DFF;
  }
}

.agreement-link {
  color: #165DFF;
  text-decoration: none;

  &:hover {
    color: #0E42D2;
    text-decoration: underline;
  }
}

.submit-item {
  margin-bottom: 0;
}

.submit-btn {
  width: 100%;
  height: 36px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  background-color: #165DFF;
  border: none;
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.2);
  transition: all 100ms cubic-bezier(0.16, 1, 0.3, 1);
  letter-spacing: 1px;

  &:hover {
    background-color: #3C7EFF;
    box-shadow: 0 6px 16px rgba(22, 93, 255, 0.3);
  }

  &:active {
    transform: scale(0.96);
    background-color: #0E42D2;
  }

  &:disabled {
    background-color: #C9CDD4;
    box-shadow: none;
    transform: none;
  }
}

.footer-info {
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #E5E6EB;
}

.tips {
  margin: 0 0 8px;
  font-size: 12px;
  color: #86909C;
}

.copyright {
  margin: 0;
  font-size: 12px;
  color: #C9CDD4;
}

@media (max-width: 768px) {
  .register-container {
    padding: 24px 16px;
  }

  .register-box {
    padding: 24px;
    border-radius: 8px;
  }

  .register-title {
    font-size: 20px;
    line-height: 28px;
  }

  .logo-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    margin-bottom: 16px;
  }

  .section-divider {
    margin-bottom: 16px;
  }

  .form-section {
    margin-bottom: 20px;
  }

  .submit-btn {
    height: 44px;
    font-size: 16px;
    border-radius: 12px;
  }

  .register-input :deep(.el-input__inner) {
    height: 44px;
    line-height: 44px;
  }
}
</style>
