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
    '/v1/student/job/apply/list',
    { params }
  ).then((res: any) => {
    if (res.code === 200 && res.data && res.data.list) {
      res.data.list = res.data.list.map((item: any) => ({
        apply_id: item.applyId,
        job_id: item.jobId,
        job_title: item.jobTitle,
        salary_amount: item.salaryAmount,
        enterprise_name: item.enterpriseName || '',
        apply_status: item.applyStatus || 0,
        apply_status_text: getStatusText(item.applyStatus),
        create_time: item.createdAt ? formatDate(item.createdAt) : '',
      }))
    }
    return res
  })
}

function getStatusText(status: number): string {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
  }
  return statusMap[status] || '未知'
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function cancelApply(applyId: string) {
  return request.delete<any, { code: number; message: string; data: any }>(
    '/v1/student/job/apply/cancel',
    { params: { apply_id: applyId } }
  )
}
