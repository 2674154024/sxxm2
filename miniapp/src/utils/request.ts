import axios from 'axios'
import { useUserStore } from '../store/index'
import CryptoJS from 'crypto-js'

const baseURL = '/api'

const service = axios.create({
  baseURL,
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.token || uni.getStorageSync('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        uni.navigateTo({ url: '/pages/student/login/index' })
      }
      uni.showToast({
        title: res.message || '请求失败',
        icon: 'none'
      })
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    uni.showToast({
      title: error.message || '网络错误',
      icon: 'none'
    })
    return Promise.reject(error)
  }
)

export const encryptPassword = (password: string): string => {
  const key = CryptoJS.enc.Utf8.parse('parttime_secret_key_32bytes')
  const iv = CryptoJS.enc.Utf8.parse('parttime_iv_16byt')
  const encrypted = CryptoJS.AES.encrypt(password, key, {
    iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })
  return encrypted.toString()
}

export default service