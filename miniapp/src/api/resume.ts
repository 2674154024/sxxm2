import request from '../utils/request'

export interface WorkTimeSlot {
  mon: string[]
  tue: string[]
  wed: string[]
  thu: string[]
  fri: string[]
  sat: string[]
  sun: string[]
}

export interface Resume {
  id: string
  user_id: string
  avatar: string
  real_name: string
  gender: 'male' | 'female'
  school_id: string
  school_name: string
  phone: string
  work_time: WorkTimeSlot
  skills: string[]
  introduction: string
  created_at: string
  updated_at: string
}

export interface ResumeResponse {
  code: number
  message: string
  data: Resume
}

export interface UpdateResumeParams {
  avatar?: string
  gender?: 'male' | 'female'
  phone?: string
  work_time?: WorkTimeSlot
  skills?: string[]
  introduction?: string
}

export const resumeApi = {
  getResume(): Promise<ResumeResponse> {
    return request.get('/v1/student/resume')
  },
  updateResume(data: UpdateResumeParams): Promise<ResumeResponse> {
    return request.put('/v1/student/resume', data)
  },
  uploadAvatar(filePath: string): Promise<{ code: number; message: string; data: { url: string } }> {
    return request.post('/v1/student/resume/avatar', { filePath })
  }
}

export default resumeApi