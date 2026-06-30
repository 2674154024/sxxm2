import { defineStore } from 'pinia'

export interface EnterpriseInfo {
  enterprise_id: string
  enterprise_name: string
  credit_code: string
  legal_person: string
  phone: string
  verify_status: number
  avatar: string
}

export const useEnterpriseStore = defineStore('enterprise', {
  state: () => ({
    enterprise: null as EnterpriseInfo | null,
    token: null as string | null,
    isAuthenticated: false
  }),
  actions: {
    login(token: string, enterprise: EnterpriseInfo) {
      this.token = token
      this.enterprise = enterprise
      this.isAuthenticated = true
      localStorage.setItem('token', token)
      localStorage.setItem('enterprise', JSON.stringify(enterprise))
    },
    logout() {
      this.token = null
      this.enterprise = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('enterprise')
    },
    init() {
      const token = localStorage.getItem('token')
      const enterpriseStr = localStorage.getItem('enterprise')
      if (token && enterpriseStr) {
        this.token = token
        this.enterprise = JSON.parse(enterpriseStr)
        this.isAuthenticated = true
      }
    }
  },
  getters: {
    enterpriseName: (state) => state.enterprise?.enterprise_name || '',
    isVerified: (state) => state.enterprise?.verify_status === 2
  }
})