import request from '../utils/request'

export interface ComplaintType {
  value: string
  label: string
}

export interface AppliedJob {
  apply_id: string
  job_name: string
  company_name: string
}

export interface ComplaintRequest {
  complaint_type: string
  apply_id?: string
  accused_name: string
  content: string
  evidence_images?: string[]
}

export interface ComplaintResponse {
  code: number
  message: string
}

export interface ComplaintTypesResponse {
  code: number
  message: string
  data: ComplaintType[]
}

export interface AppliedJobsResponse {
  code: number
  message: string
  data: AppliedJob[]
}

export const complaintApi = {
  getComplaintTypes(): Promise<ComplaintTypesResponse> {
    return request.get('/v1/student/complaint/types')
  },
  getAppliedJobs(): Promise<AppliedJobsResponse> {
    return request.get('/v1/student/complaint/applied-jobs')
  },
  submitComplaint(data: ComplaintRequest): Promise<ComplaintResponse> {
    return request.post('/v1/student/complaint', data)
  }
}

export default complaintApi