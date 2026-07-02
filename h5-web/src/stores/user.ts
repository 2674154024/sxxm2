import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import { getApplyList } from '@/api/apply'
import { getFavoriteList } from '@/api/job'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<any>(null)
  const stats = reactive({
    pendingCount: 0,
    approvedCount: 0,
    favoriteCount: 0,
  })
  const statsLoaded = ref(false)

  const isLoggedIn = computed(() => !!token.value)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: any) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    stats.pendingCount = 0
    stats.approvedCount = 0
    stats.favoriteCount = 0
    statsLoaded.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function initFromStorage() {
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('Failed to parse user info from storage')
      }
    }
  }

  async function loadStats() {
    if (!isLoggedIn.value) return
    try {
      const [pendingRes, approvedRes, favoriteRes] = await Promise.all([
        getApplyList({ status: 0, page: 1, size: 1 }),
        getApplyList({ status: 1, page: 1, size: 1 }),
        getFavoriteList({ page: 1, size: 1 }),
      ])
      stats.pendingCount = pendingRes.code === 200 ? (pendingRes.data?.total || 0) : 0
      stats.approvedCount = approvedRes.code === 200 ? (approvedRes.data?.total || 0) : 0
      stats.favoriteCount = favoriteRes.code === 200 ? (favoriteRes.data?.total || 0) : 0
      statsLoaded.value = true
    } catch (error) {
      console.error('加载统计数据失败:', error)
    }
  }

  function incrementPending() {
    stats.pendingCount++
  }

  function incrementFavorite() {
    stats.favoriteCount++
  }

  function decrementFavorite() {
    if (stats.favoriteCount > 0) {
      stats.favoriteCount--
    }
  }

  return {
    token,
    userInfo,
    stats,
    statsLoaded,
    isLoggedIn,
    setToken,
    setUserInfo,
    logout,
    initFromStorage,
    loadStats,
    incrementPending,
    incrementFavorite,
    decrementFavorite,
  }
})
