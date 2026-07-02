<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import {
  TrendCharts,
  Briefcase,
  CircleCheck,
  Wallet,
  Setting,
  User,
  Search,
  SwitchButton,
  HomeFilled,
  ArrowDown
} from '@element-plus/icons-vue'
import { searchApi } from '@/api/admin'

const router = useRouter()
const route = useRoute()
const store = useAdminStore()

const sidebarCollapsed = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const showSearchResults = ref(false)
const showUserDropdown = ref(false)

const adminMenus = computed(() => {
  const menus: any[] = []
  const roleType = store.adminRoleType

  if (roleType === 1 || roleType === 5) {
    menus.push({
      path: '/admin/audit/enterprise',
      name: '企业审核',
      icon: Briefcase,
    })
    menus.push({
      path: '/admin/audit/job',
      name: '岗位审核',
      icon: Briefcase,
    })
  }

  if (roleType === 2 || roleType === 5) {
    menus.push({
      path: '/admin/risk/complaint',
      name: '投诉工单',
      icon: CircleCheck,
    })
    menus.push({
      path: '/admin/risk/dashboard',
      name: '风控看板',
      icon: CircleCheck,
    })
  }

  if (roleType === 3 || roleType === 5) {
    menus.push({
      path: '/admin/operation/report',
      name: '数据报表',
      icon: TrendCharts,
    })
    menus.push({
      path: '/admin/operation/notification',
      name: '推送管理',
      icon: TrendCharts,
    })
  }

  if (roleType === 4 || roleType === 5) {
    menus.push({
      path: '/admin/finance/settlement',
      name: '薪资发放',
      icon: Wallet,
    })
    menus.push({
      path: '/admin/finance/report',
      name: '财务报表',
      icon: Wallet,
    })
  }

  if (roleType === 5) {
    menus.push({
      path: '/admin/system/config',
      name: '系统配置',
      icon: Setting,
    })
    menus.push({
      path: '/admin/system/roles',
      name: '角色权限',
      icon: Setting,
    })
    menus.push({
      path: '/admin/system/audit-log',
      name: '审计日志',
      icon: Setting,
    })
  }

  return menus
})

const roleNames: Record<number, string> = {
  1: '审核管理员',
  2: '风控管理员',
  3: '运营管理员',
  4: '财务管理员',
  5: '超级管理员'
}

const activePath = computed(() => route.path)

