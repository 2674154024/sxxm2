<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Clock, Check, Warning, View, Download, Edit } from '@element-plus/icons-vue'

interface Agreement {
  id: string
  name: string
  type: string
  count: number
  status: string
  description: string
  create_time: string
  expire_time: string
  version: string
}

const agreements = ref<Agreement[]>([
  { 
    id: '1', 
    name: '学生兼职协议', 
    type: 'student', 
    count: 15, 
    status: 'active',
    description: '规范学生兼职工作内容、劳动报酬、工作时间等权利义务关系',
    create_time: '2026-01-15',
    expire_time: '2027-01-15',
    version: 'V2.0'
  },
  { 
    id: '2', 
    name: '企业入驻协议', 
    type: 'enterprise', 
    count: 1, 
    status: 'active',
    description: '企业入驻平台的合作协议，约定双方权利义务',
    create_time: '2026-02-01',
    expire_time: '2027-02-01',
    version: 'V1.5'
  },
  { 
    id: '3', 
    name: '保密协议', 
    type: 'secret', 
    count: 8, 
    status: 'active',
    description: '保护企业商业秘密和敏感信息的保密协议',
    create_time: '2026-03-10',
    expire_time: '2027-03-10',
    version: 'V1.2'
  },
  { 
    id: '4', 
    name: '服务协议', 
    type: 'service', 
    count: 20, 
    status: 'expired',
    description: '平台服务使用协议，规范用户使用平台服务的行为',
    create_time: '2025-06-01',
    expire_time: '2026-06-01',
    version: 'V1.0'
  },
  {
    id: '5',
    name: '实习协议',
    type: 'intern',
    count: 12,
    status: 'pending',
    description: '学生实习期间的权利义务约定协议',
    create_time: '2026-06-20',
    expire_time: '2027-06-20',
    version: 'V1.0'
  },
  {
    id: '6',
    name: '劳务派遣协议',
    type: 'dispatch',
    count: 5,
    status: 'active',
    description: '劳务派遣相关的三方协议',
    create_time: '2026-04-15',
    expire_time: '2027-04-15',
    version: 'V1.1'
  }
])

const selectedAgreement = ref<Agreement | null>(null)
const dialogVisible = ref(false)

const stats = computed(() => {
  const list = agreements.value
  const total = list.length
  const pending = list.filter(item => item.status === 'pending').length
  const signed = list.filter(item => item.status === 'active').length
  const expired = list.filter(item => item.status === 'expired').length
  return { total, pending, signed, expired }
})

const statusMap: Record<string, { label: string; class: string }> = {
  pending: { label: '待签署', class: 'warning' },
  active: { label: '已生效', class: 'success' },
  expired: { label: '已过期', class: 'info' }
}

const typeMap: Record<string, string> = {
  student: '学生协议',
  enterprise: '企业协议',
  secret: '保密协议',
  service: '服务协议',
  intern: '实习协议',
  dispatch: '派遣协议'
}

const viewDetail = (agreement: Agreement) => {
  selectedAgreement.value = agreement
  dialogVisible.value = true
}

const downloadAgreement = (agreement: Agreement) => {
  ElMessage.info(`正在下载 ${agreement.name}`)
}
</script>

