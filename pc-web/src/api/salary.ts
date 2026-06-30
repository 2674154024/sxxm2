import request from './request'

export interface SalaryRecord {
  record_id: string
  student_name: string
  job_name: string
  work_hours: number
  hourly_wage: number
  gross_amount: number
  tax_amount: number
  net_amount: number
  status: 'pending' | 'calculated' | 'confirmed' | 'paid' | 'rejected'
  month: string
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

export interface PaymentRecord {
  record_id: string
  student_name: string
  job_name: string
  amount: number
  pay_time: string
  status: 'processing' | 'success' | 'failed'
}

export interface PaymentListResponse {
  code: number
  message: string
  data: {
    list: PaymentRecord[]
    total: number
    page: number
    size: number
  }
}

export const salaryApi = {
  getSalaryList(params: { month: string; page?: number; size?: number }): Promise<SalaryListResponse> {
    return request.get('/v1/pc/enterprise/salary/list', { params })
  },
  calculateSalary(data: { month: string }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/salary/calculate', data)
  },
  confirmSalary(data: { record_ids: string[] }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/salary/confirm', data)
  },
  paySalary(data: { record_ids: string[] }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/salary/pay', data)
  },
  getPaymentRecords(params: { page?: number; size?: number }): Promise<PaymentListResponse> {
    return request.get('/v1/pc/enterprise/salary/payment-records', { params })
  },
  updateWorkHours(data: { record_id: string; work_hours: number }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/salary/update-hours', data)
  }
}

export default salaryApi