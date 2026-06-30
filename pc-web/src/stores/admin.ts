import { defineStore } from 'pinia'

export interface AdminInfo {
  admin_id: string
  username: string
  real_name: string
  phone: string
  role_type: number
  avatar: string
}

export const useAdminStore = defineStore('admin', {
  state: () => ({
    admin: null as AdminInfo | null,
    token: null as string | null,
    adminRoleType: 0 as number,
    isAuthenticated: false
  }),
  actions: {
    login(token: string, admin: AdminInfo) {
      this.token = token
      this.admin = admin
      this.adminRoleType = admin.role_type
      this.isAuthenticated = true
      localStorage.setItem('token', token)
      localStorage.setItem('admin', JSON.stringify(admin))
      localStorage.setItem('roleType', String(admin.role_type))
    },
    logout() {
      this.token = null
      this.admin = null
      this.adminRoleType = 0
      this.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('admin')
      localStorage.removeItem('roleType')
    },
    init() {
      const token = localStorage.getItem('token')
      const adminStr = localStorage.getItem('admin')
      const roleType = localStorage.getItem('roleType')
      if (token && adminStr) {
        this.token = token
        this.admin = JSON.parse(adminStr)
        this.adminRoleType = roleType ? parseInt(roleType) : 0
        this.isAuthenticated = true
      }
    }
  },
  getters: {
    adminName: (state) => state.admin?.real_name || state.admin?.username || '',
    isSuperAdmin: (state) => state.adminRoleType === 5,
    isAuditAdmin: (state) => state.adminRoleType === 1 || state.adminRoleType === 5,
    isRiskAdmin: (state) => state.adminRoleType === 2 || state.adminRoleType === 5,
    isOperationAdmin: (state) => state.adminRoleType === 3 || state.adminRoleType === 5,
    isFinanceAdmin: (state) => state.adminRoleType === 4 || state.adminRoleType === 5
  }
})