const breadcrumb = computed(() => {
  const result: string[] = []
  const match = adminMenus.value.find(item => item.path === activePath.value)
  if (match) {
    result.push(match.name)
  }
  return result
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const toggleUserDropdown = () => {
  showUserDropdown.value = !showUserDropdown.value
}

const closeUserDropdown = () => {
  showUserDropdown.value = false
}

const handleLogout = () => {
  store.logout()
  router.push('/')
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    showSearchResults.value = false
    return
  }
  
  try {
    const response = await searchApi.searchMenus({ keyword })
    const data = response.data
    if (data.code === 200) {
      searchResults.value = data.data || []
    } else {
      searchResults.value = adminMenus.value.filter(item => 
        item.name.toLowerCase().includes(keyword.toLowerCase())
      )
    }
  } catch {
    searchResults.value = adminMenus.value.filter(item => 
      item.name.toLowerCase().includes(keyword.toLowerCase())
    )
  }
  showSearchResults.value = searchResults.value.length > 0
}

const handleSearchInput = () => {
  if (searchKeyword.value.trim()) {
    handleSearch()
  } else {
    searchResults.value = []
    showSearchResults.value = false
  }
}

const selectSearchResult = (item: any) => {
  router.push(item.path)
  searchKeyword.value = ''
  searchResults.value = []
  showSearchResults.value = false
}

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.search-box') && !target.closest('.user-info')) {
    closeUserDropdown()
    showSearchResults.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="user-avatar-wrapper">
          <div class="user-avatar">
            <User />
          </div>
        </div>
        <span class="user-name" v-if="!sidebarCollapsed">{{ store.admin?.real_name || '管理员' }}</span>
      </div>

      <nav class="menu">
        <div class="menu-item" :class="{ active: activePath.startsWith('/admin') && !activePath.includes('audit') && !activePath.includes('risk') && !activePath.includes('operation') && !activePath.includes('finance') && !activePath.includes('system') }" @click="router.push('/admin/audit/enterprise')">
          <HomeFilled class="menu-icon" />
          <span class="menu-text" v-if="!sidebarCollapsed">首页</span>
        </div>

        <div 
          v-for="item in adminMenus" 
          :key="item.path"
          class="menu-item"
          :class="{ active: activePath === item.path }"
          @click="router.push(item.path)"
        >
          <component :is="item.icon" class="menu-icon" />
          <span class="menu-text" v-if="!sidebarCollapsed">{{ item.name }}</span>
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="top-bar">
        <div class="left-section">
          <button class="sidebar-toggle" @click="toggleSidebar">
            <span class="sidebar-toggle-icon">☰</span>
          </button>
          <div class="breadcrumb" v-if="breadcrumb.length">
            <span class="breadcrumb-item">首页</span>
            <template v-for="(item, index) in breadcrumb" :key="index">
              <span class="breadcrumb-separator">/</span>
              <span class="breadcrumb-item" :class="{ active: index === breadcrumb.length - 1 }">{{ item }}</span>
            </template>
          </div>
        </div>
        <div class="right-section">
          <div class="search-box" @click.stop>
            <button class="search-icon-btn" @click="handleSearch">
              <Search class="search-icon" />
            </button>
            <input 
              v-model="searchKeyword"
              type="text" 
              placeholder="搜索菜单、功能..." 
              @keyup.enter="handleSearch"
              @input="handleSearchInput"
            />
            <div class="search-results" v-if="showSearchResults" @click.stop>
              <div 
                v-for="item in searchResults" 
                :key="item.path"
                class="search-result-item"
                @click="selectSearchResult(item)"
              >
                <component :is="item.icon || Briefcase" class="result-icon" />
                <span class="result-name">{{ item.name }}</span>
              </div>
            </div>
          </div>
          
          <div class="user-info" @click.stop="toggleUserDropdown">
            <div class="user-avatar-sm">
              <User />
            </div>
            <div class="user-detail">
              <span class="user-name">{{ store.admin?.real_name || '管理员' }}</span>
              <span class="user-role">{{ roleNames[store.adminRoleType] || '管理员' }}</span>
            </div>
            <ArrowDown class="user-arrow" :class="{ open: showUserDropdown }" />
          </div>
          <div class="user-dropdown" v-if="showUserDropdown" @click.stop>
            <div class="dropdown-header">
              <div class="dropdown-avatar">
                <User />
              </div>
              <div class="dropdown-user-info">
                <span class="dropdown-username">{{ store.admin?.real_name || '管理员' }}</span>
                <span class="dropdown-role">{{ roleNames[store.adminRoleType] || '管理员' }}</span>
              </div>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item" @click="router.push('/admin/profile'); closeUserDropdown()">
              <User class="dropdown-icon" />
              <span>个人中心</span>
            </div>
            <div class="dropdown-item" @click="router.push('/admin/settings'); closeUserDropdown()">
              <Setting class="dropdown-icon" />
              <span>账号设置</span>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item logout" @click="handleLogout">
              <SwitchButton class="dropdown-icon" />
              <span>退出登录</span>
            </div>
          </div>
        </div>
      </header>

      <div class="content-wrapper">
        <router-view />
      </div>

      <footer class="footer">
        <span>© 2026 长沙大学生兼职平台 - 管理后台 v1.0.0</span>
      </footer>
    </main>
  </div>
</template>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background: #F6F8FA;
  border-right: 1px solid #E5E6EB;
  color: #24292F;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease-out;
  position: relative;
  flex-shrink: 0;

  &.collapsed {
    width: 56px;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #E5E6EB;
  gap: 12px;
  position: relative;
}

.user-avatar-wrapper {
  flex-shrink: 0;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 14px;
  flex-shrink: 0;
  border: 1px solid #E5E6EB;
}

.user-name {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
  color: #24292F;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #C9CDD4;
    border-radius: 3px;
  }
}

