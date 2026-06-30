import request from '../utils/request'

export interface ClockRecord {
  id: string
  job_id: string
  job_name: string
  company_name: string
  clock_type: 'clock_in' | 'clock_out'
  clock_time: string
  latitude: number
  longitude: number
  address: string
  is_abnormal: boolean
  abnormal_reason?: string
  appeal_status: 'none' | 'pending' | 'approved' | 'rejected'
  created_at: string
}

export interface ClockParams {
  job_id: string
  clock_type: 'clock_in' | 'clock_out'
  latitude: number
  longitude: number
  address: string
  abnormal_reason?: string
}

export interface AppealParams {
  record_id: string
  reason: string
  evidence_images: string[]
}

export interface ClockResponse {
  code: number
  message: string
  data: {
    is_abnormal: boolean
    distance?: number
  }
}

export interface ClockListResponse {
  code: number
  message: string
  data: {
    list: ClockRecord[]
    total: number
  }
}

export interface AppealResponse {
  code: number
  message: string
}

export const clockApi = {
  clock(data: ClockParams): Promise<ClockResponse> {
    return request.post('/v1/student/clock', data)
  },
  getClockRecords(jobId?: string, page?: number, size?: number): Promise<ClockListResponse> {
    return request.get('/v1/student/clock/records', { params: { job_id: jobId, page, size } })
  },
  appeal(data: AppealParams): Promise<AppealResponse> {
    return request.post('/v1/student/clock/appeal', data)
  }
}

export default clockApi