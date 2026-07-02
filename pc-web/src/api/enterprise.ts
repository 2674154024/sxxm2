import request from './request'

export const enterpriseApi = {
  getProfile(params: { enterpriseId: string }) {
    return request.get('/v1/enterprise/profile', { params })
  }
}

export const enterpriseSearchApi = {
  searchMenus(params: { keyword: string }) {
    return request.get('/v1/enterprise/search', { params })
  }
}
