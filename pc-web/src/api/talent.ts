import request from './request'

export interface Student {
  user_id: string
  real_name: string
  phone: string
  school_name: string
  skill_tags: string[]
  credit_score: number
  cooperate_count: number
  avatar: string
}

export interface ApplyRecord {
  apply_id: string
  job_name: string
  student_name: string
  student_phone: string
  status: 'pending' | 'interview' | 'hired' | 'rejected'
  apply_time: string
}

export interface StudentListResponse {
  code: number
  message: string
  data: {
    list: Student[]
    total: number
    page: number
    size: number
  }
}

export interface ApplyListResponse {
  code: number
  message: string
  data: {
    list: ApplyRecord[]
    total: number
    page: number
    size: number
  }
}

export const talentApi = {
  getApplyList(params: { page?: number; size?: number; status?: string }): Promise<ApplyListResponse> {
    return request.get('/v1/pc/enterprise/apply/list', { params })
  },
  getTalentLibrary(params: { 
    skill_tag?: string
    credit_min?: number
    credit_max?: number
    cooperate_count?: number
    page?: number
    size?: number
  }): Promise<StudentListResponse> {
    return request.get('/v1/pc/enterprise/talent/library', { params })
  },
  inviteStudent(data: { student_id: string; job_id: string }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/talent/invite', data)
  },
  addToJob(data: { student_id: string; job_id: string }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/talent/add-to-job', data)
  },
  getStudentResume(studentId: string): Promise<{ code: number; message: string; data: any }> {
    return request.get('/v1/pc/enterprise/talent/resume', { params: { student_id: studentId } })
  },
  handleApply(data: { apply_id: string; status: 'interview' | 'hired' | 'rejected'; reason?: string }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/apply/handle', data)
  }
}

export default talentApi