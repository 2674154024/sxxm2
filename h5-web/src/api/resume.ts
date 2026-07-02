import request from '@/api/request'

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
  userId: string
  avatar: string
  realName: string
  gender: 'male' | 'female'
  schoolName: string
  phone: string
  availableTime: string
  skillTags: string
  education: string
  workExperience: string
  selfIntroduction: string
  major: string
  grade: string
  creditScore: number
}

export interface ResumeResponse {
  code: number
  message: string
  data: Resume
}

export interface UpdateResumeParams {
  avatar?: string
  gender?: 'male' | 'female'
  realName?: string
  phone?: string
  availableTime?: string
  skillTags?: string
  education?: string
  workExperience?: string
  selfIntroduction?: string
  major?: string
  grade?: string
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