<template>
  <div class="agreement-page">
    <div class="page-header">
      <div class="header-left">
        <h1>协议管理</h1>
        <p class="subtitle">管理企业各类协议模板和签署记录</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Edit">新建协议</el-button>
      </div>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="card-icon icon-primary">
          <el-icon :size="24"><Document /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">协议总数</div>
          <div class="card-value">{{ stats.total }} 份</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-warning">
          <el-icon :size="24"><Clock /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">待签署</div>
          <div class="card-value text-warning">{{ stats.pending }} 份</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-success">
          <el-icon :size="24"><Check /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">已生效</div>
          <div class="card-value text-success">{{ stats.signed }} 份</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="card-icon icon-danger">
          <el-icon :size="24"><Warning /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">已过期</div>
          <div class="card-value text-danger">{{ stats.expired }} 份</div>
        </div>
      </div>
    </div>

    <div class="agreement-list">
      <div 
        class="agreement-card" 
        v-for="agreement in agreements" 
        :key="agreement.id"
        @click="viewDetail(agreement)"
      >
        <div class="card-header">
          <div class="agreement-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <el-tag :type="statusMap[agreement.status].class" effect="light" round size="small">
            {{ statusMap[agreement.status].label }}
          </el-tag>
        </div>
        <div class="card-body">
          <h3 class="agreement-name">{{ agreement.name }}</h3>
          <p class="agreement-desc">{{ agreement.description }}</p>
          <div class="agreement-meta">
            <span class="meta-item">
              <el-tag size="small" type="info" effect="plain">{{ typeMap[agreement.type] }}</el-tag>
            </span>
            <span class="meta-item version">{{ agreement.version }}</span>
          </div>
        </div>
        <div class="card-footer">
          <div class="stat-info">
            <span class="stat-label">签署数量</span>
            <span class="stat-value">{{ agreement.count }} 份</span>
          </div>
          <div class="card-actions" @click.stop>
            <el-button type="primary" link size="small" @click="viewDetail(agreement)">
              <el-icon style="margin-right: 2px"><View /></el-icon>查看
            </el-button>
            <el-button type="primary" link size="small" @click="downloadAgreement(agreement)">
              <el-icon style="margin-right: 2px"><Download /></el-icon>下载
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="dialogVisible" 
      title="协议详情" 
      width="800px" 
      class="detail-dialog"
    >
      <div class="agreement-detail" v-if="selectedAgreement">
        <div class="detail-header">
          <div class="detail-title">
            <h2>{{ selectedAgreement.name }}</h2>
            <el-tag :type="statusMap[selectedAgreement.status].class" effect="light">
              {{ statusMap[selectedAgreement.status].label }}
            </el-tag>
          </div>
          <div class="detail-info">
            <span>版本号：{{ selectedAgreement.version }}</span>
            <span>创建时间：{{ selectedAgreement.create_time }}</span>
            <span>到期时间：{{ selectedAgreement.expire_time }}</span>
          </div>
        </div>

        <div class="detail-content">
          <div class="content-section">
            <h4>协议说明</h4>
            <p>{{ selectedAgreement.description }}</p>
          </div>

          <div class="content-section">
            <h4>协议内容预览</h4>
            <div class="preview-content">
              <p class="section-title">甲方：企业名称</p>
              <p class="section-title">乙方：学生姓名</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;根据《中华人民共和国民法典》《中华人民共和国劳动合同法》及相关法律法规，甲乙双方本着平等自愿、协商一致、诚实信用的原则，就兼职工作事宜达成如下协议：</p>
              <p class="section-title">一、工作内容</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;1.1 乙方同意按照甲方安排，从事兼职工作，具体工作内容由双方另行约定。</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;1.2 乙方应按照甲方要求，按时、保质、保量完成工作任务。</p>
              <p class="section-title">二、工作时间</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;2.1 工作时间由双方根据实际情况协商确定。</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;2.2 乙方应保证按时到岗，不得迟到早退。</p>
              <p class="section-title">三、劳动报酬</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;3.1 甲方按约定支付乙方劳动报酬，具体标准由双方协商确定。</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;3.2 报酬支付方式：按月结算 / 按次结算。</p>
              <p class="section-title">四、双方权利义务</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;4.1 甲方权利义务：提供必要的工作条件和指导；按时支付劳动报酬等。</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;4.2 乙方权利义务：遵守甲方规章制度；保守甲方商业秘密；认真完成工作任务等。</p>
              <p class="section-title">五、违约责任</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;5.1 任何一方违反本协议约定，应承担相应的违约责任。</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;5.2 因乙方原因给甲方造成损失的，乙方应承担赔偿责任。</p>
              <p class="section-title">六、协议期限</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;6.1 本协议自签订之日起生效，至约定的工作完成之日止。</p>
              <p>&nbsp;&nbsp;&nbsp;&nbsp;6.2 经双方协商一致，可以变更或解除本协议。</p>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" :icon="Download">下载协议</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.agreement-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
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

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);
    }

    .card-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      &.icon-primary {
        background: rgba(22, 93, 255, 0.1);
        color: #165DFF;
      }

      &.icon-success {
        background: rgba(0, 180, 42, 0.1);
        color: #00B42A;
      }

      &.icon-warning {
        background: rgba(255, 125, 0, 0.1);
        color: #FF7D00;
      }

      &.icon-danger {
        background: rgba(245, 63, 63, 0.1);
        color: #F53F3F;
      }
    }

    .card-content {
      flex: 1;

      .card-label {
        font-size: 14px;
        color: #86909C;
        margin-bottom: 8px;
      }

      .card-value {
        font-size: 24px;
        font-weight: 600;
        color: #1F2329;

        &.text-success {
          color: #00B42A;
        }

        &.text-warning {
          color: #FF7D00;
        }

        &.text-danger {
          color: #F53F3F;
        }
      }
    }
  }
}

.agreement-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.agreement-card {
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    transform: translateY(-4px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 20px 16px;

    .agreement-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      background: rgba(22, 93, 255, 0.1);
      color: #165DFF;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .card-body {
    padding: 0 20px 16px;
    flex: 1;

    .agreement-name {
      font-size: 16px;
      font-weight: 600;
      color: #1F2329;
      margin: 0 0 8px 0;
    }

    .agreement-desc {
      font-size: 13px;
      color: #86909C;
      line-height: 1.5;
      margin: 0 0 12px 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .agreement-meta {
      display: flex;
      align-items: center;
      gap: 12px;

      .meta-item {
        font-size: 12px;
        color: #86909C;

        &.version {
          color: #4E5969;
        }
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 20px;
    border-top: 1px solid #F2F3F5;
    background: #FAFBFC;

    .stat-info {
      .stat-label {
        font-size: 12px;
        color: #86909C;
        margin-right: 8px;
      }

      .stat-value {
        font-size: 14px;
        font-weight: 600;
        color: #165DFF;
      }
    }

    .card-actions {
      display: flex;
      gap: 4px;
    }
  }
}

.detail-dialog {
  :deep(.el-dialog__body) {
    padding: 0 24px 24px;
  }
}

.agreement-detail {
  .detail-header {
    padding: 24px 0;
    border-bottom: 1px solid #F2F3F5;
    margin-bottom: 20px;

    .detail-title {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 12px;

      h2 {
        font-size: 20px;
        font-weight: 600;
        color: #1F2329;
        margin: 0;
      }
    }

    .detail-info {
      display: flex;
      gap: 24px;
      font-size: 13px;
      color: #86909C;
    }
  }

  .detail-content {
    .content-section {
      margin-bottom: 24px;

      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #1F2329;
        margin: 0 0 12px 0;
        padding-left: 10px;
        border-left: 3px solid #165DFF;
      }

      p {
        color: #4E5969;
        line-height: 1.8;
        margin: 0 0 8px 0;
      }
    }

    .preview-content {
      padding: 24px;
      background: #F7F8FA;
      border-radius: 8px;
      max-height: 400px;
      overflow-y: auto;

      .section-title {
        font-weight: 600;
        color: #1F2329;
        margin-top: 16px;

        &:first-child {
          margin-top: 0;
        }
      }
    }
  }
}
</style>
