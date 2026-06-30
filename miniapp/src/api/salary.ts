import request from '../utils/request'

export interface SalaryRecord {
  record_id: string
  company_name: string
  job_name: string
  work_hours: number
  hourly_wage: number
  gross_amount: number
  tax_amount: number
  net_amount: number
  status: 'pending_hours' | 'pending_company' | 'pending_payment' | 'paid' | 'rejected'
  paid_time?: string
}

export interface SalarySummary {
  monthly_income: number
  total_income: number
  pending_amount: number
}

export interface SalaryListResponse {
  code: number
  message: string
  data: {
    list: SalaryRecord[]
    total: number
    page: number
    size: number
  }
}

export interface SalarySummaryResponse {
  code: number
  message: string
  data: SalarySummary
}

export interface SalaryDetailResponse {
  code: number
  message: string
  data: SalaryRecord
}

export const salaryApi = {
  getSalarySummary(): Promise<SalarySummaryResponse> {
    return request.get('/v1/student/salary/summary')
  },
  getSalaryList(params: { month?: string; page?: number; size?: number }): Promise<SalaryListResponse> {
    return request.get('/v1/student/salary/list', { params })
  },
  getSalaryDetail(recordId: string): Promise<SalaryDetailResponse> {
    return request.get('/v1/student/salary/detail', { params: { record_id: recordId } })
  }
}

export default salaryApi