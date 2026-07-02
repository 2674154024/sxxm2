<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Setting, 
  Lock, 
  Bell, 
  Upload, 
  Camera, 
  Phone,
  Postcard,
  OfficeBuilding
} from '@element-plus/icons-vue'
import { useEnterpriseStore } from '@/stores/enterprise'

const store = useEnterpriseStore()
const activeTab = ref('enterprise')
const avatarUrl = ref('')
const passwordForm = reactive({
  old_password: '',
  new_password: '',
  confirm_password: ''
})
const securityForm = reactive({
  phone: store.enterprise?.phone || '',
  email: '',
  two_factor: false
})

const form = reactive({
  enterprise_name: store.enterprise?.enterprise_name || '长沙科技有限公司',
  phone: store.enterprise?.phone || '13800138000',
  credit_code: store.enterprise?.credit_code || '91430100MA4Q123456',
  legal_person: store.enterprise?.legal_person || '张三',
  address: '湖南省长沙市岳麓区麓谷大道658号',
  description: '长沙科技有限公司是一家专注于大学生兼职服务的互联网企业，致力于为大学生提供优质的兼职岗位，为企业提供灵活的用工解决方案。',
  industry: '互联网/信息技术',
  scale: '50-100人',
  website: 'https://www.example.com',
  contact_name: '李经理',
  contact_position: '人力资源经理'
})

const notifyMessage = ref(true)
const notifyEmail = ref(true)
const notifyWechat = ref(false)

const saveEnterprise = () => {
  ElMessage.success('企业信息保存成功')
}

const saveAccount = () => {
  ElMessage.success('账号设置保存成功')
}

const updatePassword = () => {
  if (!passwordForm.old_password) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.new_password) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.new_password.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  if (passwordForm.new_password !== passwordForm.confirm_password) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  ElMessage.success('密码修改成功')
  passwordForm.old_password = ''
  passwordForm.new_password = ''
  passwordForm.confirm_password = ''
}

const saveSecurity = () => {
  ElMessage.success('安全设置保存成功')
}

const saveNotify = () => {
  ElMessage.success('通知设置保存成功')
}

const handleAvatarChange = (file: any) => {
  const isImage = file.raw.type.startsWith('image/')
  const isLt2M = file.raw.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    avatarUrl.value = e.target?.result as string
    ElMessage.success('头像上传成功')
  }
  reader.readAsDataURL(file.raw)
  return false
}

const beforeUpload = (file: any) => {
  handleAvatarChange(file)
  return false
}
</script>

