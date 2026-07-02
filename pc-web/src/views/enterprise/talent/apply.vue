<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { talentApi, type ApplyRecord } from '@/api/talent'
import {
  Search,
  User,
  Phone,
  Clock,
  Briefcase,
  View,
  Check,
  Close,
  ChatLineRound
} from '@element-plus/icons-vue'

const tableData = ref<ApplyRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const statusFilter = ref('')
const searchKeyword = ref('')
const previewVisible = ref(false)
const currentStudent = ref<any>(null)

const statusMap: Record<string, { label: string; type: string }> = {
  pending: { label: '待处理', type: 'warning' },
  interview: { label: '面试中', type: 'info' },
  hired: { label: '已录用', type: 'success' },
  rejected: { label: '已拒绝', type: 'danger' }
}

const loadData = () => {
  talentApi.getApplyList({ page: page.value, size: size.value, status: statusFilter.value }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).catch(() => {
    tableData.value = [
      { apply_id: '1', job_name: '初中数学家教', student_name: '张三', student_phone: '138****1234', status: 'pending', apply_time: '2026-06-29 10:30', student_avatar: '', school: '湖南大学', major: '数学与应用数学', grade: '大三', credit_score: 92, skill_tags: ['数学', '英语'] },
      { apply_id: '2', job_name: '超市促销', student_name: '李四', student_phone: '139****5678', status: 'interview', apply_time: '2026-06-28 14:20', student_avatar: '', school: '中南大学', major: '市场营销', grade: '大二', credit_score: 88, skill_tags: ['促销', '沟通'] },
      { apply_id: '3', job_name: '展会协助', student_name: '王五', student_phone: '137****9012', status: 'hired', apply_time: '2026-06-27 09:15', student_avatar: '', school: '湖南师范大学', major: '会展经济与管理', grade: '大四', credit_score: 95, skill_tags: ['展会', '协助'] },
      { apply_id: '4', job_name: '英语助教', student_name: '赵六', student_phone: '136****3456', status: 'rejected', apply_time: '2026-06-26 16:40', student_avatar: '', school: '长沙理工大学', major: '英语', grade: '大三', credit_score: 85, skill_tags: ['英语', '教学'] },
      { apply_id: '5', job_name: '奶茶店店员', student_name: '孙七', student_phone: '135****7890', status: 'pending', apply_time: '2026-06-25 11:25', student_avatar: '', school: '湖南农业大学', major: '食品科学', grade: '大二', credit_score: 80, skill_tags: ['服务', '饮品'] }
    ] as any
    total.value = 5
  })
}

const handleApply = (applyId: string, status: 'interview' | 'hired' | 'rejected') => {
  const actionText = status === 'interview' ? '安排面试' : status === 'hired' ? '录用' : '拒绝'
  ElMessageBox.confirm(`确定要${actionText}该投递吗？`, '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: status === 'rejected' ? 'warning' : 'info'
  }).then(() => {
    talentApi.handleApply({ apply_id: applyId, status }).then(() => {
      ElMessage.success('操作成功')
      loadData()
    })
  })
}

