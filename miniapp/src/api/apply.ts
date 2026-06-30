import request from '../utils/request'

export type ApplyStatus = 'applied' | 'interviewing' | 'accepted' | 'rejected'

export interface InterviewInfo {
  interview_time: string
  interview_method: 'video' | 'offline'
  room_id?: string
  address?: string
}

export interface ApplyRecord {
  id: string
  job_id: string
  job_name: string
  company_name: string
  salary: number
  salary_type: 'hourly' | 'daily' | 'monthly'
  status: ApplyStatus
  status_text: string
  apply_time: string
  address: string
  interview?: InterviewInfo
}

export interface ApplyListParams {
  status?: ApplyStatus
  page?: number
  size?: number
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

export interface ApplyResponse {
  code: number
  message: string
}

export const applyApi = {
  getApplyList(params: ApplyListParams): Promise<ApplyListResponse> {
    return request.get('/v1/student/apply/list', { params })
  },
  cancelApply(applyId: string): Promise<ApplyResponse> {
    return request.delete(`/v1/student/apply/${applyId}`)
  },
  enterInterview(applyId: string): Promise<ApplyResponse> {
    return request.post(`/v1/student/apply/${applyId}/interview`)
  }
}

export default applyApi