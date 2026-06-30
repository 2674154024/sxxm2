import request from '../utils/request'

export interface LoginResult {
  token: string
  user: {
    user_id: string
    real_name: string
    phone: string
    school_id: string
    school_name: string
    verify_status: number
    avatar: string
  }
}

export interface SmsCodeParams {
  phone: string
}

export interface PhoneLoginParams {
  phone: string
  code: string
}

export interface WechatLoginParams {
  code: string
}

export const login = {
  phoneLogin(data: PhoneLoginParams): Promise<LoginResult> {
    return request.post('/v1/auth/phone-login', data)
  },
  wechatLogin(data: WechatLoginParams): Promise<LoginResult> {
    return request.post('/v1/auth/wechat-login', data)
  },
  sendSmsCode(data: SmsCodeParams): Promise<void> {
    return request.post('/v1/auth/sms-code', data)
  }
}

export default login