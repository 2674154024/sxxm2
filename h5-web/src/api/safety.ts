import request from './request'

export interface CaseItem {
  id: number
  title: string
  type: string
  desc: string
  icon: string
  color: string
}

export interface CaseDetail {
  id: number
  title: string
  type: string
  icon: string
  color: string
  desc: string
  content: string
  warning: string[]
  tips: string[]
}

export function getCaseList() {
  return request.get<any, { code: number; message: string; data: CaseItem[] }>('/v1/safety/cases')
}

export function getCaseDetail(id: number) {
  return request.get<any, { code: number; message: string; data: CaseDetail }>(`/v1/safety/cases/${id}`)
}