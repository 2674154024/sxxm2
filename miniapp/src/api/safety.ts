import request from '../utils/request'

export interface SafetyCase {
  case_id: string
  title: string
  summary: string
  image_url: string
  category: 'deposit_scam' | '刷单陷阱' | 'information_leak' | 'false_recruitment'
}

export interface FAQItem {
  question: string
  answer: string
}

export interface SafetyCaseResponse {
  code: number
  message: string
  data: SafetyCase[]
}

export interface FAQResponse {
  code: number
  message: string
  data: FAQItem[]
}

export const safetyApi = {
  getSafetyCases(): Promise<SafetyCaseResponse> {
    return request.get('/v1/student/safety/cases')
  },
  getFAQ(): Promise<FAQResponse> {
    return request.get('/v1/student/safety/faq')
  }
}

export default safetyApi