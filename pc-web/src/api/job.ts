import request from './request'

export interface Job {
  job_id: string
  job_name: string
  company_name: string
  salary: number
  salary_type: 'hourly' | 'daily' | 'monthly'
  settlement_type: 'daily' | 'weekly' | 'monthly'
  address: string
  longitude: number
  latitude: number
  work_time: {
    weekday: string[]
    time_range: string[]
  }
  skill_tags: string[]
  description: string
  has_insurance: boolean
  status: 'pending' | 'approved' | 'rejected' | 'offline'
  hired_count: number
  apply_count: number
  created_time: string
}

export interface JobListResponse {
  code: number
  message: string
  data: {
    list: Job[]
    total: number
    page: number
    size: number
  }
}

export interface JobDetailResponse {
  code: number
  message: string
  data: Job
}

export interface PublishJobRequest {
  job_name: string
  salary: number
  salary_type: 'hourly' | 'daily' | 'monthly'
  settlement_type: 'daily' | 'weekly' | 'monthly'
  address: string
  longitude: number
  latitude: number
  work_time: {
    weekday: string[]
    time_range: string[]
  }
  skill_tags: string[]
  description: string
  has_insurance: boolean
}

export const jobApi = {
  getJobList(params: { page?: number; size?: number; status?: string }): Promise<JobListResponse> {
    return request.get('/v1/pc/enterprise/job/list', { params })
  },
  getJobDetail(jobId: string): Promise<JobDetailResponse> {
    return request.get('/v1/pc/enterprise/job/detail', { params: { job_id: jobId } })
  },
  publishJob(data: PublishJobRequest): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/job/publish', data)
  },
  batchPublish(data: any[]): Promise<{ code: number; message: string; data: { success: number; failed: number; errors: any[] } }> {
    return request.post('/v1/pc/enterprise/job/batch-publish', data)
  },
  offlineJob(jobId: string): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/job/offline', { job_id: jobId })
  }
}

export default jobApi