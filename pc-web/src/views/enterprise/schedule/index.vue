<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { scheduleApi, type ScheduleDay, type UnscheduledStudent } from '@/api/schedule'

const currentMonth = ref(dayjs().format('YYYY-MM'))
const scheduleDays = ref<ScheduleDay[]>([])
const unscheduledStudents = ref<UnscheduledStudent[]>([])
const totalStudents = ref(0)
const totalHours = ref(0)
const conflicts = ref(0)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const generateCalendarDays = () => {
  const year = parseInt(currentMonth.value.split('-')[0])
  const month = parseInt(currentMonth.value.split('-')[1]) - 1
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const days = []
  
  for (let i = 0; i < firstDay.getDay(); i++) {
    days.push({ date: '', isEmpty: true })
  }
  
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    const scheduleDay = scheduleDays.value.find(d => d.date === dateStr)
    days.push({ 
      date: dateStr, 
      isEmpty: false,
      schedule_list: scheduleDay?.schedule_list || [],
      isConflict: false
    })
  }
  
  return days
}

const calendarDays = computed(() => generateCalendarDays())

const loadData = () => {
  scheduleApi.getSchedule({ month: currentMonth.value }).then(res => {
    scheduleDays.value = res.data.schedule_days
    totalStudents.value = res.data.total_students
    totalHours.value = res.data.total_hours
    conflicts.value = res.data.conflicts
  }).catch(() => {
    scheduleDays.value = [
      { date: currentMonth.value + '-15', schedule_list: [{ student_id: '1', student_name: '张三', student_avatar: '', time_range: '09:00-12:00' }] },
      { date: currentMonth.value + '-16', schedule_list: [{ student_id: '2', student_name: '李四', student_avatar: '', time_range: '14:00-18:00' }] },
      { date: currentMonth.value + '-17', schedule_list: [{ student_id: '1', student_name: '张三', student_avatar: '', time_range: '09:00-12:00' }, { student_id: '3', student_name: '王五', student_avatar: '', time_range: '14:00-18:00' }] },
      { date: currentMonth.value + '-18', schedule_list: [{ student_id: '4', student_name: '赵六', student_avatar: '', time_range: '10:00-16:00' }] },
      { date: currentMonth.value + '-19', schedule_list: [{ student_id: '5', student_name: '孙七', student_avatar: '', time_range: '09:00-17:00' }] }
    ]
    totalStudents.value = 5
    totalHours.value = 42
    conflicts.value = 1
  })

  scheduleApi.getUnscheduledStudents('1').then(res => {
    unscheduledStudents.value = res.data
  }).catch(() => {
    unscheduledStudents.value = [
      { user_id: '6', real_name: '周八', avatar: '', skill_tags: ['销售', '沟通'] },
      { user_id: '7', real_name: '吴九', avatar: '', skill_tags: ['客服', '耐心'] },
      { user_id: '8', real_name: '郑十', avatar: '', skill_tags: ['会展', '礼仪'] }
    ]
  })
}

const prevMonth = () => {
  currentMonth.value = dayjs(currentMonth.value).subtract(1, 'month').format('YYYY-MM')
  loadData()
}

const nextMonth = () => {
  currentMonth.value = dayjs(currentMonth.value).add(1, 'month').format('YYYY-MM')
  loadData()
}

const confirmSchedule = () => {
  scheduleApi.confirmSchedule({ month: currentMonth.value }).then(() => {
    ElMessage.success('排班已确认')
  })
}

