import axios from 'axios'

const jobService = axios.create({
  baseURL: '/job-api',
  timeout: 10000,
})

jobService.interceptors.request.use(
  (config: any) => {
    const token = localStorage.getItem('enterprise_token') || ''
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    config.headers['X-Client-Type'] = 'pc'
    config.headers['X-Version'] = '1.0.0'
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

jobService.interceptors.response.use(
  (response: any) => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    return Promise.reject(error)
  }
)

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
  jobTitle: string
  salaryType: number
  salaryAmount: number
  settlementType: number
  workAddress: string
  workTime: string
  skillRequire: string
  recruitNum: number
  isInsured: number
}

export interface Apply {
  apply_id: string
  job_id: string
  job_name: string
  job_type: string
  salary_amount: number
  settlement_type: number
  work_address: string
  enterprise_name: string
  student_name: string
  student_phone: string
  status: string
  apply_time: string
  apply_status: number
  interview_time: string
  interview_type: string
  created_at: string
}

export interface ApplyListResponse {
  code: number
  message: string
  data: {
    list: Apply[]
    total: number
    page: number
    size: number
    pages: number
  }
}

export const jobApi = {
  getJobList(params: { page?: number; size?: number; status?: string }): Promise<JobListResponse> {
    return jobService.get('/v1/pc/enterprise/job/list', { params })
  },
  getJobDetail(jobId: string): Promise<JobDetailResponse> {
    return jobService.get('/v1/pc/enterprise/job/detail', { params: { job_id: jobId } })
  },
  publishJob(data: PublishJobRequest): Promise<{ code: number; message: string }> {
    return jobService.post('/v1/pc/enterprise/job/publish', data)
  },
  updateJob(data: any): Promise<{ code: number; message: string }> {
    return jobService.put('/v1/pc/enterprise/job/update', data)
  },
  batchPublish(data: any[]): Promise<{ code: number; message: string; data: { success: number; failed: number; errors: any[] } }> {
    return jobService.post('/v1/pc/enterprise/job/batch-publish', data)
  },
  offlineJob(jobId: string): Promise<{ code: number; message: string }> {
    return jobService.post('/v1/pc/enterprise/job/offline', { job_id: jobId })
  },
  getApplyList(params: { page?: number; size?: number; status?: number }): Promise<ApplyListResponse> {
    return jobService.get('/v1/pc/enterprise/job/apply/list', { params })
  }
}

export default jobApi