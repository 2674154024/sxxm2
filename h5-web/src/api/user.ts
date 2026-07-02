import request from '@/api/request'

export function getUserInfo() {
  return request.get<any, { code: number; message: string; data: any }>(
    '/v1/student/user/info'
  )
}

export function getProfile() {
  return request.get<any, { code: number; message: string; data: any }>(
    '/v1/student/profile'
  )
}

export function updateUserInfo(data: {
  nickname?: string
  realName?: string
  avatarUrl?: string
  availableTime?: string
  skillTags?: string
}) {
  return request.put<any, { code: number; message: string; data: any }>(
    '/v1/student/profile',
    data
  )
}