<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useEnterpriseStore } from '@/stores/enterprise'

const store = useEnterpriseStore()
const form = reactive({
  enterprise_name: store.enterprise?.enterprise_name || '',
  phone: store.enterprise?.phone || '',
  credit_code: store.enterprise?.credit_code || '',
  legal_person: store.enterprise?.legal_person || '',
  address: '',
  description: ''
})

const notifyMessage = ref(true)
const notifyEmail = ref(true)

const saveSettings = () => {
  ElMessage.success('设置已保存')
}

const updatePassword = () => {
  ElMessage.info('修改密码功能开发中')
}
</script>

<template>
  <div class="settings">
    <div class="page-header">
      <h1>设置</h1>
      <el-button type="primary" @click="saveSettings">保存设置</el-button>
    </div>

    <el-form label-width="120px" style="max-width: 600px;">
      <h3 class="form-section">企业信息</h3>
      <el-form-item label="企业名称">
        <el-input v-model="form.enterprise_name" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="信用代码">
        <el-input v-model="form.credit_code" disabled />
      </el-form-item>
      <el-form-item label="法人姓名">
        <el-input v-model="form.legal_person" />
      </el-form-item>
      <el-form-item label="企业地址">
        <el-input v-model="form.address" />
      </el-form-item>
      <el-form-item label="企业简介">
        <el-input type="textarea" v-model="form.description" :rows="4" />
      </el-form-item>

      <h3 class="form-section">安全设置</h3>
      <el-form-item label="修改密码">
        <el-button type="text" @click="updatePassword">点击修改密码</el-button>
      </el-form-item>

      <h3 class="form-section">系统设置</h3>
      <el-form-item label="消息通知">
        <el-switch v-model="notifyMessage" />
      </el-form-item>
      <el-form-item label="邮件通知">
        <el-switch v-model="notifyEmail" />
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.settings {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h1 {
    font-size: 20px;
    font-weight: bold;
    color: #1F2329;
  }
}

.form-section {
  font-size: 16px;
  font-weight: bold;
  color: #1F2329;
  margin: 32px 0 16px;
  padding-left: 8px;
  border-left: 4px solid #165DFF;
}
</style>