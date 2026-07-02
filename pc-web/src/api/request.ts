import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'

export interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
}

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const path = config.url || ''
    let token = ''
    if (path.startsWith('/v1/pc/admin')) {
      token = localStorage.getItem('admin_token') || ''
    } else if (path.startsWith('/v1/pc/enterprise')) {
      token = localStorage.getItem('enterprise_token') || ''
    } else {
      token = localStorage.getItem('admin_token') || localStorage.getItem('enterprise_token') || ''
    }
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    config.headers['X-Client-Type'] = 'pc'
    config.headers['X-Version'] = '1.0.0'
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      const error = new Error(res.message || '请求失败')
      error.response = response
      return Promise.reject(error)
    }
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    if (status === 401 || (data && data.code === 401)) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_role_type')
      localStorage.removeItem('admin')
      localStorage.removeItem('enterprise_token')
      localStorage.removeItem('enterprise_role_type')
      localStorage.removeItem('enterprise')
    } else if (status === 403) {
      console.error('无权限')
    } else if (status === 500) {
      console.error('系统繁忙')
    }
    return Promise.reject(error)
  }
)

const request = {
  get<T = any>(url: string, config?: any): Promise<ApiResponse<T>> {
    return service.get(url, config)
  },
  post<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> {
    return service.post(url, data, config)
  },
  put<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> {
    return service.put(url, data, config)
  },
  delete<T = any>(url: string, config?: any): Promise<ApiResponse<T>> {
    return service.delete(url, config)
  }
}

export default request