<template>
  <div class="settings-page">
    <div class="page-header">
      <div class="header-left">
        <h1>设置</h1>
        <p class="subtitle">管理企业信息、账号安全和系统设置</p>
      </div>
    </div>

    <div class="settings-container">
      <div class="settings-sidebar">
        <div class="user-card">
          <div class="avatar-wrapper">
            <el-avatar :size="64" src="" icon="User" />
          </div>
          <div class="user-info">
            <div class="user-name">{{ form.enterprise_name }}</div>
            <div class="user-role">企业管理员</div>
          </div>
        </div>
        <el-menu
          :default-active="activeTab"
          class="settings-menu"
          @select="(key: string) => activeTab = key"
        >
          <el-menu-item index="enterprise">
            <el-icon><OfficeBuilding /></el-icon>
            <span>企业信息</span>
          </el-menu-item>
          <el-menu-item index="account">
            <el-icon><User /></el-icon>
            <span>账号设置</span>
          </el-menu-item>
          <el-menu-item index="security">
            <el-icon><Lock /></el-icon>
            <span>安全设置</span>
          </el-menu-item>
          <el-menu-item index="notification">
            <el-icon><Bell /></el-icon>
            <span>通知设置</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="settings-content">
        <div class="content-card" v-show="activeTab === 'enterprise'">
          <div class="card-header">
            <h3>企业信息</h3>
            <p>完善企业基本信息，提升企业可信度</p>
          </div>
          <div class="card-body">
            <div class="avatar-section">
              <div class="avatar-upload">
                <div class="avatar-preview">
                  <el-avatar :size="100" :src="avatarUrl" icon="User" />
                  <div class="avatar-mask">
                    <el-icon :size="20"><Camera /></el-icon>
                    <span>更换头像</span>
                  </div>
                </div>
                <el-upload
                  class="upload-btn"
                  :show-file-list="false"
                  :before-upload="beforeUpload"
                  accept="image/*"
                >
                  <el-button type="primary" :icon="Upload" size="small">上传Logo</el-button>
                </el-upload>
                <p class="upload-tip">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
              </div>
            </div>

            <el-form :model="form" label-width="120px" class="settings-form">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="企业名称">
                    <el-input v-model="form.enterprise_name" placeholder="请输入企业名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="统一信用代码">
                    <el-input v-model="form.credit_code" disabled placeholder="统一社会信用代码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="法人代表">
                    <el-input v-model="form.legal_person" placeholder="请输入法人姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属行业">
                    <el-input v-model="form.industry" placeholder="请输入所属行业" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="企业规模">
                    <el-select v-model="form.scale" placeholder="请选择企业规模" style="width: 100%">
                      <el-option label="1-50人" value="1-50人" />
                      <el-option label="50-100人" value="50-100人" />
                      <el-option label="100-500人" value="100-500人" />
                      <el-option label="500人以上" value="500人以上" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="企业官网">
                    <el-input v-model="form.website" placeholder="请输入企业官网" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="联系电话">
                    <el-input v-model="form.phone" placeholder="请输入联系电话" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系人">
                    <el-input v-model="form.contact_name" placeholder="请输入联系人姓名" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="联系人职位">
                    <el-input v-model="form.contact_position" placeholder="请输入联系人职位" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="企业地址">
                <el-input v-model="form.address" placeholder="请输入企业地址" />
              </el-form-item>
              <el-form-item label="企业简介">
                <el-input 
                  v-model="form.description" 
                  type="textarea" 
                  :rows="4" 
                  placeholder="请输入企业简介，介绍企业的业务范围、企业文化等"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
          </div>
          <div class="card-footer">
            <el-button type="primary" @click="saveEnterprise">保存信息</el-button>
            <el-button @click="() => {}">取消</el-button>
          </div>
        </div>

        <div class="content-card" v-show="activeTab === 'account'">
          <div class="card-header">
            <h3>账号设置</h3>
            <p>管理您的登录账号和个人信息</p>
          </div>
          <div class="card-body">
            <div class="form-section">
              <h4 class="section-title">基本信息</h4>
              <el-form label-width="120px" class="settings-form">
                <el-form-item label="管理员账号">
                  <el-input value="admin@company.com" disabled />
                </el-form-item>
                <el-form-item label="昵称">
                  <el-input v-model="form.contact_name" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="form.phone" placeholder="请输入手机号" />
                </el-form-item>
              </el-form>
            </div>

            <div class="form-section">
              <h4 class="section-title">修改密码</h4>
              <el-form :model="passwordForm" label-width="120px" class="settings-form">
                <el-form-item label="原密码">
                  <el-input 
                    v-model="passwordForm.old_password" 
                    type="password" 
                    placeholder="请输入原密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input 
                    v-model="passwordForm.new_password" 
                    type="password" 
                    placeholder="请输入新密码（至少6位）"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认新密码">
                  <el-input 
                    v-model="passwordForm.confirm_password" 
                    type="password" 
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updatePassword">确认修改</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="primary" @click="saveAccount">保存设置</el-button>
          </div>
        </div>

        <div class="content-card" v-show="activeTab === 'security'">
          <div class="card-header">
            <h3>安全设置</h3>
            <p>保护您的账号安全，建议定期更新密码</p>
          </div>
          <div class="card-body">
            <div class="security-list">
              <div class="security-item">
                <div class="item-icon icon-password">
                  <el-icon :size="20"><Lock /></el-icon>
                </div>
                <div class="item-content">
                  <div class="item-title">登录密码</div>
                  <div class="item-desc">已设置，建议定期更换密码以保障账号安全</div>
                </div>
                <div class="item-action">
                  <el-button type="primary" link @click="activeTab = 'account'">修改</el-button>
                </div>
              </div>

              <div class="security-item">
                <div class="item-icon icon-phone">
                  <el-icon :size="20"><Phone /></el-icon>
                </div>
                <div class="item-content">
                  <div class="item-title">绑定手机</div>
                  <div class="item-desc">已绑定：{{ securityForm.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }}</div>
                </div>
                <div class="item-action">
                  <el-button type="primary" link>更换</el-button>
                </div>
              </div>

              <div class="security-item">
                <div class="item-icon icon-email">
                  <el-icon :size="20"><Postcard /></el-icon>
                </div>
                <div class="item-content">
                  <div class="item-title">绑定邮箱</div>
                  <div class="item-desc">未绑定，绑定邮箱可用于找回密码和接收通知</div>
                </div>
                <div class="item-action">
                  <el-button type="primary" link>去绑定</el-button>
                </div>
              </div>

              <div class="security-item">
                <div class="item-icon icon-2fa">
                  <el-icon :size="20"><Setting /></el-icon>
                </div>
                <div class="item-content">
                  <div class="item-title">两步验证</div>
                  <div class="item-desc">开启后登录时需要额外的验证码，提高账号安全性</div>
                </div>
                <div class="item-action">
                  <el-switch v-model="securityForm.two_factor" @change="saveSecurity" />
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4 class="section-title">登录记录</h4>
              <div class="login-record">
                <el-table :data="[
                  { time: '2026-06-30 10:30:00', ip: '192.168.1.100', location: '湖南省长沙市', device: 'Chrome / Windows' },
                  { time: '2026-06-29 15:20:00', ip: '192.168.1.100', location: '湖南省长沙市', device: 'Chrome / Windows' },
                  { time: '2026-06-28 09:15:00', ip: '192.168.1.100', location: '湖南省长沙市', device: 'Chrome / Windows' }
                ]" style="width: 100%">
                  <el-table-column prop="time" label="登录时间" width="180" />
                  <el-table-column prop="ip" label="IP地址" width="150" />
                  <el-table-column prop="location" label="登录地点" />
                  <el-table-column prop="device" label="设备信息" />
                </el-table>
              </div>
            </div>
          </div>
        </div>

        <div class="content-card" v-show="activeTab === 'notification'">
          <div class="card-header">
            <h3>通知设置</h3>
            <p>管理系统通知的接收方式和频率</p>
          </div>
          <div class="card-body">
            <div class="notify-list">
              <div class="notify-item">
                <div class="notify-icon icon-message">
                  <el-icon :size="20"><Bell /></el-icon>
                </div>
                <div class="notify-content">
                  <div class="notify-title">站内消息通知</div>
                  <div class="notify-desc">接收订单状态、系统公告等站内消息</div>
                </div>
                <div class="notify-action">
                  <el-switch v-model="notifyMessage" />
                </div>
              </div>

              <div class="notify-item">
                <div class="notify-icon icon-email">
                  <el-icon :size="20"><Postcard /></el-icon>
                </div>
                <div class="notify-content">
                  <div class="notify-title">邮件通知</div>
                  <div class="notify-desc">重要通知将通过邮件发送到您的邮箱</div>
                </div>
                <div class="notify-action">
                  <el-switch v-model="notifyEmail" />
                </div>
              </div>

              <div class="notify-item">
                <div class="notify-icon icon-wechat">
                  <el-icon :size="20"><Phone /></el-icon>
                </div>
                <div class="notify-content">
                  <div class="notify-title">微信通知</div>
                  <div class="notify-desc">绑定微信后可接收微信消息通知</div>
                </div>
                <div class="notify-action">
                  <el-switch v-model="notifyWechat" />
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4 class="section-title">通知类型</h4>
              <el-form label-width="160px" class="settings-form">
                <el-form-item label="订单状态变更">
                  <el-checkbox v-model="notifyMessage">站内信</el-checkbox>
                  <el-checkbox v-model="notifyEmail">邮件</el-checkbox>
                </el-form-item>
                <el-form-item label="新申请通知">
                  <el-checkbox v-model="notifyMessage">站内信</el-checkbox>
                  <el-checkbox v-model="notifyEmail">邮件</el-checkbox>
                </el-form-item>
                <el-form-item label="账单结算通知">
                  <el-checkbox v-model="notifyMessage">站内信</el-checkbox>
                  <el-checkbox v-model="notifyEmail">邮件</el-checkbox>
                </el-form-item>
                <el-form-item label="系统公告">
                  <el-checkbox v-model="notifyMessage">站内信</el-checkbox>
                  <el-checkbox v-model="notifyEmail">邮件</el-checkbox>
                </el-form-item>
              </el-form>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="primary" @click="saveNotify">保存设置</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.settings-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;

  .header-left {
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #1F2329;
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: #86909C;
      margin: 0;
    }
  }
}

