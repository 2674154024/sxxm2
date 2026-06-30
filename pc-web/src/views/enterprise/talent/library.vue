<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { talentApi, type Student } from '@/api/talent'

const tableData = ref<Student[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const skillTag = ref('')
const creditMin = ref(0)
const creditMax = ref(100)
const cooperateCount = ref(0)

const loadData = () => {
  talentApi.getTalentLibrary({
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
      { user_id: '1', real_name: '张三', phone: '138****1234', school_name: '湖南大学', skill_tags: ['数学', '英语'], credit_score: 92, cooperate_count: 5, avatar: '' },
      { user_id: '2', real_name: '李四', phone: '139****5678', school_name: '中南大学', skill_tags: ['促销', '沟通'], credit_score: 88, cooperate_count: 3, avatar: '' },
      { user_id: '3', real_name: '王五', phone: '137****9012', school_name: '湖南师范大学', skill_tags: ['展会', '协助'], credit_score: 95, cooperate_count: 8, avatar: '' },
      { user_id: '4', real_name: '赵六', phone: '136****3456', school_name: '长沙理工大学', skill_tags: ['英语', '教学'], credit_score: 85, cooperate_count: 2, avatar: '' },
      { user_id: '5', real_name: '孙七', phone: '135****7890', school_name: '湖南农业大学', skill_tags: ['服务', '饮品'], credit_score: 80, cooperate_count: 4, avatar: '' }
    ]
    total.value = 5
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

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="talent-library">
    <div class="page-header">
      <h1>人才库</h1>
    </div>

    <div class="search-bar">
      <el-input v-model="skillTag" placeholder="技能标签" style="width: 150px; margin-right: 12px;" />
      <span>信用分：</span>
      <el-input-number v-model="creditMin" :min="0" :max="100" style="width: 100px; margin-right: 8px;" />
      <span>-</span>
      <el-input-number v-model="creditMax" :min="0" :max="100" style="width: 100px; margin-left: 8px; margin-right: 12px;" />
      <el-select v-model="cooperateCount" placeholder="合作次数" style="width: 120px;">
        <el-option label="不限" :value="0" />
        <el-option label="1次以上" :value="1" />
        <el-option label="3次以上" :value="3" />
        <el-option label="5次以上" :value="5" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <div class="talent-cards">
      <div class="talent-card" v-for="student in tableData" :key="student.user_id">
        <div class="card-header">
          <div class="avatar">{{ student.real_name.charAt(0) }}</div>
          <div class="info">
            <span class="name">{{ student.real_name }}</span>
            <span class="school">{{ student.school_name }}</span>
          </div>
          <div class="credit" :class="{ high: student.credit_score >= 80, medium: student.credit_score >= 60 && student.credit_score < 80, low: student.credit_score < 60 }">
            {{ student.credit_score }}
          </div>
        </div>
        <div class="skills">
          <el-tag v-for="tag in student.skill_tags" :key="tag" size="small">{{ tag }}</el-tag>
        </div>
        <div class="footer">
          <span>合作{{ student.cooperate_count }}次</span>
          <div class="actions">
            <el-button size="small" @click="handleInvite(student.user_id)">邀请</el-button>
            <el-button size="small" @click="handleViewResume(student.user_id)">查看简历</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-pagination
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="(val: number) => { page = val; loadData() }"
      style="margin-top: 20px; text-align: right;"
    />
  </div>
</template>

<style scoped>
.talent-library {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;

  h1 {
    font-size: 20px;
    font-weight: bold;
    color: #1F2329;
  }
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background-color: #FFFFFF;
  border-radius: 8px;
}

.talent-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.talent-card {
  background-color: #FFFFFF;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  background-color: #165DFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-weight: bold;
  margin-right: 12px;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.name {
  font-size: 16px;
  font-weight: bold;
  color: #1F2329;
}

.school {
  font-size: 13px;
  color: #86909C;
}

.credit {
  font-size: 20px;
  font-weight: bold;
  padding: 4px 12px;
  border-radius: 16px;

  &.high { color: #52C41A; background-color: rgba(82, 196, 26, 0.1); }
  &.medium { color: #FAAD14; background-color: rgba(250, 173, 20, 0.1); }
  &.low { color: #FF4D4F; background-color: rgba(255, 77, 79, 0.1); }
}

.skills {
  margin-bottom: 12px;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #F2F3F5;
  font-size: 13px;
  color: #86909C;
}

.actions {
  display: flex;
  gap: 8px;
}
</style>