<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { talentApi, type Student } from '@/api/talent'
import {
  Search,
  School,
  Star,
  User,
  View,
  Message,
  Refresh
} from '@element-plus/icons-vue'

const tableData = ref<Student[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const skillTag = ref('')
const creditMin = ref(0)
const creditMax = ref(100)
const cooperateCount = ref(0)
const searchKeyword = ref('')

const loadData = () => {
  talentApi.getTalentLibrary({
    keyword: searchKeyword.value,
    skill_tag: skillTag.value,
    credit_min: creditMin.value,
    credit_max: creditMax.value,
    cooperate_count: cooperateCount.value,
    page: page.value,
    size: size.value
  }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).catch(() => {
    tableData.value = [
      { user_id: '1', real_name: '张三', phone: '138****1234', school_name: '湖南大学', skill_tags: ['数学', '英语'], credit_score: 92, cooperate_count: 5, avatar: '', major: '数学与应用数学', grade: '大三' },
      { user_id: '2', real_name: '李四', phone: '139****5678', school_name: '中南大学', skill_tags: ['促销', '沟通'], credit_score: 88, cooperate_count: 3, avatar: '', major: '市场营销', grade: '大二' },
      { user_id: '3', real_name: '王五', phone: '137****9012', school_name: '湖南师范大学', skill_tags: ['展会', '协助'], credit_score: 95, cooperate_count: 8, avatar: '', major: '会展经济与管理', grade: '大四' },
      { user_id: '4', real_name: '赵六', phone: '136****3456', school_name: '长沙理工大学', skill_tags: ['英语', '教学'], credit_score: 85, cooperate_count: 2, avatar: '', major: '英语', grade: '大三' },
      { user_id: '5', real_name: '孙七', phone: '135****7890', school_name: '湖南农业大学', skill_tags: ['服务', '饮品'], credit_score: 80, cooperate_count: 4, avatar: '', major: '食品科学', grade: '大二' },
      { user_id: '6', real_name: '周八', phone: '134****1111', school_name: '湖南中医药大学', skill_tags: ['护理', '健康'], credit_score: 90, cooperate_count: 6, avatar: '', major: '护理学', grade: '大三' }
    ] as any
    total.value = 36
  })
}

const handleInvite = (studentId: string) => {
  talentApi.inviteStudent({ student_id: studentId, job_id: '1' }).then(() => {
    ElMessage.success('邀请已发送')
  })
}

const handleViewResume = (studentId: string) => {
  talentApi.getStudentResume(studentId).then(() => {
    ElMessage.info('查看简历')
  })
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleReset = () => {
  searchKeyword.value = ''
  skillTag.value = ''
  creditMin.value = 0
  creditMax.value = 100
  cooperateCount.value = 0
  page.value = 1
  loadData()
}

const getCreditLevel = (score: number) => {
  if (score >= 90) return { text: '优秀', class: 'high' }
  if (score >= 75) return { text: '良好', class: 'medium' }
  return { text: '一般', class: 'low' }
}

const getAvatarColor = (name: string) => {
  const colors = [
    'linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%)',
    'linear-gradient(135deg, #722ED1 0%, #9254DE 100%)',
    'linear-gradient(135deg, #FF7D00 0%, #FF9A2E 100%)',
    'linear-gradient(135deg, #00B42A 0%, #23C343 100%)',
    'linear-gradient(135deg, #F53F3F 0%, #FF6B6B 100%)'
  ]
  const index = name.charCodeAt(0) % colors.length
  return colors[index]
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="talent-library">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">人才库</h1>
        <p class="page-subtitle">发现优秀人才，快速邀约面试</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-value primary">{{ total }}</span>
          <span class="stat-label">人才总数</span>
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索姓名/学校"
            :prefix-icon="Search"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-item">
          <el-input
            v-model="skillTag"
            placeholder="技能标签"
            clearable
            style="width: 140px"
          />
        </div>
        <div class="filter-item">
          <span class="filter-label">信用分</span>
          <el-input-number
            v-model="creditMin"
            :min="0"
            :max="100"
            size="default"
            controls-position="right"
            style="width: 90px"
          />
          <span class="filter-sep">-</span>
          <el-input-number
            v-model="creditMax"
            :min="0"
            :max="100"
            size="default"
            controls-position="right"
            style="width: 90px"
          />
        </div>
        <div class="filter-item">
          <el-select
            v-model="cooperateCount"
            placeholder="合作次数"
            clearable
            style="width: 130px"
          >
            <el-option label="不限" :value="0" />
            <el-option label="1次以上" :value="1" />
            <el-option label="3次以上" :value="3" />
            <el-option label="5次以上" :value="5" />
          </el-select>
        </div>
        <div class="filter-item flex-1">
          <div class="filter-actions">
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="talent-cards">
      <div class="talent-card" v-for="student in tableData" :key="student.user_id">
        <div class="talent-card__header">
          <div class="talent-avatar" :style="{ background: getAvatarColor(student.real_name) }">
            {{ student.real_name.charAt(0) }}
          </div>
          <div class="talent-basic">
            <div class="talent-name">{{ student.real_name }}</div>
            <div class="talent-school">
              <el-icon :size="12"><School /></el-icon>
              <span>{{ student.school_name }}</span>
            </div>
          </div>
          <div class="talent-credit" :class="getCreditLevel(student.credit_score).class">
            <div class="credit-score">{{ student.credit_score }}</div>
            <div class="credit-label">{{ getCreditLevel(student.credit_score).text }}</div>
          </div>
        </div>
        <div class="talent-card__body">
          <div class="info-row">
            <div class="info-item">
              <el-icon :size="12" color="#86909C"><User /></el-icon>
              <span>{{ (student as any).major || '计算机科学' }} · {{ (student as any).grade || '大三' }}</span>
            </div>
          </div>
          <div class="skills-section">
            <el-tag
              v-for="tag in student.skill_tags"
              :key="tag"
              size="small"
              effect="plain"
              type="info"
            >{{ tag }}</el-tag>
          </div>
        </div>
        <div class="talent-card__footer">
          <div class="footer-info">
            <div class="cooperate-info">
              <el-icon :size="12" color="#FF7D00"><Star /></el-icon>
              <span>合作 {{ student.cooperate_count }} 次</span>
            </div>
          </div>
          <div class="footer-actions">
            <el-button
              size="small"
              type="primary"
              plain
              :icon="View"
              @click="handleViewResume(student.user_id)"
            >查看简历</el-button>
            <el-button
              size="small"
              type="primary"
              :icon="Message"
              @click="handleInvite(student.user_id)"
            >邀请面试</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next, jumper"
        background
        :page-sizes="[12, 24, 36, 48]"
        @current-change="(val: number) => { page.value = val; loadData() }"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.talent-library {
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

    &.primary { color: #165DFF; }
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
  margin-bottom: 20px;
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
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: #4E5969;
  white-space: nowrap;
}

.filter-sep {
  color: #86909C;
}

.flex-1 {
  flex: 1;
  justify-content: flex-end;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.talent-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.talent-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }

  &__header {
    display: flex;
    align-items: center;
    padding: 16px;
    padding-bottom: 12px;
  }

  &__body {
    padding: 0 16px 12px;
    flex: 1;
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: #F7F8FA;
    border-top: 1px solid #F2F3F5;
  }
}

.talent-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}

.talent-basic {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.talent-name {
  font-size: 16px;
  font-weight: 600;
  color: #1F2329;
}

.talent-school {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909C;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.talent-credit {
  text-align: center;
  padding: 6px 12px;
  border-radius: 8px;
  flex-shrink: 0;

  &.high {
    background-color: rgba(0, 180, 42, 0.1);
    .credit-score { color: #00B42A; }
    .credit-label { color: #00B42A; }
  }
  &.medium {
    background-color: rgba(255, 125, 0, 0.1);
    .credit-score { color: #FF7D00; }
    .credit-label { color: #FF7D00; }
  }
  &.low {
    background-color: rgba(245, 63, 63, 0.1);
    .credit-score { color: #F53F3F; }
    .credit-label { color: #F53F3F; }
  }

  .credit-score {
    font-size: 18px;
    font-weight: 700;
    line-height: 1.2;
  }

  .credit-label {
    font-size: 11px;
    margin-top: 2px;
  }
}

.info-row {
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909C;
}

.skills-section {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.footer-info {
  .cooperate-info {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #86909C;
  }
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>