.menu-group {
  margin-bottom: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  cursor: pointer;
  transition: all 0.15s ease;
  position: relative;
  border-radius: 6px;
  gap: 10px;

  &:hover {
    background-color: #E5E6EB;
  }

  &.active {
    background-color: #EBF5FF;
    border-left: 2px solid #165DFF;

    .menu-icon {
      color: #165DFF;
    }

    .menu-text {
      color: #165DFF;
      font-weight: 500;
    }
  }

  &.has-children {
    justify-content: space-between;
  }
}

.menu-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: #86909C;
  transition: color 0.15s;
}

.menu-text {
  flex: 1;
  font-size: 13px;
  color: #4E5969;
  white-space: nowrap;
  overflow: hidden;
  transition: color 0.15s;
}

.menu-arrow {
  font-size: 12px;
  color: #86909C;
  transition: transform 0.2s;
  flex-shrink: 0;

  &.expanded {
    transform: rotate(90deg);
    color: #4E5969;
  }
}

.sub-menu {
  margin: 2px 0 4px 8px;
  padding-left: 8px;
}

.sub-menu-item {
  display: flex;
  align-items: center;
  padding: 5px 8px;
  font-size: 12px;
  color: #86909C;
  cursor: pointer;
  transition: all 0.15s ease;
  border-radius: 4px;
  gap: 6px;

  &:hover {
    background-color: #F2F3F5;
    color: #4E5969;
  }

  &.active {
    background-color: #EBF5FF;
    color: #165DFF;

    .sub-dot {
      background-color: #165DFF;
    }
  }
}

.sub-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #C9CDD4;
  flex-shrink: 0;
  transition: background-color 0.15s;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.top-bar {
  height: 56px;
  background-color: #FFFFFF;
  border-bottom: 1px solid #E5E6EB;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  z-index: 10;
  position: relative;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-toggle {
  background: none;
  border: none;
  font-size: 18px;
  color: #4E5969;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background-color: #F2F3F5;
  }
}

.sidebar-toggle-icon {
  font-size: 16px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.breadcrumb-item {
  color: #86909C;

  &.active {
    color: #24292F;
    font-weight: 500;
  }
}

.breadcrumb-separator {
  margin: 0 6px;
  color: #C9CDD4;
  font-size: 12px;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  width: 280px;
  height: 36px;
  border: 1px solid #E5E6EB;
  border-radius: 6px;
  background-color: #F7F8FA;
  overflow: hidden;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);

  &:focus-within {
    border-color: #165DFF;
    background-color: #FFFFFF;
    box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
  }

  .search-icon-btn {
    width: 36px;
    height: 100%;
    background: transparent;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #86909C;
    transition: all 0.15s;

    &:hover {
      color: #4E5969;
    }
  }

  .search-icon {
    width: 16px;
    height: 16px;
  }

  input {
    flex: 1;
    height: 100%;
    padding: 0 12px;
    border: none;
    font-size: 13px;
    background: transparent;
    outline: none;

    &::placeholder {
      color: #C9CDD4;
    }
  }
}

.search-results {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background-color: #FFFFFF;
  border: 1px solid #E5E6EB;
  border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  overflow: hidden;
  animation: dropdownFadeIn 0.15s ease;

  &::before {
    content: '';
    position: absolute;
    top: -6px;
    left: 16px;
    width: 12px;
    height: 12px;
    background-color: #FFFFFF;
    border-top: 1px solid #E5E6EB;
    border-left: 1px solid #E5E6EB;
    transform: rotate(45deg);
  }
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  transition: all 0.12s;
  gap: 10px;

  &:hover {
    background-color: #F7F8FA;

    .result-name {
      color: #165DFF;
    }
  }

  &:active {
    background-color: #EBF5FF;
  }
}

.result-icon {
  width: 16px;
  height: 16px;
  color: #86909C;
  flex-shrink: 0;
}

.result-name {
  font-size: 13px;
  color: #4E5969;
  transition: color 0.12s;
}

.notification-btn {
  position: relative;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86909C;
  transition: all 0.15s;

  &:hover {
    background-color: #F7F8FA;
    color: #4E5969;
  }

  &:active {
    transform: scale(0.95);
  }
}

