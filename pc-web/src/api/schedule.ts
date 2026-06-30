import request from './request'

export interface ScheduleStudent {
  user_id: string
  real_name: string
  avatar: string
}

export interface ScheduleDay {
  date: string
  schedule_list: {
    student_id: string
    student_name: string
    student_avatar: string
    time_range: string
  }[]
}

export interface ScheduleResponse {
  code: number
  message: string
  data: {
    month: string
    schedule_days: ScheduleDay[]
    total_students: number
    total_hours: number
    conflicts: number
  }
}

export interface UnscheduledStudent {
  user_id: string
  real_name: string
  avatar: string
  skill_tags: string[]
}

export interface UnscheduledResponse {
  code: number
  message: string
  data: UnscheduledStudent[]
}

export const scheduleApi = {
  getSchedule(params: { month: string; job_id?: string }): Promise<ScheduleResponse> {
    return request.get('/v1/pc/enterprise/schedule', { params })
  },
  getUnscheduledStudents(jobId: string): Promise<UnscheduledResponse> {
    return request.get('/v1/pc/enterprise/schedule/unscheduled', { params: { job_id: jobId } })
  },
  createSchedule(data: { date: string; student_id: string; time_range: string; job_id: string }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/schedule/create', data)
  },
  deleteSchedule(scheduleId: string): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/schedule/delete', { schedule_id: scheduleId })
  },
  confirmSchedule(data: { month: string; job_id?: string }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/schedule/confirm', data)
  },
  exportSchedule(data: { month: string; job_id?: string }): Promise<{ code: number; message: string; data: string }> {
    return request.post('/v1/pc/enterprise/schedule/export', data)
  }
}

export default scheduleApi