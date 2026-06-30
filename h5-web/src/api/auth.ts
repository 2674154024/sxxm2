import request from '@/api/request'

export function sendSmsCode(phone: string) {
  return request.post<any, { code: number; message: string; data: any }>(
    '/v1/auth/sms-code',
    { phone }
  )
}

export function phoneLogin(phone: string, code: string) {
  return request.post<any, { code: number; message: string; data: { token: string; user_info: any } }>(
    '/v1/auth/phone-login',
    { phone, code }
  )
}

export function passwordLogin(phone: string, password: string) {
  return request.post<any, { code: number; message: string; data: { token: string; user_info: any } }>(
    '/v1/auth/password-login',
    { phone, password }
  )
}

export function register(phone: string, code: string, password: string, nickname?: string) {
  return request.post<any, { code: number; message: string; data: { token: string; user_info: any } }>(
    '/v1/auth/register',
    { phone, code, password, nickname }
  )
}

export function getUserInfo() {
  return request.get<any, { code: number; message: string; data: any }>(
    '/v1/student/user/info'
  )
}
