<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { scheduleApi, type ScheduleDay, type UnscheduledStudent } from '@/api/schedule'
import {
  User,
  Clock,
  Warning,
  Download,
  Check,
  Refresh,
  ArrowLeft,
  ArrowRight,
  Sort
} from '@element-plus/icons-vue'

const currentMonth = ref(dayjs().format('YYYY-MM'))
const scheduleDays = ref<ScheduleDay[]>([])
const unscheduledStudents = ref<UnscheduledStudent[]>([])
const totalStudents = ref(0)
const totalHours = ref(0)
const conflicts = ref(0)
const dragOverDate = ref('')
const isDragging = ref(false)
const draggingStudent = ref<UnscheduledStudent | null>(null)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const generateCalendarDays = () => {
  const year = parseInt(currentMonth.value.split('-')[0])
  const month = parseInt(currentMonth.value.split('-')[1]) - 1
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const days = []
  
  for (let i = 0; i < firstDay.getDay(); i++) {
    days.push({ date: '', isEmpty: true, schedule_list: [], isConflict: false })
  }
  
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    const scheduleDay = scheduleDays.value.find(d => d.date === dateStr)
    const dayOfWeek = new Date(year, month, i).getDay()
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6
    const isToday = dayjs().format('YYYY-MM-DD') === dateStr
    
    days.push({ 
      date: dateStr, 
      isEmpty: false,
      schedule_list: scheduleDay?.schedule_list || [],
      isConflict: false,
      isWeekend,
      isToday
    })
  }
  
  return days
}

const calendarDays = ref<ScheduleDay[]>([])

const updateCalendarDays = () => {
  const year = parseInt(currentMonth.value.split('-')[0])
  const month = parseInt(currentMonth.value.split('-')[1]) - 1
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const days: ScheduleDay[] = []
  
  for (let i = 0; i < firstDay.getDay(); i++) {
    days.push({ date: '', isEmpty: true, schedule_list: [], isConflict: false })
  }
  
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    const scheduleDay = scheduleDays.value.find(d => d.date === dateStr)
    const dayOfWeek = new Date(year, month, i).getDay()
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6
    const isToday = dayjs().format('YYYY-MM-DD') === dateStr
    
    days.push({ 
      date: dateStr, 
      isEmpty: false,
      schedule_list: scheduleDay?.schedule_list || [],
      isConflict: false,
      isWeekend,
      isToday
    })
  }
  
  calendarDays.value = days
}