.settings-container {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.settings-sidebar {
  width: 240px;
  flex-shrink: 0;

  .user-card {
    background: #FFFFFF;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    padding: 24px;
    margin-bottom: 16px;
    text-align: center;

    .avatar-wrapper {
      margin-bottom: 12px;
      display: flex;
      justify-content: center;
    }

    .user-info {
      .user-name {
        font-size: 16px;
        font-weight: 600;
        color: #1F2329;
        margin-bottom: 4px;
      }

      .user-role {
        font-size: 13px;
        color: #86909C;
      }
    }
  }

  .settings-menu {
    background: #FFFFFF;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border-right: none;

    :deep(.el-menu-item) {
      height: 48px;
      line-height: 48px;
      margin: 0;
      border-radius: 0;

      &.is-active {
        background: rgba(22, 93, 255, 0.08);
        color: #165DFF;
      }

      &:hover {
        background: rgba(22, 93, 255, 0.04);
      }
    }
  }
}

.settings-content {
  flex: 1;
  min-width: 0;
}

.content-card {
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;

  .card-header {
    padding: 24px 32px;
    border-bottom: 1px solid #F2F3F5;

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #1F2329;
      margin: 0 0 8px 0;
    }

    p {
      font-size: 14px;
      color: #86909C;
      margin: 0;
    }
  }

  .card-body {
    padding: 32px;
  }

  .card-footer {
    padding: 16px 32px;
    border-top: 1px solid #F2F3F5;
    background: #FAFBFC;
    text-align: right;
  }
}

