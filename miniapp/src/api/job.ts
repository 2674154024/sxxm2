import request from '../utils/request'

export interface JobListItem {
  job_id: string
  job_name: string
  company_name: string
  is_certified: boolean
  salary: number
  salary_type: 'hourly' | 'daily' | 'monthly'
  settlement_type: 'daily' | 'weekly' | 'monthly'
  address: string
  distance: number
  skill_tags: string[]
  has_insurance: boolean
  industry_tag: string
}

export interface WorkTime {
  weekday: string[]
  time_range: string[]
}

export interface JobDetail {
  job_id: string
  job_name: string
  cover_image: string
  salary: number
  salary_type: 'hourly' | 'daily' | 'monthly'
  company_name: string
  is_certified: boolean
  credit_score: number
  address: string
  longitude: number
  latitude: number
  work_time: WorkTime
  settlement_type: 'daily' | 'weekly' | 'monthly'
  hired_count: number
  total_count: number
  skill_tags: string[]
  description: string
  has_insurance: boolean
  apply_status: 'none' | 'applied' | 'hired' | 'rejected'
  similar_jobs: JobListItem[]
}

export interface JobListParams {
  longitude?: number
  latitude?: number
  distance?: number
  page?: number
  size?: number
  industry_tag?: string
}

export interface JobListResponse {
  code: number
  message: string
  data: {
    list: JobListItem[]
    total: number
    page: number
    size: number
  }
}

export interface JobDetailResponse {
  code: number
  message: string
  data: JobDetail
}

export interface ApplyResponse {
  code: number
  message: string
}

export const jobApi = {
  getJobList(params: JobListParams): Promise<JobListResponse> {
    return request.get('/v1/student/job/list', { params })
  },
  getJobDetail(jobId: string): Promise<JobDetailResponse> {
    return request.get('/v1/student/job/detail', { params: { job_id: jobId } })
  },
  applyJob(jobId: string): Promise<ApplyResponse> {
    return request.post('/v1/student/apply', { job_id: jobId })
  },
  favoriteJob(jobId: string, favorite: boolean): Promise<ApplyResponse> {
    return request.post('/v1/student/favorite', { job_id: jobId, favorite })
  }
}

export default jobApi