.notification-icon {
  width: 18px;
  height: 18px;
}

.notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background-color: #F53F3F;
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 52px;
  width: 320px;
  background-color: #FFFFFF;
  border: 1px solid #E5E6EB;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
  animation: dropdownFadeIn 0.2s ease;

  &::before {
    content: '';
    position: absolute;
    top: -6px;
    right: 20px;
    width: 12px;
    height: 12px;
    background-color: #FFFFFF;
    border-top: 1px solid #E5E6EB;
    border-right: 1px solid #E5E6EB;
    transform: rotate(-45deg);
  }
}

.dropdown-title {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #24292F;
  border-bottom: 1px solid #E5E6EB;
}

.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.12s;
  gap: 12px;

  &:hover {
    background-color: #F7F8FA;
  }

  &:active {
    background-color: #EBF5FF;
  }
}

.notification-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .el-icon {
    width: 16px;
    height: 16px;
  }

  &.success {
    background-color: #E8FFEA;
    .el-icon { color: #00B42A; }
  }

  &.warning {
    background-color: #FFF7E6;
    .el-icon { color: #FF7D00; }
  }

  &.danger {
    background-color: #FFF1F0;
    .el-icon { color: #F53F3F; }
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 13px;
  color: #24292F;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-time {
  font-size: 11px;
  color: #C9CDD4;
}

.dropdown-footer {
  padding: 12px 16px;
  border-top: 1px solid #E5E6EB;
  text-align: center;
}

.view-all {
  font-size: 12px;
  color: #165DFF;
  text-decoration: none;
  transition: color 0.15s;

  &:hover {
    color: #0E42D2;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background-color: #F7F8FA;
  }

  &:active {
    background-color: #E5E6EB;
  }
}

.user-avatar-sm {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 12px;
  flex-shrink: 0;
}

.user-detail {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #24292F;
}

.user-role {
  font-size: 11px;
  color: #86909C;
}

.user-arrow {
  width: 14px;
  height: 14px;
  color: #86909C;
  transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);

  &.open {
    transform: rotate(180deg);
  }
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background-color: #FFFFFF;
  border: 1px solid #E5E6EB;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
  animation: dropdownFadeIn 0.2s cubic-bezier(0.16, 1, 0.3, 1);

  &::before {
    content: '';
    position: absolute;
    top: -6px;
    right: 28px;
    width: 12px;
    height: 12px;
    background-color: #FFFFFF;
    border-top: 1px solid #E5E6EB;
    border-right: 1px solid #E5E6EB;
    transform: rotate(-45deg);
  }
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dropdown-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #F7F8FA;
}

.dropdown-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 18px;
  flex-shrink: 0;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  margin-left: 12px;
  min-width: 0;
}

.dropdown-username {
  font-size: 14px;
  font-weight: 600;
  color: #24292F;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-role {
  font-size: 12px;
  color: #86909C;
  margin-top: 2px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  font-size: 13px;
  color: #4E5969;
  cursor: pointer;
  transition: all 0.12s;
  gap: 10px;

  &:hover {
    background-color: #F7F8FA;
    color: #165DFF;

    .dropdown-icon {
      color: #165DFF;
    }
  }

  &:active {
    background-color: #EBF5FF;
  }

  &.logout:hover {
    background-color: #FFF1F0;
    color: #F53F3F;

    .dropdown-icon {
      color: #F53F3F;
    }
  }
}

.dropdown-icon {
  width: 16px;
  height: 16px;
  color: #86909C;
  transition: color 0.12s;
}

.dropdown-divider {
  height: 1px;
  background-color: #E5E6EB;
  margin: 4px 0;
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  background-color: #FFFFFF;
  padding: 24px;

  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }

  &::-webkit-scrollbar-track {
    background: #F2F3F5;
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #C9CDD4;
    border-radius: 4px;

    &:hover {
      background: #86909C;
    }
  }
}

.footer {
  height: 40px;
  background-color: #FFFFFF;
  border-top: 1px solid #E5E6EB;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #86909C;
  flex-shrink: 0;
}
</style>