const handlePreview = (row: any) => {
  currentStudent.value = row
  previewVisible.value = true
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const getCreditLevel = (score: number) => {
  if (score >= 90) return { text: '优秀', class: 'high' }
  if (score >= 75) return { text: '良好', class: 'medium' }
  return { text: '一般', class: 'low' }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="apply-list">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">投递管理</h1>
        <p class="page-subtitle">管理收到的所有学生投递申请</p>
      </div>
      <div class="header-stats">
        <div class="stat-item" v-for="(item, key) in [
          { label: '待处理', value: tableData.filter(i => i.status === 'pending').length, type: 'warning' },
          { label: '面试中', value: tableData.filter(i => i.status === 'interview').length, type: 'primary' },
          { label: '已录用', value: tableData.filter(i => i.status === 'hired').length, type: 'success' }
        ]" :key="key">
          <span class="stat-value" :class="item.type">{{ item.value }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索学生姓名/岗位"
            :prefix-icon="Search"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-item">
          <el-select
            v-model="statusFilter"
            placeholder="状态筛选"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option label="全部状态" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="面试中" value="interview" />
            <el-option label="已录用" value="hired" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </div>
        <div class="filter-item flex-1">
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </div>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table :data="tableData" v-loading="false" stripe style="width: 100%">
        <el-table-column label="学生信息" min-width="200">
          <template #default="scope">
            <div class="student-cell">
              <div class="student-avatar">
                {{ scope.row.student_name.charAt(0) }}
              </div>
              <div class="student-info">
                <span class="student-name">{{ scope.row.student_name }}</span>
                <span class="student-phone">
                  <el-icon :size="12"><Phone /></el-icon>
                  {{ scope.row.student_phone }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="job_name" label="投递岗位" min-width="180">
          <template #default="scope">
            <div class="job-cell">
              <el-icon :size="14" color="#165DFF"><Briefcase /></el-icon>
              <span>{{ scope.row.job_name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="apply_time" label="投递时间" width="170">
          <template #default="scope">
            <div class="time-cell">
              <el-icon :size="14" color="#86909C"><Clock /></el-icon>
              <span>{{ scope.row.apply_time }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status].type" effect="light">
              {{ statusMap[scope.row.status].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <div class="action-btns">
              <el-button
                type="primary"
                link
                size="small"
                :icon="View"
                @click="handlePreview(scope.row)"
              >查看</el-button>
              <el-button
                type="primary"
                link
                size="small"
                :icon="ChatLineRound"
                v-if="scope.row.status === 'pending'"
                @click="handleApply(scope.row.apply_id, 'interview')"
              >安排面试</el-button>
              <el-button
                type="success"
                link
                size="small"
                :icon="Check"
                v-if="scope.row.status === 'pending' || scope.row.status === 'interview'"
                @click="handleApply(scope.row.apply_id, 'hired')"
              >录用</el-button>
              <el-button
                type="danger"
                link
                size="small"
                :icon="Close"
                v-if="scope.row.status === 'pending'"
                @click="handleApply(scope.row.apply_id, 'rejected')"
              >拒绝</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next, jumper"
        background
        @current-change="(val: number) => { page = val; loadData() }"
      />
    </div>

    <el-dialog
      v-model="previewVisible"
      title="学生信息预览"
      width="500px"
      :close-on-click-modal="true"
      class="preview-dialog"
    >
      <div v-if="currentStudent" class="student-preview">
        <div class="preview-header">
          <div class="preview-avatar">
            {{ currentStudent.student_name.charAt(0) }}
          </div>
          <div class="preview-basic">
            <div class="preview-name">{{ currentStudent.student_name }}</div>
            <div class="preview-school">
              <el-icon :size="14"><User /></el-icon>
              {{ (currentStudent as any).school || '湖南大学' }} · {{ (currentStudent as any).major || '计算机科学' }}
            </div>
          </div>
          <div class="preview-credit" :class="getCreditLevel((currentStudent as any).credit_score || 90).class">
            <span class="credit-score">{{ (currentStudent as any).credit_score || 90 }}</span>
            <span class="credit-label">信用分</span>
          </div>
        </div>
        <div class="preview-body">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">联系电话</span>
              <span class="info-value">{{ currentStudent.student_phone }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">年级</span>
              <span class="info-value">{{ (currentStudent as any).grade || '大三' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">投递岗位</span>
              <span class="info-value">{{ currentStudent.job_name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">投递时间</span>
              <span class="info-value">{{ currentStudent.apply_time }}</span>
            </div>
          </div>
          <div class="skills-section">
            <span class="section-label">技能标签</span>
            <div class="tags-list">
              <el-tag
                v-for="tag in (currentStudent as any).skill_tags || ['沟通', '执行']"
                :key="tag"
                size="small"
                effect="plain"
                type="info"
              >{{ tag }}</el-tag>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" :icon="Check" v-if="currentStudent?.status === 'pending'" @click="handleApply(currentStudent.apply_id, 'interview')">
          安排面试
        </el-button>
        <el-button type="success" :icon="Check" v-if="currentStudent?.status === 'pending' || currentStudent?.status === 'interview'" @click="handleApply(currentStudent.apply_id, 'hired')">
          直接录用
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.apply-list {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1F2329;
    margin: 0;
  }

  .page-subtitle {
    font-size: 13px;
    color: #86909C;
    margin: 4px 0 0 0;
  }
}

.header-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  text-align: center;

  .stat-value {
    font-size: 20px;
    font-weight: 700;
    line-height: 1.2;

    &.warning { color: #FF7D00; }
    &.primary { color: #165DFF; }
    &.success { color: #00B42A; }
  }

  .stat-label {
    font-size: 12px;
    color: #86909C;
    margin-top: 2px;
  }
}

.filter-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
}

.flex-1 {
  flex: 1;
  justify-content: flex-end;
}

.table-wrapper {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;

  :deep(.el-table) {
    --el-table-header-bg-color: #F7F8FA;
    --el-table-header-text-color: #4E5969;
    --el-table-row-hover-bg-color: #F7F8FA;
    --el-table-border-color: #F2F3F5;
  }

  :deep(.el-table th.el-table__cell) {
    font-weight: 600;
  }
}

.student-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.student-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.student-name {
  font-size: 14px;
  font-weight: 500;
  color: #1F2329;
}

.student-phone {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909C;
}

.job-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4E5969;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4E5969;
}

.action-btns {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  margin-top: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.student-preview {
  .preview-header {
    display: flex;
    align-items: center;
    padding-bottom: 20px;
    margin-bottom: 20px;
    border-bottom: 1px solid #F2F3F5;
  }

  .preview-avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: 600;
    margin-right: 16px;
  }

  .preview-basic {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .preview-name {
    font-size: 18px;
    font-weight: 600;
    color: #1F2329;
  }

  .preview-school {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #86909C;
  }

  .preview-credit {
    text-align: center;
    padding: 8px 16px;
    border-radius: 8px;

    &.high {
      background-color: rgba(0, 180, 42, 0.1);
      .credit-score { color: #00B42A; }
    }
    &.medium {
      background-color: rgba(255, 125, 0, 0.1);
      .credit-score { color: #FF7D00; }
    }
    &.low {
      background-color: rgba(245, 63, 63, 0.1);
      .credit-score { color: #F53F3F; }
    }

    .credit-score {
      font-size: 22px;
      font-weight: 700;
      line-height: 1.2;
    }

    .credit-label {
      font-size: 11px;
      color: #86909C;
      margin-top: 2px;
    }
  }
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .info-label {
    font-size: 12px;
    color: #86909C;
  }

  .info-value {
    font-size: 14px;
    color: #1F2329;
    font-weight: 500;
  }
}

.skills-section {
  .section-label {
    font-size: 12px;
    color: #86909C;
    display: block;
    margin-bottom: 8px;
  }
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.preview-dialog) {
  .el-dialog__body {
    padding-top: 8px;
  }
}
</style>
