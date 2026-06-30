import { defineStore } from 'pinia'

export interface UserInfo {
  user_id: string
  real_name: string
  phone: string
  school_id: string
  school_name: string
  verify_status: number
  avatar: string
}

export interface JobListItem {
  job_id: string
  job_name: string
  company_name: string
  is_certified: boolean
  salary: number
  salary_type: 'hourly' | 'daily' | 'monthly'
  settlement_type: 'daily' | 'weekly' | 'monthly'
  address: string
  distance: number
  skill_tags: string[]
  has_insurance: boolean
  industry_tag: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null as UserInfo | null,
    token: null as string | null
  }),
  actions: {
    login(token: string, user: UserInfo) {
      this.token = token
      this.user = user
      uni.setStorageSync('token', token)
    },
    logout() {
      this.token = null
      this.user = null
      uni.removeStorageSync('token')
    },
    init() {
      const token = uni.getStorageSync('token')
      if (token) {
        this.token = token
      }
    }
  },
  getters: {
    isLogin: (state) => !!state.token,
    userInfo: (state) => state.user
  }
})

export const useJobStore = defineStore('job', {
  state: () => ({
    jobList: [] as JobListItem[],
    total: 0,
    page: 1,
    size: 20,
    loading: false,
    industryTag: ''
  }),
  actions: {
    setJobList(list: JobListItem[], total: number, page: number) {
      this.jobList = list
      this.total = total
      this.page = page
    },
    appendJobList(list: JobListItem[]) {
      this.jobList = [...this.jobList, ...list]
    },
    setLoading(loading: boolean) {
      this.loading = loading
    },
    setIndustryTag(tag: string) {
      this.industryTag = tag
      this.page = 1
      this.jobList = []
    },
    resetPage() {
      this.page = 1
      this.jobList = []
    },
    nextPage() {
      this.page++
    }
  },
  getters: {
    hasMore: (state) => state.jobList.length < state.total
  }
})