.avatar-section {
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #F2F3F5;

  .avatar-upload {
    display: flex;
    align-items: center;
    gap: 24px;

    .avatar-preview {
      position: relative;
      cursor: pointer;

      :deep(.el-avatar) {
        border: 2px solid #F2F3F5;
      }

      .avatar-mask {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 32px;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 0 0 50px 50px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 2px;
        opacity: 0;
        transition: opacity 0.3s;
        color: #FFFFFF;
        font-size: 12px;
      }

      &:hover .avatar-mask {
        opacity: 1;
      }
    }

    .upload-btn {
      .el-button {
        margin-bottom: 8px;
      }
    }

    .upload-tip {
      font-size: 12px;
      color: #86909C;
      margin: 0;
    }
  }
}

.settings-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #4E5969;
  }
}

.form-section {
  margin-top: 32px;

  &:first-child {
    margin-top: 0;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1F2329;
    margin: 0 0 20px 0;
    padding-left: 10px;
    border-left: 3px solid #165DFF;
  }
}

.security-list {
  .security-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 0;
    border-bottom: 1px solid #F2F3F5;

    &:last-child {
      border-bottom: none;
    }

    .item-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      &.icon-password {
        background: rgba(22, 93, 255, 0.1);
        color: #165DFF;
      }

      &.icon-phone {
        background: rgba(0, 180, 42, 0.1);
        color: #00B42A;
      }

      &.icon-email {
        background: rgba(255, 125, 0, 0.1);
        color: #FF7D00;
      }

      &.icon-2fa {
        background: rgba(134, 144, 156, 0.1);
        color: #4E5969;
      }
    }

    .item-content {
      flex: 1;

      .item-title {
        font-size: 15px;
        font-weight: 500;
        color: #1F2329;
        margin-bottom: 4px;
      }

      .item-desc {
        font-size: 13px;
        color: #86909C;
      }
    }

    .item-action {
      flex-shrink: 0;
    }
  }
}

.login-record {
  margin-top: 16px;
}

.notify-list {
  .notify-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 0;
    border-bottom: 1px solid #F2F3F5;

    &:last-child {
      border-bottom: none;
    }

    .notify-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      &.icon-message {
        background: rgba(22, 93, 255, 0.1);
        color: #165DFF;
      }

      &.icon-email {
        background: rgba(0, 180, 42, 0.1);
        color: #00B42A;
      }

      &.icon-wechat {
        background: rgba(0, 180, 42, 0.1);
        color: #00B42A;
      }
    }

    .notify-content {
      flex: 1;

      .notify-title {
        font-size: 15px;
        font-weight: 500;
        color: #1F2329;
        margin-bottom: 4px;
      }

      .notify-desc {
        font-size: 13px;
        color: #86909C;
      }
    }

    .notify-action {
      flex-shrink: 0;
    }
  }
}
</style>
