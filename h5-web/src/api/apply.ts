import request from '@/api/request'

export interface ApplyItem {
  apply_id: string
  job_id: string
  job_title: string
  salary_amount: number
  enterprise_name: string
  apply_status: number
  apply_status_text: string
  create_time: string
}

export interface ApplyListResponse {
  list: ApplyItem[]
  total: number
}

export function getApplyList(params: {
  status?: number
  page: number
  size: number
}) {
  return request.get<any, { code: number; message: string; data: ApplyListResponse }>(
    '/v1/student/apply/list',
    { params }
  )
}

export function cancelApply(applyId: string) {
  return request.post<any, { code: number; message: string; data: any }>(
    '/v1/student/apply/cancel',
    { apply_id: applyId }
  )
}
