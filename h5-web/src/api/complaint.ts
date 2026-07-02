import request from '@/api/request'

export interface ComplaintTypeItem {
  key: string
  label: string
}

export interface ComplaintItem {
  complaint_id: string
  type: string
  type_text: string
  job_id?: string
  job_title?: string
  target_name: string
  content: string
  images: string[]
  status: number
  status_text: string
  create_time: string
}

export interface ComplaintTypeResponse {
  code: number
  message: string
  data: ComplaintTypeItem[]
}

export interface ComplaintListResponse {
  code: number
  message: string
  data: {
    list: ComplaintItem[]
    total: number
  }
}

export function getComplaintTypes(): Promise<ComplaintTypeResponse> {
  return Promise.resolve({
    code: 200,
    message: 'success',
    data: [
      { key: 'fake_recruitment', label: '虚假招聘' },
      { key: 'salary_arrears', label: '薪资拖欠' },
      { key: 'deposit_fraud', label: '押金诈骗' },
      { key: 'unfulfilled', label: '未履约' },
      { key: 'info_leak', label: '信息泄露' },
      { key: 'other', label: '其他问题' },
    ],
  })
}

export function createComplaint(params: {
  type: string
  job_id?: string
  target_name: string
  content: string
  images: string[]
}): Promise<any> {
  const body: any = {
    complaintType: params.type,
    complaintContent: params.content,
    defendantType: 'enterprise',
  }
  if (params.job_id && params.job_id.trim() !== '') {
    body.jobId = params.job_id
  }
  if (params.images && params.images.length > 0) {
    body.evidenceUrls = params.images.join(',')
  }
  return request.post<any, any>('/v1/student/complaint', body)
}

export function uploadEvidence(file: File): Promise<any> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, any>('/v1/complaint/upload', formData)
}

export function getComplaintList(params: { page: number; size: number }): Promise<ComplaintListResponse> {
  return request.get<any, any>('/v1/student/complaint/list', {
    params,
  }).then((res: any) => {
    if (res.code === 200 && res.data) {
      const list = (res.data || []).map((item: any) => ({
        complaint_id: item.complaintId,
        type: item.complaintType,
        type_text: getComplaintTypeText(item.complaintType),
        job_id: item.jobId,
        job_title: '',
        target_name: item.defendantId || '',
        content: item.complaintContent,
        images: item.evidenceUrls?.split(',').filter(Boolean) || [],
        status: item.status || 0,
        status_text: getComplaintStatusText(item.status),
        create_time: item.createdAt || '',
      }))
      res.data = { list, total: list.length }
    }
    return res
  })
}

function getComplaintTypeText(type: string): string {
  const typeMap: Record<string, string> = {
    fake_recruitment: '虚假招聘',
    salary_arrears: '薪资拖欠',
    deposit_fraud: '押金诈骗',
    unfulfilled: '未履约',
    info_leak: '信息泄露',
    other: '其他问题',
  }
  return typeMap[type] || type
}

function getComplaintStatusText(status: number): string {
  const statusMap: Record<number, string> = {
    0: '待处理',
    1: '处理中',
    2: '已处理',
    3: '已驳回',
  }
  return statusMap[status] || '未知'
}
