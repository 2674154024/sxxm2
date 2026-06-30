import request from '../utils/request'

export interface Agreement {
  agreement_id: string
  job_id: string
  job_name: string
  company_name: string
  salary: number
  salary_type: 'hourly' | 'daily' | 'monthly'
  work_content: string
  student_rights: string
  enterprise_rights: string
  start_date: string
  end_date: string
  status: 'pending' | 'signed' | 'completed' | 'terminated'
  created_at: string
}

export interface SignAgreementParams {
  agreement_id: string
  signature_image: string
  face_image: string
  agree: boolean
}

export interface AgreementDetailResponse {
  code: number
  message: string
  data: Agreement
}

export interface SignResponse {
  code: number
  message: string
}

export interface AgreementListResponse {
  code: number
  message: string
  data: {
    list: Agreement[]
    total: number
  }
}

export const agreementApi = {
  getAgreementList(page?: number, size?: number): Promise<AgreementListResponse> {
    return request.get('/v1/student/agreement/list', { params: { page, size } })
  },
  getAgreementDetail(agreementId: string): Promise<AgreementDetailResponse> {
    return request.get('/v1/student/agreement/detail', { params: { agreement_id: agreementId } })
  },
  signAgreement(data: SignAgreementParams): Promise<SignResponse> {
    return request.post('/v1/student/agreement/sign', data)
  }
}

export default agreementApi