const exportSchedule = () => {
  scheduleApi.exportSchedule({ month: currentMonth.value }).then(() => {
    ElMessage.success('导出成功')
  })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="schedule">
    <div class="page-header">
      <h1>智能排班</h1>
      <div class="header-actions">
        <el-button @click="prevMonth">‹</el-button>
        <span style="margin: 0 12px; font-size: 16px; font-weight: bold;">{{ currentMonth }}</span>
        <el-button @click="nextMonth">›</el-button>
      </div>
    </div>

    <div class="stats-bar">
      <div class="stat">已排班学生: <span class="value">{{ totalStudents }}</span></div>
      <div class="stat">总工时: <span class="value">{{ totalHours }}小时</span></div>
      <div class="stat" v-if="conflicts > 0">冲突: <span class="value danger">{{ conflicts }}处</span></div>
    </div>

    <div class="schedule-container">
      <div class="calendar-panel">
        <div class="calendar-header">
          <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
        </div>
        <div class="calendar-grid">
          <div 
            class="day-cell" 
            v-for="(day, index) in calendarDays" 
            :key="index"
            :class="{ empty: day.isEmpty }"
          >
            <div class="day-number" v-if="!day.isEmpty">{{ day.date.split('-')[2] }}</div>
            <div class="schedule-items" v-if="!day.isEmpty && day.schedule_list && day.schedule_list.length > 0">
              <div 
                class="schedule-item" 
                v-for="item in day.schedule_list" 
                :key="item.student_id"
              >
                <span class="student-name">{{ item.student_name }}</span>
                <span class="time-range">{{ item.time_range }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="students-panel">
        <h3>待排班学生</h3>
        <div class="student-list">
          <div 
            class="student-item" 
            v-for="student in unscheduledStudents" 
            :key="student.user_id"
          >
            <div class="avatar">{{ student.real_name.charAt(0) }}</div>
            <div class="info">
              <span class="name">{{ student.real_name }}</span>
              <div class="tags">
                <el-tag v-for="tag in student.skill_tags" :key="tag" size="small">{{ tag }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-actions">
      <el-button @click="confirmSchedule">确认排班</el-button>
      <el-button type="primary" @click="exportSchedule">导出排班表</el-button>
    </div>
  </div>
</template>

<style scoped>
.schedule {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h1 {
    font-size: 20px;
    font-weight: bold;
    color: #1F2329;
  }
}

.header-actions {
  display: flex;
  align-items: center;
}

.stats-bar {
  display: flex;
  gap: 32px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background-color: #FFFFFF;
  border-radius: 8px;
}

.stat {
  font-size: 14px;
  color: #4E5969;
}

.value {
  font-weight: bold;
  color: #1F2329;

  &.danger {
    color: #FF4D4F;
  }
}

.schedule-container {
  display: grid;
  grid-template-columns: 3fr 1fr;
  gap: 20px;
}

.calendar-panel {
  background-color: #FFFFFF;
  border-radius: 12px;
  padding: 20px;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  margin-bottom: 16px;
  font-weight: bold;
  color: #4E5969;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.day-cell {
  aspect-ratio: 1;
  border: 1px solid #F2F3F5;
  border-radius: 8px;
  padding: 8px;
  display: flex;
  flex-direction: column;

  &.empty {
    border: none;
  }
}

.day-number {
  font-size: 14px;
  color: #1F2329;
  margin-bottom: 4px;
}

.schedule-items {
  flex: 1;
  overflow: hidden;
}

.schedule-item {
  font-size: 11px;
  padding: 2px 4px;
  background-color: rgba(22, 93, 255, 0.1);
  border-radius: 4px;
  margin-bottom: 2px;
}

.student-name {
  display: block;
  color: #165DFF;
}

.time-range {
  color: #86909C;
}

.students-panel {
  background-color: #FFFFFF;
  border-radius: 12px;
  padding: 20px;

  h3 {
    font-size: 16px;
    font-weight: bold;
    color: #1F2329;
    margin-bottom: 16px;
  }
}

.student-list {
  max-height: 400px;
  overflow-y: auto;
}

.student-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #F2F3F5;

  &:last-child {
    border-bottom: none;
  }
}

.avatar {
  width: 40px;
  height: 40px;
  background-color: #165DFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 14px;
  margin-right: 12px;
}

.info {
  flex: 1;
}

.name {
  font-size: 14px;
  font-weight: bold;
  color: #1F2329;
  display: block;
  margin-bottom: 4px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style>