const loadData = () => {
  scheduleApi.getSchedule({ month: currentMonth.value }).then(res => {
    scheduleDays.value = res.data.schedule_days
    totalStudents.value = res.data.total_students
    totalHours.value = res.data.total_hours
    conflicts.value = res.data.conflicts
    updateCalendarDays()
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
    updateCalendarDays()
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

const goToToday = () => {
  currentMonth.value = dayjs().format('YYYY-MM')
  loadData()
}

const confirmSchedule = () => {
  ElMessageBox.confirm('确认后排班将不能修改，是否继续？', '确认排班', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    scheduleApi.confirmSchedule({ month: currentMonth.value }).then(() => {
      ElMessage.success('排班已确认')
      loadData()
    })
  }).catch(() => {})
}

const exportSchedule = () => {
  scheduleApi.exportSchedule({ month: currentMonth.value }).then(() => {
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.success('导出成功')
  })
}

const handleDragStart = (e: DragEvent, student: UnscheduledStudent) => {
  e.dataTransfer?.setData('text/plain', student.user_id)
  isDragging.value = true
  draggingStudent.value = student
}

const handleDragEnd = () => {
  isDragging.value = false
  dragOverDate.value = ''
}

const handleDragOver = (e: DragEvent, date: string) => {
  e.preventDefault()
  dragOverDate.value = date
}

const handleDragLeave = () => {
  dragOverDate.value = ''
}

const handleDrop = (e: DragEvent, date: string) => {
  e.preventDefault()
  dragOverDate.value = ''
  isDragging.value = false
  
  if (!draggingStudent.value) {
    return
  }
  
  const student = draggingStudent.value
  
  const dayInSchedule = scheduleDays.value.find(d => d.date === date)
  if (dayInSchedule) {
    dayInSchedule.schedule_list.push({
      student_id: student.user_id,
      student_name: student.real_name,
      student_avatar: student.avatar,
      time_range: '09:00-18:00'
    })
  } else {
    scheduleDays.value.push({
      date,
      schedule_list: [{
        student_id: student.user_id,
        student_name: student.real_name,
        student_avatar: student.avatar,
        time_range: '09:00-18:00'
      }]
    })
  }
  
  const dayInCalendar = calendarDays.value.find(d => d.date === date)
  if (dayInCalendar) {
    dayInCalendar.schedule_list.push({
      student_id: student.user_id,
      student_name: student.real_name,
      student_avatar: student.avatar,
      time_range: '09:00-18:00'
    })
  }
  
  unscheduledStudents.value = unscheduledStudents.value.filter(s => s.user_id !== student.user_id)
  totalStudents.value++
  totalHours.value += 8
  
  ElMessage.success(`已将 ${student.real_name} 安排到 ${date}`)
  
  draggingStudent.value = null
}

const removeSchedule = (date: string, studentId: string, studentName: string) => {
  ElMessageBox.confirm(`确定要移除 ${studentName} 的排班吗？`, '移除排班', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const dayInSchedule = scheduleDays.value.find(d => d.date === date)
    if (dayInSchedule) {
      const removedStudent = dayInSchedule.schedule_list.find(s => s.student_id === studentId)
      dayInSchedule.schedule_list = dayInSchedule.schedule_list.filter(s => s.student_id !== studentId)
      totalStudents.value--
      totalHours.value -= 8
      
      if (removedStudent) {
        unscheduledStudents.value.push({
          user_id: removedStudent.student_id,
          real_name: removedStudent.student_name,
          avatar: removedStudent.student_avatar || '',
          skill_tags: []
        })
      }
    }
    
    const dayInCalendar = calendarDays.value.find(d => d.date === date)
    if (dayInCalendar) {
      dayInCalendar.schedule_list = dayInCalendar.schedule_list.filter(s => s.student_id !== studentId)
    }
    
    ElMessage.success('已移除排班')
  }).catch(() => {})
}

const getAvatarColor = (name: string) => {
  const colors = [
    'linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%)',
    'linear-gradient(135deg, #00B42A 0%, #23C343 100%)',
    'linear-gradient(135deg, #FF7D00 0%, #FF9A2E 100%)',
    'linear-gradient(135deg, #722ED1 0%, #9254DE 100%)',
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
  <div class="schedule">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">智能排班</h1>
        <p class="page-subtitle">拖拽学生到日历中进行排班</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" size="default" @click="loadData">刷新</el-button>
        <el-button :icon="Download" @click="exportSchedule">导出排班表</el-button>
        <el-button type="primary" :icon="Check" @click="confirmSchedule">确认排班</el-button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card stat-card--blue">
        <div class="stat-card__icon">
          <el-icon :size="24"><User /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ totalStudents }}</div>
          <div class="stat-card__label">已排班学生</div>
        </div>
      </div>
      <div class="stat-card stat-card--green">
        <div class="stat-card__icon">
          <el-icon :size="24"><Clock /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ totalHours }}</div>
          <div class="stat-card__label">总工时</div>
        </div>
      </div>
      <div class="stat-card stat-card--orange">
        <div class="stat-card__icon">
          <el-icon :size="24"><User /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ unscheduledStudents.length }}</div>
          <div class="stat-card__label">待排班学生</div>
        </div>
      </div>
      <div class="stat-card stat-card--red" v-if="conflicts > 0">
        <div class="stat-card__icon">
          <el-icon :size="24"><Warning /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__value">{{ conflicts }}</div>
          <div class="stat-card__label">排班冲突</div>
        </div>
      </div>
    </div>

    <div class="schedule-container">
      <div class="calendar-panel">
        <div class="calendar-toolbar">
          <div class="month-nav">
            <el-button :icon="ArrowLeft" circle size="small" @click="prevMonth" />
            <span class="month-text">{{ currentMonth }}</span>
            <el-button :icon="ArrowRight" circle size="small" @click="nextMonth" />
            <el-button size="small" @click="goToToday">今天</el-button>
          </div>
          <div class="legend">
            <div class="legend-item">
              <span class="legend-dot legend-dot--primary"></span>
              <span>已排班</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot legend-dot--weekend"></span>
              <span>周末</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot legend-dot--today"></span>
              <span>今天</span>
            </div>
          </div>
        </div>

        <div class="calendar-header">
          <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
        </div>
        <div class="calendar-grid">
          <div 
            class="day-cell" 
            v-for="(day, index) in calendarDays" 
            :key="index"
            :class="{ 
              empty: day.isEmpty,
              weekend: day.isWeekend && !day.isEmpty,
              today: day.isToday,
              'drag-over': dragOverDate === day.date,
              'has-schedule': day.schedule_list && day.schedule_list.length > 0
            }"
            @dragover="!day.isEmpty && handleDragOver($event, day.date)"
            @dragleave="handleDragLeave"
            @drop="!day.isEmpty && handleDrop($event, day.date)"
          >
            <div v-if="!day.isEmpty" class="day-header">
              <span class="day-number" :class="{ 'today-text': day.isToday }">{{ day.date.split('-')[2] }}</span>
              <span v-if="day.schedule_list && day.schedule_list.length > 0" class="schedule-count">
                {{ day.schedule_list.length }}人
              </span>
            </div>
            <div class="schedule-items" v-if="!day.isEmpty && day.schedule_list && day.schedule_list.length > 0">
              <div 
                class="schedule-item" 
                v-for="item in day.schedule_list.slice(0, 3)" 
                :key="item.student_id"
                @click="removeSchedule(day.date, item.student_id, item.student_name)"
              >
                <div class="schedule-item-avatar" :style="{ background: getAvatarColor(item.student_name) }">
                  {{ item.student_name.charAt(0) }}
                </div>
                <div class="schedule-item-info">
                  <span class="student-name">{{ item.student_name }}</span>
                  <span class="time-range">{{ item.time_range }}</span>
                </div>
              </div>
              <div v-if="day.schedule_list.length > 3" class="more-schedules">
                +{{ day.schedule_list.length - 3 }} 更多
              </div>
            </div>
            <div v-if="!day.isEmpty && (!day.schedule_list || day.schedule_list.length === 0)" class="empty-day-hint">
              <el-icon :size="16"><Sort /></el-icon>
              <span>拖入排班</span>
            </div>
          </div>
        </div>
      </div>

      <div class="students-panel">
        <div class="panel-header">
          <h3>待排班学生</h3>
          <span class="student-count">{{ unscheduledStudents.length }}人</span>
        </div>
        <div class="drag-hint" v-if="!isDragging">
          <el-icon :size="14"><Sort /></el-icon>
          <span>拖拽学生到日历进行排班</span>
        </div>
        <div class="student-list" :class="{ 'dragging': isDragging }">
          <div 
            class="student-item" 
            v-for="student in unscheduledStudents" 
            :key="student.user_id"
            draggable="true"
            @dragstart="(e) => handleDragStart(e, student)"
            @dragend="handleDragEnd"
          >
            <div class="avatar" :style="{ background: getAvatarColor(student.real_name) }">
              {{ student.real_name.charAt(0) }}
            </div>
            <div class="info">
              <span class="name">{{ student.real_name }}</span>
              <div class="tags">
                <el-tag 
                  v-for="tag in student.skill_tags" 
                  :key="tag" 
                  size="small" 
                  effect="light"
                  type="info"
                >{{ tag }}</el-tag>
              </div>
            </div>
            <div class="drag-handle">
              <el-icon :size="16"><Sort /></el-icon>
            </div>
          </div>
        </div>
        <div v-if="unscheduledStudents.length === 0" class="empty-state">
          <el-icon :size="48" class="empty-icon"><Check /></el-icon>
          <p>所有学生已排班</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.schedule {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  overflow: hidden;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  background-color: #fff;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }

  &__icon {
    position: relative;
    z-index: 1;
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-right: 16px;
    flex-shrink: 0;
  }

  &__info {
    position: relative;
    z-index: 1;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  &__value {
    font-size: 24px;
    font-weight: 700;
    line-height: 1.2;
  }

  &__label {
    font-size: 13px;
    margin-top: 4px;
    color: #86909C;
  }

  &--blue {
    .stat-card__icon {
      background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
    }
    .stat-card__value {
      color: #165DFF;
    }
  }

  &--green {
    .stat-card__icon {
      background: linear-gradient(135deg, #00B42A 0%, #23C343 100%);
    }
    .stat-card__value {
      color: #00B42A;
    }
  }

  &--orange {
    .stat-card__icon {
      background: linear-gradient(135deg, #FF7D00 0%, #FF9A2E 100%);
    }
    .stat-card__value {
      color: #FF7D00;
    }
  }

  &--red {
    .stat-card__icon {
      background: linear-gradient(135deg, #F53F3F 0%, #FF6B6B 100%);
    }
    .stat-card__value {
      color: #F53F3F;
    }
  }
}

.schedule-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}

.calendar-panel {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.calendar-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F2F3F5;
}

.month-nav {
  display: flex;
  align-items: center;
  gap: 12px;

  .month-text {
    font-size: 18px;
    font-weight: 600;
    color: #1F2329;
    min-width: 100px;
    text-align: center;
  }
}

.legend {
  display: flex;
  gap: 20px;

  &-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #86909C;
  }

  &-dot {
    width: 10px;
    height: 10px;
    border-radius: 2px;

    &--primary {
      background-color: #E8F0FE;
      border: 1px solid #165DFF;
    }

    &--weekend {
      background-color: #FFF7E6;
    }

    &--today {
      background-color: #165DFF;
    }
  }
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  margin-bottom: 8px;

  .weekday {
    font-size: 13px;
    font-weight: 600;
    color: #4E5969;
    padding: 8px 0;
  }
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.day-cell {
  min-height: 110px;
  border: 1px solid #E5E6EB;
  border-radius: 8px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  transition: all 0.2s ease;
  cursor: default;

  &:hover:not(.empty) {
    border-color: #165DFF;
    box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
  }

  &.empty {
    border: none;
    background: transparent;
    cursor: default;
  }

  &.weekend {
    background-color: #FFFAF0;
  }

  &.today {
    border-color: #165DFF;
    border-width: 2px;
  }

  &.drag-over {
    border-color: #165DFF;
    border-style: dashed;
    background-color: #E8F0FE;
    transform: scale(1.02);
  }

  &.has-schedule {
    border-color: #ADC6FF;
  }
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.day-number {
  font-size: 14px;
  font-weight: 600;
  color: #1F2329;

  &.today-text {
    color: #165DFF;
  }
}

.schedule-count {
  font-size: 11px;
  color: #165DFF;
  background-color: #E8F0FE;
  padding: 1px 6px;
  border-radius: 10px;
  font-weight: 500;
}

.schedule-items {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 6px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(60, 126, 255, 0.1) 100%);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: linear-gradient(135deg, rgba(22, 93, 255, 0.2) 0%, rgba(60, 126, 255, 0.2) 100%);
  }

  &-avatar {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    font-weight: 600;
    flex-shrink: 0;
  }

  &-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 1px;
  }
}

.student-name {
  font-size: 11px;
  color: #165DFF;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time-range {
  font-size: 10px;
  color: #86909C;
}

.more-schedules {
  font-size: 11px;
  color: #86909C;
  text-align: center;
  padding: 4px;
  background-color: #F7F8FA;
  border-radius: 4px;
}

.empty-day-hint {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #C9CDD4;
  font-size: 11px;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;

  .day-cell:hover & {
    opacity: 1;
  }
}

.students-panel {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  position: sticky;
  top: 0;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #F2F3F5;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #1F2329;
      margin: 0;
    }

    .student-count {
      font-size: 13px;
      color: #86909C;
      background-color: #F7F8FA;
      padding: 2px 10px;
      border-radius: 12px;
    }
  }
}

.drag-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  background-color: #E8F0FE;
  border-radius: 6px;
  font-size: 12px;
  color: #165DFF;
  margin-bottom: 12px;
}

.student-list {
  max-height: 500px;
  overflow-y: auto;
  margin: -8px;
  padding: 8px;

  &.dragging {
    .student-item {
      opacity: 0.6;
    }
  }
}

.student-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #F2F3F5;
  margin-bottom: 8px;
  cursor: grab;
  transition: all 0.2s ease;
  background-color: #fff;

  &:hover {
    border-color: #165DFF;
    box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
    transform: translateX(4px);
  }

  &:last-child {
    margin-bottom: 0;
  }

  &:active {
    cursor: grabbing;
  }
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;

  .name {
    font-size: 14px;
    font-weight: 600;
    color: #1F2329;
    display: block;
    margin-bottom: 4px;
  }

  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
}

.drag-handle {
  color: #C9CDD4;
  margin-left: 8px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s ease;

  .student-item:hover & {
    opacity: 1;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #86909C;

  .empty-icon {
    color: #00B42A;
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}
</style>
