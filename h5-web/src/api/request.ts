import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    config.headers['X-Client-Type'] = 'h5'
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
    } else if (res.code === 401) {
      return Promise.reject(new Error(res.message || '登录已过期'))
    } else {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    console.error('Request Error:', error)
    console.error('Error Response:', error.response?.data)
    console.error('Error Status:', error.response?.status)
    
    if (error.code === 'ERR_CANCELED' || error.code === 'ERR_ABORTED') {
      console.warn('请求已取消')
      return Promise.reject(error)
    }
    
    const status = error.response?.status
    if (status === 401) {
      const resData = error.response?.data
      if (resData?.code === 401) {
        return Promise.reject(new Error(resData.message || '登录已过期'))
      }
    } else if (status === 403) {
      console.error('无权限')
    } else if (status === 500) {
      console.error('系统繁忙')
    }
    return Promise.reject(error)
  }
)

export default service